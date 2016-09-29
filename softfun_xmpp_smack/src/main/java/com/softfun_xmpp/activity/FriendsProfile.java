package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.UserBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.StringUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;
import com.softfun_xmpp.utils.VipResouce;

import org.appspot.apprtc.CallActivity;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

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


/**
 * Created by 范张 on 2016-02-06.
 * 先设置此activity android:theme="@style/AppTheme.NoActionBar"
 */
public class FriendsProfile extends AppCompatActivity implements View.OnClickListener {

    /**
     * 好友id
     */
    private String mUserid;
    /**
     * 好友account
     */
    private String mUsername;

    private String mAccount;
    private String mAvatar;
    private String mNickName;
    private UserBean mUserBean;
    private ImageView backdrop_act_friendsprofile;

    private CoordinatorLayout coordlayout_activity_friendsprofile;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView iv_friendsprofile_userface;
    private TextView tv_friendsprofile_showname;
    private TextView tv_friendsprofile_orgname;
    private TextView tv_friendsprofile_userid;
    private TextView tv_friendsprofile_score;
    private TextView tv_friendsprofile_userdesc;
    private ImageView iv_friendsprofile_vip;
    private TextView tv_friendsprofile_qq;
    private TextView tv_friendsprofile_email;
    private Button bt_friendsprofile_msgchat;
    private Button bt_friendsprofile_videochat;
    private Button bt_friendsprofile_space;
    private LinearLayout ll_friendsprofile_feature;
    private LinearLayout ll_friendsprofile_add;
    private Button bt_friendsprofile_delete;
    private Button bt_friendsprofile_add;
    /**
     * 标志位：区别本页面  add:添加好友【添加】  show：查看用户【发送消息、视频、动态、删除】
     */
    private String flag = "show";

    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendsprofile);
        init();
        //初始化组件
        coordlayout_activity_friendsprofile = (CoordinatorLayout) findViewById(R.id.coordlayout_activity_friendsprofile);
        backdrop_act_friendsprofile = (ImageView) findViewById(R.id.backdrop);
        iv_friendsprofile_userface = (ImageView) findViewById(R.id.iv_friendsprofile_userface);
        tv_friendsprofile_showname = (TextView) findViewById(R.id.tv_friendsprofile_showname);
        tv_friendsprofile_orgname = (TextView) findViewById(R.id.tv_friendsprofile_orgname);
        tv_friendsprofile_userid = (TextView) findViewById(R.id.tv_friendsprofile_userid);
        tv_friendsprofile_score = (TextView) findViewById(R.id.tv_friendsprofile_score);
        tv_friendsprofile_userdesc = (TextView) findViewById(R.id.tv_friendsprofile_userdesc);
        iv_friendsprofile_vip = (ImageView) findViewById(R.id.iv_friendsprofile_vip);
        tv_friendsprofile_qq = (TextView) findViewById(R.id.tv_friendsprofile_qq);
        tv_friendsprofile_email = (TextView) findViewById(R.id.tv_friendsprofile_email);
        bt_friendsprofile_msgchat = (Button) findViewById(R.id.bt_friendsprofile_msgchat);
        bt_friendsprofile_videochat = (Button) findViewById(R.id.bt_friendsprofile_videochat);
        bt_friendsprofile_space = (Button) findViewById(R.id.bt_friendsprofile_space);
        ll_friendsprofile_feature = (LinearLayout) findViewById(R.id.ll_friendsprofile_feature);
        ll_friendsprofile_add = (LinearLayout) findViewById(R.id.ll_friendsprofile_add);
        bt_friendsprofile_add = (Button) findViewById(R.id.bt_friendsprofile_add);
        bt_friendsprofile_delete = (Button) findViewById(R.id.bt_friendsprofile_delete);
        //给页面设置工具栏
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //设置工具栏标题
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("");
        //通过CollapsingToolbarLayout修改字体颜色
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);//设置还没收缩时状态下字体颜色
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);//设置收缩后Toolbar上字体的颜色


        //获取传递的好友信息
        Intent intent = this.getIntent();
        flag = intent.getStringExtra("flag");
        if (flag == null) {
            flag = "show";
        }


        mUserid = intent.getStringExtra("userid");
        mUsername = intent.getStringExtra("username");
        mAccount = mUsername + "@" + Const.APP_PACKAGENAME;
        getFriendsInfo();


        bt_friendsprofile_msgchat.setEnabled(false);
        bt_friendsprofile_videochat.setEnabled(false);
        bt_friendsprofile_space.setEnabled(false);
        bt_friendsprofile_msgchat.setOnClickListener(this);
        bt_friendsprofile_videochat.setOnClickListener(this);
        bt_friendsprofile_space.setOnClickListener(this);
        bt_friendsprofile_add.setOnClickListener(this);
        bt_friendsprofile_delete.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_friendsprofile_msgchat: {
                if(mImService.isMyFriends(mAccount)){
                    Intent intent = new Intent(this,ChatActivity.class);
                    intent.putExtra(ChatActivity.F_ACCOUNT,mAccount);
                    intent.putExtra(ChatActivity.F_NICKNAME,mNickName);
                    intent.putExtra(ChatActivity.F_AVATARURL,mAvatar);
                    startActivity(intent);
                }else{
                    ToastUtils.showToastSafe("对方目前还不是您的好友。");
                }
                break;
            }
            case R.id.bt_friendsprofile_videochat:
                sentApplyVideoMessage();
                break;
            case R.id.bt_friendsprofile_space:

                break;
            case R.id.bt_friendsprofile_add:
                //申请添加好友
                AsmackUtils.addFriend(mUserBean.getUsername(),mUserBean.getShowname());
                finish();
                break;
            case R.id.bt_friendsprofile_delete:
                //删除好友
                deleteDialog(mAccount);
                break;
        }
    }

    private void deleteDialog(final String mAccount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("您需要删除好友吗？");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mImService!=null){
                    //拒绝添加好友
                    mImService.sendPresence(Presence.Type.unsubscribe,mAccount);
                    mImService.sendPresence(Presence.Type.unsubscribed,mAccount);
                    Intent intent = new Intent();
                    String app_package_flag = getResources().getString(R.string.app_package_flag);
                    intent.setAction(app_package_flag+".activity.MainActivity");
                    intent.addCategory("android.intent.category.DEFAULT");
                    startActivity(intent);
                    finish();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }







    /**
     * 发送视频聊天申请
     */
    private void sentApplyVideoMessage() {
        final String roomId = Integer.toString((new Random()).nextInt(100000000));
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //1、创建一个消息
                Message msg = new Message();
                JivePropertiesExtension jpe = new JivePropertiesExtension();
                msg.setFrom(IMService.mCurAccount);
                msg.setTo(mAccount);
                msg.setBody("视频申请");
                msg.setType(Message.Type.chat);
                jpe.setProperty(Const.MSGFLAG,Const.MSGFLAG_VIDEO);
                jpe.setProperty("roomid",roomId);
                msg.addExtension(jpe);
                //调用服务内的发送消息方法
                mImService.sendMessage(msg);
            }
        });

