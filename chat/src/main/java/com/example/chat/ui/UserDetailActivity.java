package com.example.chat.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chat.ChatApplication;
import com.example.chat.R;
import com.example.chat.adapter.OnShareMessageItemClickListener;
import com.example.chat.adapter.ShareMultipleLayoutAdapter;
import com.example.chat.base.CommonImageLoader;
import com.example.chat.base.Constant;
import com.example.chat.bean.ImageItem;
import com.example.chat.bean.SharedMessage;
import com.example.chat.bean.User;
import com.example.chat.listener.OnShareMessageReceivedListener;
import com.example.chat.manager.MessageCacheManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.manager.UserManager;
import com.example.chat.mvp.ShareMessageTask.ShareMessageContacts;
import com.example.chat.mvp.ShareMessageTask.ShareMessageModel;
import com.example.chat.mvp.ShareMessageTask.ShareMessagePresenter;
import com.example.chat.service.GroupMessageService;
import com.example.chat.util.CommonUtils;
import com.example.chat.util.LogUtil;
import com.example.chat.view.CommentPopupWindow;
import com.example.commonlibrary.baseadapter.SuperRecyclerView;
import com.example.commonlibrary.baseadapter.foot.LoadMoreFooterView;
import com.example.commonlibrary.baseadapter.foot.OnLoadMoreListener;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemChildClickListener;
import com.example.commonlibrary.cusotomview.ListViewDecoration;
import com.example.commonlibrary.cusotomview.RoundAngleImageView;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;


/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/22      9:41
 * QQ:             1981367757
 */

public class UserDetailActivity extends SlideBaseActivity<List<SharedMessage>,ShareMessagePresenter> implements View.OnClickListener, OnShareMessageReceivedListener, ShareMessageContacts.View<List<SharedMessage>>, OnShareMessageItemClickListener, SwipeRefreshLayout.OnRefreshListener, OnLoadMoreListener {
        private SuperRecyclerView display;
        //        private AppBarLayout appBarLayout;
        private ImageView bg;

        private FloatingActionButton floatingActionButton;
        String uid;
        private User mUser;
        private TextView nick;
        private TextView signature;
        private TextView sex;
        private RoundAngleImageView avatar;
        private RelativeLayout middle;
        private TextView address;
        private ShareMultipleLayoutAdapter mShareMultipleLayoutAdapter;
        private String currentId;
        private int currentPosition;
        private CommentPopupWindow mCommentPopupWindow;
        private ShareMessagePresenter presenter;
        private int currentCommentPosition;
        private String replyUid;
        private int commentItemOffset;
        private LinearLayout bottomInput;
        private EditText input;
        private LinearLayoutManager mLinearLayoutManager;
        private boolean isLoading = false;
        private int mKeyBoardHeight = 0;
        private int screenHeight;
        private RelativeLayout rootContainer;
        private ImageView send;
        private SwipeRefreshLayout refresh;


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
                return R.layout.activity_user_detail;
        }


        @Override
        public void initView() {
                floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_activity_user_detail_button);
                floatingActionButton.setImageResource(R.drawable.ic_mode_edit_blue_grey_900_24dp);
                display = (SuperRecyclerView) findViewById(R.id.rcv_activity_user_detail_display);
                refresh = (SwipeRefreshLayout) findViewById(R.id.refresh_activity_user_detail_refresh);
                bottomInput = (LinearLayout) findViewById(R.id.ll_user_detail_bottom);
                input = (EditText) findViewById(R.id.et_user_detail_input);
                send = (ImageView) findViewById(R.id.iv_user_detail_send);
                rootContainer = (RelativeLayout) findViewById(R.id.rl_activity_user_detail_container);
                rootContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                                Rect rect = new Rect();
                                rootContainer.getWindowVisibleDisplayFrame(rect);
                                screenHeight = rootContainer.getRootView().getHeight();
                                int keyBoardHeight = screenHeight - rect.bottom;
