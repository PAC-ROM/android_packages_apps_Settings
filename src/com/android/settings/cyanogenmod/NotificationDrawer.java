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

package com.android.settings.cyanogenmod;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NotificationDrawer extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "NotificationDrawer";

    // Default timeout for heads up snooze. 5 minutes.
    protected static final int DEFAULT_TIME_HEADS_UP_SNOOZE = 300000;

    private static final String UI_COLLAPSE_BEHAVIOUR = "notification_drawer_collapse_on_dismiss";
    private static final String STATUS_BAR_CUSTOM_HEADER = "custom_status_bar_header";
    private static final String SWIPE_TO_SWITCH_SCREEN_DETECTION = "full_swipe_to_switch_detection";
    private static final String PREF_HEADS_UP_EXPANDED = "heads_up_expanded";
    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";
    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";
    private static final String PREF_HEADS_UP_SHOW_UPDATE = "heads_up_show_update";
    private static final String PREF_HEADS_UP_GRAVITY = "heads_up_gravity";

    private ListPreference mCollapseOnDismiss;
    private ListPreference mHeadsUpSnoozeTime;
    private ListPreference mHeadsUpTimeOut;
    private CheckBoxPreference mStatusBarCustomHeader;
    private CheckBoxPreference mFullScreenDetection;
    private CheckBoxPreference mHeadsUpExpanded;
    private CheckBoxPreference mHeadsUpShowUpdates;
    private CheckBoxPreference mHeadsUpGravity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_drawer);

        PackageManager pm = getPackageManager();

        // Notification drawer
        int collapseBehaviour = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_COLLAPSE_ON_DISMISS,
                Settings.System.STATUS_BAR_COLLAPSE_IF_NO_CLEARABLE);
        mCollapseOnDismiss = (ListPreference) findPreference(UI_COLLAPSE_BEHAVIOUR);
        mCollapseOnDismiss.setValue(String.valueOf(collapseBehaviour));
        mCollapseOnDismiss.setOnPreferenceChangeListener(this);
        updateCollapseBehaviourSummary(collapseBehaviour);

        mStatusBarCustomHeader = (CheckBoxPreference) findPreference(STATUS_BAR_CUSTOM_HEADER);
        mStatusBarCustomHeader.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) == 1);
        mStatusBarCustomHeader.setOnPreferenceChangeListener(this);

        mFullScreenDetection = (CheckBoxPreference) findPreference(SWIPE_TO_SWITCH_SCREEN_DETECTION);
        mFullScreenDetection.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SWIPE_TO_SWITCH_SCREEN_DETECTION, 0) == 1);
        mFullScreenDetection.setOnPreferenceChangeListener(this);

        mHeadsUpExpanded = (CheckBoxPreference) findPreference(PREF_HEADS_UP_EXPANDED);
        mHeadsUpExpanded.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_EXPANDED, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpExpanded.setOnPreferenceChangeListener(this);

        mHeadsUpShowUpdates = (CheckBoxPreference) findPreference(PREF_HEADS_UP_SHOW_UPDATE);
        mHeadsUpShowUpdates.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_SHOW_UPDATE, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpShowUpdates.setOnPreferenceChangeListener(this);

        mHeadsUpGravity = (CheckBoxPreference) findPreference(PREF_HEADS_UP_GRAVITY);
        mHeadsUpGravity.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HEADS_UP_GRAVITY_BOTTOM, 0, UserHandle.USER_CURRENT) == 1);
        mHeadsUpGravity.setOnPreferenceChangeListener(this);

        mHeadsUpSnoozeTime = (ListPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(this);
        int headsUpSnoozeTime = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_SNOOZE_TIME, DEFAULT_TIME_HEADS_UP_SNOOZE);
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
        int headsUpTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFCATION_DECAY, defaultTimeOut);
        mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
        updateHeadsUpTimeOutSummary(headsUpTimeOut);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mCollapseOnDismiss) {
            int value = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_COLLAPSE_ON_DISMISS, value);
            updateCollapseBehaviourSummary(value);
            return true;
        } else if (preference == mStatusBarCustomHeader) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                Settings.System.STATUS_BAR_CUSTOM_HEADER, value ? 1 : 0);
            return true;
        } else if (preference == mFullScreenDetection) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                Settings.System.SWIPE_TO_SWITCH_SCREEN_DETECTION, value ? 1 : 0);
            return true;
        } else if (preference == mHeadsUpSnoozeTime) {
            int headsUpSnoozeTime = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_SNOOZE_TIME,
                    headsUpSnoozeTime);
            updateHeadsUpSnoozeTimeSummary(headsUpSnoozeTime);
            return true;
        } else if (preference == mHeadsUpTimeOut) {
            int headsUpTimeOut = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_NOTIFCATION_DECAY,
                    headsUpTimeOut);
            updateHeadsUpTimeOutSummary(headsUpTimeOut);
            return true;
        } else if (preference == mHeadsUpExpanded) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_EXPANDED,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsUpShowUpdates) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_SHOW_UPDATE,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHeadsUpGravity) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.HEADS_UP_GRAVITY_BOTTOM,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }

        return false;
    }

    private void updateCollapseBehaviourSummary(int setting) {
        String[] summaries = getResources().getStringArray(
                R.array.notification_drawer_collapse_on_dismiss_summaries);
        mCollapseOnDismiss.setSummary(summaries[setting]);
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
}
