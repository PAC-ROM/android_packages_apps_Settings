/*
 * Copyright (C) 2013 Android Open Kang Project
 * Modified by the PAC-man Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.aokp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import static com.android.internal.util.aokp.AwesomeConstants.*;
import com.android.internal.util.aokp.LockScreenHelpers;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.TargetDrawable;
import com.android.settings.aokp.fragments.ShortcutPickHelper;
import com.android.settings.R;
import com.android.settings.aokp.Utils;
import com.android.settings.aokp.util.Helpers;
import com.android.settings.aokp.AOKPPreferenceFragment;
import com.android.settings.aokp.ROMControlActivity;
import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.lang.NumberFormatException;
import com.android.settings.aokp.fragments.IconPicker.OnIconPickListener;

public class Lockscreens extends AOKPPreferenceFragment implements
        ShortcutPickHelper.OnPickListener, ColorPickerDialog.OnColorChangedListener,
        GlowPadView.OnTriggerListener, OnIconPickListener {

    private static final String TAG = "Lockscreen";
    private static final boolean DEBUG = false;

    private Resources mResources;
    private ContentResolver cr;

    private GlowPadView mWaveView;
    private TextView mHelperText;
    private View mLockscreenOptions;
    private boolean mIsLandscape;
    private boolean mIsScreenLarge;

    private Switch mLockEightTargetsSwitch;
    private Switch mLockBatterySwitch;
    private Switch mLockRotateSwitch;
    private Switch mLockVolControlSwitch;
    private Switch mLockVolWakeSwitch;
    private Switch mLockPageHintSwitch;
    private Switch mLockMinimizeChallangeSwitch;
    private Switch mLockCarouselSwitch;
    private Switch mLockAllWidgetsSwitch;
    private Switch mLockUnlimitedWidgetsSwitch;
    private Button mLockTextColorButton;
    private Switch mCameraWidgetSwitch;

    private TextView mLockEightTargetsText;
    private TextView mLockTextColorText;
    private TextView mLockBatteryText;
    private TextView mLockRotateText;
    private TextView mLockVolControlText;
    private TextView mLockVolWakeText;
    private TextView mLockPageHintText;
    private TextView mLockMinimizeChallangeText;
    private TextView mLockCarouselText;
    private TextView mLockAllWidgetsText;
    private TextView mLockUnlimitedWidgetsText;
    private TextView mCameraWidgetText;

    private ShortcutPickHelper mPicker;
    private ImageButton mDialogIcon;
    private Button mDialogLabel;

    private IconPicker mIconPicker;
    private File mImageTmp;
    private Activity mActivity;
    private ArrayList<TargetInfo> mTargetStore = new ArrayList<TargetInfo>();
    private ViewGroup mContainer;
    private int mTargetOffset;
    private int mTargetInset;
    private int defaultColor;
    private int textColor;

    private int mTargetIndex = 0;
    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static String EMPTY_LABEL;

    class TargetInfo {
        String uri, pkgName;
        StateListDrawable icon;
        Drawable defaultIcon;
        String iconType;
        String iconSource;
        TargetInfo(StateListDrawable target) {
            icon = target;
        }
        TargetInfo(String in, StateListDrawable target, String iType, String iSource, Drawable dI) {
            uri = in;
            icon = target;
            defaultIcon = dI;
            iconType = iType;
            iconSource = iSource;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContainer = container;
        setHasOptionsMenu(true);
        mActivity = getActivity();
        cr = mActivity.getContentResolver();
        mIsScreenLarge = Utils.isTablet() ||
                        Settings.System.getInt(cr,
                        Settings.System.LOCKSCREEN_TARGETS_USE_EIGHT, 1) == 1;
        mResources = getResources();
        mTargetInset = mResources.getDimensionPixelSize(com.android.internal.R.dimen.lockscreen_target_inset);
        mIsLandscape = mResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        mTargetOffset = mIsLandscape && !mIsScreenLarge ? 2 : 0;
        mIconPicker = new IconPicker(mActivity, this);
        mPicker = new ShortcutPickHelper(mActivity, this);
        mImageTmp = new File(mActivity.getCacheDir() + "/target.tmp");
        EMPTY_LABEL = mActivity.getResources().getString(R.string.lockscreen_target_empty);
        return inflater.inflate(R.layout.lockscreen_targets, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWaveView = ((GlowPadView) mActivity.findViewById(R.id.lock_target));
        mWaveView.setOnTriggerListener(this);
        initializeView(Settings.System.getString(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGETS));
        mLockscreenOptions = ((View) getActivity().findViewById(R.id.lockscreen_options));
        if (mLockscreenOptions != null) {
            mLockscreenOptions.getParent().bringChildToFront(mLockscreenOptions);
            mIsLandscape = false;
        } else {
            mIsLandscape = true;
        }
        mHelperText = ((TextView) mActivity.findViewById(R.id.helper_text));
        defaultColor = mResources
                .getColor(com.android.internal.R.color.config_defaultNotificationColor);
        textColor = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, defaultColor);

        mLockTextColorText = ((TextView) mActivity.findViewById(R.id.lockscreen_button_id));
        mLockTextColorText.setOnClickListener(mLockTextColorTextListener);
        mLockTextColorButton = ((Button) mActivity.findViewById(R.id.lockscreen_color_button));
        mLockTextColorButton.setBackgroundColor(textColor);
        mLockTextColorButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog picker = new ColorPickerDialog(mContext, textColor);
                picker.setOnColorChangedListener(Lockscreens.this);
                picker.show();
            }
        });

        mLockBatteryText = ((TextView) mActivity.findViewById(R.id.lockscreen_battery_id));
        mLockBatteryText.setOnClickListener(mLockBatteryTextListener);
        mLockBatterySwitch = (Switch) mActivity.findViewById(R.id.lockscreen_battery_switch);
        mLockBatterySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_BATTERY, checked);
                updateSwitches();
            }
        });

        mLockRotateText = ((TextView) mActivity.findViewById(R.id.lockscreen_rotate_id));
        mLockRotateText.setOnClickListener(mLockRotateTextListener);
        mLockRotateSwitch = (Switch) mActivity.findViewById(R.id.lockscreen_rotate_switch);
        mLockRotateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_AUTO_ROTATE, checked);
                updateSwitches();
            }
        });

        mLockVolControlText = ((TextView) mActivity.findViewById(
                R.id.lockscreen_vol_controls_id));
        mLockVolControlText.setOnClickListener(mLockVolControlTextListener);
        mLockVolControlSwitch = (Switch) mActivity.findViewById(
                R.id.lockscreen_vol_controls_switch);
        mLockVolControlSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr, Settings.System.VOLUME_MUSIC_CONTROLS,
                                checked);
                        updateSwitches();
                    }
                });

        mLockVolWakeText = ((TextView) mActivity.findViewById(R.id.lockscreen_vol_wake_id));
        mLockVolWakeText.setOnClickListener(mLockVolWakeTextListener);
        mLockVolWakeSwitch = (Switch) mActivity.findViewById(R.id.lockscreen_vol_wake_switch);
        mLockVolWakeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.VOLUME_WAKE_SCREEN, checked);
                updateSwitches();
            }
        });

        mLockAllWidgetsText = ((TextView) mActivity
                .findViewById(R.id.lockscreen_all_widgets_id));
        mLockAllWidgetsText.setOnClickListener(mLockAllWidgetsTextListener);
        mLockAllWidgetsSwitch = (Switch) mActivity.findViewById(
                R.id.lockscreen_all_widgets_switch);
        mLockAllWidgetsSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_ALL_WIDGETS,
                                checked);
                        updateSwitches();
                    }
                });

        mLockUnlimitedWidgetsText = ((TextView) mActivity.findViewById(
                R.id.lockscreen_unlimited_widgets_id));
        mLockUnlimitedWidgetsText.setOnClickListener(mLockUnlimitedWidgetsTextListener);
        mLockUnlimitedWidgetsSwitch = (Switch) mActivity.findViewById(
                R.id.lockscreen_unlimited_widgets_switch);
        mLockUnlimitedWidgetsSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, checked);
                        updateSwitches();
                    }
                });

        mLockPageHintText = ((TextView) mActivity.findViewById(
                R.id.lockscreen_hide_page_hints_id));
        mLockPageHintText.setOnClickListener(mLockPageHintTextListener);
        mLockPageHintSwitch = (Switch) mActivity.findViewById(
                R.id.lockscreen_hide_page_hints_switch);
        mLockPageHintSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, checked);
                        updateSwitches();
                    }
                });

        mLockMinimizeChallangeText = ((TextView) mActivity.findViewById(
                R.id.lockscreen_minimize_challange_id));
        mLockMinimizeChallangeText.setOnClickListener(mLockMinimizeChallangeTextListener);
        mLockMinimizeChallangeSwitch = (Switch) mActivity.findViewById(
                R.id.lockscreen_minimize_challange_switch);
        mLockMinimizeChallangeSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, checked);
                        updateSwitches();
                    }
                });

        mLockCarouselText = ((TextView) mActivity.findViewById(R.id.lockscreen_carousel_id));
        mLockCarouselText.setOnClickListener(mLockCarouselTextListener);
        mLockCarouselSwitch = (Switch) mActivity.findViewById(R.id.lockscreen_carousel_switch);
        mLockCarouselSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, checked);
                        updateSwitches();
                    }
                });

        mLockEightTargetsText = ((TextView) mActivity
                .findViewById(R.id.lockscreen_targets_use_eight));
        mLockEightTargetsText.setOnClickListener(mLockEightTargetsTextListener);
        mLockEightTargetsSwitch = (Switch) mActivity.findViewById(R.id.lockscreen_use_eight_targets_switch);
        mLockEightTargetsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_TARGETS_USE_EIGHT,
                        checked);
                updateSwitches();
                    }
               });
   
        mCameraWidgetText = ((TextView) getActivity().findViewById(R.id.lockscreen_camera_widget_id));
        mCameraWidgetText.setOnClickListener(mCameraWidgetTextListener);
        mCameraWidgetSwitch = (Switch) getActivity().findViewById(R.id.lockscreen_camera_widget_switch);
        mCameraWidgetSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_CAMERA_WIDGET_SHOW, checked);
                        updateSwitches();
                    }
                });

        if (isSW600DPScreen(mContext)) {
            // Lockscreen Camera Widget doesn't appear at SW600DP
            Settings.System.putBoolean(cr,
                    Settings.System.LOCKSCREEN_CAMERA_WIDGET_SHOW, false);
            Settings.System.putBoolean(cr,
                    Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, false);
            mLockMinimizeChallangeText.setVisibility(View.GONE);
            mLockMinimizeChallangeSwitch.setVisibility(View.GONE);
            mCameraWidgetText.setVisibility(View.GONE);
            mCameraWidgetSwitch.setVisibility(View.GONE);
        }

        updateSwitches();
    }

    private TextView.OnClickListener mLockTextColorTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_text_color_title),
                    getResources().getString(R.string.lockscreen_text_color_summary));
        }
    };

    private TextView.OnClickListener mLockEightTargetsTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_target_use_eight_text),
                    getResources().getString(R.string.lockscreen_target_use_eight_summary));
        }
    };

    private TextView.OnClickListener mLockBatteryTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_battery_title),
                    getResources().getString(R.string.lockscreen_battery_summary));
        }
    };

    private TextView.OnClickListener mLockRotateTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_auto_rotate_title),
                    getResources().getString(R.string.lockscreen_auto_rotate_summary));
        }
    };

    private TextView.OnClickListener mLockVolControlTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.volume_music_controls_title),
                    getResources().getString(R.string.volume_music_controls_summary));
        }
    };

    private TextView.OnClickListener mLockVolWakeTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.volume_rocker_wake_title),
                    getResources().getString(R.string.volume_rocker_wake_summary));
        }
    };

    private TextView.OnClickListener mLockUnlimitedWidgetsTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_unlimited_widgets_title),
                    getResources().getString(R.string.lockscreen_unlimited_widgets_summary));
        }
    };

    private TextView.OnClickListener mLockAllWidgetsTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_all_widgets_title),
                    getResources().getString(R.string.lockscreen_all_widgets_summary));
        }
    };

    private TextView.OnClickListener mLockPageHintTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_hide_initial_page_hints_title),
                    getResources().getString(R.string.lockscreen_hide_initial_page_hints_summary));
        }
    };

    private TextView.OnClickListener mLockMinimizeChallangeTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_minimize_challenge_title),
                    getResources().getString(R.string.lockscreen_minimize_challenge_summary));
        }
    };

    private TextView.OnClickListener mLockCarouselTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(
                            R.string.lockscreen_use_widget_container_carousel_title),
                    getResources().getString(
                            R.string.lockscreen_use_widget_container_carousel_summary));
        }
    };

     private TextView.OnClickListener mCameraWidgetTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(
                            R.string.lockscreen_camera_widget_title),
                    getResources().getString(
                            R.string.lockscreen_camera_widget_summary));
        }
    };

    private void updateSwitches() {
        mLockBatterySwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_BATTERY, false));
        mLockRotateSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_AUTO_ROTATE, false));
        mLockVolControlSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.VOLUME_MUSIC_CONTROLS, false));
        mLockVolWakeSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.VOLUME_WAKE_SCREEN, false));
        mLockAllWidgetsSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_ALL_WIDGETS, false));
        mLockUnlimitedWidgetsSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, false));
        mLockPageHintSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, false));
        mLockMinimizeChallangeSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, false));
        mLockCarouselSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, false));
        mLockEightTargetsSwitch.setChecked(Settings.System.getBoolean(cr,                       
                Settings.System.LOCKSCREEN_TARGETS_USE_EIGHT, false));
        mCameraWidgetSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_CAMERA_WIDGET_SHOW, true));
    }

   /**
     * Create a layered drawable
     * @param back - Background image to use when target is active
     * @param front - Front image to use for target
     * @param inset - Target inset padding
     * @param frontBlank - Whether the front image for active target should be blank
     * @return StateListDrawable
     */
    private StateListDrawable getLayeredDrawable(Drawable back, Drawable front, int inset, boolean frontBlank) {
        front.mutate();
        back.mutate();
        InsetDrawable[] inactivelayer = new InsetDrawable[2];
        InsetDrawable[] activelayer = new InsetDrawable[2];
        Drawable activeFront = frontBlank ? mResources.getDrawable(android.R.color.transparent) : front;
        Drawable inactiveBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_lock_pressed);
        inactivelayer[0] = new InsetDrawable(inactiveBack, 0, 0, 0, 0);
        inactivelayer[1] = new InsetDrawable(front, inset, inset, inset, inset);
        activelayer[0] = new InsetDrawable(back, 0, 0, 0, 0);
        activelayer[1] = new InsetDrawable(activeFront, inset, inset, inset, inset);
        StateListDrawable states = new StateListDrawable();
        LayerDrawable inactiveLayerDrawable = new LayerDrawable(inactivelayer);
        inactiveLayerDrawable.setId(0, 0);
        inactiveLayerDrawable.setId(1, 1);
        LayerDrawable activeLayerDrawable = new LayerDrawable(activelayer);
        activeLayerDrawable.setId(0, 0);
        activeLayerDrawable.setId(1, 1);
        states.addState(TargetDrawable.STATE_INACTIVE, inactiveLayerDrawable);
        states.addState(TargetDrawable.STATE_ACTIVE, activeLayerDrawable);
        states.addState(TargetDrawable.STATE_FOCUSED, activeLayerDrawable);
        return states;
    }

    private void initializeView(String input) {
        if (input == null) {
            input = GlowPadView.EMPTY_TARGET;
        }
        mTargetStore.clear();
        final int maxTargets = mIsScreenLarge ? GlowPadView.MAX_TABLET_TARGETS : GlowPadView.MAX_PHONE_TARGETS;
        final PackageManager packMan = mActivity.getPackageManager();
        final Drawable activeBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
        final String[] targetStore = input.split("\\|");
        if (mIsLandscape && !mIsScreenLarge) {
            mTargetStore.add(new TargetInfo(null));
            mTargetStore.add(new TargetInfo(null));
        }
        //Add the unlock icon
        Drawable unlockFront = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_unlock_normal);
        Drawable unlockBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_unlock_activated);
        mTargetStore.add(new TargetInfo(getLayeredDrawable(unlockBack, unlockFront, 0, true)));
        for (int cc = 0; cc < 8 - mTargetOffset - 1; cc++) {
            String uri = GlowPadView.EMPTY_TARGET;
            Drawable front = null;
            Drawable back = activeBack;
            boolean frontBlank = false;
            String iconType = null;
            String iconSource = null;
            int tmpInset = mTargetInset;
            if (cc < targetStore.length && cc < maxTargets) {
                uri = targetStore[cc];
                if (!uri.equals(GlowPadView.EMPTY_TARGET)) {
                    try {
                        Intent in = Intent.parseUri(uri, 0);
                        if (in.hasExtra(GlowPadView.ICON_FILE)) {
                            String rSource = in.getStringExtra(GlowPadView.ICON_FILE);
                            File fPath = new File(rSource);
                            if (fPath != null) {
                                if (fPath.exists()) {
                                    front = new BitmapDrawable(getResources(), getRoundedCornerBitmap(BitmapFactory.decodeFile(rSource)));
                                    tmpInset = tmpInset + 5;
                                }
                            }
                        } else if (in.hasExtra(GlowPadView.ICON_RESOURCE)) {
                            String rSource = in.getStringExtra(GlowPadView.ICON_RESOURCE);
                            String rPackage = in.getStringExtra(GlowPadView.ICON_PACKAGE);
                            if (rSource != null) {
                                if (rPackage != null) {
                                    try {
                                        Context rContext = mActivity.createPackageContext(rPackage, 0);
                                        int id = rContext.getResources().getIdentifier(rSource, "drawable", rPackage);
                                        front = rContext.getResources().getDrawable(id);
                                        id = rContext.getResources().getIdentifier(rSource.replaceAll("_normal", "_activated"),
                                                "drawable", rPackage);
                                        back = rContext.getResources().getDrawable(id);
                                        tmpInset = 0;
                                        frontBlank = true;
                                    } catch (NameNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (NotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    front = mResources.getDrawable(mResources.getIdentifier(rSource, "drawable", "android"));
                                    back = mResources.getDrawable(mResources.getIdentifier(
                                            rSource.replaceAll("_normal", "_activated"), "drawable", "android"));
                                    tmpInset = 0;
                                    frontBlank = true;
                                }
                            }
                        }
                        if (front == null) {
                            ActivityInfo aInfo = in.resolveActivityInfo(packMan, PackageManager.GET_ACTIVITIES);
                            if (aInfo != null) {
                                front = aInfo.loadIcon(packMan);
                            } else {
                                front = mResources.getDrawable(android.R.drawable.sym_def_app_icon).mutate();
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (cc >= maxTargets) {
                mTargetStore.add(new TargetInfo(null));
                continue;
            }
            if (back == null || front == null) {
                Drawable emptyIcon = mResources.getDrawable(R.drawable.ic_empty).mutate();
                front = emptyIcon;
            }
            mTargetStore.add(new TargetInfo(uri, getLayeredDrawable(back,front, tmpInset, frontBlank), iconType,
                    iconSource, front.getConstantState().newDrawable().mutate()));
        }
        ArrayList<TargetDrawable> tDraw = new ArrayList<TargetDrawable>();
        for (TargetInfo i : mTargetStore) {
            if (i != null) {
                tDraw.add(new TargetDrawable(mResources, i.icon));
            } else {
                tDraw.add(new TargetDrawable(mResources, null));
            }
        }
        mWaveView.setTargetResources(tDraw);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
            bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 24;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    @Override
    public void onResume() {
        super.onResume();
        // If running on a phone, remove padding around container
        if (!mIsScreenLarge) {
            mContainer.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
            .setIcon(R.drawable.ic_settings_backup) // use the backup icon
            .setAlphabeticShortcut('r')
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENU_SAVE, 0, R.string.wifi_save)
            .setIcon(R.drawable.ic_menu_save)
            .setAlphabeticShortcut('s')
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetAll();
                return true;
            case MENU_SAVE:
                saveAll();
                Toast.makeText(mActivity, R.string.lockscreen_target_save, Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    /**
     * Resets the target layout to stock
     */
    private void resetAll() {
        new AlertDialog.Builder(mActivity)
        .setTitle(R.string.lockscreen_target_reset_title)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setMessage(R.string.lockscreen_target_reset_message)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                initializeView(GlowPadView.EMPTY_TARGET);
                saveAll();
                Toast.makeText(mActivity, R.string.lockscreen_target_reset, Toast.LENGTH_LONG).show();
            }
        }).setNegativeButton(R.string.cancel, null)
        .create().show();
    }

    /**
     * Save targets to settings provider
     */
    private void saveAll() {
        StringBuilder targetLayout = new StringBuilder();
        ArrayList<String> existingImages = new ArrayList<String>();
        final int maxTargets = mIsScreenLarge ? GlowPadView.MAX_TABLET_TARGETS : GlowPadView.MAX_PHONE_TARGETS;
        for (int i = mTargetOffset + 1; i <= mTargetOffset + maxTargets; i++) {
            String uri = mTargetStore.get(i).uri;
            String type = mTargetStore.get(i).iconType;
            String source = mTargetStore.get(i).iconSource;
            existingImages.add(source);
            if (!uri.equals(GlowPadView.EMPTY_TARGET) && type != null) {
                try {
                    Intent in = Intent.parseUri(uri, 0);
                    in.putExtra(type, source);
                    String pkgName = mTargetStore.get(i).pkgName;
                    if (pkgName != null) {
                        in.putExtra(GlowPadView.ICON_PACKAGE, mTargetStore.get(i).pkgName);
                    } else {
                        in.removeExtra(GlowPadView.ICON_PACKAGE);
                    }
                    uri = in.toUri(0);
                } catch (URISyntaxException e) {
                }
            }
            targetLayout.append(uri);
            targetLayout.append("|");
        }
        targetLayout.deleteCharAt(targetLayout.length() - 1);
        Settings.System.putString(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGETS, targetLayout.toString());
        for (File pic : mActivity.getFilesDir().listFiles()) {
            if (pic.getName().startsWith("lockscreen_") && !existingImages.contains(pic.toString())) {
                pic.delete();
            }
        }
    }

    /**
     * Updates a target in the GlowPadView
     */
    private void setTarget(int position, String uri, Drawable draw, String iconType, String iconSource, String pkgName) {
        TargetInfo item = mTargetStore.get(position);
        StateListDrawable state = (StateListDrawable) item.icon;
        LayerDrawable inActiveLayer = (LayerDrawable) state.getStateDrawable(0);
        LayerDrawable activeLayer = (LayerDrawable) state.getStateDrawable(1);
        inActiveLayer.setDrawableByLayerId(1, draw);
        boolean isSystem = iconType != null && iconType.equals(GlowPadView.ICON_RESOURCE);
        if (!isSystem) {
            final Drawable activeBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
            activeLayer.setDrawableByLayerId(0, new InsetDrawable(activeBack, 0, 0, 0, 0));
            activeLayer.setDrawableByLayerId(1, draw);
        } else {
            InsetDrawable empty = new InsetDrawable(mResources.getDrawable(android.R.color.transparent), 0, 0, 0, 0);
            activeLayer.setDrawableByLayerId(1, empty);
            int activeId = mResources.getIdentifier(iconSource.replaceAll("_normal", "_activated"), "drawable", "android");
            Drawable back = null;
            if (activeId != 0) {
                back = mResources.getDrawable(activeId);
                activeLayer.setDrawableByLayerId(0, back);
            } else {
                final Drawable activeBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
                activeLayer.setDrawableByLayerId(0, new InsetDrawable(activeBack, 0, 0, 0, 0));
            }
        }
        item.defaultIcon = mDialogIcon.getDrawable().getConstantState().newDrawable().mutate();
        item.uri = uri;
        item.iconType = iconType;
        item.iconSource = iconSource;
        item.pkgName = pkgName;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        try {
            Intent i = Intent.parseUri(uri, 0);
            PackageManager pm = mActivity.getPackageManager();
            ActivityInfo aInfo = i.resolveActivityInfo(pm, PackageManager.GET_ACTIVITIES);
            Drawable icon = null;
            if (aInfo != null) {
                icon = aInfo.loadIcon(pm).mutate();
            } else {
                icon = mResources.getDrawable(android.R.drawable.sym_def_app_icon);
            }
            mDialogLabel.setText(friendlyName);
            mDialogLabel.setTag(uri);
            mDialogIcon.setImageDrawable(resizeForDialog(icon));
            mDialogIcon.setTag(null);
        } catch (Exception e) {
       }
    }

    private Drawable resizeForDialog(Drawable image) {
        int size = (int) mResources.getDimension(android.R.dimen.app_icon_size);
        Bitmap d = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, size, size, false);
        return new BitmapDrawable(mResources, bitmapOrig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String shortcut_name = null;
        if (data != null) {
            shortcut_name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        }
        if (shortcut_name != null && shortcut_name.equals(EMPTY_LABEL)) {
            mDialogLabel.setText(EMPTY_LABEL);
            mDialogLabel.setTag(GlowPadView.EMPTY_TARGET);
            mDialogIcon.setImageResource(R.drawable.ic_empty);
        } else if (requestCode == IconPicker.REQUEST_PICK_SYSTEM || requestCode == IconPicker.REQUEST_PICK_GALLERY
                || requestCode == IconPicker.REQUEST_PICK_ICON_PACK) {
            mIconPicker.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode != Activity.RESULT_CANCELED && resultCode != Activity.RESULT_CANCELED) {
            mPicker.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onGrabbed(View v, int handle) {
        if (!mIsLandscape) {
            updateVisiblity(false);
        }
    }

    @Override
    public void onReleased(View v, int handle) {
        if (!mIsLandscape) {
            updateVisiblity(true);
        }
    }

    public void onTargetChange(View v, int target) {
    }

    @Override
    public void onTrigger(View v, final int target) {
        mTargetIndex = target;
        if ((target != 0 && (mIsScreenLarge || !mIsLandscape)) || (target != 2 && !mIsScreenLarge && mIsLandscape)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.lockscreen_target_edit_title);
            builder.setMessage(R.string.lockscreen_target_edit_msg);
            View view = View.inflate(mActivity, R.layout.lockscreen_shortcut_dialog, null);
            view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mDialogLabel.getText().equals(EMPTY_LABEL)) {
                        try {
                            mImageTmp.createNewFile();
                            mImageTmp.setWritable(true, false);
                            mIconPicker.pickIcon(getId(), mImageTmp);
                        } catch (IOException e) {
                        }
                    }
                }
            });
            view.findViewById(R.id.label).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPicker.pickShortcut(new String[] {EMPTY_LABEL}, new ShortcutIconResource[] {
                            ShortcutIconResource.fromContext(mActivity, android.R.drawable.ic_delete) }, getId());
                }
            });
            mDialogIcon = ((ImageButton) view.findViewById(R.id.icon));
            mDialogLabel = ((Button) view.findViewById(R.id.label));
            TargetInfo item = mTargetStore.get(target);
            mDialogIcon.setImageDrawable(mTargetStore.get(target).defaultIcon.mutate());
            TargetInfo tmpIcon = new TargetInfo(null);
            tmpIcon.iconType = item.iconType;
            tmpIcon.iconSource = item.iconSource;
            tmpIcon.pkgName = item.pkgName;
            mDialogIcon.setTag(tmpIcon);
            if (mTargetStore.get(target).uri.equals(GlowPadView.EMPTY_TARGET)) {
                mDialogLabel.setText(EMPTY_LABEL);
            } else {
                mDialogLabel.setText(mPicker.getFriendlyNameForUri(mTargetStore.get(target).uri));
            }
            mDialogLabel.setTag(mTargetStore.get(target).uri);
            builder.setView(view);
            builder.setPositiveButton(R.string.ok,  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TargetInfo vObject = (TargetInfo) mDialogIcon.getTag();
                    String type = null, source = null, pkgName = null;
                    int targetInset = mTargetInset;
                    if (vObject != null) {
                        type = vObject.iconType;
                        source = vObject.iconSource;
                        pkgName = vObject.pkgName;
                    }
                    if (type != null && type.equals(GlowPadView.ICON_RESOURCE)) {
                        targetInset = 0;
                    }
                    InsetDrawable pD = new InsetDrawable(mDialogIcon.getDrawable(), targetInset,
                            targetInset, targetInset, targetInset);
                    setTarget(mTargetIndex, mDialogLabel.getTag().toString(), pD, type, source, pkgName);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
            ((TextView)dialog.findViewById(android.R.id.message)).setTextAppearance(mActivity,
                    android.R.style.TextAppearance_DeviceDefault_Small);
        }
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
    }

    @Override
    public void iconPicked(int requestCode, int resultCode, Intent in) {
        Drawable ic = null;
        String iconType = null;
        String pkgName = null;
        String iconSource = null;
        if (requestCode == IconPicker.REQUEST_PICK_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                File mImage = new File(mActivity.getFilesDir() + "/lockscreen_" + System.currentTimeMillis() + ".png");
                if (mImageTmp.exists()) {
                    mImageTmp.renameTo(mImage);
                }
                mImage.setReadOnly();
                iconType = GlowPadView.ICON_FILE;
                iconSource = mImage.toString();
                ic = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(mImage.toString()));
            } else {
                if (mImageTmp.exists()) {
                    mImageTmp.delete();
                }
                return;
            }
        } else if (requestCode == IconPicker.REQUEST_PICK_SYSTEM) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            ic = mResources.getDrawable(mResources.getIdentifier(resourceName, "drawable", "android")).mutate();
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else if (requestCode == IconPicker.REQUEST_PICK_ICON_PACK && resultCode == Activity.RESULT_OK) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            pkgName = in.getStringExtra(IconPicker.PACKAGE_NAME);
            try {
                Context rContext = mActivity.createPackageContext(pkgName, 0);
                int id = rContext.getResources().getIdentifier(resourceName, "drawable", pkgName);
                ic = rContext.getResources().getDrawable(id);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else {
            return;
        }
        TargetInfo tmpIcon = new TargetInfo(null);
        tmpIcon.iconType = iconType;
        tmpIcon.iconSource = iconSource;
        tmpIcon.pkgName = pkgName;
        mDialogIcon.setTag(tmpIcon);
        mDialogIcon.setImageDrawable(ic);
    }


    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
            }
        }
    }

    private H mHandler = new H();

    private void updateVisiblity(boolean visible) {
        if (visible) {
            mLockEightTargetsSwitch.setVisibility(View.VISIBLE);
            mLockBatterySwitch.setVisibility(View.VISIBLE);
            mLockRotateSwitch.setVisibility(View.VISIBLE);
            mLockVolControlSwitch.setVisibility(View.VISIBLE);
            mLockVolWakeSwitch.setVisibility(View.VISIBLE);
            mLockPageHintSwitch.setVisibility(View.VISIBLE);
            mLockMinimizeChallangeSwitch.setVisibility(View.VISIBLE);
            mLockCarouselSwitch.setVisibility(View.VISIBLE);
            mLockAllWidgetsSwitch.setVisibility(View.VISIBLE);
            mLockUnlimitedWidgetsSwitch.setVisibility(View.VISIBLE);
            mLockEightTargetsText.setVisibility(View.VISIBLE);
            mLockBatteryText.setVisibility(View.VISIBLE);
            mLockRotateText.setVisibility(View.VISIBLE);
            mLockVolControlText.setVisibility(View.VISIBLE);
            mLockVolWakeText.setVisibility(View.VISIBLE);
            mLockPageHintText.setVisibility(View.VISIBLE);
            mLockMinimizeChallangeText.setVisibility(View.VISIBLE);
            mLockCarouselText.setVisibility(View.VISIBLE);
            mLockAllWidgetsText.setVisibility(View.VISIBLE);
            mLockUnlimitedWidgetsText.setVisibility(View.VISIBLE);
            mLockTextColorText.setVisibility(View.VISIBLE);
            mLockTextColorButton.setVisibility(View.VISIBLE);
            mHelperText.setText(getResources().getString(R.string.lockscreen_options_info));
        } else {
            mLockEightTargetsSwitch.setVisibility(View.GONE);
            mLockBatterySwitch.setVisibility(View.GONE);
            mLockRotateSwitch.setVisibility(View.GONE);
            mLockVolControlSwitch.setVisibility(View.GONE);
            mLockVolWakeSwitch.setVisibility(View.GONE);
            mLockPageHintSwitch.setVisibility(View.GONE);
            mLockMinimizeChallangeSwitch.setVisibility(View.GONE);
            mLockCarouselSwitch.setVisibility(View.GONE);
            mLockAllWidgetsSwitch.setVisibility(View.GONE);
            mLockUnlimitedWidgetsSwitch.setVisibility(View.GONE);
            mLockEightTargetsText.setVisibility(View.GONE);
            mLockBatteryText.setVisibility(View.GONE);
            mLockRotateText.setVisibility(View.GONE);
            mLockVolControlText.setVisibility(View.GONE);
            mLockVolWakeText.setVisibility(View.GONE);
            mLockPageHintText.setVisibility(View.GONE);
            mLockMinimizeChallangeText.setVisibility(View.GONE);
            mLockCarouselText.setVisibility(View.GONE);
            mLockAllWidgetsText.setVisibility(View.GONE);
            mLockUnlimitedWidgetsText.setVisibility(View.GONE);
            mLockTextColorText.setVisibility(View.GONE);
            mLockTextColorButton.setVisibility(View.GONE);
            mHelperText.setText(getResources().getString(R.string.lockscreen_target_info));
        }
    }

    @Override
    public void onColorChanged(int color) {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, color);
        textColor = color;
        mLockTextColorButton.setBackgroundColor(textColor);
    }

    public void createMessage(final String title, final String summary) {
        AlertDialog ad = new AlertDialog.Builder(mContext).create();
        ad.setTitle(title);
        ad.setCancelable(false);
        ad.setMessage(summary);
        ad.setButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    @Override
    public void onFinishFinalAnimation() {
    }
}