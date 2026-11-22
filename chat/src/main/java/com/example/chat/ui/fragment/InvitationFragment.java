package com.example.chat.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.example.chat.R;
import com.example.chat.adapter.NewFriendAdapter;
import com.example.chat.base.Constant;
import com.example.chat.bean.ChatMessage;
import com.example.chat.bean.InvitationMsg;
import com.example.chat.bean.User;
import com.example.chat.db.ChatDB;
import com.example.chat.listener.AddFriendCallBackListener;
import com.example.chat.listener.OnSendTagMessageListener;
import com.example.chat.manager.MsgManager;
import com.example.chat.manager.UserCacheManager;
import com.example.chat.manager.UserManager;
import com.example.chat.ui.MainActivity;
import com.example.chat.util.LogUtil;
import com.example.commonlibrary.BaseFragment;
import com.example.commonlibrary.baseadapter.listener.OnSimpleItemChildClickListener;
import com.example.commonlibrary.baseadapter.swipeview.Closeable;
import com.example.commonlibrary.baseadapter.swipeview.OnSwipeMenuItemClickListener;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenu;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenuCreator;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenuItem;
import com.example.commonlibrary.baseadapter.swipeview.SwipeMenuRecyclerView;
import com.example.commonlibrary.baseadapter.viewholder.BaseWrappedViewHolder;
import com.example.commonlibrary.cusotomview.ListViewDecoration;

import cn.bmob.v3.exception.BmobException;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2016/11/5      18:41
 * QQ:             1981367757
 */
public class InvitationFragment extends BaseFragment {
        private NewFriendAdapter adapter;
        private SwipeMenuRecyclerView display;
        private LinearLayoutManager linearLayoutManager;


        @Override
        protected boolean isNeedHeadLayout() {
                return false;
        }

        @Override
        protected boolean isNeedEmptyLayout() {
                return false;
        }

        @Override
        protected int getContentLayout() {
                return R.layout.fragment_invitation;
        }

