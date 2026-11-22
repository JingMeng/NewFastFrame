package com.example.chat.ui;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/27      17:31
 * QQ:             1981367757
 */


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.adapter.GridPictureAdapter;
import com.example.chat.adapter.VisibilityAdapter;
import com.example.chat.base.CommonImageLoader;
import com.example.chat.base.Constant;
import com.example.chat.bean.HappyBean;
import com.example.chat.bean.HappyContentBean;
import com.example.chat.bean.ImageItem;
import com.example.chat.bean.PictureBean;
import com.example.chat.bean.SharedMessage;
import com.example.chat.bean.WinXinBean;
import com.example.chat.listener.OnCreateSharedMessageListener;
import com.example.chat.manager.LocationManager;
import com.example.chat.manager.MessageCacheManager;
import com.example.chat.manager.MsgManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.mvp.ShareMessageTask.ShareMessageContacts;
import com.example.chat.mvp.ShareMessageTask.ShareMessageModel;
import com.example.chat.mvp.ShareMessageTask.ShareMessagePresenter;
import com.example.chat.util.LogUtil;
import com.example.commonlibrary.baseadapter.SuperRecyclerView;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemChildClickListener;
import com.example.commonlibrary.baseadapter.swipeview.Closeable;
import com.example.commonlibrary.baseadapter.swipeview.OnSwipeMenuItemClickListener;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenu;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenuCreator;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenuItem;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenuRecyclerView;
import com.example.commonlibrary.cusotomview.ListViewDecoration;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import mabeijianxi.camera.MediaRecorderActivity;
import mabeijianxi.camera.model.MediaRecorderConfig;


/**
 * 缂栬緫璇磋鐨勭晫闈? */
public class EditShareMessageActivity extends SlideBaseActivity<List<SharedMessage>,ShareMessagePresenter> implements View.OnClickListener, ShareMessageContacts.View<List<SharedMessage>>, AdapterView.OnItemClickListener, OnSwipeMenuItemClickListener {
        /**
         * 缂栬緫鍐呭
         */
        private EditText edit;
        /**
         * 瀹氫綅
         */
        private TextView location;


        //        鐐瑰嚮閫夋嫨鍙互鐪嬭璇磋鐨勭敤鎴?        private TextView visibility;
        /**
         * 榛樿閫夋嫨鎵€鏈変汉鍙
         */
        private int selectedVisibilityPosition = 1;
        private List<String> selectedVisibleUsers;

        private ShareMessagePresenter mShareMessagePresenter;


        private SuperRecyclerView display;
        private GridPictureAdapter adapter;
        private ArrayList<ImageItem> selectedImageList = new ArrayList<>();
        private ImageView video;
        /**
         * 瑙嗛瀛樺偍璺緞
         */
        private String mPath;
        private String videoScreenshot;
        private boolean isVideo;
        private List<String> addressList;
        private WinXinBean mWinXinBean;
        private HappyContentBean mHappyContentBean;
        private HappyBean mHappyBean;
        private PictureBean mPictureBean;
        private ImageView urlAvatar;
        private TextView urlTitle;
        private CardView mCardView;
        private SwipeMenuRecyclerView selectedFriend;
        private List<String> invisibleUsers;
        private TextView title;
        private String from;


        @Override
        public void initView() {
                edit = (EditText) findViewById(R.id.et_edit_share_message_edit);
                location = (TextView) findViewById(R.id.tv_edit_share_message_location);
                visibility = (TextView) findViewById(R.id.tv_edit_share_message_visibility);
                display = (SuperRecyclerView) findViewById(R.id.rcv_edit_share_message_display);
                RelativeLayout visibilityContainer = (RelativeLayout) findViewById(R.id.rl_edit_share_message_visibility_container);
                video = (ImageView) findViewById(R.id.iv_edit_share_message_video);
                RelativeLayout locationLayout = (RelativeLayout) findViewById(R.id.rl_edit_share_message_location);
                urlAvatar = (ImageView) findViewById(R.id.iv_edit_share_message_url_avatar);
                urlTitle = (TextView) findViewById(R.id.tv_edit_share_message_url_title);
                mCardView = (CardView) findViewById(R.id.cv_edit_share_message_url_container);
                selectedFriend = (SwipeMenuRecyclerView) findViewById(R.id.swrc_edit_share_message_visibility);
                title = (TextView) findViewById(R.id.tv_edit_share_message_visibility_title);
                visibilityContainer.setOnClickListener(this);
                video.setOnClickListener(this);
                locationLayout.setOnClickListener(this);
                mCardView.setOnClickListener(this);
        }


