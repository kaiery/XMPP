package com.softfun_xmpp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.AsmackUtils;
import com.softfun_xmpp.utils.ImageLoaderUtils;
import com.softfun_xmpp.utils.ThreadUtils;
import com.softfun_xmpp.utils.VipResouce;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.edu.zafu.coreprogress.listener.impl.UIProgressListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by 范张 on 2016-02-06.
 * 先设置此activity android:theme="@style/AppTheme.NoActionBar"
 */
public class MyProfile extends AppCompatActivity implements View.OnClickListener {


    private static final int CROP_BACKGROUND = 0;
    private static final int CROP_USERFACE = 1;
    private int flag;
    private String mAccount;
    private String mAvatar;
    private String mNickName;
    private String mUserid;
    private String mScore;
    private String mQQ;
    private String mEmail;
    private String mDesc;
    private String mBackdrop;
    private String mOrgname;
    private String mVip;


    private ImageView backdrop;

    private CoordinatorLayout coordlayout_activity_friendsprofile;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView iv_userface;
    private TextView tv_showname;
    private TextView tv_orgname;
    private TextView tv_userid;
    private TextView tv_score;
    private TextView tv_userdesc;
    private ImageView iv_vip;
    private TextView tv_qq;
    private TextView tv_email;
    private Button bt_edit_userface;
    private Button bt_edit_background;
    private Button bt_edit_password;
    private LinearLayout ll_item1;
    private LinearLayout ll_item2;
    private LinearLayout ll_item3;
    private LinearLayout ll_item4;
    private String cropFileName;
    private MyCropImage_ProgressListener progressListener;
    private RelativeLayout rl;
    private String mCropImageUri;
    private String mCropImagePath;
    private String mUploadType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        //初始化组件
        coordlayout_activity_friendsprofile = (CoordinatorLayout) findViewById(R.id.coordlayout_activity);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        iv_userface = (ImageView) findViewById(R.id.iv_userface);
        tv_showname = (TextView) findViewById(R.id.tv_showname);
        tv_orgname = (TextView) findViewById(R.id.tv_orgname);
        tv_userid = (TextView) findViewById(R.id.tv_userid);
        tv_score = (TextView) findViewById(R.id.tv_score);
        tv_userdesc = (TextView) findViewById(R.id.tv_userdesc);
        iv_vip = (ImageView) findViewById(R.id.iv_vip);
        tv_qq = (TextView) findViewById(R.id.tv_qq);
        tv_email = (TextView) findViewById(R.id.tv_email);
        bt_edit_userface = (Button) findViewById(R.id.bt_edit_userface);
        bt_edit_background = (Button) findViewById(R.id.bt_edit_background);
        bt_edit_password = (Button) findViewById(R.id.bt_edit_password);
        ll_item1 = (LinearLayout) findViewById(R.id.item1);
        ll_item2 = (LinearLayout) findViewById(R.id.item2);
        ll_item3 = (LinearLayout) findViewById(R.id.item3);
        ll_item4 = (LinearLayout) findViewById(R.id.item4);
        rl = (RelativeLayout) findViewById(R.id.rl);

        if(rl!=null){
            rl.setVisibility(View.GONE);
        }

        bt_edit_userface.setEnabled(false);
        bt_edit_background.setEnabled(false);
        bt_edit_password.setEnabled(false);


