package com.example.chat.ui;

import android.support.v4.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.example.chat.R;
import com.example.chat.util.CommonUtils;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.utils.ToastUtils;

import static android.view.View.GONE;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/12/27      22:14
 * QQ:             1981367757
 */

public class SearchActivity extends SlideBaseActivity {
        private SearchView searchView;
        private RecyclerView display;


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
                return R.layout.activity_search;
        }


        @Override
        public void initView() {
                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setNeedNavigation(false);
                setToolBar(toolBarOption);
                display = (RecyclerView) findViewById(R.id.rcv_search_display);
                display.setVisibility(GONE);
                findViewById(R.id.ll_search_container).setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                                finish();
                                return true;
                        }
                });
        }

        @Override
        protected void initData() {

        }




        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.search_menu_layout, menu);
                final MenuItem menuItem = menu.findItem(R.id.search_action_item);
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                MenuItemCompat.expandActionView(menuItem);
                        }
                });
                MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                                ToastUtils.showShortToast("钄撳欢");
                                return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                                ToastUtils.showShortToast("鏀剁缉");
                                finish();
                                return true;
                        }
                });
                searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                                CommonUtils.hideSoftInput(SearchActivity.this,searchView);
                                return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                                ToastUtils.showShortToast(newText);
                                return true;
                        }
                });
                return true;
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                return super.onOptionsItemSelected(item);
        }

        @Override
        public void updateData(Object o) {

        }
}

