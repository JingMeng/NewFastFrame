package com.example.chat.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2017/5/27      10:45
 * QQ:             1981367757
 */

public class ViewPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragmentList;
        private List<String> mTitleList;

        public ViewPageAdapter(List<Fragment> fragmentList, List<String> titleList, FragmentManager fm) {
                super(fm);
                mFragmentList = fragmentList;
                mTitleList = titleList;
        }

        @Override
        public Fragment getItem(int position) {
                return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
                return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
                return mTitleList.get(position);
        }
}

