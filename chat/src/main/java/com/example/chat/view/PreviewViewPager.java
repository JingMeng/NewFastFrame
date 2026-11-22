package com.example.chat.view;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/30      21:35
 * QQ:             1981367757
 */

public class PreviewViewPager extends ViewPager {
        public PreviewViewPager(Context context) {
                this(context, null);
        }

        public PreviewViewPager(Context context, AttributeSet attrs) {
                super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
                try {
                        return super.onTouchEvent(ev);
                } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                }
                return false;
        }


        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
                try {
                        return super.onInterceptTouchEvent(ev);
                } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                }
                return false;
        }
}

