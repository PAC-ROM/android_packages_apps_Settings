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

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class EdgeSwipeNavigation extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String GESTURE_ONE = "gesture_one";
    private static final String GESTURE_TWO = "gesture_two";
    private static final String GESTURE_THREE = "gesture_three";
    private static final String GESTURE_FOUR = "gesture_four";
    private static final String GESTURE_TYPE_ONE = "gesture_type_one";
    private static final String GESTURE_TYPE_TWO = "gesture_type_two";
    private static final String GESTURE_TYPE_THREE = "gesture_type_three";
    private static final String GESTURE_TYPE_FOUR = "gesture_type_four";
    private static final String GESTURE_SWIPE_CAPTURE = "gesture_swipe_capture";
    private static final String ZONE_ONE_SIZE = "zone_one_grid";
    private static final String ZONE_TWO_SIZE = "zone_two_grid";
    private static final String ZONE_THREE_SIZE = "zone_three_grid";
    private static final String ZONE_FOUR_SIZE = "zone_four_grid";
    private static final String GESTURE_SWIPE_DISTANCE = "gesture_swipe_distance";

    private ListPreference mEdgeSwipeBottom;
    private ListPreference mEdgeSwipeTop;
    private ListPreference mEdgeSwipeRight;
    private ListPreference mEdgeSwipeLeft;

    private ListPreference mGestureTypeOne;
    private ListPreference mGestureTypeTwo;
    private ListPreference mGestureTypeThree;
    private ListPreference mGestureTypeFour;

    private CheckBoxPreference mCaptureSwipe;

    private QuadNumberPickerPreference mZoneOneSize;
    private QuadNumberPickerPreference mZoneTwoSize;
    private QuadNumberPickerPreference mZoneThreeSize;
    private QuadNumberPickerPreference mZoneFourSize;

    private SeekBarPreference mEdgeSwipeDistance;

    private ContentResolver mContentResolver;

    private SharedPreferences mPrefs;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                getActivity().getApplicationContext());

        addPreferencesFromResource(R.xml.edge_swipe);

        PreferenceScreen prefSet = getPreferenceScreen();

        mContentResolver = getActivity().getApplicationContext().getContentResolver();

        mEdgeSwipeLeft = (ListPreference) findPreference(GESTURE_ONE);
        mEdgeSwipeLeft.setOnPreferenceChangeListener(this);
        int left = Settings.System.getInt(mContentResolver, GESTURE_ONE, 0);
        mEdgeSwipeLeft.setValue(String.valueOf(left));
        updateSummary(mEdgeSwipeLeft, left);
        if (left == 7) {
            try {
                mEdgeSwipeLeft.setIcon(getActivity().getPackageManager().getActivityIcon(
                        Intent.parseUri(Settings.System.getString(mContentResolver,
                        Settings.System.GESTURE_APP_ONE), 0)));
            } catch (Exception e) {
            }
        }

        mEdgeSwipeTop = (ListPreference) findPreference(GESTURE_TWO);
        mEdgeSwipeTop.setOnPreferenceChangeListener(this);
        int top = Settings.System.getInt(mContentResolver, GESTURE_TWO, 0);
        mEdgeSwipeTop.setValue(String.valueOf(top));
        updateSummary(mEdgeSwipeTop, top);
        if (top == 7) {
            try {
                mEdgeSwipeTop.setIcon(getActivity().getPackageManager().getActivityIcon(
                        Intent.parseUri(Settings.System.getString(mContentResolver,
                        Settings.System.GESTURE_APP_TWO), 0)));
            } catch (Exception e) {
            }
        }

        mEdgeSwipeRight = (ListPreference) findPreference(GESTURE_THREE);
        mEdgeSwipeRight.setOnPreferenceChangeListener(this);
        int right = Settings.System.getInt(mContentResolver, GESTURE_THREE, 0);
        mEdgeSwipeRight.setValue(String.valueOf(right));
        updateSummary(mEdgeSwipeRight, right);
        if (right == 7) {
            try {
                mEdgeSwipeRight.setIcon(getActivity().getPackageManager().getActivityIcon(
                        Intent.parseUri(Settings.System.getString(mContentResolver,
                        Settings.System.GESTURE_APP_THREE), 0)));
            } catch (Exception e) {
            }
        }

        mEdgeSwipeBottom = (ListPreference) findPreference(GESTURE_FOUR);
        mEdgeSwipeBottom.setOnPreferenceChangeListener(this);
        int bottom = Settings.System.getInt(mContentResolver, GESTURE_FOUR, 0);
        mEdgeSwipeBottom.setValue(String.valueOf(bottom));
        updateSummary(mEdgeSwipeBottom, bottom);
        if (bottom == 7) {
            try {
                mEdgeSwipeBottom.setIcon(getActivity().getPackageManager().getActivityIcon(
                        Intent.parseUri(Settings.System.getString(mContentResolver,
                        Settings.System.GESTURE_APP_FOUR), 0)));
            } catch (Exception e) {
            }
        }

        mGestureTypeOne = (ListPreference) findPreference(GESTURE_TYPE_ONE);
        mGestureTypeOne.setOnPreferenceChangeListener(this);
        int gestureType = Settings.System.getInt(mContentResolver, GESTURE_TYPE_ONE, 0);
        mGestureTypeOne.setValue(String.valueOf(gestureType));
        updateSummary(mGestureTypeOne, gestureType);

        mGestureTypeTwo = (ListPreference) findPreference(GESTURE_TYPE_TWO);
        mGestureTypeTwo.setOnPreferenceChangeListener(this);
        gestureType = Settings.System.getInt(mContentResolver, GESTURE_TYPE_TWO, 0);
        mGestureTypeTwo.setValue(String.valueOf(gestureType));
        updateSummary(mGestureTypeTwo, gestureType);

        mGestureTypeThree = (ListPreference) findPreference(GESTURE_TYPE_THREE);
        mGestureTypeThree.setOnPreferenceChangeListener(this);
        gestureType = Settings.System.getInt(mContentResolver, GESTURE_TYPE_THREE, 0);
        mGestureTypeThree.setValue(String.valueOf(gestureType));
        updateSummary(mGestureTypeThree, gestureType);

        mGestureTypeFour = (ListPreference) findPreference(GESTURE_TYPE_FOUR);
        mGestureTypeFour.setOnPreferenceChangeListener(this);
        gestureType = Settings.System.getInt(mContentResolver, GESTURE_TYPE_FOUR, 0);
        mGestureTypeFour.setValue(String.valueOf(gestureType));
        updateSummary(mGestureTypeFour, gestureType);

        mCaptureSwipe = (CheckBoxPreference) findPreference(GESTURE_SWIPE_CAPTURE);
        mCaptureSwipe.setChecked(Settings.System.getInt(mContentResolver,
                GESTURE_SWIPE_CAPTURE, 0) == 1);

        mZoneOneSize = (QuadNumberPickerPreference) prefSet.findPreference(ZONE_ONE_SIZE);
        mZoneOneSize.setOnPreferenceChangeListener(this);
        mZoneTwoSize = (QuadNumberPickerPreference) prefSet.findPreference(ZONE_TWO_SIZE);
        mZoneTwoSize.setOnPreferenceChangeListener(this);
        mZoneThreeSize = (QuadNumberPickerPreference) prefSet.findPreference(ZONE_THREE_SIZE);
        mZoneThreeSize.setOnPreferenceChangeListener(this);
        mZoneFourSize = (QuadNumberPickerPreference) prefSet.findPreference(ZONE_FOUR_SIZE);
        mZoneFourSize.setOnPreferenceChangeListener(this);

        mEdgeSwipeDistance = (SeekBarPreference) prefSet.findPreference(GESTURE_SWIPE_DISTANCE);
        mEdgeSwipeDistance.setDefault(Settings.System.getInt(getActivity().getApplicationContext()
                .getContentResolver(), Settings.System.GESTURE_SWIPE_DISTANCE, 0));
        mEdgeSwipeDistance.setOnPreferenceChangeListener(this);
        mEdgeSwipeDistance.setSummary(String.valueOf(mEdgeSwipeDistance.getDefault()));
    }

    private void updateSummary(ListPreference preference, int value) {
        final CharSequence[] entries = preference.getEntries();
        final CharSequence[] values = preference.getEntryValues();
        int best = 0;
        for (int i = 0; i < values.length; i++) {
            int summaryValue = Integer.parseInt(values[i].toString());
            if (value >= summaryValue) {
                best = i;
            }
        }
        preference.setSummary(entries[best].toString());
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mEdgeSwipeBottom) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_FOUR, value);
            updateSummary(mEdgeSwipeBottom, value);
            if (value == 7) {
                // Pick an application
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
                startActivityForResult(pickIntent, 3);
            } else {
                mEdgeSwipeBottom.setIcon(null);
            }
        } else if (preference == mEdgeSwipeTop) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_TWO, value);
            updateSummary(mEdgeSwipeTop, value);
            if (value == 7) {
                // Pick an application
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
                startActivityForResult(pickIntent, 1);
            } else {
                mEdgeSwipeTop.setIcon(null);
            }
        } else if (preference == mEdgeSwipeRight) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_THREE, value);
            updateSummary(mEdgeSwipeRight, value);
            if (value == 7) {
                // Pick an application
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
                startActivityForResult(pickIntent, 2);
            } else {
                mEdgeSwipeRight.setIcon(null);
            }
        } else if (preference == mEdgeSwipeLeft) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_ONE, value);
            updateSummary(mEdgeSwipeLeft, value);
            if (value == 7) {
                // Pick an application
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
                startActivityForResult(pickIntent, 0);
            } else {
                mEdgeSwipeLeft.setIcon(null);
            }
        } else if (preference == mGestureTypeOne) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_TYPE_ONE, value);
            updateSummary(mGestureTypeOne, value);
        } else if (preference == mGestureTypeTwo) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_TYPE_TWO, value);
            updateSummary(mGestureTypeTwo, value);
        } else if (preference == mGestureTypeThree) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_TYPE_THREE, value);
            updateSummary(mGestureTypeThree, value);
        } else if (preference == mGestureTypeFour) {
            int value = Integer.parseInt((String) objValue);
            Settings.System.putInt(mContentResolver, GESTURE_TYPE_FOUR, value);
            updateSummary(mGestureTypeFour, value);
        } else if (preference == mZoneOneSize) {
            String value = (String) objValue;
            Settings.System.putString(mContentResolver, Settings.System.TOUCH_ZONE_ONE, value);
        } else if (preference == mZoneTwoSize) {
            String value = (String) objValue;
            Settings.System.putString(mContentResolver, Settings.System.TOUCH_ZONE_TWO, value);
        } else if (preference == mZoneThreeSize) {
            String value = (String) objValue;
            Settings.System.putString(mContentResolver, Settings.System.TOUCH_ZONE_THREE, value);
        } else if (preference == mZoneFourSize) {
            String value = (String) objValue;
            Settings.System.putString(mContentResolver, Settings.System.TOUCH_ZONE_FOUR, value);
        } else if (preference == mEdgeSwipeDistance) {
            int value = (Integer) objValue;
            Settings.System.putInt(mContentResolver, GESTURE_SWIPE_DISTANCE, value);
            mEdgeSwipeDistance.setSummary(String.valueOf(value));
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCaptureSwipe) {
            Settings.System.putInt(getContentResolver(), Settings.System.GESTURE_SWIPE_CAPTURE,
                    mCaptureSwipe.isChecked() ? 1 : 0);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Settings.System.putInt(mContentResolver, Settings.System.SHOW_GESTURES, 1);
            }
        }, 500);
    }

    @Override
    public void onPause() {
        super.onPause();

         mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Settings.System.putInt(mContentResolver, Settings.System.SHOW_GESTURES, 0);
            }
        }, 500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

         mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Settings.System.putInt(mContentResolver, Settings.System.SHOW_GESTURES, 0);
            }
        }, 500);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch (requestCode) {
                case 0:
                    Settings.System.putString(mContentResolver, Settings.System.GESTURE_APP_ONE, data.toUri(0));
                    try {
                        mEdgeSwipeLeft.setIcon(getActivity().getPackageManager().getActivityIcon(data));
                    } catch (Exception e) {
                    }
                    break;
                case 1:
                    Settings.System.putString(mContentResolver, Settings.System.GESTURE_APP_TWO, data.toUri(0));
                    try {
                        mEdgeSwipeTop.setIcon(getActivity().getPackageManager().getActivityIcon(data));
                    } catch (Exception e) {
                    }
                    break;
                case 2:
                    Settings.System.putString(mContentResolver, Settings.System.GESTURE_APP_THREE, data.toUri(0));
                    try {
                        mEdgeSwipeRight.setIcon(getActivity().getPackageManager().getActivityIcon(data));
                    } catch (Exception e) {
                    }
                    break;
                case 3:
                    Settings.System.putString(mContentResolver, Settings.System.GESTURE_APP_FOUR, data.toUri(0));
                    try {
                        mEdgeSwipeBottom.setIcon(getActivity().getPackageManager().getActivityIcon(data));
                    } catch (Exception e) {
                    }
                    break;
            }
        }
    }

}
