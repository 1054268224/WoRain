package com.cydroid.softmanager.powersaver.notification;

import android.content.Context;
import android.os.BatteryStats;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.loader.content.AsyncTaskLoader;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.powersaver.analysis.AppBatteryInfo;
import com.cydroid.softmanager.powersaver.notification.strategy.SimplePowerConsumeAppMonitorFactory;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BackgroundPowerConsumeLoader extends AsyncTaskLoader<List<PowerConsumeAppData>> {

    private static final String TAG = "BackgroundPowerConsumeLoader";
    private final Context mContext;
    private List<PowerConsumeAppData> mData;

    public BackgroundPowerConsumeLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public List<PowerConsumeAppData> loadInBackground() {
        Log.i(TAG, "begin load");
        MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(mContext);
        List<ProcessMemoryEntity> runningProcessList = memoryManager
                .getRunningProcessMemoryEntitys(MemoryManager.CLEAN_TYPE_PISTOL);
        ArrayList<PowerConsumeAppData> runningPowerConsumeApps = new ArrayList<PowerConsumeAppData>();
        for (ProcessMemoryEntity runningProcess : runningProcessList) {
            runningPowerConsumeApps.add(new PowerConsumeAppData(runningProcess, 0, false));
        }
        Log.d(TAG, "getRunning runningPowerConsumeApps size=" + runningPowerConsumeApps.size());
        attachWhitelistInfo(runningPowerConsumeApps);
        attachPowerUsageInfo(runningPowerConsumeApps);
        attachEncryptionsAppsInfo(runningPowerConsumeApps);
        attachPowerConsumeIgnoredInfo(runningPowerConsumeApps);
        attachPowerConsumeAlertType(runningPowerConsumeApps);
        return runningPowerConsumeApps;
    }

    private void attachWhitelistInfo(ArrayList<PowerConsumeAppData> runningPowerConsumeApps) {
        WhiteListManager whiteListManager = WhiteListManager.getInstance();
        whiteListManager.init(mContext);
        for (PowerConsumeAppData runningPowerConsumeApp : runningPowerConsumeApps) {
            if (whiteListManager.isInUserWhiteApps(runningPowerConsumeApp.mPackageName)) {
                Log.d(TAG, "pkg:" + runningPowerConsumeApp.mPackageName + " in user whitelist");
                runningPowerConsumeApp.mIsInUserWhiteAppList = true;
            }
        }
    }

    private void attachEncryptionsAppsInfo(ArrayList<PowerConsumeAppData> runningPowerConsumeApps) {
        ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(mContext.getContentResolver());
        for (PowerConsumeAppData runningPowerConsumeApp : runningPowerConsumeApps) {
            if (encryptionsApps.contains(runningPowerConsumeApp.mPackageName)) {
                runningPowerConsumeApp.mIsPrivateApp = true;
            }
        }
    }

    private void attachPowerUsageInfo(List<PowerConsumeAppData> runningPowerConsumeApps) {
        SparseArray<Double> powerConsumeByUid = getPowerUsageMap();
        for (PowerConsumeAppData runningPowerConsumeApp : runningPowerConsumeApps) {
            Log.d(TAG, "pkg:" + runningPowerConsumeApp.mPackageName + " power value = "
                    + powerConsumeByUid.get(runningPowerConsumeApp.mProcess.uid, 0d));
            runningPowerConsumeApp.mPowerValue = powerConsumeByUid.get(runningPowerConsumeApp.mProcess.uid,
                    0d);
        }
    }

    private void attachPowerConsumeIgnoredInfo(ArrayList<PowerConsumeAppData> runningPowerConsumeApps) {
        PowerConsumeAppManager powerConsumeAppManager = PowerConsumeAppManager.getInstance(mContext);
        for (PowerConsumeAppData runningPowerConsumeApp : runningPowerConsumeApps) {
            Log.d(TAG, "pkg:" + runningPowerConsumeApp.mPackageName + " in ignored list");
            runningPowerConsumeApp.mIsInIgnoredAppList = powerConsumeAppManager
                    .isIgnoredApp(runningPowerConsumeApp.mPackageName);
        }

    }

    private void attachPowerConsumeAlertType(ArrayList<PowerConsumeAppData> runningPowerConsumeApps) {
        HashMap<String, Map<String, String>> alertMap = new HashMap<String, Map<String, String>>();
        PowerConsumeAppManager powerConsumeAppManager = PowerConsumeAppManager.getInstance(mContext);
        for (String monitorName : SimplePowerConsumeAppMonitorFactory.MONITOR_NAME_LIST) {
            Map<String, String> monitorMap = powerConsumeAppManager.getPowerConsumeAppData(monitorName);
            alertMap.put(monitorName, monitorMap);
        }
        for (PowerConsumeAppData runningPowerConsumeApp : runningPowerConsumeApps) {
            for (Entry<String, Map<String, String>> entry : alertMap.entrySet()) {
                if (entry.getValue().containsKey(runningPowerConsumeApp.mPackageName)) {
                    Log.d(TAG, "pkg:" + runningPowerConsumeApp.mPackageName + " is alert by monitor "
                            + entry.getKey());
                    runningPowerConsumeApp.mAlertTypes.add(entry.getKey());
                }
            }
        }
    }

    private SparseArray<Double> getPowerUsageMap() {
        SparseArray<Double> powerConsumeByUid = new SparseArray<Double>();
        BatteryStatsHelper mBatteryHelper = new BatteryStatsHelper(mContext, true);
        mBatteryHelper.create((Bundle) null);
        mBatteryHelper.clearStats();
        mBatteryHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, -1);
        List<BatterySipper> sipperList = mBatteryHelper.getUsageList();
        if (sipperList == null) {
            return powerConsumeByUid;
        }
        for (BatterySipper sipper : sipperList) {
            AppBatteryInfo appinfo = new AppBatteryInfo(sipper);
            if (appinfo.drainType != BatterySipper.DrainType.APP || appinfo.userId <= 0) {
                continue;
            }
            Double savedValue = powerConsumeByUid.get(appinfo.userId, 0d);
            if (appinfo.powerValue > savedValue) {
                powerConsumeByUid.put(appinfo.userId, appinfo.powerValue);
            }
        }
        return powerConsumeByUid;
    }

    @Override
    public void deliverResult(List<PowerConsumeAppData> data) {
        if (isReset()) {
            if (data != null) {
                onReleaseResources(data);
            }
        }
        List<PowerConsumeAppData> oldApps = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    private void onReleaseResources(List<PowerConsumeAppData> data) {
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<PowerConsumeAppData> data) {
        super.onCanceled(data);
        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
    }
}
