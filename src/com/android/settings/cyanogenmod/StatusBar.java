/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.MSimTelephonyManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.pac.util.SeekBarPreferenceCHOS;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBar";

    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";

    private static final String STATUS_BAR_BATTERY_SHOW_PERCENT = "status_bar_battery_show_percent";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";

    private static final String STATUS_BAR_STYLE_HIDDEN = "4";
    private static final String STATUS_BAR_STYLE_TEXT = "6";

    private static final String TINTED_STATUSBAR = "tinted_statusbar";
    private static final String TINTED_STATUSBAR_OPTION = "tinted_statusbar_option";
    private static final String TINTED_STATUSBAR_FILTER = "status_bar_tinted_filter";
    private static final String TINTED_STATUSBAR_TRANSPARENT = "tinted_statusbar_transparent";
    private static final String TINTED_NAVBAR_TRANSPARENT = "tinted_navbar_transparent";
    private static final String CATEGORY_TINTED = "category_tinted_statusbar";

    private ListPreference mStatusBarClockStyle;
    private ListPreference mStatusBarBattery;
    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarStyle;
    private ListPreference mBatteryBarThickness;
    private ListPreference mTintedStatusbar;
    private ListPreference mTintedStatusbarOption;
    private CheckBoxPreference mBatteryBarChargingAnimation;
    private ColorPickerPreference mBatteryBarColor;
    private SystemSettingCheckBoxPreference mStatusBarBatteryShowPercent;
    private ListPreference mStatusBarCmSignal;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private ListPreference mStatusBarNetStatsUpdate;
    private CheckBoxPreference mStatusBarNetworkStats;
    private CheckBoxPreference mTintedStatusbarFilter;
    private SeekBarPreferenceCHOS mTintedStatusbarTransparency;
    private SeekBarPreferenceCHOS mTintedNavbarTransparency;

    private ContentObserver mSettingsObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarClockStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY);
        mStatusBarBatteryShowPercent =
                (SystemSettingCheckBoxPreference) findPreference(STATUS_BAR_BATTERY_SHOW_PERCENT);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);

        mStatusBarBrightnessControl = (CheckBoxPreference)
                prefSet.findPreference(Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL);
        refreshBrightnessControl();

        int clockStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CLOCK, 1);
        mStatusBarClockStyle.setValue(String.valueOf(clockStyle));
        mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntry());
        mStatusBarClockStyle.setOnPreferenceChangeListener(this);

        mStatusBarNetworkStats = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS);
        mStatusBarNetStatsUpdate = (ListPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS_UPDATE);

        mStatusBarNetworkStats.setChecked((Settings.PAC.getInt(resolver, Settings.PAC.STATUS_BAR_NETWORK_STATS, 0) == 1));

        long statsUpdate = Settings.PAC.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.PAC.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
        mStatusBarNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntry());
        mStatusBarNetStatsUpdate.setOnPreferenceChangeListener(this);

        final PreferenceCategory tintedCategory =
                     (PreferenceCategory) prefSet.findPreference(CATEGORY_TINTED);

        mTintedStatusbar = (ListPreference) findPreference(TINTED_STATUSBAR);
        int tintedStatusbar = Settings.PAC.getInt(resolver,
                    Settings.PAC.STATUS_BAR_TINTED_COLOR, 0);
        mTintedStatusbar.setValue(String.valueOf(tintedStatusbar));
        mTintedStatusbar.setSummary(mTintedStatusbar.getEntry());
        mTintedStatusbar.setOnPreferenceChangeListener(this);

        mTintedStatusbarFilter = (CheckBoxPreference) findPreference(TINTED_STATUSBAR_FILTER);
        mTintedStatusbarFilter.setEnabled(tintedStatusbar != 0);

        mTintedStatusbarTransparency = (SeekBarPreferenceCHOS) findPreference(TINTED_STATUSBAR_TRANSPARENT);
        mTintedStatusbarTransparency.setValue(Settings.System.getInt(resolver,
                Settings.PAC.STATUS_BAR_TINTED_STATBAR_TRANSPARENT, 100));
        mTintedStatusbarTransparency.setEnabled(tintedStatusbar != 0);
        mTintedStatusbarTransparency.setOnPreferenceChangeListener(this);

        mTintedStatusbarOption = (ListPreference) findPreference(TINTED_STATUSBAR_OPTION);
        mTintedNavbarTransparency = (SeekBarPreferenceCHOS) findPreference(TINTED_NAVBAR_TRANSPARENT);

        int batteryStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int signalStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        if (Utils.isWifiOnly(getActivity())
                || (MSimTelephonyManager.getDefault().isMultiSimEnabled())) {
            prefSet.removePreference(mStatusBarCmSignal);
        }

        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        mBatteryBar.setValue((Settings.PAC.getInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR, 0)) + "");
        mBatteryBar.setSummary(mBatteryBar.getEntry());

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.PAC.getInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR_STYLE, 0)) + "");
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        int defaultColor = 0xffffffff;
        int intColor = Settings.PAC.getInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR_COLOR, defaultColor);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBatteryBarColor.setSummary(hexColor);

        mBatteryBarChargingAnimation = (CheckBoxPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.PAC.getInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);

        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryBarThickness.setOnPreferenceChangeListener(this);
        mBatteryBarThickness.setValue((Settings.PAC.getInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR_THICKNESS, 1)) + "");
        mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());

        updateBatteryBarOptions();
        enableStatusBarBatteryDependents();

        mSettingsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                refreshBrightnessControl();
            }

            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, null);
            }
        };
        boolean hasNavBarByDefault = getResources().getBoolean(
                  com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.PAC.getInt(getContentResolver(),
                  Settings.PAC.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1;

        if (!hasNavBarByDefault && !enableNavigationBar) {
            tintedCategory.removePreference(mTintedStatusbarOption);
            tintedCategory.removePreference(mTintedNavbarTransparency);
        } else {
            int tintedStatusbarOption = Settings.PAC.getInt(resolver,
                    Settings.PAC.STATUS_BAR_TINTED_OPTION, 0);
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
    public void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                true, mSettingsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_BATTERY, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);

            enableStatusBarBatteryDependents();
            return true;
        } else if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarClockStyle) {
            int clockStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarClockStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CLOCK, clockStyle);
            mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.STATUSBAR_BATTERY_BAR_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBar) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBar.findIndexOfValue((String) newValue);
            Settings.PAC.putInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR, val);
            mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
            updateBatteryBarOptions();
            return true;
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.PAC.putInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR_STYLE, val);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int val = Integer.valueOf((String) newValue);
            int index = mBatteryBarThickness.findIndexOfValue((String) newValue);
            Settings.PAC.putInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR_THICKNESS, val);
            mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) newValue);
            int index = mStatusBarNetStatsUpdate.findIndexOfValue((String) newValue);
            Settings.PAC.putLong(getActivity().getApplicationContext().getContentResolver(),
                    Settings.PAC.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntries()[index]);
            return true;
        } else if (preference == mTintedStatusbar) {
            int val = Integer.parseInt((String) newValue);
            int index = mTintedStatusbar.findIndexOfValue((String) newValue);
            Settings.PAC.putInt(resolver,
                Settings.PAC.STATUS_BAR_TINTED_COLOR, val);
            mTintedStatusbar.setSummary(mTintedStatusbar.getEntries()[index]);
            if (mTintedStatusbarOption != null) {
                mTintedStatusbarOption.setEnabled(val != 0);
            }
            mTintedStatusbarFilter.setEnabled(val != 0);
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
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;
        if (preference == mStatusBarNetworkStats) {
            value = mStatusBarNetworkStats.isChecked();
            Settings.PAC.putInt(resolver, Settings.PAC.STATUS_BAR_NETWORK_STATS, value ? 1 : 0);
            return true;
        } else if (preference == mBatteryBarChargingAnimation) {
            value = mBatteryBarChargingAnimation.isChecked();
            Settings.PAC.putInt(resolver, Settings.PAC.STATUSBAR_BATTERY_BAR_ANIMATE, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void refreshBrightnessControl() {
        try {
            if (Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            } else {
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_brightness_summary);
            }
        } catch (SettingNotFoundException e) {
            // Do nothing
        }
    }

    private void enableStatusBarBatteryDependents() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY, 0) == 4 ||
            Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY, 0) == 6) {
            mStatusBarBatteryShowPercent.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
        }
    }

    private void updateBatteryBarOptions() {
        if (Settings.PAC.getInt(getActivity().getContentResolver(),
               Settings.PAC.STATUSBAR_BATTERY_BAR, 0) == 0) {
            mBatteryBarStyle.setEnabled(false);
            mBatteryBarThickness.setEnabled(false);
            mBatteryBarChargingAnimation.setEnabled(false);
            mBatteryBarColor.setEnabled(false);
        } else {
            mBatteryBarStyle.setEnabled(true);
            mBatteryBarThickness.setEnabled(true);
            mBatteryBarChargingAnimation.setEnabled(true);
            mBatteryBarColor.setEnabled(true);
        }
    }
}
