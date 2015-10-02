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
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.SlimSeekBarPreference;
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
    private static final String DIM_NAV_BUTTONS = "dim_nav_buttons";
    private static final String DIM_NAV_BUTTONS_TOUCH_ANYWHERE = "dim_nav_buttons_touch_anywhere";
    private static final String DIM_NAV_BUTTONS_TIMEOUT = "dim_nav_buttons_timeout";
    private static final String DIM_NAV_BUTTONS_ALPHA = "dim_nav_buttons_alpha";
    private static final String DIM_NAV_BUTTONS_ANIMATE = "dim_nav_buttons_animate";
    private static final String DIM_NAV_BUTTONS_ANIMATE_DURATION = "dim_nav_buttons_animate_duration";

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
    private SwitchPreference mDimNavButtons;
    private SwitchPreference mDimNavButtonsTouchAnywhere;
    private SlimSeekBarPreference mDimNavButtonsTimeout;
    private SlimSeekBarPreference mDimNavButtonsAlpha;
    private SwitchPreference mDimNavButtonsAnimate;
    private SlimSeekBarPreference mDimNavButtonsAnimateDuration;

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
        if (DeviceUtils.isPhone(getActivity())) {
            mNavigationBarLeftPref.setOnPreferenceChangeListener(this);
        } else {
            prefs.removePreference(mNavigationBarLeftPref);
            mNavigationBarLeftPref = null;
        }

        mNavigationBarImeArrows = (SwitchPreference) findPreference(NAVIGATION_BAR_IME_ARROWS);
        mNavigationBarImeArrows.setOnPreferenceChangeListener(this);

        mDimNavButtons = (SwitchPreference) findPreference(DIM_NAV_BUTTONS);
        mDimNavButtons.setOnPreferenceChangeListener(this);

        mDimNavButtonsTouchAnywhere = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_TOUCH_ANYWHERE);
        mDimNavButtonsTouchAnywhere.setOnPreferenceChangeListener(this);

        mDimNavButtonsTimeout = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_TIMEOUT);
        mDimNavButtonsTimeout.setDefault(3000);
        mDimNavButtonsTimeout.isMilliseconds(true);
        mDimNavButtonsTimeout.setInterval(1);
        mDimNavButtonsTimeout.minimumValue(100);
        mDimNavButtonsTimeout.multiplyValue(100);
        mDimNavButtonsTimeout.setOnPreferenceChangeListener(this);

        mDimNavButtonsAlpha = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ALPHA);
        mDimNavButtonsAlpha.setDefault(50);
        mDimNavButtonsAlpha.setInterval(1);
        mDimNavButtonsAlpha.setOnPreferenceChangeListener(this);

        mDimNavButtonsAnimate = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE);
        mDimNavButtonsAnimate.setOnPreferenceChangeListener(this);

        mDimNavButtonsAnimateDuration = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE_DURATION);
        mDimNavButtonsAnimateDuration.setDefault(2000);
        mDimNavButtonsAnimateDuration.isMilliseconds(true);
        mDimNavButtonsAnimateDuration.setInterval(1);
        mDimNavButtonsAnimateDuration.minimumValue(100);
        mDimNavButtonsAnimateDuration.multiplyValue(100);
        mDimNavButtonsAnimateDuration.setOnPreferenceChangeListener(this);

        updateSettings();
        updateNavigationBarCanMove();
        updateNavigationBarLeft();
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
                Settings.Secure.DEV_FORCE_SHOW_NAVBAR, 0) == 1;

        if (mHardware.isSupported(CMHardwareManager.FEATURE_KEY_DISABLE) &&
                !Action.isNavBarDefault(getActivity()) &&
                UserHandle.myUserId() == UserHandle.USER_OWNER) {
            mDisableHardwareKeys.setChecked(disableHardwareKeys);
            mDisableHardwareKeys.setOnPreferenceChangeListener(this);
            prefs.removePreference(mEnableNavigationBar);
        } else {
            prefs.removePreference(mDisableHardwareKeys);
        }

        if (mEnableNavigationBar != null) {
            mEnableNavigationBar.setChecked(enableNavigationBar);
            mEnableNavigationBar.setOnPreferenceChangeListener(this);
        }

        if (mNavigationBarCanMove != null) {
            mNavigationBarCanMove.setChecked(Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.NAVIGATION_BAR_CAN_MOVE, 1) == 0);
        }

        mNavigationBarImeArrows.setChecked(Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_IME_ARROWS, 0) == 1);

        if (mDimNavButtons != null) {
            mDimNavButtons.setChecked(Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.DIM_NAV_BUTTONS, 0) == 1);
        }

        if (mDimNavButtonsTouchAnywhere != null) {
            mDimNavButtonsTouchAnywhere.setChecked(Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.DIM_NAV_BUTTONS_TOUCH_ANYWHERE, 0) == 1);
        }

        if (mDimNavButtonsTimeout != null) {
            final int dimTimeout = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.DIM_NAV_BUTTONS_TIMEOUT, 3000);
            // minimum 100 is 1 interval of the 100 multiplier
            mDimNavButtonsTimeout.setInitValue((dimTimeout / 100) - 1);
        }

        if (mDimNavButtonsAlpha != null) {
            int alphaScale = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.DIM_NAV_BUTTONS_ALPHA, 50);
            mDimNavButtonsAlpha.setInitValue(alphaScale);
        }

        if (mDimNavButtonsAnimate != null) {
            mDimNavButtonsAnimate.setChecked(Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.DIM_NAV_BUTTONS_ANIMATE, 0) == 1);
        }

        if (mDimNavButtonsAnimateDuration != null) {
            final int animateDuration = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.DIM_NAV_BUTTONS_ANIMATE_DURATION, 2000);
            // minimum 100 is 1 interval of the 100 multiplier
            mDimNavButtonsAnimateDuration.setInitValue((animateDuration / 100) - 1);
        }

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
        mDimNavButtons.setEnabled(show);
        mDimNavButtonsTouchAnywhere.setEnabled(show);
        mDimNavButtonsTimeout.setEnabled(show);
        mDimNavButtonsAlpha.setEnabled(show);
        mDimNavButtonsAnimate.setEnabled(show);
        mDimNavButtonsAnimateDuration.setEnabled(show);
    }

    private void updateNavigationBarCanMove() {
        boolean enable = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_CAN_MOVE, 1) == 0;
        if (enable) {
            if (mNavigationBarLeftPref != null)
                mNavigationBarLeftPref.setEnabled(false);
        } else {
            if (mNavigationBarLeftPref != null)
                mNavigationBarLeftPref.setEnabled(true);
        }
    }

    private void updateNavigationBarLeft() {
        boolean enable = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVBAR_LEFT_IN_LANDSCAPE, 1) == 0;
        if (enable) {
            if (mNavigationBarCanMove != null)
                mNavigationBarCanMove.setEnabled(false);
        } else {
            if (mNavigationBarCanMove != null)
                mNavigationBarCanMove.setEnabled(true);
        }
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
            updateNavigationBarCanMove();
            return true;
        } else if (preference == mNavigationBarLeftPref) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVBAR_LEFT_IN_LANDSCAPE,
                    ((Boolean) newValue) ? 0 : 1);
            updateNavigationBarLeft();
            return true;
        } else if (preference == mNavigationBarImeArrows) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.NAVIGATION_BAR_IME_ARROWS,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtons) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.DIM_NAV_BUTTONS,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsTouchAnywhere) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.DIM_NAV_BUTTONS_TOUCH_ANYWHERE,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsTimeout) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.DIM_NAV_BUTTONS_TIMEOUT, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mDimNavButtonsAlpha) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.DIM_NAV_BUTTONS_ALPHA, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mDimNavButtonsAnimate) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.DIM_NAV_BUTTONS_ANIMATE,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsAnimateDuration) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                Settings.PAC.DIM_NAV_BUTTONS_ANIMATE_DURATION,
                Integer.parseInt((String) newValue));
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSettings();
        updateNavigationBarCanMove();
        updateNavigationBarLeft();
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
