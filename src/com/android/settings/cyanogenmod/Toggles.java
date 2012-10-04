package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.cyanogenmod.TouchInterceptor;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.Arrays;

public class Toggles extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "TogglesLayout";

    private static final String PREF_SHOW_TOGGLES = "enable_toggles";
    private static final String PREF_ENABLED_TOGGLES = "enabled_toggles";
    private static final String PREF_SHOW_BRIGHTNESS = "show_brightness_slider";
    private static final String PREF_TOGGLES_STYLE = "toggle_style";
    private static final String PREF_TOGGLES_LAYOUT = "toggles_layout";
    private static final String PREF_ALT_BUTTON_LAYOUT = "toggles_layout_preference";

    private static String[] mValues;

    CheckBoxPreference mShowToggles;
    Preference mEnabledToggles;
    Preference mLayout;
    CheckBoxPreference mShowBrightness;
    ListPreference mTogglesLayout;
    ListPreference mToggleStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.notification_drawer_toggles);

        mShowToggles = (CheckBoxPreference) findPreference(PREF_SHOW_TOGGLES);
        mShowToggles.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_ENABLE, 0) == 1);

        mShowBrightness = (CheckBoxPreference) findPreference(PREF_SHOW_BRIGHTNESS);
        mShowBrightness.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_SHOW_BRIGHTNESS, 0) == 1);

        mToggleStyle = (ListPreference) findPreference(PREF_TOGGLES_STYLE);
        mToggleStyle.setOnPreferenceChangeListener(this);
        mToggleStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_STYLE, 3)));

        mTogglesLayout = (ListPreference) findPreference(PREF_ALT_BUTTON_LAYOUT);
        mTogglesLayout.setOnPreferenceChangeListener(this);
        mTogglesLayout.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS, 1)));

        mEnabledToggles = findPreference(PREF_ENABLED_TOGGLES);

        mLayout = findPreference(PREF_TOGGLES_LAYOUT);

        mValues = getResources().getStringArray(R.array.available_toggles_entries);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mShowBrightness) {
            boolean value = mShowBrightness.isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_SHOW_BRIGHTNESS, value ? 1 : 0);
            return true;
        } else if(preference == mShowToggles) {
            boolean value = mShowToggles.isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_ENABLE, value ? 1 : 0);
            return true;
        } else if (preference == mEnabledToggles) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            ArrayList<String> enabledToggles = getTogglesStringArray(getActivity());

            final String[] finalArray = getResources().getStringArray(
                    R.array.available_toggles_values);

            boolean checkedToggles[] = new boolean[finalArray.length];

            for (int i = 0; i < checkedToggles.length; i++) {
                if (enabledToggles.contains(finalArray[i])) {
                    checkedToggles[i] = true;
                }
            }

            builder.setTitle(R.string.toggles_display_dialog);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.toggles_display_close,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.setMultiChoiceItems(mValues, checkedToggles, new OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    String toggleKey = (finalArray[which]);

                    if (isChecked) {
                        addToggle(getActivity(), toggleKey);
                    } else {
                        removeToggle(getActivity(), toggleKey);
                    }
                }
            });

            AlertDialog d = builder.create();

            d.show();

            return true;
        } else if (preference == mLayout) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            TogglesLayout fragment = new TogglesLayout();
            ft.addToBackStack(PREF_TOGGLES_LAYOUT);
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == mToggleStyle) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_STYLE, val);
            return true;
        } else if (preference == mTogglesLayout) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS, val);
            return true;
        }
        return false;
    }

    public static void addToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getTogglesStringArray(context);
        enabledToggles.add(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    public static void removeToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getTogglesStringArray(context);
        enabledToggles.remove(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    public static class TogglesLayout extends ListFragment {

        private ListView mButtonList;
        private ButtonAdapter mButtonAdapter;
        private Context mContext;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            mContext = getActivity().getBaseContext();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(R.layout.order_power_widget_buttons_activity, container,
                    false);

            return v;
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mButtonList = this.getListView();
            ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
            mButtonAdapter = new ButtonAdapter(mContext);
            setListAdapter(mButtonAdapter);
        };

        @Override
        public void onDestroy() {
            ((TouchInterceptor) mButtonList).setDropListener(null);
            setListAdapter(null);
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            // reload our buttons and invalidate the views for redraw
            mButtonAdapter.reloadButtons();
            mButtonList.invalidateViews();
        }

        private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                // get the current button list
                ArrayList<String> toggles = getTogglesStringArray(mContext);

                // move the button
                if (from < toggles.size()) {
                    String toggle = toggles.remove(from);

                    if (to <= toggles.size()) {
                        toggles.add(to, toggle);

                        // save our buttons
                        setTogglesFromStringArray(mContext, toggles);

                        // tell our adapter/listview to reload
                        mButtonAdapter.reloadButtons();
                        mButtonList.invalidateViews();
                    }
                }
            }
        };

        private class ButtonAdapter extends BaseAdapter {
            private Context mContext;
            private Resources mSystemUIResources = null;
            private LayoutInflater mInflater;
            private ArrayList<Toggle> mToggles;

            public ButtonAdapter(Context c) {
                mContext = c;
                mInflater = LayoutInflater.from(mContext);

                PackageManager pm = mContext.getPackageManager();
                if (pm != null) {
                    try {
                        mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                    } catch (Exception e) {
                        mSystemUIResources = null;
                        Log.e(TAG, "Could not load SystemUI resources", e);
                    }
                }

                reloadButtons();
            }

            public void reloadButtons() {
                mToggles = new ArrayList<Toggle>();
                ArrayList<String> toggleArray = getTogglesStringArray(mContext);

                for(int i = 0; i < toggleArray.size(); i++) {
                    mToggles.add(new Toggle(toggleArray.get(i)));
                }
            }

            public int getCount() {
                return mToggles.size();
            }

            public Object getItem(int position) {
                return mToggles.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                final View v;
                if (convertView == null) {
                    v = mInflater.inflate(R.layout.order_power_widget_button_list_item, null);
                } else {
                    v = convertView;
                }

                Toggle toggle = mToggles.get(position);
                final TextView name = (TextView) v.findViewById(R.id.name);
                String[] toggleValues = getResources().getStringArray(
                        R.array.available_toggles_values);

                for(int i = 0; i < toggleValues.length; i++) {
                    if(toggle.getId().equals(toggleValues[i])) {
                        name.setText(mValues[i]);
                        break;
                    }
                }

                return v;
            }
        }

    }

    public static class Toggle {
        private String mId;

        public Toggle(String id) {
            mId = id;
        }

        public String getId() {
            return mId;
        }
    }

    public static void setTogglesFromStringArray(Context c, ArrayList<String> newGoodies) {
        String newToggles = "";

        for (int i = 0; i < newGoodies.size(); i++) {
            newToggles += newGoodies.get(i);

            if(i + 1 < newGoodies.size()) {
                newToggles += "|";
            }
        }

        Settings.System.putString(c.getContentResolver(), Settings.System.STATUSBAR_TOGGLES,
                newToggles);
    }

    public static ArrayList<String> getTogglesStringArray(Context c) {
        String cluster = Settings.System.getString(c.getContentResolver(),
                Settings.System.STATUSBAR_TOGGLES);

        if (cluster == null) {
            Log.e(TAG, "cluster was null");
            cluster = "|";
        }

        String[] togglesStringArray = cluster.split("\\|");

        return new ArrayList<String>(Arrays.asList(togglesStringArray));
    }
}
