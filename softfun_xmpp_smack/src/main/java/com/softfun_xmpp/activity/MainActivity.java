package com.softfun_xmpp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalSoundPool;
import com.softfun_xmpp.application.SystemVars;
import com.softfun_xmpp.bean.DialogBean;
import com.softfun_xmpp.bean.UpdateBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.fragment.MainFragment;
import com.softfun_xmpp.network.DownloadAPK;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.notification.NotificationUtilEx;
import com.softfun_xmpp.utils.AppUtils;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.FileUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.SpUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;
import com.softfun_xmpp.utils.VipResouce;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //导航视图
    private NavigationView navigationView;
    //抽屉布局
    private DrawerLayout drawer;
    //工具按钮栏
    private Toolbar toolbar;
    //抽屉开关
    private ActionBarDrawerToggle toggle;
    //抽屉头部视图
    private View headerView;
    //抽屉头部布局
    private RelativeLayout ll_nav_header;
    //抽屉头部背景
    private ImageView iv_nav_header_background;
    //抽屉头部视图:头像组件
    private ImageView iv_nav_header_userface;
    //抽屉头部视图:用户名称组件
    private TextView tv_nav_header_showname;
    //抽屉头部视图:vip组件
    private ImageView iv_nav_header_vip;

    //抽屉菜单
    private Menu headerMenu;
    //抽屉菜单内自定义条目布局
    private MenuItem menuItem;
    //抽屉菜单内自定义条目布局内的真正组件
    private SwitchCompat swh_nav_menu_receivemsg;
    private static final int permsRequestCode = 200;
    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;


    //rtc偏好
    //视频聊天参数-------------------------------------
    public static final int CONNECTION_REQUEST = 1;
    public static boolean commandLineRun = false;
    public static SharedPreferences sharedPref;
    public static String keyprefVideoCallEnabled;
    public static String keyprefCamera2;
    public static String keyprefResolution;
    public static String keyprefFps;
    public static String keyprefCaptureQualitySlider;
    public static String keyprefVideoBitrateType;
    public static String keyprefVideoBitrateValue;
    public static String keyprefVideoCodec;
    public static String keyprefAudioBitrateType;
    public static String keyprefAudioBitrateValue;
    public static String keyprefAudioCodec;
    public static String keyprefHwCodecAcceleration;
    public static String keyprefCaptureToTexture;
    public static String keyprefNoAudioProcessingPipeline;
    public static String keyprefAecDump;
    public static String keyprefOpenSLES;
    public static String keyprefDisableBuiltInAec;
    public static String keyprefDisableBuiltInAgc;
    public static String keyprefDisableBuiltInNs;
    public static String keyprefEnableLevelControl;
    //public static String keyprefDisplayHud;
    //public static String keyprefTracing;
    //public static String keyprefRoomServerUrl;
    public static String keyprefRoom;
    /**
     * 初始化rtc默认偏好
     */
    private void initRTCPreference() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
        keyprefCamera2 = getString(R.string.pref_camera2_key);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
        keyprefVideoBitrateType = getString(R.string.pref_startvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_startvideobitratevalue_key);
        keyprefVideoCodec = getString(R.string.pref_videocodec_key);
        keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
        keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
        keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
        keyprefAecDump = getString(R.string.pref_aecdump_key);
        keyprefOpenSLES = getString(R.string.pref_opensles_key);
        keyprefDisableBuiltInAec = getString(R.string.pref_disable_built_in_aec_key);
        keyprefDisableBuiltInAgc = getString(R.string.pref_disable_built_in_agc_key);
        keyprefDisableBuiltInNs = getString(R.string.pref_disable_built_in_ns_key);
        keyprefEnableLevelControl = getString(R.string.pref_enable_level_control_key);
        //keyprefDisplayHud = getString(R.string.pref_displayhud_key);
        //keyprefTracing = getString(R.string.pref_tracing_key);
        //keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRTCPreference();
        setContentView(R.layout.main);

        SystemVars.getInstance().setMainActivity(this);
        //初始化Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new MainFragment()).commit();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);
        ll_nav_header = (RelativeLayout) headerView.findViewById(R.id.ll_nav_header);

        tv_nav_header_showname = (TextView) headerView.findViewById(R.id.tv_nav_header_showname);
        tv_nav_header_showname.setText(IMService.mCurNickName);

        iv_nav_header_vip = (ImageView) headerView.findViewById(R.id.iv_nav_header_vip);

        /**
         * vip图标
         */
        if(IMService.mCurVip==null || IMService.mCurVip.equals("0")){
            iv_nav_header_vip.setVisibility(View.GONE);
        }else{
            iv_nav_header_vip.setVisibility(View.VISIBLE);
            iv_nav_header_vip.setImageResource(VipResouce.getVipResouce(IMService.mCurVip));
        }

        //得到抽屉菜单
//        headerMenu = navigationView.getMenu();
        //得到抽屉菜单内自定义组件条目布局（这里是自定义了一个开关条目布局）
//        menuItem = headerMenu.findItem(R.id.nav_switch);
        //得到抽屉菜单内自定义条目布局内的真正组件，通过组件id
