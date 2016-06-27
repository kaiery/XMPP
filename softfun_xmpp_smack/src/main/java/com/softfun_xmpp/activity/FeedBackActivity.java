package com.softfun_xmpp.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.softfun_xmpp.R;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

public class FeedBackActivity extends AppCompatActivity implements View.OnClickListener {


    private Toolbar mToolbar;
    private EditText mEtContent;
    private EditText mEtContact;
    private Button mBtn;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mEtContact = (EditText) findViewById(R.id.et_contact);
        mBtn = (Button) findViewById(R.id.btn);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        assignViews();


        mToolbar.setTitle("意见反馈");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBtn.setOnClickListener(this);
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
        switch( v.getId() )
        {
            case R.id.btn:
                if(TextUtils.isEmpty(mEtContent.getText().toString().trim())){
                    mEtContent.setError("您的意见或反馈还没填写呢");
                    return;
                }
                final String content = mEtContent.getText().toString();
                final String contact = mEtContact.getText().toString();
                final String account = IMService.mCurAccount.substring(0, IMService.mCurAccount.lastIndexOf("@"));
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        HttpUtil.okhttpPost_insertFeedBack(account  , content,contact);
                        ToastUtils.showToastSafe("感谢您的反馈");
                        finish();
                    }
                });
                break;
        }
    }
}
