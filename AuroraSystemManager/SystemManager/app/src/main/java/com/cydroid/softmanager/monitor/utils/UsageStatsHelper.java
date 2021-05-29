// Gionee <yangxinruo><2016-1-5> add for CR01618272 begin
package com.cydroid.softmanager.monitor.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.memoryclean.filterrules.ApplicationInfoManager;
import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;

import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

public class UsageStatsHelper {
    private final static String TAG = "UsageStatsHelper";

    private static final int MIN_FOREGROUND_TIME = 60 * 1000;
    private static final int MIN_LAUNCH_COUNT = 1;
    private static final int MAX_STORE_FREQ_APPS_COUNT = 5;
    public static final String PREF_FREQ_APPS = "main_frequent_apps";
    public static final String KEY_FREQ_APPS = "frequent_apps_list";

    Context mContext;

    public UsageStatsHelper(Context context) {
        mContext = context;
    }

    private boolean isThirdApplication(String pkgName) {
        ApplicationInfoManager applicationInfoManager = ApplicationInfoManager.getInstance(mContext);
        return !applicationInfoManager.isSystemApp(pkgName);
    }

    public ArrayList<String> getFreqList() {
        return getFreqList(Integer.MAX_VALUE);
    }

    public ArrayList<String> getFreqList(int maxSize) {
        return loadFrequentApp(maxSize);
    }

    private synchronized ArrayList<String> loadFrequentApp(int maxSize) {
        ArrayList<String> resList = new ArrayList<String>();
        MainProcessSettingsProviderHelper mainProcessSettingsProviderHelper = new MainProcessSettingsProviderHelper(
                mContext);
        String freqAppsStr = mainProcessSettingsProviderHelper.getString(KEY_FREQ_APPS, "");
        SimpleStringSplitter colonSplitter = new SimpleStringSplitter(':');
        colonSplitter.setString(freqAppsStr);
        while (resList.size() < maxSize && colonSplitter.hasNext()) {
            resList.add(colonSplitter.next());
        }
        return resList;
    }

    @SuppressLint("NewApi")
    public int dataUpdate(final long timeSpan) {

        new Thread() {
            @Override
            public void run() {
                UsageStatsManager usageStatsManager = (UsageStatsManager) mContext
                        .getSystemService(Context.USAGE_STATS_SERVICE);
                long endTime = System.currentTimeMillis();
                long beginTime = System.currentTimeMillis() - timeSpan;
                if (beginTime < 0)
                    beginTime = 0;
                List<UsageStats> statsList = usageStatsManager
                        .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
                if (statsList == null) {
                    return;
                }
                Log.d(TAG, "t1 " + beginTime + " t2 " + endTime + " count " + statsList.size());

                HashMap<String, UsageStats> usageMap = new HashMap<String, UsageStats>();
                final int statCount = statsList.size();
                for (int i = 0; i < statCount; i++) {
                    final android.app.usage.UsageStats pkgStats = statsList.get(i);
                    if (isThirdApplication(pkgStats.getPackageName())) {
                        usageMap.put(pkgStats.getPackageName(), pkgStats);
                    }
                }

                List<ResolveInfo> infos = HelperUtils.getLauncherShowActivity(mContext);
                List<UsageStats> usageStats = new ArrayList<UsageStats>();
                for (ResolveInfo info : infos) {
                    UsageStats aStats = usageMap.get(info.activityInfo.packageName);
                    if (aStats == null) {
                        continue;
                    }
                    Log.d(TAG, "freq packageName : " + aStats.getPackageName() + " launchCount : "
                            + aStats.mLaunchCount + " usageTime : " + aStats.getTotalTimeInForeground());
                    // 未使用过的应用或者使用小于1分钟的应用过滤掉
                    if (aStats.mLaunchCount < MIN_LAUNCH_COUNT
                            || aStats.getTotalTimeInForeground() < MIN_FOREGROUND_TIME) {
                        Log.d(TAG,
                                "freq packageName : " + aStats.getPackageName()
                                        + " removed because foreground time < " + MIN_FOREGROUND_TIME
                                        + " or launch count < " + MIN_LAUNCH_COUNT);
                        continue;
                    }
                    usageStats.add(aStats);
                }

                if (usageStats != null && usageStats.size() > 0) {
                    Collections.sort(usageStats, mComparator);
                    updateFreqList(usageStats);
                }
            }
        }.start();
        return 0;
    }

    private synchronized int updateFreqList(List<UsageStats> usageStats) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String dateStr = formatter.format(curDate);
        SmartCleanInfoWriter.writeToDataFile("\n<---Refresh Frequent App " + dateStr + "--->");
        ArrayList<String> frequentApp = new ArrayList<String>();
        for (UsageStats stats : usageStats) {
            Log.d(TAG, "Frequent packageName : " + stats.getPackageName() + " launchCount : "
                    + stats.mLaunchCount + " usageTime : " + stats.getTotalTimeInForeground());
            frequentApp.add(stats.getPackageName());
            SmartCleanInfoWriter.writeToDataFile("Name:" + stats.getPackageName() + " Launch : "
                    + stats.mLaunchCount + " Time : " + stats.getTotalTimeInForeground());
            if (frequentApp.size() >= MAX_STORE_FREQ_APPS_COUNT) {
                break;
            }
        }
        storeFrequentApp(frequentApp);
        return frequentApp.size();
    }

    private synchronized void storeFrequentApp(ArrayList<String> frequentApp) {
        String freqAppsStr = TextUtils.join(":", frequentApp);
        Log.d(TAG, "store freq apps :" + freqAppsStr);
        MainProcessSettingsProviderHelper mainProcessSettingsProviderHelper = new MainProcessSettingsProviderHelper(
                mContext);
        mainProcessSettingsProviderHelper.putString(KEY_FREQ_APPS, freqAppsStr);
        recordDataLog(freqAppsStr);
    }

    private void recordDataLog(String data) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String dateStr = formatter.format(curDate);
        SmartCleanInfoWriter.writeToDataFile("\n<---Get Frequent App at ScreenOff " + dateStr + "--->");
        SmartCleanInfoWriter.writeToDataFile(data);
    }

    private final Comparator<UsageStats> mComparator = new Comparator<UsageStats>() {
        @Override
        public int compare(UsageStats aStats, UsageStats bStats) {
            int result = 0;
            if (aStats != null && bStats != null) {
                if ((aStats.mLaunchCount > bStats.mLaunchCount)
                        || ((aStats.mLaunchCount == bStats.mLaunchCount)
                                && (aStats.getTotalTimeInForeground() > bStats.getTotalTimeInForeground()))) {
                    result = -1;
                } else if ((aStats.mLaunchCount < bStats.mLaunchCount)
                        || ((aStats.mLaunchCount == bStats.mLaunchCount)
                                && (aStats.getTotalTimeInForeground() < bStats.getTotalTimeInForeground()))) {
                    result = 1;
                } else {
                    result = 0;
                }
            } else if (aStats != null && bStats == null) {
                result = -1;
            } else if (aStats == null && bStats != null) {
                result = 1;
            }
            return result;
        }
    };
}
// Gionee <yangxinruo><2016-1-5> add for CR01618272 end