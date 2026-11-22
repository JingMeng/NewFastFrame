package com.example.cootek.newfastframe.ui;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.commonlibrary.baseadapter.adapter.ViewPagerAdapter;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.BaseFragment;
import com.example.cootek.newfastframe.R;
import com.example.cootek.newfastframe.ui.fragment.LocalListFragment;
import com.example.cootek.newfastframe.ui.fragment.SingerListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by COOTEK on 2017/9/2.
 */

public class LocalMusicListActivity extends MusicBaseActivity {
    private TabLayout tabLayout;
    private ViewPager display;

    @Override
    public void updateData(Object o) {

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
        return R.layout.activity_local_music_list;
    }

    @Override
    protected void initView() {
        tabLayout = (TabLayout) findViewById(R.id.tb_activity_local_music_list_tab);
        display = (ViewPager) findViewById(R.id.vp_activity_local_music_list_display);
        tabLayout.setupWithViewPager(display);

    }

    @Override
    protected void initData() {
        ToolBarOption toolBarOption = new ToolBarOption();
        toolBarOption.setTitle("鏈湴闊充箰");
        toolBarOption.setNeedNavigation(true);
        setToolBar(toolBarOption);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        tabLayout.setupWithViewPager(display);
        List<String> titleList = new ArrayList<>();
        titleList.add("鍗曟洸");
        titleList.add("姝屾墜");
        titleList.add("涓撹緫");
        List<BaseFragment> fragmentList = new ArrayList<>();
        fragmentList.add(LocalListFragment.newInstance());
        fragmentList.add(SingerListFragment.newInstance());
        fragmentList.add(LocalListFragment.newInstance());
        viewPagerAdapter.setTitleAndFragments(titleList, fragmentList);
        display.setOffscreenPageLimit(2);
        display.setAdapter(viewPagerAdapter);
        display.setCurrentItem(0);
    }
}

