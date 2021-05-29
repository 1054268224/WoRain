package com.cydroid.systemmanager.rubbishcleaner.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;

import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;

import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.service.ResidualRemoveService;
import com.cydroid.systemmanager.rubbishcleaner.service.RubbishScanService;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.PreferenceHelper;
import com.cydroid.systemmanager.utils.ServiceUtil;
import com.cydroid.systemmanager.utils.UnitUtil;
import com.cydroid.systemmanager.utils.UseNetState;

public class RubbishMonitorReceiver extends BroadcastReceiver {
    private static final String TAG = "CyeeRubbishCleaner/RubbishMonitorReceiver";

    private boolean DEBUG = true;
    //Gionee guoxt 2015-03-04 modified for CR01449811 begin
    public static final boolean gnVFflag = SystemProperties.get("ro.cy.custom").equals("VISUALFAN");
    //Gionee guoxt 2015-03-04 modified for CR01449811 end

    private static final HashSet<String> mHashSet = new HashSet<String>();
    private Handler mHandler = new Handler();
    private static final int POST_DELAY_TIME = 5000;
    //Gionee <xuwen><2015-07-28> add for CR01527111 begin
    private Context mContext;
    //Gionee <xuwen><2015-07-28> add for CR01527111 end

    PendingIntent pIntentAuto = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        //Gionee <xuwen><2015-07-28> add for CR01527111 begin
        mContext = context;
        //Gionee <xuwen><2015-07-28> add for CR01527111 end
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            // Gionee <houjie> <2016-05-12> add for CR01685134 begin
            if (isPermitBindService(context)) {
                startServiceOnTime(context);

            }
            //Chenyee guoxt modify for CSW1703VF-53 begin
            if (MsgConst.cy1703VF ) {
                int flag = Settings.System.getInt(mContext.getContentResolver(),"auto_cacheclean_timing",  0);
                if(flag ==1) {
                    startServiceCacheOn4Time(context);
                }
            }
            //Chenyee guoxt modify for CSW1703VF-53 end

        } else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
            String pkgName = splitPkgName(intent.getDataString());
            if (mHashSet.contains(pkgName)) {
                mHashSet.remove(pkgName);
            }
            if (PreferenceHelper.getBoolean(context, "apk_del_alert_key", false)) {
                Log.d(DEBUG, TAG, "RubbishMonitorReceiver deteck has apk installed, pkgName = "
                        + pkgName);
                startServiceWhenPkgAdded(context, pkgName);
            }

        } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && isPermitBindService(context)) {
            if (PreferenceHelper.getBoolean(context, "residual_del_alert_key", true)) {
                final String pkgName = splitPkgName(intent.getDataString());
                Log.d(DEBUG, TAG, "RubbishMonitorReceiver, apk is removed, pkgName = " + pkgName);
                mHashSet.add(pkgName);
                // 排除覆盖安装的情况
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (mHashSet.contains(pkgName)) {
                            mHashSet.remove(pkgName);
                            startServiceWhenPkgRemoved(context, pkgName);
                        }
                    }
                }, POST_DELAY_TIME);
            }
        //Chenyee guoxt modify for CSW1703VF-53 begin
        }else if(action.equals("com.cydroid.systemmanager.enable.autoclean4")){
            int flag = Settings.System.getInt(mContext.getContentResolver(),"auto_cacheclean_timing",  0);
            Log.d(DEBUG, TAG, "able: autoclean4 " +"flag:"+  flag);
            startServiceCacheOn4Time(context);
        }else if(action.equals("com.cydroid.systemmanager.disable.autoclean4")){
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(pIntentAuto != null) {
                alarmManager.cancel(pIntentAuto);
            }
            int flag = Settings.System.getInt(mContext.getContentResolver(),"auto_cacheclean_timing",  0);
            Log.d(DEBUG, TAG, "disable: autoclean4 " +"flag:"+  flag);
           //Chenyee guoxt modify for CSW1703VF-53 end
        }
    }

    //gionee yewq 2015-10-22 modify for CR01569699 end
    private String splitPkgName(String dataStr) {
        String pkgName = null;
        pkgName = dataStr.substring(8); // length of "package:"
        return pkgName;
    }

    private void startServiceWhenPkgAdded(Context context, String pkgName) {
        //Gionee <xuwen><2015-07-28> modify for CR01527111 begin
        Intent intent = new Intent(UnitUtil.APK_DELETE_ACTION);
        intent.putExtra(UnitUtil.APK_DELETE_PACKAGE_NAME_KEY, pkgName);
        mContext.sendBroadcast(intent);
        //Gionee <xuwen><2015-07-28> modify for CR01527111 end
    }

    private void startServiceWhenPkgRemoved(Context context, String pkgName) {
        Intent servIntent = new Intent(context, ResidualRemoveService.class);
        servIntent.putExtra("pkgName", pkgName);
        ServiceUtil.startForegroundService(context,servIntent);
    }

    private void startServiceOnTime(Context context) {
        Log.d(DEBUG, TAG, "set repeat Alarm to start RubbishScanService");
        Intent intent = new Intent(context, RubbishScanService.class);
        PendingIntent pIntent = PendingIntent.getService(context, 0, intent, 0);
        long bootTime = SystemClock.elapsedRealtime();
        long systemTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(systemTime);
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间超过了17点，则设为下一天
        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        long time = selectTime - systemTime;
        Log.d(DEBUG, TAG, "bootElapsedTime=" + bootTime + ", nextTime = " + time);
        bootTime += time;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, bootTime,
                AlarmManager.INTERVAL_DAY, pIntent);
    }


    //Chenyee guoxt modify for CSW1703VF-53 begin
    private void startServiceCacheOn4Time(Context context) {
        Log.d(DEBUG, TAG, "set repeat Alarm to start RubbishScanService");
        Intent intentCache = new Intent(context, RubbishScanService.class);
        intentCache.setAction("com.cydroid.systemanager.fourclock.clear");
         pIntentAuto = PendingIntent.getService(context, 0, intentCache, 0);
        long bootTime = SystemClock.elapsedRealtime();
        long systemTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(systemTime);
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间超过了17点，则设为下一天
        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        long time = selectTime - systemTime;
        Log.d(DEBUG, TAG, "startServiceCacheOn4Time bootElapsedTime=" + bootTime + ", nextTime = " + time);
        bootTime += time;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, bootTime,
                AlarmManager.INTERVAL_DAY, pIntentAuto);
    }
    //Chenyee guoxt modify for CSW1703VF-53 end

    //chenyee liu_shuang 20181029 modify for CSW1805A-1221 begin
    private boolean isPermitBindService(Context context) {
        Log.d(DEBUG, TAG, "state = " + !UseNetState.getState(context, false));
        return !UseNetState.getState(context, false);
    }
    //chenyee liu_shuang 20181029 modify for CSW1805A-1221 end

}
