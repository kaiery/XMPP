package com.softfun_xmpp.bbs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.softfun_xmpp.R;


public class INFOActivity extends AppCompatActivity {

    private WebView wv;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webinfo_index);
        wv = (WebView) findViewById(R.id.wv);
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);

        String title = getIntent().getStringExtra("title");
        String code = getIntent().getStringExtra("code");
        //给页面设置工具栏
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_webinfo_index);
        //标题写在setSupportActionBar前面
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initWebView(code);
    }


    private void initWebView(String code) {
        try {
            //地址 http://111.1.62.169:7001/nj110/queryInfoList.do?type=03
            String weburl = "http://111.1.62.169:7001/nj110/queryInfoList.do?type="+code;
            wv.loadUrl(weburl);
            //重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
            wv.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            //监听进度
            wv.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    pb.setMax(100);
                    pb.setProgress(newProgress);
                }
            });
            //监听开始、完成
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    pb.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    pb.setVisibility(View.GONE);
                }
            });
            //获取url
            String url = wv.getUrl();
            WebSettings settings = wv.getSettings();
            //开启javascript
            settings.setJavaScriptEnabled(true);

            //直接运行js脚本  java->js
            //wv.loadUrl("javascript:alert('你好')");

            //js->java
            //wv.addJavascriptInterface(new JsObject(this), "jsobject");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    // 设置回退
    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
            wv.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (wv.canGoBack()) {
                    wv.goBack(); // goBack()表示返回WebView的上一页面
                    return true;
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
