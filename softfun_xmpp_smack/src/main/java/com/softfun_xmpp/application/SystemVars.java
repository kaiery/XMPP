package com.softfun_xmpp.application;

import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

/**
 * Created by 范张 on 2016-04-20.
 */
public class SystemVars {

    /**
     * * 判断手机里是否已经还有com.fanzhang.softfun.MainActivity，
     * 如果有，那么点击通知之后，进入聊天页面，返回之后，进入main
     * 如果么有，那么点击通知之后，进入聊天页面，返回之后，重新开启splash页面，再进入main
     * notificationUtil中需要进行判断
     */
    private String main;
    /**
     * 当前的是否已经注册私聊广播接收者
     * SoftFunSocket中需要进行判断，是否需要进行通知提醒
     */
    private boolean receive_private_chat = false;
    /**
     * 当前的是否跟某一个用户进行私聊
     * SoftFunSocket中需要进行判断，是否需要进行通知提醒
     */
    private String private_chat_userid = null;
    /**
     * 保存主界面，用于：关闭socket之后 踢人或 重新登录，关闭主界面执行 finish操作
     */
    private AppCompatActivity mainActivity;
    /**
     * 系统当前正在进行视频聊天，如果正在视频聊天，其他人不能再次 发送给我 或者 我发送给别人 视频聊天
     */
    private boolean videoing = false;
    private Gson gson = null;




    public String getMain() {
        return main;
    }
    public void setMain(String main) {
        this.main = main;
    }
    public boolean isReceive_private_chat() {
        return receive_private_chat;
    }
    public void setReceive_private_chat(boolean receive_private_chat) {
        this.receive_private_chat = receive_private_chat;
    }
    public String getPrivate_chat_userid() {
        return private_chat_userid;
    }
    public void setPrivate_chat_userid(String private_chat_userid) {
        this.private_chat_userid = private_chat_userid;
    }
    public AppCompatActivity getMainActivity() {
        return mainActivity;
    }
    public void setMainActivity(AppCompatActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public boolean isVideoing() {
        return videoing;
    }
    public void setVideoing(boolean videoing) {
        this.videoing = videoing;
    }
    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }












    private SystemVars() {
        gson = new Gson();
    }
    private static class SingletonHolder {
        private final static SystemVars INSTANCE = new SystemVars();
    }
    public static SystemVars getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
