package com.softfun_xmpp.connection;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.CipherUtils;
import com.softfun_xmpp.utils.SpUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 连接管理单例
 */
public class ConnManager {
    //todo 必须添加此代码，才能执行自动连接服务器
    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //单例结构体--
    private ConnManager() {
        context = GlobalContext.getInstance();
        initConnection();
    }

    private static class SingletonHolder {
        private final static ConnManager INSTANCE = new ConnManager();
    }

    public static ConnManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    //单例结构体end--


    private XMPPConnection conn;
    private Context context;

    /**
     * 初始化连接
     */
    private void initConnection() {
        //创建连接配置对象
        ConnectionConfiguration config = new ConnectionConfiguration(
                context.getResources().getString(R.string.socket_ip),
                context.getResources().getInteger(R.integer.socket_port));
        //额外配置
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//明文传输
        config.setDebuggerEnabled(false);//开启调试模式
        config.setSendPresence(false);//发送在线状态,不发送，可以接受离线消息，true则不能接收离线消息
        config.setReconnectionAllowed(true);//自动重连接
        config.setCompressionEnabled(false);//关闭压缩
        // asmack bug
        configure1(ProviderManager.getInstance());
        //SmackAndroid.init(context);
        //创建连接对象
        conn = new XMPPConnection(config);
    }



    /**
     * 停止服务
     */
    public void stopServce() {
        Intent server = new Intent(context, IMService.class);
        context.stopService(server);
    }


    /**
     * 登录
     *
     * @param name
     * @param password
     * @return
     */
    public boolean login(String name, String password) {
        //System.out.println("====================  loginloginloginloginloginloginlogin  =====================");
        try {
            if (!conn.isConnected()) {
                conn.connect();
            }
            //System.out.println("鉴定：：：：" + conn.isAuthenticated());
            if (conn.getUser() == null) {
                String md5 = CipherUtils.md5(password);
                conn.login(name, md5);
                IMService.conn = conn;
                IMService.mCurAccount = name + "@" + Const.APP_PACKAGENAME;
                IMService.mCurNickName = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.NICKNAME);//accountManager.getAccountAttribute("name");
                IMService.mCurRoletype = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.ROLETYPE);
                IMService.mCurVip = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.VIP);
                IMService.mCurBackground = GlobalContext.getInstance().getResources().getString(R.string.app_server) + AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.BACKGROUND);
                IMService.mCurAvatarUrl = GlobalContext.getInstance().getResources().getString(R.string.app_server) + AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.AVATARURL);
                IMService.mCurUserid = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.USERID);
                IMService.mCurScore = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.SCORE);
                IMService.mCurQq = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.QQ);
                IMService.mCurEmail = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.EMAIL);
                IMService.mCurDesc = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.USERDESC);
                IMService.mCurOrgname = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.ORGNAME);
                IMService.mCurOrgid = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.ORGID);
                IMService.mCurUserphone = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.USERPHONE);
                IMService.mCurPhonetic = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.PHONETIC);
                IMService.mCurIdcard = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.IDCARD);
                IMService.mCurSn = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.SN);
                IMService.mCurCompany = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.COMPANY);
                IMService.mCurSpecialty = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.SPECIALTY);
                IMService.mCurPositional = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.POSITIONAL);
                IMService.mCurWorkinglife = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.WORKINGLIFE);
                IMService.mCurAddress = AsmackUtils.getVcardInfo(conn, IMService.mCurAccount, Const.ADDRESS);


                //接收离线消息
                OfflineMessageManager omm = new OfflineMessageManager(conn);//创建一个离线消息对象
                Iterator<Message> offlineMsglist = omm.getMessages();//获取消息，结果为一个Message迭代器
                if (IMService.mOfflineMsglist == null) {
                    IMService.mOfflineMsglist = new ArrayList<>();
                }
                while (offlineMsglist.hasNext()) {
                    Message message = offlineMsglist.next();
                    ////System.out.println("离线消息："+message);
                    IMService.mOfflineMsglist.add(message);
                }
                omm.deleteMessages();//将服务器上的离线消息删除。


            }
            AsmackUtils.setPresence(Presence.Mode.available);

            SpUtils.put(Const.USERNAME, name);
            SpUtils.put(Const.PASSWORD, password);

            //自动链接
