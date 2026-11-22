package com.example.cootek.newfastframe.view;

import android.content.Context;
import androidx.core.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.commonlibrary.utils.DensityUtil;
import com.nineoldandroids.view.ViewHelper;


/**
 * 椤圭洰鍚嶇О:    FilpperLayoutDemo
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/4      12:25
 * QQ:             1981367757
 */

public class DragLayout extends ViewGroup {


    /**
     * 姝ｅ湪鎷栨嫿
     */
    public static final int DRAG_STATE_DRAGING = 1;
    /**
     * 鍏抽棴鐘舵€?     */
    public static final int DRAG_STATE_CLOSE = 0;
    /**
     * 鎵撳紑鐘舵€?     */
    public static final int DRAG_STATE_OPEN = 2;
    /**
     * 宸﹂亶鐨勮彍鍗曞竷灞€
     */
    private ViewGroup menu;
    /**
     * 鍙宠竟鐨勪富鐣岄潰甯冨眬
     */
    private ViewGroup content;


    private int range;
    private int menuRange;


    private OnDragDeltaChangeListener mListener;
    private boolean isOnClick = false;
    private boolean intercept = false;

    public void setListener(OnDragDeltaChangeListener listener) {
        mListener = listener;
    }

    /**
     * 鎵嬪娍鐩戝惉鍣?     */
    private GestureDetector mGestureDetector;


    /**
     * 鎷栨嫿瀛恦iew鐨勮緟鍔╃被
     */
    private ViewDragHelper mViewDragHelper;


