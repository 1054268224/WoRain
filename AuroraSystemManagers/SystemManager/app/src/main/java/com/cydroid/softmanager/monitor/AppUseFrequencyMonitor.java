package com.cydroid.softmanager.monitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.monitor.interfaces.IMonitorJob;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppManager;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

public class AppUseFrequencyMonitor implements IMonitorJob {

    private static final String YOUJU_FLAG = "youju";
    private static final String MONITOR_FLAG = "monitor";
    private static final int SORT_BY_FREQUENCY = 2;
    private NotificationManager mNotificationManager;
    private static final String TAG = "AppUseFrequencyMonitor";

    @Override
    public void setExecTime(int hour, int minutes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void execute(Context context) {
        // TODO Auto-generated method stub
        sendUseFrequencyMonitorNotification(context);
    }

    private void sendUseFrequencyMonitorNotification(Context context) {
        String app_monitor_summary = null;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(R.string.app_monitor_title);
        String app_monitor_title = context.getResources().getString(R.string.app_monitor_title);
        UninstallAppManager uninstallAppManager = UninstallAppManager.getInstance();
        uninstallAppManager.init(context);
        List<UninstallAppInfo> uninstallAppInfos = uninstallAppManager
            .getAllUninstallAppsByShowType(SORT_BY_FREQUENCY);
        Log.d(TAG,
                "get freq app list size=" + uninstallAppInfos.size());
        if (uninstallAppInfos.size() <= 0) {
            return;
        }

        if (uninstallAppInfos.size() >= 3) {
            app_monitor_summary = uninstallAppInfos.get(0).getTitle()
                    + "、" + uninstallAppInfos.get(1).getTitle() + "、"
                    + uninstallAppInfos.get(2).getTitle();
        } else if (uninstallAppInfos.size() == 2) {
            app_monitor_summary = uninstallAppInfos.get(0).getTitle()
                    + "、" + uninstallAppInfos.get(1).getTitle();
        } else if (uninstallAppInfos.size() == 1) {
            app_monitor_summary = uninstallAppInfos.get(0).getTitle();

        }
        Log.d(TAG, "show msg=" + app_monitor_summary);
        Intent intent = new Intent();
        intent.setClassName("com.cydroid.softmanager",
                "com.cydroid.softmanager.softmanager.UninstallAppActivity");
        intent.putExtra(MONITOR_FLAG, SORT_BY_FREQUENCY);
        intent.putExtra(YOUJU_FLAG, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(context).setContentTitle(app_monitor_title)
                .setContentText(app_monitor_summary).setContentIntent(contentIntent)
                // Gionee: mengjk  modify for notify TitleIcon And TitleText Color Change
                .setSmallIcon(R.drawable.notify).setColor(context.getResources().getColor(R.color.notify_icon_text_color)).setWhen(System.currentTimeMillis())
                .setTicker(app_monitor_title).setAutoCancel(true);

        Notification notification = builder.getNotification();

        mNotificationManager.notify(R.string.app_monitor_title, notification);
    }

}