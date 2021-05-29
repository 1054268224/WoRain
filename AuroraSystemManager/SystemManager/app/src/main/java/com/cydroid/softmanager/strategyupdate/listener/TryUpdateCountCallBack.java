package com.cydroid.softmanager.strategyupdate.listener;

import java.util.Calendar;

import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;
import com.cydroid.softmanager.update.UpdateService;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.provider.Settings.System;
/**
 * Created by mengjk on 17-5-16.
 */
public class TryUpdateCountCallBack implements StrategyUpdateCallBack {

    private static final String TAG = TryUpdateCountCallBack.class.getSimpleName();
    private static final String SYSTEM_DATE_TRY_UPDATE_COUNT = "system_data_try_update_count";
    private static final int SYSTEM_DATE_TRY_UPDATE_MAX_COUNT = 3;

    @Override
    public void notifyUpdateStart(Context context) {
    }

    @Override
    public void notifyUpdateCompleted(Context context) {
        Log.d(TAG, "tryUpdateCount update completed so reinit trycount and set update time");
        System.putString(context.getContentResolver(), UpdateService.SYSTEM_DATE_UPDATE_TIME,
                getTodayTimeString());
        System.putInt(context.getContentResolver(), SYSTEM_DATE_TRY_UPDATE_COUNT, 0);
    }

    @Override
    public void notifyStrategyParseCompleted(Context context, String type, IStrategy strategy) {
    }

    @Override
    public void notifyUpdateFail(final Context context) {
        Log.d(TAG, "notifyUpdateFail");
        int tryUpdateCount = System.getInt(context.getContentResolver(), SYSTEM_DATE_TRY_UPDATE_COUNT, 0);
        tryUpdateCount++;
        Log.d(TAG, "tryUpdateCount--" + tryUpdateCount);
        if (tryUpdateCount > SYSTEM_DATE_TRY_UPDATE_MAX_COUNT) {
            Log.d(TAG, "tryUpdateCount over limit count, can not update this day-" + tryUpdateCount);
            System.putString(context.getContentResolver(), UpdateService.SYSTEM_DATE_UPDATE_TIME,
                    getTodayTimeString());
            System.putInt(context.getContentResolver(), SYSTEM_DATE_TRY_UPDATE_COUNT, 0);
        } else {
            Log.d(TAG, "can still update --" + tryUpdateCount);
            System.putString(context.getContentResolver(), UpdateService.SYSTEM_DATE_UPDATE_TIME, "");
            System.putInt(context.getContentResolver(), SYSTEM_DATE_TRY_UPDATE_COUNT, tryUpdateCount);
        }
    }

    private static String getTodayTimeString() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + month + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
                + c.get(Calendar.HOUR_OF_DAY);
    }

}
