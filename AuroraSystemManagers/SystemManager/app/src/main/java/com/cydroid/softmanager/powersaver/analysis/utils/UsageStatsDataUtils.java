/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: UsageStats数据辅助类
 *
 * Revised Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.utils.Log;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

public class UsageStatsDataUtils {
    private static final String TAG = "UsageStatsDataUtils";

    public static class SimpleUsageData {
        public long foregroundTime;
        public int launchCount;

        public SimpleUsageData(long foregroundTime, int launchCount) {
            super();
            this.foregroundTime = foregroundTime;
            this.launchCount = launchCount;
        }
    }

    public static ConcurrentHashMap<String, SimpleUsageData> getAllPackageUsageStats(Context context,
            String dayTime) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        ConcurrentHashMap<String, SimpleUsageData> resStatsMap = new ConcurrentHashMap<String, SimpleUsageData>();
        try {
            // Gionee <yangxinruo> <2015-10-29> modify for CR01577196 begin
            SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
            Date date = sdf.parse(dayTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            long beginTime = calendar.getTimeInMillis();
            Log.i(TAG, "getAllPackageUsageStats beginTime :" + sdfFull.format(calendar.getTime()));
            int periodDays = DatePeriodUtils.PERIOD_DURATION / 24;
            int timespan = (periodDays - 1) * 24 * 3600 * 1000 + 1;
            if (timespan < 1) {
                timespan = 1;
            }
            Log.d(TAG, "query timespan = " + timespan);
            calendar.add(Calendar.MILLISECOND, timespan);

            long endTime = calendar.getTimeInMillis();
            Log.i(TAG, "getAllPackageUsageStats endTime :" + sdfFull.format(calendar.getTime()));

            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    beginTime, endTime);
            // Gionee <yangxinruo> <2015-10-29> modify for CR01577196 end
            if (stats == null) {
                Log.i(TAG, "getAllPackageUsageStats stats == null return");
                return resStatsMap;
            }

            Log.i(TAG, "getAllPackageUsageStats statCount : " + stats.size());
            for (android.app.usage.UsageStats pkgStats : stats) {
                long pkgForegroundTime = pkgStats.getTotalTimeInForeground();
                int periodDaysMillis = periodDays * 24 * 3600 * 1000;
                if (pkgForegroundTime > periodDaysMillis) {
                    Log.d(TAG,
                            "may get a wrong UsageStats data,request period:" + periodDaysMillis
                                    + " response  fore:" + pkgForegroundTime + " begin:"
                                    + pkgStats.getFirstTimeStamp() + " end:" + pkgStats.getLastTimeStamp());
                    SmartCleanInfoWriter.writeToDataFile("WRONG USAGESTATS:pkgname:"
                            + pkgStats.getPackageName() + " foregroundTime:" + pkgForegroundTime
                            + "request begin:" + beginTime + " end:" + endTime + " response begin:"
                            + pkgStats.getFirstTimeStamp() + " end:" + pkgStats.getLastTimeStamp());
                    continue;
                }
                resStatsMap.put(pkgStats.getPackageName(),
                        new SimpleUsageData(pkgForegroundTime, pkgStats.mLaunchCount));
                Log.i(TAG, "getAllPackageUsageStats: " + dayTime + " ----> " + pkgStats.getPackageName()
                        + " ---> " + pkgForegroundTime + "," + pkgStats.mLaunchCount);
            }
        } catch (Exception ex) {
            Log.e(TAG, "getAllPackageUsageStats", ex);
        }
        return resStatsMap;
    }

    public static long getTimeInForeground(ConcurrentHashMap<String, SimpleUsageData> statsMap,
            String pkgName) {
        long res = 0;
        if (statsMap.size() <= 0)
            return res;
        if (statsMap.get(pkgName) == null)
            return res;
        res = statsMap.get(pkgName).foregroundTime;
        Log.i(TAG, "getTimeInForeground: " + pkgName + " , time:" + res);
        return res;
    }

    public static int getLaunchCount(ConcurrentHashMap<String, SimpleUsageData> statsMap, String pkgName) {
        int res = 0;
        if (statsMap.size() <= 0)
            return res;
        if (statsMap.get(pkgName) == null)
            return res;
        res = statsMap.get(pkgName).launchCount;
        Log.i(TAG, "getLaunchCount: " + pkgName + " , launch:" + res);
        return res;
    }
}
