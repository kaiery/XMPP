package kaiery.csrs.application;

import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

/**
 * ------------------------------
 * Created by 范张 on 2016-10-26.
 * ------------------------------
 */
public class SystemSingleton {

    /**
     * 保存主界面，用于：关闭socket之后 踢人或 重新登录，关闭主界面执行 finish操作
     */
    private AppCompatActivity mainActivity;
    private Gson gson = null;

    public AppCompatActivity getMainActivity() {
        return mainActivity;
    }
    public void setMainActivity(AppCompatActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public Gson getGson() {
        return gson;
    }
    public void setGson(Gson gson) {
        this.gson = gson;
    }











    private SystemSingleton() {
        gson = new Gson();
    }

    private static class SingletonHolder {
        private static final SystemSingleton INSTANCE = new SystemSingleton();
    }
    public static SystemSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }






}
