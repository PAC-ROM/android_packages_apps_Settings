/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.android.settings.pac;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.pac.utils.SeekBarPreferenceCham;
import com.android.settings.pac.utils.AppMultiSelectListPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import com.android.internal.util.pac.AwesomeAnimationHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ListViewAnimationInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ListViewAnimationInterfaceSettings";

    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_CACHE = "listview_cache";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String LISTVIEW_ANIM_DURATION = "listview_anim_duration";
    private static final String KEY_LISTVIEW_EXCLUDED_APPS = "listview_blacklist";

    private static final int MENU_RESET = Menu.FIRST;

    private ContentResolver mResolver;
    private Context mContext;
    private SeekBarPreferenceCham mListViewDuration;
    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;
    private ListPreference mListViewCache;
    private AppMultiSelectListPreference mExcludedAppsPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.listview_animation_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();

        mListViewAnimation = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_ANIMATION);
        if (getProperVal(mListViewAnimation) != null) {
             mListViewAnimation.setValue(getProperVal(mListViewAnimation));
             mListViewAnimation.setSummary(getListAnimationName(Integer.valueOf(getProperVal(mListViewAnimation))));
        }
        mListViewAnimation.setOnPreferenceChangeListener(this);

        mListViewCache = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_CACHE);
        if (getProperVal(mListViewCache) != null) {
             mListViewCache.setValue(getProperVal(mListViewCache));
             mListViewCache.setSummary(getListCacheName(Integer.valueOf(getProperVal(mListViewCache))));
        }
        mListViewCache.setOnPreferenceChangeListener(this);

        mListViewInterpolator = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_INTERPOLATOR);
        if (getProperVal(mListViewInterpolator) != null) {
             mListViewInterpolator.setValue(getProperVal(mListViewInterpolator));
             mListViewInterpolator.setSummary(getListInterpolatorName(Integer.valueOf(getProperVal(mListViewInterpolator))));
        }
        mListViewInterpolator.setOnPreferenceChangeListener(this);

        int listviewDuration = Settings.PAC.getInt(mResolver,
                Settings.PAC.LISTVIEW_DURATION, 25);
        mListViewDuration = (SeekBarPreferenceCham) prefSet.findPreference(LISTVIEW_ANIM_DURATION);
        mListViewDuration.setValue(listviewDuration);
        mListViewDuration.setOnPreferenceChangeListener(this);

        mExcludedAppsPref = (AppMultiSelectListPreference) prefSet.findPreference(KEY_LISTVIEW_EXCLUDED_APPS);
        Set<String> excludedApps = getExcludedApps();
        if (excludedApps != null) mExcludedAppsPref.setValues(excludedApps);
        mExcludedAppsPref.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog  = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.profile_reset_title);
        alertDialog.setMessage(R.string.animation_settings_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetAllValues();
                resetAllSettings();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetAllValues() {
        mListViewDuration.setValue(25);
        mListViewAnimation.setValue("0");
        mListViewInterpolator.setValue("0");
        mListViewCache.setValue("0");
    }

    private void resetAllSettings() {
        setProperVal(mListViewDuration, 25);
        setProperVal(mListViewAnimation, 0);
        mListViewAnimation.setSummary(getListAnimationName(0));
        setProperVal(mListViewInterpolator, 0);
        mListViewInterpolator.setSummary(getListInterpolatorName(0));
        setProperVal(mListViewCache, 0);
        mListViewCache.setSummary(getListCacheName(0));
        Settings.PAC.putString(getContentResolver(),
                Settings.PAC.LISTVIEW_ANIMATION_EXCLUDED_APPS, "");
        mExcludedAppsPref.setClearValues();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mListViewAnimation) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver, Settings.PAC.LISTVIEW_ANIMATION, val);
            mListViewAnimation.setSummary(getListAnimationName(val));
        } else if (preference == mListViewCache) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver, Settings.PAC.LISTVIEW_ANIMATION_CACHE, val);
            mListViewCache.setSummary(getListCacheName(val));
        } else if (preference == mListViewInterpolator) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver, Settings.PAC.LISTVIEW_INTERPOLATOR, val);
            mListViewInterpolator.setSummary(getListInterpolatorName(val));
        } else if (preference == mExcludedAppsPref) {
            storeExcludedApps((Set<String>) objValue);
        } else if (preference == mListViewDuration) {
            int val = ((Integer)objValue).intValue();
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.LISTVIEW_DURATION,
                    val);
        } else {
            return false;
        }
        return true;
    }

    private void setProperVal(Preference preference, int val) {
        String mString = "";
        if (preference == mListViewAnimation) {
            mString = Settings.PAC.LISTVIEW_ANIMATION;
        } else if (preference == mListViewCache) {
            mString = Settings.PAC.LISTVIEW_ANIMATION_CACHE;
        } else if (preference == mListViewInterpolator) {
            mString = Settings.PAC.LISTVIEW_INTERPOLATOR;
        } else if (preference == mListViewDuration) {
            mString = Settings.PAC.LISTVIEW_DURATION;
        }

        Settings.PAC.putInt(mContext.getContentResolver(), mString, val);
    }

    private String getProperVal(Preference preference) {
        String mString = "";
        if (preference == mListViewAnimation) {
            mString = Settings.PAC.LISTVIEW_ANIMATION;
        } else if (preference == mListViewCache) {
            mString = Settings.PAC.LISTVIEW_ANIMATION_CACHE;
        } else if (preference == mListViewInterpolator) {
            mString = Settings.PAC.LISTVIEW_INTERPOLATOR;
        }

        return Settings.PAC.getString(mContext.getContentResolver(), mString);
    }

    private String getListAnimationName(int index) {
        String[] str = mContext.getResources().getStringArray(R.array.listview_animation_entries);
        return str[index];
    }

    private String getListCacheName(int index) {
        String[] str = mContext.getResources().getStringArray(R.array.listview_cache_entries);
        return str[index];
    }

    private String getListInterpolatorName(int index) {
        String[] str = mContext.getResources().getStringArray(R.array.listview_interpolator_entries);
        return str[index];
    }

    private Set<String> getExcludedApps() {
        String excluded = Settings.PAC.getString(getContentResolver(),
                Settings.PAC.LISTVIEW_ANIMATION_EXCLUDED_APPS);
        if (TextUtils.isEmpty(excluded))
            return null;

        return new HashSet<String>(Arrays.asList(excluded.split("\\|")));
    }

    private void storeExcludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.PAC.putString(getContentResolver(),
                Settings.PAC.LISTVIEW_ANIMATION_EXCLUDED_APPS, builder.toString());
    }

}
