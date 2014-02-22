/*
 * Copyright (C) 2014 PAC-Roms
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

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class AnimationSettings extends SettingsPreferenceFragment  implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "AnimationSettings";

    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";

    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;
    private ListPreference mToastAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.animation_settings);

        //ListView Animations
        mListViewAnimation = (ListPreference) findPreference(KEY_LISTVIEW_ANIMATION);
        if (mListViewAnimation != null) {
           int listViewAnimation = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.LISTVIEW_ANIMATION, 1);
           mListViewAnimation.setSummary(mListViewAnimation.getEntry());
           mListViewAnimation.setValue(String.valueOf(listViewAnimation));
        }
        mListViewAnimation.setOnPreferenceChangeListener(this);

        mListViewInterpolator = (ListPreference) findPreference(KEY_LISTVIEW_INTERPOLATOR);
        if (mListViewInterpolator != null) {
           int listViewInterpolator = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.LISTVIEW_INTERPOLATOR, 1);
           mListViewInterpolator.setSummary(mListViewInterpolator.getEntry());
           mListViewInterpolator.setValue(String.valueOf(listViewInterpolator));
        }
        mListViewInterpolator.setOnPreferenceChangeListener(this);

        //Toast Animation
        mToastAnimation = (ListPreference) findPreference(KEY_TOAST_ANIMATION);
        if (mToastAnimation != null) {
           int toastAnimation = Settings.PAC.getInt(getContentResolver(),
                    Settings.PAC.TOAST_ANIMATION, 1);
           mToastAnimation.setSummary(mToastAnimation.getEntry());
           mToastAnimation.setSummary(mToastAnimation.getEntries()[toastAnimation]);
           mToastAnimation.setValue(String.valueOf(toastAnimation));
        }
        mToastAnimation.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mListViewAnimation) {
            int value = Integer.parseInt((String) objValue);
            int index = mListViewAnimation.findIndexOfValue((String) objValue);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.LISTVIEW_ANIMATION,
                    value);
            mListViewAnimation.setSummary(mListViewAnimation.getEntries()[index]);
            return true;
        } else if (preference == mListViewInterpolator) {
            int value = Integer.parseInt((String) objValue);
            int index = mListViewInterpolator.findIndexOfValue((String) objValue);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.LISTVIEW_INTERPOLATOR,
                    value);
            mListViewInterpolator.setSummary(mListViewInterpolator.getEntries()[index]);
            return true;
        } else if (preference == mToastAnimation) {
            int value = Integer.parseInt((String) objValue);
            int index = mToastAnimation.findIndexOfValue((String) objValue);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.TOAST_ANIMATION,
                    value);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Context context = this.getActivity().getApplicationContext();
            if (context != null) {
                Toast.makeText(context, "Toast Test", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
