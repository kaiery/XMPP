package softfun.mycamera.application;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import softfun.mycamera.R;

/**
 * Created by 范张 on 2016-04-21.
 */
public class ImageLoaderUtils {


    /**
     * 清除磁盘缓存
     */
    public static void clearCache(){
        DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
        if(diskCache!=null){
            diskCache.clear();
        }
    }


    public static DisplayImageOptions getOptions_NoCacheInMem_NoCacheInDisk_Exif_EXACTLY() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.avatar) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.avatar)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.avatar)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(false)
                .cacheOnDisk(false)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY)//图像将完全按比例缩小的目标大小
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .build();//构建完成
        return options;
    }


}
