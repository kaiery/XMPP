package com.softfun_xmpp.bbs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.softfun_xmpp.R;
import com.softfun_xmpp.adapter.GridAdapter_Gallery;
import com.softfun_xmpp.bbs.bean.CodeBean;
import com.softfun_xmpp.bean.ImagePickBean;
import com.softfun_xmpp.components.TagsGridView;
import com.softfun_xmpp.constant.Const;
import com.softfun_xmpp.network.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.edu.zafu.coreprogress.listener.impl.UIProgressListener;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class BBSNewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE = 1;
    private TagsGridView gv;
    private Button btn_addimg;
    private Button btn_sendnewbbs;
    private EditText et_title;
    private EditText et_content;
    private Spinner Spinner_type;

    /**
     * 已经选择的图片
     */
    private ArrayList<String> mSelectPath;
    /**
     * 重命名的文件列表
     */
    ArrayList<String> fileNameList ;

    private List<ImagePickBean> mNameList;
    private GridAdapter_Gallery adapter;
    private ArrayAdapter arrayAdapter;
    private List<CodeBean> mTypeList;
    private List<String> mStringTypeList;
    /**
     * 获取返回的图片列表
     */
    private List<String> paths;

    /**
     * 长按标记
     */
    private boolean isLong;

    /**
     * 上传成功的总数
     */
    private int uploadDones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_new);

        //给页面设置工具栏
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_bbs_new);
        //标题写在setSupportActionBar前面
        toolbar.setTitle("发表");
        setSupportActionBar(toolbar);
        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSelectPath = new ArrayList<>();

        et_title = (EditText) findViewById(R.id.et_title);
        et_content = (EditText) findViewById(R.id.et_content);
        btn_sendnewbbs = (Button) findViewById(R.id.btn_sendnewbbs);
        btn_addimg = (Button) findViewById(R.id.btn_addimg);
        Spinner_type = (Spinner) findViewById(R.id.Spinner_type);
        gv = (TagsGridView) findViewById(R.id.gv);


        btn_addimg.setOnClickListener(this);
        btn_sendnewbbs.setOnClickListener(this);

        mNameList = new ArrayList<>();
        adapter = new GridAdapter_Gallery(this, mNameList);
        gv.setAdapter(adapter);


        //添加点击事件
        adapter.setUserPhotoClickListener(new GridAdapter_Gallery.UserPhotoClickListener() {
            @Override
            public void onUserPhotoClick(View view, int position) {
                if (mNameList.size() > 0 && isLong) {
                    ImagePickBean pickBean = mNameList.get(position);
                    pickBean.setCheck(!pickBean.isCheck());
                    mNameList.set(position, pickBean);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        //添加长按事件
        adapter.setUserPhotoLongClickListener(new GridAdapter_Gallery.UserPhotoLongClickListener() {
            @Override
            public void onUserPhotoLongClick(View view, int position) {
                for (int i = 0; i < mNameList.size(); i++) {
                    mNameList.get(i).setShowcheck(true);
                    if (i == position) {
                        mNameList.get(i).setCheck(true);
                    } else {
                        mNameList.get(i).setCheck(false);
                    }
                }
                adapter.notifyDataSetChanged();
                isLong = true;
                invalidateOptionsMenu();
            }
        });

        mTypeList = new ArrayList<>();
        mTypeList.add(new CodeBean("10", "休闲农业"));
        mTypeList.add(new CodeBean("02", "农业动态"));
        mTypeList.add(new CodeBean("03", "农事提醒"));
        mTypeList.add(new CodeBean("04", "市场分析"));
        mTypeList.add(new CodeBean("05", "农资商家"));
        mTypeList.add(new CodeBean("06", "新品技术"));
        mTypeList.add(new CodeBean("07", "产品供求"));
        mTypeList.add(new CodeBean("08", "名优特产"));
        mTypeList.add(new CodeBean("09", "便民信息"));
        mStringTypeList = new ArrayList<>();
        mStringTypeList.add("休闲农业");
        mStringTypeList.add("农业动态");
        mStringTypeList.add("农事提醒");
        mStringTypeList.add("市场分析");
        mStringTypeList.add("农资商家");
        mStringTypeList.add("新品技术");
        mStringTypeList.add("产品供求");
        mStringTypeList.add("名优特产");
        mStringTypeList.add("便民信息");
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mStringTypeList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        Spinner_type.setAdapter(arrayAdapter);
        Spinner_type.setSelection(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addimg: {
                Intent intent = new Intent(BBSNewActivity.this, MultiImageSelectorActivity.class);
                // 是否显示调用相机拍照
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
                // 最大图片选择数量
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
                // 设置模式 (支持 单选/MultiImageSelectorActivity.MODE_SINGLE 或者 多选/MultiImageSelectorActivity.MODE_MULTI)
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
                // 默认选择图片,回填选项(支持String ArrayList)
                intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
                //if(mSelectPath != null && mSelectPath.size()>0){
                //intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
                //}
                startActivityForResult(intent, REQUEST_IMAGE);
                break;
            }
            case R.id.btn_sendnewbbs:
                sendNewBBS();
                break;
        }
    }

    private void sendNewBBS() {
        final String title = et_title.getText().toString();
        String type = Spinner_type.getSelectedItem().toString();
        final String typecode = getTypeCode(type);
        final String content = et_content.getText().toString();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "标题为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "内容为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(fileNameList==null){
            fileNameList = new ArrayList<>();
        }
        if(fileNameList.size()>0){
            for (String fn : fileNameList) {
                fileNameList.remove(fn);
            }
            fileNameList.clear();
        }
        //上传图片
        uploadBBSPhoto(fileNameList);

        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                int trycount = 0;
                while (uploadDones!=mSelectPath.size() && trycount<=30 ){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    trycount++;
                }
                if(uploadDones!=mSelectPath.size() || trycount>30){
                    return false;
                }else{
                    //图片上传成功
                    //提交新贴
//                    图片路径不对，要转换成web地址，目前是本机手机地址
                    String result = HttpUtil.okhttpPost_insertBBSNew(title,typecode,content,fileNameList);
                    return result != null;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(aBoolean){
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "图片上传失败", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


    private void uploadBBSPhoto(ArrayList<String> fileNameList) {
        uploadDones = 0;
        //遍历所有已经选择的图片数组
        for (int i = 0; i < mSelectPath.size(); i++) {
            //设置每一个为正在上传
            mNameList.get(i).setIsupload(true);
            //得到每一个item
            final ProgressBar item = (ProgressBar) gv.getChildAt(i).findViewById(R.id.pb);
            item.setVisibility(View.VISIBLE);
            //每一个图片路径
            String filepath = mSelectPath.get(i);
            //每一个图片上传后的命名
            String filename = UUID.randomUUID().toString()+ filepath.substring(filepath.lastIndexOf("."));
            fileNameList.add(filename);
            //这个是ui线程回调，可直接操作UI
            UIProgressListener uiProgressRequestListener = new UIProgressListener() {
                @Override
                public void onUIProgress(long bytesWrite, long contentLength, boolean done) {
//                        System.out.println("bytesWrite:" + bytesWrite);
//                        System.out.println("contentLength" + contentLength);
//                        System.out.println((100 * bytesWrite) / contentLength + " % done ");
//                        System.out.println("done:" + done);
//                        System.out.println("================================");
                    //ui层回调
                    item.setProgress((int) ((100 * bytesWrite) / contentLength));
                    if (done) {
                        uploadDones++;
                    }
                }

                @Override
                public void onUIStart(long bytesWrite, long contentLength, boolean done) {
                    super.onUIStart(bytesWrite, contentLength, done);
                    //Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUIFinish(long bytesWrite, long contentLength, boolean done) {
                    super.onUIFinish(bytesWrite, contentLength, done);
                    //Toast.makeText(getApplicationContext(), "end", Toast.LENGTH_SHORT).show();
                }
            };
            //上传
            HttpUtil.okhttpPost_uploadFile(filepath,filename, uiProgressRequestListener, Const.UPLOAD_TYPE_WEBIMAGE,new MyCallBack());
        }
    }

    private class MyCallBack implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        }
    }

    private String getTypeCode(String type) {
        for (CodeBean codeBean : mTypeList) {
            if (codeBean.getValue().equals(type)) {
                return codeBean.getKey();
            }
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                //清空mNameList
                if (mNameList.size() > 0) {
                    for (int i = 0; i < mNameList.size(); i++) {
                        mNameList.remove(i);
                    }
                }
                mNameList.clear();
                adapter.notifyDataSetChanged();

                //mSelectPath
                if (mSelectPath.size() > 0) {
                    for (int k = 0; k < mSelectPath.size(); k++) {
                        mSelectPath.remove(k);
                    }
                }
                mSelectPath.clear();

                // 获取返回的图片列表
                paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                for (String path : paths) {
                    boolean showcheck = false;
                    if (isLong) {
                        showcheck = true;
                    }
                    mNameList.add(new ImagePickBean(path, "", showcheck, false));
                    mSelectPath.add(path);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete_item:
                if (isLong) {
                    List<ImagePickBean> mDeleteList = new ArrayList<>();
                    for (int i = 0; i < mNameList.size(); i++) {
                        if (mNameList.get(i).isCheck()) {
                            mDeleteList.add(mNameList.get(i));
                            mSelectPath.remove(mNameList.get(i).getUrl());
                        }
                    }
                    mNameList.removeAll(mDeleteList);
                    adapter.notifyDataSetChanged();
                    if (mNameList.size() == 0) {
                        isLong = false;
                        invalidateOptionsMenu();
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        if (isLong) {
            inflater.inflate(R.menu.common_listitem_menu_delete, menu);
        } else {

        }
        return true;
    }


    @Override
    // 设置回退
    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && isLong)) {
            for (int i = 0; i < mNameList.size(); i++) {
                mNameList.get(i).setShowcheck(false);
                mNameList.get(i).setCheck(false);
            }
            adapter.notifyDataSetChanged();
            isLong = false;
            invalidateOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}
