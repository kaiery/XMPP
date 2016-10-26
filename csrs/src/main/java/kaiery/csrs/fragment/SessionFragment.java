package kaiery.csrs.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import kaiery.csrs.R;

/**
 * 会话Fragment
 */
public class SessionFragment extends Fragment {


    private ListView mLv;
    private View mFragmentView;

    public SessionFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    /**
     * Fragment中的布局被移除时调用
     */
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

    private void init() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == mFragmentView) {
            mFragmentView = inflater.inflate(R.layout.fragment_session, container, false);

            if (savedInstanceState == null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_autoviewpager, new AutoViewPagerFragment())
                        .add(R.id.fragment_btn, new BtnGridViewFragment())
                        .commit();
            }
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







    private void initView(View view) {
    }

    private void initData() {

    }






    private void initListener() {
    }

}
