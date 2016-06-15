package com.softfun_xmpp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.GroupBean;
import com.softfun_xmpp.utils.ImageLoaderUtils;

import java.util.List;

public class MyGroupAdapter extends BaseAdapter {

    //private final LayoutInflater mInflater;
    private Context mContext;
    private List<GroupBean> mData;

    public MyGroupAdapter(List<GroupBean> data , Context context) {
        this.mData = data;
        //this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView =  View.inflate(mContext, R.layout.item_layout_group, null);
            holder = new ViewHolder();
            //得到各个控件的对象
            holder.tv = (TextView) convertView.findViewById(R.id.tv_item_groupname);
            holder.iv = (ImageView) convertView.findViewById(R.id.item_list_iv);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        //设置TextView显示的内容，即我们存放在动态数组中的数据
        holder.tv.setText(mData.get(position).getChild());
        //群头像
        String groupface = mData.get(position).getGroupface();
        if(groupface==null) {
            groupface = "drawable://" + R.drawable.groupface0;
        }
        ImageLoader.getInstance().displayImage(groupface,holder.iv, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular());

        return convertView;
    }


    /*存放控件*/
    public final class ViewHolder{
        public TextView tv;
        public ImageView iv;
    }
}
