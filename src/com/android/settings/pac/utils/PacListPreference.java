/*
 * Copyright (C) 2014 The CyanogenMod project
 * This code has been modified. Portions copyright (C) 2015 The PAC-ROM Project.
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

package com.android.settings.pac.utils;

import android.content.Context;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.AttributeSet;

public class PacListPreference extends ListPreference {
    public PacListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PacListPreference(Context context) {
        super(context, null);
    }

    @Override
    protected boolean persistString(String value) {
        if (shouldPersist()) {
            if (value == getPersistedString(null)) {
                // It's already there, so the same as persisting
                return true;
            }

            Settings.PAC.putString(getContext().getContentResolver(), getKey(), value);

            return true;
        }
        return false;
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        return Settings.PAC.getString(getContext().getContentResolver(), getKey());
    }

    @Override
    protected boolean isPersisted() {
        return Settings.PAC.getString(getContext().getContentResolver(), getKey()) != null;
    }
}
