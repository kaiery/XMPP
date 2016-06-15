package com.softfun_xmpp.components;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.softfun_xmpp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 范张 on 2016-04-23.
 */
public class RefreshListView extends ListView {

    private final int PULL_DOWN = 1;//下拉状态
    private final int RELEASE_STATE = 2;//松开刷新状态
    private final int REFRESHING = 3;//正在刷新状态
    private final int REFRESHED = 4;//刷新完成状态
    private int mCurrentstate = PULL_DOWN; //当前状态

    /**
     * 内部监听器
     */
    private OnRefreshDataListener refreshDataListener;

    private View otherView;
    /**
     * 整个尾布局
     */
    private View footer;
    /**
     * 整个头布局
     */
    private LinearLayout head;
    /**
     * 刷新头的布局
     */
    private LinearLayout ll_listview_refresh_head;
    /**
     * 获取测量后刷新头组件的高度
     */
    private int measuredHeight_header;
    /**
     * 获取测量后尾部组件的高度
     */
    private int measuredHeight_footer;
    /**
     * 头布局中额外的组件高度
     */
    private int otherMeasuredHeight;
    private float downY = -1;
    /**
     * listview在屏幕中的Y坐标
     */
    private int listviewOnScreenY;
    private boolean bb;
    private TextView desc;
    private TextView time;
    private ImageView arrow;
    private ProgressBar pb;
    private RotateAnimation up_ra;
    private RotateAnimation down_ra;
    /**
     * 是否是加载更多
     */
    private boolean isLoadMore;
    /**
     * 是否【开启上拉加载更多】
     */
    private boolean LoadMoreEnable;

    /**
     * 是否【开启上拉加载更多】
     */
    public void setLoadMoreEnable(boolean loadMoreEnable) {
        LoadMoreEnable = loadMoreEnable;
    }

    /**
     * 下拉阻尼
     */
    private static float OFFSET_RADIO = 1.8f;

    private String headHintText_pull;//文字：下拉刷新
    private String headHintText_release;//文字：松开刷新
    private String headHintText_refreshing;//文字：正在刷新数据
    private String headHintText_refreshed;//文字：数据刷新完成
    public String getHeadHintText_pull() {
        return headHintText_pull;
    }
    public void setHeadHintText_pull(String headHintText_pull) {
        this.headHintText_pull = headHintText_pull;
    }
    public String getHeadHintText_release() {
        return headHintText_release;
    }
    public void setHeadHintText_release(String headHintText_release) {
        this.headHintText_release = headHintText_release;
    }
    public String getHeadHintText_refreshing() {
        return headHintText_refreshing;
    }
    public void setHeadHintText_refreshing(String headHintText_refreshing) {
        this.headHintText_refreshing = headHintText_refreshing;
    }
    public String getHeadHintText_refreshed() {
        return headHintText_refreshed;
    }
    public void setHeadHintText_refreshed(String headHintText_refreshed) {
        this.headHintText_refreshed = headHintText_refreshed;
    }


