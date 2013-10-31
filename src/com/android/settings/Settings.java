/*
 * Copyright (C) 2013 PAC-man ROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.util.ExtendedPropertiesUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Settings extends Activity {

    private static final String SHAREDPREF_SETTINGS_SPLITUP = "settings_splitup";
    private static final String[] titles = { "System", "PAC-Man" };
    private LayoutInflater mInflater;
    private List<View> listViews;
    private LocalActivityManager localManager;
    private PagerTabStrip mTabStrip;
    private TextView pac_tab, system_tab;
    private ViewPager mPager;
    private ViewPagerAdapter mPagerAdapter;
    private int bmpW;
    private int currIndex = 0;
    private int mLayout = 0;
    private int offset = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences SETTINGS_SPLITUP = getSharedPreferences(SHAREDPREF_SETTINGS_SPLITUP, 0);
        int settings_mode = SETTINGS_SPLITUP.getInt("settings_splitup", 1);

        mLayout = ExtendedPropertiesUtils.getActualProperty("com.android.settings.layout");
        if (mLayout == 720) {
            Intent intent = new Intent(this, MainSetting.class);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        } else if (settings_mode == 0) {
        	Intent mTabs = new Intent(this, SettingsTabs.class);
        	startActivity(mTabs);
        	finish();
        } else if (settings_mode == 1) {
        	setContentView(R.layout.mainsetting);
            localManager = new LocalActivityManager(this, true);
            localManager.dispatchCreate(savedInstanceState);

            InitViewPager();
        }
    }

    private void InitViewPager() {
        mInflater = getLayoutInflater();
        mPager = (ViewPager) findViewById(R.id.viewPager);
        listViews = new ArrayList<View>();
        Intent SystemSettingsIntent = new Intent(this, MainSetting.class);
        listViews.add(localManager.startActivity("SystemSettings",
                SystemSettingsIntent).getDecorView());
        Intent PacSettingsIntent = new Intent(this, PacSettings.class);
        listViews.add(localManager.startActivity("PacSettings",
                PacSettingsIntent).getDecorView());
        mPagerAdapter = new ViewPagerAdapter(listViews);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        mTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        mTabStrip.setTabIndicatorColorResource(android.R.color.holo_blue_light);
    }

    public class ViewPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public ViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    };

    public class MyOnPageChangeListener implements OnPageChangeListener {

        int one = offset + bmpW;
        int two = one;

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(one, 0, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, 0, 0, 0);
                    }
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, one, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                    }
                    break;
            }
            currIndex = arg0;
            animation.setFillAfter(true);
            animation.setDuration(300);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    /*
     * Settings subclasses for launching independently.
     */
    public static class BluetoothSettingsActivity extends MainSetting { /* empty */ }
    public static class WirelessSettingsActivity extends MainSetting { /* empty */ }
    public static class TetherSettingsActivity extends MainSetting { /* empty */ }
    public static class VpnSettingsActivity extends MainSetting { /* empty */ }
    public static class DateTimeSettingsActivity extends MainSetting { /* empty */ }
    public static class StorageSettingsActivity extends MainSetting { /* empty */ }
    public static class WifiSettingsActivity extends MainSetting { /* empty */ }
    public static class WifiP2pSettingsActivity extends MainSetting { /* empty */ }
    public static class InputMethodAndLanguageSettingsActivity extends MainSetting { /* empty */ }
    public static class KeyboardLayoutPickerActivity extends MainSetting { /* empty */ }
    public static class InputMethodAndSubtypeEnablerActivity extends MainSetting { /* empty */ }
    public static class SpellCheckersSettingsActivity extends MainSetting { /* empty */ }
    public static class LocalePickerActivity extends MainSetting { /* empty */ }
    public static class UserDictionarySettingsActivity extends MainSetting { /* empty */ }
    public static class SoundSettingsActivity extends MainSetting { /* empty */ }
    public static class DisplaySettingsActivity extends MainSetting { /* empty */ }
    public static class DeviceInfoSettingsActivity extends MainSetting { /* empty */ }
    public static class ApplicationSettingsActivity extends MainSetting { /* empty */ }
    public static class ManageApplicationsActivity extends MainSetting { /* empty */ }
    public static class AppOpsDetailsActivity extends MainSetting { /* empty */ }
    public static class AppOpsSummaryActivity extends MainSetting { /* empty */ }
    public static class StorageUseActivity extends MainSetting { /* empty */ }
    public static class DevelopmentSettingsActivity extends MainSetting { /* empty */ }
    public static class AccessibilitySettingsActivity extends MainSetting { /* empty */ }
    public static class SecuritySettingsActivity extends MainSetting { /* empty */ }
    public static class LocationSettingsActivity extends MainSetting { /* empty */ }
    public static class PrivacySettingsActivity extends MainSetting { /* empty */ }
    public static class RunningServicesActivity extends MainSetting { /* empty */ }
    public static class ManageAccountsSettingsActivity extends MainSetting { /* empty */ }
    public static class PowerUsageSummaryActivity extends MainSetting { /* empty */ }
    public static class AccountSyncSettingsActivity extends MainSetting { /* empty */ }
    public static class AccountSyncSettingsInAddAccountActivity extends MainSetting { /* empty */ }
    public static class CryptKeeperSettingsActivity extends MainSetting { /* empty */ }
    public static class DeviceAdminSettingsActivity extends MainSetting { /* empty */ }
    public static class DataUsageSummaryActivity extends MainSetting { /* empty */ }
    public static class AdvancedWifiSettingsActivity extends MainSetting { /* empty */ }
    public static class TextToSpeechSettingsActivity extends MainSetting { /* empty */ }
    public static class AndroidBeamSettingsActivity extends MainSetting { /* empty */ }
    public static class WifiDisplaySettingsActivity extends MainSetting { /* empty */ }
    public static class ApnSettingsActivity extends MainSetting { /* empty */ }
    public static class ApnEditorActivity extends MainSetting { /* empty */ }
    public static class DreamSettingsActivity extends MainSetting { /* empty */ }
    public static class NotificationShortcutsSettingsActivity extends MainSetting { /* empty */ }
    public static class QuietHoursSettingsActivity extends MainSetting { /* empty */ }
    public static class ProfilesSettingsActivity extends MainSetting { /* empty */ }
    public static class SystemSettingsActivity extends MainSetting { /* empty */ }
    public static class NotificationStationActivity extends MainSetting { /* empty */ }
    public static class UserSettingsActivity extends MainSetting { /* empty */ }
    public static class NotificationAccessSettingsActivity extends MainSetting { /* empty */ }
    public static class BlacklistSettingsActivity extends MainSetting { /* empty */ }
}
