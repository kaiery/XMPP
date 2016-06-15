package com.softfun_xmpp.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.OrgBean;

import java.util.List;

/**
 * Created by 范张 on 2016-05-05.
 */
public class RecyclerAdapter_org extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private final List mList;
    /**
     * 上下文
     */
    private Context mContext;

    private LinearLayoutManager layoutManager;
    /**
     * 正在加载的位置
     */
    public int mLoadMorePosition;
    public int lastVisibleItem;
    /**
     * 开关：正在加载
     */
    public boolean mIsLoadingMore;
    /**
     * 开启上拉加载模式
     */
    private boolean mIsFooterEnable;


    /**
     * 加载更多数据 内部变量
     */
    private LoadMoreListener mListener;


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


    /**
     * 内部变量：点击监听器
     */
    private MyItemClickListener mItemClickListener;
    /**
     * 内部变量：长击监听器
     */
    private MyItemLongClickListener mItemLongClickListener;
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















    public RecyclerAdapter_org(RecyclerView rv, List list, Context context) {


        layoutManager = (LinearLayoutManager) rv.getLayoutManager();
        this.mContext = context;
        this.mList = list;

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * 重写监听滚动状态变化事件
             * @param recyclerView 组件
             * @param newState 状态
             */
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
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
                }
            }
            /**
             * 重写监听滚动事件
             * @param recyclerView 组件
             * @param dx    x
             * @param dy    y
             */
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //上拉加载代码
                if(!mIsLoadingMore && dy > 0 && mIsFooterEnable){
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                }
                //上拉加载代码end


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
            v = LayoutInflater.from(mContext).inflate(R.layout.item_layout_org, parent, false);
            return new ListHolder(v,mItemClickListener,mItemLongClickListener);
        }else if(viewType == TYPE_FOOTER){
            v = LayoutInflater.from(mContext).inflate(R.layout.item_layout_search_friends_footerview, parent, false);
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
            ((ListHolder)holder).setData((OrgBean) mList.get(position),position);
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
        if(mList!=null){
            int count = mList.size();
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



    /***
     * **内部类
     * 给Item布局内的组件赋值
     */
    public class ListHolder extends RecyclerView.ViewHolder {
        //定义点击
        private MyItemClickListener mListener;
        //定义长击
        private MyItemLongClickListener mLongClickListener;

        private ImageView iv;
        private TextView tv_item_orgname;

        /***
         * 找到组件
         * @param itemView
         */
        public ListHolder(View itemView,MyItemClickListener listener,MyItemLongClickListener longClickListener) {
            super(itemView);

            iv = (ImageView) itemView.findViewById(R.id.item_list_iv);
            tv_item_orgname = (TextView) itemView.findViewById(R.id.tv_item_orgname);

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
        public void setData(OrgBean orgBean, int position) {
            tv_item_orgname.setText(orgBean.getOrgname());
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
