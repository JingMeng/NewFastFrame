/*
 * Copyright 2016 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.commonlibrary.baseadapter.swipeview.touch;

import android.graphics.Canvas;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;


public class DefaultItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private OnItemMovementListener onItemMovementListener;

    private OnItemMoveListener onItemMoveListener;

    private OnItemStateChangedListener onItemStateChangedListener;

    private boolean isItemViewSwipeEnabled;

    private boolean isLongPressDragEnabled;

    public DefaultItemTouchHelperCallback() {
    }

    public void setLongPressDragEnabled(boolean canDrag) {
        this.isLongPressDragEnabled = canDrag;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return isLongPressDragEnabled;
    }

    public void setItemViewSwipeEnabled(boolean canSwipe) {
        this.isItemViewSwipeEnabled = canSwipe;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return isItemViewSwipeEnabled;
    }

    public void setOnItemMoveListener(OnItemMoveListener onItemMoveListener) {
        this.onItemMoveListener = onItemMoveListener;
    }

    public OnItemMoveListener getOnItemMoveListener() {
        return onItemMoveListener;
    }

    public void setOnItemMovementListener(OnItemMovementListener onItemMovementListener) {
        this.onItemMovementListener = onItemMovementListener;
    }

    public OnItemMovementListener getOnItemMovementListener() {
        return onItemMovementListener;
    }

    public void setOnItemStateChangedListener(OnItemStateChangedListener onItemStateChangedListener) {
        this.onItemStateChangedListener = onItemStateChangedListener;
    }

    public OnItemStateChangedListener getOnItemStateChangedListener() {
        return onItemStateChangedListener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder targetViewHolder) {
        if (onItemMovementListener != null) {
            int dragFlags = onItemMovementListener.onDragFlags(recyclerView, targetViewHolder);
            int swipeFlags = onItemMovementListener.onSwipeFlags(recyclerView, targetViewHolder);
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            //閫傜敤GridLayoutManager绫诲瀷鍜孡inearLayoutManager绫诲瀷銆?            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                //鎷栨嫿鐨勬柟鍚戙€?                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                //渚ф粦鐨勬柟鍚戯細left right銆?                return makeMovementFlags(dragFlags, swipeFlags);
            } else if (layoutManager instanceof LinearLayoutManager) {
                //绾挎€ф柟鍚戝垎涓猴細姘村钩鍜屽瀭鐩存柟鍚戙€?                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    return makeMovementFlags(dragFlags, swipeFlags);
                } else {
                    //鎷栨嫿鐨勬柟鍚戙€?                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    //渚ф粦鐨勬柟鍚戯細left right銆?                    int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
            }
        }
        return makeMovementFlags(0, 0);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //鍒ゆ柇褰撳墠鏄惁鏄痵wipe鏂瑰紡锛氫晶婊戙€?        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            //1.ItemView--ViewHolder; 2.渚ф粦鏉＄洰鐨勯€忔槑搴︾▼搴﹀叧鑱旇皝锛焏X(delta澧為噺锛岃寖鍥达細褰撳墠鏉＄洰-width~width)銆?            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            float alpha = 1;
            if (layoutManager instanceof LinearLayoutManager) {
                int orientation = ((LinearLayoutManager) layoutManager).getOrientation();
                if (orientation == LinearLayoutManager.HORIZONTAL) {
                    alpha = 1 - Math.abs(dY) / viewHolder.itemView.getHeight();
                } else if (orientation == LinearLayoutManager.VERTICAL) {
                    alpha = 1 - Math.abs(dX) / viewHolder.itemView.getWidth();
                }
            }
            viewHolder.itemView.setAlpha(alpha);//1~0
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }


    @Override
    public boolean onMove(RecyclerView arg0, RecyclerView.ViewHolder srcHolder, RecyclerView.ViewHolder targetHolder) {
        if (srcHolder.getItemViewType() == targetHolder.getItemViewType()) {// 鐩稿悓绫诲瀷鐨勫厑璁稿垏鎹€?            if (onItemMoveListener == null)
                return false;
            //鍥炶皟鍒锋柊鏁版嵁鍙婄晫闈€?            return onItemMoveListener.onItemMove(srcHolder.getAdapterPosition(), targetHolder.getAdapterPosition());
        }
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //鍥炶皟鍒锋柊鏁版嵁鍙婄晫闈€?        if (onItemMoveListener != null)
            onItemMoveListener.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (onItemStateChangedListener != null) {
            onItemStateChangedListener.onSelectedChanged(viewHolder, actionState);
        }
    }
}
