package com.example.chat.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.core.app.ActivityOptionsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
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
import com.example.chat.manager.UserManager;
import com.example.chat.mvp.ShareMessageTask.ShareMessageContacts;
import com.example.chat.mvp.ShareMessageTask.ShareMessageModel;
import com.example.chat.mvp.ShareMessageTask.ShareMessagePresenter;
import com.example.chat.service.GroupMessageService;
import com.example.chat.ui.BasePreViewActivity;
import com.example.chat.ui.EditShareMessageActivity;
import com.example.chat.ui.HappyContentDisplayActivity;
import com.example.chat.ui.ImageDisplayActivity;
import com.example.chat.ui.MainActivity;
import com.example.chat.ui.SelectedPictureActivity;
import com.example.chat.ui.UserDetailActivity;
import com.example.chat.ui.VideoPlayActivity;
import com.example.chat.ui.WallPaperActivity;
import com.example.chat.ui.WeiXinNewsActivity;
import com.example.chat.util.ChatUtil;
import com.example.chat.util.CommonUtils;
import com.example.chat.util.LogUtil;
import com.example.chat.util.PhotoUtil;
import com.example.chat.view.CommentPopupWindow;
import com.example.chat.view.fab.FloatingActionButton;
import com.example.chat.view.fab.FloatingActionsMenu;
import com.example.commonlibrary.BaseActivity;
import com.example.commonlibrary.BaseApplication;
import com.example.commonlibrary.BaseFragment;
import com.example.commonlibrary.baseadapter.SuperRecyclerView;
import com.example.commonlibrary.baseadapter.foot.LoadMoreFooterView;
import com.example.commonlibrary.baseadapter.foot.OnLoadMoreListener;
import com.example.commonlibrary.baseadapter.manager.WrappedLinearLayoutManager;
import com.example.commonlibrary.utils.ToastUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/27      16:47
 * QQ:             1981367757
 */

public class ShareMessageFragment extends BaseFragment<List<SharedMessage>,ShareMessagePresenter> implements ShareMessageContacts.View<List<SharedMessage>>, View.OnClickListener, OnShareMessageReceivedListener, AdapterView.OnItemClickListener, OnShareMessageItemClickListener, OnLoadMoreListener {


    private SuperRecyclerView display;
    private SwipeRefreshLayout refresh;
    private WrappedLinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener;
    private ShareMultipleLayoutAdapter mAdapter;
    private LinearLayout bottomInput;
    private ImageView send;
    private EditText input;
    private RelativeLayout container;
    private ShareMessagePresenter presenter;
    private int screenHeight;
    private String simpleDate = "0000-00-00 01:00:00";
    private FloatingActionsMenu mMenu;
    private FloatingActionButton video;
    private FloatingActionButton normal;
    private FloatingActionButton image;
    //        private File photoFile;
    private View headerView;


    @Override
    public void onResume() {
        super.onResume();
        onHiddenChanged(false);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((MainActivity) getActivity()).initActionBar("鏈嬪弸鍦?);
        }
    }


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
        return R.layout.fragment_share_message;
    }

    @Override
    public void initView() {
        display = (SuperRecyclerView) findViewById(R.id.rcv_share_fragment_display);
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh_share_fragment_refresh);
        bottomInput = (LinearLayout) findViewById(R.id.ll_share_fragment_bottom);
        send = (ImageView) findViewById(R.id.iv_share_fragment_send);
        container = (RelativeLayout) findViewById(R.id.rl_share_fragment_container);
        mMenu = (FloatingActionsMenu) findViewById(R.id.fam_share_message_menu);
        normal = (FloatingActionButton) findViewById(R.id.fab_share_message_normal);
        video = (FloatingActionButton) findViewById(R.id.fab_share_message_video);
        input = (EditText) findViewById(R.id.et_share_fragment_input);
        image = (FloatingActionButton) findViewById(R.id.fab_share_message_image);
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                container.getWindowVisibleDisplayFrame(rect);
                screenHeight = container.getRootView().getHeight();
                int keyBoardHeight = screenHeight - rect.bottom;
                if (keyBoardHeight != mKeyBoardHeight) {
                    if (keyBoardHeight > mKeyBoardHeight) {
                        bottomInput.setVisibility(View.VISIBLE);
                        mMenu.setVisibility(View.GONE);
                        mKeyBoardHeight = keyBoardHeight;
                        mLinearLayoutManager.scrollToPositionWithOffset(currentPosition, getListOffset());
                    } else {
                        mKeyBoardHeight = keyBoardHeight;
                        mMenu.setVisibility(View.VISIBLE);
                        bottomInput.setVisibility(View.GONE);
                    }
                }
            }
        });
        send.setOnClickListener(this);
        normal.setOnClickListener(this);
        video.setOnClickListener(this);
        image.setOnClickListener(this);
        display.setLoadMoreFooterView(new LoadMoreFooterView(getContext()));
        display.setOnLoadMoreListener(this);
        display.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mCommentPopupWindow != null && mCommentPopupWindow.isShowing()) {
                    mCommentPopupWindow.dismiss();
                }
                if (mMenu.isExpanded()) {
                    mMenu.collapse();
                }
                LogUtil.e("container:onTouch");
