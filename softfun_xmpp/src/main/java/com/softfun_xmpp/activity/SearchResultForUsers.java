package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.softfun_xmpp.R;
import com.softfun_xmpp.adapter.ListAdapter_SearchResult;
import com.softfun_xmpp.bean.FriendInfoBean;
import com.softfun_xmpp.bean.ResultBean;
import com.softfun_xmpp.components.RecyclerItemDecoration;
import com.softfun_xmpp.network.HttpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 范张 on 2016-03-16.
 */
public class SearchResultForUsers extends AppCompatActivity  {
    /**
     * 搜索内容
     */
    private String keyword ;


    private RecyclerView mRecyclerView;
    private ListAdapter_SearchResult adapter;
    //private SwipeRefreshLayout mSwipeLayout;
    /**
     * listview的元素布局管理器
     */
    private LinearLayoutManager layoutManager;
    private List<FriendInfoBean> mList = new ArrayList<>();
    private int page = 0;
    private int rows = 20;
    private boolean ismore = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult);

        //给页面设置工具栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_act_searchresult);
        //标题写在setSupportActionBar前面
        toolbar.setTitle(R.string.action_searchuser);
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        keyword = getIntent().getStringExtra("keyword");
        initSearchFriends();
        new queryOracle_Search(false).execute(keyword);

    }

    /**
     * 搜索用户
     */
    private void initSearchFriends() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        //构建数据适配器
        adapter = new ListAdapter_SearchResult(this, mList,mRecyclerView);
        mRecyclerView.setAdapter(adapter);
        //添加条目的分割线
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(this, R.drawable.recycleritemdecoration));
        //添加点击事件
        adapter.setOnItemClickListener(new ListAdapter_SearchResult.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FriendInfoBean fb = mList.get(position);
                if (fb != null) {
                    //进入对方的个人资料，底部有一个按钮，添加好友，点击后，进入一个界面，录入hello，发送
                    Intent intent = new Intent(SearchResultForUsers.this, FriendsProfile.class);
                    intent.putExtra("userid",fb.getUserid());
                    intent.putExtra("flag","add");
                    startActivity(intent);
                }
            }
        });
        //添加长击事件
        adapter.setOnItemLongClickListener(new ListAdapter_SearchResult.MyItemLongClickListener() {
            @Override
            public boolean onLongClick(View parent, int position) {
                FriendInfoBean cb = mList.get(position);
                if (cb != null) {
                }
                return true;
            }
        });

        //mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
       // mSwipeLayout.setOnRefreshListener(this);
        //mSwipeLayout.setRefreshing(false);
        //设置彩色颜色
        //mSwipeLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        //上拉加载更多
        adapter.setLoadMoreListener(new ListAdapter_SearchResult.LoadMoreListener(){
            @Override
            public void onLoadMore() {
                new queryOracle_Search(false).execute(keyword);
            }
        });
    }




//    /**
//     * 下拉刷新
//     */
//    @Override
//    public void onRefresh() {
//        new queryOracle_Search(true).execute(keyword);
//    }


    public class queryOracle_Search extends AsyncTask<String ,Void,ResultBean> {
        boolean mRefresh;
        public queryOracle_Search(boolean refresh) {
            this.mRefresh = refresh;
        }

        @Override
        protected ResultBean doInBackground(String... params) {
            String mKeyword = params[0];
            if(ismore && !mRefresh){
                page = page+1;
            }else{
                page = 1;
            }
            return HttpUtil.okhttpPost_QueryOracle_SearchUsersList(mKeyword, page, rows );
        }

        @Override
        protected void onPostExecute(ResultBean resultBean) {
            super.onPostExecute(resultBean);
            List<FriendInfoBean> list = (List<FriendInfoBean>) resultBean.getDatalist();
            int count = (int) resultBean.getDatamap().get("count");
            if(count==1 && mList.size()==1){
                mRefresh = true;
            }
            if(mRefresh){
                mList.clear();
            }
            if(list.size()>0){
                for (int i = 0; i < list.size(); i++) {
                    FriendInfoBean fb = list.get(i);
                    mList.add(fb);
                }
                adapter.notifyDataSetChanged();

                if(count - (page*rows)>0){
                    adapter.notifyMoreFinish(true);
                    ismore = true;
                }else{
                    adapter.setAutoLoadMoreEnable(false);
                    ismore = false;
                }
            }else{
                ismore = false;
                adapter.notifyMoreFinish(false);
            }
            //mSwipeLayout.setRefreshing(false);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
