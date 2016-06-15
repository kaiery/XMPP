package com.softfun_xmpp.bbs.bean;

/**
 * Created by 范张 on 2016-03-31.
 */
public class CodeBean {
    private String key;
    private String value;

    public CodeBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
