package com.android.settings.pac.navbar;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;

public class NavbarTabHostFragment extends Fragment implements
        ActionBar.TabListener {

    private static ViewPager mViewPager;

    private static final Integer[] tabNames = new Integer[] {
            R.string.navbar_tab_arrange, R.string.navbar_tab_settings };

    private static final Fragment[] fragments = new Fragment[] {
            new ArrangeNavbarFragment(), new NavbarSettingsFragment() };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View main = inflater.inflate(R.layout.fragment_navbar_tabhost,
                container, false);

        mViewPager = (ViewPager) main.findViewById(R.id.viewpager);

        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(
                getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        if (container instanceof PreferenceFrameLayout) ((PreferenceFrameLayout.LayoutParams) main
                .getLayoutParams()).removeBorders = true;
        return main;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(tabNames[position]);
        }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {}
}
