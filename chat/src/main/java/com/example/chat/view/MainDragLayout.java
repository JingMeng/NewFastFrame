package com.example.chat.view;

import android.content.Context;
import androidx.core.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.chat.R;
import com.example.chat.listener.OnDragDeltaChangeListener;
import com.example.chat.util.LogUtil;
import com.nineoldandroids.view.ViewHelper;


/**
 * 椤圭洰鍚嶇О:    FilpperLayoutDemo
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/4      12:25
 * QQ:             1981367757
 */

public class MainDragLayout extends ViewGroup {


        private static final boolean SHADOW = true;
        /**
         * 姝ｅ湪鎷栨嫿
         */
        public static final int DRAG_STATE_DRAGING = 1;
        /**
         * 鍏抽棴鐘舵€?         */
        public static final int DRAG_STATE_CLOSE = 0;
        /**
         * 鎵撳紑鐘舵€?         */
        public static final int DRAG_STATE_OPEN = 2;
        /**
         * 宸﹂亶鐨勮彍鍗曞竷灞€
         */
        private ViewGroup menu;
        /**
         * 鍙宠竟鐨勪富鐣岄潰甯冨眬
         */
        private ContentView content;


        private int range;


        private OnDragDeltaChangeListener mListener;
        private ImageView ivShadow;
        private boolean isOnClick = false;

        public void setListener(OnDragDeltaChangeListener listener) {
                mListener = listener;
        }

        /**
         * 鎵嬪娍鐩戝惉鍣?         */
        private GestureDetector mGestureDetector;


        /**
         * 鎷栨嫿瀛恦iew鐨勮緟鍔╃被
         */
        private ViewDragHelper mViewDragHelper;


        private int width;
        private int height;
        private boolean isUseAnimation = true;


        public MainDragLayout(Context context) {
                this(context, null);
        }

        public MainDragLayout(Context context, AttributeSet attrs) {
                this(context, attrs, 0);
        }

        public MainDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                mGestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
                        @Override
                        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                                濡傛灉姘村钩鏂瑰悜鐨勫亸绉婚噺澶т簬绔栫洿鏂瑰悜鐨勫亸绉婚噺锛屽氨娑堝寲璇ヤ簨浠?
                                return Math.abs(distanceX) > Math.abs(distanceY) || super.onScroll(e1, e2, distanceX, distanceY);

                        }
                });
//                姘歌繙瑕佽寰楋紝璇ュ伐鍏峰疄鐜扮殑鎷栨媺鏄熀浜庢帶浠朵綅缃殑鏀瑰彉鏉ュ疄鐜扮殑
                mViewDragHelper = ViewDragHelper.create(this, new DragViewCallBack());
        }


        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && getCurrentState() == DRAG_STATE_OPEN && ev.getX() > range) {
                        isOnClick = true;
                }
                if (ev.getAction() == MotionEvent.ACTION_UP && getCurrentState() == DRAG_STATE_OPEN && ev.getX() > range) {
                        if (isOnClick) {
                                isOnClick = false;
                                closeMenu();
                                return true;
                        }
                }
                return super.dispatchTouchEvent(ev);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
//                濡傛灉绗﹀悎鏉′欢鐩存帴鎴幏浜嬩欢鐢辫嚜宸卞湪onTouchEvent鏂规硶涓鐞?//                濡傛灉鍦ㄩ噴鏀捐嚜鍔ㄦ粦鍔ㄧ殑杩囩▼涓偣鍑伙紝浼氬鑷村仠姝㈠姩鐢?                boolean result = mViewDragHelper.shouldInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
//                杩欓噷鏄负浜嗛槻姝㈠湪婊戝姩鐨勮繃绋嬩腑,绐佺劧鐐瑰嚮,瀵艰嚧琚偣鍑荤殑椤甸潰鍋滄婊戝姩鐨勭殑鎯呭喌鍙戠敓
                if (!result && mViewDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING && ev.getAction() == MotionEvent.ACTION_UP) {
                        if (getChildAt(2).getLeft() < range / 2) {
                                closeMenu();
                        } else {
                                openMenu();
                        }
                }
                return result;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

