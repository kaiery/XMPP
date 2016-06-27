package com.softfun_xmpp.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;

import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 范张 on 2016-01-27.
 */
public  class NotificationUtil {

    private static NotificationManager mNotificationManager1 =  (NotificationManager) GlobalContext.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
    private static NotificationCompat.Builder mBuilder1 = new NotificationCompat.Builder(GlobalContext.getInstance());
    //通知id，用于识别此通知的唯一标识，可用于更新通知
    private static int DOWNLOAD_UPDATEFILE_ID = 0;


    private static Map<String,List<Message>> chatmap;


    /**
     * 清除已读消息list
     * @param sourceid
     */
    public static void deleteItem(String sourceid){
        if(chatmap!=null){
            if(chatmap.containsKey(sourceid)){
                chatmap.remove(sourceid);
            }
        }
    }

    public static void deleteNotifi(String sourceid){
        mNotificationManager1.cancel(sourceid.hashCode());
    }

    /***
     * 发送文本通知
     */
    public static void notification_msg(Message jsonBean){
        String title = GlobalContext.getInstance().getResources().getString(R.string.app_name);
        if(chatmap==null){
            //静态map为空
            chatmap = new HashMap<>();
            List<Message> list = new ArrayList<>();
            list.add(jsonBean);
            chatmap.put(jsonBean.getFrom(),list);
        }else{
            //检查是否存在此来源消息
            if(chatmap.containsKey(jsonBean.getFrom())){
                //如果已经存在消息来源，则在此来源消息数组中添加新消息
                chatmap.get(jsonBean.getFrom()).add(jsonBean);
            }else{
                //如果没有此消息来源，则新建一个此消息来源的数组
                List<Message> list = new ArrayList<>();
                list.add(jsonBean);
                chatmap.put(jsonBean.getFrom(), list);
            }
        }

        for (Map.Entry<String, List<Message>> entry : chatmap.entrySet()) {

            if(entry.getKey().equals(jsonBean.getFrom())){

                //通知ID
                int MESSAGE_ID = entry.getKey().hashCode();
                //System.out.println("MESSAGE_ID:"+MESSAGE_ID);
                int REQUESTID = (int) System.currentTimeMillis();
                //System.out.println("REQUESTID:"+REQUESTID);
                List<Message> list = entry.getValue();
                String userid = list.get(list.size()-1).getFrom();
                String showname = list.get(list.size()-1).getFrom();
                String ticker = list.get(list.size()-1).getBody();
                String text = list.get(list.size()-1).getBody();
//                if(list.get(list.size()-1).getChattype().equals("HTMLTEXT")){
//                    ticker = showname+"："+list.get(list.size()-1).getHtmltext();
//                    text = list.get(list.size()-1).getHtmltext();
//                }else if(list.get(list.size()-1).getChattype().equals("IMAGE")){
//                    ticker = showname+"："+"图片";
//                    text = showname+"给您发来一张图片。";
//                }else if(list.get(list.size()-1).getChattype().equals("RECORD")){
//                    ticker = showname+"："+"语音";
//                    text = showname+"给您发来一条语音。";
//                }else if(list.get(list.size()-1).getChattype().equals("TEXT")){
//                    ticker = showname+"："+list.get(list.size()-1).getHtmltext();
//                    text = list.get(list.size()-1).getHtmltext();
//                }else if(list.get(list.size()-1).getChattype().equals("APPLYFRIEND")){
//                    ticker = showname+"：请求加为好友";
//                    text = list.get(list.size()-1).getHtmltext();
//                }

//                Bundle bundle = new Bundle();
//
//                UserBean mUserBean = new UserBean();
//                mUserBean.setUserid(userid);
//                mUserBean.setShowname(showname);
//                bundle.putSerializable("userbean", mUserBean);
//
//                Intent intent = new Intent();
//                Intent[] intents = new Intent[2];
//                boolean isExsitMianActivity = IsExsitMianActivity();
//                if(isExsitMianActivity){
//                    //如果主程序已经在任务栈中
//                    intent.setAction("com.fanzhang.softfun.chat.PrivateChat");
//                    intent.addCategory("android.intent.category.DEFAULT");
//                    intent.putExtras(bundle);
//                }else{
//                    //如果主程序不在任务栈中，则需要开启2个intent，其中第1个就是主程序，第2个就是通知栏点击后进入的详细页面
//                    //但，如果是申请好友，则只需要进入主界面即可。
//                    if(list.get(list.size()-1).getChattype().equals("APPLYFRIEND")){
//                        intents[0] = Intent.makeRestartActivityTask(new ComponentName(GlobalContext.getInstance(), SplashActivity.class));
//
//                        //构造dynmsgbean
//                        DynMsgBean dynMsgBean = new DynMsgBean();
//                        dynMsgBean.setUserid(jsonBean.getSourceid());
//                        dynMsgBean.setShowname(jsonBean.getSourcename());
//                        dynMsgBean.setPinyin("");
//                        dynMsgBean.setPhonetic("");
//                        dynMsgBean.setVip("0");
//                        dynMsgBean.setUsertype("");
//                        dynMsgBean.setUserface(jsonBean.getSourceface());
//                        dynMsgBean.setUnreaded(1);
//                        dynMsgBean.setType(jsonBean.getType());
//                        dynMsgBean.setChattype(jsonBean.getChattype());
//                        dynMsgBean.setStamp(jsonBean.getStamp());
//                        dynMsgBean.setHtmltext(jsonBean.getHtmltext());
//                        dynMsgBean.setOwner(GlobalSharePreferences.getInstance().getUserid());
//                        bundle.putSerializable("dynmsgbean", dynMsgBean);
//                        intents[1] = new Intent();
//                        intents[1].setAction("com.fanzhang.softfun.add.ApplyFriendListActivity");
//                        intents[1].addCategory("android.intent.category.DEFAULT");
//                        intents[1].putExtras(bundle);
//                    }else{
//                        if(list.get(list.size()-1).getType().equals("CHAT")){
//                            //私聊
//                            intents[0] = Intent.makeRestartActivityTask(new ComponentName(GlobalContext.getInstance(), SplashActivity.class));
//                            intents[1] = new Intent();
//                            intents[1].setAction("com.fanzhang.softfun.chat.PrivateChat");
//                            intents[1].addCategory("android.intent.category.DEFAULT");
//                            intents[1].putExtras(bundle);
//                        }else if(list.get(list.size()-1).getType().equals("GROUPCHAT")){
//                            //群聊
//                        }
//                    }
//                }
//
//                mBuilder1.setLargeIcon(BitmapFactory.decodeResource(GlobalContext.getInstance().getResources(), R.mipmap.ic_launcher))
//                        .setSmallIcon(R.mipmap.ic_launcher_thumb)
//                        .setTicker(ticker)                                                                                  //设置状态栏的显示的信息
//                        .setContentTitle(title)           //设置下拉后通知标题
//                        .setContentText(text)                                                                  //设置下拉后通知文本
//                        .setWhen(System.currentTimeMillis())                                                                //设置时间发生时间
//                        .setAutoCancel(true)                                                                                //设置是否点击后自动清除
//                        .setDefaults(Notification.DEFAULT_ALL)                                                              //声音、震动、闪光
//                        .setPriority(Notification.PRIORITY_DEFAULT)                                         //优先级
//                ;
//                NotificationCompat.InboxStyle inboxStyle =   new NotificationCompat.InboxStyle();
//                // 设定一个标题的收件箱展开布局
//                inboxStyle.setBigContentTitle(title);//覆盖setContentTitle
//                inboxStyle.setSummaryText(showname);
//                inboxStyle.addLine(text);
//                for (int i = list.size()-2; i >= 0 ; i--) {
//                    inboxStyle.addLine(list.get(i).getHtmltext());
//                }
//                mBuilder1.setStyle(inboxStyle);
//                PendingIntent resultPendingIntent;
//
//                if(isExsitMianActivity){
//                    resultPendingIntent = PendingIntent.getActivity(GlobalContext.getInstance(), REQUESTID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                }else{
//                    resultPendingIntent = PendingIntent.getActivities(GlobalContext.getInstance(), REQUESTID, intents, PendingIntent.FLAG_UPDATE_CURRENT);
//                }
//
//                mBuilder1.setContentIntent(resultPendingIntent);//点击的动作
//                mNotificationManager1.notify(MESSAGE_ID, mBuilder1.build());
            }
        }
    }

//
//    /**
//     * 判断手机里是否已经还有com.fanzhang.softfun.MainActivity，
//     * 如果有，那么点击通知之后，进入聊天页面，返回之后，进入main
//     * 如果么有，那么点击通知之后，进入聊天页面，返回之后，重新开启splash页面，再进入main
//     * @return
//     */
//    private static boolean IsExsitMianActivity(){
//        System.out.println("GlobalUtil.getInstance().getMain():"+GlobalUtil.getInstance().getMain());
//        if(GlobalUtil.getInstance().getMain()!=null) {
//            System.out.println(true);
//            return true;
//        }else{
//            System.out.println(false);
//            return false;
//        }
//    }




