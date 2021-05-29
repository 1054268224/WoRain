package com.cydroid.softmanager.powersaver.utils;

import com.cydroid.softmanager.utils.Log;

import java.math.BigDecimal;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.powersaver.interfaces.IPowerService;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.BatteryManager;

public class PowerTimer {
    private final Context mContext;
    private final PowerConfig mConfig;
    private static final int HOUR_TO_MINS = 60;
    private static final boolean DEBUG = true;
    private static final String TAG = "PowerTimer";

    public PowerTimer(Context context) {
        mContext = context;
        mConfig = PowerConfigParser.getProjectConfig(context);
    }

    // TODO:需要根据测试结果获得在极致省电模式时的可用时间
    public int getTimeInSuperMode() {
        /*        int[] durations = {120, 120, 120, 120, 120, 120, 120, 120, 120, 120};
                int time = getTimeAccordingLevel(durations);*/
        int level = getCurrentLevel();
        if (level < 0) {
            return 0;
        }
        // Gionee <yangxinruo> <2015-09-21> modify for CR01557555 begin
        // Gionee <yangxinruo> <2015-9-9> modify for CR01550335 begin
        float currentCapacity = level * mConfig.battery_capacity / (float) 100;
        // Gionee <yangxinruo> <2015-9-9> modify for CR01550335 end
        int time = (int) (HOUR_TO_MINS * (currentCapacity / (float) mConfig.current_in_supermode));
        Log.d(DEBUG, TAG, "getTimeInSuperMode(), currentCapacity = " + currentCapacity + ", time = " + time);
        // Gionee <yangxinruo> <2015-09-21> modify for CR01557555 end
        return time;
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
            // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
            if (BatteryStateInfo.getBatteryLevel(mContext) <= 1)
                timeStr = res.getString(R.string.power_low, timeStr);
            else
                timeStr = res.getString(R.string.power_about, timeStr);
            // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end
        }

        return timeStr;
    }

    private int getCurrentLevel() {
        return BatteryStateInfo.getBatteryLevel(mContext);
    }

    private int getTimeInOriginalMode() {
        int[] durations = {mConfig.zero_ten_time, mConfig.ten_twenty_time, mConfig.twenty_thirty_time,
                mConfig.thirty_forty_time, mConfig.forty_fifty_time, mConfig.fifty_sixty_time,
                mConfig.sixty_seventy_time, mConfig.seventy_eight_time, mConfig.eight_ninety_time,
                mConfig.ninety_hundred_time};

        return getTimeAccordingLevel(durations);

    }

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

        Log.d(DEBUG, TAG, "tens = " + tens + ", single = " + single);

        for (int i = 0; i < tens; i++) {
            sum += durations[i];
        }

        if (level != 100) {
            sum += single * durations[tens] / size;
        }
        sum = (int) (sum * getMagnification(level));

        return sum;
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

    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
    public long getWeightedTime(float weight) {
        BigDecimal bd = new BigDecimal(weight);
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
        Log.d(TAG, "getWeightedTime weight=" + bd.floatValue());
        int time = 0;
        time = getTimeInOriginalMode();
        Log.d(TAG, "getWeightedTime oriTime=" + time);
        time = (int) (time / bd.floatValue());
        return time;
    }
    // Gionee <yangxinruo> <2016-3-18> add for CR01654969 end

    public long getTimeInMode(IPowerService modeService, int mode) {
        if (modeService == null) {
            return -2;
        }
        try {
            if (modeService.isConfigDiffFromDefault(mode)) {
                return -1;
            } else {
                float weight = modeService.getModeDefaultWeight(mode);
                return getWeightedTime(weight);
            }
        } catch (Exception e) {
            Log.d(TAG, "call service error :" + e);
            return -2;
        }
    }

    public String getTimeStrInNormalMode(String format, IPowerService modeService) {
        String str = null;
        int time = 0;
        // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 begin
        str = format;
        // time = mPowerTimer.getTimeInNormalMode();
        if (modeService == null) {
            Log.d(TAG, "PowerService is null");
            return String.format(str, formatTime(-1));
        }
        time = (int) getTimeInMode(modeService, PowerConsts.NORMAL_MODE);
        if (time == -1) {
            str = mContext.getResources().getString(R.string.mode_item_config_changed);
        } else {
            str = String.format(str, formatTime(time));
        }
        // Gionee <yangxinruo> <2016-3-18> modify for CR01654969 end
        Log.d(TAG, "getTimeStrInNormalMode(), str = " + str);
        return str;
    }

    public String getTimeStrInSuperMode(String format, String perfix) {
        Resources res = mContext.getResources();
        int time = getTimeInSuperMode();
        String msgSuperPerfix = perfix;
        if (BatteryStateInfo.getBatteryLevel(mContext) <= 1)
            msgSuperPerfix = res.getString(R.string.dialog_superpower_message_low);
        String message = String.format(format, msgSuperPerfix, formatTime(time));
        Log.d(TAG, "getTimeStrInSuperMode(), str = " + message);
        return message;
    }

    // Gionee xionghg add for power saving optimization 145357 begin
    /**
     * Optimize old API, when {time} in middle of timeStr
     *
     * @param timeStrFormat  format string with single placeholder to format timeStr
     * @param timeFormat     format string with single placeholder to format time
     * @return final formatted timeStr
     */
    public String getTimeStrInSuperModeOptimizer(String timeStrFormat, String timeFormat) {
        Resources res = mContext.getResources();
        int time = getTimeInSuperMode();
        String timeStr = "";
        if (time < 0) {
            timeStr = res.getString(R.string.power_cannotget);
        } else {
            int hours = time / 60;
            int minutes = time % 60;
            timeStr = hours + res.getString(R.string.power_hours) +
                    minutes + res.getString(R.string.power_minutes);
            timeStr = String.format(timeFormat, timeStr);
        }
        String message;
        if (timeStrFormat == null || timeStrFormat.length() == 0) {
            // format just once if timeStrFormat is null
            message = timeStr;
        } else {
            message = String.format(timeStrFormat, timeStr);
        }
        Log.d(TAG, "getTimeStrInSuperModeOpt(), str = " + message);
        return message;
    }
    // Gionee xionghg add for power saving optimization 145357 begin
}
