// Gionee <liuyb> <2013-11-13> add for CR00952063 begin
package com.cydroid.softmanager.common;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.System;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cydroid.softmanager.update.UpdateService;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;
public class GnSystemDataUpdateReceiver extends BroadcastReceiver {
    private final static String TAG = "GnSystemDataUpdateReceiver";
    private final static String SYSTEM_DATE_UPDATE_FLAG = "system_data_update_flag";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.i(TAG, "wifi:" + wifiInfo.isConnected());

        if (wifiInfo.isConnected()) {
            // Gionee <liuyb> <2014-07-17> modify for CR01308515 begin
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean flag = preferences.getBoolean("is_first_utilization", true);
            if (!flag
                    && !getTodayTimeString().equals(
                            System.getString(context.getContentResolver(), SYSTEM_DATE_UPDATE_FLAG))) {
                Log.i(TAG, "GnSystemDataUpdate(context).startUpdate()");
                startUpdate(context);
                System.putString(context.getContentResolver(), SYSTEM_DATE_UPDATE_FLAG, getTodayTimeString());
            }
            // Gionee <liuyb> <2014-07-17> modify for CR01308515 end
        }
    }

    private static String getTodayTimeString() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + month + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
                + c.get(Calendar.HOUR_OF_DAY);
    }

    private void startUpdate(Context context) {
        Log.d(TAG, "startUpdate UpdateService");
        Intent updateIntent = new Intent(context, UpdateService.class);
        try {
            ServiceUtil.startForegroundService(context,updateIntent);
        } catch (Exception e) {
            Log.e(TAG, "startUpdate", e);
        }
    }
}
// Gionee <liuyb> <2013-11-13> add for CR00952063 end