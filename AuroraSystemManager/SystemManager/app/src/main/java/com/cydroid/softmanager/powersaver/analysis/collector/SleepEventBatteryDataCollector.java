/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 灭屏休眠事件采集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;

import com.android.internal.os.BatteryStatsHelper;
import com.cydroid.softmanager.powersaver.analysis.utils.BatteryDataUtils;
import com.cydroid.softmanager.powersaver.analysis.utils.DatePeriodUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.HelperUtils.WakelockInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryStats;
import android.os.PowerManager;
import android.os.BatteryStats.HistoryItem;
import android.os.PowerManager.WakeLock;

public class SleepEventBatteryDataCollector extends BatteryDataCollector {
    private static final String TAG = "SleepEventBatteryDataCollector";

    private static final String WORKING_PERIOD_DATA_PTAH = "data/data/com.cydroid.softmanager/ScreenOffEventInfo_current.obj";
    private static final String RESULT_DATA_SET_PTAH = "data/data/com.cydroid.softmanager/ScreenOffEventInfo_result.obj";

    private static final int STATUS_SCREEN_ON = 0;
    private static final int STATUS_SCREEN_OFF = 1;
    private static final int STATUS_SLEEP = 2;
    private static final int STATUS_UNKNOW = STATUS_SCREEN_ON;

    private static final String PREF_WAKELOCK_INFO = "analysis_wakelock_info";
    private static final String PREF_SLEEP_STATE = "analysis_sleep_state";
    private static final String PREF_KEY_LAST_SYSTEM_STATUS = "status_in_last_record";
    private static final String PREF_KEY_LAST_RECORD_TIMESTAMP = "last_record_timestamp";

    private ScreenOffWakelockReceiver mWakelockReceiver = null;

    public SleepEventBatteryDataCollector(Context context) {
        super(context);
    }

    @Override
    public void init() {
        super.init();
        mWakelockReceiver = new ScreenOffWakelockReceiver(mContext);
    }

    @Override
    public void deinit() {
        if (mWakelockReceiver != null) {
            mWakelockReceiver.remove();
        }
        super.deinit();
    }

    @Override
    public void onTimePeriodChange(BatteryStatsHelper normalBatteryStats, String periodLabel) {
        Map<String, String> workingRecordData = getDataFromFile(WORKING_PERIOD_DATA_PTAH);
        if (workingRecordData == null) {
            workingRecordData = new HashMap<String, String>();
        }
        Map<String, Map<String, String>> resultDataSet = getDataFromFile(RESULT_DATA_SET_PTAH);
        if (resultDataSet == null) {
            resultDataSet = new HashMap<String, Map<String, String>>();
        }

        String[] lastSleepState = getLastSleepData();
        Log.d(TAG, "onTimePeriodChange workingRecordData size = " + workingRecordData.size()
                + " last state = " + lastSleepState[0] + " last timestamp = " + lastSleepState[1]);
        String[] newSleepState = AttachHistoryEvents(normalBatteryStats, workingRecordData, lastSleepState);
        Log.d(TAG, "onTimePeriodChange after process new data workingRecordData size = "
                + workingRecordData.size());
        saveLastSleepData(newSleepState);
        Log.i(TAG,
                "resultDataSet.put " + periodLabel + ", currentRecordData.size " + workingRecordData.size());
        resultDataSet.put(periodLabel, (Map<String, String>) BatteryDataUtils.deepClone(workingRecordData));
        workingRecordData.clear();

        saveDataToFile(WORKING_PERIOD_DATA_PTAH, (Serializable) workingRecordData);
        saveDataToFile(RESULT_DATA_SET_PTAH, (Serializable) resultDataSet);
    }

    private String[] getLastSleepData() {
        SharedPreferences sleepStatePreference = mContext.getSharedPreferences(PREF_SLEEP_STATE,
                Context.MODE_PRIVATE);
        int state = sleepStatePreference.getInt(PREF_KEY_LAST_SYSTEM_STATUS, STATUS_UNKNOW);
        long timestamp = sleepStatePreference.getLong(PREF_KEY_LAST_RECORD_TIMESTAMP, 0);
        String[] res = new String[] {String.valueOf(state), String.valueOf(timestamp)};
        return res;
    }

