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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.internal.util.pac.DeviceUtils;
import com.android.internal.util.pac.Action;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import cyanogenmod.hardware.CMHardwareManager;

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

    private static final int DLG_NAVIGATION_WARNING = 0;

    private int mNavBarMenuDisplayValue;

    private CMHardwareManager mHardware;

    private ListPreference mMenuDisplayLocation;
    private ListPreference mNavBarMenuDisplay;
    private SwitchPreference mEnableNavigationBar;
    private SwitchPreference mDisableHardwareKeys;
    private SwitchPreference mNavigationBarCanMove;
    private PreferenceScreen mButtonPreference;
    private PreferenceScreen mStyleDimenPreference;
    private SwitchPreference mNavigationBarImeArrows;
    private SwitchPreference mNavigationBarLeftPref;
    private PreferenceScreen mNavigationBarRing;

    private SettingsObserver mSettingsObserver = new SettingsObserver(new Handler());
    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.registerContentObserver(Settings.PAC.getUriFor(
                    Settings.PAC.NAVIGATION_BAR_SHOW), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateSettings();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity activity = getActivity();
        mHardware = CMHardwareManager.getInstance(activity);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mMenuDisplayLocation = (ListPreference) findPreference(PREF_MENU_LOCATION);
        mMenuDisplayLocation.setOnPreferenceChangeListener(this);

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);

        mButtonPreference = (PreferenceScreen) findPreference(PREF_BUTTON);
        mStyleDimenPreference = (PreferenceScreen) findPreference(PREF_STYLE_DIMEN);

        mNavigationBarRing = (PreferenceScreen) findPreference(KEY_NAVIGATION_BAR_RING);

        mEnableNavigationBar = (SwitchPreference) findPreference(ENABLE_NAVIGATION_BAR);
        mDisableHardwareKeys = (SwitchPreference) findPreference(DISABLE_HARDWARD_KEYS);

        mNavigationBarCanMove = (SwitchPreference) findPreference(PREF_NAVIGATION_BAR_CAN_MOVE);
        if (DeviceUtils.isPhone(getActivity())) {
            mNavigationBarCanMove.setOnPreferenceChangeListener(this);
        } else {
            prefs.removePreference(mNavigationBarCanMove);
            mNavigationBarCanMove = null;
        }

        mNavigationBarLeftPref = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);
        if (!DeviceUtils.isPhone(getActivity())) {
            prefs.removePreference(mNavigationBarLeftPref);
            mNavigationBarLeftPref = null;
        }

        mNavigationBarImeArrows = (SwitchPreference) findPreference(NAVIGATION_BAR_IME_ARROWS);
        mNavigationBarImeArrows.setOnPreferenceChangeListener(this);

        updateSettings();
    }

    private void updateSettings() {
        PreferenceScreen prefs = getPreferenceScreen();

        mMenuDisplayLocation.setValue(Settings.PAC.getInt(getActivity()
                .getContentResolver(), Settings.PAC.MENU_LOCATION,
                0) + "");
        mNavBarMenuDisplayValue = Settings.PAC.getInt(getActivity()
                .getContentResolver(), Settings.PAC.MENU_VISIBILITY,
                2);
        mNavBarMenuDisplay.setValue(mNavBarMenuDisplayValue + "");

        boolean enableNavigationBar = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_SHOW,
                Action.isNavBarDefault(getActivity()) ? 1 : 0) == 1;

        boolean disableHardwareKeys = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.DEV_FORCE_SHOW_NAVBAR, 1) == 1;

        if (mHardware.isSupported(CMHardwareManager.FEATURE_KEY_DISABLE)) {
            prefs.removePreference(mEnableNavigationBar);
            mDisableHardwareKeys.setChecked(disableHardwareKeys);
            mDisableHardwareKeys.setOnPreferenceChangeListener(this);
        } else {
            prefs.removePreference(mDisableHardwareKeys);
            mEnableNavigationBar.setChecked(enableNavigationBar);
            mEnableNavigationBar.setOnPreferenceChangeListener(this);
        }

        if (mNavigationBarCanMove != null) {
            mNavigationBarCanMove.setChecked(Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_CAN_MOVE, 1) == 0);
        }

        mNavigationBarImeArrows.setChecked(Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_IME_ARROWS, 0) == 1);

        updateNavbarPreferences(enableNavigationBar || disableHardwareKeys);
    }

    private void updateNavbarPreferences(boolean show) {
        mNavBarMenuDisplay.setEnabled(show);
        mButtonPreference.setEnabled(show);
        mStyleDimenPreference.setEnabled(show);
        if (mNavigationBarCanMove != null) {
            mNavigationBarCanMove.setEnabled(show);
        }
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
            if (!((Boolean) newValue) && !Action.isPieEnabled(getActivity())
                    && Action.isNavBarDefault(getActivity())) {
                showDialogInner(DLG_NAVIGATION_WARNING);
                return true;
            }
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
        updateSettings();
        mSettingsObserver.observe();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        NavbarSettings getOwner() {
            return (NavbarSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_NAVIGATION_WARNING:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.navigation_bar_warning_no_navigation_present)
                    .setNegativeButton(R.string.dlg_cancel,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.PAC.putInt(getActivity().getContentResolver(),
                                    Settings.PAC.PIE_CONTROLS, 1);
                            Settings.PAC.putInt(getActivity().getContentResolver(),
                                    Settings.PAC.NAVIGATION_BAR_SHOW, 0);
                            getOwner().updateNavbarPreferences(false);
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_NAVIGATION_WARNING:
                    getOwner().mEnableNavigationBar.setChecked(true);
                    getOwner().updateNavbarPreferences(true);
                    break;
            }
        }
    }

}
