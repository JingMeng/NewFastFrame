package com.example.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.example.chat.R;
import com.example.chat.base.Constant;
import com.example.chat.bean.GroupChatMessage;
import com.example.chat.bean.GroupTableMessage;
import com.example.chat.bean.RecentMsg;
import com.example.chat.bean.SharedMessage;
import com.example.chat.bean.User;
import com.example.chat.db.ChatDB;
import com.example.chat.events.GroupInfoEvent;
import com.example.chat.listener.OnShareMessageReceivedListener;
import com.example.chat.manager.ChatNotificationManager;
import com.example.chat.manager.MessageCacheManager;
import com.example.chat.manager.MsgManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.manager.UserManager;
import com.example.chat.ui.MainActivity;
import com.example.chat.util.CommonUtils;
import com.example.chat.util.JsonUtil;
import com.example.chat.util.LogUtil;
import com.example.commonlibrary.BaseApplication;
import com.example.commonlibrary.rxbus.RxBusManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.ValueEventListener;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/17      18:47
 * QQ:             1981367757
 */

public class GroupMessageService extends Service {


        private BmobRealTimeData data;
        private List<String> uidList = new ArrayList<>();
        private String table;
        //        private List<String> groupTableList = new ArrayList<>();
        private List<String> groupChatMessageList = new ArrayList<>();


        private NotifyBinder mNotifyBinder = new NotifyBinder();
        private List<String> shareMessageList = new ArrayList<>();
        private static List<OnShareMessageReceivedListener> sListeners = new ArrayList<>();

        public static void registerListener(OnShareMessageReceivedListener onShareMessageReceivedListener) {
                if (!sListeners.contains(onShareMessageReceivedListener)) {
                        sListeners.add(onShareMessageReceivedListener);
                }
        }

        public static void unRegisterListener(OnShareMessageReceivedListener listener) {
                if (sListeners.contains(listener)) {
                        sListeners.remove(listener);
                }
        }


        @Override
        public void onCreate() {
                super.onCreate();
        }

