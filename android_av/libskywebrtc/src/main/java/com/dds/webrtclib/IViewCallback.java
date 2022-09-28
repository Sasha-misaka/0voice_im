package com.dds.webrtclib;

import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;


/**
 * Created by dds on 2017/10/23.
 */

public interface IViewCallback {

    void onSetLocalStream(MediaStream stream, String socketId);

    void onAddRemoteStream(MediaStream stream, String userId, String userName, int talkType);
    void onAddRemoteStream(MediaStreamTrack track, String userId, String userName, int talkType);

    void onCloseWithId(String socketId);

    /**
     * 加入房间的响应信息
     * @param bJoin 是否成功加入房间
     * @param userId 用户ID，在加入房间时如果指定ID则返回相同的ID,如果没有指定则由服务器生成
     * @param userName 用户名
     * @param talkType 通话类型 参考Command.TalkType含义
     * @param roomId    房间ID，在加入房间时如果指定ID则返回相同的ID,如果没有指定则由服务器生成
     * @param roomName 房间名称
     */
    void onJoinComplete(boolean bJoin, String userId, String userName, int talkType, String roomId, String roomName);

    /**
     * 离开房间的响应信息
     * @param bLeave 是否正常离开，true:正常离开；false：异常离开，具体异常信息查看result
     * @param result
     */
    void onLeaveComplete(boolean bLeave, String result);

    /**
     * 远程用户加入，比如你是A已经在房间，然后B加入房间则你收到B加入房间的信息
     * @param userId 加入用户的ID
     * @param userName 加入用户的名称
     * @param userType 加入用户的角色, 目前默认为普通角色不需要做处理
     * @param talkType 通话类型 参考Command.TalkType含义
     */
    void onUserJoined(String userId, String userName, int userType, int talkType);

    /**
     * 远程用户离开
     * @param userId
     * @param userName
     * @param userType
     */
    void onUserLeave(String userId, String userName, int userType);


    /**
     * 远程用户开关麦克风或摄像头
     * @param userId
     * @param userName
     * @param index  设备索引，0摄像头，1麦克风，2共享屏幕，3系统声音
     * @param enable
     */
    void onTurnTalkType(String userId, String userName, int index, boolean enable);

    /**
     * 网络状态改变
     */
    void onNetStateChanged(NetState netState);
}
