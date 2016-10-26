package kaiery.csrs.beans;

/**
 * Created by 范张 on 2016-04-01.
 */
public class FeatureBtnBean {
    private String btnname;
    private int btnicon;
    private String btcode;

    public String getBtnname() {
        return btnname;
    }

    public void setBtnname(String btnname) {
        this.btnname = btnname;
    }

    public int getBtnicon() {
        return btnicon;
    }

    public void setBtnicon(int btnicon) {
        this.btnicon = btnicon;
    }

    public String getBtcode() {
        return btcode;
    }

    public void setBtcode(String btcode) {
        this.btcode = btcode;
    }

    public FeatureBtnBean(String btnname, int btnicon, String btcode) {
        this.btnname = btnname;
        this.btnicon = btnicon;
        this.btcode = btcode;
    }

    public FeatureBtnBean() {

    }
}
