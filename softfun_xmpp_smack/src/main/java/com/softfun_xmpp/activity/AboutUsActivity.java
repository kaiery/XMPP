package com.softfun_xmpp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun_xmpp.R;
import com.softfun_xmpp.utils.ImageLoaderUtils;

public class AboutUsActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private ImageView mIv;
    private LinearLayout mItem1;
    private LinearLayout mItem2;
    private ImageView iv_rqcode;

    private void assignViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mIv = (ImageView) findViewById(R.id.iv);
        mItem1 = (LinearLayout) findViewById(R.id.item1);
        mItem2 = (LinearLayout) findViewById(R.id.item2);
        iv_rqcode = (ImageView) findViewById(R.id.iv_rqcode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        assignViews();
        mToolbar.setTitle("关于我们");
        setSupportActionBar(mToolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String rqcode_url = getResources().getString(R.string.app_server)+"update/RQ_code.png";
        ImageLoader.getInstance().displayImage(rqcode_url,iv_rqcode, ImageLoaderUtils.getOptions_NoCacheInMem_CacheInDisk_Exif_EXACTLY());


        mItem1.setOnClickListener(this);
        mItem2.setOnClickListener(this);
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
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.item1:
                Intent intent2=new Intent();
                intent2.setAction("android.intent.action.CALL");
                intent2.addCategory("android.intent.category.DEFAULT");
                intent2.setData(Uri.parse("tel:"+getResources().getString(R.string.hotline_number)));
                startActivity(intent2);
                break;
            case R.id.item2:

                break;
        }
    }
}
