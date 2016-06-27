package com.softfun_xmpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.DialogBean;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

public class EvaluateActivity extends Activity implements View.OnClickListener {

    private TextView mTvDialogTitle;
    private TextView mTvDialogContent;
    private TextView mTvDialogOk;
    private TextView mTvDialogCancel;
    private RatingBar mRb;
    private DialogBean dialogBean;
    private EditText mEt;
    private String mTargetName;
    private float mScore;
    private String mCurAccount;
    private String mMemo;

    private void assignViews() {
        mTvDialogTitle = (TextView) findViewById(R.id.tv_dialog_title);
        mTvDialogContent = (TextView) findViewById(R.id.tv_dialog_content);
        mTvDialogOk = (TextView) findViewById(R.id.tv_dialog_ok);
        mTvDialogCancel = (TextView) findViewById(R.id.tv_dialog_cancel);
        mRb = (RatingBar) findViewById(R.id.rb);
        mEt = (EditText) findViewById(R.id.et);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 使屏幕不显示标题栏(必须要在setContentView方法执行前执行)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏，使内容全屏显示(必须要在setContentView方法执行前执行)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //触摸外围空白，不关闭本页面
        setFinishOnTouchOutside(false);
        setContentView(R.layout.activity_evaluate);
        assignViews();

        Intent intent = getIntent();
        mTargetName = intent.getStringExtra(Const.USERNAME);
        //System.out.println(mTargetName);
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



        //滑块的星形数量
        mRb.setNumStars(5);
        //设置分数
        mRb.setRating((float) 3.5);
        //设置每次更改的最小长度
        mRb.setStepSize((float) 0.5);
        mScore = (float) (20*3.5);
        //设置监听器
        mRb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mScore = 20*rating;
            }
        });
    }

    @Override
    public void onBackPressed() {}


    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.tv_dialog_ok: {
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        mCurAccount = IMService.mCurAccount.substring(0, IMService.mCurAccount.lastIndexOf("@"));
                        mMemo = mEt.getText().toString();
                        int code = HttpUtil.okhttpPost_updateEvaluate(mCurAccount,mTargetName, (long) mScore,mMemo);
                        if(code==1){
                            ToastUtils.showToastSafe("感谢您的评价");
                            finish();
                        }else{
                            ToastUtils.showToastSafe("评价失败了");
                            finish();
                        }
                    }
                });
                break;
            }
        }
    }
}
