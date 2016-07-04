package com.softfun_xmpp.connection;


import android.content.Context;
import android.content.Intent;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.CipherUtils;
import com.softfun_xmpp.utils.SpUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 连接管理单例
 */
public class ConnManager {


    private MyConnectionListener myConnectionListener;

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


    private AbstractXMPPConnection conn;
    private Context context;

    /**
     * 初始化连接
     */
    private void initConnection() {
        try {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setServiceName("softfun")
                    .setHost(context.getResources().getString(R.string.socket_ip))
                    .setPort(context.getResources().getInteger(R.integer.socket_port))
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setCompressionEnabled(true)
                    .setDebuggerEnabled(false)
                    .setSendPresence(false)
                    .build();

            conn = new XMPPTCPConnection(config);

            //conn.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            if (!conn.isConnected()) {
                conn.connect();
            }
            if (conn!=null && conn.isConnected()  && !conn.isAuthenticated() && conn.getUser() == null) {
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
                List<Message> offlineMsglist = omm.getMessages();//获取消息，结果为一个Message迭代器
                if (IMService.mOfflineMsglist == null) {
                    IMService.mOfflineMsglist = new ArrayList<>();
                }
                for (int i = 0; i < offlineMsglist.size(); i++) {
                    Message message = offlineMsglist.get(i);
                    IMService.mOfflineMsglist.add(message);
                }
                omm.deleteMessages();//将服务器上的离线消息删除。
            }
            AsmackUtils.setPresence(Presence.Mode.available);

            SpUtils.put(Const.USERNAME, name);
            SpUtils.put(Const.PASSWORD, password);


            PingManager pingManager = PingManager.getInstanceFor(conn);
            PingManager.setDefaultPingInterval(30);
            pingManager.setPingInterval(30);
            pingManager.registerPingFailedListener(new PingFailedListener() {
                @Override
                public void pingFailed() {
                    System.out.println("====================  pingFailed  =====================");
                    login(SpUtils.get(Const.USERNAME, "").toString(), SpUtils.get(Const.PASSWORD, "").toString());
                }
            });
            ServerPingWithAlarmManager.getInstanceFor(conn).isEnabled();
            ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(conn);
            reconnectionManager.enableAutomaticReconnection();
            //自动链接
            if(myConnectionListener==null){
                myConnectionListener = new MyConnectionListener();
                conn.addConnectionListener(myConnectionListener);
            }


            //todo 启动服务，
            // 1、同步服务器上的花名册，更新或插入到本机数据库，
            // 2、添加花名册监听器
            Intent server = new Intent(context, IMService.class);
            context.startService(server);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //登录失败
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    SpUtils.remove(Const.PASSWORD);
                    ToastUtils.showToastSafe("登录失败");
                    IMService.mReLoginCount++;
                }
            });
            initConnection();
            return false;
        }
    }

    private class MyConnectionListener implements ConnectionListener{
        @Override
        public void connected(XMPPConnection connection) {
            System.out.println("====================  connected  =====================");
        }
        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            System.out.println("====================  authenticated  =====================");
        }
        @Override
        public void connectionClosed() {
            System.out.println("====================  connectionClosed  =====================");
            conn.removeConnectionListener(myConnectionListener);
            myConnectionListener = null;
        }
        /**
         * 重复登录，T人代码 需要调试
         * @param e
         */
        @Override
        public void connectionClosedOnError(Exception e) {
            conn.removeConnectionListener(myConnectionListener);
            myConnectionListener = null;
            System.out.println("====================  connectionClosedOnError  ====================="+e);
            if (e instanceof XMPPException.StreamErrorException) {
                XMPPException.StreamErrorException xe = (XMPPException.StreamErrorException) e;
                final StreamError error = xe.getStreamError();
                if (error != null) {
                    if (error.getCondition().name().equalsIgnoreCase("conflict")) {// 被踢下线
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
        public void reconnectionSuccessful() {
            System.out.println("====================  reconnectionSuccessful  =====================");
            login(SpUtils.get(Const.USERNAME, "").toString(), SpUtils.get(Const.PASSWORD, "").toString());
        }
        @Override
        public void reconnectingIn(int seconds) {
            System.out.println("====================  "+seconds+"  =====================");
            ToastUtils.showToastSafe("重新连接倒计时："+seconds);
        }
        @Override
        public void reconnectionFailed(Exception e) {
            System.out.println("====================  reconnectionFailed  =====================");
        }
    }

}
