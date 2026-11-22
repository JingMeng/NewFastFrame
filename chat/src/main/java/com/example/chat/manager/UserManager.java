package com.example.chat.manager;

/**
 * 椤圭洰鍚嶇О:    HappyChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/9/11      17:31
 * QQ:             1981367757
 */


import androidx.annotation.NonNull;

import com.example.chat.base.Constant;
import com.example.chat.bean.CustomInstallation;
import com.example.chat.bean.User;
import com.example.chat.db.ChatDB;
import com.example.chat.listener.AddBlackCallBackListener;
import com.example.chat.listener.AddFriendCallBackListener;
import com.example.chat.listener.CancelBlackCallBlackListener;
import com.example.chat.util.ChatUtil;
import com.example.chat.util.CommonUtils;
import com.example.chat.util.LogUtil;
import com.example.commonlibrary.BaseApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 鐢ㄦ埛绠＄悊绫? * 涓庣敤鎴锋搷浣滄湁鍏崇殑鎿嶄綔绫伙細鐧诲綍銆侀€€鍑恒€佽幏鍙栧ソ鍙嬪垪琛ㄣ€佽幏鍙栧綋鍓嶇殑鐧诲綍鐢ㄦ埛,鍒犻櫎濂藉弸锛屾坊鍔犲ソ鍙嬬瓑
 */
public class UserManager {
        private static final String COLUMN_NAME_CONTACTS = "contacts";
        private static final String COLUMN_NAME_BLACKLIST = "black";
        //        鐢ㄤ簬鍚屾
        private static final Object INSTANCE_LOCK = new Object();
        private static UserManager INSTANCE;

        public static UserManager getInstance() {
                if (INSTANCE == null) {
                        synchronized (INSTANCE_LOCK) {
                                if (INSTANCE == null) {
                                        INSTANCE = new UserManager();
                                }
                        }
                }
                return INSTANCE;
        }


        /**
         * 濡傛灉娌℃湁鑷畾涔夌敤鎴峰疄浣撶被extend BmobUser
         * 榛樿杩斿洖BmobUser绫诲瀷鐨勭敤鎴峰疄浣?         * 鑾峰彇褰撳墠鐢ㄦ埛
         *
         * @return 褰撳墠鐢ㄦ埛
         */
        public User getCurrentUser() {
                return BmobUser.getCurrentUser(BaseApplication.getInstance(), User.class);
        }