    public RefreshListView(Context context) {
        this(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initAnimation();
        initEvent();
        if(this.headHintText_pull ==null){
            this.headHintText_pull = "下拉刷新";
        }
        if(this.headHintText_release ==null){
            this.headHintText_release = "松开刷新";
        }
        if(this.headHintText_refreshing ==null){
            this.headHintText_refreshing = "正在刷新数据";
        }
        if(this.headHintText_refreshed ==null){
            this.headHintText_refreshed = "数据刷新完成";
        }
    }

    /**
     * 添加滑动事件
     */
    private void initEvent() {
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //状态停止时，如果listview显示最后一条数据的时候。加载更多
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (LoadMoreEnable) {
                        if (getLastVisiblePosition() == getAdapter().getCount() - 1 && !isLoadMore) {
                            //如果是最后一条数据，就显示footer
                            footer.setPadding(0, 0, 0, 0);
                            //进一步定位到最后一条，省去了再次拖动一下
                            setSelection(getAdapter().getCount());
                            isLoadMore = true;
                            if (refreshDataListener != null) {
                                refreshDataListener.LoadMore();
                            }
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }


    private void initView() {
        initFooter();
        initHead();
    }

    /**
     * 完成自己的触摸事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //需要我们的功能时，屏蔽掉父类的touch事件
        //下拉时，并且当listview显示第一条数据的时候，这时候屏蔽
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getRawY();//按下时的Y坐标
                //------------------
                int[] location_this = new int[2];
                this.getLocationOnScreen(location_this);
                listviewOnScreenY = location_this[1];
                if (otherView != null) {
                    int[] location_other = new int[2];
                    otherView.getLocationOnScreen(location_other);
                    if (listviewOnScreenY == location_other[1]) {
                        bb = true;
                    } else {
                        bb = false;
                    }
                } else {
                    int[] location_header = new int[2];
                    head.getLocationOnScreen(location_header);
                    if (listviewOnScreenY == location_header[1]) {
                        bb = true;
                    } else {
                        bb = false;
                    }
                }
                //------------------
                break;

            case MotionEvent.ACTION_MOVE: {
                //如果额外的头布局没有完全显示，就不相应自己的触摸事件
                //判断额外的头布局是否完全显示
                //取listview在屏幕的坐标  和 额外的头布局在屏幕中的坐标
                int[] location = new int[2];
                if (listviewOnScreenY == 0) {
                    this.getLocationOnScreen(location);
                    listviewOnScreenY = location[1];
                }
                if (otherView != null) {
                    otherView.getLocationOnScreen(location);
                    if (location[1] < listviewOnScreenY) {
                        //额外的头布局没有完全显示
                        //继续响应listview的触摸事件
                        break;
                    }
                } else {
                    head.getLocationOnScreen(location);
                    if (location[1] < listviewOnScreenY) {
                        //额外的头布局没有完全显示
                        //继续响应listview的触摸事件
                        break;
                    }
                }
            }


            if (downY == -1) {
                downY = ev.getRawY();
            }
            float moveY = ev.getRawY();
            float dy = moveY - downY;
            //下拉时，并且当listview显示第一条数据的时候，这时候屏蔽,处理自己的事件
            if (dy > 0 && getFirstVisiblePosition() == 0 && bb) {
                //当前pading top 的参数值
                float scrollYdis = -measuredHeight_header + dy;
                if (scrollYdis < 0 && mCurrentstate != PULL_DOWN) {
                    mCurrentstate = PULL_DOWN;
                    refreshState();
                } else if (scrollYdis >= 0 && mCurrentstate != RELEASE_STATE) {
                    mCurrentstate = RELEASE_STATE;
                    refreshState();
                }
                if(scrollYdis>10){
                    head.setPadding(0, (int) (scrollYdis/OFFSET_RADIO), 0, 0);
                }else{
                    head.setPadding(0, (int) (scrollYdis), 0, 0);
                }

                return true;
            }
            break;
            case MotionEvent.ACTION_UP:
                //重置
                downY = -1;
                //重置
                bb = false;
                //如果PULL_DOWN状态，松开后恢复原状
                if (mCurrentstate == PULL_DOWN) {
                    head.setPadding(0, -measuredHeight_header, 0, 0);
                } else if (mCurrentstate == RELEASE_STATE) {
                    //刷新数据
                    head.setPadding(0, 0, 0, 0);
                    mCurrentstate = REFRESHING;//将状态改变为正在刷新数据的状态
                    refreshState();
                    //真正刷新数据，adapter
                    if (refreshDataListener != null) {
                        refreshDataListener.RefreshData();
                    }
                }

                break;
        }
        return super.onTouchEvent(ev);
    }


    public interface OnRefreshDataListener {
        void RefreshData();

        void LoadMore();
    }

    public void setRefreshDataListener(OnRefreshDataListener refreshDataListener) {
        this.refreshDataListener = refreshDataListener;
    }


    /**
     * 初始化动画
     */
    private void initAnimation() {
        up_ra = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        up_ra.setDuration(200);
        up_ra.setFillAfter(true);//动画结束状态保持

        down_ra = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        down_ra.setDuration(200);
        down_ra.setFillAfter(true);//动画结束状态保持
    }


    /**
     * 状态改变
     */
    private void refreshState() {
        switch (mCurrentstate) {
            case PULL_DOWN://下拉刷新
                //改变文字
                desc.setText(headHintText_pull);
                desc.setVisibility(VISIBLE);
                arrow.startAnimation(down_ra);
                pb.setVisibility(INVISIBLE);
                arrow.setVisibility(VISIBLE);
                break;
            case RELEASE_STATE://松开刷新
                desc.setText(headHintText_release);
                arrow.startAnimation(up_ra);
                break;
            case REFRESHING:
                arrow.clearAnimation();
                arrow.setVisibility(INVISIBLE);
                pb.setVisibility(VISIBLE);
                desc.setText(headHintText_refreshing);
                break;
            case REFRESHED:
                arrow.clearAnimation();
                arrow.setVisibility(VISIBLE);
                arrow.setImageResource(R.drawable.ic_refreshfinished);
                pb.setVisibility(INVISIBLE);
                desc.setText(headHintText_refreshed);
                break;
        }
    }


    /**
     * 刷新数据成功，
     */
    public void refreshFinish() {
        if (isLoadMore) {
            isLoadMore = false;
            footer.setPadding(0, 0, 0, -measuredHeight_footer);
        } else {
            mCurrentstate = this.REFRESHED;
            refreshState();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    arrow.clearAnimation();
                    desc.setText(headHintText_pull);
                    pb.setVisibility(INVISIBLE);
                    arrow.setVisibility(VISIBLE);
                    arrow.setImageResource(R.drawable.ic_arrowdown);
                    SimpleDateFormat formDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
                    time.setText(formDate.format(new Date()));
                    time.setVisibility(VISIBLE);
                    head.setPadding(0, -measuredHeight_header, 0, 0);
                    mCurrentstate = PULL_DOWN;
                }
            }, 1000);
        }
    }

