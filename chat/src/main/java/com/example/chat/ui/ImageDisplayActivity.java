package com.example.chat.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chat.listener.OnDownLoadFileListener;
import com.example.chat.manager.DownLoadManager;
import com.example.chat.util.LogUtil;
import com.example.chat.view.CircleLoadView;

import cn.bmob.v3.exception.BmobException;
import mabeijianxi.camera.MediaRecorderBase;
import mabeijianxi.camera.util.DeviceUtils;
import mabeijianxi.camera.views.SurfaceVideoView;

import static mabeijianxi.camera.util.DeviceUtils.getScreenWidth;


/**
 * 椤圭洰鍚嶇О:    HappyChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/9/20      22:14
 * QQ:             1981367757
 */

/**
 * 鍥剧墖灞曠ず绫? */
public class ImageDisplayActivity extends SlideBaseActivity implements MediaPlayer.OnPreparedListener, SurfaceVideoView.OnPlayStateListener, MediaPlayer.OnErrorListener, View.OnClickListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {
//        private ZoomImageView display;
        private CircleLoadView mCircleLoadView;
        private SurfaceVideoView mSurfaceVideoView;
        private boolean mNeedResume;
//        private ProgressBar load;
        //        private ImageDisplayRecyclerView imageDisplay;
//        private LinearLayoutManager mLinearLayoutManager;




        @Override
        public void initView() {
//                display = (ZoomImageView) findViewById(R.id.iv_image_display);
                mCircleLoadView = (CircleLoadView) findViewById(R.id.clv_image_display_loading);
                mSurfaceVideoView = (SurfaceVideoView) findViewById(R.id.svv_image_display_video);
//                load = (ProgressBar) findViewById(R.id.pb_image_display_load);
//                imageDisplay = (ImageDisplayRecyclerView) findViewById(R.id.image_display_show);
                int screenWidth = getScreenWidth(this);
                mSurfaceVideoView.getLayoutParams().height = (int) (screenWidth / (MediaRecorderBase.SMALL_VIDEO_WIDTH / (MediaRecorderBase.SMALL_VIDEO_HEIGHT * 1.0f)));
//                display.setOnLongClickListener(new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(View v) {
//                                LogUtil.e("闀挎寜鏆傛椂鏃犳搷浣?);
//                                return true;
//                        }
//                });
                mSurfaceVideoView.setOnPreparedListener(this);
                mSurfaceVideoView.setOnPlayStateListener(this);
                mSurfaceVideoView.setOnErrorListener(this);
                mSurfaceVideoView.setOnClickListener(this);
                mSurfaceVideoView.setOnInfoListener(this);
                mSurfaceVideoView.setOnCompletionListener(this);
        }




        @Override
        public void onBackPressed() {
                super.onBackPressed();
                LogUtil.e("onBackPressed");
        }

        @Override
        protected void onDestroy() {
                LogUtil.e("onDestroy11");
                super.onDestroy();
                if (mSurfaceVideoView != null) {
                        mSurfaceVideoView.release();
                        mSurfaceVideoView = null;
                }
//                CommonImageLoader.getInstance().clearAllData();
        }

        @Override
        public void initData() {
//                ViewCompat.setTransitionName(display, getIntent().getStringExtra("name"));
//                String url = getIntent().getStringExtra("url");
                initVideo();
//                if (url.endsWith(".gif")) {
//                        LogUtil.e("鏄痝if鍥剧墖");
//                        load.setVisibility(View.VISIBLE);
//                        Glide.with(this).load(url).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).thumbnail(0.1f).listener(new ImageRequestListener()).into(display);
//                }
//                else if (getIntent().getStringExtra("videoUrl") == null) {
//                        LogUtil.e("1灞曠ず鍥剧墖" + url);
//                        File file = new File(url);
//                        if (file.exists()) {
////                                Picasso.with(this).load(file).into(display);
//                                Glide.with(this).load(file).asBitmap().placeholder(R.drawable.wallpaper1)
//                                        .error(R.drawable.wallpaper1).format(DecodeFormat.PREFER_ARGB_8888)
//                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                        .into(display);
//                        } else {
//                                Glide.with(this).load(url).asBitmap().placeholder(R.drawable.wallpaper1)
//                                        .error(R.drawable.wallpaper1).format(DecodeFormat.PREFER_ARGB_8888)
//                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                        .into(display);
//                        }
//                }
        }

