package kaiery.csrs.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kaiery.csrs.R;

/**
 * 联系人的Fragment
 * <p/>
 * 所有联系人：Roster
 * 联系人组：RosterGroup
 * 单个联系人：RosterEntry
 */
public class ContactFragment extends Fragment  {

    private View mFragmentView;



    public ContactFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mFragmentView) {
            ((ViewGroup) mFragmentView.getParent()).removeView(mFragmentView);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mFragmentView) {
            mFragmentView = inflater.inflate(R.layout.fragment_contact, container, false);
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
     * 初始化
     */
    private void init() {
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    private void initView(View view) {
    }

    /**
     * 初始化数据
     * 得到所有联系人
     */
    private void initData() {

    }

















    /**
     * 初始化监听器
     */
    private void initListener() {


    }













































}
