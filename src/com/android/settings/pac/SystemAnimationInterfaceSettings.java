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
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.internal.util.pac.AwesomeAnimationHelper;

public class SystemAnimationInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SystemAnimationInterfaceSettings";

    private static final String ACTIVITY_OPEN = "activity_open";
    private static final String ACTIVITY_CLOSE = "activity_close";
    private static final String TASK_OPEN = "task_open";
    private static final String TASK_CLOSE = "task_close";
    private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
    private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
    private static final String ANIMATION_DURATION = "animation_duration";
    private static final String ANIMATION_NO_OVERRIDE = "animation_no_override";
    private static final String WALLPAPER_OPEN = "wallpaper_open";
    private static final String WALLPAPER_CLOSE = "wallpaper_close";
    private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
    private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";
    private static final String TASK_OPEN_BEHIND = "task_open_behind";

    private static final int MENU_RESET = Menu.FIRST;

    private ContentResolver mResolver;
    private Context mContext;

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
    private SwitchPreference mAnimNoOverride;
    private SeekBarPreferenceCham mAnimationDuration;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.system_animation_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();

        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(mContext.getResources(), mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }

        mAnimNoOverride = (SwitchPreference) prefSet.findPreference(ANIMATION_NO_OVERRIDE);
        mAnimNoOverride.setChecked(Settings.PAC.getInt(mResolver,
                Settings.PAC.ANIMATION_CONTROLS_NO_OVERRIDE, 0) == 1);
        mAnimNoOverride.setOnPreferenceChangeListener(this);

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
                Settings.PAC.ANIMATION_CONTROLS_DURATION, 0);
        mAnimationDuration = (SeekBarPreferenceCham) prefSet.findPreference(ANIMATION_DURATION);
        mAnimationDuration.setValue(defaultDuration);
        mAnimationDuration.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
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
        alertDialog.setTitle(R.string.reset);
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
        mAnimationDuration.setValue(0);
        mAnimNoOverride.setChecked(false);
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
        setProperVal(mAnimationDuration, 0);
        setProperVal(mAnimNoOverride, 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAnimNoOverride) {
            boolean value = (Boolean) objValue;
            Settings.PAC.putInt(mResolver, Settings.PAC.ANIMATION_CONTROLS_NO_OVERRIDE, value ? 1 : 0);
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
        }

        return Settings.PAC.getString(mResolver, mString);
    }

}
