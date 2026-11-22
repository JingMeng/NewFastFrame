package com.example.chat.ui;

import android.app.Activity;
import android.content.Intent;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.adapter.ImagePageAdapter;
import com.example.chat.base.CommonImageLoader;
import com.example.chat.bean.ImageItem;
import com.example.chat.util.LogUtil;
import com.example.chat.view.PreviewViewPager;
import com.example.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/29      15:52
 * QQ:             1981367757
 */

public class BasePreViewActivity extends SlideBaseActivity implements View.OnClickListener, Animation.AnimationListener {

        /**
         * 鐜板湪棰勮鐨勫浘鐗囦綅缃?         */
        protected int currentPosition;


        /**
         * 澶撮儴甯冨眬
         */
        protected RelativeLayout topBar;


        /**
         * 搴曢儴甯冨眬
         */
        protected RelativeLayout bottomView;


        /**
         * 宸查€夋嫨鐨勫浘鐗囦俊鎭?         */
        protected List<ImageItem> selectedList = new ArrayList<>();


        /**
         * 浼犻€掕繃鏉ラ渶瑕侀瑙堢殑鍥剧墖淇℃伅
         */
        protected ArrayList<ImageItem> previewList = new ArrayList<>();


        protected PreviewViewPager display;


        protected ImagePageAdapter mImagePageAdapter;


        protected ImageView back;
        protected Button finish;
        protected ImageView delete;
        protected TextView description;
        private String from;