        initData();
        bt_edit_userface.setOnClickListener(this);
        bt_edit_background.setOnClickListener(this);
        bt_edit_password.setOnClickListener(this);
        ll_item1.setOnClickListener(this);
        ll_item2.setOnClickListener(this);
        ll_item3.setOnClickListener(this);
        ll_item4.setOnClickListener(this);
    }

    private void initData() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {

                //获取个人信息
                reGetInfo();

                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //给页面设置工具栏
                        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setTitle(mNickName);
                        setSupportActionBar(toolbar);
                        //添加返回按钮
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        //设置工具栏标题
                        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
                        collapsingToolbar.setTitle("");
                        //通过CollapsingToolbarLayout修改字体颜色
                        collapsingToolbar.setExpandedTitleColor(Color.WHITE);//设置还没收缩时状态下字体颜色
                        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);//设置收缩后Toolbar上字体的颜色



                        //设置标题
                        collapsingToolbar.setTitle(mNickName);
                        backdrop.setTag(mBackdrop);
                        iv_userface.setTag(mAvatar);
                        ImageLoader.getInstance().displayImage(mBackdrop, backdrop, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
                        ImageLoader.getInstance().displayImage(mAvatar, iv_userface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());


                        tv_showname.setText(mNickName);
                        tv_orgname.setText(mOrgname);
                        tv_userid.setText(mUserid);
                        tv_score.setText(mScore + " 积分");
                        tv_userdesc.setText(mDesc);
                        tv_qq.setText(mQQ);
                        tv_email.setText(mEmail);
                        //vip
                        if (mVip.equals("0")) {
                            iv_vip.setVisibility(View.GONE);
                        } else {
                            iv_vip.setVisibility(View.VISIBLE);
                            iv_vip.setImageResource(VipResouce.getVipResouce(mVip));
                        }



                        bt_edit_userface.setEnabled(true);
                        bt_edit_background.setEnabled(true);
                        bt_edit_password.setEnabled(true);
                    }
                });
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_edit_background: {
                flag = CROP_BACKGROUND;
                mUploadType = Const.UPLOAD_TYPE_BACKGROUND;
                //拾取图片
                Crop.pickImage(this);
                break;
            }
            case R.id.bt_edit_password:
                Intent intent_ep = new Intent(this,EditPasswordActivity.class);
                startActivity(intent_ep);
                break;
            case R.id.bt_edit_userface:
                flag = CROP_USERFACE;
                mUploadType = Const.UPLOAD_TYPE_FACE;
                //拾取图片
                Crop.pickImage(this);
                break;
            case R.id.item1:
                Intent intent = new Intent(this,EditItemActivity.class);
                intent.putExtra(EditItemActivity.ITEM_NAME,"昵称");
                intent.putExtra(EditItemActivity.ITEM_VALUE,mNickName);
                intent.putExtra(EditItemActivity.ITEM_FIELD, Const.NICKNAME);
                intent.putExtra(EditItemActivity.MAX_LENGTH, 10);
                startActivityForResult(intent,EditItemActivity.REQUESTCODE);
                break;
            case R.id.item2:
                Intent intent1 = new Intent(this,EditItemActivity.class);
                intent1.putExtra(EditItemActivity.ITEM_NAME,"QQ帐号");
                intent1.putExtra(EditItemActivity.ITEM_VALUE,mQQ);
                intent1.putExtra(EditItemActivity.ITEM_FIELD, Const.QQ);
                intent1.putExtra(EditItemActivity.MAX_LENGTH, 25);
                startActivityForResult(intent1,EditItemActivity.REQUESTCODE);
                break;
            case R.id.item3:
                Intent intent2 = new Intent(this,EditItemActivity.class);
                intent2.putExtra(EditItemActivity.ITEM_NAME,"Email");
                intent2.putExtra(EditItemActivity.ITEM_VALUE,mEmail);
                intent2.putExtra(EditItemActivity.ITEM_FIELD, Const.EMAIL);
                intent2.putExtra(EditItemActivity.MAX_LENGTH, 50);
                startActivityForResult(intent2,EditItemActivity.REQUESTCODE);
                break;
            case R.id.item4:
                Intent intent3 = new Intent(this,EditItemActivity.class);
                intent3.putExtra(EditItemActivity.ITEM_NAME,"个人简介");
                intent3.putExtra(EditItemActivity.ITEM_VALUE,mDesc);
                intent3.putExtra(EditItemActivity.ITEM_FIELD, Const.USERDESC);
                intent3.putExtra(EditItemActivity.MAX_LENGTH, 250);
                startActivityForResult(intent3,EditItemActivity.REQUESTCODE);
                break;
        }
    }


    /**
     * 开始裁剪
     * @param source
     */
    private void beginCrop(Uri source) {
        //定义截图文件保存地址
        cropFileName = UUID.randomUUID().toString()+".png";
        Uri destination = Uri.fromFile(new File(getCacheDir(), cropFileName));
        Crop.of(source, destination).asSquare().start(this);
    }

    /**
     * 裁剪完毕
     * @param resultCode
     * @param result
     */
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            //得到裁剪后的uri地址
            //System.out.println("====================  Crop.getOutput(result)  =====================:"+Crop.getOutput(result));
            mCropImageUri = Crop.getOutput(result).toString();
            mCropImagePath = Crop.getOutput(result).getPath();
            //初始化进度监听器
            progressListener = new MyCropImage_ProgressListener(flag);
            //上传图片
            HttpUtil.okhttpPost_uploadFile(mCropImagePath,cropFileName, progressListener, mUploadType,new MyCallBack());
            //如果裁剪的是头像
            if(flag==CROP_USERFACE){
                ImageLoader.getInstance().displayImage(mCropImageUri, iv_userface, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_circular());
            }else if(flag==CROP_BACKGROUND){
                //如果裁剪的是背景图
                ImageLoader.getInstance().displayImage(mCropImageUri, backdrop, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            String message = Crop.getError(result).getMessage();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }


    private class MyCallBack implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        }
    }

    /**
     * 上传进度监听器
     */
    private class MyCropImage_ProgressListener extends UIProgressListener{

        private final int mFlag;
        final Map<String , String> map = new HashMap<>();
        /**
         * 存储在m_user表中图像地址
         */
        String uploadImageUrl;

        public MyCropImage_ProgressListener(int flag) {
            this.mFlag = flag;
        }

        @Override
        public void onUIProgress(long currentBytes, long contentLength, boolean done) {
            //ui层回调
            rl.setVisibility(View.VISIBLE);
            //System.out.println((int) ((100 * currentBytes) / contentLength));
            if (done) {
                rl.setVisibility(View.GONE);
                //如果裁剪的是头像
                if(mFlag==CROP_USERFACE){
                    uploadImageUrl = Const.WEB_FACE_PATH + mAccount +"/"+ Const.WEB_FACE_THUMBS +cropFileName;
                    map.put(Const.AVATARURL,uploadImageUrl);
                    IMService.mCurAvatarUrl = GlobalContext.getInstance().getResources().getString(R.string.app_server) + uploadImageUrl;
                }else if(mFlag==CROP_BACKGROUND){
                    //如果裁剪的是背景图
                    uploadImageUrl = Const.WEB_BACKGROUND_PATH + mAccount+"/"+cropFileName;
                    map.put(Const.BACKGROUND,uploadImageUrl);
                    IMService.mCurBackground = GlobalContext.getInstance().getResources().getString(R.string.app_server) +uploadImageUrl;
                }
                //设置自己的Vcard信息
                AsmackUtils.setVcardInfo();
                //更新m_user表
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        String username = IMService.mCurAccount.substring(0,IMService.mCurAccount.lastIndexOf("@"));
                        HttpUtil.okhttpPost_updateVcard(map,username);
                    }
                });
            }
        }

        @Override
        public void onUIStart(long currentBytes, long contentLength, boolean done) {
            super.onUIStart(currentBytes, contentLength, done);
        }

        @Override
        public void onUIFinish(long currentBytes, long contentLength, boolean done) {
            super.onUIFinish(currentBytes, contentLength, done);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            //修改内容完毕后，进行更新界面
            if(resultCode==EditItemActivity.RESULT_SUCCEED){
                String mItemField = data.getStringExtra(EditItemActivity.ITEM_FIELD);
                String value = data.getStringExtra(EditItemActivity.ITEM_VALUE);
                if(mItemField.equals(Const.QQ)){
                    tv_qq.setText(value);
                }
                if(mItemField.equals(Const.NICKNAME)){
                    tv_showname.setText(value);
                }
                if(mItemField.equals(Const.EMAIL)){
                    tv_email.setText(value);
                }
                if(mItemField.equals(Const.USERDESC)){
                    tv_userdesc.setText(value);
                }

                reGetInfo();
            }
            //修改内容失败
            if(resultCode==EditItemActivity.RESULT_FAILURE){

            }


            //拾取图片成功后
            if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
                beginCrop(data.getData());
            } else if (requestCode == Crop.REQUEST_CROP) {
                handleCrop(resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 重新获取个人信息
     */
    private void reGetInfo() {
        mAccount = IMService.mCurAccount;
        mUserid = IMService.mCurUserid;
        mAvatar = IMService.mCurAvatarUrl + "?userid=" + mUserid;
        mNickName = IMService.mCurNickName;
        mScore = IMService.mCurScore;
        mQQ = IMService.mCurQq;
        mEmail = IMService.mCurEmail;
        mDesc = IMService.mCurDesc;
        mBackdrop = IMService.mCurBackground;
        mOrgname = IMService.mCurOrgname;
        mVip = IMService.mCurVip;

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



}