//        Intent intent = new Intent(this, UIActivity.class);
//        intent.putExtra("mTargetNickName",mNickName);
//        intent.putExtra("mTargetAccount",mAccount.substring(0,mAccount.lastIndexOf("@"))+"@"+Const.APP_PACKAGENAME );
//        startActivity(intent);
        connectToRoom(roomId,false,false,0);
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

            intent.putExtra("mTargetNickName",mNickName);
            intent.putExtra("mTargetAccount",mAccount.substring(0,mAccount.lastIndexOf("@"))+"@"+Const.APP_PACKAGENAME );

            startActivityForResult(intent, CONNECTION_REQUEST);
        }
    }
    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }
        new android.app.AlertDialog.Builder(this)
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
     * 获取好友信息通过userid
     * @return
     */
    private void getFriendsInfo() {
        new getFriendsInfoTask().execute(mUserid,mUsername);
    }
    /**
     * 异步加载好友信息
     */
    public class getFriendsInfoTask extends AsyncTask<String, Void, UserBean> {
        @Override
        protected UserBean doInBackground(String... params) {
            String userid = params[0];
            String username = params[1];
            UserBean userBean;
            //如果好友id为空，就查询好友username
            if (!StringUtils.isEmpty(userid)) {
                userBean = HttpUtil.okhttpPost_QueryUserInfoByUserid(userid);
            }else{
                userBean = HttpUtil.okhttpPost_QueryUserInfoByUsername(username);
            }
            return userBean;
        }
        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            //如下获取信息展示
            if (mUserBean == null) {
                mUserBean = userBean;
            }
            //设置标题
            collapsingToolbar.setTitle(userBean.getShowname());
            //异步加载好友背景图片
            String backgroundURL = getResources().getString(R.string.app_server) + userBean.getBackground();
            //用户头像
            mAvatar = getResources().getString(R.string.app_server) + userBean.getUserface() + "?userid=" + userBean.getUserid();
            mNickName = userBean.getShowname();
            //设置imageview的tag，区别唯一
            backdrop_act_friendsprofile.setTag(backgroundURL);
            iv_friendsprofile_userface.setTag(mAvatar);
            ImageLoader.getInstance().displayImage(backgroundURL, backdrop_act_friendsprofile, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
            ImageLoader.getInstance().displayImage(mAvatar, iv_friendsprofile_userface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());


            tv_friendsprofile_showname.setText(userBean.getShowname());
            tv_friendsprofile_orgname.setText(userBean.getOrgname());
            tv_friendsprofile_userid.setText(userBean.getUserid());
            tv_friendsprofile_score.setText(userBean.getScore() + " 积分");
            tv_friendsprofile_userdesc.setText(userBean.getUserdesc());
            tv_friendsprofile_qq.setText(userBean.getQq());
            tv_friendsprofile_email.setText(userBean.getEmail());
            //vip
            if (userBean.getVip() == 0) {
                iv_friendsprofile_vip.setVisibility(View.GONE);
            } else {
                iv_friendsprofile_vip.setVisibility(View.VISIBLE);
                iv_friendsprofile_vip.setImageResource(VipResouce.getVipResouce(userBean.getVip() + ""));
            }


            bt_friendsprofile_msgchat.setEnabled(true);
            bt_friendsprofile_videochat.setEnabled(true);
            bt_friendsprofile_space.setEnabled(true);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
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
     * 初始化绑定服务
     */
    private void init(){
        //绑定服务
        Intent service = new Intent(this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onDestroy() {
        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        super.onDestroy();
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
            //System.out.println("====================  FriendsProfile onServiceConnected  =====================");
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();

            //判断对方是否已经是我的好友
            if(mImService.isMyFriends(mAccount)){
                //是好友，
                if(flag.equals("show")){
                    ll_friendsprofile_feature.setVisibility(View.VISIBLE);
                    ll_friendsprofile_add.setVisibility(View.GONE);
                }
            }else{
                //不是好友，显示添加好友按钮
                ll_friendsprofile_feature.setVisibility(View.GONE);
                ll_friendsprofile_add.setVisibility(View.VISIBLE);
            }

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //System.out.println("==================== FriendsProfile onServiceDisconnected  =====================");
        }
    }
}