        @Override
        protected void onNewIntent(Intent intent) {
                super.onNewIntent(intent);
                if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
                        display.setVisibility(View.GONE);
                        video.setVisibility(View.GONE);
                        mCardView.setVisibility(View.VISIBLE);
                        mCardView.setOnClickListener(this);
                        String type = getIntent().getStringExtra("type");
                        switch (type) {
                                case "wei_xin":
                                        mWinXinBean = (WinXinBean) getIntent().getSerializableExtra("share_info");
                                        initWeiXinInfo();
                                        break;
                                case "happy":
                                        mHappyBean = (HappyBean) getIntent().getSerializableExtra("share_info");
                                        initHappyInfo();
                                        break;
                                case "happy_content":
                                        mHappyContentBean = (HappyContentBean) getIntent().getSerializableExtra("share_info");
                                        initHappyContentInfo();
                                        break;
                                case "picture":
                                        mPictureBean= (PictureBean) getIntent().getSerializableExtra("share_info");
                                        initPictureContentInfo();
                                        break;
                        }
                        return;
                }
                mPath = intent.getStringExtra(MediaRecorderActivity.VIDEO_URI);
                videoScreenshot = intent.getStringExtra(MediaRecorderActivity.VIDEO_SCREENSHOT);
                if (videoScreenshot != null && !videoScreenshot.equals("")) {
                        LogUtil.e("鎴浘涓嶄负绌?);
                        display.setVisibility(View.GONE);
                        video.setVisibility(View.VISIBLE);
                        Bitmap bitmap = BitmapFactory.decodeFile(videoScreenshot);
                        video.setImageBitmap(bitmap);
                } else {
                        LogUtil.e("鎴浘涓虹┖");
                        if (isVideo) {
                                LogUtil.e("瑙嗛鍙栨秷鎷?);
                                finish();
                        } else {
                                LogUtil.e("false");
                                display.setVisibility(View.VISIBLE);
                                video.setVisibility(View.GONE);
                        }
                }
        }




        @Override
        protected void onRestart() {
                super.onRestart();
                LogUtil.e("onRestart");
        }


        @Override
        protected void onStart() {
                super.onStart();
                LogUtil.e("onStart");
        }


        private boolean isFirstCreate = true;

        @Override
        protected void onResume() {
                super.onResume();
                LogUtil.e("1122onResume:" + "鍒濆鍖?);
                if (!isFirstCreate) {
                        if (from != null && from.equals("video") && videoScreenshot == null) {
                                LogUtil.e("杩欐槸鍙栨秷瑙嗛鐨勬搷浣?);
                                finish();
                                return;
                        }
                } else {
                        isFirstCreate = false;
                }
                if (adapter != null) {
                        adapter.notifyDataSetChanged();
                }

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
                return R.layout.edit_share_message;
        }

        @Override
        public void initData() {
                mShareMessagePresenter = new ShareMessagePresenter(this,new ShareMessageModel(ChatApplication
                .getChatMainComponent().getMainRepositoryManager()));
                initActionBar();
                if (LocationManager.getInstance().getLocationList() != null) {
                        location.setText(LocationManager.getInstance().getLocationList().get(0));
                }
//                杩欓噷鍒濆鍖栧浘鐗囬€夋嫨鍣?                from = getIntent().getStringExtra("destination");
                if (from != null) {
                        if (from.equals("video")) {
                                LogUtil.e("杩欐槸瑙嗛璇磋");
                                isVideo = true;
                                MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                                        .doH264Compress(true)
                                        .smallVideoWidth(480)
                                        .smallVideoHeight(360)
                                        .recordTimeMax(6 * 1000)
                                        .maxFrameRate(20)
                                        .minFrameRate(8)
                                        .captureThumbnailsTime(1)
                                        .recordTimeMin((int) (1.5 * 1000))
                                        .build();
                                MediaRecorderActivity.goSmallVideoRecorder(this, EditShareMessageActivity.class.getName(), config);
                        } else if (from.equals("url")) {
                                display.setVisibility(View.GONE);
                                video.setVisibility(View.GONE);
                                mCardView.setVisibility(View.VISIBLE);
                                mCardView.setOnClickListener(this);
                                String type = getIntent().getStringExtra("type");
                                switch (type) {
                                        case "wei_xin":
                                                mWinXinBean = (WinXinBean) getIntent().getSerializableExtra("share_info");
                                                initWeiXinInfo();
                                                break;
                                        case "happy":
                                                mHappyBean = (HappyBean) getIntent().getSerializableExtra("share_info");
                                                initHappyInfo();
                                                break;
                                        case "happy_content":
                                                mHappyContentBean = (HappyContentBean) getIntent().getSerializableExtra("share_info");
                                                initHappyContentInfo();
                                                break;
                                        case "picture":
                                                mPictureBean= (PictureBean) getIntent().getSerializableExtra("share_info");
                                                initPictureContentInfo();
                                }
                        } else if (from.equals("image")) {
                                display.setVisibility(View.VISIBLE);
                                LogUtil.e("杩欐槸鍥剧墖璇磋");
                                initCommonImageLoader();
                                initImageAdapter();
                        } else {
                                LogUtil.e("杩欐槸鏂囨湰璇磋");
                                display.setVisibility(View.GONE);
                        }
                }
                initBottomVisibilityData();
        }

        VisibilityAdapter mAdapter;


        private void initBottomVisibilityData() {
                LogUtil.e("鍒濆鍖栧簳閮ㄦ暟鎹?);
                selectedFriend.setLayoutManager(new LinearLayoutManager(this));
                selectedFriend.setItemAnimator(new DefaultItemAnimator());
                selectedFriend.addItemDecoration(new ListViewDecoration(this));
                selectedFriend.setHasFixedSize(true);
                selectedFriend.setSwipeMenuCreator(new SwipeMenuCreator() {
                        @Override
                        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                                SwipeMenuItem deleteItem = new SwipeMenuItem(EditShareMessageActivity.this);
                                int width = getResources().getDimensionPixelSize(R.dimen.recent_top_height);
                                deleteItem.setBackgroundDrawable(new ColorDrawable(Color.rgb(0xF9,
                                        0x3F, 0x25))).setText("鍒犻櫎").setTextColor(Color.WHITE).setHeight(ViewGroup.LayoutParams.MATCH_PARENT).setWidth(width);
                                swipeRightMenu.addMenuItem(deleteItem);
                        }
                });
                selectedFriend.setSwipeMenuItemClickListener(this);
                mAdapter = new VisibilityAdapter();
                selectedFriend.setAdapter(mAdapter);
        }


        public void updateBottomData() {
                if (selectedVisibilityPosition == 0 || selectedVisibilityPosition == 1) {
                        title.setVisibility(View.GONE);
                        selectedFriend.setVisibility(View.GONE);
                } else if (selectedVisibilityPosition == 2) {
                        title.setVisibility(View.VISIBLE);
                        title.setText("鍙鐨勫涓?");
                        selectedFriend.setVisibility(View.VISIBLE);
                        mAdapter.clearAllData();
                        mAdapter.addData(selectedVisibleUsers);
                } else if (selectedVisibilityPosition == 3) {
                        title.setText("涓嶅彲瑙佺殑濡備笅:");
                        title.setVisibility(View.VISIBLE);
                        selectedFriend.setVisibility(View.VISIBLE);
                        mAdapter.clearAllData();
                        mAdapter.addData(invisibleUsers);
                }
        }

        private void initImageAdapter() {
                display.setLayoutManager(new GridLayoutManager(this, 4));
                display.setHasFixedSize(true);
                display.setItemAnimator(new DefaultItemAnimator());
                adapter = new GridPictureAdapter();
                ImageItem imageItem = new ImageItem();
                imageItem.setItemType(ImageItem.ITEM_CAMERA);
                adapter.getData().add(imageItem);
//                adapter.setMaxSelectedCount(CommonImageLoader.getInstance().getMaxSelectedCount());
                adapter.setOnItemClickListener(new OnSimpleItemChildClickListener() {
                    @Override
                    public void onItemChildClick(int position, View view, int id) {
                        if (id == R.id.iv_grid_picture_item_display) {
                            LogUtil.e("鐐瑰嚮item鐨刾osition" + position);
                            if (position == adapter.getData().size() - 1) {
//                                        杩涘叆閫夋嫨鍥剧墖鐣岄潰
                                Intent intent = new Intent(EditShareMessageActivity.this, SelectedPictureActivity.class);
                                startActivityForResult(intent, Constant.REQUEST_CODE_SELECT_PICTURE);
                            } else {
                                LogUtil.e("杩涘叆棰勮鐣岄潰");
                                Intent intent = new Intent(EditShareMessageActivity.this, BasePreViewActivity.class);
                                intent.putExtra(CommonImageLoader.CURRENT_POSITION, position);
                                intent.putExtra(CommonImageLoader.PREVIEW_FROM, CommonImageLoader.PREVIEW_DELETE);
                                startActivity(intent);
                            }
                        }
                    }
                });
                display.setAdapter(adapter);
        }

        private void initHappyContentInfo() {
                urlAvatar.setVisibility(View.GONE);
                if (mHappyContentBean != null) {
                        urlTitle.setText(mHappyContentBean.getContent());
                }
        }

        private void initPictureContentInfo() {
                Glide.with(this).load(mPictureBean.getUrl()).into(urlAvatar);
                urlTitle.setText("涓€寮犵編濂冲浘鐗?);
        }

        private void initHappyInfo() {
                if (mHappyBean != null) {
                        urlTitle.setText(mHappyBean.getContent());
                        Glide.with(this).load(mHappyBean.getUrl()).into(urlAvatar);
                }
        }

        private void initWeiXinInfo() {
                if (mWinXinBean != null) {
                        urlTitle.setText(mWinXinBean.getTitle());
                        Glide.with(this).load(mWinXinBean.getPicUrl()).into(urlAvatar);
                }
        }

        private void initActionBar() {
                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setAvatar(null);
                toolBarOption.setTitle("蹇冩儏");
                toolBarOption.setNeedNavigation(true);
                toolBarOption.setRightText("鍙戦€?);
                toolBarOption.setRightResId(R.drawable.ic_send_blue_grey_900_24dp);
                toolBarOption.setRightListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                //                                鍙戦€佹搷浣?                                if (edit.getText().toString().trim().equals("")) {
                                        ToastUtils.showShortToast("鍙戦€佺殑鍐呭涓嶈兘涓虹┖");
                                        return;
                                }
                                int visibleType;
                                if (selectedVisibilityPosition == 0) {
                                        visibleType = Constant.SHARE_MESSAGE_VISIBLE_TYPE_PRIVATE;
                                } else {
                                        visibleType = Constant.SHARE_MESSAGE_VISIBLE_TYPE_PUBLIC;

                                        showLoadDialog("姝ｅ湪鍙戣〃璇磋..........");
                                        MsgManager.getInstance().createSharedMessage(mHappyBean, mHappyContentBean, mWinXinBean, mPictureBean, location.getText().toString().trim(), mPath, videoScreenshot, edit.getText().toString().trim(), selectedImageList, invisibleUsers, visibleType, new OnCreateSharedMessageListener() {
                                                @Override
                                                public void onSuccess(final SharedMessage message) {
                                                        mShareMessagePresenter.addShareMessage(message);
                                                }

                                                @Override
                                                public void onFailed(int errorId, String errorMsg) {
                                                        dismissBaseDialog();
                                                        LogUtil.e("鎬讳綋涓婁紶鍥剧墖澶辫触");
                                                        LogUtil.e(errorMsg + errorId);
                                                }
                                        });
                                }
                        }
                });
                setToolBar(toolBarOption);
        }

        private void initCommonImageLoader() {
                CommonImageLoader imageLoader = CommonImageLoader.getInstance();
                imageLoader.initStanderConfig(8);
        }


        @Override
        protected void onDestroy() {
                super.onDestroy();
                CommonImageLoader.getInstance().clearAllData();
                mShareMessagePresenter.onDestroy();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode == Activity.RESULT_CANCELED && requestCode == Constant.REQUEST_CODE_SELECT_VISIBILITY) {
                        LogUtil.e("鍙栨秷閫夋嫨濂藉弸锛屾洿鏂板彲瑙佺晫闈㈡枃鏈?);
                        selectedVisibilityPosition = 1;
                        visibility.setText("瀵规墍鏈変汉閮藉彲瑙?);
                        updateBottomData();
                        return;
                }
                if (resultCode == Activity.RESULT_OK) {
//                        杩欓噷杩涜鍥剧墖閫夋嫨鍚嶶RL娑堟伅鐨勫洖璋冨鐞?                        switch (requestCode) {
                                case Constant.REQUEST_CODE_SELECT_VISIBILITY:
//                                        閫夋嫨鍙鎬?                                        if (data != null) {
                                                if (selectedVisibleUsers != null && selectedVisibleUsers.size() > 0) {
                                                        selectedVisibleUsers.clear();
                                                }
                                                if (invisibleUsers != null && invisibleUsers.size() > 0) {
                                                        invisibleUsers.clear();
                                                }
                                                if (selectedVisibilityPosition == 2) {
                                                        selectedVisibleUsers = data.getStringArrayListExtra(Constant.RESULT_CODE_SELECT_VISIBILITY);
                                                        List<String> users = new ArrayList<>(UserCacheManager.getInstance().getContacts().keySet());
                                                        if (invisibleUsers == null) {
                                                                invisibleUsers = new ArrayList<>();
                                                        }
                                                        for (String uid :
                                                                selectedVisibleUsers) {
                                                                if (!users.contains(uid)) {
                                                                        invisibleUsers.add(uid);
                                                                }
                                                        }
                                                } else if (selectedVisibilityPosition == 3) {
                                                        invisibleUsers = data.getStringArrayListExtra(Constant.RESULT_CODE_SELECT_VISIBILITY);
                                                }
                                        }
                                        if (selectedVisibleUsers != null) {
                                                for (String uid :
                                                        invisibleUsers) {
                                                        LogUtil.e("涓嶅彲瑙佺敤鎴稩D" + uid + "\n");
                                                }
                                        }
                                        updateBottomData();
                                        break;
                                case Constant.REQUEST_CODE_SELECT_PICTURE:
                                        if (selectedImageList.size() > 0) {
                                                selectedImageList.clear();
                                        }
                                        selectedImageList.addAll(CommonImageLoader.getInstance().getSelectedImages());
                                        adapter.clearAllData();
                                        adapter.getData().addAll(selectedImageList);
                                        ImageItem imageItem = new ImageItem();
                                        imageItem.setItemType(ImageItem.ITEM_CAMERA);
                                        adapter.getData().add(imageItem);
                                        adapter.notifyDataSetChanged();
                                        break;
                        }
                }
                super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.rl_edit_share_message_visibility_container) {
                        mBaseDialog.setDialogContentView(R.layout.select_visibility_layout).setTitle("璋佸彲瑙?).setBottomLayoutVisible(true).setLeftButton("鍙栨秷", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        cancelBaseDialog();
                                }
                        }).setRightButton("纭畾", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        selectedVisibleUsers = null;
                                        invisibleUsers = null;
                                        dismissBaseDialog();
                                        RadioGroup radioGroup = (RadioGroup) mBaseDialog.getMiddleLayout().findViewById(R.id.rg_select_visibility_container);
                                        int i1 = radioGroup.getCheckedRadioButtonId();
                                        if (i1 == R.id.rb_visibility_private) {
                                                selectedVisibilityPosition = 0;
                                                visibility.setText("浠呭鑷繁鍙");
                                                updateBottomData();

                                        } else if (i1 == R.id.rb_visibility_public) {
                                                selectedVisibilityPosition = 1;
                                                visibility.setText("瀵规墍鏈変汉閮藉彲瑙?);
                                                updateBottomData();

                                        } else if (i1 == R.id.rb_visibility_part) {
                                                selectedVisibilityPosition = 2;
                                                visibility.setText("瀵归儴鍒嗕汉鍙");
                                                Intent partVisibility = new Intent(EditShareMessageActivity.this, SelectedFriendsActivity.class);
                                                partVisibility.putExtra("from", "select_visibility");
                                                partVisibility.putExtra("title", "瀵硅皝鍙");
                                                startActivityForResult(partVisibility, Constant.REQUEST_CODE_SELECT_VISIBILITY);

                                        } else if (i1 == R.id.rb_visibility_exclude_part) {
                                                selectedVisibilityPosition = 3;
                                                visibility.setText("瀵归儴鍒嗕汉涓嶅彲瑙?);
                                                Intent intent = new Intent(EditShareMessageActivity.this, SelectedFriendsActivity.class);
                                                intent.putExtra("from", "select_visibility");
                                                intent.putExtra("title", "瀵硅皝涓嶅彲瑙?);
                                                startActivityForResult(intent, Constant.REQUEST_CODE_SELECT_VISIBILITY);

                                        }
                                }
                        }).show();
                        ((RadioGroup) mBaseDialog.getMiddleLayout().findViewById(R.id.rg_select_visibility_container)).check(getCheckIdFromPosition(selectedVisibilityPosition));

                } else if (i == R.id.iv_edit_share_message_video) {
                        LogUtil.e("鐐瑰嚮浜嗘挱鏀惧綍鍒惰棰?);
                        Intent intent = new Intent(this, VideoPlayActivity.class);
                        intent.putExtra("path", mPath);
                        startActivity(intent);

                } else if (i == R.id.rl_edit_share_message_location) {
                        if (addressList != null) {
                                addressList.clear();
                        } else {
                                addressList = new ArrayList<>();
                        }
                        addressList.addAll(LocationManager.getInstance().getLocationList());
                        addressList.add("涓嶆樉绀?);
                        showChooseDialog("閫夋嫨浣嶇疆", addressList, this);

                } else if (i == R.id.cv_edit_share_message_url_container) {
                        Intent happyContentIntent = new Intent(this, HappyContentDisplayActivity.class);
                        if (getIntent().getStringExtra("type").equals("happy")) {
                                happyContentIntent.putExtra("content", mHappyBean.getContent());
                                happyContentIntent.putExtra("url", mHappyBean.getUrl());
                                startActivity(happyContentIntent);
                        } else if (getIntent().getStringExtra("type").equals("happy_content")) {
                                happyContentIntent.putExtra("content", mHappyContentBean.getContent());
                                startActivity(happyContentIntent);
                        } else {
                                WeiXinNewsActivity.start(this, mWinXinBean.getTitle(), mWinXinBean.getUrl());
                        }

                }
        }

        private int getCheckIdFromPosition(int selectedVisibilityPosition) {
                switch (selectedVisibilityPosition) {
                        case 0:
                                return R.id.rb_visibility_private;
                        case 1:
                                return R.id.rb_visibility_public;
                        case 2:
                                return R.id.rb_visibility_part;
                        case 3:
                                return R.id.rb_visibility_exclude_part;
                        default:
                                return 0;
                }
        }

        @Override
        public void updateShareMessageAdded(SharedMessage shareMessage) {
                dismissBaseDialog();
                LogUtil.e("鎴愬姛鍙戦€?);
                ToastUtils.showShortToast("鎴愬姛鍙戦€?);
                LogUtil.e("杩欓噷鐨勮璇碔D" + shareMessage.getObjectId());
//                杩欓噷鎶婃湰鍦板瓨鍌ㄧ殑瑙嗛鎴栧浘鐗囩殑URL瀛樺埌鍐呭瓨涓?                if (shareMessage.getMsgType().equals(Constant.MSG_TYPE_SHARE_MESSAGE_IMAGE)) {
                        List<String> urlList = new ArrayList<>();
                        for (ImageItem imageItem :
                                selectedImageList) {
                                urlList.add(imageItem.getPath());
                        }
                        MessageCacheManager.getInstance().saveShareMessageCache(shareMessage.getObjectId(), urlList);
                } else if (shareMessage.getMsgType().equals(Constant.MSG_TYPE_SHARE_MESSAGE_VIDEO)) {
                        List<String> urlList = new ArrayList<>();
                        urlList.add(videoScreenshot);
                        urlList.add(mPath);
                        MessageCacheManager.getInstance().saveShareMessageCache(shareMessage.getObjectId(), urlList);
                } else if (shareMessage.getMsgType().equals(Constant.MSG_TYPE_SHARE_MESSAGE_LINK)) {
                        LogUtil.e("杩欓噷鏄彂閫侀摼鎺ョ殑娑堟伅");
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("url_share_message", shareMessage);
                        startActivity(intent);
                        finish();
                        return;
                }
                Intent intent = new Intent();
                intent.putExtra(Constant.RESULT_CODE_SHARE_MESSAGE, shareMessage);
                setResult(Activity.RESULT_OK, intent);
                finish();
        }

        @Override
        public void updateShareMessageDeleted(String id) {

        }

        @Override
        public void updateLikerAdd(String id) {

        }

        @Override
        public void updateLikerDeleted(String id) {

        }

        @Override
        public void updateCommentAdded(String id, String content, int position) {

        }

        @Override
        public void updateCommentDeleted(String id, String content, int position) {

        }

        @Override
        public void updateAllShareMessages(List<SharedMessage> data, boolean isPullRefresh) {

        }


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                location.setText(addressList.get(position));
                dismissBaseDialog();
        }

        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, @SwipeMenuRecyclerView.DirectionMode int direction) {
                LogUtil.e("adapterPosition" + adapterPosition);
                if (selectedVisibilityPosition == 2) {
                        selectedVisibleUsers.remove(adapterPosition);
                } else if (selectedVisibilityPosition == 3) {
                        String oldId = invisibleUsers.remove(adapterPosition);
                        selectedVisibleUsers.add(oldId);
                }
                updateBottomData();
        }

        @Override
        public void updateData(List<SharedMessage> sharedMessages) {

        }
}

