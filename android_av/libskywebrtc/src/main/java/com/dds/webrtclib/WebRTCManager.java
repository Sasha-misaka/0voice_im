package com.dds.webrtclib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.dds.webrtclib.bean.MediaType;
import com.dds.webrtclib.bean.MyIceServer;
import com.dds.webrtclib.ws.IConnectEvent;
import com.dds.webrtclib.ws.ISignalingEvents;
import com.dds.webrtclib.ws.IWebSocket;
import com.dds.webrtclib.ws.JavaWebSocket;

import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制信令和各种操作，
 * （1）出现异常时通知显示界面；
 * （2）对方关闭声音或者画面时也通知对方
 * Created by dds on 2019/4/5.
 * android_shuai@163.com
 */
public class WebRTCManager  {

    private final static String TAG = "sing_WebRTCManager";


    private PeerConnectionHelper _peerHelper;

    private String appId_;
    private String roomId_;
    private String uid_;
    private String uname_;
    private int mediaType_;



    private Handler handler = new Handler(Looper.getMainLooper());

    public static WebRTCManager getInstance() {
        return Holder.wrManager;
    }
	
    private static class Holder {
        private static WebRTCManager wrManager = new WebRTCManager();
    }

    // init address
    public void init(String appId, String roomId, String roomName, String uid, String uname, int mediaType) {
        _peerHelper = new PeerConnectionHelper(appId, roomId, roomName, uid, uname, mediaType);
        appId_ = appId;
        roomId_ = roomId;
        uid_ = uid;
        uname_ = uname;
    }

    // connect
    public void connect() {


    }

    public void setCallback(IViewCallback callback) {
        if (_peerHelper != null) {
            _peerHelper.setViewCallback(callback);
        }
    }

    //===================================控制功能==============================================
    public void joinRoom(Context context, EglBase eglBase) {
        if (_peerHelper != null) {
            _peerHelper.initContext(context, eglBase);
            _peerHelper.joinRoom(roomId_);
        }
    }

    public void switchCamera() {
        if (_peerHelper != null) {
            _peerHelper.switchCamera();
        }
    }

    public void toggleMute(boolean enable) {
        if (_peerHelper != null) {
            _peerHelper.toggleMute(enable);
        }
    }

    public void toggleCamera(boolean enable) {
        if (_peerHelper != null) {
            _peerHelper.toggleCamera(enable);
        }
    }

    public void toggleSpeaker(boolean enable) {
        if (_peerHelper != null) {
            _peerHelper.toggleSpeaker(enable);
        }
    }

    public void exitRoom() {
        if (_peerHelper != null) {
            _peerHelper.exitRoom();
        }
    }



}
