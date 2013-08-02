/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import android.os.Handler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.net.Uri;


import com.android.settings.R;


public class GPSEnabler implements CompoundButton.OnCheckedChangeListener  {
    private final Context mContext;
    private Switch mSwitch;

    private GPSObserver mGPSObserver;

    public GPSEnabler(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        mGPSObserver = new GPSObserver(new Handler());
    }

    private void updateSwitch() {
        boolean isEnabled = getGpsState(mContext);
        if (mSwitch.isChecked() != isEnabled)
            mSwitch.setChecked(isEnabled);
    }

    public void resume() {
        updateSwitch();
        mGPSObserver.observe();
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mGPSObserver.unobserve();
        mSwitch.setOnCheckedChangeListener(null);
    }

    private boolean getGpsState(Context context) {
        ContentResolver resolver = context.getContentResolver();
        return Settings.Secure.isLocationProviderEnabled(resolver,
                LocationManager.GPS_PROVIDER);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        updateSwitch();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked != getGpsState(mContext))
            Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),LocationManager.GPS_PROVIDER, isChecked);
    }

    private void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mSwitch.setChecked(checked);
        }
    }

    class GPSObserver extends ContentObserver {
        GPSObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.Secure.LOCATION_PROVIDERS_ALLOWED),
                            false, this);
        }
        public void unobserve() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSwitch();
        }
    }
}
