package com.example.chat.ui;

import android.app.Activity;
import android.content.Intent;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.chat.R;
import com.example.chat.adapter.SearchFriendAdapter;
import com.example.chat.bean.User;
import com.example.chat.manager.UserManager;
import com.example.chat.util.LogUtil;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemChildClickListener;
import com.example.commonlibrary.cusotomview.ListViewDecoration;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.utils.ToastUtils;

import java.util.List;

import cn.bmob.v3.listener.FindListener;


/**
 * 椤圭洰鍚嶇О:    HappyChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/9/25      12:18
 * QQ:             1981367757
 */
public class SearchFriendActivity extends SlideBaseActivity implements View.OnClickListener {
        private EditText input;
        //        private SearchFriendAdapter adapter;
        private RecyclerView display;
        private Button search;
        private SearchFriendAdapter mAdapter;



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
                return R.layout.activity_search_friend;
        }

        @Override
        public void initView() {
                input = (EditText) findViewById(R.id.et_search_friend_input);
                search = (Button) findViewById(R.id.btn_search_friend);
                display = (RecyclerView) findViewById(R.id.swrc_search_friend_display);
                display.setLayoutManager(new LinearLayoutManager(this));
                display.setItemAnimator(new DefaultItemAnimator());
                display.addItemDecoration(new ListViewDecoration(this));
                search.setOnClickListener(this);

        }


        @Override
        public void initData() {
                mAdapter = new SearchFriendAdapter();
                mAdapter.setOnItemClickListener(new OnSimpleItemChildClickListener() {
                        @Override
                        public void onItemChildClick(int position, View view, int id) {
                                Intent intent = new Intent(SearchFriendActivity.this, UserInfoActivity.class);
                                User user = mAdapter.getData(position);
                                intent.putExtra("uid", user.getObjectId());
                                intent.putExtra("user", user);
                                startActivity(intent);
                                finish();
                        }
                });
                display.setAdapter(mAdapter);
                initActionBar();
        }

        private void initActionBar() {
                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setAvatar(null);
                toolBarOption.setNeedNavigation(true);
                toolBarOption.setTitle("鏌ユ壘濂藉弸");
                setToolBar(toolBarOption);
        }

        private void searchUsers() {
                showLoadDialog("姝ｅ湪鎼滅储锛岃绋嶅€?......");
                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                        dismissLoadDialog();
                        ToastUtils.showShortToast("璇疯緭鍏ョ敤鎴峰悕杩涜鏌ヨ!");
                        return;
                }
                UserManager.getInstance().queryUsers(input.getText().toString().trim(), new FindListener<User>() {
                                @Override
                                public void onSuccess(List<User> list) {
                                        dismissLoadDialog();
                                        if (list != null && list.size() > 0) {
                                                LogUtil.e("鏌ヨ鐢ㄦ埛鎴愬姛锛氫釜鏁颁负" + list.size());
                                                LogUtil.e(list.size() + "澶у皬");
                                                mAdapter.clearAllData();
                                                mAdapter.addData(list);
//                                                adapter.setData(list);
                                        }
                                }

                                @Override
                                public void onError(int i, String s) {
                                        LogUtil.e("鏌ヨ鍑洪敊!" + s + i);
                                }
                        }
                );
        }

        @Override
        public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.btn_search_friend) {
                        searchUsers();

                }
        }


        public static void start(Activity activity) {
                Intent intent = new Intent(activity, SearchFriendActivity.class);
                activity.startActivity(intent);
        }

        @Override
        public void updateData(Object o) {

        }
}

