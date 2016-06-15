package com.softfun_xmpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.SystemVars;
import com.softfun_xmpp.bean.DialogBean;
import com.softfun_xmpp.connection.IMService;

public class DialogActivity extends Activity implements View.OnClickListener {

    private TextView mTvDialogTitle;
    private TextView mTvDialogContent;
    private TextView mTvDialogOk;
    private TextView mTvDialogCancel;

    private DialogBean dialogBean;

    private void assignViews() {
        mTvDialogTitle = (TextView) findViewById(R.id.tv_dialog_title);
        mTvDialogContent = (TextView) findViewById(R.id.tv_dialog_content);
        mTvDialogOk = (TextView) findViewById(R.id.tv_dialog_ok);
        mTvDialogCancel = (TextView) findViewById(R.id.tv_dialog_cancel);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //触摸外围空白，不关闭本页面
        setFinishOnTouchOutside(false);

        setContentView(R.layout.activity_dialog);

        assignViews();

        Intent intent = getIntent();
        dialogBean = (DialogBean)intent.getSerializableExtra("dialogBean");

        mTvDialogTitle.setText(dialogBean.getTitle());
        mTvDialogContent.setText(dialogBean.getContent());
        if(dialogBean.getButtonType()== DialogBean.ButtonType.onebutton){
            mTvDialogCancel.setVisibility(View.GONE);
        }else{
            mTvDialogCancel.setVisibility(View.VISIBLE);
        }
        mTvDialogOk.setText("确定");
        mTvDialogCancel.setText("取消");


        mTvDialogOk.setOnClickListener(this);
        mTvDialogCancel.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.tv_dialog_ok: {
                if(dialogBean.getDialogType() == DialogBean.DialogType.tologin){
                    toLoginActivity();
                }
                if(dialogBean.getDialogType() == DialogBean.DialogType.toTick){
                    toTickActivity();
                }
                break;
            }
            case R.id.tv_dialog_cancel: {
                finish();
                break;
            }
        }
    }

    /**
     * 踢人，跳转到登录
     */
    private void toTickActivity() {
        //前提，服务已停止
        SystemVars.getInstance().getMainActivity().finish();
        //结束本任务栈，一定要执行下面2句，不然原来的程序仍在进程中。
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);//杀死了进程
    }

    /**
     * 询问之后，再关闭socket，并跳转到登录
     */
    private void toLoginActivity() {
        Intent imservice = new Intent(this,IMService.class);
        stopService(imservice);
        SystemVars.getInstance().getMainActivity().finish();
        //结束本任务栈，一定要执行下面2句，不然原来的程序仍在进程中。
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);//杀死了进程
    }

    @Override
    protected void onPause() {
        super.onPause();
        //overridePendingTransition(R.animator.activity_fade_out, R.animator.activity_fade_in);
    }

    @Override
    public void onBackPressed() {

    }


}
