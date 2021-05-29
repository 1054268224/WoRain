/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 功耗卡顿数据采集服务
 *
 * Revised Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis;

import com.cydroid.softmanager.powersaver.analysis.collector.BatteryDataCollector;
import com.cydroid.softmanager.powersaver.analysis.utils.DatePeriodUtils;
import com.cydroid.softmanager.utils.Log;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.chenyee.featureoption.ServiceUtil;
public class AnalysisPowerDataService extends Service {
    private static final String TAG = "AnalysisPowerDataService";
    private static final String ACTION_PERIOD_CHANGED = "com.cydroid.softmanager.powersaver.analysis.ACTION_PERIOD_CHANGED";
    public static final String ACTION_SERVICE_START = "com.cydroid.softmanager.powersaver.analysis.ACTION_SERVICE_START";
    private AnalysisManager mAnalysisManager;
    private PendingIntent mMonitorNextPeriodAlarmPendingIntent;

    @Override
    public void onCreate() {
        mAnalysisManager = AnalysisManager.getInstance();
        mAnalysisManager.init(getApplicationContext());
        Intent periodChangeIntent = new Intent(this, this.getClass());
        periodChangeIntent.setAction(ACTION_PERIOD_CHANGED);
        mMonitorNextPeriodAlarmPendingIntent = PendingIntent.getService(this, 0, periodChangeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onDestroy() {
        mAnalysisManager.deinit();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mMonitorNextPeriodAlarmPendingIntent);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY;
        }
        ServiceUtil.handleStartForegroundServices(this);
        String actionStr = intent.getAction();
        Log.d(TAG, "onStartCommand-->" + actionStr);
        if (actionStr == null || actionStr.isEmpty()) {
            return Service.START_STICKY;
        }
        if (actionStr.equals(ACTION_SERVICE_START)) {
            Log.d(TAG, "AnalysisPowerDataService start -----> ");
            long nextAlarmTime = DatePeriodUtils.getNextPeriodTimeInMillis(System.currentTimeMillis());
            setNextPeriodAlarm(nextAlarmTime);
        } else if (actionStr.equals(ACTION_PERIOD_CHANGED)) {
            Log.d(TAG, "AnalysisPowerDataService ACTION_PERIOD_CHANGED -----> ");
            notifyPeriodChanged();
            long nextAlarmTime = DatePeriodUtils.getNextPeriodTimeInMillis(System.currentTimeMillis());
            setNextPeriodAlarm(nextAlarmTime);
        }
        return Service.START_STICKY;
    }

    private void setNextPeriodAlarm(long nextAlarmTime) {
        Log.d(TAG, "next period ----->" + nextAlarmTime);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mMonitorNextPeriodAlarmPendingIntent);
        alarmManager.set(AlarmManager.RTC, nextAlarmTime, mMonitorNextPeriodAlarmPendingIntent);
    }

    private void notifyPeriodChanged() {
        Intent periodChangedIntent = new Intent(BatteryDataCollector.ACTION_TIME_PERIOD_CHANGE);
        sendBroadcast(periodChangedIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
