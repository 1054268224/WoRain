/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.softmanager.uninstall;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.UserHandle;

import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UninstallAppUtils {
    private static final String TAG = "UninstallAppUtils";

    private static final long MAX_WAIT_TIME = 4 * 1000;
    private static final long WAIT_TIME_INCR = 10;

    private static final long DAY_TIME = 24 * 60 * 60 * 1000;
    private static final long MONTH_TIME = DAY_TIME * 30*3;

    private static final String UNINSTALL_APP_SHOW_TYPE_PREFERENCES_NAME =
            "com.cydroid.softmanager.uninstall_app_show_type";
    private static final String UNINSTALL_APP_SHOW_TYPE_KEY = "uninstallAppShowType";

    private static Map<String, UsageStats> sUsageStats = new HashMap<>();

    public static void setUninstallShowType(Context context, int uninstallAppShowType) {
        SharedPreferences sp = context.getSharedPreferences(UNINSTALL_APP_SHOW_TYPE_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        sp.edit().putInt(UNINSTALL_APP_SHOW_TYPE_KEY, uninstallAppShowType).apply();
    }

    public static int getUninstallShowType(Context context) {
        SharedPreferences sp = context.getSharedPreferences(UNINSTALL_APP_SHOW_TYPE_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        return sp.getInt(UNINSTALL_APP_SHOW_TYPE_KEY, 0);
    }

    public static long getLastUpdateTime(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            Log.d(TAG, "getLastUpdateTime packageName:" + packageName + ", lastUpdateTime:" + info.lastUpdateTime);
            return info.lastUpdateTime;
        } catch (Exception e) {
            Log.d(TAG, "getLastUpdateTime e:" + e.getMessage());
            return 0;
        }
    }

    //guoxt modify for oversea O new api start 
    public static long invokeSSMGetPackageSize(Context context, String packageName) {
        long packageSize = 0;
        try {
            UUID UUID_DEFAULT = UUID.fromString("41217664-9172-527a-b3d5-edabb50a7d69");

            Object ssm = context.getSystemService("storagestats");
            Class<?> ssmClass = Class.forName("android.app.usage.StorageStatsManager");
            Method ssmMethod = ssmClass.getDeclaredMethod("queryStatsForPackage",
                    UUID.class, String.class, UserHandle.class);
            Object storageStats = ssmMethod.invoke(ssm, UUID_DEFAULT, packageName, new UserHandle(0));

            Class<?> ss = Class.forName("android.app.usage.StorageStats");
            Method methodGetDataBytes = ss.getDeclaredMethod("getDataBytes");
            Method methodGetCacheBytes = ss.getDeclaredMethod("getCacheBytes");
            Method methodGetCodeBytes = ss.getDeclaredMethod("getCodeBytes");
            long dataBytes = (long) methodGetDataBytes.invoke(storageStats);
            long cacheBytes = (long) methodGetCacheBytes.invoke(storageStats);
            long codeBytes = (long) methodGetCodeBytes.invoke(storageStats);
            Log.d(TAG, "dataBytes:" + dataBytes + " cacheBytes:" + cacheBytes + " codeBytes:" + codeBytes);
            packageSize = dataBytes + cacheBytes + codeBytes;
            Log.d(TAG, "invokeSSMGetPackageSizeInfo packageName= " + packageName + ", size = " + packageSize);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e);
        }
        return packageSize;
    }
    //guoxt modify for oversea O  end 


    public static long getAppFrequent(Context context, String packageName) {
        long lastUse = MONTH_TIME + 1;
        if (null == packageName || packageName.isEmpty()) {
            Log.d(TAG, "getFrequentAppList packageName1:" + packageName +
                    ", lastUse:" + lastUse);
            return lastUse;
        }

        if (null != sUsageStats && !sUsageStats.isEmpty()
                && null != sUsageStats.get(packageName)) {
            UsageStats stats = sUsageStats.get(packageName);
            lastUse = System.currentTimeMillis() - stats.getLastTimeUsed();
            Log.d(TAG, "getFrequentAppList packageName2:" + packageName +
                    ", lastUse:" + lastUse);
            return lastUse;
        }

        UsageStatsManager usageStatsManager = (UsageStatsManager)
                context.getSystemService("usagestats");
//        final List<UsageStats> statsList = usageStatsManager
//                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        final List<UsageStats> statsList = usageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, 0, System.currentTimeMillis());
        if (null == statsList) {
            return lastUse;
        }
        sUsageStats.clear();
        for (int i = 0; i < statsList.size(); ++i) {
            final UsageStats stats = statsList.get(i);
            sUsageStats.put(stats.getPackageName(), stats);
            if (packageName.equals(stats.getPackageName())) {
                if(stats.getLastTimeUsed()!= 0) {
                    lastUse = System.currentTimeMillis() - stats.getLastTimeUsed();
                }else{
                    lastUse = System.currentTimeMillis() - getLastUpdateTime(context,packageName);

                }
            }
        }

        Log.d(TAG, "getFrequentAppList packageName3:" + packageName +
                ", lastUse:" + lastUse);
        return lastUse;
    }

    // Gionee xionghonggang 2017-03-01 add for 73800 begin
    public static long getAppFrequentNew(Context context, String packageName) {
        long lastUse = MONTH_TIME + 1;
        if (null == sUsageStats || sUsageStats.isEmpty()) {
            reloadUsageStats(context);
            if (sUsageStats.isEmpty()) {
                return lastUse;
            }
        }
        UsageStats stats = sUsageStats.get(packageName);
        if (stats != null && stats.getLastTimeUsed()!= 0) {
                lastUse = System.currentTimeMillis() - stats.getLastTimeUsed();
            Log.d(TAG, "packageName:" + packageName +
                    ", now:" + getDateFromSeconds(System.currentTimeMillis()) +  ", lastUse:" + getDateFromSeconds(stats.getLastTimeUsed()));

            return lastUse;
        }
        else{
            Log.d(TAG, "packageName:" + packageName +
                    ", now:" + getDateFromSeconds(System.currentTimeMillis()) +  ", lastinstall:" + getDateFromSeconds(getLastUpdateTime(context,packageName)));
            lastUse = System.currentTimeMillis() - getLastUpdateTime(context,packageName);
            lastUse =  lastUse >0 ? lastUse:MONTH_TIME + 1;

            return lastUse;
        }
        //return lastUse;
    }

    public static String getDateFromSeconds(long seconds){
        if(seconds==0)
            return " ";
        else{
            Date date=new Date();
            try{
                date.setTime(seconds);
            }catch(NumberFormatException nfe){
            }
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
    }

    public static void reloadUsageStats(Context context) {
        if (null == sUsageStats) {
            sUsageStats = new HashMap<>();
        }
        UsageStatsManager usageStatsManager = (UsageStatsManager)
                context.getSystemService("usagestats");
        final List<UsageStats> statsList = usageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, 0, System.currentTimeMillis());

        sUsageStats.clear();
        for (int i = 0; i < statsList.size(); ++i) {
            final UsageStats stats = statsList.get(i);
            sUsageStats.put(stats.getPackageName(), stats);
        }
        Log.d(TAG, "reload UsageStats, new UsageStats list size is: " + sUsageStats.size());
    }
    // Gionee xionghonggang 2017-03-01 add for 73800 end    
}
