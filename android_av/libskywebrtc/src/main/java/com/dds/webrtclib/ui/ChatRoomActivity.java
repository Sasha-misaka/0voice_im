package com.dds.webrtclib.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dds.webrtclib.IViewCallback;
import com.dds.webrtclib.NetState;
import com.dds.webrtclib.PeerConnectionHelper;
import com.dds.webrtclib.ProxyVideoSink;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.bean.MediaType;
import com.dds.webrtclib.bean.MemberBean;
import com.dds.webrtclib.cmd.Command;
import com.dds.webrtclib.utils.PermissionUtil;
import com.dds.webrtclib.ws.IConnectEvent;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群聊界面
 * 支持 9 路同時通信
 */
public class ChatRoomActivity extends AppCompatActivity implements IViewCallback {

    private FrameLayout wr_video_view;

    private WebRTCManager manager;
    private Map<String, SurfaceViewRenderer> _videoViews = new HashMap<>();
    private Map<String, ProxyVideoSink> _sinks = new HashMap<>();
    private List<MemberBean> _infos = new ArrayList<>();

    private int mScreenWidth;

    private EglBase rootEglBase;
    private String appId_;
    private String roomId_;
    private String roomName_;
    private String uid_;
    private String uname_;
    private int mediaType_;
    private String TAG = "ChatRoom";


    public static void openActivity(Activity activity, String appId, String roomId, String roomName, String uid, String uname, int mediaType) {
        Intent intent = new Intent(activity, ChatRoomActivity.class);
        intent.putExtra("appId", appId);
        intent.putExtra("roomId", roomId);
        intent.putExtra("roomName", roomName);
        intent.putExtra("uid", uid);
        intent.putExtra("uname", uname);
        intent.putExtra("mediaType", mediaType);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wr_activity_chat_room);
        initView();
        initVar();
        ChatRoomFragment chatRoomFragment = new ChatRoomFragment();
        replaceFragment(chatRoomFragment);

