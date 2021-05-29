/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: mengdw
 *
 * Date: 2016-12-06 for CR01775579
 */
package com.cydroid.softmanager.trafficassistant.lockscreenused.controler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;

import com.cydroid.softmanager.monitor.utils.TrafficMonitorUtil;
import com.cydroid.softmanager.powersaver.activities.BatteryUseRankActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.controler.TrafficSettingControler;
import com.cydroid.softmanager.trafficassistant.lockscreenused.LockScreenTrafficDetailActivity;
import com.cydroid.softmanager.trafficassistant.net.AppItem;
import com.cydroid.softmanager.trafficassistant.NotificationController;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.google.android.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LockScreenTrafficUsedController {
    private static final String TAG = "LockScreenTrafficUsedController";
    // mengdw <2017-04-22> add for 103262 begin
    private static final int MSG_QUERY_TAFFIC_APPS_END = 0;
    private static final int ACTION_SCREEN_OFF = 0;
    private static final int ACTION_PRESENT = 1;
    // mengdw <2017-04-22> add for 103262 end
    private static LockScreenTrafficUsedController mInstance;
    
    private final Context mContext;
    private final long mBeginTime;
    private final TrafficMonitorUtil mTrafficMonitorUtil;
    private final ArrayList<AppItem> mLockApps = Lists.newArrayList();
    private final ArrayList<AppItem> mUnLockApps = Lists.newArrayList();
    private ScreenChangeReceive mScreenChangeReceive;
    private final TrafficSettingControler mTrafficSettingControler;
    // mengdw <2017-04-22> add for 103262 begin
    private final LockScreenTrafficHandler mLockScreenTrafficHandler;
    // mengdw <2017-04-22> add for 103262 end

    public static LockScreenTrafficUsedController getInstance(Context context) {
        synchronized (LockScreenTrafficUsedController.class) {
            if (null == mInstance) {
                mInstance = new LockScreenTrafficUsedController(context.getApplicationContext());
            }
        }
        return mInstance;
    }
    
    public void finalizeLockScreenTrafficUsedController() {
        if (null != mScreenChangeReceive) {
            mContext.unregisterReceiver(mScreenChangeReceive);
        }
        if (null != mLockScreenTrafficHandler
                && mLockScreenTrafficHandler.hasMessages(MSG_QUERY_TAFFIC_APPS_END)) {
            mLockScreenTrafficHandler.removeMessages(MSG_QUERY_TAFFIC_APPS_END);
        }
    }
    
    public long getLockScreenTotalTraffic() {
        long result = 0;
        ArrayList<AppItem> apps = getLockScreenTrafficApps();
        if(apps != null && apps.size() > 0) {
            for(int i = 0; i < apps.size(); i++) {
                result = result + apps.get(i).total;
            }
        }
        return result;
    }
    
    public boolean isReachLimit() {
        long usedTotal = getLockScreenTotalTraffic();
        long limit = mTrafficSettingControler.getLockScreenRemindLimit();
        int selectIndex = mTrafficSettingControler.getLockScreenRemindSetting();
        Log.d(TAG, "isReachLimit usedTotal=" + usedTotal + " limit=" + limit + " selectIndex=" + selectIndex);
        return usedTotal > limit;

    }
    
    public synchronized ArrayList<AppItem> getLockScreenTrafficApps() {
        ArrayList<AppItem> apps = Lists.newArrayList();
        for(int i = 0; i < mUnLockApps.size(); i++) {
            int unlockUid = mUnLockApps.get(i).key;
            long unlockTraffic = mUnLockApps.get(i).total;
            for(int j = 0; j < mLockApps.size(); j++) {
                int lockUid = mLockApps.get(j).key;
                long lockTraffic = mLockApps.get(j).total;
                if(lockUid == unlockUid && unlockTraffic > lockTraffic) {
                    AppItem item = new AppItem(unlockUid);
                    item.total = unlockTraffic - lockTraffic;
                    apps.add(item);
                }
            }
        }
        Collections.sort(apps, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem lhs, AppItem rhs) {
                return Long.compare(rhs.total, lhs.total);
            }
        });
        return apps;
    }
    
    // mengdw <2017-04-22> modify for 103262 begin
    public synchronized void updateLockScreenApps(ArrayList<AppItem> apps) {
        mLockApps.clear();
        mLockApps.addAll(apps);
    }

    public synchronized void updateUnlockScreenApps(ArrayList<AppItem> apps) {
        mUnLockApps.clear();
        mUnLockApps.addAll(apps);
    }
    // mengdw <2017-04-22> modify for 103262 end
    
    // mengdw <2017-04-22> add for 103262 begin
    private void updateLockScreenAppTraffic(final long endTime, final int action) {
        Thread queryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mTrafficMonitorUtil.updateQueryTime(mBeginTime, endTime);
                mTrafficMonitorUtil.initNetParam();
                ArrayList<AppItem> apps = mTrafficMonitorUtil.getApplicationsTraffic(mContext);
                mTrafficMonitorUtil.closeSession();
                if (null != mLockScreenTrafficHandler) {
                    Message msg = new Message();
                    msg.what = MSG_QUERY_TAFFIC_APPS_END;
                    msg.arg1 = action;
                    msg.obj = apps;
                    mLockScreenTrafficHandler.sendMessage(msg);
                }
            }
        });
        queryThread.start();
    }
    // mengdw <2017-04-22> add for 103262 end
    
    private long getUtcTime() {
        int[] timeArray = null;
        long strartTime = System.currentTimeMillis();
        timeArray = TimeFormat.getNowTimeArray();
        timeArray[1] += 1;

        if (timeArray != null) {
            strartTime = TimeFormat.getStartTime(timeArray[0], timeArray[1], timeArray[2], 0, 0, 0);
        }
        return strartTime;
    }
    
    private void registerLockScreenreceiver() {
        mScreenChangeReceive = new ScreenChangeReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenChangeReceive, filter);
    }

    private LockScreenTrafficUsedController(Context context) {
        mContext = context;
        int simIndex = TrafficassistantUtil.getActivatedSimCardNo(mContext);
        mBeginTime = getUtcTime();
        mTrafficSettingControler = TrafficSettingControler.getInstance(context);
        mTrafficMonitorUtil = new TrafficMonitorUtil(mContext, mBeginTime, System.currentTimeMillis(), simIndex);
        // mengdw <2017-04-22> add for 103262 begin
        mLockScreenTrafficHandler = new LockScreenTrafficHandler();
        // mengdw <2017-04-22> add for 103262 end
        registerLockScreenreceiver();
    }
    
    private void startNotification(long totalTraffic) {
        Log.d(TAG, "startNotification totalTraffic=" + totalTraffic);
        NotificationManager manager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent();
        intent.setClass(mContext, LockScreenTrafficDetailActivity.class);
        // mengdw <2017-04-24> add for 123502 begin
        Bundle bundle = new Bundle();
        ArrayList<AppItem> apps = getLockScreenTrafficApps();
        bundle.putParcelableArrayList(Constant.KEY_LOCKSCREEN_TRAFFIC, apps);
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // mengdw <2017-04-24> add for 123502 end
        Notification.Builder builder = new Notification.Builder(mContext);
        String title = String.format(mContext.getString(R.string.lock_screen_traffic_title),
                Formatter.formatFileSize(mContext, totalTraffic));
        String content = mContext.getResources().getString(R.string.lock_screen_traffic_content);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.notify);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        // Gionee: mengjk  modify for notify TitleIcon And TitleText Color Change
        builder.setColor(mContext.getResources().getColor(R.color.notify_icon_text_color));
        builder.setTicker(title);
        Notification notification = builder.getNotification();
        manager.notify(R.string.lock_screen_traffic_title, notification);
    }
    
    private void checkLockScreenTrafficUsed() {
        boolean isLockScreencRemindClose = mTrafficSettingControler.isLockScreenRemindClose();
        Log.d(TAG, "checkLockScreenTrafficUsed isLockScreencRemindClose=" + isLockScreencRemindClose);
        if (!isLockScreencRemindClose) {
            long usedTotal = getLockScreenTotalTraffic();
            int simIndex = TrafficassistantUtil.getActivatedSimCardNo(mContext);
            Log.d(TAG, "checkLockScreenTrafficUsed usedTotal=" + usedTotal + " simIndex=" + simIndex);
            if (isReachLimit() && simIndex >= 0) {
                startNotification(usedTotal);
            }
        }
    }
    
    // mengdw <2017-04-22> add for 103262 begin
    private class LockScreenTrafficHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage msg=" + msg.what);
            if (MSG_QUERY_TAFFIC_APPS_END == msg.what) {
                int action = msg.arg1;
                ArrayList<AppItem> apps = (ArrayList<AppItem>)msg.obj;
                if (ACTION_SCREEN_OFF == action) {
                    updateLockScreenApps(apps);
                } else if (ACTION_PRESENT == action) {
                    updateUnlockScreenApps(apps);
                    checkLockScreenTrafficUsed();
                }
            }
        }
    }
    // mengdw <2017-04-22> add for 103262 end
    
    private class ScreenChangeReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action=" + action);
            // mengdw <2017-04-22> modify for 103262 begin
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                long endTime = System.currentTimeMillis();
                updateLockScreenAppTraffic(endTime, ACTION_SCREEN_OFF);
            } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                long endTime = System.currentTimeMillis();
                updateLockScreenAppTraffic(endTime, ACTION_PRESENT);
            }
            // mengdw <2017-04-22> modify for 103262 end
        }
    }
}