    /**
     * 初始化尾部
     */
    private void initFooter() {
        footer = View.inflate(getContext(), R.layout.listview_refresh_footer, null);
        //测量
        footer.measure(0, 0);
        measuredHeight_footer = footer.getMeasuredHeight();
        footer.setPadding(0, 0, 0, -measuredHeight_footer);
        //加载到listview中
        addFooterView(footer);
    }

    /**
     * 初始化头部
     */
    private void initHead() {
        head = (LinearLayout) View.inflate(getContext(), R.layout.listview_refresh_container, null);
        //刷新头的布局
        ll_listview_refresh_head = (LinearLayout) head.findViewById(R.id.ll_listview_refresh_head);
        //获取子组件
        desc = (TextView) ll_listview_refresh_head.findViewById(R.id.tv_listview_head_state_dec);
        time = (TextView) ll_listview_refresh_head.findViewById(R.id.tv_listview_head_refresh_time);
        arrow = (ImageView) ll_listview_refresh_head.findViewById(R.id.iv_listview_head_arrow);
        pb = (ProgressBar) ll_listview_refresh_head.findViewById(R.id.pb_listview_head_loading);
        time.setText("");
        desc.setText(headHintText_pull);
        //获取刷新头组件的高度
        //测量
        ll_listview_refresh_head.measure(0, 0);
        //获取测量后的高度
        measuredHeight_header = ll_listview_refresh_head.getMeasuredHeight();
        //隐藏刷新头布局，额外布局不影响
        head.setPadding(0, -measuredHeight_header, 0, 0);

        //加载到listview中
        addHeaderView(head);
    }

    /**
     * 加载额外的头组件
     *
     * @param view
     */
    public void addHeaderViewEx(View view) {
        if (head != null) {
            otherView = view;
            //测量
            view.measure(0, 0);
            //获取
            otherMeasuredHeight = view.getMeasuredHeight();
            head.addView(view);
        }
    }
}
