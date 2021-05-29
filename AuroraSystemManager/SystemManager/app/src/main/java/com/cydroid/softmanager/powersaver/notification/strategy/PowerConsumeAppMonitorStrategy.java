/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用检测调度
 *
 * Date: 2017-02-10
 */
package com.cydroid.softmanager.powersaver.notification.strategy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.cydroid.softmanager.powersaver.utils.BatteryStateInfo;
import com.cydroid.softmanager.utils.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class PowerConsumeAppMonitorStrategy implements PowerConsumeAppMonitor.IMonitorStateChangedCallback {
    private static final String TAG = "PowerConsumeAppMonitorStrategy";

    private final HashMap<String, PowerConsumeAppMonitor> mMonitorsMap = new HashMap<String, PowerConsumeAppMonitor>();
    private final HashMap<String, Long> mMonitorNextUpdateTimesMap = new HashMap<String, Long>();
    private IChangeCallback mCallback;
    private final Context mContext;
    private long mLastUpdateElapsedTime = -1L;
    private boolean mIsWorking = true;

    public PowerConsumeAppMonitorStrategy(Context context) {
        mContext = context;
        mIsWorking = !BatteryStateInfo.isChargingNow(mContext);
        new BatteryChargeReceiver().set();
    }

    @Override
    public void onDeactivate(PowerConsumeAppMonitor monitor) {
        for (Entry<String, PowerConsumeAppMonitor> entry : mMonitorsMap.entrySet()) {
            if (entry.getValue() == monitor) {
                mMonitorNextUpdateTimesMap.remove(entry.getKey());
//                HashMap<String, Double> overThresholdResult = new HashMap<String, Double>();
//                if (mCallback != null) {
//                    mCallback.onMonitorDataUpdate(entry.getKey(), overThresholdResult);
//                }
                break;
            }
        }
    }

    @Override
    public void onActivate(PowerConsumeAppMonitor monitor) {
        for (Entry<String, PowerConsumeAppMonitor> entry : mMonitorsMap.entrySet()) {
            if (entry.getValue() == monitor) {
                mMonitorNextUpdateTimesMap.put(entry.getKey(), monitor.getUpdateInterval());
                monitor.init();
                break;
            }
        }
        if (mCallback != null) {
            Log.d(TAG, "CALLBACK:" + monitor.getClass().getName() + " active ,call onChange");
            mCallback.onChange();
        }
    }

    public void addPowerConsumeAppMonitors(String[] monitorList) {
        for (String monitorName : monitorList) {
            PowerConsumeAppMonitor monitor = mMonitorsMap.get(monitorName);
            if (monitor != null) {
                Log.d(TAG, "monitor " + monitorName + "already exist");
                continue;
            }
            monitor = SimplePowerConsumeAppMonitorFactory.createMonitorInstanceByName(mContext, monitorName);
            if (monitor == null) {
                Log.d(TAG, "skip load monitor " + monitorName);
                continue;
            }
            mMonitorsMap.put(monitorName, monitor);
            monitor.activate();
        }
        refreshPowerConsumeAppMonitors(monitorList, SystemClock.elapsedRealtime());
    }

    private void refreshPowerConsumeAppMonitors(String[] monitorList, long timestamp) {
        int changedCount = 0;
        for (String monitorName : monitorList) {
            PowerConsumeAppMonitor monitor = mMonitorsMap.get(monitorName);
            if (monitor == null) {
                continue;
            }
            if (!isWorking()) {
                mMonitorNextUpdateTimesMap.remove(monitorName);
                changedCount++;
                monitor.addStateChangedCallback(null);
            } else {
                if (monitor.getState() == PowerConsumeAppMonitor.STATE_ACTIVATED) {
                    monitor.init();
                    mMonitorNextUpdateTimesMap.put(monitorName, timestamp);
                    changedCount++;
                }
                monitor.addStateChangedCallback(this);
            }
        }
        if (changedCount > 0 && mCallback != null) {
            Log.d(TAG, "CALLBACK:refreshPowerConsumeAppMonitors res=" + changedCount + " ,call onChange");
            mCallback.onChange();
        }
    }

    public void removePowerConsumeAppMonitor(String[] monitorList) {
        for (String monitorName : monitorList) {
            PowerConsumeAppMonitor monitor = mMonitorsMap.get(monitorName);
            if (monitor == null) {
                continue;
            }
            monitor.deactivate();
            mMonitorsMap.remove(monitorName);
        }
    }

    public boolean isWorking() {
        return mIsWorking;
    }

    public void setWorking(boolean enable) {
        mIsWorking = enable;
    }

    public int getMonitorsCount() {
        return mMonitorsMap.size();
    }

    public void setCallback(IChangeCallback callback) {
        mCallback = callback;
    }

    public long getNextUpdateElapsedTime() {
        return getNextUpdateElapsedTime(SystemClock.elapsedRealtime());
    }

    public long getNextUpdateElapsedTime(long timestamp) {
        if (mMonitorNextUpdateTimesMap.isEmpty()) {
            return -1l;
        }
        long curretnElapsedTime = timestamp;
        long minNextTime = Long.MAX_VALUE;
        String nextMonitorName = "";
        for (Entry<String, Long> entry : mMonitorNextUpdateTimesMap.entrySet()) {
            Long nextUpdateTime = entry.getValue();
            if (nextUpdateTime < minNextTime) {
                minNextTime = nextUpdateTime;
                nextMonitorName = entry.getKey();
            }
        }
        if (minNextTime < curretnElapsedTime) {
            minNextTime = curretnElapsedTime;
        }
        Log.d(TAG, "next update after " + (minNextTime - curretnElapsedTime) + " for monitor "
                + nextMonitorName + " currenttime=" + curretnElapsedTime);
        return minNextTime;
    }

    public void updateData() {
        updateData(SystemClock.elapsedRealtime());
    }

    public void updateData(long timestamp) {
        long curretnElapsedTime = timestamp;
        SystemClock.elapsedRealtime();
        if (mLastUpdateElapsedTime < 0) {
            mLastUpdateElapsedTime = curretnElapsedTime;
        }
        for (Entry<String, Long> intervalEntry : mMonitorNextUpdateTimesMap.entrySet()) {
            long nextUpdateTime = intervalEntry.getValue();
            String monitorName = intervalEntry.getKey();
            if (nextUpdateTime > curretnElapsedTime) {
                continue;
            }
            PowerConsumeAppMonitor monitor = mMonitorsMap.get(monitorName);
            Log.d(TAG, "update monitor " + monitorName);
            mMonitorNextUpdateTimesMap.put(monitorName, monitor.getUpdateInterval() + curretnElapsedTime);
            Log.d(TAG, "set next update time for " + monitorName + " at "
                    + (monitor.getUpdateInterval() + curretnElapsedTime));
            updatePowerConsumeAppMonitor(monitorName, monitor);
        }
        mLastUpdateElapsedTime = curretnElapsedTime;
    }

    private void updatePowerConsumeAppMonitor(final String monitorName,
                                              final PowerConsumeAppMonitor monitor) {
        new Thread() {

            @Override
            public void run() {
                String dateStr = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        .format(new Date(System.currentTimeMillis()));
                Log.d(TAG, "update monitor " + monitor.getClass().getSimpleName() + " time:" + dateStr);
                monitor.dataUpdate();
                HashMap<String, Double> overThresholdResult = monitor.getOverThresholdData();
                if (mCallback != null) {
                    mCallback.onMonitorDataUpdate(monitorName, overThresholdResult);
                }
            }

        }.start();
    }

    public interface IChangeCallback {
        void onChange();

        void onMonitorDataUpdate(String monitorName, HashMap<String, Double> overThresholdResult);
    }

    public class BatteryChargeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setWorking(!BatteryStateInfo.isChargingNow(mContext));
            Log.d(TAG, "battery charging state changed");
            if (isWorking()) {
                mLastUpdateElapsedTime = -1l;
            }
            String[] allMonitorNames = new String[mMonitorsMap.size()];
            int conut = 0;
            for (String monitorName : mMonitorsMap.keySet()) {
                allMonitorNames[conut] = monitorName;
                conut++;
            }
            refreshPowerConsumeAppMonitors(allMonitorNames, SystemClock.elapsedRealtime());
        }

        public void set() {
            IntentFilter filter = new IntentFilter("android.intent.action.ACTION_POWER_CONNECTED");
            filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            mContext.registerReceiver(this, filter);
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }
}
