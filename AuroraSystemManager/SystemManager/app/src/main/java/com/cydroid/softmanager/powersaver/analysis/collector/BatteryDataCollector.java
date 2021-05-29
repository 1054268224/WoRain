/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: BatteryStats数据采集器基类
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.cydroid.softmanager.powersaver.analysis.utils.DatePeriodUtils;
import com.cydroid.softmanager.utils.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryStats;
import android.os.Handler;

public abstract class BatteryDataCollector implements IAnalysisDataCollector {
    private static final String TAG = "BatteryDataCollector";

    public static final String ACTION_TIME_PERIOD_CHANGE = "com.cydroid.softmanager.action.4_PERIOD_DATAPROCESS";
    public static final String ACTION_SYSTEM_RESET_BATTERY_INFO = "com.gionee.intent.action.SYSTEM_FLUSH_BATTERY_INFO";

    private static final String SYSTEM_BATTERY_BIN_PATH = "/data/system";
    private static final String BACKUP_SYSTEM_BATTERY_BIN_PATH = "/data/system/batterystatsbackup";

    protected static final int RECORD_MODE_TIME_PERIOD_CHANGED = 2;
    protected static final int RECORD_MODE_BATTERY_STATS_RESET = 1;

    private static final String PREF_NAME_ANALYSIS_DATA = "remote_analysis_data_preference";
    private static final String PREF_KEY_LAST_RECORD_BATTERY_DATA_MODE = "last_flush_battery_data_mode";

    protected Context mContext;
    protected INewDataListener mDataListener;

    private BatteryDataBroadcastReceiver mBatteryDataBroadcastReceiver;

    BatteryDataCollector(Context context) {
        mContext = context;
    }

    @Override
    public void init() {
        mBatteryDataBroadcastReceiver = new BatteryDataBroadcastReceiver(mContext);
    }

    @Override
    public void deinit() {
        if (mBatteryDataBroadcastReceiver != null) {
            mBatteryDataBroadcastReceiver.remove();
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
    public List<Map<String, Object>> getAndFlushNewDataSet() {
        String workingPeriodLabel = getCurrentPeriodLabel();
        return getAndFlushNewDataSet(workingPeriodLabel);
    }

    private String getCurrentPeriodLabel() {
        long currentMillis = System.currentTimeMillis();
        String workingPeriodLabel = DatePeriodUtils.getCurrentPeriodDateInFormatString(currentMillis);
        return workingPeriodLabel;
    }

    private String getLastPeriodLabel() {
        long currentMillis = System.currentTimeMillis();
        String lastPeriodLabel = DatePeriodUtils.getLastPeriodDateInFormatString(currentMillis);
        return lastPeriodLabel;
    }

    private class BatteryDataBroadcastReceiver extends BroadcastReceiver {
        private final Context mContext;

        BatteryDataBroadcastReceiver(Context context) {
            mContext = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_TIME_PERIOD_CHANGE);
            filter.addAction(ACTION_SYSTEM_RESET_BATTERY_INFO);
            mContext.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr == null || actionStr.isEmpty()) {
                return;
            }
            switch (actionStr) {
                case ACTION_TIME_PERIOD_CHANGE:
                    BatteryStatsHelper normalBatteryStats = getBatteryStats(SYSTEM_BATTERY_BIN_PATH);
                    if (normalBatteryStats == null) {
                        Log.d(TAG, "getNormalBatteryStats failed !!! ");
                        return;
                    }
                    String periodLabel = getLastPeriodLabel();
                    onTimePeriodChange(normalBatteryStats, periodLabel);
                    saveBatteryInfoRecordMode(RECORD_MODE_TIME_PERIOD_CHANGED);
                    if (mDataListener != null) {
                        mDataListener.onNewData(BatteryDataCollector.this);
                    }
                    break;
                case ACTION_SYSTEM_RESET_BATTERY_INFO:
                    BatteryStatsHelper backupBatteryStats = getBatteryStats(BACKUP_SYSTEM_BATTERY_BIN_PATH);
                    if (backupBatteryStats == null) {
                        Log.d(TAG, "getBackupBatteryStats failed !!! ");
                        return;
                    }
                    onBatteryStatsReset(backupBatteryStats);
                    saveBatteryInfoRecordMode(RECORD_MODE_BATTERY_STATS_RESET);
                    break;
                default:
                    Log.d(TAG, "unknow action");
                    break;
            }
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }

    private void saveBatteryInfoRecordMode(int mode) {
        Editor preferenceEditor = mContext.getSharedPreferences(PREF_NAME_ANALYSIS_DATA, Context.MODE_PRIVATE)
                .edit();
        preferenceEditor.putInt(PREF_KEY_LAST_RECORD_BATTERY_DATA_MODE, mode);
        preferenceEditor.commit();
        Log.d(TAG, "saveBatteryInfoRecordMode mode = " + mode);
    }

    protected int getBatteryInfoRecordMode() {
        SharedPreferences preference = mContext.getSharedPreferences(PREF_NAME_ANALYSIS_DATA,
                Context.MODE_PRIVATE);
        return preference.getInt(PREF_KEY_LAST_RECORD_BATTERY_DATA_MODE, 0);
    }

    private BatteryStatsHelper getBatteryStats(String filePath) {
        try {
            BatteryStatsHelper batteryHelper = new BatteryStatsHelper(mContext, true);
            //guoxt ,modify for Ptest begin
            BatteryStats stats = new BatteryStatsImpl(new File(filePath), new Handler(), null,null,null);
            //BatteryStats stats = new BatteryStatsImpl(new File(filePath), new Handler(), null,null);
            //guoxt ,modify for Ptest end
            batteryHelper.create(stats);
            ((BatteryStatsImpl) stats).readLocked();
            batteryHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, -1);
            return batteryHelper;
        } catch (Exception e) {
            Log.d(TAG, "get batteryStats error!:" + e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends Serializable> T getDataFromFile(String path) {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(path));
            Serializable result = (Serializable) in.readObject();
            return (T) result;
        } catch (Exception e) {
            Log.e(TAG, "getDataFromFile(path:" + path + ")-------->", e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "getDataFromFile close file failed!!-------->", e);
                }
            }
        }
    }

    protected void saveDataToFile(String path, Serializable data) {
        ObjectOutputStream out = null;
        try {
            File objFile = new File(path);
            if (objFile.exists()) {
                boolean delRes = objFile.delete();
                Log.d(TAG, "reset file " + path + ", del res = " + delRes);
            }
            out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(data);
            out.flush();
            Log.i(TAG, "saveDataToFile size() --------> class = " + data.getClass().getSimpleName()
                    + " data = " + data.toString());
        } catch (Exception e) {
            Log.e(TAG, "saveDataToFile-------->", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "saveDataToFile close file failed!!-------->", e);
                }
            }
        }
    }

    public abstract List<Map<String, Object>> getAndFlushNewDataSet(String workingPeriodLabel);

    public abstract void onTimePeriodChange(BatteryStatsHelper normalBatteryStats, String periodLabel);

    public abstract void onBatteryStatsReset(BatteryStatsHelper backupBatteryStats);
}
