/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.update;

import java.util.Calendar;

// import com.cydroid.softmanager.strategyupdate.WhiteListUpdateController;
// import com.cydroid.softmanager.strategyupdate.listener.StrategyUpdateCallBack;
// import com.cydroid.softmanager.strategyupdate.listener.StrategyUpdateCallBackManager;
// import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;
import com.cydroid.softmanager.utils.Log;
import android.app.job.JobParameters;
import android.app.job.JobService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.System;

@SuppressLint("NewApi")
public class UpdateService extends JobService {
    private static final String TAG = "UpdateService";
    public static final int JOB_ID = 1;
    public static final String SYSTEM_DATE_UPDATE_TIME = "system_data_update_time";

    private Context mContext;
    private JobParameters mJobParams;
    private JobService mJobService;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        mJobService = this;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "start UpdateService job. (" + params.getJobId() + ")");
        mJobParams = params;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (isNeedUpdate(preferences)) {
            Log.i(TAG, "GnSystemDataUpdate(context).startUpdate()");
            System.putString(mContext.getContentResolver(), SYSTEM_DATE_UPDATE_TIME, getTodayTimeString());
            // WhiteListUpdateController updateController = new WhiteListUpdateController(mContext);
            // updateController.registerWhiteListUpdateStrategyCallBack(new UpdateCompletedCallBack());
            // updateController.startUpdate();
            return true;
        }
        return false;
    }

    private boolean isNeedUpdate(SharedPreferences preferences) {
        boolean isNetworkUploadEnabled = !preferences.getBoolean("is_first_utilization", true);
        String lastUpdateStr = System.getString(mContext.getContentResolver(), SYSTEM_DATE_UPDATE_TIME);
        boolean isNotYetUploadToday = !getTodayTimeString().equals(lastUpdateStr);
        Log.d(TAG, "isNetworkUploadEnabled:" + isNetworkUploadEnabled + " isNotYetUploadToday:"
                + isNotYetUploadToday + " lastUpdate at:" + lastUpdateStr);
        return isNetworkUploadEnabled && isNotYetUploadToday;
    }

    private static String getTodayTimeString() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + month + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
                + c.get(Calendar.HOUR_OF_DAY);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "UpdateService job finished(" + params.getJobId() + ")!");
        return false;
    }

}
