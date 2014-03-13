package com.android.settings.aokp.ribbons;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


import com.android.internal.util.aokp.AokpRibbonHelper;
import com.android.settings.R;

public class HorizontalRibbonSettings extends Fragment implements OnSeekBarChangeListener {

    private SeekBar mRibbonSize;
    private Context mContext;
    public String[] SETTINGS_SYSTEM;
    private int mSeekBarProgress;

    public HorizontalRibbonSettings() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        View main = inflater.inflate(R.layout.fragment_horizontal_ribbons_settings, container, false);

        mRibbonSize = (SeekBar) main.findViewById(R.id.ribbon_size);
        mRibbonSize.setProgress(Settings.System.getInt(mContext.getContentResolver(), SETTINGS_SYSTEM[AokpRibbonHelper.HORIZONTAL_RIBBON_SIZE], 30));
        mRibbonSize.setOnSeekBarChangeListener(this);

        return main;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mSeekBarProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mRibbonSize) {
            Settings.System.putInt(mContext.getContentResolver(), SETTINGS_SYSTEM[AokpRibbonHelper.HORIZONTAL_RIBBON_SIZE], mSeekBarProgress);
        }
    }
}