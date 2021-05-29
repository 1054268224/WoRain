//Gionee <xuhz> <2014-03-03> add for CR01090601 begin
package com.cydroid.softmanager.monitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.cydroid.softmanager.monitor.interfaces.IMonitorJob;
import com.cydroid.softmanager.monitor.utils.PowerUsageUtil;
import com.cydroid.softmanager.powersaver.activities.BatteryUseRankActivity;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.R;
//import com.gionee.youju.statistics.sdk.YouJuAgent;

public class PowerUsageMonitor implements IMonitorJob {
    private Context mContext;
    private final static int SHOW_TAOST = 1000;
    private String[] mAppNames;

    public Handler mHandler;

    private static final String TAG="PowerUsageMonitor";
    private final static int TOP_CONSUME_APP_NUMBER = 3;

    @Override
    public void setExecTime(int hour, int minutes) {
        // TODO Auto-generated method stub
    }

    @Override
    public void execute(Context context) {
        Log.d(TAG,"enter execute");
        mContext = context;

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (mAppNames == null || mAppNames.length == 0) {
                    return;
                }

                StringBuilder strName = new StringBuilder();
                // Gionee <liuyb> <2014-6-24> modify for CR01298040 begin
                for (int i = 0; i < mAppNames.length; i++) {
                    if (mAppNames[i] != null) {
                        strName.append(mAppNames[i]);
                        if (i != mAppNames.length - 1) {
                            strName.append("、");
                        }
                    }
                }
                if (strName.length() < 1) {
                    return;
                }
                // Gionee <liuyb> <2014-6-24> modify for CR01298040 end
                CharSequence title = mContext.getText(R.string.power_monitor_title);
                String text = mContext.getResources().getString(R.string.power_monitor_message,
                        strName.toString());

                NotificationManager manager = (NotificationManager) mContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                Intent intent = new Intent();
                intent.setClass(mContext, BatteryUseRankActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                Notification.Builder builder = new Notification.Builder(mContext).setContentTitle(title)
                        .setContentText(text).setContentIntent(pendingIntent).setSmallIcon(R.drawable.notify)
                        // Gionee: mengjk  modify for notify TitleIcon And TitleText Color Change
                        .setColor(mContext.getResources().getColor(R.color.notify_icon_text_color)).setWhen(System.currentTimeMillis()).setTicker(title)
                        .setAutoCancel(true);

                Notification notification = builder.getNotification();

                manager.notify(R.string.power_monitor_title, notification);

                //YouJuAgent.onEvent(mContext, "Noti_PowerRank");
            }
        };

        new MyThread().start();

    }

    class MyThread extends Thread {
        @Override
        public void run() {
            PowerUsageUtil powerUsage = new PowerUsageUtil(mContext);
            powerUsage.updateAppUsage();

            mAppNames = powerUsage.getPowerUsageAppName(TOP_CONSUME_APP_NUMBER);

            Message msg = new Message();
            msg.what = SHOW_TAOST;
            mHandler.sendMessage(msg);
        }
    }
}
// Gionee <xuhz> <2014-03-03> add for CR01090601 end