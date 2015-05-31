/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.notification;

import static com.android.settings.notification.SettingPref.TYPE_GLOBAL;
import static com.android.settings.notification.SettingPref.TYPE_PAC;
import static com.android.settings.notification.SettingPref.TYPE_SYSTEM;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.PAC;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.hardware.VibratorIntensity;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OtherSoundSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String TAG = "OtherSoundSettings";

    private static final int DEFAULT_ON = 1;

    private static final int EMERGENCY_TONE_SILENT = 0;
    private static final int EMERGENCY_TONE_ALERT = 1;
    private static final int EMERGENCY_TONE_VIBRATE = 2;
    private static final int DEFAULT_EMERGENCY_TONE = EMERGENCY_TONE_SILENT;

    private static final int DOCK_AUDIO_MEDIA_DISABLED = 0;
    private static final int DOCK_AUDIO_MEDIA_ENABLED = 1;
    private static final int DEFAULT_DOCK_AUDIO_MEDIA = DOCK_AUDIO_MEDIA_DISABLED;

    private static final int VOLUME_STEPS_5 = 0;
    private static final int VOLUME_STEPS_7 = 1;
    private static final int VOLUME_STEPS_15 = 2;
    private static final int VOLUME_STEPS_30 = 3;
    private static final int VOLUME_STEPS_45 = 4;
    private static final int VOLUME_STEPS_60 = 5;

    private static final String KEY_DIAL_PAD_TONES = "dial_pad_tones";
    private static final String KEY_SCREEN_LOCKING_SOUNDS = "screen_locking_sounds";
    private static final String KEY_DOCKING_SOUNDS = "docking_sounds";
    private static final String KEY_VOLUME_ADJUST_SOUNDS = "volume_adjust_sounds";
    private static final String KEY_TOUCH_SOUNDS = "touch_sounds";
    private static final String KEY_DOCK_AUDIO_MEDIA = "dock_audio_media";
    private static final String KEY_EMERGENCY_TONE = "emergency_tone";

    private static final String KEY_POWER_NOTIFICATIONS = "power_notifications";
    private static final String KEY_POWER_NOTIFICATIONS_VIBRATE = "power_notifications_vibrate";
    private static final String KEY_POWER_NOTIFICATIONS_RINGTONE = "power_notifications_ringtone";

    private static final String KEY_VOLUME_STEPS_ALARM = "volume_steps_alarm";
    private static final String KEY_VOLUME_STEPS_DTMF = "volume_steps_dtmf";
    private static final String KEY_VOLUME_STEPS_MUSIC = "volume_steps_music";
    private static final String KEY_VOLUME_STEPS_NOTIFICATION = "volume_steps_notification";
    private static final String KEY_VOLUME_STEPS_RING = "volume_steps_ring";
    private static final String KEY_VOLUME_STEPS_SYSTEM = "volume_steps_system";
    private static final String KEY_VOLUME_STEPS_VOICE_CALL = "volume_steps_voice_call";

    // Request code for power notification ringtone picker
    private static final int REQUEST_CODE_POWER_NOTIFICATIONS_RINGTONE = 1;

    // Used for power notification uri string if set to silent
    private static final String POWER_NOTIFICATIONS_SILENT_URI = "silent";

    private SwitchPreference mPowerSounds;
    private SwitchPreference mPowerSoundsVibrate;
    private Preference mPowerSoundsRingtone;


    private static final SettingPref PREF_DIAL_PAD_TONES = new SettingPref(
            TYPE_SYSTEM, KEY_DIAL_PAD_TONES, System.DTMF_TONE_WHEN_DIALING, DEFAULT_ON) {
        @Override
        public boolean isApplicable(Context context) {
            return Utils.isVoiceCapable(context);
        }
    };

    private static final SettingPref PREF_SCREEN_LOCKING_SOUNDS = new SettingPref(
            TYPE_SYSTEM, KEY_SCREEN_LOCKING_SOUNDS, System.LOCKSCREEN_SOUNDS_ENABLED, DEFAULT_ON);

    private static final SettingPref PREF_DOCKING_SOUNDS = new SettingPref(
            TYPE_GLOBAL, KEY_DOCKING_SOUNDS, Global.DOCK_SOUNDS_ENABLED, DEFAULT_ON) {
        @Override
        public boolean isApplicable(Context context) {
            return hasDockSettings(context);
        }
    };

    private static final SettingPref PREF_VOLUME_ADJUST_SOUNDS = new SettingPref(
            TYPE_SYSTEM, KEY_VOLUME_ADJUST_SOUNDS, System.VOLUME_ADJUST_SOUNDS_ENABLED,
            DEFAULT_ON) {
        @Override
        public boolean isApplicable(Context context) {
            return Utils.hasVolumeRocker(context);
        }
    };

    private static final SettingPref PREF_TOUCH_SOUNDS = new SettingPref(
            TYPE_SYSTEM, KEY_TOUCH_SOUNDS, System.SOUND_EFFECTS_ENABLED, DEFAULT_ON) {
        @Override
        protected boolean setSetting(Context context, int value) {
            final AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (value != 0) {
                am.loadSoundEffects();
            }
            return super.setSetting(context, value);
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        // Unload sound effects to free some memory
        boolean touchSoundsEnabled = android.provider.Settings.System.getInt(getContentResolver(),
                System.SOUND_EFFECTS_ENABLED, DEFAULT_ON) == 1 ? true : false;
        if (!touchSoundsEnabled) {
            AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            am.unloadSoundEffects();
        }
    }

    private static final SettingPref PREF_DOCK_AUDIO_MEDIA = new SettingPref(
            TYPE_GLOBAL, KEY_DOCK_AUDIO_MEDIA, Global.DOCK_AUDIO_MEDIA_ENABLED,
            DEFAULT_DOCK_AUDIO_MEDIA, DOCK_AUDIO_MEDIA_DISABLED, DOCK_AUDIO_MEDIA_ENABLED) {
        @Override
        public boolean isApplicable(Context context) {
            return hasDockSettings(context);
        }

        @Override
        protected String getCaption(Resources res, int value) {
            switch(value) {
                case DOCK_AUDIO_MEDIA_DISABLED:
                    return res.getString(R.string.dock_audio_media_disabled);
                case DOCK_AUDIO_MEDIA_ENABLED:
                    return res.getString(R.string.dock_audio_media_enabled);
                default:
                    throw new IllegalArgumentException();
            }
        }
    };

    private static final SettingPref PREF_EMERGENCY_TONE = new SettingPref(
            TYPE_GLOBAL, KEY_EMERGENCY_TONE, Global.EMERGENCY_TONE, DEFAULT_EMERGENCY_TONE,
            EMERGENCY_TONE_ALERT, EMERGENCY_TONE_VIBRATE, EMERGENCY_TONE_SILENT) {
        @Override
        public boolean isApplicable(Context context) {
            final int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
            return activePhoneType == TelephonyManager.PHONE_TYPE_CDMA;
        }

        @Override
        protected String getCaption(Resources res, int value) {
            switch(value) {
                case EMERGENCY_TONE_SILENT:
                    return res.getString(R.string.emergency_tone_silent);
                case EMERGENCY_TONE_ALERT:
                    return res.getString(R.string.emergency_tone_alert);
                case EMERGENCY_TONE_VIBRATE:
                    return res.getString(R.string.emergency_tone_vibrate);
                default:
                    throw new IllegalArgumentException();
            }
        }
    };

    private static final SettingPref PREF_VOLUME_STEPS_ALARM = new SettingPref(
            TYPE_PAC, KEY_VOLUME_STEPS_ALARM, PAC.VOLUME_STEPS_ALARM,
            VOLUME_STEPS_7,
            VOLUME_STEPS_5, VOLUME_STEPS_7, VOLUME_STEPS_15,
            VOLUME_STEPS_30, VOLUME_STEPS_45, VOLUME_STEPS_60) {
        @Override
        protected String getCaption(Resources res, int value) {
            return volStepsCaption(res, value);
        }

        @Override
        public void update(Context context) {
            super.update(context);
            final int steps = getInt(mType,
                    context.getContentResolver(), mSetting, mDefault);
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            updateVolumeSteps(audioManager, KEY_VOLUME_STEPS_ALARM,
                    audioManager.STREAM_ALARM, volSteps(steps));
         }
    };

    private static final SettingPref PREF_VOLUME_STEPS_DTMF = new SettingPref(
            TYPE_PAC, KEY_VOLUME_STEPS_DTMF, PAC.VOLUME_STEPS_DTMF,
            VOLUME_STEPS_15,
            VOLUME_STEPS_5, VOLUME_STEPS_7, VOLUME_STEPS_15,
            VOLUME_STEPS_30, VOLUME_STEPS_45, VOLUME_STEPS_60) {
        @Override
        protected String getCaption(Resources res, int value) {
            return volStepsCaption(res, value);
        }

        @Override
        public void update(Context context) {
            super.update(context);
            final int steps = getInt(mType,
                    context.getContentResolver(), mSetting, mDefault);
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            updateVolumeSteps(audioManager, KEY_VOLUME_STEPS_DTMF,
                    audioManager.STREAM_DTMF, volSteps(steps));
        }
    };

    private static final SettingPref PREF_VOLUME_STEPS_MUSIC = new SettingPref(
            TYPE_PAC, KEY_VOLUME_STEPS_MUSIC, PAC.VOLUME_STEPS_MUSIC,
            VOLUME_STEPS_15,
            VOLUME_STEPS_5, VOLUME_STEPS_7, VOLUME_STEPS_15,
            VOLUME_STEPS_30, VOLUME_STEPS_45, VOLUME_STEPS_60) {
        @Override
        protected String getCaption(Resources res, int value) {
            return volStepsCaption(res, value);
        }

        @Override
        public void update(Context context) {
            super.update(context);
            final int steps = getInt(mType,
                    context.getContentResolver(), mSetting, mDefault);
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            updateVolumeSteps(audioManager, KEY_VOLUME_STEPS_MUSIC,
                    audioManager.STREAM_MUSIC, volSteps(steps));
        }
    };

    private static final SettingPref PREF_VOLUME_STEPS_NOTIFICATION = new SettingPref(
            TYPE_PAC, KEY_VOLUME_STEPS_NOTIFICATION, PAC.VOLUME_STEPS_NOTIFICATION,
            VOLUME_STEPS_7,
            VOLUME_STEPS_5, VOLUME_STEPS_7, VOLUME_STEPS_15,
            VOLUME_STEPS_30, VOLUME_STEPS_45, VOLUME_STEPS_60) {
        @Override
        protected String getCaption(Resources res, int value) {
            return volStepsCaption(res, value);
        }

        @Override
        public void update(Context context) {
            super.update(context);
            final int steps = getInt(mType,
                    context.getContentResolver(), mSetting, mDefault);
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            updateVolumeSteps(audioManager, KEY_VOLUME_STEPS_NOTIFICATION,
                    audioManager.STREAM_NOTIFICATION, volSteps(steps));
        }
    };

    private static final SettingPref PREF_VOLUME_STEPS_RING = new SettingPref(
            TYPE_PAC, KEY_VOLUME_STEPS_RING, PAC.VOLUME_STEPS_RING,
            VOLUME_STEPS_7,
            VOLUME_STEPS_5, VOLUME_STEPS_7, VOLUME_STEPS_15,
            VOLUME_STEPS_30, VOLUME_STEPS_45, VOLUME_STEPS_60) {
        @Override
        protected String getCaption(Resources res, int value) {
            return volStepsCaption(res, value);
        }

        @Override
        public void update(Context context) {
            super.update(context);
            final int steps = getInt(mType,
                    context.getContentResolver(), mSetting, mDefault);
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            updateVolumeSteps(audioManager, KEY_VOLUME_STEPS_MUSIC,
                    audioManager.STREAM_RING, volSteps(steps));
        }
    };

    private static final SettingPref PREF_VOLUME_STEPS_SYSTEM = new SettingPref(
            TYPE_PAC, KEY_VOLUME_STEPS_SYSTEM, PAC.VOLUME_STEPS_SYSTEM,
            VOLUME_STEPS_7,
            VOLUME_STEPS_5, VOLUME_STEPS_7, VOLUME_STEPS_15,
            VOLUME_STEPS_30, VOLUME_STEPS_45, VOLUME_STEPS_60) {
        @Override
        protected String getCaption(Resources res, int value) {
            return volStepsCaption(res, value);
        }

        @Override
        public void update(Context context) {
            super.update(context);
            final int steps = getInt(mType,
                    context.getContentResolver(), mSetting, mDefault);
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            updateVolumeSteps(audioManager, KEY_VOLUME_STEPS_SYSTEM,
                    audioManager.STREAM_SYSTEM, volSteps(steps));
        }
    };

    private static final SettingPref PREF_VOLUME_STEPS_VOICE_CALL = new SettingPref(
            TYPE_PAC, KEY_VOLUME_STEPS_VOICE_CALL, PAC.VOLUME_STEPS_VOICE_CALL,
            VOLUME_STEPS_5,
            VOLUME_STEPS_5, VOLUME_STEPS_7, VOLUME_STEPS_15,
            VOLUME_STEPS_30, VOLUME_STEPS_45, VOLUME_STEPS_60) {
        @Override
        protected String getCaption(Resources res, int value) {
            return volStepsCaption(res, value);
        }

        @Override
        public void update(Context context) {
            super.update(context);
            final int steps = getInt(mType,
                    context.getContentResolver(), mSetting, mDefault);
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            updateVolumeSteps(audioManager, KEY_VOLUME_STEPS_VOICE_CALL,
                    audioManager.STREAM_VOICE_CALL, volSteps(steps));
        }
    };

    private static final SettingPref[] PREFS = {
        PREF_DIAL_PAD_TONES,
        PREF_SCREEN_LOCKING_SOUNDS,
        PREF_DOCKING_SOUNDS,
        PREF_VOLUME_ADJUST_SOUNDS,
        PREF_TOUCH_SOUNDS,
        PREF_DOCK_AUDIO_MEDIA,
        PREF_EMERGENCY_TONE,
        PREF_VOLUME_STEPS_ALARM,
        PREF_VOLUME_STEPS_DTMF,
        PREF_VOLUME_STEPS_MUSIC,
        PREF_VOLUME_STEPS_NOTIFICATION,
        PREF_VOLUME_STEPS_RING,
        PREF_VOLUME_STEPS_SYSTEM,
        PREF_VOLUME_STEPS_VOICE_CALL,
    };

    private final SettingsObserver mSettingsObserver = new SettingsObserver();

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.other_sound_settings);

        mContext = getActivity();

        // power state change notification sounds
        mPowerSounds = (SwitchPreference) findPreference(KEY_POWER_NOTIFICATIONS);
        mPowerSounds.setChecked(Global.getInt(getContentResolver(),
                Global.POWER_NOTIFICATIONS_ENABLED, 0) != 0);
        mPowerSoundsVibrate = (SwitchPreference) findPreference(KEY_POWER_NOTIFICATIONS_VIBRATE);
        mPowerSoundsVibrate.setChecked(Global.getInt(getContentResolver(),
                Global.POWER_NOTIFICATIONS_VIBRATE, 0) != 0);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            removePreference(KEY_POWER_NOTIFICATIONS_VIBRATE);
        }

        mPowerSoundsRingtone = findPreference(KEY_POWER_NOTIFICATIONS_RINGTONE);
        String currentPowerRingtonePath =
                Global.getString(getContentResolver(), Global.POWER_NOTIFICATIONS_RINGTONE);

        // set to default notification if we don't yet have one
        if (currentPowerRingtonePath == null) {
                currentPowerRingtonePath = System.DEFAULT_NOTIFICATION_URI.toString();
                Global.putString(getContentResolver(),
                        Global.POWER_NOTIFICATIONS_RINGTONE, currentPowerRingtonePath);
        }
        // is it silent ?
        if (currentPowerRingtonePath.equals(POWER_NOTIFICATIONS_SILENT_URI)) {
            mPowerSoundsRingtone.setSummary(
                    getString(R.string.power_notifications_ringtone_silent));
        } else {
            final Ringtone ringtone =
                    RingtoneManager.getRingtone(getActivity(), Uri.parse(currentPowerRingtonePath));
            if (ringtone != null) {
                mPowerSoundsRingtone.setSummary(ringtone.getTitle(getActivity()));
            }
        }

        for (SettingPref pref : PREFS) {
            pref.init(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSettingsObserver.register(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSettingsObserver.register(false);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPowerSounds) {
            Global.putInt(getContentResolver(),
                    Global.POWER_NOTIFICATIONS_ENABLED,
                    mPowerSounds.isChecked() ? 1 : 0);

        } else if (preference == mPowerSoundsVibrate) {
            Global.putInt(getContentResolver(),
                    Global.POWER_NOTIFICATIONS_VIBRATE,
                    mPowerSoundsVibrate.isChecked() ? 1 : 0);

        } else if (preference == mPowerSoundsRingtone) {
            launchNotificationSoundPicker(REQUEST_CODE_POWER_NOTIFICATIONS_RINGTONE,
                    Global.getString(getContentResolver(),
                            Global.POWER_NOTIFICATIONS_RINGTONE));
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    private static boolean hasDockSettings(Context context) {
        return context.getResources().getBoolean(R.bool.has_dock_settings);
    }

    // === Callbacks ===

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            final ContentResolver cr = getContentResolver();
            if (register) {
                for (SettingPref pref : PREFS) {
                    cr.registerContentObserver(pref.getUri(), false, this);
                }
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            for (SettingPref pref : PREFS) {
                if (pref.getUri().equals(uri)) {
                    pref.update(mContext);
                    return;
                }
            }
        }
    }

    // === Indexing ===

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

        public List<SearchIndexableResource> getXmlResourcesToIndex(
                Context context, boolean enabled) {
            final SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.other_sound_settings;
            return Arrays.asList(sir);
        }

        public List<String> getNonIndexableKeys(Context context) {
            final ArrayList<String> rt = new ArrayList<String>();
            for (SettingPref pref : PREFS) {
                if (!pref.isApplicable(context)) {
                    rt.add(pref.getKey());
                }
            }
            return rt;
        }
    };

    private void launchNotificationSoundPicker(int code, String currentPowerRingtonePath) {
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                getString(R.string.power_notifications_ringtone_title));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                System.DEFAULT_NOTIFICATION_URI);
        if (currentPowerRingtonePath != null &&
                !currentPowerRingtonePath.equals(POWER_NOTIFICATIONS_SILENT_URI)) {
            Uri uri = Uri.parse(currentPowerRingtonePath);
            if (uri != null) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
            }
        }
        startActivityForResult(intent, code);
    }

    private void setPowerNotificationRingtone(Intent intent) {
        final Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

        final String toneName;
        final String toneUriPath;

        if ( uri != null ) {
            final Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
            toneName = ringtone.getTitle(getActivity());
            toneUriPath = uri.toString();
        } else {
            // silent
            toneName = getString(R.string.power_notifications_ringtone_silent);
            toneUriPath = POWER_NOTIFICATIONS_SILENT_URI;
        }

        mPowerSoundsRingtone.setSummary(toneName);
        Global.putString(getContentResolver(),
                Global.POWER_NOTIFICATIONS_RINGTONE, toneUriPath);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_POWER_NOTIFICATIONS_RINGTONE:
                if (resultCode == Activity.RESULT_OK) {
                    setPowerNotificationRingtone(data);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    // === Volume Steps ===

    private static String volStepsCaption(Resources res, int value) {
        switch(value) {
            case VOLUME_STEPS_5:
                return res.getString(R.string.volume_steps_5);
            case VOLUME_STEPS_7:
                return res.getString(R.string.volume_steps_7);
            case VOLUME_STEPS_15:
                return res.getString(R.string.volume_steps_15);
            case VOLUME_STEPS_30:
                return res.getString(R.string.volume_steps_30);
            case VOLUME_STEPS_45:
                return res.getString(R.string.volume_steps_45);
            case VOLUME_STEPS_60:
                return res.getString(R.string.volume_steps_60);
            default:
                throw new IllegalArgumentException();
        }
    }

    private static int volSteps(int value) {
        switch(value) {
            case VOLUME_STEPS_5:
                return 5;
            case VOLUME_STEPS_7:
                return 7;
            case VOLUME_STEPS_15:
                return 15;
            case VOLUME_STEPS_30:
                return 30;
            case VOLUME_STEPS_45:
                return 45;
            case VOLUME_STEPS_60:
                return 60;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void updateVolumeSteps(AudioManager audioManager,
            String settingsKey, int streamType, int steps){
        //Change the setting live
        audioManager.setStreamMaxVolume(streamType, steps);
        Log.i(TAG, "Volume steps:" + settingsKey + "" + String.valueOf(steps));
    }
}
