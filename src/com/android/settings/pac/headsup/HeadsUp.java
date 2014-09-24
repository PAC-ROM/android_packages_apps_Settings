/*
 * Copyright (C) 2012 The CyanogenMod project
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

package com.android.settings.pac.headsup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.os.UserHandle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class HeadsUp extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "HeadsUp";

    // Default timeout for heads up snooze. 5 minutes.
    protected static final int DEFAULT_TIME_HEADS_UP_SNOOZE = 300000;

    private static final String PREF_HEADS_UP_EXPANDED = "heads_up_expanded";
    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";
    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";
    private static final String PREF_HEADS_UP_SHOW_UPDATE = "heads_up_show_update";
    private static final String PREF_HEADS_UP_GRAVITY = "heads_up_gravity";
    private static final String HEADS_UP_BG_COLOR = "heads_up_bg_color";
    private static final String HEADS_UP_TEXT_COLOR = "heads_up_text_color";
    private static final String PREF_HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN = "heads_up_exclude_from_lock_screen";

    private ListPreference mHeadsUpSnoozeTime;
    private ListPreference mHeadsUpTimeOut;
    private CheckBoxPreference mHeadsUpExpanded;
    private CheckBoxPreference mHeadsUpShowUpdates;
    private CheckBoxPreference mHeadsUpGravity;
    private CheckBoxPreference mHeadsExcludeFromLockscreen;

    private ColorPickerPreference mHeadsUpBgColor;
    private ColorPickerPreference mHeadsUpTextColor;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DEFAULT_BACKGROUND_COLOR = 0x00ffffff;
    private static final int DEFAULT_TEXT_COLOR = 0xffffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.heads_up);

        PackageManager pm = getPackageManager();

        mHeadsUpExpanded = (CheckBoxPreference) findPreference(PREF_HEADS_UP_EXPANDED);
        mHeadsUpExpanded.setChecked(Settings.PAC.getIntForUser(getContentResolver(),
                Settings.PAC.HEADS_UP_EXPANDED, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpExpanded.setOnPreferenceChangeListener(this);

        mHeadsUpShowUpdates = (CheckBoxPreference) findPreference(PREF_HEADS_UP_SHOW_UPDATE);
        mHeadsUpShowUpdates.setChecked(Settings.PAC.getIntForUser(getContentResolver(),
                Settings.PAC.HEADS_UP_SHOW_UPDATE, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpShowUpdates.setOnPreferenceChangeListener(this);

        mHeadsUpGravity = (CheckBoxPreference) findPreference(PREF_HEADS_UP_GRAVITY);
        mHeadsUpGravity.setChecked(Settings.PAC.getIntForUser(getContentResolver(),
                Settings.PAC.HEADS_UP_GRAVITY_BOTTOM, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpGravity.setOnPreferenceChangeListener(this);

        mHeadsExcludeFromLockscreen = (CheckBoxPreference) findPreference(PREF_HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN);
        mHeadsExcludeFromLockscreen.setChecked(Settings.PAC.getIntForUser(getContentResolver(),
                Settings.PAC.HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsExcludeFromLockscreen.setOnPreferenceChangeListener(this);

        mHeadsUpSnoozeTime = (ListPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(this);
        int headsUpSnoozeTime = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.HEADS_UP_SNOOZE_TIME, DEFAULT_TIME_HEADS_UP_SNOOZE);
        mHeadsUpSnoozeTime.setValue(String.valueOf(headsUpSnoozeTime));
        updateHeadsUpSnoozeTimeSummary(headsUpSnoozeTime);

        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        int defaultTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_notification_decay", null, null));
        mHeadsUpTimeOut = (ListPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
        mHeadsUpTimeOut.setOnPreferenceChangeListener(this);
        int headsUpTimeOut = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.HEADS_UP_NOTIFCATION_DECAY, defaultTimeOut);
        mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
        updateHeadsUpTimeOutSummary(headsUpTimeOut);

        // Heads Up background color
        mHeadsUpBgColor =
                (ColorPickerPreference) findPreference(HEADS_UP_BG_COLOR);
        mHeadsUpBgColor.setOnPreferenceChangeListener(this);
        final int intColor = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.HEADS_UP_BG_COLOR, 0x00ffffff);
        String hexColor = String.format("#%08x", (0x00ffffff & intColor));
        mHeadsUpBgColor.setSummary(hexColor);
        mHeadsUpBgColor.setNewPreviewColor(intColor);

        // Heads Up text color
        mHeadsUpTextColor =
                (ColorPickerPreference) findPreference(HEADS_UP_TEXT_COLOR);
        mHeadsUpTextColor.setOnPreferenceChangeListener(this);
        final int intTextColor = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.HEADS_UP_TEXT_COLOR, 0x00000000);
        String hexTextColor = String.format("#%08x", (0x00000000 & intTextColor));
        mHeadsUpTextColor.setSummary(hexTextColor);
        mHeadsUpTextColor.setNewPreviewColor(intTextColor);
        setHasOptionsMenu(true);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mHeadsUpSnoozeTime) {
            int headsUpSnoozeTime = Integer.valueOf((String) objValue);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.HEADS_UP_SNOOZE_TIME,
                    headsUpSnoozeTime);
            updateHeadsUpSnoozeTimeSummary(headsUpSnoozeTime);
            return true;
        } else if (preference == mHeadsUpTimeOut) {
            int headsUpTimeOut = Integer.valueOf((String) objValue);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.HEADS_UP_NOTIFCATION_DECAY,
                    headsUpTimeOut);
            updateHeadsUpTimeOutSummary(headsUpTimeOut);
            return true;
        } else if (preference == mHeadsUpExpanded) {
            Settings.PAC.putIntForUser(getContentResolver(),
                    Settings.PAC.HEADS_UP_EXPANDED,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsUpShowUpdates) {
            Settings.PAC.putIntForUser(getContentResolver(),
                    Settings.PAC.HEADS_UP_SHOW_UPDATE,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsUpGravity) {
            Settings.PAC.putIntForUser(getContentResolver(),
                    Settings.PAC.HEADS_UP_GRAVITY_BOTTOM,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsExcludeFromLockscreen) {
            Settings.PAC.putIntForUser(getContentResolver(),
                    Settings.PAC.HEADS_UP_EXCLUDE_FROM_LOCK_SCREEN,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsUpBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.HEADS_UP_BG_COLOR,
                    intHex);
            return true;
        } else if (preference == mHeadsUpTextColor) {
            String hexText = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hexText);
            int intHexText = ColorPickerPreference.convertToColorInt(hexText);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.HEADS_UP_TEXT_COLOR,
                    intHexText);
            return true;
        }

        return false;
    }

    private void updateHeadsUpSnoozeTimeSummary(int value) {
        String summary = value != 0
                ? getResources().getString(R.string.heads_up_snooze_summary, value / 60 / 1000)
                : getResources().getString(R.string.heads_up_snooze_disabled_summary);
        mHeadsUpSnoozeTime.setSummary(summary);
    }

    private void updateHeadsUpTimeOutSummary(int value) {
        String summary = getResources().getString(R.string.heads_up_time_out_summary,
                value / 1000);
        if (value == 0) {
            mHeadsUpTimeOut.setSummary(
                    getResources().getString(R.string.heads_up_time_out_never_summary));
        } else {
            mHeadsUpTimeOut.setSummary(summary);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset_default_message)
                .setIcon(R.drawable.ic_settings_backup)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.reset);
        alertDialog.setMessage(R.string.reset_colors);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        Settings.PAC.putInt(getContentResolver(),
                Settings.PAC.HEADS_UP_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
        mHeadsUpBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);
        Settings.PAC.putInt(getContentResolver(),
                Settings.PAC.HEADS_UP_TEXT_COLOR, 0);
        mHeadsUpTextColor.setNewPreviewColor(DEFAULT_TEXT_COLOR);
    }
}
