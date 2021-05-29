package com.cydroid.softmanager.powersaver.notification.strategy;

import android.content.Context;
import android.os.BatteryStats;
import android.os.Bundle;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

public class BatteryStatsUtils {
    private static final String TAG = "BatteryStatsUtils";

    private static final int BATTERY_STATS_TYPE = BatteryStats.STATS_SINCE_CHARGED;

    public static synchronized List<BatterySipper> getBatterySippers(Context context) {
        List<BatterySipper> res = null;
        try {
            BatteryStatsHelper batteryHelper = new BatteryStatsHelper(context, true);
            batteryHelper.create((Bundle) null);
            batteryHelper.clearStats();
            batteryHelper.refreshStats(BATTERY_STATS_TYPE, -1);
            res = batteryHelper.getUsageList();
            long periodTime = batteryHelper.getStatsPeriod() / 1000;
            Log.d(TAG, "batteryStat period:" + periodTime / 3600000 + "hours " + periodTime % 3600000 / 60000
                    + "mins " + periodTime % 3600000 % 60000 / 1000);
        } catch (Exception e) {
            Log.d(TAG, "get BatterySipper error! " + e);
            res = null;
        }
        return res;
    }
}
