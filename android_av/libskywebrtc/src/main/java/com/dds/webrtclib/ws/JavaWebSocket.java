package com.dds.webrtclib.ws;

import android.annotation.SuppressLint;
import android.nfc.Tag;
import android.util.Log;

import org.java_websocket.drafts.Draft_6455;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dds.webrtclib.NetState;
import com.dds.webrtclib.cmd.Command;
import com.dds.webrtclib.utils.TimeTool;
import com.dds.webrtclib.utils.Utils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.webrtc.IceCandidate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public class JavaWebSocket implements IWebSocket {

    private final static String TAG = "dds_JavaWebSocket";
    private WebSocketClient mWebSocketClient;
    private NetState wsConnectionState = NetState.NET_INIT;     // websocket连接状态

    private ISignalingEvents events;
    public final static int RE_CONNECT_MAX_TIMES = 5;
    private String wss_ = Utils.wssUrl;
    private int reconnectTimes = 0;

    public JavaWebSocket(ISignalingEvents events) {
        this.events = events;
    }

    long startTime_;
    private String lastCommand_ = "";           // 保存最后一条命令，只保存join的命令
    private String lastCommandMessage_ = "";    // 保存最后一条命令的信息

    private void connect() {
        startTime_ = System.currentTimeMillis();
        String wss = wss_;
        URI uri;
        try {
            uri = new URI(wss_);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            events.onWebSocketStateChange(NetState.NET_DISCONNECTED_AND_EXIT);
            return;
        }
        if (mWebSocketClient == null) {
            Map<String, String> map = new HashMap<>();
            map.put("Origin", Utils.videoUrl);
            mWebSocketClient = new WebSocketClient(uri, new Draft_6455(), map) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    if (wsConnectionState == NetState.NET_INIT) {    // 第一次连接成功
                        wsConnectionState = NetState.NET_CONNECTED;
                    } else {
                        wsConnectionState = NetState.NET_CONNECTED;
                        events.onWebSocketStateChange(NetState.NET_CONNECTED);
                    }
                    reconnectTimes = 0;
                    if (lastCommand_.equals(Command.SignalType.JOIN)) {
                        if (sendMessage(lastCommandMessage_) == 0) {
                            lastCommand_ = "";
                        }
                    }
                    Log.i(TAG, "open websocket time = " + (System.currentTimeMillis() - startTime_));
                }

                @Override
                public void onMessage(String message) {
                    wsConnectionState = NetState.NET_CONNECTED;
                    Log.d(TAG, message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.e(TAG, "onClose:" + reason);
                    reconnectTimes++;
                    if(NetState.NET_CLOSE != wsConnectionState) {   // 不是主动关闭时才做异常处理
                        if (reconnectTimes >= RE_CONNECT_MAX_TIMES) {
                            wsConnectionState = NetState.NET_DISCONNECTED_AND_EXIT;
                            events.onWebSocketStateChange(wsConnectionState);
                        } else {
                            wsConnectionState = NetState.NET_DISCONNECTED;
                            events.onWebSocketStateChange(wsConnectionState);
                        }
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "onError -> " + ex.toString());
                }
            };
        }
        if (wss.startsWith("wss")) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                if (sslContext != null) {
                    sslContext.init(null, new TrustManager[]{new TrustManagerTest()}, new SecureRandom());
                }

                SSLSocketFactory factory = null;
                if (sslContext != null) {
                    factory = sslContext.getSocketFactory();
                }

                if (factory != null) {
                    mWebSocketClient.setSocket(factory.createSocket());
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mWebSocketClient.connect();
    }


    @Override
    public boolean isOpen() {
        return wsConnectionState == NetState.NET_CONNECTED;
    }

    @Override
    public void reConnect() {
        Log.w(TAG, "网络正在尝试连接中...");
        mWebSocketClient = null;
        connect();
    }

    @Override
    public NetState getNetState() {
        return wsConnectionState;
    }

    public void close() {
        if (mWebSocketClient != null) {
            wsConnectionState = NetState.NET_CLOSE;
            mWebSocketClient.close();
        }

    }


    //============================需要发送的=====================================
    @Override
    public int joinRoom(String room) {
        String msg = Command.getInstance().packJoin();
        if (wsConnectionState == NetState.NET_CONNECTED) {
            return sendMessage(msg);
        } else {     // 如果没有连接则先连接websocket
            Log.i(TAG, "wait websocket connect room server ......");
            lastCommand_ = Command.SignalType.JOIN;
            lastCommandMessage_ = msg;
            connect();
            return 0;
        }
    }

    @Override
    public int exitRoom() {
        String msg = Command.getInstance().packLeave();
        return sendMessage(msg);
    }

    @Override
    public int sendOffer(String desc, String remoteUid) {
        String msg = Command.getInstance().packOffer(desc, remoteUid);
        return sendMessage(msg);
    }

    @Override
    public int sendAnswer(String desc, String remoteUid) {
        String msg = Command.getInstance().packAnswer(desc, remoteUid);
        return sendMessage(msg);
    }

    @Override
    public int sendIceCandidate(String desc, String remoteUid) {
        String msg = Command.getInstance().packCandidate(desc, remoteUid);
        return sendMessage(msg);
    }

    public int sendKeepLive() {
        String msg = Command.getInstance().packKeepLive();
        return sendMessage(msg);
    }

    @Override
    public int sendStats(String stats) {
        return sendMessage(stats);
    }

    @Override
    public int sendTurnTalkType(int index, boolean isMute) {
        String msg = Command.getInstance().packTurnType(index, isMute);
        return sendMessage(msg);
    }

    @Override
    public int sendPeerConnected(String remoteUid, String connectType) {
        String msg = Command.getInstance().packPeerConnected(remoteUid, connectType);
        return sendMessage(msg);
    }
    //============================需要发送的=====================================


    //============================需要接收的=====================================
    @Override
    public void handleMessage(String message) {
        Log.i(TAG, "message: " + message);
        try {
            JSONObject json = new JSONObject(message);
            String cmd = json.getString("cmd");
            if (cmd == null) {
                Log.e(TAG, "the message can't find the cmd");
                return;
            }
            switch (cmd) {
                case Command.SignalType.RESP_JOIN:
                    handleJoinToRoom(json);
                    break;
                case Command.SignalType.RESP_LEAVE:
                    handleResponseLeaveRoom(json);
                    break;
                case Command.SignalType.RESP_OFFER:
                    handleResponseOffer(json);
                    break;
                case Command.SignalType.NOTIFYE_NEW_PEER:
                    handleRemoteJoinToRoom(json);      // 新人加入
                    break;
                case Command.SignalType.ON_REMOTE_LEAVE:
                    handleRemoteOutRoom(json);
                    break;
                case Command.SignalType.ON_REMOTE_OFFER:
                    handleOffer(json);
                    break;
                case Command.SignalType.ON_REMOTE_ANSWER:
                    handleAnswer(json);
                    break;
                case Command.SignalType.ON_REMOTE_CANDIDATE:
                    handleRemoteCandidate(json);
                    break;
                case Command.SignalType.RESP_GENERAL_MSG:
                    handleGeneralMsg(json);
                    break;
                case Command.SignalType.RESP_KEEP_LIVE:
                    handleResponseKeepLive(json);
                    break;
                case Command.SignalType.ON_REMOTE_TURN_TALK_TYPE:
                    handleRemoteTurnTalkType(json);
                    break;
                default:
                    Log.e(TAG, "can't handle cmd = " + cmd);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "WebSocket message JSON parsing error: " + e.toString());
        }
    }

    private void handleRemoteTurnTalkType(JSONObject json) {
        events.onRemoteTurnTalkType(json);
    }

    // 自己进入房间
    private void handleJoinToRoom(JSONObject json) {
        events.onResponseJoinRoom(json);
    }

    private void handleResponseLeaveRoom(JSONObject json) {
        events.onResponseLeaveRoom(json);
    }

    private void handleResponseOffer(JSONObject json) {
        events.onResponseOffer(json);
    }

    // 自己已经在房间，有人进来
    private void handleRemoteJoinToRoom(JSONObject json) {
        events.onNotifyNewPeerJoinRoom(json);
    }

    // 有人离开了房间
    private void handleRemoteOutRoom(JSONObject json) {
        events.onRemoteLeaveRoom(json);
    }

    // 处理交换信息
    private void handleRemoteCandidate(JSONObject json) {
        events.onRemoteIceCandidate(json);
    }

    private void handleGeneralMsg(JSONObject json) {
        events.onGeneralMsg(json);
    }

    private void handleResponseKeepLive(JSONObject json) {
        events.onResponseKeepLive(json);
    }


    // 处理Offer
    private void handleOffer(JSONObject json) {
        events.onReceiveOffer(json);
    }

    // 处理Answer
    private void handleAnswer(JSONObject json) {
        events.onReceiverAnswer(json);
    }
    //============================需要接收的=====================================


    // 忽略证书
    public static class TrustManagerTest implements X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }


    int sendMessage(String msg) {
        if (wsConnectionState == NetState.NET_CONNECTED) {
            Log.i(TAG, "c->s -->" + msg);
            mWebSocketClient.send(msg);
            return 0;
        } else {
            Log.e(TAG, "当前网络已经断开...., 请重连后再发送");
            return -1;
        }
    }
}
