package com.example.chat.ui;

import android.app.Activity;
import android.content.Intent;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.adapter.ContactsAdapter;
import com.example.chat.base.Constant;
import com.example.chat.bean.User;
import com.example.chat.db.ChatDB;
import com.example.chat.listener.OnEditDataCompletedListener;
import com.example.chat.manager.MsgManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.util.CommonUtils;
import com.example.chat.util.LogUtil;
import com.example.chat.view.MyLetterView;
import com.example.commonlibrary.BaseApplication;
import com.example.commonlibrary.baseadapter.viewholder.BaseWrappedViewHolder;
import com.example.commonlibrary.cusotomview.ListViewDecoration;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.listener.SaveListener;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/27      20:36
 * QQ:             1981367757
 */
public class SelectedFriendsActivity extends SlideBaseActivity implements MyLetterView.MyLetterChangeListener, ContactsAdapter.OnItemCheckListener {
        RecyclerView display;
        MyLetterView mMyLetterView;
        SectionIndexer mIndexer;
        TextView middle;
        String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private LinearLayout bottomDisplay;
        private List<String> selectedContacts = new ArrayList<>();
        private ContactsAdapter adapter;
        private String from;
        private LinearLayoutManager mLinearLayoutManager;


        @Override
        public void initView() {
                display = (RecyclerView) findViewById(R.id.rcv_selected_friends_display);
                mMyLetterView = (MyLetterView) findViewById(R.id.ml_selected_friends_letter);
                middle = (TextView) findViewById(R.id.tv_selected_friends_middle);
                bottomDisplay = (LinearLayout) findViewById(R.id.ll_selected_friends_bottom);
                mMyLetterView.setListener(this);
        }

        @Override
        public void initData() {
                mLinearLayoutManager = new LinearLayoutManager(this);
                display.setLayoutManager(mLinearLayoutManager);
                display.addItemDecoration(new ListViewDecoration(this));
                display.setItemAnimator(new DefaultItemAnimator());
                mMyLetterView.setTextView(middle);
                adapter = new ContactsAdapter();
                adapter.setOnCheckedChangeListener(this);
                mIndexer = new AlphabetIndexer(ChatDB.create().getSortedKeyCursor(), 0, alphabet);
                adapter.setSectionIndexer(mIndexer);
                display.setAdapter(adapter);
                adapter.addData(UserCacheManager.getInstance().getAllContacts());
                initActionBar();
        }

        private void initActionBar() {
                from = getIntent().getStringExtra("from");
                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setNeedNavigation(true);
                toolBarOption.setTitle("閫夋嫨濂藉弸");
                toolBarOption.setRightText("瀹屾垚");
                toolBarOption.setRightListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if (from.equals("createGroup")) {
                                        List<String> list = new ArrayList<>();
                                        list.add("缇ょ粍鍚?);
                                        list.add("缇ゆ弿杩?);
                                        if (selectedContacts != null && selectedContacts.size() > 0) {
                                                showEditDialog("寤虹兢", list, new OnEditDataCompletedListener() {
                                                        @Override
                                                        public void onDataInputCompleted(List<String> data) {
                                                                if (!CommonUtils.isNetWorkAvailable(BaseApplication.getInstance())) {
                                                                        LogUtil.e("缃戠粶杩炴帴澶辫触");
                                                                        ToastUtils.showShortToast("缃戠粶杩炴帴澶辫触");
                                                                        return;
                                                                }
                                                                if (selectedContacts.size() > 0) {
                                                                        showLoadDialog("姝ｅ湪寤虹兢涓?....");
                                                                        MsgManager.getInstance().sendCreateGroupMessage(data.get(0), data.get(1), selectedContacts, new SaveListener() {
                                                                                @Override
                                                                                public void onSuccess() {
                                                                                        LogUtil.e("1鎬讳綋寤虹兢鎴愬姛");
                                                                                        ToastUtils.showShortToast("寤虹兢鎴愬姛");
                                                                                        dismissLoadDialog();
                                                                                        finish();
                                                                                }
                                                                                @Override
                                                                                public void onFailure(int i, String s) {
                                                                                        dismissLoadDialog();
                                                                                        LogUtil.e("鎬讳綋寤虹兢澶辫触" + s + i);
                                                                                }
                                                                        });
                                                                } else {
                                                                        ToastUtils.showShortToast("浣犳病鏈夐€夋嫨濂藉弸");
                                                                }
                                                        }
                                                });
                                        }
                                } else if (from.equals("select_visibility")) {
                                        Intent intent = new Intent();
                                        intent.putStringArrayListExtra(Constant.RESULT_CODE_SELECT_VISIBILITY, (ArrayList<String>) selectedContacts);
                                        setResult(Activity.RESULT_OK, intent);
                                        finish();
                                }
                        }
                });
                toolBarOption.setAvatar(null);
                setToolBar(toolBarOption);
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
                return R.layout.selected_friends;
        }

        @Override
        public void onLetterChanged(String s) {
                int index = alphabet.indexOf(s);
                mLinearLayoutManager.scrollToPosition(mIndexer.getPositionForSection(index));
        }

        private ImageView getImageView(String url, String tag) {
                LogUtil.e("娣诲姞View11111 ");
                ImageView avatar = new ImageView(SelectedFriendsActivity.this);
                int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                avatar.setLayoutParams(params);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatar.setPadding(5, 5, 5, 5);
                avatar.setTag(tag);
                Glide.with(this).load(url).into(avatar);
                return avatar;
        }



        @Override
        public void onItemChecked(boolean isCheck, User user, BaseWrappedViewHolder holder) {
                if (isCheck) {
                        selectedContacts.add(user.getObjectId());
                        bottomDisplay.addView(getImageView(user.getAvatar(), user.getObjectId()));

                } else {
                        selectedContacts.remove(user.getObjectId());
                        if (bottomDisplay.findViewWithTag(user.getObjectId()) != null) {
                                LogUtil.e("绉婚櫎View");
                                bottomDisplay.removeView(bottomDisplay.findViewWithTag(user.getObjectId()));
                        }
                }
                bottomDisplay.invalidate();
        }

        @Override
        public void updateData(Object o) {

        }
}

