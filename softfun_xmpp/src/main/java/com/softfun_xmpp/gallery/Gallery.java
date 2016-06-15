package com.softfun_xmpp.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.utils.ImageLoaderUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 范张 on 2016-02-25.
 */
public class Gallery extends Activity {

    private ViewPager id_viewpager;
    //private int[] mImgs = new int[]{R.drawable.splash};
    /**
     * 要加载的所有图片url数组
     */
    private String[] mImgUrl;
    /**
     * 图片容器
     */
    private ImageView[] mImageViews;
    /**
     * 当前要看的图片地址url
     */
    private String currentUrl ;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 使屏幕不显示标题栏(必须要在setContentView方法执行前执行)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏，使内容全屏显示(必须要在setContentView方法执行前执行)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gallery);
        //找到组件
        id_viewpager = (ViewPager) findViewById(R.id.id_gallery_viewpager);
        //获得传递值
        Intent intent = getIntent();
        mImgUrl = intent.getStringArrayExtra("imgUrl");
        currentUrl = intent.getStringExtra("currentUrl");
        //设置容器长度
        mImageViews = new ImageView[mImgUrl.length];
        //找到当前要查看图片所在list中的位置
        //从磁盘缓存中找出所有图片的缓存
        int currentNum = 0;
        for (int i = 0; i < mImgUrl.length; i++) {
            if(mImgUrl[i].equals(currentUrl)){
                currentNum = i;

            }
        }


        initData(currentNum);
    }


    /**
     * 初始化
     * @param currentNum
     */
    private void initData(int currentNum) {
        id_viewpager.setAdapter(new PagerAdapter() {

            //实例化内页
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ZoomImageView zoomImageView = new ZoomImageView(getApplicationContext());
                ImageLoader.getInstance().displayImage(mImgUrl[position],zoomImageView, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());
                //zoomImageView.setImageBitmap(BitmapUtil.ScaleBitmapFixScreen(initImageFromCache(mImgUrl[position])));
                container.addView(zoomImageView);
                mImageViews[position] = zoomImageView;
                return zoomImageView;
            }

            //移除内页
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mImageViews[position]);
            }

            //返回数量
            @Override
            public int getCount() {
                return mImageViews.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });

        id_viewpager.setCurrentItem(currentNum);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }





    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
