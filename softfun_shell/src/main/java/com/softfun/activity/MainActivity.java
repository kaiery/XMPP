package com.softfun.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.softfun.R;
import com.softfun.bean.PackageBean;
import com.softfun.bean.UpdateBean;
import com.softfun.bean.UserBean;
import com.softfun.fragment.btngrid.BtnGridViewFragment;
import com.softfun.network.DownloadAPK;
import com.softfun.network.HttpUtil;
import com.softfun.utils.AppUtils;
import com.softfun.utils.MatherListUtil;
import com.softfun.utils.ThreadUtils;
import com.softfun.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    /**
     * 平台包含应用的包名
     */
    private String[] mPackageList;
    /**
     * 平台包含应用的应用名
     */
    private String[] mNameList;
    /**
     * 当前系统的所有应用
     */
    private List<PackageInfo> mAllApps;
    /**
     * 平台可用应用List
     */
    public List<PackageBean> mAppPackageList;

    private static final int permsRequestCode = 200;
    public static UserBean mUserbean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // 使屏幕不显示标题栏(必须要在setContentView方法执行前执行)
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        // 隐藏状态栏，使内容全屏显示(必须要在setContentView方法执行前执行)
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {

            mUserbean = getIntent().getParcelableExtra("USERBEAN");
            System.out.println("====================    ====================="+ mUserbean.getShowname());
            new InitDataAsycTask().execute();
        }
    }


    private class InitDataAsycTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mPackageList = getResources().getStringArray(R.array.softfun_app_packages_list);
            mNameList = getResources().getStringArray(R.array.softfun_app_name_list);

            mAppPackageList = new ArrayList<>();
            if(mPackageList!=null && mNameList!=null && mPackageList.length==mNameList.length){
                for (int i = 0; i < mPackageList.length; i++) {
                    PackageBean bean = new PackageBean();
                    bean.setAppname(mNameList[i]);
                    bean.setPackagecode(mPackageList[i]);
                    bean.setOp("");
                    mAppPackageList.add(bean);
                }
            }

            PackageBean bean = new PackageBean();
            bean.setAppname("更多");
            bean.setPackagecode("com.softfun_more");
            bean.setOp("");
            mAppPackageList.add(bean);


            mAllApps = AppUtils.getAllApps(MainActivity.this);
            if(mAllApps !=null && mAllApps.size()>0 && mPackageList!=null && mPackageList.length>0){
                List<String> sysList = new ArrayList<>();
                for (int i = 0; i < mAllApps.size(); i++) {
                    sysList.add(mAllApps.get(i).packageName);
                }

                List<String> appList = new ArrayList<>();
                for (int i = 0; i < mPackageList.length; i++) {
                    appList.add(mPackageList[i]);
                }
                List<MatherListUtil.ListOPBean> comparedList = MatherListUtil.compare(appList, sysList);
                for (int i = 0; i < comparedList.size(); i++) {
                    if(comparedList.get(i).getOp().equals("-")){
                        for (int k = 0; k < mAppPackageList.size(); k++) {
                            if(mAppPackageList.get(k).getPackagecode().equals(comparedList.get(i).getStr())){
                                mAppPackageList.get(k).setOp("-");
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //1、创建fragment对象
            BtnGridViewFragment fragment = new BtnGridViewFragment();
            //2、创建fragment管理器
            FragmentManager fm = getSupportFragmentManager();
            //3、开启事务
            FragmentTransaction ft = fm.beginTransaction();
            //4、加载fragment
            ft.add(R.id.container, fragment);
            //5、提交
            ft.commit();


            //权限检查
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
                getPermission();
            }else{
                writeUserLoginLog();
            }

            //检测版本是否需要升级
            checkVersion();
        }
    }

    /**
     * 写用户登录日志
     */
    private void writeUserLoginLog() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //System.out.println("====================  网络类型  ====================="+NetWorkUtils.getCurrentNetType(MainActivity.this));
                //System.out.println("====================  运营商  ====================="+NetWorkUtils.getProvider(MainActivity.this));
                HttpUtil.okhttpPost_writeUserLoginLog(mUserbean.getUsername());
            }
        });
    }
    /**
     * 版本检查
     */
    private void checkVersion() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final UpdateBean updateBean = HttpUtil.okhttpGet_UpdateInfo("com.softfun_shell");
                if(updateBean!=null){
                    //获取应用程序的版本号
                    int verCode = AppUtils.getVerCode(MainActivity.this);
                    if(updateBean.getVercode()>verCode){
                        //弹出对话框
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                }
            }
        });
    }



    /**
     * ANDROID6 权限申请
     */
    private void getPermission() {
        if ( (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE
                    },
                    permsRequestCode
            );
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case 200:
                boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(storageAccepted){
                    System.out.println("====================  手机存储授权成功  =====================");
                }else{
                    System.out.println("====================  手机存储授权失败  =====================");
                }
                boolean phoneStateAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                if(phoneStateAccepted){
                    System.out.println("====================  手机状态授权成功  =====================");
                    writeUserLoginLog();
                }else{
                    System.out.println("====================  手机状态授权失败  =====================");
                    ToastUtils.showToastSafe_Long("请授权，否则系统无法正常工作。");
                    finish();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
