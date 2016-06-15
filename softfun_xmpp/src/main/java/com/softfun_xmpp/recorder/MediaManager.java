package com.softfun_xmpp.recorder;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class MediaManager{

    private static MediaPlayer mMediaPlayer;
    private static boolean isPuase ;
    public static String mDirection;
    public static void playSound(String direction, String soundFile, MediaPlayer.OnCompletionListener listener){
        mDirection = direction;
        if(mMediaPlayer==null){
            mMediaPlayer = new MediaPlayer();
            //设置异常监听
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    //直接重置
                    mMediaPlayer.reset();
                    return false;
                }
            });
        }else{
            mMediaPlayer.reset();
        }
        //设置参数
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setDataSource(soundFile);
            //prepare异步实现
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            //mMediaPlayer.prepare();//同步实现
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void pause(){
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            isPuase = true;
        }
    }

    public static void resume(){
        if(mMediaPlayer!=null && isPuase){
            mMediaPlayer.start();
            isPuase = false;
        }
    }

    public static void release(){
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


}
