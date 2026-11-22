package com.example.music;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import androidx.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.example.commonlibrary.BaseApplication;
//import com.tencent.bugly.Bugly;
//import com.tencent.bugly.BuglyStrategy;
//import com.tencent.bugly.beta.Beta;
//import com.tencent.bugly.beta.interfaces.BetaPatchListener;
//import com.tencent.bugly.beta.upgrade.UpgradeStateListener;

import java.util.Locale;


/**
 * Created by COOTEK on 2017/8/30.
 */

public class App extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
//        setStrictMode();
//        // 璁剧疆鏄惁寮€鍚儹鏇存柊鑳藉姏锛岄粯璁や负true
//        Beta.enableHotfix = true;
//        // 璁剧疆鏄惁鑷姩涓嬭浇琛ヤ竵
//        Beta.canAutoDownloadPatch = true;
//        // 璁剧疆鏄惁鎻愮ず鐢ㄦ埛閲嶅惎
//        Beta.canNotifyUserRestart = true;
//        // 璁剧疆鏄惁鑷姩鍚堟垚琛ヤ竵
//        Beta.canAutoPatch = true;
//
//        /**
//         *  鍏ㄩ噺鍗囩骇鐘舵€佸洖璋?//         */
//        Beta.upgradeStateListener = new UpgradeStateListener() {
//            @Override
//            public void onUpgradeFailed(boolean b) {
//
//            }
//
//            @Override
//            public void onUpgradeSuccess(boolean b) {
//
//            }
//
//            @Override
//            public void onUpgradeNoVersion(boolean b) {
//                Toast.makeText(getApplicationContext(), "鏈€鏂扮増鏈?, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onUpgrading(boolean b) {
//                Toast.makeText(getApplicationContext(), "onUpgrading", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onDownloadCompleted(boolean b) {
//
//            }
//        };
//
//        /**
//         * 琛ヤ竵鍥炶皟鎺ュ彛锛屽彲浠ョ洃鍚ˉ涓佹帴鏀躲€佷笅杞姐€佸悎鎴愮殑鍥炶皟
//         */
//        Beta.betaPatchListener = new BetaPatchListener() {
//            @Override
//            public void onPatchReceived(String patchFileUrl) {
//                Toast.makeText(getApplicationContext(), patchFileUrl, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onDownloadReceived(long savedLength, long totalLength) {
//                Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(),
//                        "%s %d%%",
//                        Beta.strNotificationDownloading,
//                        (int) (totalLength == 0 ? 0 : savedLength * 100 / totalLength)), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onDownloadSuccess(String patchFilePath) {
//                Toast.makeText(getApplicationContext(), patchFilePath, Toast.LENGTH_SHORT).show();
////                Beta.applyDownloadedPatch();
//            }
//
//            @Override
//            public void onDownloadFailure(String msg) {
//                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onApplySuccess(String msg) {
//                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onApplyFailure(String msg) {
//                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onPatchRollback() {
//                Toast.makeText(getApplicationContext(), "onPatchRollback", Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        long start = System.currentTimeMillis();
//        // 杩欓噷瀹炵幇SDK鍒濆鍖栵紝appId鏇挎崲鎴愪綘鐨勫湪Bugly骞冲彴鐢宠鐨刟ppId,璋冭瘯鏃跺皢绗笁涓弬鏁拌缃负true
//        Bugly.init(this, "2e5309db50", true);
//        long end = System.currentTimeMillis();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
//        MultiDex.install(base);

        // 瀹夎tinker
//        Beta.installTinker();
    }


    @TargetApi(9)
    protected void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    }
}

