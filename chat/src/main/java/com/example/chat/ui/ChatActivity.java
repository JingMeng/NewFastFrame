package com.example.chat.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.adapter.ChatMessageAdapter;
import com.example.chat.base.CommonImageLoader;
import com.example.chat.base.Constant;
import com.example.chat.bean.BaseMessage;
import com.example.chat.bean.ChatMessage;
import com.example.chat.bean.FaceText;
import com.example.chat.bean.GroupChatMessage;
import com.example.chat.bean.GroupTableMessage;
import com.example.chat.bean.ImageItem;
import com.example.chat.bean.User;
import com.example.chat.db.ChatDB;
import com.example.chat.events.GroupInfoEvent;
import com.example.chat.listener.AddFriendCallBackListener;
import com.example.chat.listener.OnMessageReceiveListener;
import com.example.chat.listener.OnSendMessageListener;
import com.example.chat.listener.SendFileListener;
import com.example.chat.manager.ChatNotificationManager;
import com.example.chat.manager.MessageCacheManager;
import com.example.chat.manager.MsgManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.manager.UserManager;
import com.example.chat.manager.VoiceRecordManager;
import com.example.chat.receiver.PushMessageReceiver;
import com.example.chat.util.CommonUtils;
import com.example.chat.util.FaceTextUtil;
import com.example.chat.util.LogUtil;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.rxbus.RxBusManager;
import com.example.commonlibrary.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import io.reactivex.functions.Consumer;

/**
 * 椤圭洰鍚嶇О:    HappyChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/9/13      21:53
 * QQ:             1981367757
 */
