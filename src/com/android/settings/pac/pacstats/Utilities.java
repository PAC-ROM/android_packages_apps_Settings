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

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cyanogenmod.providers.CMSettings;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Utilities {
    public static String getUniqueID(Context context) {
        final String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return digest(context.getPackageName() + id);
    }

    public static String getCarrier(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrier = tm.getNetworkOperatorName();
        if (TextUtils.isEmpty(carrier)) {
            carrier = "Unknown";
        }
        return carrier;
    }

    public static String getCarrierId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierId = tm.getNetworkOperator();
        if (TextUtils.isEmpty(carrierId)) {
            carrierId = "0";
        }
        return carrierId;
    }

    public static String getCountryCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();
        if (TextUtils.isEmpty(countryCode)) {
            countryCode = "Unknown";
        }
        return countryCode;
    }

    public static String getDevice() {
        return SystemProperties.get("ro.pac.device", Build.PRODUCT);
    }

    public static String getModVersion() {
        return SystemProperties.get("ro.build.display.id", Build.DISPLAY);
    }

    public static String getRomName() {
        return LocalTools.getProp("ro.pacstats.name");
    }

    public static String getRomVersion() {
        return LocalTools.getProp("ro.pacstats.version");
    }

    public static long getTimeFrame() {
        String tFrameStr = LocalTools.getProp("ro.pacstats.tframe");
        return Long.valueOf(tFrameStr);
    }

    public static String getStatsUrl() {
        String returnUrl = LocalTools.getProp("ro.pacstats.url");

        if (returnUrl.isEmpty()) {
            return null;
        }

        // if the last char of the link is not /, add it
        if (!returnUrl.substring(returnUrl.length() - 1).equals("/")) {
            returnUrl += "/";
        }

        return returnUrl;
    }

    public static String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return new BigInteger(1, md.digest(input.getBytes())).toString(16).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check to see if global stats are enabled.
     * @param context
     * @return Whether or not stats collection is enabled.
     */
    public static boolean isStatsCollectionEnabled(Context context) {
        return CMSettings.Secure.getInt(context.getContentResolver(),
                CMSettings.Secure.STATS_COLLECTION, 1) != 0;
    }

    /**
     * Enabled or disable stats collection
     * @param context
     * @param enabled Boolean that sets collection being enabled.
     */
    public static void setStatsCollectionEnabled(Context context, boolean enabled) {
        int enable = (enabled) ? 1 : 0;
        CMSettings.Secure.putInt(context.getContentResolver(),
                CMSettings.Secure.STATS_COLLECTION, enable);
    }
}
