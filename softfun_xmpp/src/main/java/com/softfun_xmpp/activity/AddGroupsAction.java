package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.GroupBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.VipResouce;

import org.jivesoftware.smackx.muc.MultiUserChat;

public class AddGroupsAction extends AppCompatActivity implements View.OnClickListener {


    private Toolbar mToolbarActAddfriendAction;
    private ImageView mIvGroupface;
    private TextView mTvGroupname;
    private ImageView mIvLvl;
    private TextView mTvGrouptypeName;
    private Button mBtReject;
    private Button mBtAgree;
    private TextView mTvMsg;

    /**
     * 有@conference.softfun的群JID
     */
    private String mRoomJid;
    private String mRoomName;
    private String mRoomAvatarurl;
    private String mReason;
    private String mFromAccount;
    private String mGrouptype;
    private String mGi;
    private String mGcity;

    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;


    private void assignViews() {
        mToolbarActAddfriendAction = (Toolbar) findViewById(R.id.toolbar_act_addfriend_action);
        mIvGroupface = (ImageView) findViewById(R.id.iv_groupface);
        mTvGroupname = (TextView) findViewById(R.id.tv_groupname);
        mIvLvl = (ImageView) findViewById(R.id.iv_lvl);
        mTvGrouptypeName = (TextView) findViewById(R.id.tv_grouptype);
        mBtReject = (Button) findViewById(R.id.bt_reject);
        mBtAgree = (Button) findViewById(R.id.bt_agree);
        mTvMsg = (TextView) findViewById(R.id.tv_msg);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_groups_action);

        assignViews();

        init();

        //获取传递的用户登录信息
        Intent intent = this.getIntent();
        mRoomJid = intent.getStringExtra(MultiChatActivity.F_ROOM_JID);
        mRoomName = intent.getStringExtra(MultiChatActivity.F_ROOM_NAME);
        mRoomAvatarurl = intent.getStringExtra(MultiChatActivity.F_ROOM_AVATARURL);
        mReason = intent.getStringExtra(MultiChatActivity.F_REASON);
        mFromAccount = intent.getStringExtra(MultiChatActivity.F_FROM_ACCOUNT);

        //给页面设置工具栏
        mToolbarActAddfriendAction.setTitle("群组邀请");
        setSupportActionBar(mToolbarActAddfriendAction);
        //添加返回按钮
        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar!=null){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTvGroupname.setText(mRoomName);
        //群头像
        if(mRoomAvatarurl==null){
            mRoomAvatarurl = "drawable://"  +R.drawable.groupface0;
        }
        ImageLoader.getInstance().displayImage(mRoomAvatarurl,mIvGroupface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());

        mBtAgree.setOnClickListener(this);
        mBtReject.setOnClickListener(this);
        mBtAgree.setEnabled(false);
        mBtReject.setEnabled(false);

        new queryOracle_queryGroupInfoByRoomName_AsyncTask().execute(AsmackUtils.filterAccountToUserName(IMService.mCurAccount)  ,mRoomJid);
    }

    private void init() {
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
     * 根据群名字，查找群的基本扩展信息
     */
    private  class queryOracle_queryGroupInfoByRoomName_AsyncTask extends AsyncTask<String,Void,GroupBean>{
        @Override
        protected GroupBean doInBackground(String... params) {
            return HttpUtil.okhttpPost_queryGroupInfoByRoomName(params[0],params[1]);
        }

        @Override
        protected void onPostExecute(GroupBean groupBean) {
            super.onPostExecute(groupBean);
            if(groupBean!=null){

                mGrouptype = groupBean.getGrouptype();
                mGi = groupBean.getGi();
                mGcity = groupBean.getGcity();
                if(TextUtils.isEmpty(mGi)){
                    mGi = ",";
                }
                if(TextUtils.isEmpty(mGcity)){
                    mGcity = ",,";
                }


                mIvLvl.setImageResource(VipResouce.getVipResouce(groupBean.getLvl()+""));
                mTvGroupname.setText(AsmackUtils.filterGroupName(groupBean.getChildid()));
                mTvMsg.setText(mReason);
                mTvGrouptypeName.setText(groupBean.getGrouptypename());
                mBtReject.setEnabled(true);
                mBtAgree.setEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId() )
        {
            case R.id.bt_reject:
                //拒绝
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        MultiUserChat.decline(IMService.conn, mRoomJid, mFromAccount, "抱歉，我不想加入。");
                    }
                });
                finish();
                break;
            case R.id.bt_agree:
                //同意并加入
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        String roomName = mRoomJid;
                        Integer code = HttpUtil.okhttpPost_insertGroup(roomName,
                                AsmackUtils.filterAccountToUserName(IMService.mCurAccount),
                                mGrouptype + "",
                                mGi,
                                mGcity);
                        if (code == 1) {
                            mImService.InitMultiRoom();
                            //发送群消息，让 本群内其他群成员，加入我的信息
                            AsmackUtils.updateOtherGroupMemberToUpdateMyInfo(mRoomJid+ Const.ROOM_JID_SUFFIX);
                            //操作成功
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }
                    }
                });

                break;
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
            //System.out.println("====================  sessionFragment onServiceConnected  =====================");
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //System.out.println("==================== sessionFragment onServiceDisconnected  =====================");
        }
    }




}