        protected CheckBox origin;
        protected CheckBox select;


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
                return R.layout.base_pre_view;
        }


        @Override
        public void initView() {
                back = (ImageView) findViewById(R.id.iv_picture_top_bar_back);
                description = (TextView) findViewById(R.id.tv_picture_top_bar_description);
                finish = (Button) findViewById(R.id.btn_picture_top_bar_finish);
                delete = (ImageView) findViewById(R.id.iv_picture_top_bar_delete);
                topBar = (RelativeLayout) findViewById(R.id.picture_top_bar);
                bottomView = (RelativeLayout) findViewById(R.id.rl_base_preview_bottom);
                origin = (CheckBox) findViewById(R.id.cb_base_preview_origin);
                select = (CheckBox) findViewById(R.id.cb_base_preview_select);
                display = (PreviewViewPager) findViewById(R.id.vp_base_preview_display);
                back.setOnClickListener(this);
                delete.setOnClickListener(this);
                select.setOnClickListener(this);
                finish.setOnClickListener(this);
                origin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                        origin.setText(getString(R.string.origin_size, previewList.get(currentPosition).getSize()));
                                } else {
                                        origin.setText(getString(R.string.origin_text));
                                }
                        }
                });
        }


        @Override
        public void initData() {
                LogUtil.e("preViewinitData");
                selectedList = CommonImageLoader.getInstance().getSelectedImages();
                currentPosition = getIntent().getIntExtra(CommonImageLoader.CURRENT_POSITION, 0);
                from = getIntent().getStringExtra(CommonImageLoader.PREVIEW_FROM);
                if (from.equals(CommonImageLoader.PREVIEW_DELETE)) {
                        LogUtil.e("鍙垹闄ょ殑棰勮");
                        finish.setVisibility(View.GONE);
                        delete.setVisibility(View.VISIBLE);
                        bottomView.setVisibility(View.GONE);
                        mImagePageAdapter = new ImagePageAdapter(this, selectedList);
                        description.setText(getString(R.string.preview_image_count, currentPosition + 1, selectedList.size()));
                } else if (from.equals(CommonImageLoader.PREVIEW_SELECT)) {
                        previewList = CommonImageLoader.getInstance().getCurrentImageFolder().getAllImages();
//                        isOrigin = getIntent().getBooleanExtra(CommonImageLoader.IS_ORIGIN, false);
                        LogUtil.e("鍙€夋嫨鐨勯瑙?);
                        finish.setVisibility(View.VISIBLE);
                        delete.setVisibility(View.GONE);
                        bottomView.setVisibility(View.VISIBLE);
                        notifyTextChanged();
                        mImagePageAdapter = new ImagePageAdapter(this, previewList);
                        description.setText(getString(R.string.preview_image_count, currentPosition + 1, previewList.size()));
                } else {
                        finish.setVisibility(View.GONE);
                        delete.setVisibility(View.GONE);
                        bottomView.setVisibility(View.GONE);
                        mImagePageAdapter = new ImagePageAdapter(this, selectedList);
                        description.setText(getString(R.string.preview_image_count, currentPosition + 1, selectedList.size()));
                }
                mImagePageAdapter.setOnPhotoViewClickListener(new ImagePageAdapter.OnPhotoViewClickListener() {
                        @Override
                        public void onPhotoViewClick(View view, int position) {
                                LogUtil.e("鐐瑰嚮鍥剧墖鎷? + position);
                                Animation topAnimation;
                                if (topBar.getVisibility() == View.VISIBLE) {
                                        topAnimation = AnimationUtils.loadAnimation(BasePreViewActivity.this, R.anim.top_out);
                                } else {
                                        topAnimation = AnimationUtils.loadAnimation(BasePreViewActivity.this, R.anim.top_in);
                                }
                                topAnimation.setAnimationListener(BasePreViewActivity.this);
                                topBar.startAnimation(topAnimation);
                                if (from.equals(CommonImageLoader.PREVIEW_SELECT)) {
                                        Animation bottomAnimation;
                                        if (bottomView.getVisibility() == View.VISIBLE) {
                                                bottomAnimation = AnimationUtils.loadAnimation(BasePreViewActivity.this, R.anim.bottom_out);
                                        } else {
                                                bottomAnimation = AnimationUtils.loadAnimation(BasePreViewActivity.this, R.anim.bottom_in);
                                        }
                                        bottomAnimation.setAnimationListener(BasePreViewActivity.this);
                                        bottomView.startAnimation(bottomAnimation);
                                }
                        }
                });
                display.setAdapter(mImagePageAdapter);
                display.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                        @Override
                        public void onPageSelected(int position) {
                                LogUtil.e("婊氬姩");
                                currentPosition = position;
                                if (from.equals(CommonImageLoader.PREVIEW_SELECT)) {
                                        description.setText(getString(R.string.preview_image_count, position + 1, previewList.size()));
                                        if (selectedList.contains(previewList.get(position))) {
                                                select.setChecked(true);
                                        } else {
                                                select.setChecked(false);
                                        }
                                } else {
                                        description.setText(getString(R.string.preview_image_count, position + 1, selectedList.size()));
                                }
                        }
                });
                display.setCurrentItem(currentPosition, false);
        }


        private void notifyTextChanged() {
                LogUtil.e("鏁版嵁鏇存柊");
                LogUtil.e("currentPosition" + currentPosition);
                LogUtil.e("宸查€夋嫨鐨勫ぇ灏? + selectedList.size());
                if (selectedList.contains(previewList.get(currentPosition))) {

                        LogUtil.e("鏁版嵁鏇存柊true");
                        select.setChecked(true);
                } else {
                        select.setChecked(false);
                        LogUtil.e("鏁版嵁鏇存柊false");
                }
                if (selectedList.size() > 0) {
                        finish.setEnabled(true);
                        finish.setText(getString(R.string.finish_count, selectedList.size(), CommonImageLoader.getInstance().getMaxSelectedCount()));
                } else {
                        finish.setText(getString(R.string.finish));
                        finish.setEnabled(false);
                }
        }

        @Override
        public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.iv_picture_top_bar_back) {
                        LogUtil.e("鐐瑰嚮杩斿洖閿?);
                        finish();

                } else if (i == R.id.btn_picture_top_bar_finish) {
                        LogUtil.e("鐐瑰嚮瀹屾垚閿?);
                        finish();

                } else if (i == R.id.iv_picture_top_bar_delete) {
                        LogUtil.e("鐐瑰嚮鍒犻櫎閿?);
                        showBaseDialog("鎻愮ず", "纭畾瑕佸垹闄よ鐓х墖鍚?", "鍙栨秷", "纭畾", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        cancelBaseDialog();
                                }
                        }, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        dismissBaseDialog();
                                        selectedList.remove(currentPosition);
                                        if (selectedList.size() > 0) {
                                                LogUtil.e("閫氱煡鏁版嵁鏀瑰彉");
                                                mImagePageAdapter.notifyDataSetChanged();
                                                display.setCurrentItem(currentPosition, true);
                                                description.setText(getString(R.string.preview_image_count, currentPosition + 1, selectedList.size()));
                                        } else {
                                                onBackPressed();
                                        }
                                }
                        });

                } else if (i == R.id.cb_base_preview_select) {
                        int size = CommonImageLoader.getInstance().getMaxSelectedCount();
                        if (select.isChecked()) {
                                if (selectedList.size() >= size) {
                                        ToastUtils.showShortToast("鏈€澶氬彧鑳芥坊鍔? + size + "鍥剧墖");
                                        select.setChecked(false);
                                } else {
                                        LogUtil.e("selectedList.size()" + selectedList.size());
                                        selectedList.add(previewList.get(currentPosition));
                                        LogUtil.e("CommonImageLoaderSize" + CommonImageLoader.getInstance().getSelectedImages().size());
                                }
                        } else {
                                selectedList.remove(previewList.get(currentPosition));
                        }
                        if (size > 0) {
                                finish.setEnabled(true);
                                finish.setText(getString(R.string.finish_count, CommonImageLoader.getInstance().getSelectedImages().size(), size));
                        } else {
                                finish.setEnabled(false);
                                finish.setText(R.string.finish);
                        }

                }
        }


        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
                LogUtil.e("topBar");
                if (topBar.getVisibility() == View.VISIBLE) {
                        LogUtil.e("topBarVISIBLE");
                        topBar.setVisibility(View.GONE);
                } else {
                        LogUtil.e("topBarGONE");
                        topBar.setVisibility(View.VISIBLE);
                }
                if (from.equals(CommonImageLoader.PREVIEW_SELECT)) {
                        LogUtil.e("bottomView");
                        if (bottomView.getVisibility() == View.VISIBLE) {
                                bottomView.setVisibility(View.GONE);
                                LogUtil.e("bottomViewGONE");
                        } else {
                                bottomView.setVisibility(View.VISIBLE);
                                LogUtil.e("bottomViewVISIBLE");
                        }
                }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        public static void startBasePreview(Activity activity, List<ImageItem> list, int position) {
                CommonImageLoader.getInstance().clearAllData();
                CommonImageLoader.getInstance().setSelectedImages(list);
                Intent intent = new Intent(activity, BasePreViewActivity.class);
                intent.putExtra(CommonImageLoader.PREVIEW_FROM, CommonImageLoader.PREVIEW_BASE);
                intent.putExtra(CommonImageLoader.CURRENT_POSITION, position);
                activity.startActivity(intent);
        }

        @Override
        public void updateData(Object o) {

        }
}

