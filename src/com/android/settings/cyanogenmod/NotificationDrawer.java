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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.text.Spannable;
import android.widget.EditText;
import android.os.UserHandle;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.quicklaunch.BookmarkPicker;

import java.net.URISyntaxException;

import com.android.settings.omni.preference.AppSelectListPreference;

import com.android.internal.util.slim.DeviceUtils;

public class NotificationDrawer extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String CLOCK_SHORTCUT = "clock_shortcut";
    private static final String CALENDAR_SHORTCUT = "calendar_shortcut";
    private static final String TAG = "NotificationDrawer";

    private static final String UI_COLLAPSE_BEHAVIOUR = "notification_drawer_collapse_on_dismiss";
    private static final String STATUS_BAR_CUSTOM_HEADER = "custom_status_bar_header";
    private static final String SWIPE_TO_SWITCH_SCREEN_DETECTION = "full_swipe_to_switch_detection";
    private static final String PREF_NOTIFICATION_SHOW_WIFI_SSID = "notification_show_wifi_ssid";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_NOTI_REMINDER_SOUND =  "noti_reminder_sound";
    private static final String PREF_NOTI_REMINDER_ENABLED = "noti_reminder_enabled";
    private static final String PREF_NOTI_REMINDER_INTERVAL = "noti_reminder_interval";
    private static final String PREF_NOTI_REMINDER_RINGTONE = "noti_reminder_ringtone";
    private static final String PREF_NOTIFICATION_HIDE_CARRIER = "notification_hide_carrier";

    private ListPreference mCollapseOnDismiss;
    private CheckBoxPreference mStatusBarCustomHeader;
    private CheckBoxPreference mFullScreenDetection;
    private CheckBoxPreference mShowWifiName;
    private AppSelectListPreference mClockShortcut;
    private AppSelectListPreference mCalendarShortcut;
    private Preference mCustomLabel;
    CheckBoxPreference mReminder;
    ListPreference mReminderInterval;
    ListPreference mReminderMode;
    RingtonePreference mReminderRingtone;
    CheckBoxPreference mHideCarrier;

    String mCustomLabelText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_drawer);
        PreferenceScreen prefScreen = getPreferenceScreen();

        // Notification drawer
        int collapseBehaviour = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_COLLAPSE_ON_DISMISS,
                Settings.System.STATUS_BAR_COLLAPSE_IF_NO_CLEARABLE);
        mClockShortcut = (AppSelectListPreference) findPreference(CLOCK_SHORTCUT);
        mClockShortcut.setOnPreferenceChangeListener(this);

        mCalendarShortcut = (AppSelectListPreference) findPreference(CALENDAR_SHORTCUT);
        mCalendarShortcut.setOnPreferenceChangeListener(this);

        updateClockCalendarSummary();
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

        mShowWifiName = (CheckBoxPreference) findPreference(PREF_NOTIFICATION_SHOW_WIFI_SSID);
        mShowWifiName.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_SHOW_WIFI_SSID, 0) == 1);
        mShowWifiName.setOnPreferenceChangeListener(this);

        // Custom Carrier Label Text
        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mHideCarrier = (CheckBoxPreference) findPreference(PREF_NOTIFICATION_HIDE_CARRIER);
        boolean hideCarrier = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NOTIFICATION_HIDE_CARRIER, 0) == 1;
        mHideCarrier.setChecked(hideCarrier);
        mHideCarrier.setOnPreferenceChangeListener(this);

        // Notification Remider
        mReminder = (CheckBoxPreference) findPreference(PREF_NOTI_REMINDER_ENABLED);
        mReminder.setChecked(Settings.System.getIntForUser(getContentResolver(),
                Settings.System.REMINDER_ALERT_ENABLED, 0, UserHandle.USER_CURRENT) == 1);
        mReminder.setOnPreferenceChangeListener(this);

        mReminderInterval = (ListPreference) findPreference(PREF_NOTI_REMINDER_INTERVAL);
        int interval = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.REMINDER_ALERT_INTERVAL, 0, UserHandle.USER_CURRENT);
        mReminderInterval.setOnPreferenceChangeListener(this);
        updateReminderIntervalSummary(interval);

        mReminderMode = (ListPreference) findPreference(PREF_NOTI_REMINDER_SOUND);
        int mode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.REMINDER_ALERT_NOTIFY, 0, UserHandle.USER_CURRENT);
        mReminderMode.setValue(String.valueOf(mode));
        mReminderMode.setOnPreferenceChangeListener(this);
        updateReminderModeSummary(mode);

        mReminderRingtone =
                (RingtonePreference) findPreference(PREF_NOTI_REMINDER_RINGTONE);
        Uri ringtone = null;
        String ringtoneString = Settings.System.getStringForUser(getContentResolver(),
                Settings.System.REMINDER_ALERT_RINGER, UserHandle.USER_CURRENT);
        if (ringtoneString == null) {
            // Value not set, defaults to Default Ringtone
            ringtone = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_RINGTONE);
        } else {
            ringtone = Uri.parse(ringtoneString);
        }
        Ringtone alert = RingtoneManager.getRingtone(getActivity(), ringtone);
        mReminderRingtone.setSummary(alert.getTitle(getActivity()));
        mReminderRingtone.setOnPreferenceChangeListener(this);
        mReminderRingtone.setEnabled(mode != 0);

    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);

            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.android.settins.LABEL_CHANGED");
                    getActivity().sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mClockShortcut) {
            String value = (String) objValue;
            // a value of null means to use the default
            Settings.System.putString(getContentResolver(), Settings.System.CLOCK_SHORTCUT, value);
            updateClockCalendarSummary();
        } else if (preference == mCalendarShortcut) {
            String value = (String) objValue;
            // a value of null means to use the default
            Settings.System.putString(getContentResolver(), Settings.System.CALENDAR_SHORTCUT, value);
            updateClockCalendarSummary();
        } else if (preference == mCollapseOnDismiss) {
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
        } else if (preference == mShowWifiName) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                Settings.System.NOTIFICATION_SHOW_WIFI_SSID, value ? 1 : 0);
            return true;
        } else if (preference == mReminder) {
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.REMINDER_ALERT_ENABLED,
                    (Boolean) objValue ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mReminderInterval) {
            int interval = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.REMINDER_ALERT_INTERVAL,
                    interval, UserHandle.USER_CURRENT);
            updateReminderIntervalSummary(interval);
        } else if (preference == mReminderMode) {
            int mode = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.REMINDER_ALERT_NOTIFY,
                    mode, UserHandle.USER_CURRENT);
            updateReminderModeSummary(mode);
            mReminderRingtone.setEnabled(mode != 0);
            return true;
        } else if (preference == mReminderRingtone) {
            Uri val = Uri.parse((String) objValue);
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), val);
            mReminderRingtone.setSummary(ringtone.getTitle(getActivity()));
            Settings.System.putStringForUser(getContentResolver(),
                    Settings.System.REMINDER_ALERT_RINGER,
                    val.toString(), UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mHideCarrier) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NOTIFICATION_HIDE_CARRIER,
                    (Boolean) objValue ? 1 : 0);
            return true;
        }

        return false;
    }

    private void updateCollapseBehaviourSummary(int setting) {
        String[] summaries = getResources().getStringArray(
                R.array.notification_drawer_collapse_on_dismiss_summaries);
        mCollapseOnDismiss.setSummary(summaries[setting]);
    }

    private void updateReminderIntervalSummary(int value) {
        int resId;
        switch (value) {
            case 1000:
                resId = R.string.noti_reminder_interval_1s;
                break;
            case 2000:
                resId = R.string.noti_reminder_interval_2s;
                break;
            case 2500:
                resId = R.string.noti_reminder_interval_2dot5s;
                break;
            case 3000:
                resId = R.string.noti_reminder_interval_3s;
                break;
            case 3500:
                resId = R.string.noti_reminder_interval_3dot5s;
                break;
            case 4000:
                resId = R.string.noti_reminder_interval_4s;
                break;
            default:
                resId = R.string.noti_reminder_interval_1dot5s;
                break;
        }
        mReminderInterval.setValue(Integer.toString(value));
        mReminderInterval.setSummary(getResources().getString(resId));
    }

    private void updateReminderModeSummary(int value) {
        int resId;
        switch (value) {
            case 1:
                resId = R.string.enabled;
                break;
            case 2:
                resId = R.string.noti_reminder_sound_looping;
                break;
            default:
                resId = R.string.disabled;
                break;
        }
        mReminderMode.setSummary(getResources().getString(resId));
    }

    private void updateClockCalendarSummary() {
        final PackageManager packageManager = getPackageManager();

        mClockShortcut.setSummary(getResources().getString(R.string.default_shortcut));
        mCalendarShortcut.setSummary(getResources().getString(R.string.default_shortcut));

        String clockShortcutIntentUri = Settings.System.getString(getContentResolver(), Settings.System.CLOCK_SHORTCUT);
        if (clockShortcutIntentUri != null) {
            Intent clockShortcutIntent = null;
            try {
                clockShortcutIntent = Intent.parseUri(clockShortcutIntentUri, 0);
            } catch (URISyntaxException e) {
                clockShortcutIntent = null;
            }

            if(clockShortcutIntent != null) {
                ResolveInfo info = packageManager.resolveActivity(clockShortcutIntent, 0);
                if (info != null) {
                    mClockShortcut.setSummary(info.loadLabel(packageManager));
                }
            }
        }

        String calendarShortcutIntentUri = Settings.System.getString(getContentResolver(), Settings.System.CALENDAR_SHORTCUT);
        if (calendarShortcutIntentUri != null) {
            Intent calendarShortcutIntent = null;
            try {
                calendarShortcutIntent = Intent.parseUri(calendarShortcutIntentUri, 0);
            } catch (URISyntaxException e) {
                calendarShortcutIntent = null;
            }

            if(calendarShortcutIntent != null) {
                ResolveInfo info = packageManager.resolveActivity(calendarShortcutIntent, 0);
                if (info != null) {
                    mCalendarShortcut.setSummary(info.loadLabel(packageManager));
                }
            }
        }
    }
}
