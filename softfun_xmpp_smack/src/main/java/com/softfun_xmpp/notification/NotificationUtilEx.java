package com.softfun_xmpp.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.text.Html;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.bean.NotificationBean;
import com.softfun_xmpp.constant.Const;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 范张 on 2016-06-29.
 */
public class NotificationUtilEx {

    /**
     * 通知管理器
     */
    private final static NotificationManager mNotificationManager =  (NotificationManager) GlobalContext.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
    /***
     * 通知构建器
     */
    private final static NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(GlobalContext.getInstance());
    /**
     *通知id，用于识别此通知的唯一标识，可用于更新通知
     */
    private  static int MESSAGE_ID = 0;
    /**
     *
     */
    private static Map<String,NotificationBean> mMap;



    private NotificationUtilEx() {
        mMap = new HashMap<>();
    }
    private static class SingletonHolder {
        private final static NotificationUtilEx INSTANCE = new NotificationUtilEx();
    }
    public static NotificationUtilEx getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 发送通知消息
     * @param from
     * @param title
     * @param msg
     * @param chattype
     * @param nickname
     * @param avatarurl
     * @param group_jid
     */
    public  void notification_msg(String from,String title,String msg,String chattype ,String nickname,String avatarurl,String group_jid){

        if(!mMap.containsKey(from)){
            NotificationBean bean = new NotificationBean();
            bean.setAvatarurl(avatarurl);
            bean.setChattype(chattype);
            bean.setFrom(from);
            bean.setGroup_jid(group_jid);
            bean.setMsg(msg);
            bean.setNickname(nickname);
            bean.setTitle(title);
            mMap.put(from,bean);
        }else{
            NotificationBean bean = mMap.get(from);
            bean.setMsg(msg);
            mMap.put(from,bean);
        }

        for (Map.Entry<String, NotificationBean> entry : mMap.entrySet()) {
            if(entry.getKey().equals(from)){
                NotificationBean value = entry.getValue();

                int REQUESTID = (int) System.currentTimeMillis();

                mBuilder.setLargeIcon(BitmapFactory.decodeResource(GlobalContext.getInstance().getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.mipmap.ic_launcher_thumb)
                        .setTicker(value.getMsg())                                                                     //设置状态栏的显示的信息  jsonBean.getSourcename() + ":" + jsonBean.getHtmltext()
                        .setContentTitle(value.getTitle())           //设置下拉后通知标题
                        .setContentText(value.getMsg())                                                                  //设置下拉后通知文本
                        .setWhen(System.currentTimeMillis())                                                                //设置时间发生时间
                        .setAutoCancel(true)                                                              //设置是否点击后自动清除
                        .setDefaults(Notification.DEFAULT_ALL)                                                              //声音、震动、闪光
                        //.setOngoing(true)                                                                 //不能被用户x掉，会一直显示，如音乐播放等
                        .setPriority(Notification.PRIORITY_DEFAULT)                                         //优先级
                ;
                NotificationCompat.InboxStyle inboxStyle =   new NotificationCompat.InboxStyle();
                // 设定一个标题的收件箱展开布局
                inboxStyle.setBigContentTitle(value.getTitle());//覆盖setContentTitle
                inboxStyle.addLine(value.getMsg());
                inboxStyle.addLine(Html.fromHtml("<b>来自："+GlobalContext.getInstance().getResources().getString(R.string.app_name)+"</b>"));
                mBuilder.setStyle(inboxStyle);

                Intent intent = new Intent();
                intent.setAction("com.softfun_xmpp.activity.MainActivity");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.putExtra(Const.TYPE,value.getChattype());
                intent.putExtra(Const.NOTIFICATION_FROM,value.getFrom());
                intent.putExtra(Const.NICKNAME,value.getNickname());
                intent.putExtra(Const.AVATARURL,value.getAvatarurl());
                intent.putExtra(Const.GROUP_JID,value.getGroup_jid());

                PendingIntent resultPendingIntent = PendingIntent.getActivity(GlobalContext.getInstance(), REQUESTID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);//点击的动作

                MESSAGE_ID = entry.getKey().hashCode();
                mNotificationManager.notify(MESSAGE_ID, mBuilder.build());
            }
        }
    }



    /**
     * 清除已读通知消息
     */
    public void deleteNotification(String from){
        if(mMap!=null){
            if(mMap.containsKey(from)){
                mMap.remove(from);
            }
        }
    }

}
