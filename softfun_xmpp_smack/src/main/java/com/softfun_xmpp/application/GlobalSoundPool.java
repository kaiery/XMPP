package com.softfun_xmpp.application;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.softfun_xmpp.R;


/**
 * Created by 范张 on 2016-02-18.
 */
public class GlobalSoundPool {


    private static SoundPool mSoundPool;

    /**
     * 敲门
     */
    private static int id_knock;
    /**
     * B一声提示音
     */
    private static int id_bi;
    /**
     * 摇一摇提示音
     */
    private static int id_rock;
    /**
     * 按下提示音
     */
    private static int id_press;
    /**
     * 摇一摇结果提示音
     */
    private static int id_result;
    /**
     * 视频等待提示音
     */
    private static int id_video;
    /**
     * 录音发送提示音
     */
    private static int id_xiu;

    private static int id_notification;


    private int play_videocoming;

    /**
     *系统自带通知声音
     */
    public final static int SYSTEM_NOTIFICATION = 99;

    public GlobalSoundPool() {
        init();
    }

    private static class SingletonHolder {
        private final static GlobalSoundPool INSTANCE = new GlobalSoundPool();
    }
    public static GlobalSoundPool getInstance() {
        return SingletonHolder.INSTANCE;
    }


    private static String getRealPathFromURI(Uri contentUri) {
        if(contentUri!=null){
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader loader = new CursorLoader(GlobalContext.getInstance(), contentUri, proj, null, null, null);
            Cursor cursor = loader.loadInBackground();
            if(cursor!=null){
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                cursor.close();
                return path;
            }else{
                if(contentUri.toString().contains("file://")){
                    return contentUri.toString().substring( contentUri.toString().indexOf("file://")+"file://".length()  );
                }else{
                    return "";
                }
            }
        }else{
            return "";
        }
    }


    private static void init(){
        try {
            //获取系统通知声音的Uri
            Uri uriSound = RingtoneManager.getActualDefaultRingtoneUri(GlobalContext.getInstance(), RingtoneManager.TYPE_NOTIFICATION );
            //获取系统通知声音的真实地址
            String soundPath = getRealPathFromURI(uriSound);
            //               /system/media/audio/notifications/Skyline.ogg
            if((Build.VERSION.SDK_INT) >= Build.VERSION_CODES.LOLLIPOP){
                soundInit_1(soundPath);
            }
            else{
                soundInit_0(soundPath);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private static void soundInit_0(String soundPath) {
        mSoundPool = new SoundPool(50, AudioManager.STREAM_SYSTEM, 0);

        id_bi=mSoundPool.load(GlobalContext.getInstance(), R.raw.v_bi,1);
        id_rock=mSoundPool.load(GlobalContext.getInstance(), R.raw.v_rock,1);
        id_result=mSoundPool.load(GlobalContext.getInstance(), R.raw.v_result,1);
        id_press=mSoundPool.load(GlobalContext.getInstance(), R.raw.v_press,1);
        id_video=mSoundPool.load(GlobalContext.getInstance(), R.raw.v_videocoming,1);
        id_xiu=mSoundPool.load(GlobalContext.getInstance(), R.raw.v_xiu,1);
        id_knock=mSoundPool.load(GlobalContext.getInstance(),R.raw.v_knock,1);
        if(!soundPath.equals("")){
            id_notification = mSoundPool.load(soundPath, 1);
        }else{
            id_notification = mSoundPool.load(GlobalContext.getInstance(),R.raw.v_msg,1);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void soundInit_1(String soundPath) {
        AudioAttributes aa = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(aa)
                .setMaxStreams(50)
                .build();
        id_bi = mSoundPool.load(GlobalContext.getInstance(), R.raw.v_bi, 1);
        id_rock = mSoundPool.load(GlobalContext.getInstance(), R.raw.v_rock,1);
        id_result = mSoundPool.load(GlobalContext.getInstance(), R.raw.v_result,1);
        id_press = mSoundPool.load(GlobalContext.getInstance(), R.raw.v_press,1);
        id_video = mSoundPool.load(GlobalContext.getInstance(), R.raw.v_videocoming,1);
        id_xiu = mSoundPool.load(GlobalContext.getInstance(),R.raw.v_xiu,1);
        id_knock = mSoundPool.load(GlobalContext.getInstance(),R.raw.v_knock,1);
        if(!soundPath.equals("")){
            id_notification = mSoundPool.load(soundPath, 1);
        }else{
            id_notification = mSoundPool.load(GlobalContext.getInstance(),R.raw.v_msg,1);
        }
    }





    public void play(int playid){
        switch( playid )
        {
            case SYSTEM_NOTIFICATION: {
                mSoundPool.play(id_notification,0.5f,0.5f,1,0,1);
                break;
            }
            case R.raw.v_bi: {
                mSoundPool.play(id_bi,0.5f,0.5f,1,0,1);
                break;
            }
            case R.raw.v_rock: {
                mSoundPool.play(id_rock,1,1,1,0,1);
                break;
            }
            case R.raw.v_press: {
                mSoundPool.play(id_press,1,1,1,0,1);
                break;
            }
            case R.raw.v_result: {
                mSoundPool.play(id_result,1,1,1,0,1);
                break;
            }
            case R.raw.v_videocoming: {
                play_videocoming = mSoundPool.play(id_video, 1, 1, 1, -1, 1);
                break;
            }
            case R.raw.v_xiu: {
                mSoundPool.play(id_xiu,1,1,1,0,1);
                break;
            }
            case R.raw.v_knock: {
                mSoundPool.play(id_knock,1,1,1,0,1);
                break;
            }
        }
    }

    /**
     * 停止播放 v_videocoming
     */
    public void stop(){
        if(mSoundPool!=null){
            mSoundPool.stop(play_videocoming);
        }
    }

}
