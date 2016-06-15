package com.softfun_xmpp.recorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Vibrator;

import com.softfun_xmpp.application.GlobalContext;

import java.io.File;
import java.util.UUID;

/**
 * Created by 范张 on 2016-02-17.
 * 录音类
 * 单例模式
 */
public class AudioManager {
    private MediaRecorder mMediaRecorder;
    private String mDir;
    private String mCurrentFilePath;


    //常量：已经准备好
    private boolean isPrepared;


    private AudioManager(String dir){
        mDir = dir;
    }
    private  Vibrator vibrator;

    /**
     * 提供一个回调接口
     * 准备完毕
     */
    public interface AudioStatsListener{
        void wellPrepared();
    }
    public AudioStatsListener mListener;
    public void setOnAudioStatsListener(AudioStatsListener listener){
        mListener = listener;
    }





    //单例模式
    private static AudioManager mInstance;
    public static AudioManager getInstance(String dir){
        if(mInstance == null){
            synchronized (AudioManager.class){
                if(mInstance == null){
                    mInstance = new AudioManager(dir);
                }
            }
        }
        return mInstance;
    }


    /**
     * 录音机准备函数
     */
    public void prepareAudio(){
        try {
            //震动
            vibrator = (Vibrator) GlobalContext.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
            long [] pattern = {1,1000,100,1000};   // {100,100,100,100}就是指的震动电机先关闭0.1秒再震动0.1秒，如此循环两次
            vibrator.vibrate(pattern,-1);  //-1为不循环震动，1为最高模式循环震动，2为所给参数的格式循环震动

            isPrepared = false;
            File dir = new File(mDir);
            if(!dir.exists()){
                dir.mkdirs();
            }
            String fileName = generateFileName();
            File file = new File(dir,fileName);
            mCurrentFilePath = file.getAbsolutePath();
            //System.out.println("刚录制的保存在："+mCurrentFilePath);
            mMediaRecorder = new MediaRecorder();
            //设置输出文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            //设置MediaRecorder的音频源是麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            //设置音频的编码为amr
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //设置完成，已经准备好
            mMediaRecorder.prepare();

            //如果还在震动
            if(vibrator.hasVibrator()){
                //取消震动
                vibrator.cancel();
            }
            vibrator = null;

            //启动
            mMediaRecorder.start();
            //准备完成
            isPrepared = true;

            if(mListener!=null){
                mListener.wellPrepared();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成随机文件名
     * @return
     */
    private String generateFileName() {
        return UUID.randomUUID().toString()+".amr";
    }

    public int getVoiceLevel(int maxLevel){
        if(isPrepared){
            //mMediaRecorder.getMaxAmplitude()值范围在1~32767之间,如下解释
            //mMediaRecorder.getMaxAmplitude() / 32768 = 0~1之间的小数值
            //maxLevel*0~1之间的小数值 = 0~maxLevel之间的小数值
            //0~maxLevel之间的小数值 + 1 就是我们预期要得到的正数 1~maxLevel
            try {
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 +1;
            } catch (Exception e) {

            }
        }
        return 1;
    }


    public void release(){
        if(mMediaRecorder!=null){
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void cancel(){
        release();
        if(mCurrentFilePath!=null){
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }


}
