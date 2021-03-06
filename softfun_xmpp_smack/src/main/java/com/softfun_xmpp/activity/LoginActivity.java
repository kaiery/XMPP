package com.softfun_xmpp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.softfun_xmpp.utils.ToastUtils;

public class LoginActivity extends BaseNoActionActivity implements View.OnClickListener {

    private ImageView mIv;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtLogin;
    private TextView mTvForgetpassword;
    private TextView mTvSignup;
    private RelativeLayout mMask;
    private ConnManager manager;
    private static final int permsRequestCode = 200;
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

        //获取Intent传递的参数.自动登录
        boolean is_autologin = getIntent().getBooleanExtra(Const.AUTOLOGIN,false);
        String pUsername = getIntent().getStringExtra(Const.USERNAME);
        String pPassword = getIntent().getStringExtra(Const.PASSWORD);
        if(is_autologin){
            mBtLogin.setEnabled(false);
            mEtUsername.setEnabled(false);
            mEtPassword.setEnabled(false);
            mTvForgetpassword.setEnabled(false);
            mTvSignup.setEnabled(false);
            mMask.setVisibility(View.VISIBLE);
            //自动登录
            login(pUsername, pPassword);
        }

        //授权
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            getPermission();
        }
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
                login(username, password);
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

    private void login(final String username, final String password) {
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





    /**
     * ANDROID6 权限申请
     */
    private void getPermission() {
        if (  (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) ) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_PHONE_STATE
                    }, permsRequestCode );
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case 200:
                for (int i = 0; i < grantResults.length; i++) {
                    boolean Accepted = grantResults[i]==PackageManager.PERMISSION_GRANTED;
                    if(Accepted){
                        //System.out.println(permissions[i]+"授权");
                    }else{
                        //System.out.println(permissions[i]+"拒绝授权");
                        ToastUtils.showToastSafe_Long("请授权，否则系统无法正常工作。");
                        finish();
                    }
                }
                ////System.out.println("====================  授权完毕  =====================");
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