//                                int status = getStatusHeight();
                                if (keyBoardHeight != mKeyBoardHeight) {
                                        if (keyBoardHeight > mKeyBoardHeight) {
                                                bottomInput.setVisibility(View.VISIBLE);
                                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bottomInput.getLayoutParams();
                                                int realHeight = screenHeight -  bottomInput.getHeight() - keyBoardHeight;
                                                layoutParams.setMargins(0, realHeight, 0, 0);
                                                bottomInput.setLayoutParams(layoutParams);
                                                mKeyBoardHeight = keyBoardHeight;
                                                floatingActionButton.setVisibility(View.GONE);
                                                mLinearLayoutManager.scrollToPositionWithOffset(currentPosition, getListOffset());
                                        } else {
                                                floatingActionButton.setVisibility(View.VISIBLE);
                                                mKeyBoardHeight = keyBoardHeight;
                                                bottomInput.setVisibility(View.GONE);
                                        }
                                }
                        }
                });
//                appBarLayout.addOnOffsetChangedListener(this);
                send.setOnClickListener(this);
                refresh.setOnRefreshListener(this);
                floatingActionButton.setOnClickListener(this);
                rootContainer.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                                if (mCommentPopupWindow != null && mCommentPopupWindow.isShowing()) {
                                        mCommentPopupWindow.dismiss();
                                }

//                                杩欓噷杩涜鐐瑰嚮鍏抽棴缂栬緫妗?                                if (bottomInput.getVisibility() == View.VISIBLE) {
                                        LogUtil.e("瑙︽懜鐣岄潰鐐瑰嚮鍏抽棴杈撳叆娉?);
                                        dealBottomView(false);
                                        return true;
                                }
                                return false;
                        }
                });
        }

        private int getListOffset() {
                int offset = 0;
                if (currentCommentPosition == -1) {
                        ToastUtils.showShortToast("鐐瑰嚮璇勮");
                        LogUtil.e("22222鐐瑰嚮璇勮");
                        int firstVisiblePosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                        View view = mLinearLayoutManager.getChildAt(currentPosition - firstVisiblePosition);
                        offset += view.getHeight();
                }
                offset += mKeyBoardHeight;
                offset += bottomInput.getHeight();
//                offset += getStatusHeight();
                if (replyUid != null) {
                        offset += commentItemOffset;
                }
                return screenHeight - offset;
        }


        @Override
        public void initData() {
                uid = getIntent().getStringExtra("uid");
                if (uid.equals(UserManager.getInstance().getCurrentUserObjectId())) {
                        LogUtil.e("褰撳墠鐢ㄦ埛111");
                        floatingActionButton.setVisibility(View.VISIBLE);
                        mUser = UserManager.getInstance().getCurrentUser();
                } else {
                        floatingActionButton.setVisibility(View.GONE);
                        mUser = UserCacheManager.getInstance().getUser(uid);
                }
                presenter = new ShareMessagePresenter(this,new ShareMessageModel(ChatApplication
                .getChatMainComponent().getMainRepositoryManager()));
                display.setLayoutManager(mLinearLayoutManager = new LinearLayoutManager(this));
                display.setItemAnimator(new DefaultItemAnimator());
                display.addItemDecoration(new ListViewDecoration(this));
                mShareMultipleLayoutAdapter = new ShareMultipleLayoutAdapter(null);
                display.setLoadMoreFooterView(new LoadMoreFooterView(this));
                display.setOnLoadMoreListener(this);
                display.addHeaderView(getHeaderView());
//                璁剧疆item鍐呴儴鐨勭偣鍑讳簨浠?                mShareMultipleLayoutAdapter.setOnShareMessageItemClickListener(this);
                mShareMultipleLayoutAdapter.setOnItemClickListener(new OnSimpleItemChildClickListener() {
                        @Override
                        public void onItemChildClick(int position, View view, int id) {
                                if (id == R.id.riv_share_fragment_item_main_avatar) {
                                        onImageAvatarClick(UserCacheManager.getInstance().getUser(mShareMultipleLayoutAdapter.getData(position).getBelongId()).getAvatar());

                                } else if (id == R.id.tv_share_fragment_item_main_name) {
                                        onNameClick(mShareMultipleLayoutAdapter.getData(position).getBelongId());

                                } else if (id == R.id.iv_share_fragment_item_main_comment) {
                                        boolean isLike = false;
                                        if (mShareMultipleLayoutAdapter.getData(position).getLikerList() != null && mShareMultipleLayoutAdapter.getData(position).getLikerList().contains(UserManager.getInstance().getCurrentUserObjectId())) {
                                                LogUtil.e("宸叉湁璧?);
                                                isLike = true;
                                        }
                                        onCommentBtnClick(view, mShareMultipleLayoutAdapter.getData(position).getObjectId(), position, isLike);
                                }

                        }
                });
//                璁剧疆鍔犺浇鏇村鐐瑰嚮浜嬩欢

                display.setAdapter(mShareMultipleLayoutAdapter);
//                        mShareMultipleLayoutAdapter.setOnShareMessageItemClickListener(this);
                GroupMessageService.registerListener(this);
                display.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                refresh.setRefreshing(true);
                                onRefresh();
                        }
                }, 200);
                updateUserData();
        }


        private void loadData(boolean isPullRefresh, String time) {
                presenter.loadShareMessages(uid, isPullRefresh, time);
        }

        private View getHeaderView() {
                View view = getLayoutInflater().inflate(R.layout.user_detail_header_layout, display, false);
                bg = (ImageView) view.findViewById(R.id.iv_background);
                nick = (TextView) view.findViewById(R.id.tv_user_detail_name);
                sex = (TextView) view.findViewById(R.id.tv_user_detail_sex);
                signature = (TextView) view.findViewById(R.id.tv_user_detail_signature);
                avatar = (RoundAngleImageView) view.findViewById(R.id.riv_user_detail_avatar);
                middle = (RelativeLayout) view.findViewById(R.id.rl_user_detail_middle);
                address = (TextView) view.findViewById(R.id.tv_user_detail_address);
                return view;
        }

        private void updateUserData() {

                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setTitle(mUser.getNick());
                toolBarOption.setNeedNavigation(true);
                setToolBar(toolBarOption);
                signature.setText(mUser.getSignature());
                address.setText(mUser.getAddress());
                nick.setText(mUser.getNick());
                sex.setText(mUser.isSex() ? "鐢? : "濂?);
                Glide.with(this).load(mUser.getAvatar())
                        .bitmapTransform(new BlurTransformation(this, 5))
                        .into(bg);
                Glide.with(this).load(mUser.getAvatar()).into(avatar);
//                Glide.with(this).load(mUser.getAvatar()).into(bgCover);
//                ViewHelper.setAlpha(bgCover, 1);
        }