//                鍦ㄨ繖閲岀洿鎺ヤ氦缁檝iewDragHelper澶勭悊
                mViewDragHelper.processTouchEvent(event);
                return false;
        }

        @Override
        protected void onFinishInflate() {
                super.onFinishInflate();
                menu = (ViewGroup) getChildAt(0);
                View view = getChildAt(getChildCount() - 1);
                if (view instanceof ContentView) {
                        content = (ContentView) view;
                        LogUtil.e("range" + range);
                        LogUtil.e("range1" + getWidth() * 0.8);
                        content.setRange((int) (getWidth() * 0.8));
                }
                if (SHADOW) {
                        ivShadow = new ImageView(getContext());
                        LogUtil.e("杩欓噷鎵ц浜嗗悧");
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        ivShadow.setImageResource(R.drawable.shadow);
                        addView(ivShadow, 1, layoutParams);
                }
                menu.setClickable(true);
                content.setClickable(true);
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                width = w;
                height = h;
                range = (int) (width * 0.8f);
                if (content != null) {
                        content.setRange(range);
                }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                for (int i = 0; i < getChildCount(); i++) {
                        getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
                }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int size = getChildCount();
                menu.layout(0, 0, width, height);
                int left = getChildAt(2).getLeft();
                for (int i = 1; i < size; i++) {
                        getChildAt(i).layout(left, 0, left + width, height);
                }
        }

        private class DragViewCallBack extends ViewDragHelper.Callback {
                /**
                 * 鏄惁鎹曡幏瀛恦iew
                 *
                 * @param child     瀛恦iew
                 * @param pointerId 琚嫋鎷芥垨鐪熸鍋氬姩鐢荤殑瀛恑ewID
                 * @return 缁撴灉
                 */
                @Override
                public boolean tryCaptureView(View child, int pointerId) {
                        return true;
                }

                /**
                 * 杩欓噷杩涜杈圭晫妫€鏌?榛樿鏄窛绂荤埗甯冨眬宸﹁竟涓?鐨勶紝鍗充笉鍏佽鎷栨嫿
                 *
                 * @param child 瀛恦iew
                 * @param left  鏈€鏂扮殑瀛恦iew鎺т欢鐨勫乏杈硅窛绂荤埗甯冨眬鐨勬帶浠剁殑宸﹁竟
                 * @param dx    鍋忕Щ閲?                 * @return 閫傚悎鐨勮窛绂荤埗甯冨眬宸﹁竟鐨勮窛绂?                 */
                @Override
                public int clampViewPositionHorizontal(View child, int left, int dx) {
                        int result;
                        if (child.equals(content)) {
                                if (left < 0) {
                                        result = 0;
                                } else if (left > range) {
                                        result = range;
                                } else {
                                        result = left;
                                }
                        } else {
                                if (getChildAt(2).getLeft() + dx > range) {
                                        result = range;
                                } else if (getChildAt(2).getLeft() + dx < 0) {
                                        result = 0;
                                } else {
                                        result = getChildAt(2).getLeft() + dx;
                                }
                        }
                        return result;
                }

                @Override
                public int clampViewPositionVertical(View child, int top, int dy) {
                        return super.clampViewPositionVertical(child, top, dy);

                }

                /**
                 * 璁剧疆姘村钩绉诲姩鐨勮寖鍥?                 *
                 * @param child 瀛恦iew
                 * @return 绉诲姩鐨勬渶澶ц窛绂?                 */
                @Override
                public int getViewHorizontalDragRange(View child) {
                        return width;
                }

                /**
                 * 琚嫋鎷界殑瀛恦iew閲婃斁鐨勭灛闂村洖璋?                 *
                 * @param releasedChild 琚嫋鎷界殑瀛恦iew
                 * @param xvel          x杞翠笂鐨勯€熷害
                 * @param yvel          y杞翠笂鐨勯€熷害
                 */
                @Override
                public void onViewReleased(View releasedChild, float xvel, float yvel) {
                        LogUtil.e("onViewReleased");
                        if (xvel > 0) {
//                                鍚戝彸婊戝姩鐨勮秼鍔?                                openMenu();
                        } else if (xvel < 0) {
//                                鍚戝乏婊戝姩鐨勮秼鍔?                                closeMenu();
                        } else {
                                if (getChildAt(2).getLeft() < range / 2) {
                                        closeMenu();
                                } else {
                                        openMenu();
                                }
                        }
                }


                /**
                 * 瀛恦iew鎷栨嫿杩囩▼涓綅缃敼鍙樼殑鍥炶皟
                 *
                 * @param changedView 琚嫋鎷界殑瀛恦iew
                 * @param left        璺濈鐖跺竷灞€宸﹁竟鐨勮窛绂?                 * @param top         璺濈鐖跺竷灞€涓婇潰鐨勮窛绂?                 * @param dx          x鏂瑰悜鐨勫亸绉婚噺
                 * @param dy          y鏂瑰悜鐨勫亸绉婚噺
                 */
                @Override
                public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                        if (dx == 0) {
                                return;
                        }
                        if (changedView.equals(menu)) {
//                                濡傛灉鎷栨嫿鐨勬槸menu锛屽竷灞€涓嶅彉
                                menu.layout(0, 0, width, height);
//                                content.layout(marginLeft, 0, marginLeft + width, height);
//                                杩欓噷濡傛灉鍦ㄥ睆骞曚笂鎵嬫寚鎸夊畾锛屽氨浼氬嚭鐜癲x=0,dy=1鐨勬儏鍐?                                content.layout(left, 0, left + width, height);
                        }
//                                濡傛灉鏄唴瀹瑰竷灞€锛屽氨闅忔嫋鎷戒綅绉昏€屽彉鍖?鍥犱负鍦╯moothSlideViewTo涓細杩涜鎺т欢杈圭紭鍜屽唴瀹硅竟缂樹箣闂寸殑绉诲姩,鎵€浠ヨ繖閲宑ontent瑕侀殢鏃舵敼鍙樺竷灞€
//                                content.layout(marginLeft, 0, marginLeft + width, height);
//                        0~~1涔嬮棿
                        float percent = left / (float) range;
                        float desWidth = (float) (width / 4.0 * (percent - 1));
//                        杩涜鍐呭绉诲姩锛屾湰韬帶浠朵綅缃笉鍙?                        ViewHelper.setTranslationX(menu, desWidth);
                        if (SHADOW) {
//                                ivShadow.layout(marginLeft, 0, marginLeft + width, height);
                                ivShadow.layout(left, 0, left + width, height);
//                                ivShadow.setAlpha(percent * 225);
//                                ViewHelper.setAlpha(ivShadow, percent * 255);
//                                闃村奖鏁堟灉
                                float f1 = 1 - percent * 0.5f;
                                ViewHelper.setScaleX(ivShadow, f1 * 1.2f * (1 - percent * 0.10f));
                                ViewHelper.setScaleY(ivShadow, f1 * 1.85f * (1 - percent * 0.10f));
                        }
                        int dragState = getCurrentState();
                        if (dragState == DRAG_STATE_OPEN) {
                                if (mListener != null) {
                                        mListener.onOpenMenu();
                                }
                        } else if (dragState == DRAG_STATE_CLOSE) {
                                if (mListener != null) {
                                        mListener.onCloseMenu();
                                }
                        } else {
                                if (mListener != null) {
                                        mListener.onDrag(changedView, percent);
                                }
                        }
                }
        }

        /**
         * 鏍规嵁鍋忕Щ閲忔潵鍒ゆ柇鐩墠鐨勭姸鎬?         *
         * @return 鐘舵€?         */
        public int getCurrentState() {
                int marginLeft = getChildAt(2).getLeft();
                if (marginLeft == 0) {
                        return DRAG_STATE_CLOSE;
                } else if (marginLeft == range) {
                        return DRAG_STATE_OPEN;
                } else {
                        return DRAG_STATE_DRAGING;
                }
        }

        /**
         * 鍏抽棴鑿滃崟
         */
        public void closeMenu() {
                if (isUseAnimation) {
//                        濡傛灉content鍐呭鐣岄潰鐨勬帶浠剁殑宸﹀亸绉婚噺鍜屽姩鐢绘渶缁堢殑浣嶇疆涓嶄竴鑷村氨return true(涔熷氨鏄彲浠ユ粦鍔ㄧ殑鎯呭喌涓?,鍐呴儴鏄噰鐢⊿croller杩涜璁＄畻鐨勶紝鎵€浠ラ渶瑕侀噸缁?                        if (mViewDragHelper.smoothSlideViewTo(content, 0, 0)) {
//                                鍏煎鐗堟湰,閲嶇粯浼氳皟鐢╟omputeScroll()
                                ViewCompat.postInvalidateOnAnimation(this);
                        }
                } else {
//                        濡傛灉娌℃湁鍔ㄧ敾灏辩洿鎺ヨ甯冨眬鍒扮粓鐐?                        content.layout(0, 0, width, height);
                        if (mListener != null) {
                                mListener.onCloseMenu();
                        }
                }
        }

        /**
         * 鎵撳紑鑿滃崟
         */
        public void openMenu() {
                if (isUseAnimation) {
                        if (mViewDragHelper.smoothSlideViewTo(content, range, 0)) {
                                ViewCompat.postInvalidateOnAnimation(this);
                        }
                } else {
//                        娌℃湁浣跨敤鍔ㄧ敾
                        content.layout(range, 0, range + width, height);
                        if (mListener != null) {
                                mListener.onOpenMenu();
                        }
                }
        }


        @Override
        public void computeScroll() {
                super.computeScroll();
//                璋冪敤璇ユ柟娉曪紝鍐呴儴杩涜view鐨勫姩鐢绘晥鏋滅殑閲嶇粯,return true 琛ㄦ槑杩樺湪杩涜涓紝false琛ㄦ槑宸茬粡瀹屾垚浜?杩欓噷杩涜鎺т欢浣嶇疆鐨勭Щ鍔?                if (mViewDragHelper.continueSettling(true)) {
//                        涓嶆柇璋冪敤computeScroll
                        ViewCompat.postInvalidateOnAnimation(this);
                }
        }
}

