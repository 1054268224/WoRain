package com.cydroid.softmanager.memoryclean.strategy;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;

import androidx.annotation.WorkerThread;

import com.cydroid.softmanager.memoryclean.filterrules.IFilterRulesKit;
import com.cydroid.softmanager.memoryclean.filterrules.IMemoryCleanRule;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.powersaver.analysis.collector.MemoryCleanExcludeAppsRecorder;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by houjie on 1/6/17.
 */
public abstract class Weapon implements MemoryCleanWeapon, IFilterRulesKit {
    private static final String TAG = "Weapon";
    private static final boolean DEBUG = true;

    protected Context mContext;
    protected Bundle mParams;

    @Override
    public void init(Context context) {
        mContext = context;
    }

    @Override
    public List<ProcessMemoryEntity> loadRunningPackages() {
        Log.d(TAG, "loadRunningPackages begin");
        Map<String, ProcessMemoryEntity> processMemoryEntitys = new HashMap<String, ProcessMemoryEntity>();
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        for (int i = 0; i < processes.size(); ++i) {
            ActivityManager.RunningAppProcessInfo process = processes.get(i);
            loadProcessMemoryEntity(processMemoryEntitys, process);
        }
        List<ProcessMemoryEntity> result = new ArrayList<ProcessMemoryEntity>();
        result.addAll(processMemoryEntitys.values());
        Log.d(TAG, "loadRunningPackages end");
        return result;
    }

    private Map<String, ProcessMemoryEntity> loadProcessMemoryEntity(
            Map<String, ProcessMemoryEntity> processMemoryEntitys,
            ActivityManager.RunningAppProcessInfo process) {
        Log.d(TAG, "loadProcessMemoryEntity process:" + process.processName);
        for (String pkgName : process.pkgList) {
            ProcessMemoryEntity entity = processMemoryEntitys.get(pkgName);
            if (null == entity) {
                entity = createProcessMemoryEntity(pkgName, process);
                processMemoryEntitys.put(pkgName, entity);
            }
        }
        return processMemoryEntitys;
    }

    private ProcessMemoryEntity createProcessMemoryEntity(String pkgName,
            ActivityManager.RunningAppProcessInfo process) {
        ProcessMemoryEntity entity = new ProcessMemoryEntity();
        entity.mPackageName = pkgName;
        // entity.isInUserWhiteAppList = mUserWhiteApps.contains(pkgName);
        entity.mProcess = process;
        return entity;
    }

    public List<ProcessMemoryEntity> filterRunningPackagesByRules(List<IMemoryCleanRule> rules,
            List<ProcessMemoryEntity> entities) {
        for (IMemoryCleanRule rule : rules) {
            List<ProcessMemoryEntity> fulfilledList = rule.getFulfilledProcesses(this, entities);
            if (DEBUG) {
                Log.d(TAG, "following " + fulfilledList.size() + " processes will NOT be cleaned by rule :"
                        + rule.getClass().getSimpleName());
                for (ProcessMemoryEntity possesInfo : fulfilledList) {
                    Log.d(TAG, "----------> " + possesInfo.mPackageName);
                }
            }
            entities.removeAll(fulfilledList);
        }
        Log.d(TAG, "getPssByRunningAppProcessInfos begin, entities size = " + entities.size());
        List<Long> appsPss = getPssByRunningAppProcessInfos(entities);
        Log.d(TAG, "getPssByRunningAppProcessInfos end, appsPss size = " + appsPss.size());
        for (int i = 0; i < entities.size(); ++i) {
            long pss = appsPss.get(i);
            ProcessMemoryEntity entity = entities.get(i);
            entity.mPss += pss;
        }
        return entities;
    }

    private List<Long> getPssByRunningAppProcessInfos(
            List<ProcessMemoryEntity> entities) {
        int[] pids = new int[entities.size()];
        for (int i = 0; i < entities.size(); ++i) {
            pids[i] = entities.get(i).mProcess.pid;
        }
        return getProcessPss(pids);
    }

    // xionghg 2017-08-26 modify for android o begin
    @WorkerThread
    private List<Long> getProcessPss(int[] pids) {
        List<Long> result = new ArrayList<>();

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        Debug.MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(pids);
        for (int i = 0; i < memoryInfos.length; i++) {
            result.add((long) memoryInfos[i].getTotalPss());
            Log.e(TAG, "getProcessPss: pid=" + pids[i] + ", pss=" + result.get(i));
        }

        return result;
    }
    // xionghg 2017-08-26 modify for android o end

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void setParams(Bundle params) {
        mParams = params;
    }

    @Override
    public MemoryCleanExcludeAppsRecorder getExcludeAppsRecorder() {
        return null;
    }
}
