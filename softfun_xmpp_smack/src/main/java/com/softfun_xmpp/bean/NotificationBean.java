package com.softfun_xmpp.bean;

/**
 * Created by 范张 on 2016-06-29.
 */
public class NotificationBean {
    private String from;
    private String title;
    private String msg;
    private String chattype;
    private String nickname;
    private String avatarurl;
    private String group_jid;

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    public String getChattype() {
        return chattype;
    }

    public void setChattype(String chattype) {
        this.chattype = chattype;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getGroup_jid() {
        return group_jid;
    }

    public void setGroup_jid(String group_jid) {
        this.group_jid = group_jid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
