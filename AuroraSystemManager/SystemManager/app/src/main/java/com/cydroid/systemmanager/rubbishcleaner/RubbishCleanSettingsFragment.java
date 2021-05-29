package com.cydroid.systemmanager.rubbishcleaner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.cydroid.softmanager.R;

import cyee.preference.CyeeListPreference;
import cyee.preference.CyeePreference;
import cyee.preference.CyeePreference.OnPreferenceChangeListener;
import cyee.preference.CyeePreferenceFragment;
import cyee.preference.CyeePreferenceScreen;
import cyee.preference.CyeeSwitchPreference;

public class RubbishCleanSettingsFragment extends CyeePreferenceFragment implements
        OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
    private Context mAppContext;
    public static final String AUTO_UPDATE_KEY = "rubbish_auto_update_key";
    private static final String MANUAL_UPDATE_KEY = "rubbish_manual_update_key";
    private CyeeListPreference mRubbishSizeAlertListPref;
    private CyeeSwitchPreference mApkDelAlertSwitch;
    private CyeeSwitchPreference mResidualDelAlertSwitch;
    private CyeeSwitchPreference  mAutoUpdate;
    private CyeePreferenceScreen mManualUpdateScreen;
    private SharedPreferences mRubbishAutoUpdatePreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rubbish_clean_settings_prefs);
        mAppContext = getActivity().getApplicationContext();
        init();
        registerPreferenceListener();
        registerPreferenceChangeListener();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("rubbish_size_alert_key")) {
            setListSummary();
        } else if (key.equals(AUTO_UPDATE_KEY)) {
            if (mAutoUpdate.isChecked()) {
                mRubbishAutoUpdatePreferences.edit().putBoolean(AUTO_UPDATE_KEY, true).commit();
            } else {
                mRubbishAutoUpdatePreferences.edit().putBoolean(AUTO_UPDATE_KEY, false).commit();
            }
        }
    }


    @Override
    public boolean onPreferenceChange(CyeePreference arg0, Object arg1) {
        setListSummary();
        return true;
    }

    private void init() {
        mRubbishAutoUpdatePreferences = mAppContext.getSharedPreferences(AUTO_UPDATE_KEY, Context.MODE_PRIVATE);
        
        mRubbishSizeAlertListPref = (CyeeListPreference) findPreference("rubbish_size_alert_key");
        mApkDelAlertSwitch = (CyeeSwitchPreference) findPreference("apk_del_alert_key");
        mResidualDelAlertSwitch = (CyeeSwitchPreference) findPreference("residual_del_alert_key");
        
        mAutoUpdate = (CyeeSwitchPreference) findPreference(AUTO_UPDATE_KEY);
        //mManualUpdateScreen = (CyeePreferenceScreen) findPreference(MANUAL_UPDATE_KEY);
        //setManualClickListener();

        String defaultSize = "100";
        String nowSize = mRubbishSizeAlertListPref.getValue();
        if (nowSize == null) {
            mRubbishSizeAlertListPref.setValue(defaultSize);
        }

        setListSummary();
    }

	/*
    private void setManualClickListener() {
        mManualUpdateScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(CyeePreference arg0) {
                        Toast.makeText(mAppContext, R.string.rubbish_update_start_check,Toast.LENGTH_SHORT).show();
                        Intent servIntent = new Intent(mAppContext, KSCleanerUpdateService.class);
                        mAppContext.startService(servIntent);
                        return false;
                    }

                });
    }
    */

    private void registerPreferenceListener() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    private void registerPreferenceChangeListener() {
        mRubbishSizeAlertListPref.setOnPreferenceChangeListener(this);
    }

    private void setListSummary() {
        mRubbishSizeAlertListPref.setSummary(mRubbishSizeAlertListPref.getEntry());
    }

}
