/*
 * Copyright (C) 2014-2015 The PAC-ROM Project
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.settings.pac.utils.AppMultiSelectListPreference;
import com.android.settings.pac.utils.SeekBarPreferenceCham;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import com.android.internal.util.pac.AwesomeAnimationHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnimationSettings extends SettingsPreferenceFragment  implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AnimationSettings";


    private static final String ANIMATION_CONTROLS_EXIT_ONLY = "animation_controls_exit_only";
    private static final String ANIMATION_CONTROLS_REVERSE_EXIT = "animation_controls_reverse_exit";
    private static final String ACTIVITY_OPEN = "activity_open";
    private static final String ACTIVITY_CLOSE = "activity_close";
    private static final String TASK_OPEN = "task_open";
    private static final String TASK_CLOSE = "task_close";
    private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
    private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
    private static final String ANIMATION_DURATION = "animation_duration";
    private static final String WALLPAPER_OPEN = "wallpaper_open";
    private static final String WALLPAPER_CLOSE = "wallpaper_close";
    private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
    private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";
    private static final String TASK_OPEN_BEHIND = "task_open_behind";
    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_CACHE = "listview_cache";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String LISTVIEW_ANIM_DURATION = "listview_anim_duration";
    private static final String KEY_LISTVIEW_EXCLUDED_APPS = "listview_blacklist";
    private static final String PREF_TOAST_ANIMATION = "toast_animation";
    private static final String PREF_TOAST_TEST_ANIMATION = "toast_test_animation";

    private static final int MENU_RESET = Menu.FIRST;

    private ContentResolver mResolver;
    private Context mContext;

    private SwitchPreference mAnimExitOnly;
    private SwitchPreference mAnimReverseExit;
    private ListPreference mActivityOpenPref;
    private ListPreference mActivityClosePref;
    private ListPreference mTaskOpenPref;
    private ListPreference mTaskClosePref;
    private ListPreference mTaskMoveToFrontPref;
    private ListPreference mTaskMoveToBackPref;
    private ListPreference mWallpaperOpen;
    private ListPreference mWallpaperClose;
    private ListPreference mWallpaperIntraOpen;
    private ListPreference mWallpaperIntraClose;
    private ListPreference mTaskOpenBehind;
    private SeekBarPreferenceCham mAnimationDuration;
    private SeekBarPreferenceCham mListViewDuration;
    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;
    private ListPreference mListViewCache;
    private AppMultiSelectListPreference mExcludedAppsPref;
    private Preference mToastTestAnimation;
    private ListPreference mToastAnimation;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.animation_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();

        // System animations
        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(mContext.getResources(), mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }

        mAnimExitOnly = (SwitchPreference) findPreference(ANIMATION_CONTROLS_EXIT_ONLY);
        mAnimExitOnly.setChecked(Settings.PAC.getInt(mResolver,
                Settings.PAC.ANIMATION_CONTROLS_EXIT_ONLY, 0) == 1);
        mAnimExitOnly.setOnPreferenceChangeListener(this);

        mAnimReverseExit = (SwitchPreference) findPreference(ANIMATION_CONTROLS_REVERSE_EXIT);
        mAnimReverseExit.setChecked(Settings.PAC.getInt(mResolver,
                Settings.PAC.ANIMATION_CONTROLS_REVERSE_EXIT, 0) == 1);
        mAnimReverseExit.setOnPreferenceChangeListener(this);

        mActivityOpenPref = (ListPreference) prefSet.findPreference(ACTIVITY_OPEN);
        mActivityOpenPref.setOnPreferenceChangeListener(this);
        if (getProperVal(mActivityOpenPref) != null) {
             mActivityOpenPref.setValue(getProperVal(mActivityOpenPref));
             mActivityOpenPref.setSummary(getProperSummary(mActivityOpenPref));
        }
        mActivityOpenPref.setEntries(mAnimationsStrings);
        mActivityOpenPref.setEntryValues(mAnimationsNum);

        mActivityClosePref = (ListPreference) prefSet.findPreference(ACTIVITY_CLOSE);
        mActivityClosePref.setOnPreferenceChangeListener(this);
        if (getProperVal(mActivityClosePref) != null) {
             mActivityClosePref.setValue(getProperVal(mActivityClosePref));
             mActivityClosePref.setSummary(getProperSummary(mActivityClosePref));
        }
        mActivityClosePref.setEntries(mAnimationsStrings);
        mActivityClosePref.setEntryValues(mAnimationsNum);

        mTaskOpenPref = (ListPreference) prefSet.findPreference(TASK_OPEN);
        mTaskOpenPref.setOnPreferenceChangeListener(this);
        if (getProperVal(mTaskOpenPref) != null) {
             mTaskOpenPref.setValue(getProperVal(mTaskOpenPref));
             mTaskOpenPref.setSummary(getProperSummary(mTaskOpenPref));
        }
        mTaskOpenPref.setEntries(mAnimationsStrings);
        mTaskOpenPref.setEntryValues(mAnimationsNum);

        mTaskClosePref = (ListPreference) prefSet.findPreference(TASK_CLOSE);
        mTaskClosePref.setOnPreferenceChangeListener(this);
        if (getProperVal(mTaskClosePref) != null) {
             mTaskClosePref.setValue(getProperVal(mTaskClosePref));
             mTaskClosePref.setSummary(getProperSummary(mTaskClosePref));
        }
        mTaskClosePref.setEntries(mAnimationsStrings);
        mTaskClosePref.setEntryValues(mAnimationsNum);

        mTaskMoveToFrontPref = (ListPreference) prefSet.findPreference(TASK_MOVE_TO_FRONT);
        mTaskMoveToFrontPref.setOnPreferenceChangeListener(this);
        if (getProperVal(mTaskMoveToFrontPref) != null) {
             mTaskMoveToFrontPref.setValue(getProperVal(mTaskMoveToFrontPref));
             mTaskMoveToFrontPref.setSummary(getProperSummary(mTaskMoveToFrontPref));
        }
        mTaskMoveToFrontPref.setEntries(mAnimationsStrings);
        mTaskMoveToFrontPref.setEntryValues(mAnimationsNum);

        mTaskMoveToBackPref = (ListPreference) prefSet.findPreference(TASK_MOVE_TO_BACK);
        mTaskMoveToBackPref.setOnPreferenceChangeListener(this);
        if (getProperVal(mTaskMoveToBackPref) != null) {
             mTaskMoveToBackPref.setValue(getProperVal(mTaskMoveToBackPref));
             mTaskMoveToBackPref.setSummary(getProperSummary(mTaskMoveToBackPref));
        }
        mTaskMoveToBackPref.setEntries(mAnimationsStrings);
        mTaskMoveToBackPref.setEntryValues(mAnimationsNum);

        mWallpaperOpen = (ListPreference) prefSet.findPreference(WALLPAPER_OPEN);
        mWallpaperOpen.setOnPreferenceChangeListener(this);
        if (getProperVal(mWallpaperOpen) != null) {
             mWallpaperOpen.setValue(getProperVal(mWallpaperOpen));
             mWallpaperOpen.setSummary(getProperSummary(mWallpaperOpen));
        }
        mWallpaperOpen.setEntries(mAnimationsStrings);
        mWallpaperOpen.setEntryValues(mAnimationsNum);

        mWallpaperClose = (ListPreference) prefSet.findPreference(WALLPAPER_CLOSE);
        mWallpaperClose.setOnPreferenceChangeListener(this);
        if (getProperVal(mWallpaperClose) != null) {
             mWallpaperClose.setValue(getProperVal(mWallpaperClose));
             mWallpaperClose.setSummary(getProperSummary(mWallpaperClose));
        }
        mWallpaperClose.setEntries(mAnimationsStrings);
        mWallpaperClose.setEntryValues(mAnimationsNum);

        mWallpaperIntraOpen = (ListPreference) prefSet.findPreference(WALLPAPER_INTRA_OPEN);
        mWallpaperIntraOpen.setOnPreferenceChangeListener(this);
        if (getProperVal(mWallpaperIntraOpen) != null) {
             mWallpaperIntraOpen.setValue(getProperVal(mWallpaperIntraOpen));
             mWallpaperIntraOpen.setSummary(getProperSummary(mWallpaperIntraOpen));
        }
        mWallpaperIntraOpen.setEntries(mAnimationsStrings);
        mWallpaperIntraOpen.setEntryValues(mAnimationsNum);

        mWallpaperIntraClose = (ListPreference) prefSet.findPreference(WALLPAPER_INTRA_CLOSE);
        mWallpaperIntraClose.setOnPreferenceChangeListener(this);
        if (getProperVal(mWallpaperIntraClose) != null) {
             mWallpaperIntraClose.setValue(getProperVal(mWallpaperIntraClose));
             mWallpaperIntraClose.setSummary(getProperSummary(mWallpaperIntraClose));
        }
        mWallpaperIntraClose.setEntries(mAnimationsStrings);
        mWallpaperIntraClose.setEntryValues(mAnimationsNum);

        mTaskOpenBehind = (ListPreference) prefSet.findPreference(TASK_OPEN_BEHIND);
        mTaskOpenBehind.setOnPreferenceChangeListener(this);
        if (getProperVal(mTaskOpenBehind) != null) {
             mTaskOpenBehind.setValue(getProperVal(mTaskOpenBehind));
             mTaskOpenBehind.setSummary(getProperSummary(mTaskOpenBehind));
        }
        mTaskOpenBehind.setEntries(mAnimationsStrings);
        mTaskOpenBehind.setEntryValues(mAnimationsNum);

        int defaultDuration = Settings.PAC.getInt(mResolver,
                Settings.PAC.ANIMATION_CONTROLS_DURATION, 25);
        mAnimationDuration = (SeekBarPreferenceCham) prefSet.findPreference(ANIMATION_DURATION);
        mAnimationDuration.setValue(defaultDuration);
        mAnimationDuration.setOnPreferenceChangeListener(this);

        // Listview animations
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

        // Toast Animation
        mToastAnimation = (ListPreference) prefSet.findPreference(PREF_TOAST_ANIMATION);
        mToastAnimation.setValue(Integer.toString(Settings.PAC.getInt(mResolver,
             Settings.PAC.ANIMATION_TOAST, 1)));
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        mToastAnimation.setOnPreferenceChangeListener(this);

        mToastTestAnimation = (Preference) prefSet.findPreference(PREF_TOAST_TEST_ANIMATION);

        setHasOptionsMenu(true);
        updateRevExitAnim();
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
        mActivityOpenPref.setValue("0");
        mActivityClosePref.setValue("0");
        mTaskOpenPref.setValue("0");
        mTaskClosePref.setValue("0");
        mTaskMoveToFrontPref.setValue("0");
        mTaskMoveToBackPref.setValue("0");
        mWallpaperOpen.setValue("0");
        mWallpaperClose.setValue("0");
        mWallpaperIntraOpen.setValue("0");
        mWallpaperIntraClose.setValue("0");
        mTaskOpenBehind.setValue("0");
        mAnimationDuration.setValue(25);
        mAnimExitOnly.setChecked(false);
        mAnimReverseExit.setChecked(false);
        mListViewDuration.setValue(25);
        mListViewAnimation.setValue("0");
        mListViewInterpolator.setValue("0");
        mListViewCache.setValue("0");
    }

    private void resetAllSettings() {
        setProperVal(mActivityOpenPref, 0);
        mActivityOpenPref.setSummary(getProperSummary(mActivityOpenPref));
        setProperVal(mActivityClosePref, 0);
        mActivityClosePref.setSummary(getProperSummary(mActivityClosePref));
        setProperVal(mTaskOpenPref, 0);
        mTaskOpenPref.setSummary(getProperSummary(mTaskOpenPref));
        setProperVal(mTaskClosePref, 0);
        mTaskClosePref.setSummary(getProperSummary(mTaskClosePref));
        setProperVal(mTaskMoveToFrontPref, 0);
        mTaskMoveToFrontPref.setSummary(getProperSummary(mTaskMoveToFrontPref));
        setProperVal(mTaskMoveToBackPref, 0);
        mTaskMoveToBackPref.setSummary(getProperSummary(mTaskMoveToBackPref));
        setProperVal(mWallpaperOpen, 0);
        mWallpaperOpen.setSummary(getProperSummary(mWallpaperOpen));
        setProperVal(mWallpaperClose, 0);
        mWallpaperClose.setSummary(getProperSummary(mWallpaperClose));
        setProperVal(mWallpaperIntraOpen, 0);
        mWallpaperIntraOpen.setSummary(getProperSummary(mWallpaperIntraOpen));
        setProperVal(mWallpaperIntraClose, 0);
        mWallpaperIntraClose.setSummary(getProperSummary(mWallpaperIntraClose));
        setProperVal(mTaskOpenBehind, 0);
        mTaskOpenBehind.setSummary(getProperSummary(mTaskOpenBehind));
        setProperVal(mAnimationDuration, 25);
        setProperVal(mAnimExitOnly, 0);
        setProperVal(mAnimReverseExit, 0);
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
        if (preference == mToastTestAnimation) {
            Toast.makeText(mContext, mContext.getString(R.string.toast_test_animation), Toast.LENGTH_SHORT).show();
            return true;
        }
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAnimExitOnly) {
            boolean value = (Boolean) objValue;
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ANIMATION_CONTROLS_EXIT_ONLY,
                    value ? 1 : 0);
            updateRevExitAnim();
        } else if (preference == mAnimReverseExit) {
            boolean value = (Boolean) objValue;
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ANIMATION_CONTROLS_REVERSE_EXIT,
                    value ? 1 : 0);
        } else if (preference == mActivityOpenPref) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[0], val);
            mActivityOpenPref.setSummary(getProperSummary(mActivityOpenPref));
        } else if (preference == mActivityClosePref) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[1], val);
            mActivityClosePref.setSummary(getProperSummary(mActivityClosePref));
        } else if (preference == mTaskOpenPref) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[2], val);
            mTaskOpenPref.setSummary(getProperSummary(mTaskOpenPref));
        } else if (preference == mTaskClosePref) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[3], val);
            mTaskClosePref.setSummary(getProperSummary(mTaskClosePref));
        } else if (preference == mTaskMoveToFrontPref) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[4], val);
            mTaskMoveToFrontPref.setSummary(getProperSummary(mTaskMoveToFrontPref));
        } else if (preference == mTaskMoveToBackPref) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[5], val);
            mTaskMoveToBackPref.setSummary(getProperSummary(mTaskMoveToBackPref));
        } else if (preference == mWallpaperOpen) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[6], val);
            mWallpaperOpen.setSummary(getProperSummary(mWallpaperOpen));
        } else if (preference == mWallpaperClose) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[7], val);
            mWallpaperClose.setSummary(getProperSummary(mWallpaperClose));
        } else if (preference == mWallpaperIntraOpen) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[8], val);
            mWallpaperIntraOpen.setSummary(getProperSummary(mWallpaperIntraOpen));
        } else if (preference == mWallpaperIntraClose) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[9], val);
            mWallpaperIntraClose.setSummary(getProperSummary(mWallpaperIntraClose));
        } else if (preference == mTaskOpenBehind) {
            int val = Integer.parseInt((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[10], val);
            mTaskOpenBehind.setSummary(getProperSummary(mTaskOpenBehind));
        } else if (preference == mAnimationDuration) {
            int val = ((Integer)objValue).intValue();
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ANIMATION_CONTROLS_DURATION,
                    val);
        } else if (preference == mListViewAnimation) {
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
        } else if (preference == mToastAnimation) {
            int val = Integer.parseInt((String) objValue);
            int index = mToastAnimation.findIndexOfValue((String) objValue);
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.ANIMATION_TOAST, val);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
        } else {
            return false;
        }
        return true;
    }

    private void setProperVal(Preference preference, int val) {
        String mString = "";
        if (preference == mActivityOpenPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[0];
        } else if (preference == mActivityClosePref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[1];
        } else if (preference == mTaskOpenPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[2];
        } else if (preference == mTaskClosePref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[3];
        } else if (preference == mTaskMoveToFrontPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[4];
        } else if (preference == mTaskMoveToBackPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[5];
        } else if (preference == mWallpaperOpen) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[6];
        } else if (preference == mWallpaperClose) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[7];
        } else if (preference == mWallpaperIntraOpen) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[8];
        } else if (preference == mWallpaperIntraClose) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[9];
        } else if (preference == mTaskOpenBehind) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[10];
        } else if (preference == mListViewAnimation) {
            mString = Settings.PAC.LISTVIEW_ANIMATION;
        } else if (preference == mListViewCache) {
            mString = Settings.PAC.LISTVIEW_ANIMATION_CACHE;
        } else if (preference == mListViewInterpolator) {
            mString = Settings.PAC.LISTVIEW_INTERPOLATOR;
        } else if (preference == mListViewDuration) {
            mString = Settings.PAC.LISTVIEW_DURATION;
        }

        Settings.PAC.putInt(mResolver, mString, val);
    }

    private String getProperSummary(Preference preference) {
        String mString = "";
        if (preference == mActivityOpenPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[0];
        } else if (preference == mActivityClosePref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[1];
        } else if (preference == mTaskOpenPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[2];
        } else if (preference == mTaskClosePref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[3];
        } else if (preference == mTaskMoveToFrontPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[4];
        } else if (preference == mTaskMoveToBackPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[5];
        } else if (preference == mWallpaperOpen) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[6];
        } else if (preference == mWallpaperClose) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[7];
        } else if (preference == mWallpaperIntraOpen) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[8];
        } else if (preference == mWallpaperIntraClose) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[9];
        } else if (preference == mTaskOpenBehind) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[10];
        }

        String mNum = Settings.PAC.getString(mResolver, mString);
        return AwesomeAnimationHelper.getProperName(mContext.getResources(), Integer.valueOf(mNum));
    }

    private String getProperVal(Preference preference) {
        String mString = "";
        if (preference == mActivityOpenPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[0];
        } else if (preference == mActivityClosePref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[1];
        } else if (preference == mTaskOpenPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[2];
        } else if (preference == mTaskClosePref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[3];
        } else if (preference == mTaskMoveToFrontPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[4];
        } else if (preference == mTaskMoveToBackPref) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[5];
        } else if (preference == mWallpaperOpen) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[6];
        } else if (preference == mWallpaperClose) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[7];
        } else if (preference == mWallpaperIntraOpen) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[8];
        } else if (preference == mWallpaperIntraClose) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[9];
        } else if (preference == mTaskOpenBehind) {
            mString = Settings.PAC.ACTIVITY_ANIMATION_CONTROLS[10];
        } else if (preference == mListViewAnimation) {
            mString = Settings.PAC.LISTVIEW_ANIMATION;
        } else if (preference == mListViewCache) {
            mString = Settings.PAC.LISTVIEW_ANIMATION_CACHE;
        } else if (preference == mListViewInterpolator) {
            mString = Settings.PAC.LISTVIEW_INTERPOLATOR;
        }

        return Settings.PAC.getString(mResolver, mString);
    }

    private void updateRevExitAnim() {
        boolean enabled = Settings.PAC.getInt(mResolver,
                Settings.PAC.ANIMATION_CONTROLS_EXIT_ONLY, 0) == 1;

        mAnimReverseExit.setEnabled(!enabled);
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
