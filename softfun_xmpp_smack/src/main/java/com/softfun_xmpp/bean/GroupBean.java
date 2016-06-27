package com.softfun_xmpp.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 范张 on 2016-05-18.
 */
public class GroupBean implements Parcelable{
    private String child;
    private String childid;
    private String groupface;
    private String grouptype;
    private String grouptypename;
    private String lvl;
    private String groupnum;
    private String gi;
    private String giname;
    private String gcity;
    private String gcityname;
    private String mynickname;
    private String announce;
    private String master;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }

    public String getChildid() {
        return childid;
    }

    public void setChildid(String childid) {
        this.childid = childid;
    }

    public String getGroupface() {
        return groupface;
    }

    public void setGroupface(String groupface) {
        this.groupface = groupface;
    }

    public String getGrouptype() {
        return grouptype;
    }

    public void setGrouptype(String grouptype) {
        this.grouptype = grouptype;
    }

    public String getGrouptypename() {
        return grouptypename;
    }

    public void setGrouptypename(String grouptypename) {
        this.grouptypename = grouptypename;
    }

    public String getLvl() {
        return lvl;
    }

    public void setLvl(String lvl) {
        this.lvl = lvl;
    }

    public String getGroupnum() {
        return groupnum;
    }

    public void setGroupnum(String groupnum) {
        this.groupnum = groupnum;
    }

    public String getGi() {
        return gi;
    }

    public void setGi(String gi) {
        this.gi = gi;
    }

    public String getGiname() {
        return giname;
    }

    public void setGiname(String giname) {
        this.giname = giname;
    }

    public String getGcity() {
        return gcity;
    }

    public void setGcity(String gcity) {
        this.gcity = gcity;
    }

    public String getGcityname() {
        return gcityname;
    }

    public void setGcityname(String gcityname) {
        this.gcityname = gcityname;
    }

    public String getMynickname() {
        return mynickname;
    }

    public void setMynickname(String mynickname) {
        this.mynickname = mynickname;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }


    public GroupBean() {

    }

    public GroupBean(Parcel source) {
        this.child = source.readString();
        this.childid = source.readString();
        this.groupface = source.readString();
        this.grouptype = source.readString();
        this.grouptypename = source.readString();
        this.lvl = source.readString();
        this.groupnum = source.readString();
        this.gi = source.readString();
        this.giname = source.readString();
        this.gcity = source.readString();
        this.gcityname = source.readString();
        this.mynickname = source.readString();
        this.announce = source.readString();
        this.master = source.readString();
    }

    // 必须要创建一个名叫CREATOR的常量。
    public static final Parcelable.Creator<GroupBean> CREATOR = new Parcelable.Creator<GroupBean>(){

        @Override
        public GroupBean createFromParcel(Parcel source) {
            return new GroupBean(source);
        }

        @Override
        public GroupBean[] newArray(int size) {
            return new GroupBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(child        );
        dest.writeString(childid      );
        dest.writeString(groupface    );
        dest.writeString(grouptype    );
        dest.writeString(grouptypename);
        dest.writeString(lvl          );
        dest.writeString(groupnum     );
        dest.writeString(gi           );
        dest.writeString(giname       );
        dest.writeString(gcity        );
        dest.writeString(gcityname    );
        dest.writeString(mynickname   );
        dest.writeString(announce     );
        dest.writeString(master       );
    }
}