    private String[] AttachHistoryEvents(BatteryStatsHelper batteryStatsHelper, Map<String, String> result,
            String[] lastState) {
        int lastSystemStatus = Integer.parseInt(lastState[0]);
        long beginTime = Long.parseLong(lastState[1]);
        BatteryStats stats = batteryStatsHelper.getStats();
        Log.i(TAG, "AttachHistoryEvents -------> begin:" + beginTime);

        long lastEventTimestamp = beginTime;
        int sleepCounts = 0;
        if (stats.startIteratingHistoryLocked()) {
            final HistoryItem rec = new HistoryItem();
            while (stats.getNextHistoryLocked(rec)) {
                if (rec.currentTime <= beginTime) {
                    continue;
                }
                if (!rec.isDeltaData()) {
                    continue;
                }

                final boolean isCpuRunningOn = (rec.states & HistoryItem.STATE_CPU_RUNNING_FLAG) != 0;
                final boolean isScreenOn = (rec.states & HistoryItem.STATE_SCREEN_ON_FLAG) != 0;
                int currentSystemStatus = computeSystemStatus(isCpuRunningOn, isScreenOn);
                if (currentSystemStatus == lastSystemStatus) {
                    continue;
                }
                if (lastSystemStatus == STATUS_SCREEN_ON) {
                    if (currentSystemStatus == STATUS_SCREEN_OFF) {
                        sleepCounts = 0;
                    }
                } else if (lastSystemStatus == STATUS_SCREEN_OFF) {
                    if (currentSystemStatus == STATUS_SCREEN_ON) {
                        if (sleepCounts == 0) {
                            long idleDuration = rec.currentTime - lastEventTimestamp;
                            long screenOffTimestamp = lastEventTimestamp;
                            if (beginTime > 0) {
                                result.put(String.valueOf(rec.currentTime),
                                        eventMapToString(screenOffTimestamp, idleDuration, false));
                            }
                        }
                    } else if (currentSystemStatus == STATUS_SLEEP) {
                        if (sleepCounts == 0) {
                            long idleDuration = rec.currentTime - lastEventTimestamp;
                            long screenOffTimestamp = lastEventTimestamp;
                            if (beginTime > 0) {
                                result.put(String.valueOf(rec.currentTime),
                                        eventMapToString(screenOffTimestamp, idleDuration, true));
                            }
                        }
                        sleepCounts++;
                    }
                }
                if (rec.currentTime > lastEventTimestamp) {
                    lastEventTimestamp = rec.currentTime;
                }
                lastSystemStatus = currentSystemStatus;
            }
        }
        String[] newState = new String[] {String.valueOf(lastSystemStatus),
                String.valueOf(lastEventTimestamp)};
        Log.d(TAG,
                "history record finish Status = " + lastSystemStatus + " timestamp = " + lastEventTimestamp);
        return newState;
    }

    private int computeSystemStatus(boolean isCpuRunningOn, boolean isScreenOn) {
        if (isScreenOn) {
            return STATUS_SCREEN_ON;
        } else {
            if (isCpuRunningOn) {
                return STATUS_SCREEN_OFF;
            } else {
                return STATUS_SLEEP;
            }
        }
    }

    private String eventMapToString(long screenOffTimestamp, long idleDuration, boolean isSleep) {
        StringBuilder resBuilder = new StringBuilder();
        resBuilder.append(screenOffTimestamp).append(",").append(idleDuration).append(",")
                .append(isSleep);
        return resBuilder.toString();
    }