public class ChatActivity extends SlideBaseActivity implements View.OnClickListener, TextWatcher, View.OnLongClickListener, View.OnTouchListener, ChatMessageAdapter.OnItemClickListener, OnMessageReceiveListener, SendFileListener {
        private SwipeRefreshLayout mSwipeRefreshLayout;
        private RecyclerView display;
        private Button add;
        private Button emotion;
        private EditText input;
        private Button speak;
        private Button voice;
        private Button send;
        private Button keyboard;
        private ViewPager mViewPager;
        private LinearLayout l1_more;
        private TextView location;
        private TextView picture;
        private TextView camera;
        private RelativeLayout r1_emotion;
        private LinearLayout l1_add;
        private RelativeLayout record_container;
        private ImageView record_display;
        private TextView record_tip;
        private LinearLayoutManager mLinearLayoutManager;
        private ChatMessageAdapter mAdapter;
        private String localImagePath;
        private List<FaceText> emotionFaceList;
        private GridViewAdapter gridViewAdapter;
        private GridViewAdapter mGridViewAdapter;
        private VoiceRecordManager mVoiceRecordManager;
        private String uid = "";
        private int[] voiceImageId;
        private Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                }
        };
        private User user;
        private String from;
        private String groupId;
        private GroupTableMessage mGroupTableMessage;
        private boolean exit=false;


        @Override
        public void initView() {
                initMiddleView();
                initBottomView();
        }

        private void initMiddleView() {
                record_container = (RelativeLayout) findViewById(R.id.r1_chat_middle_voice_container);
                record_display = (ImageView) findViewById(R.id.iv_chat_middle_voice_display);
                record_tip = (TextView) findViewById(R.id.tv_chat_middle_voice_tip);
                mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swl_chat_refresh);
                display = (RecyclerView) findViewById(R.id.rcy_chat_display);
        }


        private void initBottomView() {
                add = (Button) findViewById(R.id.btn_chat_bottom_add);
                emotion = (Button) findViewById(R.id.btn_chat_bottom_emotion);
                input = (EditText) findViewById(R.id.et_chat_bottom_input);
                speak = (Button) findViewById(R.id.btn_chat_bottom_speak);
                send = (Button) findViewById(R.id.btn_chat_bottom_send);
                voice = (Button) findViewById(R.id.btn_chat_bottom_voice);
                keyboard = (Button) findViewById(R.id.btn_chat_bottom_keyboard);
                l1_more = (LinearLayout) findViewById(R.id.l1_chat_bottom_more);
                mViewPager = (ViewPager) findViewById(R.id.vp_chat_bottom_emotion);
                r1_emotion = (RelativeLayout) findViewById(R.id.r1_chat_bottom_emotion);
                l1_add = (LinearLayout) findViewById(R.id.l1_chat_bottom_add);
                picture = (TextView) findViewById(R.id.tv_chat_bottom_picture);
                camera = (TextView) findViewById(R.id.tv_chat_bottom_camera);
                location = (TextView) findViewById(R.id.tv_chat_bottom_location);
                add.setOnClickListener(this);
                emotion.setOnClickListener(this);
                input.addTextChangedListener(this);
                input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                                if (hasFocus) {
                                        LogUtil.e("鑱氱劍");
                                        scrollToBottom();
                                        if (l1_more.getVisibility() == View.VISIBLE) {
                                                l1_more.setVisibility(View.GONE);
                                        }
                                }
                        }
                });
                voice.setOnClickListener(this);
                send.setOnClickListener(this);
                keyboard.setOnClickListener(this);
                location.setOnClickListener(this);
                picture.setOnClickListener(this);
                camera.setOnClickListener(this);
                speak.setOnTouchListener(this);
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                                mSwipeRefreshLayout.setRefreshing(true);
                                BaseMessage message = mAdapter.getData(0);
                                LoadMessage(message);
                        }
                });
        }


        @Override
        public void initData() {
                from = getIntent().getStringExtra("from");
                if (from.equals("person")) {
                        user = (User) getIntent().getSerializableExtra("user");
                        uid = user.getObjectId();
                } else if (from.equals("group")) {
                        groupId = getIntent().getStringExtra("groupId");
                        mGroupTableMessage = MessageCacheManager.getInstance().getGroupTableMessage(groupId);
                }
                initActionBar();
//                 澹伴煶闊抽噺鍙樺寲鍥剧墖璧勬簮
                voiceImageId = new int[]{R.mipmap.chat_icon_voice1, R.mipmap.chat_icon_voice2, R.mipmap.chat_icon_voice3, R.mipmap.chat_icon_voice4,
                        R.mipmap.chat_icon_voice5, R.mipmap.chat_icon_voice6};
                initRecordManager();
                initEmotionInfo();
                mLinearLayoutManager = new LinearLayoutManager(this);
                display.setLayoutManager(mLinearLayoutManager);
//                display.setHasFixedSize(true);
                display.setItemAnimator(new DefaultItemAnimator());
//                mAdapter = new ChatAdapter(this, null);
                mAdapter = new ChatMessageAdapter();
                mAdapter.setOnItemClickListener(this);
                display.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                                        hideSoftInpuutView();
                                        CommonUtils.hideSoftInput(ChatActivity.this,input);
                                }
                        }
                });
                display.setAdapter(mAdapter);
                IntentFilter intentFilter = new IntentFilter(Constant.NEW_MESSAGE_ACTION);
                intentFilter.setPriority(20);
                registerReceiver(mReceiver, intentFilter);
                refreshData();
                scrollToBottom();
                registerRxBus();
        }


        private void registerRxBus() {
                RxBusManager.getInstance().registerEvent(GroupInfoEvent.class, new Consumer<GroupInfoEvent>() {
                        @Override
                        public void accept(GroupInfoEvent groupInfoEvent) {
//                                鍒锋柊杩囨潵鐨勶紝鏇存柊涓嬬兢缁撴瀯娑堟伅
                                mGroupTableMessage = MessageCacheManager.getInstance().getGroupTableMessage(groupId);
                                if (mGroupTableMessage != null) {
                                        LogUtil.e(mGroupTableMessage);
                                }
                                int type = groupInfoEvent.getType();
                                String content = groupInfoEvent.getContent();
                                switch (type) {
                                        case GroupInfoEvent.TYPE_GROUP_NICK:
                                                List<BaseMessage> messageList = mAdapter.getData();
                                                if (messageList.size() > 0) {
                                                        for (BaseMessage message :
                                                                messageList) {
                                                                if (message.getBelongId().equals(UserManager.getInstance().getCurrentUserObjectId())) {
                                                                        message.setBelongNick(mGroupTableMessage.getGroupNick());
                                                                }
                                                        }
                                                }
                                                mAdapter.notifyDataSetChanged();
                                                break;
                                        case GroupInfoEvent.TYPE_GROUP_NAME:
                                                String title = mGroupTableMessage.getGroupName() + "(" + mGroupTableMessage.getGroupNumber().size() + ")";
                                                getCustomTitle().setText(title);
                                                break;
                                        case GroupInfoEvent.TYPE_GROUP_NOTIFICATION:
                                                LogUtil.e("杩欓噷瑕佸仛缇ら€氱煡鐨勭晫闈㈠睍绀? + content);
                                                break;
                                        case GroupInfoEvent.TYPE_GROUP_DESCRIPTION:
                                                LogUtil.e("杩欓噷瑕佸仛缇ゆ弿杩扮殑鐣岄潰灞曠ず" + content);
                                                break;
                                        case GroupInfoEvent.TYPE_GROUP_AVATRA:
                                                LogUtil.e("杩欓噷瑕佸仛缇ゅご鍍忕殑鐣岄潰灞曠ず" + content);
                                                break;
                                        case GroupInfoEvent.TYPE_GROUP_NUMBER:
                                                if (groupId != null) {
                                                        LogUtil.e("杩欓噷閫氱煡鎴愬憳鐨勫彉鍖? + content);
                                                        if (content.equals(groupId)) {
                                                                exit=true;
                                                                Toast.makeText(ChatActivity.this, "浣犲凡缁忚鎻愬嚭璇ョ兢", Toast.LENGTH_SHORT).show();
                                                        }
                                                }
                                                break;
                                        default:
                                                break;
                                }
                        }
                }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                                LogUtil.e("rxbus浼犻€掑嚭鐜板紓甯?);
                                if (throwable != null) {
                                        LogUtil.e(throwable.getMessage());
                                }
                        }
                });
        }

        private void initActionBar() {
                String title;
                String avatar;
                if (from.equals("person")) {
                        title = user.getNick();
                        avatar = null;
                } else {
                        title = mGroupTableMessage.getGroupName() + "(" + mGroupTableMessage.getGroupNumber().size() + ")";
                        avatar = mGroupTableMessage.getGroupAvatar();
                }
                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setNeedNavigation(true);
                toolBarOption.setAvatar(avatar);
                toolBarOption.setTitle(title);
                if (from.equals("group")) {
                        toolBarOption.setRightText("淇℃伅");
                        toolBarOption.setRightListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        GroupInfoActivity.start(ChatActivity.this, groupId, Constant.REQUEST_CODE_EDIT_GROUP_INFO);
                                }
                        });
                }
                setToolBar(toolBarOption);
        }
        

        private void refreshData() {
                LogUtil.e("refreshData12356");
                if (from.equals("person")) {
                        if (ChatDB.create().updateReceivedChatMessageReaded(user, true) > 0) {
                                LogUtil.e("鏇存柊璇ョ敤鎴锋墍鍙戞潵鐨勬秷鎭负宸茶鐘舵€佹垚鍔?);
                        } else {
                                LogUtil.e("鏇存柊璇ョ敤鎴锋墍鍙戞潵鐨勬秷鎭负宸茶鐘舵€佸け璐?);
                        }
                        mAdapter.clearAllData();
                        mAdapter.getData().addAll(ChatDB.create().queryChatMessagesFromDB(uid, 1, 0));
                } else {
                        if (ChatDB.create().updateReceivedGroupChatMessageReaded(groupId, true)) {
                                LogUtil.e("鏇存柊璇ョ兢鎵€鍙戞潵鐨勬秷鎭负宸茶鐘舵€佹垚鍔?);
                        } else {
                                LogUtil.e("鏇存柊璇ョ兢鎵€鍙戞潵鐨勬秷鎭负宸茶鐘舵€佸け璐?);
                        }
                        LogUtil.e("鍒锋柊缇ゆ秷鎭?);
                        mAdapter.clearAllData();
                        mAdapter.getData().addAll(ChatDB.create().queryGroupChatMessageFromDB(groupId, 1, 0));
//                        mAdapter.getData().clear();
//                        mAdapter.getData().addAll(ChatDB.create().queryGroupChatMessageFromDB(groupId, 1, 0));
                        mAdapter.notifyDataSetChanged();
                }
                PushMessageReceiver.registerListener(this);
        }

        private ChatMessageReceiver mReceiver = new ChatMessageReceiver();

        @Override
        public void updateData(Object o) {

        }


        private class ChatMessageReceiver extends BroadcastReceiver {

                @Override
                public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(Constant.NEW_MESSAGE_ACTION)) {
                                if (intent.getStringExtra("from").equals("person") && from.equals("person")) {
                                        List<ChatMessage> list = (List<ChatMessage>) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        processNewMessage(list);
                                } else if (intent.getStringExtra("from").equals("group")) {
                                        LogUtil.e("杩欓噷瀹炴椂骞挎挱鎺ユ敹鍒板疄鏃剁兢娑堟伅鎷?);
                                        GroupChatMessage message = (GroupChatMessage) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        if (from.equals("group") && message.getGroupId().equals(groupId)) {
                                                if (MsgManager.getInstance().saveRecentAndChatGroupMessage(message)) {

                                                        boolean isRefresh=intent.getBooleanExtra("isRefresh",false);
                                                        if (!isRefresh&&!hasFocus) {
                                                                ChatNotificationManager.getInstance(ChatActivity.this).sendGroupMessageNotification(message, ChatActivity.this);
                                                        }

                                                        onNewGroupChatMessageCome(message);
                                                }
                                        } else {
                                                LogUtil.e("鍏朵粬缇ゆ秷鎭氦鐢变富鐣岄潰澶勭悊");
                                                return;
                                        }
                                } else if (intent.getStringExtra("from").equals("groupTable")) {
                                        LogUtil.e("杩欓噷鑱婂ぉ鐣岄潰鎺ユ敹鍒扮兢缁撴瀯娑堟伅锛岀洿鎺ヨ繑鍥烇紝涓嶅彇娑堝箍鎾紝璁╀富鐣岄潰鎺ュ彈骞挎挱骞惰В鏋?);
                                        return;
                                } else if (intent.getStringExtra("from").equals("table")) {
                                        LogUtil.e("瀹炴椂妫€娴嬪緱鍒扮殑缇ょ粨鏋勬秷鎭埌鍟?杩欐槸鑱婂ぉ鐣岄潰");
                                        GroupTableMessage message = (GroupTableMessage) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        if (from.equals("group") && message.getGroupId().equals(groupId)) {
//                                            鑷繁鐨勭兢鐣岄潰
//                                                鍒锋柊缇ょ粨鏋勬秷鎭?                                                mGroupTableMessage = MessageCacheManager.getInstance().getGroupTableMessage(groupId);
                                                LogUtil.e(mGroupTableMessage);
                                                if (message.getGroupAvatar() != null) {
                                                        Glide.with(ChatActivity.this).load(message.getGroupAvatar()).into(getIcon());
                                                }
                                                getCustomTitle().setText(message.getGroupName() + "(" + message.getGroupNumber().size() + ")");
                                        } else {

                                                LogUtil.e("鍏朵粬缇ょ粨鏋勮〃浜ょ敱涓荤晫闈㈠鐞?);
                                                return;
                                        }
                                }
                        }
                        abortBroadcast();
                }
        }

        private void processNewMessage(List<ChatMessage> list) {

                for (final ChatMessage message : list
                        ) {
                        if (!message.getBelongId().equals(uid)) {
//                                濡傛灉涓嶆槸璇ョ敤鎴风殑娑堟伅锛屼笉鎺ュ彈
                                return;
                        }
                        message.setSendStatus(Constant.SEND_STATUS_SUCCESS);
                        message.setReadStatus(Constant.RECEIVE_UNREAD);
                        switch (message.getTag()) {
//                                閭€璇锋秷鎭簲璇ヤ笉浼氬嚭鐜板湪杩欓噷
                                case Constant.TAG_AGREE:
                                        UserManager.getInstance().addNewFriend(message.getBelongId(), UserManager.getInstance().getCurrentUserObjectId(), new AddFriendCallBackListener() {
                                                @Override
                                                public void onSuccess(User user) {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃垚鍔熸娴嬪埌鏈鐨勫悓鎰忔秷鎭苟淇濆瓨鍒版暟鎹簱涓垚鍔?);
                                                        UserCacheManager.getInstance().addContact(user);
                                                        MsgManager.getInstance().saveAndUploadReceiverMessage(true, message);
                                                        onAgreeMessageCome(message);
                                                }

                                                @Override
                                                public void onFailed(BmobException e) {
                                                        LogUtil.e("淇濆瓨浠庢湇鍔″櫒涓婃娴嬪緱鏉ョ殑鍚屾剰娑堟伅澶辫触");
                                                }
                                        });
                                        break;
                                case Constant.TAG_ADD_FRIEND:
//                                                        涓嶅鐞?                                        break;
                                case Constant.TAG_ASK_READ:
                                        LogUtil.e("鎺ユ敹鍒扮殑鍥炴墽宸茶鏍囩娑堟伅");
                                        LogUtil.e(message);
                                        LogUtil.e("杩欓噷鏇存柊宸茶娑堟伅涓哄凡璇?);
                                        MsgManager.getInstance().updateReadTagMsgReaded(message.getConversationId(), message.getCreateTime());
                                        if (ChatDB.create().hasFriend(message.getBelongId()) && ChatDB.create().isBlackUser(message.getBelongId())) {
                                                LogUtil.e("鐢变簬鏄粦鍚嶅崟锛岀洿鎺ユ洿鏂版湇鍔″櫒涓婄殑宸茶鍥炴墽娑堟伅涓哄凡璇荤姸鎬?);
                                                MsgManager.getInstance().updateMsgReaded(false, message.getConversationId(), message.getCreateTime());
                                        } else {
                                                if (MsgManager.getInstance().uploadAndUpdateChatMessageReadStatus(message, true) > 0) {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃垚鍔熸娴嬪埌鏈鐨勫凡璇诲洖鎵ф秷鎭?);
                                                        onAskReadMessageCome(message);
                                                } else {
                                                        LogUtil.e("淇濆瓨浠庢湇鍔″櫒涓婃娴嬪緱鏉ョ殑鍥炴墽宸茶娑堟伅澶辫触");
                                                }
                                        }
                                        break;
                                default:
//                                                        榛樿鍒欐槸鑱婂ぉ娑堟伅
                                        if (ChatDB.create().hasFriend(message.getBelongId()) && ChatDB.create().isBlackUser(message.getBelongId())) {
//                                                                                榛戝悕鍗曠敤鎴?                                                LogUtil.e("榛戝悕鍗曠敤鎴凤紝涓嶆帴鍙楁秷鎭?);
                                                MsgManager.getInstance().updateMsgReaded(false, message.getConversationId(), message.getCreateTime());
                                                LogUtil.e("鏇存柊鏈嶅姟鍣ㄤ笂榛戝悕鍗曞彂鏉ョ殑娑堟伅涓哄凡璇?);
                                        } else {
                                                if (MsgManager.getInstance().saveAndUploadReceiverMessage(true, message)) {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃垚鍔熸娴嬪埌鏈鐨勮亰澶╂秷鎭?);
                                                        onNewChatMessageCome(message);
                                                } else {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃娴嬫湭璇荤殑鑱婂ぉ娑堟伅澶辫触");
                                                }
                                        }
                                        break;

                        }

                }
        }

        private void initEmotionInfo() {
                List<View> list = new ArrayList<>();
                emotionFaceList = FaceTextUtil.getFaceTextList();
                for (int i = 0; i < 2; i++) {
                        list.add(getGridView(i));
                }
                mViewPager.setAdapter(new MyViewPagerAdapter(list));
        }

        private View getGridView(int i) {
                View emotionView = LayoutInflater.from(this).inflate(R.layout.emotion1, null);
                GridView gridView = (GridView) emotionView.findViewById(R.id.gv_display);
                if (i == 0) {
                        gridView.setAdapter(gridViewAdapter = new GridViewAdapter(this, emotionFaceList.subList(0, 21)));
                        gridView.setTag(gridViewAdapter);
                } else {
                        gridView.setAdapter(mGridViewAdapter = new GridViewAdapter(this, emotionFaceList.subList(21, emotionFaceList.size())));
                        gridView.setTag(mGridViewAdapter);
                }
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                GridViewAdapter gridViewAdapter = (GridViewAdapter) parent.getTag();
                                FaceText faceText = (FaceText) gridViewAdapter.getItem(position);
                                String content = faceText.getText();
                                if (input != null) {
                                        int startIndex = input.getSelectionStart();
                                        CharSequence content1 = input.getText().insert(startIndex, content);
                                        input.setText(FaceTextUtil.toSpannableString(ChatActivity.this.getApplicationContext(), content1.toString()));
//                                        閲嶆柊瀹氫綅鍏夋爣浣嶇疆
                                        CharSequence info = input.getText();
                                        if (info instanceof Spannable) {
                                                Spannable spannable = (Spannable) info;
                                                Selection.setSelection(spannable, startIndex + content.length());
                                        }
                                }
                        }
                });
                return emotionView;
        }

        private void initRecordManager() {
                mVoiceRecordManager = VoiceRecordManager.getInstance();
                mVoiceRecordManager.setOnVoiceChangeListener(new VoiceRecordManager.OnVoiceChangerListener() {
                        /**
                         * 鑼冨洿锛?~5
                         * @param value  鍊?                         */
                        @Override
                        public void onVoiceColumnChange(int value) {
                                record_display.setImageResource(voiceImageId[value]);
                        }

                        @Override
                        public void onRecordTimeChange(int time, String localVoicePath) {
                                if (time > VoiceRecordManager.MAX_RECORD_TIME) {
                                        time = VoiceRecordManager.MAX_RECORD_TIME;
//                                杩欓噷鍋氫竴涓満鍒讹紝闃叉閿欒閲嶅鍙戝娆¤闊?                                        speak.setPressed(false);
                                        speak.setClickable(false);
                                        sendVoiceMessage(localVoicePath, time);
                                        handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                        speak.setClickable(true);
                                                }
                                        }, 1000);
                                }
                        }
                });
        }

        /**
         * 鍙戦€佸０闊虫枃浠?         *
         * @param localPath  澹伴煶鏂囦欢鐨勫綋鍦板瓨鍌ㄨ矾寰?         * @param recordTime 褰曞埗鐨勬椂闂?         */
        private void sendVoiceMessage(String localPath, int recordTime) {
                if (exit) {
                        Toast.makeText(this, "宸茬粡琚彁鍑鸿缇わ紝涓嶈兘鍙戦€佹秷鎭?, Toast.LENGTH_SHORT).show();
                        return;
                }

                if (isBlack) {
                        ToastUtils.showShortToast("瀵规柟涓洪粦鍚嶅崟锛屼笉鑳藉彂閫佹秷鎭?);
                        return;
                }

                MsgManager manager = MsgManager.getInstance();
                String id;
                final boolean result;
                if (from.equals("person")) {
                        id = uid;
                        result = true;
                } else {
                        result = false;
                        id = groupId;
                }
                manager.sendVoiceMessage(result, id, localPath, recordTime, new SendFileListener() {
                        @Override
                        public void onProgress(int progress) {

                        }

                        @Override
                        public void onStart(BaseMessage message) {
                                mAdapter.addData(message);
//                                mAdapter.addMessages(message);
                                LogUtil.e("璇煶鏂囦欢");
                                if (result) {
                                        LogUtil.e(((ChatMessage) message));
                                } else {
                                        LogUtil.e(((GroupChatMessage) message));
                                }
                                scrollToBottom();
                        }

                        @Override
                        public void onSuccess() {
                                LogUtil.e("鍙戦€佽闊虫垚鍔?);
                                mAdapter.notifyDataSetChanged();
                                scrollToBottom();
                        }

                        @Override
                        public void onFailed(BmobException e) {
                                LogUtil.e("鍙戦€佽闊冲け璐? + e.getMessage() + e.getErrorCode());
                                mAdapter.notifyDataSetChanged();
                                scrollToBottom();
                        }
                });
        }

        /**
         * 鍔犺浇璇ユ秷鎭箣鍓嶇殑10璋冩秷鎭?         *
         * @param message 鑱婂ぉ娑堟伅
         */
        private void LoadMessage(BaseMessage message) {
                List<BaseMessage> list = new ArrayList<>();
                if (from.equals("person")) {
                        if (message != null) {
                                list.addAll(ChatDB.create().queryChatMessagesFromDB(uid, 1, Long.valueOf(message.getCreateTime())));
                        } else {
                                list.addAll(ChatDB.create().queryChatMessagesFromDB(uid, 1, 0));
                        }
                } else {
                        if (message != null) {
                                list.addAll(ChatDB.create().queryGroupChatMessageFromDB(groupId, 1, Long.valueOf(message.getCreateTime())));
                        } else {
                                list.addAll(ChatDB.create().queryGroupChatMessageFromDB(groupId, 1, 0));
                        }
                }
                int size = list.size();
                LogUtil.e("鍚戜笂鎷夊彇鐨勬秷鎭ぇ灏忎负" + size);
                mAdapter.addData(0, list);
//                mAdapter.addMessages(list);
                mLinearLayoutManager.scrollToPositionWithOffset(size, 0);
                mSwipeRefreshLayout.setRefreshing(false);
        }


        private boolean isBlack=false;


        @Override
        protected void onResume() {
                super.onResume();
                if (uid != null) {
                        isBlack=ChatDB.create().isBlackUser(uid);
                }
                PushMessageReceiver.registerListener(this);
        }

        @Override
        protected boolean isNeedHeadLayout() {
                return true;
        }

        @Override
        protected boolean isNeedEmptyLayout() {
                return false;
        }

        @Override
        protected int getContentLayout() {
                return R.layout.activity_chat;
        }

        @Override
        protected void onPause() {
                super.onPause();
                PushMessageReceiver.unRegisterListener(this);
        }

        private void scrollToBottom() {
                mLinearLayoutManager.scrollToPositionWithOffset(mAdapter.getItemCount() - 1, 0);
        }

        @Override
        public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.btn_chat_bottom_add) {
                        if (l1_more.getVisibility() == View.GONE) {
                                l1_more.setVisibility(View.VISIBLE);
                                l1_add.setVisibility(View.VISIBLE);
                                r1_emotion.setVisibility(View.GONE);
                                CommonUtils.hideSoftInput(this, input);
                        } else if (l1_add.getVisibility() == View.VISIBLE) {
                                l1_more.setVisibility(View.GONE);
                        } else {
                                r1_emotion.setVisibility(View.GONE);
                                l1_add.setVisibility(View.VISIBLE);
                        }

                } else if (i == R.id.btn_chat_bottom_emotion) {
                        if (speak.getVisibility() == View.VISIBLE) {
                                keyboard.setVisibility(View.GONE);
                                speak.setVisibility(View.GONE);
                                input.setVisibility(View.VISIBLE);
                                send.setVisibility(View.VISIBLE);
                        }
                        if (l1_more.getVisibility() == View.GONE) {
                                l1_more.setVisibility(View.VISIBLE);
                                l1_add.setVisibility(View.GONE);
                                r1_emotion.setVisibility(View.VISIBLE);
                                CommonUtils.hideSoftInput(this, input);
                        } else if (r1_emotion.getVisibility() == View.VISIBLE) {
                                l1_more.setVisibility(View.GONE);
                        } else {
                                l1_add.setVisibility(View.GONE);
                                r1_emotion.setVisibility(View.VISIBLE);
                                CommonUtils.hideSoftInput(this, input);
                        }

                } else if (i == R.id.btn_chat_bottom_voice) {
                        if (l1_more.getVisibility() == View.VISIBLE) {
                                l1_more.setVisibility(View.GONE);
                        }
                        keyboard.setVisibility(View.VISIBLE);
                        speak.setVisibility(View.VISIBLE);
                        input.setVisibility(View.GONE);
                        voice.setVisibility(View.GONE);
                        CommonUtils.hideSoftInput(this, input);

                } else if (i == R.id.btn_chat_bottom_keyboard) {
                        keyboard.setVisibility(View.GONE);
                        voice.setVisibility(View.VISIBLE);
                        speak.setVisibility(View.GONE);
                        input.setVisibility(View.VISIBLE);

                } else if (i == R.id.btn_chat_bottom_send) {
                        if (l1_more.getVisibility() == View.VISIBLE) {
                                l1_more.setVisibility(View.GONE);
                        }
                        if (TextUtils.isEmpty(input.getText().toString().trim())) {
                                ToastUtils.showShortToast(getString(R.string.chat_input_empty));
                                input.setText("");
                                return;
                        }
                        sendTextMessage(input.getText().toString().trim());
                        input.setText("");

                } else if (i == R.id.tv_chat_bottom_camera) {//                                File imageFile = FileUtil.newFile(FileUtil.newDir(Constant.IMAGE_CACHE_DIR).getAbsolutePath() + System.currentTimeMillis() + ".jpg");
//                                localImagePath = imageFile.getAbsolutePath();
                        localImagePath = CommonImageLoader.getInstance().takePhoto(this, Constant.REQUEST_CODE_TAKE_PICTURE).getAbsolutePath();

                } else if (i == R.id.tv_chat_bottom_picture) {
                        CommonImageLoader.getInstance().pickPhoto(this, Constant.REQUEST_CODE_SELECT_FROM_LOCAL);

                } else if (i == R.id.tv_chat_bottom_location) {
                        Intent locationIntent = new Intent(this, MapActivity.class);
                        startActivityForResult(locationIntent, Constant.REQUEST_MAP);

                }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == Activity.RESULT_OK) {
                        switch (requestCode) {
                                case Constant.REQUEST_CODE_TAKE_PICTURE:
                                        LogUtil.e("鎷嶇収寰楀埌鐨勫浘鐗囪矾寰? + localImagePath);
                                        LogUtil.e("鍥剧墖鏄惁瀛樺湪" + new File(localImagePath).exists());
                                        LogUtil.e("鍥剧墖鐨勫ぇ灏忎负" + new File(localImagePath).length());
                                        sendImageMessage(localImagePath);
                                        break;
                                case Constant.REQUEST_CODE_SELECT_FROM_LOCAL:
                                        if (data != null) {
                                                Uri uri = data.getData();
                                                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                                                if (cursor != null && cursor.moveToFirst()) {
                                                        int index = cursor.getColumnIndex("_data");
                                                        String path = cursor.getString(index);
                                                        if (path != null) {
                                                                LogUtil.e("鑾峰彇鐨勫埌鐨勫浘鐗囪矾寰? + path);
                                                                sendImageMessage(path);
                                                        } else {
                                                                ToastUtils.showShortToast("鎵句笉鍒颁綘瑕佺殑鍥剧墖");
                                                        }
                                                }
                                                if (cursor != null && !cursor.isClosed()) {
                                                        cursor.close();
                                                }
                                        }
                                        break;
                                case Constant.REQUEST_CODE_LOCATION:
                                        break;
                                case Constant.REQUEST_MAP:
                                        if (data != null) {
                                                String localPath = data.getStringExtra("localPath");
                                                String longitude = data.getStringExtra("longitude");
                                                String latitude = data.getStringExtra("latitude");
                                                String address = data.getStringExtra("address");
                                                LogUtil.e("localPath:" + localPath + "    longitude:" + longitude + "      latitude:" + latitude + "     address:" + address);
                                                sendLocationChatMessage(localPath, latitude, longitude, address);
                                        }
                                        break;
                                case Constant.REQUEST_CODE_EDIT_GROUP_INFO:
//                                        refreshData();
//                                        String nick = data.getStringExtra("nick");
//                                        List<BaseMessage> list = mAdapter.getAllData();
//                                        for (BaseMessage message :
//                                                list) {
//                                                if (message.getBelongId().equals(UserCacheManager.getInstance().getUser().getObjectId())) {
//                                                        message.setBelongNick(nick);
//                                                }
//                                        }
//                                        閫氱煡鏁版嵁鍒锋柊锛屽洜涓虹兢鏄电О鏀瑰彉
                                        mAdapter.clearAllData();
                                        mAdapter.getData().addAll(ChatDB.create().queryGroupChatMessageFromDB(groupId, 1, 0));
                                        mAdapter.notifyDataSetChanged();
                                        break;

                        }
                }
        }

        /**
         * 鍙戦€佸湴鍧€娑堟伅
         *
         * @param localPath 鍥剧墖
         * @param latitude  绾害
         * @param longitude 缁忓害
         * @param address   鍦板潃
         */
        private void sendLocationChatMessage(String localPath, String latitude, String longitude, String address) {
                if (exit) {
                        Toast.makeText(this, "宸茬粡琚彁鍑鸿缇わ紝涓嶈兘鍙戦€佹秷鎭?, Toast.LENGTH_SHORT).show();
                        return;
                }
                if (isBlack) {
                        ToastUtils.showShortToast("瀵规柟涓洪粦鍚嶅崟锛屼笉鑳藉彂閫佹秷鎭?);
                        return;
                }
                if (l1_more.getVisibility() == View.VISIBLE) {
                        l1_more.setVisibility(View.GONE);
                        l1_add.setVisibility(View.GONE);
                        r1_emotion.setVisibility(View.GONE);
                }
                String id;
                boolean result;
                if (from.equals("person")) {
                        id = uid;
                        result = true;
                } else {
                        result = false;
                        id = groupId;
                }
                MsgManager msgManager = MsgManager.getInstance();
                String content = localPath + "," + latitude + "," + longitude + "," + address;
                msgManager.sendLocationMessage(result, id, content,
                        new SendFileListener() {
                                @Override
                                public void onProgress(int progress) {
                                }

                                @Override
                                public void onStart(BaseMessage message) {
                                        mAdapter.addData(message);
                                        scrollToBottom();
                                }

                                @Override
                                public void onSuccess() {
                                        mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFailed(BmobException e) {
                                        mAdapter.notifyDataSetChanged();
                                }
                        });

        }

        /**
         * 鍙戦€佸浘鐗囨秷鎭?         *
         * @param localImagePath 鍥剧墖鐨勬湰鍦板湴鍧€
         */
        private void sendImageMessage(String localImagePath) {
                if (exit) {
                        Toast.makeText(this, "宸茬粡琚彁鍑鸿缇わ紝涓嶈兘鍙戦€佹秷鎭?, Toast.LENGTH_SHORT).show();
                        return;
                }
                if (isBlack) {
                        ToastUtils.showShortToast("瀵规柟涓洪粦鍚嶅崟锛屼笉鑳藉彂閫佹秷鎭?);
                        return;
                }

                if (l1_more.getVisibility() == View.VISIBLE) {
                        l1_more.setVisibility(View.GONE);
                        l1_add.setVisibility(View.GONE);
                        r1_emotion.setVisibility(View.GONE);
                }
                String id;
                boolean result;
                if (from.equals("person")) {
                        result = true;
                        id = uid;
                } else {
                        result = false;
                        id = groupId;
                }
                final MsgManager msgManager = MsgManager.getInstance();
                msgManager.sendImageMessage(result, id, localImagePath, new SendFileListener() {
                        @Override
                        public void onProgress(int progress) {
//                                杩欓噷鍙互鍋氬浘鐗囦笂浼犵殑鎿嶄綔
                        }

                        @Override
                        public void onStart(BaseMessage message) {


                                LogUtil.e("onStart");
                                LogUtil.e("鍥剧墖娑堟伅");
                                mAdapter.addData(message);
                                scrollToBottom();
                                File file = new File(message.getContent());
                                while (file.exists() && file.length() == 0) {
                                        try {
                                                Thread.sleep(200);
                                        } catch (InterruptedException e) {
                                                e.printStackTrace();
                                                LogUtil.e("寮傚父");
                                        }
                                }
                        }

                        @Override
                        public void onSuccess() {
                                LogUtil.e("鍙戦€佸浘鐗囨垚鍔?);
                                mAdapter.notifyDataSetChanged();
                                scrollToBottom();
                        }

                        @Override
                        public void onFailed(BmobException e) {
                                mAdapter.notifyDataSetChanged();
                                scrollToBottom();
                                LogUtil.e("鍙戦€佸浘鐗囨秷鎭け璐? + e.getMessage() + e.getErrorCode());
                        }
                });
        }

        /**
         * 鍙戦€佹枃鏈秷鎭?         *
         * @param content 鍐呭
         */
        private void sendTextMessage(final String content) {
                if (exit) {
                        Toast.makeText(this, "宸茬粡琚彁鍑鸿缇わ紝涓嶈兘鍙戦€佹秷鎭?, Toast.LENGTH_SHORT).show();
                        return;
                }
                if (isBlack) {
                        ToastUtils.showShortToast("瀵规柟涓洪粦鍚嶅崟锛屼笉鑳藉彂閫佹秷鎭?);
                        return;
                }
                MsgManager manager = MsgManager.getInstance();
                BaseMessage baseMessage;
                String id;
                if (from.equals("person")) {
                        id = uid;
                        baseMessage = manager.createChatMessage(content, id, Constant.TAG_MSG_TYPE_TEXT);
                } else {
                        id = groupId;
                        LogUtil.e("缇D涓? + id);
                        baseMessage = manager.createGroupChatMessage(content, id, Constant.TAG_MSG_TYPE_TEXT);
                }
                mAdapter.addData(baseMessage);
                scrollToBottom();
                manager.sendTextMessage(false, baseMessage,
                        new OnSendMessageListener() {
                                @Override
                                public void onSending() {
                                        mAdapter.notifyDataSetChanged();
                                        scrollToBottom();
                                        LogUtil.e("鍙戦€佷腑.......");
                                }

                                @Override
                                public void onSuccess() {
////                                        杩欓噷涓嶉渶瑕侀€氱煡鏁版嵁鐨勬敼鍙?鍥犱负鍦ㄥ垱寤鸿娑堟伅鐨勬椂鍊欏凡缁忛粯璁や负鍙戦€佹垚鍔熺姸鎬?//                                        finalBaseMessage.setSendStatus(Constant.SEND_STATUS_SUCCESS);
                                        LogUtil.e("鍙戦€佹垚鍔?);
                                        mAdapter.notifyDataSetChanged();
                                        scrollToBottom();
                                }

                                @Override
                                public void onFailed(BmobException e) {
                                        LogUtil.e("鍙戦€佸け璐? + e.getMessage() + e.getErrorCode());
                                        mAdapter.notifyDataSetChanged();
                                        scrollToBottom();
                                }
                        }
                );
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                scrollToBottom();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                        voice.setVisibility(View.GONE);
                        send.setVisibility(View.VISIBLE);
                } else {
                        voice.setVisibility(View.VISIBLE);
                        send.setVisibility(View.GONE);
                }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public boolean onLongClick(View v) {
                return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                                if (!CommonUtils.isSupportSdcard()) {
                                        ToastUtils.showShortToast("闇€瑕丼D鍗℃敮鎸?");
                                        return false;
                                }
                                speak.setPressed(true);
                                record_container.setVisibility(View.VISIBLE);
                                record_tip.setText(R.string.chat_middle_voice_tip);
                                mVoiceRecordManager.startRecording(uid);
                                return true;
                        case MotionEvent.ACTION_MOVE:
                                if (event.getY() < 0) {
                                        record_tip.setText(R.string.chat_middle_voice_tip1);
                                        record_tip.setTextColor(Color.RED);
                                } else {
                                        record_tip.setText(R.string.chat_middle_voice_tip);
                                        record_tip.setTextColor(Color.WHITE);
                                }
                                return true;
                        case MotionEvent.ACTION_UP:
                                speak.setPressed(false);
                                record_container.setVisibility(View.INVISIBLE);
                                if (event.getY() < 0) {
                                        mVoiceRecordManager.cancelRecord();
                                } else {
                                        int recordTime = mVoiceRecordManager.stopRecord();
                                        if (recordTime > 1) {
                                                sendVoiceMessage(mVoiceRecordManager.getVoiceFilePath(), recordTime);
                                        } else {
                                                ToastUtils.showShortToast("褰曞埗鏃堕棿杩囩煭");
                                        }
                                }
                                return true;
                }
                return false;
        }

        @Override
        public void onPictureClick(final View view, final String contentUrl, int position) {
                LogUtil.e("鐐瑰嚮浜嗗浘鐗?);
                List<BaseMessage> list = mAdapter.getData();
                List<BaseMessage> imageList = new ArrayList<>();
                for (BaseMessage baseMessage :
                        list) {
                        if (baseMessage.getMsgType().equals(Constant.TAG_MSG_TYPE_IMAGE)) {
                                imageList.add(baseMessage);
                        }
                }
                List<ImageItem> result = new ArrayList<>();
                int picturePosition = 0;
                LogUtil.e("contentUrl" + contentUrl);
                for (int i = 0; i < imageList.size(); i++) {
                        String content = imageList.get(i).getContent();
                        LogUtil.e("content" + content);
                        if (content.contains("&")) {
                                content = content.split("&")[0];
                        }
                        if (content.equals(contentUrl)) {
                                picturePosition = i;
                                LogUtil.e("鐩哥瓑position" + i);
                        }
                        ImageItem imageItem = new ImageItem();
                        imageItem.setPath(content);
                        result.add(imageItem);
                }
                LogUtil.e("position" + picturePosition);
                BasePreViewActivity.startBasePreview(this, result, picturePosition);
//                ImageDisplayActivity.start(ChatActivity.this, view, contentUrl);
        }

        @Override
        public void onAvatarClick(View view, int position, boolean isRight) {
                Intent intent = new Intent(this, UserInfoActivity.class);
                if (from.equals("person")) {
                        intent.putExtra("uid", isRight ? UserManager.getInstance().getCurrentUserObjectId() : user.getObjectId());
                } else {
                        GroupChatMessage message = (GroupChatMessage) mAdapter.getData(position);
                        User user = new User();
                        user.setObjectId(message.getBelongId());
                        user.setNick(message.getBelongNick());
                        user.setUsername(message.getBelongUserName());
                        user.setAvatar(message.getBelongAvatar());
                        user.setSex(false);
                        if (!isRight) {
                                intent.putExtra("uid", message.getBelongId());
                                intent.putExtra("user", user);
                        } else {
                                intent.putExtra("uid", UserManager.getInstance().getCurrentUserObjectId());
                        }
                }
                startActivity(intent);
        }

        @Override
        public void onMessageClick(View view, int position) {
                Toast.makeText(this, "1娑堟伅涓讳綋鐐瑰嚮鏆傛棤鎿嶄綔", Toast.LENGTH_SHORT).show();
                BaseMessage chatMessage = mAdapter.getData(position);
                if (chatMessage != null && chatMessage.getMsgType().equals(Constant.TAG_MSG_TYPE_LOCATION)) {
//                        杩欓噷鐐瑰嚮浜嗗湴鍧€鏄剧ず鍥剧墖锛岃繘鍏apActivity
                        Intent intent = new Intent(this, MapActivity.class);
                        String[] content = chatMessage.getContent().split(",");
                        for (int i = 0; i < content.length; i++) {
                                LogUtil.e("鐐瑰嚮鍦板浘鐨勫唴瀹?" + content[i]);
                        }

                        String latitude = null;
                        String longitude = null;
                        String address = null;
                        if (content.length == 5) {
                                latitude = content[2];
                                longitude = content[3];
                                address = content[4];
                        } else if (content.length == 4) {
                                latitude = content[1];
                                longitude = content[2];
                                address = content[3];
                        }
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("address", address);
                        intent.putExtra("destination", "browse");
                        startActivity(intent);
                }
        }

        @Override
        public void onItemResendClick(View view, final BaseMessage baseMessage, int position) {
//                閲嶅彂娑堟伅
                Integer msgType = baseMessage.getMsgType();
                String id;
                final MsgManager msgManager = MsgManager.getInstance();
                if (msgType.equals(Constant.TAG_MSG_TYPE_TEXT)) {
                        msgManager.resendTextChatMessage(baseMessage,
                                new OnSendMessageListener() {

                                        @Override
                                        public void onSending() {
                                                mAdapter.notifyDataSetChanged();
                                                LogUtil.e("鍙戦€佷腑.........");
                                        }

                                        @Override
                                        public void onSuccess() {
                                                baseMessage.setSendStatus(Constant.SEND_STATUS_SUCCESS);
                                                mAdapter.notifyDataSetChanged();
                                                LogUtil.e("閲嶅彂娑堟伅鎴愬姛");
                                        }

                                        @Override
                                        public void onFailed(BmobException e) {
                                                mAdapter.notifyDataSetChanged();
                                                LogUtil.e("閲嶅彂娑堟伅澶辫触" + e.getMessage() + e.getErrorCode());
                                        }
                                });
                } else if (msgType.equals(Constant.TAG_MSG_TYPE_IMAGE)) {

                        msgManager.resendImageChatMessage(baseMessage, this);

                } else if (msgType.equals(Constant.TAG_MSG_TYPE_VOICE)) {

                        msgManager.resendVoiceChatMessage(baseMessage, this);

                } else if (msgType.equals(Constant.TAG_MSG_TYPE_LOCATION)) {

                        msgManager.resendLocationChatMessage(baseMessage, this);
                } else {
//                        鏈煡绫诲瀷
                }
        }


        @Override
        protected void onDestroy() {
                if (mReceiver != null) {
                        unregisterReceiver(mReceiver);
                        mReceiver = null;
                }
                PushMessageReceiver.unRegisterListener(this);
                mVoiceRecordManager.setOnVoiceChangeListener(null);
                RxBusManager.getInstance().unSubscrible(this);
                super.onDestroy();
        }

        @Override
        public void onNewChatMessageCome(ChatMessage message) {
                LogUtil.e("鑱婂ぉ鐣岄潰");
//                鍦ㄨ亰澶╃晫闈㈡帴鏀跺埌娑堟伅鐩存帴鏇存柊涓哄凡璇荤姸鎬?                ChatDB.create().updateChatMessageReadStatus(message.getConversationId(), message.getCreateTime()
                        , true);
                mAdapter.addData(message);
                scrollToBottom();
        }

        @Override
        public void onNewGroupChatMessageCome(GroupChatMessage message) {

                ChatDB.create().updateReceivedGroupChatMessageReaded(groupId, true);
                mAdapter.addData(message);
                scrollToBottom();
        }


        private volatile boolean hasFocus = true;

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
                this.hasFocus = hasFocus;
        }

        @Override
        public void onAskReadMessageCome(ChatMessage chatMessage) {
                LogUtil.e("璇锋眰璇诲彇鐨勬秷鎭潵浜嗘病鏈?2");

                if (from.equals("person")) {
                        List<BaseMessage> list = mAdapter.getData();
                        for (BaseMessage message :
                                list) {
                                if (((ChatMessage) message).getConversationId().equals(chatMessage.getConversationId()) && message.getCreateTime().equals(chatMessage.getCreateTime())) {
                                        LogUtil.e("鎵惧埌瑕佹眰璇诲彇鐨勬秷鎭媺");
                                        message.setReadStatus(Constant.READ_STATUS_READED);
                                        break;
                                }
                        }
                }
                mAdapter.notifyDataSetChanged();
//                scrollToBottom();
        }

        @Override
        public void onNetWorkChanged(boolean isConnected) {
                if (!isConnected) {
                        Toast.makeText(this, "缃戠粶杩炴帴澶辫触", Toast.LENGTH_SHORT).show();
                }
        }

        @Override
        public void onAddFriendMessageCome(ChatMessage chatMessage) {
//                涓嶅鐞?
        }

        @Override
        public void onAgreeMessageCome(ChatMessage chatMessage) {
                LogUtil.e("閭€璇锋秷鎭埌chatActivity");
                mAdapter.addData(chatMessage);
                scrollToBottom();
        }

        @Override
        public void onOffline() {
                Toast.makeText(this, "浣犵殑甯愬彿宸插湪鍏朵粬鍦版柟鐧诲綍锛屾墍浠ヨ杩笅绾?, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }

        @Override
        public void onGroupTableMessageCome(GroupTableMessage message) {

        }

//        閲嶆柊鍙戠殑鍥炶皟

        @Override
        public void onProgress(int progress) {
        }
//        閲嶆柊鍙戠殑鍥炶皟

        @Override
        public void onStart(BaseMessage message) {
                mAdapter.notifyDataSetChanged();

        }
//        閲嶆柊鍙戠殑鍥炶皟

        @Override
        public void onSuccess() {
                mAdapter.notifyDataSetChanged();
        }
//        閲嶆柊鍙戠殑鍥炶皟

        @Override
        public void onFailed(BmobException e) {
                mAdapter.notifyDataSetChanged();
        }

        private class GridViewAdapter extends BaseAdapter {
                private List<FaceText> mFaceTextList = new ArrayList<>();
                private Context mContext;

                GridViewAdapter(Context context, List<FaceText> faceTextList) {
                        this.mContext = context;
                        if (faceTextList != null && faceTextList.size() > 0) {
                                mFaceTextList.clear();
                                mFaceTextList.addAll(faceTextList);
                        }
                }

                @Override
                public int getCount() {
                        return mFaceTextList.size();
                }

                @Override
                public Object getItem(int position) {
                        return mFaceTextList.get(position);
                }

                @Override
                public long getItemId(int position) {
                        return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                        ViewHolder viewHolder;
                        if (convertView == null) {
                                viewHolder = new ViewHolder();
                                convertView = LayoutInflater.from(mContext).inflate(R.layout.emtion_item, parent, false);
                                viewHolder.display = (ImageView) convertView.findViewById(R.id.iv_emotion_item_display);
                                convertView.setTag(viewHolder);
                        } else {
                                viewHolder = (ViewHolder) convertView.getTag();
                        }
                        FaceText item = mFaceTextList.get(position);
                        viewHolder.display.setImageDrawable(mContext.getResources().getDrawable(mContext.getResources().getIdentifier(item.getText().substring(1), "mipmap", mContext.getPackageName())));
                        return convertView;
                }
        }

        private class ViewHolder {
                ImageView display;
        }

        private class MyViewPagerAdapter extends PagerAdapter {
                private List<View> mViews = new ArrayList<>();

                private MyViewPagerAdapter(List<View> list) {
                        if (list != null && list.size() > 0) {
                                mViews.clear();
                                mViews.addAll(list);
                        }
                }

                @Override
                public int getCount() {
                        return mViews.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                        return view == object;
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                        container.addView(mViews.get(position));
                        return mViews.get(position);
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                        container.removeView(mViews.get(position));
                }
        }


}

