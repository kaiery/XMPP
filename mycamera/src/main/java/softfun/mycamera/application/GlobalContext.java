package softfun.mycamera.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import softfun.mycamera.R;


public class GlobalContext extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //获取Context
        context = getApplicationContext();

        //初始化
        initImageLoader(getApplicationContext());
    }


    public static Context getInstance() {
        return context;
    }


    public static void initImageLoader(Context context) {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.avatar) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.avatar)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.avatar)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY)//图像将完全按比例缩小的目标大小
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .build();//构建完成


        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);    //线程优先级
        config.denyCacheImageMultipleSizesInMemory();       //同一个图片地址不允许存在多个
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());//缓存文件名命名规则
        config.memoryCache(new UsingFreqLimitedMemoryCache(3 * 1024 * 1024));//内存缓存
        config.memoryCacheSize(3 * 1024 * 1024);
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB磁盘缓存
        config.tasksProcessingOrder(QueueProcessingType.LIFO);//后进先出顺序
        config.imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)); // connectTimeout (5 s), readTimeout (30 s)超时时间
        config.defaultDisplayImageOptions(options);
        //初始化单例配置
        ImageLoader.getInstance().init(config.build());
    }
}
