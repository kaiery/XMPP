package com.softfun_xmpp.bean;

/**
 * Created by 范张 on 2016-01-21.
 */
public class UpdateBean {

    /**
     * vercode : 2
     * downloadurl : http://192.168.0.112/SoftFun/softfun.apk
     * desc : 发现新版本
     */

    private int vercode;
    private String downloadurl;
    private String desc;
    private long filesize;

    public void setVercode(int vercode) {
        this.vercode = vercode;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getVercode() {
        return vercode;
    }

    public String getDownloadurl() {
        return downloadurl;
    }

    public String getDesc() {
        return desc;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }
}
