package kaiery.csrs.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import kaiery.csrs.R;
import kaiery.csrs.activity.MainActivity;
import kaiery.csrs.adapter.ModuleRecyclerAdapter;
import kaiery.csrs.beans.ModuleBean;

/**
 *
 */
public class MainGoogleFragment extends Fragment {
    private static final String INDEX = "index";
    private static final String MODULE_LIST = "module_list";

    private FrameLayout mFragmentContainer;
    private RecyclerView mRecyclerview;


    private LayoutInflater mLayoutInflater;
    private List<ModuleBean> mList = new ArrayList<>();
    private ModuleRecyclerAdapter mAdapter;

    /**
     * 创建一个新的实例的片段
     */
    public static MainGoogleFragment newInstance(int index,List<ModuleBean> list) {
        MainGoogleFragment fragment = new MainGoogleFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        bundle.putSerializable(MODULE_LIST, (Serializable) list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int index = getArguments().getInt(INDEX, 0);
        Serializable serializable = getArguments().getSerializable(MODULE_LIST);
        if(serializable!=null){
            mList = (List<ModuleBean>) serializable;
        }

        View view = null;
        switch (index) {
            case 0: {
                view = inflater.inflate(R.layout.fragment_module,container,false);
                initModule(view);
                break;
            }
            case 1: {

                break;
            }
            case 2: {
                view = inflater.inflate(R.layout.fragment_demo_settings, container, false);
                initDemoSettings(view);
                break;
            }
            case 3: {

                break;
            }
            case 4: {

                break;
            }
        }
        return view;
    }

    private void initModule(View view) {
        mFragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        mRecyclerview = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerview.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),4); //网格中的列数
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int type = mRecyclerview.getAdapter().getItemViewType(position);
                if(type== ModuleBean.TYPE_HEADER){
                    return gridLayoutManager.getSpanCount(); //就是网格中的列数
                }else{
                    return 1;
                }
            }
        });






        mRecyclerview.setLayoutManager(gridLayoutManager);
        mAdapter = new ModuleRecyclerAdapter(getContext());
        mRecyclerview.setAdapter(mAdapter);
        mAdapter.addList(mList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Init demo settings
     */
    private void initDemoSettings(View view) {

        final MainActivity mainActivity = (MainActivity) getActivity();
        final SwitchCompat switchColored = (SwitchCompat) view.findViewById(R.id.fragment_demo_switch_colored);
        final SwitchCompat switchFiveItems = (SwitchCompat) view.findViewById(R.id.fragment_demo_switch_five_items);
        final SwitchCompat showHideBottomNavigation = (SwitchCompat) view.findViewById(R.id.fragment_demo_show_hide);
        final SwitchCompat showSelectedBackground = (SwitchCompat) view.findViewById(R.id.fragment_demo_selected_background);
        final SwitchCompat switchForceTitleHide = (SwitchCompat) view.findViewById(R.id.fragment_demo_force_title_hide);

        switchColored.setChecked(mainActivity.isBottomNavigationColored());
        switchFiveItems.setChecked(mainActivity.getBottomNavigationNbItems() == 5);

        switchColored.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.updateBottomNavigationColor(isChecked);
            }
        });
        switchFiveItems.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.updateBottomNavigationItems(isChecked);
            }
        });
        showHideBottomNavigation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.showOrHideBottomNavigation(isChecked);
            }
        });
        showSelectedBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.updateSelectedBackgroundVisibility(isChecked);
            }
        });
        switchForceTitleHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.setForceTitleHide(isChecked);
            }
        });
    }


    /**
     * 刷新列表：平滑滚动到0位置
     */
    public void refresh() {
        if (getArguments().getInt("index", 0) > 0 && mRecyclerview != null) {
            mRecyclerview.smoothScrollToPosition(0);
        }
    }

    /**
     * fragment渐显
     */
    public void willBeDisplayed() {
        if (mFragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_navigation_fade_in);
            mFragmentContainer.startAnimation(fadeIn);
        }
    }

    /**
     * fragment渐隐
     */
    public void willBeHidden() {
        if (mFragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_navigation_fade_out);
            mFragmentContainer.startAnimation(fadeOut);
        }
    }
}
