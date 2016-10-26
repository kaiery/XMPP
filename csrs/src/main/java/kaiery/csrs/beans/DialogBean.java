package kaiery.csrs.beans;

import java.io.Serializable;

/**
 * Created by 范张 on 2016-03-12.
 */
public class DialogBean implements Serializable {
    private String title;
    private String content;
    private ButtonType buttonType;
    private DialogType dialogType;


    public enum ButtonType {
        onebutton, twobutton,none
    }

    public enum DialogType{
        tologin,tip,toTick
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ButtonType getButtonType() {
        return buttonType;
    }

    public void setButtonType(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public DialogType getDialogType() {
        return dialogType;
    }

    public void setDialogType(DialogType dialogType) {
        this.dialogType = dialogType;
    }
}
