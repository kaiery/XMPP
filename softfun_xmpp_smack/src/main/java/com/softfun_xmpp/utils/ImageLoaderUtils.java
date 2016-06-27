package com.softfun_xmpp.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.softfun_xmpp.R;

import java.io.File;

/**
 * Created by 范张 on 2016-04-21.
 */
public class ImageLoaderUtils {

    public static final long GB = 1073741824; // 1024 * 1024 * 1024
    public static final long MB = 1048576; // 1024 * 1024
    public static final long KB = 1024;

    /**
     * 磁盘缓存信息
     * @return
     */
    public static String getCacheInfo(){
        DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
        if(diskCache!=null){
            try {
                File directory = diskCache.getDirectory();
                long size = FileUtils.getFolderSize(directory);
                if (size >= GB) {
                    return String.format("%.2f GB", size * 1.0 / GB);
                } else if (size >= MB) {
                    return String.format("%.2f MB", size * 1.0 / MB);
                } else {
                    return String.format("%.2f KB", size * 1.0 / KB);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "没有缓存";
    }

    /**
     * 清除磁盘缓存
     */
    public static void clearCache(){
        DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
        if(diskCache!=null){
            diskCache.clear();
        }
    }


    /***
     * 按钮Grid图标使用
     * @return
     */
    public static DisplayImageOptions getOptions_NoCacheInMem_NoCacheInDisk_NoExif() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.no_image_available) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.no_image_available)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.no_image_available)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(false)
                .cacheOnDisk(false)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(false)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.NONE)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                //.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
                //.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                //.displayer(new Displayer(0))//是否设置为圆角，弧度为多少
                //.displayer(new RoundedBitmapDisplayer(50,10))//是否设置为圆角，弧度为多少,避免使用.displayer(new RoundedBitmapDisplayer(20)) //他会创建新的ARGB_8888格式的Bitmap对象；
                //.displayer(new FadeInBitmapDisplayer(1400))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        return options;
    }

    /**
     * 缓存：磁盘。
     * 图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
     * @return
     */
    public static DisplayImageOptions getOptions_NoCacheInMem_CacheInDisk_Exif_IN_SAMPLE_POWER_OF_2() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.no_image_available) //设置图片在下载期间显示的图片
                //.showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
                //.showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(false)
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)//图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                //.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
                //.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                //.displayer(new Displayer(0))//是否设置为圆角，弧度为多少
                //.displayer(new RoundedBitmapDisplayer(50,10))//是否设置为圆角，弧度为多少,避免使用.displayer(new RoundedBitmapDisplayer(20)) //他会创建新的ARGB_8888格式的Bitmap对象；
                //.displayer(new FadeInBitmapDisplayer(1400))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        return options;
    }

    /**
     * 缓存：磁盘。
     * 图像将完全按比例缩小的目标大小
     * @return
     */
    public static DisplayImageOptions getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.no_image_available) //设置图片在下载期间显示的图片
                //.showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
                //.showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(false)
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY)//图像将完全按比例缩小的目标大小
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                //.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
                //.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                //.displayer(new Displayer(0))//是否设置为圆角，弧度为多少
                //.displayer(new RoundedBitmapDisplayer(50,10))//是否设置为圆角，弧度为多少,避免使用.displayer(new RoundedBitmapDisplayer(20)) //他会创建新的ARGB_8888格式的Bitmap对象；
                //.displayer(new FadeInBitmapDisplayer(1400))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        return options;
    }

    /**
     * 磁盘缓存
     * 图片会缩放到目标大小完全
     * 圆形
     * @return
     */
    public static DisplayImageOptions getOptions_NoCacheInMem_CacheInDisk_Exif_circular() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.useravatar) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.useravatar)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(false)
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(false)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                //.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
                //.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .displayer(new Displayer(0))//是否设置为圆角，弧度为多少
                //.displayer(new RoundedBitmapDisplayer(999,0))//是否设置为圆角，弧度为多少,避免使用.displayer(new RoundedBitmapDisplayer(20)) //他会创建新的ARGB_8888格式的Bitmap对象；
                //.displayer(new FadeInBitmapDisplayer(1400))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        return options;
    }

    public static DisplayImageOptions getOptions_CacheInMem_CacheInDisk_Exif_circular() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.useravatar) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.useravatar)//设置图片Uri为空或是错误的时候显示的图片
                //.showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(false)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                //.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .displayer(new Displayer(0))//是否设置为圆角，弧度为多少
                //.displayer(new RoundedBitmapDisplayer(999,0))//是否设置为圆角，弧度为多少,避免使用.displayer(new RoundedBitmapDisplayer(20)) //他会创建新的ARGB_8888格式的Bitmap对象；
                //.displayer(new FadeInBitmapDisplayer(1400))//是否图片加载好后渐入的动画时间
                .build();//构建完成
        return options;
    }


    /**
     * 磁盘缓存
     * 内存缓存
     * 图片会缩放到目标大小完全
     * 圆形
     * @return
     */
    public static DisplayImageOptions getOptions_CacheInMem_CacheInDisk_Exif_circular_border() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.no_image_available) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.no_image_available)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.no_image_available)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .displayer(new CircleBitmapDisplayer(Color.WHITE, 1))
                .build();//构建完成
        return options;
    }


    /**
     *磁盘缓存
     * 内存缓存
     * 图片会缩放到目标大小完全
     * @return
     */
    public static DisplayImageOptions getOptions_CacheInMem_CacheInDisk_Exif() {
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.no_image_available) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.no_image_available)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.no_image_available)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.EXACTLY)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .build();//构建完成
        return options;
    }
}
