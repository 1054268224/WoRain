/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除最近及频繁使用规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.monitor.service.ActivityChangeMonitor;
import com.cydroid.softmanager.monitor.utils.UsageStatsHelper;
import com.cydroid.softmanager.utils.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.Intent;

public class RuleExcludeRecentAndFrequent implements IMemoryCleanRule {

    private static final int RECENT_APP_TIMEOUT = 3 * 60 * 60 * 1000;
    private static final String TAG = "RuleExcludeRecentAndFrequent";
    private static final int TASK_MAX = 50;
    private static final int REMAINS_APPS_COUNT = 3;

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        int freqReserveAppsCount = excludeFreqProcesses(manager, srcList, resList);
        excludeRecentProcesses(manager, srcList, resList, freqReserveAppsCount);
        return resList;
    }

    private int excludeFreqProcesses(IFilterRulesKit manager, List<ProcessMemoryEntity> srcList,
            ArrayList<ProcessMemoryEntity> resList) {
        UsageStatsHelper usageStatsHelper = new UsageStatsHelper(manager.getContext());
        ArrayList<String> frequentApps = usageStatsHelper.getFreqList();
        int freqReserveAppsCount = 0;
        for (int i = 0; i < frequentApps.size() && freqReserveAppsCount < REMAINS_APPS_COUNT / 2; i++) {
            String freqPkg = frequentApps.get(i);
            if (isInProcessList(freqPkg, srcList)) {
                copyProcessMemoryEntityByName(freqPkg, resList, srcList);
                freqReserveAppsCount++;
                Log.d(TAG, "reserve runnning Freq app ----->" + freqPkg);
                if (manager.getExcludeAppsRecorder() != null) {
                    manager.getExcludeAppsRecorder().record(freqPkg, "freq");
                }
            }
        }
        return freqReserveAppsCount;
    }

    private int excludeRecentProcesses(IFilterRulesKit manager, List<ProcessMemoryEntity> srcList,
            ArrayList<ProcessMemoryEntity> resList, int freqReserveAppsCount) {
        ActivityManager am = (ActivityManager) manager.getContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RecentTaskInfo> recentTasks = am.getRecentTasks(TASK_MAX,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        int recentReserveAppsCount = 0;
        if (recentTasks != null) {
            Map<String, String> toBackgroundTimeMap = ActivityChangeMonitor
                    .getAppToBackgroundTimes(manager.getContext());
            long currentTime = System.currentTimeMillis();
            for (int i = 0; i < recentTasks.size()
                    && recentReserveAppsCount < REMAINS_APPS_COUNT - freqReserveAppsCount; i++) {
                RecentTaskInfo task = recentTasks.get(i);
                Intent intent = new Intent(task.baseIntent);
                if (task.origActivity != null) {
                    intent.setComponent(task.origActivity);
                }
                String pkgName = intent.getComponent().getPackageName();
                if (pkgName != null && isInProcessList(pkgName, srcList)
                        && !isInProcessList(pkgName, resList)) {
                    if (isNotExpiredRecentApp(pkgName, toBackgroundTimeMap, currentTime)) {
                        copyProcessMemoryEntityByName(pkgName, resList, srcList);
                    }
                    recentReserveAppsCount++;
                    Log.d(TAG, "reserve runnning Recent app ----->" + pkgName);
                    if (manager.getExcludeAppsRecorder() != null) {
                        manager.getExcludeAppsRecorder().record(pkgName, "recent");
                    }
                }
            }
        }
        return recentReserveAppsCount;
    }

    private boolean isNotExpiredRecentApp(String pkgName, Map<String, String> toBackgroundTimeMap,
            long currentTime) {
        if (!toBackgroundTimeMap.containsKey(pkgName)) {
            Log.d(TAG, "pkg:" + pkgName + " no bg record");
            return true;
        }
        long timeDiff = 0;
        try {
            timeDiff = currentTime - Long.parseLong(toBackgroundTimeMap.get(pkgName));
        } catch (Exception e) {
            Log.e(TAG, "get bg time for pkg:" + pkgName + " error! " + e);
            return true;
        }
        Log.d(TAG, "pkg:" + pkgName + " to bg before " + timeDiff + "ms");
        return timeDiff < RECENT_APP_TIMEOUT;
    }

    private boolean isInProcessList(String pkgName, List<ProcessMemoryEntity> srcList) {
        for (ProcessMemoryEntity processEntitiy : srcList) {
            if (processEntitiy.mPackageName.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private void copyProcessMemoryEntityByName(String pkgName, ArrayList<ProcessMemoryEntity> resList,
            List<ProcessMemoryEntity> srcList) {
        for (ProcessMemoryEntity srcEntity : srcList) {
            if (srcEntity.mPackageName.equals(pkgName)) {
                resList.add(srcEntity);
            }
        }
    }
}
