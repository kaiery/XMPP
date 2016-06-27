package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.UserBean;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.StringUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RESULT_SUCCEED = 1;
    public static final int RESULT_FAILURE = 0;
    private Toolbar mToolbar;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtPassword1;
    private EditText mEtCheckma;
    private EditText mEtPhone;
    private Button mBtSignup;
    private Button mBtCheckma;
    private EditText mEtNickname;
    private String yzm;
    private int recLen = 60;
    private Handler handler = new Handler();


    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mEtUsername = (EditText) findViewById(R.id.et_username);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mEtPassword1 = (EditText) findViewById(R.id.et_password1);
        mEtPhone = (EditText) findViewById(R.id.et_phone);
        mEtPhone = (EditText) findViewById(R.id.et_phone);
        mBtSignup = (Button) findViewById(R.id.bt_signup);
        mBtCheckma = (Button) findViewById(R.id.bt_checkma);
        mEtNickname = (EditText) findViewById(R.id.et_nickname);
        mEtCheckma = (EditText) findViewById(R.id.et_checkma);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        assignViews();

        mToolbar.setTitle("注册");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        mBtSignup.setOnClickListener(this);
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


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.bt_checkma:
                if(StringUtils.isEmpty( mEtPhone.getText().toString().trim()   )){
                    mEtPhone.setError("请输入电话号码");
                    return;
                }else{
                    handler.postDelayed(runnable, 1000);
                    //获取验证码
                    ThreadUtils.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            yzm = HttpUtil.okhttpPost_sendsms(mEtPhone.getText().toString().trim());
                        }
                    });
                }
                break;
            case R.id.bt_signup:
                if(StringUtils.isEmpty( mEtUsername.getText().toString().trim()   )){
                    mEtUsername.setError("请输入帐号信息");
                    return;
                }else if(StringUtils.isEmpty(mEtPassword.getText().toString().trim() )){
                    mEtPassword.setError("请输入密码信息");
                    return;
                }else if(StringUtils.isEmpty(mEtPhone.getText().toString().trim() )){
                    mEtPhone.setError("请输入电话号码");
                    return;
                }else if(StringUtils.isEmpty(mEtNickname.getText().toString().trim() )){
                    mEtNickname.setError("请输入昵称");
                    return;
                }
                else if(StringUtils.isEmpty(mEtCheckma.getText().toString().trim() )){
                    mEtCheckma.setError("请输入验证码");
                    return;
                }
                else if( !mEtCheckma.getText().toString().equals(yzm)){
                    mEtCheckma.setError("验证码不正确");
                    return;
                }



                final UserBean userBean = new UserBean();
                userBean.setUsername(mEtUsername.getText().toString().trim());
                userBean.setPassword(mEtPassword.getText().toString().trim());
                userBean.setPhone(mEtPhone.getText().toString().trim());
                userBean.setNickname(mEtNickname.getText().toString().trim());
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        int code = HttpUtil.okhttpPost_registUser(userBean);//ConnManager.getInstance().regist(userBean);
                        if(code==1){
                            Intent data = new Intent();
                            data.putExtra(Const.USERNAME, mEtUsername.getText().toString().trim());
                            data.putExtra(Const.PASSWORD, mEtPassword.getText().toString().trim());
                            setResult(RESULT_SUCCEED, data);
                            finish();//关闭当前的界面  ★一定记得关闭界面,数据才会被返回
                        }else if(code == 409){
                            ToastUtils.showToastSafe("用户已被注册");
                        }else{
                            ToastUtils.showToastSafe("注册失败");
                        }
                    }
                });
                break;
        }
    }
}
