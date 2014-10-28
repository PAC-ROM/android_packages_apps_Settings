/*
 * Copyright (C) 2014 Pac-Roms
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

package com.android.settings.pac;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.pac.util.PacSettingCheckBoxPreference;
import com.android.settings.pac.util.SeekBarPreferenceCHOS;

public class TintedStatusbar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "TintedStatusbarSettings";

    private static final String TINTED_STATUSBAR = "tinted_statusbar";
    private static final String TINTED_STATUSBAR_OPTION = "tinted_statusbar_option";
    private static final String TINTED_STATUSBAR_FILTER = "status_bar_tinted_filter";
    private static final String TINTED_STATUSBAR_TRANSPARENT = "tinted_statusbar_transparent";
    private static final String TINTED_NAVBAR_TRANSPARENT = "tinted_navbar_transparent";
    private static final String TINTED_FULL_MODE = "status_bar_tinted_full_mode";
    private static final String CATEGORY_TINTED = "category_tinted_statusbar";

    private ListPreference mTintedStatusbar;
    private ListPreference mTintedStatusbarOption;
    private CheckBoxPreference mTintedStatusbarFilter;
    private CheckBoxPreference mTintedFullMode;
    private SeekBarPreferenceCHOS mTintedStatusbarTransparency;
    private SeekBarPreferenceCHOS mTintedNavbarTransparency;

    private CharSequence mPreviousTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tsb_control);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        final PreferenceCategory tintedCategory =
                     (PreferenceCategory) prefSet.findPreference(CATEGORY_TINTED);

        int tintedStatusbar = getTintedStatusbarColor(resolver);

        mTintedStatusbar = (ListPreference) findPreference(TINTED_STATUSBAR);
        mTintedStatusbar.setValue(String.valueOf(tintedStatusbar));
        mTintedStatusbar.setSummary(mTintedStatusbar.getEntry());
        mTintedStatusbar.setOnPreferenceChangeListener(this);

        mTintedStatusbarFilter = (CheckBoxPreference) findPreference(TINTED_STATUSBAR_FILTER);
        mTintedStatusbarFilter.setEnabled(tintedStatusbar != 0);

        int tintedStatusbarOption = getTintedStatusbarOption(resolver);

        mTintedFullMode = (CheckBoxPreference) findPreference(TINTED_FULL_MODE);
        mTintedFullMode.setEnabled((tintedStatusbar == 2) &&
                       (tintedStatusbarOption == 0 || tintedStatusbarOption == 2));

        mTintedStatusbarTransparency = (SeekBarPreferenceCHOS) findPreference(TINTED_STATUSBAR_TRANSPARENT);
        mTintedStatusbarTransparency.setValue(Settings.PAC.getInt(resolver,
                Settings.PAC.STATUS_BAR_TINTED_STATBAR_TRANSPARENT, 100));
        mTintedStatusbarTransparency.setEnabled(tintedStatusbar != 0);
        mTintedStatusbarTransparency.setOnPreferenceChangeListener(this);

        mTintedStatusbarOption = (ListPreference) findPreference(TINTED_STATUSBAR_OPTION);
        mTintedNavbarTransparency = (SeekBarPreferenceCHOS) findPreference(TINTED_NAVBAR_TRANSPARENT);

        boolean hasNavBarByDefault = getResources().getBoolean(
                  com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.PAC.getInt(getContentResolver(),
                  Settings.PAC.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1;

        // Hide navigation bar category on devices without navigation bar
        if (!hasNavBarByDefault && !enableNavigationBar) {
            tintedCategory.removePreference(mTintedStatusbarOption);
            tintedCategory.removePreference(mTintedNavbarTransparency);
        } else {
            mTintedStatusbarOption.setValue(String.valueOf(tintedStatusbarOption));
            mTintedStatusbarOption.setSummary(mTintedStatusbarOption.getEntry());
            mTintedStatusbarOption.setEnabled(tintedStatusbar != 0);
            mTintedStatusbarOption.setOnPreferenceChangeListener(this);

            mTintedNavbarTransparency.setValue(Settings.PAC.getInt(resolver,
                    Settings.PAC.STATUS_BAR_TINTED_NAVBAR_TRANSPARENT, 100));
            mTintedNavbarTransparency.setEnabled(tintedStatusbar != 0);
            mTintedNavbarTransparency.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mTintedStatusbar) {
            int val = Integer.parseInt((String) newValue);
            int index = mTintedStatusbar.findIndexOfValue((String) newValue);
            Settings.PAC.putInt(resolver,
                Settings.PAC.STATUS_BAR_TINTED_COLOR, val);
            mTintedStatusbar.setSummary(mTintedStatusbar.getEntries()[index]);
            if (mTintedStatusbarOption != null) {
                mTintedStatusbarOption.setEnabled(val != 0);
            }
            mTintedStatusbarFilter.setEnabled(val != 0);
            mTintedFullMode.setEnabled((val == 2) &&
                             ((getTintedStatusbarOption(resolver) == 0) ||
                              (getTintedStatusbarOption(resolver) == 2)));
            mTintedStatusbarTransparency.setEnabled(val != 0);
            if (mTintedNavbarTransparency != null) {
                mTintedNavbarTransparency.setEnabled(val != 0);
            }
        } else if (preference == mTintedStatusbarOption) {
            int val = Integer.parseInt((String) newValue);
            int index = mTintedStatusbarOption.findIndexOfValue((String) newValue);
            Settings.PAC.putInt(resolver,
                Settings.PAC.STATUS_BAR_TINTED_OPTION, val);
            mTintedStatusbarOption.setSummary(mTintedStatusbarOption.getEntries()[index]);
            mTintedFullMode.setEnabled((getTintedStatusbarColor(resolver) == 2) &&
                             ((val == 0) || (val == 2)));
        } else if (preference == mTintedStatusbarTransparency) {
            int val = ((Integer)newValue).intValue();
            Settings.PAC.putInt(resolver,
                    Settings.PAC.STATUS_BAR_TINTED_STATBAR_TRANSPARENT, val);
            return true;
        } else if (preference == mTintedNavbarTransparency) {
            int val = ((Integer)newValue).intValue();
            Settings.PAC.putInt(resolver,
                    Settings.PAC.STATUS_BAR_TINTED_NAVBAR_TRANSPARENT, val);
            return true;
        } else {
            return false;
        }
        return true;
    }

    private int getTintedStatusbarColor(ContentResolver resolver) {
        int tintedStatusbar = Settings.PAC.getInt(resolver,
                    Settings.PAC.STATUS_BAR_TINTED_COLOR, 0);
        return tintedStatusbar;
    }

    private int getTintedStatusbarOption(ContentResolver resolver) {
        int tintedStatusbarOption = Settings.PAC.getInt(resolver,
                    Settings.PAC.STATUS_BAR_TINTED_OPTION, 0);
        return tintedStatusbarOption;
    }


}
