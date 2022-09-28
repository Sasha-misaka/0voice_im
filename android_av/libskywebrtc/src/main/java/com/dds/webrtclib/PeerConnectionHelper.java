package com.dds.webrtclib;


import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dds.webrtclib.bean.MediaType;
import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.cmd.Command;
import com.dds.webrtclib.utils.PermissionUtil;
import com.dds.webrtclib.utils.TimeTool;
import com.dds.webrtclib.utils.Uuid;
import com.dds.webrtclib.ws.ISignalingEvents;
import com.dds.webrtclib.ws.IWebSocket;
import com.dds.webrtclib.ws.JavaWebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStats;
import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.RTCStatsReport;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PeerConnectionHelper implements ISignalingEvents {

    public final static String TAG = "dds_webRtcHelper";

    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int FPS = 10;
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";

    private String preferredVideoCodec = VIDEO_CODEC_H264;
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String LOCAL_STREAM_ID = "ARDAMS";
    private PeerConnectionFactory _factory;
    private MediaStream _localStream = null;
    private VideoTrack _localVideoTrack = null;
    private AudioTrack _localAudioTrack = null;
    private VideoCapturer captureAndroid = null;
    private VideoSource videoSource = null;
    private AudioSource audioSource = null;

    private Map<String, Peer> _connectionPeerDic;

    private IViewCallback viewCallback;
    private int mediaType_;

    private AudioManager mAudioManager;
    private boolean isJoined = false;
    private boolean isJoinRoom = false;

    public void joinRoom(String roomId) {

        executor.execute(() -> {
            if (_localStream == null) {
                createLocalStream();
            }
            // 开始显示
            _webSocket = new JavaWebSocket(this);
            _webSocket.joinRoom(roomId);
            isJoinRoom = true;
        });
    }

    enum Role {Caller, Receiver,}

    private Role _role;

    private IWebSocket _webSocket;

    private Context _context;

    private EglBase _rootEglBase;

    @Nullable
    private SurfaceTextureHelper surfaceTextureHelper;

    private final ExecutorService executor;

    // 用户信息
    private String appId_ = "10000";   //
    private String roomId_ = null;
    private String roomName_ = "room_0001";
    private String userId_ = null;
    private String userName_ = "client_0001";

    private int userType_ = Command.UserType.NORMAL;
    private int talkType_ = Command.TalkType.AUDIO_VIDEO;
    private boolean isAudioOnly = false;
    private int reportStatsInterval = 30000;    // 单位毫秒

    // 本地+远程用户map
    private Map<String, RoomUser> joinedUsers;

    // 保活定时器
    Timer keepLiveTimer_ = null;


    private class RoomUser {
        public RoomUser(String appId, String uid, String uname, int userType, int talkType,
                        PeerConnection.RTCConfiguration rtcConfig) {
            appId_ = appId;
            this.appId = appId;
            this.uid = uid;
            this.uname = uname;
            this.userType = userType;
            this.talkType = talkType;
            this.rtcConfig = rtcConfig;
        }
        public RoomUser() {

        }
        public String appId;
        public String uid;
        public String uname;
        public int userType;
        public int talkType;
        PeerConnection.RTCConfiguration rtcConfig;
    }

    public PeerConnectionHelper(String appId, String roomId, String roomName, String uid, String uname, int mediaType) {
        this.appId_ = appId;
        this.roomId_ = roomId;
        this.roomName_ = roomName;
        this.userId_ = uid;
        this.userName_ = uname;
        this.mediaType_ = mediaType;
        this._connectionPeerDic = new HashMap<>();
        this.joinedUsers = new HashMap<>();

        executor = Executors.newSingleThreadExecutor();

        Command.CommonUserInfo userInfo = Command.getInstance().new CommonUserInfo(appId, "xsffdsfsd",
                roomId_, roomName_,
                userId_, userName_,
                Command.UserType.NORMAL,
                mediaType_ == MediaType.TYPE_VIDEO? Command.TalkType.AUDIO_VIDEO: Command.TalkType.AUDIO_ONLY,
                "android", "android 1.0.0");
        // 初始化Command里面的用户信息
        Command.getInstance().setUserInfo(userInfo);
    }

    public void connect(String roomId) {

    }

    // 设置界面的回调
    public void setViewCallback(IViewCallback callback) {
        viewCallback = callback;
    }

    // ===================================webSocket回调信息=======================================

    public void initContext(Context context, EglBase eglBase) {
        Log.i(TAG, "initContext");
        _context = context;
        _rootEglBase = eglBase;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }



    // 响应join命令
    @Override
    public void onResponseJoinRoom(JSONObject json) {
        if (mediaType_ == MediaType.TYPE_VIDEO) {
            toggleSpeaker(true);
        }
        Log.i(TAG, "onResponseJoinRoom");
        executor.execute(() -> {
            if (_factory == null) {
                _factory = createConnectionFactory();
            }

            if (_localStream == null) {
                createLocalStream();
            }
            try {
                if (json.getInt("result") == 0) {
                    this.roomId_ = json.getString("roomId");
                    this.roomName_ = json.getString("roomName");
                    this.userId_ = json.getString("uid");
                    this.userName_ = json.getString("uname");
                    reportStatsInterval = json.getInt("reportStatsInterval") * 1000;
                    isJoined = true;
                    viewCallback.onJoinComplete(isJoined, userId_, userName_, talkType_, roomId_, roomName_);

                    if(!json.isNull("userList")) {
                        JSONArray roomUserArray = json.getJSONArray("userList");
                        for (int i = 0; i < roomUserArray.length(); ++i) {
                            JSONObject jsonUser = roomUserArray.getJSONObject(i);
                            if (jsonUser.getString("uid") != this.userId_) {
                                RoomUser roomUser = new RoomUser();
                                roomUser.appId = jsonUser.getString("appId");
                                roomUser.uid = jsonUser.getString("uid");
                                roomUser.uname = jsonUser.getString("uname");
                                roomUser.userType = jsonUser.getInt("userType");
                                roomUser.talkType = jsonUser.getInt("talkType");
                                Log.i(TAG, "onResponseJoinRoom room have uname: " + roomUser.uname);
                                this.joinedUsers.put(roomUser.uid, roomUser);
                                viewCallback.onUserJoined(roomUser.uid, roomUser.uname, roomUser.userType, roomUser.talkType);
                            }
                        }
                    }

                    startKeepLive(Command.EcerRTCConstant.KEEPALIVE_INTERVAL);
                } else {
                    isJoined = false;
                    viewCallback.onJoinComplete(isJoined, userId_, userName_, talkType_, roomId_, roomName_);
                }
            } catch (JSONException e) {
                Log.e(TAG, "onResponseJoinRoom JSON parsing error: " + e.toString());
            }
        });

    }
    // 通知有人加入
    @Override
    public void onNotifyNewPeerJoinRoom(JSONObject json) {
        executor.execute(() -> {
            Log.i(TAG, "onNotifyNewPeerJoinRoom");
			if (_localStream == null) {
                createLocalStream();
            }
            try {
                String remoteUserId = json.getString("uid");
                String remoteUserName = json.getString("uname");
                int userType = json.getInt("userType");
                int talkType = json.getInt("talkType");

                PeerConnection.RTCConfiguration rtcConfig =
                        getRTCConfigurationFromPCConfigJSON(json.getString("rtcConfig"));
                // 加入用户列表
                if (!this.joinedUsers.containsKey(remoteUserId)) {
                    RoomUser roomUser = new RoomUser(this.appId_, remoteUserId, remoteUserName, userType,
                            talkType, rtcConfig);
                    Log.i(TAG, "onNotifyNewPeerJoinRoom room have uname: " + roomUser.uname);
                    this.joinedUsers.put(remoteUserId, roomUser);
                }
                // 通知界面xx加入，以增加显示视图

                //
                Peer mPeer = new Peer(userId_, remoteUserId, remoteUserName, rtcConfig);
                RoomUser roomUser = joinedUsers.get(remoteUserId);
                mPeer.talkType = roomUser.talkType;
                _role = Role.Caller;        // 发起者
                mPeer.createOffer(offerOrAnswerConstraint(), false); // onSetSuccess 响应创建的offer


            } catch (JSONException e) {
                Log.e(TAG, "onNotifyNewPeerJoinRoom JSON parsing error: " + e.toString());
            }
        });
    }

    /**
     * 收到leave的响应，则可以关闭退出
     * @param json
     */
    @Override
    public void onResponseLeaveRoom(JSONObject json) {
        executor.execute(() -> {
            // 关闭与对端的连接
            stopAllPeerConnection();
            // 通知完成离开
//            notifyLeaveComplete();
            if (_webSocket != null) {
                _webSocket.close();
                _webSocket = null;
            }
        });
    }
    @Override
    public void onRemoteIceCandidate(JSONObject json) {
        executor.execute(() -> {
            Log.i(TAG, "onRemoteIceCandidate");
            try {
                String remoteUserId = json.getString("uid");
                String remoteUserName = json.getString("uname");
                String candidateString = json.getString("msg");
                IceCandidate iceCandidate = toJavaCandidate(new JSONObject(candidateString));
                Peer peer = _connectionPeerDic.get(remoteUserId);
                if (peer != null) {
                    peer.addIceCandidate(iceCandidate);
                } else {
                    Log.e(TAG, "can't find the peer of id:" + remoteUserId);
                }
            } catch (JSONException e) {
                Log.e(TAG, "onRemoteIceCandidate JSON parsing error: " + e.toString());
            }
        });
    }
    @Override
    public void onRemoteIceCandidateRemove(JSONObject json) {
        // todo 移除
        executor.execute(() -> Log.d(TAG, "send onRemoteIceCandidateRemove"));

    }

    @Override
    public void onReceiveOffer(JSONObject json) {
        Log.i(TAG, "onReceiveOffer");
        executor.execute(() -> {
            if (_localStream == null) {
                createLocalStream();
            }
            try {
                _role = Role.Receiver;  // 接收者
                String remoteUserId = json.getString("uid");
                String remoteUserName = json.getString("uname");
                PeerConnection.RTCConfiguration rtcConfig =
                        getRTCConfigurationFromPCConfigJSON(json.getString("rtcConfig"));
                boolean isIceReset = json.getBoolean("isIceReset");
                if(_connectionPeerDic.get(remoteUserId) != null) {
                    Peer peer = _connectionPeerDic.get(remoteUserId);
                    peer.enableStatsEvent(false, 0);
                    peer.close();
                    _connectionPeerDic.remove(remoteUserId);
                }
                // 加入用户列表
                if (!this.joinedUsers.containsKey(remoteUserId)) {
//                    RoomUser roomUser = new RoomUser(this.appId_, remoteUserId, remoteUserName, userType,
//                            talkType, rtcConfig);
//                    Log.i(TAG, "room have uname: " + roomUser.uname);
//                    this.joinedUsers.put(remoteUserId, roomUser);
                }

                Peer mPeer = _connectionPeerDic.get(remoteUserId);
                if(mPeer == null) {
                    Log.i(TAG, "onReceiveOffer, new Peer " + remoteUserName);
                    mPeer = new Peer(userId_, remoteUserId, remoteUserName, rtcConfig);
                    RoomUser roomUser = joinedUsers.get(remoteUserId);
                    if(roomUser != null)
                    mPeer.talkType = roomUser.talkType;
                    mPeer.pc.addStream(_localStream);
                    _connectionPeerDic.put(remoteUserId, mPeer);
                }
                JSONObject sdpJson = new JSONObject(json.getString("msg"));
                String type = sdpJson.optString("type");
                String sdp = sdpJson.getString("sdp");

                if (mediaType_ == MediaType.TYPE_VIDEO) {
                    sdp = preferCodec(sdp, preferredVideoCodec, false);
                }

                SessionDescription sessionDescription = new SessionDescription(
                        SessionDescription.Type.OFFER, sdp);
                if (mPeer != null) {
                    mPeer.setRemoteDescription(sessionDescription);
                }
            } catch (JSONException e) {
                Log.e(TAG, "onReceiveOffer JSON parsing error: " + e.toString());
            }

        });

    }

    @Override
    public void onReceiverAnswer(JSONObject json) {
        Log.i(TAG, "onReceiverAnswer");
        executor.execute(() -> {
            try {
                String remoteUserId = json.getString("uid");
                String remoteUserName = json.getString("uname");

                Peer mPeer = _connectionPeerDic.get(remoteUserId);

                JSONObject sdpJson = new JSONObject(json.getString("msg"));
                String type = sdpJson.optString("type");
                String sdp = sdpJson.getString("sdp");

                if (mediaType_ == MediaType.TYPE_VIDEO) {
                    sdp = preferCodec(sdp, preferredVideoCodec, false);
                }

                SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER,
                        sdp);
                if (mPeer != null) {
                    mPeer.setRemoteDescription(sessionDescription);
                }
            } catch (JSONException e) {
                Log.e(TAG, "onReceiverAnswer JSON parsing error: " + e.toString());
            }
        });

    }
    @Override
    public void onRemoteLeaveRoom(JSONObject json) {
        Log.i(TAG, "onRemoteLeaveRoom");
        executor.execute(() -> closePeerConnection(json));
    }

    @Override
    public void onResponseKeepLive(JSONObject json) {
//        Log.i(TAG, "onResponseKeepLive");
        executor.execute(() -> {
            try {
                int ret = json.getInt("result");
                // 如果长时间没收到回应，则需要考虑进行websocket重连服务器

            } catch (JSONException e) {
                Log.e(TAG, "onResponseKeepLive JSON parsing error: " + e.toString());
            }
        });
    }

    @Override
    public void onRemoteTurnTalkType(JSONObject json) {
        Log.i(TAG, "onRemoteTurnTalkType");
        executor.execute(() -> {
            try {
                String uid = json.getString("uid");
                String userName = json.getString("uname");
                int index = json.getInt("index");
                boolean enable = json.getBoolean("enable");
                viewCallback.onTurnTalkType(uid, userName, index, enable);
            } catch (JSONException e) {
                Log.e(TAG, "onRemoteTurnTalkType JSON parsing error: " + e.toString());
            }
        });
    }

    private PeerConnectionFactory createConnectionFactory() {
        Log.i(TAG, "createConnectionFactory");
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(_context)
                        .createInitializationOptions());

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                _rootEglBase.getEglBaseContext(),
                true,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(_rootEglBase.getEglBaseContext());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        return PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(JavaAudioDeviceModule.builder(_context).createAudioDeviceModule())
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    // 创建本地流
    private void createLocalStream() {
        if (_factory == null) {
            _factory = createConnectionFactory();
        }
        long time1 = System.currentTimeMillis();
        Log.i(TAG, "createLocalStream");
        _localStream = _factory.createLocalMediaStream(LOCAL_STREAM_ID);
        // 音频
        audioSource = _factory.createAudioSource(createAudioConstraints());
        _localAudioTrack = _factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        _localStream.addTrack(_localAudioTrack);

        if (mediaType_ == MediaType.TYPE_VIDEO) {
            //创建需要传入设备的名称
            captureAndroid = createVideoCapture();
            // 视频
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", _rootEglBase.getEglBaseContext());
            videoSource = _factory.createVideoSource(captureAndroid.isScreencast());
            if (mediaType_ == MediaType.TYPE_VIDEO) {
                 videoSource.adaptOutputFormat(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
            }
            captureAndroid.initialize(surfaceTextureHelper, _context, videoSource.getCapturerObserver());
            captureAndroid.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
            _localVideoTrack = _factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
            _localStream.addTrack(_localVideoTrack);
        }

        long time2 = System.currentTimeMillis();
        if (viewCallback != null) {
            viewCallback.onSetLocalStream(_localStream, userId_);
        }
        long time3 = System.currentTimeMillis();
        Log.i(TAG, "createLocalMediaStream time = " + (time2-time1));
        Log.i(TAG, "onSetLocalStream       time = " + (time3-time2));
    }

    // 关闭通道流
    private void closePeerConnection(JSONObject json) {
        Log.i(TAG, "closePeerConnection");
        try {
            String remoteUserId = json.getString("uid");
            String remoteUserName = json.getString("uname");
            Peer mPeer = _connectionPeerDic.get(remoteUserId);
            if (mPeer != null) {
                mPeer.pc.close();
            }
            _connectionPeerDic.remove(remoteUserId);
            joinedUsers.remove(remoteUserId);
            if (viewCallback != null) {
                viewCallback.onCloseWithId(remoteUserId);
            }
        } catch (Exception e) {
            Log.e(TAG, "closePeerConnection JSON parsing error: " + e.toString());
        }
        // 关闭本地显示

    }

    // 关闭所有的通道
    private void closeAllPeerConnection() {
        for (String key : _connectionPeerDic.keySet()) {
            Peer mPeer = _connectionPeerDic.get(key);
            mPeer.close();
        }
        _connectionPeerDic.clear();
    }


    //**************************************逻辑控制**************************************
    // 调整摄像头前置后置
    public void switchCamera() {
        if (captureAndroid == null) return;
        if (captureAndroid instanceof CameraVideoCapturer) {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) captureAndroid;
            cameraVideoCapturer.switchCamera(null);
        } else {
            Log.d(TAG, "Will not switch camera, video caputurer is not a camera");
        }

    }

    // 设置自己静音
    public void toggleMute(boolean enable) {
        Log.d(TAG, "toggleMute " + enable);
        if (_localAudioTrack != null) {
            _localAudioTrack.setEnabled(enable);
        }
        if (enable)
            Log.i(TAG, "打开本地麦克风");
        else
            Log.i(TAG, "关闭本地麦克风");
        _webSocket.sendTurnTalkType(1, enable);
    }

    public void toggleCamera(boolean enable) {
        if (_localVideoTrack != null) {
            _localVideoTrack.setEnabled(enable);
        }
        if (enable)
            Log.i(TAG, "打开本地摄像头");
        else
            Log.i(TAG, "关闭本地摄像头");
        _webSocket.sendTurnTalkType(0, enable);
    }


    public void toggleSpeaker(boolean enable) {
        Log.d(TAG, "toggleSpeaker " + enable);
        if (mAudioManager != null) {
            mAudioManager.setSpeakerphoneOn(enable);
        }
    }

    // 退出房间
    public void exitRoom() {
        if(!isJoinRoom) {
            Log.e(TAG,"no join room");
            return;
        }
        Log.d(TAG, "exitRoom");
        if (viewCallback != null) {
            viewCallback = null;
        }
        executor.execute(() -> {
            ArrayList myCopy;
            // 关闭保活信息
            stopKeepLive();
            // 关闭统计信息
            stopAllStats();

            // 退出房间
            if(_webSocket != null)
                _webSocket.exitRoom();
            // 关闭所有的PeerConnection
            closeAllPeerConnection();

            if (audioSource != null) {
                audioSource.dispose();
                audioSource = null;
            }

            if (videoSource != null) {
                videoSource.dispose();
                videoSource = null;
            }

            if (captureAndroid != null) {
                try {
                    captureAndroid.stopCapture();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                captureAndroid.dispose();
                captureAndroid = null;
            }

            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.dispose();
                surfaceTextureHelper = null;
            }


            if (_factory != null) {
                _factory.dispose();
                _factory = null;
            }
        });
    }


    private VideoCapturer createVideoCapture() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapture(new Camera2Enumerator(_context));
        } else {
            videoCapturer = createCameraCapture(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapture(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(_context);
    }


    //**************************************各种约束******************************************/
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";

    private MediaConstraints createAudioConstraints() {
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
        return audioConstraints;
    }

    private MediaConstraints offerOrAnswerConstraint() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        ArrayList<MediaConstraints.KeyValuePair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        keyValuePairs.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", String.valueOf(mediaType_ == MediaType.TYPE_VIDEO)));
        mediaConstraints.mandatory.addAll(keyValuePairs);
        return mediaConstraints;
    }

    // Converts a JSON candidate to a Java object.
    private IceCandidate toJavaCandidate(JSONObject json) throws JSONException {
        return new IceCandidate(
                json.getString("id"), json.getInt("label"), json.getString("candidate"));
    }


    //**************************************内部类******************************************/
    private class Peer implements SdpObserver, PeerConnection.Observer {


        public void createOffer(MediaConstraints mediaConstraints, boolean isIceReset) {
            if(!isIceReset) {
                // 先清除自己
                if(_connectionPeerDic.get(remoteUserId) != null) {
                    this.enableStatsEvent(false, 0);
                    this.close();
                }
            }
            pc.addStream(_localStream);
            _connectionPeerDic.put(remoteUserId, this);
            setInitiator(true);
            this.isSendPeerConnectedResult = false;
            pc.createOffer(this, mediaConstraints);
        }

        public void addIceCandidate(IceCandidate iceCandidate) {
            this.pc.addIceCandidate(iceCandidate);
        }


        public class StatsInit {
            int     audSendPacketsLost  = 0; // audio发送丢包数
            int     audSendPacketsSent  = 0; // audio发送包数
            int     audSendBytes        = 0;   // audio发送字节数
            long    audSendTimeStamp    = 0;   // 发送的时间戳

            int     audRecvPacketsLost  = 0; // audio接收丢包数
            int     audRecvPacketsReceived  = 0; // audio接收包数
            int     audRecvBytes        = 0;   // audio接收字节数
            long    audRecvTimeStamp    = 0;   // 接收的时间戳

            int     vidSendPacketsLost  = 0; // video发送丢包数
            int     vidSendPacketsSent  = 0; // video发送包数
            int     vidSendBytes        = 0;   // video发送字节数
            long    vidSendTimeStamp    = 0;   // 发送的时间戳

            int     vidRecvPacketsLost  = 0; // video接收丢包数
            int     vidRecvPacketsReceived  = 0; // video接收包数
            int     vidRecvBytes        = 0;   // audio接收字节数
            long    vidRecvTimeStamp    = 0;   // 接收的时间戳
        }

        public class statResult {
            int     audSendPacketsLost  = 0; // audio发送丢包数
            int     audSendPacketsSent  = 0; // audio发送包数
            int     audSendBytes        = 0;   // audio发送字节数
            String  audSendCodecName    = "";   // 编码器
            String  audSendPacketsLostRate = "0"; // 丢包率
            int  audSendBitRate      = 0;    // 发送的码率


            int     audRecvPacketsLost  = 0; // audio接收丢包数
            int     audRecvPacketsReceived  = 0; // audio接收包数
            int     audRecvBytes        = 0;   // audio接收字节数
            String  audRecvCodecName    = "";   // 编码器
            String  audRecvPacketsLostRate = "0"; // 丢包率
            int  audRecvBitRate      = 0;    // 收到的码率


            int     vidSendPacketsLost  = 0; // video发送丢包数
            int     vidSendPacketsSent  = 0; // video发送包数
            int     vidSendBytes        = 0;   // video发送字节数
            int     vidSendFrameRateInput = 0; // 输入帧率
            int     vidSendFrameRateSent = 0; // 实际发送的帧率
            int     vidSendWidth = 0;
            int     vidSendHeight= 0;
            String  vidSendCodecName = "";      // 编码器
            String  vidSendPacketsLostRate = "0"; // 丢包率
            int  vidSendBitRate = 0;         // 发送的码率

            int     vidRecvPacketsLost  = 0; // video接收丢包数
            int     vidRecvPacketsReceived  = 0; // video接收包数
            int     vidRecvBytes        = 0;   // audio接收字节数
            int     vidRecvFrameRateReceived = 0; // 收到的帧率
            int     vidRecvFrameRateOutput = 0;   // 对方发送的实际帧率
            int     vidRecvWidth = 0;
            int     vidRecvHeight = 0;
            String  vidRecvCodecName = "";   // 编码器
            String  vidRecvPacketsLostRate = "0";// 丢包率
            int  vidRecvBitRate = 0;  // 收到的码率

            // 视频带宽相关信息
            String     targetEncBitrate = "0";     // 视频编码器的目标比特率。
            String     actualEncBitrate = "0"; // 视频编码器实际编码的码率，通常这与目标码率是匹配的
            String     availableSendBandwidth = "0";   // 视频数据发送可用的带宽。
            String     availableReceiveBandwidth = "0";    //  视频数据接收可用的带宽。
            String     retransmitBitrate = "0";    // 如果RTX被使用的话，表示重传的码率
            String     transmitBitrate = "0"; // 实际发送的码率，如果此数值与googActualEncBitrate有较大的出入，可能是fec的影响。

            String localPortNumber = "0";
            String localNetworkType = "";
            String localIpAddress = "";
            String localTransport = "";
            String localCandidateType = "Unknown";
        }
        private PeerConnection pc = null;
        private String localUserId;     // 本地用户id
        private String remoteUserId;    // 远程用户id
        private String remoteUserName;  // 远程用户名
        private int userType;
        private int talkType;
        private SessionDescription localSdp;
        private Timer statsTimer = null;
        private StatsInit statInit = null;
        private statResult statResult = null;
        private long reportStatsStartTime = 0;
        private boolean isSendPeerConnectedResult = false;
        private boolean initiator = false;
        public Peer(String localUserId,
                    String remoteUserId,
                    String remoteUserName,
                    PeerConnection.RTCConfiguration rtcConfig) {
            this.pc = createPeerConnection(rtcConfig);
            this.pc.setBitrate(100*1024, 200*1024, 300*1024);
            this.localUserId = localUserId;
            this.remoteUserId = remoteUserId;
            statInit = new StatsInit();
            statResult = new statResult();
            enableStatsEvent(true, Command.EcerRTCConstant.GETSTATSREPORT_INTERVAL);
        }

        public void setInitiator(boolean b) {
            this.initiator = b;
        }

        public void addStream(MediaStream stream) {
            this.pc.addStream(stream);
        }

        public void close () {
            this.pc.close();
        }
        public void createAnswer() {
            pc.createAnswer(Peer.this, offerOrAnswerConstraint());
        }
        public void setRemoteDescription(final SessionDescription sdp) {

            String sdpDescription = sdp.description;
//                    if (preferIsac) {
//                        sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
//                    }
            if (mediaType_ == MediaType.TYPE_VIDEO) {
                sdpDescription = preferCodec(sdpDescription, preferredVideoCodec, false);
            }
//                    if (peerConnectionParameters.audioStartBitrate > 0) {
//                        sdpDescription = setStartBitrate(
//                                AUDIO_CODEC_OPUS, false, sdpDescription, peerConnectionParameters.audioStartBitrate);
//                    }
            Log.d(TAG, "Set remote SDP.");
            SessionDescription sdpRemote = new SessionDescription(sdp.type, sdpDescription);
            // 设置本地和远程 Session Description Protocol (SDP) ，这两个接口配置我们在通信时的相关参数。
            this.pc.setRemoteDescription(this, sdpRemote);    // 这是用来发送给对端？
        }


        //****************************PeerConnection.Observer****************************/
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i(TAG, "onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.i(TAG, "onIceConnectionChange: " + iceConnectionState.toString());

            if(iceConnectionState.toString().equals("failed"))
            if (_webSocket.getNetState() == NetState.NET_CONNECTED) { // ws连接可用
                if (this.initiator) {
                    Log.i(TAG, "oniceconnectionstatechange createOffer");
                    createOffer(offerOrAnswerConstraint(), true);   // 重新连接
                } else {
                    Log.i(TAG, "oniceconnectionstatechange wait caller restart ice");
                }
            }
        }

        @Override
        public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
            Log.i(TAG, "onConnectionChange: " + newState.toString());
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.i(TAG, "onIceConnectionReceivingChange:" + b);

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.i(TAG, "onIceGatheringChange:" + iceGatheringState.toString());

        }


        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            // 发送IceCandidate
            String desc = toJsonCandidate(iceCandidate);
            _webSocket.sendIceCandidate(desc, remoteUserId);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            Log.i(TAG, "onIceCandidatesRemoved:");
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            if (viewCallback != null) {
                viewCallback.onAddRemoteStream(mediaStream, remoteUserId, remoteUserName, talkType);
            }
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            if (viewCallback != null) {
                viewCallback.onCloseWithId(remoteUserId);
            }
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
            Log.i(TAG, "onAddTrack kind = " + receiver.track().kind());
//             if (viewCallback != null) {
//                if(receiver.track().kind() == "video")
//                    viewCallback.onAddRemoteStream(receiver.track(), remoteUserId, remoteUserName, talkType);
//            }
        }

        @Override
        public void onTrack(RtpTransceiver transceiver) {

        }

        private void jsonPut(JSONObject json, String key, Object value) {
            try {
                json.put(key, value);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        private String toJsonSessionDescription(SessionDescription sdp) {
            Log.i(TAG, "toJsonSessionDescription");
            JSONObject json = new JSONObject();

            String type = "offer";
            if (sdp.type == SessionDescription.Type.OFFER) {
                type = "offer";
            } else if (sdp.type == SessionDescription.Type.ANSWER){
                type = "answer";
            } else if (sdp.type == SessionDescription.Type.PRANSWER){
                type = "pranswer";
            } else {
                type = "unkown";
                Log.e(TAG, "toJsonSessionDescription failed: unknown the sdp type");
            }
            String sdpDescription = sdp.description;
            sdpDescription =  changeStreamId(sdpDescription, _localStream.getId(), userId_.toString());

            jsonPut(json, "sdp", sdpDescription);
            jsonPut(json, "type", type);

            return json.toString();
        }

        private String toJsonCandidate(IceCandidate candidate) {
            JSONObject json = new JSONObject();

            jsonPut(json, "id", candidate.sdpMid);
            jsonPut(json, "label", candidate.sdpMLineIndex);
            jsonPut(json, "candidate", candidate.sdp);

            return json.toString();
        }



        //****************************SdpObserver****************************/

        @Override
        public void onCreateSuccess(SessionDescription origSdp) {
            Log.v(TAG, "sdp创建成功       " + origSdp.type);
            //设置本地的SDP
//            if(origSdp.type == SessionDescription.Type.OFFER)
            {
                String sdpDescription = origSdp.description;
                if (mediaType_ == MediaType.TYPE_VIDEO) {
                    sdpDescription = preferCodec(sdpDescription, preferredVideoCodec, false);
                }

                localSdp = new SessionDescription(origSdp.type, sdpDescription);
                pc.setLocalDescription(Peer.this, localSdp);
            }

        }

        @Override
        public void onSetSuccess() {
            Log.v(TAG, "sdp连接成功        " + pc.signalingState().toString() + ", _role = " + _role.toString());

            if (pc.signalingState() == PeerConnection.SignalingState.HAVE_REMOTE_OFFER) {
                pc.createAnswer(Peer.this, offerOrAnswerConstraint());
            } else if (pc.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                //判断连接状态为本地发送offer
                if (_role == Role.Receiver) {
                    //接收者，发送Answer
                    String desc = toJsonSessionDescription(pc.getLocalDescription());
                    _webSocket.sendAnswer(desc, remoteUserId);

                } else if (_role == Role.Caller) {
                    //发送者,发送自己的offer
                    String desc = toJsonSessionDescription(pc.getLocalDescription());
                    _webSocket.sendOffer(desc, remoteUserId);
                }
            } else if (pc.signalingState() == PeerConnection.SignalingState.STABLE) {
                // Stable 稳定的
                if (_role == Role.Receiver) {
                    String desc = toJsonSessionDescription(pc.getLocalDescription());
                    _webSocket.sendAnswer(desc, remoteUserId);
                }
            }

        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(TAG, "onCreateFailure failed:" + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.e(TAG, "onSetFailure failed:" + s);
        }


        //初始化 RTCPeerConnection 连接管道
        private PeerConnection createPeerConnection(PeerConnection.RTCConfiguration rtcConfig) {
			if (_factory == null) {
                _factory = createConnectionFactory();
            }
            // 管道连接抽象类实现方法
            return _factory.createPeerConnection(rtcConfig, this);
        }

        public void enableStatsEvent(boolean enable, int periodMs) {
            if(statsTimer == null) {
                statsTimer = new Timer();
            } else {
                statsTimer.cancel();
            }

            if (enable) {
                try {
                    statsTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    getStats();
                                }
                            });
                        }
                    }, periodMs, periodMs);
                } catch (Exception e) {
                    Log.e(TAG, "Can not schedule statistics timer", e);
                }
            } else {
                statsTimer.cancel();
            }
        }
        private Map<String, String> getReportMap(StatsReport report) {
            Map<String, String> reportMap = new HashMap<String, String>();
            for (StatsReport.Value value : report.values) {
                reportMap.put(value.name, value.value);
            }
            return reportMap;
        }
        private void getStats() {
            if (pc == null) {
                return;
            }
            // 虽然该接口被Deprecated，但这里还是他能够正常获取统计信息
            boolean success = pc.getStats(new StatsObserver() {
                @Override
                public void onComplete(final StatsReport[] reports) {
                    for (StatsReport report : reports) {
                        try {
                            long curTime = (long)report.timestamp;
                            if (report.type.equals("ssrc") && report.id.contains("ssrc") && report.id.contains("send")) {
                                // Send video statistics.
                                Map<String, String> reportMap = getReportMap(report);
                                String trackId = reportMap.get("googTrackId");
                                if (reportMap.get("mediaType").equals("audio")) {
                                    long timeStamp = curTime;//TimeTool.getCurrentTimeMillis();
                                    long duration = Command.EcerRTCConstant.GETSTATSREPORT_INTERVAL;
                                    if (statInit.audSendTimeStamp > 0 && statInit.audSendTimeStamp < timeStamp) {
                                        duration = timeStamp - statInit.audSendTimeStamp;
                                    }
                                    statInit.audSendTimeStamp = timeStamp;  // 保存时间点
                                    int statValue = 0;
                                    statValue = 0; //Integer.parseInt(reportMap.get("packetsLost"));
                                    int lostPackets = statValue - statInit.audSendPacketsLost; // 丢失的包数量
                                    statInit.audSendPacketsLost = statValue;
                                    statResult.audSendPacketsLost = statValue;
                                    statValue = Integer.parseInt(reportMap.get("packetsSent"));
                                    int sendPackets = statValue - statInit.audSendPacketsSent;
                                    statInit.audSendPacketsSent = statValue;
                                    statResult.audSendPacketsSent = statValue;
                                    // 计算丢包率
                                    if (sendPackets > 0) {
                                        float lostRate = (lostPackets / sendPackets);
                                        statResult.audSendPacketsLostRate = String.format("%.2f", lostRate);
                                    }
                                    // 计算码率
                                    statValue = Integer.parseInt(reportMap.get("bytesSent"));
                                    int sendBytes = statValue - statInit.audSendBytes;  // 这段时间内发送字节数量
                                    statInit.audSendBytes = statValue;
                                    statResult.audSendBytes = statValue;
                                    if (sendBytes > 0) {
                                        statResult.audSendBitRate = (int)(sendBytes * 8 * 1000 / duration / 1024);
                                    }
                                    //获取编码器类型
                                    statResult.audSendCodecName = reportMap.get("googCodecName");
                                } else if (reportMap.get("mediaType").equals("video")) {
                                    long timeStamp = curTime;//TimeTool.getCurrentTimeMillis();
                                    long duration = Command.EcerRTCConstant.GETSTATSREPORT_INTERVAL;
                                    if (statInit.vidSendTimeStamp > 0 && statInit.vidSendTimeStamp < timeStamp) {
                                        duration = timeStamp - statInit.vidSendTimeStamp;
                                    }
                                    statInit.vidSendTimeStamp = timeStamp;  // 保存时间点
                                    int statValue = 0;
                                    statValue = Integer.parseInt(reportMap.get("packetsLost"));
                                    int lostPackets = statValue - statInit.vidSendPacketsLost; // 丢失的包数量
                                    statInit.vidSendPacketsLost = statValue;
                                    statResult.vidSendPacketsLost = statValue;
                                    statValue = Integer.parseInt(reportMap.get("packetsSent"));
                                    int sendPackets = statValue - statInit.vidSendPacketsSent;
                                    statInit.vidSendPacketsSent = statValue;
                                    statResult.vidSendPacketsSent = statValue;
                                    // 计算丢包率
                                    if (sendPackets > 0) {
                                        float lostRate = (lostPackets / sendPackets);
                                        statResult.vidSendPacketsLostRate = String.format("%.2f", lostRate);
                                    }
                                    // 计算码率
                                    statValue = Integer.parseInt(reportMap.get("bytesSent"));
                                    int sendBytes = statValue - statInit.vidSendBytes;  // 这段时间内发送字节数量
                                    statInit.vidSendBytes = statValue;
                                    statResult.vidSendBytes = statValue;
                                    if (sendBytes > 0) {
                                        statResult.vidSendBitRate = (int)(sendBytes * 8 * 1000 / duration / 1024);
                                    }
                                    //获取编码器类型
                                    statResult.vidSendCodecName = reportMap.get("googCodecName");
                                    // 输入帧率
                                    statResult.vidSendFrameRateInput = Integer.parseInt(reportMap.get("googFrameRateInput"));
                                    // 实际发送的帧率
                                    statResult.vidSendFrameRateSent = Integer.parseInt(reportMap.get("googFrameRateSent"));
                                    // 分辨率
                                    statResult.vidSendWidth = Integer.parseInt(reportMap.get("googFrameWidthSent"));
                                    statResult.vidSendHeight = Integer.parseInt(reportMap.get("googFrameHeightSent"));
                                }

                            } else if (report.type.equals("ssrc") && report.id.contains("ssrc")
                                    && report.id.contains("recv")) {
                                // Receive video statistics.
                                Map<String, String> reportMap = getReportMap(report);
                                // Check if this stat is for video track.
                                String frameWidth = reportMap.get("googFrameWidthReceived");
                                if (reportMap.get("mediaType").equals("audio")) {
                                    long timeStamp = curTime;//TimeTool.getCurrentTimeMillis();
                                    long duration = Command.EcerRTCConstant.GETSTATSREPORT_INTERVAL;
                                    if (statInit.audRecvTimeStamp > 0 && statInit.audRecvTimeStamp < timeStamp) {
                                        duration = timeStamp - statInit.audRecvTimeStamp;
                                    }
                                    statInit.audRecvTimeStamp = timeStamp;  // 保存时间点
                                    int statValue = 0;
                                    statValue = Integer.parseInt(reportMap.get("packetsLost"));
                                    int lostPackets = statValue - statInit.audRecvPacketsLost; // 丢失的包数量
                                    statInit.audRecvPacketsLost = statValue;
                                    statResult.audRecvPacketsLost = statValue;
                                    statValue = Integer.parseInt(reportMap.get("packetsReceived"));
                                    int recvPackets = statValue - statInit.audRecvPacketsReceived;
                                    statInit.audRecvPacketsReceived = statValue;
                                    statResult.audRecvPacketsReceived = statValue;
                                    // 计算丢包率
                                    if (recvPackets > 0) {
                                        float lostRate = (lostPackets / recvPackets);
                                        statResult.audRecvPacketsLostRate = String.format("%.2f", lostRate);
                                    }
                                    // 计算码率
                                    statValue = Integer.parseInt(reportMap.get("bytesReceived"));
                                    int recvBytes = statValue - statInit.audRecvBytes;  // 这段时间内发送字节数量
                                    statInit.audRecvBytes = statValue;
                                    statResult.audRecvBytes = statValue;
                                    if (recvBytes > 0) {
                                        statResult.audRecvBitRate = (int)(recvBytes * 8 * 1000 / duration / 1024);
                                    }
                                    //获取编码器类型
                                    statResult.audRecvCodecName = reportMap.get("googCodecName");
                                } else if (reportMap.get("mediaType").equals("video")) {
                                    long timeStamp = curTime;//TimeTool.getCurrentTimeMillis();
                                    long duration = Command.EcerRTCConstant.GETSTATSREPORT_INTERVAL;
                                    if (statInit.vidRecvTimeStamp > 0 && statInit.vidRecvTimeStamp < timeStamp) {
                                        duration = timeStamp - statInit.vidRecvTimeStamp;
                                    }
                                    statInit.vidRecvTimeStamp = timeStamp;  // 保存时间点
                                    int statValue = 0;
                                    statValue = Integer.parseInt(reportMap.get("packetsLost"));
                                    int lostPackets = statValue - statInit.vidRecvPacketsLost; // 丢失的包数量
                                    statInit.vidRecvPacketsLost = statValue;
                                    statResult.vidRecvPacketsLost = statValue;
                                    statValue = Integer.parseInt(reportMap.get("packetsReceived"));
                                    int recvPackets = statValue - statInit.vidRecvPacketsReceived;
                                    statInit.vidRecvPacketsReceived = statValue;
                                    statResult.vidRecvPacketsReceived = statValue;
                                    // 计算丢包率
                                    if (recvPackets > 0) {
                                        float lostRate = (lostPackets / recvPackets);
                                        statResult.vidRecvPacketsLostRate = String.format("%.2f", lostRate);
                                    }
                                    // 计算码率
                                    statValue = Integer.parseInt(reportMap.get("bytesReceived"));
                                    int recvBytes = statValue - statInit.vidRecvBytes;  // 这段时间内发送字节数量
                                    statInit.vidRecvBytes = statValue;
                                    statResult.vidRecvBytes = statValue;
                                    if (recvBytes > 0) {
                                        statResult.vidRecvBitRate = (int)(recvBytes * 8 * 1000 / duration / 1024);
                                    }
                                    //获取编码器类型
                                    statResult.vidRecvCodecName = reportMap.get("googCodecName");
                                    // 输入帧率
                                    statResult.vidRecvFrameRateOutput = Integer.parseInt(reportMap.get("googFrameRateOutput"));
                                    // 实际发送的帧率
                                    statResult.vidRecvFrameRateReceived = Integer.parseInt(reportMap.get("googFrameRateReceived"));
                                    // 分辨率
                                    statResult.vidRecvWidth = Integer.parseInt(reportMap.get("googFrameWidthReceived"));
                                    statResult.vidRecvHeight = Integer.parseInt(reportMap.get("googFrameHeightReceived"));
                                }
                            } else if (report.type.equals("VideoBwe")) {
                                // BWE statistics.
                                Map<String, String> reportMap = getReportMap(report);
                                statResult.targetEncBitrate = reportMap.get("googTargetEncBitrate");
                                statResult.actualEncBitrate = reportMap.get("googActualEncBitrate");
                                statResult.availableSendBandwidth = reportMap.get("googAvailableSendBandwidth");
                                statResult.availableReceiveBandwidth = reportMap.get("googAvailableReceiveBandwidth");
                                statResult.retransmitBitrate = reportMap.get("googRetransmitBitrate");
                                statResult.transmitBitrate = reportMap.get("googTransmitBitrate");
                            } else if (report.type.equals("localcandidate")) {
                                // Connection statistics.
                                Map<String, String> reportMap = getReportMap(report);
                                statResult.localPortNumber = reportMap.get("portNumber");
                                statResult.localNetworkType = reportMap.get("networkType");
                                statResult.localIpAddress = reportMap.get("ipAddress");
                                statResult.localTransport = reportMap.get("transport");
                                statResult.localCandidateType = reportMap.get("candidateType");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Can not parse ", e);
                        }
                    }
                    if(!isSendPeerConnectedResult
                            && (statResult.audSendBytes > 0
                            || statResult.vidSendBytes > 0)) {
                        String connectType = "Unknown";
                        if (statResult.localCandidateType.equals("relayed")
                                || statResult.localCandidateType.equals("relay")) {
                            connectType = "TURN";
                        } else if (statResult.localCandidateType.equals("all")
                                || statResult.localCandidateType.equals("host")) {
                            connectType = "STUN";
                        }
                        if(_webSocket.sendPeerConnected(remoteUserId, connectType) == 0) {
                            isSendPeerConnectedResult = true;
                        }
                    }

                    //发送统计结果
                    if (reportStatsStartTime == 0) {
                        reportStatsStartTime = TimeTool.getCurrentTimeMillis();
                    } else if (TimeTool.getCurrentTimeMillis() - reportStatsStartTime > reportStatsInterval) {
                        reportStatsStartTime =TimeTool.getCurrentTimeMillis();
                        // 报告信息, 封装好直接发送
                        _webSocket.sendStats(packReportStats());
                    }
                }
            }, null);

//            pc.getStats(new RTCStatsCollectorCallback() {
//                @Override
//                public void onStatsDelivered(RTCStatsReport report) {
//                    Log.i(TAG, "stats" + report.toString());
//                    // 打印
//                    Map<String, RTCStats> stats = report.getStatsMap();
//                    Log.i(TAG, "" + stats.get("codec"));
//                    RTCStats stat = stats.get("inbound-rtp");
//                    stat = stats.get("outbound-rtp");
//                    stat = stats.get("outbound-rtp");
//                    stat = stats.get("local-candidate");
//                    stat = stats.get("remote-candidate");
//                }
//            });
        }

        String packReportStats() {
            JSONObject json = new JSONObject();
            jsonPut(json, "cmd", Command.SignalType.REPORT_STATS);
            jsonPut(json, "appId", appId_);
            jsonPut(json, "roomId", roomId_);
            jsonPut(json, "uid", this.localUserId);
            jsonPut(json, "remoteUid", this.remoteUserId);

            // 封装audio
            JSONObject audJson = new JSONObject();
            // 先封装send
            JSONObject audSendJson = new JSONObject();
            jsonPut(audSendJson, "packetsLostRate", this.statResult.audSendPacketsLostRate);
            jsonPut(audSendJson, "bitRate", this.statResult.audSendBitRate);
            // 然后封装recv
            JSONObject audRecvJson = new JSONObject();
            jsonPut(audRecvJson, "packetsLostRate", this.statResult.audRecvPacketsLostRate);
            jsonPut(audRecvJson, "bitRate", this.statResult.audRecvBitRate);
            // 加入到audio
            jsonPut(audJson, "send", audSendJson);
            jsonPut(audJson, "recv", audRecvJson);

            // 封装video
            JSONObject vidJson = new JSONObject();
            // 先封装send
            JSONObject vidSendJson = new JSONObject();
            jsonPut(vidSendJson, "packetsLostRate", this.statResult.vidSendPacketsLostRate);
            jsonPut(vidSendJson, "bitRate", this.statResult.vidSendBitRate);
            jsonPut(vidSendJson, "frameRateSent", this.statResult.vidSendFrameRateSent);
            jsonPut(vidSendJson, "width", this.statResult.vidSendWidth);
            jsonPut(vidSendJson, "height", this.statResult.vidSendHeight);
            jsonPut(vidSendJson, "codecName", this.statResult.vidSendCodecName);

            // 然后封装recv
            JSONObject vidRecvJson = new JSONObject();
            jsonPut(vidRecvJson, "packetsLostRate", this.statResult.vidRecvPacketsLostRate);
            jsonPut(vidRecvJson, "bitRate", this.statResult.audRecvBitRate);
            jsonPut(vidRecvJson, "frameRateRecv", this.statResult.vidRecvFrameRateReceived);
            jsonPut(vidRecvJson, "width", this.statResult.vidRecvWidth);
            jsonPut(vidRecvJson, "height", this.statResult.vidRecvHeight);
            jsonPut(vidRecvJson, "codecName", this.statResult.vidRecvCodecName);

            // 加入到video
            jsonPut(vidJson, "send", vidSendJson);
            jsonPut(vidJson, "recv", vidRecvJson);

            // 加入audio、video
            jsonPut(json, "audio", audJson);
            jsonPut(json, "video", vidJson);
            jsonPut(json, "time", TimeTool.getTime());

            return json.toString().replaceAll("\\\\","");
        }

    }


    // ===================================替换编码方式=========================================
    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        return sdpDescription;
