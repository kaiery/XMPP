package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softfun_xmpp.R;


/**
 * Created by 范张 on 2016-02-19.
 */
public class AddFriends extends AppCompatActivity implements View.OnClickListener {


    private EditText et_search;
    private Button bt_clear_search_text;
    private LinearLayout ll_add_friends_findfriend;
    private LinearLayout ll_add_friends_findgroup;
    private TextView tv_add_friends_findfriend_text;
    private TextView tv_add_friends_findgroup_text;
    private View line_add_friends;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //过渡效果
        setContentView(R.layout.activity_add_friends);

        et_search = (EditText) findViewById(R.id.et_search);
        bt_clear_search_text = (Button) findViewById(R.id.bt_clear_search_text);
        ll_add_friends_findfriend = (LinearLayout) findViewById(R.id.ll_add_friends_findfriend);
        ll_add_friends_findgroup = (LinearLayout) findViewById(R.id.ll_add_friends_findgroup);
        tv_add_friends_findfriend_text = (TextView) findViewById(R.id.tv_add_friends_findfriend_text);
        tv_add_friends_findgroup_text = (TextView) findViewById(R.id.tv_add_friends_findgroup_text);
        line_add_friends = findViewById(R.id.line_add_friends);
        //给页面设置工具栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_act_addfriend);
        //标题写在setSupportActionBar前面
        toolbar.setTitle(R.string.action_add);
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initSearch();
    }

    /**
     * 初始化搜索组件
     */
    private void initSearch() {
        bt_clear_search_text.setOnClickListener(this);
        ll_add_friends_findfriend.setOnClickListener(this);
        ll_add_friends_findgroup.setOnClickListener(this);

        //设置文本改变监听器
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int textLength = et_search.getText().length();
                if (textLength > 0) {
                    bt_clear_search_text.setVisibility(View.VISIBLE);
                    ll_add_friends_findfriend.setVisibility(View.VISIBLE);
                    ll_add_friends_findgroup.setVisibility(View.VISIBLE);
                    line_add_friends.setVisibility(View.VISIBLE);
                    tv_add_friends_findfriend_text.setText(et_search.getText().toString());
                    tv_add_friends_findgroup_text.setText(et_search.getText().toString());
                } else {
                    bt_clear_search_text.setVisibility(View.GONE);
                    ll_add_friends_findfriend.setVisibility(View.GONE);
                    ll_add_friends_findgroup.setVisibility(View.GONE);
                    line_add_friends.setVisibility(View.GONE);
                    tv_add_friends_findfriend_text.setText("");
                    tv_add_friends_findgroup_text.setText("");
                }

            }
        });

//        et_search.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_ENTER) {
//                    Toast.makeText(AddFriends.this,
//                            et_search.getText().toString().trim(),
//                            Toast.LENGTH_LONG).show();
//                }
//                return false;
//            }
//        });
    }


    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_clear_search_text: {
                et_search.setText("");
                bt_clear_search_text.setVisibility(View.GONE);
                break;
            }
            case R.id.ll_add_friends_findfriend:
                if (!TextUtils.isEmpty(et_search.getText().toString().trim())) {
                    Intent intent = new Intent(AddFriends.this,SearchResultForUsers.class);
                    intent.putExtra("type","0");
                    intent.putExtra("keyword",et_search.getText().toString());
                    startActivity(intent);
                }
                break;
            case R.id.ll_add_friends_findgroup:
                if (!TextUtils.isEmpty(et_search.getText().toString().trim())) {
                    Intent intent = new Intent(AddFriends.this,SearchResultForUsers.class);
                    intent.putExtra("type","1");
                    intent.putExtra("keyword",et_search.getText().toString());
                    startActivity(intent);
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.add_friends_menu, menu);
        return true;
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
    protected void onPause() {
        super.onPause();
    }


}
