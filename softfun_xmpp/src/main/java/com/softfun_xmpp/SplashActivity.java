package com.softfun_xmpp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.softfun_xmpp.activity.LoginActivity;
import com.softfun_xmpp.baseActivity.BaseNoActionActivity;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.CipherUtils;
import com.softfun_xmpp.utils.ThreadUtils;

public class SplashActivity extends BaseNoActionActivity implements View.OnClickListener {

    private volatile boolean isEntered;
    private Button bt_enterApp;
    private boolean is_autologin = false;
    private String pUsername;
    private String pPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // TODO: 2016-06-14
        //获取隐式意图调用app时传递的参数，用户名和密码
        Intent intent = getIntent();
        Uri uri = intent.getData();
        //System.out.println("====================  隐式接收的数据  ===================== " + uri);
        if(uri!=null){
            String uriStr  = uri.getScheme()+"://"+uri.getHost()+":"+uri.getPort()+"/"+uri.getPath();
            String parameters = uriStr.substring(uriStr.lastIndexOf("/")+1);
            String[] paramenterList = parameters.split(",");
            pUsername = CipherUtils.decrypt(paramenterList[0],CipherUtils.getKey());
            pPassword = CipherUtils.decrypt(paramenterList[1],CipherUtils.getKey());
            if(!TextUtils.isEmpty(pUsername) && !TextUtils.isEmpty(pPassword)){
                is_autologin = true;
            }
        }else{
            is_autologin = false;
        }

        bt_enterApp = (Button) findViewById(R.id.bt_enterApp);
        bt_enterApp.setOnClickListener(this);
        //停止3秒进入主界面
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(3000);
                enterLoginActivity();
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        enterLoginActivity();
        return true;
    }

    private synchronized void enterLoginActivity() {
        if (isEntered) {
            return;
        }
        isEntered = true;

        //进入主界面
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        if(is_autologin){
            intent.putExtra(Const.AUTOLOGIN, true);
            intent.putExtra(Const.USERNAME,pUsername);
            intent.putExtra(Const.PASSWORD,pPassword);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_enterApp:
                enterLoginActivity();
                break;
        }
    }
}
