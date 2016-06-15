package com.softfun_xmpp.kit.webview;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.softfun_xmpp.bbs.BBSNewActivity;
import com.softfun_xmpp.components.ImageViewPage;


/**
 * js与java的桥梁类
 */
public class JsObject {


    private final Context context;
    private String currentImg;
    private String[] imgAlist;


    public JsObject(Context context) {
        this.context = context;
    }

    /**
     * 测试
     * @param str
     */
    @JavascriptInterface
    public void sayhello(String str) {
        System.out.println(str);
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 进入发表新帖子的页面
     */
    @JavascriptInterface
    public void writeNewBBS(){
        Intent intent = new Intent(context, BBSNewActivity.class);
        context.startActivity(intent);
    }


    /**
     * 得到当前图片的地址
     * @param imgurl
     */
    @JavascriptInterface
    public void openImg(String imgurl) {
        this.currentImg = imgurl;
        Intent intent = new Intent(context,ImageViewPage.class);
        intent.putExtra("currentImg",currentImg);
        intent.putExtra("imgAlist",imgAlist);
        context.startActivity(intent);
    }


    /**
     * 获取网页中所有图片的list
     *
     * @param imgList
     */
    @JavascriptInterface
    public void setImgList(String imgList) {
        imgAlist = imgList.split(";");
    }


}
