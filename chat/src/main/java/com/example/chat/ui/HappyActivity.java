package com.example.chat.ui;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chat.R;
import com.example.chat.adapter.ViewPageAdapter;
import com.example.chat.manager.UserManager;
import com.example.chat.ui.fragment.HappyContentFragment;
import com.example.chat.ui.fragment.HappyFragment;
import com.example.chat.ui.fragment.PictureFragment;
import com.example.chat.ui.fragment.WeiXinFragment;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2017/1/6      21:59
 * QQ:             1981367757
 */
public class HappyActivity extends SlideBaseActivity {
        private TabLayout mTabLayout;
        private ViewPager display;

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
                return R.layout.activity_happy;
        }


        @Override
        public void initView() {
                mTabLayout = (TabLayout) findViewById(R.id.tl_activity_happy_tab);
                display = (ViewPager) findViewById(R.id.vp_activity_happy_display);
                Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

        }



        @Override
        public void initData() {
                initActionBar();
                initFragments();
        }

        private void initFragments() {
                mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.base_color_text_blue));
                mTabLayout.setTabTextColors(getResources().getColor(R.color.base_color_text_black), getResources().getColor(R.color.base_color_text_blue));
                mTabLayout.setupWithViewPager(display);
                WeiXinFragment weiXinFragment = new WeiXinFragment();
                HappyFragment happyFragment = new HappyFragment();
                HappyContentFragment happyContentFragment = new HappyContentFragment();
                PictureFragment pictureFragment = new PictureFragment();
                List<Fragment> fragmentList = new ArrayList<>();
                fragmentList.add(weiXinFragment);
                fragmentList.add(happyFragment);
                fragmentList.add(happyContentFragment);
                fragmentList.add(pictureFragment);
                List<String> list = new ArrayList<>();
                list.add("寰俊绮鹃€?);
                list.add("绮鹃€夎叮鍥?);
                list.add("绮鹃€夌瑧璇?);
                list.add("缇庡コ鍥剧墖");
                ViewPageAdapter viewPageAdapter = new ViewPageAdapter(fragmentList, list, getSupportFragmentManager());
                display.setOffscreenPageLimit(1);
                display.setAdapter(viewPageAdapter);
                display.setCurrentItem(0);
        }

        private void initActionBar() {
                ToolBarOption toolBarOption = new ToolBarOption();

                        toolBarOption.setAvatar(UserManager.getInstance().getCurrentUser().getAvatar());

                toolBarOption.setNeedNavigation(true);
                String title = "寮€蹇冩椂鍒?;
                toolBarOption.setTitle(title);
                setToolBar(toolBarOption);
        }

        //
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.happy_main_menu, menu);
                return true;
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.happy_main_add_friend) {
                        SearchFriendActivity.start(this);
                        finish();

                } else if (i == R.id.happy_main_create_group) {
                        ToastUtils.showShortToast("鐐瑰嚮浜嗗垱寤虹兢");
                        Intent selectIntent = new Intent(this, SelectedFriendsActivity.class);
                        selectIntent.putExtra("from", "createGroup");
                        startActivity(selectIntent);

                }
                return true;
        }
        public static void startActivity(Activity activity) {
                Intent intent = new Intent(activity, HappyActivity.class);
                activity.startActivity(intent);
        }

        @Override
        public void updateData(Object o) {

        }
}

