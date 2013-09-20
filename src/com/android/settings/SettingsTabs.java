package com.android.settings;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class SettingsTabs extends TabActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);
        
        TabHost tabHost = getTabHost();
        
        // System
        TabSpec mSystem = tabHost.newTabSpec("System");
        mSystem.setIndicator("System");
        Intent SystemIntent = new Intent(this, MainSetting.class);
        mSystem.setContent(SystemIntent);
        
        // Pac Settings
        TabSpec mPacSettings = tabHost.newTabSpec("PacSettings");
        mPacSettings.setIndicator("PacSettings");
        Intent PacSettingsIntent = new Intent(this, PacSettings.class);
        mPacSettings.setContent(PacSettingsIntent);
        
        tabHost.addTab(mSystem);
        tabHost.addTab(mPacSettings);
	}
	
	/*
     * Settings subclasses for launching independently.
     */
    public static class BluetoothSettingsActivity extends MainSetting { /* empty */ }
    public static class WirelessSettingsActivity extends MainSetting { /* empty */ }
    public static class TetherSettingsActivity extends MainSetting { /* empty */ }
    public static class VpnSettingsActivity extends MainSetting { /* empty */ }
    public static class DateTimeSettingsActivity extends MainSetting { /* empty */ }
    public static class StorageSettingsActivity extends MainSetting { /* empty */ }
    public static class WifiSettingsActivity extends MainSetting { /* empty */ }
    public static class WifiP2pSettingsActivity extends MainSetting { /* empty */ }
    public static class InputMethodAndLanguageSettingsActivity extends MainSetting { /* empty */ }
    public static class KeyboardLayoutPickerActivity extends MainSetting { /* empty */ }
    public static class InputMethodAndSubtypeEnablerActivity extends MainSetting { /* empty */ }
    public static class SpellCheckersSettingsActivity extends MainSetting { /* empty */ }
    public static class LocalePickerActivity extends MainSetting { /* empty */ }
    public static class UserDictionarySettingsActivity extends MainSetting { /* empty */ }
    public static class SoundSettingsActivity extends MainSetting { /* empty */ }
    public static class DisplaySettingsActivity extends MainSetting { /* empty */ }
    public static class DeviceInfoSettingsActivity extends MainSetting { /* empty */ }
    public static class ApplicationSettingsActivity extends MainSetting { /* empty */ }
    public static class ManageApplicationsActivity extends MainSetting { /* empty */ }
    public static class AppOpsSummaryActivity extends MainSetting { /* empty */ }
    public static class StorageUseActivity extends MainSetting { /* empty */ }
    public static class DevelopmentSettingsActivity extends MainSetting { /* empty */ }
    public static class AccessibilitySettingsActivity extends MainSetting { /* empty */ }
    public static class SecuritySettingsActivity extends MainSetting { /* empty */ }
    public static class LocationSettingsActivity extends MainSetting { /* empty */ }
    public static class PrivacySettingsActivity extends MainSetting { /* empty */ }
    public static class RunningServicesActivity extends MainSetting { /* empty */ }
    public static class ManageAccountsSettingsActivity extends MainSetting { /* empty */ }
    public static class PowerUsageSummaryActivity extends MainSetting { /* empty */ }
    public static class AccountSyncSettingsActivity extends MainSetting { /* empty */ }
    public static class AccountSyncSettingsInAddAccountActivity extends MainSetting { /* empty */ }
    public static class CryptKeeperSettingsActivity extends MainSetting { /* empty */ }
    public static class DeviceAdminSettingsActivity extends MainSetting { /* empty */ }
    public static class DataUsageSummaryActivity extends MainSetting { /* empty */ }
    public static class AdvancedWifiSettingsActivity extends MainSetting { /* empty */ }
    public static class TextToSpeechSettingsActivity extends MainSetting { /* empty */ }
    public static class AndroidBeamSettingsActivity extends MainSetting { /* empty */ }
    public static class WifiDisplaySettingsActivity extends MainSetting { /* empty */ }
    public static class ApnSettingsActivity extends MainSetting { /* empty */ }
    public static class ApnEditorActivity extends MainSetting { /* empty */ }
    public static class DreamSettingsActivity extends MainSetting { /* empty */ }
    public static class NotificationShortcutsSettingsActivity extends MainSetting { /* empty */ }
    public static class QuietHoursSettingsActivity extends MainSetting { /* empty */ }
    public static class ProfilesSettingsActivity extends MainSetting { /* empty */ }
    public static class SystemSettingsActivity extends MainSetting { /* empty */ }
    public static class NotificationStationActivity extends MainSetting { /* empty */ }
    public static class UserSettingsActivity extends MainSetting { /* empty */ }
    public static class NotificationAccessSettingsActivity extends MainSetting { /* empty */ }
    public static class BlacklistSettingsActivity extends MainSetting { /* empty */ }
}
