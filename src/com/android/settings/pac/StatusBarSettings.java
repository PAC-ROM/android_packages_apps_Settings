package com.android.settings.pac;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_MAX_NOTIF = "status_bar_max_notifications";
    private static final String STATUS_BAR_DONOTDISTURB = "status_bar_donotdisturb";
    private static final String STATUS_BAR_TRAFFIC = "status_bar_traffic";
    private static final String KEY_SMS_BREATH = "pref_key_sms_breath";
    private static final String KEY_MISSED_CALL_BREATH = "missed_call_breath";
    private static final String KEY_NOTIFICATION_BEHAVIOUR = "notifications_behaviour";
    private static final String STATUS_BAR_AUTO_HIDE = "status_bar_auto_hide";
    private static final String PREF_FULLSCREEN_STATUSBAR = "fullscreen_statusbar";
    private static final String FREF_FULLSCREEN_STATUSBAR_TIMEOUT = "fullscreen_statusbar_timeout";

    private CheckBoxPreference mStatusBarDoNotDisturb;
    private CheckBoxPreference mStatusBarTraffic;
    private CheckBoxPreference mFullScreenStatusBar;
    private ListPreference mStatusBarMaxNotif;
    private ListPreference mNotificationsBehavior;
    private CheckBoxPreference mSMSBreath;
    private CheckBoxPreference mMissedCallBreath;
    private ListPreference mStatusBarAutoHide;
    private ListPreference mFullScreenStatusBarTimeout;

    private Context mContext;
    private int mAllowedLocations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();

        addPreferencesFromResource(R.xml.status_bar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        mStatusBarMaxNotif = (ListPreference) prefSet.findPreference(STATUS_BAR_MAX_NOTIF);
        int maxNotIcons = Settings.System.getInt(getActivity().getContentResolver(),
        Settings.System.MAX_NOTIFICATION_ICONS, 2);
        mStatusBarMaxNotif.setValue(String.valueOf(maxNotIcons));
            mStatusBarMaxNotif.setOnPreferenceChangeListener(this);

        mNotificationsBehavior = (ListPreference) findPreference(KEY_NOTIFICATION_BEHAVIOUR);
        int CurrentBehavior = Settings.System.getInt(getContentResolver(), Settings.System.NOTIFICATIONS_BEHAVIOUR, 0);
        mNotificationsBehavior.setValue(String.valueOf(CurrentBehavior));
        mNotificationsBehavior.setSummary(mNotificationsBehavior.getEntry());
        mNotificationsBehavior.setOnPreferenceChangeListener(this);

        mSMSBreath = (CheckBoxPreference) findPreference(KEY_SMS_BREATH);
        mSMSBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.SMS_BREATH, 0) == 1);

        mMissedCallBreath = (CheckBoxPreference) findPreference(KEY_MISSED_CALL_BREATH);
        mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.MISSED_CALL_BREATH, 0) == 1);

        mStatusBarAutoHide = (ListPreference) prefSet.findPreference(STATUS_BAR_AUTO_HIDE);
        int statusBarAutoHideValue = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.AUTO_HIDE_STATUSBAR, 0);
        mStatusBarAutoHide.setValue(String.valueOf(statusBarAutoHideValue));
        updateStatusBarAutoHideSummary(statusBarAutoHideValue);
        mStatusBarAutoHide.setOnPreferenceChangeListener(this);

        mFullScreenStatusBarTimeout = (ListPreference) findPreference(FREF_FULLSCREEN_STATUSBAR_TIMEOUT);
        mFullScreenStatusBarTimeout.setOnPreferenceChangeListener(this);
        mFullScreenStatusBarTimeout.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.FULLSCREEN_STATUSBAR_TIMEOUT, 10000) + "");

        mStatusBarDoNotDisturb = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_DONOTDISTURB);
        mStatusBarDoNotDisturb.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_DONOTDISTURB, 0) == 1));

        mStatusBarTraffic = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC);
        mStatusBarTraffic.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_TRAFFIC, false));

        mFullScreenStatusBar = (CheckBoxPreference)findPreference(PREF_FULLSCREEN_STATUSBAR);
        mFullScreenStatusBar.setChecked(Settings.System.getBoolean(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.FULLSCREEN_STATUSBAR, true));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarMaxNotif) {
            int maxNotIcons = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MAX_NOTIFICATION_ICONS, maxNotIcons);
            return true;
        } else if (preference == mNotificationsBehavior) {
            String val = (String) newValue;
                     Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATIONS_BEHAVIOUR,
            Integer.valueOf(val));
            int index = mNotificationsBehavior.findIndexOfValue(val);
            mNotificationsBehavior.setSummary(mNotificationsBehavior.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarAutoHide) {
            int statusBarAutoHideValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.AUTO_HIDE_STATUSBAR, statusBarAutoHideValue);
            updateStatusBarAutoHideSummary(statusBarAutoHideValue);
            return true;
        } else if (preference == mFullScreenStatusBarTimeout) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FULLSCREEN_STATUSBAR_TIMEOUT, val);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mStatusBarDoNotDisturb) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DONOTDISTURB,
                    mStatusBarDoNotDisturb.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mStatusBarTraffic) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC, mStatusBarTraffic.isChecked());
            return true;
        } else if (preference == mFullScreenStatusBar) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FULLSCREEN_STATUSBAR,
                    mFullScreenStatusBar.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mSMSBreath) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.SMS_BREATH,
                    mSMSBreath.isChecked() ? 1 : 0);
           return true;
         } else if (preference == mMissedCallBreath) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.MISSED_CALL_BREATH,
                    mMissedCallBreath.isChecked() ? 1 : 0);
           return true;
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    private void updateStatusBarAutoHideSummary(int value) {
        if (value == 0) {
            /* StatusBar AutoHide deactivated */
            mStatusBarAutoHide.setSummary(getResources().getString(R.string.auto_hide_statusbar_off));
        } else {
            mStatusBarAutoHide.setSummary(getResources().getString(value == 1
                    ? R.string.auto_hide_statusbar_summary_nonperm
                    : R.string.auto_hide_statusbar_summary_all));
        }
    }

}
