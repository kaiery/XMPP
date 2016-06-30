package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.UserBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.VipResouce;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by 范张 on 2016-03-22.
 */
public class AddFriendsAction extends AppCompatActivity implements View.OnClickListener {

    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;

    private Toolbar mToolbarActAddfriendAction;
    private ImageView mIvFriendsprofileUserface;
    private TextView mTvFriendsprofileShowname;
    private Button mBtAddFriendsFindfriendReject;
    private Button mBtAddFriendsFindfriendAgree;
    private ImageView iv_friendsprofile_vip;
    private TextView tv_friendsprofile_orgname;
    private String account;
    private String nickname;
    private String avatarurl;

    private void assignViews() {
        mToolbarActAddfriendAction = (Toolbar) findViewById(R.id.toolbar_act_addfriend_action);
        mIvFriendsprofileUserface = (ImageView) findViewById(R.id.iv_friendsprofile_userface);
        mTvFriendsprofileShowname = (TextView) findViewById(R.id.tv_friendsprofile_showname);
        mBtAddFriendsFindfriendReject = (Button) findViewById(R.id.bt_add_friends_findfriend_reject);
        mBtAddFriendsFindfriendAgree = (Button) findViewById(R.id.bt_add_friends_findfriend_agree);
        iv_friendsprofile_vip = (ImageView) findViewById(R.id.iv_friendsprofile_vip);
        tv_friendsprofile_orgname = (TextView) findViewById(R.id.tv_friendsprofile_orgname);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_action);

        init();

        assignViews();

        //获取传递的用户登录信息
        Intent intent = this.getIntent();
        account = intent.getStringExtra(ChatActivity.F_ACCOUNT);
        nickname = intent.getStringExtra(ChatActivity.F_NICKNAME);
        avatarurl = intent.getStringExtra(ChatActivity.F_AVATARURL);

        //给页面设置工具栏
        mToolbarActAddfriendAction.setTitle("好友申请");
        setSupportActionBar(mToolbarActAddfriendAction);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mTvFriendsprofileShowname.setText(nickname);
        //用户头像
        if(avatarurl==null){
            avatarurl = "drawable://"  +R.drawable.useravatar;
        }

        ImageLoader.getInstance().displayImage(avatarurl,mIvFriendsprofileUserface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());

        mBtAddFriendsFindfriendReject.setOnClickListener(this);
        mBtAddFriendsFindfriendAgree.setOnClickListener(this);
        mBtAddFriendsFindfriendReject.setEnabled(false);
        mBtAddFriendsFindfriendAgree.setEnabled(false);

        new queryOracle_queryUserInfoByUsername_AsyncTask().execute(account.substring(0,account.lastIndexOf("@")));
    }

    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.bt_add_friends_findfriend_agree: {
                if(mImService!=null){
                    //同意添加好友
                    mImService.sendPresence(Presence.Type.subscribed,account);
                    mImService.sendPresence(Presence.Type.subscribe,account);
                    AsmackUtils.setPresence(Presence.Mode.available);
                    finish();
                }
                break;
            }
            case R.id.bt_add_friends_findfriend_reject: {
                if(mImService!=null){
                    //拒绝添加好友
                    mImService.sendPresence(Presence.Type.unsubscribe,account);
                    mImService.sendPresence(Presence.Type.unsubscribed,account);
                    finish();
                }
                break;
            }
        }
    }


    /**
     * 查询oracle,
     * 1、获取此用户
     */
    public class queryOracle_queryUserInfoByUsername_AsyncTask extends AsyncTask<String ,Void,UserBean> {
        @Override
        protected UserBean doInBackground(String... params) {
            return HttpUtil.okhttpPost_QueryUserInfoByUsername(params[0]);
        }

        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            if(userBean!=null){
                if(  (userBean.getVip()+"").equals("0")){
                    iv_friendsprofile_vip.setVisibility(View.GONE);
                }else{
                    iv_friendsprofile_vip.setImageResource(VipResouce.getVipResouce(userBean.getVip()+""));
                }
                tv_friendsprofile_orgname.setText(userBean.getOrgname());

            }
        }
    }


//    /**
//     * 插入oracle：添加好友关系，
//     * 插入sqlite，添加好友关系
//     */
//    public class Insert_AddFriendsAction_AsyncTask extends AsyncTask<Void,Void,Void>{
//        @Override
//        protected Void doInBackground(Void... params) {
//            boolean b;
//            try {
//                String pid = UUID.randomUUID().toString();
//                //插入oralce，添加好友关系(仅限一条数据)
//                String stamp = HttpUtil.okhttpPost_Insert_ApplyFriend(mUserBean.getUserid(),pid);
//                long result_dao;
//                if(stamp!=null){
//                    //3、插入本地数据库，添加好友关系（仅限一条数据）,好友联系人（仅限一条）
//                    result_dao = dao.insert_ApplyFriend(mUserBean,stamp,pid);
//                    b = result_dao > 0 ;
//                }else{
//                    b= false ;
//                }
//                if(b){
//                    //刷新好友列表，添加新增的好友
//                    //获取好友bean
//                    FriendInfoBean fib = new FriendInfoBean();
//                    fib.setOp("");
//                    fib.setOrgid("");
//                    fib.setUserid(mUserBean.getUserid());
//                    fib.setShowname(mUserBean.getShowname());
//                    fib.setPinyin(mUserBean.getPhonetic().substring(0, 1).toUpperCase());
//                    fib.setPhonetic(mUserBean.getPhonetic());
//                    fib.setVip(mUserBean.getVip());
//                    fib.setUsertype(mUserBean.getUsertype());
//                    fib.setUserface(mUserBean.getUserface());
//                    //发送广播，刷新列表
//                    SendBroadCast.sendBroadcast_friend(AddFriendsAction.this, fib, SendBroadCast.RECEIVE_FRIENDS_ADD);
//                    //发送消息，告知对方我已经接受了好友申请
//                    ActionManager.getInstance().acceptFriend(mUserBean.getUserid());
//                }
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aBoolean) {
//            super.onPostExecute(aBoolean);
//            Intent intent = new Intent(AddFriendsAction.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    }





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
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();



            //判断对方是否已经是我的好友
            if(mImService.isMyFriends(account)){
                mBtAddFriendsFindfriendReject.setEnabled(false);
                mBtAddFriendsFindfriendAgree.setEnabled(false);
            }else{
                mBtAddFriendsFindfriendReject.setEnabled(true);
                mBtAddFriendsFindfriendAgree.setEnabled(true);
            }

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
