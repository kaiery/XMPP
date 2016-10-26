package kaiery.csrs.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kaiery.csrs.R;

/**
 * 群组的Fragment
 */
public class GroupFragment extends Fragment{

    private View mFragmentView;


    public GroupFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mFragmentView) {
            mFragmentView = inflater.inflate(R.layout.fragment_group, container, false);
            initView(mFragmentView);
        }
        return mFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    /**
     *  初始服务等
     */
    private void init() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化组件
     * @param view
     */
    private void initView(View view) {
    }

    /**
     * 初始化监听
     */
    private void initListener() {
    }

    /**
     * 初始化数据
     */
    private void initData() {
    }










}