        private void dealListener() {
                LogUtil.e("111111111111鍚姩鎺ュ彈瀹炴椂鐩戝惉鐨勬湇鍔″暒鍟﹀暒");
                table = "_User";
                data = new BmobRealTimeData();
                data.start(BaseApplication.getInstance(), new ValueEventListener() {
                                @Override
                                public void onConnectCompleted() {
                                        LogUtil.e("瀹炴椂鐩戝惉锛氳繛鎺ユ湇鍔″櫒鎴愬姛鍟﹀暒鍟?);
                                        if (data.isConnected()) {
                                                LogUtil.e("杩炴帴鍟﹀暒鍟?);
                                                if (UserCacheManager.getInstance().getContacts() != null && UserCacheManager.getInstance().getContacts().keySet().size() > 0) {
                                                        LogUtil.e("鏈夊ソ鍙嬫暟鎹?);
                                                        uidList.clear();
                                                        uidList.addAll(UserCacheManager.getInstance().getContacts().keySet());
                                                        for (String uid : uidList) {
                                                                if (!uid.equals(UserManager.getInstance().getCurrentUserObjectId())) {
                                                                        data.subRowUpdate(table, uid);
                                                                } else {
                                                                        LogUtil.e("鏈湴鐢ㄦ埛淇℃伅鐨勪慨鏀癸紝涓嶇洃鍚?);
                                                                }
                                                        }
                                                } else {
                                                        LogUtil.e("娌℃湁濂藉弸鏁版嵁");
                                                }
                                                LogUtil.e("杩欓噷寮€濮嬬洃鍚兢鏁版嵁");
                                                if (MessageCacheManager.getInstance().getAllGroupId() != null && MessageCacheManager.getInstance().getAllGroupId().size() > 0) {
                                                        groupChatMessageList.clear();
                                                        groupChatMessageList.addAll(MessageCacheManager.getInstance().getAllGroupId());
                                                        for (String groupId :
                                                                groupChatMessageList) {
                                                                data.subTableUpdate("g" + groupId);
                                                                data.subRowUpdate("GroupTableMessage", groupId);
                                                        }

                                                } else {
                                                        LogUtil.e("娌℃湁缇ゆ暟鎹彲鐩戝惉");
                                                }
                                                if (shareMessageList.size() > 0) {
                                                        for (String id :
                                                                shareMessageList) {
                                                                data.subRowUpdate("SharedMessage", id);
                                                                data.subRowDelete("SharedMessage", id);
                                                        }
                                                }

                                        } else {
                                                LogUtil.e("鏈繛鎺?);
                                        }
                                }

                                @Override
                                public void onDataChange(JSONObject jsonObject) {
                                        LogUtil.e("鐩戝惉鍒版暟鎹暒鍟﹀暒\n");
                                        LogUtil.e("鏁版嵁鏍煎紡濡備笅:\n");
                                        LogUtil.e("json鏁版嵁" + jsonObject.toString());
                                        try {
                                                JSONObject object = jsonObject.getJSONObject("data");
                                                String action = jsonObject.getString("action");
                                                if (action.equals("updateRow")) {
                                                        LogUtil.e("鏇存柊琛岀殑鐩戝惉鍒板暒");
                                                } else if (action.equals("deleteRow")) {
                                                        LogUtil.e("鍒犻櫎琛岀殑鐩戝惉鍒板暒");
//                                                        杩欓噷鍙湁璇磋娑堟伅鐩戝惉琛岀殑鍒犻櫎
                                                        String id = jsonObject.getString("objectId");
                                                        LogUtil.e("鍒犻櫎鐨勮璇碔D涓? + id);
                                                        long result = ChatDB.create().deleteSharedMessage(id);
                                                        LogUtil.e("鍒犻櫎鐨勮鍙蜂负" + result);
                                                        if (result > 0) {
                                                                if (sListeners.size() > 0) {
                                                                        for (OnShareMessageReceivedListener listener :
                                                                                sListeners) {
                                                                                listener.onDeleteShareMessage(id);
                                                                        }
                                                                }
                                                        } else {
                                                                LogUtil.e("鍒犻櫎涓嶆垚鍔?);
                                                        }
                                                        return;
                                                }
                                                if (!JsonUtil.getString(object, "creatorId").equals("")) {
                                                        LogUtil.e("瀹炴椂鐩戝惉鐨勭兢缁撴瀯鏇存柊娑堟伅鍒板暒1");
                                                        final GroupTableMessage groupTableMessage = MsgManager.getInstance().createReceiveGroupTableMsg(object.toString());
//                                                        鍒ゆ柇鏄惁鏄涪鍑虹兢鐨勬秷鎭?                                                        if (!groupTableMessage.getGroupNumber().contains(UserManager.getInstance().getCurrentUserObjectId())) {
//                                                                閫€鍑虹兢鐨勬秷鎭?//                                                                鍦ㄦ湇鍔″櫒涓婂垹闄よ嚜宸辩殑缇ょ粨鏋勬秷鎭?                                                                LogUtil.e("杩欓噷浜嗘病??");
                                                                mNotifyBinder.removeGroup(groupTableMessage.getGroupId());
                                                                LogUtil.e(MessageCacheManager.getInstance().getGroupTableMessage(groupTableMessage.getGroupId()));
                                                                MsgManager.getInstance().deleteGroupTableMessage(MessageCacheManager.getInstance().getGroupTableMessage(groupTableMessage.getGroupId()).getObjectId(), new DeleteListener() {
                                                                        @Override
                                                                        public void onSuccess() {
                                                                                LogUtil.e("鍦ㄦ湇鍔″櫒涓婂垹闄よ嚜宸辩殑缇ょ粨鏋勬秷鎭垚鍔?");
                                                                                MessageCacheManager.getInstance().deleteGroupTableMessage(groupTableMessage.getGroupId());
                                                                                ChatDB.create().deleteRecentMsg(groupTableMessage.getGroupId());
                                                                                ChatDB.create().deleteGroupTableMessage(groupTableMessage.getGroupId());
                                                                                RxBusManager.getInstance().post(new GroupInfoEvent(groupTableMessage.getGroupId(),GroupInfoEvent.TYPE_GROUP_NUMBER));
                                                                        }
                                                                        @Override
                                                                        public void onFailure(int i, String s) {
                                                                                LogUtil.e("鍦ㄦ湇鍔″櫒涓婂垹闄よ嚜宸辩殑缇ょ粨鏋勬秷鎭け璐?);
                                                                        }
                                                                });
                                                                return;
                                                        }
                                                        GroupTableMessage message = MessageCacheManager.getInstance().getGroupTableMessage(groupTableMessage.getGroupId());
                                                        message.setGroupAvatar(groupTableMessage.getGroupAvatar());
                                                        message.setNotification(groupTableMessage.getNotification());
                                                        message.setGroupDescription(groupTableMessage.getGroupDescription());
                                                        message.setGroupNumber(groupTableMessage.getGroupNumber());
                                                        message.setGroupName(groupTableMessage.getGroupName());
//                                                        杩欓噷涔熻鍚屾鏇存柊鑷繁鐨勭兢缁撴瀯娑堟伅,濡傛灉鏄兢涓诲氨涓嶉渶瑕佹洿鏂帮紝闈炵兢涓绘墠闇€瑕?                                                        if (UserManager.getInstance().getCurrentUser() != null && !UserManager.getInstance().getCurrentUser().getObjectId().equals(message.getCreatorId())) {
                                                                LogUtil.e("闈炵兢涓伙紝闇€瑕佹洿鏂?);
                                                                MsgManager.getInstance().updateGroupTableMessage(message);
                                                        }
                                                        ChatDB.create().saveGroupTableMessage(message);
                                                        MessageCacheManager.getInstance().addGroupTableMessage(message);
                                                        RecentMsg recentMsg=ChatDB.create().getRecentMsg(message.getGroupId());
                                                        LogUtil.e("1杩欓噷鏇存敼鏈€杩戠兢娑堟伅");
                                                        if (recentMsg != null) {
                                                                LogUtil.e("杩欓噷姝ｅ紡鏇存敼鏈€杩戠兢娑堟伅");
                                                                recentMsg.setAvatar(message.getGroupAvatar());
                                                                recentMsg.setName(message.getGroupName());
                                                                ChatDB.create().saveRecentMessage(recentMsg);
                                                        }
                                                        Intent intent1 = new Intent(Constant.NEW_MESSAGE_ACTION);
                                                        intent1.putExtra("from", "table");
                                                        intent1.putExtra(Constant.NEW_MESSAGE, message);
                                                        sendOrderedBroadcast(intent1, null);
                                                } else if (!JsonUtil.getString(object, "groupId").equals("")) {
                                                        LogUtil.e("瀹炴椂鐩戝惉鐨勭兢娑堟伅鍒板暒");
                                                        GroupChatMessage groupChatMessage = MsgManager.getInstance().createReceiveGroupChatMsg(object);
                                                        if (groupChatMessage.getBelongId().equals(UserManager.getInstance().getCurrentUserObjectId())) {
//                                                                涓嶆帴鍙楁湰鐢ㄦ埛鍙戣繃鏉ョ殑娑堟伅
                                                                LogUtil.e("瀹炴椂妫€娴嬪埌鏈敤鎴风殑缇ゆ秷鎭紝涓嶆帴鍙?);
                                                                return;
                                                        }
                                                        Intent intent1 = new Intent(Constant.NEW_MESSAGE_ACTION);

                                                        if (ChatDB.create().isExistGroupChatMessage(groupChatMessage.getGroupId(), groupChatMessage.getCreateTime())) {
                                                                LogUtil.e("杩欓噷鏄洿鏂扮兢娑堟伅鐨勬樀绉版垨澶村儚");
                                                                ChatDB.create().saveGroupChatMessage(groupChatMessage);
                                                                intent1.putExtra("isRefresh",true);
                                                        }
                                                        intent1.putExtra("from", "group");
                                                        intent1.putExtra(Constant.NEW_MESSAGE, groupChatMessage);
                                                        sendOrderedBroadcast(intent1, null);
                                                        if (!CommonUtils.isAppOnForeground(getBaseContext())) {
                                                                if (BaseApplication.getAppComponent().getSharedPreferences().getBoolean(groupChatMessage.getGroupId(),true)) {
                                                                        ChatNotificationManager.getInstance(getBaseContext()).notify(Constant.NOTIFICATION_TAG_GROUP_MESSAGE, groupChatMessage.getGroupId(), true, true, getBaseContext(),
                                                                                MessageCacheManager.getInstance().getGroupTableMessage(groupChatMessage.getGroupId()).getGroupName(), R.mipmap.ic_launcher, groupChatMessage.getContent(),
                                                                                MainActivity.class);
                                                                }
                                                        }
                                                } else if (!JsonUtil.getString(object, "username").equals("")) {
                                                        LogUtil.e("瀹炴椂鐩戝惉鐨勭敤鎴锋秷鎭埌鍟?);
                                                        User user = MsgManager.getInstance().createUserFromJsonObject(object);
                                                        MessageCacheManager.getInstance().setUserDataLastUpdateTime(user.getObjectId(), JsonUtil.getString(object, "updatedAt"));
                                                        ChatDB.create().addOrUpdateContacts(user);
                                                        UserCacheManager.getInstance().addContact(user);
                                                } else if (JsonUtil.getInt(object, "visibleType") != 0) {
                                                        LogUtil.e("鐩戝惉鍒板垎浜秷鎭?);
                                                        LogUtil.e("鐩戝惉鍒拌璇存暟鎹洿鏂版搷浣?);
                                                        SharedMessage sharedMessage = MsgManager.getInstance().createSharedMessageFromJson(object);
                                                        if (sharedMessage != null) {
                                                                ChatDB.create().saveSharedMessage(sharedMessage);
                                                                LogUtil.e(sharedMessage);
                                                                if (sListeners.size() > 0) {
                                                                        for (OnShareMessageReceivedListener listener :
                                                                                sListeners) {
                                                                                listener.onAddShareMessage(sharedMessage);
                                                                        }
                                                                }
                                                        }
                                                }
                                        } catch (JSONException e) {
                                                e.printStackTrace();
                                                LogUtil.e("瑙ｆ瀽鍑洪敊");
                                        }
                                }
                        }
                );

        }

        public class NotifyBinder extends Binder {


                public void addGroup(String groupId) {
                        if (data.isConnected()) {
                                if (!groupChatMessageList.contains(groupId)) {
                                        LogUtil.e("杩樻病鏈夌洃鍚繃璇ョ兢娑堟伅锛岃繖閲屽紑濮嬬洃鍚?);
                                        groupChatMessageList.add(groupId);
                                        data.subTableUpdate("g" + groupId);
                                        data.subRowUpdate("GroupTableMessage", groupId);
                                } else {
                                        LogUtil.e("宸茬粡鐩戝惉杩囪缇ゆ秷鎭媺锛屾棤闇€鍐嶆鐩戝惉");
                                }
                        } else {
                                LogUtil.e("缃戠粶寮傚父锛屾暟鎹笌鏈嶅姟鍣ㄤ笂杩炴帴涓嶄笂group");
                        }
                }


                public void addUser(String uid){
                        if (data.isConnected()) {
                                if (!uidList.contains(uid)) {
                                        data.subRowUpdate(table, uid);
                                }else {
                                        LogUtil.e("宸茬粡鐩戝惉璇ュソ鍙?);
                                }
                        }else {
                                LogUtil.e("缃戠粶寮傚父锛屾暟鎹篃鏈嶅姟鍣ㄨ繛鎺ヤ笉涓?);
                        }
                }


                public void removeGroup(String groupId) {
                        if (data.isConnected()) {
                                data.unsubTableUpdate("g" + groupId);
                                data.unsubRowUpdate("GroupTableMessage", groupId);
                                if (groupChatMessageList.contains(groupId)) {
                                        groupChatMessageList.remove(groupId);
                                }
                        }
                }


                public void addShareMessage(String shareMessageId) {
                        LogUtil.e("鍒版湇鍔¤繖閲?");
                        if (data.isConnected()) {
                                data.subRowUpdate("SharedMessage", shareMessageId);
                                data.subRowDelete("SharedMessage", shareMessageId);
                                if (!shareMessageList.contains(shareMessageId)) {
                                        shareMessageList.add(shareMessageId);
                                        LogUtil.e("娣诲姞璇磋鐩戝惉" + shareMessageId);
                                        data.subRowUpdate("SharedMessage", shareMessageId);
                                        data.subRowDelete("SharedMessage", shareMessageId);
                                } else {
                                        LogUtil.e("宸茬粡鐩戝惉杩囪璇磋" + shareMessageId + "鐨勬秷鎭?鏃犻渶鍐嶆鐩戝惉");
                                }
                        } else {
                                LogUtil.e("缃戠粶寮傚父锛屾暟鎹笌鏈嶅姟鍣ㄤ笂杩炴帴涓嶄笂add");
                                //                                杩欓噷鎶奍D缂撳瓨鍒板唴瀛樹腑,鍦ㄧ綉缁滆繛鎺ユ垚鍔熺殑鏃跺€欙紝鍐嶄粠缂撳瓨涓洃鍚?                                if (!shareMessageList.contains(shareMessageId)) {
                                        shareMessageList.add(shareMessageId);
                                } else {
                                        LogUtil.e("宸茬粡鐩戝惉杩囪璇磋" + shareMessageId + "鐨勬秷鎭?鏃犻渶鍐嶆鐩戝惉");
                                }
                        }
                }


                public void removeShareMessage(String shareMessageId) {
                        LogUtil.e("鍒版湇鍔¤繖閲屾媺1");
                        if (data.isConnected()) {
                                LogUtil.e("绉婚櫎璇磋鐩戝惉" + shareMessageId);
                                data.unsubRowUpdate("SharedMessage", shareMessageId);
                                data.unsubRowDelete("SharedMessage", shareMessageId);
                                if (shareMessageList.contains(shareMessageId)) {
                                        shareMessageList.remove(shareMessageId);
                                } else {
                                        LogUtil.e("宸茬粡绉婚櫎杩囪璇磋" + shareMessageId + "鐨勬秷鎭?鏃犻渶鍐嶆绉婚櫎");
                                }
                        } else {
                                LogUtil.e("缃戠粶寮傚父锛屾暟鎹笌鏈嶅姟鍣ㄤ笂杩炴帴涓嶄笂 remove");
                        }
                }

                public void startListener() {
                        dealListener();
                }
        }


        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
                dealListener();
                return mNotifyBinder;
        }

        @Override
        public int onStartCommand(final Intent intent, int flags, int startId) {
                return super.onStartCommand(intent, flags, startId);
        }


        @Override
        public void onDestroy() {
                super.onDestroy();
                LogUtil.e("杩欓噷鏄疄鏃舵娴嬫湇鍔＄殑onDestroy");

                if (data != null) {
                        if (uidList.size() > 0) {
                                LogUtil.e("111111111鍙栨秷瀹炴椂鐩戝惉");
                                for (String uid : uidList) {
                                        data.unsubRowUpdate(table, uid);
                                }
                                uidList.clear();
                        }
                        if (groupChatMessageList.size() > 0) {
                                for (String groupId :
                                        groupChatMessageList) {
                                        data.unsubTableUpdate("g" + groupId);
                                }
                                groupChatMessageList.clear();
                        }
                        if (shareMessageList.size() > 0) {
                                LogUtil.e("鏈夐仐鐣欑殑璇磋娑堟伅鏈彇娑堢洃鍚紝鐜板湪鍙栨秷");
                                for (String id :
                                        shareMessageList) {
                                        data.unsubRowUpdate("SharedMessage", id);
                                        data.unsubRowDelete("SharedMessage", id);
                                }
                                shareMessageList.clear();
                        }
                }
        }
}