    private int width;
    private int height;
    private boolean isUseAnimation = true;


    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
        if (ev.getAction() == MotionEvent.ACTION_DOWN && getCurrentState() == DRAG_STATE_OPEN && isShouldIntercept(ev)) {
            isOnClick = true;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP && getCurrentState() == DRAG_STATE_OPEN && isShouldIntercept(ev)) {
            if (isOnClick) {
                isOnClick = false;
                closeMenu();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isShouldIntercept(MotionEvent ev) {
        if (mode == LEFT_MODE) {
            return ev.getX() > range;
        } else {
            return ev.getX() < (width - range);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//                濡傛灉绗﹀悎鏉′欢鐩存帴鎴幏浜嬩欢鐢辫嚜宸卞湪onTouchEvent鏂规硶涓鐞?//                濡傛灉鍦ㄩ噴鏀捐嚜鍔ㄦ粦鍔ㄧ殑杩囩▼涓偣鍑伙紝浼氬鑷村仠姝㈠姩鐢?        if (isIntercept()) {
            return false;
        }
        boolean result = mViewDragHelper.shouldInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
//                杩欓噷鏄负浜嗛槻姝㈠湪婊戝姩鐨勮繃绋嬩腑,绐佺劧鐐瑰嚮,瀵艰嚧琚偣鍑荤殑椤甸潰鍋滄婊戝姩鐨勭殑鎯呭喌鍙戠敓

        if (!result && mViewDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING && ev.getAction() == MotionEvent.ACTION_UP) {
            if (mode == LEFT_MODE) {
                if (content.getLeft() < range / 2) {
                    closeMenu();
                } else {
                    openMenu();
                }
            } else {
                if (content.getRight() < range / 2) {
                    openMenu();
                } else {
                    closeMenu();
                }
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
    }

    private void initView() {
        if (menu != null) {
            return;
        }
        menu = (ViewGroup) getChildAt(0);
        if (mode == RIGHT_MODE) {
            menu.setPadding(DensityUtil.dip2px(getContext(),80), DensityUtil.dip2px(getContext(),20),DensityUtil.dip2px(getContext(),20),DensityUtil.dip2px(getContext(),20));
        }else {
            menu.setPadding(DensityUtil.dip2px(getContext(),20), DensityUtil.dip2px(getContext(),20),DensityUtil.dip2px(getContext(),80),DensityUtil.dip2px(getContext(),20));
        }
        content = (ViewGroup) getChildAt(1);
        menu.setClickable(true);
        content.setClickable(true);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize(w,h);
        initView();

    }






    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initSize(getMeasuredWidth(),getMeasuredHeight());
        initView();
        for (int i = 0; i < getChildCount(); i++) {
            if (i == 0) {
//                MeasureSpec.makeMeasureSpec(range, MeasureSpec.EXACTLY)
                getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
            } else {
                getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    private void initSize(int measuredWidth, int measuredHeight) {
        width = measuredWidth;
        height = measuredHeight;
        range = (int) (width * 0.8f);
        menuRange = width / 4;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        menu.layout(menu.getLeft(), 0, width + menu.getLeft(), height);
        content.layout(content.getLeft(), 0, width + content.getLeft(), height);
    }

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    public boolean isIntercept() {
        return intercept;
    }

    public void switchMenu() {
        if (getCurrentState() == DRAG_STATE_OPEN) {
            closeMenu();
        } else if (getCurrentState() == DRAG_STATE_CLOSE) {
            openMenu();
        }
    }

    public int getDistance() {
        if (width==0) {
            return (int) ((DensityUtil.getScreenWidth(getContext()))*0.2f);
        }else {
            return width-range;
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
         * @param dx    鍋忕Щ閲?         * @return 閫傚悎鐨勮窛绂荤埗甯冨眬宸﹁竟鐨勮窛绂?         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int result;
            if (mode == LEFT_MODE) {
                if (child.equals(content)) {
                    if (left < 0) {
                        result = 0;
                    } else if (left > range) {
                        result = range;
                    } else {
                        result = left;
                    }
                } else {
                    if (content.getLeft() + dx > range) {
                        result = range;
                    } else if (content.getLeft() + dx < 0) {
                        result = 0;
                    } else {
                        result = content.getLeft() + dx;
                    }
                }
            } else {
                if (child.equals(content)) {
                    if (left > 0) {
                        result = 0;
                    } else if (left < -range) {
                        result = -range;
                    } else {
                        result = left;
                    }
                } else {
                    if (content.getLeft() + dx > 0) {
                        result = 0;
                    } else if (content.getLeft() + dx < -range) {
                        result = -range;
                    } else {
                        result = content.getLeft() + dx;
                    }
                }
            }
            return result;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);

        }

        /**
         * 璁剧疆姘村钩绉诲姩鐨勮寖鍥?         *
         * @param child 瀛恦iew
         * @return 绉诲姩鐨勬渶澶ц窛绂?         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return width;
        }

        /**
         * 琚嫋鎷界殑瀛恦iew閲婃斁鐨勭灛闂村洖璋?         *
         * @param releasedChild 琚嫋鎷界殑瀛恦iew
         * @param xvel          x杞翠笂鐨勯€熷害
         * @param yvel          y杞翠笂鐨勯€熷害
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel > 0) {
//                                鍚戝彸婊戝姩鐨勮秼鍔?                if (mode == LEFT_MODE) {
                    openMenu();
                } else {
                    closeMenu();
                }
            } else if (xvel < 0) {
//                                鍚戝乏婊戝姩鐨勮秼鍔?                if (mode == LEFT_MODE) {
                    closeMenu();
                } else {
                    openMenu();
                }
            } else {
                if (mode == LEFT_MODE) {
                    if (content.getLeft() < range / 2) {
                        closeMenu();
                    } else {
                        openMenu();
                    }
                } else {
                    if (content.getRight() < range / 2) {
                        openMenu();
                    } else {
                        closeMenu();
                    }
                }
            }
        }


        /**
         * 瀛恦iew鎷栨嫿杩囩▼涓綅缃敼鍙樼殑鍥炶皟
         *
         * @param changedView 琚嫋鎷界殑瀛恦iew
         * @param left        璺濈鐖跺竷灞€宸﹁竟鐨勮窛绂?         * @param top         璺濈鐖跺竷灞€涓婇潰鐨勮窛绂?         * @param dx          x鏂瑰悜鐨勫亸绉婚噺
         * @param dy          y鏂瑰悜鐨勫亸绉婚噺
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (dx == 0) {
                return;
            }
            float percent = 0;
            if (mode == LEFT_MODE) {
                percent = left / (float) range;
                if (changedView.equals(menu)) {
                    content.layout(left, 0, left + width, height);
                }
                float desWidth = (menuRange * (percent - 1));
                menu.layout((int) desWidth, 0, (int) (desWidth + width), height);
            } else {
                percent = (-left) / (float) range;

                if (changedView.equals(menu)) {
                    content.layout(left, 0, left + width, height);
                }
                float desWidth = (menuRange * (1 - percent));
                menu.layout((int) desWidth, 0, (int) (desWidth + width), height);

            }
//                        0~~1涔嬮棿
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
                ViewHelper.setScaleX(content, (1 - percent * 0.20f));
                ViewHelper.setScaleY(content, (1 - percent * 0.20f));
                ViewHelper.setScaleX(menu, (0.8f + percent * 0.20f));
                ViewHelper.setScaleY(menu, (0.8f + percent * 0.20f));
                if (mListener != null) {
                    mListener.onDrag(changedView, percent);
                }
            }
        }
    }


    public static final int LEFT_MODE = 0;
    public static final int RIGHT_MODE = 1;
    private int mode = LEFT_MODE;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 鏍规嵁鍋忕Щ閲忔潵鍒ゆ柇鐩墠鐨勭姸鎬?     *
     * @return 鐘舵€?     */
    public int getCurrentState() {
        int marginLeft = content.getLeft();
        if (marginLeft == 0) {
            return DRAG_STATE_CLOSE;
        } else if ((marginLeft == range&&mode==LEFT_MODE)||(marginLeft==(-range)&&mode==RIGHT_MODE)) {
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
//                        濡傛灉content鍐呭鐣岄潰鐨勬帶浠剁殑宸﹀亸绉婚噺鍜屽姩鐢绘渶缁堢殑浣嶇疆涓嶄竴鑷村氨return true(涔熷氨鏄彲浠ユ粦鍔ㄧ殑鎯呭喌涓?,鍐呴儴鏄噰鐢⊿croller杩涜璁＄畻鐨勶紝鎵€浠ラ渶瑕侀噸缁?            if (mViewDragHelper.smoothSlideViewTo(content, 0, 0)) {
//                                鍏煎鐗堟湰,閲嶇粯浼氳皟鐢╟omputeScroll()
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
//                        濡傛灉娌℃湁鍔ㄧ敾灏辩洿鎺ヨ甯冨眬鍒扮粓鐐?            content.layout(0, 0, width, height);
            if (mListener != null) {
                mListener.onCloseMenu();
            }
        }
    }

    /**
     * 鎵撳紑鑿滃崟
     */
    public void openMenu() {
        int newRange = range;
        if (mode == RIGHT_MODE) {
            newRange = -range;
        }
        if (isUseAnimation) {
            if (mViewDragHelper.smoothSlideViewTo(content, newRange, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
//                        娌℃湁浣跨敤鍔ㄧ敾
            content.layout(newRange, 0, newRange + width, height);
            if (mListener != null) {
                mListener.onOpenMenu();
            }
        }
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
//                璋冪敤璇ユ柟娉曪紝鍐呴儴杩涜view鐨勫姩鐢绘晥鏋滅殑閲嶇粯,return true 琛ㄦ槑杩樺湪杩涜涓紝false琛ㄦ槑宸茬粡瀹屾垚浜?杩欓噷杩涜鎺т欢浣嶇疆鐨勭Щ鍔?        if (mViewDragHelper.continueSettling(true)) {
//                        涓嶆柇璋冪敤computeScroll
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}

