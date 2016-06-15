package com.softfun.utils;

import android.content.Context;
import android.widget.Toast;

import com.softfun.application.GlobalContext;


/**
 * Created by 范张 on 2016-04-15.
 */
public class ToastUtils {


    private static Context context = GlobalContext.getInstance();

    /**
     * 可以在子线程弹出toast
     * @param text
     */
    public static void showToastSafe(final String text){
        ThreadUtils.runInUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showToastSafe_Long(final String text){
        ThreadUtils.runInUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,text,Toast.LENGTH_LONG).show();
            }
        });
    }
}
