package com.softfun_xmpp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.softfun_xmpp.R;
import com.softfun_xmpp.baseActivity.BaseNoActionActivity;
import com.softfun_xmpp.connection.ConnManager;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.SpUtils;
import com.softfun_xmpp.utils.StringUtils;
import com.softfun_xmpp.utils.ThreadUtils;

public class LoginActivity extends BaseNoActionActivity implements View.OnClickListener {

    private ImageView mIv;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtLogin;
    private TextView mTvForgetpassword;
    private TextView mTvSignup;
    private RelativeLayout mMask;
    private ConnManager manager;

    private void assignViews() {
        mIv = (ImageView) findViewById(R.id.iv);
        mEtUsername = (EditText) findViewById(R.id.et_username);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mBtLogin = (Button) findViewById(R.id.bt_login);
        mTvForgetpassword = (TextView) findViewById(R.id.tv_forgetpassword);
        mTvSignup = (TextView) findViewById(R.id.tv_signup);
        mMask = (RelativeLayout) findViewById(R.id.mask1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        assignViews();

        manager = ConnManager.getInstance();
        mEtUsername.setText(SpUtils.get(Const.USERNAME,"")+"");
        mEtPassword.setText(SpUtils.get(Const.PASSWORD,"")+"");
        mBtLogin.setOnClickListener(this);
        mTvSignup.setOnClickListener(this);
        mTvForgetpassword.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login: {
                mBtLogin.setEnabled(false);
                mEtUsername.setEnabled(false);
                mEtPassword.setEnabled(false);
                mTvForgetpassword.setEnabled(false);
                mTvSignup.setEnabled(false);
                mMask.setVisibility(View.VISIBLE);
                final String username = mEtUsername.getText().toString();
                final String password = mEtPassword.getText().toString();
                if (StringUtils.isEmpty(username)) {
                    mEtUsername.setError("用户名为空");
                    mBtLogin.setEnabled(true);
                    mEtUsername.setEnabled(true);
                    mEtPassword.setEnabled(true);
                    mTvForgetpassword.setEnabled(true);
                    mTvSignup.setEnabled(true);
                    mMask.setVisibility(View.GONE);
                    return;
                }
                if (StringUtils.isEmpty(password)) {
                    mEtPassword.setError("密码为空");
                    mBtLogin.setEnabled(true);
                    mEtUsername.setEnabled(true);
                    mEtPassword.setEnabled(true);
                    mTvForgetpassword.setEnabled(true);
                    mTvSignup.setEnabled(true);
                    mMask.setVisibility(View.GONE);
                    return;
                }

                //耗时操作
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        if(manager.login(username,password)){
                            //等待服务开启
                            while (!IMService.isCreate){
                                SystemClock.sleep(100);
                            }
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mBtLogin.setEnabled(true);
                                    mEtUsername.setEnabled(true);
                                    mEtPassword.setEnabled(true);
                                    mTvForgetpassword.setEnabled(true);
                                    mTvSignup.setEnabled(true);
                                    mMask.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
                break;
            }

            case R.id.tv_signup:
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivityForResult(intent,0);
                break;

            case R.id.tv_forgetpassword:
                Intent intent1 = new Intent(LoginActivity.this,ForgetPasswordActivity.class);
                startActivity(intent1);
                break;
        }
    }



    private long firstTime = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                //如果两次按键时间间隔大于2秒，则不退出
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;//更新firstTime
                    return true;
                } else {
                    //两次按键小于2秒时，退出应用
                    //finish();//仅仅是关闭此activity
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);//杀死了进程
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if(resultCode==SignupActivity.RESULT_SUCCEED){
                String username = data.getStringExtra(Const.USERNAME);
                String password = data.getStringExtra(Const.PASSWORD);
                mEtUsername.setText(username);
                mEtPassword.setText(password);
            }
            if(resultCode==SignupActivity.RESULT_FAILURE){

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
