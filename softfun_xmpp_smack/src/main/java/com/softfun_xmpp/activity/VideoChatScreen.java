package com.softfun_xmpp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalSoundPool;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.ToastUtils;

import org.appspot.apprtc.CallActivity;

import java.util.Random;

import static com.softfun_xmpp.activity.MainActivity.CONNECTION_REQUEST;
import static com.softfun_xmpp.activity.MainActivity.commandLineRun;
import static com.softfun_xmpp.activity.MainActivity.keyprefAecDump;
import static com.softfun_xmpp.activity.MainActivity.keyprefAudioBitrateType;
import static com.softfun_xmpp.activity.MainActivity.keyprefAudioBitrateValue;
import static com.softfun_xmpp.activity.MainActivity.keyprefAudioCodec;
import static com.softfun_xmpp.activity.MainActivity.keyprefCamera2;
import static com.softfun_xmpp.activity.MainActivity.keyprefCaptureQualitySlider;
import static com.softfun_xmpp.activity.MainActivity.keyprefCaptureToTexture;
import static com.softfun_xmpp.activity.MainActivity.keyprefDisableBuiltInAec;
import static com.softfun_xmpp.activity.MainActivity.keyprefDisableBuiltInAgc;
import static com.softfun_xmpp.activity.MainActivity.keyprefDisableBuiltInNs;
import static com.softfun_xmpp.activity.MainActivity.keyprefEnableLevelControl;
import static com.softfun_xmpp.activity.MainActivity.keyprefFps;
import static com.softfun_xmpp.activity.MainActivity.keyprefHwCodecAcceleration;
import static com.softfun_xmpp.activity.MainActivity.keyprefNoAudioProcessingPipeline;
import static com.softfun_xmpp.activity.MainActivity.keyprefOpenSLES;
import static com.softfun_xmpp.activity.MainActivity.keyprefResolution;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoBitrateType;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoBitrateValue;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoCallEnabled;
import static com.softfun_xmpp.activity.MainActivity.keyprefVideoCodec;
import static com.softfun_xmpp.activity.MainActivity.sharedPref;


public class VideoChatScreen extends Activity implements View.OnClickListener {


    private String sourceid = null;
    private String sourcename = null;
    private String sourceface = null;
    private String roomId = null;
    private boolean isVideoing = false;

