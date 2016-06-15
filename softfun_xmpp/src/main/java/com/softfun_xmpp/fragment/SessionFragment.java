package com.softfun_xmpp.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.activity.AddFriendsAction;
import com.softfun_xmpp.activity.AddGroupsAction;
import com.softfun_xmpp.activity.ChatActivity;
import com.softfun_xmpp.activity.MultiChatActivity;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.dbhelper.ContactsDbHelper;
import com.softfun_xmpp.dbhelper.GroupDbHelper;
import com.softfun_xmpp.dbhelper.SmsDbHelper;
import com.softfun_xmpp.fragment.autoviewpager.AutoViewPagerFragment;
import com.softfun_xmpp.fragment.btngrid.BtnGridViewFragment;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.provider.SmsProvider;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.DateUtil;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.VipResouce;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Date;

import static com.tb.emoji.EmojiUtil.getFaceText;

/**
 * 会话Fragment
 */
public class SessionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private ListView mLv;
    private MyCursorAdapter mAdapter;
    private View mFragmentView;
    /**
     * 通过绑定服务得到的服务实例
     */
    private IMService mImService;
    private LoaderManager manager = null;

    public SessionFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    /**
     * Fragment中的布局被移除时调用
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mFragmentView) {
            ((ViewGroup) mFragmentView.getParent()).removeView(mFragmentView);
        }
    }

    @Override
    public void onDestroy() {
        unRegisterContentObserver();
        //解绑服务
        if (mMyServiceConnection != null) {
            getActivity().unbindService(mMyServiceConnection);
        }
        super.onDestroy();
    }

    private void init() {
        registerContentObserver();
        //绑定服务
        Intent service = new Intent(getActivity(), IMService.class);
        //绑定
        getActivity().bindService(service, mMyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mFragmentView) {
            mFragmentView = inflater.inflate(R.layout.fragment_session, container, false);

            if (savedInstanceState == null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_autoviewpager, new AutoViewPagerFragment())
                        .add(R.id.fragment_btn, new BtnGridViewFragment())
                        .commit();
            }
            initView(mFragmentView);
        }
        return mFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }







    private void initView(View view) {
        mLv = (ListView) view.findViewById(R.id.lv);
    }

    private void initData() {

        //先给mAdapter构造方法内一个空的cursor
        mAdapter = new MyCursorAdapter(getActivity(),null);
        mLv.setAdapter(mAdapter);
        //初始化加载器管理器
        manager = getLoaderManager();

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                while (!IMService.isCreate){
                    SystemClock.sleep(500);
                }
                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //初始化Loader
                        manager.initLoader(0,null,SessionFragment.this);
                    }
                });
            }
        });
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = new String[]{IMService.mCurAccount,IMService.mCurAccount};
        return new CursorLoader(getContext(), SmsProvider.URI_SESSION, null, null, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data!=null){
            if(data.getCount()>0){
                mAdapter.swapCursor(data);
            }else{
                mAdapter.swapCursor(null);
            }
        }else{
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mAdapter!=null)
            mAdapter.swapCursor(null);
    }


    /**
     * 设置或更新Adapter
     */
    private void setOrUpdateAdapter() {
        System.out.println("====================  manager.restartLoader  SessionFragment =====================");
        manager.restartLoader(0,null,this);
    }

    /**
     * 适配器
     */
    private class MyCursorAdapter extends CursorAdapter {

        public MyCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }


        private  class   ViewHolder  {
            ImageView iv_avater ;
            ImageView iv_vip ;
            TextView tv_nickname;
            TextView tv_body;
            TextView tv_stamp;
            TextView tv_unreaded;
        }

        //如果converView = null 的时候，返回一个具体的根视图
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = View.inflate(context, R.layout.item_layout_fragment_session, null);

            ViewHolder holder  =   new ViewHolder();
            holder.iv_avater = (ImageView) view.findViewById(R.id.iv_avater);
            holder.iv_vip = (ImageView) view.findViewById(R.id.iv_vip);
            holder.tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);
            holder.tv_body = (TextView) view.findViewById(R.id.tv_body);
            holder.tv_stamp = (TextView) view.findViewById(R.id.tv_stamp);
            holder.tv_unreaded = (TextView) view.findViewById(R.id.tv_unreaded);
            view.setTag(holder);
            return view;
        }
        //设置数据，显示数据
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder)view.getTag();

            String body = cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.BODY));
            String account = cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.SESSION_ACCOUNT));
            String stamp = cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.TIME));
            String unread =cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.UNREAD_MSG_COUNT));
            if(unread.equals("0")){
                holder.tv_unreaded.setVisibility(View.GONE);
            }else{
                holder.tv_unreaded.setVisibility(View.VISIBLE);
            }
            holder.tv_unreaded.setText(unread);


            /*====================  判断 强制显示 群邀请、好友申请 的昵称  =====================*/
            String msgflag = cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.FLAG))+"";
            String type = cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.TYPE))+"";

            //昵称================================================
            String nickname;
            //正常群聊
            if(!msgflag.equals(Const.MSGFLAG_GROUP_INVITE) && type.equals(Message.Type.groupchat.name())){
                nickname = cursor.getString(cursor.getColumnIndex(SmsDbHelper.SmsTable.ROOM_NAME));
            }else
                //群邀请
                if(msgflag.equals(Const.MSGFLAG_GROUP_INVITE) && type.equals(Message.Type.groupchat.name())){
                    nickname = getResources().getString(R.string.groupchat_invite);
                }else
                    //好友邀请
                    if(type.equals(Presence.Type.subscribe.name())){
                        nickname = getResources().getString(R.string.chat_invite);
                    }else{
                        //正常私聊
                        nickname = AsmackUtils.getFieldByAccountFromContactTable(account,ContactsDbHelper.ContactTable.NICKNAME);
                    }
            holder.tv_nickname.setText(nickname);

            holder.tv_stamp.setText(DateUtil.formatFriendly( new Date(Long.parseLong(stamp)) ));

            //头像================================================
            String avatar ="";
            //群头像
            if(type.equals(Message.Type.groupchat.name())){
                avatar = AsmackUtils.getFieldByGroupJidFromGroupTable(account+Const.ROOM_JID_SUFFIX, GroupDbHelper.GroupTable.FACE);
            }else
                //私聊头像
                if(type.equals(Message.Type.chat.name())){
                    avatar = AsmackUtils.getFieldByAccountFromContactTable(account,ContactsDbHelper.ContactTable.AVATARURL);
                }
            if(avatar==null || avatar.equals("")) {
                avatar = "drawable://" + R.drawable.useravatar;
            }
            ImageLoader.getInstance().displayImage(avatar,holder.iv_avater, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());

            String vip = AsmackUtils.getFieldByAccountFromContactTable(account,ContactsDbHelper.ContactTable.VIP);
            if(vip==null || vip.equals("0")){
                holder.iv_vip.setVisibility(View.GONE);
            }else{
                holder.iv_vip.setVisibility(View.VISIBLE);
                holder.iv_vip.setImageResource(VipResouce.getVipResouce(vip));
            }
            if(msgflag.equals(Const.MSGFLAG_IMG)){
                body = "图片";
            }
            holder.tv_body.setText(getFaceText(getActivity(),body));
        }


        public String getPositionValue(int position,String field){
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex( field ));
        }
    }



    private void initListener() {
        //点击进入聊天界面
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String account = mAdapter.getPositionValue(position,SmsDbHelper.SmsTable.SESSION_ACCOUNT);//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.SESSION_ACCOUNT));
                String type = mAdapter.getPositionValue(position,SmsDbHelper.SmsTable.TYPE);                    //mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.TYPE));
                final String nickName = AsmackUtils.getFieldByAccountFromContactTable(account, ContactsDbHelper.ContactTable.NICKNAME);
                final String avatar = AsmackUtils.getFieldByAccountFromContactTable(account,ContactsDbHelper.ContactTable.AVATARURL);
                //如果是私聊 聊天消息
                if(type.equals(Message.Type.chat.name())){
                    Intent intent = new Intent(getActivity(),ChatActivity.class);
                    intent.putExtra(ChatActivity.F_ACCOUNT,account);
                    intent.putExtra(ChatActivity.F_NICKNAME,nickName);
                    intent.putExtra(ChatActivity.F_AVATARURL,avatar);
                    startActivity(intent);
                }
                //如果是订阅消息
                if(type.equals(Presence.Type.subscribe.name())){
                    //如果已经是我的好友了，双方互加了
                    if(mImService.isMyFriends(account)){
                        Intent intent = new Intent(getActivity(),ChatActivity.class);
                        intent.putExtra(ChatActivity.F_ACCOUNT,account);
                        intent.putExtra(ChatActivity.F_NICKNAME,nickName);
                        intent.putExtra(ChatActivity.F_AVATARURL,avatar);
                        startActivity(intent);
                    }else{
                        mImService.updateChatMessageToIsRead(account);
                        Intent intent = new Intent(getActivity(),AddFriendsAction.class);
                        intent.putExtra(ChatActivity.F_ACCOUNT,account);
                        intent.putExtra(ChatActivity.F_NICKNAME,nickName);
                        intent.putExtra(ChatActivity.F_AVATARURL,avatar);
                        startActivity(intent);
                    }
                }
                //如果是群组消息
                if(type.equals(Message.Type.groupchat.name())){
                    final String room_jid = mAdapter.getPositionValue(position,SmsDbHelper.SmsTable.ROOM_JID);;//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.ROOM_JID));
                    final String room_name = mAdapter.getPositionValue(position,SmsDbHelper.SmsTable.ROOM_NAME);;//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.ROOM_NAME));
                    final String room_face = AsmackUtils.getFieldByGroupJidFromGroupTable(account+Const.ROOM_JID_SUFFIX, GroupDbHelper.GroupTable.FACE);
                    String msgflag = mAdapter.getPositionValue(position,SmsDbHelper.SmsTable.FLAG);;//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FLAG));
                    if(msgflag.equals(Const.MSGFLAG_GROUP_INVITE)){
                        //System.out.println("--------------------------------这是群聊邀请消息");
                        ThreadUtils.runInThread(new Runnable() {
                            @Override
                            public void run() {
                                //判断是否已经加入了此群
                                if(HttpUtil.okhttpPost_queryIsGroupMember(room_jid, AsmackUtils.filterAccountToUserName(IMService.mCurAccount))){
                                    //如果我已经加入了此群
                                    //进入群聊聊天界面
                                    Intent intent = new Intent(getContext(), MultiChatActivity.class);
                                    intent.putExtra(MultiChatActivity.F_ROOM_JID, room_jid);
                                    intent.putExtra(MultiChatActivity.F_ROOM_NAME, room_name);
                                    startActivity(intent);
                                }else{
                                    String msg = mAdapter.getPositionValue(position,SmsDbHelper.SmsTable.BODY);;//mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.BODY));
                                    String from_account = mAdapter.getPositionValue(position,SmsDbHelper.SmsTable.FROM_ACCOUNT);;// mCursor.getString(mCursor.getColumnIndex(SmsDbHelper.SmsTable.FROM_ACCOUNT));
                                    //我没有加入，等待我的决定，是否加入？
                                    mImService.updateMultiChatMessageToIsRead(room_jid);
                                    //进入群邀请回应界面
                                    Intent intent = new Intent(getActivity(),AddGroupsAction.class);
                                    intent.putExtra(MultiChatActivity.F_ROOM_JID,account);
                                    intent.putExtra(MultiChatActivity.F_ROOM_NAME,nickName);
                                    intent.putExtra(MultiChatActivity.F_ROOM_AVATARURL,avatar);
                                    intent.putExtra(MultiChatActivity.F_REASON,msg);
                                    intent.putExtra(MultiChatActivity.F_FROM_ACCOUNT,from_account);
                                    startActivity(intent);
                                }
                            }
                        });
                    }else{
                        //普通群聊消息
                        Intent intent = new Intent(getContext(), MultiChatActivity.class);
                        intent.putExtra(MultiChatActivity.F_ROOM_JID,room_jid+Const.ROOM_JID_SUFFIX);
                        intent.putExtra(MultiChatActivity.F_ROOM_NAME,room_name);
                        intent.putExtra(MultiChatActivity.F_ROOM_AVATARURL,room_face);
                        startActivity(intent);
                    }
                }

            }
        });
    }




    //---------------内容观察者---------------//
    SessionContentObserver observer = new SessionContentObserver(new Handler());
    /**
     * 注册内容观察者
     */
    public void registerContentObserver(){
        //System.out.println("注册内容观察者");
        //第2个参数为true：
        //content://" + AUTHORITIES + "/contact  的孩子 content://" + AUTHORITIES + "/contact/xxxx 也会被通知
        //观察私聊消息
        getActivity().getContentResolver().registerContentObserver(SmsProvider.URI_SMS,true,observer);

        //观察群聊消息
        getActivity().getContentResolver().registerContentObserver(SmsProvider.URI_GROUPSMS,true,observer);
    }

    /**
     * 销毁内容观察者
     */
    public void unRegisterContentObserver(){
        //System.out.println("销毁内容观察者");
        getActivity().getContentResolver().unregisterContentObserver(observer);
    }


    /**
     * 联系人的内容的观察者类。
     */
    public class SessionContentObserver extends ContentObserver {
        /**
         * 创建一个内容的观察者。
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SessionContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 如果目标Uri中的内容发生改变就接收到通知
         * @param selfChange
         * @param uri
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if(IMService.isCreate){
                setOrUpdateAdapter();
            }
        }
    }
//---------------内容观察者end---------------//































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


            /*====================  获取离线消息  =====================*/
            if(IMService.mOfflineMsglist!=null){
                //System.out.println("离线消息-=-=-=-=-=-=-=-="+IMService.mOfflineMsglist.size());
                for (int i = 0; i < IMService.mOfflineMsglist.size(); i++) {
                    Message message = IMService.mOfflineMsglist.get(i);
                    //System.out.println("--离线消息：--"+"收到离线消息, Received from 【" + message.getFrom() + "】 message: " + message.getBody());
                    String session_account = AsmackUtils.filterAccount(message.getFrom());
                    mImService.saveMessage(session_account,message);
                }
                IMService.mOfflineMsglist.clear();
                IMService.mOfflineMsglist = null;
            }

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //System.out.println("==================== sessionFragment onServiceDisconnected  =====================");
        }
    }
}
