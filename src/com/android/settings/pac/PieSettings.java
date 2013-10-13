package com.android.settings.pac;

import android.app.AlertDialog;
import android.content.Context;
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
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class PieSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PIE_ENABLED = "pie_enabled";
    private static final String PIE_GRAVITY = "pie_gravity";
    private static final String PIE_MODE = "pie_mode";
    private static final String PIE_SIZE = "pie_size";
    private static final String PIE_TRIGGER = "pie_trigger";
    private static final String PIE_ANGLE = "pie_angle";
    private static final String PIE_GAP = "pie_gap";
    private static final String PIE_MENU = "pie_menu";
    private static final String PIE_POWER = "pie_power";
    private static final String PIE_LASTAPP = "pie_lastapp";
    private static final String PIE_KILLTASK = "pie_killtask";
    private static final String PIE_APPWINDOW = "pie_appwindow";
    private static final String PIE_SEARCH = "pie_search";
    private static final String PIE_CENTER = "pie_center";
    private static final String PIE_STICK = "pie_stick";

    private SwitchPreference mPieEnabled;
    private ListPreference mPieMode;
    private ListPreference mPieSize;
    private ListPreference mPieGravity;
    private ListPreference mPieTrigger;
    private ListPreference mPieAngle;
    private ListPreference mPieGap;
    private CheckBoxPreference mPieMenu;
    private CheckBoxPreference mPiePower;
    private CheckBoxPreference mPieLastApp;
    private CheckBoxPreference mPieKillTask;
    private CheckBoxPreference mPieAppWindow;
    private CheckBoxPreference mPieSearch;
    private CheckBoxPreference mPieCenter;
    private CheckBoxPreference mPieStick;

    private Context mContext;
    private int mAllowedLocations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pie_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        mPieEnabled = (SwitchPreference) findPreference(PIE_ENABLED);
        mPieEnabled.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ENABLED, 0) == 1);
        mPieEnabled.setOnPreferenceChangeListener(this);

        mPieMenu = (CheckBoxPreference) prefSet.findPreference(PIE_MENU);
        mPieMenu.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MENU, 1) == 1);

        mPiePower = (CheckBoxPreference) prefSet.findPreference(PIE_POWER);
        mPiePower.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_POWER, 0) == 1);

        mPieLastApp = (CheckBoxPreference) prefSet.findPreference(PIE_LASTAPP);
        mPieLastApp.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_LAST_APP, 0) == 1);

        mPieKillTask = (CheckBoxPreference) prefSet.findPreference(PIE_KILLTASK);
        mPieKillTask.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_KILL_TASK, 0) == 1);

        mPieAppWindow = (CheckBoxPreference) prefSet.findPreference(PIE_APPWINDOW);
        mPieAppWindow.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_APP_WINDOW, 0) == 1);

        mPieSearch = (CheckBoxPreference) prefSet.findPreference(PIE_SEARCH);
        mPieSearch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_SEARCH, 1) == 1);

        mPieCenter = (CheckBoxPreference) prefSet.findPreference(PIE_CENTER);
        mPieCenter.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_CENTER, 1) == 1);

        mPieStick = (CheckBoxPreference) prefSet.findPreference(PIE_STICK);
        mPieStick.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_STICK, 1) == 1);

        mPieGravity = (ListPreference) prefSet.findPreference(PIE_GRAVITY);
        int pieGravity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GRAVITY, 3);
        mPieGravity.setValue(String.valueOf(pieGravity));
        mPieGravity.setOnPreferenceChangeListener(this);

        mPieMode = (ListPreference) prefSet.findPreference(PIE_MODE);
        int pieMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MODE, 2);
        mPieMode.setValue(String.valueOf(pieMode));
        mPieMode.setOnPreferenceChangeListener(this);

        mPieSize = (ListPreference) prefSet.findPreference(PIE_SIZE);
        mPieTrigger = (ListPreference) prefSet.findPreference(PIE_TRIGGER);
        try {
            float pieSize = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_SIZE, 1.0f);
            mPieSize.setValue(String.valueOf(pieSize));

            float pieTrigger = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_TRIGGER);
            mPieTrigger.setValue(String.valueOf(pieTrigger));
        } catch(SettingNotFoundException ex) {
            // So what
        }

        mPieSize.setOnPreferenceChangeListener(this);
        mPieTrigger.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefSet.findPreference(PIE_GAP);
        int pieGap = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GAP, 2);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefSet.findPreference(PIE_ANGLE);
        int pieAngle = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setOnPreferenceChangeListener(this);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieEnabled) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ENABLED, (Boolean) newValue ? 1 : 0);
            try {
                if (Settings.System.getInt(getActivity().getContentResolver(),Settings.System.PIE_ENABLED) == 0) {
                    Settings.System.putFloat(getActivity().getContentResolver(),
                            Settings.System.PIE_TRIGGER, 0);
                } else if (Settings.System.getInt(getActivity().getContentResolver(),Settings.System.PIE_ENABLED) == 1) {
                    Settings.System.putFloat(getActivity().getContentResolver(),
                            Settings.System.PIE_TRIGGER, 1);
                }
            } catch (SettingNotFoundException ex) {
                    // So what
            }
            return true;
        } else if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_MODE, pieMode);
            return true;
        } else if (preference == mPieSize) {
            float pieSize = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_SIZE, pieSize);
            return true;
        } else if (preference == mPieGravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GRAVITY, pieGravity);
            return true;
        } else if (preference == mPieAngle) {
            int pieAngle = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ANGLE, pieAngle);
            return true;
        } else if (preference == mPieGap) {
            int pieGap = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GAP, pieGap);
            return true;
        } else if (preference == mPieTrigger) {
            float pieTrigger = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_TRIGGER, pieTrigger);
            return true;
        }
            return false;
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPieMenu) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_MENU, mPieMenu.isChecked() ? 1 : 0);
        } else if (preference == mPiePower) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_POWER, mPiePower.isChecked() ? 1 : 0);
        } else if (preference == mPieLastApp) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_LAST_APP, mPieLastApp.isChecked() ? 1 : 0);
        } else if (preference == mPieKillTask) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_KILL_TASK, mPieKillTask.isChecked() ? 1 : 0);
        } else if (preference == mPieAppWindow) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_APP_WINDOW, mPieAppWindow.isChecked() ? 1 : 0);
        } else if (preference == mPieSearch) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_SEARCH, mPieSearch.isChecked() ? 1 : 0);
        } else if (preference == mPieCenter) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_CENTER, mPieCenter.isChecked() ? 1 : 0);
        } else if (preference == mPieStick) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_STICK, mPieStick.isChecked() ? 1 : 0);
        }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
}
