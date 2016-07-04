package com.softfun_xmpp.utils;

import android.content.Context;
import android.widget.Toast;

import com.softfun_xmpp.application.GlobalContext;

/**
 * Created by 范张 on 2016-04-15.
 */
public class ToastUtils {


    private static Context context = GlobalContext.getInstance();
    private static Toast toast;
    /**
     * 可以在子线程弹出toast
     * @param text
     */
    public static void showToastSafe(final String text){
        ThreadUtils.runInUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                } else {
                    toast.cancel();//关闭吐司显示
                    toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                }
                toast.show();//重新显示吐司
            }
        });
    }

    public static void showToastSafe_Long(final String text){
        ThreadUtils.runInUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                } else {
                    toast.cancel();//关闭吐司显示
                    toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                }
                toast.show();//重新显示吐司
            }
        });
    }
}
