package com.cydroid.softmanager.applock.verifier.auth;

import android.content.Context;
import android.provider.Settings;

/**
 * @author xionghg
 * @created 17-8-8.
 */

public class SettingVerifierUtil implements VerifierUtil {

    private static final String TAG = "SettingVerifierUtil";
    private static final String INPUT_ERROR_NUMBER = "input_error_number";
    private static final String INPUT_ERROR_TIME = "input_error_time";
    private static final int START_TIMING_NUMBER = 5;
    private static final int TOTAL_TIMING_NUMBER = 15;
    private static final long ONE_MIN_TO_MILLI = 60 * 1000;

    private final Context mAppContext;

    public SettingVerifierUtil(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    public boolean isInFreeze() {
        return getFreezeRemainingMillis() > 0;
    }

    @Override
    public int getPwdFailRemainCount() {
        int number = getErrorNumber();
        int remain = START_TIMING_NUMBER - number;
        if (remain < 0) remain = 0;
        return remain;
    }

    @Override
    public long getFreezeRemainingMillis() {
        int number = getErrorNumber();
        if (number < START_TIMING_NUMBER) {
            return -1;
        }
        long result = 0;
        long waitTime = getWaitInputPwdMinute(number) * ONE_MIN_TO_MILLI;

        long durationTime = getDurationTime(waitTime);
        if (durationTime >= 0) {
            //refresh wait time
            result = waitTime - durationTime;
        }
        return result;
    }

    @Override
    public void verifySuccess() {
        setErrorNumber(0);
        setErrorTime(0);
    }

    @Override
    public void verifyFailed() {
        addErrorNumber();
        setErrorTime(System.currentTimeMillis());
    }

    private int addErrorNumber() {
        int number = getErrorNumber();
        //最低1分钟，最多1024分钟，即错误10次以上都保存15
        if (number < TOTAL_TIMING_NUMBER) {
            number++;
            setErrorNumber(number);
        }
        return number;
    }

    private static long getWaitInputPwdMinute(int number) {
        return (long) Math.pow(2, number - START_TIMING_NUMBER);
    }

    private long getDurationTime(long waitTime) {
        long oldTime = getErrorTime();
        if (oldTime != 0) {
            long durationTime = System.currentTimeMillis() - oldTime;
            if (durationTime < waitTime) {
                return durationTime;
            }
        }
        return -1;
    }

    private int getErrorNumber() {
        return Settings.Secure.getInt(mAppContext.getContentResolver(), INPUT_ERROR_NUMBER, 0);
    }

    private void setErrorNumber(int newNumber) {
        Settings.Secure.putInt(mAppContext.getContentResolver(), INPUT_ERROR_NUMBER, newNumber);
    }

    private long getErrorTime() {
        return Settings.Secure.getLong(mAppContext.getContentResolver(), INPUT_ERROR_TIME, 0);
    }

    private void setErrorTime(long newTime) {
        Settings.Secure.putLong(mAppContext.getContentResolver(), INPUT_ERROR_TIME, newTime);
    }

    @Override
    public boolean isFpInFreeze() {
        return isInFreeze();
    }

    @Override
    public int getFpFailRemainCount() {
        return getPwdFailRemainCount();
    }

    @Override
    public long getFpFreezeRemainingMillis() {
        return getFreezeRemainingMillis();
    }
}
