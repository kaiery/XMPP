package kaiery.csrs.beans;

import java.io.Serializable;

/**
 * ------------------------------
 * Created by 范张 on 2016-10-24.
 * ------------------------------
 */

public class ModuleBean implements Serializable {
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_BUTTON = 2;

    private int type;
    private String header;

    private String id;
    private String title;
    private String icon;
    private String action;
    private String[] arg;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String[] getArg() {
        return arg;
    }

    public void setArg(String[] arg) {
        this.arg = arg;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
