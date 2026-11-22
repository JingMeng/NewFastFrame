package com.example.commonlibrary.net.download;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 涓嬭浇鐘舵€? */
public class DownloadStatus {

    private DownloadStatus() {
        throw new RuntimeException("DownloadStatus cannot be initialized!");
    }

    // 鏈笅杞?    public static final int NORMAL = 0;
    // 绛夊緟涓嬭浇
    public static final int WAIT = 1;
    // 寮€濮嬩笅杞?    public static final int START = 2;
    // 姝ｅ湪涓嬭浇
    public static final int DOWNLOADING = 3;
    // 鍋滄涓嬭浇
    public static final int STOP = 4;
    // 涓嬭浇澶辫触
    public static final int ERROR = 5;
    // 涓嬭浇瀹屾垚
    public static final int COMPLETE = 6;
    // 鍙栨秷涓嬭浇
    public static final int CANCEL = 7;
    // 瀹夎涓?    public static final int INSTALLING = 8;
    // 宸茬粡瀹夎
    public static final int INSTALLED = 9;
    // 鏀惰棌鐘舵€?    public static final int COLLECT = 10;

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @IntDef({NORMAL, WAIT, START, DOWNLOADING, STOP, COMPLETE, CANCEL, ERROR, INSTALLING, INSTALLED, COLLECT})
    public @interface DlStatus {}
}

