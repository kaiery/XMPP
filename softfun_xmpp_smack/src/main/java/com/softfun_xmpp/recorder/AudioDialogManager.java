package com.softfun_xmpp.recorder;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.softfun_xmpp.R;


/**
 * Created by 范张 on 2016-02-16.
 */
public class AudioDialogManager {
    private Dialog mDialog;
    private ImageView iv_audiodialog_icon;
    private ImageView iv_audiodialog_voice;
    private TextView tv_audiodialog_label;
    private Context mContext;


    public AudioDialogManager(Context mContext) {
        this.mContext = mContext;
    }

    public void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.AudioDialogTheme);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.audiodialog, null);
        mDialog.setContentView(view);

        tv_audiodialog_label = (TextView) mDialog.findViewById(R.id.tv_audiodialog_label);
        iv_audiodialog_icon = (ImageView) mDialog.findViewById(R.id.iv_audiodialog_icon);
        iv_audiodialog_voice = (ImageView) mDialog.findViewById(R.id.iv_audiodialog_voice);

        mDialog.show();
        tv_audiodialog_label.setText(mContext.getResources().getString(R.string.recoder_recodering_dialog));
    }

    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            iv_audiodialog_icon.setVisibility(View.VISIBLE);
            iv_audiodialog_voice.setVisibility(View.VISIBLE);
            tv_audiodialog_label.setVisibility(View.VISIBLE);

            iv_audiodialog_icon.setImageResource(R.drawable.recorder);
            tv_audiodialog_label.setText(mContext.getResources().getString(R.string.recoder_recodering_dialog));
        }
    }

    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            iv_audiodialog_icon.setVisibility(View.VISIBLE);
            iv_audiodialog_voice.setVisibility(View.GONE);
            tv_audiodialog_label.setVisibility(View.VISIBLE);

            iv_audiodialog_icon.setImageResource(R.drawable.cancel);
            tv_audiodialog_label.setText(mContext.getResources().getString(R.string.recoder_want_cancel));
        }
    }

    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            iv_audiodialog_icon.setVisibility(View.VISIBLE);
            iv_audiodialog_voice.setVisibility(View.GONE);
            tv_audiodialog_label.setVisibility(View.VISIBLE);

            iv_audiodialog_icon.setImageResource(R.drawable.voice_to_short);
            tv_audiodialog_label.setText(mContext.getResources().getString(R.string.recoder_too_short));
        }
    }

    public void dimissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()) {
            //通过方法名找到资源ID
            int resID = mContext.getResources().getIdentifier("v"+level,"drawable",mContext.getPackageName());
            iv_audiodialog_voice.setImageResource(resID);
        }
    }

}
