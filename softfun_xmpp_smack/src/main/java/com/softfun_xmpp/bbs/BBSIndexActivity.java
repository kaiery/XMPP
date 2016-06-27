package com.softfun_xmpp.bbs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.softfun_xmpp.R;
import com.softfun_xmpp.connection.IMService;
import com.softfun_xmpp.kit.webview.JsObject;

public class BBSIndexActivity extends AppCompatActivity {

    private WebView wv;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_index);
        wv = (WebView) findViewById(R.id.wv);
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);

        String title = getIntent().getStringExtra("title");
        //给页面设置工具栏
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_bbs_index);
        //标题写在setSupportActionBar前面
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initWebView();
    }

    private void initWebView() {
        try {
            //地址
            String weburl = getResources().getString(R.string.app_server) + "queryBBSIndexInfo?userid=" + IMService.mCurAccount + "&perNum=20&pageNum=1";
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
            //System.out.println("====================  url  ===================== "+url);
            WebSettings settings = wv.getSettings();
            //开启javascript
            settings.setJavaScriptEnabled(true);

            //直接运行js脚本  java调用js
            //wv.loadUrl("javascript:alert('你好')");

            //js调用java
            wv.addJavascriptInterface(new JsObject(this), "jsobject");
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
            case R.id.action_newbbs_item:
                Intent intent = new Intent(this, BBSNewActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bbs_menu, menu);
        return true;
    }
}
