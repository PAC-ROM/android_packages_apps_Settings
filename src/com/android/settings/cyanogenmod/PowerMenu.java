/*
 * Copyright (C) 2012 CyanogenMod
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

import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PowerMenu extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "PowerMenu";

    private static final String KEY_REBOOT = "power_menu_reboot";
    private static final String KEY_SCREENSHOT = "power_menu_screenshot";
    private static final String KEY_EXPANDED_DESKTOP = "power_menu_expanded_desktop";
    private static final String KEY_PROFILES = "power_menu_profiles";
    private static final String KEY_AIRPLANE = "power_menu_airplane";
    private static final String KEY_USER = "power_menu_user";
    private static final String KEY_SOUND = "power_menu_sound";
    private static final String PREF_REBOOT_KEYGUARD = "show_reboot_keyguard";

    private SwitchPreference mRebootPref;
    private SwitchPreference mScreenshotPref;
    private ListPreference mExpandedDesktopPref;
    private SwitchPreference mProfilesPref;
    private SwitchPreference mAirplanePref;
    private SwitchPreference mUserPref;
    private SwitchPreference mSoundPref;
    private SwitchPreference mShowRebootKeyguard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.power_menu_settings);

        mRebootPref = (SwitchPreference) findPreference(KEY_REBOOT);
        mRebootPref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWER_MENU_REBOOT_ENABLED, 1) == 1));
        mRebootPref.setOnPreferenceChangeListener(this);

        mScreenshotPref = (SwitchPreference) findPreference(KEY_SCREENSHOT);
        mScreenshotPref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWER_MENU_SCREENSHOT_ENABLED, 0) == 1));
        mScreenshotPref.setOnPreferenceChangeListener(this);

        PreferenceScreen prefSet = getPreferenceScreen();
        mExpandedDesktopPref = (ListPreference) prefSet.findPreference(KEY_EXPANDED_DESKTOP);
        mExpandedDesktopPref.setOnPreferenceChangeListener(this);
        int expandedDesktopValue = Settings.System.getInt(getContentResolver(), Settings.System.EXPANDED_DESKTOP_STYLE, 0);
        mExpandedDesktopPref.setValue(String.valueOf(expandedDesktopValue));
        updateExpandedDesktopSummary(expandedDesktopValue);

        mProfilesPref = (SwitchPreference) findPreference(KEY_PROFILES);
        mProfilesPref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWER_MENU_PROFILES_ENABLED, 1) == 1));
        mProfilesPref.setOnPreferenceChangeListener(this);

        // Only enable if System Profiles are also enabled
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.SYSTEM_PROFILES_ENABLED, 1) == 1;
        mProfilesPref.setEnabled(enabled);

        mAirplanePref = (SwitchPreference) findPreference(KEY_AIRPLANE);
        mAirplanePref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWER_MENU_AIRPLANE_ENABLED, 1) == 1));
        mAirplanePref.setOnPreferenceChangeListener(this);

        mUserPref = (SwitchPreference) findPreference(KEY_USER);
        if (!UserHandle.MU_ENABLED
            || !UserManager.supportsMultipleUsers()) {
            getPreferenceScreen().removePreference(mUserPref);
        } else {
            mUserPref.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.POWER_MENU_USER_ENABLED, 0) == 1));
            mUserPref.setOnPreferenceChangeListener(this);
        }

        mSoundPref = (SwitchPreference) findPreference(KEY_SOUND);
        mSoundPref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.POWER_MENU_SOUND_ENABLED, 1) == 1));
        mSoundPref.setOnPreferenceChangeListener(this);

        mShowRebootKeyguard = (SwitchPreference) findPreference(PREF_REBOOT_KEYGUARD);
        mShowRebootKeyguard.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_REBOOT_KEYGUARD, true));
        mShowRebootKeyguard.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mExpandedDesktopPref) {
            int expandedDesktopValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.EXPANDED_DESKTOP_STYLE, expandedDesktopValue);
            updateExpandedDesktopSummary(expandedDesktopValue);
            return true;
        } else if (preference == mScreenshotPref) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_SCREENSHOT_ENABLED,
                    value ? 1 : 0);
            return true;
        } else if (preference == mRebootPref) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_REBOOT_ENABLED,
                    value ? 1 : 0);
            return true;
        } else if (preference == mProfilesPref) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_PROFILES_ENABLED,
                    value ? 1 : 0);
            return true;
       } else if (preference == mAirplanePref) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_AIRPLANE_ENABLED,
                    value ? 1 : 0);
            return true;
       } else if (preference == mUserPref) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_USER_ENABLED,
                    value ? 1 : 0);
            return true;
       } else if (preference == mSoundPref) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_MENU_SOUND_ENABLED,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowRebootKeyguard) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_REBOOT_KEYGUARD,
                    ((SwitchPreference)preference).isChecked());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateExpandedDesktopSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Expanded desktop deactivated
            Settings.System.putInt(getContentResolver(),
                    Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 0);
            mExpandedDesktopPref.setSummary(res.getString(R.string.expanded_desktop_disabled));
        } else if (value == 1) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 1);
            mExpandedDesktopPref.setSummary(res.getString(R.string.expanded_desktop_status_bar));
        } else if (value == 2) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.POWER_MENU_EXPANDED_DESKTOP_ENABLED, 1);
            mExpandedDesktopPref.setSummary(res.getString(R.string.expanded_desktop_no_status_bar));
        }
    }
}