    private void saveLastSleepData(String[] newSleepState) {
        Editor sleepStatePreferenceEditor = mContext
                .getSharedPreferences(PREF_SLEEP_STATE, Context.MODE_PRIVATE).edit();
        sleepStatePreferenceEditor.putInt(PREF_KEY_LAST_SYSTEM_STATUS, Integer.parseInt(newSleepState[0]));
        sleepStatePreferenceEditor.putLong(PREF_KEY_LAST_RECORD_TIMESTAMP, Long.parseLong(newSleepState[1]));
        sleepStatePreferenceEditor.commit();
    }

    @Override
    public void onBatteryStatsReset(BatteryStatsHelper backupBatteryStats) {
        Log.i(TAG, "onBatteryStatsReset -------> ");
        Map<String, String> workingRecordData = getDataFromFile(WORKING_PERIOD_DATA_PTAH);
        if (workingRecordData == null) {
            workingRecordData = new HashMap<String, String>();
        }

        String[] lastSleepState = getLastSleepData();
        Log.d(TAG, "onBatteryStatsReset workingRecordData size = " + workingRecordData.size()
                + " last state = " + lastSleepState[0] + " " + lastSleepState[1]);
        String[] newSleepState = AttachHistoryEvents(backupBatteryStats, workingRecordData, lastSleepState);
        Log.d(TAG, "onBatteryStatsReset after process new data workingRecordData size = "
                + workingRecordData.size());
        saveLastSleepData(newSleepState);

        saveDataToFile(WORKING_PERIOD_DATA_PTAH, (Serializable) workingRecordData);
    }

    public class ScreenOffWakelockReceiver extends BroadcastReceiver {
        private final Context mContext;

        public ScreenOffWakelockReceiver(Context context) {
            mContext = context;
            IntentFilter eventFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            mContext.registerReceiver(this, eventFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            new WakelockLogThread(context).start();
        }

        public void remove() {
            mContext.unregisterReceiver(this);
        }
    }

    private class WakelockLogThread extends Thread {
        private static final int WAKELOCK_TIMEOUT = 10000;
        private final Context mContext;

        public WakelockLogThread(Context context) {
            super();
            mContext = context;
        }

        @Override
        public void run() {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            WakeLock screenOffWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "for_record_STAT_SCREENOFF");
            screenOffWakelock.setReferenceCounted(false);
            screenOffWakelock.acquire(WAKELOCK_TIMEOUT);
            long timestamp = System.currentTimeMillis();
            recordWakelocks(mContext, timestamp);
            if (screenOffWakelock.isHeld()) {
                screenOffWakelock.release();
            }
        }

        private void recordWakelocks(Context context, long timestamp) {
            SharedPreferences wakelockPreference = context.getSharedPreferences(PREF_WAKELOCK_INFO,
                    Context.MODE_PRIVATE);
            ArrayList<WakelockInfo> wakelocks = HelperUtils.getWakelocks(context);
            List<Map<String, String>> wakelocksArray = new ArrayList<Map<String, String>>();
            Log.d(TAG, "get wakelock list size = " + wakelocks.size());
            for (WakelockInfo wakelockinfo : wakelocks) {
                if ("for_record_STAT_SCREENOFF".equals(wakelockinfo.name)) {
                    continue;
                }
                if ("screen_off_clean".equals(wakelockinfo.name)) {
                    continue;
                }
                Map<String, String> wakelockMap = new HashMap<String, String>();
                wakelockMap.put("n", wakelockinfo.name);
                wakelockMap.put("pn", wakelockinfo.pkgName);
                wakelockMap.put("pv", wakelockinfo.pkgVersion);
                wakelocksArray.add(wakelockMap);
            }
            String jsonString = BatteryDataUtils.mapArrayInfoToJsonString(wakelocksArray);
            Log.d(TAG, "save MapArray (time:" + timestamp + ") info " + jsonString);
            wakelockPreference.edit().putString(String.valueOf(timestamp), jsonString).commit();
        }
    }