//            if (connectionListener == null) {
//                connectionListener = new myConnectionListener();
//                conn.addConnectionListener(connectionListener);
//            }
            conn.addConnectionListener(new ConnectionListener() {
                @Override
                public void connectionClosed() {
                    //System.out.println("====================  connectionClosed  =====================");
                }
                @Override
                public void connectionClosedOnError(Exception e) {
                    //System.out.println("====================  connectionClosedOnError  =====================");
                    //IMService.isReConnection = true;
                    if (e instanceof XMPPException) {
                        XMPPException xe = (XMPPException) e;
                        final StreamError error = xe.getStreamError();
                        String errorCode = "";
                        if (error != null) {
                            errorCode = error.getCode();// larosn 0930
                            //System.out.println("====================  " + "IMXmppManager 连接断开，错误码:" + errorCode + "  =====================");
                            if (errorCode.equalsIgnoreCase("conflict")) {// 被踢下线
                                //发送广播
                                Intent intent = new Intent();
                                intent.setAction(Const.RELOGIN_BROADCAST_ACTION);
                                intent.putExtra("msg", "您的帐号已在其他设备上登录。！");
                                context.sendBroadcast(intent);
                                return;
                            }
                        }
                    }
                }
                @Override
                public void reconnectingIn(int seconds) {
                    //System.out.println("====================  reconnectingIn  =====================");
                }
                @Override
                public void reconnectionSuccessful() {
                    //System.out.println("====================  reconnectionSuccessful  =====================");
                    login(SpUtils.get(Const.USERNAME, "").toString(), SpUtils.get(Const.PASSWORD, "").toString());
                }
                @Override
                public void reconnectionFailed(Exception e) {
                    //System.out.println("====================  reconnectionFailed  =====================");
                }
            });


            //todo 启动服务，
            // 1、同步服务器上的花名册，更新或插入到本机数据库，
            // 2、添加花名册监听器
            Intent server = new Intent(context, IMService.class);
            context.startService(server);

            return true;
        } catch (XMPPException e) {
            //登录失败
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    SpUtils.remove(Const.PASSWORD);
                    ToastUtils.showToastSafe("登录失败");
                    IMService.mReLoginCount++;
                }
            });
            e.printStackTrace();
            initConnection();
            return false;
        }
    }


//    /**
//     * 用户注册
//     *
//     * @param userBean
//     * @return
//     */
//    public int regist(UserBean userBean) {
//        int code;
//        try {
//            if (!conn.isConnected()) {
//                conn.connect();
//            }
//            Map<String, String> map = new HashMap<>();
//            map.put("name", userBean.getNickname());
//            map.put("email", userBean.getPhone());
//            AccountManager accountManager = conn.getAccountManager();
//            accountManager.createAccount(userBean.getUsername(), userBean.getPassword(), map);
//            code = 0;
//        } catch (XMPPException e) {
//            e.printStackTrace();
//            //System.out.println("-----:" + e.getXMPPError().toXML());
//            //System.out.println("----");
//            code = e.getXMPPError().getCode();
//            if (code == 409) {
//                //System.out.println("用户已被注册");
//                return code;
//            }
//        }
//        return code;
//    }

//    /**
//     * 获取用户头像信息
//     * @param connection
//     * @param user
//     * @return
//     */
//    public static Drawable getUserImage(XMPPConnection connection, String user) {
//        ByteArrayInputStream bais = null;
//        try {
//            VCard vcard = new VCard();
//            vcard.load(connection, AsmackUtils.filterAccount(user));
//            if (vcard.getAvatar() == null)
//                return null;
//            bais = new ByteArrayInputStream(vcard.getAvatar());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (bais == null)
//            return null;
//        return FormatTools.getInstance().InputStream2Drawable(bais);
//    }


    /**
     * 解决asmack bug
     *
     * @param pm
     */
    private void configure(ProviderManager pm) {
        pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
        // Time
        try {
            pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Roster Exchange
        pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());
        // Message Events
        pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());
        // Chat State
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        // XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());
        // Group Chat Invitations
        pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());
        // Service Discovery # Items //解析房间列表
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        // Service Discovery # Info //某一个房间的信息
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
        // MUC User
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
        // MUC Admin
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
        // MUC Owner
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
        // Delayed Delivery
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
        // Version
        try {
            pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }
        // VCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
        // Offline Message Indicator
        pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
        // Last Activity
        pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
        // User Search
        pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
        // SharedGroupsInfo
        pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());
        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());
        pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
        pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.MalformedActionError());
        pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadLocaleError());
        pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadPayloadError());
        pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadSessionIDError());
        pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.SessionExpiredError());
    }

    public static void configure1(ProviderManager pm) {
        // Private Data Storage
        pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
        // Time
        try {
            pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
        }
        // Roster Exchange
        pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());
        // Message Events
        pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider());
        // Chat State
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
        // XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());
        // Group Chat Invitations
        pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());
        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
        // MUC User
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
        // MUC Admin
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
        // MUC Owner
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
        // Delayed Delivery
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
        // Version
        try {
            pm.addIQProvider("query", "jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }
        // VCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());
        // Offline Message Indicator
        pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());
        // Last Activity
        pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
        // User Search
        pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
        // SharedGroupsInfo
        pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());
        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address", new MultipleAddressesProvider());
        // FileTransfer
        pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
        // Privacy
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
        pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.MalformedActionError());
        pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadLocaleError());
        pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadPayloadError());
        pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadSessionIDError());
        pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.SessionExpiredError());
    }
}
