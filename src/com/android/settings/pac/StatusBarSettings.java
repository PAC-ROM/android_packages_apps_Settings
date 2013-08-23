package com.android.settings.pac;

import android.app.AlertDialog;
import android.content.Context;
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
    private static final String KEY_NOTIFICATION_BEHAVIOUR = "notifications_behaviour";
    private static final String STATUS_BAR_AUTO_HIDE = "status_bar_auto_hide";

    private CheckBoxPreference mStatusBarDoNotDisturb;
    private CheckBoxPreference mStatusBarTraffic;
    private ListPreference mStatusBarMaxNotif;
    private ListPreference mNotificationsBehavior;
    private CheckBoxPreference mStatusBarAutoHide;

    private Context mContext;
    private int mAllowedLocations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

           mStatusBarDoNotDisturb = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_DONOTDISTURB);
           mStatusBarDoNotDisturb.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                   Settings.System.STATUS_BAR_DONOTDISTURB, 0) == 1));

           mStatusBarTraffic = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC);
           mStatusBarTraffic.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                   Settings.System.STATUS_BAR_TRAFFIC, false));

           mStatusBarAutoHide = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_AUTO_HIDE);
           mStatusBarAutoHide.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                   Settings.System.AUTO_HIDE_STATUSBAR, 0) == 1));
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
        } else if (preference == mStatusBarAutoHide) {
            value = mStatusBarAutoHide.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.AUTO_HIDE_STATUSBAR, value ? 1 : 0);
             return true;
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
