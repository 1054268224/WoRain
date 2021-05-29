/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 电量变化事件采集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.cydroid.softmanager.powersaver.analysis.utils.BatteryDataUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.BatteryManager;

public class PowerLevelDataCollector implements IAnalysisDataCollector {
    private static final String TAG = "PowerLevelDataCollector";

    private static final String PREF_POWER_LEVEL = "analysis_power_level";
    private static final String PREF_KEY_LAST_POWER_LEVEL = "last_power_level";

    private final Context mContext;
    private final ArrayList<Map<String, Object>> mPowerLevelEvents;
    private INewDataListener mDataListener;
    private PowerLevelReceiver mPowerLevelReceiver;

    public PowerLevelDataCollector(Context context) {
        mContext = context;
        mPowerLevelEvents = new ArrayList<Map<String, Object>>();
    }

    @Override
    public void init() {
        mPowerLevelReceiver = new PowerLevelReceiver(mContext);
    }

    @Override
    public void deinit() {
        if (mPowerLevelReceiver != null) {
            mPowerLevelReceiver.remove();
        }
        if (mDataListener != null) {
            mDataListener = null;
        }
    }

    @Override
    public void setNewDataListener(INewDataListener newDataListener) {
        mDataListener = newDataListener;
    }

    @Override
    public synchronized List<Map<String, Object>> getAndFlushNewDataSet() {
        ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (!mPowerLevelEvents.isEmpty()) {
            result.addAll(mPowerLevelEvents);
        }
        mPowerLevelEvents.clear();
        return result;
    }

    public class PowerLevelReceiver extends BroadcastReceiver {
        private final Context mContext;

        public PowerLevelReceiver(Context context) {
            mContext = context;
            IntentFilter eventFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            mContext.registerReceiver(this, eventFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr == null || actionStr.isEmpty()
                    || !actionStr.equals(Intent.ACTION_BATTERY_CHANGED)) {
                return;
            }
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level < 0) {
                Log.d(TAG, "invalid power value!");
                return;
            }
            int lastPowerLevel = getLastPowerLevel();
            if (level == lastPowerLevel) {
                Log.d(TAG, "power value not change, skip");
                return;
            }
            saveLastPowerLevel(level);
            new RunningProcessesLogThread(context, level).start();
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }

    private class RunningProcessesLogThread extends Thread {
        private final Context mContext;
        private final int mPowerLevel;

        public RunningProcessesLogThread(Context context, int level) {
            super();
            mContext = context;
            mPowerLevel = level;
        }

        @Override
        public void run() {
            long timestamp = System.currentTimeMillis();
            recordRunningProcesses(mContext, timestamp, mPowerLevel);
        }

        private void recordRunningProcesses(Context context, long timestamp, int powerValue) {
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> processesInfoList = am.getRunningAppProcesses();
            Log.d(TAG, "get running processes list size = " + processesInfoList.size());
            List<Map<String, String>> processesArray = new ArrayList<Map<String, String>>();
            HashSet<String> processesName = new HashSet<String>();
            PackageManager pm = mContext.getPackageManager();
            for (RunningAppProcessInfo processInfo : processesInfoList) {
                if (processInfo.pkgList == null || processInfo.pkgList.length == 0) {
                    continue;
                }
                String pkgName = processInfo.pkgList[0];
                if (processesName.contains(pkgName)) {
                    continue;
                }
                processesName.add(pkgName);
                String pkgUid = String.valueOf(processInfo.uid);
                String pkgVersion = HelperUtils.getPackageVersion(pm, pkgName);
                if (pkgVersion.isEmpty()) {
                    continue;
                }
                Map<String, String> processesMap = new HashMap<String, String>();
                processesMap.put("pn", pkgName);
                processesMap.put("pv", pkgVersion);
                processesArray.add(processesMap);
            }
            String processesArrayJsonString = BatteryDataUtils.mapArrayInfoToJsonString(processesArray);
            HashMap<String, Object> eventMap = new HashMap<String, Object>();
            eventMap.put("timestamp", String.valueOf(timestamp));
            eventMap.put("powerValue", powerValue);
            List<String> processesArrayJsonStrings = new ArrayList<String>();
            try {
                processesArrayJsonStrings = BatteryDataUtils.jsonStringToArrayInfo(processesArrayJsonString);
            } catch (JSONException e) {
                Log.d(TAG, "parse json error " + e);
            }
            for (int i = 0; i < processesArrayJsonStrings.size(); i++) {
                eventMap.put("rP" + i, processesArrayJsonStrings.get(i));
            }
            int resultSize = addToDataSet(eventMap);
            Log.i(TAG, "resultDataSet.put :" + eventMap + ", currentRecordData.size " + resultSize);
            if (mDataListener != null) {
                mDataListener.onNewData(PowerLevelDataCollector.this);
            }
        }
    }

    public int getLastPowerLevel() {
        SharedPreferences powerStatePreference = mContext.getSharedPreferences(PREF_POWER_LEVEL,
                Context.MODE_PRIVATE);
        return powerStatePreference.getInt(PREF_KEY_LAST_POWER_LEVEL, -1);
    }

    public void saveLastPowerLevel(int level) {
        Editor powerStatePreference = mContext.getSharedPreferences(PREF_POWER_LEVEL, Context.MODE_PRIVATE)
                .edit();
        powerStatePreference.putInt(PREF_KEY_LAST_POWER_LEVEL, level);
        powerStatePreference.commit();
    }

    public synchronized int addToDataSet(HashMap<String, Object> event) {
        mPowerLevelEvents.add(event);
        return mPowerLevelEvents.size();
    }
}
