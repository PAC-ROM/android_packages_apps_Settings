
package com.android.settings.pac;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.ShortcutPickHelper;
import com.android.settings.pac.util.Helpers;
import com.android.settings.pac.weather.WeatherPrefs;
import com.android.settings.pac.weather.WeatherRefreshService;
import com.android.settings.pac.weather.WeatherService;

public class Weather extends SettingsPreferenceFragment implements
        ShortcutPickHelper.OnPickListener, OnPreferenceChangeListener {

    public static final String TAG = "Weather";

    private static final String WEATHER = "weather";
    private static final String WEATHER_REFRESH_INTERVAL = "refresh_interval";
    private static final String WEATHER_STATUSBAR_LOCATION = "statusbar_location";
    private static final String WEATHER_CUSTOM_LOCATION = "custom_location";
    private static final String WEATHER_ENABLE_WEATHER = "enable_weather";
    private static final String WEATHER_SHOW_LOACATION= "show_location";
    private static final String WEATHER_SHORTCLICK = "weather_shortclick";
    private static final String WEATHER_LONGCLICK = "weather_longclick";

    private final static String ACTION_APP = "**app**";
    private final static String ACTION_NOTHING = "**nothing**";
    private final static String ACTION_UPDATE = "**update**";

    private EditTextPreference mCustomWeatherLoc;
    private CheckBoxPreference mUseCustomLoc;
    private CheckBoxPreference mShowLoc;
    private CheckBoxPreference mUseCelcius;
    private ListPreference mStatusBarLocation;
    private ListPreference mWeatherSyncInterval;
    private ListPreference mWeatherShortClick;
    private ListPreference mWeatherLongClick;
    private SwitchPreference mEnableWeather;

    private Context mContext;
    private ShortcutPickHelper mPicker;
    private Preference mPreference;
    private String mString;

    SharedPreferences prefs;

    private static final int LOC_WARNING = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        addPreferencesFromResource(R.xml.weather);

        prefs = mContext.getSharedPreferences(WEATHER, Context.MODE_WORLD_WRITEABLE);

        mPicker = new ShortcutPickHelper(getActivity(), this);

        mWeatherSyncInterval = (ListPreference) findPreference(WEATHER_REFRESH_INTERVAL);
        mWeatherSyncInterval.setOnPreferenceChangeListener(this);
        mWeatherSyncInterval.setSummary(Integer.toString(WeatherPrefs.getRefreshInterval(mContext))
                + getResources().getString(R.string.weather_refresh_interval_minutes));

        mStatusBarLocation = (ListPreference) findPreference(WEATHER_STATUSBAR_LOCATION);
        mStatusBarLocation.setOnPreferenceChangeListener(this);
        mStatusBarLocation.setValue(Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.STATUSBAR_WEATHER_STYLE, 1) + "");

        mCustomWeatherLoc = (EditTextPreference) findPreference(WEATHER_CUSTOM_LOCATION);
        mCustomWeatherLoc.setOnPreferenceChangeListener(this);
        mCustomWeatherLoc
                .setSummary(WeatherPrefs.getCustomLocation(mContext));

        mEnableWeather = (SwitchPreference) findPreference(WEATHER_ENABLE_WEATHER);
        mEnableWeather.setChecked(Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.USE_WEATHER, 0) == 1);
        mEnableWeather.setOnPreferenceChangeListener(this);

        mShowLoc = (CheckBoxPreference) findPreference(WEATHER_SHOW_LOACATION);
        mShowLoc.setChecked(Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.WEATHER_SHOW_LOCATION, 0) == 1);

        mUseCustomLoc = (CheckBoxPreference) findPreference(WeatherPrefs.KEY_USE_CUSTOM_LOCATION);
        mUseCustomLoc.setChecked(WeatherPrefs.getUseCustomLocation(mContext));

        mUseCelcius = (CheckBoxPreference) findPreference(WeatherPrefs.KEY_USE_CELCIUS);
        mUseCelcius.setChecked(WeatherPrefs.getUseCelcius(mContext));

        mWeatherShortClick = (ListPreference) findPreference(WEATHER_SHORTCLICK);
        mWeatherShortClick.setOnPreferenceChangeListener(this);
        mWeatherShortClick.setSummary(getProperSummary(mWeatherShortClick));

        mWeatherLongClick = (ListPreference) findPreference(WEATHER_LONGCLICK);
        mWeatherLongClick.setOnPreferenceChangeListener(this);
        mWeatherLongClick.setSummary(getProperSummary(mWeatherLongClick));

        setHasOptionsMenu(true);

        if (!Settings.Secure.isLocationProviderEnabled(
                getContentResolver(), LocationManager.NETWORK_PROVIDER)
                && !mUseCustomLoc.isChecked()) {
            showDialog(LOC_WARNING);
        }

        if (Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.STATUSBAR_WEATHER_STYLE, 1) == 0) {
            mStatusBarLocation.setSummary(getResources().getString(R.string.above_carrier_text));
            mWeatherShortClick.setEnabled(false);
            mWeatherLongClick.setEnabled(false);
        } else if (Settings.PAC.getInt(getContentResolver(),
                Settings.PAC.STATUSBAR_WEATHER_STYLE, 1) == 1) {
            mStatusBarLocation.setSummary(getResources().getString(R.string.weather_panel));
            mWeatherShortClick.setEnabled(true);
            mWeatherLongClick.setEnabled(true);
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        LayoutInflater factory = LayoutInflater.from(mContext);

        switch (dialogId) {
            case LOC_WARNING:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.weather_loc_warning_title))
                        .setMessage(getResources().getString(R.string.weather_loc_warning_msg))
                        .setCancelable(false)
                        .setPositiveButton(
                                getResources().getString(R.string.weather_loc_warning_positive),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Settings.Secure.setLocationProviderEnabled(
                                                getContentResolver(),
                                                LocationManager.NETWORK_PROVIDER, true);
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.weather_loc_warning_negative),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                }).create();
        }
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.weather, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.get_weather:
                Intent i = new Intent(getActivity().getApplicationContext(),
                        WeatherRefreshService.class);
                i.setAction(WeatherService.INTENT_WEATHER_REQUEST);
                i.putExtra(WeatherService.INTENT_EXTRA_ISMANUAL, true);
                getActivity().getApplicationContext().startService(i);
                Helpers.msgShort(getActivity().getApplicationContext(),
                        getString(R.string.weather_refreshing));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUseCustomLoc) {
            return WeatherPrefs.setUseCustomLocation(mContext,
                    ((CheckBoxPreference) preference).isChecked());
        } else if (preference == mShowLoc) {
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.WEATHER_SHOW_LOCATION,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);

            Intent i = new Intent(getActivity().getApplicationContext(),
                    WeatherRefreshService.class);
            i.setAction(WeatherService.INTENT_WEATHER_REQUEST);
            i.putExtra(WeatherService.INTENT_EXTRA_ISMANUAL, true);
            getActivity().getApplicationContext().startService(i);
            Helpers.msgShort(getActivity().getApplicationContext(),
                    getString(R.string.weather_refreshing));

            return true;
        } else if (preference == mUseCelcius) {
            return WeatherPrefs.setUseCelcius(mContext,
                    ((CheckBoxPreference) preference).isChecked());
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mEnableWeather) {
            boolean value = ((Boolean)newValue).booleanValue();
            Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.USE_WEATHER,
                    value ? 1 : 0);

            boolean check = ((SwitchPreference) preference).isChecked();

            Intent i = new Intent(getActivity().getApplicationContext(),
                    WeatherRefreshService.class);
            i.setAction(WeatherService.INTENT_WEATHER_REQUEST);
            i.putExtra(WeatherService.INTENT_EXTRA_ISMANUAL, true);
            PendingIntent weatherRefreshIntent = PendingIntent.getService(getActivity(), 0, i, 0);
            if (!check) {
                AlarmManager alarms = (AlarmManager) getActivity().getSystemService(
                        Context.ALARM_SERVICE);
                alarms.cancel(weatherRefreshIntent);
            } else {
                getActivity().startService(i);
            }

            return true;

        } else if (preference == mWeatherSyncInterval) {
            int newVal = Integer.parseInt((String) newValue);
            preference.setSummary(newValue
                    + getResources().getString(R.string.weather_refresh_interval_minutes));

            return WeatherPrefs.setRefreshInterval(mContext, newVal);

        } else if (preference == mCustomWeatherLoc) {

            String newVal = (String) newValue;

            Intent i = new Intent(getActivity().getApplicationContext(),
                    WeatherRefreshService.class);
            getActivity().getApplicationContext().startService(i);
            preference.setSummary(newVal);
            return WeatherPrefs.setCustomLocation(mContext, newVal);

         } else if (preference == mStatusBarLocation) {

            String newVal = (String) newValue;

            if (Integer.parseInt(newVal) == 0) {
                mStatusBarLocation.setSummary(getResources().getString(R.string.above_carrier_text));
                mWeatherShortClick.setEnabled(false);
                mWeatherLongClick.setEnabled(false);
            } else if (Integer.parseInt(newVal) == 1) {
                mStatusBarLocation.setSummary(getResources().getString(R.string.weather_panel));
                mWeatherShortClick.setEnabled(true);
                mWeatherLongClick.setEnabled(true);
            }

            return Settings.PAC.putInt(getActivity().getContentResolver(),
                    Settings.PAC.STATUSBAR_WEATHER_STYLE,
                    Integer.parseInt(newVal));

        } else if (preference == mWeatherShortClick) {

            mPreference = preference;
            mString = Settings.PAC.WEATHER_PANEL_SHORTCLICK;
            if (newValue.equals(ACTION_APP)) {
                final String label = getResources().getString(R.string.lockscreen_target_empty);
                final ShortcutIconResource iconResource =
                        ShortcutIconResource.fromContext(getActivity(), android.R.drawable.ic_delete);
                mPicker.pickShortcut(
                        new String[] { label },
                        new ShortcutIconResource[] { iconResource },
                        getId());
            } else {
                result = Settings.PAC.putString(getContentResolver(), Settings.PAC.WEATHER_PANEL_SHORTCLICK, (String) newValue);
                mWeatherShortClick.setSummary(getProperSummary(mWeatherShortClick));
            }

        } else if (preference == mWeatherLongClick) {

            mPreference = preference;
            mString = Settings.PAC.WEATHER_PANEL_LONGCLICK;
            if (newValue.equals(ACTION_APP)) {
                final String label = getResources().getString(R.string.lockscreen_target_empty);
                final ShortcutIconResource iconResource =
                        ShortcutIconResource.fromContext(getActivity(), android.R.drawable.ic_delete);
                mPicker.pickShortcut(
                        new String[] { label },
                        new ShortcutIconResource[] { iconResource },
                        getId());
            } else {
                result = Settings.PAC.putString(getContentResolver(), Settings.PAC.WEATHER_PANEL_LONGCLICK, (String) newValue);
                mWeatherLongClick.setSummary(getProperSummary(mWeatherLongClick));
            }

        }
        return false;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        mPreference.setSummary(friendlyName);
        Settings.PAC.putString(getContentResolver(), mString, (String) uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getProperSummary(Preference preference) {
        if (preference == mWeatherLongClick) {
            mString = Settings.PAC.WEATHER_PANEL_LONGCLICK;
        } else if (preference == mWeatherShortClick) {
            mString = Settings.PAC.WEATHER_PANEL_SHORTCLICK;
        }

        String uri = Settings.PAC.getString(getActivity().getContentResolver(),mString);
        String empty = "";

        if (uri == null) {
            return empty;
        }

        if (uri.startsWith("**")) {
            if (uri.equals(ACTION_UPDATE)) {
                return getResources().getString(R.string.update);
            } else if (uri.equals(ACTION_NOTHING)) {
                return getResources().getString(R.string.nothing);
            }
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }
}
