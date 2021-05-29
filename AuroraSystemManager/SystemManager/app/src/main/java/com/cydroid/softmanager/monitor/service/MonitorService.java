// Gionee <liuyb> <2014-2-25> add for CR01083582 begin
package com.cydroid.softmanager.monitor.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemProperties;
import android.text.format.Formatter;

import java.util.HashMap;
import java.util.Iterator;

//Gionee <liuyb> <2014-6-28> add for CR01311273 begin
import com.cydroid.softmanager.common.Util;
//Gionee <liuyb> <2014-6-28> add for CR01311273 end
import com.cydroid.softmanager.monitor.AppUseFrequencyMonitor;
import com.cydroid.softmanager.monitor.RootMonitor;
import com.cydroid.softmanager.monitor.TrafficMonitor;
import com.cydroid.softmanager.monitor.interfaces.IMonitorJob;
import com.cydroid.softmanager.monitor.utils.CommonUtil;
//Gionee <xuhz> <2014-03-03> add for CR01090601 begin
import com.cydroid.softmanager.monitor.PowerUsageMonitor;
import com.cydroid.softmanager.utils.HelperUtils;
import com.wheatek.proxy.ui.HostTrafficMonitorMainActivity;
import com.cydroid.softmanager.utils.Log;

import android.content.SharedPreferences;
import cyee.preference.CyeePreferenceManager;

//Gionee <xuhz> <2014-03-03> add for CR01090601 end
// Gionee <liuyb> <2014-07-23> add for CR01316210 begin
import com.cydroid.softmanager.softmanager.model.ApplicationsInfo;
import com.chenyee.featureoption.ServiceUtil;
// Gionee <liuyb> <2014-07-23> add for CR01316210 end
public class MonitorService extends Service {
    private Context mContext;

    private static final String TAG = "MonitorService";

    private SharedPreferences mSharedPreferences;

