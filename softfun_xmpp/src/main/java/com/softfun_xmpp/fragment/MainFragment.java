package com.softfun_xmpp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.softfun_xmpp.R;
import com.softfun_xmpp.utils.ToolBarUtils;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private View view;


    private ViewPager vp;
    private LinearLayout tabBottom;
    private List<Fragment> mFragments = new ArrayList<>();
    private ToolBarUtils toolBarUtils;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(view==null){
            view = inflater.inflate(R.layout.activity_main, container, false);
            vp = (ViewPager) view.findViewById(R.id.vp);
            tabBottom = (LinearLayout) view.findViewById(R.id.tab_bottom);
            initData();
        }
        return view;
    }



    private void initData() {
        //viewpager -->view -->pagerAdapet
        //viewpager-->fragment -->fragmentPagerAdapet -->fragment数量很少*****
        //viewpager-->fragment -->fragmentStatePagerAdapet
        //添加Fragment到集合中
        mFragments.add(new SessionFragment());
        mFragments.add(new ContactFragment());
        mFragments.add(new GroupFragment());
        vp.setOffscreenPageLimit(mFragments.size()-1);
        vp.setAdapter(new MyPagerAdapet(getActivity().getSupportFragmentManager()));
        vp.addOnPageChangeListener(new MyPagerChangerLinstener());

        String[] toolbarTitles = {"首页","联系人","群组"};
        int[] toolbarIcon = {R.drawable.tab_messages,R.drawable.tab_friends,R.drawable.tab_groups};
        //底部按钮栏
        toolBarUtils = new ToolBarUtils();
        //创建
        toolBarUtils.createToolBar(tabBottom,toolbarTitles,toolbarIcon);
        //接口回调点击事件
        toolBarUtils.setmOnToolBarClickListener(new ToolBarUtils.OnToolBarClickListener() {
            @Override
            public void onToolBarClick(int position) {
                vp.setCurrentItem(position);
            }
        });
        //初始化第一个
        toolBarUtils.changeColor(0);
        vp.setCurrentItem(0);
    }

    /**
     * 适配器：FragmentPagerAdapter
     */
    private class MyPagerAdapet extends FragmentPagerAdapter {
        public MyPagerAdapet(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return 3;//3个fragment
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }
    }

    /**
     * 翻页监听器
     */
    private class MyPagerChangerLinstener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            toolBarUtils.changeColor(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }


}
