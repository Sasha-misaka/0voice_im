package com.dds.webrtclib.cmd;;
import com.dds.webrtclib.utils.TimeTool;

import org.json.JSONException;
import org.json.JSONObject;


public class Command {
    private CommonUserInfo userInfo;

    public void setUserInfo(CommonUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    private static Command command = null;
    public static Command getInstance() {
        if (command == null) {
            command = new Command();
        }
        return command;
    }



    public class SignalType {
        final public static String LOGON   = "logon";        // 登录负载均衡服务器获取roomserver地址，暂时不用
        final public static String JOIN    = "join";
        final public static String LEAVE   = "leave";
        final public static String OFFER   = "offer";
        final public static String ANSWER   = "answer";
        final public static String CANDIDATE   = "candidate";
        final public static String KEEP_LIVE   = "keepLive";
        final public static String REPORT_INFO   = "reportInfo";
        final public static String REPORT_STATS   = "reportStats";
        /**
         * Index   设备索引：0摄像头，1麦克风，2共享屏幕，3系统声音
         * Enable  设备情况： false关闭，true开启
         * */
        final public static String TURN_TALK_TYPE = "turnTalkType";  // 更新通话类型，主动通知自己的情况，比如自己关闭声音则房间其他人听不到你的声音，比如自己关闭画面则其他人看不到你画面.
        final public static String PEER_CONNECTED =  "peerConnected";

        // 服务器回应请求信令
        final public static String RESP_JOIN =       "respJoin";
        final public static String RESP_LEAVE =      "respLeave";
        final public static String RESP_OFFER =      "respOffer";
        final public static String RESP_ANSWER =     "respAnswer";
        final public static String RESP_CANDIDATE =  "respCandidate";
        final public static String RESP_KEEP_LIVE =   "respKeepLive";
        final public static String RESP_TURN_TALK_TYPE = "respTurnTalkType";
        final public static String RESP_GENERAL_MSG =    "generalMsgResp";

        // 服务器转发的请求信令
        final public static String ON_REMOTE_LEAVE =     "relayLeave";
        final public static String ON_REMOTE_OFFER =     "relayOffer";
        final public static String ON_REMOTE_ANSWER =    "relayAnswer";
        final public static String ON_REMOTE_CANDIDATE =  "relayCandidate";
        final public static String ON_REMOTE_TURN_TALK_TYPE = "relayTurnTalkType";   // 对端更新通话类型，接收端接收到该信令则通过回调提示应用层对端比如关闭声音等

        // 服务器主动给客户端发命令
        final public static String NOTIFYE_NEW_PEER =     "notifyNewPeer";
    }

    public class EcerRTCConstant {
        /** EcerRTC SDK版本号 */
        final public static String SDK_VERSION_NAME = "1.0.0";
        /** logon version */
        final public static String LOGON_VERSION = "1";
        /** keepAlive时间间隔 */
        final public static int KEEPALIVE_INTERVAL = 5 * 1000;
        /** keepAlive最大连续失败次数 */
        final public static int KEEPALIVE_FAILEDTIMES_MAX = 4;
        /** keepAliveTimer时间间隔 */
        final public static int KEEPALIVE_TIMER_INTERVAL = 2 * 1000;
        /** keepAlive未收到result最大超时时间 */
        final public static int KEEPALIVE_TIMER_TIMEOUT_MAX = 20;
        /** keepAlive未收到result最大超时时间 */
        final public static int KEEPALIVE_TIMER_TIMEOUT_RECONNECT = 12;
        /** reconnect最大连续次数 */
        final public static int RECONNECT_MAXTIMES = 10;
        /** reconnect连续重连时间间隔 */
        final public static int RECONNECT_TIMEOUT = 1 * 1000;
        /** getStatsReport时间间隔 */
        final public static int GETSTATSREPORT_INTERVAL = 2 * 1000;
        final public static int PEER_CONNECT_STATE_TIMEOUT = 20000;   // 设置20秒
    }

    /** 连接类型 */
    public class ConnectionType  {
        /** P2P模式 */
        final public static int P2P = 0;
        /** MediaServer模式 */
        final public static int MEDIASERVER = 1;
    }

    /** 用户模式类型 */
    public class UserType{
        /** 普通模式 */
        final public static int NORMAL = 0;
        /** 观察者模式 */
        final public static int OBSERVER = 1;
    }

    /** 通话类型 **/
    public class TalkType {
        final public static int AUDIO_ONLY = 0;      //无视频有音频;
        final public static int AUDIO_VIDEO = 1;     //有视频有音频;
        final public static int VIDEO_ONLY = 2;      //有视频无音频;
        final public static int NO_AUDIO_VIDEO = 3;   //无视频无音频
    }

    /** 与服务器的连接状态 */
    public class ConnectionState{
        final public static String CONNECTED = "CONNECTED";
        final public static String DISCONNECTED = "DISCONNECTED";
        final public static String ROOM_ERROR = "ROOM_ERROR";
    }
    /** websocket的连接状态 */
    public class wsConnectionState {
        final public static String CONNECTED = "CONNECTED";
        final public static String DISCONNECTED = "DISCONNECTED";
        final public static String CONNECTING = "CONNECTING";
    }

    /** logonAndJoin status */
    public class LogonAndJoinStatus {
        final public static int CONNECT = 0;
        final public static int RECONNECT = 1;
    }
    /** offer status */
    public class OfferStatus {
        final public static String SENDING = "SENDING";
        final public static String DONE = "DONE";
    }

    public class RoomErrorCode {
        final public static int ROOM_ERROR_SUCCESS  	 = 0;		// 为0
        final public static int ROOM_ERROR_FULL  		 = 1;		// 房间已经满了
        final public static int ROOM_ERROR_NOT_FIND_UID  = 2;		// 不能找到指定的用户ID
        final public static int ROOM_ERROR_PARSE_FAILED  = 3;		// 解析json错误
        final public static int ROOM_ERROR_WEBSOCKET_BROKEN  = 4;	// websocket出现异常
        final public static int ROOM_ERROR_NOT_FIND_RID	     = 5;	// 没有找到指定的房间
        final public static int ROOM_ERROR_NOT_FIND_REMOTEID = 6;	// 远程ID
        final public static int ROOM_ERROR_WEBSOCKET_FAILED  = 7;	// websocket出错
        final public static int ROOM_ERROR_ICE_FULL_LOADING  = 8;	// ice server负载已满
        final public static int ROOM_ERROR_ICE_INVALID       = 9;	// ice server失效
        final public static int ROOM_ERROR_NO_MICROPHONE_DEV = 10;    // 没有音频设备
    }

    public class Errors {
        final public static String ROOM_ERROR_SUCCESS = "successful";	// 0
        final public static String ROOM_ERROR_FULL =    "room is full; it up to max number";	// 1
        final public static String ROOM_ERROR_NOT_FIND_UID = "can't find the designated uid; it may leave the room halfway"; // 2
        final public static String ROOM_ERROR_PARSE_FAILED ="server parse the message faild; please check the format";	// 3
        final public static String ROOM_ERROR_WEBSOCKET_BROKEN ="websocket may be broken";	// 4
        final public static String ROOM_ERROR_NOT_FIND_RID ="can't find the designated room id";	// 5
        final public static String ROOM_ERROR_NOT_FIND_REMOTEID ="can't find the remote user id";	//6
        final public static String ROOM_ERROR_WEBSOCKET_FAILED ="can't connect to room server ";	// 7
        final public static String ROOM_ERROR_ICE_FULL_LOADING ="ice server bandwidth is full loading"; // 8
        final public static String ROOM_ERROR_ICE_INVALID ="ice server invalid; please report to the ecer technology";//9
        final public static String ROOM_ERROR_NO_MICROPHONE_DEV ="no microphone device; you can't use talk feature";
    }

    public class Platform {
        final public static String ANDROID = "android";
        final public static String IOS = "ios";
        final public static String WIN_PC = "winpc";
        final public static String WEB = "web";      // 暂不做手机web的优化，所以只要是浏览器统一认为是web
    }

    public class CommonUserInfo {
        private String appId = "";   //
        private String token = "";
        private String roomId = "";
        private String roomName = "";
        private String userId = "";
        private String userName = "";
        private int userType = UserType.NORMAL;    // 用户类型
        private int talkType = TalkType.AUDIO_VIDEO;
        private String time = "";
        private String osName = "";
        private String browser = "";
        private String sdkInfo = "";

        public CommonUserInfo(String appId, String token,
                              String roomId, String roomName,
                              String userId, String userName,
                              int userType, int talkType,
                              String osName, String sdkInfo) {
            this.appId = appId;
            this.token = token;
            this.roomId = roomId;
            this.roomName = roomName;
            this.userId = userId;
            this.userName = userName;
            this.userType = userType;
            this.talkType = talkType;
            this.osName = osName;
            this.browser = "androidApp";
            this.sdkInfo = sdkInfo;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getUserType() {
            return userType;
        }

        public void setUserType(int userType) {
            this.userType = userType;
        }

        public int getTalkType() {
            return talkType;
        }

        public void setTalkType(int talkType) {
            this.talkType = talkType;
        }

        public String getTime() {
            return TimeTool.getTime();
        }


        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getBrowser() {
            return browser;
        }

        public void setBrowser(String browser) {
            this.browser = browser;
        }

        public String  getSdkInfo() {
            return sdkInfo;
        }

        public void setSdkInfo(String sdkInfo) {
            this.sdkInfo = sdkInfo;
        }
    }
    // Put a |key|->|value| mapping in |json|.
    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // 封装加入命令
    public String packJoin() {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.JOIN);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "token", userInfo.getToken());
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "roomName", userInfo.getRoomName());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "uname", userInfo.getUserName());
        jsonPut(json, "userType", userInfo.getUserType());
        jsonPut(json, "talkType", userInfo.getTalkType());
        jsonPut(json, "time", userInfo.getTime());
        jsonPut(json, "osName", userInfo.getOsName());
        jsonPut(json, "browser", userInfo.getBrowser());
        jsonPut(json, "sdkInfo", userInfo.getSdkInfo());

        return json.toString();
    }
    // 封装离开命令
    public String packLeave() {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.LEAVE);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "uname", userInfo.getUserName());
        jsonPut(json, "userType", userInfo.getUserType());
        jsonPut(json, "time", userInfo.getTime());

        return json.toString();
    }
    // 封装offer
    public String packOffer(String desc, String remoteUid) {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.OFFER);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "uname", userInfo.getUserName());
        jsonPut(json, "remoteUid", remoteUid);
        jsonPut(json, "time", userInfo.getTime());
        jsonPut(json, "msg", desc);

        return json.toString();
    }
    // 封装answer
    public String packAnswer(String desc, String remoteUid) {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.ANSWER);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "uname", userInfo.getUserName());
        jsonPut(json, "remoteUid", remoteUid);
        jsonPut(json, "time", userInfo.getTime());
        jsonPut(json, "msg", desc);

        return json.toString();
    }

    // 封装candidate
    public String packCandidate(String desc, String remoteUid) {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.CANDIDATE);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "uname", userInfo.getUserName());
        jsonPut(json, "remoteUid", remoteUid);
        jsonPut(json, "time", userInfo.getTime());
        jsonPut(json, "msg", desc);

        return json.toString();
    }

    // 封装keeplive
    public String packKeepLive() {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.KEEP_LIVE);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "time", userInfo.getTime());

        return json.toString();
    }

    // 封装peerConnected
    public String packPeerConnected(String remoteUid, String connectType) {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.PEER_CONNECTED);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "connectType", connectType);
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "remoteUid", remoteUid);
        jsonPut(json, "time", userInfo.getTime());

        return json.toString();
    }

    public String packTurnType(int index, boolean isMute) {
        JSONObject json = new JSONObject();
        jsonPut(json, "cmd", SignalType.TURN_TALK_TYPE);
        jsonPut(json, "appId", userInfo.getAppId());
        jsonPut(json, "roomId", userInfo.getRoomId());
        jsonPut(json, "uid", userInfo.getUserId());
        jsonPut(json, "uname", userInfo.getUserName());
        jsonPut(json, "index", index);
        jsonPut(json, "enable", isMute);
        jsonPut(json, "time", userInfo.getTime());

        return json.toString();
    }

}