    // Gionee <yangxinruo> <2016-4-20> add for CR01669840 begin
    private HashMap<String, PendingIntent> mPendingIntentMap;
    private AlarmManager mAlarmManager = null;
    // Gionee <yangxinruo> <2016-4-20> add for CR01669840 end
    @Override
    public void onCreate() {
        mContext = this;
        Log.i(TAG, "onCreate---->");
        // Gionee <xuhz> <2014-03-03> add for CR01090601 begin
        mSharedPreferences = CyeePreferenceManager.getDefaultSharedPreferences(mContext);
        // Gionee <xuhz> <2014-03-03> add for CR01090601 end
        // Gionee <yangxinruo> <2016-4-20> add for CR01669840 begin
        mPendingIntentMap = new HashMap<String, PendingIntent>();
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        // Gionee <yangxinruo> <2016-4-20> add for CR01669840 end
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            ServiceUtil.handleStartForegroundServices(this);
            String type = intent.getStringExtra("type");
            String jobkey = intent.getStringExtra(CommonUtil.JOB_KEY);
            if (jobkey != null) {
                Log.i(TAG, "onStartCommand----> jobkey = " + jobkey);
                if ("power".equals(jobkey)) {
                    // monitor pee week
                    // Gionee <xuhz> <2014-03-03> add for CR01090601 begin
                    if (mSharedPreferences.getBoolean("power_key", false)) {
                        IMonitorJob rootMonitor = new PowerUsageMonitor();
                        rootMonitor.execute(mContext);
                    }
                }
                // Gionee <xuhz> <2014-03-03> add for CR01090601 end
                if ("net".equals(jobkey)) {
                    // Gionee <jianghuan> <2014-03-08> add for CR01111459 begin
                    Log.d(TAG, "onStartCommand switch=" + mSharedPreferences.getBoolean("net_key", false));
                    if (mSharedPreferences.getBoolean("net_key", false)) {
                        IMonitorJob trafficMonitor = new TrafficMonitor();
                        trafficMonitor.execute(mContext);
                    }
                    // Gionee <jianghuan> <2014-03-08> add for CR01111459 end
                }
                if ("apps".equals(jobkey)) {
                    // Gionee <daizm> <2014-03-26> add for CR01140039 begin
                    if (mSharedPreferences.getBoolean("apps_key", false)) {
                        IMonitorJob appUseFrequencyMonitor = new AppUseFrequencyMonitor();
                        appUseFrequencyMonitor.execute(mContext);
                    }
                    // Gionee <daizm> <2014-03-26> add for CR01140039 begin
                }

                if ("root".equals(jobkey)) {
                    // monitor pee day
//                    if (mSharedPreferences.getBoolean("root_key", true)) {
//                        IMonitorJob rootMonitor = new RootMonitor();
//                        rootMonitor.execute(mContext);
//                    }
                    // Gionee <liuyb> <2014-07-23> add for CR01316210 begin
                    if (ApplicationsInfo.mPackageSizeInfoMap != null) {
                        ApplicationsInfo.mPackageSizeInfoMap.clear();
                        Log.i(TAG, "every day clean mPackageSizeInfoMap data");
                    }
                    // Gionee <liuyb> <2014-07-23> add for CR01316210 end
                }

                // Gionee <jianghuan> <2014-05-14> add for CR01255397 begin
                if ("netpackage".equals(jobkey)) {

                    HostTrafficMonitorMainActivity.TrafficPackageSettingNoti(mContext);

                }
                // Gionee <jianghuan> <2014-05-14> add for CR01255397 end
            }
            if (type != null) {
                Log.i(TAG, "onStartCommand----> type = " + type);
                if (CommonUtil.TYPE_BOOT_COMPLETED.equals(type)) {
                    // boot completed

                    // IMonitorJob permissionDbMonitor = new PermissionDBMonitor();
                    // permissionDbMonitor.execute(mContext);

                    // Gionee <liuyb> <2014-6-28> add for CR01311273 begin
                    Util.enableMmsComponent(mContext);
                    // Gionee <liuyb> <2014-6-28> add for CR01311273 end
                    Util.initRootNameList(mContext);
                    // Gionee <liuyb> <2014-10-13> add for CR01393511 begin
                    Util.modifySettingPermissionComponent(mContext);
                    // Gionee <liuyb> <2014-10-13> add for CR01393511 end
                }

                // Gionee <jianghuan> <2014-06-09> add for CR01275659 begin
                if (CommonUtil.TYPE_BOOT_COMPLETED.equals(type)) {
                    HelperUtils.writeAppProcessLimitOptions(mSharedPreferences);
                }
                // Gionee <jianghuan> <2014-06-09> add for CR01275659 end
                // Gionee <yangxinruo> <2016-4-20> add for CR01669840 begin
                if (CommonUtil.TYPE_TIME_CHANGED.equals(type)) {
                    setMonitorAlarmManagerForAll();
                }
                // Gionee <yangxinruo> <2016-4-20> add for CR01669840 end
            }
        }
        // Gionee <yangxinruo> <2016-4-20> delete for CR01669840 begin
        /*
        this.stopSelf();
        */
        // Gionee <yangxinruo> <2016-4-20> delete for CR01669840 end
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy---->");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind---->");
        // TODO Auto-generated method stub
        return null;
    }

    // Gionee <yangxinruo> <2016-4-20> add for CR01669840 begin
    @SuppressLint("NewApi")
    private void setMonitorAlarmManagerForAll() {
        Log.d(TAG, "setMonitorAlarmManagerForAll count(mPendingIntentMap)  -->" + mPendingIntentMap.size());
        Iterator<String> iterator = CommonUtil.mJobMap.keySet().iterator();
        while (iterator != null && iterator.hasNext()) {
            String jobKey = iterator.next();
            Log.d(TAG, "set setMonitorAlarmManagerForAll start-----> " + jobKey);
            PendingIntent monitorAlarmPendingIntent = mPendingIntentMap.get(jobKey);
            if (monitorAlarmPendingIntent == null) {
                Log.d(TAG, "set setMonitorAlarmManagerForAll create intent-----> " + jobKey);
                Intent timeoutIntent = new Intent();
                timeoutIntent.putExtra(CommonUtil.JOB_KEY, jobKey);
                timeoutIntent.setAction(CommonUtil.ACTION_MONITOR_ALARM + "_" + jobKey.toUpperCase());
                monitorAlarmPendingIntent = PendingIntent.getBroadcast(mContext, 0, timeoutIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);

                mPendingIntentMap.put(jobKey, monitorAlarmPendingIntent);
            } else {
                Log.d(TAG, "set setMonitorAlarmManagerForAll cancel old alarm intent---->" + jobKey);
                mAlarmManager.cancel(monitorAlarmPendingIntent);
            }
            // Gionee <yangxinruo> <2015-09-15> modify for CR01551835 begin
            if (Build.VERSION.SDK_INT >= 19) {
                Log.d(TAG, "set setMonitorAlarmManagerForAll start setexact----->" + jobKey);
                mAlarmManager.setExact(AlarmManager.RTC,
                        CommonUtil.getNowToNextMonitorInfoTime(CommonUtil.mJobMap.get(jobKey)),
                        mPendingIntentMap.get(jobKey));
            } else {
                Log.d(TAG, "set setMonitorAlarmManagerForAll start set----->" + jobKey);
                mAlarmManager.set(AlarmManager.RTC,
                        CommonUtil.getNowToNextMonitorInfoTime(CommonUtil.mJobMap.get(jobKey)),
                        mPendingIntentMap.get(jobKey));
            }
            // Gionee <yangxinruo> <2015-09-15> modify for CR01551835 end
        }
    }
    // Gionee <yangxinruo> <2016-4-20> add for CR01669840 end
}
// Gionee <liuyb> <2014-2-25> add for CR01083582 end
