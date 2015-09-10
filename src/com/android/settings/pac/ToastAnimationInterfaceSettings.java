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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ToastAnimationInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ToastAnimationInterfaceSettings";

    private static final String PREF_TOAST_ANIMATION = "toast_animation";
    private static final String PREF_TOAST_TEST_ANIMATION = "toast_test_animation";

    private ContentResolver mResolver;
    private Context mContext;
    private Preference mToastTestAnimation;
    private ListPreference mToastAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.toast_animation_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();

        mToastAnimation = (ListPreference) prefSet.findPreference(PREF_TOAST_ANIMATION);
        mToastAnimation.setValue(Integer.toString(Settings.PAC.getInt(mResolver,
             Settings.PAC.ANIMATION_TOAST, 1)));
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        mToastAnimation.setOnPreferenceChangeListener(this);

        mToastTestAnimation = (Preference) prefSet.findPreference(PREF_TOAST_TEST_ANIMATION);
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
        if (preference == mToastAnimation) {
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
}
