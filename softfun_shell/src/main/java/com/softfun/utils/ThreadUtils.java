package com.softfun.utils;

import android.os.Handler;


public class ThreadUtils {
    /**
     * 主线程里的handler
     */
    public static Handler mHandler = new Handler();


    /**
     * 子线程执行task
     */
    public static void runInThread(Runnable task){
        new Thread(task).start();
    }

    /**
     * Ui线程执行task
     */
    public static void runInUiThread(Runnable task){
        mHandler.post(task);
    }
}
