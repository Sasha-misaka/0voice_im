
package com.zhangwuji.im.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.melink.bqmmsdk.bean.BQMMGif;
import com.melink.bqmmsdk.bean.Emoji;
import com.melink.bqmmsdk.sdk.BQMM;
import com.melink.bqmmsdk.sdk.BQMMMessageHelper;
import com.melink.bqmmsdk.sdk.IBqmmSendMessageListener;
import com.melink.bqmmsdk.ui.keyboard.BQMMKeyboard;
import com.melink.bqmmsdk.ui.keyboard.IGifButtonClickListener;
import com.melink.bqmmsdk.widget.BQMMEditView;
import com.melink.bqmmsdk.widget.BQMMSendButton;
import com.zhangwuji.im.DB.entity.Group;
import com.zhangwuji.im.DB.entity.Message;
import com.zhangwuji.im.DB.entity.PeerEntity;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.app.IMApplication;
import com.zhangwuji.im.config.DBConstant;
import com.zhangwuji.im.config.HandlerConstant;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.config.SysConstant;
import com.zhangwuji.im.imcore.entity.AudioMessage;
import com.zhangwuji.im.imcore.entity.ImageMessage;
import com.zhangwuji.im.imcore.entity.TextMessage;
import com.zhangwuji.im.imcore.entity.UnreadEntity;
import com.zhangwuji.im.imcore.event.MessageEvent;
import com.zhangwuji.im.imcore.event.PriorityEvent;
import com.zhangwuji.im.imcore.event.SelectEvent;
import com.zhangwuji.im.imcore.manager.IMLoginManager;
import com.zhangwuji.im.imcore.manager.IMStackManager;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.protobuf.helper.EntityChangeEngine;
import com.zhangwuji.im.ui.adapter.MessageAdapter;
import com.zhangwuji.im.imcore.entity.ImageItem;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.bqmmgif.BQMMGifManager;
import com.zhangwuji.im.ui.bqmmgif.IBqmmSendGifListener;
import com.zhangwuji.im.ui.helper.AudioPlayerHandler;
import com.zhangwuji.im.ui.helper.AudioRecordHandler;
import com.zhangwuji.im.ui.helper.Emoparser;
import com.zhangwuji.im.ui.helper.LoginInfoSp;
import com.zhangwuji.im.ui.plugin.ExtensionModule;
import com.zhangwuji.im.ui.plugin.IPluginClickListener;
import com.zhangwuji.im.ui.plugin.IPluginData;
import com.zhangwuji.im.ui.plugin.IPluginModule;
import com.zhangwuji.im.ui.plugin.PluginAdapter;
import com.zhangwuji.im.ui.plugin.message.entity.BigMojiMessage;
import com.zhangwuji.im.ui.widget.MGProgressbar;
import com.zhangwuji.im.utils.CommonUtil;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Nana
 * @Description ???????????????
 * @date 2014-7-15
 * <p/>
 */
