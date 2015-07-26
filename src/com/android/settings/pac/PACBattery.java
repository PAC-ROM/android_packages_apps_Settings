/*
 * Copyright (C) 2015 The PAC-ROM Project
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
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.List;

public class PACBattery extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener, Indexable {

    private static final String TAG = "PACBattery";

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_BATTERY_BAR_HIGH = "status_bar_battery_bar_high";
    private static final String STATUS_BAR_BATTERY_BAR_LOW = "status_bar_battery_bar_low";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;
    private static final int STATUS_BAR_BATTERY_STYLE_BAR = 7;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;
    private ColorPickerPreference mStatusBarBatteryBarHigh;
    private ColorPickerPreference mStatusBarBatteryBarLow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pac_battery);

        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        int batteryStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int batteryShowPercent = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

        mStatusBarBatteryBarHigh =
                (ColorPickerPreference) findPreference(STATUS_BAR_BATTERY_BAR_HIGH);
        mStatusBarBatteryBarHigh.setOnPreferenceChangeListener(this);

        mStatusBarBatteryBarLow =
                (ColorPickerPreference) findPreference(STATUS_BAR_BATTERY_BAR_LOW);
        mStatusBarBatteryBarLow.setOnPreferenceChangeListener(this);

        updateColors();
        enableStatusBarBatteryDependents(batteryStyle);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateColors();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) objValue);
            int index = mStatusBarBattery.findIndexOfValue((String) objValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) objValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) objValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarBatteryBarHigh) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.BATTERY_BAR_HIGH_COLOR, intHex);
            return true;
        } else if (preference == mStatusBarBatteryBarLow) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.BATTERY_BAR_LOW_COLOR, intHex);
            return true;
        }
        return false;
    }

    private void updateColors() {
        int highColor = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.BATTERY_BAR_HIGH_COLOR, 0xff99cc00);
        String hexHighColor = String.format("#%08x", (0xff99cc00 & highColor));
        mStatusBarBatteryBarHigh.setSummary(hexHighColor);
        mStatusBarBatteryBarHigh.setNewPreviewColor(highColor);

        int lowColor = Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.BATTERY_BAR_LOW_COLOR, 0xffff4444);
        String hexlowColor = String.format("#%08x", (0xffff4444 & lowColor));
        mStatusBarBatteryBarLow.setSummary(hexlowColor);
        mStatusBarBatteryBarLow.setNewPreviewColor(lowColor);
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT ||
                batteryIconStyle == STATUS_BAR_BATTERY_STYLE_BAR) {
            mStatusBarBatteryShowPercent.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
        }
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_BAR) {
            mStatusBarBatteryBarHigh.setEnabled(true);
            mStatusBarBatteryBarLow.setEnabled(true);
        } else {
            mStatusBarBatteryBarHigh.setEnabled(false);
            mStatusBarBatteryBarLow.setEnabled(false);
        }
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
        alertDialog.setMessage(R.string.recent_panel_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Settings.PAC.putInt(getActivity().getContentResolver(),
                        Settings.PAC.BATTERY_BAR_HIGH_COLOR, 0xff99cc00);
                Settings.PAC.putInt(getActivity().getContentResolver(),
                        Settings.PAC.BATTERY_BAR_LOW_COLOR, 0xffff4444);
                updateColors();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.pac_battery;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}