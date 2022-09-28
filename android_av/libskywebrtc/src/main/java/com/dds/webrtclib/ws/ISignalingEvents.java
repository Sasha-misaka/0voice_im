package com.dds.webrtclib.ws;

import com.dds.webrtclib.NetState;

import org.json.JSONObject;

/**
 * Created by dds on 2019/1/3.
 * android_shuai@163.com
 */
public interface ISignalingEvents {

    // webSocket连接成功
    void onWebSocketStateChange(NetState netState);


    // 进入房间
    void onResponseJoinRoom(JSONObject json);
    // 响应离开房间的命令
    void onResponseLeaveRoom(JSONObject json);
    // 响应offer
    void onResponseOffer(JSONObject json);
    // 响应answer
    void onResponseAnswer(JSONObject json);
    // 响应Candidate
    void onResponseCandidate(JSONObject json);
    // 响应通用消息
    void onResponseGeneralMessage(JSONObject json);
    // 响应保活信息，保活不成功时返回错误信息
    void onResponseKeepLive(JSONObject json);

    // 有新人进入房间
    void onNotifyNewPeerJoinRoom(JSONObject json);
    // 收到远端client的candidate，即打洞信息
    void onRemoteIceCandidate(JSONObject json);
    void onGeneralMsg(JSONObject json);
    void onRemoteIceCandidateRemove(JSONObject json);


    void onRemoteLeaveRoom(JSONObject json);

    void onReceiveOffer(JSONObject json);

    void onReceiverAnswer(JSONObject json);

    void onRemoteTurnTalkType(JSONObject json);
}
