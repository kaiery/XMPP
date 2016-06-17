package com.softfun.fragment.btngrid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.softfun.R;
import com.softfun.activity.MainActivity;
import com.softfun.bean.PackageBean;
import com.softfun.bean.UpdateBean;
import com.softfun.components.TagsGridView;
import com.softfun.constant.Const;
import com.softfun.network.DownloadAPK;
import com.softfun.network.HttpUtil;
import com.softfun.utils.ImageLoaderUtils;
import com.softfun.utils.SpUtils;
import com.softfun.utils.ThreadUtils;
import com.softfun.utils.ToastUtils;
import com.softfun.utils.ToolsUtil;

import java.util.List;

public class BtnGridViewFragment extends Fragment {

    private View view;
    private TagsGridView mGv;

    private List<PackageBean> mList;
    private MyGridAdapter adapter;
    private int mScreenWidth;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_btn, container, false);
            mGv = (TagsGridView) view.findViewById(R.id.gv);
            initData();
        }
        return view;
    }

    private void initData() {

        //得到窗口管理器
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        //得到显示指标
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        //获得屏幕宽度
        mScreenWidth = dm.widthPixels;

        mList = ((MainActivity) getActivity()).mAppPackageList;
        adapter = new MyGridAdapter();
        mGv.setAdapter(adapter);
    }


    private class MyGridAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;

        public MyGridAdapter() {
            mInflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewTag viewTag;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_gridview_image, null);
                viewTag = new ItemViewTag((ImageView) convertView.findViewById(R.id.ItemImage), (TextView) convertView.findViewById(R.id.ItemText));
                convertView.setTag(viewTag);
            } else {
                viewTag = (ItemViewTag) convertView.getTag();
            }

            viewTag.mName.setText(mList.get(position).getAppname());

            ViewGroup.LayoutParams layoutParams = viewTag.mIcon.getLayoutParams();
            layoutParams.width = mScreenWidth / 3;
            layoutParams.height = mScreenWidth / 3;
            viewTag.mIcon.setLayoutParams(layoutParams);

            String packagecode = mList.get(position).getPackagecode();
            String suffix = packagecode.substring(packagecode.lastIndexOf("_") + 1);
            int drawId = getContext().getResources().getIdentifier("flat_" + suffix, "drawable", getContext().getPackageName());
            int drawSelectId = getContext().getResources().getIdentifier("flat_" + suffix + "_selected", "drawable", getContext().getPackageName());
            ImageLoader.getInstance().displayImage("drawable://" + drawId, viewTag.mIcon, ImageLoaderUtils.getOptions_NoCacheInMem_NoCacheInDisk_NoExif());
            viewTag.mIcon.setOnTouchListener( new ImageTouchListener( viewTag.mIcon, drawId, drawSelectId,packagecode ) );
            return convertView;
        }

        class ItemViewTag {
            protected ImageView mIcon;
            protected TextView mName;

            public ItemViewTag(ImageView icon, TextView name) {
                this.mName = name;
                this.mIcon = icon;
            }
        }
    }


    private class ImageTouchListener implements View.OnTouchListener {
        private final ImageView mView;
        private final int drawable;
        private final int drawableSelected;
        private String mPackageName;
        private String mPackageNameSuffix;

        public ImageTouchListener(ImageView view, int drawId, int drawSelectId, String packagecode) {
            this.mView = view;
            drawable = drawId;
            drawableSelected = drawSelectId;
            mPackageName = packagecode;//"com.softfun_xmpp";
            mPackageNameSuffix = packagecode.substring(packagecode.lastIndexOf("_")+1); //"xmpp"
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ImageLoader.getInstance().displayImage("drawable://" + drawableSelected, mView, ImageLoaderUtils.getOptions_NoCacheInMem_NoCacheInDisk_NoExif());
                    break;
                case MotionEvent.ACTION_UP:
                    ImageLoader.getInstance().displayImage("drawable://" + drawable, mView, ImageLoaderUtils.getOptions_NoCacheInMem_NoCacheInDisk_NoExif());

                    if(!mPackageNameSuffix.equals("more")){
                        try {

                            String username = SpUtils.get(Const.USERNAME, "") + "";
                            String password = SpUtils.get(Const.PASSWORD, "") + "";
                            String HashData = ToolsUtil.HashData(username,password);
                            System.out.println("====================  HashData  ====================="+HashData);
                            Intent intent = new Intent();
                            String action = mPackageName + ".action";
                            intent.setAction(action);
                            // 指定数据格式
                            String uri = "softfun://" + mPackageName + ":1702/" + mPackageNameSuffix+"/"+HashData ;
                            intent.setData(Uri.parse(uri));
                            // 3.指定清单文件中声明的类别
                            intent.addCategory("android.intent.category.DEFAULT");
                            // 4.开启界面
                            startActivity(intent);
                        } catch (Exception e) {
                            //提示用户下载
                            promptUser(mPackageNameSuffix);
                        }
                    }
                    break;
            }
            return true;
        }
    }

    /**
     * 提示用户下载
     * @param suffix
     */
    private void promptUser(final String suffix) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                final UpdateBean updateBean = HttpUtil.okhttpPost_queryAppModule(suffix);
                if(updateBean!=null){
                    ThreadUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("提示");
                            builder.setMessage("应用组件不存在，您需要立即下载吗？");
                            builder.setPositiveButton("下载组件", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //下载升级文件
                                    new DownloadAPK(updateBean.getFilesize()).execute(updateBean.getDownloadurl());
                                }
                            });
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //取消
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    });
                }else{
                    ToastUtils.showToastSafe("组件不存在，即将上线。");
                }
            }
        });
    }
}
