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

import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PACSystem extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "PACSystem";

    private static final String RECENT_PANEL = "recent_panel";

    private PreferenceScreen mRecentPanel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pac_system);

        PreferenceScreen prefs = getPreferenceScreen();

        mRecentPanel = (PreferenceScreen) findPreference(RECENT_PANEL);

        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            prefs.removePreference(mRecentPanel);
            Settings.PAC.putInt(getContentResolver(),
                    Settings.PAC.USE_SLIM_RECENTS, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        return false;
    }
}