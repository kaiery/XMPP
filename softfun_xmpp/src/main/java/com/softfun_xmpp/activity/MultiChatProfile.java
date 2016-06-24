package com.softfun_xmpp.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.GroupBean;
import com.softfun_xmpp.bean.GroupMemberBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.dbhelper.GroupDbHelper;
import com.softfun_xmpp.dbhelper.SmsDbHelper;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.provider.GroupProvider;
import com.softfun_xmpp.provider.SmsProvider;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToolsUtil;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.List;

public class MultiChatProfile extends AppCompatActivity implements View.OnClickListener {

    private static final int CHANGEMASTER_REQUEST_CODE = 8;
    private CoordinatorLayout coordlayoutActivity;
    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsingToolbar;
    /**
     * 背景图
     */
    private ImageView backdrop;
    /**
     * 工具兰
     */
    private Toolbar toolbar;
    /**
     * 群聊名字布局
     */
    private RelativeLayout item1;
    /**
     * 群聊名字组件
     */
    private TextView tvGroupname;
    /**
     * 群类型布局
     */
    private RelativeLayout item2;
    /**
     * 群类型组件
     */
    private TextView tvGrouptype;
    /**
     * 群类型详细组件
     */
    private TextView tvGrouptypedetail;
    /**
     * 群二维码布局
     */
    private RelativeLayout item3;
    /**
     * 群二维码组件
     */
    private ImageView ivGrouprqcode;
    /**
     * 群公告布局
     */
    private RelativeLayout item4;
    /**
     * 群公告组件
     */
    private TextView tvGroupannounce;
    /**
     * 消息免打扰组件
     */
    private SwitchCompat swh1;
    /**
     * 群成员布局
     */
    private RelativeLayout item6;
    /**
     * 我在群中的昵称布局
     */
    private RelativeLayout item7;
    /**
     * 我在群中的昵称组件
     */
    private TextView tvGroupmynickname;
    /**
     * 群主转让布局
     */
    private RelativeLayout item8;
    /**
     * 退群按钮
     */
    private Button btn;
    /**
     * 进度布局
     */
    private RelativeLayout rl;

    /**
     * 群Jid，包括了@XXXXX
     */
    private String mRoomJid;
    private String mRoomName;
    private GroupBean mGroupBean;
    private String mBackdrop;
    private String mAvatar;
    private Bundle mBundle;
    /**
     * 拿到的服务接口
     */
    private IMService mImService;

    private void assignViews() {
        coordlayoutActivity = (CoordinatorLayout) findViewById(R.id.coordlayout_activity);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        item1 = (RelativeLayout) findViewById(R.id.item1);
        tvGroupname = (TextView) findViewById(R.id.tv_groupname);
        item2 = (RelativeLayout) findViewById(R.id.item2);
        tvGrouptype = (TextView) findViewById(R.id.tv_grouptype);
        tvGrouptypedetail = (TextView) findViewById(R.id.tv_grouptypedetail);
        item3 = (RelativeLayout) findViewById(R.id.item3);
        ivGrouprqcode = (ImageView) findViewById(R.id.iv_grouprqcode);
        item4 = (RelativeLayout) findViewById(R.id.item4);
        tvGroupannounce = (TextView) findViewById(R.id.tv_groupannounce);
        swh1 = (SwitchCompat) findViewById(R.id.swh1);
        item6 = (RelativeLayout) findViewById(R.id.item6);
        item7 = (RelativeLayout) findViewById(R.id.item7);
        tvGroupmynickname = (TextView) findViewById(R.id.tv_groupmynickname);
        item8 = (RelativeLayout) findViewById(R.id.item8);
        btn = (Button) findViewById(R.id.btn);
        rl = (RelativeLayout) findViewById(R.id.rl);

        if(rl!=null){
            rl.setVisibility(View.GONE);
        }
        btn.setEnabled(false);

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_chat_profile);

        assignViews();

        mRoomJid = getIntent().getStringExtra(MultiChatActivity.F_ROOM_JID);
        mRoomName = getIntent().getStringExtra(MultiChatActivity.F_ROOM_NAME);

        init();

