package com.cydroid.systemmanager.rubbishcleaner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.cydroid.softmanager.R;

import cyee.preference.CyeeListPreference;
import cyee.preference.CyeePreference;
import cyee.preference.CyeePreference.OnPreferenceChangeListener;
import cyee.preference.CyeePreferenceFragment;
import cyee.preference.CyeePreferenceScreen;
import cyee.preference.CyeeSwitchPreference;

public class RubbishCleanSettingsFragmentWheatek extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener{
    private Context mAppContext;

    private static final String RUBSIZE_KEY = "rubbish_size_alert_key";
    private static final String APKDEL_KEY = "apk_del_alert_key";
    private static final String RESIDUALDEL_KEY = "residual_del_alert_key";


    private ListPreference mRubbishSizeAlertListPref;
    private SwitchPreferenceCompat mApkDelAlertSwitch;
    private SwitchPreferenceCompat mResidualDelAlertSwitch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.rubbish_clean_settings_wheatek,rootKey);
        mAppContext = getActivity().getApplicationContext();
        init();
        registerPreferenceListener();
        registerPreferenceChangeListener();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("rubbish_size_alert_key")) {
            setListSummary();
        }
    }


    private void init() {

        mRubbishSizeAlertListPref = (ListPreference) findPreference(RUBSIZE_KEY);
        mApkDelAlertSwitch = (SwitchPreferenceCompat) findPreference(APKDEL_KEY);
        mResidualDelAlertSwitch = (SwitchPreferenceCompat) findPreference(RESIDUALDEL_KEY);


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

    }

    private void setListSummary() {
        mRubbishSizeAlertListPref.setSummary(mRubbishSizeAlertListPref.getEntry());
    }

}
