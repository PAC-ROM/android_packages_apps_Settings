/*
 * Copyright (C) 2012-2015 Slimroms
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

package com.android.settings.pac.navbar;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.internal.util.pac.DeviceUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NavbarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavBar";
    private static final String PREF_MENU_LOCATION = "pref_navbar_menu_location";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "pref_navbar_menu_display";
    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String DISABLE_HARDWARD_KEYS = "disable_hard_keys";
    private static final String PREF_BUTTON = "navbar_button_settings";
    private static final String PREF_STYLE_DIMEN = "navbar_style_dimen_settings";
    private static final String PREF_NAVIGATION_BAR_CAN_MOVE = "navbar_can_move";
    private static final String NAVIGATION_BAR_IME_ARROWS = "navigation_bar_ime_arrows";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_NAVIGATION_BAR_RING = "navigation_bar_ring";

    private int mNavBarMenuDisplayValue;

    ListPreference mMenuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    SwitchPreference mEnableNavigationBar;
    SwitchPreference mDisableHardwareKeys;
    SwitchPreference mNavigationBarCanMove;
    PreferenceScreen mButtonPreference;
    PreferenceScreen mStyleDimenPreference;
    SwitchPreference mNavigationBarImeArrows;
    SwitchPreference mNavigationBarLeftPref;
    PreferenceScreen mNavigationBarRing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mMenuDisplayLocation = (ListPreference) findPreference(PREF_MENU_LOCATION);
        mMenuDisplayLocation.setValue(Settings.PAC.getInt(getActivity()
                .getContentResolver(), Settings.PAC.MENU_LOCATION,
                0) + "");
        mMenuDisplayLocation.setOnPreferenceChangeListener(this);

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplayValue = Settings.PAC.getInt(getActivity()
                .getContentResolver(), Settings.PAC.MENU_VISIBILITY,
                2);
        mNavBarMenuDisplay.setValue(mNavBarMenuDisplayValue + "");
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);

        mButtonPreference = (PreferenceScreen) findPreference(PREF_BUTTON);
        mStyleDimenPreference = (PreferenceScreen) findPreference(PREF_STYLE_DIMEN);

        mNavigationBarRing = (PreferenceScreen) findPreference(KEY_NAVIGATION_BAR_RING);

        boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1 ||
                Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) == 1;

        mDisableHardwareKeys = (SwitchPreference) findPreference(DISABLE_HARDWARD_KEYS);
        mEnableNavigationBar = (SwitchPreference) findPreference(ENABLE_NAVIGATION_BAR);
        if (hasNavBarByDefault) {
            getPreferenceScreen().removePreference(mDisableHardwareKeys);
            mEnableNavigationBar.setChecked(enableNavigationBar);
            mEnableNavigationBar.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mEnableNavigationBar);
            mDisableHardwareKeys.setChecked(enableNavigationBar);
            mDisableHardwareKeys.setOnPreferenceChangeListener(this);
        }

        mNavigationBarLeftPref = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);
        if (!DeviceUtils.isPhone(getActivity())) {
            prefs.removePreference(mNavigationBarLeftPref);
            mNavigationBarLeftPref = null;
        }

        mNavigationBarCanMove = (SwitchPreference) findPreference(PREF_NAVIGATION_BAR_CAN_MOVE);
        mNavigationBarCanMove.setChecked(Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_CAN_MOVE,
                DeviceUtils.isPhone(getActivity()) ? 1 : 0) == 0);
        mNavigationBarCanMove.setOnPreferenceChangeListener(this);

        mNavigationBarImeArrows = (SwitchPreference) findPreference(NAVIGATION_BAR_IME_ARROWS);
        mNavigationBarImeArrows.setChecked(Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_IME_ARROWS, 0) == 1);
        mNavigationBarImeArrows.setOnPreferenceChangeListener(this);

        updateNavbarPreferences(enableNavigationBar);
    }

    private void updateNavbarPreferences(boolean show) {
        mNavBarMenuDisplay.setEnabled(show);
        mButtonPreference.setEnabled(show);
        mStyleDimenPreference.setEnabled(show);
        mNavigationBarCanMove.setEnabled(show);
        mMenuDisplayLocation.setEnabled(show
            && mNavBarMenuDisplayValue != 1);
        mNavigationBarImeArrows.setEnabled(show);
        if (mNavigationBarLeftPref != null) {
            mNavigationBarLeftPref.setEnabled(show);
        }
        mNavigationBarRing.setEnabled(show);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mMenuDisplayLocation) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.MENU_LOCATION, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            mNavBarMenuDisplayValue = Integer.parseInt((String) newValue);
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.MENU_VISIBILITY, mNavBarMenuDisplayValue);
            mMenuDisplayLocation.setEnabled(mNavBarMenuDisplayValue != 1);
            return true;
        } else if (preference == mDisableHardwareKeys) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.DEV_FORCE_SHOW_NAVBAR,
                    ((Boolean) newValue) ? 1 : 0);
            updateNavbarPreferences((Boolean) newValue);
            return true;
        } else if (preference == mEnableNavigationBar) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_SHOW,
                    ((Boolean) newValue) ? 1 : 0);
            updateNavbarPreferences((Boolean) newValue);
            return true;
        } else if (preference == mNavigationBarCanMove) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_CAN_MOVE,
                    ((Boolean) newValue) ? 0 : 1);
            return true;
        } else if (preference == mNavigationBarImeArrows) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_IME_ARROWS,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