        Intent intent = getIntent();//获取相关的intent
        appId_ = intent.getStringExtra("appId");
        roomId_ = intent.getStringExtra("roomId");
        roomName_ = intent.getStringExtra("roomName");
        uid_ = intent.getStringExtra("uid");
        uname_ = intent.getStringExtra("uname");
        mediaType_ = intent.getIntExtra("mediaType", 0);
        startCall();

    }


    private void initView() {
        wr_video_view = findViewById(R.id.wr_video_view);
    }

    private void initVar() {
        // 设置宽高比例
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (manager != null) {
            mScreenWidth = manager.getDefaultDisplay().getWidth();
        }
        wr_video_view.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mScreenWidth));
        rootEglBase = EglBase.create();

    }

    private void startCall() {
        // 初始化
        WebRTCManager.getInstance().init(appId_, roomId_, roomName_, uid_, uname_, mediaType_);

        WebRTCManager.getInstance().connect();
        manager = WebRTCManager.getInstance();
        manager.setCallback(this);

        if (!PermissionUtil.isNeedRequestPermission(ChatRoomActivity.this)) {
            manager.joinRoom(getApplicationContext(), rootEglBase);
        }

    }

    @Override
    public void onSetLocalStream(MediaStream stream, String userId) {
        List<VideoTrack> videoTracks = stream.videoTracks;
        runOnUiThread(() -> {
            Log.i(TAG, "onSetLocalStream -> addView, userId: " + userId);
            addView(userId, stream);
        });
    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String userId, String userName, int talkType) {
        runOnUiThread(() -> {
            Log.i(TAG, "onAddRemoteStream -> addView, userId: " + userId);
            addView(userId, stream);
        });
    }

    @Override
    public void onAddRemoteStream(MediaStreamTrack track, String userId, String userName, int talkType) {
        runOnUiThread(() -> {
            Log.i(TAG, "onAddRemoteStream -> addView, userId: " + userId);
            addView(userId, (VideoTrack) track);
        });
    }

    @Override
    public void onCloseWithId(String userId) {
        runOnUiThread(() -> {
            removeView(userId);
        });
    }

    @Override
    public void onJoinComplete(boolean bJoin, String userId, String userName, int talkType, String roomId, String roomName) {
        String msg = userName + "加入" + "房间" + roomId;
        if(bJoin) {
            msg += "成功";
        } else {
            msg += "失败";
        }
        showCallbackInfo(msg);
    }

    @Override
    public void onLeaveComplete(boolean bLeave, String result) {
        String msg = "离开";
        if(bLeave) {
            msg += "成功";
        } else {
            msg += ("失败, 错误原因" + result);
        }
        showCallbackInfo(msg);
    }

    @Override
    public void onUserJoined(String userId, String userName, int userType, int talkType) {
        String msg = "远程用户" + userName + "加入房间";
        if(talkType == Command.TalkType.AUDIO_ONLY) {
            msg += ",未开启摄像头";
        }
        showCallbackInfo(msg);
    }

    @Override
    public void onUserLeave(String userId, String userName, int userType) {
        String msg = "远程用户" + userName + "离开房间";
        showCallbackInfo(msg);
    }

    /**
     * 这里的处理只是直接使用toast进行显示，实际效果可以做到类似微信一样，可以看到对方已关闭摄像头、麦克风的信息
     * @param userId
     * @param userName
     * @param index  设备索引，0摄像头，1麦克风，2共享屏幕，3系统声音
     * @param enable
     */
    @Override
    public void onTurnTalkType(String userId, String userName, int index, boolean enable) {
        String dev = "未知设备";
        if(index == 0) {
            dev = "摄像头";
        } else if(index == 1) {
            dev = "麦克风";
        }
        String act = "";
        if(enable) {
            act = "打开";
        } else {
            act = "关闭";
        }
        String msg = userName + act + dev;
        showCallbackInfo(msg);
    }

    /**
     * 网络状态改变NET_DISCONNECTED,           // 网络断开, SDK内部会尝试重连
     *     NET_TRY_CONNECTING,         // 网络重连中
     *     NET_DISCONNECTED_AND_EXIT   // 网络断开并退出，则调用者可以直接关闭音视频聊天了
     */
    @Override
    public void onNetStateChanged(NetState netState) {
        String msg = null;
        if(netState == NetState.NET_DISCONNECTED) {
            msg = "网络出现异常";
        } else if(netState == NetState.NET_RE_CONNECTING) {
            msg = "网络努力重连中";
        }
        else if(netState == NetState.NET_DISCONNECTED_AND_EXIT) {
            msg = "网络已经断开，请挂机稍后再试";
        }

        showCallbackInfo(msg);
        if(netState == NetState.NET_DISCONNECTED_AND_EXIT) {
            hangUp();
        }
    }

    private void addView(String userId, MediaStream stream) {

        if(_sinks.get(userId) != null) {        // 如果已经存在则直接显示
            ProxyVideoSink sink = _sinks.get(userId);
            stream.videoTracks.get(0).addSink(sink);
        }
        else {
            SurfaceViewRenderer renderer = new SurfaceViewRenderer(ChatRoomActivity.this);
            renderer.init(rootEglBase.getEglBaseContext(), null);
            renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            renderer.setMirror(true);
            // set render
            ProxyVideoSink sink = new ProxyVideoSink();
            sink.setTarget(renderer);
            if (stream.videoTracks.size() > 0) {
                stream.videoTracks.get(0).addSink(sink);
            }
            Log.i(TAG, "addView, put user id:" + userId);
            _videoViews.put(userId, renderer);
            _sinks.put(userId, sink);
            _infos.add(new MemberBean(userId));
            wr_video_view.addView(renderer);

            int size = _infos.size();
            for (int i = 0; i < size; i++) {
                MemberBean memberBean = _infos.get(i);
                SurfaceViewRenderer renderer1 = _videoViews.get(memberBean.getId());
                if (renderer1 != null) {
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.height = getWidth(size);
                    layoutParams.width = getWidth(size);
                    layoutParams.leftMargin = getX(size, i);
                    layoutParams.topMargin = getY(size, i);
                    renderer1.setLayoutParams(layoutParams);
                }

            }
        }
    }
    private void addView(String userId, VideoTrack track) {

        if(_sinks.get(userId) != null) {        // 如果已经存在则直接显示
            ProxyVideoSink sink = _sinks.get(userId);
            track.addSink(sink);
        }
        else {
            SurfaceViewRenderer renderer = new SurfaceViewRenderer(ChatRoomActivity.this);
            renderer.init(rootEglBase.getEglBaseContext(), null);
            renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            renderer.setMirror(true);
            // set render
            ProxyVideoSink sink = new ProxyVideoSink();
            sink.setTarget(renderer);
            track.addSink(sink);

            Log.i(TAG, "addView, put user id:" + userId);
            _videoViews.put(userId, renderer);
            _sinks.put(userId, sink);
            _infos.add(new MemberBean(userId));
            wr_video_view.addView(renderer);

            int size = _infos.size();
            for (int i = 0; i < size; i++) {
                MemberBean memberBean = _infos.get(i);
                SurfaceViewRenderer renderer1 = _videoViews.get(memberBean.getId());
                if (renderer1 != null) {
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.height = getWidth(size);
                    layoutParams.width = getWidth(size);
                    layoutParams.leftMargin = getX(size, i);
                    layoutParams.topMargin = getY(size, i);
                    renderer1.setLayoutParams(layoutParams);
                }

            }
        }
    }

    private void removeView(String userId) {
        ProxyVideoSink sink = _sinks.get(userId);
        SurfaceViewRenderer renderer = _videoViews.get(userId);
        if (sink != null) {
            sink.setTarget(null);
        }
        if (renderer != null) {
            renderer.release();
        }
        _sinks.remove(userId);
        _videoViews.remove(userId);
        _infos.remove(new MemberBean(userId));
        wr_video_view.removeView(renderer);


        int size = _infos.size();
        for (int i = 0; i < _infos.size(); i++) {
            MemberBean memberBean = _infos.get(i);
            SurfaceViewRenderer renderer1 = _videoViews.get(memberBean.getId());
            if (renderer1 != null) {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.height = getWidth(size);
                layoutParams.width = getWidth(size);
                layoutParams.leftMargin = getX(size, i);
                layoutParams.topMargin = getY(size, i);
                renderer1.setLayoutParams(layoutParams);
            }

        }

    }

    private int getWidth(int size) {
        if (size <= 4) {
            return mScreenWidth / 2;
        } else if (size <= 9) {
            return mScreenWidth / 3;
        }
        return mScreenWidth / 3;
    }

    private int getX(int size, int index) {
        if (size <= 4) {
            if (size == 3 && index == 2) {
                return mScreenWidth / 4;
            }
            return (index % 2) * mScreenWidth / 2;
        } else if (size <= 9) {
            if (size == 5) {
                if (index == 3) {
                    return mScreenWidth / 6;
                }
                if (index == 4) {
                    return mScreenWidth / 2;
                }
            }

            if (size == 7 && index == 6) {
                return mScreenWidth / 3;
            }

            if (size == 8) {
                if (index == 6) {
                    return mScreenWidth / 6;
                }
                if (index == 7) {
                    return mScreenWidth / 2;
                }
            }
            return (index % 3) * mScreenWidth / 3;
        }
        return 0;
    }

    private int getY(int size, int index) {
        if (size < 3) {
            return mScreenWidth / 4;
        } else if (size < 5) {
            if (index < 2) {
                return 0;
            } else {
                return mScreenWidth / 2;
            }
        } else if (size < 7) {
            if (index < 3) {
                return mScreenWidth / 2 - (mScreenWidth / 3);
            } else {
                return mScreenWidth / 2;
            }
        } else if (size <= 9) {
            if (index < 3) {
                return 0;
            } else if (index < 6) {
                return mScreenWidth / 3;
            } else {
                return mScreenWidth / 3 * 2;
            }

        }
        return 0;
    }


    @Override  // 屏蔽返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        exit();
        super.onDestroy();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.wr_container, fragment)
                .commit();

    }

    // 切换摄像头
    public void switchCamera() {
        manager.switchCamera();
    }

    // 挂断
    public void hangUp() {
        exit();

//        Intent intent = new Intent(MainActivy)
        this.finish();

    }

    // 静音
    public void toggleMic(boolean enable) {
        manager.toggleMute(enable);
    }

    // 免提
    public void toggleSpeaker(boolean enable) {
        manager.toggleSpeaker(enable);
    }

    // 打开关闭摄像头
    public void toggleCamera(boolean enable) {
        manager.toggleCamera(enable);
    }

    private void exit() {
        manager.exitRoom();
        for (SurfaceViewRenderer renderer : _videoViews.values()) {
            renderer.release();
        }
        for (ProxyVideoSink sink : _sinks.values()) {
            sink.setTarget(null);
        }
        _videoViews.clear();
        _sinks.clear();
        _infos.clear();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            Log.i(PeerConnectionHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
        manager.joinRoom(getApplicationContext(), rootEglBase);


    }

    void showCallbackInfo(String msg) {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(ChatRoomActivity.this, msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            LinearLayout linearLayout = (LinearLayout)toast.getView();
            TextView textView = (TextView)linearLayout.getChildAt(0);
            textView.setTextSize(25);
            textView.setTextColor(Color.BLUE);
            toast.show();
        });
    }
}
