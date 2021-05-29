package com.cydroid.softmanager;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.util.MemInfoReader;
import com.chenyee.featureoption.ServiceUtil;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.monitor.service.CpuRamMonitorService;
import com.cydroid.softmanager.monitor.service.ScreenOffCleanService;
import com.cydroid.softmanager.oneclean.WhiteListMrgActivity;
import com.cydroid.softmanager.powersaver.notification.PowerConsumeService;
import com.cydroid.softmanager.utils.Log;
import com.example.systemmanageruidemo.UnitUtil;

import java.lang.annotation.Native;

import cyee.preference.CyeePreference;
import cyee.preference.CyeePreferenceGroup;
import cyee.provider.CyeeSettings;

public class SettingPrefsFragmentWheatek extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, PreferenceManager.OnPreferenceTreeClickListener {
    private static final String TAG = "SettingsPrefsFragment";

    private static final int MEMORY_SIZE = 600 * 1024 * 1024;
    private static final String RESULT_NOTE = "clean_result_note";
    private static final String PROCESS_WHITE_LIST = "process_white_list";
    public static final String MEMORY_SETTINGS_KEY = "memory_settings_key";
    public static final String CLICK_WHITE_LIST = "click_white_list";
    public static final String BOOT_OPT_MANAGEMENT = "boot_opt";
    private static final String APP_PROCESS_LIMIT_KEY = "app_process_limit";
    private static final String CATEGORY_SYS_KEY = "category_sys";
    private static final String CATEGORY_KEY = "category_key";
    private static final String SCREENOFF_CLEAN_KEY = "screenoff_clean_key";
    private static final String SCREENOFF_CLEAN_TOAST_KEY = "screenoff_clean_toast_key";
    private static final String CATEGORY_AUTOCLEAN_KEY = "category_autoclean";
    private static final String CPU_OVERLOAD_MONITOR_KEY = "cpu_overload_monitor_key";
    private static final String POWER_CONSUME_KEY = "power_consume_key";
    private static final String AUTO_CACHECLEAN_TIMING_KEY = "auto_cacheclean_timing";
    public static final Uri NAVI_SETTING_URI = Uri.parse("content://navi.providers.setting/game");
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private final MemInfoReader mMemInfoReader = new MemInfoReader();
    private int fromautoscreen = 0;
    private int setFrom = 0;
    public static final int MAIN_SET = 0;
    private ListPreference limitList;
    private SwitchPreferenceCompat screenOffSw;
    private Preference whiteList;
    boolean defaultValue;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Consts.gnIPFlag) {
            setPreferencesFromResource(R.xml.systemmanager_settings_ip, rootKey);
        } else if (Consts.cy1703VF) {
            setPreferencesFromResource(R.xml.systemmanager_settings_1703vf, rootKey);
        } else {
            setPreferencesFromResource(R.xml.systemmanager_settings_wheatek, rootKey);
        }
        fromautoscreen = getActivity().getIntent().getIntExtra("is_from_autoclean", 0);
        setFrom = getActivity().getIntent().getIntExtra("set_from", 0);
        initprefs();
    }

    private void initprefs() {
        screenOffSw = (SwitchPreferenceCompat) findPreference(SCREENOFF_CLEAN_KEY);
        whiteList = (Preference) findPreference(PROCESS_WHITE_LIST);
        limitList = (ListPreference) findPreference(APP_PROCESS_LIMIT_KEY);
        initView();
    }

    private void initView() {
        if (Consts.cy1703VF) {
            if (fromautoscreen == 1) {
                removePrefs(CATEGORY_SYS_KEY);
                removePrefs(CATEGORY_KEY);
                screenOffSw.setSummary(getStr(R.string.screenoff_clean_setting_summary_sleep2h));
                screenOffSw.setTitle(getStr(R.string.system_manager_preference_screen_preference_title));
            } else {
                removePrefs(CATEGORY_AUTOCLEAN_KEY);
            }
        }
        if (setFrom == MAIN_SET) {
            removePrefs(CATEGORY_KEY);
            removePrefs(CATEGORY_AUTOCLEAN_KEY);
        } else {
            removePrefs(CATEGORY_SYS_KEY);
        }
        mMemInfoReader.readMemInfo();
        long ramSize = Util.translateCapacity(mMemInfoReader.getTotalSize());
        if (ramSize <= 1073741824L) {
            removePrefs(CPU_OVERLOAD_MONITOR_KEY);
        }
        if (!Consts.isWhistListSupport && findPreference(CATEGORY_KEY) != null) {
            removePrefs(CATEGORY_KEY);
        }

        String defaultSize = "不限制";
        String nowise = limitList.getValue();
        if (nowise == null) {
            limitList.setValue(defaultSize);
        }
        limitList.setSummary(limitList.getEntry());
        limitList.setOnPreferenceChangeListener(this::onPreferenceChange);
        if (!Consts.IS_TELECOM_PROJECT) {
            removePrefs(APP_PROCESS_LIMIT_KEY);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (fromautoscreen != 1) {
            loadSettings();
        }
    }

    private void loadSettings() {
        if (whiteList == null) {
            return;
        }
        whiteList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!mSharedPreferences.getBoolean(CLICK_WHITE_LIST, false)) {
                    mSharedPreferences.edit().putBoolean(CLICK_WHITE_LIST, true).commit();
                }
                mContext.startActivity(new Intent(mContext, WhiteListMrgActivity.class));
                return false;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            String value = newValue.toString();
            /*showToast(value);*/
            limitList.setValue(value);
            limitList.setSummary(value);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (preference.getKey().equals(POWER_CONSUME_KEY)) {
            boolean defaultValue = true;
            if (Consts.cyBAFlag) {
                defaultValue = false;
            }
            boolean val = pref.getBoolean(POWER_CONSUME_KEY, defaultValue);
            Log.d(TAG, "power_consume_key changed val=" + val);
            Intent startPowerConsumeAlarm = new Intent(mContext, PowerConsumeService.class);
            if (val) {
                startPowerConsumeAlarm.setAction(PowerConsumeService.ACTION_START_ALARM);
            } else {
                startPowerConsumeAlarm.setAction(PowerConsumeService.ACTION_STOP_ALARM);
            }
            ServiceUtil.startForegroundService(mContext, startPowerConsumeAlarm);
            return true;
        } else if (preference.getKey().equals(SCREENOFF_CLEAN_KEY)) {
            Intent cleanIntent = new Intent(mContext, ScreenOffCleanService.class);
            cleanIntent.setAction("com.cydroid.screenoffclean");
            Preference toastPref = (Preference) findPreference(SCREENOFF_CLEAN_TOAST_KEY);
            boolean defaultValue = true;
            if (Consts.gnIPFlag || Consts.cy1703VF || Consts.cyBAFlag) {
                defaultValue = false;
            }
            if (pref.getBoolean(SCREENOFF_CLEAN_KEY, defaultValue)) {
                if (!toastPref.isEnabled()) {
                    toastPref.setEnabled(true);
                }
                String str = "start";
                if (pref.getBoolean(SCREENOFF_CLEAN_TOAST_KEY, defaultValue)) {
                    str += "-showtoast";
                } else {
                    str += "-hidetoast";
                }
                cleanIntent.putExtra("event", str);
            } else {
                cleanIntent.putExtra("event", "stop");
            }
            ServiceUtil.startForegroundService(mContext, cleanIntent);
        } else if (preference.getKey().equals(SCREENOFF_CLEAN_TOAST_KEY)) {
            Intent cleanIntent = new Intent(mContext, ScreenOffCleanService.class);
            cleanIntent.setAction("com.cydroid.screenoffclean");
            boolean defaultValue = true;
            if (Consts.gnIPFlag || Consts.cy1703VF || Consts.cyBAFlag) {
                defaultValue = false;
            }
            if (pref.getBoolean(SCREENOFF_CLEAN_TOAST_KEY, defaultValue)) {
                cleanIntent.putExtra("event", "showtoast");
            } else {
                cleanIntent.putExtra("event", "hidetoast");
            }
            ServiceUtil.startForegroundService(mContext, cleanIntent);
        } else if (preference.getKey().equals(CPU_OVERLOAD_MONITOR_KEY)) {
            Intent cpuMonitorIntent = new Intent(mContext, CpuRamMonitorService.class);
            boolean flag = true;
            mMemInfoReader.readMemInfo();
            long ramSize = Util.translateCapacity(mMemInfoReader.getTotalSize());
            //1073741824L = 1GB
            if (ramSize <= 1073741824L || Consts.cyBAFlag) {
                flag = false;
            }
            if (pref.getBoolean(CPU_OVERLOAD_MONITOR_KEY, flag)) {
                cpuMonitorIntent.putExtra("event", "start");
            } else {
                cpuMonitorIntent.putExtra("event", "stop");
            }
            ServiceUtil.startForegroundService(mContext, cpuMonitorIntent);
            //Chenyee guoxt modify for CSW1703VF-53 begin
        } else if (preference.getKey().equals(AUTO_CACHECLEAN_TIMING_KEY)) {
            if (pref.getBoolean(AUTO_CACHECLEAN_TIMING_KEY, false)) {
                Settings.System.putInt(mContext.getContentResolver(), AUTO_CACHECLEAN_TIMING_KEY, 1);
                Intent intent = new Intent();
                intent.setAction("com.cydroid.systemmanager.enable.autoclean4");
                mContext.sendBroadcast(intent);
            } else {
                Settings.System.putInt(mContext.getContentResolver(), AUTO_CACHECLEAN_TIMING_KEY, 0);
                Intent intent = new Intent();
                intent.setAction("com.cydroid.systemmanager.disable.autoclean4");
                mContext.sendBroadcast(intent);
            }
        }

        return super.onPreferenceTreeClick(preference);
    }

    private String getStr(int id) {
        return UnitUtil.getStr(mContext, id);
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);
    }

    private void removePrefs(String key) {
        getPreferenceScreen().removePreferenceRecursively(key);
    }

    private void showToast(String arg) {
        Toast.makeText(getContext(), arg, Toast.LENGTH_SHORT).show();
    }
}
