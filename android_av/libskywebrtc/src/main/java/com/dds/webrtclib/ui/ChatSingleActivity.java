package com.dds.webrtclib.ui;

import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
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
import com.dds.webrtclib.cmd.Command;
import com.dds.webrtclib.utils.PermissionUtil;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

/**
 * 单聊界面
 * 1. 一对一视频通话
 * 2. 一对一语音通话
 */
public class ChatSingleActivity extends AppCompatActivity implements IViewCallback {
    private SurfaceViewRenderer local_view;
    private SurfaceViewRenderer remote_view;
    private ProxyVideoSink localRender;
    private ProxyVideoSink remoteRender;

    private WebRTCManager manager;

    private boolean videoEnable = false;
    private boolean isSwappedFeeds;

    private EglBase rootEglBase;
    private String appId_;
    private String roomId_;
    private String roomName_;
    private String uid_;
    private String uname_;
    private int mediaType_;

    public static void openActivity(Activity activity, String appId, String roomId, String roomName, String uid, String uname, int mediaType) {
        Intent intent = new Intent(activity, ChatSingleActivity.class);
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
        setContentView(R.layout.wr_activity_chat_single);
        initVar();
        initListener();
    }


    private int previewX, previewY;
    private int moveX, moveY;

    private void initVar() {
        Intent intent = getIntent();
        appId_ = intent.getStringExtra("appId");
        roomId_ = intent.getStringExtra("roomId");
        roomName_ = intent.getStringExtra("roomName");
        uid_ = intent.getStringExtra("uid");
        uname_ = intent.getStringExtra("uname");
        mediaType_ = intent.getIntExtra("mediaType", 0);
        if (mediaType_ == MediaType.TYPE_VIDEO) {
            videoEnable = true;
        } else {
            videoEnable = false;
        }

        ChatSingleFragment chatSingleFragment = new ChatSingleFragment();
        replaceFragment(chatSingleFragment, videoEnable);
        rootEglBase = EglBase.create();
        if (videoEnable) {
            local_view = findViewById(R.id.local_view_render);
            remote_view = findViewById(R.id.remote_view_render);

            // 本地图像初始化
            local_view.init(rootEglBase.getEglBaseContext(), null);
            local_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            local_view.setZOrderMediaOverlay(true);
            local_view.setMirror(true);
            localRender = new ProxyVideoSink();
            //远端图像初始化
            remote_view.init(rootEglBase.getEglBaseContext(), null);
            remote_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
            remote_view.setMirror(true);
            remoteRender = new ProxyVideoSink();
            setSwappedFeeds(true);

            local_view.setOnClickListener(v -> setSwappedFeeds(!isSwappedFeeds));
        }

        startCall();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        if (videoEnable) {
            // 设置小视频可以移动
            local_view.setOnTouchListener((view, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        previewX = (int) motionEvent.getX();
                        previewY = (int) motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) motionEvent.getX();
                        int y = (int) motionEvent.getY();
                        moveX = (int) motionEvent.getX();
                        moveY = (int) motionEvent.getY();
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) local_view.getLayoutParams();
                        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); // Clears the rule, as there is no removeRule until API 17.
                        lp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                        int left = lp.leftMargin + (x - previewX);
                        int top = lp.topMargin + (y - previewY);
                        lp.leftMargin = left;
                        lp.topMargin = top;
                        view.setLayoutParams(lp);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (moveX == 0 && moveY == 0) {
                            view.performClick();
                        }
                        moveX = 0;
                        moveY = 0;
                        break;
                }
                return true;
            });
        }
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        this.isSwappedFeeds = isSwappedFeeds;
        localRender.setTarget(isSwappedFeeds ? remote_view : local_view);
        remoteRender.setTarget(isSwappedFeeds ? local_view : remote_view);
    }

    private void startCall() {
        manager = WebRTCManager.getInstance();
        manager.init(appId_, roomId_, roomName_, uid_, uname_, mediaType_);

        manager.connect();
        manager.setCallback(this);
        if (!PermissionUtil.isNeedRequestPermission(ChatSingleActivity.this)) {
            manager.joinRoom(getApplicationContext(), rootEglBase);
        }
    }

    private void replaceFragment(Fragment fragment, boolean videoEnable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("videoEnable", videoEnable);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.wr_container, fragment)
                .commit();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }


    // 切换摄像头
    public void switchCamera() {
        manager.switchCamera();
    }

    // 挂断
    public void hangUp() {
        disConnect();
        this.finish();
    }

    // 静音
    public void toggleMic(boolean enable) {
        manager.toggleMute(enable);
    }

    // 扬声器
    public void toggleSpeaker(boolean enable) {
        manager.toggleSpeaker(enable);

    }

    @Override
    protected void onDestroy() {
        disConnect();
        super.onDestroy();

    }

    private void disConnect() {
        if (manager != null) {
        manager.exitRoom();
        }
        if (localRender != null) {
            localRender.setTarget(null);
            localRender = null;
        }
        if (remoteRender != null) {
            remoteRender.setTarget(null);
            remoteRender = null;
        }

        if (local_view != null) {
            local_view.release();
            local_view = null;
        }
        if (remote_view != null) {
            remote_view.release();
            remote_view = null;
        }
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

    @Override
    public void onSetLocalStream(MediaStream stream, String socketId) {
        if (stream.videoTracks.size() > 0) {
            stream.videoTracks.get(0).addSink(localRender);
        }

        if (videoEnable) {
            stream.videoTracks.get(0).setEnabled(true);
        }
    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String userId, String userName, int talkType) {
        if (stream.videoTracks.size() > 0) {
            if(videoEnable)
                stream.videoTracks.get(0).addSink(remoteRender);
        }
        if (videoEnable) {
            stream.videoTracks.get(0).setEnabled(true);

            runOnUiThread(() -> setSwappedFeeds(false));
        }
    }

    @Override
    public void onAddRemoteStream(MediaStreamTrack track, String userId, String userName, int talkType) {

    }

    @Override
    public void onCloseWithId(String socketId) {
        runOnUiThread(() -> {
            disConnect();
            ChatSingleActivity.this.finish();
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

    @Override
    public void onNetStateChanged(NetState netState) {
        String msg = null;
        if(netState == NetState.NET_DISCONNECTED) {
            msg = "网络出现异常";
        } else if(netState == NetState.NET_RE_CONNECTING) {
            msg = "网络努力重连中";
        } else if (netState == NetState.NET_DISCONNECTED_AND_EXIT) {
            msg = "网络已经断开，请挂机稍后再试";
        }

        showCallbackInfo(msg);
        if(netState == NetState.NET_DISCONNECTED_AND_EXIT) {
            hangUp();
        }
    }

    void showCallbackInfo(String msg) {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(ChatSingleActivity.this, msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            LinearLayout linearLayout = (LinearLayout)toast.getView();
            TextView textView = (TextView)linearLayout.getChildAt(0);
            textView.setTextSize(25);
            textView.setTextColor(Color.BLUE);
            toast.show();
        });
    }
}
