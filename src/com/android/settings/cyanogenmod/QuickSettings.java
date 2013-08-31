/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.internal.util.cm.QSConstants;
import com.android.internal.util.cm.QSUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "QuickSettingsPanel";

    private static final String SEPARATOR = "OV=I=XseparatorX=I=VO";
    private static final String EXP_RING_MODE = "pref_ring_mode";
    private static final String EXP_NETWORK_MODE = "pref_network_mode";
    private static final String EXP_SCREENTIMEOUT_MODE = "pref_screentimeout_mode";
    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String NO_NOTIFICATIONS_PULLDOWN = "no_notifications_pulldown";
    private static final String DISABLE_PANEL = "disable_quick_settings";
    private static final String GENERAL_SETTINGS = "pref_general_settings";
    private static final String STATIC_TILES = "static_tiles";
    private static final String DYNAMIC_TILES = "pref_dynamic_tiles";
    private static final String QS_TILES_STYLE = "quicksettings_tiles_style";
    private static final String TILE_PICKER = "tile_picker";
    private static final String FLOATING_WINDOW ="floating_window";

    private MultiSelectListPreference mRingMode;
    private ListPreference mNetworkMode;
    private ListPreference mScreenTimeoutMode;
    private CheckBoxPreference mCollapsePanel;
    private SwitchPreference mDisablePanel;
    private ListPreference mQuickPulldown;
    private ListPreference mNoNotificationsPulldown;
    private CheckBoxPreference mFloatingWindow;
    private PreferenceCategory mGeneralSettings;
    private PreferenceCategory mStaticTiles;
    private PreferenceCategory mDynamicTiles;
    private PreferenceScreen mQsTilesStyle;
    private PreferenceScreen mTilePicker;

    private ContentResolver resolver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_settings_panel);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        resolver = getActivity().getContentResolver();
        mGeneralSettings = (PreferenceCategory) prefSet.findPreference(GENERAL_SETTINGS);
        mStaticTiles = (PreferenceCategory) prefSet.findPreference(STATIC_TILES);
        mDynamicTiles = (PreferenceCategory) prefSet.findPreference(DYNAMIC_TILES);
        mQuickPulldown = (ListPreference) prefSet.findPreference(QUICK_PULLDOWN);
        mNoNotificationsPulldown = (ListPreference) prefSet.findPreference(NO_NOTIFICATIONS_PULLDOWN);
        mDisablePanel = (SwitchPreference) prefSet.findPreference(DISABLE_PANEL);
        mQsTilesStyle = (PreferenceScreen) prefSet.findPreference(QS_TILES_STYLE);
        mTilePicker = (PreferenceScreen) prefSet.findPreference(TILE_PICKER);
        mFloatingWindow = (CheckBoxPreference) prefSet.findPreference(FLOATING_WINDOW);
        mFloatingWindow.setChecked(Settings.System.getInt(resolver, Settings.System.QS_FLOATING_WINDOW, 0) == 1);

        // Add the sound mode
        mRingMode = (MultiSelectListPreference) prefSet.findPreference(EXP_RING_MODE);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            String storedRingMode = Settings.System.getString(resolver,
                    Settings.System.EXPANDED_RING_MODE);
            if (storedRingMode != null) {
                String[] ringModeArray = TextUtils.split(storedRingMode, SEPARATOR);
                mRingMode.setValues(new HashSet<String>(Arrays.asList(ringModeArray)));
                updateSummary(storedRingMode, mRingMode, R.string.pref_ring_mode_summary);
            }
            mRingMode.setOnPreferenceChangeListener(this);
        } else {
            mStaticTiles.removePreference(mRingMode);
        }

        // Add the network mode preference
        mNetworkMode = (ListPreference) prefSet.findPreference(EXP_NETWORK_MODE);
        if (mNetworkMode != null) {
            mNetworkMode.setSummary(mNetworkMode.getEntry());
            mNetworkMode.setOnPreferenceChangeListener(this);
        }

        // Screen timeout mode
        mScreenTimeoutMode = (ListPreference) prefSet.findPreference(EXP_SCREENTIMEOUT_MODE);
        mScreenTimeoutMode.setSummary(mScreenTimeoutMode.getEntry());
        mScreenTimeoutMode.setOnPreferenceChangeListener(this);

        // Remove unsupported options
        if (!QSUtils.deviceSupportsImeSwitcher(getActivity())) {
            if (findPreference(Settings.System.QS_DYNAMIC_IME) != null) {
                mDynamicTiles.removePreference(findPreference(Settings.System.QS_DYNAMIC_IME));
            }
        }
        if (!QSUtils.deviceSupportsUsbTether(getActivity())) {
            if (findPreference(Settings.System.QS_DYNAMIC_USBTETHER) != null) {
                mDynamicTiles.removePreference(findPreference(Settings.System.QS_DYNAMIC_USBTETHER));
            }
        }
        if (!QSUtils.deviceSupportsWifiDisplay(getActivity())) {
            if (findPreference(Settings.System.QS_DYNAMIC_WIFI) != null) {
                mDynamicTiles.removePreference(findPreference(Settings.System.QS_DYNAMIC_WIFI));
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        QuickSettingsUtil.updateAvailableTiles(getActivity());

        if (mNetworkMode != null) {
            if (QuickSettingsUtil.isTileAvailable(QSConstants.TILE_NETWORKMODE)) {
                mStaticTiles.addPreference(mNetworkMode);
            } else {
                mStaticTiles.removePreference(mNetworkMode);
        }

        }

        if (!Utils.isPhone(getActivity())) {
            if (mQuickPulldown != null) {
                mGeneralSettings.removePreference(mQuickPulldown);
            }
            if (mDisablePanel != null) {
                mGeneralSettings.removePreference(mDisablePanel);
            }
            if (mNoNotificationsPulldown != null) {
                mGeneralSettings.removePreference(mNoNotificationsPulldown);
            }
        } else {
            mQuickPulldown.setOnPreferenceChangeListener(this);
            int quickPulldownValue = Settings.System.getInt(resolver, Settings.System.QS_QUICK_PULLDOWN, 0);
            mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
            updatePulldownSummary(quickPulldownValue);

            boolean disablePanel = Settings.System.getInt(resolver,
                Settings.System.QS_DISABLE_PANEL, 0) == 0;
            mDisablePanel.setChecked(disablePanel);
            mDisablePanel.setOnPreferenceChangeListener(this);

            mNoNotificationsPulldown.setOnPreferenceChangeListener(this);
            int noNotificationsPulldownValue = Settings.System.getInt(resolver, Settings.System.QS_NO_NOTIFICATION_PULLDOWN, 0);
            mNoNotificationsPulldown.setValue(String.valueOf(noNotificationsPulldownValue));
            updateNoNotificationsPulldownSummary(noNotificationsPulldownValue);

            setEnablePreferences(disablePanel);
        }

    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFloatingWindow) {
            Settings.System.putInt(resolver, Settings.System.QS_FLOATING_WINDOW,
                    mFloatingWindow.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class MultiSelectListPreferenceComparator implements Comparator<String> {
        private MultiSelectListPreference pref;

        MultiSelectListPreferenceComparator(MultiSelectListPreference p) {
            pref = p;
        }

        @Override
        public int compare(String lhs, String rhs) {
            return Integer.compare(pref.findIndexOfValue(lhs),
                    pref.findIndexOfValue(rhs));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContentResolver();
        if (preference == mRingMode) {
            ArrayList<String> arrValue = new ArrayList<String>((Set<String>) newValue);
            Collections.sort(arrValue, new MultiSelectListPreferenceComparator(mRingMode));
            String value = TextUtils.join(SEPARATOR, arrValue);
            Settings.System.putString(resolver, Settings.System.EXPANDED_RING_MODE, value);
            updateSummary(value, mRingMode, R.string.pref_ring_mode_summary);
            return true;
        } else if (preference == mNetworkMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mNetworkMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.EXPANDED_NETWORK_MODE, value);
            mNetworkMode.setSummary(mNetworkMode.getEntries()[index]);
            return true;
        } else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QS_QUICK_PULLDOWN,
                    quickPulldownValue);
            updatePulldownSummary(quickPulldownValue);
            return true;
        } else if (preference == mNoNotificationsPulldown) {
            int noNotificationsPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QS_NO_NOTIFICATION_PULLDOWN,
                    noNotificationsPulldownValue);
            updateNoNotificationsPulldownSummary(noNotificationsPulldownValue);
            return true;
        } else if (preference == mScreenTimeoutMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mScreenTimeoutMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.EXPANDED_SCREENTIMEOUT_MODE, value);
            mScreenTimeoutMode.setSummary(mScreenTimeoutMode.getEntries()[index]);
            return true;
        } else if (preference == mDisablePanel) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.System.putInt(resolver,
                    Settings.System.QS_DISABLE_PANEL,
                    value ? 0 : 1);
            setEnablePreferences(mDisablePanel.isChecked());
            return true;
        }
        return false;
    }

    private void updateSummary(String val, MultiSelectListPreference pref, int defSummary) {
        // Update summary message with current values
        final String[] values = parseStoredValue(val);
        if (values != null) {
            final int length = values.length;
            final CharSequence[] entries = pref.getEntries();
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < length; i++) {
                CharSequence entry = entries[Integer.parseInt(values[i])];
                if (i != 0) {
                    summary.append(" | ");
                }
                summary.append(entry);
            }
            pref.setSummary(summary);
        } else {
            pref.setSummary(defSummary);
        }
    }

    private void updatePulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            /* quick pulldown deactivated */
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else {
            String direction = res.getString(value == 2
                    ? R.string.quick_pulldown_summary_left
                    : R.string.quick_pulldown_summary_right);
            mQuickPulldown.setSummary(res.getString(R.string.summary_quick_pulldown, direction));
        }
    }

    private void updateNoNotificationsPulldownSummary(int value) {

        if (value == 0) {
            /* No Notifications Pulldown deactivated */
            mNoNotificationsPulldown.setSummary(getResources().getString(R.string.no_notifications_pulldown_off));
        } else {
            mNoNotificationsPulldown.setSummary(getResources().getString(value == 1
                    ? R.string.no_notifications_pulldown_summary_nonperm
                    : R.string.no_notifications_pulldown_summary_all));
        }
    }

    public static String[] parseStoredValue(CharSequence val) {
        if (TextUtils.isEmpty(val)) {
            return null;
        } else {
            return val.toString().split(SEPARATOR);
        }
    }
    private void setEnablePreferences(boolean status) {
        if (mRingMode != null) {
            mRingMode.setEnabled(status);
        }
        if (mNetworkMode != null) {
            mNetworkMode.setEnabled(status);
        }
        if (mScreenTimeoutMode != null) {
            mScreenTimeoutMode.setEnabled(status);
        }
        if (mNoNotificationsPulldown != null) {
            mNoNotificationsPulldown.setEnabled(status);
        }
        if (mCollapsePanel != null) {
            mCollapsePanel.setEnabled(status);
        }
        if (mQuickPulldown != null) {
            mQuickPulldown.setEnabled(status);
        }
        if (mQsTilesStyle != null) {
            mQsTilesStyle.setEnabled(status);
        }
        if (mTilePicker != null) {
            mTilePicker.setEnabled(status);
        }
        if (mFloatingWindow != null) {
            mFloatingWindow.setEnabled(status);
        }
        if (findPreference(Settings.System.QS_COLLAPSE_PANEL) != null) {
            findPreference(Settings.System.QS_COLLAPSE_PANEL).setEnabled(status);
        }
        if (findPreference(Settings.System.QS_DYNAMIC_ALARM) != null) {
            findPreference(Settings.System.QS_DYNAMIC_ALARM).setEnabled(status);
        }
        if (findPreference(Settings.System.QS_DYNAMIC_BUGREPORT) != null) {
            findPreference(Settings.System.QS_DYNAMIC_BUGREPORT).setEnabled(status);
        }
        if (findPreference(Settings.System.QS_DYNAMIC_IME) != null) {
            findPreference(Settings.System.QS_DYNAMIC_IME).setEnabled(status);
        }
        if (findPreference(Settings.System.QS_DYNAMIC_USBTETHER) != null) {
            findPreference(Settings.System.QS_DYNAMIC_USBTETHER).setEnabled(status);
        }
        if (findPreference(Settings.System.QS_DYNAMIC_WIFI) != null) {
            findPreference(Settings.System.QS_DYNAMIC_WIFI).setEnabled(status);
        }
    }

}
