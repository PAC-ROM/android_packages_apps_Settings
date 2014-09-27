package com.android.settings.pac.navbar;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.pac.util.aokp.BaseSetting;
import com.android.settings.pac.util.aokp.BaseSetting.OnSettingChangedListener;
import com.android.settings.pac.util.aokp.SingleChoiceSetting;
import com.android.settings.pac.util.aokp.CheckboxSetting;
import com.android.settings.pac.util.ShortcutPickerHelper;
import com.android.settings.pac.util.ShortcutPickerHelper.OnPickListener;

import com.android.internal.util.pac.DeviceUtils;

public class NavbarSettingsFragment extends Fragment implements OnSettingChangedListener {

    protected Context mContext;

    SingleChoiceSetting navbar_width, navbar_height, navbar_height_landscape;
    CheckboxSetting navbar_left;

    public NavbarSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.navbar_settings, container, false);

        navbar_width = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_width);
        navbar_height = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_height);
        navbar_height_landscape = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_height_landscape);
        navbar_left = (CheckboxSetting) v.findViewById(R.id.navigation_bar_left);

        if (DeviceUtils.isPhone(getActivity())) {
            navbar_height_landscape.setVisibility(View.GONE);
        } else {
            navbar_width.setVisibility(View.GONE);
            navbar_left.setVisibility(View.GONE);
        }

        return v;
    }


    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
    }
}
