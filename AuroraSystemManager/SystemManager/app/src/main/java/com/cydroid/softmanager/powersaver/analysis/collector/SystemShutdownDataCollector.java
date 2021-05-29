/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 关机事件采集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cydroid.softmanager.utils.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class SystemShutdownDataCollector implements IAnalysisDataCollector {
    private static final String TAG = "BatteryChargeDataCollector";
    private final Context mContext;
    private final ArrayList<Map<String, Object>> mShutdownEvents;
    private INewDataListener mDataListener;
    private ShutdownReceiver mShutdownReceiver;

    public SystemShutdownDataCollector(Context context) {
        mContext = context;
        mShutdownEvents = new ArrayList<Map<String, Object>>();
    }

    @Override
    public void init() {
        mShutdownReceiver = new ShutdownReceiver(mContext);
    }

    @Override
    public void deinit() {
        if (mShutdownReceiver != null) {
            mShutdownReceiver.remove();
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
        ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (!mShutdownEvents.isEmpty()) {
            result.addAll(mShutdownEvents);
        }
        mShutdownEvents.clear();
        return result;
    }

    public class ShutdownReceiver extends BroadcastReceiver {
        private final Context mContext;

        public ShutdownReceiver(Context context) {
            mContext = context;
            IntentFilter eventFilter = new IntentFilter();
            eventFilter.addAction(Intent.ACTION_SHUTDOWN);
            mContext.registerReceiver(this, eventFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr == null || actionStr.isEmpty()) {
                return;
            }
            if (actionStr.equals(Intent.ACTION_SHUTDOWN)) {
                onPowerOff(System.currentTimeMillis());
            }
        }

        private void onPowerOff(long timestamp) {
            long powerDuration = SystemClock.elapsedRealtime();
            if (isInvalidEvent(powerDuration)) {
                return;
            }
            HashMap<String, Object> powerMap = new HashMap<String, Object>();
            powerMap.put("time", millisToSeconds(powerDuration));
            int resultSize = addToDataSet(powerMap);
            Log.i(TAG, "resultDataSet.put :" + powerMap + ", currentRecordData.size " + resultSize);
            if (mDataListener != null) {
                mDataListener.onNewData(SystemShutdownDataCollector.this);
            }
        }

        private boolean isInvalidEvent(long powerDuration) {
            return powerDuration <= 0;
        }

        private long millisToSeconds(long num) {
            return (long) Math.ceil((num / 1000f));
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }

    public int addToDataSet(HashMap<String, Object> event) {
        mShutdownEvents.add(event);
        return mShutdownEvents.size();
    }
}
