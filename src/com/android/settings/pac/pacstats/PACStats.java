/*
 * Copyright (C) 2016 The PAC-ROM Project
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

package com.android.settings.pac.pacstats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import cyanogenmod.providers.CMSettings;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class PACStats extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {

    private static final String PREF_FILE_NAME = "PACStats";
    /* package */ static final String ANONYMOUS_OPT_IN = "pref_pacstats_opt_in";
    /* package */ static final String ANONYMOUS_LAST_CHECKED = "pref_pacstats_checked_in";

    /* package */ static final String KEY_LAST_JOB_ID = "last_job_id";
    /* package */ static final int QUEUE_MAX_THRESHOLD = 1000;

    public static final String KEY_STATS = "stats_collection";

    private static final String VIEW_STATS = "pref_view_stats";
    private static final String PAC_STATS_URL = "http://www.pac-rom.com/#Stats";

    private SwitchPreference mStatsSwitch;
    private Preference mViewStats;
    private Dialog mOkDialog;
    private boolean mOkClicked;

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_FILE_NAME, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pac_stats);
        mStatsSwitch = (SwitchPreference) findPreference(KEY_STATS);
        mViewStats = (Preference) findPreference(VIEW_STATS);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mStatsSwitch) {
            boolean checked = mStatsSwitch.isChecked();
            if (checked) {
                // Display the confirmation dialog
                mOkClicked = false;
                if (mOkDialog != null) {
                    mOkDialog.dismiss();
                }
                mOkDialog = new AlertDialog.Builder(this.getActivity())
                .setMessage(this.getResources().getString(R.string.pac_stats_warning))
                .setTitle(R.string.pac_stats_warning_title)
                .setPositiveButton(android.R.string.yes, this)
                .setNeutralButton(getString(R.string.pac_learn_more), this)
                .setNegativeButton(android.R.string.no, this)
                .show();
                mOkDialog.setOnDismissListener(this);
            }
            // will initiate opt out sequence if necessary
            ReportingServiceManager.setAlarm(getActivity());
            return true;
        } else if (preference == mViewStats) {
            // Display the stats page
            Uri uri = Uri.parse(PAC_STATS_URL);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onClick(DialogInterface arg0, int arg1) {
        if (arg1 == DialogInterface.BUTTON_POSITIVE) {
            mOkClicked = true;
            CMSettings.Secure.putIntForUser(getContentResolver(),
                    CMSettings.Secure.STATS_COLLECTION_REPORTED, 1,
                    UserHandle.USER_OWNER);
        } else if (arg1 == DialogInterface.BUTTON_NEGATIVE){
            mStatsSwitch.setChecked(false);
            // clear opt out flags
            CMSettings.Secure.putIntForUser(getContentResolver(),
                    CMSettings.Secure.STATS_COLLECTION_REPORTED, 0,
                    UserHandle.USER_OWNER);
        } else {
            Uri uri = Uri.parse("http://www.cyanogenmod.com/blog/cmstats-what-it-is-and-why-you-should-opt-in");
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    public void onDismiss(DialogInterface arg0) {
        if (!mOkClicked) {
            mStatsSwitch.setChecked(false);
        }
    }

    public static void updateLastSynced(Context context) {
        getPreferences(context)
                .edit()
                .putLong(ANONYMOUS_LAST_CHECKED,System.currentTimeMillis())
                .commit();
    }

    private static int getLastJobId(Context context) {
        return getPreferences(context).getInt(KEY_LAST_JOB_ID, 0);
    }

    private static void setLastJobId(Context context, int id) {
        getPreferences(context)
                .edit()
                .putInt(KEY_LAST_JOB_ID, id)
                .commit();
    }

    public static int getNextJobId(Context context) {
        int lastId = getLastJobId(context);
        if (lastId >= QUEUE_MAX_THRESHOLD) {
            lastId = 1;
        } else {
            lastId += 1;
        }
        setLastJobId(context, lastId);
        return lastId;
    }

    @Override
    protected int getMetricsCategory() {
        return CMMetricsLogger.ANONYMOUS_STATS;
    }
}