        private void initVideo() {
                String videoUrl = getIntent().getStringExtra("videoUrl");
                String id = getIntent().getStringExtra("id");
                if (videoUrl != null) {
                        mSurfaceVideoView.setVisibility(View.VISIBLE);
                        DownLoadManager.getInstance().download(videoUrl, id, new OnDownLoadFileListener() {
                                @Override
                                public void onStart() {
                                        LogUtil.e("寮€濮嬩笅杞借棰?);
                                        mCircleLoadView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onProgress(int value) {
                                        LogUtil.e("鍊煎ぇ灏忎负" + value);
                                        mCircleLoadView.updateProgress(value);
                                }

                                @Override
                                public void onSuccess(String localPath) {
                                        LogUtil.e("涓嬭浇瑙嗛鎴愬姛,淇濆瓨鐨勮矾寰勪负" + localPath);
                                        mCircleLoadView.setVisibility(View.GONE);
//                                        dismissLoadDialog();
                                        mSurfaceVideoView.setVideoPath(localPath);
                                        if (mSurfaceVideoView != null && mNeedResume) {
                                                mNeedResume = false;
                                                if (mSurfaceVideoView.isRelease())
                                                        mSurfaceVideoView.reOpen();
                                                else
                                                        mSurfaceVideoView.start();
                                        }
                                }

                                @Override
                                public void onFailed(BmobException e) {
                                        mCircleLoadView.setVisibility(View.GONE);
                                        LogUtil.e("涓嬭浇瑙嗛澶辫触" + e.getMessage() + e.getErrorCode());
                                }
                        });
                } else {
                        mSurfaceVideoView.setVisibility(View.GONE);
                        mCircleLoadView.setVisibility(View.GONE);
                }
        }






        @Override
        protected void onResume() {
                super.onResume();
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
                return R.layout.activity_image_display;
        }


        @Override
        protected void onPause() {
                super.onPause();
                if (mSurfaceVideoView != null) {
                        if (mSurfaceVideoView.isPlaying()) {
                                mNeedResume = true;
                                mSurfaceVideoView.pause();
                        }
                }
        }


        @Override
        public void onPrepared(MediaPlayer mp) {
                mSurfaceVideoView.setVolume(SurfaceVideoView.getSystemVolumn(this));
                mSurfaceVideoView.start();
                mCircleLoadView.setVisibility(View.GONE);
        }


        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
                switch (event.getKeyCode()) {// 璺熼殢绯荤粺闊抽噺璧?                        case KeyEvent.KEYCODE_VOLUME_DOWN:
                        case KeyEvent.KEYCODE_VOLUME_UP:
                                mSurfaceVideoView.dispatchKeyEvent(this, event);
                                break;
                }
                return super.dispatchKeyEvent(event);
        }

        @Override
        public void onStateChanged(boolean isPlaying) {
                mCircleLoadView.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
                if (!isFinishing()) {
                        // 鎾斁澶辫触
                        LogUtil.e("鎾斁澶辫触");
                }
                finish();
                return false;
        }

        @Override
        public void onClick(View v) {
                if (mSurfaceVideoView.isPlaying()) {
                        Toast.makeText(this, "1姝ｅ湪鎾斁涓?, Toast.LENGTH_SHORT).show();
                        mSurfaceVideoView.pause();
                } else {
                        if (mSurfaceVideoView.isRelease())
                                mSurfaceVideoView.reOpen();
                        else
                                mSurfaceVideoView.start();
                }

        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                        case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                                // 闊抽鍜岃棰戞暟鎹笉姝ｇ‘
                                break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                                if (!isFinishing())
                                        mSurfaceVideoView.pause();
                                break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                                if (!isFinishing())
                                        mSurfaceVideoView.start();
                                break;
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                                if (DeviceUtils.hasJellyBean()) {
                                        mSurfaceVideoView.setBackground(null);
                                } else {
                                        mSurfaceVideoView.setBackgroundDrawable(null);
                                }
                                break;
                }
                return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
                if (!isFinishing()) {
                        Toast.makeText(this, "鎾斁瀹屾垚", Toast.LENGTH_SHORT).show();
                        mCircleLoadView.setVisibility(View.VISIBLE);
                }
        }

        public static void start(final Activity activity, final View view, final String url) {
                Intent imageIntent = new Intent(activity, ImageDisplayActivity.class);
                imageIntent.putExtra("name", "photo");
                imageIntent.putExtra("url", url);
                LogUtil.e("鍚姩鍥剧墖娴忚鐣岄潰11");
                ActivityCompat.startActivity(activity, imageIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "photo").toBundle());
        }

        @Override
        public void updateData(Object o) {

        }
}

