package com.example.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;

import com.example.chat.base.Constant;
import com.example.chat.bean.ChatMessage;
import com.example.chat.bean.GroupTableMessage;
import com.example.chat.bean.SharedMessage;
import com.example.chat.manager.MessageCacheManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.manager.UserManager;
import com.example.chat.util.LogUtil;
import com.example.commonlibrary.BaseApplication;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/13      17:00
 * QQ:             1981367757
 */

public class PollService extends Service {

        private Handler mHandler;
        private int time;

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
                return null;
        }


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
                if (intent != null) {
                        time = intent.getIntExtra("time", 6);
                } else {
                        time = 5;
                }
                LogUtil.e("time" + time);
                new Thread(new Runnable() {
                        @Override
                        public void run() {
                                LogUtil.e("鍚庡彴瀹氭椂鏈嶅姟run22334566789");
                                Looper.prepare();
                                mHandler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                                dealWork();
                                                sendEmptyMessageDelayed(0, time * 1000);
                                        }
                                };
                                mHandler.sendEmptyMessage(0);
                                Looper.loop();
                                LogUtil.e("12221");
                        }
                }).start();
                LogUtil.e("杩欓噷Thread2");
//                new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//
//
//                        }
//                }).start();
                return super.onStartCommand(intent, START_FLAG_RETRY, startId);
        }

        private void dealWork() {
                LogUtil.e("1dealWork");
                LogUtil.e("鎷夊彇鍗曡亰娑堟伅");
                BmobQuery<ChatMessage> query = new BmobQuery<>();
                if (UserManager.getInstance().getCurrentUser() != null) {
                        query.addWhereEqualTo(Constant.TAG_TO_ID, UserManager.getInstance().getCurrentUserObjectId());
                } else {
                        return;
                }
                query.addWhereEqualTo(Constant.TAG_MESSAGE_SEND_STATUS, Constant.SEND_STATUS_SUCCESS);
                query.addWhereEqualTo(Constant.TAG_MESSAGE_READ_STATUS, Constant.READ_STATUS_UNREAD);
//                鎸夊崌搴忚繘琛屾帓搴?                query.setLimit(50);
                query.order("createdAt");
                query.findObjects(BaseApplication.getInstance(), new FindListener<ChatMessage>() {
                        @Override
                        public void onSuccess(List<ChatMessage> list) {
                                LogUtil.e("1鎷夊彇鍗曡亰娑堟伅鎴愬姛");
                                if (list != null && list.size() > 0) {
                                        Intent intent = new Intent(Constant.NEW_MESSAGE_ACTION);
                                        intent.putExtra("from", "person");
                                        intent.putExtra(Constant.NEW_MESSAGE, (Serializable) list);
                                        sendOrderedBroadcast(intent, null);
                                }
                        }

                        @Override
                        public void onError(int i, String s) {
                                LogUtil.e("鎷夊彇鍗曡亰娑堟伅澶辫触");
                                LogUtil.e("鍦ㄦ湇鍔″櫒涓婃煡璇㈣亰澶╂秷鎭け璐ワ細" + s + i);
                        }
                });
//                                杩欓噷寮€濮嬫娴嬬兢缁撴瀯娑堟伅
                BmobQuery<GroupTableMessage> groupTableQuery = new BmobQuery<>();
//                                绗竴娆℃娴嬩笉闇€瑕佽鏉′欢

                String lastGroupMessageTime = MessageCacheManager.getInstance().getLastGroupMessageTime(Constant.GROUP_TABLE_TIME);
                if (lastGroupMessageTime == null) {
                        return;
                }
                groupTableQuery.addWhereEqualTo("readStatus", Constant.READ_STATUS_UNREAD);
                if (lastGroupMessageTime.equals("0000-00-00 01:00:00")) {
                        LogUtil.e("绗竴娆℃娴嬬兢缁撴瀯娑堟伅锛屼笉璁炬潯浠?);
                } else {
                        LogUtil.e("1涓嶆槸绗竴娆℃娴嬫秷鎭?);
                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                                Date oldDate=sdf1.parse(MessageCacheManager.getInstance().getLastShareMessageTime(Constant.GROUP_TABLE_TIME));
                                long currentTime=oldDate.getTime();
                                LogUtil.e("涔嬪墠鐨勬椂闂?+currentTime+",,,"+sdf1.format(oldDate));
                                currentTime+=1000;
                                Date date=new Date(currentTime);
                                String newTime=sdf1.format(date);
                                LogUtil.e("Long鏃堕棿"+currentTime);
                                LogUtil.e("鐜板湪鐨勬椂闂?+newTime);
                                groupTableQuery.addWhereGreaterThan("updatedAt", new BmobDate(date));
//                                groupTableQuery.addWhereGreaterThan("updatedAt", new BmobDate(sdf1.parse(lastGroupMessageTime)));
                        } catch (ParseException e) {
                                e.printStackTrace();
                                LogUtil.e("瑙ｆ瀽鏃堕棿鍑洪敊");
                        }
                }
                if (UserManager.getInstance().getCurrentUser() != null) {
                        groupTableQuery.addWhereEqualTo("toId", UserManager.getInstance().getCurrentUserObjectId());
                } else {
                        return;
                }
                groupTableQuery.order("updatedAt");
                groupTableQuery.findObjects(BaseApplication.getInstance(), new FindListener<GroupTableMessage>() {
                                @Override
                                public void onSuccess(List<GroupTableMessage> list) {
                                        if (list != null && list.size() > 0) {
                                                String time = list.get(list.size() - 1).getUpdatedAt();
                                                LogUtil.e("鏈€澶х殑缇ょ粨鏋勬洿鏂版椂闂达細" + time);
                                                MessageCacheManager.getInstance().setLastGroupMessageTime(Constant.GROUP_TABLE_TIME, time);
                                                Intent tableIntent = new Intent(Constant.NEW_MESSAGE_ACTION);
                                                tableIntent.putExtra("from", "groupTable");
                                                tableIntent.putExtra(Constant.NEW_MESSAGE, (Serializable) list);
                                                sendOrderedBroadcast(tableIntent, null);
                                        }
                                }

                                @Override
                                public void onError(int i, String s) {
                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃煡璇㈢兢缁撴瀯琛ㄦ秷鎭け璐ワ細" + s + i);
                                }
                        }
                );
                LogUtil.e("杩欓噷寮€濮嬪湪鏈嶅姟鍣ㄤ笂鎷夊彇璇磋娑堟伅1");
//                                鎷夊彇鏉′欢鏄紝涓嶇鍙戦€佺姸鎬佷负鎴愬姛鎴栬€呮槸澶辫触锛堝け璐ユ剰鍛崇潃鏄帹閫佸け璐ョ殑鎯呭喌锛?棣栨鎷夊彇鍏ㄩ儴鏁版嵁锛屼互鍚庢瘡涓€鎷夊彇鏁版嵁鍦ㄤ箣鍓嶆媺鍙栨暟鎹殑鏃堕棿涔嬪悗
                BmobQuery<SharedMessage> shareQuery = new BmobQuery<>();
                if (UserCacheManager.getInstance().getContacts() != null && UserCacheManager.getInstance().getContacts().size() > 0) {
                        List<String> list = new ArrayList<>(UserCacheManager.getInstance().getContacts().keySet());
                        list.add(UserManager.getInstance().getCurrentUserObjectId());
//                        list.add(UserCacheManager.getInstance().getUser().getObjectId());
                        shareQuery.addWhereContainedIn("belongId", list);
                } else {
                        if (UserManager.getInstance().getCurrentUser() == null) {
                                LogUtil.e("鐢ㄦ埛宸查€€鍑虹櫥褰?);
                                return;
                        }
                        shareQuery.addWhereEqualTo("belongId", UserManager.getInstance().getCurrentUser().getObjectId());
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (!MessageCacheManager.getInstance().getLastShareMessageTime(Constant.SHARE_TIME).equals("0000-00-00 01:00:00")) {
                        try {
//                                杩欓噷鏄痓mob鐨勪竴涓猙ug锛岀浉鍚屾椂闂寸珶鐒朵篃鑳芥煡璇㈠緱鍒帮紝鎵€浠ヨ繖閲屽湪鍘熸湁鐨勬椂闂翠笂澶氭坊鍔犱簡涓€绉?                                LogUtil.e("鏃堕棿鏄灏?"+MessageCacheManager.getInstance().getLastShareMessageTime(Constant.SHARE_TIME));
                                Date oldDate=simpleDateFormat.parse(MessageCacheManager.getInstance().getLastShareMessageTime(Constant.SHARE_TIME));
                                long currentTime=oldDate.getTime();
                                LogUtil.e("涔嬪墠鐨勬椂闂?+currentTime+",,,"+simpleDateFormat.format(oldDate));
                                currentTime+=1000;
                                Date date=new Date(currentTime);
                               String newTime=simpleDateFormat.format(date);
                                LogUtil.e("Long鏃堕棿"+currentTime);
                                LogUtil.e("鐜板湪鐨勬椂闂?+newTime);
                                shareQuery.addWhereGreaterThan("createdAt", new BmobDate(date));

                        } catch (ParseException e) {
                                e.printStackTrace();
                                LogUtil.e("鏃堕棿瑙ｆ瀽鍑洪敊"+e.getMessage());
                        }
                } else {
                        LogUtil.e("绗竴娆℃媺鍙栬璇存秷鎭?);
                }
//                                鎷夊彇鏈€鏂扮殑10鏉¤璇存秷鎭?                shareQuery.order("-createdAt");
                shareQuery.setLimit(10);
                shareQuery.findObjects(BaseApplication.getInstance(), new FindListener<SharedMessage>() {
                        @Override
                        public void onSuccess(List<SharedMessage> list) {
                                if (list != null && list.size() > 0) {
                                        String time = list.get(0).getUpdatedAt();
                                        MessageCacheManager.getInstance().setLastShareMessageTime(Constant.SHARE_TIME,time);
                                        LogUtil.e("鏈€澶х殑璇磋娑堟伅鏇存柊鏃堕棿" + time);
//                                        杩欓噷绛涢€夊嚭涓嶅彲瑙佷汉鍒楄〃鏄惁鍖呭惈鏈敤鎴?                                        LogUtil.e("瀹氭椂妫€娴嬪埌鐨勮璇存秷鎭涓?);
                                        List<SharedMessage> result = new ArrayList<>(list);
                                        for (SharedMessage message :
                                                list) {
                                                LogUtil.e(message);
                                                if (message.getBelongId().equals(UserManager.getInstance().getCurrentUserObjectId())) {
                                                        continue;
                                                }
                                                if (message.getVisibleType().equals(Constant.SHARE_MESSAGE_VISIBLE_TYPE_PRIVATE)) {
                                                        result.remove(message);
                                                }else {
                                                        if (message.getInVisibleUserList().contains(UserManager.getInstance().getCurrentUserObjectId())) {
                                                                result.remove(message);
                                                        }
                                                }
                                        }
                                        LogUtil.e("绛涢€夊彲瑙佸悗鐨勮璇村垪琛?);
                                        for (SharedMessage message :
                                                result) {
                                                LogUtil.e(message);
                                        }
                                        if (result.size() > 0) {
                                                Intent intent = new Intent(Constant.NEW_SHARE_MESSAGE_ACTION);
                                                intent.putExtra("from", "share");
                                                intent.putExtra(Constant.NEW_MESSAGE, (Serializable) result);
                                                sendBroadcast(intent);
                                        }
                                } else {
                                        LogUtil.e("鏈嶅姟鍣ㄤ笂闈㈡殏鏃舵病鏈夊ソ鍙嬪彂鐨勮璇存秷鎭?);
                                }
                        }

                        @Override
                        public void onError(int i, String s) {
                                LogUtil.e("瀹氭椂鎷夊彇鏁版嵁澶辫触" + s);
                        }
                });
        }


        @Override
        public void onDestroy() {
                super.onDestroy();

        }
}

