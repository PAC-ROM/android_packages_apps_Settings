/*
 * Copyright (C) 2013 SlimRoms Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HeadsUpManager extends Fragment
        implements OnItemClickListener {

    private static final String TAG = "HeadsUpManager";

    private TextView mNoUserAppsInstalled;
    private ListView mAppsList;
    private HeadsUpAppListAdapter mAdapter;
    private List<AppInfo> mApps;

    private PackageManager mPm;
    private Activity mActivity;

    private SharedPreferences mPreferences;

    // holder for package data passed into the adapter
    public static final class AppInfo {
        String title;
        String packageName;
        boolean enabled;
        boolean headsUpEnabled;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mActivity = getActivity();
        mPm = mActivity.getPackageManager();

        return inflater.inflate(R.layout.heads_up_manager, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNoUserAppsInstalled = (TextView) mActivity.findViewById(R.id.error);

        mAppsList = (ListView) mActivity.findViewById(R.id.apps_list);
        mAppsList.setOnItemClickListener(this);

        // get shared preference
        mPreferences = mActivity.getSharedPreferences("heads_up_manager", Activity.MODE_PRIVATE);

        // load apps and construct the list
        loadApps();
        setHasOptionsMenu(true);
    }

    private void loadApps() {
        mApps = loadInstalledApps();

        // if app list is empty inform the user
        // else go ahead and construct the list
        if (mApps == null || mApps.isEmpty()) {
            mNoUserAppsInstalled.setText(R.string.privacy_guard_no_user_apps);
            mNoUserAppsInstalled.setVisibility(View.VISIBLE);
            mAppsList.setVisibility(View.GONE);
        } else {
            mNoUserAppsInstalled.setVisibility(View.GONE);
            mAppsList.setVisibility(View.VISIBLE);
            mAdapter = new HeadsUpAppListAdapter(mActivity, mApps);
            mAppsList.setAdapter(mAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // on click change the heads up status for this item
        final AppInfo app = (AppInfo) parent.getItemAtPosition(position);

Log.e(TAG, "app.headsUpEnabled" + app.headsUpEnabled);

        app.headsUpEnabled = !app.headsUpEnabled;
        mPm.setHeadsUpSetting(app.packageName, app.headsUpEnabled);

Log.e(TAG, "app.packageName" + app.packageName);
Log.e(TAG, "app.headsUpEnabled" + app.headsUpEnabled);

        mAdapter.notifyDataSetChanged();
    }

    /**
    * Uses the package manager to query for all currently installed apps
    * for the list.
    */
    private List<AppInfo> loadInstalledApps() {
        List<AppInfo> apps = new ArrayList<AppInfo>();
        List<PackageInfo> packages = mPm.getInstalledPackages(
            PackageManager.GET_PERMISSIONS | PackageManager.GET_SIGNATURES);
        boolean showSystemApps = shouldShowSystemApps();
        Signature platformCert;

        try {
            PackageInfo sysInfo = mPm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            platformCert = sysInfo.signatures[0];
        } catch (PackageManager.NameNotFoundException e) {
            platformCert = null;
        }

        for (PackageInfo info : packages) {
            final ApplicationInfo appInfo = info.applicationInfo;

            // hide apps signed with the platform certificate to avoid the user
            // shooting himself in the foot
            if (platformCert != null && info.signatures != null
                    && platformCert.equals(info.signatures[0])) {
                continue;
            }

            // skip all system apps if they shall not be included
            if (!showSystemApps && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue;
            }

            AppInfo app = new AppInfo();
            app.title = appInfo.loadLabel(mPm).toString();
            app.packageName = info.packageName;
            app.enabled = appInfo.enabled;
            app.headsUpEnabled = mPm.getHeadsUpSetting(app.packageName);
            apps.add(app);
        }

        // sort the apps by their enabled state, then by title
        Collections.sort(apps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (lhs.enabled != rhs.enabled) {
                    return lhs.enabled ? -1 : 1;
                }
                return lhs.title.compareToIgnoreCase(rhs.title);
            }
        });

        return apps;
    }

    private boolean shouldShowSystemApps() {
        return mPreferences.getBoolean("show_system_apps", false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.heads_up_manager, menu);
        menu.findItem(R.id.show_system_apps).setChecked(shouldShowSystemApps());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_system_apps:
                item.setChecked(!item.isChecked());
                mPreferences.edit().putBoolean("show_system_apps", item.isChecked()).commit();
                loadApps();
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // rebuild the list; the user might have changed settings inbetween
        loadApps();
    }
}
