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

public class ExpertListActivity extends AppCompatActivity {
    private RecyclerView mRv;
    private List<UserBean> datalist  = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private RecyclerAdapter_user adapter;
    private String mOrgid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_list);

        mRv = (RecyclerView) findViewById(R.id.rv);
        //给页面设置工具栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_act_searchresult);
        //标题写在setSupportActionBar前面
        toolbar.setTitle("专家列表");
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mOrgid = getIntent().getStringExtra("orgid");

        initData();
    }

    private void initData() {
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRv.setLayoutManager(layoutManager);
        mRv.setHasFixedSize(true);
        //构建数据适配器
        adapter = new RecyclerAdapter_user(mRv,datalist,ExpertListActivity.this);
        mRv.setAdapter(adapter);
        //添加条目的分割线
        mRv.addItemDecoration(new RecyclerItemDecoration(this, R.drawable.recycleritemdecoration));
        //添加点击事件
        adapter.setOnItemClickListener(new RecyclerAdapter_user.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                UserBean userBean = datalist.get(position);
                if (userBean != null) {
                    Intent intent = new Intent(ExpertListActivity.this,ExpertProfileActivity.class);
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
                return true;
            }
        });

        adapter.mIsLoadingMore = true;
        queryData();
    }


    private void queryData() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                if(mOrgid==null){
                    return;
                }
                ResultBean resultBean = HttpUtil.okhttpPost_queryNj110ZJList(mOrgid);
                List<UserBean> list = (List<UserBean>) resultBean.getDatalist();
                if(list.size()>0){
                    if(datalist==null){
                        datalist = new ArrayList<>();
                    }
                    datalist.clear();
                    for (int i = 0; i < list.size(); i++) {
                        UserBean userBean = list.get(i);
                        datalist.add(userBean);
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