    @Override
    public List<Map<String, Object>> getAndFlushNewDataSet(String workingPeriodLabel) {
        List<Map<String, Object>> sleepEventMapList = new ArrayList<Map<String, Object>>();
        Map<String, Map<String, String>> allBatteryEvent = getDataFromFile(RESULT_DATA_SET_PTAH);
        Iterator<Entry<String, Map<String, String>>> it = allBatteryEvent.entrySet().iterator();
        SharedPreferences wakelockPreference = mContext.getSharedPreferences(PREF_WAKELOCK_INFO,
                Context.MODE_PRIVATE);
        Map<String, String> wakelockInfoMap = (Map<String, String>) wakelockPreference.getAll();
        Log.d(TAG, "wakelocks info size=" + wakelockInfoMap.size());
        long lastDataRecordTimestamp = -1l;
        while (it.hasNext()) {
            Entry<String, Map<String, String>> eventEntry = it.next();
            String periodTime = eventEntry.getKey();
            Log.i(TAG, "dayTime periodLabel ---> " + periodTime);
            if (periodTime.equals(workingPeriodLabel)) {
                Log.d(TAG, "do not upload data in workingPeriodLabel = " + workingPeriodLabel);
                continue;
            }
            long periodTimeMillis = DatePeriodUtils.convertDateFormatStringToMillis(periodTime);
            if (periodTimeMillis > lastDataRecordTimestamp) {
                lastDataRecordTimestamp = periodTimeMillis;
            }
            Map<String, String> chartInfo = eventEntry.getValue();

            for (Map.Entry<String, String> entry : chartInfo.entrySet()) {
                HashMap<String, Object> eventMap = eventStringToMap(entry.getValue());
                if (eventMap == null) {
                    continue;
                }
                String wakelocksStr = pickUpWakelockStrByDuration(
                        Long.parseLong((String) eventMap.get("timestamp")),
                        Long.parseLong((String) eventMap.get("idleDuration")), wakelockInfoMap);
                List<String> wakelocksStrs = new ArrayList<String>();
                try {
                    wakelocksStrs = BatteryDataUtils.jsonStringToArrayInfo(wakelocksStr);
                } catch (JSONException e) {
                    Log.d(TAG, "parse json error " + e);
                }
                for (int i = 0; i < wakelocksStrs.size(); i++) {
                    eventMap.put("wL" + i, wakelocksStrs.get(i));
                }
                sleepEventMapList.add(eventMap);
            }
            it.remove();
        }
        if (lastDataRecordTimestamp > 0) {
            Log.d(TAG, "remove wakelock info before " + lastDataRecordTimestamp + " from preference");
            for (String wakelockTimestampStr : wakelockInfoMap.keySet()) {
                if (Long.parseLong(wakelockTimestampStr) < lastDataRecordTimestamp) {
                    wakelockPreference.edit().remove(wakelockTimestampStr);
                }
            }
            wakelockPreference.edit().commit();
        }
        saveDataToFile(RESULT_DATA_SET_PTAH, (Serializable) allBatteryEvent);
        return sleepEventMapList;
    }

    private HashMap<String, Object> eventStringToMap(String value) {
        String[] resStr = value.split(",", 3);
        if (resStr.length < 3) {
            return null;
        }
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("timestamp", resStr[0]);
        resMap.put("idleDuration", resStr[1]);
        resMap.put("isSleep", resStr[2]);
        return resMap;
    }

    private String pickUpWakelockStrByDuration(long beginTime, long idleDuration,
            Map<String, String> wakelockInfoMap) {
        long endtime = beginTime + idleDuration;
        String res = "";
        long wakelockTimestamp = -1;
        for (Entry<String, String> entry : wakelockInfoMap.entrySet()) {
            wakelockTimestamp = Long.parseLong(entry.getKey());
            if (wakelockTimestamp < beginTime) {
                continue;
            }
            if (wakelockTimestamp >= endtime) {
                continue;
            }
            res = entry.getValue();
            break;
        }
        if (!res.isEmpty()) {
            Log.d(TAG, "get wakelocks info between begin=" + beginTime + " end=" + endtime + " diff="
                    + (wakelockTimestamp - beginTime) + " data=" + res);
        }
        return res;
    }
}