        @Override
        public void initView() {
                display = (SwipeMenuRecyclerView) findViewById(R.id.swrc_new_friend_display);
                display.setLayoutManager(linearLayoutManager=new LinearLayoutManager(getActivity()));
                display.setHasFixedSize(true);
                display.setItemAnimator(new DefaultItemAnimator());
                display.addItemDecoration(new ListViewDecoration(getActivity()));
                display.setSwipeMenuCreator(new SwipeMenuCreator() {
                        @Override
                        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                                int width = (int) getResources().getDimension(R.dimen.recent_top_height);
                                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                                SwipeMenuItem item = new SwipeMenuItem(getActivity());
                                item.setBackgroundDrawable(new ColorDrawable(Color.rgb(0xF9,
                                        0x3F, 0x25))).setText("鍒犻櫎").setTextColor(Color.WHITE).setHeight(height).setWidth(width);
                                swipeRightMenu.addMenuItem(item);
                        }
                });
        }


        @Override
        public void initData() {
                adapter = new NewFriendAdapter();
                adapter.addData(ChatDB.create().getAllInvitationMsg());
               adapter.setOnItemClickListener(new OnSimpleItemChildClickListener() {
                       @Override
                       public void onItemChildClick(final int position, View view, int id) {
                               if (id == R.id.btn_new_friend_item_agree) {
                                       showLoading("姝ｅ湪娣诲姞........");
                                       LogUtil.e("姝ｅ湪鍚屾剰娣诲姞涓哄ソ鍙?........");
                                       final InvitationMsg message = adapter.getData(position);
                                       UserManager.getInstance().addNewFriend(message.getBelongId(), message.getToId(),
                                               new AddFriendCallBackListener() {
                                                       @Override
                                                       public void onSuccess(User user) {
                                                               hideLoading();
                                                               UserCacheManager.getInstance().addContact(user);
                                                               if (ChatDB.create().updateInvitationMsgStatus(message, Constant.READ_STATUS_READED) > 0) {
                                                                       LogUtil.e("鏇存柊閭€璇锋秷鎭凡璇荤姸鎬佹垚鍔?);
                                                               } else {
                                                                       LogUtil.e("鏇存柊閭€璇锋秷鎭凡璇荤姸鎬佸け璐?);
                                                               }
                                                               LogUtil.e("姝ｅ湪鍙戦€佸洖鎵у悓鎰忔秷鎭?........");
                                                               MsgManager.getInstance().sendTagMessage(user.getObjectId(), Constant.TAG_AGREE,
                                                                       new OnSendTagMessageListener() {
                                                                               @Override
                                                                               public void onSuccess(ChatMessage chatMessage) {
                                                                                       LogUtil.e("鍙戦€佸悓鎰忔秷鎭垚鍔?);
                                                                                       LogUtil.e("鏈€缁堟坊鍔犲ソ鍙嬫垚鍔熸坊鍔犲ソ鍙嬫垚鍔?);
                                                                                       BaseWrappedViewHolder baseWrappedViewHolder=
                                                                                               (BaseWrappedViewHolder) display.getChildViewHolder(linearLayoutManager.findViewByPosition(position));
                                                                                       baseWrappedViewHolder.setVisible(R.id.btn_new_friend_item_agree, false)
                                                                                               .setVisible(R.id.tv_new_friend_item_agree, true);
                                                                                       ((MainActivity) getActivity()).notifyContactAndRecent(chatMessage);
                                                                               }

                                                                               @Override
                                                                               public void onFailed(BmobException e) {
                                                                                       LogUtil.e("鍙戦€佸悓鎰忔秷鎭け璐?+e.getMessage()+e.getErrorCode());
                                                                               }
                                                                       });
                                                       }

                                                       @Override
                                                       public void onFailed(BmobException e) {
                                                               hideLoading();
                                                               LogUtil.e("娣诲姞濂藉弸澶辫触" + e.getMessage() + e.getErrorCode());
                                                       }
                                               });
                               }
                       }
               });
                display.setSwipeMenuItemClickListener(new OnSwipeMenuItemClickListener() {
                        @Override
                        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, @SwipeMenuRecyclerView.DirectionMode int direction) {
                                InvitationMsg msg = adapter.getData(adapterPosition);
                                ChatDB.create().deleteInvitationMsg(msg.getBelongId(), msg.getTime());
                                adapter.removeData(adapterPosition);
                                ((MainActivity) getActivity()).notifyMenuUpdate();
                        }
                });
                display.setAdapter(adapter);
        }


        @Override
        public void onResume() {
                super.onResume();
                onHiddenChanged(false);
        }

        @Override
        public void onHiddenChanged(boolean hidden) {
                super.onHiddenChanged(hidden);
                if (!hidden) {
                        ((MainActivity) getActivity()).initActionBar("閭€璇?);
                }

        }


        public void updateData() {
                adapter.clearAllData();
                adapter.addData(ChatDB.create().getAllInvitationMsg());
        }

        @Override
        protected void updateView() {
        }

        public void updateInvitationData(ChatMessage chatMessage) {
                LogUtil.e("InvitationFragment锛氭湁鏂扮殑閭€璇锋秷鎭潵鎷夋媺锛屾洿鏂伴個璇峰垪琛?);
                InvitationMsg invitationMsg = new InvitationMsg();
                invitationMsg.setName(chatMessage.getBelongUserName());
                invitationMsg.setTime(chatMessage.getCreateTime());
                invitationMsg.setContent(chatMessage.getContent());
                invitationMsg.setReadStatus(chatMessage.getReadStatus());
                invitationMsg.setNick(chatMessage.getBelongNick());
                invitationMsg.setAvatar(chatMessage.getBelongAvatar());
                invitationMsg.setBelongId(chatMessage.getBelongId());
                invitationMsg.setMsgType(chatMessage.getMsgType());
                invitationMsg.setToId(chatMessage.getToId());
                adapter.addData(invitationMsg);
        }

        @Override
        public void updateData(Object o) {

        }
}

