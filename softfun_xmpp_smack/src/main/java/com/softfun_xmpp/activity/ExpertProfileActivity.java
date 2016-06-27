package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.opentok.android.demo.opentoksamples.UIActivity;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.DialogBean;
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

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

public class ExpertProfileActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 专家id
     */
    private String mUserid;
    /**
     * 专家account
     */
    private String mUsername;
    private String mAccount;
    private String mAvatar;
    private String mNickName;
    private String mUserphone;


    private UserBean mUserBean;
    private ImageView backdrop_act_friendsprofile;
    //private CoordinatorLayout coordlayout_activity_friendsprofile;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView iv_friendsprofile_userface;
    private TextView tv_friendsprofile_showname;
    private TextView tv_friendsprofile_orgname;
    private TextView tv_friendsprofile_userid;
    private TextView tv_friendsprofile_score;
    private TextView tv_friendsprofile_userdesc;

    private TextView tv_address;
    private TextView tv_workinglife;
    private TextView tv_positional;
    private TextView tv_specialty;
    private TextView tv_company;
    private TextView tv_userphone;

    private ImageView iv_friendsprofile_vip;
    private TextView tv_friendsprofile_qq;
    private TextView tv_friendsprofile_email;
    private IMService mImService;
    private LinearLayout group1;
    private LinearLayout group2;
    private Button bt_msgchat;
    private Button bt_videochat;
    private Button bt_sms;
    private Button bt_call;
    private Button bt_friendsprofile_add;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_profile);
        init();
        //初始化组件
        //coordlayout_activity_friendsprofile = (CoordinatorLayout) findViewById(R.id.coordlayout_activity_friendsprofile);
        backdrop_act_friendsprofile = (ImageView) findViewById(R.id.backdrop);
        iv_friendsprofile_userface = (ImageView) findViewById(R.id.iv_avater);
        tv_friendsprofile_showname = (TextView) findViewById(R.id.tv_showname);
        tv_friendsprofile_orgname = (TextView) findViewById(R.id.tv_orgname);
        tv_friendsprofile_userid = (TextView) findViewById(R.id.tv_userid);
        tv_friendsprofile_score = (TextView) findViewById(R.id.tv_score);
        tv_friendsprofile_userdesc = (TextView) findViewById(R.id.tv_userdesc);
        iv_friendsprofile_vip = (ImageView) findViewById(R.id.iv_vip);
        tv_friendsprofile_qq = (TextView) findViewById(R.id.tv_qq);
        tv_friendsprofile_email = (TextView) findViewById(R.id.tv_email);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_workinglife = (TextView) findViewById(R.id.tv_workinglife);
        tv_positional = (TextView) findViewById(R.id.tv_positional);
        tv_specialty = (TextView) findViewById(R.id.tv_specialty);
        tv_company = (TextView) findViewById(R.id.tv_company);
        tv_userphone = (TextView) findViewById(R.id.tv_userphone);


        group1 = (LinearLayout) findViewById(R.id.ll_group1);
        group2 = (LinearLayout) findViewById(R.id.ll_group2);
        bt_call = (Button) findViewById(R.id.bt_call);
        bt_msgchat = (Button) findViewById(R.id.bt_msgchat);
        bt_videochat = (Button) findViewById(R.id.bt_videochat);
        bt_sms = (Button) findViewById(R.id.bt_sms);
        bt_friendsprofile_add = (Button) findViewById(R.id.bt_friendsprofile_add);

        bt_call.setOnClickListener(this);
        bt_msgchat.setOnClickListener(this);
        bt_videochat.setOnClickListener(this);
        bt_sms.setOnClickListener(this);
        bt_friendsprofile_add.setOnClickListener(this);


        group1.setVisibility(View.GONE);
        group2.setVisibility(View.GONE);

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



        Intent intent = this.getIntent();
        mUserid = intent.getStringExtra("userid");
        mUsername = intent.getStringExtra("username");
        mAccount = mUsername + "@" + Const.APP_PACKAGENAME;

        getFriendsInfo();
    }

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
            mUserphone = userBean.getUserphone();
            //设置imageview的tag，区别唯一
            backdrop_act_friendsprofile.setTag(backgroundURL);
            iv_friendsprofile_userface.setTag(mAvatar);
            ImageLoader.getInstance().displayImage(backgroundURL, backdrop_act_friendsprofile, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
            ImageLoader.getInstance().displayImage(mAvatar, iv_friendsprofile_userface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());


            tv_address.setText(userBean.getAddress());
            tv_workinglife.setText(userBean.getWorkinglife());
            tv_positional.setText(userBean.getPositional());
            tv_specialty.setText(userBean.getSpecialty());
            tv_company.setText(userBean.getCompany());
            tv_userphone.setText(userBean.getUserphone());


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


        }
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


    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.bt_call:
                Intent  intent2=new Intent();
                intent2.setAction("android.intent.action.CALL");
                intent2.addCategory("android.intent.category.DEFAULT");
                intent2.setData(Uri.parse("tel:"+mUserphone));
                startActivity(intent2);
                break;
            case R.id.bt_sms:
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+mUserphone));
                sendIntent.putExtra("sms_body", "");
                startActivity(sendIntent);
                break;
            case R.id.bt_videochat:
                //申请视频
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
                        msg.addExtension(jpe);
                        //调用服务内的发送消息方法
                        mImService.sendMessage(msg);
                    }
                });

                Intent intent1 = new Intent(this, UIActivity.class);
                //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent1.putExtra("mTargetNickName",mNickName);
                intent1.putExtra("mTargetAccount",mAccount.substring(0,mAccount.lastIndexOf("@"))+"@"+Const.APP_PACKAGENAME );
                startActivityForResult(intent1,Const.RESULT_VIDEO_EVALUATE);
                break;
            case R.id.bt_msgchat:
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
            case R.id.bt_friendsprofile_add:
                //申请添加好友
                AsmackUtils.addFriend(mUserBean.getUsername(),mUserBean.getShowname());
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Const.RESULT_VIDEO_EVALUATE){
            Intent intent = new Intent(this,EvaluateActivity.class);
            intent.putExtra(Const.USERNAME,mAccount.substring(0,mAccount.lastIndexOf("@")));

            Bundle bundle = new Bundle();
            DialogBean dialogBean = new DialogBean();
            dialogBean.setTitle("提示");
            dialogBean.setContent("为了提供更好的服务，请您为专家进行评价。");
            dialogBean.setButtonType(DialogBean.ButtonType.onebutton);
            bundle.putSerializable("dialogBean", dialogBean);
            intent.putExtras(bundle);

            startActivity(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
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

            group1.setVisibility(View.VISIBLE);
            group2.setVisibility(View.VISIBLE);
            //判断对方是否已经是我的好友
            if(mImService.isMyFriends(mAccount)){
                //是好友，
                bt_friendsprofile_add.setVisibility(View.GONE);
            }else{
                bt_friendsprofile_add.setVisibility(View.VISIBLE);
            }

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //System.out.println("==================== FriendsProfile onServiceDisconnected  =====================");
        }
    }
}
