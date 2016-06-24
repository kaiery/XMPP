package com.softfun_xmpp.connection;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.bean.GroupBean;
import com.softfun_xmpp.bean.GroupMemberBean;
import com.softfun_xmpp.bean.MUCParams;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.dbhelper.ContactsDbHelper;
import com.softfun_xmpp.dbhelper.GroupDbHelper;
import com.softfun_xmpp.dbhelper.SmsDbHelper;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.provider.ContactsProvider;
import com.softfun_xmpp.provider.GroupProvider;
import com.softfun_xmpp.provider.SmsProvider;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.MatherListUtil;
import com.softfun_xmpp.utils.PinYinUtils;
import com.softfun_xmpp.utils.SpUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.packet.DelayInformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IMService extends Service {
    /**
     * 连接对象
     */
    public static XMPPConnection conn;


    public static String mCurAccount;
    public static String mCurNickName;
    public static String mCurAvatarUrl;
    public static String mCurDesc;
    public static String mCurUserid;
    public static String mCurOrgid;
    public static String mCurOrgname;
    public static String mCurUserphone;
    public static String mCurPhonetic;
    public static String mCurIdcard;
    public static String mCurQq;
    public static String mCurEmail;
    public static String mCurBackground;
    public static String mCurScore;
    public static String mCurVip;
    public static String mCurRoletype;
    public static String mCurSn;
    public static String mCurCompany;
    public static String mCurSpecialty;
    public static String mCurPositional;
    public static String mCurWorkinglife;
    public static String mCurAddress;

    public static Map<String,List<GroupMemberBean>> mGroupMemberMap;

    /**
     * 重复登录失败次数
     */
    public static int mReLoginCount;
    /**
     * 当前的离线消息
     */
    public static List<Message> mOfflineMsglist;
    /**
     * 离线的好友申请消息数组
     */
    public static List<Presence> mOffPresenceList;
    /**
     * 离线的群聊邀请消息数组
     */
    public static List<Message> mOffGroupInviteList;
    /**
     * 当前与我正在聊天的对象的 JID
     */
    public static String chatObject = "";
    /**
     * 是否正在聊天，独占
     */
    public static boolean isVideo;

    private Roster mRoster;
    /**
     * 花名册监听器实例
     */
    private MyRosterListener mRosterListener;


    private ChatManager mChatManager;
    private Chat mCurChat;
    private Map<String, Chat> mChatMap = new HashMap<>();
    /**
     * 服务初始化完成标记
     */
    public static boolean isCreate;




    /**
     * 我参与的群组数组
     */
    public static List<GroupBean> mMyGroupList;
    /**
     * 我参与的群聊对象Map
     */
    public static Map<String, MultiUserChat> mMultiUserChatMap = new HashMap<>();

    private Map<MultiUserChat, MUCParams> mucsList = new ConcurrentHashMap<MultiUserChat, MUCParams>();
    private Map<String, MultiUserChat> mucsJIDs = new ConcurrentHashMap<String, MultiUserChat>();
    public Set<MultiUserChat> listMUCs() {
        return mucsList.keySet();
    }
    public MUCParams getMUCParams(MultiUserChat multiUserChat) {
        return mucsList.get(multiUserChat);
    }
    public Collection<MUCParams> listMUCParams() {
        return mucsList.values();
    }

    /**
     * 群聊邀请的监听
     */
    private ReaderThread mXMPPReadWorker  = new ReaderThread();




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }


    public class MyBinder extends Binder {
        /**
         * 返回服务的实例
         */
        public IMService getService() {
            return IMService.this;
        }
    }



    @Override
    public void onCreate() {
        //System.out.println("--------------service  onCreate-------------");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //System.out.println("--------------service  onStartCommand-------------");
        initService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //System.out.println("====================  服务被销毁  =====================");
        clearxx();
        conn.disconnect();
        super.onDestroy();
    }

    private void initService() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String name;
                String password ;
                /*========    从服务器同步花名册     =====*/
                if (conn == null) {
                    name = SpUtils.get(Const.USERNAME, "") + "";
                    password = SpUtils.get(Const.PASSWORD, "") + "";
                    if (name.equals("") || password.equals("")) {
                        ConnManager.getInstance().stopServce();
                        return;
                    }
                    ConnManager.getInstance().login(name, password);
                }

                //得到花名册对象
                mRoster = conn.getRoster();

                //得到联系人
                Collection<RosterEntry> entries = mRoster.getEntries();

                //花名册  添加  动作监听
                mRosterListener = new MyRosterListener();
                mRoster.addRosterListener(mRosterListener);

                isCreate = false;
                //对比 本机联系人 ： openfire联系人,用于校验 离线后，别人删除我之后，我删除别人
                MatchRosterAndSqliteData(entries);
                for (RosterEntry entry : entries) {
                    insertOrUpdateEntry(entry);
                }

                /*========    从服务器同步花名册end     =====*/

                /*========    创建消息管理器     =====*/
                if (mChatManager == null) {
                    mChatManager = IMService.conn.getChatManager();
                }
                mChatManager.addChatListener(mMyChatManagerListener);
                /*========    创建消息管理器end     =====*/

                //接收好友状态监听器
                //conn.addPacketListener(mMypaMyPacketListener, new AndFilter(new PacketTypeFilter(Message.class), new FromContainsFilter(Const.APP_PACKAGENAME)));
                PacketFilter filter = new AndFilter(new PacketTypeFilter(Presence.class));
                conn.addPacketListener(mMyPacketListener, filter); //注册监听

                //广播消息
                PacketFilter messageFilter = new AndFilter(new PacketTypeFilter(Message.class));
                conn.addPacketListener(mMyPacketMessageListener, messageFilter);

                //手工查询离线的好友申请消息
                //***--离线用户登录后，查询username=自己，并且sub=2(openfire内置表的参数)， 的记录，展示在 sessionfragment列表中，存入数据库，
                name = SpUtils.get(Const.USERNAME, "") + "";
                HttpUtil.okhttpPost_queryOffPresences(name);
                if(mOffPresenceList!=null){
                    if(mOffPresenceList.size()>0){
                        for (Presence presence : mOffPresenceList) {
                            savePresenceSubscribe(presence);
                        }
                    }
                }
                /*====================  从服务器上同步群组信息  =====================*/


                //群聊邀请的监听
                MultiUserChat.addInvitationListener(conn, mXMPPReadWorker);
                InitMultiRoom();

                //手工获取（离线）的群邀请消息,获取后删除
                HttpUtil.okhttpPost_queryOffGroupInvite(mCurAccount);
                if(mOffGroupInviteList!=null){
                    if(mOffGroupInviteList.size()>0){
                        for (Message message : mOffGroupInviteList) {
                            String roomjid = message.getProperty(Const.GROUP_JID).toString();
                            saveGroupMessage(roomjid,message);
                        }
                    }
                }
                /*====================  从服务器上同步群组信息end  =====================*/
                isCreate = true;
            }
        });
    }



    private class ReaderThread implements InvitationListener {
        public void invitationReceived(Connection connection, String roomjid, String inviter, String reason, String password, Message message) {
            System.out.println("====================  接收到一条聊天室的邀请  ===================== ");
            String msg_to = message.getTo();
            if(!TextUtils.isEmpty(msg_to) && msg_to.equals(mCurAccount)){
                message.setFrom(inviter); //群聊发起人
                message.setBody(reason); //群聊消息内容
                message.setType(Message.Type.groupchat); //聊天类型
                message.setProperty(Const.MSGFLAG,Const.MSGFLAG_GROUP_INVITE);//    群聊消息的类型：群邀请的类型
                saveGroupMessage(AsmackUtils.filterGroupJid(roomjid),message);
            }
        }
    }


        /**
     * 初始化群组（查询，进入、监听，更新本地数据库）
     */
    public void InitMultiRoom() {
        //获取我的群
        String username = IMService.mCurAccount.substring(0,IMService.mCurAccount.lastIndexOf("@"));
        mMyGroupList = HttpUtil.okhttpPost_queryMyGroups(username);
        if(mMyGroupList==null){
            mMyGroupList = new ArrayList<>();
        }
        //对比 本机群组 与  服务器数据库中的最新群组
        MatchGroupAndSqliteData();

        if(mMyGroupList!=null && mMyGroupList.size()>0){
            for (GroupBean groupBean : mMyGroupList) {
                //加入每一个群
                MultiUserChat multiUserChat = AsmackUtils.joinMultiUserChat(mCurNickName, groupBean.getChild(), "123456");
                if(multiUserChat!=null){
                    if(!mMultiUserChatMap.containsKey(groupBean.getChildid())){

                        MUCParams mucParams = new MUCParams();
                        multiMsgListener mMultiMsgListener = new multiMsgListener();
                        multiParticipantStatus mMultiPartcipantStatus = new multiParticipantStatus();
                        //监听每一个群聊消息
                        multiUserChat.addMessageListener(mMultiMsgListener);
                        mucParams.setMessageListener(mMultiMsgListener);
                        //监听每一个群聊的状态事件
                        multiUserChat.addParticipantStatusListener(mMultiPartcipantStatus);
                        mucParams.setParticipantStatusListener(mMultiPartcipantStatus);
                        mucsList.put(multiUserChat, mucParams);
                        mucsJIDs.put(multiUserChat.getRoom(), multiUserChat);
                        System.out.println("====================  groupBean.getChildid()   ===================== " + groupBean.getChildid()+"  添加了监听");
                        System.out.println("====================  multiUserChat.getRoom()  ===================== " + multiUserChat.getRoom()+"  添加了监听");
                        mMultiUserChatMap.put(groupBean.getChildid(),multiUserChat);

                        //获取群成员
                        AsmackUtils.getGroupMember(AsmackUtils.filterGroupJid(groupBean.getChildid()));
                    }
                }
                insertOrUpdateGroup(groupBean);
            }
        }
    }


    /**
     * 初始化新加入的群组
     * @param mRoomJid  无@后缀
     */
    public void AddInitMultiRoom(String mRoomJid) {
        //获取我的群
        String username = IMService.mCurAccount.substring(0,IMService.mCurAccount.lastIndexOf("@"));
        mMyGroupList = HttpUtil.okhttpPost_queryMyGroups(username);
        if(mMyGroupList==null){
            mMyGroupList = new ArrayList<>();
        }
        //对比 本机群组 与  服务器数据库中的最新群组
        MatchGroupAndSqliteData();

        GroupBean _GroupBean = null;
        if(mMyGroupList!=null && mMyGroupList.size()>0){
            for (GroupBean groupBean : mMyGroupList) {
                if(groupBean!=null){
                    if(groupBean.getChildid().equals(mRoomJid+Const.ROOM_JID_SUFFIX)){
                        _GroupBean = groupBean;
                        break;
                    }
                }
            }

            if(_GroupBean!=null){
                //加入群
                MultiUserChat multiUserChat = AsmackUtils.joinMultiUserChat(mCurNickName, _GroupBean.getChild(), "123456");
                if(multiUserChat!=null){
                    if(!mMultiUserChatMap.containsKey(_GroupBean.getChildid())){

                        MUCParams mucParams = new MUCParams();
                        multiMsgListener mMultiMsgListener = new multiMsgListener();
                        multiParticipantStatus mMultiPartcipantStatus = new multiParticipantStatus();
                        //监听每一个群聊消息
                        multiUserChat.addMessageListener(mMultiMsgListener);
                        mucParams.setMessageListener(mMultiMsgListener);
                        //监听每一个群聊的状态事件
                        multiUserChat.addParticipantStatusListener(mMultiPartcipantStatus);
                        mucParams.setParticipantStatusListener(mMultiPartcipantStatus);
                        mucsList.put(multiUserChat, mucParams);
                        mucsJIDs.put(multiUserChat.getRoom(), multiUserChat);

                        mMultiUserChatMap.put(_GroupBean.getChildid(),multiUserChat);
                        //获取群成员
                        AsmackUtils.getGroupMember(AsmackUtils.filterGroupJid(_GroupBean.getChildid()));
                    }
                }
                insertOrUpdateGroup(_GroupBean);
            }
        }
    }

    /**
     * 移除群聊坚挺
     */
    public void removeMultUserChatListener(String groupjid) {
        if(mMultiUserChatMap.containsKey(groupjid)){

            for (MultiUserChat multiUserChat : listMUCs()) {
                System.out.println("-------------------"+multiUserChat.getRoom());
                if(multiUserChat.getRoom().equals(groupjid)){
                    MUCParams mucParams = getMUCParams(multiUserChat);
                    multiUserChat.removeMessageListener(mucParams.getMessageListener());
                    multiUserChat.removeParticipantStatusListener(mucParams.getParticipantStatusListener());
                    multiUserChat.leave();
                    mucsList.remove(multiUserChat);
                    mucsJIDs.remove(multiUserChat.getRoom());
                }
            }
            mMultiUserChatMap.remove(groupjid);
        }
    }


    /**
     * 对比 本机群组 与  服务器数据库中的最新群组
     */
    private void MatchGroupAndSqliteData(){
        Cursor cursor;
        String sqlWhere = " owner=? " ;
        String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
        String sqlOrder = GroupDbHelper.GroupTable.PINYIN+" asc ";
        cursor = getContentResolver().query(GroupProvider.URI_GROUP, null, sqlWhere, sqlWhereArgs, sqlOrder);
        if(cursor!=null){
            int count = cursor.getCount();
            int size = mMyGroupList.size();
            if(count >0 && size >=0){
                //本地群组的groupJid数组
                List<String> localList = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    localList.add(cursor.getString(cursor.getColumnIndex(GroupDbHelper.GroupTable.JID)));
                }
                //远程数据库中的groupJid数组
                List<String> xmppList = new ArrayList<>();
                for (GroupBean bean : mMyGroupList) {
                    xmppList.add(bean.getChildid());
                }
                List<MatherListUtil.ListOPBean> list = MatherListUtil.compare(localList,xmppList);
                for (MatherListUtil.ListOPBean listOPBean : list) {
                    //System.out.println("loaclList需要："+listOPBean.getOp()+"  "+listOPBean.getStr());
                    if(listOPBean.getOp().equals("-")){
                        //删除此条群组信息
                        getContentResolver().delete(
                                GroupProvider.URI_GROUP,
                                GroupDbHelper.GroupTable.JID + "=? and " + GroupDbHelper.GroupTable.OWNER + "=?",
                                new String[]{listOPBean.getStr(), IMService.mCurAccount});
                    }
                }
                cursor.close();
            }
        }
    }


    /**
     * 保存群聊消息
     * @param session_account -
     * @param msg -
     */
    public void saveGroupMessage(String session_account, Message msg) {
        String roomjid = session_account;
        String roomname = AsmackUtils.filterGroupName(roomjid);
        String fromAccount = msg.getFrom();
        fromAccount = AsmackUtils.filterAccount(fromAccount);
        String toAccount = msg.getTo();
        toAccount = AsmackUtils.filterAccount(toAccount);

        ContentValues values = new ContentValues();
        values.put(SmsDbHelper.SmsTable.FROM_ACCOUNT, fromAccount);  //群聊发言人
        values.put(SmsDbHelper.SmsTable.TO_ACCOUNT, toAccount); //群聊接收人（只有邀请类型时具备）
        values.put(SmsDbHelper.SmsTable.BODY, msg.getBody());//群聊消息内容
        values.put(SmsDbHelper.SmsTable.STATUS, "");
        values.put(SmsDbHelper.SmsTable.TYPE, msg.getType().name());//聊天类型：群聊
        values.put(SmsDbHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsDbHelper.SmsTable.SESSION_ACCOUNT, session_account);//聊天会话：群聊jid
        String isRead = (fromAccount.equals(IMService.mCurAccount)) ? SmsDbHelper.ISREAD : SmsDbHelper.UNREAD;
        if(IMService.chatObject.equals(session_account+Const.ROOM_JID_SUFFIX)){
            isRead = SmsDbHelper.ISREAD;
        }
        values.put(SmsDbHelper.SmsTable.TAG, isRead);
        values.put(SmsDbHelper.SmsTable.OWNER, IMService.mCurAccount);//所属者
        values.put(SmsDbHelper.SmsTable.ROOM_JID, roomjid);//群聊jid
        values.put(SmsDbHelper.SmsTable.ROOM_NAME, roomname);//群聊名称

        String msgflag;
        if (msg.getProperty(Const.MSGFLAG) != null) {
            msgflag = msg.getProperty(Const.MSGFLAG).toString(); //群聊的消息类型：文本、语音、视频、群邀请
        } else {
            msgflag = "";
        }
        values.put(SmsDbHelper.SmsTable.FLAG, msgflag);
        if (msgflag.equals(Const.MSGFLAG_RECORD)) {
            values.put(SmsDbHelper.SmsTable.RECORDLEN, msg.getProperty(Const.RECORDLEN) + "");
            values.put(SmsDbHelper.SmsTable.RECORDTIME, msg.getProperty(Const.RECORDTIME) + "");
            values.put(SmsDbHelper.SmsTable.RECORDURL, msg.getProperty(Const.RECORDURL) + "");
        }
        //图片
        if(msgflag.equals(Const.MSGFLAG_IMG)){
            values.put(SmsDbHelper.SmsTable.IMGURL, msg.getProperty(Const.REALIMAGEURL) + "");
        }
        getContentResolver().insert(SmsProvider.URI_GROUPSMS, values);
    }


    /***
     * 全部清除
     */
    public void clearxx() {
        //群聊每一个都移除
        for (MultiUserChat multiUserChat : listMUCs()) {
            MUCParams mucParams = getMUCParams(multiUserChat);
            multiUserChat.removeMessageListener(mucParams.getMessageListener());
            multiUserChat.removeParticipantStatusListener(mucParams.getParticipantStatusListener());
            multiUserChat.leave();
        }
        mucsList.clear();
        mucsJIDs.clear();

        mMyGroupList.clear();
        mMultiUserChatMap.clear();

        //花名册  移除  动作监听
        if (mRoster != null && mRosterListener != null) {
            mRoster.removeRosterListener(mRosterListener);
            mRoster = null;
            mRosterListener = null;
        }
        // 移除messageListener
        if (mCurChat != null && mMyMessageListener != null) {
            mCurChat.removeMessageListener(mMyMessageListener);
        }
        for (Map.Entry<String, Chat> entry : mChatMap.entrySet()) {
            //移除  消息管理器
            if (entry.getValue() != null && mMyMessageListener != null) {
                entry.getValue().removeMessageListener(mMyMessageListener);
            }
        }
        if (mMyPacketListener != null) {
            conn.removePacketListener(mMyPacketListener);
        }
        if (mMyPacketMessageListener != null) {
            conn.removePacketListener(mMyPacketMessageListener);
        }

        //通知下线
        Presence presence = new Presence(Presence.Type.unavailable);
        IMService.conn.sendPacket(presence);
    }


    /**
     * 花名册添加动作监听器
     */
    private class MyRosterListener implements RosterListener {
        @Override
        public void entriesAdded(Collection<String> addresses) {
            //修改数据库
            for (String address : addresses) {
                //从花名册中得到一个联系人
                RosterEntry entry = mRoster.getEntry(address);
                //更新或插入
                insertOrUpdateEntry(entry);
            }
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            //修改数据库
            for (String address : addresses) {
                //从花名册中得到一个联系人
                RosterEntry entry = mRoster.getEntry(address);
                //更新或插入
                insertOrUpdateEntry(entry);
            }
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            //修改数据库
            for (String account : addresses) {
                //删除
                getContentResolver().delete(
                        ContactsProvider.URI_CONTACT,
                        ContactsDbHelper.ContactTable.ACCOUNT + "=? and " + ContactsDbHelper.ContactTable.OWNER + "=?",
                        new String[]{account, IMService.mCurAccount});
            }
        }

        @Override
        public void presenceChanged(Presence presence) {
            String account = presence.getFrom();
            Presence bestPresence = mRoster.getPresence(account);
            //修改数据库
            ////System.out.println("====================  presenceChanged  =====================");
            updatePresence(account, bestPresence.getType().name());
        }
    }


    /**
     * 消息监听器实例
     */
    private MyMessageListener mMyMessageListener = new MyMessageListener();

    /**
     * 消息监听器
     */
    private class MyMessageListener implements MessageListener {
        @Override
        public void processMessage(Chat chat, Message message) {
            if (!message.getBody().equals("")) {
                //接收到消息，保存消息
                String session_account = chat.getParticipant();
                session_account = AsmackUtils.filterAccount(session_account);
                //System.out.println("####接收到的消息#######session_account:" + session_account + "   " + message.getFrom() + "      " + message.getTo());
                if (message.getProperty(Const.MSGFLAG).equals(Const.MSGFLAG_VIDEO)) {
                    enterVideoActivity(message.getFrom());
                }else if(message.getProperty(Const.MSGFLAG).equals(Const.MSGFLAG_IMG)){
                    saveMessage(session_account, message);
                } else {
                    saveMessage(session_account, message);
                }
            }
        }
    }

    /**
     * 进入视频申请页面
     *
     * @param form -
     */
    private void enterVideoActivity(String form) {
        //System.out.println("====================  IMService.isVideo  =====================" + IMService.isVideo);
        if (!IMService.isVideo) {
            Intent intent1 = new Intent();
            intent1.putExtra("sourceid", form);
            intent1.putExtra("sourcename", AsmackUtils.getFieldByAccountFromContactTable(form, ContactsDbHelper.ContactTable.NICKNAME));
            intent1.putExtra("sourceface", AsmackUtils.getFieldByAccountFromContactTable(form, ContactsDbHelper.ContactTable.AVATARURL));
            intent1.setAction("com.softfun_xmpp.activity.VideoChatScreen");
            intent1.addCategory("android.intent.category.DEFAULT");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        }
    }


    /**
     * 聊天管理监听器实例
     */
    MyChatManagerListener mMyChatManagerListener = new MyChatManagerListener();

    /**
     * 聊天管理监听器
     */
    private class MyChatManagerListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            //判断chat是否存在map里
            String participant = chat.getParticipant();//此次对话的参与者（对方）
            //处理jid后缀源
            participant = AsmackUtils.filterAccount(participant);
            if (!mChatMap.containsKey(participant)) {
                //保存chat
                mChatMap.put(participant, chat);
                chat.addMessageListener(mMyMessageListener);
            } else {
                mCurChat = mChatMap.get(participant);
            }
            if (createdLocally) {
                //System.out.println("====================  自己创建的会话  =====================");
            } else {
                //System.out.println("====================  别人创建的会话  =====================");
            }
        }
    }



    /**
     * 更新或插入群组基本信息
     * @param groupBean -
     */
    private void insertOrUpdateGroup(GroupBean groupBean) {
        ContentValues values = new ContentValues();
        String groupjid = groupBean.getChildid();
        String groupname = groupjid.substring(0,groupjid.lastIndexOf("#"));
        values.put(GroupDbHelper.GroupTable.JID,groupjid);
        values.put(GroupDbHelper.GroupTable.FACE, GlobalContext.getInstance().getResources().getString(R.string.app_server) + groupBean.getGroupface());
        values.put(GroupDbHelper.GroupTable.LVL, groupBean.getLvl());
        values.put(GroupDbHelper.GroupTable.ROOMNUM, groupBean.getGroupnum());
        values.put(GroupDbHelper.GroupTable.TYPE, groupBean.getGrouptype());
        values.put(GroupDbHelper.GroupTable.OWNER, mCurAccount);
        values.put(GroupDbHelper.GroupTable.PINYIN, PinYinUtils.getShortPinYin(groupname));
        //先更新  再插入
        int updateCount = getContentResolver().update(GroupProvider.URI_GROUP, values, GroupDbHelper.GroupTable.JID + "=?", new String[]{groupBean.getChildid()});
        //没有更新到记录
        if (updateCount <= 0) {
            //插入记录
            getContentResolver().insert(GroupProvider.URI_GROUP, values);
        }
    }


    /**
     * 将openfire的联系人信息，更新或插入到基本联系人表
     *
     * @param entry -
     */
    private void insertOrUpdateEntry(RosterEntry entry) {
        ContentValues values = new ContentValues();
        //账户
        String account = entry.getUser();
        //处理账户信息
        account = AsmackUtils.filterAccount(account);
        //昵称
        String nickname ;//entry.getName();
        nickname = AsmackUtils.getVcardInfo(IMService.conn, account, Const.NICKNAME);
        //处理昵称
        if (nickname == null || "".equals(nickname)) {
            nickname = account.substring(0, account.lastIndexOf("@"));//kaiery@softfun.com  -->kaiery
        }
        //状态
        String status = null;
        Iterator<Presence> presences = mRoster.getPresences(entry.getUser());
        while (presences.hasNext()) {
            Presence presence = presences.next();
            Presence.Type type = presence.getType();
            //System.out.println("====================  在线状态  ====================="+nickname+"   "+ type);
            if (type.equals(Presence.Type.available)) {
                status = presence.getType().name();
            } else {
                status = "离线";
            }
        }
        //头像
        String avatarurl;
        String user = AsmackUtils.filterAccount(entry.getUser());
        avatarurl = GlobalContext.getInstance().getResources().getString(R.string.app_server) + AsmackUtils.getVcardInfo(IMService.conn, user, Const.AVATARURL);

        String vip = AsmackUtils.getVcardInfo(IMService.conn, user, Const.VIP);
        String background = GlobalContext.getInstance().getResources().getString(R.string.app_server) + AsmackUtils.getVcardInfo(IMService.conn, user, Const.BACKGROUND);
        String score = AsmackUtils.getVcardInfo(IMService.conn, user, Const.SCORE);
        String roletype = AsmackUtils.getVcardInfo(IMService.conn, user, Const.ROLETYPE);

        values.put(ContactsDbHelper.ContactTable.OWNER, IMService.mCurAccount);
        values.put(ContactsDbHelper.ContactTable.ACCOUNT, account);
        values.put(ContactsDbHelper.ContactTable.NICKNAME, nickname);
        values.put(ContactsDbHelper.ContactTable.AVATARURL, avatarurl);
        values.put(ContactsDbHelper.ContactTable.PINYIN, PinYinUtils.getPinYin(nickname));
        values.put(ContactsDbHelper.ContactTable.STATUS, status);
        values.put(ContactsDbHelper.ContactTable.VIP, vip);
        values.put(ContactsDbHelper.ContactTable.BACKGROUND, background);
        values.put(ContactsDbHelper.ContactTable.SCORE, score);
        values.put(ContactsDbHelper.ContactTable.ROLETYPE, roletype);

        //先更新  再插入
        int updateCount = getContentResolver().update(ContactsProvider.URI_CONTACT, values, ContactsDbHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
        //没有更新到记录
        if (updateCount <= 0) {
            //插入记录
            getContentResolver().insert(ContactsProvider.URI_CONTACT, values);
        }
    }

    /**
     * 更新用户状态
     */
    private void updatePresence(String account, String status) {
        account = AsmackUtils.filterAccount(account);
        if (status == null) {
            status = "离线";
        }
        ContentValues values = new ContentValues();
        values.put(ContactsDbHelper.ContactTable.ACCOUNT, account);
        values.put(ContactsDbHelper.ContactTable.STATUS, status);
        getContentResolver().update(ContactsProvider.URI_CONTACT, values, ContactsDbHelper.ContactTable.ACCOUNT + "=?", new String[]{account});
    }


    /**
     * 通过绑定服务，提供外部调用，发送消息
     *
     * @param msg -
     */
    public void sendMessage(final Message msg) {
        try {
            //2、创建聊天对象 (目标对象jid，消息监听者)
            String toAccount = msg.getTo();
            if (mChatMap.containsKey(toAccount)) {
                mCurChat = mChatMap.get(toAccount);
            } else {
                mCurChat = mChatManager.createChat(toAccount, mMyMessageListener);
                mChatMap.put(toAccount, mCurChat);
            }
            //发送消息
            mCurChat.sendMessage(msg);
            if (msg.getProperty(Const.MSGFLAG).equals(Const.MSGFLAG_VIDEO)) {

            } else {
                //保存消息到数据库
                saveMessage(toAccount, msg);
            }

        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }



//    public void sendGroupMessage(MultiUserChat multiUserChat,String msg){
//        //测试发送一个群聊消息
//        AsmackUtils.sendMultiChatMessage(multiUserChat,msg);
////        if(msg.getProperty(Const.MSGFLAG).equals(Const.MSGFLAG_VIDEO)){
////
////        } else{
////            //保存消息到数据库
////            saveGroupMessage(msg.getProperty(Const.GROUP_JID)+"",msg);
////        }
//    }

    /**
     * 保存消息到数据库：---> contentResolver-->contentProvider -->sqlite
     *
     * @param msg -
     */
    public void saveMessage(String session_account, Message msg) {
        String fromAccount = msg.getFrom();
        fromAccount = AsmackUtils.filterAccount(fromAccount);
        String toAccount = msg.getTo();
        toAccount = AsmackUtils.filterAccount(toAccount);

        ContentValues values = new ContentValues();
        values.put(SmsDbHelper.SmsTable.FROM_ACCOUNT, fromAccount);
        values.put(SmsDbHelper.SmsTable.TO_ACCOUNT, toAccount);
        values.put(SmsDbHelper.SmsTable.BODY, msg.getBody());
        values.put(SmsDbHelper.SmsTable.STATUS, "");
        values.put(SmsDbHelper.SmsTable.TYPE, msg.getType().name());
        values.put(SmsDbHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsDbHelper.SmsTable.SESSION_ACCOUNT, session_account);
        values.put(SmsDbHelper.SmsTable.TAG, getMsgTag(session_account, fromAccount));
        values.put(SmsDbHelper.SmsTable.OWNER, IMService.mCurAccount);

        String msgflag;
        if (msg.getProperty(Const.MSGFLAG) != null) {
            msgflag = msg.getProperty(Const.MSGFLAG).toString();
        } else {
            msgflag = "";
        }
        values.put(SmsDbHelper.SmsTable.FLAG, msgflag);
        //语音
        if (msgflag.equals(Const.MSGFLAG_RECORD)) {
            values.put(SmsDbHelper.SmsTable.RECORDLEN, msg.getProperty(Const.RECORDLEN) + "");
            values.put(SmsDbHelper.SmsTable.RECORDTIME, msg.getProperty(Const.RECORDTIME) + "");
            values.put(SmsDbHelper.SmsTable.RECORDURL, msg.getProperty(Const.RECORDURL) + "");
        }
        //图片
        if(msgflag.equals(Const.MSGFLAG_IMG)){
            values.put(SmsDbHelper.SmsTable.IMGURL, msg.getProperty(Const.REALIMAGEURL) + "");
        }

        getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    /**
     * 根据是否已经在对应人员聊天界面，判断此条消息是否为已读/未读
     *
     * @param session_account 参与者
     * @param from_account    消息来自
     * @return -
     */
    private String getMsgTag(String session_account, String from_account) {
        if (session_account != null && from_account != null) {
            if (session_account.equals(from_account)) {
                if (chatObject.equals(session_account)) {
                    return SmsDbHelper.ISREAD;
                } else {
                    return SmsDbHelper.UNREAD;
                }
            } else {
                return SmsDbHelper.ISREAD;
            }
        }
        return null;
    }


    /**
     * 进入聊天界面，将我与此人私聊的聊天的消息全部设置为已读状态 1
     * @param session_acctount 参与者JID
     */
    public void updateChatMessageToIsRead(String session_acctount) {
        //如果是私聊,置为已读
        ContentValues values = new ContentValues();
        values.put(SmsDbHelper.SmsTable.TAG, SmsDbHelper.ISREAD);
        String where = SmsDbHelper.SmsTable.SESSION_ACCOUNT + " = ? and " + SmsDbHelper.SmsTable.TO_ACCOUNT + " = ? ";
        String[] args = new String[]{session_acctount, IMService.mCurAccount};
        getContentResolver().update(SmsProvider.URI_SMS, values, where, args);
    }




    /** 进入聊天界面，将我与此群的聊天的消息全部设置为已读状态 1
     *
     * @param roomjid -群jid
     */
    public void updateMultiChatMessageToIsRead(String roomjid) {
        ContentValues values = new ContentValues();
        values.put(SmsDbHelper.SmsTable.TAG, SmsDbHelper.ISREAD);
        String where = SmsDbHelper.SmsTable.OWNER + " = ? and " + SmsDbHelper.SmsTable.ROOM_JID + " = ? ";
        String[] args = new String[]{IMService.mCurAccount,roomjid};
        getContentResolver().update(SmsProvider.URI_SMS, values, where, args);
    }


    /**
     * 接收好友状态监听器实例
     */
    private MyPacketListener mMyPacketListener = new MyPacketListener();

    /**
     * 接收好友状态监听器类
     */
    private class MyPacketListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
            //Log.i("===packet===", packet.toXML());
            if (packet instanceof Presence) {
                //Log.i("Presence", packet.toXML());
                Presence presence = (Presence) packet;
                String from = presence.getFrom();//发送方
                String to = presence.getTo();//接收方
                ////System.out.println("from:" + from + "   to:" + to);
                //Presence.Type有7中状态
                if (presence.getType().equals(Presence.Type.subscribe)) {//好友申请
                    ////System.out.println("====================  subscribe  =====================" + from + "   发出好友申请");
                    savePresenceSubscribe(presence);

                } else if (presence.getType().equals(Presence.Type.subscribed)) {//同意添加好友
                    ////System.out.println("====================  subscribed  =====================" + from + "   同意添加好友");

                } else if (presence.getType().equals(Presence.Type.unsubscribe)) {
                    ////System.out.println("====================  unsubscribe  =====================" + from + "   拒绝了好友关系");

                } else if (presence.getType().equals(Presence.Type.unsubscribed)) {
                    ////System.out.println("====================  unsubscribed  =====================" + from + "   删除了您");
                    savePresenceSubscribe(presence);

                } else if (presence.getType().equals(Presence.Type.unavailable)) { //好友下线   要更新好友列表，可以在这收到包后，发广播到指定页面   更新列表
                    ////System.out.println("====================  unavailable  =====================" + from + "   下线");
                } else if (presence.getType().equals(Presence.Type.available)) {//好友上线
                    ////System.out.println("====================  available  =====================");

                } else {
                    //error
                    ////System.out.println("====================  error  =====================");
                }
            }
        }
    }


    /**
     * 接收广播消息监听器
     */
    private MyPacketMessageListener mMyPacketMessageListener = new MyPacketMessageListener();

    private class MyPacketMessageListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
            ////System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            if (packet instanceof Message) {
                Message message = (Message) packet;
                String body = message.getBody();
                if (message.getType().equals(Message.Type.normal)) {
                    if(!TextUtils.isEmpty(body)){
                        ToastUtils.showToastSafe_Long(body);
                    }
                }
            }
        }
    }


    /**
     * 接收到好友申请，保存好友申请
     * <p/>
     * 接收到好友删除，被动删除对方
     *
     * @param presence -
     */
    public void savePresenceSubscribe(Presence presence) {
        String fromAccount = presence.getFrom();
        fromAccount = AsmackUtils.filterAccount(fromAccount);
        String toAccount = presence.getTo();
        toAccount = AsmackUtils.filterAccount(toAccount);

        //申请好友消息
        if (presence.getType().equals(Presence.Type.subscribe)) {
            if (!isMyFriends(fromAccount)) {
                ContentValues values = new ContentValues();
                 /*====================  好友申请消息插入数据库  =====================*/
                values.put(SmsDbHelper.SmsTable.FROM_ACCOUNT, fromAccount);
                values.put(SmsDbHelper.SmsTable.TO_ACCOUNT, toAccount);
                values.put(SmsDbHelper.SmsTable.BODY, "好友申请");
                values.put(SmsDbHelper.SmsTable.STATUS, "");
                values.put(SmsDbHelper.SmsTable.TYPE, presence.getType().name());
                values.put(SmsDbHelper.SmsTable.TIME, System.currentTimeMillis());
                values.put(SmsDbHelper.SmsTable.SESSION_ACCOUNT, fromAccount);
                values.put(SmsDbHelper.SmsTable.TAG, getMsgTag(fromAccount, fromAccount));
                values.put(SmsDbHelper.SmsTable.OWNER, IMService.mCurAccount);
                getContentResolver().insert(SmsProvider.URI_SMS, values);
                /*====================  好友申请消息插入数据库END  =====================*/
            }
        }

        //被对方删除,我现在就要被动删除他
        if (presence.getType().equals(Presence.Type.unsubscribed)) {
            RosterEntry entry = mRoster.getEntry(fromAccount);
            if (entry != null) {
                try {
                    mRoster.removeEntry(entry); //删除某个好友
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
            //删除联系人表此人，如果有的话
            getContentResolver().delete(ContactsProvider.URI_CONTACT, ContactsDbHelper.ContactTable.ACCOUNT + "=?", new String[]{fromAccount});
            //删除消息表此人，如果有的话
            getContentResolver().delete(SmsProvider.URI_SMS, SmsDbHelper.SmsTable.SESSION_ACCOUNT + "=?", new String[]{fromAccount});
        }
    }


    /**
     * 发送好友订阅（同意添加好友、拒绝添加好友）
     */
    public void sendPresence(Presence.Type type, String to) {
        //同意添加他为好友
        Presence presence = new Presence(type);
        presence.setTo(to);
        conn.sendPacket(presence);

        //如果我拒绝添加他为好友
        if (presence.getType().equals(Presence.Type.unsubscribe)) {
            RosterEntry entry = mRoster.getEntry(to);
            if (entry != null) {
                try {
                    mRoster.removeEntry(entry); //删除某个好友
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
            //删除联系人表此人，如果有的话
            getContentResolver().delete(ContactsProvider.URI_CONTACT, ContactsDbHelper.ContactTable.ACCOUNT + "=?", new String[]{to});
            //删除消息表此人，如果有的话
            getContentResolver().delete(SmsProvider.URI_SMS, SmsDbHelper.SmsTable.SESSION_ACCOUNT + "=?", new String[]{to});
        }
    }


    /**
     * 对方是否是我的好友，我们是否已经是好友？
     */
    public boolean isMyFriends(String account) {
        RosterEntry entry;
        try {
            entry = mRoster.getEntry(account);
            if (entry != null) {
                if (!entry.getType().equals(RosterPacket.ItemType.both)) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    /**
     * 发送群消息
     * @param multiUserChat
     * @param message
     */
    public void sendGroupMessage(MultiUserChat multiUserChat,Message message){
        AsmackUtils.sendMultiChatMessage(multiUserChat,message);
    }





    /**
     * 每一个聊天室 的消息监听
     */
    private class multiMsgListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
            Message message = (Message) packet;
            ////System.out.println(IMService.mCurAccount+"==================== 接收到聊天室的聊天消息   ===================== "+message.getBody());
            if(message.getType().equals(org.jivesoftware.smack.packet.Message.Type.groupchat)){
                if(message.getBody()!=null){
                    DelayInformation inf = (DelayInformation) message.getExtension("x", "jabber:x:delay");
                    Date sentDate;
                    if (inf != null) {
                        sentDate = inf.getStamp();
                    } else {
                        sentDate = new Date();
                    }
                    // 接收来自聊天室的聊天信息
                    String msgflag;
                    if(message.getProperty(Const.MSGFLAG)!=null){
                        msgflag = message.getProperty(Const.MSGFLAG)+"";
                    }else{
                        msgflag = "";
                    }
                    //消息类型是  有新成员加入本群
                    if(msgflag.equals(Const.MSGFLAG_GROUP_NEW_MEMBER)){
                        //System.out.println("====================  有新成员加入本群 消息  =====================");
                        //有@conference.softfun的群JID
                        String groupjid = message.getProperty(Const.GROUP_JID)+"";
                        String new_account = message.getProperty(Const.ACCOUNT)+"";
                        GroupMemberBean groupMemberBean =   HttpUtil.okhttpPost_queryNewGroupMember(groupjid,AsmackUtils.filterAccountToUserName(new_account));
                        AsmackUtils.updateGroupMemberMap(AsmackUtils.filterGroupJid(groupjid),groupMemberBean);
                    }else
                    //消息类型是  群更改管理员==============================================================
                    if(msgflag.equals(Const.MSGFLAG_GROUP_CHANGEMASTER)){
                        ////System.out.println("====================  群更改管理员 消息  =====================");
                        String groupname = message.getProperty(Const.GROUP_JID)+"";
                        String selectAccount = message.getProperty(Const.MSGFLAG_GROUP_NEW_MASTER)+"";
                        String oldMaster = message.getProperty(Const.ACCOUNT)+"";
                        //更新master
                        if(mGroupMemberMap.containsKey(groupname)){
                            List<GroupMemberBean> list = mGroupMemberMap.get(groupname);
                            for (int i = 0; i < list.size(); i++) {
                                if(list.get(i).getAccount().equals(selectAccount)){
                                    GroupMemberBean bean = list.get(i);
                                    bean.setMaster("1");
                                    list.set(i,bean);
                                }
                                if(list.get(i).getAccount().equals(oldMaster)){
                                    GroupMemberBean bean = list.get(i);
                                    bean.setMaster("0");
                                    list.set(i,bean);
                                }
                            }
                        }
                    }else
                    //消息类型是 群解散==============================================================
                    if(msgflag.equals(Const.MSGFLAG_GROUP_DISMISS)){
                        String groupname = message.getProperty(Const.GROUP_JID)+"";
                        ////System.out.println("====================  接收到群解散消息  =====================");
                        //移除监听
                        if(mMultiUserChatMap.containsKey(groupname+Const.ROOM_JID_SUFFIX)){
                            for (MultiUserChat multiUserChat : listMUCs()) {
                                System.out.println("-------------------"+multiUserChat.getRoom());
                                if(multiUserChat.getRoom().equals(groupname+Const.ROOM_JID_SUFFIX)){
                                    MUCParams mucParams = getMUCParams(multiUserChat);
                                    multiUserChat.removeMessageListener(mucParams.getMessageListener());
                                    multiUserChat.removeParticipantStatusListener(mucParams.getParticipantStatusListener());
                                    mucsList.remove(multiUserChat);
                                    mucsJIDs.remove(multiUserChat.getRoom());
                                }
                            }
                            mMultiUserChatMap.remove(groupname+Const.ROOM_JID_SUFFIX);
                        }


                        if(mGroupMemberMap.containsKey(groupname)){
                            mGroupMemberMap.remove(groupname);
                        }
                        if(mMultiUserChatMap.containsKey(groupname+Const.ROOM_JID_SUFFIX)){
                            mMultiUserChatMap.remove(groupname+Const.ROOM_JID_SUFFIX);
                        }
                        //删除本地数据库的群组表，我不再参与此群
                        getContentResolver().delete(
                                GroupProvider.URI_GROUP,
                                GroupDbHelper.GroupTable.JID + "=? and " + GroupDbHelper.GroupTable.OWNER + "=?",
                                new String[]{groupname+Const.ROOM_JID_SUFFIX, IMService.mCurAccount}
                        );
                        //删除本地群消息
                        getContentResolver().delete(
                                SmsProvider.URI_GROUPSMS,
                                SmsDbHelper.SmsTable.TYPE +"=?  and  "+SmsDbHelper.SmsTable.ROOM_JID+" =?  and "+SmsDbHelper.SmsTable.OWNER+"  =? ",
                                new String[]{ Message.Type.groupchat.name(), groupname ,IMService.mCurAccount}
                        );
                    }else
                    //消息类型是 离开、脱离群==============================================================
                    if(msgflag.equals(Const.MSGFLAG_GROUP_LEAVE)){
                        String leave_account = message.getProperty(Const.ACCOUNT)+"";
                        String leave_username = AsmackUtils.filterAccountToUserName(leave_account);
                        String groupname = message.getProperty(Const.GROUP_JID)+"";
                        //System.out.println("====================  接收到 脱离群消息  =====================");
                        //如果离开群的人是我自己
                        if(leave_account.equals(IMService.mCurAccount)){
                            //回调不做处理，已经在按钮端调用服务方法处理完毕
                        }else if(!leave_account.equals(IMService.mCurAccount)){
                            //别人要离开群
                            if(mGroupMemberMap.containsKey(groupname)){
                                boolean b = false;
                                List<GroupMemberBean> list = mGroupMemberMap.get(groupname);
                                for (int i = 0; i < list.size(); i++) {
                                    if(list.get(i).getAccount().equals(leave_username)){
                                        list.remove(i);
                                        b = true;
                                        break;
                                    }
                                }
                                if(b){
                                    mGroupMemberMap.remove(groupname);
                                    mGroupMemberMap.put(groupname,list);
                                }
                            }
                        }
                    }else
                    //消息类型是 群踢人==============================================================
                    if(msgflag.equals(Const.MSGFLAG_GROUP_KICK)){
                        //踢人操作
                        String kick_account = message.getProperty(Const.ACCOUNT)+"";
                        String kicked_username = message.getProperty(Const.MSGFLAG_GROUP_KICKED_USERNAME)+"";
                        String groupname = message.getProperty(Const.GROUP_JID)+"";
                        ////System.out.println(kick_account);
                        ////System.out.println(kicked_username);
                        ////System.out.println(groupname);
                        ////System.out.println("====================  接收到群踢消息  =====================");
                        if(mGroupMemberMap.containsKey(groupname)){
                            boolean b = false;
                            List<GroupMemberBean> list = mGroupMemberMap.get(groupname);
                            for (int i = 0; i < list.size(); i++) {
                                if(list.get(i).getAccount().equals(kicked_username)){
                                    list.remove(i);
                                    //删除远程表
                                    //如果是我要删除TA ，那么我自己去删除数据库的数据
                                    if(kick_account.equals(IMService.mCurAccount)){
                                        ////System.out.println("====================  我要删除TA  =====================");
                                        HttpUtil.okhttpPost_deleteGroupMember(groupname, kicked_username);
                                    }
                                    //删除本地表
                                    //如果我是被删除的人
                                    if((kicked_username+"@"+Const.APP_PACKAGENAME).equals(IMService.mCurAccount)){
                                        ////System.out.println("====================  我是被删除的人  =====================");
                                        //移除监听
                                        if(mMultiUserChatMap.containsKey(groupname+Const.ROOM_JID_SUFFIX)){
                                            for (MultiUserChat multiUserChat : listMUCs()) {
                                                System.out.println("-------------------"+multiUserChat.getRoom());
                                                if(multiUserChat.getRoom().equals(groupname+Const.ROOM_JID_SUFFIX)){
                                                    MUCParams mucParams = getMUCParams(multiUserChat);
                                                    multiUserChat.removeMessageListener(mucParams.getMessageListener());
                                                    multiUserChat.removeParticipantStatusListener(mucParams.getParticipantStatusListener());
                                                    mucsList.remove(multiUserChat);
                                                    mucsJIDs.remove(multiUserChat.getRoom());
                                                }
                                            }
                                            mMultiUserChatMap.remove(groupname+Const.ROOM_JID_SUFFIX);
                                        }


                                        if(mGroupMemberMap.containsKey(groupname)){
                                            mGroupMemberMap.remove(groupname);
                                        }
                                        if(mMultiUserChatMap.containsKey(groupname+Const.ROOM_JID_SUFFIX)){
                                            mMultiUserChatMap.remove(groupname+Const.ROOM_JID_SUFFIX);
                                        }
                                        //删除本地数据库的群组表，我不再参与此群
                                        getContentResolver().delete(
                                                GroupProvider.URI_GROUP,
                                                GroupDbHelper.GroupTable.JID + "=? and " + GroupDbHelper.GroupTable.OWNER + "=?",
                                                new String[]{groupname+Const.ROOM_JID_SUFFIX, IMService.mCurAccount}
                                        );
                                        //删除本地群消息
                                        getContentResolver().delete(
                                                SmsProvider.URI_GROUPSMS,
                                                SmsDbHelper.SmsTable.TYPE +"=?  and  "+SmsDbHelper.SmsTable.ROOM_JID+" =?  and "+SmsDbHelper.SmsTable.OWNER+"  =? ",
                                                new String[]{ Message.Type.groupchat.name(), groupname ,IMService.mCurAccount}
                                        );
                                    }
                                    b = true;
                                    break;
                                }
                            }
                            if(b){
                                mGroupMemberMap.remove(groupname);
                                mGroupMemberMap.put(groupname,list);
                            }
                        }
                    }else{
                        //消息类型是  聊天==============================================================
                        //插入本地数据库
                        Message groupMessage = new Message();
                        groupMessage.setFrom(message.getProperty(Const.ACCOUNT)+"");
                        groupMessage.setTo("");
                        groupMessage.setBody(message.getBody());
                        groupMessage.setType(org.jivesoftware.smack.packet.Message.Type.groupchat);
                        groupMessage.setProperty(Const.MSGFLAG,msgflag);
                        if(msgflag.equals(Const.MSGFLAG_RECORD)){
                            groupMessage.setProperty(Const.RECORDLEN,message.getProperty(Const.RECORDLEN));
                            groupMessage.setProperty(Const.RECORDTIME,message.getProperty(Const.RECORDTIME));
                            groupMessage.setProperty(Const.RECORDURL,message.getProperty(Const.RECORDURL));
                        }
                        if(msgflag.equals(Const.MSGFLAG_IMG)){
                            groupMessage.setProperty(Const.REALIMAGEURL,message.getProperty(Const.REALIMAGEURL));
                        }
                        groupMessage.setProperty(Const.GROUP_JID,message.getProperty(Const.GROUP_JID));
                        saveGroupMessage(groupMessage.getProperty(Const.GROUP_JID)+"",groupMessage);
                    }
                }
            }
        }
    }

    /**
     * 每一个聊天室 的监听会议室状态（成员的进入、离开等）
     */
    private class multiParticipantStatus implements ParticipantStatusListener {
        @Override
        public void adminGranted(String arg0) {
            //System.out.println("adminGranted:"+arg0);
        }

        @Override
        public void adminRevoked(String arg0) {
            //System.out.println("adminRevoked:"+arg0);
        }

        @Override
        public void banned(String arg0, String arg1, String arg2) {
            //System.out.println("banned:"+arg0+" "+arg1+"  "+arg2);
        }

        @Override
        public void joined(String participant) {
            //System.out.println(StringUtils.parseResource(participant)+ " has joined the room.");
        }

        @Override
        public void kicked(String arg0, String arg1, String arg2) {
            //System.out.println("kicked:"+arg0+" "+arg1+" "+arg2);
        }

        @Override
        public void left(String participant) {
            //System.out.println(StringUtils.parseResource(participant)+ " has left the room.");

        }

        @Override
        public void membershipGranted(String arg0) {
            //System.out.println("membershipGranted:"+arg0);
        }

        @Override
        public void membershipRevoked(String arg0) {
            //System.out.println("membershipRevoked:"+arg0);
        }

        @Override
        public void moderatorGranted(String arg0) {
            //System.out.println("moderatorGranted:"+arg0);
        }

        @Override
        public void moderatorRevoked(String arg0) {
            //System.out.println("moderatorRevoked:"+arg0);
        }

        @Override
        public void nicknameChanged(String participant, String newNickname) {
            //System.out.println(StringUtils.parseResource(participant)+ " 现在被称为： " + newNickname + ".");
        }

        @Override
        public void ownershipGranted(String arg0) {
            //System.out.println("ownershipGranted:"+arg0);
        }

        @Override
        public void ownershipRevoked(String arg0) {
            //System.out.println("ownershipRevoked:"+arg0);
        }

        @Override
        public void voiceGranted(String arg0) {
            //System.out.println("voiceGranted:"+arg0);
        }

        @Override
        public void voiceRevoked(String arg0) {
            //System.out.println("voiceRevoked:"+arg0);
        }
    }


    /**
     * 对比 本机联系人 ： openfire联系人   ，用于校验 离线后，别人删除我之后，我删除别人
     * @param entries - openfire联系人
     */
    private void MatchRosterAndSqliteData(Collection<RosterEntry> entries) {
        //本机联系人
        String sqlWhere = " owner=? ";
        String[] sqlWhereArgs = new String[]{IMService.mCurAccount};
        String sqlOrder = ContactsDbHelper.ContactTable.PINYIN+" asc";
        Cursor cursor = getContentResolver().query(ContactsProvider.URI_CONTACT, null, sqlWhere, sqlWhereArgs, sqlOrder);
        if(cursor!=null){
            if(cursor.getCount()>0 && entries.size()>0){
                //本地联系人的account数组
                List<String> loaclList = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    loaclList.add(cursor.getString(cursor.getColumnIndex(ContactsDbHelper.ContactTable.ACCOUNT)));
                }
                //openfire联系人的account数组
                List<String> xmppList = new ArrayList<>();
                for (RosterEntry entry : entries) {
                    xmppList.add(entry.getUser());
                }
                List<MatherListUtil.ListOPBean> list = MatherListUtil.compare(loaclList,xmppList);
                for (MatherListUtil.ListOPBean listOPBean : list) {
                    ////System.out.println("loaclList需要："+listOPBean.getOp()+"  "+listOPBean.getStr());
                    if(listOPBean.getOp().equals("-")){
                        //删除此条联系人
                        getContentResolver().delete(
                                ContactsProvider.URI_CONTACT,
                                ContactsDbHelper.ContactTable.ACCOUNT + "=? and " + ContactsDbHelper.ContactTable.OWNER + "=?",
                                new String[]{listOPBean.getStr(), IMService.mCurAccount});
                    }
                }
            }
            cursor.close();
        }
    }


    /**
     * 获取某一个好友的在线状态
     * @param account
     * @return
     */
    public String getRosterStatus(String account){
        String status = null;
        Iterator<Presence> presences = mRoster.getPresences(account);
        while (presences.hasNext()) {
            Presence presence = presences.next();
            Presence.Type type = presence.getType();
            if (type.equals(Presence.Type.available)) {
                status = presence.getType().name();
            } else {
                status = getResources().getString(R.string.offline);//"离线";
            }
        }
        return status;
    }




}