public class MessageActivity extends TTBaseActivity
        implements
        OnRefreshListener2<ListView>,
        View.OnClickListener,
        OnTouchListener,
        TextWatcher,
        SensorEventListener {

    private static Handler uiHandler = null;// ????????????

    private IPluginData mIPluginData=null;
    private PluginAdapter mPluginAdapter;
    private PullToRefreshListView lvPTR = null;
    private BQMMEditView messageEdt = null;
    private TextView sendBtn = null;
    private Button recordAudioBtn = null;
    private ImageView keyboardInputImg = null;
    private ImageView soundVolumeImg = null;
    private LinearLayout soundVolumeLayout = null;
    private ImageView audioInputImg = null;
    private ImageView addPhotoBtn = null;
    private ImageView addEmoBtn = null;
    private ImageView addLocationBtn = null;
    private LinearLayout emoLayout = null;
    private String audioSavePath = null;
    private InputMethodManager inputManager = null;
    private AudioRecordHandler audioRecorderInstance = null;
    private TextView textView_new_msg_tip = null;
    private MessageAdapter adapter = null;
    private Thread audioRecorderThread = null;
    private Dialog soundVolumeDialog = null;
    private LinearLayout addOthersPanelView = null;
    MGProgressbar progressbar = null;
    //private boolean audioReday = false; ???????????????
    private SensorManager sensorManager = null;
    private Sensor sensor = null;
    private String takePhotoSavePath = "";
    private Logger logger = Logger.getLogger(MessageActivity.class);
    private IMService imService;
    private User loginUser;
    private PeerEntity peerEntity;
    // ?????????session
    private String currentSessionKey;
    private int historyTimes = 0;
    private RelativeLayout inputbox;
    BQMMKeyboard bqmmKeyboard;
    BQMM bqmmsdk;
    BQMMSendButton bqmmSendButton;
    /**
     * ??????Toast
     */
    private Toast mToast;
    /**
     * BQMM??????
     * ??????????????????
     */
    private Rect tmp = new Rect();
    private int mScreenHeight;
    private View mMainContainer;
    private final int DISTANCE_SLOP = 180;
    private final String LAST_KEYBOARD_HEIGHT = "last_keyboard_height";
    private boolean mPendingShowPlaceHolder;
    private LinearLayout toolbox_buttom_layout;
    private  int peerType=1;
    LoginInfoSp loginInfoSp = LoginInfoSp.instance();

    /**
     * end ??????Toast
     */
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("message_activity#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            initData();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.d("message_activity#onCreate:%s", this);
        super.onCreate(savedInstanceState);



        loginInfoSp.init(this);
        LoginInfoSp.bqmmplugSpIdentity bqmmplugSpIdentity=loginInfoSp.getbqmmplug();



        bqmmsdk = BQMM.getInstance();
        bqmmsdk.initConfig(getBaseContext(), bqmmplugSpIdentity.getAppid(), bqmmplugSpIdentity.getSecret());

        currentSessionKey = getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(currentSessionKey);
        peerType = Integer.parseInt(sessionInfo[0]);

        initSoftInputMethod();
        initEmo();
        initAudioHandler();
        initAudioSensor();
        initView();
        imServiceConnector.connect(this);

        EventBus.getDefault().register(this);

        logger.d("message_activity#register im service and eventBus");
    }

    /**
     * @Description ?????????????????????
     * ???????????? todo
     */
    private void initView() {
        // ??????????????????(????????????????????????????????????)
        LayoutInflater.from(this).inflate(R.layout.tt_activity_message, topContentView);

        //TOP_CONTENT_VIEW
        setLeftButton(R.drawable.ac_back_icon);
       // setLeftText(getResources().getString(R.string.top_left_back));
        setRightButton(R.drawable.de_conversation_info);
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightBtn.setOnClickListener(this);

        mMainContainer = findViewById(R.id.main_container);
        // ????????????(??????PTR)
        lvPTR = (PullToRefreshListView) this.findViewById(R.id.message_list);
        textView_new_msg_tip = (TextView) findViewById(R.id.tt_new_msg_tip);
        lvPTR.getRefreshableView().addHeaderView(LayoutInflater.from(this).inflate(R.layout.tt_messagelist_header,lvPTR.getRefreshableView(), false));
        Drawable loadingDrawable = getResources().getDrawable(R.drawable.pull_to_refresh_indicator);
        final int indicatorWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29,
                getResources().getDisplayMetrics());
        loadingDrawable.setBounds(new Rect(0, indicatorWidth, 0, indicatorWidth));
        lvPTR.getLoadingLayoutProxy().setLoadingDrawable(loadingDrawable);
        lvPTR.getRefreshableView().setCacheColorHint(Color.WHITE);
        lvPTR.getRefreshableView().setSelector(new ColorDrawable(Color.WHITE));
        lvPTR.getRefreshableView().setOnTouchListener(lvPTROnTouchListener);
        adapter = new MessageAdapter(this);
        lvPTR.setAdapter(adapter);
        lvPTR.setOnRefreshListener(this);
        lvPTR.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true) {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            textView_new_msg_tip.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        });
        textView_new_msg_tip.setOnClickListener(this);
        inputbox= (RelativeLayout)findViewById(R.id.pannel_container);
        toolbox_buttom_layout= (LinearLayout)findViewById(R.id.toolbox_buttom_layout);

        // ???????????????????????????
        sendBtn = (TextView) this.findViewById(R.id.send_message_btn);
        recordAudioBtn = (Button) this.findViewById(R.id.record_voice_btn);
        audioInputImg = (ImageView) this.findViewById(R.id.voice_btn);
        messageEdt = (BQMMEditView) this.findViewById(R.id.message_text);
        RelativeLayout.LayoutParams messageEdtParam = (LayoutParams) messageEdt.getLayoutParams();
        messageEdtParam.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
        messageEdtParam.addRule(RelativeLayout.RIGHT_OF, R.id.voice_btn);
        keyboardInputImg = (ImageView) this.findViewById(R.id.show_keyboard_btn);
        addPhotoBtn = (ImageView) this.findViewById(R.id.show_add_photo_btn);
        addEmoBtn = (ImageView) this.findViewById(R.id.show_emo_btn);


        messageEdt.setOnFocusChangeListener(msgEditOnFocusChangeListener);
        messageEdt.setOnClickListener(this);
        messageEdt.addTextChangedListener(this);
        addPhotoBtn.setOnClickListener(this);
        addEmoBtn.setOnClickListener(this);
        keyboardInputImg.setOnClickListener(this);
        audioInputImg.setOnClickListener(this);
        recordAudioBtn.setOnTouchListener(this);
        sendBtn.setOnClickListener(this);
        initSoundVolumeDlg();

        //OTHER_PANEL_VIEW
        addOthersPanelView = (LinearLayout)findViewById(R.id.add_others_panel);


        bqmmKeyboard = (BQMMKeyboard) findViewById(R.id.bqmm_keyboard);
        bqmmSendButton = (BQMMSendButton) findViewById(R.id.btn_send);
        //EMO_LAYOUT
        emoLayout = (LinearLayout) findViewById(R.id.emo_layout);


        bqmmsdk.setEditView(messageEdt);
        bqmmsdk.setKeyboard(bqmmKeyboard, new IGifButtonClickListener() {
            @Override
            public void didClickGifTab() {
                showSoftInput(messageEdt);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BQMMGifManager.getInstance(MessageActivity.this).showTrending();
                    }
                }, 300);
            }
        });

        bqmmsdk.setSendButton(bqmmSendButton);
        bqmmsdk.load();
        BQMMGifManager.getInstance(this).addEditViewListeners();

        bqmmsdk.setBqmmSendMsgListener(new IBqmmSendMessageListener() {
            @Override
            public void onSendMixedMessage(List<Object> list, boolean b) {
            }

            @Override
            public void onSendFace(Emoji emoji) {

                BigMojiMessage bigMojiMessage =new BigMojiMessage();
                bigMojiMessage.setType("#im_big_bqmm#");
                bigMojiMessage.setThumbail(emoji.getThumbail());
                bigMojiMessage.setMainimage(emoji.getMainImage());
                bigMojiMessage.setEmotext(emoji.getEmoText());
                bigMojiMessage.setCode(emoji.getEmoCode());
                bigMojiMessage.setDisplaytext(emoji.getEmoText()); //???????????????????????????txt

                TextMessage textMessage = TextMessage.buildIMessageFosend(bigMojiMessage, loginUser, peerEntity);
                imService.getMessageManager().sendText(textMessage);
                pushList(textMessage);

                scrollToBottomListItem();

            }
        });

        BQMMGifManager.getInstance(getBaseContext()).setBQMMSendGifListener(new IBqmmSendGifListener() {
            @Override
            public void onSendBQMMGif(final BQMMGif bqmmGif) {

                BigMojiMessage bigMojiMessage =new BigMojiMessage();
                bigMojiMessage.setType("#im_gif_bqmm#");
                bigMojiMessage.setThumbail(bqmmGif.getGif_thumb());
                bigMojiMessage.setMainimage(bqmmGif.getSticker_url());
                bigMojiMessage.setEmotext(bqmmGif.getText());
                bigMojiMessage.setCode(bqmmGif.getSticker_id());
                bigMojiMessage.setDisplaytext(bqmmGif.getText()); //???????????????????????????txt

                TextMessage textMessage = TextMessage.buildIMessageFosend(bigMojiMessage, loginUser, peerEntity);
                imService.getMessageManager().sendText(textMessage);
                pushList(textMessage);
                scrollToBottomListItem();

            }
        });
        //LOADING
        View view = LayoutInflater.from(MessageActivity.this)
                .inflate(R.layout.tt_progress_ly, null);
        progressbar = (MGProgressbar) view.findViewById(R.id.tt_progress);
        LayoutParams pgParms = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pgParms.bottomMargin = 50;
        addContentView(view, pgParms);


        /**
         * ????????????????????????
         */
        messageEdt.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Keyboard -> BQMM
                if (mPendingShowPlaceHolder) {
                    // ?????????mPendingShowPlaceHolder????????????????????????Keyboard??????????????????Keyboard????????????????????????
                    if (isKeyboardVisible()) {
                        ViewGroup.LayoutParams params = toolbox_buttom_layout.getLayoutParams();
                        int distance = getDistanceFromInputToBottom();
                        // ??????PlaceHolder??????
                        if (distance > DISTANCE_SLOP && distance != params.height) {
                            params.height = distance;
                            toolbox_buttom_layout.setLayoutParams(params);
                            getPreferences(MODE_PRIVATE).edit().putInt(LAST_KEYBOARD_HEIGHT, distance).apply();
                        }
                        return false;
                    } else {
                        scrollToBottomListItem();
                        showBqmmKeyboard();
                        mPendingShowPlaceHolder = false;
                        return false;
                    }
                } else {//BQMM -> Keyboard
                    if (isBqmmKeyboardVisible() && isKeyboardVisible()) {
                        scrollToBottomListItem();
                        hideBqmmKeyboard();
                        return false;
                    }
                }
                return true;
            }
        });
    }


    // ????????????,imservice?????????????????????newIntent
    private void initData() {
        historyTimes = 0;
        adapter.clearItem();
        ImageMessage.clearImageMessageList();
        loginUser = imService.getLoginManager().getLoginInfo();
        peerEntity = imService.getSessionManager().findPeerEntity(currentSessionKey);
        if(peerEntity==null)
        {
            //??????????????????????????????????????????
        }

        // ??????????????????????????????????????????
        setTitleByUser();
        reqHistoryMsg();
        adapter.setImService(imService, loginUser,peerType);
        imService.getUnReadMsgManager().readUnreadSession(currentSessionKey);
        imService.getNotificationManager().cancelSessionNotifications(currentSessionKey);


        mIPluginData=new IPluginData();
        mIPluginData.setConversationType(peerType);
        mIPluginData.setPeerEntity(peerEntity);
        mIPluginData.setImService(imService);
        mIPluginData.setLoginUser(loginUser);
        mIPluginData.setCurrentSessionKey(currentSessionKey);

        this.mPluginAdapter = new PluginAdapter();
        this.mPluginAdapter.setOnPluginClickListener(new IPluginClickListener() {
            public void onClick(IPluginModule pluginModule, int position) {
                pluginModule.onClick(MessageActivity.this, mIPluginData,position);
            }
        });
        //??????????????????
        ExtensionModule extensionModule=new ExtensionModule();
        List<IPluginModule> pluginModules =extensionModule.getPluginModules(peerType);
        if (pluginModules != null && this.mPluginAdapter != null) {
            this.mPluginAdapter.addPlugins(pluginModules);
        }
        this.mPluginAdapter.bindView(addOthersPanelView);
        this.mPluginAdapter.setVisibility(View.VISIBLE);
    }

    private void initSoftInputMethod() {
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * ????????????Message??????????????????????????????session?????????
     */
    @Override
    protected void onNewIntent(Intent intent) {
        logger.d("message_activity#onNewIntent:%s", this);
        super.onNewIntent(intent);
        setIntent(intent);
        historyTimes = 0;
        if (intent == null) {
            return;
        }
        String newSessionKey = getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        if (newSessionKey == null) {
            return;
        }
        logger.d("chat#newSessionInfo:%s", newSessionKey);
        if (!newSessionKey.equals(currentSessionKey)) {
            currentSessionKey = newSessionKey;
            initData();
        }
    }


    /**
     * ??????????????????
     * 1. ?????????user????????? ????????????UserProfile
     * 2. ???????????????????????????????????????????????????
     */
    private void setTitleByUser() {
        setTitle(peerEntity.getMainName());
        int peerType = peerEntity.getType();
        switch (peerType) {
            case DBConstant.SESSION_TYPE_GROUP: {
                Group group = (Group) peerEntity;
                Set<Integer> memberLists = group.getlistGroupMemberIds();
                if (!memberLists.contains(loginUser.getPeerId())) {
                    Toast.makeText(MessageActivity.this, R.string.no_group_member, Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case DBConstant.SESSION_TYPE_SINGLE: {
                topTitleTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IMUIHelper.openUserProfileActivity(MessageActivity.this, peerEntity.getPeerId());
                    }
                });
            }
            break;
        }
    }


    private void handleImagePickData(List<ImageItem> list) {
        ArrayList<ImageMessage> listMsg = new ArrayList<>();
        ArrayList<ImageItem> itemList = (ArrayList<ImageItem>) list;
        for (ImageItem item : itemList) {
            ImageMessage imageMessage = ImageMessage.buildForSend(item, loginUser, peerEntity);
            listMsg.add(imageMessage);
            pushList(imageMessage);
        }
        imService.getMessageManager().sendImages(listMsg);
    }

    @Subscribe(threadMode = ThreadMode.MAIN,priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onUserEvent(SelectEvent event) {
        List<ImageItem> itemList = event.getList();
        if (itemList != null || itemList.size() > 0)
            handleImagePickData(itemList);
    }

    /**
     * ??????: 1.EventBus???cancelEventDelivery????????????postThread?????????????????????????????????????????????
     * 2. onEvent(A a)  onEventMainThread(A a) ????????????????????????????????????
     * ??????: ?????????????????????????????????event??????onEvent??????handler??????????????????
     * ??????cancelEventDelivery
     * <p/>
     * todo  need find good solution
     */
    @Subscribe(threadMode = ThreadMode.POSTING,priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_RECEIVED_MESSAGE: {
                Message entity = (Message) event.object;
                /**?????????????????????*/
                if (currentSessionKey.equals(entity.getSessionKey())) {
                    android.os.Message message = android.os.Message.obtain();
                    message.what = HandlerConstant.MSG_RECEIVED_MESSAGE;
                    message.obj = entity;
                    uiHandler.sendMessage(message);
                    EventBus.getDefault().cancelEventDelivery(event);
                }
            }
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN,priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onEventMainThread(MessageEvent event) {
        MessageEvent.Event type = event.getEvent();
        Message entity = event.getMessageEntity();
        switch (type) {
            case ACK_SEND_MESSAGE_OK: {
                onMsgAck(event.getMessageEntity());
            }
            break;
            case ACK_SEND_MESSAGE_FAILURE:
                // ???????????????????????????
                showToast(R.string.message_send_failed);
            case ACK_SEND_MESSAGE_TIME_OUT: {
                onMsgUnAckTimeoutOrFailure(event.getMessageEntity());
            }
            break;

            case HANDLER_IMAGE_UPLOAD_FAILD: {
                logger.d("pic#onUploadImageFaild");
                ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
                adapter.updateItemState(imageMessage);
                showToast(R.string.message_send_failed);
            }
            break;

            case HANDLER_IMAGE_UPLOAD_SUCCESS: {
                ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
                adapter.updateItemState(imageMessage);
            }
            break;
            case HISTORY_MSG_OBTAIN: {
                if (historyTimes == 1) {
                    adapter.clearItem();
                    reqHistoryMsg();
                }
            }
            break;
            case SENDPUSHLIST:{
                pushList(entity);
            }
            break;
            case PLUGINCOMPLETE:
            {
                messageEdt.clearFocus();//??????????????????
                scrollToBottomListItem();
                addOthersPanelView.setVisibility(View.GONE);
                inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
                emoLayout.setVisibility(View.GONE);
                addOthersPanelView.setVisibility(View.GONE);
                closebroad();
            }
            break;
        }
    }

    /**
     * audio?????????????????????????????????
     */
    protected void initAudioHandler() {
        uiHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_RECORD_FINISHED:
                        onRecordVoiceEnd((Float) msg.obj);
                        break;

                    // ????????????
                    case HandlerConstant.HANDLER_STOP_PLAY:
                        // ?????????????????????
                        //adapter.stopVoicePlayAnim((String) msg.obj);
                        break;

                    case HandlerConstant.RECEIVE_MAX_VOLUME:
                        onReceiveMaxVolume((Integer) msg.obj);
                        break;

                    case HandlerConstant.RECORD_AUDIO_TOO_LONG:
                        doFinishRecordAudio();
                        break;

                    case HandlerConstant.MSG_RECEIVED_MESSAGE:
                        Message entity = (Message) msg.obj;
                        onMsgRecv(entity);
                        break;

                    default:
                        break;
                }
            }
        };
    }

    /**
     * [??????] DB????????????session?????????manager????????????
     *
     * @param messageEntity
     */
    private void onMsgAck(Message messageEntity) {
        logger.d("message_activity#onMsgAck");
        int msgId = messageEntity.getMsgId();
        logger.d("chat#onMsgAck, msgId:%d", msgId);

        /**??????????????????ID?????*/
        long localId = messageEntity.getId();
        adapter.updateItemState(messageEntity);
    }


    private void handleUnreadMsgs() {
        logger.d("messageacitivity#handleUnreadMsgs sessionId:%s", currentSessionKey);
        // ??????????????????
        UnreadEntity unreadEntity = imService.getUnReadMsgManager().findUnread(currentSessionKey);
        if (null == unreadEntity) {
            return;
        }
        int unReadCnt = unreadEntity.getUnReadCnt();
        if (unReadCnt > 0) {
            imService.getNotificationManager().cancelSessionNotifications(currentSessionKey);
            adapter.notifyDataSetChanged();
            scrollToBottomListItem();
        }
    }


    // ?????????????????????session???
    private void onMsgRecv(Message entity) {
        logger.d("message_activity#onMsgRecv");

        imService.getUnReadMsgManager().ackReadMsg(entity);
        logger.d("chat#start pushList");
        pushList(entity);
        ListView lv = lvPTR.getRefreshableView();
        if (lv != null) {

            if (lv.getLastVisiblePosition() < adapter.getCount()) {
                textView_new_msg_tip.setVisibility(View.VISIBLE);
            } else {
                scrollToBottomListItem();
            }
        }
    }


    private void onMsgUnAckTimeoutOrFailure(Message messageEntity) {
        logger.d("chat#onMsgUnAckTimeoutOrFailure, msgId:%s", messageEntity.getMsgId());
        // msgId ???????????????0
        adapter.updateItemState(messageEntity);
    }


    /**
     * @Description ?????????????????????
     */
    private void showGroupManageActivity() {
        Intent i = new Intent(this, GroupManagermentActivity.class);
        i.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
        startActivity(i);
    }

    /**
     * @Description ?????????AudioManager??????????????????????????????????????????
     */
    private void initAudioSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }



    private void initEmo() {
        Emoparser.getInstance(MessageActivity.this);
        IMApplication.gifRunning = true;
    }



    /**
     * @Description ????????????????????????
     */
    private void initSoundVolumeDlg() {
        soundVolumeDialog = new Dialog(this, R.style.SoundVolumeStyle);
        soundVolumeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        soundVolumeDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        soundVolumeDialog.setContentView(R.layout.tt_sound_volume_dialog);
        soundVolumeDialog.setCanceledOnTouchOutside(true);
        soundVolumeImg = (ImageView) soundVolumeDialog.findViewById(R.id.sound_volume_img);
        soundVolumeLayout = (LinearLayout) soundVolumeDialog.findViewById(R.id.sound_volume_bk);
    }

    /**
     * 1.???????????????????????????
     * 2.?????????????????????????????????
     */
    private void reqHistoryMsg() {
        historyTimes++;
        List<Message> msgList = imService.getMessageManager().loadHistoryMsg(historyTimes,currentSessionKey,peerEntity);
        pushList(msgList);
        scrollToBottomListItem();
    }
    /**
     * @param msg
     */
    public void pushList(Message msg) {
        logger.d("chat#pushList msgInfo:%s", msg);
        adapter.addItem(msg);
    }

    public void pushList(List<Message> entityList) {
        logger.d("chat#pushList list:%d", entityList.size());
        adapter.loadHistoryList(entityList);
    }


    /**
     * @Description ????????????(60s)???????????????????????????
     */
    public void doFinishRecordAudio() {
        try {
            if (audioRecorderInstance.isRecording()) {
                audioRecorderInstance.setRecording(false);
            }
            if (soundVolumeDialog.isShowing()) {
                soundVolumeDialog.dismiss();
            }

            recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal);

            audioRecorderInstance.setRecordTime(SysConstant.MAX_SOUND_RECORD_TIME);
            onRecordVoiceEnd(SysConstant.MAX_SOUND_RECORD_TIME);
        } catch (Exception e) {
        }
    }

    /**
     * @param voiceValue
     * @Description ?????????????????????????????????????????????
     */
    private void onReceiveMaxVolume(int voiceValue) {
        if (voiceValue < 200.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_02);
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_03);
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_04);
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_05);
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_06);
        } else if (voiceValue > 28000.0) {
            soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_07);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }


    /**
     * @param audioLen
     * @Description ?????????????????????????????????
     */
    private void onRecordVoiceEnd(float audioLen) {
        logger.d("message_activity#chat#audio#onRecordVoiceEnd audioLen:%f", audioLen);
        AudioMessage audioMessage = AudioMessage.buildForSend(audioLen, audioSavePath, loginUser, peerEntity);
        imService.getMessageManager().sendVoice(audioMessage);
        pushList(audioMessage);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
    }




    /**************************
     * ????????????????????????????????? start
     **************************************/
    private void closebroad() {
        if (isBqmmKeyboardVisible()) {
            hideBqmmKeyboard();
        } else if (isKeyboardVisible()) {
            hideSoftInput(messageEdt);
        }
        BQMMGifManager.getInstance(MessageActivity.this).setSearchUIVisible(false);
    }

    private boolean isKeyboardVisible() {
        return (getDistanceFromInputToBottom() > DISTANCE_SLOP && !isBqmmKeyboardVisible())
                || (getDistanceFromInputToBottom() > (toolbox_buttom_layout.getHeight() + DISTANCE_SLOP) && isBqmmKeyboardVisible());
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    private void hideSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    private void showBqmmKeyboard() {
        emoLayout.setVisibility(View.VISIBLE);
        bqmmKeyboard.showKeyboard();
        toolbox_buttom_layout.setVisibility(View.VISIBLE);
    }

    private void hideBqmmKeyboard() {
        bqmmKeyboard.hideKeyboard();
        emoLayout.setVisibility(View.GONE);
        toolbox_buttom_layout.setVisibility(View.GONE);
    }

    private boolean isBqmmKeyboardVisible() {
      //  return bqmmKeyboard.isKeyboardVisible();
        if(toolbox_buttom_layout.getVisibility()==View.VISIBLE)
        {
            return  true;
        }
        else
        {
            return false;
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private int getDistanceFromInputToBottom() {
        return mScreenHeight - getInputBottom();
    }

    /**
     * ????????????????????????
     */
    private int getInputBottom() {
        inputbox.getGlobalVisibleRect(tmp);
        return tmp.bottom;
    }

    /**************************
     * ????????????????????????????????? end
     **************************************/


    @Override
    public void onPullDownToRefresh(
            final PullToRefreshBase<ListView> refreshView) {
        // ????????????
        refreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ListView mlist = lvPTR.getRefreshableView();
                int preSum = mlist.getCount();
                Message messageEntity = adapter.getTopMsgEntity();
                if (messageEntity != null) {
                    List<Message> historyMsgInfo = imService.getMessageManager().loadHistoryMsg(messageEntity, historyTimes);
                    if (historyMsgInfo.size() > 0) {
                        historyTimes++;
                        adapter.loadHistoryList(historyMsgInfo);
                    }
                }

                int afterSum = mlist.getCount();
                mlist.setSelection(afterSum - preSum);
                /**???????????????????????????????????????*/
                //mlist.setSelection(size);
                // ????????????
//                if (!(mlist).isStackFromBottom()) {
//                    mlist.setStackFromBottom(true);
//                }
//                mlist.setStackFromBottom(false);
                refreshView.onRefreshComplete();
            }
        }, 200);
    }



    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            sendBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEdt
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
            addPhotoBtn.setVisibility(View.GONE);
        } else {
            addPhotoBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEdt
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
            sendBtn.setVisibility(View.GONE);
        }
        scrollToBottomListItem();
    }

    /**
     * @Description ?????????????????????
     */
    private void scrollToBottomListItem() {
        logger.d("message_activity#scrollToBottomListItem");

        // todo eric, why use the last one index + 2 can real scroll to the
        // bottom?
        ListView lv = lvPTR.getRefreshableView();
        if (lv != null) {
            lv.setSelection(adapter.getCount() + 1);
        }
        textView_new_msg_tip.setVisibility(View.GONE);
    }



    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent arg0) {
        try {
            if (!AudioPlayerHandler.getInstance().isPlaying()) {
                return;
            }
            float range = arg0.values[0];
            if (null != sensor && range == sensor.getMaximumRange()) {
                // ??????????????????
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_NORMAL, this);
            } else {
                // ????????????
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_IN_CALL, this);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static Handler getUiHandler() {
        return uiHandler;
    }

    private void actFinish() {
        inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
        IMStackManager.getStackManager().popTopActivitys(MainActivity.class);
        IMApplication.gifRunning = false;
        MessageActivity.this.finish();
    }


    private OnTouchListener lvPTROnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                messageEdt.clearFocus();
                closebroad();
            }
            return false;
        }
    };

    private View.OnFocusChangeListener msgEditOnFocusChangeListener = new android.view.View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
        }
    };

    public void showToast(int resId) {
        String text = getResources().getString(resId);
        if (mToast == null) {
            mToast = Toast.makeText(MessageActivity.this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        IMApplication.gifRunning = false;
        cancelToast();
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        logger.d("message_activity#onPause:%s", this);
        super.onPause();
    }
    @Override
    protected void onStop() {
        logger.d("message_activity#onStop:%s", this);
        if (null != adapter) {
            adapter.hidePopup();
        }
        AudioPlayerHandler.getInstance().clear();
        super.onStop();
    }

    @Override
    protected void onStart() {
        logger.d("message_activity#onStart:%s", this);
        super.onStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((isBqmmKeyboardVisible() || isKeyboardVisible())) {
                closebroad();
            }
            else
            {
                actFinish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Activity???????????????????????????????????????
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mScreenHeight <= 0) {
            mMainContainer.getGlobalVisibleRect(tmp);
            mScreenHeight = tmp.bottom;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;
        switch (requestCode) {
            default:
            {
                int position = (requestCode >> 8) - 1;
                int reqCode = requestCode & 255;
                IPluginModule pluginModule = this.mPluginAdapter.getPluginModule(position);
                if (pluginModule != null) {
                    pluginModule.onActivityResult(reqCode, resultCode, data);
                }
            }break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        logger.d("message_activity#onresume:%s", this);
        super.onResume();
        IMApplication.gifRunning = true;
        historyTimes = 0;
        // not the first time
        if (imService != null) {
            // ??????session???????????????
            handleUnreadMsgs();
        }
    }

    @Override
    protected void onDestroy() {
        logger.d("message_activity#onDestroy:%s", this);
        historyTimes = 0;
        imServiceConnector.disconnect(this);
        EventBus.getDefault().unregister(this);
        adapter.clearItem();
        sensorManager.unregisterListener(this, sensor);
        ImageMessage.clearImageMessageList();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                actFinish();
                break;
            case R.id.right_btn:
                showGroupManageActivity();
                break;
            case R.id.show_add_photo_btn: {
                recordAudioBtn.setVisibility(View.GONE);
                keyboardInputImg.setVisibility(View.GONE);
                messageEdt.setVisibility(View.VISIBLE);
                audioInputImg.setVisibility(View.VISIBLE);
                addEmoBtn.setVisibility(View.VISIBLE);
                closebroad();
                emoLayout.setVisibility(View.GONE);
                addOthersPanelView.setVisibility(View.VISIBLE);
                toolbox_buttom_layout.setVisibility(View.VISIBLE);
                scrollToBottomListItem();

            }
            break;
            case R.id.show_emo_btn: {
                /**yingmu ?????????????????????*/
                recordAudioBtn.setVisibility(View.GONE);
                keyboardInputImg.setVisibility(View.GONE);
                messageEdt.setVisibility(View.VISIBLE);
                audioInputImg.setVisibility(View.VISIBLE);
                addEmoBtn.setVisibility(View.VISIBLE);
                bqmmKeyboard.setVisibility(View.VISIBLE);


                if (isBqmmKeyboardVisible()) { // PlaceHolder -> Keyboard
                    showSoftInput(messageEdt);
                } else if (isKeyboardVisible()) { // Keyboard -> PlaceHolder
                    mPendingShowPlaceHolder = true;
                    hideSoftInput(messageEdt);
                } else { // Just show PlaceHolder
                    showBqmmKeyboard();
                    addOthersPanelView.setVisibility(View.GONE);
                }
                scrollToBottomListItem();
            }
            break;
            case R.id.send_message_btn: {
                logger.d("message_activity#send btn clicked");

                String content = messageEdt.getText().toString();
                logger.d("message_activity#chat content:%s", content);
                if (content.trim().equals("")) {
                    Toast.makeText(MessageActivity.this,
                            getResources().getString(R.string.message_null), Toast.LENGTH_LONG).show();
                    return;
                }
                TextMessage textMessage = TextMessage.buildForSend(content, loginUser, peerEntity);
                imService.getMessageManager().sendText(textMessage);
                messageEdt.setText("");
                pushList(textMessage);
                scrollToBottomListItem();
            }
            break;
            case R.id.voice_btn: {
                inputManager.hideSoftInputFromWindow(messageEdt.getWindowToken(), 0);
                messageEdt.setVisibility(View.GONE);
                audioInputImg.setVisibility(View.GONE);
                recordAudioBtn.setVisibility(View.VISIBLE);
                keyboardInputImg.setVisibility(View.VISIBLE);
                emoLayout.setVisibility(View.GONE);
                addOthersPanelView.setVisibility(View.GONE);
                messageEdt.setText("");
                messageEdt.clearFocus();
                closebroad();
            }
            break;
            case R.id.show_keyboard_btn: {
                recordAudioBtn.setVisibility(View.GONE);
                keyboardInputImg.setVisibility(View.GONE);
                messageEdt.setVisibility(View.VISIBLE);
                audioInputImg.setVisibility(View.VISIBLE);
                addEmoBtn.setVisibility(View.VISIBLE);
                emoLayout.setVisibility(View.GONE);
            }
            break;
            case R.id.message_text: {
                scrollToBottomListItem();
                break;
            }
            case R.id.tt_new_msg_tip:
            {
                scrollToBottomListItem();
                textView_new_msg_tip.setVisibility(View.GONE);
            }
            break;
        }
    }


    // ????????????????????????
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        scrollToBottomListItem();
        if (id == R.id.record_voice_btn) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                if (AudioPlayerHandler.getInstance().isPlaying())
                    AudioPlayerHandler.getInstance().stopPlayer();
                y1 = event.getY();
                recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_pressed);
                recordAudioBtn.setText(MessageActivity.this.getResources().getString(
                        R.string.release_to_send_voice));

                soundVolumeImg.setImageResource(R.drawable.tt_sound_volume_01);
                soundVolumeImg.setVisibility(View.VISIBLE);
                soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_default_bk);
                soundVolumeDialog.show();
                audioSavePath = CommonUtil
                        .getAudioSavePath(IMLoginManager.instance().getLoginId());

                // ??????callback???????????????????????????MotionEvent.ACTION_UP ??????
                audioRecorderInstance = new AudioRecordHandler(audioSavePath);

                audioRecorderThread = new Thread(audioRecorderInstance);
                audioRecorderInstance.setRecording(true);
                logger.d("message_activity#audio#audio record thread starts");
                audioRecorderThread.start();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                y2 = event.getY();
                if (y1 - y2 > 180) {
                    soundVolumeImg.setVisibility(View.GONE);
                    soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_cancel_bk);
                } else {
                    soundVolumeImg.setVisibility(View.VISIBLE);
                    soundVolumeLayout.setBackgroundResource(R.drawable.tt_sound_volume_default_bk);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                y2 = event.getY();
                if (audioRecorderInstance.isRecording()) {
                    audioRecorderInstance.setRecording(false);
                }
                if (soundVolumeDialog.isShowing()) {
                    soundVolumeDialog.dismiss();
                }
                recordAudioBtn.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal);
                recordAudioBtn.setText(MessageActivity.this.getResources().getString(
                        R.string.tip_for_voice_forward));
                if (y1 - y2 <= 180) {
                    if (audioRecorderInstance.getRecordTime() >= 0.5) {
                        if (audioRecorderInstance.getRecordTime() < SysConstant.MAX_SOUND_RECORD_TIME) {
                            android.os.Message msg = uiHandler.obtainMessage();
                            msg.what = HandlerConstant.HANDLER_RECORD_FINISHED;
                            msg.obj = audioRecorderInstance.getRecordTime();
                            uiHandler.sendMessage(msg);
                        }
                    } else {
                        soundVolumeImg.setVisibility(View.GONE);
                        soundVolumeLayout
                                .setBackgroundResource(R.drawable.tt_sound_volume_short_tip_bk);
                        soundVolumeDialog.show();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                if (soundVolumeDialog.isShowing())
                                    soundVolumeDialog.dismiss();
                                this.cancel();
                            }
                        }, 700);
                    }
                }
            }
        }
        return false;
    }



}
