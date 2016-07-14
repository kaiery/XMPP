package com.softfun_xmpp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.bean.UpdateBean;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.DownloadAPK;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AppUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.SpUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.ToastUtils;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {


    private Toolbar mToolbar;
    private RelativeLayout mItem1;
    private RelativeLayout mItem2;
    private RelativeLayout mItem3;
    private RelativeLayout mItem4;
    private RelativeLayout mItem5;
    private TextView mTvCache;
    private TextView mTvVer;
    private Intent intent_web;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mItem1 = (RelativeLayout) findViewById(R.id.item1);
        mItem2 = (RelativeLayout) findViewById(R.id.item2);
        mItem3 = (RelativeLayout) findViewById(R.id.item3);
        mItem4 = (RelativeLayout) findViewById(R.id.item4);
        mItem5 = (RelativeLayout) findViewById(R.id.item5);
        mTvCache = (TextView) findViewById(R.id.tv_cache);
        mTvVer = (TextView) findViewById(R.id.tv_ver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        assignViews();

        mToolbar.setTitle("系统设置");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
        intent_web = new Intent(this,WebFrameActivity.class);
        mItem1.setOnClickListener(this);
        mItem2.setOnClickListener(this);
        mItem3.setOnClickListener(this);
        mItem4.setOnClickListener(this);
        mItem5.setOnClickListener(this);


        initData();
    }

    private void initData() {
        //当前缓存
        mTvCache.setText(ImageLoaderUtils.getCacheInfo());
        //当前版本
        mTvVer.setText("当前版本:"+AppUtils.getVerName(this));
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
        switch( v.getId() )
        {
            case R.id.item1:
                intent_web.putExtra(Const.WEB_TITLE,"使用帮助");
                intent_web.putExtra(Const.WEB_URL,getResources().getString(R.string.app_server)+getResources().getString(R.string.app_help_weburl));
                startActivity(intent_web);
                break;
            case R.id.item2:
                Intent intent = new Intent(this,FeedBackActivity.class);
                startActivity(intent);
                break;
            case R.id.item3:
                clearCache();
                break;
            case R.id.item4:
                //检查更新
                checkVersion();
                break;
            case R.id.item5:
                Intent intent1 = new Intent(this,AboutUsActivity.class);
                startActivity(intent1);
                break;
        }
    }

    private void clearCache() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("您需要清除缓存吗？");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ImageLoaderUtils.clearCache();
                mTvCache.setText(ImageLoaderUtils.getCacheInfo());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    /**
     * 检测版本
     */
    private void checkVersion() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                String app_package_flag = getResources().getString(R.string.app_package_flag);
                final UpdateBean updateBean = HttpUtil.okhttpGet_UpdateInfo(app_package_flag);
                if(updateBean!=null){
                    //获取应用程序的版本号
                    int verCode = AppUtils.getVerCode(SettingActivity.this);
                    if(updateBean.getVercode()>verCode){
                        //有新版本
                        SpUtils.put(Const.UPDATE,0);
                        //弹出对话框
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                                builder.setTitle("提示");
                                builder.setMessage(updateBean.getDesc());
                                builder.setPositiveButton("立即升级！", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //下载升级文件
                                        new DownloadAPK(updateBean.getFilesize()).execute(updateBean.getDownloadurl());
                                    }
                                });
                                builder.setNegativeButton("下次再说。", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //取消
                                    }
                                });
                                builder.show();
                            }
                        });
                    }else{
                        //已经是最新版本
                        SpUtils.put(Const.UPDATE,1);
                        ToastUtils.showToastSafe("已经是最新版本了.");
                    }
                    SpUtils.put(Const.UPDATEVERCODE,updateBean.getVercode());
                    SpUtils.put(Const.UPDATEDESC, updateBean.getDesc());
                    SpUtils.put(Const.UPDATEDOWNLOADURL,updateBean.getDownloadurl());
                    SpUtils.put(Const.UPDATEFILESIZE,updateBean.getFilesize());
                }
            }
        });
    }
}
