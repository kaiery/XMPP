package kaiery.csrs.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

import kaiery.csrs.R;
import kaiery.csrs.beans.ImagePickBean;

/***
 * 用户图片拾取的网格适配器
 */
public class GridAdapter_Gallery extends BaseAdapter {

    private List<ImagePickBean> mList = new ArrayList<>();
    private LayoutInflater mInflater;

    /**
     * 成员变量
     */
    private UserPhotoClickListener mListener;
    private UserPhotoLongClickListener mLongListener;
    /**
     * 加载更多接口
     */
    public interface UserPhotoClickListener{
        void onUserPhotoClick(View view, int position);
    }
    public interface UserPhotoLongClickListener{
        void onUserPhotoLongClick(View view, int position);
    }
    /**
     * 公开加载更多方法
     */
    public void setUserPhotoClickListener(UserPhotoClickListener listener) {
        mListener = listener;
    }
    public void setUserPhotoLongClickListener(UserPhotoLongClickListener listener) {
        mLongListener = listener;
    }

    public GridAdapter_Gallery(Context context, List<ImagePickBean> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        //params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_userphoto_image, null);
            // construct an item tag
            holder = new ViewHolder((ImageView) convertView.findViewById(R.id.iv_item_userphotoimage),
                    (TextView) convertView.findViewById(R.id.tv_item_name),
                    (CheckBox) convertView.findViewById(R.id.checkBox),
                    (ProgressBar) convertView.findViewById(R.id.pb)
            );
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set name
        //holder.mName.setText(mList.get(position).getBtnname());
        // set icon
        ImageLoader.getInstance().displayImage("file://"  + mList.get(position).getUrl(), holder.mIcon, getOptions());
        //holder.mIcon.setLayoutParams(params);

        holder.mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onUserPhotoClick(v, position);
                }
            }
        });

        holder.mIcon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mLongListener!=null){
                    mLongListener.onUserPhotoLongClick(v,position);
                }
                return true;
            }
        });
        holder.mCheckBox.setClickable(false);

        if(mList.get(position).isShowcheck()){
            holder.mCheckBox.setVisibility(View.VISIBLE);
        }else{
            holder.mCheckBox.setVisibility(View.GONE);
        }

        if(mList.get(position).isCheck()){
            holder.mCheckBox.setChecked(true);
        } else {
            holder.mCheckBox.setChecked(false);
        }


        if(mList.get(position).isupload()) {
            holder.mProgressBar.setVisibility(View.VISIBLE);
            holder.mProgressBar.setProgress(mList.get(position).getProgress());
        }else {
            holder.mProgressBar.setVisibility(View.GONE);
        }
        return convertView;
    }


    class ViewHolder {
        protected ImageView mIcon;
        protected TextView mName;
        protected CheckBox mCheckBox;
        protected ProgressBar mProgressBar;
        /**
         * The constructor to construct a navigation view tag
         *
         * @param name the name view of the item
         * @param icon the icon view of the item
         */
        public ViewHolder (ImageView icon, TextView name,CheckBox checkBox,ProgressBar progressBar) {
            this.mName = name;
            this.mIcon = icon;
            this.mCheckBox = checkBox;
            this.mProgressBar = progressBar;
        }
    }


    private DisplayImageOptions getOptions() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.mipmap.ic_launcher) //设置图片在下载期间显示的图片
                //.showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
                //.showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(false)
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                        //.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
                        //.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                        //.displayer(new Displayer(0))//是否设置为圆角，弧度为多少
                        //.displayer(new RoundedBitmapDisplayer(50,10))//是否设置为圆角，弧度为多少
                        //.displayer(new FadeInBitmapDisplayer(1400))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        return options;
    }
}
