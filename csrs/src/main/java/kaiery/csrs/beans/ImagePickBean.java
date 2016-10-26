package kaiery.csrs.beans;

/**
 * Created by 范张 on 2016-04-01.
 */
public class ImagePickBean {

    private String url;
    private String name;
    private boolean showcheck;
    private boolean check;
    private boolean isupload;
    private int progress;


    public ImagePickBean(String url, String name, boolean showcheck, boolean check) {
        this.url = url;
        this.name = name;
        this.showcheck = showcheck;
        this.check = check;
    }

    public ImagePickBean() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShowcheck() {
        return showcheck;
    }

    public void setShowcheck(boolean showcheck) {
        this.showcheck = showcheck;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean isupload() {
        return isupload;
    }

    public void setIsupload(boolean isupload) {
        this.isupload = isupload;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