    private TextView tv_videochat_showname;
    private ImageView iv_videochat_userface;
    private Button bt_videochat_phone;
    private Button bt_videochat_phone_disconnected;
    private TextView tv_videochat_waiting;
    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //加入此代码，则可以实现 在锁屏状态下，进行显示Activity
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        // 使屏幕不显示标题栏(必须要在setContentView方法执行前执行)
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏，使内容全屏显示(必须要在setContentView方法执行前执行)
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_videochat);

        //绑定服务
        Intent service = new Intent(this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);

        Intent intent = getIntent();
        sourceid = intent.getStringExtra("sourceid");
        sourcename = intent.getStringExtra("sourcename");
        sourceface = intent.getStringExtra("sourceface");
        roomId = intent.getStringExtra("roomid");

        String userfaceUrl = sourceface;

        tv_videochat_showname = (TextView) findViewById(R.id.tv_videochat_showname);
        bt_videochat_phone_disconnected = (Button) findViewById(R.id.bt_videochat_phone_disconnected);
        bt_videochat_phone = (Button) findViewById(R.id.bt_videochat_phone);
        tv_videochat_waiting = (TextView) findViewById(R.id.tv_videochat_waiting);


        iv_videochat_userface = (ImageView) findViewById(R.id.iv_videochat_userface);
        iv_videochat_userface.setTag(userfaceUrl);

        if (userfaceUrl == null) {
            userfaceUrl = "drawable://" + R.drawable.useravatar;
            //iv_videochat_userface.setImageResource(R.drawable.useravatar);
            ImageLoader.getInstance().displayImage(userfaceUrl, iv_videochat_userface, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular_border());
        } else {
            ImageLoader.getInstance().displayImage(userfaceUrl, iv_videochat_userface, ImageLoaderUtils.getOptions_CacheInMem_CacheInDisk_Exif_circular_border());
        }

        tv_videochat_showname.setText(sourcename);

        isVideoing = false;

        tv_videochat_waiting.setVisibility(View.GONE);
        bt_videochat_phone_disconnected.setVisibility(View.VISIBLE);
        bt_videochat_phone.setVisibility(View.VISIBLE);

        //播放声音：视频coming
        //GlobalSoundPool.getInstance().play(R.raw.v_videocoming);
        mediaPlayer = MediaPlayer.create(this, R.raw.v_videocoming);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        bt_videochat_phone.setOnClickListener(this);
        bt_videochat_phone_disconnected.setOnClickListener(this);


        //设置我正在视频聊天
        IMService.isVideo = true;
        //System.out.println("====================  IMService.isVideo = true  5=====================");

        // 注册对方忙的时候 的动态广播消息
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction(Const.VIDEO_WORKING_BROADCAST_ACTION);
        registerReceiver(dynamicReceiver, filter_dynamic);
        //IMService.VIDEO_UI_CREATE = true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onClick(View v) {
        //IMService.VIDEO_UI_CREATE = false;
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
        mImService.callbackRefuseVideoMsg(sourceid.substring(0, sourceid.lastIndexOf("@")) + "@" + Const.APP_PACKAGENAME, "视频通话取消");
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
//        Intent intent = new Intent(this, UIActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("mTargetNickName",sourcename);
//        intent.putExtra("mTargetAccount",sourceid.substring(0,sourceid.lastIndexOf("@"))+"@"+ Const.APP_PACKAGENAME);
//        startActivity(intent);
        connectToRoom(roomId, false, false, 0);

        finish();


    }


    /**
     * 连接房间
     *
     * @param roomId         -
     * @param commandLineRun_ -
     * @param loopback       -
     * @param runTimeMs      -
     */
    private void connectToRoom(String roomId, boolean commandLineRun_, boolean loopback, int runTimeMs) {
        commandLineRun = commandLineRun_;
        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }
        //String roomUrl = sharedPref.getString(keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));
        String roomUrl = "http://"+IMService.RTC_ROOMSERVER;
        // Video call enabled flag.
        boolean videoCallEnabled = sharedPref.getBoolean(keyprefVideoCallEnabled, Boolean.valueOf(getString(R.string.pref_videocall_default)));
        // Use Camera2 option.
        boolean useCamera2 = sharedPref.getBoolean(keyprefCamera2, Boolean.valueOf(getString(R.string.pref_camera2_default)));
        // Get default codecs.
        String videoCodec = sharedPref.getString(keyprefVideoCodec, getString(R.string.pref_videocodec_default));
        String audioCodec = sharedPref.getString(keyprefAudioCodec, getString(R.string.pref_audiocodec_default));
        // Check HW codec flag.
        boolean hwCodec = sharedPref.getBoolean(keyprefHwCodecAcceleration, Boolean.valueOf(getString(R.string.pref_hwcodec_default)));
        // Check Capture to texture.
        boolean captureToTexture = sharedPref.getBoolean(keyprefCaptureToTexture, Boolean.valueOf(getString(R.string.pref_capturetotexture_default)));
        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPref.getBoolean(keyprefNoAudioProcessingPipeline, Boolean.valueOf(getString(R.string.pref_noaudioprocessing_default)));
        // Check Disable Audio Processing flag.
        boolean aecDump = sharedPref.getBoolean(keyprefAecDump, Boolean.valueOf(getString(R.string.pref_aecdump_default)));
        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPref.getBoolean(keyprefOpenSLES, Boolean.valueOf(getString(R.string.pref_opensles_default)));
        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPref.getBoolean(keyprefDisableBuiltInAec, Boolean.valueOf(getString(R.string.pref_disable_built_in_aec_default)));
        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPref.getBoolean(keyprefDisableBuiltInAgc, Boolean.valueOf(getString(R.string.pref_disable_built_in_agc_default)));
        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPref.getBoolean(keyprefDisableBuiltInNs, Boolean.valueOf(getString(R.string.pref_disable_built_in_ns_default)));
        // Check Enable level control.
        boolean enableLevelControl = sharedPref.getBoolean(keyprefEnableLevelControl, Boolean.valueOf(getString(R.string.pref_enable_level_control_key)));
        // 得到视频分辨率设置
        int videoWidth = 0;
        int videoHeight = 0;
        String resolution = sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
        String[] dimensions = resolution.split("[ x]+");
        if (dimensions.length == 2) {
            try {
                videoWidth = Integer.parseInt(dimensions[0]);
                videoHeight = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                videoWidth = 0;
                videoHeight = 0;
                Log.e("rtc", "错误的视频分辨率设置: " + resolution);
            }
        }
        // Get camera fps from settings.
        int cameraFps = 0;
        String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
        String[] fpsValues = fps.split("[ x]+");
        if (fpsValues.length == 2) {
            try {
                cameraFps = Integer.parseInt(fpsValues[0]);
            } catch (NumberFormatException e) {
                Log.e("rtc", "错误的相机fps设置: " + fps);
            }
        }
        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPref.getBoolean(keyprefCaptureQualitySlider, Boolean.valueOf(getString(R.string.pref_capturequalityslider_default)));
        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        String bitrateTypeDefault = getString(
                R.string.pref_startvideobitrate_default);
        String bitrateType = sharedPref.getString(
                keyprefVideoBitrateType, bitrateTypeDefault);
        if (!bitrateType.equals(bitrateTypeDefault)) {
            String bitrateValue = sharedPref.getString(keyprefVideoBitrateValue, getString(R.string.pref_startvideobitratevalue_default));
            videoStartBitrate = Integer.parseInt(bitrateValue);
        }
        int audioStartBitrate = 0;
        bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
        bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
        if (!bitrateType.equals(bitrateTypeDefault)) {
            String bitrateValue = sharedPref.getString(keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
            audioStartBitrate = Integer.parseInt(bitrateValue);
        }
        // Check statistics display option.
        //boolean displayHud = sharedPref.getBoolean(keyprefDisplayHud, Boolean.valueOf(getString(R.string.pref_displayhud_default)));
        //boolean tracing = sharedPref.getBoolean(keyprefTracing, Boolean.valueOf(getString(R.string.pref_tracing_default)));
        // Start AppRTCDemo activity.
        Log.d("rtc", "连接到房间 " + roomId + " at URL " + roomUrl);
        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(this, CallActivity.class);
            intent.setData(uri);
            intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
            intent.putExtra(CallActivity.EXTRA_LOOPBACK, loopback);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
            intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
            intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
            //intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
            //intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
            intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
            intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);

            intent.putExtra("mTargetNickName", sourcename);
            intent.putExtra("mTargetAccount", sourceid.substring(0, sourceid.lastIndexOf("@")) + "@" + Const.APP_PACKAGENAME);

            startActivityForResult(intent, CONNECTION_REQUEST);
        }
    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create().show();
        return false;
    }

    /**
     * 广播接受者
     */
    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Const.VIDEO_WORKING_BROADCAST_ACTION)) {
                final String msg = intent.getStringExtra("msg");
                ToastUtils.showToastSafe(msg);//"对方正忙，无法进行视频聊天"
                //设置我不再视频聊天
                IMService.isVideo = false;
                finish();
            }
        }
    };


    @Override
    protected void onDestroy() {

        unregisterReceiver(dynamicReceiver);

        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }

        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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


    /**
     * 绑定服务的连接对象
     */
    private MyServiceConnection mMyServiceConnection = new MyServiceConnection();

    /**
     * 绑定服务的连接对象类
     */
    class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ////System.out.println("====================  onServiceDisconnected  =====================");
        }
    }
}
