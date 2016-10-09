package com.softfun.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.softfun.R;
import com.softfun.bean.UserBean;
import com.softfun.constant.Const;
import com.softfun.network.HttpUtil;
import com.softfun.utils.CipherUtils;
import com.softfun.utils.SpUtils;

public class LoginActivity extends Activity implements View.OnClickListener {

//    private ImageView mIvPic;
    private EditText mEtUsername;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private ProgressBar mPb;

    private void assignViews() {
//        mIvPic = (ImageView) findViewById(R.id.iv_pic);
        mEtUsername = (EditText) findViewById(R.id.et_username);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mPb = (ProgressBar) findViewById(R.id.pb);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 使屏幕不显示标题栏(必须要在setContentView方法执行前执行)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏，使内容全屏显示(必须要在setContentView方法执行前执行)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_login);

        assignViews();

        init();
        initListener();
    }

    private void init() {
        //得到窗口管理器
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //得到显示指标
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);


//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mIvPic.getLayoutParams();
//        layoutParams.height = dm.heightPixels/2;
//        mIvPic.setLayoutParams(layoutParams);
        mEtUsername.setText(SpUtils.get(Const.USERNAME,"")+"");
        mEtPassword.setText(SpUtils.get(Const.PASSWORD,"")+"");
    }

    private void initListener() {
        mBtnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.btn_login:
                String username = mEtUsername.getText().toString();
                String password = mEtPassword.getText().toString();
                if(TextUtils.isEmpty(username)){
                    mEtUsername.setError("用户名为空");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mEtPassword.setError("密码为空");
                    return;
                }

                new LoginTask().execute(username,password);

                break;
        }
    }


    private class LoginTask extends AsyncTask<String ,Void ,UserBean>{

        private String username;
        private String password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waiting();
        }

        @Override
        protected UserBean doInBackground(String... params) {
            username = params[0];
            password = params[1];
            return HttpUtil.okhttpPost_Login(username, CipherUtils.md5(password));
        }

        @Override
        protected void onPostExecute(UserBean bean) {
            super.onPostExecute(bean);
            if(bean!=null){
                idle();
                SpUtils.put(Const.USERNAME, username);
                SpUtils.put(Const.PASSWORD, password);
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("USERBEAN", bean);
                intent.putExtras(mBundle);
                startActivity(intent);
                finish();
            }
        }
    }

    private void waiting() {
        mPb.setVisibility(View.VISIBLE);
        mEtUsername.setEnabled(false);
        mEtPassword.setEnabled(false);
        mBtnLogin.setEnabled(false);
    }

    private void idle(){
        mPb.setVisibility(View.GONE);
        mEtUsername.setEnabled(true);
        mEtPassword.setEnabled(true);
        mBtnLogin.setEnabled(true);
    }
}