//
//        /**
//         * @param appBarLayout   appBarLayout
//         * @param verticalOffset 鍋忕Щ閲?    0-1   0:瀹屽叏灞曞紑      1锛氬畬鍏ㄦ敹缂?//         */
//        @Override
//        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//
//                float percent = Math.abs(verticalOffset) / (float) appBarLayout.getTotalScrollRange();
//                LogUtil.e("鐧惧垎姣?" + percent);
////                ViewHelper.setAlpha(bgCover, percent);
//                if (percent > 0.5) {
//                        middle.setVisibility(View.GONE);
//                        if (percent > 0.7) {
//                                signature.setVisibility(View.GONE);
//                        } else {
//                                signature.setVisibility(View.VISIBLE);
//                        }
//                } else {
//                        middle.setVisibility(View.VISIBLE);
//                        signature.setVisibility(View.VISIBLE);
//                }
//        }

        @Override
        public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.fab_activity_user_detail_button) {
                        Intent intent = new Intent(this, EditUserInfoActivity.class);
                        startActivityForResult(intent, Constant.REQUEST_CODE_EDIT_USER_INFO);

                } else if (i == R.id.iv_user_detail_send) {
                        String content = input.getText().toString().trim();
                        if (content.equals("") || content.contains("$") || content.contains("&")) {
                                LogUtil.e("璇勮鍐呭涓嶈兘涓虹┖鎴栧寘鍚壒娈婄鍙凤紝姣斿$鎴栬€?");
                        } else {
                                String wrappedContent;
                                if (replyUid != null) {
                                        wrappedContent = UserManager.getInstance().getCurrentUserObjectId() + "$" + replyUid + "$" + content;
                                } else {
                                        wrappedContent = UserManager.getInstance().getCurrentUserObjectId() + "$" + content;
                                }
                                presenter.addComment(mShareMultipleLayoutAdapter.getData(currentPosition).getObjectId(), wrappedContent);
                                presenter.addComment(mShareMultipleLayoutAdapter.getData(currentPosition).getObjectId(), wrappedContent);
                        }
                        input.setText("");

                }
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == Activity.RESULT_OK) {
                        switch (requestCode) {
                                case Constant.REQUEST_CODE_EDIT_USER_INFO:
                                        mUser = (User) data.getSerializableExtra("user");
                                        updateUserData();
                                        Intent intent = new Intent();
                                        intent.putExtra("user", mUser);
                                        setResult(Activity.RESULT_OK, intent);
                                        break;
                        }
                }
        }


        @Override
        protected void onDestroy() {
                super.onDestroy();
                GroupMessageService.unRegisterListener(this);
//                appBarLayout.removeOnOffsetChangedListener(this);
        }

        @Override
        public void onAddLiker(String id, String uid) {

        }

        @Override
        public void onDeleteLiker(String id, String uid) {

        }

        @Override
        public void onDeleteCommentMessage(String id, String content) {

        }

        @Override
        public void onAddCommentMessage(String id, String content) {

        }

        @Override
        public void onAddShareMessage(SharedMessage sharedMessage) {
                LogUtil.e("瀹炴椂妫€娴嬪埌璇磋娑堟伅鍒版媺");
                LogUtil.e(sharedMessage);
                if (mShareMultipleLayoutAdapter.getData().contains(sharedMessage)) {
                        SharedMessage oldSharedMessage = mShareMultipleLayoutAdapter.getSharedMessageById(sharedMessage.getObjectId());
                        if (oldSharedMessage != null) {
                                int index = mShareMultipleLayoutAdapter.getData().indexOf(oldSharedMessage);
                                mShareMultipleLayoutAdapter.getData().set(index, sharedMessage);
                        }
                } else {
                        mShareMultipleLayoutAdapter.getData().add(0, sharedMessage);
                }
                mShareMultipleLayoutAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDeleteShareMessage(String id) {

        }

        @Override
        public void onImageAvatarClick(String uid) {

        }

        @Override
        public void onNameClick(String uid) {

        }

        @Override
        public void onCommentBtnClick(View view, String id, int shareMessagePosition, boolean isLike) {
                LogUtil.e("鐐瑰嚮璇勮鎸夐挳");
                LogUtil.e("鏄惁宸茬偣璧? + isLike);
                LogUtil.e("浣嶇疆" + shareMessagePosition);
                currentId = id;
                currentPosition = shareMessagePosition;
                if (mCommentPopupWindow == null) {
                        mCommentPopupWindow = new CommentPopupWindow(this);
                        mCommentPopupWindow.setOnCommentPopupItemClickListener(new CommentPopupWindow.OnCommentPopupItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position, boolean isLiker) {
                                        if (position == 0) {
                                                LogUtil.e("澶勭悊璧炵殑鎿嶄綔");
                                                if (isLiker) {
                                                        LogUtil.e("杩欓噷鍙栨秷鐐硅禐鎿嶄綔");
                                                        LogUtil.e("鍒犻櫎鐐硅禐鏄殑ID" + currentId);
                                                        presenter.deleteLiker(currentId);
                                                } else {
                                                        LogUtil.e("杩欓噷娣诲姞鐐硅禐鎿嶄綔");
                                                        LogUtil.e("position涓哄灏? + position);
                                                        LogUtil.e("娣诲姞鐐硅禐鏃剁殑ID" + currentId);
                                                        presenter.addLiker(currentId);
                                                }
                                        } else if (position == 1) {
                                                LogUtil.e("澶勭悊璇勮鐨勬搷浣?);
//                                                杩涜浣嶇Щ
                                                currentCommentPosition = -1;
                                                replyUid = null;
                                                commentItemOffset = 0;
                                                dealBottomView(true);
                                        }
                                }
                        });
                }
                if (isLike) {
                        mCommentPopupWindow.setLikerName("鍙栨秷");
                } else {
                        mCommentPopupWindow.setLikerName("璧?);
                }
                mCommentPopupWindow.showPopupWindow(view);

        }

        private void dealBottomView(boolean isShow) {
                if (isShow) {
                        bottomInput.setVisibility(View.VISIBLE);
                        CommonUtils.showSoftInput(this, input);
                        input.requestFocus();
                } else {
                        bottomInput.setVisibility(View.GONE);
                        CommonUtils.hideSoftInput(this, input);
                }
        }

        @Override
        public void onCommentItemClick(View view, String id, int shareMessagePosition, int commentPosition, String replyUid) {
                LogUtil.e("浣嶇疆" + shareMessagePosition);
                currentPosition = shareMessagePosition;
                currentCommentPosition = commentPosition;
                ViewParent viewParent = view.getParent();
                if (viewParent != null) {
                        ViewGroup parent = (ViewGroup) viewParent;
                        commentItemOffset += parent.getHeight() - view.getBottom();
                        if (parent.getParent() != null) {
                                ViewGroup rootParent = (ViewGroup) parent.getParent();
                                commentItemOffset += rootParent.getHeight() + parent.getBottom();
                        }
                }
                this.replyUid = replyUid;
                dealBottomView(true);
        }

        @Override
        public void onCommentItemLongClick(String id, int shareMessagePosition, int commentPosition) {
                currentPosition = shareMessagePosition;
                currentCommentPosition = commentPosition;
                showCommentDialog(id, commentPosition);
        }

        private void showCommentDialog(final String id, final int commentPosition) {
                List<String> list = new ArrayList<>();
                list.add("澶嶅埗");
                if (CommonUtils.content2List(mShareMultipleLayoutAdapter.getSharedMessageById(id).getCommentMsgList().get(commentPosition)).get(0).equals(UserManager.getInstance().getCurrentUser().getObjectId())) {
                        list.add("鍒犻櫎");
                }
                showChooseDialog("鎿嶄綔", list, new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position1, long id1) {
                                dismissBaseDialog();
                                if (position1 == 0) {
                                        LogUtil.e("澶嶅埗鎿嶄綔");
                                } else {
                                        LogUtil.e("鍒犻櫎鎿嶄綔");
                                        presenter.deleteComment(id, commentPosition);
                                }
                        }
                });
        }

        @Override
        public void onCommentItemNameClick(String uid) {
                enterUserDetailActivity(uid);
        }

        private void enterUserDetailActivity(String uid) {
                if (uid.equals(UserManager.getInstance().getCurrentUser().getObjectId())) {
                        return;
                }
                Intent intent = new Intent(this, UserDetailActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("from", "other");
                startActivity(intent);
        }

        @Override
        public void onLikerTextViewClick(String uid) {
                enterUserDetailActivity(uid);
        }

        @Override
        public void onLinkViewClick(SharedMessage shareMessage) {

        }

        @Override
        public void onDeleteShareMessageClick(final String id) {
                LogUtil.e("鍒犻櫎");
                showBaseDialog("鎻愮ず", "纭畾瑕佸垹闄よ璇磋娑堟伅鍚?", "鍙栨秷", "纭畾", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                cancelBaseDialog();
                        }
                }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dismissBaseDialog();
                                presenter.deleteShareMessage(id);
                        }
                });
        }

        @Override
        public void onPhotoItemClick(final View view, final String id, final int photoPosition, final String url) {
                LogUtil.e("鐐瑰嚮鍥剧墖");
                LogUtil.e("鐐瑰嚮鐨勮璇碔D");
                List<String> cache = MessageCacheManager.getInstance().getShareMessageCache(id);
                final String newUrl;
                if (!url.contains("$")) {
                        LogUtil.e("杩欐槸鍥剧墖URL");
                        if (cache == null || cache.size() == 0) {
                                LogUtil.e("娌℃湁缂撳瓨璇佹槑涓嶆槸鑷繁鍙戠殑璇磋");
                                newUrl = url;
                        } else {
                                LogUtil.e("鏈夌紦瀛樿瘉鏄庢槸鑷繁鐨勫浘鐗囪璇达紝杩欓噷灏辨槸鐢ㄧ紦瀛?);
                                newUrl = cache.get(photoPosition);
                        }
                        LogUtil.e("url:" + newUrl);
                        File file = new File(newUrl);
                        if (file.exists()) {
                                LogUtil.e("鎻愬墠鍔犺浇鍥剧墖鏂囦欢鎴愬姛");
                                List<ImageItem> list = new ArrayList<>();
                                ImageItem imageItem;
                                for (String str :
                                        mShareMultipleLayoutAdapter.getSharedMessageById(id).getImageList()) {
                                        imageItem = new ImageItem();
                                        imageItem.setPath(str);
                                        LogUtil.e("url" + str);
                                }
                                CommonImageLoader.getInstance().setSelectedImages(list);
                                Intent intent = new Intent(UserDetailActivity.this, ImageDisplayActivity.class);
                                intent.putStringArrayListExtra("urlList", (ArrayList<String>) mShareMultipleLayoutAdapter.getSharedMessageById(id).getImageList());
                                intent.putExtra("position", photoPosition);
                                intent.putExtra("name", "photo");
                                intent.putExtra("url", url);
                                LogUtil.e("鍚姩鍥剧墖娴忚鐣岄潰11");
                                startActivity(intent);
                                ActivityCompat.startActivity(UserDetailActivity.this, intent, ActivityOptionsCompat.makeSceneTransitionAnimation(UserDetailActivity.this, view, "photo").toBundle());
//                                Picasso.with(this).load(file).fetch(new Callback() {
//                                        @Override
//                                        public void onSuccess() {
//
//                                        }
//
//                                        @Override
//                                        public void onError() {
//                                                LogUtil.e("鎻愬墠鍔犺浇澶辫触");
//                                                ToastUtils.showShortToast("鍔犺浇澶辫触");
//                                        }
//                                });
                        } else {
                                List<ImageItem> list = new ArrayList<>();
                                ImageItem imageItem;
                                for (String str :
                                        mShareMultipleLayoutAdapter.getSharedMessageById(id).getImageList()) {
                                        imageItem = new ImageItem();
                                        imageItem.setPath(str);
                                        LogUtil.e("url" + str);
                                }
                                CommonImageLoader.getInstance().setSelectedImages(list);
                                Intent intent = new Intent(UserDetailActivity.this, ImageDisplayActivity.class);
                                intent.putStringArrayListExtra("urlList", (ArrayList<String>) mShareMultipleLayoutAdapter.getSharedMessageById(id).getImageList());
                                intent.putExtra("position", photoPosition);
                                intent.putExtra("name", "photo");
                                intent.putExtra("url", url);
                                LogUtil.e("鍚姩鍥剧墖娴忚鐣岄潰11");
                                startActivity(intent);
//                                Picasso.with(this).load(newUrl).fetch(new Callback() {
//                                        @Override
//                                        public void onSuccess() {
//                                                LogUtil.e("1鎻愬墠鍔犺浇鎴愬姛");
//                                                LogUtil.e("鎻愬墠鍔犺浇鍥剧墖鏂囦欢鎴愬姛");
////                                                ActivityCompat.startActivity(getActivity(), intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, "photo").toBundle());
////                                                ImageDisplayActivity.start(getActivity(), view, newUrl);
//                                        }
//
//                                        @Override
//                                        public void onError() {
//                                                LogUtil.e("鎻愬墠鍔犺浇澶辫触");
//                                                ToastUtils.showShortToast("鍔犺浇澶辫触");
//                                        }
//                                });
                        }
                } else {
                        if (cache == null || cache.size() == 0) {
                                LogUtil.e("娌℃湁缂撳瓨璇佹槑涓嶆槸鑷繁鍙戠殑璇磋");
                                LogUtil.e("杩欐槸瑙嗛鍜屽皝闈㈡贩鍚堢殑URL鍒楄〃");
                                List<String> urlList = CommonUtils.content2List(url);
                                if (urlList != null && urlList.size() > 0) {
                                        String videoUrl = null;
                                        String imageUrl = null;
                                        LogUtil.e("瑙嗛URL鍒楄〃");
                                        for (String str : urlList
                                                ) {
                                                if (str.contains(".jpg") || str.contains(".png") || str.contains(".jpeg")) {
                                                        LogUtil.e("灏侀潰鍥剧墖URL" + str);
                                                        imageUrl = str;
                                                } else {
                                                        LogUtil.e("瑙嗛URL" + str);
                                                        videoUrl = str;
                                                }
                                        }
                                        if (videoUrl != null) {
                                                Intent videoIntent = new Intent(UserDetailActivity.this, ImageDisplayActivity.class);
                                                videoIntent.putExtra("name", "photo");
                                                videoIntent.putExtra("url", imageUrl);
                                                videoIntent.putExtra("videoUrl", videoUrl);
                                                videoIntent.putExtra("id", id);
                                                startActivity(videoIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(UserDetailActivity.this, view, "photo").toBundle());
                                        }
                                }
                        } else {
                                LogUtil.e("鏈夌紦瀛樿瘉鏄庢槸鑷繁鐨勮棰戣璇达紝杩欓噷灏辨槸鐢ㄧ紦瀛?);
                                String videoUrl = cache.get(1);
                                Intent intent = new Intent(UserDetailActivity.this, VideoPlayActivity.class);
                                intent.putExtra("path", videoUrl);
                                startActivity(intent);
                        }
                }

        }


        @Override
        public void updateShareMessageAdded(SharedMessage shareMessage) {

        }

        @Override
        public void updateShareMessageDeleted(String id) {
                SharedMessage sharedMessage = mShareMultipleLayoutAdapter.getSharedMessageById(id);
                LogUtil.e("灏嗚鍒犻櫎鐨勮璇存秷鎭暟鎹牸寮?);
                if (sharedMessage != null) {
                        LogUtil.e(sharedMessage);
                        mShareMultipleLayoutAdapter.getData().remove(sharedMessage);
                        mShareMultipleLayoutAdapter.notifyDataSetChanged();
//                杩欓噷閫氱煡鍒犻櫎
                        notifySharedMessageChanged(id, false);
//                        ((MainActivity) getActivity()).notifySharedMessageChanged(sharedMessage.getObjectId(), false);
                } else {
                        LogUtil.e("璇ヨ璇村凡鍒犻櫎");
                }
        }

        @Override
        public void updateLikerAdd(String id) {
                if (!mShareMultipleLayoutAdapter.getSharedMessageById(id).getLikerList().contains(UserManager.getInstance().getCurrentUserObjectId())) {
                        LogUtil.e("杩樻湭鐐硅禐锛岃繖閲屾坊鍔犵偣璧?);
                        mShareMultipleLayoutAdapter.getSharedMessageById(id).getLikerList().add(UserManager.getInstance().getCurrentUserObjectId());
                        mShareMultipleLayoutAdapter.notifyDataSetChanged();
                } else {
                        LogUtil.e("宸茬粡鐐硅禐锛岃繖閲屾坊鍔犵偣璧炲け璐?鍙兘鐨勫師鍥犳槸鍥犱负瀹炴椂宸茬粡妫€娴嬪埌鎷?);
                }
        }

        @Override
        public void updateLikerDeleted(String id) {
                if (mShareMultipleLayoutAdapter.getSharedMessageById(id).getLikerList().contains(UserManager.getInstance().getCurrentUserObjectId())) {
                        LogUtil.e("宸叉湁鐐硅禐锛岃繖閲屽垹闄ょ偣璧?);
                        mShareMultipleLayoutAdapter.getSharedMessageById(id).getLikerList().remove(UserManager.getInstance().getCurrentUserObjectId());
                        mShareMultipleLayoutAdapter.notifyDataSetChanged();
                } else {
                        LogUtil.e("娌℃湁鐐硅禐锛岃繖閲屽垹闄ょ偣璧炲け璐ワ紝鍙兘鐨勫師鍥犳槸鍥犱负瀹炴椂宸茬粡妫€娴嬪埌鍟?);
                }
        }

        @Override
        public void updateCommentAdded(String id, String content, int position) {
                LogUtil.e("鏇存柊娣诲姞璇勮鎿嶄綔锛岃繖閲屽氨涓嶆洿鏂颁簡锛屽洜涓哄湪瀹炴椂妫€娴嬬殑鏃跺€欏凡缁忔洿鏂版媺");
                dealBottomView(false);
                mShareMultipleLayoutAdapter.notifyDataSetChanged();

        }

        @Override
        public void updateCommentDeleted(String id, String content, int position) {
                LogUtil.e("鏇存柊鍒犻櫎璇勮鎿嶄綔锛岃繖閲屽氨涓嶆洿鏂颁簡锛屽洜涓哄湪瀹炴椂妫€娴嬬殑鏃跺€欏凡缁忔洿鏂板暒鍟﹀暒");
                mShareMultipleLayoutAdapter.notifyDataSetChanged();
        }

        @Override
        public void updateAllShareMessages(List<SharedMessage> data, boolean isPullRefresh) {
                if (data != null && data.size() > 0) {
                        mShareMultipleLayoutAdapter.addData(data);
                        ArrayList<String> idList = new ArrayList<>();
                        for (SharedMessage sharedMessage :
                                data) {
                                idList.add(sharedMessage.getObjectId());
                        }
                        notifySharedMessageChanged(idList, true);
                }
        }

        private void notifySharedMessageChanged(String id, boolean isAdd) {
                Intent intent = new Intent(Constant.NOTIFY_CHANGE_ACTION);
                ArrayList<String> idList = new ArrayList<>();
                idList.add(id);
                intent.putStringArrayListExtra("id", idList);
                intent.putExtra("isAdd", isAdd);
                sendBroadcast(intent);
        }

        private void notifySharedMessageChanged(ArrayList<String> list, boolean isAdd) {
                Intent intent = new Intent(Constant.NOTIFY_CHANGE_ACTION);
                intent.putStringArrayListExtra("id", list);
                intent.putExtra("isAdd", isAdd);
                sendBroadcast(intent);
        }

        @Override
        public void onRefresh() {
                SharedMessage shareMessage = mShareMultipleLayoutAdapter.getData(0);
                LogUtil.e("涓嬫媺鍔犺浇鐨勫墠涓€涓璇存秷鎭负1");
                if (shareMessage == null) {
                        LogUtil.e("棣栨鍔犺浇鏁版嵁");
                        loadData(true, "0000-00-00 01:00:00");
                } else {
                        LogUtil.e(shareMessage);
                        loadData(true, shareMessage.getCreatedAt());
                }
        }


        @Override
        public void hideLoading() {
                if (refresh.isRefreshing()) {
                        refresh.setRefreshing(false);
                }
                super.hideLoading();
        }

        @Override
        public void updateData(List<SharedMessage> sharedMessages) {

        }

        @Override
        public void loadMore() {
                int size = mShareMultipleLayoutAdapter.getData().size();
                if (size > 0) {
                        loadData(false, mShareMultipleLayoutAdapter.getData(size - 1).getCreatedAt());
                } else {
                        LogUtil.e("鑾峰彇鏇村鐨勬椂鍊檇ata涓虹┖");
                }
        }
}

