package com.example.discover.base.baseadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.example.discover.utils.DebugUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/13 0013.
 */

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    public List<T> mData = new ArrayList<>();
    public Context mContext;

    public BaseRecyclerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        DebugUtil.debug("position11", position + "");
        if (position == mData.size()) {
            holder.baseFillHolder(null, position);
            return;
        }
        holder.baseFillHolder(mData.get(position), position);

    }

    @Override
    public int getItemCount() {
        //考虑有footer的情况
        return mData.size() + 1;
    }

    public void addAll(List<T> data) {
        this.mData.addAll(data);
        DebugUtil.debug("listSize", data.size() + "");
    }

    public void clear() {
        this.mData.clear();
    }

    public void delete(int position) {
        if (this.mData != null && this.mData.size() > position) {
            this.mData.remove(position);
        }
        notifyItemRemoved(position);

        if (position != mData.size()) {
            notifyItemRangeRemoved(position, mData.size() - position);
        }
    }
}
