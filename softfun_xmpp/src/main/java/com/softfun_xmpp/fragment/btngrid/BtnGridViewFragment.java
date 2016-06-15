package com.softfun_xmpp.fragment.btngrid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.softfun_xmpp.R;
import com.softfun_xmpp.activity.ExpertActivity;
import com.softfun_xmpp.adapter.GridAdapter;
import com.softfun_xmpp.bbs.BBSIndexActivity;
import com.softfun_xmpp.bbs.INFOActivity;
import com.softfun_xmpp.bean.FeatureBtnBean;
import com.softfun_xmpp.components.TagsGridView;

import java.util.ArrayList;
import java.util.List;

public class BtnGridViewFragment extends Fragment {

    private View view;
    private TagsGridView gv;

    private List<FeatureBtnBean> mList;
    private GridAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_btn, container, false);
            gv = (TagsGridView) view.findViewById(R.id.gv);
            initData();
        }
        return view;
    }

    private void initData() {
        mList = new ArrayList<>();
        mList.add(new FeatureBtnBean("我的专家库", R.drawable.nj110_ic_1,"99"));
        mList.add(new FeatureBtnBean("柑桔产业转型发展", R.drawable.nj110_ic_13,"11"));
        mList.add(new FeatureBtnBean("农技论坛", R.drawable.nj110_ic_3,"97"));
        mList.add(new FeatureBtnBean("农业动态", R.drawable.nj110_ic_4,"02"));
        mList.add(new FeatureBtnBean("农事提醒", R.drawable.nj110_ic_5,"03"));
        mList.add(new FeatureBtnBean("市场分析", R.drawable.nj110_ic_6,"04"));
        mList.add(new FeatureBtnBean("新品种技术", R.drawable.nj110_ic_7,"06"));
        mList.add(new FeatureBtnBean("农资商家", R.drawable.nj110_ic_8,"05"));
        mList.add(new FeatureBtnBean("农产品供求", R.drawable.nj110_ic_9,"07"));
        mList.add(new FeatureBtnBean("休闲农业", R.drawable.nj110_ic_10,"10"));
        mList.add(new FeatureBtnBean("名优特产", R.drawable.nj110_ic_11,"08"));
        mList.add(new FeatureBtnBean("便民信息", R.drawable.nj110_ic_12,"09"));
        mList.add(new FeatureBtnBean("我的视频", R.drawable.nj110_ic_2,"98"));



        adapter = new GridAdapter(getContext(), mList);
        gv.setAdapter(adapter);
        //添加点击事件
        gv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent();
                        switch (mList.get(position).getBtcode()) {
                            case "99":
                                intent.setClass(getContext(), ExpertActivity.class);
                                startActivity(intent);
                                break;
                            case "98":
                                Toast.makeText(getContext(), "你选择了：" + mList.get(position).getBtnname()+"，即将上线", Toast.LENGTH_SHORT).show();
                                break;
                            case "97":
                                intent.setClass(getContext(), BBSIndexActivity.class);
                                intent.putExtra("title",mList.get(position).getBtnname());
                                startActivity(intent);
                                break;
                            case "02":
                                //http://111.1.62.169:7001/nj110/queryInfoList.do?type=03
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "03":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "04":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "06":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "05":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "07":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "10":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "08":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "09":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                            case "11":
                                intent.setClass(getContext(), INFOActivity.class);
                                intent.putExtra("title", mList.get(position).getBtnname());
                                intent.putExtra("code",mList.get(position).getBtcode());
                                startActivity(intent);
                                break;
                        }
                    }
                }
        );
    }
}
