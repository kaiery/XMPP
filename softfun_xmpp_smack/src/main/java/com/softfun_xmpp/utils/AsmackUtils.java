package com.softfun_xmpp.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.bean.GroupMemberBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.dbhelper.ContactsDbHelper;
import com.softfun_xmpp.dbhelper.GroupDbHelper;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.provider.ContactsProvider;
import com.softfun_xmpp.provider.GroupProvider;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AsmackUtils {


    private static Context context;

    static {
        context = GlobalContext.getInstance();
    }

    /**
     * 过滤账户信息后缀,添加@XXX
     *
     * @param account
     * @return
     */
    @NonNull
    public static String filterAccount(String account) {
        if(account.lastIndexOf("@")>0){
            return account.substring(0, account.lastIndexOf("@")) + "@" + Const.APP_PACKAGENAME;
        }else{
            return account+ "@" + Const.APP_PACKAGENAME;
        }
    }

    /**
     * 将用户的jid 转为 username
     * @param account
     * @return
     */
    public static String filterAccountToUserName(String account){
        return account.substring(0,account.lastIndexOf("@"));
    }

    /**
     * 过滤群Jid后缀,去掉#XXX
     * @param jid
     * @return
     */
    public static String filterGroupName(String jid) {
        if(!TextUtils.isEmpty(jid)){
            return jid.substring(0,jid.lastIndexOf("#"));
        }
        return "";
    }


    /**
     * 过滤群Jid后缀,去掉@XXX
     * @param jid
     * @return
     */
    public static String filterGroupJid(String jid) {
        if(!TextUtils.isEmpty(jid)){
            if(jid.lastIndexOf("@")>0){
                return jid.substring(0,jid.lastIndexOf("@"));
            }else{
                return jid;
            }
        }
        return "";
    }



    /**
     * 通过 account 获取 联系人表中的字段值
     *
     * @param account
     * @param field
     * @return
     */
    public static String getFieldByAccountFromContactTable(String account, String field) {

        if(account.lastIndexOf("/")>0){
            //肖群浩@softfun/iphone
            account = account.substring(0,account.lastIndexOf("/"));
        }

        String fieldValue = null;
        String[] columns = new String[]{field};
        String where = ContactsDbHelper.ContactTable.ACCOUNT + "=?";
        String[] args = new String[]{account};
        Cursor cursor = context.getContentResolver().query(ContactsProvider.URI_CONTACT, columns, where, args, null);
        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            fieldValue = cursor.getString(cursor.getColumnIndex(field));
            cursor.close();
        }
        return fieldValue;
    }

    /**
     * 通过 groupjid 获取 群组表中的字段值
     * @param groupjid
     * @param field
     * @return
     */
    public static String getFieldByGroupJidFromGroupTable(String groupjid,String field){
        String fieldValue = null;
        String[] columns = new String[]{field};
        String where = GroupDbHelper.GroupTable.JID + "=?";
        String[] args = new String[]{groupjid};
        Cursor cursor = context.getContentResolver().query(GroupProvider.URI_GROUP, columns, where, args, null);
        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            fieldValue = cursor.getString(cursor.getColumnIndex(field));
            cursor.close();
        }
        return fieldValue;
    }



    /**
     * 获取用户Vcard信息
     *
     * @param conn
     * @param user
     * @return
     */
    public static VCard getUserVcard(AbstractXMPPConnection conn, String user) {
        VCardManager vCardManager = VCardManager.getInstanceFor(conn);
        VCard vcard = null;
        try {
            vcard = new VCard();
            if (!conn.isConnected()) {
                conn.connect();
            }
            vcard = vCardManager.loadVCard(user);
            //vcard.load(conn, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcard;
    }


// TODO: 2016-05-10 传递map统一更新自己的vcard ，修改个人信息后，数据库就不用再调用存储过程进行更新了

    /**
     * 设置自己的vcard信息
     */
    public static void setVcardInfo() {
        try {
            AbstractXMPPConnection conn = IMService.conn;
            if (!conn.isConnected()) {
                conn.connect();
            }
            String appServer = GlobalContext.getInstance().getResources().getString(R.string.app_server);
            VCard vCard = new VCard();
            vCard.setNickName(IMService.mCurNickName);
            vCard.setField(Const.USERID, IMService.mCurUserid);
            vCard.setField(Const.USERNAME, IMService.mCurAccount.substring(0, IMService.mCurAccount.lastIndexOf("@")));
            vCard.setField(Const.NICKNAME, IMService.mCurNickName);
            vCard.setField(Const.ORGID, IMService.mCurOrgid);
            vCard.setField(Const.ORGNAME, IMService.mCurOrgname);
            vCard.setField(Const.USERPHONE, IMService.mCurUserphone);
            vCard.setField(Const.PHONETIC, IMService.mCurPhonetic);
            vCard.setField(Const.IDCARD, IMService.mCurIdcard);
            vCard.setField(Const.QQ, IMService.mCurQq);
            vCard.setField(Const.EMAIL, IMService.mCurEmail);
            vCard.setField(Const.AVATARURL, IMService.mCurAvatarUrl.substring(appServer.length()));
            vCard.setField(Const.USERDESC, IMService.mCurDesc);
            vCard.setField(Const.BACKGROUND, IMService.mCurBackground.substring(appServer.length()));
            vCard.setField(Const.SCORE, IMService.mCurScore);
            vCard.setField(Const.VIP, IMService.mCurVip);
            vCard.setField(Const.ROLETYPE, IMService.mCurRoletype);
            vCard.setField(Const.SN, IMService.mCurSn);
            vCard.setField(Const.COMPANY, IMService.mCurCompany);
            vCard.setField(Const.SPECIALTY, IMService.mCurSpecialty);
            vCard.setField(Const.POSITIONAL, IMService.mCurPositional);
            vCard.setField(Const.WORKINGLIFE, IMService.mCurWorkinglife);
            vCard.setField(Const.ADDRESS, IMService.mCurAddress);
            vCard.save(conn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String filterChineseToUrl(String str){
        String encode = "";
        try {
            encode = URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encode;
    }


    public static String encodeBase64(String data) {
        byte[] bytes = null;
        try {
            bytes = data.getBytes("ISO-8859-1");//<-------Here
        }
        catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        if(bytes!=null){
            return new String(bytes);
        }
        return "";
    }

    /**
     * 获取用户Vcard字段信息
     *
     * @param conn
     * @param user
     * @param field
     * @return
     */
    public static String getVcardInfo(AbstractXMPPConnection conn, String user, String field) {
        VCard vcard = getUserVcard(conn, user);
        return vcard.getField(field);
    }


    /**
     * 申请添加好友
     *
     * @param user JID  xxx@softfun
     */
    public static void addFriend(String user, String nickname) {
        try {
            // 申请添加好友
            Roster roster = Roster.getInstanceFor(IMService.conn);
            //Roster roster = IMService.conn.getRoster();
            String JID = user + "@" + Const.APP_PACKAGENAME;
            roster.createEntry(JID, nickname, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 改变状态
     *
     * @param mode
     */
    public static void setPresence(Presence.Mode mode) {
        try {
            //在线状态
            Presence presence = new Presence(Presence.Type.available);
            if (mode.equals(Presence.Mode.available)) {
                //模式
                presence.setMode(Presence.Mode.available);
                //签名
                presence.setStatus("在线");
            } else if (mode.equals(Presence.Mode.away)) {
                //模式
                presence.setMode(Presence.Mode.away);
                //签名
                presence.setStatus("离开");
            }
            if (mode.equals(Presence.Mode.xa)) {
                //模式
                presence.setMode(Presence.Mode.xa);
                //签名
                presence.setStatus("外出");
            }
            if (mode.equals(Presence.Mode.dnd)) {
                //模式
                presence.setMode(Presence.Mode.dnd);
                //签名
                presence.setStatus("请勿打扰");
            }
            if (mode.equals(Presence.Mode.chat)) {
                //模式
                presence.setMode(Presence.Mode.chat);
                //签名
                presence.setStatus("正在聊天");
            }
            IMService.conn.sendStanza(presence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

























//    /**
//     * 初始化会议室列表
//     */
//    public static List<HostedRoom> getHostRooms() {
//        if (IMService.conn == null)
//            return null;
//        Collection<HostedRoom> hostrooms;
//        List<HostedRoom> roominfos = new ArrayList<>();
//        try {
//            new ServiceDiscoveryManager(IMService.conn);
//            hostrooms = MultiUserChat.getHostedRooms(IMService.conn, IMService.conn.getServiceName());
//            for (HostedRoom entry : hostrooms) {
//                roominfos.add(entry);
//                //System.out.println("room  名字：" + entry.getName() + " - ID:" + entry.getJid());
//            }
//            //System.out.println("room  服务会议数量:" + roominfos.size());
//        } catch (XMPPException e) {
//            e.printStackTrace();
//        }
//        return roominfos;
//    }









    /**
     * 创建房间
     *
     * @param roomName 房间名称
     */
    public static String createRoom(String user, String roomName, String password) {
        if (IMService.conn == null)
            return null;

        MultiUserChat muc;
        try {
            // 创建一个MultiUserChat
            // roomName不能相同
            String uuid = UUID.randomUUID().toString();
            // todo OFMUCROOM表name字段扩充到255长度
            roomName = roomName+"#"+uuid ;
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(IMService.conn);
            muc = manager.getMultiUserChat(roomName+"@conference." + IMService.conn.getServiceName());//"myroom@conference.jabber.org"
            //muc = new MultiUserChat(IMService.conn, roomName+"@conference." + IMService.conn.getServiceName());
            // 创建聊天室
            muc.create(roomName);
            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复
            for (int i = 0; i < form.getFields().size(); i++) {
                FormField field = form.getFields().get(i);
                if (!FormField.Type.hidden.equals(field.getType()) && field.getVariable() != null) {
                    //设置默认值作为答复
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
            // 设置聊天室的新拥有者
            List<String> owners = new ArrayList<String>();
            owners.add(IMService.conn.getUser());// 用户JID
            submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            //设置房间描述
            submitForm.setAnswer("muc#roomconfig_roomdesc", roomName + "的描述");
            //设置房间最大用户数
            List<String> maxusers = new ArrayList<String>();
            maxusers.add("500");
            submitForm.setAnswer("muc#roomconfig_maxusers", maxusers);
            List<String> cast_values = new ArrayList<String>();
            cast_values.add("moderator");
            cast_values.add("participant");
            cast_values.add("visitor");
            submitForm.setAnswer("muc#roomconfig_presencebroadcast", cast_values);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            // 房间仅对成员开放
            submitForm.setAnswer("muc#roomconfig_membersonly", false);
            // 允许占有者邀请其他人
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            if (!password.equals("")) {
                // 进入是否需要密码
                submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
                // 设置进入密码
                submitForm.setAnswer("muc#roomconfig_roomsecret", password);
            }
            // 能够发现占有者真实 JID 的角色
            List<String> whois_values = new ArrayList<String>();
            whois_values.add("anyone");
            submitForm.setAnswer("muc#roomconfig_whois", whois_values);
            //是否启用聊天室的谈话的公共记录
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 仅允许注册的昵称登录
            submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
            // 允许使用者修改昵称
            submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);
            // 允许用户注册房间
            submitForm.setAnswer("x-muc#roomconfig_registration", false);
            /*====================    =====================*/
            //设置为公共房间
            submitForm.setAnswer("muc#roomconfig_publicroom", true);
            /*====================    =====================*/

            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            muc.sendConfigurationForm(submitForm);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return roomName;
    }

    /**
     * 加入会议室
     *
     * @param user      昵称
     * @param password  会议室密码
     * @param roomsName 会议室名
     */
    public static MultiUserChat joinMultiUserChat(String user, String roomsName,  String password) {
        if (IMService.conn == null)
            return null;
        try {
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(IMService.conn);
            MultiUserChat muc = manager.getMultiUserChat(roomsName + "@conference." + IMService.conn.getServiceName());
            //MultiUserChat muc = new MultiUserChat(IMService.conn, roomsName + "@conference." + IMService.conn.getServiceName());

            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
            // 用户加入聊天室
            muc.join(user, password,history, SmackConfiguration.getDefaultPacketReplyTimeout());
            //System.out.println("会议室【" + roomsName + "】加入成功........");
            return muc;
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("会议室【" + roomsName + "】加入失败........");
        }
        return null;
    }







    /**
     * 获取群聊对象
     * @param roomjid
     * @return
     */
    public static MultiUserChat getMultiUserChat(String roomjid){
        if(!TextUtils.isEmpty(roomjid)){
            //return new MultiUserChat(IMService.conn, roomjid);
            if(IMService.mMultiUserChatMap.containsKey(roomjid)){
                MultiUserChat multiUserChat = IMService.mMultiUserChatMap.get(roomjid);
                return multiUserChat;
            }
        }
        return null;
    }


    /**
     * 发送群聊
     * @param muc -
     * @param msg -
     */
    public static void sendMultiChatMessage(MultiUserChat muc ,Message msg){
        try {
            if(muc!=null){
                muc.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 邀请加入群
     * @param muc -
     * @param account -
     * @param reason -
     */
    public static void Invite(MultiUserChat muc,String groupjid,String account,String reason) {
        if(muc!=null && account!=null){
            if(IMService.mMultiUserChatMap.containsKey(groupjid)){
                MultiUserChat multiUserChat = IMService.mMultiUserChatMap.get(groupjid);
                if(multiUserChat!=null){
                    try {
                        multiUserChat.invite(account,reason);
                    } catch (SmackException.NotConnectedException e) {
                        //System.out.println("==================== 邀请群失败 =====================");
                        e.printStackTrace();
                    }
                }else{
                    //System.out.println("====================  multiUserChat 为空 =====================");
                }
            }
        }
    }

    /**
     * 踢人
     * @param mTargetRoomJid
     * @param kickedUsername
     */
    public static void Kick(final String mTargetRoomJid, final String kickedUsername){
        if(IMService.mMultiUserChatMap.containsKey(mTargetRoomJid+Const.ROOM_JID_SUFFIX)){
            final MultiUserChat multiUserChat = IMService.mMultiUserChatMap.get(mTargetRoomJid+Const.ROOM_JID_SUFFIX);
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    //1、创建一个消息
                    //群聊消息的结构体，跟私聊不太一样，不能设置to，from，message会自动根据所在roomjid进行赋值
                    //Message msg = new Message(mTargetRoomJid+Const.ROOM_JID_SUFFIX, org.jivesoftware.smack.packet.Message.Type.groupchat);
                    Message msg = multiUserChat.createMessage();
                    msg.setBody("");
                    JivePropertiesExtension jpe = new JivePropertiesExtension();
                    jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_GROUP_KICK);
                    jpe.setProperty(Const.GROUP_JID, mTargetRoomJid);
                    jpe.setProperty(Const.ACCOUNT, IMService.mCurAccount);
                    jpe.setProperty(Const.MSGFLAG_GROUP_KICKED_USERNAME,kickedUsername);
                    msg.addExtension(jpe);
                    AsmackUtils.sendMultiChatMessage(multiUserChat,msg);
                }
            });
        }
    }




    /**
     * 更新群成员
     * @param jid
     * @param memberBean
     * @return
     */
    public static int updateGroupMemberMap(String jid,GroupMemberBean memberBean){
        int result = 0;
        //保证mGroupMemberMap不为空
        if(IMService.mGroupMemberMap==null){
            IMService.mGroupMemberMap = new HashMap<>();
        }
        //如果mGroupMemberMap有数据
        if(IMService.mGroupMemberMap.size()>0){
            //如果有此群
            if(IMService.mGroupMemberMap.containsKey(jid)){
                List<GroupMemberBean> userList = IMService.mGroupMemberMap.get(jid);
                //如果此群内有群成员
                if(userList!=null && userList.size()>0){
                    int position = -1;
                    for (int i = 0; i < userList.size(); i++) {
                        //遍历此群的成员，如果有此人，就中断
                        if(userList.get(i).getAccount().equals(memberBean.getAccount())){
                            position = i;
                            break;
                        }
                    }
                    //如果不存在此成员
                    if( position == -1 ){
                        //不存在此成员,添加进来
                        List<GroupMemberBean> newUserList = new ArrayList<>();
                        for (int i = 0; i < userList.size(); i++) {
                            newUserList.add(userList.get(i));
                        }
                        newUserList.add(memberBean);
                        IMService.mGroupMemberMap.remove(jid);
                        IMService.mGroupMemberMap.put(jid,newUserList);
                        result = 1;
                    }
                }
            }else{
                //没有此群
                List<GroupMemberBean> newUserList = new ArrayList<>();
                newUserList.add(memberBean);
                IMService.mGroupMemberMap.put(jid,newUserList);
                result = 1;
            }
        }else{
            //没有数据
            List<GroupMemberBean> newUserList = new ArrayList<>();
            newUserList.add(memberBean);
            IMService.mGroupMemberMap.put(jid,newUserList);
            result = 1;
        }
        return result;
    }

    /**
     * 获取群成员信息，此方法移动至登录后监听每一个群的方法之后立即执行
     * @param jid
     * @return
     */
    public static void getGroupMember(final String jid){
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                List<GroupMemberBean> list = HttpUtil.okhttpPost_queryGroupMembers(jid);
                for (int i = 0; i < list.size(); i++) {
                    updateGroupMemberMap(jid,list.get(i));
                }
            }
        });
    }

    /**
     * 获取单个群成员
     * @param jid
     * @param account
     * @return
     */
    public static GroupMemberBean getGroupMemberBean(String jid,String account){
        if(IMService.mGroupMemberMap==null){
            return null;
        }else{
            if(IMService.mGroupMemberMap.size()>0){
                if(IMService.mGroupMemberMap.containsKey(jid)){
                    List<GroupMemberBean> userList = IMService.mGroupMemberMap.get(jid);
                    //如果此群内有群成员
                    if(userList!=null && userList.size()>0){
                        int position = -1;
                        for (int i = 0; i < userList.size(); i++) {
                            //遍历此群的成员，如果有此人，就中断
                            if(userList.get(i).getAccount().equals(account)){
                                position = i;
                                break;
                            }
                        }
                        //如果不存在此成员
                        if( position == -1 ){
                            return null;
                        }else{
                            return userList.get(position);
                        }
                    }else{
                        return null;
                    }
                }else{
                    return null;
                }
            }else{
                return null;
            }
        }
    }


    public static String getGroupMemberField(String jid,String account,String field){
        GroupMemberBean groupMemberBean = getGroupMemberBean(jid, account);
        if(groupMemberBean!=null){
            if(field.equals(GroupDbHelper.GroupMemberTable.ACCOUNT)){
                return groupMemberBean.getAccount();
            }
            if(field.equals(GroupDbHelper.GroupMemberTable.AVATARURL)){
                return  GlobalContext.getInstance().getResources().getString(R.string.app_server) + groupMemberBean.getAvatarurl();
            }
            if(field.equals(GroupDbHelper.GroupMemberTable.NICKNAME)){
                return  groupMemberBean.getNickname();
            }
            if(field.equals(GroupDbHelper.GroupMemberTable.PINYIN)){
                return groupMemberBean.getPinyin();
            }
        }
            return "";
    }

    /**
     * 转移群管理员
     * @param selectAccount
     * @param mTargetRoomJid
     */
    public static void changeGroupMaster(final String oldMaster, final String selectAccount, final String mTargetRoomJid) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                int code = HttpUtil.okhttpPost_updateGroupMaster(mTargetRoomJid,AsmackUtils.filterAccount(selectAccount));
                if(code>0){
                    //群主更新成功
                    //发送群消息，通知各群成员更新群主
                    if(IMService.mMultiUserChatMap.containsKey(mTargetRoomJid+Const.ROOM_JID_SUFFIX)){
                        final MultiUserChat multiUserChat = IMService.mMultiUserChatMap.get(mTargetRoomJid+Const.ROOM_JID_SUFFIX);
                        //1、创建一个消息
                        //群聊消息的结构体，跟私聊不太一样，不能设置to，from，message会自动根据所在roomjid进行赋值
                        //Message msg = new Message(mTargetRoomJid + Const.ROOM_JID_SUFFIX, org.jivesoftware.smack.packet.Message.Type.groupchat);
                        Message msg = multiUserChat.createMessage();
                        msg.setBody("");
                        JivePropertiesExtension jpe = new JivePropertiesExtension();
                        jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_GROUP_CHANGEMASTER);
                        jpe.setProperty(Const.GROUP_JID, mTargetRoomJid);
                        jpe.setProperty(Const.ACCOUNT, oldMaster);
                        jpe.setProperty(Const.MSGFLAG_GROUP_NEW_MASTER, selectAccount);
                        msg.addExtension(jpe);
                        AsmackUtils.sendMultiChatMessage(multiUserChat, msg);
                    }
                }
            }
        });
    }

    /**
     * 我加入群，本群内其他成员更新我这个新成员信息
     */
    public static void updateOtherGroupMemberToUpdateMyInfo(String mTargetRoomJid) {
        if(IMService.mMultiUserChatMap.containsKey(mTargetRoomJid)){
            final MultiUserChat multiUserChat = IMService.mMultiUserChatMap.get(mTargetRoomJid);
            //1、创建一个消息
            //群聊消息的结构体，跟私聊不太一样，不能设置to，from，message会自动根据所在roomjid进行赋值
            //Message msg = new Message(mTargetRoomJid, org.jivesoftware.smack.packet.Message.Type.groupchat);
            Message msg = multiUserChat.createMessage();
            msg.setBody("");
            JivePropertiesExtension jpe = new JivePropertiesExtension();
            jpe.setProperty(Const.MSGFLAG, Const.MSGFLAG_GROUP_NEW_MEMBER);
            jpe.setProperty(Const.GROUP_JID, mTargetRoomJid);
            jpe.setProperty(Const.ACCOUNT, IMService.mCurAccount);
            msg.addExtension(jpe);
            AsmackUtils.sendMultiChatMessage(multiUserChat,msg);
        }
    }
}
