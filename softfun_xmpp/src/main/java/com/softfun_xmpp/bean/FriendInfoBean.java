package com.softfun_xmpp.bean;

import java.io.Serializable;

/**
 * Created by 范张 on 2016-01-28.
 */
public class FriendInfoBean implements Serializable ,Comparable<FriendInfoBean>{
    private String op           ;//-：删除；+：新增
    private String userid;
    private String showname;
    private String phonetic;
    private String vip;
    private String usertype;
    private String userface;
    private String hash    ;
    private String pinyin;
    private boolean online = false;
    private String orgname = "";
    private String orgid = "";

    @Override
    public String toString() {
        return "FriendInfoBean [userid=" + userid + ", showname=" + showname + ", phonetic=" + phonetic + ", vip=" + vip + ", usertype=" + usertype + ", userface=" + userface + "]";
    }

    public FriendInfoBean(){

    }

    /**
     * 数据同步时使用
     * @param op
     * @param userid
     * @param showname
     * @param phonetic
     * @param vip
     * @param usertype
     * @param userface
     */
    public FriendInfoBean(String op, String userid, String showname, String phonetic, String vip, String usertype, String userface) {
        this.op = op;
        this.userid = userid;
        this.showname = showname;
        this.phonetic = phonetic;
        this.vip = vip;
        this.usertype = usertype;
        this.userface = userface;
    }


    /**
     * 业务逻辑时使用
     * @param op
     * @param userid
     * @param showname
     * @param phonetic
     * @param userface
     * @param pinyin *****
     */
    public FriendInfoBean(String op, String userid, String showname, String phonetic, String vip, String usertype, String userface, String pinyin) {
        this.op = op;
        this.userid = userid;
        this.showname = showname;
        this.phonetic = phonetic;
        this.vip = vip;
        this.usertype = usertype;
        this.userface = userface;
        this.pinyin = pinyin;
    }


    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    public String getUserid() {
        return userid;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }
    public String getShowname() {
        return showname;
    }
    public void setShowname(String showname) {
        this.showname = showname;
    }
    public String getPhonetic() {
        return phonetic;
    }
    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }
    public String getUserface() {
        return userface;
    }
    public void setUserface(String userface) {
        this.userface = userface;
    }
    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    public String getPinyin() {
        return pinyin;
    }
    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
    public String getVip() {
        return vip;
    }
    public void setVip(String vip) {
        this.vip = vip;
    }
    public String getUsertype() {
        return usertype;
    }
    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
    public boolean isOnline() {
        return online;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }
    public String getOrgname() {
        return orgname;
    }
    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }
    public String getOrgid() {
        return orgid;
    }
    public void setOrgid(String orgid) {
        this.orgid = orgid;
    }

    @Override
    public int compareTo(FriendInfoBean another) {
        return this.getPhonetic().compareTo(another.getPhonetic());
    }
}
