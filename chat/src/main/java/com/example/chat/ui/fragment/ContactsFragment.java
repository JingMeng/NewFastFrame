package com.example.chat.ui.fragment;

import android.content.Intent;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.AlphabetIndexer;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.adapter.ContactsAdapter;
import com.example.chat.bean.User;
import com.example.chat.db.ChatDB;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.ui.BlackListActivity;
import com.example.chat.ui.GroupListActivity;
import com.example.chat.ui.MainActivity;
import com.example.chat.ui.NearbyPeopleActivity;
import com.example.chat.ui.UserInfoActivity;
import com.example.chat.util.LogUtil;
import com.example.chat.view.MyLetterView;
import com.example.commonlibrary.BaseFragment;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemClickListener;
import com.example.commonlibrary.cusotomview.ListViewDecoration;

/**
 * 椤圭洰鍚嶇О:    HappyChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/9/15      15:25
 * QQ:             1981367757
 */
public class ContactsFragment extends BaseFragment implements MyLetterView.MyLetterChangeListener, View.OnClickListener {
        MyLetterView mMyLetterView;
        ContactsAdapter adapter;
        SectionIndexer mIndexer;
        TextView middle;
        String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private RecyclerView display;
        private LinearLayoutManager mLinearLayoutManager;


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
                return R.layout.fragment_contacts;
        }

        @Override
        public void initView() {
                display = (RecyclerView) findViewById(R.id.rcv_fragment_contacts_display);
                mMyLetterView = (MyLetterView) findViewById(R.id.ml_fragment_contacts_letter);
                middle = (TextView) findViewById(R.id.tv_fragment_contacts_middle);
                LinearLayout groupList = (LinearLayout) findViewById(R.id.ll_fragment_contacts_group);
                LinearLayout blackList = (LinearLayout) findViewById(R.id.ll_fragment_contacts_black);
                LinearLayout nearby = (LinearLayout) findViewById(R.id.ll_fragment_contacts_nearby);
                mMyLetterView.setListener(this);
                groupList.setOnClickListener(this);
                blackList.setOnClickListener(this);
                nearby.setOnClickListener(this);
        }





        @Override
        public void initData() {
                LogUtil.e("杩欓噷璁剧疆display鐨刟dapter121");
                mMyLetterView.setTextView(middle);
                adapter = new ContactsAdapter();
                LogUtil.e("鍒濆鍖栫殑鎵€鏈夊ソ鍙嬩俊鎭?);
                adapter.addData(UserCacheManager.getInstance().getAllContacts());
                if (adapter.getData() != null && adapter.getData().size() > 0) {
                        for (User user :
                                adapter.getData()) {
                                LogUtil.e(user);
                        }
                }
                mIndexer = new AlphabetIndexer(ChatDB.create().getSortedKeyCursor(), 0, alphabet);

                adapter.setSectionIndexer(mIndexer);
                mLinearLayoutManager = new LinearLayoutManager(getActivity());
                display.setLayoutManager(mLinearLayoutManager);
                display.addItemDecoration(new ListViewDecoration(getActivity()));
                display.setItemAnimator(new DefaultItemAnimator());
                adapter.setOnItemClickListener(new OnSimpleItemClickListener() {
                        @Override
                        public void onItemClick(int position, View view) {
                                User user = adapter.getData(position);
                                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                                intent.putExtra("uid", user.getObjectId());
                                startActivity(intent);
                        }
                });
                display.setAdapter(adapter);
        }

        @Override
        protected void updateView() {

        }


        @Override
        public void onResume() {
                super.onResume();
                onHiddenChanged(false);
        }

        @Override
        public void onHiddenChanged(boolean hidden) {
                super.onHiddenChanged(hidden);
                if (!hidden) {
                        ((MainActivity) getActivity()).initActionBar("濂藉弸");
                }
        }

        @Override
        public void onLetterChanged(String s) {
                int index = alphabet.indexOf(s);
                mLinearLayoutManager.scrollToPosition(mIndexer.getPositionForSection(index));
        }


        public void updateContactsData(String belongId) {
                mIndexer = new AlphabetIndexer(ChatDB.create().getSortedKeyCursor(), 0, alphabet);
                adapter.setSectionIndexer(mIndexer);
                User user= UserCacheManager.getInstance().getUser(belongId);
                LogUtil.e("杩欓噷娣诲姞鐨勫ソ鍙嬫槦鏄熷涓?);
                LogUtil.e(user);
                adapter.addData(user);
        }

        @Override
        public void onClick(View view) {
                int i = view.getId();
                if (i == R.id.ll_fragment_contacts_group) {
                        GroupListActivity.start(getActivity());

                } else if (i == R.id.ll_fragment_contacts_black) {
                        BlackListActivity.start(getActivity());

                } else if (i == R.id.ll_fragment_contacts_nearby) {
                        NearbyPeopleActivity.start(getActivity());

                }
        }

        @Override
        public void updateData(Object o) {

        }
}

