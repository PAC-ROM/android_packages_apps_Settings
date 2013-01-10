/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.DialogCreatable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.HashMap;
import java.util.List;

public class EdgeSwipeNavigationBlacklist extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, DialogCreatable {

    private static final String TAG = "UserDetailsSettings";

    private static final int MENU_REMOVE_USER = Menu.FIRST;
    private static final int DIALOG_CONFIRM_REMOVE = 1;

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_INSTALLED_APPS = "market_apps_category";
    private static final String KEY_SYSTEM_APPS = "system_apps_category";
    public static final String EXTRA_USER_ID = "user_id";

    private static final String[] SYSTEM_APPS = {
            "com.google.android.browser",
            "com.google.android.gm",
            "com.google.android.youtube"
    };

    static class AppState {
        boolean dirty;
        boolean enabled;

        AppState(boolean enabled) {
            this.enabled = enabled;
        }
    }

    private HashMap<String, AppState> mAppStates = new HashMap<String, AppState>();
    private PreferenceGroup mInstalledAppGroup;

    private IPackageManager mIPm;
    private PackageManager mPm;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.edge_swipe_blacklist);

        mIPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

        mInstalledAppGroup = (PreferenceGroup) findPreference(KEY_INSTALLED_APPS);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPm = getActivity().getPackageManager();
        refreshApps();
    }

    private void insertAppInfo(PreferenceGroup group, HashMap<String, AppState> appStateMap,
            PackageInfo info, boolean defaultState) {
        if (info != null) {
            String pkgName = info.packageName;
            String name = info.applicationInfo.loadLabel(mPm).toString();
            Drawable icon = info.applicationInfo.loadIcon(mPm);
            AppState appState = appStateMap.get(info.packageName);
            boolean enabled = appState == null ? defaultState : appState.enabled;
            CheckBoxPreference appPref = new CheckBoxPreference(getActivity());
            appPref.setTitle(name != null ? name : pkgName);
            appPref.setIcon(icon);
            String buttons = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.GESTURE_BLACKLIST);
            appPref.setChecked(buttons == null ? false : buttons.contains(pkgName));
            appPref.setKey(pkgName);
            appPref.setPersistent(false);
            appPref.setOnPreferenceChangeListener(this);
            group.addPreference(appPref);
        }
    }

    private void refreshApps() {
        mInstalledAppGroup.removeAll();

        boolean firstTime = mAppStates.isEmpty();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mPm.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo resolveInfo : apps) {
            PackageInfo info;
            try {
                info = mIPm.getPackageInfo(resolveInfo.activityInfo.packageName,
                        0 /* flags */, 0);
            } catch (RemoteException re) {
                continue;
            }
            if (firstTime) {
                mAppStates.put(resolveInfo.activityInfo.packageName,
                        new AppState(info.applicationInfo.enabled));
            }

            if (mInstalledAppGroup.findPreference(info.packageName) != null) {
                continue;
            }
            insertAppInfo(mInstalledAppGroup, mAppStates, info, false);

        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof CheckBoxPreference) {
            String packageName = preference.getKey();
            String buttons = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.GESTURE_BLACKLIST);
            if ((Boolean) newValue) {
                if (buttons != null) buttons.concat(packageName + "|");
                else buttons = packageName + "|";
            } else if (buttons != null && buttons.contains(packageName)) {
                buttons.replace(packageName + "|", "");
            }

            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.GESTURE_BLACKLIST, buttons);

        }
        return true;
    }
}
