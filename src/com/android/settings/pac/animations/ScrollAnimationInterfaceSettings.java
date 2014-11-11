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

package com.android.settings.pac.animations;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.ViewConfiguration;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.pac.util.SeekBarPreferenceCHOS;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class ScrollAnimationInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ScrollAnimationInterfaceSettings";

    private static final String ANIMATION_FLING_VELOCITY = "animation_fling_velocity";
    private static final String ANIMATION_SCROLL_FRICTION = "animation_scroll_friction";
    private static final String ANIMATION_OVERSCROLL_DISTANCE = "animation_overscroll_distance";
    private static final String ANIMATION_OVERFLING_DISTANCE = "animation_overfling_distance";
    private static final float MULTIPLIER_SCROLL_FRICTION = 10000f;
    private static final String ANIMATION_NO_SCROLL = "animation_no_scroll";
    private static final String OVERSCROLL_GLOW_COLOR = "overscroll_glow_color";

    private static final int MENU_RESET = Menu.FIRST;

    static final int DEFAULT_OVERSCROLL_COLOR = 0xffffffff;

    private ContentResolver mResolver;
    private Context mContext;

    private SeekBarPreferenceCHOS mAnimationFling;
    private SeekBarPreferenceCHOS mAnimationScroll;
    private SeekBarPreferenceCHOS mAnimationOverScroll;
    private SeekBarPreferenceCHOS mAnimationOverFling;
    private SwitchPreference mAnimNoScroll;
    private ColorPickerPreference mOverScrollGlowColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.scroll_animation_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();

        mAnimNoScroll = (SwitchPreference) prefSet.findPreference(ANIMATION_NO_SCROLL);
        mAnimNoScroll.setChecked(Settings.PAC.getInt(mResolver,
                Settings.PAC.ANIMATION_CONTROLS_NO_SCROLL, 0) == 1);
        mAnimNoScroll.setOnPreferenceChangeListener(this);

        float defaultScroll = Settings.PAC.getFloat(mResolver,
                Settings.PAC.CUSTOM_SCROLL_FRICTION, ViewConfiguration.DEFAULT_SCROLL_FRICTION);
        mAnimationScroll = (SeekBarPreferenceCHOS) prefSet.findPreference(ANIMATION_SCROLL_FRICTION);
        mAnimationScroll.setValue((int) (defaultScroll * MULTIPLIER_SCROLL_FRICTION));
        mAnimationScroll.setOnPreferenceChangeListener(this);

        int defaultFling = Settings.PAC.getInt(mResolver,
                Settings.PAC.CUSTOM_FLING_VELOCITY, ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        mAnimationFling = (SeekBarPreferenceCHOS) prefSet.findPreference(ANIMATION_FLING_VELOCITY);
        mAnimationFling.setValue(defaultFling);
        mAnimationFling.setOnPreferenceChangeListener(this);

        int defaultOverScroll = Settings.PAC.getInt(mResolver,
                Settings.PAC.CUSTOM_OVERSCROLL_DISTANCE, ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        mAnimationOverScroll = (SeekBarPreferenceCHOS) prefSet.findPreference(ANIMATION_OVERSCROLL_DISTANCE);
        mAnimationOverScroll.setValue(defaultOverScroll);
        mAnimationOverScroll.setOnPreferenceChangeListener(this);

        int defaultOverFling = Settings.PAC.getInt(mResolver,
                Settings.PAC.CUSTOM_OVERFLING_DISTANCE, ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        mAnimationOverFling = (SeekBarPreferenceCHOS) prefSet.findPreference(ANIMATION_OVERFLING_DISTANCE);
        mAnimationOverFling.setValue(defaultOverFling);
        mAnimationOverFling.setOnPreferenceChangeListener(this);

        mOverScrollGlowColor = (ColorPickerPreference) findPreference(OVERSCROLL_GLOW_COLOR);
        mOverScrollGlowColor.setOnPreferenceChangeListener(this);
        int defaultColor = Color.rgb(255, 255, 255);
        int intColor = Settings.PAC.getInt(mResolver,
                Settings.PAC.OVERSCROLL_GLOW_COLOR, defaultColor);
        mOverScrollGlowColor.setNewPreviewColor(intColor);

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
        mAnimationFling.setValue(ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        mAnimationScroll.setValue((int) (ViewConfiguration.DEFAULT_SCROLL_FRICTION * MULTIPLIER_SCROLL_FRICTION));
        mAnimationOverScroll.setValue(ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        mAnimationOverFling.setValue(ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        mOverScrollGlowColor.setNewPreviewColor(DEFAULT_OVERSCROLL_COLOR);
        mAnimNoScroll.setChecked(false);
    }

    private void resetAllSettings() {
        setProperVal(mAnimationFling, ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        Settings.PAC.putFloat(mResolver,
                   Settings.PAC.CUSTOM_SCROLL_FRICTION, ViewConfiguration.DEFAULT_SCROLL_FRICTION);
        setProperVal(mAnimationOverScroll, ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        setProperVal(mAnimationOverFling, ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        Settings.PAC.putInt(mResolver,
                   Settings.PAC.OVERSCROLL_GLOW_COLOR, DEFAULT_OVERSCROLL_COLOR);
        setProperVal(mAnimNoScroll, 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAnimNoScroll) {
            boolean value = (Boolean) objValue;
            Settings.PAC.putInt(mResolver, Settings.PAC.ANIMATION_CONTROLS_NO_SCROLL, value ? 1 : 0);
        } else if (preference == mAnimationScroll) {
            int val = ((Integer)objValue).intValue();
            Settings.PAC.putFloat(mResolver,
                   Settings.PAC.CUSTOM_SCROLL_FRICTION,
                   ((float) (val / MULTIPLIER_SCROLL_FRICTION)));
        } else if (preference == mAnimationFling) {
            int val = ((Integer)objValue).intValue();
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.CUSTOM_FLING_VELOCITY,
                    val);
        } else if (preference == mAnimationOverScroll) {
            int val = ((Integer)objValue).intValue();
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.CUSTOM_OVERSCROLL_DISTANCE,
                    val);
        } else if (preference == mAnimationOverFling) {
            int val = ((Integer)objValue).intValue();
            Settings.PAC.putInt(mResolver,
                    Settings.PAC.CUSTOM_OVERFLING_DISTANCE,
                    val);
        } else if (preference == mOverScrollGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.OVERSCROLL_GLOW_COLOR, intHex);
        } else {
            return false;
        }
        return true;
    }

    private void setProperVal(Preference preference, int val) {
        String mString = "";
        if (preference == mAnimNoScroll) {
            mString = Settings.PAC.ANIMATION_CONTROLS_NO_SCROLL;
        } else if (preference == mAnimationFling) {
            mString = Settings.PAC.CUSTOM_FLING_VELOCITY;
        } else if (preference == mAnimationOverScroll) {
            mString = Settings.PAC.CUSTOM_OVERSCROLL_DISTANCE;
        } else if (preference == mAnimationOverFling) {
            mString = Settings.PAC.CUSTOM_OVERFLING_DISTANCE;
        }

        Settings.PAC.putInt(mResolver, mString, val);
    }

}
