//Gionee: mengdw <2015-11-11> add for CR01589343 begin
package com.cydroid.softmanager.trafficassistant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TrafficPreference {
    public static final int DATA_SIZE = 4;
    public static final String KEY_IS_CALIBRATED= "IsCalibrated"; 
    public static final String KEY_ONLY_LEFT_COMMON_FLAG = "OnlyLeftCommonFlag";
    public static final String KEY_ONLY_LEFT_IDLE_FLAG = "OnlyLeftIdle";
    public static final String KEY_SAVE_ACTUAL_FLOW = "SaveActualFlow";
    public static final String KEY_CALIBRATED_ACTUAL_FLOW = "CalibratedActualFlow";
    public static final String KEY_HAS_IDLE_DATA_FLAG = "HasIdleDataFlag";
    public static final String KEY_QUERY_CODE_STATE = "QueryCodeState";
    public static final String KEY_CALIBRATE_STATUS = "CalibrateStatus";
    
    public static final String KEY_CALIBRATE_PROVINCE_SETTING = "CalibrateProvinceSetting";
    public static final String KEY_CALIBRATE_BRAND_SETTING = "CalibrateBrandSetting";
    public static final int CALIBRATE_NO_SETTING = -1;

    public static final String KEY_COMMON_TOTAL = "CommonTotal";
    public static final String KEY_COMMON_LEFT= "CommonLeft"; 
    public static final String KEY_COMMON_USED= "CommonUsed"; 
    public static final String KEY_IDLE_TOTAL = "IdleTotal"; 
    public static final String KEY_IDLE_USED= "IdleUsed"; 
    public static final String KEY_IDLE_LEFT = "IdleLeft"; 
    public static final String KEY_WARNING_PERCENT = "WarningPercent";
    public static final String KEY_START_DATE = "StartDate";
    public static final String KEY_CURRENT_DATE = "CurrentDate";
    public static final String KEY_FIRST_ENTRY_FLAG = "TrafficFirstEntryFlag";
    public static final String KEY_TRAFFIC_MONITOR = "TrafficMonitor";
    public static final String KEY_TRAFFIC_PACKAGE_SETTED_FLAG = "TrafficPackageSettedFlag";
    
    public static final String KEY_HOTSPORT_REMIND_SETTED_INDEX= "HotsportRemindSettedIndex";
    public static final String KEY_HOTSPORT_REMIND_SETTED_VALUE= "HotsportRemindSettedValue";
    public static final String KEY_HOTSPORT_LAST_REMIND_DATE= "HotsportLastRemindDate";
    public static final String KEY_HOTSPORT_LAST_REMIND_TRAFFIC= "HotsportLastRemindTraffic";
    
    public static final String KEY_SIM_FLOWLINKFLAG = "SimFlowLinkFlag";
    public static final String KEY_SIM_STOPWARNING = "SimStopWarning";
    public static final String KEY_SIM_STOP_EXHAUST = "SimStopExhaustFlag";
    public static final String KEY_SIM_RESET = "SimReset";
    public static final String KEY_NOTIFICATION_TRAFFIC_INFO = "notficationTrafficInfo";
    
    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    private static final String KEY_LOCK_SCREEN_REMIND_SETTING_INDEX = "LockScreenRemindSettingIndex";
    // Gionee: mengdw <2016-12-06> add for CR01775579 end
    // Gionee: mengdw <2016-11-21> add for CR01772354 begin
    public static final String KEY_OPERA_SERVICE_TEL = "OperaServiceTel";
    // Gionee: mengdw <2016-11-21> add for CR01772354 end
    
    private static final String TRAFFIC_PREFERENCE = "TrafficPreference";
    // Gionee: mengdw <2016-05-04> add for CR01689390 begin
    private static final String TRAFFIC_DEFAULT_PREFERENCE = "com.cydroid.softmanager_preferences";
    // Gionee: mengdw <2016-05-04> add for CR01689390 end
    // Gionee: mengdw <2016-07-18> add for CR01639347 begin
    private static final String TRAFFIC_NETWORK_PREFEREENCE = "com.gionee.traffficNetcontrolPreferce_type";
    // Gionee: mengdw <2016-07-18> add for CR01639347 end
    
    public static void setSimIntPreference(Context context, int simIndex, String key, int value) {
        String name = TRAFFIC_PREFERENCE + simIndex;
        SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        info.edit().putInt(key, value).commit();
    }
    
    public static int getSimIntPreference(Context context, int simIndex, String key, int def) {
        String name = TRAFFIC_PREFERENCE + simIndex;
        SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        return info.getInt(key, def);
    }
    
    public static void setSimFloatPreference(Context context, int simIndex, String key, float value) {
        String name = TRAFFIC_PREFERENCE + simIndex;
        SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        info.edit().putFloat(key, value).commit();
    }
    
    public static float getSimFloatPreference(Context context, int simIndex, String key, float def) {
        String name = TRAFFIC_PREFERENCE + simIndex;
        SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        return info.getFloat(key, def);
    }
    
   public static void setSimStringPreference(Context context, int simIndex, String key, String value) {
       String name = TRAFFIC_PREFERENCE + simIndex;
       SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
       info.edit().putString(key, value).commit();
   }
   
   public static String getSimStringPreference(Context context, int simIndex, String key, String def) {
       String name = TRAFFIC_PREFERENCE + simIndex;
       SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
       return info.getString(key, def);
   }
   
   public static void setSimBooleanPreference(Context context, int simIndex, String key, boolean value) {
       String name = TRAFFIC_PREFERENCE + simIndex;
       SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
       info.edit().putBoolean(key, value).commit();
   }
   
   public static boolean getSimBooleanPreference(Context context, int simIndex, String key, boolean def) {
       String name = TRAFFIC_PREFERENCE + simIndex;
       SharedPreferences info = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
       return info.getBoolean(key, def);
   }
   
   // Gionee: mengdw <2016-05-04> modify for CR01689390 begin
    public static void setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        share.edit().putBoolean(key, value).commit();
    }

    public static boolean getBooleanPreference(Context context, String key, boolean def) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        return share.getBoolean(key, def);
    }

    public static void setIntPreference(Context context, String key, int value) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        share.edit().putInt(key, value).commit();
    }

    public static int getIntPreference(Context context, String key, int def) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        return share.getInt(key, def);
    }
   // Gionee: mengdw <2016-05-04> modify for CR01689390 end
    // Gionee: mengdw <2016-12-06> add for CR01775579 begin
    public static void saveLockScreenSettingPreference(Context context, int selectedIndex) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        share.edit().putInt(KEY_LOCK_SCREEN_REMIND_SETTING_INDEX, selectedIndex).commit();
    }
    
    public static int getLockScreenSettingPreference(Context context, int def) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        return share.getInt(KEY_LOCK_SCREEN_REMIND_SETTING_INDEX, def);
    }
    // Gionee: mengdw <2016-12-06> add for CR01775579 end
 // Gionee: mengdw <2016-11-21> add for CR01772354 begin
    public static void saveBuyNumberRecordPreference(Context context, String key, String value) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        share.edit().putString(key, value).commit();
    }

    public static String getBuyNumberRecordPreference(Context context, String key, String def) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        return share.getString(key, def);
    }
    
    public static void saveOperaServiceTel(Context context, String tel) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        share.edit().putString(KEY_OPERA_SERVICE_TEL, tel).commit();
    }
    
    public static String getOperaServiceTel(Context context, String def) {
        SharedPreferences share = context.getSharedPreferences(TRAFFIC_DEFAULT_PREFERENCE,
                Context.MODE_MULTI_PROCESS);
        return share.getString(KEY_OPERA_SERVICE_TEL, def);
    }
    // Gionee: mengdw <2016-11-21> add for CR01772354 end
}
//Gionee: mengdw <2015-11-11> add for CR01589343 end
