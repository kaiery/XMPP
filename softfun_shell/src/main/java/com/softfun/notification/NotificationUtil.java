package com.softfun.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v7.app.NotificationCompat;

import com.softfun.R;
import com.softfun.application.GlobalContext;

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
     * 发送一个下载升级文件的通知，带进度条
     * @param max
     * @param progress
     */
    public static void notification_download_updatefile(int max,int progress){
        //通知id，用于识别此通知的唯一标识，可用于更新通知
        mBuilder1.setLargeIcon(BitmapFactory.decodeResource(GlobalContext.getInstance().getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
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