//                                杩欓噷杩涜鐐瑰嚮鍏抽棴缂栬緫妗?                if (bottomInput.getVisibility() == View.VISIBLE) {
                    LogUtil.e("瑙︽懜鐣岄潰鐐瑰嚮鍏抽棴杈撳叆娉?);
                    dealBottomView(false);
                    return true;
                }
                return false;
            }
        });
        refresh.setOnRefreshListener(mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //                                鏁版嵁閲忔瘮杈冨皬锛岃繘琛屽欢鏃?2绉掑悗鍐嶈繘琛屽姞杞?                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SharedMessage shareMessage = mAdapter.getData(0);
                        LogUtil.e("涓嬫媺鍔犺浇鐨勫墠涓€涓璇存秷鎭负1");
                        if (shareMessage == null) {
                            LogUtil.e("棣栨鍔犺浇鏁版嵁");
                            loadData(true, "0000-00-00 01:00:00");
                        } else {
                            LogUtil.e(shareMessage);
                            loadData(true, shareMessage.getCreatedAt());
                        }
                    }
                }, 2000);
            }
        });
        display.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                                杩欓噷杩涜閲嶅彂锛屽鏋滃湪婊氬姩鐨勬椂鍊欏氨涓嶈繘琛屾暟鎹殑鍔犺浇鍜岃鍥剧殑鏄剧ず
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LogUtil.e("浼戦棽鐨勬椂鍊欒繘琛屽姞杞?);
                    Glide.with(getActivity()).resumeRequests();
                } else {
                    LogUtil.e("婊氬姩鐨勬椂鍊欎笉杩涜鍔犺浇");
                    Glide.with(getActivity()).pauseRequests();
                }
            }
        });
    }


    private int getListOffset() {
        int offset = 0;
        if (currentCommentPosition == -1) {
            ToastUtils.showShortToast("鐐瑰嚮璇勮");
            LogUtil.e("鐐瑰嚮璇勮111");
            int firstVisiblePosition = mLinearLayoutManager.findFirstVisibleItemPosition();
            View view = mLinearLayoutManager.getChildAt(currentPosition - firstVisiblePosition);
            offset += view.getHeight();
        }
        offset += mKeyBoardHeight;
        offset += bottomInput.getHeight();
//                offset += ((BaseActivity) getActivity()).getStatusHeight();
        if (replyUid != null) {
            offset += commentItemOffset;
        }
        return screenHeight - offset;
    }


    private int mKeyBoardHeight = 0;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constant.REQUEST_CODE_EDIT_SHARE_MESSAGE:
                    if (data != null) {
                        SharedMessage sharedMessage = (SharedMessage) data.getSerializableExtra(Constant.RESULT_CODE_SHARE_MESSAGE);
                        LogUtil.e(sharedMessage);
                        updateShareMessageAdded(sharedMessage);
                        ((MainActivity) getActivity()).notifySharedMessageChanged(sharedMessage.getObjectId(), true);
                    }
                    break;
                case Constant.REQUEST_CODE_SELECT_TITLE_WALLPAPER:
                    LogUtil.e("鏀瑰彉鑳屾櫙鎴愬姛鍚庨噸缁?1111");
                    updateHeaderView();
                    break;
                case Constant.REQUEST_CODE_SELECT_PICTURE:
