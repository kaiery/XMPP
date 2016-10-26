package kaiery.csrs.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import kaiery.csrs.R;
import kaiery.csrs.adapter.holder.Holder_Module_Button;
import kaiery.csrs.adapter.holder.Holder_Module_Header;
import kaiery.csrs.beans.ModuleBean;

/**
 * ------------------------------
 * Created by 范张 on 2016-10-26.
 * ------------------------------
 */

public class ModuleRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModuleBean> mList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    public ModuleRecyclerAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * 根据数据类型，加载不同的布局
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ModuleBean.TYPE_HEADER: {
                return new Holder_Module_Header(mLayoutInflater.inflate(R.layout.item_module_header, parent, false));
            }
            case ModuleBean.TYPE_BUTTON: {
                return new Holder_Module_Button(mLayoutInflater.inflate(R.layout.item_module_button, parent, false));
            }
        }
        return null;
    }

    /**
     * 绑定数据
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ModuleBean.TYPE_HEADER: {
                ((Holder_Module_Header) holder).bindHolder(mList.get(position));
                ((Holder_Module_Header) holder).hideTopLine(position);
                break;
            }
            case ModuleBean.TYPE_BUTTON: {
                ((Holder_Module_Button) holder).bindHolder(mList.get(position));
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getType();
    }

    public void addList(List<ModuleBean> list) {
        mList.addAll(list);
    }
}
