/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 功耗卡顿数据采集器工厂
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import com.cydroid.softmanager.utils.Log;

import android.content.Context;

public class AnalysisDataCollectorFactory {

    private static final String COLLECTOR_NAME_SKIP_FRAMES_DATA = "SkipFramesData";
    private static final String COLLECTOR_NAME_SYSTEM_SHUTDOWN_DATA = "SystemShutdownData";
    private static final String COLLECTOR_NAME_BATTERY_CHARGE_DATA = "BatteryChargeData";
    private static final String COLLECTOR_NAME_SLEEP_EVENT_BATTERY_DATA = "SleepEventBatteryData";
    private static final String COLLECTOR_NAME_POWER_LEVEL_EVENT_BATTERY_DATA = "PowerLevelData";
    private static final String COLLECTOR_NAME_APPS_BATTERY_DATA = "AppsBatteryData";
    private static final String TAG = "AnalysisDataCollectorFactory";

    public static IAnalysisDataCollector createInstanceByName(Context context, String collectorName) {
        switch (collectorName) {
            case COLLECTOR_NAME_APPS_BATTERY_DATA:
                return new AppsBatteryDataCollector(context);
            case COLLECTOR_NAME_POWER_LEVEL_EVENT_BATTERY_DATA:
                return new PowerLevelDataCollector(context);
            case COLLECTOR_NAME_SLEEP_EVENT_BATTERY_DATA:
                return new SleepEventBatteryDataCollector(context);
            case COLLECTOR_NAME_BATTERY_CHARGE_DATA:
                return new BatteryChargeDataCollector(context);
            case COLLECTOR_NAME_SYSTEM_SHUTDOWN_DATA:
                return new SystemShutdownDataCollector(context);
            case COLLECTOR_NAME_SKIP_FRAMES_DATA:
                return new SkipFramesDataCollector(context);
            default:
                Log.d(TAG, "unknow colletor name " + collectorName + " !! return null");
                return null;
        }

    }

}
