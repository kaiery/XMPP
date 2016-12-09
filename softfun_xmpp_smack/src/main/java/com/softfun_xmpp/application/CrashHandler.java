package com.softfun_xmpp.application;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.softfun_xmpp.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * ------------------------------
 * Created by 范张 on 2016-10-28.
 * ------------------------------
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    /**
     * 是否开启日志输出,在Debug状态下开启,
     * 在Release状态下关闭以提示程序性能
     */
//    public static final boolean DEBUG = true;
    /**
     * 系统默认的UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /**
     * CrashHandler实例
     */
    private static CrashHandler INSTANCE;
    /**
     * 程序的Context对象
     */
    private Context mContext;
    /**
     *用来存储设备信息和异常信息
     */
    private Map<String, String> infos = new HashMap<String, String>();

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象,
     * 获取系统默认的UncaughtException处理器,
     * 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx
     */
    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {  //如果自己处理了异常，则不会弹出错误对话框，则需要手动退出app
            handleException(ex);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * 自定义错误处理,收集错误信息
     * 发送错误报告等操作均在此完成.
     * 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @return true代表处理该异常，不再向上抛异常，
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理，
     * 简单来说就是true不会弹出那个错误提示框，false就会弹出
     */
    private boolean handleException(final Throwable ex) {
        final StringBuffer stackLog = new StringBuffer();
        if (ex == null) {
            return false;
        }
        final StackTraceElement[] stack = ex.getStackTrace();
        final String message = ex.getMessage()+"\r\n";


        //System.out.println("====================  ex.getMessage()  =====================:"+message);
        for (StackTraceElement aStack : stack) {
            stackLog.append(aStack.toString()+"\r\n") ;
        }
        //System.out.println("====================  ex.getMessage()  =====================:"+stackLog.toString());

        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序出错啦:" + message, Toast.LENGTH_LONG).show();
//                可以只创建一个文件，以后全部往里面append然后发送，这样就会有重复的信息，个人不推荐
                String fileName = mContext.getResources().getString(R.string.app_name) + "错误日志_" + System.currentTimeMillis() + ".txt";
                File file = new File(Environment.getExternalStorageDirectory(), fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file, true);
                    fos.write(message.getBytes());
                    fos.write(stackLog.toString().getBytes());
//                    for (StackTraceElement aStack : stack) {
//                        fos.write(aStack.toString().getBytes());
//                        stackBuffer.append(aStack.toString()+"/n");
//                    }
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    System.out.println("====================  写文件失败  =====================");
                }
                infos.put("MESSAGE",message);
                infos.put("STACK",stackLog.toString());
                //System.out.println("====================    ====================="+stackLog.toString());
                collectDeviceInfo();
                postReport();
                Looper.loop();
            }

        }.start();
        return false;
    }

    /**
     * 提交到服务器
     */
    private void postReport() {
        try {
            String pathUrl =  mContext.getResources().getString(R.string.app_server)+"crash";//"http://192.168.0.112:8080/softfun/crash";
            // Post请求的url，与get不同的是不需要带参数
            URL postUrl = new URL(pathUrl);
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();

            // 设置是否向connection输出，因为这个是post请求，参数要放在
            // http正文内，因此需要设为true
            connection.setDoOutput(true);
            // Read from the connection. Default is true.
            connection.setDoInput(true);
            // 默认是 GET方式
            connection.setRequestMethod("POST");

            // Post 请求不能使用缓存
            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(true);

            // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
            // 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
            // 进行编码
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
            // 要注意的是connection.getOutputStream会隐含的进行connect。
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            StringBuilder contentBuffer = new StringBuilder();
            contentBuffer.setLength(0);
            contentBuffer.append("a=a");
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                //System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
                contentBuffer.append("&").append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            out.writeBytes(contentBuffer.toString());
            // 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
            //String content = "message=" + URLEncoder.encode(message+"_____", "UTF-8");
            //content +="&pswd="+URLEncoder.encode("两个个大肥人", "UTF-8");;

            //out.writeBytes(content);
//            for (StackTraceElement aStack : stack) {
//                out.write(aStack.toString().getBytes());
//            }


            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            //返回值
            while ((line = reader.readLine()) != null){
                System.out.println("-----------:"+line);
            }
            reader.close();
            connection.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




    /**
     * 收集设备参数信息
     */
    public void collectDeviceInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
            infos.put("SDK", String.valueOf(Build.VERSION.SDK_INT));
            //infos.put("ID",((TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE)).getDeviceId());
        } catch (Exception e) {
            Log.e(TAG, "一个错误发生时收集包信息", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "一个错误发生时收集信息", e);
            }
        }
        //BOARD：董事
        //BOOTLOADER：基带
        //BRAND：品牌
        //SERIAL：序列号
        //HARDWARE:硬件
        //CPU_ABI：cpu类型
        //CPU_ABI2：
        //MANUFACTURER:制造商
        //MODEL：型号
        //SDK:操作系统
    }

}
