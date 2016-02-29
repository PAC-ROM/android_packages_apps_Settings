/*
 * Copyright (C) 2016 The PAC-ROM Project
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

package com.android.settings.pac.pacstats;

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.util.Log;
import cyanogenmod.providers.CMSettings;

public class ReportingService extends IntentService {
    /* package */ static final String TAG = "PACStats";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public static final String EXTRA_OPTING_OUT = "pacstats::opt_out";

    public ReportingService() {
        super(ReportingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        String deviceId = Utilities.getUniqueID(getApplicationContext());
        String deviceName = Utilities.getDevice();
        String deviceVersion = Utilities.getModVersion();
        String deviceCountry = Utilities.getCountryCode(getApplicationContext());
        String deviceCarrier = Utilities.getCarrier(getApplicationContext());
        String deviceCarrierId = Utilities.getCarrierId(getApplicationContext());
        String romName = Utilities.getRomName();
        String romVersion = Utilities.getRomVersion();
        boolean optOut = intent.getBooleanExtra(EXTRA_OPTING_OUT, false);

        final int pacromJobId = PACStats.getNextJobId(getApplicationContext());

        if (DEBUG) Log.d(TAG, "scheduling jobs id: " + pacromJobId);

        PersistableBundle pacromBundle = new PersistableBundle();
        pacromBundle.putBoolean(StatsUploadJobService.KEY_OPT_OUT, optOut);
        pacromBundle.putString(StatsUploadJobService.KEY_DEVICE_NAME, deviceName);
        pacromBundle.putString(StatsUploadJobService.KEY_UNIQUE_ID, deviceId);
        pacromBundle.putString(StatsUploadJobService.KEY_VERSION, deviceVersion);
        pacromBundle.putString(StatsUploadJobService.KEY_COUNTRY, deviceCountry);
        pacromBundle.putString(StatsUploadJobService.KEY_CARRIER, deviceCarrier);
        pacromBundle.putString(StatsUploadJobService.KEY_CARRIER_ID, deviceCarrierId);
        pacromBundle.putString(StatsUploadJobService.KEY_ROM_NAME, romName);
        pacromBundle.putString(StatsUploadJobService.KEY_ROM_VERSION, romVersion);

        // schedule pacrom stats upload
        js.schedule(new JobInfo.Builder(pacromJobId, new ComponentName(getPackageName(),
                StatsUploadJobService.class.getName()))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(1000)
                .setExtras(pacromBundle)
                .setPersisted(true)
                .build());

        if (optOut) {
            // we've successfully scheduled the opt out.
            CMSettings.Secure.putIntForUser(getContentResolver(),
                    CMSettings.Secure.STATS_COLLECTION_REPORTED, 1, UserHandle.USER_OWNER);
        }

        // reschedule
        PACStats.updateLastSynced(this);
        ReportingServiceManager.setAlarm(this);
    }
}
