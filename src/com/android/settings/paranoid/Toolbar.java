/*
 * Copyright (C) 2012 ParanoidAndroid Project
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

package com.android.settings.paranoid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class Toolbar extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_QUICK_PULL_DOWN = "quick_pulldown";
    private static final String STATUS_BAR_MAX_NOTIF = "status_bar_max_notifications";
    private static final String NAV_BAR_TABUI_MENU = "nav_bar_tabui_menu";
    private static final String STATUS_BAR_DONOTDISTURB = "status_bar_donotdisturb";
    private static final String NAV_BAR_CATEGORY = "toolbar_navigation";
    private static final String NAV_BAR_CONTROLS = "navigation_bar_controls";
    
    private ListPreference mStatusBarMaxNotif;
    private CheckBoxPreference mQuickPullDown;
    private CheckBoxPreference mMenuButtonShow;
    private CheckBoxPreference mStatusBarDoNotDisturb;
    private PreferenceScreen mNavigationBarControls;
    private PreferenceCategory mNavigationCategory;

    private Context mContext;
    private int mAllowedLocations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.tool_bar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        mQuickPullDown = (CheckBoxPreference) prefSet.findPreference(KEY_QUICK_PULL_DOWN);
        mQuickPullDown.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_QUICK_PULLDOWN, 0) == 1);
        
        mStatusBarMaxNotif = (ListPreference) prefSet.findPreference(STATUS_BAR_MAX_NOTIF);
        int maxNotIcons = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.MAX_NOTIFICATION_ICONS, 2);
        mStatusBarMaxNotif.setValue(String.valueOf(maxNotIcons));
        mStatusBarMaxNotif.setOnPreferenceChangeListener(this);

        mNavigationCategory = (PreferenceCategory) prefSet.findPreference(NAV_BAR_CATEGORY);

        mMenuButtonShow = (CheckBoxPreference) prefSet.findPreference(NAV_BAR_TABUI_MENU);
        mMenuButtonShow.setChecked((Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.NAV_BAR_TABUI_MENU, 0) == 1));

        mNavigationBarControls = (PreferenceScreen) prefSet.findPreference(NAV_BAR_CONTROLS);

        if (!Utils.isTablet()) {
            prefSet.removePreference(mStatusBarMaxNotif);
            prefSet.removePreference(mMenuButtonShow);
            prefSet.removePreference(mStatusBarDoNotDisturb);
            prefSet.removePreference(mQuickPullDown);
        } 
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mMenuButtonShow) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.NAV_BAR_TABUI_MENU, mMenuButtonShow.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuickPullDown) {	
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.QS_QUICK_PULLDOWN,	mQuickPullDown.isChecked()
                    ? 1 : 0);
        } else if (preference == mStatusBarDoNotDisturb) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DONOTDISTURB,
                    mStatusBarDoNotDisturb.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarMaxNotif) {
            int maxNotIcons = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MAX_NOTIFICATION_ICONS, maxNotIcons);
            return true;
        }
        return false;
    }
}
