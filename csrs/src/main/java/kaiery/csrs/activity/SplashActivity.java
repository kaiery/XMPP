package kaiery.csrs.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import kaiery.csrs.R;
import kaiery.csrs.activity.base.BaseNoActionActivity;
import kaiery.csrs.application.Const;

public class SplashActivity extends BaseNoActionActivity {

    private volatile boolean isEntered;
    private boolean is_autologin = false;
    private String pUsername;
    private String pPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        init();
    }

    private void init() {
        //获取隐式意图调用app时传递的参数，用户名和密码
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if(uri!=null){
//            String uriStr  = uri.getScheme()+"://"+uri.getHost()+":"+uri.getPort()+uri.getPath();
//            String path = (uri.getPath().substring(0,1).equals("/"))?uri.getPath().substring(1):uri.getPath();
//            String parameters = path.substring(path.indexOf("/")+1);//这里的加密值可能会产生/号
//            String[] paramenterList = parameters.split(",");
//            try {
//                pUsername = CipherUtils.decryptDES(paramenterList[0], paramenterList[2]);
//                pPassword = CipherUtils.decryptDES(paramenterList[1], paramenterList[2]);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if(!TextUtils.isEmpty(pUsername) && !TextUtils.isEmpty(pPassword)){
//                is_autologin = true;
//            }
        }else{
            is_autologin = false;
        }
        //停止1秒进入主界面
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                enterLoginActivity();
            }

        },1000);

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
        //iPhone效果
        // overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
        //淡入淡出效果
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}
