package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

public class EditItemActivity extends AppCompatActivity implements View.OnClickListener {


    public static final String ITEM_NAME = "itemname";
    public static final String ITEM_VALUE = "itemvalue";
    public static final String ITEM_FIELD = "item_field";
    public static final int RESULT_SUCCEED = 1;
    public static final int RESULT_FAILURE = 0;
    public static final int REQUESTCODE = 10;
    public static final String MAX_LENGTH = "max_lenght";
    public String mItemField = "";


    private Toolbar mToolbar;
    private TextView mTv;
    private Button mBtn;
    private EditText mEt;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTv = (TextView) findViewById(R.id.tv);
        mBtn = (Button) findViewById(R.id.btn);
        mEt = (EditText) findViewById(R.id.et);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        assignViews();

        Intent intent = getIntent();
        String itemname = intent.getStringExtra(ITEM_NAME);
        String itemvalue = intent.getStringExtra(ITEM_VALUE);
        mItemField = intent.getStringExtra(ITEM_FIELD);
        int mMaxLenght = intent.getIntExtra(MAX_LENGTH, 0);

        mTv.setText(itemname);
        mEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLenght)});
        mEt.setText(itemvalue);
        mBtn.setOnClickListener(this);


        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar bar = getSupportActionBar();
        if(bar!=null){
            bar.setDisplayHomeAsUpEnabled(true);
        }
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
        if(v.getId()==R.id.btn){
            if(!TextUtils.isEmpty(mEt.getText().toString().trim())){
                final Map<String , String> map = new HashMap<>();
                map.put(mItemField,mEt.getText().toString().trim());
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        String username = IMService.mCurAccount.substring(0,IMService.mCurAccount.lastIndexOf("@"));
                        HttpUtil.okhttpPost_updateVcard(map,username);
                        if(mItemField.equals(Const.QQ)){
                            IMService.mCurQq = mEt.getText().toString().trim();
                        }
                        if(mItemField.equals(Const.NICKNAME)){
                            IMService.mCurNickName = mEt.getText().toString().trim();
                        }
                        if(mItemField.equals(Const.EMAIL)){
                            IMService.mCurEmail = mEt.getText().toString().trim();
                        }
                        if(mItemField.equals(Const.USERDESC)){
                            IMService.mCurDesc = mEt.getText().toString().trim();
                        }

                        AsmackUtils.setVcardInfo();

                        Intent data = new Intent();
                        data.putExtra(ITEM_FIELD, mItemField);
                        data.putExtra(ITEM_VALUE, mEt.getText().toString().trim());
                        setResult(RESULT_SUCCEED, data);
                        finish();//关闭当前的界面  ★一定记得关闭界面,数据才会被返回
                    }
                });

            }
        }
    }
}
