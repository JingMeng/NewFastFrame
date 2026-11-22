package com.example.chat.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.adapter.GroupGridViewAdapter;
import com.example.chat.base.CommonImageLoader;
import com.example.chat.base.Constant;
import com.example.chat.bean.GroupNumberInfo;
import com.example.chat.bean.GroupTableMessage;
import com.example.chat.bean.User;
import com.example.chat.events.GroupInfoEvent;
import com.example.chat.manager.MessageCacheManager;
import com.example.chat.manager.MsgManager;
import com.example.chat.manager.UserManager;
import com.example.chat.util.LogUtil;
import com.example.chat.util.TimeUtil;
import com.example.commonlibrary.BaseApplication;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemChildClickListener;
import com.example.commonlibrary.cusotomview.RoundAngleImageView;
import com.example.commonlibrary.cusotomview.ToolBarOption;
import com.example.commonlibrary.rxbus.RxBusManager;
import com.example.commonlibrary.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/12/31      18:01
 * QQ:             1981367757
 */

public class GroupInfoActivity extends SlideBaseActivity implements View.OnClickListener {
        private RelativeLayout headerLayout;
        private RoundAngleImageView groupAvatar;
        private TextView createTime;
        private LinearLayout numberName;
        private LinearLayout numberCount;
        private RecyclerView numberAvatar;
        private LinearLayout groupNameLayout;
        private LinearLayout groupDescription;
        private LinearLayout groupNotification;
        private LinearLayout groupRemind;
        private TextView numberNick;
        private TextView groupNumberCount;
        private TextView notification;
        private TextView description;
        private TextView remind;
        private TextView groupName;
        private TextView groupName1;
        private Button exit;
        private GroupTableMessage mGroupTableMessage;
        private GroupGridViewAdapter mAdapter;
        private String localImagePath;
        private boolean isCreator;
        private boolean isRemind;

