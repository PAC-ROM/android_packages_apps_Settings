/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import com.android.settings.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class PACStats extends PreferenceFragment
implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener,
Preference.OnPreferenceChangeListener {

    private static final String VIEW_STATS = "pref_view_stats";

    protected static final String PAC_OPT_IN = "pref_pac_opt_in";
    protected static final String PAC_LAST_CHECKED = "pref_pac_checked_in";

    private static final String PAC_STATS_URL = "http://www.pac-rom.com/#Stats";

    private CheckBoxPreference mEnableReporting;
    private Preference mViewStats;
    private Preference btnUninstall;

    private Dialog mOkDialog;
    private boolean mOkClicked;

    private SharedPreferences mPrefs;

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Utilities.SETTINGS_PREF_NAME, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pac_stats);

        mPrefs = getPreferences(this.getActivity());

        PreferenceScreen prefSet = getPreferenceScreen();
        mEnableReporting = (CheckBoxPreference) prefSet.findPreference(PAC_OPT_IN);
        mViewStats = (Preference) prefSet.findPreference(VIEW_STATS);


    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableReporting) {
            if (mEnableReporting.isChecked()) {
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
            } else {
                // Disable reporting
                mPrefs.edit().putBoolean(PAC_OPT_IN, false).apply();
            }
        } else if (preference == mViewStats) {
            // Display the stats page
            Uri uri = Uri.parse(PAC_STATS_URL);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        return false;
    }

    public void onClick(DialogInterface arg0, int arg1) {
        if (arg1 == DialogInterface.BUTTON_POSITIVE) {
            mOkClicked = true;
            mPrefs.edit().putBoolean(PAC_OPT_IN, true).apply();
            ReportingServiceManager.launchService(this.getActivity());
        } else if (arg1 == DialogInterface.BUTTON_NEGATIVE){
            mEnableReporting.setChecked(false);
        } else {
            Uri uri = Uri.parse("http://www.cyanogenmod.com/blog/cmstats-what-it-is-and-why-you-should-opt-in");
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }

    }

    public void onDismiss(DialogInterface arg0) {
        if (!mOkClicked) {
            mEnableReporting.setChecked(false);
        }

    }

}