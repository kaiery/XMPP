package com.softfun_xmpp.recorder;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalSoundPool;

import java.lang.ref.WeakReference;

/**
 * Created by 范张 on 2016-02-16.
 * 录音按钮
 */
public class AudioRecoderButton extends Button implements AudioManager.AudioStatsListener {

    private static final int DISTANCE_Y_CANCEL = 50; //将dp转换成px
    //三种状态
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECODERING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;
    //当前状态
    private int mCurState = STATE_NORMAL;
    //已经开始录音
    private boolean isRecodeing = false;

    private AudioDialogManager mDialogManager;

    private AudioManager mAudioManager;

    //录音时长
    private float mTime;
    //是否触发LongClick
    private boolean mReady;

    private Handler mHandler;


    //构造方法，让此构造方法调用2个参数的构造方法
    public AudioRecoderButton(Context context) {
        this(context, null);
    }

    public AudioRecoderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new AudioDialogManager(getContext());

        mHandler = new AudioRecordHandler(this);

        //设置一个存放录音的目录
        String dir = null;
        try {
            dir = Environment.getExternalStorageDirectory() +"/"+getResources().getString(R.string.record_folder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //得到单例模式
        mAudioManager = AudioManager.getInstance(dir);
        //设置回调方法，本类并实现接口
        mAudioManager.setOnAudioStatsListener(this);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    /**
     * 接口，录音完成后的回调
     */
    public interface AudioFinishRecorderListener{
        void onFinish(float seconds, String filePath);
    }
    private AudioFinishRecorderListener mListener;
    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener){
        mListener = listener;
    }




    /**
     * 开启一个子线程获取音量
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while(isRecodeing){
                try {
                    //每隔1秒获取一次
                    Thread.sleep(100);
                    mTime+=0.1f;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //handle常量：准备完毕
    private static final int MSG_AUDIO_PREPARED = 0x110;
    //音量变化
    private static final int MSG_VOICE_CHANGED = 0x111;
    //对话框隐藏
    private static final int MSG_DIALOG_DIMISS = 0x112;


//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch( msg.what )
//            {
//                case MSG_AUDIO_PREPARED: {
//                    //真正显示应该在audio end prepared 之后
//                    mDialogManager.showRecordingDialog();
//                    isRecodeing = true;
//                    //开启一个子线程获取音量
//                    new Thread(mGetVoiceLevelRunnable).start();
//                    break;
//                }
//                case MSG_VOICE_CHANGED: {
//                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
//                    break;
//                }
//                case MSG_DIALOG_DIMISS: {
//                    mDialogManager.dimissDialog();
//                    break;
//                }
//            }
//        }
//    };


    private class AudioRecordHandler extends Handler{

        /**
         * 宿主的弱引用
         */
        private final WeakReference<AudioRecoderButton> mTarget;

        /**
         * 构造方法，传递宿主类
         * @param context
         */
        public AudioRecordHandler(AudioRecoderButton context)
        {
            mTarget = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AudioRecoderButton target = mTarget.get();
            if(target == null){
                return;
            }
            switch( msg.what )
            {
                case MSG_AUDIO_PREPARED: {
                    //真正显示应该在audio end prepared 之后
                    mDialogManager.showRecordingDialog();
                    isRecodeing = true;
                    //开启一个子线程获取音量
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                }
                case MSG_VOICE_CHANGED: {
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                }
                case MSG_DIALOG_DIMISS: {
                    mDialogManager.dimissDialog();
                    break;
                }
            }
        }
    }

    /**
     * mAudioManager内部准备完毕后的回调
     */
    @Override
    public void wellPrepared() {
        //mAudioManager内部准备完毕,给handle发消息
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    /**
     * 触摸事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获得动作
        int action = event.getAction();
        //获得触摸点的XY
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                //播放一个很短暂的提示音:ban
                GlobalSoundPool.getInstance().play(R.raw.v_press);
                changeState(STATE_RECODERING);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isRecodeing) {
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECODERING);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if(!mReady){
                    //如果没有触发长按事件
                    reset();
                    return super.onTouchEvent(event);
                }
                //System.out.println("-------------"+mTime);
                if(!isRecodeing || mTime<0.6f){
                    //AudioManager没有内部准备好
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    //延迟1.3秒后关闭对话框
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS,1300);
                }else if (mCurState == STATE_RECODERING) {//正常录制结束
                    mDialogManager.dimissDialog();
                    mAudioManager.release();
                    if(mListener!=null){
                        mListener.onFinish(mTime,mAudioManager.getCurrentFilePath());
                    }
                } else if (mCurState == STATE_WANT_TO_CANCEL) {
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
                }
                reset();
                break;
            }
            case MotionEvent.ACTION_CANCEL:{
                //// TODO: 2016-02-18 如果有系统 提示需要录音权限，就有可能触发这个事件，试试看
                reset();
                break;
            }
        }
        return super.onTouchEvent(event);
    }


    /**
     * 恢复一些标志位
     */
    private void reset() {
        isRecodeing = false;
        mTime = 0;
        mReady = false;
        changeState(STATE_NORMAL);
    }

    /**
     * 根据xy的坐标，判断是否想要取消录音
     *
     * @param x
     * @param y
     * @return
     */
    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }

    /**
     * 改变状态
     */
    private void changeState(int state) {
        if (mCurState != state) {
            mCurState = state;
            switch (state) {
                case STATE_NORMAL: {
                    setBackgroundResource(R.drawable.button_audiorecoder_normal);
                    setTextColor(getResources().getColor(R.color.colorBlackText));
                    setText(R.string.recoder_normal);
                    break;
                }
                case STATE_RECODERING: {
                    setBackgroundResource(R.drawable.button_audiorecoder_pressed);
                    setTextColor(getResources().getColor(R.color.colorSecondText));
                    setText(R.string.recoder_recodering);
                    if (isRecodeing) {
                        mDialogManager.recording();
                    }
                    break;
                }
                case STATE_WANT_TO_CANCEL: {
                    setBackgroundResource(R.drawable.button_audiorecoder_pressed);
                    setTextColor(getResources().getColor(R.color.colorSecondText));
                    setText(R.string.recoder_want_cancel);
                    mDialogManager.wantToCancel();
                    break;
                }
            }
        }
    }


}
