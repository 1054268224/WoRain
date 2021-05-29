/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Html;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.softmanager.AutoStartMrgActivity;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import com.cydroid.softmanager.common.Consts;

public class AutoBootForbiddenMsgManager {
    private static final String TAG = "AutoBootForbiddenMsgManager";

    private static final int FORBIDDEN_MSG = 0;
    private static final int AUTOBOOT_FORBIDDEN_NOTIFICATION_ID = 0x1234;

    private static final String AUTOBOOT_FORBIDDEN_NOTIFICATION_PREFERENCES_NAME =
            "com.cydroid.softmanager.softmanager.autoboot.forbidden_noti";
    private static final String AUTOBOOT_FORBIDDEN_NOTI_TURNOFF_KEY = "forbiddenNotiTurnOff";

    private static AutoBootForbiddenMsgManager sInstance;

    private Context mContext;
    private boolean mFirstTime = true;
    private boolean mNotiTurnOff = false;
    private BackgroundHandler mBackgroundHandler;
    private final HandlerThread mMsgThread = new HandlerThread("AutoBootForbiddenMsgManager");

    private class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            Log.d(TAG, "BackgroundHandler msg.what:" + message.what);
            switch (message.what) {
                case FORBIDDEN_MSG:
                    if (!Consts.cyBAFlag){
                        String packageName = message.obj.toString();
                        showAutoBootForbiddenNotification(packageName);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static synchronized AutoBootForbiddenMsgManager getInstance() {
        if (null == sInstance) {
            sInstance = new AutoBootForbiddenMsgManager();
        }
        return sInstance;
    }

    private AutoBootForbiddenMsgManager() {
    }

    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
        }
    }

    private void initFirstTime(Context context) {
        Log.d(TAG, "initFirstTime");
        mFirstTime = false;
        mContext = context.getApplicationContext();
        mNotiTurnOff = getForbiddenMsgTurnOffSharedPreferences();
        mMsgThread.start();
        mBackgroundHandler = new BackgroundHandler(mMsgThread.getLooper());
    }

    private boolean getForbiddenMsgTurnOffSharedPreferences() {
        SharedPreferences sp = mContext.getSharedPreferences(
                AUTOBOOT_FORBIDDEN_NOTIFICATION_PREFERENCES_NAME,
                Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(AUTOBOOT_FORBIDDEN_NOTI_TURNOFF_KEY, false);
    }

    private void showAutoBootForbiddenNotification(String packageName) {
        Log.d(TAG, "createForbiddenNotification packageName:" + packageName);
        NotificationManager notiManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = makeAutoBootForbiddenNotification(notiManager, packageName);
        if (notification != null) {
            notiManager.notify(AUTOBOOT_FORBIDDEN_NOTIFICATION_ID, notification);
        }
    }

    private Notification makeAutoBootForbiddenNotification(
            NotificationManager notiManager, String packageName) {
        Resources res = mContext.getResources();
        Intent turnOffIntent = new Intent();
        turnOffIntent.setClass(mContext, AutoBootForbiddenMsgReceiver.class);
        turnOffIntent.setAction(AutoBootForbiddenMsgReceiver.ACTION_TURN_OFF_NOTIFICATION);
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(mContext,
                0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);

        String appName = "";
        ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(
                mContext.getContentResolver());
        if (!encryptionsApps.contains(packageName)) {
            try {
                ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(packageName,
                        PackageManager.GET_META_DATA);
                appName = mContext.getPackageManager().getApplicationLabel(ai).toString();
            } catch (Exception e) {
                Log.d(TAG, "PackageManager exception" + e);
                return null;
            }
        } else {
            appName = res.getString(R.string.encryptions_app_fake_name);
        }

        String msgFormat = res.getString(R.string.autoboot_forbidden_notify_content);
        String msg = String.format(msgFormat, appName);
        Log.d(TAG, "notifaction msg:" + msg);
        BigTextStyle bigStyle = new BigTextStyle();
        bigStyle.setBigContentTitle(res.getString(R.string.autoboot_forbidden_notify_title));
        bigStyle.bigText(msg);

        Intent clickIntent = new Intent(mContext, AutoStartMrgActivity.class);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(mContext, 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager) mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("2",
                "autostart", NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(mContext,"2")
                .setStyle(bigStyle)
                .setContentTitle(res.getString(R.string.autoboot_forbidden_notify_title))
                .setContentText(Html.fromHtml(msg))
                .setContentIntent(clickPendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                // Gionee: mengjk  modify for notify TitleIcon And TitleText Color Change
                .setSmallIcon(R.drawable.notify)
                .setColor(res.getColor(R.color.notify_icon_text_color))
                .setTicker(res.getString(R.string.autoboot_forbidden_notify_title))
                .setAutoCancel(true);
        builder.addAction(0, res.getString(R.string.autoboot_forbidden_notify_turn_off),
                turnOffPendingIntent);
        Notification notification = builder.build();
        return notification;
    }

    public void processAutoBootForbiddenMsg(String packageName) {
        Log.d(TAG, "processAutoBootForbiddenMsg packageName:" + packageName);

         /*guoxt 2018-03-13 modify for SW17W08IP-137 begin */
         //Chenyee <CY_TC_Req> <fujiabing> <20180424> modify for CSW1703TC-71 beign
        if(Consts.gnIPFlag || Consts.gnTCflag || Consts.cyBAFlag){
            mNotiTurnOff = true;
        }
        //Chenyee <CY_TC_Req> <fujiabing> <20180424> modify for CSW1703TC-71 end
        /*guoxt 2018-03-13 modify for SW17W08IP-137 end */
        if (mNotiTurnOff || null == packageName || packageName.isEmpty()) {
            Log.d(TAG, "processAutoBootForbiddenMsg turn off");
            return;
        }
        Message message = mBackgroundHandler.obtainMessage(FORBIDDEN_MSG, packageName);
        mBackgroundHandler.sendMessage(message);
    }

    public void closeAutoBootForbiddenMsg() {
        mNotiTurnOff = true;
        setForbiddenMsgTurnOffSharedPreferences(true);
    }

    private void setForbiddenMsgTurnOffSharedPreferences(boolean turnOff) {
        SharedPreferences shardPreferences = mContext.getSharedPreferences(
                AUTOBOOT_FORBIDDEN_NOTIFICATION_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = shardPreferences.edit();
        editor.putBoolean(AUTOBOOT_FORBIDDEN_NOTI_TURNOFF_KEY, turnOff).apply();
    }

    public void resetAutoBootForbiddenMsgTurnOff() {
        mNotiTurnOff = false;
        setForbiddenMsgTurnOffSharedPreferences(false);
    }

    public void cancelNotification() {
        NotificationManager notiManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager.cancel(AUTOBOOT_FORBIDDEN_NOTIFICATION_ID);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mMsgThread.quit();
    }
}
