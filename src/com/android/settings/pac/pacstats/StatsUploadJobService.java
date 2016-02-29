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

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.ArrayMap;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class StatsUploadJobService extends JobService {

    private static final String TAG = StatsUploadJobService.class.getSimpleName();
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public static final String KEY_UNIQUE_ID = "uniqueId";
    public static final String KEY_DEVICE_NAME = "deviceName";
    public static final String KEY_VERSION = "version";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_CARRIER = "carrier";
    public static final String KEY_CARRIER_ID = "carrierId";
    public static final String KEY_OPT_OUT = "optOut";
    public static final String KEY_ROM_NAME = "rom_name";
    public static final String KEY_ROM_VERSION = "rom_version";

    private final Map<JobParameters, StatsUploadTask> mCurrentJobs
            = Collections.synchronizedMap(new ArrayMap<JobParameters, StatsUploadTask>());

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (DEBUG)
            Log.d(TAG, "onStartJob() called with " + "jobParameters = [" + jobParameters + "]");
        final StatsUploadTask uploadTask = new StatsUploadTask(jobParameters);
        mCurrentJobs.put(jobParameters, uploadTask);
        uploadTask.execute((Void) null);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (DEBUG)
            Log.d(TAG, "onStopJob() called with " + "jobParameters = [" + jobParameters + "]");

        final StatsUploadTask cancelledJob;
        cancelledJob = mCurrentJobs.remove(jobParameters);

        if (cancelledJob != null) {
            // cancel the ongoing background task
            cancelledJob.cancel(true);
            return true; // reschedule
        }

        return false;
    }

    private class StatsUploadTask extends AsyncTask<Void, Void, Void> {

        private JobParameters mJobParams;

        public StatsUploadTask(JobParameters jobParams) {
            this.mJobParams = jobParams;
        }

        @Override
        protected Void doInBackground(Void... params) {

            PersistableBundle extras = mJobParams.getExtras();

            String deviceId = extras.getString(KEY_UNIQUE_ID);
            String deviceName = extras.getString(KEY_DEVICE_NAME);
            String deviceVersion = extras.getString(KEY_VERSION);
            String deviceCountry = extras.getString(KEY_COUNTRY);
            String deviceCarrier = extras.getString(KEY_CARRIER);
            String deviceCarrierId = extras.getString(KEY_CARRIER_ID);
            String romName = extras.getString(KEY_ROM_NAME);
            String romVersion = extras.getString(KEY_ROM_VERSION);

            boolean success = false;
            if (!isCancelled()) {
                try {
                    success = uploadToPAC(deviceId, deviceName, deviceVersion, deviceCountry,
                            deviceCarrier, deviceCarrierId, romName, romVersion);
                } catch (IOException e) {
                    Log.e(TAG, "Could not upload stats checkin to commnity server", e);
                    success = false;
                }
            }

            if (success) {
                // we hit the server, succeed either which way.
                mCurrentJobs.remove(mJobParams);
            }

            if (DEBUG)
                Log.d(TAG, "job id " + mJobParams.getJobId() + ", has finished with success="
                        + success);
            jobFinished(mJobParams, !success);
            return null;
        }
    }


    private boolean uploadToPAC(String deviceId, String deviceName, String deviceVersion,
                               String deviceCountry, String deviceCarrier, String deviceCarrierId,
                               String romName, String romVersion)
            throws IOException {
        String PACStatsUrl = Utilities.getStatsUrl();
        final Uri uri = Uri.parse(PACStatsUrl).buildUpon()
                .appendQueryParameter("device_hash", deviceId)
                .appendQueryParameter("device_name", deviceName)
                .appendQueryParameter("device_version", deviceVersion)
                .appendQueryParameter("device_country", deviceCountry)
                .appendQueryParameter("device_carrier", deviceCarrier)
                .appendQueryParameter("device_carrier_id", deviceCarrierId)
                .appendQueryParameter("rom_name", romName)
                .appendQueryParameter("rom_version", romVersion).build();
        URL url = new URL(uri.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            final int responseCode = urlConnection.getResponseCode();
            if (DEBUG) Log.d(TAG, "pac server response code=" + responseCode);
            final boolean success = responseCode == HttpURLConnection.HTTP_OK;
            if (!success) {
                Log.w(TAG, "failed sending, server returned: " + getResponse(urlConnection,
                        !success));
            }
            return success;
        } finally {
            urlConnection.disconnect();
        }

    }

    private String getResponse(HttpURLConnection httpUrlConnection, boolean errorStream)
            throws IOException {
        InputStream responseStream = new BufferedInputStream(errorStream
                ? httpUrlConnection.getErrorStream()
                : httpUrlConnection.getInputStream());

        BufferedReader responseStreamReader = new BufferedReader(
                new InputStreamReader(responseStream));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();
        responseStream.close();

        return stringBuilder.toString();
    }

}
