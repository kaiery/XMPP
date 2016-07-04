package com.softfun_xmpp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.softfun_xmpp.R;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.StringUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

public class ForgetPasswordActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText mEtUsername;
    private Toolbar mToolbar;
//    private EditText mEtPhone;
    private Button mBtCheckma;
    private EditText mEtCheckma;
    private Button mBtn;
    private String yzm;
    private int recLen = 60;
    private Handler handler = new Handler();

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mEtPhone = (EditText) findViewById(R.id.et_phone);
        mBtCheckma = (Button) findViewById(R.id.bt_checkma);
        mEtCheckma = (EditText) findViewById(R.id.et_checkma);
        mBtn = (Button) findViewById(R.id.btn);
        mEtUsername = (EditText) findViewById(R.id.et_username);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        assignViews();

        mToolbar.setTitle("密码找回");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBtn.setOnClickListener(this);
        mBtCheckma.setOnClickListener(this);
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
        switch(v.getId())
        {
            case R.id.btn:
                if(StringUtils.isEmpty( mEtUsername.getText().toString().trim()   )){
                    mEtUsername.setError("请输入帐号信息");
                    return;
                }
//                else
//                if(StringUtils.isEmpty(mEtPhone.getText().toString().trim() )){
//                    mEtPhone.setError("请输入电话号码");
//                    return;
//                }
                else if(StringUtils.isEmpty(mEtCheckma.getText().toString().trim() )){
                    mEtCheckma.setError("请输入验证码");
                    return;
                }
                else if( !mEtCheckma.getText().toString().equals(yzm)){
                    mEtCheckma.setError("验证码不正确");
                    return;
                }
                final String username = mEtUsername.getText().toString().trim();
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        int code = HttpUtil.okhttpPost_forgetPassword(username);
                        if(code==1){
                            finish();
                        }else{
                            ToastUtils.showToastSafe("密码重置失败。");
                        }
                    }
                });
                break;

            case R.id.bt_checkma:
                if(StringUtils.isEmpty( mEtUsername.getText().toString().trim()   )){
                    mEtUsername.setError("请输入帐号信息");
                    return;
                }else{
                    handler.postDelayed(runnable, 1000);
                    //获取验证码
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            final String username = mEtUsername.getText().toString().trim();
                            yzm = HttpUtil.okhttpPost_sendsms_foundPwd(username);
                        }
                    });
                }
                break;
        }
    }



    /**
     * 倒计时线程
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            recLen--;
            if(recLen==0){
                recLen = 60;
                mBtCheckma.setText("发送验证码");
                mBtCheckma.setEnabled(true);
            }else{
                mBtCheckma.setEnabled(false);
                mBtCheckma.setText("剩余(" + recLen+")");
                handler.postDelayed(this, 1000);
            }
        }
    };


}
