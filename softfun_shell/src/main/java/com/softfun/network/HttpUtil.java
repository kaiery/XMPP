package com.softfun.network;

import android.content.Context;

import com.google.gson.Gson;
import com.softfun.R;
import com.softfun.application.GlobalContext;
import com.softfun.bean.ResultBean;
import com.softfun.bean.UpdateBean;
import com.softfun.bean.UserBean;
import com.softfun.utils.NetWorkUtils;
import com.softfun.utils.ToastUtils;

import org.json.JSONObject;

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


    /**
     * 获取平台内app组件模块
     * @param suffix
     * @return
     */
    public static UpdateBean okhttpPost_queryAppModule(String suffix) {
        String url = context.getResources().getString(R.string.app_server)+"queryAppModule";
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("typedef", suffix)
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
        } finally {

        }
        return null;
    }



    /**
     * 用户登录
     * @param username
     * @param password
     */
    public static UserBean okhttpPost_Login(String username, String password) {
        ResultBean rb = new ResultBean();
        UserBean userBean = new UserBean();
        try {
            String url = context.getResources().getString(R.string.app_server)+"shell_login";
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password",password)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                response.body().close();
            }
            //开始解析json数据
            JSONObject jsonObject = new JSONObject(response.body().string());
            if(jsonObject.getBoolean("success")){
                JSONObject map = jsonObject.getJSONObject("datamap");
                JSONObject jsonUserBean = map.getJSONObject("userbean");
                userBean.setUserid(jsonUserBean.getString("userid"));
                userBean.setUsername(jsonUserBean.getString("username"));
                userBean.setPassword(jsonUserBean.getString("password"));
                userBean.setStatus(jsonUserBean.getString("status"));
                userBean.setStatusname(jsonUserBean.getString("statusname"));
                userBean.setShowname(jsonUserBean.getString("showname"));
                userBean.setLtimes(jsonUserBean.getString("ltimes"));
                userBean.setOrgid(jsonUserBean.getString("orgid"));
                userBean.setOrgname(jsonUserBean.getString("orgname"));
                userBean.setUserregdate(jsonUserBean.getString("userregdate"));
                userBean.setUserphone(jsonUserBean.getString("userphone"));
                userBean.setUsertype(jsonUserBean.getString("usertype"));
                userBean.setUsertypename(jsonUserBean.getString("usertypename"));
                userBean.setPhonetic(jsonUserBean.getString("phonetic"));
                userBean.setIdcard(jsonUserBean.getString("idcard"));
                userBean.setSort(jsonUserBean.getString("sort"));
                userBean.setQq(jsonUserBean.getString("qq"));
                userBean.setEmail(jsonUserBean.getString("email"));
                userBean.setLognum(jsonUserBean.getString("lognum"));
                userBean.setToltime(jsonUserBean.getString("toltime"));
                userBean.setUserface(jsonUserBean.getString("userface"));
                userBean.setUserdesc(jsonUserBean.getString("userdesc"));
                userBean.setBackground(jsonUserBean.getString("background"));
                userBean.setScore(jsonUserBean.getInt("score"));
                userBean.setVip(jsonUserBean.getInt("vip"));
                userBean.setAddress(jsonUserBean.getString("address"));
                userBean.setWorkinglife(jsonUserBean.getString("workinglife"));
                userBean.setPositional(jsonUserBean.getString("positional"));
                userBean.setSpecialty(jsonUserBean.getString("specialty"));
                userBean.setCompany(jsonUserBean.getString("company"));
            }
        }catch (Exception e){
            e.printStackTrace();
            //网络访问异常
            ToastUtils.showToastSafe("网络访问异常:okhttpPost_Login");
        }
        return userBean;
    }


    /**
     * 写用户登录日志
     * @param userName
     */
    public static void okhttpPost_writeUserLoginLog(String userName) {
        try {
            String url = context.getResources().getString(R.string.app_server)+"writeUserLoginLog";
            String localIpAddress = NetWorkUtils.getLocalIpAddress(context);
            RequestBody formBody = new FormBody.Builder()
                    .add("username", userName)
                    .add("ip", localIpAddress)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }
        }catch (Exception e){
            System.out.println("网络访问异常:okhttpPost_writeUserLoginLog");
            e.printStackTrace();
        }
    }

}
