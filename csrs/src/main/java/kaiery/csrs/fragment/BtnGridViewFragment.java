package kaiery.csrs.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import kaiery.csrs.R;
import kaiery.csrs.adapter.GridAdapter;
import kaiery.csrs.beans.FeatureBtnBean;
import kaiery.csrs.components.TagsGridView;

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
        mList.add(new FeatureBtnBean("我的专家库", R.drawable.inner_image,"99"));
        mList.add(new FeatureBtnBean("柑桔产业转型发展", R.drawable.inner_image,"11"));
        mList.add(new FeatureBtnBean("农技论坛", R.drawable.inner_image,"97"));
        mList.add(new FeatureBtnBean("农业动态", R.drawable.inner_image,"02"));
        mList.add(new FeatureBtnBean("农事提醒", R.drawable.inner_image,"03"));
        mList.add(new FeatureBtnBean("市场分析", R.drawable.inner_image,"04"));
        mList.add(new FeatureBtnBean("新品种技术", R.drawable.inner_image,"06"));
        mList.add(new FeatureBtnBean("农资商家", R.drawable.inner_image,"05"));
        mList.add(new FeatureBtnBean("农产品供求", R.drawable.inner_image,"07"));
        mList.add(new FeatureBtnBean("休闲农业", R.drawable.inner_image,"10"));
        mList.add(new FeatureBtnBean("名优特产", R.drawable.inner_image,"08"));
        mList.add(new FeatureBtnBean("便民信息", R.drawable.inner_image,"09"));



        adapter = new GridAdapter(getContext(), mList);
        gv.setAdapter(adapter);
        //添加点击事件
        gv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        );
    }
}
