package com.softfun_xmpp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.FeatureBtnBean;
import com.softfun_xmpp.utils.ImageLoaderUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 类似九宫格的图标网格适配器
 */
public class GridAdapter extends BaseAdapter {

    private List<FeatureBtnBean> mList = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;
    RelativeLayout.LayoutParams params;

    public GridAdapter(Context context, List<FeatureBtnBean> list) {
        mList = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewTag viewTag;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_image, null);
            // construct an item tag
            viewTag = new ItemViewTag((ImageView) convertView.findViewById(R.id.ItemImage), (TextView) convertView.findViewById(R.id.ItemText));
            convertView.setTag(viewTag);
        } else {
            viewTag = (ItemViewTag) convertView.getTag();
        }

        // set name
        viewTag.mName.setText(mList.get(position).getBtnname());
        // set icon
        ImageLoader.getInstance().displayImage("drawable://"  + mList.get(position).getBtnicon(), viewTag.mIcon, ImageLoaderUtils.getOptions_NoCacheInMem_NoCacheInDisk_NoExif());
        //viewTag.mIcon.setLayoutParams(params);
        return convertView;
    }



    class ItemViewTag {
        protected ImageView mIcon;
        protected TextView mName;

        /**
         * The constructor to construct a navigation view tag
         *
         * @param name the name view of the item
         * @param icon the icon view of the item
         */
        public ItemViewTag(ImageView icon, TextView name) {
            this.mName = name;
            this.mIcon = icon;
        }
    }
}
