package com.android.settings.pac.navbar;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.pac.BaseSetting;
import com.android.settings.pac.BaseSetting.OnSettingChangedListener;
import com.android.settings.pac.SingleChoiceSetting;
import com.android.settings.pac.util.ShortcutPickerHelper;
import com.android.settings.pac.util.ShortcutPickerHelper.OnPickListener;

public class NavbarSettingsFragment extends Fragment implements OnSettingChangedListener {

    public NavbarSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.navbar_settings, container, false);

        return v;
    }


    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
    }
}
