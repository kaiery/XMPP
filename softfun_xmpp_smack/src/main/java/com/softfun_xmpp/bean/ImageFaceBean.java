package com.softfun_xmpp.bean;

/**
 * Created by 范张 on 2016-02-17.
 */
public class ImageFaceBean {
    private String url;
    private String pid;

    public ImageFaceBean(String url, String pid) {
        this.url = url;
        this.pid = pid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