//        final String[] lines = sdpDescription.split("\r\n");
//        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
//        if (mLineIndex == -1) {
//            Log.w(TAG, "No mediaDescription line, so can't prefer " + codec);
//            return sdpDescription;
//        }
//        // A list with all the payload types with name |codec|. The payload types are integers in the
//        // range 96-127, but they are stored as strings here.
//        final List<String> codecPayloadTypes = new ArrayList<>();
//        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
//        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
//        for (String line : lines) {
//            Matcher codecMatcher = codecPattern.matcher(line);
//            if (codecMatcher.matches()) {
//                codecPayloadTypes.add(codecMatcher.group(1));
//            }
//        }
//        if (codecPayloadTypes.isEmpty()) {
//            Log.w(TAG, "No payload types with name " + codec);
//            return sdpDescription;
//        }
//
//        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
//        if (newMLine == null) {
//            return sdpDescription;
//        }
//        Log.d(TAG, "Change media description from: " + lines[mLineIndex] + " to " + newMLine);
//        lines[mLineIndex] = newMLine;
//        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }

    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        final String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; ++i) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static @Nullable
    String movePayloadTypesToFront(
            List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            Log.e(TAG, "Wrong SDP media description format: " + mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
                new ArrayList<>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }

    private static String joinString(
            Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }
    private PeerConnection.RTCConfiguration getRTCConfigurationFromPCConfigJSON(String rtcConfig)
            throws JSONException {
        Log.i(TAG, "rtcConfig:" + rtcConfig);

        LinkedList<PeerConnection.IceServer> turnServers = new LinkedList<PeerConnection.IceServer>();
        JSONObject rtcConfigJSON = new JSONObject(rtcConfig);
        JSONArray iceServers = rtcConfigJSON.getJSONArray("iceServers");
        for (int i = 0; i < iceServers.length(); ++i) {
            JSONObject server = iceServers.getJSONObject(i);
            JSONArray turnUrls = server.getJSONArray("urls");
            String username = server.has("username") ? server.getString("username") : "";
            String credential = server.has("credential") ? server.getString("credential") : "";
            for (int j = 0; j < turnUrls.length(); j++) {
                String turnUrl = turnUrls.getString(j);
                PeerConnection.IceServer iceServer = PeerConnection.IceServer
                        .builder(turnUrl)
                        .setUsername(username)
                        .setPassword(credential)
                        .createIceServer();
                turnServers.add(iceServer);
            }
        }

        PeerConnection.RTCConfiguration pcConfig = new PeerConnection.RTCConfiguration(turnServers);

        pcConfig.bundlePolicy = getBundlePolicey(rtcConfigJSON.getString("bundlePolicy"));
        pcConfig.iceTransportsType = getIceTransportsType(rtcConfigJSON.getString("iceTransportPolicy"));
        pcConfig.rtcpMuxPolicy = getRtcpMuxPolicy(rtcConfigJSON.getString("rtcpMuxPolicy"));
        return pcConfig;
    }

    private PeerConnection.BundlePolicy getBundlePolicey(String bundlePolicy) {
        if (bundlePolicy.equals("balanced")) {
            return PeerConnection.BundlePolicy.BALANCED;
        } else if (bundlePolicy.equals("max-compat")) {
            return PeerConnection.BundlePolicy.MAXCOMPAT;
        } else if (bundlePolicy.equals("max-bundle")) {
            return PeerConnection.BundlePolicy.MAXBUNDLE;
        } else {
            Log.e(TAG, "getBundlePolicey failed: bundlePolicy = " + bundlePolicy);
            return PeerConnection.BundlePolicy.MAXBUNDLE;
        }
    }

    private PeerConnection.IceTransportsType getIceTransportsType(String type) {
        if (type.equals("relay")) {
            return PeerConnection.IceTransportsType.RELAY;
        } else if (type.equals("all")) {
            return PeerConnection.IceTransportsType.ALL;
        } else if (type.equals("nohost")) {
            return PeerConnection.IceTransportsType.NOHOST;
        } else if (type.equals("none")){
            return PeerConnection.IceTransportsType.NONE;
        } else {
            Log.e(TAG, "getIceTransportsType failed: iceTransportsType = " + type);
            return PeerConnection.IceTransportsType.ALL;
        }
    }

    private PeerConnection.RtcpMuxPolicy getRtcpMuxPolicy(String policy) {
        if (policy.equals("negotiate")) {
            return PeerConnection.RtcpMuxPolicy.NEGOTIATE;
        } else if (policy.equals("require")) {
            return PeerConnection.RtcpMuxPolicy.REQUIRE;
        } else {
            Log.e(TAG, "getRtcpMuxPolicy failed: rtcpMuxPolicy = " + policy);
            return PeerConnection.RtcpMuxPolicy.REQUIRE;
        }
    }

    /**
     * 将stream ID改为userId，远端也会根据该ID做显示，所以一定要记得修改SDP
     * @param sdp
     * @param oldId
     * @param newId
     * @return
     */
    private String changeStreamId(String sdp, String oldId, String newId) {
        sdp = sdp.replace(oldId, newId);

        return sdp;
    }

    // 保活开始
    void startKeepLive(int interval) {
        if(keepLiveTimer_ != null) {
            keepLiveTimer_.cancel();
        }
        keepLiveTimer_ = new Timer();
        keepLiveTimer_.schedule(new TimerTask() {
            @Override
            public void run() {
            // 发送保活信息
            if(_webSocket.sendKeepLive() != 0) {
                if(_webSocket.getNetState() == NetState.NET_DISCONNECTED) {
                    _webSocket.reConnect();
                }
            }
            }
        },interval, interval);//延时interval秒执行
    }
    // 停止保活
    void stopKeepLive() {
        if(keepLiveTimer_ != null) {
            keepLiveTimer_.cancel();
            keepLiveTimer_ = null;
        }
    }


    // 停止所有的统计信息
    void stopAllStats() {
        for (String key : _connectionPeerDic.keySet()) {
            Peer peer = _connectionPeerDic.get(key);
            peer.enableStatsEvent(false, 0);    // 停止质量统计
        }
    }

    void stopAllPeerConnection() {
        for(String key:_connectionPeerDic.keySet()) {
            Peer peer = _connectionPeerDic.get(key);
            peer.close();
        }
        _connectionPeerDic.clear();
    }

    // ==================================信令回调===============================================

    @Override
    public void onWebSocketStateChange(NetState netState) {
        Log.i(TAG,"netState " + netState.toString());
        executor.execute(() -> {
            viewCallback.onNetStateChanged(netState);

            if (netState == NetState.NET_CONNECTED) {        // 网络恢复则重新连接
                // 清楚所有的连接，然后重新加入房间
                clearAllInitiatorState();
                joinRoom(roomId_);
            }
        });
    }

    private void clearAllInitiatorState() {
        for(String key:_connectionPeerDic.keySet()) {
            Peer peer = _connectionPeerDic.get(key);
            peer.setInitiator(false);
        }
    }


    // 响应answer
    @Override
    public void onResponseAnswer(JSONObject json) {

    }
    // 响应Candidate
    @Override
    public void onResponseCandidate(JSONObject json) {

    }
    // 响应通用消息
    @Override
    public void onResponseGeneralMessage(JSONObject json) {

    }


    @Override
    public void onResponseOffer(JSONObject json) {

    }
    @Override
    public void onGeneralMsg(JSONObject json) {

    }
}



