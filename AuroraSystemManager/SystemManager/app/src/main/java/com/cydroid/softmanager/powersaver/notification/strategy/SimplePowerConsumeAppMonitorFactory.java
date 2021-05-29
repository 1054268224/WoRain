package com.cydroid.softmanager.powersaver.notification.strategy;

import android.content.Context;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.Log;

public class SimplePowerConsumeAppMonitorFactory {
    private static final String TAG = "SimplePowerConsumeAppMonitorFactory";

    public final static String MONITOR_NAME_HIGH_VOLTAGE = "voltage";
    public final static String MONITOR_NAME_LONG_WAKELOCK = "wakelock";
    public final static String[] MONITOR_NAME_LIST = {MONITOR_NAME_HIGH_VOLTAGE, MONITOR_NAME_LONG_WAKELOCK};

    public static PowerConsumeAppMonitor createMonitorInstanceByName(Context context, String monitorName) {
        switch (monitorName) {
            case MONITOR_NAME_HIGH_VOLTAGE:
                return new HighVoltageConsumeMonitor(context);
            case MONITOR_NAME_LONG_WAKELOCK:
                return new LongWakelockDurationMonitor(context);
            default:
                Log.d(TAG, "can not find monitor : " + monitorName + " return null");
                return null;
        }
    }

    public static int getNotificationTitleStringId(String monitorName) {
        switch (monitorName) {
            case MONITOR_NAME_HIGH_VOLTAGE:
                return R.string.power_consume_notification_content;
            case MONITOR_NAME_LONG_WAKELOCK:
                return R.string.power_consume_notification_wakelock_content;
            default:
                return 0;
        }
    }

    public static String getYoujuEventNameString(String monitorName) {
        switch (monitorName) {
            case MONITOR_NAME_HIGH_VOLTAGE:
                return "High_Power";
            case MONITOR_NAME_LONG_WAKELOCK:
                return "Long_Wakelock";
            default:
                return "";
        }
    }

    public static int getMessageStringId(String monitorName) {
        switch (monitorName) {
            case MONITOR_NAME_HIGH_VOLTAGE:
                return R.string.background_running_set_power_monitor_message;
            case MONITOR_NAME_LONG_WAKELOCK:
                return R.string.background_running_set_wakelock_monitor_message;
            default:
                return 0;
        }
    }
}
