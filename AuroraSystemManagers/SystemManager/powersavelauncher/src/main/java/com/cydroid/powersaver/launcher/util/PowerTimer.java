package com.cydroid.powersaver.launcher.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.BatteryManager;

import com.cydroid.powersaver.launcher.R;

public class PowerTimer {
    private static final String TAG = "PowerTimer";
    private static final int HOUR_TO_MINS = 60;
    private Context mContext;
    private PowerConfig mConfig;

    public PowerTimer(Context context) {
        mContext = context;
        mConfig = PowerConfigParser.getProjectConfig(context);
    }

    // TODO:需要根据测试结果获得在极致省电模式时的可用时间
    public int getTimeInSuperMode() {
        int level = getCurrentLevel();
        if (level < 0) {
            return 0;
        }
        // Gionee <yangxinruo> <2015-09-21> modify for CR01557555 begin
        float currentCapacity = level * mConfig.battery_capacity / (float) 100;
        int time = (int) (HOUR_TO_MINS * (currentCapacity / (float) mConfig.current_in_supermode));
        Log.d(TAG, "getTimeInSuperMode(), currentCapacity = " + currentCapacity + ", time = " + time);
        // Gionee <yangxinruo> <2015-09-21> modify for CR01557555 end
        return time;
    }

    // Gionee xionghg add for power saving optimization 145357 begin
    public int getCanUseTimeInSuperMode() {
        int level = getCurrentLevel();
        if (level < 0) {
            return 0;
        }
        float currentCapacity = level * mConfig.battery_capacity / (float) 100;
        // TODO:根据测试结果在配置文件中获得使用时电流
        float useCurrent = 200.0f;
        int time = (int) (HOUR_TO_MINS * (currentCapacity / useCurrent));
        Log.d(TAG, "getCanUseTimeInSuperMode(), currentCapacity = " + currentCapacity + ", time = " + time);
        return time;
    }
    // Gionee xionghg add for power saving optimization 145357 end    

    /*计算在出厂状态下的可用时间*/
    private int getTimeAccordingLevel(int[] durations) {
        /*        int[] durations = {mConfig.zero_ten_time, mConfig.ten_twenty_time, mConfig.twenty_thirty_time,
                        mConfig.thirty_forty_time, mConfig.forty_fifty_time, mConfig.fifty_sixty_time,
                        mConfig.sixty_seventy_time, mConfig.seventy_eight_time, mConfig.eight_ninety_time,
                        mConfig.ninety_hundred_time};*/
        int sum = 0;
        int level = getCurrentLevel();
        // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 begin
        // int size = durations.length;
        int size = 100 / durations.length;
        // Gionee <yangxinruo> <2015-08-21> modify for CR01541645 end
        int tens = level / size;
        int single = level % size;

        Log.e(TAG, "tens = " + tens + ", single = " + single);

        for (int i = 0; i < tens; i++) {
            sum += durations[i];
        }

        if (level != 100) {
            sum += single * durations[tens] / size;
        }
        sum = (int) (sum * getMagnification(level));

        return sum;
    }

    private int getCurrentLevel() {
        return BatteryStateHelper.getBatteryLevel(mContext);
    }

    /*
     * 当电量高于某一值时，放大真实的测试使用时间
     * y = k*x + b
     * multiple = 3 放大倍数
     * xMax = (100 - lowerLimit) / 50f
     * bArg = multiple / xMax 
     * kArg = multiple + bArg 斜率
     * */
    private float getMagnification(int level) {
        /*        int lowerLimit = 40;
                if (level < lowerLimit) {
                    return 1.0f;
                } else {
                    float xMax = (100 - lowerLimit) / 50f;
                    float multiple = 3;
                    float bArg = multiple / xMax;
                    float kArg = multiple + bArg;
                    float x = (100f - level) / 50f;
                    return ( kArg / (x + 1.0f) ) + 1.0f;
                }*/

        return 1.0f;
    }

    public int getChargeTime() {
        int time = 0;
        int chargeType = BatteryStateHelper.getBatteryPlugType(mContext);
        int consumedCurrent = getConsumedCapacity(mContext);
        Log.e(TAG, "chargeType = " + chargeType + ", consumedCurrent = " + consumedCurrent);
        // TODO: maybe this situation maybe can't be happen
        if (mConfig.ac_current == 0) {
            mConfig.ac_current = 900;
        }

        if (mConfig.usb_current == 0) {
            mConfig.usb_current = 350;
        }

        if (chargeType == BatteryManager.BATTERY_PLUGGED_AC) {
            time = consumedCurrent * HOUR_TO_MINS / mConfig.ac_current;
        } else if (chargeType == BatteryManager.BATTERY_PLUGGED_USB) {
            time = consumedCurrent * HOUR_TO_MINS / mConfig.usb_current;
        }
        Log.e(TAG, "PowerTimer->getChargeTime(), time = " + time);

        return time;
    }

    private int getConsumedCapacity(Context context) {
        int capacity = (mConfig.battery_capacity / 100) * (100 - getCurrentLevel());
        Log.e(TAG, "PowerTimer->getConsumedCapacity(), battery_capacity = " + mConfig.battery_capacity
                + " level = " + getCurrentLevel() + ", capacity = " + capacity);
        return capacity;
    }

    public String formatTime(int time) {
        String timeStr;
        Resources res = mContext.getResources();
        if (time < 0) {
            timeStr = res.getString(R.string.power_cannotget);
        } else {
            int hours = time / 60;
            int minutes = time % 60;
            timeStr = hours + res.getString(R.string.power_hours) + minutes
                    + res.getString(R.string.power_minutes);
        }

        return timeStr;
    }

}