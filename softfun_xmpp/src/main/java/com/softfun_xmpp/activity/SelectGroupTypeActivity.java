package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.softfun_xmpp.R;
import com.softfun_xmpp.constant.Const;

public class SelectGroupTypeActivity extends AppCompatActivity implements View.OnClickListener {


    private Toolbar mToolbar;
    private LinearLayout mItem1;
    private LinearLayout mItem2;
    private LinearLayout mItem3;
    private LinearLayout mItem4;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mItem1 = (LinearLayout) findViewById(R.id.item1);
        mItem2 = (LinearLayout) findViewById(R.id.item2);
        mItem3 = (LinearLayout) findViewById(R.id.item3);
        mItem4 = (LinearLayout) findViewById(R.id.item4);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_type);

        assignViews();

        mToolbar.setTitle("添加群组");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }


        mItem1.setOnClickListener(this);
        mItem2.setOnClickListener(this);
        mItem3.setOnClickListener(this);
        mItem4.setOnClickListener(this);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,InputGroupInfoActivity.class);
        switch( v.getId() )
        {
            case R.id.item1:
                intent.putExtra(Const.GROUPTYPE,Const.GROUP_NORMAL);
                startActivity(intent);
                break;
            case R.id.item2:
                intent.putExtra(Const.GROUPTYPE,Const.GROUP_GAME);
                startActivity(intent);
                break;
            case R.id.item3:
                intent.putExtra(Const.GROUPTYPE,Const.GROUP_CITY);
                startActivity(intent);
                break;
            case R.id.item4:
                intent.putExtra(Const.GROUPTYPE,Const.GROUP_SECRET);
                startActivity(intent);
                break;
        }
    }
}
