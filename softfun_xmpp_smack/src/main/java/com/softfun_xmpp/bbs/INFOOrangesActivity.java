package com.softfun_xmpp.bbs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.softfun_xmpp.R;
import com.softfun_xmpp.bbs.bean.OrangeCodeBean;
import com.softfun_xmpp.network.HttpUtil;
import com.softfun_xmpp.utils.ThreadUtils;

import java.util.List;


//柑橘

public class INFOOrangesActivity extends AppCompatActivity {

    private WebView wv;
    private ProgressBar pb;
    private TextView mTitle;
    private String title;
    private String code;
    private List<OrangeCodeBean> orangeCodeBeanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webinfo_index_with_menu_toolbar);
        mTitle = (TextView) findViewById(R.id.tv_title);
        wv = (WebView) findViewById(R.id.wv);
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);

        title = getIntent().getStringExtra("title");
        code = getIntent().getStringExtra("code");
        //给页面设置工具栏
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_webinfo_index);
        //标题写在setSupportActionBar前面
        //toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //添加返回按钮
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP);

        getTypeList();

        mTitle.setText(title);
    }

    /**
     * 获取柑橘分类
     */
    private void getTypeList() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                orangeCodeBeanList = HttpUtil.okhttpPost_queryOrangeTypeList(code);
                invalidateOptionsMenu();
                if(orangeCodeBeanList!=null && orangeCodeBeanList.size()>0){
                    ThreadUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initWebView(orangeCodeBeanList.get(0).getKey());
                            //initWebView(code);
                        }
                    });
                }
            }
        });
    }


    private void initWebView(String detailtype) {
        try {
            LoadWebUrl(detailtype);

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
            //String url = wv.getUrl();
            WebSettings settings = wv.getSettings();
            //开启javascript
            settings.setJavaScriptEnabled(true);

            settings.setSavePassword(false);
            //直接运行js脚本  java->js
            //wv.loadUrl("javascript:alert('你好')");

            //js->java
            //wv.addJavascriptInterface(new JsObject(this), "jsobject");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载网页
     */
    private void LoadWebUrl(String detailtype) {
        //地址 http://111.1.62.169:7001/nj110/queryInfoList.do?type=11&detailtype=01
        String weburl = getResources().getString(R.string.app_server_n110) + "queryOrangeInfoList.do?type=" + code+"&detailtype="+detailtype;
        wv.loadUrl(weburl);
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
            case android.R.id.home: {
                if (wv.canGoBack()) {
                    wv.goBack(); // goBack()表示返回WebView的上一页面
                    return true;
                }
                finish();
                return true;
            }
        }
        //遍历orangeCodeBeanList  监听菜单点击
        for (OrangeCodeBean bean : orangeCodeBeanList) {
            if(item.getItemId()==bean.getItemid()){
                queryOrangesInfo(bean.getKey());
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
//        xml加载菜单
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);

//        动态加载菜单item
        if(orangeCodeBeanList!=null && orangeCodeBeanList.size()>0){
            SubMenu subMenu1 = menu.addSubMenu("-选择分类-");

            for (int i = 0; i < orangeCodeBeanList.size(); i++) {
                subMenu1.add(0, orangeCodeBeanList.get(i).getItemid(), Menu.NONE, orangeCodeBeanList.get(i).getValue()).setIcon(R.drawable.ic_view_list);
            }
            MenuItem subMenu1Item = subMenu1.getItem();
            //subMenu1Item.setIcon(R.drawable.ic_launcher);
            subMenu1Item.setTitle("选择分类");
            subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }

        return true;
    }


    private void queryOrangesInfo(String key) {
        //ToastUtils.showToastSafe("选择了"+key);
        LoadWebUrl(key);
    }



}
