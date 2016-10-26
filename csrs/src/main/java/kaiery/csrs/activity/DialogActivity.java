package kaiery.csrs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kaiery.csrs.R;
import kaiery.csrs.application.SystemSingleton;
import kaiery.csrs.beans.DialogBean;


public class DialogActivity extends Activity implements View.OnClickListener {

    private TextView mTvDialogTitle;
    private TextView mTvDialogContent;
    private TextView mTvDialogOk;
    private TextView mTvDialogCancel;
    private LinearLayout mLl_dlg_tip;
    private RelativeLayout mRl_dlg_dlg;
    private DialogBean dialogBean;

    private void assignViews() {
        mTvDialogTitle = (TextView) findViewById(R.id.tv_dialog_title);
        mTvDialogContent = (TextView) findViewById(R.id.tv_dialog_content);
        mTvDialogOk = (TextView) findViewById(R.id.tv_dialog_ok);
        mTvDialogCancel = (TextView) findViewById(R.id.tv_dialog_cancel);
        mLl_dlg_tip = (LinearLayout) findViewById(R.id.ll_dlg_tip);
        mRl_dlg_dlg = (RelativeLayout) findViewById(R.id.rl_dlg_dlg);
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
        }else if(dialogBean.getButtonType()== DialogBean.ButtonType.twobutton){
            mTvDialogCancel.setVisibility(View.VISIBLE);
        }else if(dialogBean.getButtonType()== DialogBean.ButtonType.none){
            mTvDialogOk.setVisibility(View.GONE);
            mTvDialogCancel.setVisibility(View.GONE);
        }

        //如果不是提示类型，有按钮
        if(!dialogBean.getDialogType().equals(DialogBean.DialogType.tip)){
            mLl_dlg_tip.setVisibility(View.GONE);
            mRl_dlg_dlg.setVisibility(View.VISIBLE);
            //有按钮
            mTvDialogOk.setText("确定");
            mTvDialogCancel.setText("取消");

            mTvDialogOk.setOnClickListener(this);
            mTvDialogCancel.setOnClickListener(this);
        }else if(dialogBean.getDialogType().equals(DialogBean.DialogType.tip)){
            mLl_dlg_tip.setVisibility(View.VISIBLE);
            mRl_dlg_dlg.setVisibility(View.GONE);
            //延迟
//            ThreadUtils.runInThread(new Runnable() {
//                @Override
//                public void run() {
//                    SystemClock.sleep(3000);
//                    ThreadUtils.runInUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            //发送广播，通知视频正在启动中
//                            Intent intent = new Intent();
//                            intent.setAction(Const.VIDEO_STARTING_BROADCAST_ACTION);
//                            sendBroadcast(intent);
//                            finish();
//                        }
//                    });
//                }
//            });
        }
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
        SystemSingleton.getInstance().getMainActivity().finish();
        //结束本任务栈，一定要执行下面2句，不然原来的程序仍在进程中。
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);//杀死了进程
    }

    /**
     * 询问之后，再关闭socket，并跳转到登录
     */
    private void toLoginActivity() {
        //Intent imservice = new Intent(this,IMService.class);
        //stopService(imservice);
        SystemSingleton.getInstance().getMainActivity().finish();
        //结束本任务栈，一定要执行下面2句，不然原来的程序仍在进程中。
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);//杀死了进程
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {

    }


}
