package com.example.chat.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.chat.R;
import com.example.chat.adapter.MenuDisplayAdapter;
import com.example.chat.base.Constant;
import com.example.chat.bean.ChatMessage;
import com.example.chat.bean.GroupChatMessage;
import com.example.chat.bean.GroupTableMessage;
import com.example.chat.bean.SharedMessage;
import com.example.chat.bean.User;
import com.example.chat.bean.WeatherInfoBean;
import com.example.chat.db.ChatDB;
import com.example.chat.listener.AddFriendCallBackListener;
import com.example.chat.listener.LocationChangedListener;
import com.example.chat.listener.OnDragDeltaChangeListener;
import com.example.chat.listener.OnMessageReceiveListener;
import com.example.chat.listener.OnNetWorkChangedListener;
import com.example.chat.manager.ChatNotificationManager;
import com.example.chat.manager.LocationManager;
import com.example.chat.manager.MessageCacheManager;
import com.example.chat.manager.MsgManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.manager.UserManager;
import com.example.chat.receiver.NetWorkChangedReceiver;
import com.example.chat.receiver.PushMessageReceiver;
import com.example.chat.service.PollService;
import com.example.chat.ui.fragment.ContactsFragment;
import com.example.chat.ui.fragment.InvitationFragment;
import com.example.chat.ui.fragment.RecentFragment;
import com.example.chat.ui.fragment.ShareMessageFragment;
import com.example.chat.util.LogUtil;
import com.example.chat.view.MainDragLayout;
import com.example.commonlibrary.BaseActivity;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemClickListener;
import com.example.commonlibrary.baseadapter.manager.WrappedLinearLayoutManager;
import com.example.commonlibrary.cusotomview.RoundAngleImageView;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.utils.ToastUtils;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity implements OnDragDeltaChangeListener, OnMessageReceiveListener, View.OnClickListener, OnNetWorkChangedListener, LocationChangedListener {
        private Fragment[] mFragments = new Fragment[4];
        private int currentPosition;
        private MainDragLayout container;
        private RecyclerView menuDisplay;
        private List<String> data = new ArrayList<>();
        private MenuDisplayAdapter menuAdapter;
        private String from;
        private ImageView bg;
        private RoundAngleImageView avatar;
        private TextView nick;
        private TextView signature;
        private RelativeLayout headLayout;
        private User user;
        private TextView net;
        private NetWorkChangedReceiver netWorkReceiver;
        private TextView weatherCity;
        private TextView weatherTemperature;
        private WeatherInfoBean mWeatherInfoBean;
        private LinearLayout bottomLayout;
        private NewMessageReceiver mReceiver;
        private ShareMessageReceiver shareReceiver;


        @Override
        protected void onNewIntent(Intent intent) {
                super.onNewIntent(intent);
                LogUtil.e("onNewIntent");
                if (intent.getSerializableExtra("url_share_message") != null) {
                        SharedMessage sharedMessage = (SharedMessage) intent.getSerializableExtra("url_share_message");
                        notifyUrlSharedMessageAdd(sharedMessage);
                        return;
                }
                from = intent.getStringExtra(Constant.NOTIFICATION_TAG);
                if (from != null && from.equals(Constant.NOTIFICATION_TAG_ADD)) {
                        addOrReplaceFragment(mFragments[2], R.id.fl_content_container);
                } else {
                        addOrReplaceFragment(mFragments[0], R.id.fl_content_container);
                        currentPosition = 0;
                }
        }


        @Override
        public void initView() {
                RecentFragment recentFragment = new RecentFragment();
                ContactsFragment contactsFragment = new ContactsFragment();
                InvitationFragment invitationFragment = new InvitationFragment();
                ShareMessageFragment shareMessageFragment = new ShareMessageFragment();
                mFragments[0] = recentFragment;
                mFragments[1] = contactsFragment;
                mFragments[2] = invitationFragment;
                mFragments[3] = shareMessageFragment;
                container = (MainDragLayout) findViewById(R.id.dl_activity_main_drag_container);
                menuDisplay = (RecyclerView) findViewById(R.id.rev_menu_display);
                nick = (TextView) findViewById(R.id.tv_menu_nick);
                signature = (TextView) findViewById(R.id.tv_menu_signature);
                avatar = (RoundAngleImageView) findViewById(R.id.riv_menu_avatar);
                headLayout = (RelativeLayout) findViewById(R.id.rl_menu_head_layout);
                bg = (ImageView) findViewById(R.id.iv_main_bg);
                net = (TextView) findViewById(R.id.tv_main_net);
                weatherCity = (TextView) findViewById(R.id.tv_menu_weather_city);
                weatherTemperature = (TextView) findViewById(R.id.tv_menu_weather_temperature);
                bottomLayout = (LinearLayout) findViewById(R.id.ll_menu_bottom_container);
                bg.setAlpha((float) 0.0);
                menuDisplay.setLayoutManager(new WrappedLinearLayoutManager(this));
                container.setListener(this);
                headLayout.setOnClickListener(this);
                net.setOnClickListener(this);
                bottomLayout.setOnClickListener(this);
                initActionBarView();
//                getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_bg));
        }


        /**
         * 杩欓噷閲嶆柊鏋勫缓涓€涓ご閮ㄥ竷灞€锛屽洜涓哄皝瑁呯殑鍩虹被涓殑澶撮儴甯冨眬涓庢粦鍔ㄥ彂鐢熷啿绐?         */
        private RoundAngleImageView icon_1;
        private TextView right_1;
        private TextView title_1;
        private ImageView rightImage_1;
        protected ImageView back_1;

        private void initActionBarView() {
                icon_1 = (RoundAngleImageView) findViewById(R.id.riv_header_layout_icon);
                right_1 = (TextView) findViewById(R.id.tv_header_layout_right);
                title_1 = (TextView) findViewById(R.id.tv_header_layout_title);
                rightImage_1 = (ImageView) findViewById(R.id.iv_header_layout_right);
                back_1 = (ImageView) findViewById(R.id.iv_header_layout_back);
                Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolBar);
        }


        @Override
        public void initData() {
                if (from == null || from.equals("") || from.equals(Constant.NOTIFICATION_TAG_MESSAGE) || from.equals(Constant.NOTIFICATION_TAG_AGREE)
                        || from.equals(Constant.NOTIFICATION_TAG_GROUP_MESSAGE)) {
                        addOrReplaceFragment(mFragments[0], R.id.fl_content_container);
                } else if (from.equals(Constant.NOTIFICATION_TAG_ADD)) {
                        addOrReplaceFragment(mFragments[2], R.id.fl_content_container);
                }
                data.add("鑱婂ぉ");
                data.add("濂藉弸");
                data.add("閭€璇?);
                data.add("鍔ㄦ€?);
                menuAdapter = new MenuDisplayAdapter();
                menuAdapter.setOnItemClickListener(new OnSimpleItemClickListener() {
                        @Override
                        public void onItemClick(int position, View view) {
                                if (currentPosition != position) {
                                        addOrReplaceFragment(mFragments[position]);
                                        currentPosition = position;
                                }
                                closeMenu();
                        }
                });
                menuDisplay.setAdapter(menuAdapter);
                menuAdapter.addData(data);
//                娉ㄥ唽娑堟伅鎺ュ彈鍣?                initReceiver();
//                bindPollService(5);
                getWeatherInfo();
                initUserInfo();
                if (getIntent().getBooleanExtra("isFirstLogin", false)) {
                        ToastUtils.showShortToast("棣栨鐧诲綍锛岃缃綘鐨勪釜浜鸿祫鏂欏惂^_^");
                        Intent intent = new Intent(this, EditUserInfoActivity.class);
                        startActivityForResult(intent, Constant.REQUEST_CODE_EDIT_USER_INFO);
                }
                if (from != null && from.equals(Constant.NOTIFICATION_TAG_GROUP_MESSAGE)) {
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("id", getIntent().getStringExtra("groupId"));
                        startActivity(intent);
                }
        }

        private void initReceiver() {
                IntentFilter intentFilter = new IntentFilter(Constant.NEW_MESSAGE_ACTION);
//                杩欓噷璁剧疆浼樺厛绾ц姣擟hatActivity浣?鑱婂ぉ鐣岄潰涓€鏃︽帴鍙楀埌娑堟伅锛岃繖閲屽氨涓嶉渶瑕佸啀鎺ュ彈
                intentFilter.setPriority(10);
//                鑱婂ぉ娑堟伅鐨勭洃鍚?                registerReceiver(mReceiver = new NewMessageReceiver(), new IntentFilter(Constant.NEW_MESSAGE_ACTION));
                IntentFilter filter = new IntentFilter();
//                璇磋娑堟伅鎺ュ彈
                filter.addAction(Constant.NEW_SHARE_MESSAGE_ACTION);
//                璇磋娑堟伅鏀瑰彉瀹炴椂鐩戝惉
                filter.addAction(Constant.NOTIFY_CHANGE_ACTION);
                registerReceiver(shareReceiver = new ShareMessageReceiver(), filter);
//                缃戠粶鐘舵€佹敼鍙樼洃鍚?                registerReceiver(netWorkReceiver = new NetWorkChangedReceiver(), new IntentFilter(Constant.NETWORK_CONNECTION_CHANGE));
                netWorkReceiver.registerListener(this);
//                鍗曡亰鍜岀郴缁熸帹閫佹秷鎭洃鍚?                PushMessageReceiver.registerListener(this);
        }

        private void initUserInfo() {
                user = UserManager.getInstance().getCurrentUser();
                if (user!=null) {
                        Glide.with(this).load(user.getAvatar()).centerCrop()
                                .into(avatar);
                        nick.setText(user.getNick());
                        updateMenuBg();
                        if (user.getSignature() == null) {
                                signature.setText("^_^1璁剧疆灞炰簬浣犵殑涓€х鍚嶅惂^_^");
                        } else {
                                signature.setText(user.getSignature());
                        }
                }
        }


        public void updateMenuBg() {
                Glide.with(this).load(UserManager.getInstance().getCurrentUser().getWallPaper()).into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                container.setBackground(resource);
                        }
                });
        }

        private void getWeatherInfo() {
                mWeatherInfoBean = new WeatherInfoBean();
                List<String> addressList = LocationManager.getInstance().getLocationList();
                if (addressList != null) {
                        String cityName = addressList.get(4).substring(addressList.get(4).indexOf("鐪?) + 1);
                        LogUtil.e("cityName" + cityName);
                        mWeatherInfoBean.setCity(cityName);
                        startSearchLiveWeather();
                } else {
                        LocationManager.getInstance().registerLocationListener(this);
                }
        }


        private void bindPollService(int second) {
                Intent intent = new Intent(this, PollService.class);
                intent.putExtra("time", second);
                startService(intent);
//                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), second * 1000, PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), PollService.class), PendingIntent.FLAG_UPDATE_CURRENT));
        }


        /**
         * 閫氱煡璇磋item鐨勫疄鏃剁洃鍚粦瀹?         *
         * @param objectId 璇磋id
         * @param isAdd    鏄惁瀹炴椂鐩戝惉璇ヨ璇?         */
        public void notifySharedMessageChanged(String objectId, boolean isAdd) {
                ((RecentFragment) mFragments[0]).notifySharedMessageChanged(objectId, isAdd);
        }


        public void initActionBar(String title) {
                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setTitle(title);
                toolBarOption.setNeedNavigation(false);
                if (user != null) {
                        toolBarOption.setAvatar(user.getAvatar());
                }
                setToolBar(toolBarOption);
        }


        @Override
        public void setToolBar(ToolBarOption option) {
                if (option.getAvatar() != null) {
                        icon_1.setVisibility(View.VISIBLE);
                        Glide.with(this).load(option.getAvatar()).into(icon_1);
                } else {
                        icon_1.setVisibility(GONE);
                }

                if (option.getRightResId() != 0) {
                        right_1.setVisibility(GONE);
                        rightImage_1.setVisibility(View.VISIBLE);
                        rightImage_1.setImageResource(option.getRightResId());
                        rightImage_1.setOnClickListener(option.getRightListener());
                } else if (option.getRightText() != null) {
                        right_1.setVisibility(View.VISIBLE);
                        rightImage_1.setVisibility(GONE);
                        right_1.setText(option.getRightText());
                        right_1.setOnClickListener(option.getRightListener());
                } else {
                        right_1.setVisibility(GONE);
                        rightImage_1.setVisibility(GONE);
                }
                if (option.getTitle() != null) {
                        title_1.setVisibility(View.VISIBLE);
                        title_1.setText(option.getTitle());
                } else {
                        title_1.setVisibility(GONE);
                }
                if (option.isNeedNavigation()) {
                        back_1.setVisibility(View.VISIBLE);
                        back_1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        finish();
                                }
                        });
                } else {
                        back_1.setVisibility(GONE);
                }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.main_menu_layout, menu);
                return true;
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.main_item_search) {
                        Intent intent = new Intent(this, SearchActivity.class);
                        startActivity(intent);

                } else if (i == R.id.main_sub_item_add) {
                        ToastUtils.showShortToast("鐐瑰嚮浜嗘坊鍔犲ソ鍙?);
                        SearchFriendActivity.start(this);

                } else if (i == R.id.main_sub_item_create) {
                        ToastUtils.showShortToast("鐐瑰嚮浜嗗垱寤虹兢");
                        Intent selectIntent = new Intent(this, SelectedFriendsActivity.class);
                        selectIntent.putExtra("from", "createGroup");
                        startActivity(selectIntent);

                } else if (i == R.id.main_sub_item_settings) {
                        ToastUtils.showShortToast("鐐瑰嚮浜嗚缃?);
                        SettingsActivity.start(this, Constant.REQUEST_CODE_EDIT_USER_INFO);

                } else if (i == R.id.main_sub_item_happy) {
                        HappyActivity.startActivity(this);
//                                Toast.makeText(this, "鍚姩demo", Toast.LENGTH_SHORT).show();
//                                Intent demo = new Intent(this, DemoActivity.class);
//                                startActivity(demo);

                } else if (i == R.id.main_sub_item_bg) {
                        ToastUtils.showShortToast("鐐瑰嚮浜嗚儗鏅?);
                        Intent wallPaperIntent = new Intent(this, WallPaperActivity.class);
                        wallPaperIntent.putExtra("from", "wallpaper");
                        startActivityForResult(wallPaperIntent, Constant.REQUEST_CODE_SELECT_WALLPAPER);
                }
                return true;
        }


        @Override
        public void OnNetWorkChanged(boolean isConnected, int type) {
                if (isConnected) {
//                        杩欓噷鍒ゆ柇缃戠粶鐨勮繛鎺ョ被鍨?                        if (type == ConnectivityManager.TYPE_WIFI) {
                                LogUtil.e("wife绫诲瀷鐨?");
                                bindPollService(5);
                        } else {
                                LogUtil.e("闈瀢ifi绫诲瀷");
                                bindPollService(6);
                        }
                        net.setVisibility(GONE);
                        if (mWeatherInfoBean == null) {
                                getWeatherInfo();
                        }
                } else {
                        net.setVisibility(View.VISIBLE);
                }
        }


        private void startSearchLiveWeather() {
                WeatherSearchQuery query = new WeatherSearchQuery(mWeatherInfoBean.getCity(), WeatherSearchQuery.WEATHER_TYPE_LIVE);
                WeatherSearch weatherSearch = new WeatherSearch(this);
                weatherSearch.setQuery(query);
                weatherSearch.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
                        @Override
                        public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
                                LogUtil.e("鏌ヨ寰楀埌鐨勫ぉ姘旂粨鏋? + i);
                                if (i == 1000) {
                                        if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null) {
                                                LocalWeatherLive localWeatherLive = localWeatherLiveResult.getLiveResult();
                                                mWeatherInfoBean.setRealTime(localWeatherLive.getReportTime());
                                                mWeatherInfoBean.setWeatherStatus(localWeatherLive.getWeather());
                                                mWeatherInfoBean.setTemperature(localWeatherLive.getTemperature() + "掳");
                                                mWeatherInfoBean.setWind(localWeatherLive.getWindDirection() + "椋?      " + localWeatherLive
                                                        .getWindPower() + "绾?);
                                                mWeatherInfoBean.setHumidity("婀垮害       " + localWeatherLive.getHumidity() + "%");
                                                weatherCity.setText(mWeatherInfoBean.getCity());
                                                weatherTemperature.setText(mWeatherInfoBean.getTemperature());
                                        } else {
                                                LogUtil.e("鑾峰彇鍒扮殑澶╂皵淇℃伅涓虹┖");
                                        }
                                } else {
                                        LogUtil.e("鑾峰彇鍒扮殑澶╂皵淇℃伅澶辫触" + i);
                                }
                        }

                        @Override
                        public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
                        }
                });
                weatherSearch.searchWeatherAsyn();
        }

        @Override
        public void onLocationChanged(List<String> addressList, double latitude, double longitude) {
                LogUtil.e("Main瀹氫綅鍒板暒鍟﹀暒");
                LocationManager.getInstance().unregisterLocationListener(this);
                String cityName = addressList.get(4).substring(addressList.get(4).indexOf("鐪?) + 1);
                mWeatherInfoBean = new WeatherInfoBean();
                mWeatherInfoBean.setCity(cityName);
                startSearchLiveWeather();
        }

        @Override
        public void onLocationFailed(int errorId, String errorMsg) {
                LogUtil.e("Main瀹氫綅澶辫触" + errorMsg + errorId);
                List<String> addressList = LocationManager.getInstance().getLocationList();
                if (addressList != null) {
                        LocationManager.getInstance().unregisterLocationListener(this);
                        String cityName = addressList.get(4).substring(addressList.get(4).indexOf("鐪?) + 1);
                        mWeatherInfoBean.setCity(cityName);
                        startSearchLiveWeather();
                }
        }

        public void notifyContactAndRecent(ChatMessage chatMessage) {
                ((RecentFragment) mFragments[0]).updateRecentData(chatMessage.getToId());
                notifyMenuUpdate();
                updateContactsData(chatMessage.getToId());
        }

        public void notifyMenuUpdate() {
                menuAdapter.notifyDataSetChanged();
        }

        @Override
        public void updateData(Object o) {

        }


        private class NewMessageReceiver extends BroadcastReceiver {
                @Override
                public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(Constant.NEW_MESSAGE_ACTION)) {
                                if (intent.getBooleanExtra("isLogout", false)) {
                                        LogUtil.e("涓荤晫闈㈡鍦ㄥ叧闂?);
                                        finish();
                                        return;
                                }
                                LogUtil.e("1鎺ュ彈鍒板箍鎾彂鏉ョ殑娑堟伅");
                                if (intent.getStringExtra("from").equals("person")) {
                                        List<ChatMessage> list;
                                        list = (List<ChatMessage>) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        onProcessNewMessages(list);
                                } else if (intent.getStringExtra("from").equals("group")) {
                                        LogUtil.e("杩欓噷瀹炴椂骞挎挱鎺ユ敹鍒板疄鏃剁兢娑堟伅鎷?);
                                        GroupChatMessage message = (GroupChatMessage) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        if (MsgManager.getInstance().saveRecentAndChatGroupMessage(message)) {
                                                onNewGroupChatMessageCome(message);
                                        }
                                } else if (intent.getStringExtra("from").equals("groupTable")) {
                                        List<GroupTableMessage> list;
                                        list = (List<GroupTableMessage>) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        for (GroupTableMessage message :
                                                list) {
                                                LogUtil.e(message);
                                                message.setReadStatus(Constant.READ_STATUS_READED);
                                                message.setSendStatus(Constant.SEND_STATUS_SUCCESS);
                                                MessageCacheManager.getInstance().addGroupTableMessage(message);
                                                MsgManager.getInstance().updateGroupTableMessageReaded(message,null);
                                                ChatDB.create().saveGroupTableMessage(message);
                                                onGroupTableMessageCome(message);
                                                MsgManager.getInstance().queryGroupChatMessage(message.getGroupId(), new FindListener<GroupChatMessage>() {
                                                        @Override
                                                        public void onSuccess(List<GroupChatMessage> list) {
                                                                LogUtil.e("鎷夊幓寰楀埌鐨勬渶杩戠殑缇よ亰娑堟伅濡備笅");
                                                                for (GroupChatMessage groupChatMessage
                                                                        : list
                                                                        ) {
                                                                        LogUtil.e(groupChatMessage);
                                                                        groupChatMessage.setReadStatus(Constant.RECEIVE_UNREAD);
                                                                        if (MsgManager.getInstance().saveRecentAndChatGroupMessage(groupChatMessage)) {
                                                                                onNewGroupChatMessageCome(groupChatMessage);
                                                                        }
                                                                }
                                                        }

                                                        @Override
                                                        public void onError(int i, String s) {
                                                                LogUtil.e("鎷夊彇鏈€杩戠殑缇ゆ秷鎭け璐? + s + i);
                                                        }
                                                });
                                        }
                                } else if (intent.getStringExtra("from").equals("table")) {
                                        LogUtil.e("瀹炴椂妫€娴嬪緱鍒扮殑缇や富鐨勭兢缁撴瀯鏇存敼娑堟伅");
                                        GroupTableMessage message = (GroupTableMessage) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        ((RecentFragment) mFragments[0]).updateRecentData(message.getGroupId());
                                        LogUtil.e(message);
                                }
                                abortBroadcast();
                        }
                }
        }

        private void onProcessNewMessages(List<ChatMessage> list) {
                String tag;
                for (final ChatMessage chatMessage :
                        list) {
                        tag = chatMessage.getTag();
                        LogUtil.e("tag锛? + tag);
                        chatMessage.setSendStatus(Constant.SEND_STATUS_SUCCESS);
                        chatMessage.setReadStatus(Constant.RECEIVE_UNREAD);
                        ChatNotificationManager.getInstance(this).sendChatMessageNotification(chatMessage, this);
                        switch (tag) {
                                case Constant.TAG_ADD_FRIEND:
                                        if (MsgManager.getInstance().saveAndUpdateInvitationMsg(chatMessage) > 0) {
                                                LogUtil.e("鍦ㄦ湇鍔″櫒涓婃垚鍔熸娴嬪埌鏈鐨勯個璇锋秷鎭?);
                                                onAddFriendMessageCome(chatMessage);
                                        } else {
                                                LogUtil.e("淇濆瓨浠庢湇鍔″櫒涓婃娴嬪緱鏉ョ殑閭€璇锋秷鎭け璐?);
                                        }
                                        break;
                                case Constant.TAG_AGREE:
                                        UserManager.getInstance().addNewFriend(chatMessage.getBelongId(), UserManager.getInstance().getCurrentUserObjectId(), new AddFriendCallBackListener() {
                                                @Override
                                                public void onSuccess(User user) {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃垚鍔熸娴嬪埌鏈鐨勫悓鎰忔秷鎭苟淇濆瓨鍒版暟鎹簱涓垚鍔?);
                                                        UserCacheManager.getInstance().addContact(user);
                                                        MsgManager.getInstance().saveAndUploadReceiverMessage(true, chatMessage);
                                                        onAgreeMessageCome(chatMessage);
                                                }

                                                @Override
                                                public void onFailed(BmobException e) {
                                                        LogUtil.e("淇濆瓨浠庢湇鍔″櫒涓婃娴嬪緱鏉ョ殑鍚屾剰娑堟伅澶辫触");
                                                }
                                        });

                                        break;
                                case Constant.TAG_ASK_READ:
                                        LogUtil.e("鎺ユ敹鍒扮殑鍥炴墽宸茶鏍囩娑堟伅");
                                        LogUtil.e(chatMessage);
                                        MsgManager.getInstance().updateReadTagMsgReaded(chatMessage.getConversationId(), chatMessage.getCreateTime());
                                        if (ChatDB.create().hasFriend(chatMessage.getBelongId()) && ChatDB.create().isBlackUser(chatMessage.getBelongId())) {
                                                LogUtil.e("鐢变簬鏄粦鍚嶅崟锛岀洿鎺ユ洿鏂版湇鍔″櫒涓婄殑宸茶鍥炴墽娑堟伅涓哄凡璇荤姸鎬?);
                                                MsgManager.getInstance().updateMsgReaded(false, chatMessage.getConversationId(), chatMessage.getCreateTime());
                                        } else {
                                                if (MsgManager.getInstance().uploadAndUpdateChatMessageReadStatus(chatMessage, true) > 0) {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃垚鍔熸娴嬪埌鏈鐨勫凡璇诲洖鎵ф秷鎭?);
                                                        onAskReadMessageCome(chatMessage);
                                                } else {
                                                        LogUtil.e("淇濆瓨浠庢湇鍔″櫒涓婃娴嬪緱鏉ョ殑鍥炴墽宸茶娑堟伅澶辫触");
                                                }
                                        }
                                        break;
                                default:
//                                                                        鑱婂ぉ娑堟伅杩欓噷瑕佸垽鏂槸鍚︽槸榛戝悕鍗曠敤鎴?                                        if (ChatDB.create().hasFriend(chatMessage.getBelongId()) && ChatDB.create().isBlackUser(chatMessage.getBelongId())) {
//                                                                                榛戝悕鍗曠敤鎴?                                                LogUtil.e("榛戝悕鍗曠敤鎴凤紝涓嶆帴鍙楁秷鎭?);
                                                MsgManager.getInstance().updateMsgReaded(false, chatMessage.getConversationId(), chatMessage.getCreateTime());
                                                LogUtil.e("鏇存柊鏈嶅姟鍣ㄤ笂榛戝悕鍗曞彂鏉ョ殑娑堟伅涓哄凡璇?);
                                        } else {
                                                if (MsgManager.getInstance().saveAndUploadReceiverMessage(true, chatMessage)) {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃垚鍔熸娴嬪埌鏈鐨勮亰澶╂秷鎭?);
                                                        onNewChatMessageCome(chatMessage);
                                                } else {
                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃娴嬫湭璇荤殑鑱婂ぉ娑堟伅澶辫触");
                                                }
                                        }
                                        break;
                        }
                }
        }


        @Override
        protected void onResume() {
                LogUtil.e("111222MainActivity锛歰nResume");
                super.onResume();
//                PushMessageReceiver.registerListener(this);
        }


//        杩欓噷璇存槑涓嬶紝鐢变簬澶村熀绫诲皝瑁呯殑澶撮儴甯冨眬涓庝晶婊戝彂鐢熷啿绐侊紝杩欓噷鎴戜滑鐩存帴鍦ㄤ富甯冨眬涓婂啓鍏ュご閮ㄥ竷灞€,鎵€浠ヨ繖閲屽純鐢ㄥ熀绫荤殑澶撮儴甯冨眬

        @Override
        protected boolean isNeedHeadLayout() {
                return false;
        }

        @Override
        protected boolean isNeedEmptyLayout() {
                return false;
        }

        @Override
        protected int getContentLayout() {
                return R.layout.activity_main_chat;
        }


        @Override
        protected void onPause() {
                LogUtil.e("MainActivity锛歰nPause");
                super.onPause();
//                PushMessageReceiver.unRegisterListener(this);
        }


        @Override
        public void onDrag(View view, float delta) {
                ViewHelper.setAlpha(bg, delta);
                ViewHelper.setAlpha(icon_1, (1 - delta));
        }


        @Override
        public void onCloseMenu() {
//                褰撲晶婊戝畬鍏ㄥ叧闂殑鏃跺€欒皟鐢?                if (ViewHelper.getAlpha(bg) != 0) {
                        ViewHelper.setAlpha(bg, 0);
                }
                if (ViewHelper.getAlpha(icon_1) != 1) {
                        ViewHelper.setAlpha(icon_1, 1);
                }
        }

        @Override
        public void onOpenMenu() {
                if (ViewHelper.getAlpha(bg) != 1) {
                        ViewHelper.setAlpha(bg, 1);
                }
                if (ViewHelper.getAlpha(icon_1) != 0) {
                        ViewHelper.setAlpha(icon_1, 0);
                }
                //                褰撲晶婊戝畬鍏ㄦ墦寮€鐨勬椂鍊欒皟鐢?        }

        public void closeMenu() {
                if (container.getCurrentState() == MainDragLayout.DRAG_STATE_OPEN) {
                        container.closeMenu();
                }
        }

        @Override
        public void onNewChatMessageCome(ChatMessage message) {
                LogUtil.e("211鏂版秷鎭暒鍟﹀暒");
                LogUtil.e(message);
                if (mFragments[0].isAdded()) {

                        ((RecentFragment) mFragments[0]).updateRecentData(message.getBelongId());
                        if (currentFragment instanceof RecentFragment) {
                        } else {
                                notifyMenuUpdate();
                        }
                } else {
                        addOrReplaceFragment(mFragments[0], R.id.fl_content_container);
                }
        }

        @Override
        public void onNewGroupChatMessageCome(GroupChatMessage message) {
                ChatNotificationManager.getInstance(this).sendGroupMessageNotification(message, this);
                if (mFragments[0].isAdded()) {
                        ((RecentFragment) mFragments[0]).updateRecentData(message.getGroupId());
                        if (currentFragment instanceof RecentFragment) {
                        } else {
                                notifyMenuUpdate();
                        }
                } else {
                        addOrReplaceFragment(mFragments[0], R.id.fl_content_container);
                }

        }

        @Override
        public void onAskReadMessageCome(ChatMessage chatMessage) {
                LogUtil.e("onAskReadMessageCome");
                notifyMenuUpdate();
//                涓嶅鐞?        }

        @Override
        public void onNetWorkChanged(boolean isConnected) {
        }

        @Override
        public void onAddFriendMessageCome(ChatMessage chatMessage) {
                LogUtil.e("onAddFriendMessageCome");
                if (mFragments[2].isAdded()) {
                        ((InvitationFragment) mFragments[2]).updateInvitationData(chatMessage);
                        if (currentFragment instanceof InvitationFragment) {

                        } else {
                                notifyMenuUpdate();
                        }
                } else {
                        addOrReplaceFragment(mFragments[2]);
                }
        }

        @Override
        public void onAgreeMessageCome(ChatMessage chatMessage) {
                LogUtil.e("onAgreeMessageCome");
                LogUtil.e("鍚屾剰娑堟伅鏍煎紡");
                LogUtil.e(chatMessage);
                onNewChatMessageCome(chatMessage);
                updateContactsData(chatMessage.getBelongId());
        }

        private void updateContactsData(String id) {
                if (mFragments[1].isAdded()) {
                        ((ContactsFragment) mFragments[1]).updateContactsData(id);
                        if (currentFragment instanceof ContactsFragment) {

                        } else {
                                notifyMenuUpdate();
                        }
                } else {
                        currentPosition=1;
                        addOrReplaceFragment(mFragments[1]);
                }
                ((RecentFragment) mFragments[0]).notifyUserAdd(id);

        }

        @Override
        public void onOffline() {
//                涓嬬嚎閫氱煡
                ToastUtils.showShortToast("浣犵殑甯愬彿鍦ㄥ叾浠栧湴鏂圭櫥褰曪紝琚揩涓嬬嚎");
                UserManager.getInstance().logout();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
        }

        @Override
        public void onGroupTableMessageCome(GroupTableMessage message) {
                LogUtil.e("onGroupTableMessageCome");
                LogUtil.e("缇ょ粨鏋勬秷鎭埌涓荤晫闈㈡媺");
//                杩欓噷寰楀埌鐨勭兢缁撴瀯娑堟伅浠呬粎鏄嚜宸辩殑缇ょ粨鏋勬秷鎭?                if (mFragments.length > 0) {
                        ((RecentFragment) mFragments[0]).notifyGroupTableMsgCome(message.getGroupId());
                }
        }

        public void openMenu() {
                if (container.getCurrentState() == MainDragLayout.DRAG_STATE_CLOSE) {
                        container.openMenu();
                }
        }

        /**
         * 鐐瑰嚮宸︿笂瑙掔殑鍥炬爣鐨勫洖璋?         *
         * @param view view
         */
        @Override
        public void onClick(View view) {
                LogUtil.e("渚ф粦鍏抽棴");
                int i = view.getId();
                if (i == R.id.dl_activity_main_drag_container) {
                        openMenu();

                } else if (i == R.id.rl_menu_head_layout) {//                                鐐瑰嚮杩涘叆涓汉淇℃伅鐣岄潰涔嬪墠锛屽厛瀹炴椂鐩戝惉涓汉淇℃伅鐣岄潰涓殑璇磋
                        Intent intent = new Intent(this, UserDetailActivity.class);
                        intent.putExtra("from", "me");
                        intent.putExtra("uid", UserManager.getInstance().getCurrentUserObjectId());
                        startActivityForResult(intent, Constant.REQUEST_CODE_EDIT_USER_INFO);

                } else if (i == R.id.tv_main_net) {
                        Intent settingIntent = new Intent();
                        settingIntent.setAction(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(settingIntent);

                } else if (i == R.id.ll_menu_bottom_container) {
                        Intent weatherIntent = new Intent(this, WeatherInfoActivity.class);
                        if (mWeatherInfoBean != null) {
                                weatherIntent.putExtra("WeatherInfo", mWeatherInfoBean);
                        }
                        startActivityForResult(weatherIntent, Constant.REQUEST_CODE_WEATHER_INFO);
                }
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == Activity.RESULT_OK) {
                        switch (requestCode) {
                                case Constant.REQUEST_CODE_EDIT_USER_INFO:
                                        user = (User) data.getSerializableExtra("user");
                                        nick.setText(user.getNick());
                                        signature.setText(user.getSignature());
                                        Glide.with(this).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).into(icon_1);
                                        ToastUtils.showShortToast("淇濆瓨涓汉淇℃伅鎴愬姛");
                                        Glide.with(this).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).into(avatar);
                                        break;
                                case Constant.REQUEST_CODE_WEATHER_INFO:
                                        LogUtil.e("杩斿洖澶╂皵鏁版嵁");
                                        WeatherInfoBean weatherInfoBean = (WeatherInfoBean) data.getSerializableExtra("WeatherInfo");
                                        if (weatherInfoBean != null) {
                                                mWeatherInfoBean = weatherInfoBean;
                                                weatherCity.setText(mWeatherInfoBean.getCity());
                                                weatherTemperature.setText(mWeatherInfoBean.getTemperature());
                                        }
                                        break;
                                case Constant.REQUEST_CODE_SELECT_WALLPAPER:
                                        updateMenuBg();
                                        break;
                        }
                }
        }

        @Override
        protected void onDestroy() {
                LogUtil.e("瀹氭椂鏈嶅姟鍙栨秷");
                PushMessageReceiver.unRegisterListener(this);
                Intent intent = new Intent(this, PollService.class);
                stopService(intent);
//                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                alarmManager.cancel(PendingIntent.getService(getApplicationContext(), 0, new Intent(getApplicationContext(), PollService.class), PendingIntent.FLAG_UPDATE_CURRENT));
                LogUtil.e("鐢ㄦ埛鏁版嵁鍙栨秷鎷夋媺");

//                鎶婂畾鏃舵娴嬬殑鏈嶅姟缁欏彇娑?                PushMessageReceiver.unRegisterListener(this);
                if (mReceiver != null) {
                        unregisterReceiver(mReceiver);
                        mReceiver = null;
                }
                if (shareReceiver != null) {
                        unregisterReceiver(shareReceiver);
                        shareReceiver = null;
                }

                if (netWorkReceiver != null) {
                        unregisterReceiver(netWorkReceiver);
                        netWorkReceiver.unregisterListener(this);
                        netWorkReceiver = null;
                }
                LogUtil.e("鐢ㄦ埛鏁版嵁鍙栨秷鎷夋媺锛岃繖娆″簲璇ヨ浜嗗惂");
                super.onDestroy();
        }

        private class ShareMessageReceiver extends BroadcastReceiver {
                @Override
                public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(Constant.NEW_SHARE_MESSAGE_ACTION)) {
                                String from = intent.getStringExtra("from");
                                if (from != null && from.equals("share")) {
                                        LogUtil.e("11鎷夊彇寰楀埌鐨勮璇存秷鎭潵浜?);
                                        List<SharedMessage> list = (List<SharedMessage>) intent.getSerializableExtra(Constant.NEW_MESSAGE);
                                        for (SharedMessage shareMessage :
                                                list) {
                                                ChatDB.create().saveSharedMessage(shareMessage);
                                        }




                                        if (mFragments[3].isAdded()){
                                                ((ShareMessageFragment) mFragments[3]).updateAllShareMessages(list,true);
                                                if (currentFragment instanceof ShareMessageFragment) {
                                                }else {
                                                        notifyMenuUpdate();
                                                }
                                        }

                                }
                        } else if (intent.getAction().equals(Constant.NOTIFY_CHANGE_ACTION)) {
                                List<String> list = intent.getStringArrayListExtra("id");
                                boolean isAdd = intent.getBooleanExtra("isAdd", false);
                                if (list != null && list.size() > 0) {
                                        for (String id :
                                                list) {
                                                notifySharedMessageChanged(id, isAdd);
                                        }
                                }
                        }
                }
        }

        private void notifyUrlSharedMessageAdd(SharedMessage sharedMessage) {
                if (currentFragment instanceof ShareMessageFragment) {
                        ((ShareMessageFragment) currentFragment).notifyUrlShareMessageCome(sharedMessage);
                } else {
                        if (!mFragments[3].isAdded()) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("url_share_message", sharedMessage);
                                mFragments[3].setArguments(bundle);
                        }
                        addOrReplaceFragment(mFragments[3]);
                }
        }


        private  long mExitTime=0;

        @Override
        public void onBackPressed() {
                if (System.currentTimeMillis() - mExitTime > 2000) {
                        ToastUtils.showShortToast("鍐嶆寜涓€娆￠€€鍑虹▼搴?);
                        mExitTime=System.currentTimeMillis();
                }else {
                        super.onBackPressed();
                }
        }
}



