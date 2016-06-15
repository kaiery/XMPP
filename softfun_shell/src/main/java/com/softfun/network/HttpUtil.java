package com.softfun.network;

import android.content.Context;

import com.google.gson.Gson;
import com.softfun.R;
import com.softfun.application.GlobalContext;
import com.softfun.bean.UpdateBean;
import com.softfun.utils.ToastUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    /**
     * 静态初始化组件
     */
    public static final OkHttpClient mOkHttpClient =  new OkHttpClient.Builder()
            //设置超时，不设置可能会报异常
            .connectTimeout(5000, TimeUnit.MINUTES)
            .readTimeout(5000, TimeUnit.MINUTES)
            .writeTimeout(5000, TimeUnit.MINUTES)
            .build();

    private static final Context context = GlobalContext.getInstance();
    private static final Gson gson = new Gson();




    /**
     * 获取升级信息
     * @return
     */
    public static UpdateBean okhttpGet_UpdateInfo(String typedef){
        String url = context.getResources().getString(R.string.app_server)+"queryUpdateInfo";
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("typedef", typedef)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                response.body().close();
                throw new IOException("Unexpected code " + response);
            }
            String jsonstr = response.body().string();
            //开始解析json数据，转为bean对象
            UpdateBean updateBean;
            updateBean = gson.fromJson(jsonstr, UpdateBean.class);
            return updateBean;
        } catch (Exception e) {
            ToastUtils.showToastSafe("网络访问异常");
            e.printStackTrace();
        } finally {

        }
        return null;
    }




}
