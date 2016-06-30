package com.softfun_xmpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.opentok.android.demo.opentoksamples.UIActivity;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalSoundPool;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.ImageLoaderUtils;


public class VideoChatScreen extends Activity implements View.OnClickListener {



    private String sourceid = null;
    private String sourcename = null;
    private String sourceface = null;
    private boolean isVideoing = false;

    private TextView tv_videochat_showname;
    private ImageView iv_videochat_userface;
    private Button bt_videochat_phone;
    private Button bt_videochat_phone_disconnected;
    private TextView tv_videochat_waiting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 使屏幕不显示标题栏(必须要在setContentView方法执行前执行)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏，使内容全屏显示(必须要在setContentView方法执行前执行)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_videochat);

        Intent intent = getIntent();
        sourceid = intent.getStringExtra("sourceid");
        sourcename = intent.getStringExtra("sourcename");
        sourceface = intent.getStringExtra("sourceface");

        String userfaceUrl = sourceface;

        tv_videochat_showname = (TextView) findViewById(R.id.tv_videochat_showname);
        bt_videochat_phone_disconnected = (Button) findViewById(R.id.bt_videochat_phone_disconnected);
        bt_videochat_phone = (Button) findViewById(R.id.bt_videochat_phone);
        tv_videochat_waiting = (TextView) findViewById(R.id.tv_videochat_waiting);


        iv_videochat_userface = (ImageView) findViewById(R.id.iv_videochat_userface);
        iv_videochat_userface.setTag(userfaceUrl);

        if(userfaceUrl==null){
            userfaceUrl = "drawable://" + R.drawable.useravatar;
            //iv_videochat_userface.setImageResource(R.drawable.useravatar);
            ImageLoader.getInstance().displayImage(userfaceUrl,iv_videochat_userface, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular_border());
        }else{
            ImageLoader.getInstance().displayImage(userfaceUrl,iv_videochat_userface, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular_border());
        }

        tv_videochat_showname.setText(sourcename);

        isVideoing = false;

        tv_videochat_waiting.setVisibility(View.GONE);
        bt_videochat_phone_disconnected.setVisibility(View.VISIBLE);
        bt_videochat_phone.setVisibility(View.VISIBLE);

        //播放声音：视频coming
        GlobalSoundPool.getInstance().play(R.raw.v_videocoming);
        bt_videochat_phone.setOnClickListener(this);
        bt_videochat_phone_disconnected.setOnClickListener(this);


        //设置我正在视频聊天
        IMService.isVideo = true;
        //System.out.println("====================  IMService.isVideo = true  5=====================");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_videochat_phone: {
                goVideo();
                break;
            }
            case R.id.bt_videochat_phone_disconnected: {
                exit();
                break;
            }
        }
    }

    private void exit() {
        //System.out.println("====================  IMService.isVideo = false  6=====================");
        //设置我不再视频聊天
        IMService.isVideo = false;
        finish();
    }


    /**
     * 进入视频聊天
     */
    private void goVideo() {
//        Intent intent = new Intent(VideoChatScreen.this,VideoActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("targetAccount", sourceid);
//        intent.putExtra("targetNickName", sourcename);
//        this.startActivity(intent);
//        finish();

//        Intent intent = new Intent();
//        intent.setAction("com.opentok.android.demo.opentoksamples.OpenTokSubclassing");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("userid", sourceid);
//        intent.putExtra("showname", sourcename);
//        this.startActivity(intent);

        IMService.isVideo = true;
        //System.out.println("====================  IMService.isVideo = true  7=====================");
        Intent intent = new Intent(this, UIActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("mTargetNickName",sourcename);
        intent.putExtra("mTargetAccount",sourceid.substring(0,sourceid.lastIndexOf("@"))+"@"+ Const.APP_PACKAGENAME);

        startActivity(intent);
        finish();


    }


    @Override
    protected void onDestroy() {
        if (!isVideoing) {
            GlobalSoundPool.getInstance().stop();
            isVideoing = true;
        }
        finish();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (!isVideoing) {
            GlobalSoundPool.getInstance().stop();
            isVideoing = true;
        }
        super.onPause();
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            exit();
            return true;
        }
        return false;
    }




}
