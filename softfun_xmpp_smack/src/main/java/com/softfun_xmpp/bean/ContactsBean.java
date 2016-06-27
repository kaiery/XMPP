package com.softfun_xmpp.bean;

/**
 * Created by 范张 on 2016-06-14.
 */
public class ContactsBean {

    private String account     ;// = "account";//帐号
    private String nickname    ;// = "nickname";//昵称
    private String avatarurl   ;// = "avatarurl";//头像
    private String pinyin      ;// = "pinyin";//拼音
    private String status      ;// = "status";//状态
    private String owner       ;// = "owner";//所有者
    private String vip         ;// = "vip";//级别
    private String background  ;// = "background";//背景图
    private String score       ;// = "score";//积分
    private String roletype    ;// = "roletype";//角色类型

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getRoletype() {
        return roletype;
    }

    public void setRoletype(String roletype) {
        this.roletype = roletype;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }
}
