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

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ColorPickerPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.ExtendedPropertiesUtils;
import android.view.IWindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class NavigationBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Preference.OnPreferenceClickListener  {


    private static final String NAV_BAR_CATEGORY = "nav_bar_category";
    private static final String NAV_BAR_STATUS = "nav_bar_status";
//    private static final String NAV_BAR_EDITOR = "nav_bar_editor";
    private static final String NAV_BAR_TABUI_MENU = "nav_bar_tabui_menu";
    private static final String NAV_BAR_COLOR = "nav_bar_color";
    private static final String NAV_BUTTON_COLOR = "nav_bar_button_color";
    private static final String NAV_GLOW_COLOR = "nav_bar_glow_color";
    private static final String NAV_BAR_COLOR_DEF = "nav_bar_color_default";

    private CheckBoxPreference mNavigationBarShow;
    private ColorPickerPreference mNavigationBarColor;
    private ColorPickerPreference mNavigationButtonColor;
    private ColorPickerPreference mNavigationGlowColor;
//    private PreferenceScreen mNavigationBarEditor;
    private CheckBoxPreference mMenuButtonShow;
    private ListPreference mGlowTimes;
    private PreferenceCategory mPrefCategory;
    private Preference mResetColor;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        addPreferencesFromResource(R.xml.navigation_bar);

        PreferenceScreen prefSet = getPreferenceScreen();

        mNavigationBarColor = (ColorPickerPreference) findPreference(NAV_BAR_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);
//        mNavigationBarColor.setAlphaSliderEnabled(true);
        mNavigationBarColor.setAlphaSliderEnabled(false);

        mNavigationButtonColor = (ColorPickerPreference) findPreference(NAV_BUTTON_COLOR);
        mNavigationButtonColor.setOnPreferenceChangeListener(this);
        mNavigationButtonColor.setAlphaSliderEnabled(true);

        mNavigationGlowColor = (ColorPickerPreference) findPreference(NAV_GLOW_COLOR);
        mNavigationGlowColor.setOnPreferenceChangeListener(this);
        mNavigationGlowColor.setAlphaSliderEnabled(false);

        mNavigationBarShow = (CheckBoxPreference) prefSet.findPreference(NAV_BAR_STATUS);

//        mNavigationBarEditor = (PreferenceScreen) prefSet.findPreference(NAV_BAR_EDITOR);
        mMenuButtonShow = (CheckBoxPreference) prefSet.findPreference(NAV_BAR_TABUI_MENU);
        mResetColor = (Preference) findPreference(NAV_BAR_COLOR_DEF);
        mResetColor.setOnPreferenceClickListener(this);

        IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            mNavigationBarShow.setChecked((Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.NAV_BAR_STATUS, !wm.hasHardwareKeys() ? 1 : 0) == 1));
        } catch (RemoteException ex) {
            // too bad, so sad, oh mom, oh dad
        }

        mMenuButtonShow.setChecked((Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.NAV_BAR_TABUI_MENU, 0) == 1));

//        mNavigationBarEditor.setEnabled(mNavigationBarShow.isChecked());
        mMenuButtonShow.setEnabled(mNavigationBarShow.isChecked());

        mPrefCategory = (PreferenceCategory) findPreference(NAV_BAR_CATEGORY);

        if (!Utils.isTablet()) {
            mPrefCategory.removePreference(mMenuButtonShow);
//        } else {
//            mPrefCategory.removePreference(mNavigationBarEditor);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int index = -1;
        if (preference == mNavigationBarColor) {
            index = ExtendedPropertiesUtils.PARANOID_COLORS_NAVBAR;
        } else if (preference == mNavigationButtonColor) {
            index = ExtendedPropertiesUtils.PARANOID_COLORS_NAVBUTTON;
        } else if (preference == mNavigationGlowColor) {
            index = ExtendedPropertiesUtils.PARANOID_COLORS_NAVGLOW;
        }
        
        if (index != -1) {
            String mSetting = Settings.System.getString(mContext.getContentResolver(),
                    ExtendedPropertiesUtils.PARANOID_COLORS_SETTINGS[index]);
            String[] mColors = (mSetting == null || mSetting.equals("") ?
                    ExtendedPropertiesUtils.PARANOID_COLORS_DEFAULTS[index] :
                    mSetting).split(ExtendedPropertiesUtils.PARANOID_STRING_DELIMITER);
            Settings.System.putString(mContext.getContentResolver(),
                    ExtendedPropertiesUtils.PARANOID_COLORS_SETTINGS[index],
                    ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(
                    newValue))).substring(1) + "|" + mColors[1] + "|1");
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        if (pref == mResetColor) {
            for (int i=ExtendedPropertiesUtils.PARANOID_COLORS_NAVBAR;
                    i <= ExtendedPropertiesUtils.PARANOID_COLORS_NAVGLOW; i++) {
                String mSetting = Settings.System.getString(mContext.getContentResolver(),
                        ExtendedPropertiesUtils.PARANOID_COLORS_SETTINGS[i]);
                String[] mColors = (mSetting == null || mSetting.equals("") ?
                        ExtendedPropertiesUtils.PARANOID_COLORS_DEFAULTS[i] :
                        mSetting).split(ExtendedPropertiesUtils.PARANOID_STRING_DELIMITER);
                Settings.System.putString(getActivity().getContentResolver(),
                        ExtendedPropertiesUtils.PARANOID_COLORS_SETTINGS[i], ExtendedPropertiesUtils.
                        PARANOID_COLORS_DEFAULTS[i].split(ExtendedPropertiesUtils.PARANOID_STRING_DELIMITER)
                        [0] + "|" + mColors[1] + "|1");
            }

            mNavigationBarColor.onColorChanged(ExtendedPropertiesUtils.PARANOID_COLORCODES_DEFAULTS[
                    ExtendedPropertiesUtils.PARANOID_COLORS_NAVBAR]);
            mNavigationButtonColor.onColorChanged(ExtendedPropertiesUtils.PARANOID_COLORCODES_DEFAULTS[
                    ExtendedPropertiesUtils.PARANOID_COLORS_NAVBUTTON]);
            mNavigationGlowColor.onColorChanged(ExtendedPropertiesUtils.PARANOID_COLORCODES_DEFAULTS[
                    ExtendedPropertiesUtils.PARANOID_COLORS_NAVGLOW]);
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mNavigationBarShow) {
            value = mNavigationBarShow.isChecked();
            mMenuButtonShow.setEnabled(value);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.NAV_BAR_STATUS, value ? 1 : 0);
//            mNavigationBarEditor.setEnabled(value);
//            mMenuButtonShow.setEnabled(value);
            return true;
        }
        else if (preference == mMenuButtonShow) {
            value = mMenuButtonShow.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.NAV_BAR_TABUI_MENU, value ? 1 : 0);
            return true;
        }
        return false;
//        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