        /**
         * 鑾峰彇褰撳墠鐨勫ソ鍙嬪垪琛ㄥ苟鍥炶皟鍦╟allback
         *
         * @param callback 鍥炶皟
         */
        public void queryAndSaveCurrentContactsList(final FindListener<User> callback) {
                BmobQuery<User> query = new BmobQuery<>();
                query.order("-updatedAt");
                query.setLimit(Constant.LIMIT_CONTACTS);
                query.addWhereRelatedTo(COLUMN_NAME_CONTACTS, new BmobPointer(getCurrentUser()));
                query.findObjects(BaseApplication.getInstance(), new FindListener<User>() {
                                @Override
                                public void onSuccess(final List<User> friend) {
                                        if (friend != null && friend.size() > 0) {
                                                if (!ChatDB.create().addOrUpdateContacts(friend)) {
                                                        LogUtil.e("鍦ㄦ暟鎹簱涓洿鏂板ソ鍙嬪け璐?);
                                                        callback.onError(0, "鍦ㄦ暟鎹簱涓洿鏂板ソ鍙嬪け璐?);
                                                        return;
                                                }
                                                queryBlackList(new FindListener<User>() {
                                                                       @Override
                                                                       public void onSuccess(List<User> list) {
                                                                               if (list != null && list.size() > 0) {
                                                                                       if (ChatDB.create().updateFriendsBlackStatus(list)) {
                                                                                               LogUtil.e("鎵归噺鏇存柊榛戝悕鍗曟垚鍔?);
                                                                                               List<User> friends = ChatDB.create().getContactsWithoutBlack();
                                                                                               callback.onSuccess(friends);
                                                                                       } else {
                                                                                               LogUtil.e("鎵归噺鏇存柊榛戝悕鍗曞け璐?);
                                                                                               callback.onError(0, "鍦ㄦ暟鎹簱涓壒閲忔洿鏂伴粦鍚嶅崟澶辫触");
                                                                                       }
                                                                               } else {
                                                                                       callback.onSuccess(friend);
                                                                               }
                                                                       }

                                                                       @Override
                                                                       public void onError(int i, String s) {
                                                                               LogUtil.e("鍦ㄦ湇鍔″櫒涓婃煡璇㈤粦鍚嶅崟澶辫触" + s + i);
                                                                               callback.onError(0, "鍦ㄦ湇鍔″櫒涓婃煡璇㈤粦鍚嶅崟澶辫触");

                                                                       }
                                                               }
                                                );
                                        } else {
                                                callback.onSuccess(null);
                                        }
                                }

                                @Override
                                public void onError(int i, String s) {
                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃煡璇㈠ソ鍙嬪け璐? + s + i);
                                        callback.onError(i, s);
                                }
                        }

                );
        }

        /**
         * 鏌ヨ榛戝悕鍗曠敤鎴?         *
         * @param callback 鍥炶皟
         */
        private void queryBlackList(final FindListener<User> callback) {
                BmobQuery<User> query = new BmobQuery<>();
                query.order("updateAt");
                query.addWhereRelatedTo(COLUMN_NAME_BLACKLIST, new BmobPointer(getCurrentUser()));
                query.findObjects(BaseApplication.getInstance(), new FindListener<User>() {
                                @Override
                                public void onSuccess(List<User> list) {
                                        callback.onSuccess(list);
                                }

                                @Override
                                public void onError(int i, String s) {
                                        callback.onError(i, s);
                                }
                        }
                );
        }

        /**
         * 鑾峰彇鏈敤鎴稩D
         *
         * @return 鐢ㄦ埛ID
         */
        public String getCurrentUserObjectId() {
                return getCurrentUser().getObjectId();
        }


        /**
         * 鏍规嵁鐢ㄦ埛鍚嶆煡璇㈢敤鎴?         *
         * @param name     鏍规嵁鐢ㄦ埛鍚嶅湪鏈嶅姟鍣ㄤ笂鏌ヨ鐢ㄦ埛
         * @param listener 鍥炶皟
         */
        public void queryUsers(String name, FindListener<User> listener) {
                BmobQuery<User> query = new BmobQuery<>();
                query.addWhereEqualTo("username", name);
                query.order("createdAt");
                query.findObjects(BaseApplication.getInstance(), listener);
        }


        /**
         * 涓嬬嚎鎿嶄綔
         */
        public void logout() {
                LogUtil.e("涓嬬嚎鍟﹀暒鍟?12233");
                UserCacheManager.getInstance().setLogin(false);
                MessageCacheManager.getInstance().setLogin(false);
                UserCacheManager.getInstance().clear();
                MessageCacheManager.getInstance().clear();
                User.logOut(BaseApplication.getInstance());
                BaseApplication.getAppComponent().getSharedPreferences()
                .edit().putBoolean(ChatUtil.LOGIN_STATUS,false);
        }

        /**
         * 娣诲姞鐢ㄦ埛涓哄ソ鍙?1銆佸湪鏈嶅姟鍣ㄤ笂娣诲姞鍏宠仈锛屾坊鍔犲鏁版嵁搴?
         *
         * @param targetId                  瀵规柟ID
         * @param currentId                 鏈敤鎴稩D
         * @param addFriendCallBackListener 鍥炶皟
         */
        public void addNewFriend(final String targetId, final String currentId, @NonNull final AddFriendCallBackListener addFriendCallBackListener) {
                findUserById(targetId, new FindListener<User>() {
                                @Override
                                public void onSuccess(final List<User> list) {
                                        if (list != null && list.size() > 0) {
                                                LogUtil.e("鍦ㄦ湇鍔″櫒涓婃煡璇㈠ソ鍙嬫垚鍔?);
                                                saveNewFriendToServer(list.get(0), currentId, new UpdateListener() {
                                                                @Override
                                                                public void onSuccess() {
                                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婂叧鑱斿ソ鍙嬫垚鍔?);
                                                                        if (ChatDB.create(currentId).addOrUpdateContacts(list.get(0)) > 0) {
//                                                                        淇濆瓨濂藉ソ鍙嬪悗鍙戦€佸悓鎰忕被鍨嬬殑娑堟伅
                                                                                addFriendCallBackListener.onSuccess(list.get(0));
                                                                        } else {
                                                                                addFriendCallBackListener.onFailed(new BmobException("鏁版嵁搴撲繚瀛樺ソ鍙嬪け璐?));
                                                                        }
                                                                }

                                                                @Override
                                                                public void onFailure(int i, String s) {
                                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婂叧鑱斿ソ鍙嬪け璐?);
                                                                        addFriendCallBackListener.onFailed(new BmobException(s));

                                                                }
                                                        }
                                                );
                                        } else {
                                                addFriendCallBackListener.onFailed(new BmobException("鍦ㄦ湇鍔″櫒涓婄伃鏈夋煡鍒拌鐢ㄦ埛"));
                                        }
                                }

                                @Override
                                public void onError(int i, String s) {
                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃煡璇㈢敤鎴峰け璐? + s + i);
                                        addFriendCallBackListener.onFailed(new BmobException(s));

                                }
                        }
                );
        }

        /**
         * 鍦ㄦ湇鍔″櫒涓婂叧鑱旇濂藉弸
         *
         * @param user           鐢ㄦ埛瀹炰綋
         * @param currentId      鐜板湪鐨勭櫥褰曠敤鎴稩D
         * @param updateListener 璺熸柊鍥炶皟
         */
        private void saveNewFriendToServer(User user, String currentId, UpdateListener updateListener) {
                if (getCurrentUser() != null && currentId.equals(getCurrentUserObjectId())) {
                        User currentUser = new User();
                        currentUser.setObjectId(currentId);
                        BmobRelation relation = new BmobRelation();
                        relation.add(user);
                        currentUser.setContacts(relation);
                        currentUser.update(BaseApplication.getInstance(), updateListener
                        );
                } else {
                        LogUtil.e("toId锛? + currentId);
                        if (getCurrentUser() != null) {
                                LogUtil.e("鐜板湪鐨刄ID锛? + getCurrentUserObjectId());
                        }
//                        涓嶆槸褰撳墠鐢ㄦ埛鐨勬儏鍐典笅
                        LogUtil.e("涓嶆槸褰撳墠鐨勭敤鎴凤紝涓嶅湪鏈嶅姟鍣ㄤ笂鍏宠仈璇ュソ鍙?);
                        updateListener.onFailure(0, "涓嶆槸褰撳墠鐨勭敤鎴凤紝涓嶅湪鏈嶅姟鍣ㄤ笂鍏宠仈璇ュソ鍙?);
                }
        }

        /**
         * 鏍规嵁鐢ㄦ埛ID鑾峰彇鐢ㄦ埛淇℃伅
         *
         * @param uid          鐢ㄦ埛ID
         * @param findListener 鍥炶皟
         */
        private void findUserById(String uid, FindListener<User> findListener) {
                BmobQuery<User> query = new BmobQuery<>();
                query.addWhereEqualTo("objectId", uid);
                query.findObjects(BaseApplication.getInstance(), findListener);
        }

        /**
         * 鏇存柊濂藉弸鐨勯粦鍚嶅崟鐘舵€?         *
         * @param username 濂藉弸鐢ㄦ埛鍚?         * @param isBlack  鏄惁鏄粦鍚嶅崟
         */
        private long updateFriendBlackStatus(String username, boolean isBlack) {
                return ChatDB.create().updateFriendBlackStatus(username, isBlack);
        }

        /**
         * 娣诲姞涓洪粦鍚嶅崟
         *
         * @param user                     鐢ㄦ埛瀹炰綋
         * @param addBlackCallBackListener 鍥炶皟
         */
        public void addToBlack(final User user, final AddBlackCallBackListener addBlackCallBackListener) {
                addBlackRelation(user, new UpdateListener() {
                                @Override
                                public void onSuccess() {
                                        long result1 = updateFriendBlackStatus(user.getUsername(), true);
                                        long result2 = ChatDB.create().deleteRecentMsg(user.getObjectId());
                                        if (result1 > 0 && result2 > 0) {
                                                addBlackCallBackListener.onSuccess(user);
                                        } else {
                                                addBlackCallBackListener.onFailed(new BmobException("鍦ㄦ暟鎹簱鏇存柊鐢ㄦ埛榛戝悕鍗曠姸鎬佸け璐ユ垨鑰呮槸鍒犻櫎鏈€杩戜細璇濆け璐?));
                                        }
//                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婂叧鑱旇鐢ㄦ埛涓洪粦鍚嶅崟澶辫触");
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婂叧鑱旇鐢ㄦ埛涓洪粦鍚嶅崟澶辫触");
                                        addBlackCallBackListener.onFailed(new BmobException(s));
                                }
                        }
                );


        }

        private void addBlackRelation(User user, UpdateListener listener) {
                User currentUser = new User();
                currentUser.setObjectId(getCurrentUserObjectId());
                BmobRelation relation = new BmobRelation();
                relation.add(user);
                currentUser.setBlack(relation);
                currentUser.update(BaseApplication.getInstance(), listener);
        }

        /**
         * 鍙栨秷榛戝悕鍗曞洖璋?         *
         * @param user     鐢ㄦ埛瀹炰綋
         * @param listener 鍥炶皟
         */
        public void cancelBlack(final User user, final CancelBlackCallBlackListener listener) {
                deleteBlackRelation(user, new UpdateListener() {
                                @Override
                                public void onSuccess() {
                                        long result = updateFriendBlackStatus(user.getUsername(), false);
                                        if (result > 0) {
                                                listener.onSuccess(user);
                                        } else {
                                                listener.onFailed(new BmobException("鍦ㄦ暟鎹簱涓洿鏂伴粦鍚嶅崟鐘舵€佸け璐?));
                                        }
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                        listener.onFailed(new BmobException(s));
                                }
                        }
                );
        }

        private void deleteBlackRelation(User user, UpdateListener listener) {
                User currentUser = new User();
                BmobRelation relation = new BmobRelation();
                relation.remove(user);
                currentUser.setBlack(relation);
                currentUser.setObjectId(getCurrentUserObjectId());
                currentUser.update(BaseApplication.getInstance(), listener);
        }

        private void bindInstallation(final UpdateListener listener) {
                BmobQuery<CustomInstallation> query = new BmobQuery<>();
                query.addWhereEqualTo("installationId", CustomInstallation.getInstallationId(BaseApplication.getInstance()));
                query.findObjects(BaseApplication.getInstance(), new FindListener<CustomInstallation>() {
                        @Override
                        public void onSuccess(List<CustomInstallation> list) {
                                if (list != null && list.size() > 0) {
                                        CustomInstallation installation = list.get(0);
                                        installation.setUid(UserManager.getInstance().getCurrentUserObjectId());
                                        installation.update(BaseApplication.getInstance(), new UpdateListener() {
                                                @Override
                                                public void onSuccess() {
                                                        LogUtil.e("缁戝畾璁惧琛ㄤ腑UID鎴愬姛");
                                                        listener.onSuccess();
                                                }

                                                @Override
                                                public void onFailure(int i, String s) {
                                                        LogUtil.e("缁戝畾璁惧琛ㄤ腑UID璁惧澶辫触");
                                                        listener.onFailure(i, s);

                                                }
                                        });
                                }
                        }

                        @Override
                        public void onError(int i, String s) {
                                LogUtil.e("鍦ㄦ湇鍔″櫒涓婃煡璇㈣澶囧け璐? + s + i);
                                listener.onFailure(i, s);
                        }
                });
        }

        /**
         * 妫€鏌ユ湰璁惧琛ㄤ腑鐨剈id   锛屽鏋滄湁锛屽氨鍙戦€佷笅绾块€氱煡锛屾搷浣滄垚鍔熷悗锛屽啀鎶妘id鏇存柊鍒版湰鍦扮殑璁惧琛ㄤ腑
         */
        public void checkInstallation(final UpdateListener listener) {
                BmobQuery<CustomInstallation> query = new BmobQuery<>();
                LogUtil.e("checkInstallation UID锛? + getCurrentUserObjectId());
                query.addWhereEqualTo("uid", getCurrentUserObjectId());
                query.findObjects(BaseApplication.getInstance(), new FindListener<CustomInstallation>() {
                                @Override
                                public void onSuccess(List<CustomInstallation> list) {
                                        if (list != null && list.size() > 0) {
                                                CustomInstallation customInstallation = list.get(0);
                                                if (customInstallation.getInstallationId().equals(CustomInstallation.getInstallationId(BaseApplication.getInstance()))) {
                                                        LogUtil.e("鐢变簬缁戝畾鐨勬槸鏈澶囪〃锛屼笉鍋氭搷浣滄墍浠?);
                                                        listener.onSuccess();
                                                } else {
//                                                        涓嶇鎺ㄩ€佹垚鍔熶笌鍚︼紝閮借鏇存柊璁惧琛ㄧ殑UID
                                                        MsgManager.getInstance().sendOfflineNotificationMsg(customInstallation, new PushListener() {
                                                                        @Override
                                                                        public void onSuccess() {
                                                                                LogUtil.e("鎺ㄩ€佷笅绾块€氱煡娑堟伅鎴愬姛");
                                                                                bindInstallation(listener);
                                                                        }

                                                                        @Override
                                                                        public void onFailure(int i, String s) {
                                                                                LogUtil.e("鎺ㄩ€佷笅绾块€氱煡娑堟伅澶辫触" + s + i);
                                                                                bindInstallation(listener);
                                                                        }
                                                                }
                                                        );
                                                }
                                        } else {
                                                LogUtil.e("鏌ヨ涓嶅埌鏈敤鎴锋墍瀵瑰簲鐨勮澶嘔D,杩欓噷鏂板缓涓€涓澶囪〃");
                                                bindInstallation(listener);
                                        }
                                }

                                @Override
                                public void onError(int i, String s) {
                                        LogUtil.e("鏌ヨ鏈敤鎴峰搴旂殑璁惧琛ㄥ嚭閿? + s + i);
                                        listener.onFailure(i, s);
                                }
                        }
                );
        }

        public void updateUserInfo(final String name, final String content, final UpdateListener listener) {
                 User user=new User();
                user.setObjectId(UserManager.getInstance().getCurrentUserObjectId());
                switch (name) {
                        case "phone":
                                user.setMobilePhoneNumber(content);
                                break;
                        case "email":
                                user.setEmail(content);
                                break;
                        case "nick":
                                user.setNick(content);
                                user.setSortedKey(CommonUtils.getSortedKey(content));
                                break;
                        case "avatar":
                                user.setAvatar(content);
                                break;
                        case "sex":
                                if (content.equals("鐢?)) {
                                        user.setSex(true);
                                } else {
                                        user.setSex(false);
                                }
                                break;
                        case "signature":
                                user.setSignature(content);
                                break;
                        case "birth":
                                user.setBirthDay(content);
                                break;
                        case "address":
                                user.setAddress(content);
                                break;
                        case "location":
                                LogUtil.e("瀹氫綅location" + content);
                                String result[] = content.split("&");
                                user.setLocation(new BmobGeoPoint(Double.parseDouble(result[0]), Double.parseDouble(result[1])));
                                break;
                        case "titleWallPaper":
                                user.setTitleWallPaper(content);
                                break;
                        case "wallPaper":
                                user.setWallPaper(content);
                                break;
                }
                user.update(BaseApplication.getInstance(), new UpdateListener() {
                        @Override
                        public void onSuccess() {
                                User currentUser = UserManager.getInstance().getCurrentUser();
                                switch (name) {
                                        case "phone":
                                                currentUser.setMobilePhoneNumber(content);
                                                break;
                                        case "email":
                                                currentUser.setEmail(content);
                                                break;
                                        case "nick":
                                                currentUser.setNick(content);
                                                currentUser.setSortedKey(CommonUtils.getSortedKey(content));
                                                break;
                                        case "avatar":
                                                currentUser.setAvatar(content);
                                                break;
                                        case "sex":
                                                if (content.equals("鐢?)) {
                                                        currentUser.setSex(true);
                                                } else {
                                                        currentUser.setSex(false);
                                                }
                                                break;
                                        case "signature":
                                                currentUser.setSignature(content);
                                                break;
                                        case "birth":
                                                currentUser.setBirthDay(content);
                                                break;
                                        case "address":
                                                currentUser.setAddress(content);
                                                break;
                                        case "location":
                                                LogUtil.e("瀹氫綅location" + content);
                                                String result[] = content.split("&");
                                                currentUser.setLocation(new BmobGeoPoint(Double.parseDouble(result[0]), Double.parseDouble(result[1])));
                                                break;
                                        case "titleWallPaper":
                                                currentUser.setTitleWallPaper(content);
                                                break;
                                        case "wallPaper":
                                                currentUser.setWallPaper(content);
                                                break;
                                }
                                listener.onSuccess();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                                listener.onFailure(i,s);

                        }
                });
        }

        public void queryNearbyPeople(double longitude, double latitude, boolean isAll,boolean isSex, FindListener<User> findListener) {
                BmobQuery<User> query = new BmobQuery<>();
                if (!isAll) {
                        query.addWhereEqualTo("sex", isSex);
                }
                query.addWhereNear("location", new BmobGeoPoint(longitude, latitude));
                query.addWhereNotEqualTo("objectId", UserManager.getInstance().getCurrentUser().getObjectId());
                query.findObjects(BaseApplication.getInstance(), findListener);
        }


        public void refreshUserInfo() {
                List<String> userList = UserCacheManager.getInstance().getAllUserId();
                if (userList != null && userList.size() > 0) {
                        for (final String uid :
                                userList) {
                                BmobQuery<User> query = new BmobQuery<>();
                                String lastTime = MessageCacheManager.getInstance().getUserDataLastUpdateTime(uid);
//                                        绗竴娆℃柇缃戞煡璇㈢敤鎴锋暟鎹?                                try {
                                        query.addWhereGreaterThan("updatedAt", new BmobDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastTime)));
                                        query.addWhereEqualTo("objectId", uid);
                                } catch (ParseException e) {
                                        e.printStackTrace();
                                        LogUtil.e("瑙ｆ瀽鏃堕棿閿欒");
                                }
                                query.findObjects(BaseApplication.getInstance(), new FindListener<User>() {
                                        @Override
                                        public void onSuccess(List<User> list) {
                                                if (list != null && list.size() > 0) {
                                                        User user = list.get(0);
                                                        MessageCacheManager.getInstance().setUserDataLastUpdateTime(uid, user.getUpdatedAt());
                                                        ChatDB.create().addOrUpdateContacts(user);
                                                        UserCacheManager.getInstance().addContact(user);
                                                }
                                        }

                                        @Override
                                        public void onError(int i, String s) {
                                                LogUtil.e("鏂綉鏈熼棿鍐呮煡璇㈢敤鎴峰け璐? + s + i);
                                        }
                                });
                        }
                }
        }
}

