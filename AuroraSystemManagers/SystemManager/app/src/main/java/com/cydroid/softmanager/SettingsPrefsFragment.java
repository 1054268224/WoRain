package com.cydroid.softmanager;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.android.internal.util.MemInfoReader;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.monitor.service.CpuRamMonitorService;
import com.cydroid.softmanager.monitor.service.ScreenOffCleanService;
import com.cydroid.softmanager.oneclean.WhiteListMrgActivity;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper;
import com.cydroid.softmanager.powersaver.notification.PowerConsumeService;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;
import com.cydroid.softmanager.utils.Log;

import cyee.preference.CyeeListPreference;
import cyee.preference.CyeePreference;
import cyee.preference.CyeePreference.OnPreferenceChangeListener;
import cyee.preference.CyeePreference.OnPreferenceClickListener;
import cyee.preference.CyeePreferenceFragment;
import cyee.preference.CyeePreferenceGroup;
import cyee.preference.CyeePreferenceManager;
import cyee.preference.CyeePreferenceScreen;
import cyee.preference.CyeeSwitchPreference;

import com.chenyee.featureoption.ServiceUtil;

public class SettingsPrefsFragment extends CyeePreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "SettingsPrefsFragment";

    private static final int MEMORY_SIZE = 600 * 1024 * 1024;
    private static final String RESULT_NOTE = "clean_result_note";
    private static final String PROCESS_WHITE_LIST = "process_white_list";
    public static final String MEMORY_SETTINGS_KEY = "memory_settings_key";
    public static final String CLICK_WHITE_LIST = "click_white_list";
    public static final String BOOT_OPT_MANAGEMENT = "boot_opt";
    private static final String APP_PROCESS_LIMIT_KEY = "app_process_limit";
    public static final Uri NAVI_SETTING_URI = Uri.parse("content://navi.providers.setting/game");
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private CyeeListPreference mAppProcessLimit;
    private final MemInfoReader mMemInfoReader = new MemInfoReader();
    private int fromautoscreen = 0;
    private int setFrom = 0;
    public static final int MAIN_SET = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Chenyee guoxt modify for CSW1703VF-53 begin
        if (Consts.gnIPFlag) {
            addPreferencesFromResource(R.xml.systemmanager_settings_ip);
        } else if (Consts.cy1703VF) {
            addPreferencesFromResource(R.xml.systemmanager_settings_1703vf);
        } else {
            addPreferencesFromResource(R.xml.systemmanager_settings);
        }
        //Chenyee guoxt modify for CSW1703VF-53 begin
        //addPreferencesFromResource(R.xml.systemmanager_settings);
        mAppProcessLimit = addListPreference(APP_PROCESS_LIMIT_KEY);
        //Chenyee guoxt modify for CSW1703VF-53 begin
        fromautoscreen = 0;
        fromautoscreen = getActivity().getIntent().getIntExtra("is_from_autoclean", 0);
        setFrom = getActivity().getIntent().getIntExtra("set_from", 0);
        if (Consts.cy1703VF) {
            if (fromautoscreen == 1) {
                getPreferenceScreen().removePreference(findPreference("category_sys"));
                getPreferenceScreen().removePreference(findPreference("category_key"));
                CyeeSwitchPreference prefSwitch = (CyeeSwitchPreference) findPreference("screenoff_clean_key");
                prefSwitch.setSummary(mContext.getResources().getString((R.string.screenoff_clean_setting_summary_sleep2h)));
                getActivity().setTitle(mContext.getResources().getString((R.string.system_manager_preference_screen_preference_title)));
            } else {
                getPreferenceScreen().removePreference(findPreference("category_autoclean"));
            }
        }
        //Chenyee guoxt modify for CSW1703VF-53 end

        if (setFrom == MAIN_SET) {
            //from Boost mainActivity
            getPreferenceScreen().removePreference(findPreference("category_key"));
            getPreferenceScreen().removePreference(findPreference("category_autoclean"));
        } else {
            //from Boost speed
            getPreferenceScreen().removePreference(findPreference("category_sys"));
        }

        /*guoxt modfiy for CR01692103 begin */
        mMemInfoReader.readMemInfo();
        long ramSize = getRamTotalMemory();
        //1073741824L = 1GB
        if (ramSize <= 1073741824L) {
            removePreference("cpu_overload_monitor_key");
        }
        /*guoxt modfiy for CR01692103 end */
        //add by chenxuanyu start
        if (!Consts.isWhistListSupport && findPreference("category_key") != null) {
            getPreferenceScreen().removePreference(findPreference("category_key"));
        }
        //add by chenxuanyu end
    }


    private void removePreference(String prefKey) {
        CyeeSwitchPreference pref = (CyeeSwitchPreference) findPreference(prefKey);
        ((CyeePreferenceGroup) findPreference("category_sys")).removePreference(pref);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mSharedPreferences = CyeePreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Chenyee guoxt modify for CSW1703VF-53 begin
        if (fromautoscreen != 1) {
            loadSettings();
            updateAppProcessLimitOptions();
        }
        //Chenyee guoxt modify for CSW1703VF-53 end
    }

    public static boolean smallMemoryPhone() {
        return (RamAndMemoryHelper.getTotalMem() < MEMORY_SIZE);
    }

    public static boolean getWhitelistFlag(Context context, SharedPreferences sharedPreferences) {
        if (sharedPreferences == null) {
            sharedPreferences = CyeePreferenceManager.getDefaultSharedPreferences(context);
        }

        return sharedPreferences.getBoolean(CLICK_WHITE_LIST, false);
    }

    private void setWhitelistFlag(SharedPreferences sharedPreferences) {
        if (!sharedPreferences.getBoolean(CLICK_WHITE_LIST, false)) {
            sharedPreferences.edit().putBoolean(CLICK_WHITE_LIST, true).commit();
        }
    }

    private void loadSettings() {
        CyeePreference whiteList = (CyeePreference) findPreference(PROCESS_WHITE_LIST);
        if (whiteList == null) {
            return;
        }
        whiteList.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(CyeePreference arg0) {
                setWhitelistFlag(mSharedPreferences);
                actionSetWhiteList(mContext);
                return false;
            }
        });
    }

    // Gionee <yangxinruo> <2015-10-29> add for CR01576434 begin
    @Override
    public boolean onPreferenceTreeClick(CyeePreferenceScreen preferenceScreen, CyeePreference preference) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (preference.getKey().equals("power_consume_key")) {
            boolean defaultValue = true;
            if (Consts.cyBAFlag) {
                defaultValue = false;
            }
            boolean val = pref.getBoolean("power_consume_key", defaultValue);
            Log.d(TAG, "power_consume_key changed val=" + val);
            Intent startPowerConsumeAlarm = new Intent(mContext, PowerConsumeService.class);
            if (val) {
                startPowerConsumeAlarm.setAction(PowerConsumeService.ACTION_START_ALARM);
            } else {
                startPowerConsumeAlarm.setAction(PowerConsumeService.ACTION_STOP_ALARM);
            }
            ServiceUtil.startForegroundService(mContext, startPowerConsumeAlarm);
            return true;
            // Gionee <yangxinruo> <2016-1-5> add for CR01618272 begin
        } else if (preference.getKey().equals("screenoff_clean_key")) {
            Intent cleanIntent = new Intent(mContext, ScreenOffCleanService.class);
            cleanIntent.setAction("com.cydroid.screenoffclean");
            CyeePreference toastPref = (CyeePreference) findPreference("screenoff_clean_toast_key");
            /*guoxt modify for begin*/
            boolean defaultValue = true;
            //Chenyee guoxt modify for CSW1703VF-53 begin
            if (Consts.gnIPFlag || Consts.cy1703VF || Consts.cyBAFlag) {
                defaultValue = false;
            }
            //Chenyee guoxt modify for CSW1703VF-53 end
            if (pref.getBoolean("screenoff_clean_key", defaultValue)) {
                if (!toastPref.isEnabled()) {
                    toastPref.setEnabled(true);
                }
                String str = "start";
                if (pref.getBoolean("screenoff_clean_toast_key", defaultValue)) {
                    str += "-showtoast";
                } else {
                    str += "-hidetoast";
                }
                cleanIntent.putExtra("event", str);
            } else {
                cleanIntent.putExtra("event", "stop");
            }
            /*guoxt modify for end*/
            ServiceUtil.startForegroundService(mContext, cleanIntent);
        } else if (preference.getKey().equals("screenoff_clean_toast_key")) {
            Intent cleanIntent = new Intent(mContext, ScreenOffCleanService.class);
            //Chenyee guoxt modify for CSW1703VF-53 begin
            cleanIntent.setAction("com.cydroid.screenoffclean");
            //Chenyee guoxt modify for CSW1703VF-53 end

            boolean defaultValue = true;
            //Chenyee guoxt modify for CSW1703VF-53 begin
            if (Consts.gnIPFlag || Consts.cy1703VF || Consts.cyBAFlag) {
                defaultValue = false;
            }
            //Chenyee guoxt modify for CSW1703VF-53 end

            if (pref.getBoolean("screenoff_clean_toast_key", defaultValue)) {
                cleanIntent.putExtra("event", "showtoast");
            } else {
                cleanIntent.putExtra("event", "hidetoast");
            }
            ServiceUtil.startForegroundService(mContext, cleanIntent);
            // Gionee <yangxinruo> <2016-1-5> add for CR01618272 end
            // Gionee <yangxinruo> <2015-12-11> add for CR01608017 begin
        } else if (preference.getKey().equals("cpu_overload_monitor_key")) {
            Intent cpuMonitorIntent = new Intent(mContext, CpuRamMonitorService.class);
            /*guoxt modfiy for CR01692103 begin */
            boolean flag = true;
            mMemInfoReader.readMemInfo();
            long ramSize = getRamTotalMemory();
            //1073741824L = 1GB
            if (ramSize <= 1073741824L || Consts.cyBAFlag) {
                flag = false;
            }
            /*guoxt modfiy for CR01692103 end */
            if (pref.getBoolean("cpu_overload_monitor_key", flag)) {
                cpuMonitorIntent.putExtra("event", "start");
            } else {
                cpuMonitorIntent.putExtra("event", "stop");
            }
            ServiceUtil.startForegroundService(mContext, cpuMonitorIntent);
            //Chenyee guoxt modify for CSW1703VF-53 begin
        } else if (preference.getKey().equals("auto_cacheclean_timing")) {
            if (pref.getBoolean("auto_cacheclean_timing", false)) {
                Settings.System.putInt(mContext.getContentResolver(), "auto_cacheclean_timing", 1);
                Intent intent = new Intent();
                intent.setAction("com.cydroid.systemmanager.enable.autoclean4");
                mContext.sendBroadcast(intent);
            } else {
                Settings.System.putInt(mContext.getContentResolver(), "auto_cacheclean_timing", 0);
                Intent intent = new Intent();
                intent.setAction("com.cydroid.systemmanager.disable.autoclean4");
                mContext.sendBroadcast(intent);
            }
        }
        //Chenyee guoxt modify for CSW1703VF-53 end
        // Gionee <yangxinruo> <2015-12-11> add for CR01608017 end
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    // Gionee <yangxinruo> <2015-10-29> add for CR01576434 end

    /*guoxt modfiy for CR01692103 begin */
    public long getRamTotalMemory() {
        long totalSize = mMemInfoReader.getTotalSize();
        return Util.translateCapacity(totalSize);
    }
    /*guoxt modfiy for CR01692103 end */

    public boolean onPreferenceChange(CyeePreference preference, Object newValue) {
        if (preference == mAppProcessLimit) {
            writeAppProcessLimitOptions(newValue);
            return true;
        }
        return false;
    }

    private CyeeListPreference addListPreference(String prefKey) {
        CyeeListPreference pref = (CyeeListPreference) findPreference(prefKey);
        pref.setOnPreferenceChangeListener(this);
        if (!Consts.IS_TELECOM_PROJECT) {
            ((CyeePreferenceGroup) findPreference("category_key")).removePreference(pref);
        }
        return pref;
    }

    private void updateAppProcessLimitOptions() {
        try {
            int limit = ActivityManagerNative.getDefault().getProcessLimit();
            CharSequence[] values = mAppProcessLimit.getEntryValues();
            String appProcessLimit = getResources().getString(R.string.app_process_limit_txt);
            for (int i = 0; i < values.length; i++) {
                int val = Integer.parseInt(values[i].toString());
                if (val >= limit) {
                    mAppProcessLimit.setValueIndex(i);
                    if (i == 0) {
                        mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[0]);
                    } else {
                        mAppProcessLimit
                                .setSummary(String.format(appProcessLimit, mAppProcessLimit.getEntries()[i]));
                    }
                    return;
                }
            }
            mAppProcessLimit.setValueIndex(0);
            mAppProcessLimit.setSummary(mAppProcessLimit.getEntries()[0]);
        } catch (RemoteException e) {
        }
    }

    private void writeAppProcessLimitOptions(Object newValue) {
        try {
            int limit = newValue != null ? Integer.parseInt(newValue.toString()) : -1;
            ActivityManagerNative.getDefault().setProcessLimit(limit);
            updateAppProcessLimitOptions();
            mSharedPreferences.edit().putInt(APP_PROCESS_LIMIT_KEY, limit).commit();
        } catch (RemoteException e) {
        }
    }

    public void actionSetWhiteList(Context context) {
        Intent intent = new Intent(context, WhiteListMrgActivity.class);
        context.startActivity(intent);
    }
}