    /**
     * 发送一个下载升级文件的通知，带进度条
     * @param max
     * @param progress
     */
    public static void notification_download_updatefile(int max,int progress){
        //通知id，用于识别此通知的唯一标识，可用于更新通知
        mBuilder1.setLargeIcon(BitmapFactory.decodeResource(GlobalContext.getInstance().getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher_thumb)
                .setTicker("正在下载更新文件...")
                .setContentTitle(GlobalContext.getInstance().getResources().getString(R.string.app_name))
                .setContentText("正在下载更新文件...")
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
;
        mBuilder1.setProgress(max, progress, false);
        mNotificationManager1.notify(DOWNLOAD_UPDATEFILE_ID, mBuilder1.build());
        if(max == progress){
            mBuilder1.setContentText("下载完成")
                    .setProgress(0, 0, false);// 移除
        }
        mNotificationManager1.notify(DOWNLOAD_UPDATEFILE_ID, mBuilder1.build());
    }

    /**
     * 刷新进度
     * @param max
     * @param progress
     */
    public static void notification_download_updatefile_progress(int max,int progress){
        mBuilder1.setProgress(max, progress, false);
        mNotificationManager1.notify(DOWNLOAD_UPDATEFILE_ID, mBuilder1.build());
    }

    /**
     * 删除进度通知
     */
    public static void notification_download_cancel(){
        mNotificationManager1.cancel(DOWNLOAD_UPDATEFILE_ID);
    }

    /**
     * 通知下载失败
     */
    public static void notification_download_failure(){
        mBuilder1.setContentText("下载失败")
                .setProgress(0, 0, false);// 移除
        mNotificationManager1.notify(DOWNLOAD_UPDATEFILE_ID, mBuilder1.build());
    }


}
