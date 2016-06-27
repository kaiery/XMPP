package com.softfun_xmpp.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.CipherUtils;
import com.softfun_xmpp.utils.StringUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

import org.jivesoftware.smackx.iqregister.AccountManager;

public class EditPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private TextView mTv;
    private Button mBtn;
    private TextView mTvPasswordOld;
    private TextView mTvPasswordNew;
    private TextView mTvPasswordAgain;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTv = (TextView) findViewById(R.id.tv);
        mBtn = (Button) findViewById(R.id.btn);
        mTvPasswordOld = (TextView) findViewById(R.id.et_password_old);
        mTvPasswordNew = (TextView) findViewById(R.id.et_password_new);
        mTvPasswordAgain = (TextView) findViewById(R.id.et_password_again);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        assignViews();

        mTv.setText("修改密码");
        mBtn.setOnClickListener(this);

        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn) {
            if (StringUtils.isEmpty(mTvPasswordOld.getText().toString().trim())) {
                mTvPasswordOld.setError("请输入旧密码");
                return;
            } else if (StringUtils.isEmpty(mTvPasswordNew.getText().toString().trim())) {
                mTvPasswordNew.setError("请输入新密码");
                return;
            } else if(StringUtils.isEmpty( mTvPasswordAgain.getText().toString().trim()   )){
                mTvPasswordAgain.setError("请输入确认的密码");
                return;
            }else if(!mTvPasswordAgain.getText().toString().trim().equals(mTvPasswordNew.getText().toString().trim())){
                mTvPasswordAgain.setError("密码不一致");
                return;
            }
            ThreadUtils.runInThread(new Runnable() {
                @Override
                public void run() {
                    String username = IMService.mCurAccount.substring(0,IMService.mCurAccount.lastIndexOf("@"));
                    String password_old = mTvPasswordOld.getText().toString().trim();
                    String password_new = mTvPasswordNew.getText().toString().trim();
                    int code = HttpUtil.okhttpPost_updatePassword(username, CipherUtils.md5(password_old),CipherUtils.md5(password_new) );
                    if(code==1){
                        try {
                            AccountManager accountManager = AccountManager.getInstance(IMService.conn);//new AccountManager(IMService.conn);
                            accountManager.changePassword(CipherUtils.md5(password_new));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ToastUtils.showToastSafe("密码修改成功");
                    }else{
                        ToastUtils.showToastSafe("密码修改失败");
                    }
                    finish();
                }
            });
        }
    }
}
