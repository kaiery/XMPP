package com.softfun.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 范张 on 2016-04-21.
 */
public class UserBean implements  Parcelable {

    private String account;
    private String nickname;
    private String username;
    private String password;
    private String phone;
    private String orgid;
    private String orgname;
    private int score;
    private int vip;
    private String background;
    private String userdesc;
    private String usertype;
    private String usertypename;

    private String userid			;
    private String status			;
    private String statusname       ;
    private String showname			;
    private String ltimes			;
    private String userregdate		;
    private String userphone		;
    private String phonetic			;
    private String idcard			;
    private String sort				;
    private String qq				;
    private String email			;
    private String lognum			;
    private String toltime			;
    private String userface			;
    private String specialty        ;
    private String positional       ;
    private String workinglife      ;
    private String address          ;
    private String company          ;






    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getPositional() {
        return positional;
    }

    public void setPositional(String positional) {
        this.positional = positional;
    }

    public String getWorkinglife() {
        return workinglife;
    }

    public void setWorkinglife(String workinglife) {
        this.workinglife = workinglife;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOrgid() {
        return orgid;
    }

    public void setOrgid(String orgid) {
        this.orgid = orgid;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getUserdesc() {
        return userdesc;
    }

    public void setUserdesc(String userdesc) {
        this.userdesc = userdesc;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getUsertypename() {
        return usertypename;
    }

    public void setUsertypename(String usertypename) {
        this.usertypename = usertypename;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusname() {
        return statusname;
    }

    public void setStatusname(String statusname) {
        this.statusname = statusname;
    }

    public String getShowname() {
        return showname;
    }

    public void setShowname(String showname) {
        this.showname = showname;
    }

    public String getLtimes() {
        return ltimes;
    }

    public void setLtimes(String ltimes) {
        this.ltimes = ltimes;
    }

    public String getUserregdate() {
        return userregdate;
    }

    public void setUserregdate(String userregdate) {
        this.userregdate = userregdate;
    }

    public String getUserphone() {
        return userphone;
    }

    public void setUserphone(String userphone) {
        this.userphone = userphone;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLognum() {
        return lognum;
    }

    public void setLognum(String lognum) {
        this.lognum = lognum;
    }

    public String getToltime() {
        return toltime;
    }

    public void setToltime(String toltime) {
        this.toltime = toltime;
    }

    public String getUserface() {
        return userface;
    }

    public void setUserface(String userface) {
        this.userface = userface;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.account);
        dest.writeString(this.nickname);
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeString(this.phone);
        dest.writeString(this.orgid);
        dest.writeString(this.orgname);
        dest.writeInt(this.score);
        dest.writeInt(this.vip);
        dest.writeString(this.background);
        dest.writeString(this.userdesc);
        dest.writeString(this.usertype);
        dest.writeString(this.usertypename);
        dest.writeString(this.userid);
        dest.writeString(this.status);
        dest.writeString(this.statusname);
        dest.writeString(this.showname);
        dest.writeString(this.ltimes);
        dest.writeString(this.userregdate);
        dest.writeString(this.userphone);
        dest.writeString(this.phonetic);
        dest.writeString(this.idcard);
        dest.writeString(this.sort);
        dest.writeString(this.qq);
        dest.writeString(this.email);
        dest.writeString(this.lognum);
        dest.writeString(this.toltime);
        dest.writeString(this.userface);
        dest.writeString(this.specialty);
        dest.writeString(this.positional);
        dest.writeString(this.workinglife);
        dest.writeString(this.address);
        dest.writeString(this.company);
    }

    public UserBean() {
    }

    protected UserBean(Parcel in) {
        this.account = in.readString();
        this.nickname = in.readString();
        this.username = in.readString();
        this.password = in.readString();
        this.phone = in.readString();
        this.orgid = in.readString();
        this.orgname = in.readString();
        this.score = in.readInt();
        this.vip = in.readInt();
        this.background = in.readString();
        this.userdesc = in.readString();
        this.usertype = in.readString();
        this.usertypename = in.readString();
        this.userid = in.readString();
        this.status = in.readString();
        this.statusname = in.readString();
        this.showname = in.readString();
        this.ltimes = in.readString();
        this.userregdate = in.readString();
        this.userphone = in.readString();
        this.phonetic = in.readString();
        this.idcard = in.readString();
        this.sort = in.readString();
        this.qq = in.readString();
        this.email = in.readString();
        this.lognum = in.readString();
        this.toltime = in.readString();
        this.userface = in.readString();
        this.specialty = in.readString();
        this.positional = in.readString();
        this.workinglife = in.readString();
        this.address = in.readString();
        this.company = in.readString();
    }

    public static final Parcelable.Creator<UserBean> CREATOR = new Parcelable.Creator<UserBean>() {
        @Override
        public UserBean createFromParcel(Parcel source) {
            return new UserBean(source);
        }

        @Override
        public UserBean[] newArray(int size) {
            return new UserBean[size];
        }
    };
}
