/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 充电事件采集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cydroid.softmanager.powersaver.utils.BatteryStateInfo;
import com.cydroid.softmanager.utils.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BatteryChargeDataCollector implements IAnalysisDataCollector {
    private static final String TAG = "BatteryChargeDataCollector";
    private static final String PREF_BATTERY_CHARGE_STATE = "battery_charge_state";

    private final Context mContext;
    private final List<Map<String, Object>> mChargingEvents;
    private INewDataListener mDataListener;
    private ChargingReceiver mChargingReceiver;

    public BatteryChargeDataCollector(Context context) {
        mContext = context;
        mChargingEvents = new ArrayList<Map<String, Object>>();
    }

    @Override
    public void init() {
        mChargingReceiver = new ChargingReceiver(mContext);
    }

    @Override
    public void deinit() {
        if (mChargingReceiver != null) {
            mChargingReceiver.remove();
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
        if (!mChargingEvents.isEmpty()) {
            result.addAll(mChargingEvents);
        }
        mChargingEvents.clear();
        return result;
    }

    public class ChargingReceiver extends BroadcastReceiver {
        private final Context mContext;

        public ChargingReceiver(Context context) {
            mContext = context;
            IntentFilter eventFilter = new IntentFilter();
            eventFilter.addAction("android.os.action.CHARGING");
            eventFilter.addAction("android.os.action.DISCHARGING");
            mContext.registerReceiver(this, eventFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr == null || actionStr.isEmpty()) {
                return;
            }
            if (actionStr.equals("android.os.action.CHARGING")) {
                onCharging(System.currentTimeMillis(), BatteryStateInfo.getBatteryLevel(context));
            } else if (actionStr.equals("android.os.action.DISCHARGING")) {
                onDischarging(System.currentTimeMillis(), BatteryStateInfo.getBatteryLevel(context));
            }
        }

        private void onCharging(long timestamp, int batteryLevel) {
            Editor statsPrefEditor = mContext
                    .getSharedPreferences(PREF_BATTERY_CHARGE_STATE, Context.MODE_PRIVATE).edit();
            statsPrefEditor.putLong("charging_start", timestamp).putInt("charging_start_power", batteryLevel)
                    .commit();
        }

        private void onDischarging(long timestamp, int batteryLevel) {
            SharedPreferences statsPref = mContext.getSharedPreferences(PREF_BATTERY_CHARGE_STATE,
                    Context.MODE_PRIVATE);
            if (!statsPref.contains("charging_start")) {
                Log.d(TAG, "no charging record found , skip discharging event");
                return;
            }
            long chargingDuration = timestamp - statsPref.getLong("charging_start", timestamp);
            int powerDiff = batteryLevel - statsPref.getInt("charging_start_power", batteryLevel);
            if (isInvalidEvent(chargingDuration, powerDiff)) {
                return;
            }
            HashMap<String, Object> chargingMap = new HashMap<String, Object>();
            chargingMap.put("time", millisToSeconds(chargingDuration));// seconds
            chargingMap.put("chargedPower", powerDiff);
            chargingMap.put("disChargingPower", batteryLevel);
            int resultSize = addToDataSet(chargingMap);
            Log.i(TAG, "resultDataSet.put :" + chargingMap + ", currentRecordData.size " + resultSize);
            statsPref.edit().remove("charging_start").remove("charging_start_power").commit();
            if (mDataListener != null) {
                mDataListener.onNewData(BatteryChargeDataCollector.this);
            }
        }

        private boolean isInvalidEvent(long chargingDuration, int powerDiff) {
            return chargingDuration <= 0 || powerDiff <= 0;
        }

        private long millisToSeconds(long num) {
            return (long) Math.ceil((num / 1000f));
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }

    public int addToDataSet(HashMap<String, Object> event) {
        mChargingEvents.add(event);
        return mChargingEvents.size();
    }
}
