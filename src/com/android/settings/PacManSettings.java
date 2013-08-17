package com.android.settings;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Vibrator;
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

import java.util.List;

public class PacManSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TRDS = "trds_settings";
    private static final String LAUNCHER = "homescreen_settings";
    private static final String LED = "led";
    private static final String VIBRATIONS = "vibrations";
    private static final String KEY_HARDWARE_KEYS = "hardware_keys";

    private SwitchPreference mTRDS;
    private PreferenceScreen header;

    private Context mContext;
    private int mAllowedLocations;

    // list off apps which we restart just to be sure due that AOSP
    // does not every time reload all resources on onConfigurationChanged
    // or because some apps are just not programmed well on that part.
    private String mTRDSApps[] = new String[] {
        "com.android.contacts",
        "com.android.calendar",
        "com.android.email",
        "com.android.vending",
        "com.android.mms",
        "com.google.android.talk",
        "com.google.android.gm",
        "com.google.android.googlequicksearchbox",
        "com.google.android.youtube",
        "com.google.android.apps.genie.geniewidget",
        "com.google.android.apps.plus",
        "com.google.android.apps.maps"
    };

    Vibrator mVibrator;
    private static boolean hasNotificationLed;
    public boolean hasButtons() {
        return !getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pac_man_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        hasNotificationLed = getResources().getBoolean(R.bool.has_notification_led);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        mTRDS = (SwitchPreference) prefSet.findPreference(TRDS);
        mTRDS.setChecked(Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.UI_INVERTED_MODE, 1) == 2);
        mTRDS.setOnPreferenceChangeListener(this);

        header = (PreferenceScreen) prefSet.findPreference(LAUNCHER);

        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        launcherIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent launcherPrefsIntent = new Intent(Intent.ACTION_MAIN);
        launcherPrefsIntent.addCategory("com.cyanogenmod.category.LAUNCHER_PREFERENCES");

        final PackageManager pm = getPackageManager();
        ActivityInfo defaultLauncher = pm.resolveActivity(launcherIntent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo;

        launcherPrefsIntent.setPackage(defaultLauncher.packageName);
        ResolveInfo launcherPrefs = pm.resolveActivity(launcherPrefsIntent, 0);
        if (launcherPrefs == null) {
            prefSet.removePreference(findPreference(LAUNCHER));
        }

        if (!hasNotificationLed) {
            prefSet.removePreference(findPreference(LED));
        }

        if (mVibrator == null || !mVibrator.hasVibrator()) {
            prefSet.removePreference(findPreference(VIBRATIONS));
        }

        if (!hasButtons()) {
            prefSet.removePreference(findPreference(KEY_HARDWARE_KEYS));
        }

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTRDS) {
        boolean value = ((Boolean)newValue).booleanValue();
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.UI_INVERTED_MODE,
                value ? 2 : 1);

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
            for(int i = 0; i < pids.size(); i++) {
                ActivityManager.RunningAppProcessInfo info = pids.get(i);

                for (int j = 0; j < mTRDSApps.length; j++) {
                    if(info.processName.equalsIgnoreCase(mTRDSApps[j])) {
                        am.killBackgroundProcesses(mTRDSApps[j]);
                    }
                }
            }

        return true;
        }
    return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == header) {

            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.addCategory(Intent.CATEGORY_HOME);
            launcherIntent.addCategory(Intent.CATEGORY_DEFAULT);

            Intent launcherPrefsIntent = new Intent(Intent.ACTION_MAIN);
            launcherPrefsIntent.addCategory("com.cyanogenmod.category.LAUNCHER_PREFERENCES");

            final PackageManager pm = getPackageManager();
            ActivityInfo defaultLauncher = pm.resolveActivity(launcherIntent,
                    PackageManager.MATCH_DEFAULT_ONLY).activityInfo;

            launcherPrefsIntent.setPackage(defaultLauncher.packageName);
            ResolveInfo launcherPrefs = pm.resolveActivity(launcherPrefsIntent, 0);

            Intent intent=new Intent().setClassName(
                            launcherPrefs.activityInfo.packageName,
                            launcherPrefs.activityInfo.name);
            startActivity(intent);

            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
