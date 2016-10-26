package kaiery.csrs.activity;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import kaiery.csrs.R;
import kaiery.csrs.adapter.GoogleBottomNavigationViewPagerAdapter;
import kaiery.csrs.application.SystemSingleton;
import kaiery.csrs.beans.DialogBean;
import kaiery.csrs.beans.ModuleBean;
import kaiery.csrs.fragment.MainFragment;
import kaiery.csrs.fragment.MainGoogleFragment;
import kaiery.csrs.utils.ImageLoaderUtils;
import kaiery.csrs.utils.VipResouce;

import static com.yolanda.nohttp.NoHttp.getContext;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

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


    //-----------GoogleBottonNavigation变量------------
    private MainGoogleFragment currentFragment;
    private GoogleBottomNavigationViewPagerAdapter ViewPagerAdapter;
    private AHBottomNavigationAdapter navigationAdapter;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private boolean useMenuResource = false;
    private int[] tabColors;
    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private FloatingActionButton floatingActionButton;
    private List<ModuleBean> mModuleBeanList = new ArrayList<>();
    //-----------GoogleBottonNavigation变量end------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initData();
        initView();
        //initClassBottomNavigation(savedInstanceState);
        //初始化 google 风格的底部导航
        initGoogleBottomNavigation();

        SystemSingleton.getInstance().setMainActivity( this );
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        tv_nav_header_showname.setText("测试");
        iv_nav_header_vip.setVisibility(View.VISIBLE);
        iv_nav_header_vip.setImageResource(VipResouce.getVipResouce("9"));
        String background = null;
        if(background==null){
            background = "drawable://" + R.drawable.background_test;
            ImageLoader.getInstance().displayImage(background,iv_nav_header_background, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
        }else{
            ImageLoader.getInstance().displayImage(background,iv_nav_header_background, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
        }

        String avatarurl = null;
        if(avatarurl==null){
            avatarurl = "drawable://" + R.drawable.avatar_test;
            ImageLoader.getInstance().displayImage(avatarurl,iv_nav_header_userface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());
        }else{
            ImageLoader.getInstance().displayImage(avatarurl,iv_nav_header_userface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());
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
        initListener();
        checkVersion();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        ll_nav_header = (RelativeLayout) headerView.findViewById(R.id.ll_nav_header);
        iv_nav_header_vip = (ImageView) headerView.findViewById(R.id.iv_nav_header_vip);
        tv_nav_header_showname = (TextView) headerView.findViewById(R.id.tv_nav_header_showname);
        iv_nav_header_background = (ImageView) headerView.findViewById(R.id.iv_nav_header_background);
        iv_nav_header_userface = (ImageView) headerView.findViewById(R.id.iv_nav_header_userface);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    private void initListener() {
//        // 注册重复登录 的动态广播消息
//        IntentFilter filter_dynamic = new IntentFilter();
//        filter_dynamic.addAction(Const.RELOGIN_BROADCAST_ACTION);
//        registerReceiver(dynamicReceiver, filter_dynamic);
//
//        //绑定服务
//        Intent service = new Intent(MainActivity.this, IMService.class);
//        //绑定
//        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 广播接受者
     */
    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if(intent.getAction().equals(Const.RELOGIN_BROADCAST_ACTION)){
//                //停止服务
//                Intent imservice = new Intent(MainActivity.this,IMService.class);
//                stopService(imservice);
//
//
//                final String msg = intent.getStringExtra("msg");
//                Intent intent_dialog = new Intent(MainActivity.this, DialogActivity.class);
//                Bundle bundle = new Bundle();
//                DialogBean dialogBean = new DialogBean();
//                dialogBean.setTitle("提示");
//                dialogBean.setContent(msg);
//                dialogBean.setDialogType(DialogBean.DialogType.toTick);
//                dialogBean.setButtonType(DialogBean.ButtonType.onebutton);
//                bundle.putSerializable("dialogBean", dialogBean);
//                intent_dialog.putExtras(bundle);
//                startActivity(intent_dialog);
//            }
        }
    };

    /**
     * 初始化数据
     */
    private void initData() {
        //模拟数据
//        for (int i = 0; i < 10; i++) {
//            int count = (int) (Math.random()*20+1);
//            if(i%2==0) {
//                ModuleBean bean = new ModuleBean();
//                bean.setType(ModuleBean.TYPE_HEADER);
//                bean.setHeader("标题分类"+i);
//                mModuleBeanList.add(bean);
//            }else {
//                for (int j = 0; j < count; j++) {
//                    ModuleBean bean = new ModuleBean();
//                    bean.setType(ModuleBean.TYPE_BUTTON);
//                    bean.setId(j+"");
//                    bean.setTitle("功能"+j);
//                    bean.setIcon("icon"+j);
//                    bean.setAction("action"+j);
//                    bean.setArg(null);
//                    mModuleBeanList.add(bean);
//                }
//            }
//        }


        String jsonObj = "[\n" +
                "    {\n" +
                "        \"header\": \"常用功能\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"实名认证\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"数字签名\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action2\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"扫一扫\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"个人信息变更\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"单位信息变更\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"单位信息查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"社会保障卡查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"异动新增\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"异动减少\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"异动申报及查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"个人基本信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"个人应缴实缴\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"信息变更\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"单位信息变更\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"个人信息变更\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"社会保障卡\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"采集人员花名册\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"已核准人员查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action2\",\n" +
                "        \"icon\": \"icon2\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"title\": \"未核准人员查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"已提交制卡查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action4\",\n" +
                "        \"icon\": \"icon4\",\n" +
                "        \"id\": \"4\",\n" +
                "        \"title\": \"已完成制卡查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action4\",\n" +
                "        \"icon\": \"icon4\",\n" +
                "        \"id\": \"4\",\n" +
                "        \"title\": \"已领卡人员查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action4\",\n" +
                "        \"icon\": \"icon4\",\n" +
                "        \"id\": \"4\",\n" +
                "        \"title\": \"单位信息查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"异动申报\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"异动新增\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"异动减少\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action2\",\n" +
                "        \"icon\": \"icon2\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"title\": \"异动申报及查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"用户维护\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action4\",\n" +
                "        \"icon\": \"icon4\",\n" +
                "        \"id\": \"4\",\n" +
                "        \"title\": \"异动统计\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action5\",\n" +
                "        \"icon\": \"icon5\",\n" +
                "        \"id\": \"5\",\n" +
                "        \"title\": \"授权异动审核\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"申报验证\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"企业申报\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"机关申报\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action2\",\n" +
                "        \"icon\": \"icon2\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"title\": \"预约查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"退休基数申报\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"单位查询\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"单位基本信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"单位参保信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action2\",\n" +
                "        \"icon\": \"icon2\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"title\": \"人员花名册\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"单位年审信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"人员异动信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"月应缴明细\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"缴费通知单\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"单位补退明细\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"到账汇总信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"到账险种信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"到账分配信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"单位应缴实缴\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"单位欠费信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"推送催缴信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"个人查询\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"基本信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"应缴实缴\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action2\",\n" +
                "        \"icon\": \"icon2\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"title\": \"养老账户\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"医保划账\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"个人账户单\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"报销回执单\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"社保卡信息\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"新单位参保\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"用户注册\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action1\",\n" +
                "        \"icon\": \"icon1\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"title\": \"参保登记\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action2\",\n" +
                "        \"icon\": \"icon2\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"title\": \"进度查询\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"预审审核\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action3\",\n" +
                "        \"icon\": \"icon3\",\n" +
                "        \"id\": \"3\",\n" +
                "        \"title\": \"初审审核\",\n" +
                "        \"type\": 2\n" +
                "    },\n" +
                "    {\n" +
                "        \"header\": \"缴费\",\n" +
                "        \"type\": 1\n" +
                "    },\n" +
                "    {\n" +
                "        \"action\": \"action0\",\n" +
                "        \"icon\": \"icon0\",\n" +
                "        \"id\": \"0\",\n" +
                "        \"title\": \"网银缴费\",\n" +
                "        \"type\": 2\n" +
                "    }\n" +
                "]" ;




        Gson gson = SystemSingleton.getInstance().getGson();
        mModuleBeanList = gson.fromJson(jsonObj, new TypeToken<List<ModuleBean>>() {}.getType());
        System.out.println(mModuleBeanList);
        //String json = gson.toJson(mModuleBeanList);
        //System.out.println("json:"+json);
    }

    /**
     * 检测版本
     */
    private void checkVersion() {
//        ThreadUtils.runInThread(new Runnable() {
//            @Override
//            public void run() {
//                String app_package_flag = getResources().getString(R.string.app_package_flag);
//                final UpdateBean updateBean = HttpUtil.okhttpGet_UpdateInfo(app_package_flag);
//                if(updateBean!=null){
//                    //获取应用程序的版本号
//                    int verCode = AppUtils.getVerCode(MainActivity.this);
//                    if(updateBean.getVercode()>verCode){
//                        //有新版本
//                        SpUtils.put(Const.UPDATE,0);
//                        //弹出对话框
//                        ThreadUtils.runInUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                                builder.setTitle("提示");
//                                builder.setMessage(updateBean.getDesc());
//                                builder.setPositiveButton("立即升级！", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        //下载升级文件
//                                        new DownloadAPK(updateBean.getFilesize()).execute(updateBean.getDownloadurl());
//                                    }
//                                });
//                                builder.setNegativeButton("下次再说。", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        //取消
//                                    }
//                                });
//                                builder.show();
//                            }
//                        });
//                    }else{
//                        //已经是最新版本
//                        SpUtils.put(Const.UPDATE,1);
//                    }
//                    SpUtils.put(Const.UPDATEVERCODE,updateBean.getVercode());
//                    SpUtils.put(Const.UPDATEDESC, updateBean.getDesc());
//                    SpUtils.put(Const.UPDATEDOWNLOADURL,updateBean.getDownloadurl());
//                    SpUtils.put(Const.UPDATEFILESIZE,updateBean.getFilesize());
//                }
//            }
//        });
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
                break;
            }
            case R.id.action_addgroup: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // 处理抽屉导航菜单的点击事件.
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            //////System.out.println("点击了我的资料菜单");
        } else if (id == R.id.nav_blog) {
            //////System.out.println("点击了我的动态菜单");
        } else if (id == R.id.nav_fav) {
            //////System.out.println("点击了我的收藏菜单");
        } else if (id == R.id.nav_setting) {
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





//    private long firstTime = 0;
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
//            IMService.MyBinder binder = (IMService.MyBinder) service;
//            //拿到绑定的服务接口
//            mImService = binder.getService();
//            //群聊邀请的监听
//            mImService.addInvitationListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        String type = intent.getStringExtra(Const.TYPE);
//        String notification_from = intent.getStringExtra(Const.NOTIFICATION_FROM);
//        String nickName = intent.getStringExtra(Const.NICKNAME);
//        String avatarurl = intent.getStringExtra(Const.AVATARURL);
//        String room_jid = intent.getStringExtra(Const.GROUP_JID);
//        if(room_jid==null) room_jid = "";
//        String room_name = (room_jid.equals(""))?"":AsmackUtils.filterGroupName(room_jid)+"";
//
//        if(type==null || type.equals("") || type.equals("null")) return;
//
//        NotificationUtilEx.getInstance().removeNotificationMapNode(notification_from);
//
//        if(!type.equals("") && !notification_from.equals("")  && !nickName.equals("")  && !avatarurl.equals("") && type.equals(Message.Type.chat.name())){
//            //跳转到私聊
//            if(mImService.isMyFriends(notification_from)){
//                Intent intent_chat = new Intent(this,ChatActivity.class);
//                intent_chat.putExtra(ChatActivity.F_ACCOUNT,notification_from);
//                intent_chat.putExtra(ChatActivity.F_NICKNAME,nickName);
//                intent_chat.putExtra(ChatActivity.F_AVATARURL,avatarurl);
//                startActivity(intent_chat);
//                return;
//            }
//        }
//
//        if(!type.equals("") && !notification_from.equals("")  && !nickName.equals("")  && !avatarurl.equals("") && type.equals(Presence.Type.subscribe.name())){
//            //跳转到好友申请
//            if(mImService.isMyFriends(notification_from)){
//                Intent intent_chat = new Intent(this,ChatActivity.class);
//                intent_chat.putExtra(ChatActivity.F_ACCOUNT,notification_from);
//                intent_chat.putExtra(ChatActivity.F_NICKNAME,nickName);
//                intent_chat.putExtra(ChatActivity.F_AVATARURL,avatarurl);
//                startActivity(intent_chat);
//                return;
//            }else{
//                mImService.updateChatMessageToIsRead(notification_from);
//                Intent intent_invite = new Intent(this,AddFriendsAction.class);
//                intent_invite.putExtra(ChatActivity.F_ACCOUNT,notification_from);
//                intent_invite.putExtra(ChatActivity.F_NICKNAME,nickName);
//                intent_invite.putExtra(ChatActivity.F_AVATARURL,avatarurl);
//                startActivity(intent_invite);
//                return;
//            }
//        }
//
//        if(!type.equals("") && !notification_from.equals("")   &&!room_jid.equals("")  && type.equals(Message.Type.groupchat.name())){
//            //跳转到群聊
//            Intent intent_groupchat = new Intent(this, MultiChatActivity.class);
//            intent_groupchat.putExtra(MultiChatActivity.F_ROOM_JID,room_jid);
//            intent_groupchat.putExtra(MultiChatActivity.F_ROOM_NAME,room_name);
//            intent_groupchat.putExtra(MultiChatActivity.F_ROOM_AVATARURL,avatarurl);
//            startActivity(intent_groupchat);
//        }
    }


    /**
     * 初始化 经典的 底部导航
     * @param savedInstanceState
     */
    private void initClassBottomNavigation(Bundle savedInstanceState) {
        //初始化Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new MainFragment())
                    .commit();
        }
    }

//-----------GoogleBottonNavigation ------------
    /**
     * 初始化 google 风格的底部导航
     */
    private void initGoogleBottomNavigation(){
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        viewPager = (AHBottomNavigationViewPager) findViewById(R.id.view_pager);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        //更新导航底部颜色的参数
        updateBottomNavigationColor(true);

        if (useMenuResource) {
            tabColors = getResources().getIntArray(R.array.tab_colors);
            navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu_3);
            navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors);
        } else {
            AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_vector_home_24dp, R.color.color_tab_1);
            AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.ic_vector_coffee_24dp, R.color.color_tab_2);
            AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_vector_user_24dp, R.color.color_tab_3);

            bottomNavigationItems.add(item1);
            bottomNavigationItems.add(item2);
            bottomNavigationItems.add(item3);

            bottomNavigation.addItems(bottomNavigationItems);
        }

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                //从适配器中得到当前fragment
                if (currentFragment == null) {
                    currentFragment = ViewPagerAdapter.getCurrentFragment();
                }
                //刷新列表：平滑滚动到0位置
                if (wasSelected) {
                    currentFragment.refresh();
                    return true;
                }
                //fragment渐隐
                if (currentFragment != null) {
                    currentFragment.willBeHidden();
                }
                //设置viewpage当前索引，并，不滚动，直接切换
                viewPager.setCurrentItem(position, false);
                //从适配器中得到当前fragment
                currentFragment = ViewPagerAdapter.getCurrentFragment();
                //fragment渐显
                currentFragment.willBeDisplayed();

                if (position == 1) {
                    bottomNavigation.setNotification("<>", 1);

                    floatingActionButton.setVisibility(View.VISIBLE);
                    floatingActionButton.setAlpha(0f);
                    floatingActionButton.setScaleX(0f);
                    floatingActionButton.setScaleY(0f);
                    floatingActionButton.animate()
                            .alpha(1)
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    floatingActionButton.animate()
                                            .setInterpolator(new LinearOutSlowInInterpolator())
                                            .start();
                                }
                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }
                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }
                            })
                            .start();

                } else {
                    if (floatingActionButton.getVisibility() == View.VISIBLE) {
                        floatingActionButton.animate()
                                .alpha(0)
                                .scaleX(0)
                                .scaleY(0)
                                .setDuration(300)
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                    }
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        floatingActionButton.setVisibility(View.GONE);
                                    }
                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        floatingActionButton.setVisibility(View.GONE);
                                    }
                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                    }
                                })
                                .start();
                    }
                }
                return true;
            }
        });

        bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
            @Override public void onPositionChange(int y) {
                //Log.d("DemoActivity", "BottomNavigation 的位置: " + y);
            }
        });
        //设置viewPage的缓存
        viewPager.setOffscreenPageLimit(4);
        ViewPagerAdapter = new GoogleBottomNavigationViewPagerAdapter(getSupportFragmentManager() ,mModuleBeanList);
        viewPager.setAdapter(ViewPagerAdapter);
        currentFragment = ViewPagerAdapter.getCurrentFragment();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AHNotification notification = new AHNotification.Builder()
                        .setText(":)")
                        .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.holo_red_dark))
                        .setTextColor(ContextCompat.getColor(getContext(),R.color.white ))
                        .build();
                bottomNavigation.setNotification(notification, 1);
                Snackbar.make(bottomNavigation, "系统数据初始化完成...",Snackbar.LENGTH_SHORT).show();
            }
        }, 3000);



    }



    /**
     * 更新导航底部颜色的参数
     */
    public void updateBottomNavigationColor(boolean isColored) {
        bottomNavigation.setColored(isColored);
    }
    /**
     * 返回如果底部导航是彩色的
     */
    public boolean isBottomNavigationColored() {
        return bottomNavigation.isColored();
    }
    /**
     * 显示或隐藏底部导航与动画
     */
    public void showOrHideBottomNavigation(boolean show) {
        if (show) {
            bottomNavigation.restoreBottomNavigation(true);
        } else {
            bottomNavigation.hideBottomNavigation(true);
        }
    }

    /**
     *  显示或隐藏选定的项目背景
     */
    public void updateSelectedBackgroundVisibility(boolean isVisible) {
        bottomNavigation.setSelectedBackgroundVisible(isVisible);
    }
    /**
     * 显示或隐藏选定的项目背景
     */
    public void setForceTitleHide(boolean forceTitleHide) {
        bottomNavigation.setForceTitlesHide(forceTitleHide);
    }

    /**
     * 返回条目的数量在底部导航
     */
    public int getBottomNavigationNbItems() {
        return bottomNavigation.getItemsCount();
    }

    /**
     * 添加或删除条目的底部导航
     * @param addItems
     */
    public void updateBottomNavigationItems(boolean addItems) {

        if (useMenuResource) {
            if (addItems) {
                navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu_5);
                navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors);
                bottomNavigation.setNotification("1", 3);
            } else {
                navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu_3);
                navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors);
            }

        } else {
            if (addItems) {
                //AHBottomNavigationItem item4 = new AHBottomNavigationItem(getString(R.string.tab_4),ContextCompat.getDrawable(getContext(), R.drawable.ic_1_24dp), ContextCompat.getColor(getContext(), R.color.color_tab_4));
                //AHBottomNavigationItem item5 = new AHBottomNavigationItem(getString(R.string.tab_5),ContextCompat.getDrawable(getContext(), R.drawable.ic_2_24dp), ContextCompat.getColor(getContext(), R.color.color_tab_5));
                AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_4, R.drawable.ic_vector_coffee_24dp, R.color.color_tab_4);
                AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_5, R.drawable.ic_vector_user_24dp, R.color.color_tab_5);

                bottomNavigation.addItem(item4);
                bottomNavigation.addItem(item5);
                bottomNavigation.setNotification("1", 3);
            } else {
                bottomNavigation.removeAllItems();
                bottomNavigation.addItems(bottomNavigationItems);
            }
        }
    }

}
