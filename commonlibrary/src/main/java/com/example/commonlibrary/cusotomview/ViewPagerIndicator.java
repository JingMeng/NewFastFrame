package com.example.commonlibrary.cusotomview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.commonlibrary.R;
import com.example.commonlibrary.utils.DensityUtil;

import java.util.List;

/**
 * 椤圭洰鍚嶇О:    NewFastFrame
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2017/11/5      10:35
 * QQ:             1981367757
 */

public class ViewPagerIndicator extends LinearLayout {
    private Paint mPaint;
    private Path mPath;
    private int mLineWidth;
    private static final float RADIO_LINE_WIDTH = 1F;
    private int mInitTranslationX;
    private int mTranslationX=0;
    private int mVisibleTabCount;
    private static final int COUNT_DEFAULT_TAB = 4;
    private static final int COLOR_TEXT_NORMAL = 0x77FFFFFF;
    private static final int COLOR_TEXT_HIGHLIGHT = 0xFFFFFFFF;
    private ViewPager mViewPager;
    private HorizontalScrollView mScrollView;
    private boolean isClicked = false; //鍒ゆ柇鏄惁鏄敱鐐瑰嚮浜х敓鐨勬粦鍔?    private int scrollLocation; //Tab婊氬姩鐨勪綅缃?    private int lineHeight =3;


    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public ViewPagerIndicator(Context context) {
        this(context, null);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        //鑾峰彇鍙鐨則ab鏁伴噺
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);
        mVisibleTabCount = a.getInt(R.styleable.ViewPagerIndicator_visibleTabCount, COUNT_DEFAULT_TAB);
        if (mVisibleTabCount < 0) {
            mVisibleTabCount = COUNT_DEFAULT_TAB;
        }
        a.recycle();
        //鍒濆鍖栫敾绗?        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#ffffff"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(3));
        setOrientation(HORIZONTAL);
    }

    public interface OnPageChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    public OnPageChangeListener mListener;

    public void setOnPageChangedListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mInitTranslationX + mTranslationX, getHeight()-lineHeight);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 閫氳繃甯冨眬娣诲姞Tab
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int cCount = getChildCount();
        if (cCount == 0) {
            return;
        }

        for (int i = 0; i < cCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            lp.weight = 1;
            lp.width = 0;
        }

        setItemClickEvent();
    }


    private void iniLine() {
        int w = getMeasuredWidth();  //灞忓箷瀹藉害
        mInitTranslationX = w / mVisibleTabCount / 2 - mLineWidth / 2;

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mPath!=null) {
            iniLine();
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    /**
     * 鎸囩ず鍣ㄨ窡闅忔墜鎸囪繘琛屾粴鍔?     *
     * @param position
     * @param offset
     */
    public void scroll(int position, float offset) {
        int tabWidth = getChildAt(0).getMeasuredWidth();
        mTranslationX = (int) (tabWidth * (position + offset));
        invalidate();

        int triangleLocation = mTranslationX + mInitTranslationX;
        int screenWidth = DensityUtil.getScreenWidth(getContext());
        if ((triangleLocation - scrollLocation) > (screenWidth - mInitTranslationX)
                && offset > 0.5 && !isClicked) {
            if (mVisibleTabCount == 1) {
                mScrollView.scrollTo(mTranslationX, 0);
                scrollLocation = mTranslationX;
            } else {
                mScrollView.scrollTo((position - 2) * tabWidth, 0);
                scrollLocation = (position - 2) * tabWidth;
            }
        } else if ((triangleLocation - scrollLocation) < mInitTranslationX && offset > 0
                && offset < 0.5 && !isClicked) {
            if (mVisibleTabCount == 1) {
                mScrollView.scrollTo(mTranslationX, 0);
                scrollLocation = mTranslationX;
            } else {
                mScrollView.scrollTo(position * tabWidth, 0);
                scrollLocation = position * tabWidth;
            }
        }
        //婊戝姩瀹屾垚鏃?灏嗘爣璁颁綅鎭㈠
        if (offset == 0) {
            isClicked = false;
        }
    }

    public void setTabItemTitles(List<String> titles) {
        if (titles != null && titles.size() > 0) {
            removeAllViews();
            requestLayout();
            for (String title : titles) {
                addView(generateTextView(title));
            }
        }
        setItemClickEvent();
    }

    /**
     * 鏍规嵁title鍒涘缓tab
     *
     * @param title
     * @return
     */
    private View generateTextView(String title) {
        TextView tv = new TextView(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.width = 0;
        lp.weight = 1;
        tv.setText(title);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv.setTextColor(COLOR_TEXT_NORMAL);
        tv.setLayoutParams(lp);
        if (mLineWidth ==0) {
            Rect bound=new Rect();
            tv.getPaint().getTextBounds(tv.getText().toString(), 0, tv.getText().length(), bound);
            mLineWidth=bound.width();
            mLineWidth+=20;
            mPath = new Path();
            mPath.moveTo(0, 0);
            mPath.lineTo(mLineWidth, 0);
            mPath.lineTo(mLineWidth, lineHeight);
            mPath.lineTo(0, mLineWidth);
            mPath.close();
        }
        return tv;
    }

    /**
     * 璁剧疆鍙Tab鏁伴噺
     *
     * @param count
     */
    public void setVisibleTabCount(int count) {
        mVisibleTabCount = count;
    }

    /**
     * 璁剧疆鍏宠仈鐨刅iewPager
     *
     * @param viewPager
     * @param position
     */
    public void setViewPager(ViewPager viewPager, int position) {
        mViewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scroll(position, positionOffset);
                if (mListener != null) {
                    mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mListener != null) {
                    mListener.onPageSelected(position);
                }

                highLightTextView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mListener != null) {
                    mListener.onPageScrollStateChanged(state);
                }
            }
        });

        highLightTextView(position);
    }

    /**
     * 璁剧疆澶栧眰HorizontalScrollView
     *
     * @param scrollView
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setScrollView(HorizontalScrollView scrollView) {
        mScrollView = scrollView;
        mScrollView.setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                scrollLocation = i2;
            }
        });
    }

    /**
     * 楂樹寒鏄剧ず閫変腑tab鏂囨湰
     *
     * @param position
     */
    public void highLightTextView(int position) {
        View view;
        for (int i = 0; i < getChildCount(); i++) {
            view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setBackground(null);
                ((TextView) view).setTextColor(COLOR_TEXT_NORMAL);
            }
        }
        view = getChildAt(position);
        if (view instanceof TextView) {
            ((TextView) view).setBackgroundResource(R.drawable.custom_drawable_tv_indicator_bg);
            ((TextView) view).setTextColor(COLOR_TEXT_HIGHLIGHT);
        }
    }

    /**
     * 璁剧疆Tab鐨勭偣鍑讳簨浠?     */
    public void setItemClickEvent() {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isClicked = true;
                    mViewPager.setCurrentItem(j);
                }
            });
        }
    }
}

