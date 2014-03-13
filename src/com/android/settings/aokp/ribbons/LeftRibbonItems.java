package com.android.settings.aokp.ribbons;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LeftRibbonItems extends RibbonItems {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        SETTINGS_SYSTEM = Settings.System.AOKP_LEFT_RIBBON;
        return super.onCreateView(inflater, container, savedinstanceState);
    }
}