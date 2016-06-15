package com.softfun_xmpp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.softfun_xmpp.R;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.kit.webview.JsObject;

public class WebFrameActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressBar mPb;
    private WebView mWv;
    private String mUrl;
    private String mTitle;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mPb = (ProgressBar) findViewById(R.id.pb);
        mWv = (WebView) findViewById(R.id.wv);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_frame);

        assignViews();

        Intent intent = getIntent();
        mUrl = intent.getStringExtra(Const.WEB_URL);
        mTitle = intent.getStringExtra(Const.WEB_TITLE);

        mToolbar.setTitle(mTitle);
        setSupportActionBar(mToolbar);
        //添加返回按钮
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        initData();
    }

    private void initData() {

        if(!TextUtils.isEmpty(mUrl)){

            mWv.loadUrl(mUrl);
            //重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
            mWv.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            //监听进度
            mWv.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    mPb.setMax(100);
                    mPb.setProgress(newProgress);
                }
            });
            //监听开始、完成
            mWv.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    mPb.setVisibility(View.VISIBLE);
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    mPb.setVisibility(View.GONE);
                }
            });
            //获取url
            String url = mWv.getUrl();
            WebSettings settings = mWv.getSettings();
            //开启javascript
            settings.setJavaScriptEnabled(true);
            //直接运行js脚本  java->js
            //wv.loadUrl("javascript:alert('你好')");
            //js调用java
            mWv.addJavascriptInterface(new JsObject(this), "jsobject");
        }
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
    // 设置回退
    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWv.canGoBack()) {
            mWv.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
