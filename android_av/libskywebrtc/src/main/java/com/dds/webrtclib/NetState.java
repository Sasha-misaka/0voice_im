package com.dds.webrtclib;

public enum NetState {
    NET_INIT,   // 初始化状态
    NET_CONNECTING,
    NET_RE_CONNECTING,         // 网络重连中
    NET_CONNECTED,
    NET_DISCONNECTED,           // 网络断开, SDK内部会尝试重连
    NET_DISCONNECTED_AND_EXIT,   // 网络断开并退出，则调用者可以直接关闭音视频聊天了
    NET_CLOSE   // 主动关闭
}