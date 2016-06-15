package com.softfun_xmpp.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;


public class ToolBarUtils {

    private List<LinearLayout> mList = new ArrayList<>();

    public void createToolBar(LinearLayout layout, String[] toolbarTitles, int[] toolbarIcon){
        for (int i = 0; i < toolbarTitles.length; i++) {
            LinearLayout linearLayout_view = (LinearLayout) View.inflate(layout.getContext(), R.layout.inflate_tab_toolbar, null);
            ImageView iv = (ImageView) linearLayout_view.findViewById(R.id.iv_tab_toolbar);
            TextView tv = (TextView) linearLayout_view.findViewById(R.id.tv_tab_toolbar);
            iv.setImageResource(toolbarIcon[i]);
            tv.setText(toolbarTitles[i]);
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            int width = 0;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
            params.weight = 1;
            layout.addView(linearLayout_view,params);
            mList.add(linearLayout_view);

            //设置点击事件
            final int finalI = i;
            linearLayout_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //3\接口回调
                    mOnToolBarClickListener.onToolBarClick(finalI);
                }
            });
        }
    }

    /**
     * 改变状态
     * @param position
     */
    public void changeColor(int position){
        for (LinearLayout ll : mList) {
            ImageView iv = (ImageView) ll.findViewById(R.id.iv_tab_toolbar);
            TextView tv = (TextView) ll.findViewById(R.id.tv_tab_toolbar);
            iv.setSelected(false);
            tv.setTextColor(GlobalContext.getInstance().getResources().getColor(R.color.colorTabDisable));
        }
        ImageView iv = (ImageView) mList.get(position).findViewById(R.id.iv_tab_toolbar);
        TextView tv = (TextView) mList.get(position).findViewById(R.id.tv_tab_toolbar);
        iv.setSelected(true);
        tv.setTextColor(GlobalContext.getInstance().getResources().getColor(R.color.colorTabActive));
    }


    /**
     * 1\创建接口和接口方法
     */
    public interface OnToolBarClickListener{
        void onToolBarClick(int position);
    }
    /**
     *2\定义接口变量
     */
    OnToolBarClickListener mOnToolBarClickListener;

    /**
     * 4\暴露公开方法（set即可）
     */
    public void setmOnToolBarClickListener(OnToolBarClickListener mOnToolBarClickListener) {
        this.mOnToolBarClickListener = mOnToolBarClickListener;
    }
}
