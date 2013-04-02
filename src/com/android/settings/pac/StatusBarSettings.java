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
    private static final String PREF_NOTIFICATION_QUICK_SETTINGS = "quick_settings_panel";
    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String NO_NOTIFICATIONS_PULLDOWN = "no_notifications_pulldown";
    private static final String DISABLE_PANEL = "disable_quick_settings";
    

    private Preference mQuickSettings;
    private CheckBoxPreference mStatusBarDoNotDisturb;
    private ListPreference mStatusBarMaxNotif;
    private CheckBoxPreference mNoNotificationsPulldown;
    private CheckBoxPreference mDisablePanel;
    private ListPreference mQuickPulldown;
    private Context mContext;
    private int mAllowedLocations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();
        mQuickPulldown = (ListPreference) prefSet.findPreference(QUICK_PULLDOWN);
        mNoNotificationsPulldown = (CheckBoxPreference) prefSet.findPreference(NO_NOTIFICATIONS_PULLDOWN);
        mDisablePanel = (CheckBoxPreference) prefSet.findPreference(DISABLE_PANEL);
        
        if (!Utils.isPhone(getActivity())) {
            if(mQuickPulldown != null)
                prefSet.removePreference(mQuickPulldown);
            if(mDisablePanel != null)
                prefSet.removePreference(mDisablePanel);
            if(mNoNotificationsPulldown != null)
                prefSet.removePreference(mNoNotificationsPulldown);
        } else {
            mQuickPulldown.setOnPreferenceChangeListener(this);
            int quickPulldownValue = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.QS_QUICK_PULLDOWN, 0);
            mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
            updatePulldownSummary(quickPulldownValue);

            mDisablePanel.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QS_DISABLE_PANEL, 0) == 0);
            mNoNotificationsPulldown.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QS_NO_NOTIFICATION_PULLDOWN, 0) == 1);
        }
           mStatusBarMaxNotif = (ListPreference) prefSet.findPreference(STATUS_BAR_MAX_NOTIF);
           int maxNotIcons = Settings.System.getInt(getActivity().getContentResolver(),
           Settings.System.MAX_NOTIFICATION_ICONS, 2);
           mStatusBarMaxNotif.setValue(String.valueOf(maxNotIcons));
           mStatusBarMaxNotif.setOnPreferenceChangeListener(this);
           mStatusBarDoNotDisturb = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_DONOTDISTURB);
           mStatusBarDoNotDisturb.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
            Settings.System.STATUS_BAR_DONOTDISTURB, 0) == 1));
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mStatusBarMaxNotif) {
            int maxNotIcons = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MAX_NOTIFICATION_ICONS, maxNotIcons);
            return true;
            } else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.QS_QUICK_PULLDOWN,
                    quickPulldownValue);
            updatePulldownSummary(quickPulldownValue);
            return true;
            } 
        return false;
	}
	
	private void updateQuickSettingsDescription() {
           if (Settings.System.getInt(getActivity().getContentResolver(),
                   Settings.System.QS_DISABLE_PANEL, 0) == 0) {
               mQuickSettings.setSummary(getString(R.string.quick_settings_enabled));
           } else {
               mQuickSettings.setSummary(getString(R.string.quick_settings_disabled));
           }
        }
        
        private void updatePulldownSummary(int value) {

          if (value == 0) {
            /* quick pulldown deactivated */
            mQuickPulldown.setSummary(getResources().getString(R.string.quick_pulldown_off));
          } else {
            String direction = getResources().getString(value == 2
                    ? R.string.quick_pulldown_summary_left
                    : R.string.quick_pulldown_summary_right);
            mQuickPulldown.setSummary(getResources().getString(R.string.summary_quick_pulldown, direction));
          }
        }



        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mStatusBarDoNotDisturb) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DONOTDISTURB,
                    mStatusBarDoNotDisturb.isChecked() ? 1 : 0);
            return true;
        }  else if (preference == mNoNotificationsPulldown) {
            Settings.System.putInt(getActivity().getContentResolver(), 
                    Settings.System.QS_NO_NOTIFICATION_PULLDOWN,
                    mNoNotificationsPulldown.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mDisablePanel) {
            Settings.System.putInt(getActivity().getContentResolver(), 
                    Settings.System.QS_DISABLE_PANEL,
                    mDisablePanel.isChecked() ? 0 : 1);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
	}
}
