package com.example.chat.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.example.chat.R;
import com.example.chat.util.LogUtil;
import com.example.chat.util.PixelUtil;
import com.example.commonlibrary.baseadapter.adapter.BaseRecyclerAdapter;
import com.example.commonlibrary.baseadapter.viewholder.BaseWrappedViewHolder;

import java.io.File;
import java.util.List;

/**
 * 椤圭洰鍚嶇О:    TestChat
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2017/1/18      13:29
 * QQ:             1981367757
 */

public class ImageDisplayAdapter extends BaseRecyclerAdapter<String, BaseWrappedViewHolder> {
        private int mPagePadding = 15;
        private int showCardWidth = 15;


        public ImageDisplayAdapter(List<String> data, int layoutId) {
                super(data, layoutId);
        }

        @Override
        protected int getLayoutId() {
                return 0;
        }


        public int getPagePadding() {
                return mPagePadding;
        }

        public void setPagePadding(int pagePadding) {
                mPagePadding = pagePadding;
        }

        public int getShowCardWidth() {
                return showCardWidth;
        }

        public void setShowCardWidth(int showCardWidth) {
                this.showCardWidth = showCardWidth;
        }

        @Override
        public BaseWrappedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                BaseWrappedViewHolder baseWrappedViewHolder = super.onCreateViewHolder(parent, viewType);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) baseWrappedViewHolder.itemView.getLayoutParams();
                layoutParams.width = (parent.getWidth() - 2 * (PixelUtil.dp2px(mPagePadding + showCardWidth)));
                baseWrappedViewHolder.itemView.setLayoutParams(layoutParams);
                baseWrappedViewHolder.itemView.setPadding(PixelUtil.dp2px(mPagePadding), 0, PixelUtil.dp2px(mPagePadding), 0);
                return baseWrappedViewHolder;
        }


        @Override
        protected void convert(BaseWrappedViewHolder holder, String data) {
                int position = holder.getAdapterPosition();
//                                杩欓噷鏈€鍚庝竴涓拰绗竴涓缃甿argin
                int leftMargin = position == 0 ? PixelUtil.dp2px(mPagePadding + showCardWidth) : 0;
                int rightMargin = position == getItemCount() - 1 ? PixelUtil.dp2px(mPagePadding + showCardWidth) : 0;
                if (leftMargin != 0 || rightMargin != 0) {
                        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
                        marginLayoutParams.setMargins(leftMargin, 0, rightMargin, 0);
                        holder.itemView.setLayoutParams(marginLayoutParams);
                }
                File file = new File(data);
                if (file.exists()) {
                        LogUtil.e("鍔犺浇鏂囦欢");
                        holder.setImageUrl(R.id.iv_image_display_item_display, file);
                } else {
                        LogUtil.e("11鍔犺浇" +
                                "url");
                        holder.setImageUrl(R.id.iv_image_display_item_display, data);
                }
        }
}