        public static void start(Context context, String groupId, int requestCode) {
                Intent intent = new Intent(context, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                ((Activity) context).startActivityForResult(intent, requestCode);
        }




        @Override
        public void initView() {
                headerLayout = (RelativeLayout) findViewById(R.id.rl_group_info_header);
                groupAvatar = (RoundAngleImageView) findViewById(R.id.riv_group_info_avatar);
                groupName = (TextView) findViewById(R.id.tv_group_info_name);
                createTime = (TextView) findViewById(R.id.tv_group_info_create_time);
                numberName = (LinearLayout) findViewById(R.id.ll_group_info_number_name);
                numberCount = (LinearLayout) findViewById(R.id.ll_group_info_number_count);
                numberAvatar = (RecyclerView) findViewById(R.id.rcv_group_info_number_avatar);
                groupNameLayout = (LinearLayout) findViewById(R.id.ll_group_info_group_name);
                groupDescription = (LinearLayout) findViewById(R.id.ll_group_info_group_description);
                groupNotification = (LinearLayout) findViewById(R.id.ll_group_info_group_notification);
                groupRemind = (LinearLayout) findViewById(R.id.ll_group_info_remind);
                exit = (Button) findViewById(R.id.btn_group_info_exit_group);
                ((TextView) numberName.findViewById(R.id.tv_group_info_item_layout_title)).setText("缇ゅ悕鐗?);
                numberNick = (TextView) numberName.findViewById(R.id.tv_group_info_item_layout_value);
                ((TextView) numberCount.findViewById(R.id.tv_group_info_item_layout_title)).setText("缇ゆ€讳汉鏁?);
                ((TextView) groupNameLayout.findViewById(R.id.tv_group_info_item_layout_title)).setText("缇ゅ悕");
                ((TextView) groupDescription.findViewById(R.id.tv_group_info_item_layout_title)).setText("缇や粙缁?);
                ((TextView) groupNotification.findViewById(R.id.tv_group_info_item_layout_title)).setText("缇ら€氱煡");
                ((TextView) groupRemind.findViewById(R.id.tv_group_info_item_layout_title)).setText("缇ゆ秷鎭彁閱?);
                groupNumberCount = (TextView) numberCount.findViewById(R.id.tv_group_info_item_layout_value);
                notification = (TextView) groupNotification.findViewById(R.id.tv_group_info_item_layout_value);
                description = (TextView) groupDescription.findViewById(R.id.tv_group_info_item_layout_value);
                remind = (TextView) groupRemind.findViewById(R.id.tv_group_info_item_layout_value);
                groupName1 = (TextView) groupNameLayout.findViewById(R.id.tv_group_info_item_layout_value);
                headerLayout.setOnClickListener(this);
                numberName.setOnClickListener(this);
                numberCount.setOnClickListener(this);
                groupNameLayout.setOnClickListener(this);
                groupNotification.setOnClickListener(this);
                groupDescription.setOnClickListener(this);
                groupRemind.setOnClickListener(this);
                exit.setOnClickListener(this);
        }




        @Override
        public void initData() {
                mGroupTableMessage = MessageCacheManager.getInstance().getGroupTableMessage(getIntent().getStringExtra("groupId"));
                ToolBarOption toolBarOption = new ToolBarOption();
                toolBarOption.setAvatar(null);
                toolBarOption.setTitle(mGroupTableMessage.getGroupName());
                toolBarOption.setNeedNavigation(true);
                setToolBar(toolBarOption);
                mAdapter = new GroupGridViewAdapter();
                numberAvatar.setLayoutManager(new GridLayoutManager(this, 5));
                numberAvatar.setItemAnimator(new DefaultItemAnimator());
                mAdapter.setOnItemClickListener(new OnSimpleItemChildClickListener() {
                        @Override
                        public void onItemChildClick(int position, View view, int id) {
                                if (id==R.id.riv_group_info_avatar_item_layout_avatar){
                                        Intent intent = new Intent(GroupInfoActivity.this, GroupNumberInfoDetailActivity.class);
                                        GroupNumberInfo groupNumberInfo = mAdapter.getData(position);
                                        intent.putExtra("isCreator", isCreator);
                                        intent.putExtra("groupId", mGroupTableMessage.getGroupId());
                                        intent.putExtra("groupNumberInfo", groupNumberInfo);
                                        startActivity(intent);
                                }
                        }
                });
                numberAvatar.setAdapter(mAdapter);
                if (UserManager.getInstance().getCurrentUser().getObjectId().equals(mGroupTableMessage.getCreatorId())) {
                        isCreator = true;
                        LogUtil.e("褰撳墠鐢ㄦ埛涓虹兢涓伙紝鍙互淇敼缇ゅご鍍?);
                        headerLayout.setClickable(true);
                        groupNotification.setClickable(true);
                        groupDescription.setClickable(true);
                        groupNameLayout.setClickable(true);
                } else {
                        isCreator = false;
                        groupNameLayout.setClickable(false);
                        groupNotification.setClickable(false);
                        groupDescription.setClickable(false);
                        headerLayout.setClickable(false);
                }
                ToolBarOption mToolBarOption = new ToolBarOption();
                mToolBarOption.setAvatar(null);
                mToolBarOption.setTitle(mGroupTableMessage.getGroupName());
                mToolBarOption.setNeedNavigation(true);
                setToolBar(mToolBarOption);
                isRemind = BaseApplication.getAppComponent()
                        .getSharedPreferences().getBoolean(mGroupTableMessage.getGroupId(),true);
                updateData();
                numberAvatar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                if (MessageCacheManager.getInstance().getAllGroupNumberInfo(mGroupTableMessage.getGroupId()) == null) {
                                        showLoadDialog("姝ｅ湪鍔犺浇缇ゆ垚鍛樻秷鎭紝璇风◢鍊?...........");
                                        MsgManager.getInstance().queryAllGroupNumber(mGroupTableMessage.getGroupId(), new FindListener<User>() {
                                                @Override
                                                public void onSuccess(final List<User> userList) {
                                                        if (userList != null && userList.size() > 0) {
                                                                LogUtil.e("缇ゆ垚鍛樹俊鎭?);
                                                                for (User user :
                                                                        userList) {
                                                                        LogUtil.e(user);
                                                                }
                                                                MsgManager.getInstance().queryAllGroupTableMessage(mGroupTableMessage.getGroupId(), new FindListener<GroupTableMessage>() {
                                                                        @Override
                                                                        public void onSuccess(List<GroupTableMessage> list) {
                                                                                dismissLoadDialog();
                                                                                List<GroupNumberInfo> groupInfoList;
                                                                                if (list != null && list.size() > 0) {
                                                                                        groupInfoList = new ArrayList<>();
                                                                                        GroupNumberInfo groupNumberInfo;
                                                                                        LogUtil.e("鏌ヨ寰楀埌鐨勬垚鍛樼兢缁撴瀯娑堟伅鍒楄〃");
                                                                                        for (GroupTableMessage message :
                                                                                                list) {
                                                                                                LogUtil.e(message);
                                                                                                groupNumberInfo = new GroupNumberInfo();
                                                                                                groupNumberInfo.setGroupNick(message.getGroupNick());
                                                                                                for (User user :
                                                                                                        userList
                                                                                                        ) {
                                                                                                        if (user.getObjectId().equals(message.getToId())) {
                                                                                                                groupNumberInfo.setUser(user);
                                                                                                        }
                                                                                                }
                                                                                                groupInfoList.add(groupNumberInfo);
                                                                                        }
                                                                                        MessageCacheManager.getInstance().setAllGroupNumberInfo(mGroupTableMessage.getGroupId(), groupInfoList);
                                                                                        mAdapter.clearAllData();
                                                                                        mAdapter.addData(MessageCacheManager.getInstance().getAllGroupNumberInfo(mGroupTableMessage.getGroupId()));
                                                                                        mAdapter.notifyDataSetChanged();
                                                                                } else {
                                                                                        LogUtil.e("鍦ㄦ湇鍔″櫒涓婃病鏈夋煡璇㈠埌缇ょ粨鏋勬秷鎭?);
                                                                                }
                                                                        }

                                                                        @Override
                                                                        public void onError(int i, String s) {
                                                                                LogUtil.e("鏌ヨ缇ゆ垚鍛樼殑缇よ祫鏂欏け璐? + s + i);
                                                                                dismissLoadDialog();
                                                                        }
                                                                });
                                                        }
                                                }

                                                @Override
                                                public void onError(int i, String s) {
                                                        dismissLoadDialog();
                                                        LogUtil.e("鏌ヨ缇ゆ垚鍛樹俊鎭け璐? + s + i);
                                                }
                                        });
                                } else {
                                        mAdapter.clearAllData();
                                        mAdapter.addData(MessageCacheManager.getInstance().getAllGroupNumberInfo(mGroupTableMessage.getGroupId()));
                                        mAdapter.notifyDataSetChanged();
                                }
                        }
                },200);
        }

        private void updateData() {
                groupName.setText(mGroupTableMessage.getGroupName());
                groupName1.setText(mGroupTableMessage.getGroupName());
                if (mGroupTableMessage.getGroupNick() == null || mGroupTableMessage.getGroupNick().equals("")) {
                        numberNick.setText("鏈缃?);
                        LogUtil.e("鏈缃?);
                } else {
                        numberNick.setText(mGroupTableMessage.getGroupNick());
                }
                groupNumberCount.setText(mGroupTableMessage.getGroupNumber().size() + "浜?);
                description.setText(mGroupTableMessage.getGroupDescription());
                notification.setText(mGroupTableMessage.getNotification());
//                榛樿寮€鍚姸鎬?                if (isRemind) {
                        remind.setText("寮€鍚?);
                } else {
                        remind.setText("鍏抽棴");
                }
                Glide.with(this).load(mGroupTableMessage.getGroupAvatar()).into(groupAvatar);
                createTime.setText("浜? + TimeUtil.getTime(Long.valueOf(mGroupTableMessage.getCreatedTime())) + "鍒涘缓");
        }


        @Override
        protected void onResume() {
                super.onResume();
        }

        @Override
        protected boolean isNeedHeadLayout() {
                return true;
        }

        @Override
        protected boolean isNeedEmptyLayout() {
                return false;
        }

        @Override
        protected int getContentLayout() {
                return R.layout.activity_group_info;
        }

        @Override
        public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.rl_group_info_header) {
                        List<String> list = new ArrayList<>();
                        list.add("鎷嶆憚");
                        list.add("浠庢墜鏈虹浉鍐岄€夋嫨");
                        showChooseDialog("璁剧疆澶村儚", list, new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        dismissBaseDialog();
                                        if (position == 0) {
//                                                        File imageFile = FileUtil.newFile(FileUtil.newDir(Constant.IMAGE_CACHE_DIR).getAbsolutePath() + System.currentTimeMillis());
                                                localImagePath = CommonImageLoader.getInstance().takePhoto(GroupInfoActivity.this, Constant.REQUEST_CODE_TAKE_PICTURE).getAbsolutePath();
                                        } else {
                                                CommonImageLoader.getInstance().pickPhoto(GroupInfoActivity.this, Constant.REQUEST_CODE_SELECT_FROM_LOCAL);
                                        }
                                }
                        });

                } else if (i == R.id.ll_group_info_number_name) {//                                缂栬緫鏄电О
                        Intent intent = new Intent(this, EditUserInfoDetailActivity.class);
                        intent.putExtra("groupId", mGroupTableMessage.getGroupId());
                        intent.putExtra("from", "groupNick");
                        startActivityForResult(intent, Constant.REQUEST_CODE_EDIT_GROUP_INFO_NICK);

                } else if (i == R.id.ll_group_info_number_count) {
                        Intent numberIntent = new Intent(this, GroupNumberDisplayActivity.class);
                        numberIntent.putExtra("groupId", mGroupTableMessage.getGroupId());
                        numberIntent.putExtra("isCreator", isCreator);
                        startActivity(numberIntent);

                } else if (i == R.id.ll_group_info_group_name) {
                        Intent groupName = new Intent(this, EditUserInfoDetailActivity.class);
                        groupName.putExtra("groupId", mGroupTableMessage.getGroupId());
                        groupName.putExtra("from", "groupName");
                        groupName.putExtra("message", mGroupTableMessage.getGroupName());
                        startActivityForResult(groupName, Constant.REQUEST_CODE_EDIT_GROUP_INFO_GROUP_NAME);

                } else if (i == R.id.ll_group_info_group_description) {
                        Intent description = new Intent(this, EditUserInfoDetailActivity.class);
                        description.putExtra("groupId", mGroupTableMessage.getGroupId());
                        description.putExtra("from", "groupDescription");
                        description.putExtra("message", mGroupTableMessage.getGroupDescription());
                        startActivityForResult(description, Constant.REQUEST_CODE_EDIT_GROUP_INFO_DESCRIPTION);

                } else if (i == R.id.ll_group_info_group_notification) {
                        Intent notification = new Intent(this, EditUserInfoDetailActivity.class);
                        notification.putExtra("groupId", mGroupTableMessage.getGroupId());
                        notification.putExtra("from", "groupNotification");
                        notification.putExtra("message", mGroupTableMessage.getNotification());
                        startActivityForResult(notification, Constant.REQUEST_CODE_EDIT_GROUP_INFO_NOTIFICATION);

                } else if (i == R.id.ll_group_info_remind) {
                        isRemind = !isRemind;
                        BaseApplication.getAppComponent().getSharedPreferences()
                                .edit().putBoolean(mGroupTableMessage.getGroupId(), isRemind);
                        if (isRemind) {
                                remind.setText("寮€鍚?);
                        } else {
                                remind.setText("鍏抽棴");
                        }

                } else if (i == R.id.btn_group_info_exit_group) {
                        if (mGroupTableMessage.getCreatorId().equals(UserManager.getInstance().getCurrentUserObjectId())) {
//                                        缇や富閫€鍑鸿缇?
                                Toast.makeText(this, "缇ょ粍涓嶈兘閫€鍑鸿缇?, Toast.LENGTH_SHORT).show();
                        } else {
                                GroupTableMessage groupTableMessage = new GroupTableMessage();
                                groupTableMessage.setObjectId(mGroupTableMessage.getGroupId());
                                List<String> numberList = new ArrayList<>(mGroupTableMessage.getGroupNumber());
                                if (numberList.contains(UserManager.getInstance().getCurrentUserObjectId())) {
                                        numberList.remove(UserManager.getInstance().getCurrentUserObjectId());
                                }
                                groupTableMessage.setGroupNumber(numberList);
                                groupTableMessage.update(this, new UpdateListener() {
                                        @Override
                                        public void onSuccess() {
                                                LogUtil.e("鏇存柊缇や富鐨勭兢缁撴瀯娑堟伅鎴愬姛");
                                                finish();
                                        }

                                        @Override
                                        public void onFailure(int i, String s) {
                                                LogUtil.e("鏇存柊缇や富鐨勭兢缁撴瀯娑堟伅澶辫触" + s + i);
                                        }
                                });
                        }


                } else {
                }
        }




        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == Activity.RESULT_OK) {
                        String message = data.getStringExtra("message");
                        switch (requestCode) {
                                case Constant.REQUEST_CODE_TAKE_PICTURE:
                                        showLoadDialog("姝ｅ湪涓婁紶缇ゅご鍍忥紝璇风◢鍚?........");
                                        LogUtil.e("鎷嶇収鐨勫浘鐗噋ath涓? + localImagePath);
                                        final BmobFile bmobFile = new BmobFile(new File(localImagePath));
                                        bmobFile.uploadblock(BaseApplication.getInstance(), new UploadFileListener() {
                                                @Override
                                                public void onSuccess() {
                                                        LogUtil.e("涓婁紶缇ゅご鍍忔垚鍔?);
                                                        MsgManager.getInstance().updateGroupMessage(mGroupTableMessage.getGroupId(), "groupAvatar", bmobFile.getFileUrl(BaseApplication.getInstance()), new UpdateListener() {
                                                                @Override
                                                                public void onSuccess() {
                                                                        dismissLoadDialog();
                                                                        ToastUtils.showShortToast("鏇存柊缇ゅご鍍忔垚鍔?);
                                                                        LogUtil.e("鏇存柊缇ゆ秷鎭殑澶村儚鎴愬姛");
                                                                        Glide.with(GroupInfoActivity.this).load(new File(localImagePath)).into(groupAvatar);
                                                                        LogUtil.e("閫氱煡鑱婂ぉ鐣岄潰缇ゅご鍍忕殑鏀瑰彉");
                                                                        RxBusManager.getInstance().post(new GroupInfoEvent(localImagePath,GroupInfoEvent.TYPE_GROUP_AVATRA));


                                                                }

                                                                @Override
                                                                public void onFailure(int i, String s) {
                                                                        dismissLoadDialog();
                                                                        LogUtil.e("鏇存柊缇ゆ秷鎭殑澶村儚澶辫触" + s + i);
                                                                }
                                                        });
                                                }
                                                @Override
                                                public void onFailure(int i, String s) {
                                                        dismissLoadDialog();
                                                        ToastUtils.showShortToast("涓婁紶缇ゅご鍍忓け璐? + s + i);
                                                        LogUtil.e("涓婁紶缇ゅご鍍忓け璐? + s + i);
                                                }
                                        });
                                        break;
                                case Constant.REQUEST_CODE_SELECT_FROM_LOCAL:
                                        Uri uri = data.getData();
                                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                                        if (cursor != null && cursor.moveToFirst()) {
                                                final String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                                                if (path != null) {
                                                        LogUtil.e("鎸戦€夌殑鍥剧墖path涓? + path);
                                                        showLoadDialog("姝ｅ湪涓婁紶缇ゅご鍍忥紝璇风◢鍚?........");
                                                        final BmobFile bmobFile1 = new BmobFile(new File(path));
                                                        bmobFile1.uploadblock(BaseApplication.getInstance(), new UploadFileListener() {
                                                                @Override
                                                                public void onSuccess() {
                                                                        LogUtil.e("涓婁紶缇ゅご鍍忔垚鍔?);
                                                                        MsgManager.getInstance().updateGroupMessage(mGroupTableMessage.getGroupId(), "groupAvatar", bmobFile1.getFileUrl(BaseApplication.getInstance()), new UpdateListener() {
                                                                                @Override
                                                                                public void onSuccess() {
                                                                                        dismissLoadDialog();
                                                                                        ToastUtils.showShortToast("鏇存柊缇ゅご鍍忔垚鍔?);
                                                                                        LogUtil.e("鏇存柊缇ゆ秷鎭殑澶村儚鎴愬姛");
//                                                                                        LogUtil.e("鐜板湪鐨勭兢澶村儚" + mGroupTableMessage.getGroupAvatar());
//                                                                                        MessageCacheManager.getInstance().getGroupTableMessage(mGroupTableMessage.getGroupId()).setGroupAvatar(bmobFile1.getFileUrl(BaseApplication.getInstance()));
//                                                                                        LogUtil.e("濡備粖鐨勭兢澶村儚" + mGroupTableMessage.getGroupAvatar());
//                                                                                        ChatDB.create().saveGroupTableMessage(mGroupTableMessage);
                                                                                        Glide.with(GroupInfoActivity.this).load(new File(path)).into(groupAvatar);
                                                                                        RxBusManager.getInstance().post(new GroupInfoEvent(path,GroupInfoEvent.TYPE_GROUP_AVATRA));
                                                                                }

                                                                                @Override
                                                                                public void onFailure(int i, String s) {
                                                                                        dismissLoadDialog();
                                                                                        LogUtil.e("鏇存柊缇ゆ秷鎭殑澶村儚澶辫触" + s + i);
                                                                                }
                                                                        });
                                                                }

                                                                @Override
                                                                public void onFailure(int i, String s) {
                                                                        dismissLoadDialog();
                                                                        ToastUtils.showShortToast("涓婁紶缇ゅご鍍忓け璐? + s + i);
                                                                        LogUtil.e("涓婁紶缇ゅご鍍忓け璐? + s + i);
                                                                }
                                                        });
                                                } else {
                                                        LogUtil.e("鎸戦€夌殑鍥剧墖鐨勮矾寰勪负绌?);
                                                }
                                        }
                                        if (cursor != null && !cursor.isClosed()) {
                                                cursor.close();
                                        }
                                        break;
                                case Constant.REQUEST_CODE_EDIT_GROUP_INFO_NICK:
                                        LogUtil.e("杩欓噷缇ゆ樀绉扮殑鏀瑰彉");
                                        mGroupTableMessage.setGroupNick(message);
                                        numberNick.setText(message);
                                        List<GroupNumberInfo> list = MessageCacheManager.getInstance().getAllGroupNumberInfo(mGroupTableMessage.getGroupId());
                                        if (list != null) {
                                                for (GroupNumberInfo info :
                                                        list) {
                                                        if (info.getUser().getObjectId().equals(UserManager.getInstance().getCurrentUser().getObjectId())) {
                                                                info.setGroupNick(message);
                                                                break;
                                                        }
                                                }
                                                mAdapter.clearAllData();
                                                mAdapter.addData(list);
                                        }
                                        RxBusManager.getInstance().post(new GroupInfoEvent(message,GroupInfoEvent.TYPE_GROUP_NICK));
                                        break;
                                case Constant.REQUEST_CODE_EDIT_GROUP_INFO_DESCRIPTION:
                                        mGroupTableMessage.setGroupDescription(message);
                                        description.setText(message);
                                        RxBusManager.getInstance().post(new GroupInfoEvent(message,GroupInfoEvent.TYPE_GROUP_DESCRIPTION));

                                        break;
                                case Constant.REQUEST_CODE_EDIT_GROUP_INFO_NOTIFICATION:
                                        mGroupTableMessage.setNotification(message);
                                        notification.setText(message);
                                        RxBusManager.getInstance().post(new GroupInfoEvent(message,GroupInfoEvent.TYPE_GROUP_NOTIFICATION));
                                        break;
                                case Constant.REQUEST_CODE_EDIT_GROUP_INFO_GROUP_NAME:
                                        mGroupTableMessage.setGroupName(message);
                                        groupName.setText(message);
                                        groupName1.setText(message);




                                        RxBusManager.getInstance().post(new GroupInfoEvent(message,GroupInfoEvent.TYPE_GROUP_NAME));
                                        break;
                        }
                }
        }

        @Override
        public void updateData(Object o) {
                
        }
}