//                                        杩欓噷杩涘叆缂栬緫鍥剧墖鐣岄潰
                    if (CommonImageLoader.getInstance().getSelectedImages().get(0) != null) {
                        String path = CommonImageLoader.getInstance().getSelectedImages().get(0).getPath();
                        cropPhoto(path);
                    }
                    break;
                case Constant.REQUEST_CODE_CROP:
                    LogUtil.e("瑁佸壀瀹屾垚");
                    try {
                        showLoading("姝ｅ湪涓婁紶鑳屾櫙锛岃绋嶅€?.......");
                        final BmobFile bmobFile = new BmobFile(new File(new URI(PhotoUtil.buildUri(getActivity()).toString())));
                        bmobFile.uploadblock(BaseApplication.getInstance(), new UploadFileListener() {
                            @Override
                            public void onSuccess() {
                                UserManager.getInstance().updateUserInfo("titleWallPaper", bmobFile.getFileUrl(BaseApplication.getInstance()), new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        hideLoading();
                                        LogUtil.e("鏇存柊鐢ㄦ埛鑳屾櫙鎴愬姛");
                                        UserManager.getInstance().getCurrentUser().setTitleWallPaper(bmobFile.getFileUrl(BaseApplication.getInstance()));
                                        updateHeaderView();
                                    }

                                    @Override
                                    public void onFailure(int i, String s) {
                                        hideLoading();
                                        LogUtil.e("鏇存柊鐢ㄦ埛鑳屾櫙澶辫触" + s + i);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                hideLoading();
                                LogUtil.e("鍔犺浇澶辫触");
                            }
                        });
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void updateHeaderView() {
        Glide.with(this).load(UserManager.getInstance().getCurrentUser().getTitleWallPaper())
                .into((ImageView) headerView.findViewById(R.id.iv_share_fragment_item_header_background));
    }

    private void cropPhoto(String path) {
        Uri uri = Uri.fromFile(new File(path));
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 400);
        cropIntent.putExtra("outputY", 200);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        Uri cropUri = PhotoUtil.buildUri(getActivity());
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        if (cropIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cropIntent, Constant.REQUEST_CODE_CROP);
        }
    }


    private void loadData(boolean isPullRefresh, String time) {
        presenter.loadAllShareMessages(isPullRefresh, time);
    }

    @Override
    public void initData() {
        presenter = new ShareMessagePresenter(this, new ShareMessageModel(ChatApplication
                .getChatMainComponent().getMainRepositoryManager()));
        mAdapter = new ShareMultipleLayoutAdapter();
        display.addHeaderView(getHeaderView());
        mAdapter.setOnShareMessageItemClickListener(this);
        display.setLayoutManager(mLinearLayoutManager = new WrappedLinearLayoutManager(getActivity()));
        display.setItemAnimator(new DefaultItemAnimator());
        display.setLoadMoreFooterView(new LoadMoreFooterView(getContext()));
        display.setOnLoadMoreListener(this);
        display.setAdapter(mAdapter);
        GroupMessageService.registerListener(this);
        mMenu.attachToRecyclerView(display);

        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
                mOnRefreshListener.onRefresh();
            }
        });
    }

    @Override
    protected void updateView() {
        long deltaTime = BaseApplication.getAppComponent()
                .getSharedPreferences().getLong(ChatUtil.DELTA_TIME, -1L);
        LogUtil.e("杩欓噷閫氳繃缂撳瓨鐨勬椂闂村樊鍊兼潵璁＄畻鍑烘湇鍔″櫒涓婄殑鏃堕棿");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long realServerTime = System.currentTimeMillis() - deltaTime;
        simpleDate = simpleDateFormat.format(new Date(realServerTime));
        LogUtil.e("寰楀埌鐨勬椂闂存牸寮忎负:" + simpleDate);
//                        display.setRefreshing(true);
        refresh.setRefreshing(true);
        mOnRefreshListener.onRefresh();
        LogUtil.e("璇磋鐣岄潰闅愯棌浜嗭紝杩欓噷鍙栨秷鐩戝惉 ");
        if (mAdapter.getData().size() > 0) {
            for (SharedMessage sharedMessage :
                    mAdapter.getData()) {
                ((MainActivity) getActivity()).notifySharedMessageChanged(sharedMessage.getObjectId(), false);
            }
        }
    }

    private View getHeaderView() {
        User currentUser = UserManager.getInstance().getCurrentUser();
        headerView = View.inflate(getContext(), R.layout.share_fragment_item_header_layout, null);
        ((TextView) headerView.findViewById(R.id.tv_share_fragment_item_header_name)).setText(currentUser.getNick());
        ImageView avatar = (ImageView) headerView.findViewById(R.id.iv_share_fragment_item_header_avatar);
        ImageView bg = (ImageView) headerView.findViewById(R.id.iv_share_fragment_item_header_background);
        Glide.with(this).load(currentUser.getAvatar()).into(avatar);
        Glide.with(this).load(currentUser.getTitleWallPaper()).into(bg);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterUserDetailActivity(UserManager.getInstance().getCurrentUser().getObjectId());
            }
        });
        bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e("鐐瑰嚮鑳屾櫙鍥剧墖");
                List<String> list = new ArrayList<>();
                list.add("鐩稿唽");
                list.add("绯荤粺");
                showChooseDialog("鎸戦€夎儗鏅浘", list, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        hideBaseDialog();
                        if (position == 0) {
                            CommonImageLoader.getInstance().initStanderConfig(1);
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), SelectedPictureActivity.class);
                            startActivityForResult(intent, Constant.REQUEST_CODE_SELECT_PICTURE);
                        } else {
                            Intent wallPaperIntent = new Intent(getActivity(), WallPaperActivity.class);
                            wallPaperIntent.putExtra("from", "title_wallpaper");
                            startActivityForResult(wallPaperIntent, Constant.REQUEST_CODE_SELECT_TITLE_WALLPAPER);
                        }
                    }
                });
            }
        });
        return headerView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        GroupMessageService.unRegisterListener(this);
    }

    @Override
    public void updateShareMessageAdded(SharedMessage shareMessage) {
        if (shareMessage != null) {
            mAdapter.addData(0, shareMessage);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateShareMessageDeleted(String id) {
        SharedMessage sharedMessage = mAdapter.getSharedMessageById(id);
        LogUtil.e("灏嗚鍒犻櫎鐨勮璇存秷鎭暟鎹牸寮?);
        if (sharedMessage != null) {
            LogUtil.e(sharedMessage);
            mAdapter.getData().remove(sharedMessage);
            mAdapter.notifyDataSetChanged();
//                杩欓噷閫氱煡鍒犻櫎
            ((MainActivity) getActivity()).notifySharedMessageChanged(sharedMessage.getObjectId(), false);
        } else {
            LogUtil.e("璇ヨ璇村凡鍒犻櫎");
        }
    }


//        鐢变簬鏇存柊闇€瑕佺殑鐐硅禐鏁版嵁鍦ㄦ暟鎹簱涓幏鍙栵紝鎵€浠ヨ繖閲屽彧闇€瑕乶otifyDataSetChanged涓嬪氨琛屼簡

    @Override
    public void updateLikerAdd(String id) {
        if (!mAdapter.getSharedMessageById(id).getLikerList().contains(UserManager.getInstance().getCurrentUserObjectId())) {
            LogUtil.e("杩樻湭鐐硅禐锛岃繖閲屾坊鍔犵偣璧?);
            mAdapter.getSharedMessageById(id).getLikerList().add(UserManager.getInstance().getCurrentUserObjectId());
        } else {
            LogUtil.e("宸茬粡鐐硅禐锛岃繖閲屾坊鍔犵偣璧炲け璐?鍙兘鐨勫師鍥犳槸鍥犱负瀹炴椂宸茬粡妫€娴嬪埌鎷?);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateLikerDeleted(String id) {
        if (mAdapter.getSharedMessageById(id).getLikerList().contains(UserManager.getInstance().getCurrentUserObjectId())) {
            LogUtil.e("宸叉湁鐐硅禐锛岃繖閲屽垹闄ょ偣璧?);
            mAdapter.getSharedMessageById(id).getLikerList().remove(UserManager.getInstance().getCurrentUserObjectId());
        } else {
            LogUtil.e("娌℃湁鐐硅禐锛岃繖閲屽垹闄ょ偣璧炲け璐ワ紝鍙兘鐨勫師鍥犳槸鍥犱负瀹炴椂宸茬粡妫€娴嬪埌鍟?);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateCommentAdded(String id, String content, int position) {
        LogUtil.e("鏇存柊娣诲姞璇勮鎿嶄綔锛岃繖閲屽氨涓嶆洿鏂颁簡锛屽洜涓哄湪瀹炴椂妫€娴嬬殑鏃跺€欏凡缁忔洿鏂版媺");
        dealBottomView(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateCommentDeleted(String id, String content, int position) {
        LogUtil.e("鏇存柊鍒犻櫎璇勮鎿嶄綔锛岃繖閲屽氨涓嶆洿鏂颁簡锛屽洜涓哄湪瀹炴椂妫€娴嬬殑鏃跺€欏凡缁忔洿鏂板暒鍟﹀暒");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateAllShareMessages(List<SharedMessage> data, boolean isPullRefresh) {

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
    public void onImageAvatarClick(String uid) {
        LogUtil.e("鐐瑰嚮鐢ㄦ埛澶村儚鍥炶皟");
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        if (uid.equals(UserManager.getInstance().getCurrentUserObjectId())) {
            intent.putExtra("from", "me");
        } else {
            intent.putExtra("from", "other");
        }
        intent.putExtra("uid", uid);
        startActivity(intent);
    }

    @Override
    public void onNameClick(String uid) {
        LogUtil.e("鐐瑰嚮鐢ㄦ埛濮撳悕鍥炶皟");
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        if (uid.equals(UserManager.getInstance().getCurrentUserObjectId())) {
            intent.putExtra("from", "me");
        } else {
            intent.putExtra("from", "other");
        }
        intent.putExtra("uid", uid);
        startActivity(intent);
    }


    private CommentPopupWindow mCommentPopupWindow;
    private int currentPosition;
    private String currentId;

    @Override
    public void onCommentBtnClick(View view, String id, int shareMessagePosition, final boolean isLike) {
        LogUtil.e("鐐瑰嚮璇勮鎸夐挳");
        LogUtil.e("鏄惁宸茬偣璧? + isLike);
        LogUtil.e("浣嶇疆" + shareMessagePosition);
        currentId = id;
        currentPosition = shareMessagePosition;
        if (mCommentPopupWindow == null) {
            mCommentPopupWindow = new CommentPopupWindow(getContext());
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

    private int currentCommentPosition;
    private String replyUid;
    private int commentItemOffset = 0;

    @Override
    public void onCommentItemClick(View view, String id, int shareMessagePosition, int position, String replyUser) {
        LogUtil.e("浣嶇疆" + shareMessagePosition);
        currentPosition = shareMessagePosition;
        currentCommentPosition = position;
        ViewParent viewParent = view.getParent();
        if (viewParent != null) {
            ViewGroup parent = (ViewGroup) viewParent;
            commentItemOffset += parent.getHeight() - view.getBottom();
            if (parent.getParent() != null) {
                ViewGroup rootParent = (ViewGroup) parent.getParent();
                commentItemOffset += rootParent.getHeight() + parent.getBottom();
            }
        }
        this.replyUid = replyUser;
        dealBottomView(true);
    }

    private void dealBottomView(boolean isShow) {
        if (isShow) {
            bottomInput.setVisibility(View.VISIBLE);
            CommonUtils.showSoftInput(getContext(), input);
            input.requestFocus();
        } else {
            bottomInput.setVisibility(View.GONE);
            CommonUtils.hideSoftInput(getContext(), input);
        }
    }

    @Override
    public void onCommentItemLongClick(String id, int shareMessagePosition, int position) {
        currentPosition = shareMessagePosition;
        currentCommentPosition = position;
        showCommentDialog(id, position);
    }

    private void showCommentDialog(final String id, final int position) {
        List<String> list = new ArrayList<>();
        list.add("澶嶅埗");
        if (CommonUtils.content2List(mAdapter.getSharedMessageById(id).getCommentMsgList().get(position)).get(0).equals(UserManager.getInstance().getCurrentUser().getObjectId())) {
            list.add("鍒犻櫎");
        }
        ((BaseActivity) getActivity()).showChooseDialog("鎿嶄綔", list, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position1, long id1) {
                ((BaseActivity) getActivity()).dismissBaseDialog();
                if (position1 == 0) {
                    LogUtil.e("澶嶅埗鎿嶄綔");
                } else {
                    LogUtil.e("鍒犻櫎鎿嶄綔");
                    presenter.deleteComment(id, position);
                }
            }
        });
    }

    @Override
    public void onCommentItemNameClick(String uid) {
        enterUserDetailActivity(uid);
    }


    @Override
    public void onLikerTextViewClick(String uid) {
        enterUserDetailActivity(uid);
    }

    @Override
    public void onLinkViewClick(SharedMessage shareMessage) {

        Intent happyContentIntent = new Intent(getActivity(), HappyContentDisplayActivity.class);
        if (shareMessage.getUrlList().size() == 0) {
//                     绗戣瘽
            happyContentIntent.putExtra("content", shareMessage.getUrlTitle());
            startActivity(happyContentIntent);
        } else if (shareMessage.getUrlList().size() == 1) {
//                        瓒ｅ浘
            if (shareMessage.getUrlTitle() == null) {
//                                缇庡コ鍥剧墖
                List<ImageItem> list = new ArrayList<>();
                ImageItem imageItem = new ImageItem();
                imageItem.setPath(shareMessage.getUrlList().get(0));
                list.add(imageItem);
                BasePreViewActivity.startBasePreview(getActivity(), list, 0);
            } else {
                happyContentIntent.putExtra("content", shareMessage.getUrlTitle());
                happyContentIntent.putExtra("url", shareMessage.getUrlList().get(0));
                startActivity(happyContentIntent);
            }
        } else {
//                        寰俊绮鹃€?            WeiXinNewsActivity.start(getActivity(), shareMessage.getUrlList().get(0), shareMessage.getUrlList().get(1));
        }
    }

    private void enterUserDetailActivity(String uid) {
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra("uid", uid);
        if (!uid.equals(UserManager.getInstance().getCurrentUserObjectId())) {
            intent.putExtra("from", "other");
        } else {
            intent.putExtra("from", "me");
        }
        startActivity(intent);
    }


    @Override
    public void onDeleteShareMessageClick(final String id) {
        LogUtil.e("鍒犻櫎");
        ((BaseActivity) (getActivity())).showBaseDialog("鎻愮ず", "纭畾瑕佸垹闄よ璇磋娑堟伅鍚?", "鍙栨秷", "纭畾", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity) getActivity()).cancelBaseDialog();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity) getActivity()).dismissBaseDialog();
                presenter.deleteShareMessage(id);
            }
        });
    }

    @Override
    public void onPhotoItemClick(final View view, final String id, final int position, final String url) {
        LogUtil.e("鐐瑰嚮鍥剧墖");
        List<String> cache = MessageCacheManager.getInstance().getShareMessageCache(id);
        if (!url.contains("$")) {
            List<ImageItem> list = new ArrayList<>();
            if (cache != null) {
                for (String cacheId :
                        cache) {
                    ImageItem imageItem = new ImageItem();
                    imageItem.setPath(cacheId);
                    list.add(imageItem);
                }
            } else {
                SharedMessage message = mAdapter.getSharedMessageById(id);
                for (String pId :
                        message.getImageList()) {
                    ImageItem imageItem = new ImageItem();
                    imageItem.setPath(pId);
                    list.add(imageItem);
                }
            }
            BasePreViewActivity.startBasePreview(getActivity(), list, position);
        } else {
            if (cache == null || cache.size() == 0) {
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
                        Intent videoIntent = new Intent(getContext(), ImageDisplayActivity.class);
                        videoIntent.putExtra("name", "photo");
                        videoIntent.putExtra("url", imageUrl);
                        videoIntent.putExtra("videoUrl", videoUrl);
                        videoIntent.putExtra("id", id);
                        startActivity(videoIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, "photo").toBundle());
                    }
                }
            } else {
                LogUtil.e("鏈夌紦瀛樿瘉鏄庢槸鑷繁鐨勮棰戣璇达紝杩欓噷灏辨槸鐢ㄧ紦瀛?);
                String videoUrl = cache.get(1);
                Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                intent.putExtra("path", videoUrl);
                startActivity(intent);
            }
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_share_fragment_send) {
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
                presenter.addComment(mAdapter.getData(currentPosition - 1).getObjectId(), wrappedContent);
            }
            input.setText("");

        } else if (i == R.id.fab_share_message_normal) {
            if (mMenu.isExpanded()) {
                mMenu.collapse();
            }
            Intent intent = new Intent(getActivity(), EditShareMessageActivity.class);
            intent.putExtra("destination", "text");
            startActivityForResult(intent, Constant.REQUEST_CODE_EDIT_SHARE_MESSAGE);

        } else if (i == R.id.fab_share_message_video) {
            if (mMenu.isExpanded()) {
                mMenu.collapse();
            }
            Intent videoIntent = new Intent(getActivity(), EditShareMessageActivity.class);
            videoIntent.putExtra("destination", "video");
            startActivityForResult(videoIntent, Constant.REQUEST_CODE_EDIT_SHARE_MESSAGE);

        } else if (i == R.id.fab_share_message_image) {
            if (mMenu.isExpanded()) {
                mMenu.collapse();
            }
            Intent imageIntent = new Intent(getActivity(), EditShareMessageActivity.class);
            imageIntent.putExtra("destination", "image");
            startActivityForResult(imageIntent, Constant.REQUEST_CODE_EDIT_SHARE_MESSAGE);

        }
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
        mAdapter.addData(sharedMessage);
    }

    @Override
    public void onDeleteShareMessage(String id) {
        LogUtil.e("瀹炴椂妫€娴嬪埌鍒犻櫎璇磋娑堟伅鍒板暒");
        LogUtil.e("娑堟伅涓猴細" + id);
        SharedMessage sharedMessage = mAdapter.getSharedMessageById(id);
        LogUtil.e("灏嗚绉婚櫎鐨勮璇存秷鎭牸寮?);
        if (sharedMessage != null) {
            LogUtil.e(sharedMessage);
            mAdapter.getData().remove(sharedMessage);
            mAdapter.notifyDataSetChanged();
            ((MainActivity) getActivity()).notifySharedMessageChanged(id, false);
        } else {
            LogUtil.e("鍒犻櫎娑堟伅绔熺劧涓虹┖锛屾€庝箞鍥炰簨?????");
        }
    }

    public void notifyUrlShareMessageCome(SharedMessage sharedMessage) {
        updateShareMessageAdded(sharedMessage);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((MainActivity) getActivity()).dismissBaseDialog();
        switch (position) {
            case 0:
                CommonImageLoader.getInstance().initStanderConfig(1);
                Intent intent = new Intent();
                intent.setClass(getActivity(), SelectedPictureActivity.class);
                startActivityForResult(intent, Constant.REQUEST_CODE_SELECT_PICTURE);
                break;
            case 1:
                Intent wallPaperIntent = new Intent(getActivity(), WallPaperActivity.class);
                wallPaperIntent.putExtra("from", "title_wallpaper");
                startActivityForResult(wallPaperIntent, Constant.REQUEST_CODE_SELECT_TITLE_WALLPAPER);
                break;
            default:
                break;
        }
    }


    @Override
    public void loadMore() {
        int size = mAdapter.getData().size();
        if (mAdapter != null && size > 0) {
            loadData(false, mAdapter.getData(size - 1).getCreatedAt());
        } else {
            LogUtil.e("鑾峰彇鏇村鐨勬椂鍊檇ata涓虹┖");
        }
    }
}
