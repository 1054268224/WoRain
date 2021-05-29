/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 应用功耗数据采集器
 *
 * Revised Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.cydroid.softmanager.powersaver.analysis.AppBatteryInfo;
import com.cydroid.softmanager.powersaver.analysis.utils.BatteryDataUtils;
import com.cydroid.softmanager.powersaver.analysis.utils.UsageStatsDataUtils;
import com.cydroid.softmanager.powersaver.analysis.utils.UsageStatsDataUtils.SimpleUsageData;
import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppsBatteryDataCollector extends BatteryDataCollector {
    private static final String TAG = "AppsBatteryDataCollector";
    private static final boolean DEBUG = true;
    private static final String LAST_SAVED_DATA_PTAH = "data/data/com.cydroid.softmanager/mLastBatteryInfo.obj";
    private static final String WORKING_PERIOD_DATA_PTAH = "data/data/com.cydroid.softmanager/mTotalBatteryInfo.obj";
    private static final String RESULT_DATA_SET_PTAH = "data/data/com.cydroid.softmanager/allTotalBatteryInfo.obj";

    AppsBatteryDataCollector(Context context) {
        super(context);
    }

    @Override
    public void onTimePeriodChange(BatteryStatsHelper normalBatteryStats, String periodLabel) {
        Map<String, AppBatteryInfo> currentAppBatteryInfoMap = getAppBatteryInfoMap(normalBatteryStats);
        SmartCleanInfoWriter.log(TAG, "onTimePeriodChange-----> currentAppBatteryInfoMap.size = "
                + currentAppBatteryInfoMap.size() + "(apps)");
        Map<String, Map<String, AppBatteryInfo>> resultAppBatteryInfoSet = getDataFromFile(
                RESULT_DATA_SET_PTAH);
        if (resultAppBatteryInfoSet == null) {
            resultAppBatteryInfoSet = new HashMap<String, Map<String, AppBatteryInfo>>();
        }
        Map<String, AppBatteryInfo> workingPeriodAppBatteryInfoMap = getDataFromFile(
                WORKING_PERIOD_DATA_PTAH);
        if (workingPeriodAppBatteryInfoMap == null) {
            workingPeriodAppBatteryInfoMap = new HashMap<String, AppBatteryInfo>();
        }
        Map<String, AppBatteryInfo> lastRecordAppBatteryInfoMap = getDataFromFile(LAST_SAVED_DATA_PTAH);

        if (lastRecordAppBatteryInfoMap == null || lastRecordAppBatteryInfoMap.isEmpty()) { // A点
            workingPeriodAppBatteryInfoMap.clear(); // total清空
            Log.i(TAG, "lastRecordData.isEmpty() --> currentRecordData.clear()");
        } else {
            int lastBatteryStatsRecordMode = getBatteryInfoRecordMode();
            Log.d(TAG, "onTimePeriodChange last data record mode = " + lastBatteryStatsRecordMode);
            if (lastBatteryStatsRecordMode == RECORD_MODE_TIME_PERIOD_CHANGED) {
                Log.i(TAG,
                        "onTimePeriodChange lastmode=RECORD_MODE_TIME_PERIOD_CHANGED get subtractionAppBatteryInfoMap");
                workingPeriodAppBatteryInfoMap = BatteryDataUtils
                        .subtractionAppBatteryInfoMap(currentAppBatteryInfoMap, lastRecordAppBatteryInfoMap);
            } else if (lastBatteryStatsRecordMode == RECORD_MODE_BATTERY_STATS_RESET) {
                Log.i(TAG,
                        "onTimePeriodChange lastmode=RECORD_MODE_BATTERY_STATS_RESET get addAppBatteryInfoMap");
                workingPeriodAppBatteryInfoMap = BatteryDataUtils
                        .addAppBatteryInfoMap(workingPeriodAppBatteryInfoMap, currentAppBatteryInfoMap);
            } else {
                workingPeriodAppBatteryInfoMap.clear(); // total清空
                Log.i(TAG, "onTimePeriodChange lastmode unknow--> currentRecordData.clear()");
            }
            Log.i(TAG, "resultDataSet.put " + periodLabel + ", currentRecordData.size "
                    + workingPeriodAppBatteryInfoMap.size());
            if (!workingPeriodAppBatteryInfoMap.isEmpty()) {
                resultAppBatteryInfoSet.put(periodLabel, (Map<String, AppBatteryInfo>) BatteryDataUtils
                        .deepClone(workingPeriodAppBatteryInfoMap)); // 累计数据作为前一时段的最终数据
                workingPeriodAppBatteryInfoMap.clear(); // 重置累计值
            }
        }

        lastRecordAppBatteryInfoMap = currentAppBatteryInfoMap;
        SmartCleanInfoWriter.log(TAG, "onTimePeriodChange-----> resultAppBatteryInfoSet.size = "
                + resultAppBatteryInfoSet.size() + "(days)");
        if (resultAppBatteryInfoSet.size() == 0) {
            Log.d(TAG, "collect nothing today ,show batterystats data = " + currentAppBatteryInfoMap);
        }
        saveDataToFile(WORKING_PERIOD_DATA_PTAH, (Serializable) workingPeriodAppBatteryInfoMap);
        saveDataToFile(LAST_SAVED_DATA_PTAH, (Serializable) lastRecordAppBatteryInfoMap);
        saveDataToFile(RESULT_DATA_SET_PTAH, (Serializable) resultAppBatteryInfoSet);
    }

    private Map<String, AppBatteryInfo> getAppBatteryInfoMap(BatteryStatsHelper batteryStats) {
        Map<String, AppBatteryInfo> batteryInfo = new HashMap<String, AppBatteryInfo>();
        List<BatterySipper> sippers = batteryStats.getUsageList();
        for (BatterySipper sipper : sippers) {
            AppBatteryInfo appinfo = new AppBatteryInfo(sipper);
            if (isSystemProcessPowerInfo(appinfo)) {
                continue;
            }
            appinfo.packageWithHighestDrain = appinfo.packageWithHighestDrain.split(":")[0];
            if (batteryInfo.containsKey(appinfo.packageWithHighestDrain)) {
                batteryInfo.get(appinfo.packageWithHighestDrain).add(appinfo);
                Log.d(DEBUG, TAG, "getAppBatteryInfoFromStats ADD appinfo -------> " + appinfo.toString());
            } else {
                batteryInfo.put(appinfo.packageWithHighestDrain, appinfo);
                Log.d(DEBUG, TAG, "getAppBatteryInfoFromStats GET appinfo -------> " + appinfo.toString());
            }
        }
        return batteryInfo;
    }

    private boolean isSystemProcessPowerInfo(AppBatteryInfo appinfo) {
        return appinfo.packageWithHighestDrain == null || appinfo.userId <= 10000;
    }

    @Override
    public void onBatteryStatsReset(BatteryStatsHelper backupBatteryStats) {
        Map<String, AppBatteryInfo> backupAppBatteryPowerMap = getAppBatteryInfoMap(backupBatteryStats);
        SmartCleanInfoWriter.log(TAG, "onBatteryStatsReset-----> backupAppBatteryPowerMap.size = "
                + backupAppBatteryPowerMap.size() + "(apps)");
        Map<String, AppBatteryInfo> workingPeriodAppBatteryInfoMap = getDataFromFile(
                WORKING_PERIOD_DATA_PTAH);
        if (workingPeriodAppBatteryInfoMap == null) {
            workingPeriodAppBatteryInfoMap = new HashMap<String, AppBatteryInfo>();
        }
        Map<String, AppBatteryInfo> lastRecordAppBatteryInfoMap = getDataFromFile(LAST_SAVED_DATA_PTAH);

        if (lastRecordAppBatteryInfoMap == null || lastRecordAppBatteryInfoMap.isEmpty()) { // B点
            workingPeriodAppBatteryInfoMap.clear();
            Log.i(TAG, "lastRecordData.isEmpty() --> currentRecordData.clear()");
        } else {
            int lastBatteryStatsRecordMode = getBatteryInfoRecordMode();
            Log.d(TAG, "onBatteryStatsReset last data record mode = " + lastBatteryStatsRecordMode);
            if (lastBatteryStatsRecordMode == RECORD_MODE_TIME_PERIOD_CHANGED) {
                Log.i(TAG,
                        "onBatteryStatsReset lastmode=RECORD_MODE_TIME_PERIOD_CHANGED get subtractionAppBatteryInfoMap");
                workingPeriodAppBatteryInfoMap = BatteryDataUtils
                        .subtractionAppBatteryInfoMap(backupAppBatteryPowerMap, lastRecordAppBatteryInfoMap);
            } else if (lastBatteryStatsRecordMode == RECORD_MODE_BATTERY_STATS_RESET) {
                Log.i(TAG,
                        "onBatteryStatsReset lastmode=RECORD_MODE_BATTERY_STATS_RESET get addAppBatteryInfoMap");
                workingPeriodAppBatteryInfoMap = BatteryDataUtils
                        .addAppBatteryInfoMap(workingPeriodAppBatteryInfoMap, backupAppBatteryPowerMap);
            } else {
                workingPeriodAppBatteryInfoMap.clear();
                Log.i(TAG, "lastRecordData.isEmpty() --> currentRecordData.clear()");
            }
        }
        lastRecordAppBatteryInfoMap = backupAppBatteryPowerMap;

        saveDataToFile(WORKING_PERIOD_DATA_PTAH, (Serializable) workingPeriodAppBatteryInfoMap);
        saveDataToFile(LAST_SAVED_DATA_PTAH, (Serializable) lastRecordAppBatteryInfoMap);
    }

    @Override
    public List<Map<String, Object>> getAndFlushNewDataSet(String workingPeriodLabel) {
        ArrayList<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        Map<String, Map<String, AppBatteryInfo>> resultAppBatteryInfoSet = getDataFromFile(
                RESULT_DATA_SET_PTAH);
        String imei = HelperUtils.getImei(mContext);
        if (!isTestMode() && imei.isEmpty()) {
            Log.d(TAG, "invalid imei, do not upload data!");
            return new ArrayList<Map<String, Object>>();
        }
        Log.i(TAG,
                "getAndFlushNewDataSet resultAppBatteryInfoSet size --> " + resultAppBatteryInfoSet.size());
        PackageManager packageManger = mContext.getPackageManager();
        Iterator<Entry<String, Map<String, AppBatteryInfo>>> it = resultAppBatteryInfoSet.entrySet()
                .iterator();
        while (it.hasNext()) {
            Entry<String, Map<String, AppBatteryInfo>> appInfoEntry = it.next();
            String periodLabel = appInfoEntry.getKey();
            Log.i(TAG, "dayTime periodLabel ---> " + periodLabel);
            if (periodLabel.equals(workingPeriodLabel)) {
                Log.d(TAG, "do not upload data in workingPeriodLabel = " + workingPeriodLabel);
                continue;
            }
            Map<String, AppBatteryInfo> appBatteryInfoMap = appInfoEntry.getValue();
            Log.i(TAG, "batteryInfo.size ---> " + appBatteryInfoMap.size());
            ConcurrentHashMap<String, SimpleUsageData> usageStats = UsageStatsDataUtils
                    .getAllPackageUsageStats(mContext, periodLabel);
            for (Map.Entry<String, AppBatteryInfo> entry : appBatteryInfoMap.entrySet()) {
                String pkgName = entry.getKey().split(":")[0];
                if (pkgName == null || pkgName.isEmpty()) {
                    continue;
                }
                String pkgLabel = "";
                String pkgVersion = "";
                try {
                    ApplicationInfo appinfo = HelperUtils.getApplicationInfo(mContext, pkgName);
                    if (appinfo != null) {
                        pkgLabel = appinfo.loadLabel(packageManger).toString();
                    }
                    PackageInfo pi = packageManger.getPackageInfo(pkgName, 0);
                    pkgVersion = pi.versionName;
                } catch (Exception e) {
                    Log.d(TAG, "can not get package label or version:" + pkgName);
                    pkgLabel = "";
                    pkgVersion = "";
                }
                AppBatteryInfo appBatteryInfo = entry.getValue();
                long usageTime = UsageStatsDataUtils.getTimeInForeground(usageStats, pkgName);
                int launchCount = UsageStatsDataUtils.getLaunchCount(usageStats, pkgName);
                if (!isAppDataValid(appBatteryInfo, usageTime, launchCount)) {
                    Log.d(TAG, "INVALID APP DATA :" + appBatteryInfo.toString());
                    continue;
                }
                Map<String, Object> postDataMap = new HashMap<String, Object>();
                postDataMap.put("date", periodLabel);
                postDataMap.put("pkgName", pkgName);
                postDataMap.put("pkgLabel", pkgLabel);
                postDataMap.put("appName", pkgLabel);
                postDataMap.put("pkgVersion", pkgVersion);
                postDataMap.put("appVersion", pkgVersion);
                postDataMap.put("powerValue", appBatteryInfo.powerValue);// mAh
                postDataMap.put("usageTime", millisToSeconds(usageTime));// second
                postDataMap.put("cpuTime", millisToSeconds(appBatteryInfo.cpuTime));// second
                postDataMap.put("cpuFgTime", millisToSeconds(appBatteryInfo.cpuFgTime));// second
                postDataMap.put("launchCount", launchCount);
                postDataMap.put("mobileRxBytes", appBatteryInfo.mobileRxBytes);
                postDataMap.put("mobileTxBytes", appBatteryInfo.mobileTxBytes);
                postDataMap.put("wifiRxBytes", appBatteryInfo.wifiRxBytes);
                postDataMap.put("wifiTxBytes", appBatteryInfo.wifiTxBytes);
                res.add(postDataMap);
            }
            usageStats = null;
            it.remove();
        }
        Log.d(TAG, "save flushed dataSet");
        saveDataToFile(RESULT_DATA_SET_PTAH, (Serializable) resultAppBatteryInfoSet);
        return res;
    }

    private boolean isTestMode() {
        return DEBUG;
    }

    private boolean isAppDataValid(AppBatteryInfo appBatteryInfo, long usageTime, int launchCount) {
        return (appBatteryInfo.powerValue > 0 || appBatteryInfo.cpuFgTime > 0 || appBatteryInfo.cpuTime > 0
                || appBatteryInfo.wakeLockTime > 0 || appBatteryInfo.mobileRxBytes > 0
                || appBatteryInfo.mobileTxBytes > 0 || appBatteryInfo.wifiRxBytes > 0
                || appBatteryInfo.wifiTxBytes > 0 || usageTime > 0 || launchCount > 0);
    }

    private long millisToSeconds(long num) {
        return (long) Math.ceil((num / 1000f));
    }
}