//        swh_nav_menu_receivemsg = (SwitchCompat) menuItem.getActionView().findViewById(R.id.swh_nav_menu_receivemsg);
//        swh_nav_menu_receivemsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                //System.out.println(isChecked);
//            }
//        });

        initData();

        initListener();
    }




    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * 写用户登录日志
     */
    private void writeUserLoginLog() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.okhttpPost_writeUserLoginLog(AsmackUtils.filterAccountToUserName(IMService.mCurAccount));
            }
        });
    }

    private void initListener() {
        // 注册重复登录 的动态广播消息
        IntentFilter filter_dynamic = new IntentFilter();
        filter_dynamic.addAction(Const.RELOGIN_BROADCAST_ACTION);
        registerReceiver(dynamicReceiver, filter_dynamic);



        //绑定服务
        Intent service = new Intent(MainActivity.this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 广播接受者
     */
    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Const.RELOGIN_BROADCAST_ACTION)){

                //停止服务
                Intent imservice = new Intent(MainActivity.this,IMService.class);
                stopService(imservice);


                final String msg = intent.getStringExtra("msg");
                Intent intent_dialog = new Intent(MainActivity.this, DialogActivity.class);
                Bundle bundle = new Bundle();
                DialogBean dialogBean = new DialogBean();
                dialogBean.setTitle("提示");
                dialogBean.setContent(msg);
                dialogBean.setDialogType(DialogBean.DialogType.toTick);
                dialogBean.setButtonType(DialogBean.ButtonType.onebutton);
                bundle.putSerializable("dialogBean", dialogBean);
                intent_dialog.putExtras(bundle);
                startActivity(intent_dialog);
            }
        }
    };

    private void initData() {
        GlobalSoundPool.getInstance();

        //授权
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            getPermission();
        }else{
            writeUserLoginLog();
        }

        FileUtils.createFolder(getResources().getString(R.string.record_folder));

        iv_nav_header_background = (ImageView) headerView.findViewById(R.id.iv_nav_header_background);
        iv_nav_header_userface = (ImageView) headerView.findViewById(R.id.iv_nav_header_userface);
        String background = IMService.mCurBackground;
        if(background==null){
            iv_nav_header_background.setImageResource(R.drawable.background01);
        }else{
            ImageLoader.getInstance().displayImage(background,iv_nav_header_background, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
        }

        String avatarurl = IMService.mCurAvatarUrl;
        if(avatarurl==null){
            iv_nav_header_userface.setImageResource(R.drawable.useravatar);
        }else{
            ImageLoader.getInstance().displayImage(avatarurl,iv_nav_header_userface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());
        }

        //检测版本是否需要升级
        checkVersion();
    }

    /**
     * 检测版本
     */
    private void checkVersion() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String app_package_flag = getResources().getString(R.string.app_package_flag);
                final UpdateBean updateBean = HttpUtil.okhttpGet_UpdateInfo(app_package_flag);
                if(updateBean!=null){
                    //获取应用程序的版本号
                    int verCode = AppUtils.getVerCode(MainActivity.this);
                    if(updateBean.getVercode()>verCode){
                        //有新版本
                        SpUtils.put(Const.UPDATE,0);
                        //弹出对话框
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("提示");
                                builder.setMessage(updateBean.getDesc());
                                builder.setPositiveButton("立即升级！", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //下载升级文件
                                        new DownloadAPK(updateBean.getFilesize()).execute(updateBean.getDownloadurl());
                                    }
                                });
                                builder.setNegativeButton("下次再说。", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //取消
                                    }
                                });
                                builder.show();
                            }
                        });
                    }else{
                        //已经是最新版本
                        SpUtils.put(Const.UPDATE,1);
                    }
                    SpUtils.put(Const.UPDATEVERCODE,updateBean.getVercode());
                    SpUtils.put(Const.UPDATEDESC, updateBean.getDesc());
                    SpUtils.put(Const.UPDATEDOWNLOADURL,updateBean.getDownloadurl());
                    SpUtils.put(Const.UPDATEFILESIZE,updateBean.getFilesize());
                }
            }
        });
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
                writeUserLoginLog();
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //****直接返回桌面
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            startActivity(intent);
            //super.onBackPressed();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 这里执行搜索操作
                //////System.out.println("开始搜索");
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                //////System.out.println("搜索内容改变了");
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() )
        {
            case R.id.action_add: {
                //////System.out.println("点击了添加好友菜单");
                Intent intent = new Intent(MainActivity.this, AddFriends.class);
                startActivity(intent);
                break;
            }
            case R.id.action_addgroup: {
                Intent intent1 = new Intent(this,SelectGroupTypeActivity.class);
                startActivity(intent1);
                break;
            }
//            case R.id.action_share: {
//                Intent intent=new Intent(Intent.ACTION_SEND);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
//                intent.putExtra(Intent.EXTRA_TEXT, "分享测试");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(Intent.createChooser(intent, getTitle()));
//                break;
//            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // 处理抽屉导航菜单的点击事件.
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            //////System.out.println("点击了我的资料菜单");
            Intent intent = new Intent(MainActivity.this, MyProfile.class);
            startActivity(intent);
        } else if (id == R.id.nav_blog) {
            //////System.out.println("点击了我的动态菜单");
        } else if (id == R.id.nav_fav) {
            //////System.out.println("点击了我的收藏菜单");
        } else if (id == R.id.nav_setting) {
            Intent intent_setting = new Intent(this,SettingActivity.class);
            startActivity(intent_setting);
            //////System.out.println("点击了系统设置菜单");
        //} else if (id == R.id.nav_info) {
            //////System.out.println("点击了关于我们菜单");
        } else if (id == R.id.nav_exit){
            Intent intent = new Intent(MainActivity.this, DialogActivity.class);
            Bundle bundle = new Bundle();
            DialogBean dialogBean = new DialogBean();
            dialogBean.setTitle("提示");
            dialogBean.setContent("您是否需要退出，重新登录？");
            dialogBean.setDialogType(DialogBean.DialogType.tologin);
            dialogBean.setButtonType(DialogBean.ButtonType.twobutton);
            bundle.putSerializable("dialogBean", dialogBean);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        //点击条目后，关闭抽屉
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }





    private long firstTime = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode)
        {
//            case KeyEvent.KEYCODE_BACK:
//                long secondTime = System.currentTimeMillis();
//                //如果两次按键时间间隔大于2秒，则不退出
//                if (secondTime - firstTime > 2000) {
//                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//                    firstTime = secondTime;//更新firstTime
//                    return true;
//                } else {
//                    //两次按键小于2秒时，退出应用
//                    //GlobalUtil.getInstance().setMain(null);
//                    finish();
//                }
//                break;
            case KeyEvent.KEYCODE_HOME:{
                break;
            }
        }
        return super.onKeyUp(keyCode, event);
    }


    /***
     * 主界面销毁
     */
    @Override
    protected void onDestroy() {
        //////System.out.println("主界面销毁");
        unregisterReceiver(dynamicReceiver);

        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
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
            //群聊邀请的监听
            mImService.addInvitationListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        //System.out.println("====================  onNewIntent  =====================");
        super.onNewIntent(intent);
        String type = intent.getStringExtra(Const.TYPE);
        String notification_from = intent.getStringExtra(Const.NOTIFICATION_FROM);
        String nickName = intent.getStringExtra(Const.NICKNAME);
        String avatarurl = intent.getStringExtra(Const.AVATARURL);
        String room_jid = intent.getStringExtra(Const.GROUP_JID);
        if(room_jid==null) room_jid = "";
        String room_name = (room_jid.equals(""))?"":AsmackUtils.filterGroupName(room_jid)+"";

        if(type==null || type.equals("") || type.equals("null")) return;

        NotificationUtilEx.getInstance().removeNotificationMapNode(notification_from);

        if(!type.equals("") && !notification_from.equals("")  && !nickName.equals("")  && !avatarurl.equals("") && type.equals(Message.Type.chat.name())){
            //跳转到私聊
            if(mImService.isMyFriends(notification_from)){
                Intent intent_chat = new Intent(this,ChatActivity.class);
                intent_chat.putExtra(ChatActivity.F_ACCOUNT,notification_from);
                intent_chat.putExtra(ChatActivity.F_NICKNAME,nickName);
                intent_chat.putExtra(ChatActivity.F_AVATARURL,avatarurl);
                startActivity(intent_chat);
                return;
            }
        }

        if(!type.equals("") && !notification_from.equals("")  && !nickName.equals("")  && !avatarurl.equals("") && type.equals(Presence.Type.subscribe.name())){
            //跳转到好友申请
            if(mImService.isMyFriends(notification_from)){
                Intent intent_chat = new Intent(this,ChatActivity.class);
                intent_chat.putExtra(ChatActivity.F_ACCOUNT,notification_from);
                intent_chat.putExtra(ChatActivity.F_NICKNAME,nickName);
                intent_chat.putExtra(ChatActivity.F_AVATARURL,avatarurl);
                startActivity(intent_chat);
                return;
            }else{
                mImService.updateChatMessageToIsRead(notification_from);
                Intent intent_invite = new Intent(this,AddFriendsAction.class);
                intent_invite.putExtra(ChatActivity.F_ACCOUNT,notification_from);
                intent_invite.putExtra(ChatActivity.F_NICKNAME,nickName);
                intent_invite.putExtra(ChatActivity.F_AVATARURL,avatarurl);
                startActivity(intent_invite);
                return;
            }
        }

        if(!type.equals("") && !notification_from.equals("")   &&!room_jid.equals("")  && type.equals(Message.Type.groupchat.name())){
            //跳转到群聊
            Intent intent_groupchat = new Intent(this, MultiChatActivity.class);
            intent_groupchat.putExtra(MultiChatActivity.F_ROOM_JID,room_jid);
            intent_groupchat.putExtra(MultiChatActivity.F_ROOM_NAME,room_name);
            intent_groupchat.putExtra(MultiChatActivity.F_ROOM_AVATARURL,avatarurl);
            startActivity(intent_groupchat);
        }


    }


}
