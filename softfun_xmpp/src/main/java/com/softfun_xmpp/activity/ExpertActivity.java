package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.softfun_xmpp.R;
import com.softfun_xmpp.adapter.RecyclerAdapter_org;
import com.softfun_xmpp.bean.OrgBean;
import com.softfun_xmpp.bean.ResultBean;
import com.softfun_xmpp.components.RecyclerItemDecoration;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpertActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRv;
    private Button mBtn_search;
    private Button mBtn_superior;
    private List<OrgBean> datalist  = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private RecyclerAdapter_org adapter;
    private String ExpertOrgid;
    private String parentorg;
    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert);

        mRv = (RecyclerView) findViewById(R.id.rv);
        mBtn_search = (Button) findViewById(R.id.btn_search);
        mBtn_superior = (Button) findViewById(R.id.btn_superior);

        mBtn_search.setOnClickListener(this);
        mBtn_superior.setOnClickListener(this);

        //给页面设置工具栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_act_searchresult);
        //标题写在setSupportActionBar前面
        toolbar.setTitle("机构列表");
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initData();
    }

    private void initData() {
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRv.setLayoutManager(layoutManager);
        mRv.setHasFixedSize(true);
        //构建数据适配器
        adapter = new RecyclerAdapter_org(mRv,datalist,ExpertActivity.this);
        mRv.setAdapter(adapter);
        //添加条目的分割线
        mRv.addItemDecoration(new RecyclerItemDecoration(this, R.drawable.recycleritemdecoration));
        //添加点击事件
        adapter.setOnItemClickListener(new RecyclerAdapter_org.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                OrgBean orgBean = datalist.get(position);
                if (orgBean != null) {
                    ExpertOrgid = orgBean.getOrgid();
                    parentorg = orgBean.getParentorg();
                    queryData(ExpertOrgid);
                }
            }
        });
        //添加长击事件
        adapter.setOnItemLongClickListener(new RecyclerAdapter_org.MyItemLongClickListener() {
            @Override
            public boolean onLongClick(View parent, int position) {
                return true;
            }
        });


        queryData("2016061715224837265");//农技110的机构id
    }

    public void queryData(final String orgid){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                ResultBean resultBean = HttpUtil.okhttpPost_queryNj110OrgFlat(orgid);
                List<OrgBean> list = (List<OrgBean>) resultBean.getDatalist();
                if(list.size()>0){
                    flag++;
                    if(datalist==null){
                        datalist = new ArrayList<>();
                    }
                    datalist.clear();
                    for (int i = 0; i < list.size(); i++) {
                        OrgBean orgBean = list.get(i);
                        datalist.add(orgBean);
                    }

                    ThreadUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }else{
                    ThreadUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String orgid = ExpertOrgid;
                            if(flag>0){
                                ExpertOrgid = parentorg;
                            }else{
                                ExpertOrgid = "";
                            }
                            flag = 0;
                            enterExpertListActivity(orgid);
                        }
                    });
                }
            }
        });
    }

    private void enterExpertListActivity(String orgid) {
        Intent intent = new Intent(ExpertActivity.this,ExpertListActivity.class);
        intent.putExtra("orgid",orgid);
        startActivity(intent);
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

    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.btn_superior:
                querySuperiorData();
                break;
            case R.id.btn_search:
                Intent intent = new Intent(this,ExpertSearchActiviy.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 查询上一级机构
     */
    private void querySuperiorData() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                ResultBean resultBean = HttpUtil.okhttpPost_queryNj110ParentOrgFlat(ExpertOrgid);
                List<OrgBean> list = (List<OrgBean>) resultBean.getDatalist();
                if(list.size()>0){
                    if(datalist==null){
                        datalist = new ArrayList<>();
                    }
                    datalist.clear();
                    for (int i = 0; i < list.size(); i++) {
                        OrgBean orgBean = list.get(i);
                        datalist.add(orgBean);
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
