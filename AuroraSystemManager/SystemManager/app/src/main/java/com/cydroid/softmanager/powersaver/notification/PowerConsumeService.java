/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用检测与提醒服务
 *
 * Revised Date: 2017-02-05
 */
package com.cydroid.softmanager.powersaver.notification;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.memoryclean.filterrules.ApplicationInfoManager;
import com.cydroid.softmanager.powersaver.notification.strategy.PowerConsumeAppMonitorStrategy;
import com.cydroid.softmanager.powersaver.notification.strategy.SimplePowerConsumeAppMonitorFactory;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import com.chenyee.featureoption.ServiceUtil;
public class PowerConsumeService extends Service implements PowerConsumeAppMonitorStrategy.IChangeCallback {
    private static final String TAG = "PowerConsumeService";

    private static final String[] MONITOR_NAME_LIST = {
            SimplePowerConsumeAppMonitorFactory.MONITOR_NAME_HIGH_VOLTAGE,
            SimplePowerConsumeAppMonitorFactory.MONITOR_NAME_LONG_WAKELOCK};

    public static final String ACTION_START_ALARM = "com.cydroid.softmanager.powersaver.notification.ACTION_START_ALARM";
    public static final String ACTION_STOP_ALARM = "com.cydroid.softmanager.powersaver.notification.ACTION_STOP_ALARM";
    private static final String ACTION_MONITOR_UPDATED = "com.cydroid.softmanager.powersaver.notification.ACTION_UPDATE_ALARM";

    public static final String ACTION_CLEAN_PROCESSES = "com.cydroid.softmanager.powersaver.notification.ACTION_KILL_PROCESSES";
    public static final String ACTION_IGNORE_PROCESSES = "com.cydroid.softmanager.powersaver.notification.ACTION_IGNORE_PROCESSES";

    private Context mContext;
    private final Handler mHandler = new Handler();
    private AlarmManager mAlarmManager;
    private PowerConsumeAppManager mPowerConsumeAppManager;
    private PowerConsumeAppMonitorStrategy mPowerConsumeAppMonitorStrategy;
    private PendingIntent mUpdatePendingIntent;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate---->");
        mContext = this;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mPowerConsumeAppManager = PowerConsumeAppManager.getInstance(mContext);
        mPowerConsumeAppMonitorStrategy = new PowerConsumeAppMonitorStrategy(mContext);