        initData();
    }


    private void init() {

        //绑定服务
        Intent service = new Intent(MultiChatProfile.this, IMService.class);
        //绑定
        bindService(service, mMyServiceConnection, BIND_AUTO_CREATE);


        btn.setOnClickListener(this);
        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        item3.setOnClickListener(this);
        item4.setOnClickListener(this);
        item6.setOnClickListener(this);
        item7.setOnClickListener(this);
        item8.setOnClickListener(this);
    }


    private void initData() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //获取群信息
                reGetInfo();
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //给页面设置工具栏
                        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setTitle(mRoomName);
                        setSupportActionBar(toolbar);
                        //添加返回按钮
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        //设置工具栏标题
                        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
                        collapsingToolbar.setTitle(mRoomName);
                        //通过CollapsingToolbarLayout修改字体颜色
                        collapsingToolbar.setExpandedTitleColor(Color.WHITE);//设置还没收缩时状态下字体颜色
                        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);//设置收缩后Toolbar上字体的颜色
                        //设置标题
                        collapsingToolbar.setTitle(mRoomName);

                        mBackdrop = getResources().getString(R.string.app_server) + "web/Images/groupbg/group_bg"+ ToolsUtil.getRandomRect(1,27)+".jpg";
                        ImageLoader.getInstance().displayImage(mBackdrop, backdrop, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());

                        tvGroupname.setText(mRoomName);
                        //兴趣群
                        if(mGroupBean.getGrouptype().equals("1")){
                            tvGrouptype.setText(mGroupBean.getGrouptypename());
                            tvGrouptypedetail.setText(mGroupBean.getGiname());
                            tvGrouptypedetail.setVisibility(View.VISIBLE);
                        }
                        //同城群
                        if(mGroupBean.getGrouptype().equals("2")){
                            tvGrouptype.setText(mGroupBean.getGrouptypename());
                            tvGrouptypedetail.setText(mGroupBean.getGcityname());
                            tvGrouptypedetail.setVisibility(View.VISIBLE);
                        }
                        //基础群、私密群
                        if(mGroupBean.getGrouptype().equals("0") || mGroupBean.getGrouptype().equals("3")){
                            tvGrouptype.setText(mGroupBean.getGrouptypename());
                            tvGrouptypedetail.setVisibility(View.GONE);
                        }


                        tvGroupmynickname.setText(mGroupBean.getMynickname());
                        tvGroupannounce.setText(mGroupBean.getAnnounce());

                        //消息免打扰
                        swh1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                //

                            }
                        });

                    }
                });
            }
        });
    }


    /**
     * 获取群信息
     */
    private void reGetInfo() {
        mGroupBean = HttpUtil.okhttpPost_queryGroupInfoByRoomName(AsmackUtils.filterAccountToUserName(IMService.mCurAccount),mRoomJid.substring(0, mRoomJid.lastIndexOf("@")));
        mBundle = new Bundle();
        mBundle.putParcelable(EditMultiItemActivity.PAR_KEY, mGroupBean);
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
            case R.id.item1:
//                if(IMService.mCurAccount.equals(mGroupBean.getMaster())){
//                    Intent item1 = new Intent(this,EditMultiItemActivity.class);
//                    item1.putExtras(mBundle);
//                    item1.putExtra(EditMultiItemActivity.ITEM_NAME,"群聊名称");
//                    item1.putExtra(EditMultiItemActivity.ITEM_FIELD, Const.GROUP_FIELD_NAME);
//                    item1.putExtra(EditMultiItemActivity.MAX_LENGTH, 127);
//                    startActivityForResult(item1,EditMultiItemActivity.REQUESTCODE);
//                }
                break;
            case R.id.item2:
                if(IMService.mCurAccount.equals(mGroupBean.getMaster())){
                    //工厂模式
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("请选择");
                    final String[] items = {"编辑类型","编辑详细"};
                    builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(getApplicationContext(), "您点击了"+items[which], 0).show();
                            String groupFieldType ;
                            if(which==0){
                                groupFieldType = Const.GROUP_FIELD_TYPE;
                            }else{
                                groupFieldType = Const.GROUP_FIELD_TYPE_DETAIL;
                            }
                            Intent item2 = new Intent(MultiChatProfile.this, EditMultiItemActivity.class);
                            item2.putExtras(mBundle);
                            item2.putExtra(EditMultiItemActivity.ITEM_NAME, "群类型");
                            item2.putExtra(EditMultiItemActivity.ITEM_FIELD, groupFieldType);
                            item2.putExtra(EditMultiItemActivity.MAX_LENGTH, 0);
                            startActivityForResult(item2, EditMultiItemActivity.REQUESTCODE);
                            //关闭对话框
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                break;
            case R.id.item3:
                //二维码
//                Intent item3 = new Intent(this,EditMultiItemActivity.class);
//                item3.putExtras(mBundle);
//                item3.putExtra(EditMultiItemActivity.ITEM_NAME,"群二维码");
//                item3.putExtra(EditMultiItemActivity.ITEM_FIELD, Const.GROUP_FIELD_QRCODE);
//                startActivity(item3);
                break;
            case R.id.item4:
                if(IMService.mCurAccount.equals(mGroupBean.getMaster())){
                    Intent item4 = new Intent(this,EditMultiItemActivity.class);
                    item4.putExtras(mBundle);
                    item4.putExtra(EditMultiItemActivity.ITEM_NAME,"群公告");
                    item4.putExtra(EditMultiItemActivity.ITEM_FIELD, Const.GROUP_FIELD_ANNOUNCE);
                    item4.putExtra(EditMultiItemActivity.MAX_LENGTH, 255);
                    startActivityForResult(item4,EditMultiItemActivity.REQUESTCODE);
                }
                break;
            case R.id.item6:
                //群成员
                Intent item5 = new Intent(this,MultiMemberActivity.class);
                item5.putExtra(Const.GROUP_JID,mRoomJid);
                item5.putExtra(Const.GROUP_FIELD_MASTER,mGroupBean.getMaster());
                startActivity(item5);
                break;
            case R.id.item7:
                //我在本群的昵称
                Intent item7 = new Intent(this,EditMultiItemActivity.class);
                item7.putExtras(mBundle);
                item7.putExtra(EditMultiItemActivity.ITEM_NAME,"我在本群的昵称");
                item7.putExtra(EditMultiItemActivity.ITEM_FIELD, Const.GROUP_FIELD_MYNICKNAME);
                item7.putExtra(EditMultiItemActivity.MAX_LENGTH, 25);
                startActivityForResult(item7,EditMultiItemActivity.REQUESTCODE);
                break;
            case R.id.item8:
                //转让群主
                if(IMService.mCurAccount.equals(mGroupBean.getMaster())){
                    Intent item8 = new Intent(this,SelectOneMemberActivity.class);
                    item8.putExtra(Const.GROUP_JID,mRoomJid);
                    item8.putExtra(Const.GROUP_FIELD_MASTER,mGroupBean.getMaster());
                    startActivityForResult(item8,CHANGEMASTER_REQUEST_CODE);
                }
                break;
            case R.id.item9:
                //聊天记录
                break;
            case R.id.item10:
                //清空聊天记录
                break;
            case R.id.btn:
                //退出并删除群
                exitOrDeleteGroup();
                break;
        }
    }

    /**
     * 退出或删除群
     */
    private void exitOrDeleteGroup() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final String groupname = AsmackUtils.filterGroupJid(mRoomJid);
                MultiUserChat multiUserChat = null;
                if(IMService.mMultiUserChatMap.containsKey(mRoomJid)){
                    multiUserChat = IMService.mMultiUserChatMap.get(mRoomJid);
                }

                //如果我是群管理员
                if(IMService.mCurAccount.equals(mGroupBean.getMaster())){
                    //群成员list
                    List<GroupMemberBean> userList = IMService.mGroupMemberMap.get(groupname);
                    //有成员
                    if (userList.size() > 0) {
                        //就是我自己，直接（oracle）删除群，删除（oracle）群成员，删除本地数据库群,再删除内存变量（mMultiUserChatMap、mGroupMemberMap）
                        if(userList.size()==1 && userList.get(0).getAccount().equals(AsmackUtils.filterAccountToUserName(IMService.mCurAccount))){
                            //xmpp离开群
                            if (multiUserChat != null && multiUserChat.isJoined()) {
                                multiUserChat.leave();
                            }
                            deleteGroupAndDeleteVarMap(groupname);
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }else{
                            //还有其他成员，提示“本群还有其他成员存在，如果继续操作，将解散本群。”
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //直接（oracle）删除群,包含删除成员。
                                    //发一个群消息，通知其他群成员，删除本地数据库群,再删除内存变量（mMultiUserChatMap、mGroupMemberMap）
                                    showMyDialog("本群还有其他成员存在，如果继续操作，将解散本群。",groupname);
                                }
                            });
                        }
                    } else {
                        //无成员,直接（oracle）删除群，删除（oracle）群成员，删除本地数据库群，再删除内存变量（mMultiUserChatMap、mGroupMemberMap）
                        //xmpp离开群
                        if (multiUserChat != null && multiUserChat.isJoined()) {
                            multiUserChat.leave();
                        }
                        deleteGroupAndDeleteVarMap(groupname);
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                }else{
                    //不是管理员，只能直接退出群，删除（oracle）群成员，发一个群消息，通知其他群成员，内存变量删除（mGroupMemberMap）我
                    LeaveGroup(groupname);
                    ThreadUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        });
    }

    /**
     * 删除oracle群组、群成员，删除内存变量（mMultiUserChatMap、mGroupMemberMap）
     * @param groupname
     */
    private void deleteGroupAndDeleteVarMap(String groupname) {
        //删除群组（oracle）,包含删除成员。
        int code = HttpUtil.okhttpPost_deleteGroup(groupname);
        if(code>0){
            //删除本地数据库群
            getContentResolver().delete(
                    GroupProvider.URI_GROUP,
                    GroupDbHelper.GroupTable.JID + "=? and " + GroupDbHelper.GroupTable.OWNER + "=?",
                    new String[]{ mRoomJid, IMService.mCurAccount});
            //删除本地群消息
            getContentResolver().delete(
                    SmsProvider.URI_GROUPSMS,
                    SmsDbHelper.SmsTable.TYPE +"=?  and  "+SmsDbHelper.SmsTable.ROOM_JID+" =?  and "+SmsDbHelper.SmsTable.OWNER+"  =? ",
                    new String[]{ Message.Type.groupchat.name(), groupname ,IMService.mCurAccount}
            );

            //再删除内存变量（mMultiUserChatMap、mGroupMemberMap）
            if(IMService.mGroupMemberMap!=null){
                if(IMService.mGroupMemberMap.containsKey(groupname)){
                    IMService.mGroupMemberMap.remove(groupname);
                }
            }
            if(IMService.mMultiUserChatMap!=null){
                if(IMService.mMultiUserChatMap.containsKey(mRoomJid)){
                    IMService.mMultiUserChatMap.remove(mRoomJid);
                }
            }
        }
    }

    private void showMyDialog(String msg, final String groupname) {
        //工厂模式
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //发一个群消息，通知其他群成员，删除本地数据库群,再删除内存变量（mMultiUserChatMap、mGroupMemberMap）
                //删除oracle群组、群成员，删除内存变量（mMultiUserChatMap、mGroupMemberMap）
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        //发一个群消息，通知其他群成员，删除本地数据库群,再删除内存变量（mMultiUserChatMap、mGroupMemberMap）
                        MultiUserChat multiUserChat = IMService.mMultiUserChatMap.get(groupname+Const.ROOM_JID_SUFFIX);
                        Message msg = new Message(groupname+Const.ROOM_JID_SUFFIX, org.jivesoftware.smack.packet.Message.Type.groupchat);
                        msg.setBody("");
                        msg.setProperty(Const.MSGFLAG, Const.MSGFLAG_GROUP_DISMISS);//解散
                        msg.setProperty(Const.GROUP_JID, groupname);
                        AsmackUtils.sendMultiChatMessage(multiUserChat,msg);

                        //删除oracle群组、群成员，删除内存变量（mMultiUserChatMap、mGroupMemberMap）
                        deleteGroupAndDeleteVarMap(groupname);

                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                });
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            //修改内容完毕后，进行更新界面
            if(resultCode==EditMultiItemActivity.RESULT_SUCCEED){
                String mItemField = data.getStringExtra(EditMultiItemActivity.ITEM_FIELD);
                String mItemValue = data.getStringExtra(EditMultiItemActivity.ITEM_VALUE);
                //群名称
                if (mItemField.equals(Const.GROUP_FIELD_NAME)) {
                    tvGroupname.setText(mItemValue);
                }
                //群公告
                if (mItemField.equals(Const.GROUP_FIELD_ANNOUNCE)) {
                    tvGroupannounce.setText(mItemValue);
                }
                //我的群名字
                if (mItemField.equals(Const.GROUP_FIELD_MYNICKNAME)) {
                    tvGroupmynickname.setText(mItemValue);
                }
                //详细类型
                if (mItemField.equals(Const.GROUP_FIELD_TYPE_DETAIL)) {
                    tvGrouptypedetail.setText(mItemValue);
                }
                //类型
                if (mItemField.equals(Const.GROUP_FIELD_TYPE)) {
                    tvGrouptype.setText(mItemValue);
                }
            }
            //修改内容失败
            if(resultCode==EditItemActivity.RESULT_FAILURE){

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * 离开群
     * @param mTargetRoomJid
     */
    public  void LeaveGroup(final String mTargetRoomJid){
        if(IMService.mMultiUserChatMap.containsKey(mTargetRoomJid+Const.ROOM_JID_SUFFIX)){
            String groupname = mTargetRoomJid;

            //我自己先删除oracle群成员关系
            HttpUtil.okhttpPost_deleteGroupMember(groupname, AsmackUtils.filterAccountToUserName(IMService.mCurAccount));


            MultiUserChat multiUserChat = IMService.mMultiUserChatMap.get(mTargetRoomJid+Const.ROOM_JID_SUFFIX);
            //1、创建一个消息
            //群聊消息的结构体，跟私聊不太一样，不能设置to，from，message会自动根据所在roomjid进行赋值
            Message msg = new Message(mTargetRoomJid+Const.ROOM_JID_SUFFIX, org.jivesoftware.smack.packet.Message.Type.groupchat);
            msg.setBody("");
            msg.setProperty(Const.MSGFLAG, Const.MSGFLAG_GROUP_LEAVE);
            msg.setProperty(Const.GROUP_JID, mTargetRoomJid);
            msg.setProperty(Const.ACCOUNT, IMService.mCurAccount);
            AsmackUtils.sendMultiChatMessage(multiUserChat,msg);

            //清空自己的内存变量(删除群成员)
            if(IMService.mGroupMemberMap.containsKey(groupname)){
                IMService.mGroupMemberMap.remove(groupname);
            }

            System.out.println("====================    ====================="+IMService.mMultiUserChatMap);

            //清空自己的内存变量(删除群监听)
            //拿到 服务，调用服务内的方法，移除监听
            mImService.removeMultUserChatListener(groupname+Const.ROOM_JID_SUFFIX);

            //清空自己的内存变量(删除群数组)
            int removePosition = -1;
            for (int i = 0; i < IMService.mMyGroupList.size(); i++) {
                if(IMService.mMyGroupList.get(i).getChildid().equals(groupname+Const.ROOM_JID_SUFFIX)){
                    removePosition = i;
                    break;
                }
            }
            if(removePosition!=-1){
                IMService.mMyGroupList.remove(removePosition);
            }


            //删除本地数据库的群组表，我不再参与此群
            getContentResolver().delete(
                    GroupProvider.URI_GROUP,
                    GroupDbHelper.GroupTable.JID + "=? and " + GroupDbHelper.GroupTable.OWNER + "=?",
                    new String[]{groupname+Const.ROOM_JID_SUFFIX, IMService.mCurAccount});
            //删除本地群消息
            getContentResolver().delete(
                    SmsProvider.URI_GROUPSMS,
                    SmsDbHelper.SmsTable.TYPE +"=?  and  "+SmsDbHelper.SmsTable.ROOM_JID+" =?  and "+SmsDbHelper.SmsTable.OWNER+"  =? ",
                    new String[]{ Message.Type.groupchat.name(), groupname ,IMService.mCurAccount}
            );
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        if (mMyServiceConnection != null) {
            unbindService(mMyServiceConnection);
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
            ////System.out.println("====================  onServiceConnected  =====================");
            IMService.MyBinder binder = (IMService.MyBinder) service;
            //拿到绑定的服务接口
            mImService = binder.getService();
            btn.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ////System.out.println("====================  onServiceDisconnected  =====================");
        }
    }
}
