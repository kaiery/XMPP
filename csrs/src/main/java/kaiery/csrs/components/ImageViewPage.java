package kaiery.csrs.components;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import kaiery.csrs.R;
import uk.co.senab.photoview.PhotoView;


/**
 * 加载webview中的图片url的图片浏览翻页组件
 */
public class ImageViewPage extends Activity {

    private String currentImg;
    private String[] imgAlist;

    private ViewPager vp;
    private TextView tv_imgcount;
    private int pos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentImg = getIntent().getStringExtra("currentImg");
        imgAlist = getIntent().getStringArrayExtra("imgAlist");

        setContentView(R.layout.activity_imagepage);

        vp = (ViewPager) findViewById(R.id.vp);
        tv_imgcount = (TextView) findViewById(R.id.tv_imgcount);
        vp.setAdapter(new MyAdapet());

        for (int i = 0; i < imgAlist.length; i++) {
            if(imgAlist[i].equals(currentImg)){
                pos = i;
                break;
            }
        }
        vp.setCurrentItem(pos);
        //监听viewpage翻页事件
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tv_imgcount.setText((position+1)+"/"+imgAlist.length);
            }
            @Override
            public void onPageSelected(int position) {

            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /**
     * 适配器
     */
    private class MyAdapet extends PagerAdapter {
        @Override
        public int getCount() {
            return imgAlist.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(ImageViewPage.this,R.layout.item_imagepage,null);
            uk.co.senab.photoview.PhotoView iv = (PhotoView) view.findViewById(R.id.pv_item);

            //加载图片
            String url = imgAlist[position];
            ImageLoader.getInstance().displayImage(url, iv, null, new MyImageLoadingListener(), new MyImageLoadingProgressListener());
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


    /**
     * 监听状态
     */
    public class MyImageLoadingListener implements ImageLoadingListener {
        @Override
        public void onLoadingStarted(String s, View view) {
        }
        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {

        }
        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
        }
        @Override
        public void onLoadingCancelled(String s, View view) {
        }
    }

    /**
     * 监听进度
     */
    public class MyImageLoadingProgressListener implements ImageLoadingProgressListener{
        @Override
        public void onProgressUpdate(String s, View view, int i, int i1) {

        }
    }




    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
           // ex.printStackTrace();
        }
        return false;
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }
}
