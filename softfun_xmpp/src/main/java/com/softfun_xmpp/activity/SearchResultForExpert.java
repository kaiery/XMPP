package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.softfun_xmpp.R;
import com.softfun_xmpp.adapter.RecyclerAdapter_user;
import com.softfun_xmpp.bean.ResultBean;
import com.softfun_xmpp.bean.UserBean;
import com.softfun_xmpp.components.RecyclerItemDecoration;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 范张 on 2016-03-16.
 */
public class SearchResultForExpert extends AppCompatActivity {
    /**
     * 搜索内容
     */
    private String keyword ;


    private RecyclerView mRecyclerView;
    private RecyclerAdapter_user adapter;
    /**
     * listview的元素布局管理器
     */
    private LinearLayoutManager layoutManager;
    private List<UserBean> mList = new ArrayList<>();
    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        //给页面设置工具栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_act_searchresult);
        //标题写在setSupportActionBar前面
        toolbar.setTitle(R.string.action_searchuser);
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        keyword = getIntent().getStringExtra("keyword");
        type = getIntent().getStringExtra("type");
        initData();
    }

    /**
     * 搜索用户
     */
    private void initData() {

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        //构建数据适配器
        adapter = new RecyclerAdapter_user(mRecyclerView,mList,this);
        mRecyclerView.setAdapter(adapter);
        //添加条目的分割线
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(this, R.drawable.recycleritemdecoration));
        //添加点击事件
        adapter.setOnItemClickListener(new RecyclerAdapter_user.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                UserBean userBean = mList.get(position);
                if (userBean != null) {
                    Intent intent = new Intent(SearchResultForExpert.this,ExpertProfileActivity.class);
                    intent.putExtra("username",userBean.getUsername());
                    intent.putExtra("userid",userBean.getUserid());
                    startActivity(intent);
                }
            }
        });
        //添加长击事件
        adapter.setOnItemLongClickListener(new RecyclerAdapter_user.MyItemLongClickListener() {
            @Override
            public boolean onLongClick(View parent, int position) {
                UserBean cb = mList.get(position);
                if (cb != null) {
                }
                return true;
            }
        });

        adapter.mIsLoadingMore = true;

        queryData();
    }



    private void queryData() {
        //姓名搜索
        if(type.equals("0")){
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    ResultBean resultBean = HttpUtil.okhttpPost_queryRemoteExpertbyshowname(keyword);
                    List<UserBean> list = (List<UserBean>) resultBean.getDatalist();
                    if(list.size()>0){
                        if(mList==null){
                            mList = new ArrayList<>();
                        }
                        mList.clear();
                        for (int i = 0; i < list.size(); i++) {
                            UserBean userBean = list.get(i);
                            mList.add(userBean);
                        }

                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }else{
            //特长搜索
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    ResultBean resultBean = HttpUtil.okhttpPost_queryRemoteExpertbytechang(keyword);
                    List<UserBean> list = (List<UserBean>) resultBean.getDatalist();
                    if(list.size()>0){
                        if(mList==null){
                            mList = new ArrayList<>();
                        }
                        mList.clear();
                        for (int i = 0; i < list.size(); i++) {
                            UserBean userBean = list.get(i);
                            mList.add(userBean);
                        }

                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }

    }


//    public class queryOracle_Search extends AsyncTask<String ,Void,ResultBean> {
//
//        @Override
//        protected ResultBean doInBackground(String... params) {
//            String mKeyword = params[0];
//           return HttpUtil.okhttpPost_queryRemoteExpertbyshowname(mKeyword );
//        }
//
//        @Override
//        protected void onPostExecute(ResultBean resultBean) {
//            super.onPostExecute(resultBean);
//
//            List<UserBean> list = (List<UserBean>) resultBean.getDatalist();
//            if(list.size()>0){
//                if(mList==null){
//                    mList = new ArrayList<>();
//                }
//                mList.clear();
//                for (int i = 0; i < list.size(); i++) {
//                    UserBean userBean = list.get(i);
//                    mList.add(userBean);
//                }
//                adapter.notifyDataSetChanged();
//            }
//        }
//    }



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
