/*
 * Copyright (C) 2013 SlimRoms Project
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

package com.android.settings.slim;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.slim.DeviceUtils;

public class RecentsPanel extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "RecentsPanelSettings";

    private static final String RECENT_MENU_CLEAR_ALL = "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "recent_menu_clear_all_location";

    private CheckBoxPreference mRecentClearAll;
    private ListPreference mRecentClearAllPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.recents_apps_panel);

        PreferenceScreen prefSet = getPreferenceScreen();

        mRecentClearAll = (CheckBoxPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL);
        mRecentClearAll.setChecked(Settings.System.getInt(getContentResolver(),
            Settings.System.SHOW_CLEAR_RECENTS_BUTTON, 1) == 1);
        mRecentClearAll.setOnPreferenceChangeListener(this);
        mRecentClearAllPosition = (ListPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        String recentClearAllPosition = Settings.System.getString(getContentResolver(), Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
             mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRecentClearAll) {
            Settings.System.putInt(getContentResolver(), Settings.System.SHOW_CLEAR_RECENTS_BUTTON,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mRecentClearAllPosition) {
            Settings.System.putString(getContentResolver(), Settings.System.CLEAR_RECENTS_BUTTON_LOCATION, (String) newValue);
            return true;
        } else {
        return false;
        }
    }
}
