package kaiery.csrs.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import kaiery.csrs.R;


/**
 * 自动轮播图
 */
public class AutoViewPagerFragment extends Fragment {

    private static final int SCROLL_WHAT = 1;
    private View view;
    private ViewPager vp_auto;
    private String[] imgAlist;
    /**
     * 上一个指示器按钮
     */
    Button mPreSelectedBt;
    /**
     * 加载指示器的LinearLayout组件
     */
    private LinearLayout mNumLayout;
    private MyAdapet adapter;
    private final long interval = 5000;
    private MyHandler handler = new MyHandler();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_autoviewpager, container, false);
            vp_auto = (ViewPager) view.findViewById(R.id.vp_auto);
            mNumLayout = (LinearLayout) view.findViewById(R.id.mNumLayout_auto);
            initData();
        }
        return view;
    }

    private void initData() {
        // TODO 网络访问轮播图地址 http://111.1.62.169/SoftFun/queryBanner
        imgAlist = new String[4];
        String service = getContext().getResources().getString(R.string.app_server);
        imgAlist[0]=service+"web/banner/gallery1.jpg";
        imgAlist[1]=service+"web/banner/gallery2.jpg";
        imgAlist[2]=service+"web/banner/gallery3.jpg";
        imgAlist[3]=service+"web/banner/gallery4.jpg";
        adapter = new MyAdapet();
        vp_auto.setAdapter(adapter);

        //监听页面改变
        vp_auto.addOnPageChangeListener(new MyPagerChange());


        //初始化指示器
        Bitmap bitmap = BitmapFactory. decodeResource(getResources(), R.mipmap.icon_dot_darw);
        for (int i = 0; i < imgAlist.length; i++) {
            Button bt = new Button(view.getContext());
            bt.setLayoutParams( new ViewGroup.LayoutParams(bitmap.getWidth(),bitmap.getHeight()));
            if(i==0){
                bt.setBackgroundResource(R.mipmap.icon_dot_normal_white );
            }else{
                bt.setBackgroundResource(R.mipmap.icon_dot_darw );
            }
            mNumLayout.addView(bt);
        }
        //设置第一个指示器为当前状态
        Button currentBt = (Button)mNumLayout.getChildAt(0);
        currentBt.setBackgroundResource(R.mipmap.icon_dot_normal_white);
        mPreSelectedBt = currentBt;


        //自动滚动画廊
        sendScrollMessage(interval);

    }


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
            View view = View.inflate(getContext(),R.layout.item_autopagerfragment_image_item,null);
            ImageView iv = (ImageView) view.findViewById(R.id.tv_item);

            //加载图片
            String url = imgAlist[position];
            ImageLoader.getInstance().displayImage(url, iv, getOptions());
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private DisplayImageOptions getOptions() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.mipmap.ic_launcher) //设置图片在下载期间显示的图片
//                .showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
//                .showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
                //.cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                        //.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
                        //.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                        //.displayer(new Displayer(0))//是否设置为圆角，弧度为多少
                        //.displayer(new RoundedBitmapDisplayer(50,10))//是否设置为圆角，弧度为多少
                .displayer(new FadeInBitmapDisplayer(1400))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        return options;
    }




    /**
     * 监听页面位置改变
     */
    private class MyPagerChange implements ViewPager.OnPageChangeListener {
        /**
         * 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到 调用
         * @param position 当前页面，及你点击滑动的页面
         * @param positionOffset 当前页面偏移的百分比
         * @param positionOffsetPixels :当前页面偏移的像素位置
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        /**
         *此方法是页面跳转完后得到调用，position是你当前选中的页面的Position（位置编号）。
         * @param position
         */
        @Override
        public void onPageSelected(int position) {
            if (mPreSelectedBt != null){
                mPreSelectedBt .setBackgroundResource(R.mipmap.icon_dot_darw);
            }
            Button currentBt = (Button)mNumLayout.getChildAt(position);
            currentBt.setBackgroundResource(R.mipmap.icon_dot_normal_white );
            mPreSelectedBt = currentBt;
        }
        /**
         * 滚动状态改变时调用的方法。用于发现当用户拖动开始,当寻呼机自动解决当前页面,或当它完全停止/空闲
         * @param state ==1的时辰默示正在滑动，state==2的时辰默示滑动完毕了，state==0的时辰默示什么都没做
         */
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }







    /**
     * 自动滚动画廊
     * @param delayTimeInMills
     */
    private void sendScrollMessage(long delayTimeInMills) {
        /** remove messages before, keeps one message is running at most **/
        handler.removeMessages(SCROLL_WHAT);
        handler.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCROLL_WHAT:
                    scrollOnce();
                    sendScrollMessage(interval);
                    break;
            }
        }
    }

    /**
     * 滚动逻辑
     */
    private void scrollOnce() {
        int currentItem = vp_auto.getCurrentItem();
        if(currentItem==imgAlist.length-1){
            vp_auto.setCurrentItem(0,true);
        }else{
            vp_auto.setCurrentItem(currentItem+1,true);
        }
    }
}
