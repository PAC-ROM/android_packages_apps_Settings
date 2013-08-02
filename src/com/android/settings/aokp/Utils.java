package com.android.settings.aokp;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.ExtendedPropertiesUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class Utils {

    private static final String TAG = "Utils";

    // Device types
    public static final int DEVICE_PHONE = 0;
    public static final int DEVICE_TABLET = 1;

   public static int getScreenType(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(dm);
        int shortSize = Math.min(dm.heightPixels, dm.widthPixels);
        int shortSizeDp = shortSize * DisplayMetrics.DENSITY_DEFAULT / DisplayMetrics.DENSITY_DEVICE;
        if (shortSizeDp < 600) {
            return DEVICE_PHONE;
        } else {
            return DEVICE_TABLET;
        }
    }

    public static boolean isPhone(Context con) {
        return getScreenType(con) == DEVICE_PHONE;
    }

    public static boolean isTablet(){
        return ExtendedPropertiesUtils.isTablet();
    }
}

