package com.softfun_xmpp.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.FriendInfoBean;
import com.softfun_xmpp.bean.ImageFaceBean;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.VipResouce;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter_SearchResult extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    /**
     * 网络路径前缀
     */
    private String mServiceHead;
    /**
     * 数据list
     */
    private List<FriendInfoBean> mlist;
    /**
     * 上下文
     */
    private Context mcontext;
    /**
     * 内部变量：点击监听器
     */
    private MyItemClickListener mItemClickListener;
    /**
     * 内部变量：长击监听器
     */
    private MyItemLongClickListener mItemLongClickListener;
    /**
     * 可见的item起始位置
     */
    private int mStart,mEnd;
    /**
     * 要加载的图片地址数组
     */
    public static List<ImageFaceBean> URLS;

    private LinearLayoutManager layoutManager ;

    private boolean mFristIn = true;
    /**
     * 当前适配器中图片的url地址
     */
    private List<String> mImgs;

    private int lastVisibleItem;


    /**
     * 开关：正在加载
     */
    private boolean mIsLoadingMore = false;
    /**
     * 正在加载的位置
     */
    private int mLoadMorePosition;
    /**
     * 开启上拉加载模式
     */
    private boolean mIsFooterEnable = false;

    /**
     * 加载更多数据 内部变量
     */
    private LoadMoreListener mListener;



    /***
     * 构造方法
     * 传入初始化数据
     * @param context
     * @param list
     */
    public ListAdapter_SearchResult(Context context, List<FriendInfoBean> list, RecyclerView recyclerView){
        this.mServiceHead = context.getResources().getString(R.string.app_server);
        this.mcontext = context;
        this.mlist = list;

//        mImgs = new ArrayList<String>();
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        windowManager.getDefaultDisplay().getMetrics(outMetrics);


        layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        URLS = new ArrayList<>();//new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String url = mServiceHead + list.get(i).getUserface()+"?userid="+list.get(i).getUserid();
            ImageFaceBean bean = new ImageFaceBean(url,url);
            URLS.add( bean );
        }
        if(URLS.size()==0){
            mFristIn = false;
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * 重写监听滚动状态变化事件
             * @param recyclerView
             * @param newState
             */
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //加载图片的操作权在这里！！
                //判断滚动状态
                //IDLE闲置状态
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    //判断URLS是否跟mlist长度一样，意味着是否有新数据被添加或删除
                    if(URLS.size()!=mlist.size()){
                        if(mlist.size()>URLS.size()){
                            //有新增数据,从新增的数据开始添加到URLS内
                            for (int i = URLS.size(); i < mlist.size(); i++) {
                                String url = mServiceHead + mlist.get(i).getUserface()+"?userid="+mlist.get(i).getUserid();
                                ImageFaceBean bean = new ImageFaceBean(url,url);
                                URLS.add( bean );
                            }
                        }
                    }
                    //加载可见item,从指定位置加载图像
                    //ImageLoaderUtils.LoadImages(mStart,mEnd,URLS,recyclerView);

                    //上拉加载代码
                    if(!mIsLoadingMore){
                        if(lastVisibleItem + 1 == getItemCount()){
                            mIsLoadingMore = true;
                            mLoadMorePosition = lastVisibleItem;
                            //调用加载更多
                            mListener.onLoadMore();
                        }
                    }
                    //上拉加载代码END

                }else{
                    //停止下载图片
                    //ImageLoaderUtils.cancelAllTasks(mStart,mEnd,URLS,recyclerView);
                }
            }

            /**
             * 重写监听滚动事件
             * @param recyclerView
             * @param dx
             * @param dy
             */
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //上拉加载代码
                if(!mIsLoadingMore && dy > 0 && mIsFooterEnable){
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                }
                //上拉加载代码end


                //不断获取第一个可见item、最后一个可见item
                mStart = layoutManager.findFirstVisibleItemPosition();
                mEnd = layoutManager.findFirstVisibleItemPosition()+layoutManager.getChildCount()-1;
                //如果是第一次进入程序，则主动加载第一屏的图像
                if(mFristIn && layoutManager.getChildCount()>0){
                    mFristIn = false;
                    //ImageLoaderUtils.LoadImages(mStart,mEnd,URLS,recyclerView);
                }

            }
        });
    }



    /***
     * 当Itemview创建时的回调
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        //根据类型选择不同的item布局xml
        if(viewType == TYPE_ITEM){
            v = LayoutInflater.from(mcontext).inflate(R.layout.item_layout_search_friends, parent, false);
            return new ListHolder(v,mItemClickListener,mItemLongClickListener);
        }else if(viewType == TYPE_FOOTER){
            v = LayoutInflater.from(mcontext).inflate(R.layout.item_layout_search_friends_footerview, parent, false);
            return new ListHolder_Footer(v);
        }
        return null;
    }


    /****
     * 将要渲染新的Item的时候的回调
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ListHolder) {
            //传入将要渲染的item位置
            ((ListHolder)holder).setData(mlist.get(position),position);
        }

    }

    /***
     * 返回list的数据量
     * 上拉 加载属性
     * count设置为数据总条数
     * @return
     */
    @Override
    public int getItemCount() {
        if(mlist!=null){
            int count = mlist.size();
            if (mIsFooterEnable) count++;  //新增一个固化item，用来显示 正在加载数据
            return count;
        }
        return 0;
    }

    /**
     * 根据当前显示item的位置，判断使用哪个item布局
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        int footerPosition = getItemCount() - 1;
        if (footerPosition == position && mIsFooterEnable) {
            return TYPE_FOOTER;
        }else{
            return TYPE_ITEM;
        }
    }

    /**
     * 定义点击接口
     */
    public interface MyItemClickListener {
        public void onItemClick(View view, int position);
    }
    /**
     * 定义长击接口
     */
    public interface MyItemLongClickListener {
        public boolean onLongClick(View parent, int position);
    }

    /**
     * 公开点击方法
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

    /**
     * 公开长击方法
     * @param listener
     */
    public void setOnItemLongClickListener(MyItemLongClickListener listener){
        this.mItemLongClickListener = listener;
    }


    /**
     * 加载更多接口
     */
    public interface LoadMoreListener{
        void onLoadMore();
    }
    /**
     * 公开加载更多方法
     */
    public void setLoadMoreListener(LoadMoreListener listener) {
        mListener = listener;
    }


    /***
     * **内部类
     * 给Item布局内的组件赋值
     */
    public class ListHolder extends RecyclerView.ViewHolder {
        //定义点击
        private MyItemClickListener mListener;
        //定义长击
        private MyItemLongClickListener mLongClickListener;

        private ImageView iv_userface ;
        private TextView tv_showname;
        private TextView tv_item_orgname;
        private ImageView iv_item_vip;

        /***
         * 找到组件
         * @param itemView
         */
        public ListHolder(View itemView,MyItemClickListener listener,MyItemLongClickListener longClickListener) {
            super(itemView);

            iv_userface = (ImageView) itemView.findViewById(R.id.item_list_iv_friends);
            tv_showname = (TextView) itemView.findViewById(R.id.tv_item_showname);
            tv_item_orgname = (TextView) itemView.findViewById(R.id.tv_item_orgname);
            iv_item_vip = (ImageView) itemView.findViewById(R.id.iv_item_vip);

            this.mListener = listener;
            this.mLongClickListener = longClickListener;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mLongClickListener != null) {
                        mLongClickListener.onLongClick(v, getAdapterPosition());//getPosition
                    }
                    return true;
                }
            });
        }
        /***
         * 给组件赋值
         */
        public void setData(FriendInfoBean fb, int position) {
            //给图片添加一个身份标识，用于处理被回收的item,被回收的item显示新的图片之前，会显示一下旧的图片。
            String userface = mServiceHead + fb.getUserface()+"?userid="+fb.getUserid();
            iv_userface.setTag(userface);
            ImageLoader.getInstance().displayImage(userface,iv_userface, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular());
            tv_showname.setText(fb.getShowname());
            tv_item_orgname.setText(fb.getOrgname());
            //给积分图标设置身份标识
            iv_item_vip.setTag(fb.getUserid());
            /**
             * vip图标
             */
            if(fb.getVip().equals("0")){
                iv_item_vip.setVisibility(View.GONE);
            }else{
                iv_item_vip.setVisibility(View.VISIBLE);
                iv_item_vip.setImageResource(VipResouce.getVipResouce(fb.getVip()));
            }

        }
    }


    /**
     * 内部类，固化正在加载布局
     */
    class ListHolder_Footer extends RecyclerView.ViewHolder {
        public ListHolder_Footer(View view) {
            super(view);
        }
    }

    /**
     * 设置是否开启加载模式
     * @param autoLoadMore
     */
    public void setAutoLoadMoreEnable(boolean autoLoadMore) {
        mIsFooterEnable = autoLoadMore;
    }


    public void notifyMoreFinish(boolean hasMore) {
        if(mIsFooterEnable){
            //更新item，删除固化的item
            notifyItemRemoved(mLoadMorePosition + 1);
        }
        setAutoLoadMoreEnable(hasMore);
        mIsLoadingMore = false;
    }

}
