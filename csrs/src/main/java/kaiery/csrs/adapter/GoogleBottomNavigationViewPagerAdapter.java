package kaiery.csrs.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import kaiery.csrs.beans.ModuleBean;
import kaiery.csrs.fragment.MainGoogleFragment;

/**
 *
 */
public class GoogleBottomNavigationViewPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<MainGoogleFragment> fragments = new ArrayList<>();
	private MainGoogleFragment currentFragment;

	/**
	 * 实例化
	 * @param fm
     */
	public GoogleBottomNavigationViewPagerAdapter(FragmentManager fm, List<ModuleBean> list) {
		super(fm);
		//实例化各Fragment
		fragments.clear();
		fragments.add(MainGoogleFragment.newInstance(0,list));
		fragments.add(MainGoogleFragment.newInstance(1,null));
		fragments.add(MainGoogleFragment.newInstance(2,null));
		fragments.add(MainGoogleFragment.newInstance(3,null));
		fragments.add(MainGoogleFragment.newInstance(4,null));
	}


	@Override
	public MainGoogleFragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (getCurrentFragment() != object) {
			currentFragment = ((MainGoogleFragment) object);
		}
		super.setPrimaryItem(container, position, object);
	}

	/**
	 * 获取当前Fragment
	 */
	public MainGoogleFragment getCurrentFragment() {
		return currentFragment;
	}
}