        Intent serviceIntent = new Intent(mContext, PowerConsumeService.class);
        serviceIntent.setAction(ACTION_MONITOR_UPDATED);
        mUpdatePendingIntent = PendingIntent.getService(mContext, 0, serviceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY;
        }
        ServiceUtil.handleStartForegroundServices(PowerConsumeService.this);
        String actionStr = intent.getAction();
        Log.d(TAG, "onStartCommand-->" + actionStr);
        if (actionStr == null || actionStr.isEmpty()) {
            return Service.START_STICKY;
        }
        if (actionStr.equals(ACTION_START_ALARM)) {
            mPowerConsumeAppMonitorStrategy.setCallback(this);
            startMonitors(MONITOR_NAME_LIST);
        } else if (actionStr.equals(ACTION_STOP_ALARM)) {
            mPowerConsumeAppMonitorStrategy.setCallback(null);
            cancelPendingUpdate();
            stopMonitors(MONITOR_NAME_LIST);
        } else if (actionStr.equals(ACTION_MONITOR_UPDATED)) {
            // async execute check result at onMonitorDataUpdate
            mPowerConsumeAppMonitorStrategy.updateData();
            setNextUpdate();
        } else if (actionStr.equals(ACTION_IGNORE_PROCESSES)) {
            ArrayList<String> alertPackagesList = intent.getStringArrayListExtra("packages");
            if (alertPackagesList == null || alertPackagesList.isEmpty()) {
                return Service.START_STICKY;
            }
            ignorePackages(alertPackagesList);
            cancelNotification(intent.getStringExtra("alert_type"));
        } else if (actionStr.equals(ACTION_CLEAN_PROCESSES)) {
            ArrayList<String> alertPackagesList = intent.getStringArrayListExtra("packages");
            if (alertPackagesList == null || alertPackagesList.isEmpty()) {
                return Service.START_STICKY;
            }
            killPackages(alertPackagesList);
            cancelNotification(intent.getStringExtra("alert_type"));
        }
        return Service.START_STICKY;
    }

    private void cancelNotification(String alertType) {
        if (alertType != null) {
            Log.d(TAG, "cancel notification for " + alertType);
        }
        NotificationManager notiManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager.cancel(R.string.power_monitor_title);
    }

    private void ignorePackages(ArrayList<String> alertPackagesList) {
        PowerConsumeAppManager powerConsumeAppManager = PowerConsumeAppManager.getInstance(mContext);
        for (String packageName : alertPackagesList) {
            Log.d(TAG, "ignore this app " + packageName);
            powerConsumeAppManager.addUserIgnoredApp(packageName);
        }
    }

    private int killPackages(ArrayList<String> alertPackagesList) {
        int count = 0;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (String packageName : alertPackagesList) {
            Log.d(TAG, "get action to kill packages ----->" + packageName);
            am.forceStopPackage(packageName);
            count++;
            sendKillPackageToast(packageName);
        }
        return count;
    }

    private void sendKillPackageToast(String pkgName) {
        String appName = "";
        ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(getContentResolver());
        if (!encryptionsApps.contains(pkgName)) {
            try {
                PackageManager pm = mContext.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
                appName = pm.getApplicationLabel(appInfo).toString();
            } catch (Exception e) {
                Log.d(TAG, "PackageManager exception" + e);
                appName = "";
            }
            if (appName.isEmpty()) {
                return;
            }
        } else {
            appName = getResources().getString(R.string.encryptions_app_fake_name);
        }
        // Gionee <yangxinruo> <2015-12-1> modify for CR01602138 begin
        Toast.makeText(mContext,
                mContext.getResources().getString(R.string.power_consume_processed_kill) + appName,
                Toast.LENGTH_SHORT).show();
        // Gionee <yangxinruo> <2015-12-1> modify for CR01602138 end
    }

    private void startMonitors(String[] monitorList) {
        if (mPowerConsumeAppMonitorStrategy == null) {
            mPowerConsumeAppMonitorStrategy = new PowerConsumeAppMonitorStrategy(mContext);
        }
        mPowerConsumeAppMonitorStrategy.addPowerConsumeAppMonitors(monitorList);
    }

    private void stopMonitors(String[] monitorList) {
        if (mPowerConsumeAppMonitorStrategy == null) {
            Log.d(TAG, "strategy not exist! stop service");
            stopSelf();
        }
        mPowerConsumeAppMonitorStrategy.removePowerConsumeAppMonitor(monitorList);

        if (mPowerConsumeAppMonitorStrategy.getMonitorsCount() <= 0) {
            Log.d(TAG, "All monitor in strategy stopped! stop service");
            stopSelf();
        }
    }

    private void setNextUpdate() {
        cancelPendingUpdate();
        long nextUpdateTime = mPowerConsumeAppMonitorStrategy.getNextUpdateElapsedTime();
        if (nextUpdateTime > 0) {
            mAlarmManager.setExact(AlarmManager.ELAPSED_REALTIME, nextUpdateTime, mUpdatePendingIntent);
        } else {
            Log.d(TAG, "invalid update time from strategy");
        }
    }

    private void cancelPendingUpdate() {
        mAlarmManager.cancel(mUpdatePendingIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy-->");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // implements PowerConsumeAppMonitorStrategy.IChangeCallback BEGIN
    @Override
    public void onChange() {
        setNextUpdate();
    }

    @Override
    public void onMonitorDataUpdate(final String monitorName,
                                    final HashMap<String, Double> overThresholdResult) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updatePowerConsumeAppData(monitorName, overThresholdResult);
                if (!overThresholdResult.isEmpty()) {
                    showPowerConsumeAppNotifaction(monitorName, overThresholdResult);
                }
            }
        });
    }
    // implements PowerConsumeAppMonitorStrategy.IChangeCallback END

    protected void updatePowerConsumeAppData(String monitorName,
                                             HashMap<String, Double> overThresholdResult) {
        mPowerConsumeAppManager.putPowerConsumeAppData(monitorName, overThresholdResult);
    }

    protected void showPowerConsumeAppNotifaction(String monitorName,
                                                  HashMap<String, Double> overThresholdResult) {
        String topScorePackage = getTopScoreAvailApp(overThresholdResult);
        //postTopScorePkgToYouju(monitorName, topScorePackage);
        NotificationManager notiManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = makePowerConsumeNotification(notiManager, monitorName, topScorePackage);
        if (notification != null) {
            // TODO separately or one noti for different monitors?
            notiManager.notify(R.string.power_monitor_title, notification);
//            notiManager.notify(monitorName.hashCode(), notification);
        }
    }

    private Notification makePowerConsumeNotification(NotificationManager notiManager, String monitorName,
                                                      String topScorePackage) {
        ArrayList<String> alertPackagesList = new ArrayList<String>();
        alertPackagesList.add(topScorePackage);
        Intent ignoreIntent = new Intent();
        ignoreIntent.setClass(mContext, PowerConsumeService.class);
        ignoreIntent.setAction(ACTION_IGNORE_PROCESSES);
        ignoreIntent.putStringArrayListExtra("packages", alertPackagesList);
        ignoreIntent.putExtra("alert_type", monitorName);
        PendingIntent ignorePendingIntent = PendingIntent.getService(mContext,
                R.string.power_consume_notification_ignore, ignoreIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent cleanIntent = new Intent();
        cleanIntent.setClass(mContext, PowerConsumeService.class);
        cleanIntent.setAction(ACTION_CLEAN_PROCESSES);
        cleanIntent.putStringArrayListExtra("packages", alertPackagesList);
        cleanIntent.putExtra("alert_type", monitorName);
        PendingIntent killPendingIntent = PendingIntent.getService(mContext,
                R.string.power_consume_notification_kill, cleanIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String appName = "";
        ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(getContentResolver());
        if (!encryptionsApps.contains(topScorePackage)) {
            try {
                ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(topScorePackage,
                        PackageManager.GET_META_DATA);
                appName = mContext.getPackageManager().getApplicationLabel(ai).toString();
            } catch (Exception e) {
                Log.d(TAG, "PackageManager exception" + e);
                return null;
            }
        } else {
            appName = getResources().getString(R.string.encryptions_app_fake_name);
        }
        Intent mainContentIntent = new Intent();
        mainContentIntent.setClass(mContext, BackgroundAppListActivity.class);
        mainContentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainContentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainContentIntent.putStringArrayListExtra("alert_packages", alertPackagesList);
        mainContentIntent.putExtra("alert_type", monitorName);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(mContext,
                R.string.power_consume_notification_title, mainContentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Resources rm = mContext.getResources();
        String title = String.format(getMonitorTitleString(monitorName), appName);
        Log.d(TAG, "notifaction msg:" + title);
        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentTitle(rm.getString(R.string.power_consume_notification_title))
                .setContentText(Html.fromHtml(title)).setContentIntent(clickPendingIntent)
                // Gionee: mengjk  modify for notify TitleIcon And TitleText Color Change
                .setSmallIcon(R.drawable.notify).setColor(mContext.getResources().getColor(R.color.notify_icon_text_color)).setWhen(System.currentTimeMillis())
                .setTicker(rm.getString(R.string.power_consume_notification_title))
                .setPriority(Notification.PRIORITY_MAX).setAutoCancel(true);
        builder.addAction(0, mContext.getResources().getString(R.string.power_consume_notification_kill),
                killPendingIntent);
        builder.addAction(0, mContext.getResources().getString(R.string.power_consume_notification_ignore),
                ignorePendingIntent);
        Notification notification = builder.build();
        return notification;
    }

    private String getMonitorTitleString(String monitorName) {
        return mContext
                .getString(SimplePowerConsumeAppMonitorFactory.getNotificationTitleStringId(monitorName));
    }

/*
    private void postTopScorePkgToYouju(String monitorName, String topScorePackage) {
        Map<String, Object> youJuMap = new HashMap<String, Object>();
        youJuMap.put("pkgname", topScorePackage);
        youJuMap.put("pkgversion",
                HelperUtils.getPackageVersion(mContext.getPackageManager(), topScorePackage));
        if (!SimplePowerConsumeAppMonitorFactory.getYoujuEventNameString(monitorName).isEmpty()) {
            YouJuManager.onEvent(mContext,
                    SimplePowerConsumeAppMonitorFactory.getYoujuEventNameString(monitorName), null, youJuMap);
        }
    }
    */

    private String getTopScoreAvailApp(HashMap<String, Double> overThresholdResult) {
        ApplicationInfoManager applicationInfoManager = ApplicationInfoManager.getInstance(mContext);
        ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(mContext.getContentResolver());
        double maxScore = -1d;
        String res = "";
        for (Entry<String, Double> entry : overThresholdResult.entrySet()) {
            String pkgName = entry.getKey();
            Double score = entry.getValue();
            if (mPowerConsumeAppManager.isIgnoredApp(pkgName)) {
                Log.d(TAG, "pkg " + pkgName + " in ignore list,do not report");
                continue;
            }
            if (applicationInfoManager.isSystemApp(pkgName)) {
                Log.d(TAG, "pkg " + pkgName + " is rom app,do not report");
                continue;
            }
            if (encryptionsApps.contains(pkgName)) {
                Log.d(TAG, "pkg " + pkgName + " is encryption app,do not report");
                continue;
            }
            if (score > maxScore) {
                maxScore = score;
                res = pkgName;
            }
        }
        return res;
    }

}
