/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-12-27
 */
package com.cydroid.softmanager.memoryclean;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Bundle;

import com.cydroid.softmanager.memoryclean.filterrules.IMemoryCleanRule;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCts;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentInputMethod;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLauncher;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLiveWallpaper;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentTop;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseAudioChannel;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseWidget;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeExplicity;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagCantSaveState;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagPersistent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeHightImportance;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeSystemWhitelisted;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.memoryclean.strategy.MemoryCleanWeapon;
import com.cydroid.softmanager.memoryclean.strategy.MemoryCleanWeaponsFactory;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private static final String TAG = "MemoryManager";
    private static MemoryManager sInstance;

    /**
     * 取值0，可用于系统管家小火箭清理。
     */
    public static final int CLEAN_TYPE_ROCKET = 0;

    /**
     * 取值1，可用于系统管家的锁屏清理。
     */
    public static final int CLEAN_TYPE_ASSAULT_RIFLE = 1;

    /**
     * 取值2，可用于类似SystemUI的清理。
     */
    public static final int CLEAN_TYPE_CANNON = 2;

    /**
     * 取值3，可用于垃圾清理。
     */
    public static final int CLEAN_TYPE_EMMAGEE = 3;

    /**
     * 取值4，可用高耗电提醒。
     */
    public static final int CLEAN_TYPE_PISTOL = 4;

    private Context mContext;
    private boolean mFirstTime = true;

    public static synchronized MemoryManager getInstance() {
        if (null == sInstance) {
            sInstance = new MemoryManager();
        }
        return sInstance;
    }

    private MemoryManager() {
    }

    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
    }

    private void initFirstTime(Context context) {
        Log.d(TAG, "initFirstTime");
        mFirstTime = false;
        mContext = context.getApplicationContext();
    }

    public synchronized void memoryClean(int cleanType, IMemoryCleanNativeCallback callback) {
        memoryClean(cleanType, callback, null);
    }

    public synchronized void memoryClean(int cleanType, IMemoryCleanNativeCallback callback, Bundle params) {
        Log.d(TAG, "memoryClean (type:" + cleanType + ")------>");
        MemoryCleanWeapon weapon = MemoryCleanWeaponsFactory.createWeapon(cleanType);
        weapon.init(mContext);
        weapon.setParams(params);
        List<ProcessMemoryEntity> runningPackages = weapon.loadRunningPackages();
        weapon.filterRunningPackages(runningPackages);
        cleanProcessMemoryEntitys(runningPackages, callback);
    }

    /*
    public synchronized void appMemoryClean(String packageName, IMemoryCleanNativeCallback callback) {
        List<ProcessMemoryEntity> cleanPackages = new ArrayList<ProcessMemoryEntity>();
        ProcessMemoryEntity entity = createProcessMemoryEntity(packageName);
        if (null != entity) {
            cleanPackages.add(entity);
        }
        cleanProcessMemoryEntitys(cleanPackages, callback);
    }
    
    private ProcessMemoryEntity createProcessMemoryEntity(String packageName) {
    
    }
    */

    public synchronized List<ProcessMemoryEntity> getRunningProcessMemoryEntitysIncludeWhitelist(int cleanType) {
        return getRunningProcessMemoryEntitysIncludeWhitelist(cleanType, null);
    }

    public synchronized List<ProcessMemoryEntity> getRunningProcessMemoryEntitysIncludeWhitelist(int cleanType, Bundle params) {
        MemoryCleanWeapon weapon = MemoryCleanWeaponsFactory.createWeapon(cleanType);
        weapon.init(mContext);
        weapon.setParams(params);
        List<ProcessMemoryEntity> runningPackages = weapon.loadRunningPackages();
        return weapon.filterRunningPackagesByRules(getExcludeRules(), runningPackages);
    }

    private List<IMemoryCleanRule> getExcludeRules() {
        List<IMemoryCleanRule> excludeAppRules = new ArrayList<IMemoryCleanRule>();
        excludeAppRules.add(new RuleExcludeFlagCantSaveState());
        excludeAppRules.add(new RuleExcludeFlagPersistent());
        excludeAppRules.add(new RuleExcludeHightImportance());
        excludeAppRules.add(new RuleExcludeCurrentUseAudioChannel());
        excludeAppRules.add(new RuleExcludeCurrentLauncher());
        excludeAppRules.add(new RuleExcludeCurrentTop());
        excludeAppRules.add(new RuleExcludeExplicity());
        excludeAppRules.add(new RuleExcludeCurrentLiveWallpaper());
        excludeAppRules.add(new RuleExcludeCts());
        excludeAppRules.add(new RuleExcludeCurrentInputMethod());
        excludeAppRules.add(new RuleExcludeSystemWhitelisted());
        excludeAppRules.add(new RuleExcludeCurrentUseWidget());
        return excludeAppRules;
    }

    public synchronized List<ProcessMemoryEntity> getRunningProcessMemoryEntitys(int cleanType) {
        return getRunningProcessMemoryEntitys(cleanType, null);
    }

    public synchronized List<ProcessMemoryEntity> getRunningProcessMemoryEntitys(int cleanType, Bundle params) {
        MemoryCleanWeapon weapon = MemoryCleanWeaponsFactory.createWeapon(cleanType);
        weapon.init(mContext);
        weapon.setParams(params);
        List<ProcessMemoryEntity> runningPackages = weapon.loadRunningPackages();
        return weapon.filterRunningPackages(runningPackages);
    }

    public synchronized void cleanProcessMemoryEntitys(List<ProcessMemoryEntity> entities,
            IMemoryCleanNativeCallback callback) {
        Log.d(TAG, "cleanProcessMemoryEntitys entities.isEmpty():" + entities.isEmpty());
        if (null != callback) {
            callback.onMemoryCleanReady(entities);
        }
        if (entities.isEmpty()
                && null != callback) {
            Log.d(TAG, "cleanProcessMemoryEntitys end");
            callback.onMemoryCleanFinished(0, 0L);
            return;
        }
        int totalProcesses = entities.size();
        long totalPss = 0L;
        Log.d(TAG, "cleanProcessMemoryEntitys size:" + entities.size());
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ProcessMemoryEntity entity : entities) {
            Log.d(TAG, "cleanProcessMemoryEntitys forceStopPackage entity.mPackageName:" + entity.mPackageName);
            forceStopPackage(am, entity.mPackageName);
            totalPss += entity.mPss;
        }
        Log.d(TAG, "cleanProcessMemoryEntitys end totalProcesses:" + totalProcesses + ", totalPss:" + totalPss);
        if (null != callback) {
            callback.onMemoryCleanFinished(totalProcesses, totalPss);
        }
    }

    private void forceStopPackage(ActivityManager am, String pkgName) {
        if (isTopActivityApp(pkgName) || isBaseActivityApp(pkgName)) {
            return;
        }
        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage",
                    String.class);
            method.invoke(am, pkgName);
            Log.d(TAG, "kill: --------->" + pkgName);
        } catch (Exception ex) {
            Log.e(TAG, "forceStopPackage exception:" + ex.toString());
        }
    }

    private boolean isTopActivityApp(String pkgName) {
        return pkgName.equals(getTopActivityPackageName(mContext));
    }

    private String getTopActivityPackageName(Context context) {
        String pkgName = "";
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).topActivity != null) {
                pkgName = runningTaskInfos.get(0).topActivity.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgName;
    }

    private boolean isBaseActivityApp(String pkgName) {
        return pkgName.equals(getBaseActivityPackageName(mContext));
    }

    private String getBaseActivityPackageName(Context context) {
        String pkgName = "";
        try {
            ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).baseActivity != null) {
                pkgName = runningTaskInfos.get(0).baseActivity.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgName;
    }
}
