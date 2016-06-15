package com.softfun_xmpp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.softfun_xmpp.activity.LoginActivity;
import com.softfun_xmpp.baseActivity.BaseNoActionActivity;
import com.softfun_xmpp.utils.ThreadUtils;

public class SplashActivity extends BaseNoActionActivity implements View.OnClickListener {

    private volatile boolean isEntered;
    private Button bt_enterApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // TODO: 2016-06-14
        Intent intent = getIntent();
        Uri uri = intent.getData();
        System.out.println("====================  隐式接收的数据  ===================== " + uri);


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
        Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch( v.getId())
        {
            case R.id.bt_enterApp:
                enterLoginActivity();
                break;
        }
    }
}
