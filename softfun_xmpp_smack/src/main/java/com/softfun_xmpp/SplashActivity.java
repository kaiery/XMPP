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

    private static final String TAG = "SplashActivity";
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
        //System.out.println("===========  隐式接收的数据  =============== " + uri);
        if(uri!=null){
            String uriStr  = uri.getScheme()+"://"+uri.getHost()+":"+uri.getPort()+uri.getPath();
            String path = (uri.getPath().substring(0,1).equals("/"))?uri.getPath().substring(1):uri.getPath();
            String parameters = path.substring(path.indexOf("/")+1);//这里的加密值可能会产生/号
            String[] paramenterList = parameters.split(",");
            try {
                pUsername = CipherUtils.decryptDES(paramenterList[0], paramenterList[2]);
                pPassword = CipherUtils.decryptDES(paramenterList[1], paramenterList[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
