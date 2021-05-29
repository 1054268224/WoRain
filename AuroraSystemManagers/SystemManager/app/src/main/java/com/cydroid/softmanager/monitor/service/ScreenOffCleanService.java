// Gionee <yangxinruo><2016-1-5> add for CR01618272 begin
package com.cydroid.softmanager.monitor.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.Display;
import android.widget.Toast;

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.powersaver.mode.NightModeController;
import com.cydroid.softmanager.powersaver.utils.BatteryStateInfo;
import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.monitor.utils.ProcessCpuTracker;
import com.cydroid.softmanager.monitor.utils.UsageStatsHelper;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.cydroid.softmanager.monitor.receiver.MonitorReceiver;
import com.chenyee.featureoption.ServiceUtil;
import com.cydroid.softmanager.R;

public class ScreenOffCleanService extends Service implements IMemoryCleanNativeCallback {
    private final static String TAG = "ScreenOffCleanService";
    private final static int SCREEN_OFF_POLLING_INTERVAL = 5000;

    private final static int EVENT_SCREEN_OFF_POLLING = 1;
    // Gionee <yangxinruo> <2016-1-27> add for CR01630781 begin
    private final static int EVENT_SET_SCREEN_OFF_TIMEOUT = 2;
    // Gionee <yangxinruo> <2016-1-27> add for CR01630781 end

    private final static int REMAIN_TOP_APP_NUMBER = 3;

    private final static long FREQ_APP_UPDATE_INTERVAL = 6 * 3600 * 1000;// 6 hours
    private final static long FREQ_APP_QUERY_SPAN = 5 * 24 * 3600 * 1000;// 5 days
    private final static String UPDATE_USAGE_DATA = "com.cydroid.softmanager.monitor.service.ScreenOffCleanService.UPDATE_USAGE_DATA";

    private final static int TASK_MAX = 50;

    // Gionee <yangxinruo> <2016-1-27> add for CR01630781 begin
    private final static long CLEAN_TIMEOUT = 10 * 1000;
    // Gionee <yangxinruo> <2016-1-27> add for CR01630781 end

    // Gionee <yangxinruo> <2016-1-12> modify for CR01622195 begin
    private final static long MIN_CLEAN_INTERVAL = 5 * 60 * 1000;
    private final static long MAX_CLEAN_INTERVAL = 60 * 60 * 1000;

    private final static int CLEAN_TYPE = MemoryManager.CLEAN_TYPE_ASSAULT_RIFLE;
    private long mCurrentCleanInterval = MIN_CLEAN_INTERVAL / 2;
    private long mLastCleanTime = 0;
    private int mLastCleanRemains = Integer.MAX_VALUE;
    // Gionee <yangxinruo> <2016-1-12> modify for CR01622195 end

    private Context mContext;
    private MemoryManager mMemoryManager;

    private ScreenStateReceiver mScreenStateReceiver;

    private WakeLock mScreenOffWakelock;
    private UsageStatsHelper mUsageStatsHelper;
    private PendingIntent mUsageUpdateIntent;
    public final static String BROADCAST_SCREENOFF_FINISH = "com.cydroid.softmanager.monitor.service.ScreenOffCleanService.BROADCAST_SCREENOFF_FINISH";
    public static final String SHOULD_SHOW_WHITELIST_NOTI = "show_whitelist_noti";
    public final static String CLOSE_WHITELIST_INTENT = "chenyee.action.closewhitelist.reminder";
    public static final int ID_WHITELIST_NOTI = 15;
    private PendingIntent mWhitelistManagerPendingIntent;
    private ActivityChangeMonitor mActivityChangeMonitor;
    //Chenyee guoxt modify for CSW1703VF-53 begin
    private static final long SET_ALARM_CLEAN_WAIT_TIME = 2 * 60 * 60 * 1000;
    private AlarmManager mAlarmManager = null;
    private PendingIntent mEnterCleanPendingIntent = null;
    private final static String DELAY_2H_CLEAN = "com.cydroid.softmanager.monitor.service.ScreenOffCleanService.delay2hour";
    //Chenyee guoxt modify for CSW1703VF-53 end
    private boolean mStopService;

    private long mScreenOffTime;
    private long mMaxCleanSize;
    private boolean mShowToast;

    // Gionee <yangxinruo> <2016-1-20> modify for CR01625798 begin
//    private Set<String> mExcludeSet;
    // Gionee <yangxinruo> <2016-1-20> modify for CR01625798 end

    // Gionee <yangxinruo> <2015-12-17> add for CR01610842 begin
    private boolean mIsRunning = false;
    // Gionee <yangxinruo> <2015-12-17> add for CR01610842 end
    private boolean mIsInNightMode = false;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EVENT_SCREEN_OFF_POLLING) {
                boolean needRecord = false;
                if (msg.obj != null && msg.obj.equals("needrecord")) {
                    needRecord = true;
                }
                screenOffClean(needRecord);
                //Chenyee guoxt 20180505 modify for CSW1703VF-53 begin
                if(!Consts.cy1703VF) {
                    Message m = obtainMessage(EVENT_SCREEN_OFF_POLLING);
                    sendMessageDelayed(m, SCREEN_OFF_POLLING_INTERVAL);
                }
                //Chenyee guoxt 20180505 modify for CSW1703VF-53 end

            } else if (msg.what == EVENT_SET_SCREEN_OFF_TIMEOUT) {
                // Gionee <yangxinruo> <2016-1-27> add for CR01630781 begin
                Log.d(TAG, "screen off timeout ,release wakelock");
                processCleanResult(0, Integer.MAX_VALUE);
                // Gionee <yangxinruo> <2016-1-27> add for CR01630781 end
            }
        }
    };

    // Gionee <yangxinruo> <2016-1-27> modify for CR01630781 begin
//    private BroadcastReceiver mScreendOffWakelockReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "BROADCAST_SCREENOFF_FINISH received");
//            long res = 0;
//            int currentRemains = Integer.MAX_VALUE;
//            if (intent != null) {
//                res = intent.getLongExtra("cleansize", 0);
//                currentRemains = (int) intent.getLongExtra("cleanremains", Integer.MAX_VALUE);
//            }
//            processCleanResult(res, currentRemains);
//        }
//    };

    private void processCleanResult(long res, int currentRemains) {
        mHandler.removeMessages(EVENT_SET_SCREEN_OFF_TIMEOUT);
        if (isNeedShowWhitelistNoti()) {
            showWhitelistNoti();
        }
        // Gionee <yangxinruo> <2016-1-12> modify for CR01622195 begin
        if (res > mMaxCleanSize)
            mMaxCleanSize = res;
        if (currentRemains <= mLastCleanRemains) {
            // Gionee <yangxinruo> <2015-12-17> delete for CR01610842 begin
            mCurrentCleanInterval = Math.min(mCurrentCleanInterval * 2, MAX_CLEAN_INTERVAL);
            // Gionee <yangxinruo> <2015-12-17> delete for CR01610842 end
        } else {
            mCurrentCleanInterval = MIN_CLEAN_INTERVAL;
        }
        Log.d(TAG, "screenoff clean worked size=" + res + " next clean interval=" + mCurrentCleanInterval
                + " lastRemains=" + mLastCleanRemains + " currentRemains=" + currentRemains);
        mLastCleanRemains = currentRemains;
        // Gionee <yangxinruo> <2016-1-12> modify for CR01622195 end

        // Gionee <yangxinruo> <2015-12-17> add for CR01610842 begin
        mIsRunning = false;
        // Gionee <yangxinruo> <2015-12-17> add for CR01610842 end

        if (mScreenOffWakelock.isHeld()) {
            Log.d(TAG, "BROADCAST_SCREENOFF_FINISH release wakelock");
            mScreenOffWakelock.release();
        }
    }
    // Gionee <yangxinruo> <2016-1-27> modify for CR01630781 end

    private boolean isNeedShowWhitelistNoti() {
        // Chenyee <CY_Oversea_Req> <xionghg> <20170926> add for 223259 SW17W16TL-18 begin
        if (Consts.gnTCflag || Consts.gnTLFlag || Consts.cyBAFlag || !Consts.isWhistListSupport) {
            return false;
        }
        // Chenyee <CY_Oversea_Req> <xionghg> <20170926> add for 223259 SW17W16TL-18 end
        MainProcessSettingsProviderHelper settingsHelper = new MainProcessSettingsProviderHelper(mContext);
        return settingsHelper.getBoolean(SHOULD_SHOW_WHITELIST_NOTI, true);
    }

    private void showWhitelistNoti() {
        if (mWhitelistManagerPendingIntent == null) {
            Intent whitelistIntent = new Intent("com.cydroid.softmanager.action.WHITE_LIST_MANAGER");
            whitelistIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            whitelistIntent.putExtra("is_from_screen_off_clean", true);
            mWhitelistManagerPendingIntent = PendingIntent.getActivity(mContext, 0, whitelistIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        BigTextStyle bigStyle = new BigTextStyle();
        bigStyle.setBigContentTitle(
                mContext.getResources().getString(R.string.screenoff_whitelist_noti_title));
        bigStyle.bigText(mContext.getResources().getString(R.string.screenoff_whitelist_noti_message));
       //Chenyee guoxt modify for CSW1705P-35 begin
        NotificationManager manager = (NotificationManager) mContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("3",
                "screenoff", NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);
       //Chenyee guoxt modify for CSW1705P-35 end
        Notification.Builder notification = new Notification.Builder(mContext,"3").setStyle(bigStyle)
                .setVisibility(Notification.VISIBILITY_PUBLIC).setSmallIcon(R.drawable.notify)
                // Gionee: mengjk modify for notify TitleIcon And TitleText Color Change
                .setAutoCancel(true)
                .setColor(mContext.getResources().getColor(R.color.notify_icon_text_color))
                .setContentIntent(mWhitelistManagerPendingIntent)
                .setContentTitle(mContext.getResources().getString(R.string.screenoff_whitelist_noti_title))
                .setContentText(mContext.getResources().getString(R.string.screenoff_whitelist_noti_message));
       //Chenyee guoxt modify for CSW1703A-3315 begin
        notification.addAction(0, mContext.getResources().getString(R.string.autoboot_forbidden_notify_turn_off),
                getClosePendingIntent());
        //Chenyee guoxt modify for CSW1703A-3315 end
        Notification noti = notification.build();
        Log.d(TAG, "show whitelist notifaction");
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(ID_WHITELIST_NOTI, noti);
    }

    //Chenyee guoxt modify for CSW1703A-3315 begin
    private PendingIntent getClosePendingIntent() {
        Intent turnOffIntent = new Intent();
        turnOffIntent.setClass(mContext, MonitorReceiver.class);
        turnOffIntent.setAction(CLOSE_WHITELIST_INTENT);
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(mContext,
                0, turnOffIntent, PendingIntent.FLAG_ONE_SHOT);

        return turnOffPendingIntent;
    }
    //Chenyee guoxt modify for CSW1703A-3315 end

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        mStopService = false;
        mShowToast = true;
        mContext = getApplicationContext();
        mScreenStateReceiver = new ScreenStateReceiver(mContext);
//        mExcludeSet = new HashSet<String>();
        mMemoryManager = MemoryManager.getInstance();
        mMemoryManager.init(mContext);
        //Chenyee guoxt 20180505 modify for CSW1703VF-53 begin
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        //Chenyee guoxt modify for CSW1703VF-53 end
        //guoxt modify for 4.1.1 begin 
        //mActivityChangeMonitor = new ActivityChangeMonitor(mContext);
        //mActivityChangeMonitor.listen();
        //guoxt modify for 4.1.1 end 

        initUsageUpdateAlarmIntent();
        setUsageUpdateAlarm();

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mScreenOffWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "screen_off_clean");
        mScreenOffWakelock.setReferenceCounted(false);
//        mContext.registerReceiver(mScreendOffWakelockReceiver, new IntentFilter(BROADCAST_SCREENOFF_FINISH));

        if (powerManager.isInteractive()) {
            Log.d(TAG, "system isInteractive now");
            return;
        }
        // Gionee <yangxinruo> <2016-1-12> add for CR01622195 begin
        if (BatteryStateInfo.isChargingNow(mContext)) {
            Log.d(TAG, "charging now,do not need screen off clean");
            mScreenOffTime = Long.MAX_VALUE;
            return;
        }
        Log.d(TAG, "not isInteractive sendEmptyMessage EVENT_SCREEN_OFF_POLLING");
        // Gionee <yangxinruo> <2016-1-12> add for CR01622195 end
        mScreenOffTime = SystemClock.elapsedRealtime();
        mMaxCleanSize = 0;
        mHandler.sendEmptyMessage(EVENT_SCREEN_OFF_POLLING);
    }

    private void initUsageUpdateAlarmIntent() {
        Intent updateIntent = new Intent(mContext, ScreenOffCleanService.class);
        updateIntent.setAction(UPDATE_USAGE_DATA);
        mUsageUpdateIntent = PendingIntent.getService(mContext, 0, updateIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mUsageStatsHelper = new UsageStatsHelper(mContext);
        Log.d(TAG, "Update usage data time--->"
                + DateFormat.format("yyyy-MM-dd kk:mm:ss", System.currentTimeMillis()).toString());
        mUsageStatsHelper.dataUpdate(FREQ_APP_QUERY_SPAN);
        Log.d(TAG, "Update usage data begin--->");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        //mActivityChangeMonitor.remove();
        unsetUsageUpdateAlarm();
        mHandler.removeMessages(EVENT_SCREEN_OFF_POLLING);
        mContext.unregisterReceiver(mScreenStateReceiver);
//        mContext.unregisterReceiver(mScreendOffWakelockReceiver);
        mMemoryManager = null;
        super.onDestroy();
    }

    private void setUsageUpdateAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mUsageUpdateIntent);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                FREQ_APP_UPDATE_INTERVAL, mUsageUpdateIntent);
    }

    private void unsetUsageUpdateAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mUsageUpdateIntent);
    }

    private List<String> getRunningRecentList() {
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RecentTaskInfo> recentTasks = activityManager.getRecentTasks(TASK_MAX,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        HashSet<String> runningApps = new HashSet<String>();
        ArrayList<String> resList = new ArrayList<String>();
        if (recentTasks != null) {
            List<RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
            for (RunningAppProcessInfo appProcessInfo : appProcessInfos) {
                for (String packageName : appProcessInfo.pkgList) {
                    runningApps.add(packageName);
                }
            }
            for (int i = 0; i < recentTasks.size(); i++) {
                RecentTaskInfo task = recentTasks.get(i);
                Intent intent = new Intent(task.baseIntent);
                if (task.origActivity != null) {
                    intent.setComponent(task.origActivity);
                }
                String pkgName = intent.getComponent().getPackageName();
                if (pkgName != null && runningApps.contains(pkgName)) {
                    resList.add(pkgName);
                }
            }
        }
        return resList;
    }

//    private void screenOffClean() {
//        screenOffClean(false);
//    }

    private void screenOffClean(boolean needRecord) {
        // Gionee <yangxinruo> <2016-1-12> add for CR01622195 begin
        if(!Consts.cy1703VF) {
            if (SystemClock.elapsedRealtime() - mLastCleanTime < mCurrentCleanInterval) {
                return;
            }
        }
        // Gionee <yangxinruo> <2016-1-12> add for CR01622195 end
        if (mStopService) {
            stopSelf();
            return;
        }
        // Gionee <yangxinruo> <2015-12-17> add for CR01610842 begin
        if (mIsRunning) {
            Log.d(TAG, "last screenoff clean not finish yet ,cancel");
            return;
        }
        mScreenOffWakelock.acquire();
        // Gionee <yangxinruo> <2015-12-17> add for CR01610842 begin
        mIsRunning = true;
        // Gionee <yangxinruo> <2015-12-17> add for CR01610842 end
        // HelperUtils.showProcessAdjList(mContext);
        Log.d(TAG, "screen off cleaning -->acquire mScreenOffWakelock lasttime="
                + (SystemClock.elapsedRealtime() - mLastCleanTime));
        // Gionee <yangxinruo> <2016-1-12> modify for CR01622195 begin
        mLastCleanTime = SystemClock.elapsedRealtime();
        // Gionee <yangxinruo> <2016-1-12> modify for CR01622195 end

//        ArrayList<String> targetList = (ArrayList<String>) mUsageStatsHelper.getFreqList();

        // Gionee <yangxinruo> <2016-1-20> modify for CR01625798 begin
        // Gionee <yangxinruo> <2016-8-16> delete for CR01742808 begin
        // ArrayList<String> updatedExcludeList = getExcludeList();
        // mExcludeSet.addAll(updatedExcludeList);
        // Gionee <yangxinruo> <2016-8-16> delete for CR01742808 end

        // Gionee <yangxinruo> <2016-1-20> modify for CR01627417 begin
        boolean isInNightMode = new NightModeController(mContext).isInNightMode();
        oneCleanByScreenOff(isInNightMode, needRecord);
        // Gionee <yangxinruo> <2016-1-27> add for CR01630781 begin
//        if (!res) {
//            Log.d(TAG, "OneClean start failed!release wakelock");
//            processCleanResult(0, Integer.MAX_VALUE);
//        } else {
        Message msg = mHandler.obtainMessage(EVENT_SET_SCREEN_OFF_TIMEOUT);
        mHandler.sendMessageDelayed(msg, CLEAN_TIMEOUT);
//        }
        // Gionee <yangxinruo> <2016-1-27> add for CR01630781 end
        // Gionee <yangxinruo> <2016-1-20> modify for CR01627417 end
        // Gionee <yangxinruo> <2016-1-20> modify for CR01625798 end
    }

    private void oneCleanByScreenOff(final boolean isInNightMode, final boolean needRecord) {
        mIsInNightMode = isInNightMode;
        new Thread() {

            @Override
            public void run() {
                Bundle params = new Bundle();
                params.putBoolean("is_night_mode", mIsInNightMode);
                params.putBoolean("is_need_record", needRecord);
                List<ProcessMemoryEntity> cleanList = mMemoryManager
                        .getRunningProcessMemoryEntitys(CLEAN_TYPE, params);
                Log.d(TAG, "there are " + cleanList.size() + " pkgs to cleaned");
                if (isDisplayOn()) {
                    Log.d(TAG, "screen on now,cancel clean process");
                    return;
                }
                if (cleanList.isEmpty()) {
                    processCleanResult(0, 0);
                    return;
                }
                mMemoryManager.cleanProcessMemoryEntitys(cleanList, ScreenOffCleanService.this);
            }

        }.start();
    }

    protected boolean isDisplayOn() {
        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display curDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
        return curDisplay.getState() == Display.STATE_ON;
    }

    class ScreenStateReceiver extends BroadcastReceiver {
        private static final int TOAST_MIN_SCREENOFF_TIME = 10 * 60 * 1000;// 10min
        private static final int TOAST_MIN_CLEAN_SIZE = 10 * 1024 * 1024;// 10MB

        public ScreenStateReceiver(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            context.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String actionStr = intent.getAction();
            Log.d(TAG, "ScreenStateReceiver onReceive action:" + actionStr);
            if (Intent.ACTION_SCREEN_ON.equals(actionStr)) {
                if (Consts.cy1703VF && mEnterCleanPendingIntent != null ) {
                    mAlarmManager.cancel(mEnterCleanPendingIntent);
                    Log.d(TAG, "cancel action:mEnterCleanPendingIntent" );
                }

                mHandler.removeMessages(EVENT_SCREEN_OFF_POLLING);
                long screenoffTime = SystemClock.elapsedRealtime() - mScreenOffTime;
                // Gionee <yangxinruo> <2015-12-17> modify for CR01610842 begin
                if (shouldMakeToast(screenoffTime, mMaxCleanSize)) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.screenoff_clean_toast_msg,
                                    // xionghg 2017-08-15 modify for 188020 begin
                                    // Formatter.formatFileSize(context, mMaxCleanSize)),
                                    String.valueOf(mMaxCleanSize / 1024 / 1024)),
                                    // xionghg 2017-08-15 modify for 188020 end
                            Toast.LENGTH_LONG).show();
                }
                // Gionee <yangxinruo> <2015-12-17> modify for CR01610842 end
            } else if (Intent.ACTION_SCREEN_OFF.equals(actionStr)) {

                // Gionee <yangxinruo> <2016-1-8> add for CR01620291 begin
                boolean isPlugged = BatteryStateInfo.isChargingNow(mContext);
                /*guoxt 2018-0317 modify for CSW1703A-1060 begin*/
                Log.refreshLogEnable();
                /*guoxt 2018-0317 modify for CSW1703A-1060 end*/
                if (isPlugged) {
                    Log.d(TAG, "charging now,do not need screen off clean");
                    mScreenOffTime = Long.MAX_VALUE;
                    // Log.d(TAG, "ScreenStateReceiver onReceive action:" + actionStr + " process
                    // finished");
                    return;
                }
                // Gionee <yangxinruo> <2016-1-8> add for CR01620291 end

//                    recordDataLog(mUsageStatsHelper.getFreqList().toString());
               //Chenyee guoxt 20180505 modify for CSW1703VF-53 begin
                if(Consts.cy1703VF) {
                    mScreenOffTime = SystemClock.elapsedRealtime();
                    initScreenOffClean();
                    Intent serviceIntent = new Intent(mContext, ScreenOffCleanService.class);
                    serviceIntent.setAction(DELAY_2H_CLEAN);
                    mEnterCleanPendingIntent = PendingIntent.getService(context, 0, serviceIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    if (mEnterCleanPendingIntent == null) {
                        Log.d(TAG, "enter autoclean2h mode intent is null!!!");
                        return;
                    }else{
                        mAlarmManager.cancel(mEnterCleanPendingIntent);
                    }
                    mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + SET_ALARM_CLEAN_WAIT_TIME,
                            mEnterCleanPendingIntent);
                }else{
                    screenoffhander();
                }
                //Chenyee guoxt 20180505 modify for CSW1703VF-53 end
            }
            // Log.d(TAG, "ScreenStateReceiver onReceive action:" + actionStr + " process finished");
        }

//        private void recordDataLog(String data) {
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//            Date curDate = new Date(System.currentTimeMillis());
//            String dateStr = formatter.format(curDate);
//            SmartCleanInfoWriter.writeToDataFile("\n<---Get Frequent App at ScreenOff " + dateStr + "--->");
//            SmartCleanInfoWriter.writeToDataFile(data);
//        }

        private boolean shouldMakeToast(long screenoffTime, long cleanSize) {
            Log.d(TAG, "should make toast ? screenoffTime=" + screenoffTime + " cleanSize=" + cleanSize);
            return mShowToast && screenoffTime > TOAST_MIN_SCREENOFF_TIME && cleanSize > TOAST_MIN_CLEAN_SIZE;
        }
    }

    //Chenyee guoxt 20180505 modify for CSW1703VF-53 begin
    public  void screenoffhander(){
        mScreenOffTime = SystemClock.elapsedRealtime();
        initScreenOffClean();
        Message screenOffMsg = mHandler.obtainMessage(EVENT_SCREEN_OFF_POLLING);
        screenOffMsg.obj = "needrecord";
        mHandler.sendMessage(screenOffMsg);
    }
    //Chenyee guoxt 20180505 modify for CSW1703VF-53 end

    private void initScreenOffClean() {
        // Gionee <yangxinruo> <2015-12-17> delete for CR01610842 begin
        // mCurrentCleanInterval = MIN_CLEAN_INTERVAL;
        // Gionee <yangxinruo> <2015-12-17> delete for CR01610842 end
        mLastCleanTime = 0;
        mMaxCleanSize = 0;
        // Gionee <yangxinruo> <2016-1-20> modify for CR01625798 begin
        // mExcludeList = getExcludeList();
//        mExcludeSet.clear();
        // Gionee <yangxinruo> <2016-1-20> modify for CR01625798 end
        // Gionee <yangxinruo> <2016-1-12> add for CR01622195 begin
        mCurrentCleanInterval = MIN_CLEAN_INTERVAL / 2;
        // Gionee <yangxinruo> <2016-1-12> add for CR01622195 end
    }

    //Chenyee guoxt 20180505 modify for CSW1703VF-53 begin
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            ServiceUtil.handleStartForegroundServices(ScreenOffCleanService.this);
            String action = intent.getAction();
            if (action != null && action.equals(UPDATE_USAGE_DATA)) {
                Log.d(TAG, "Update usage data time--->"
                        + DateFormat.format("yyyy-MM-dd kk:mm:ss", System.currentTimeMillis()).toString());
                mUsageStatsHelper.dataUpdate(FREQ_APP_QUERY_SPAN);
                Log.d(TAG, "Update usage data begin--->");
            //Chenyee guoxt modify for CSW1703VF-53 begin
            }else if(action != null && action.equals("com.cydroid.screenoffclean")){
                Log.d(TAG, "ScreenOffCleanService----");
                String eventStr = intent.getStringExtra("event");
                if (eventStr != null) {
                    String[] events = eventStr.split("-");
                    for (String event : events) {
                        Log.d(TAG, "onStartCommand event--->" + event);
                        if (event.equals("stop"))
                            mStopService = true;
                        else if (event.equals("start")) {
                            mStopService = false;
                            initScreenOffClean();
                        } else if (event.equals("showtoast"))
                            mShowToast = true;
                        else if (event.equals("hidetoast"))
                            mShowToast = false;
                    }
                }

            }else if(action != null && action.equals(DELAY_2H_CLEAN)){
                screenoffhander();
                Log.d(TAG, "start sleep 2h clean ");
            }
        }
        return START_STICKY;
    }
    //Chenyee guoxt 20180505 modify for CSW1703VF-53 end

    @Override
    public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {
    }

    @Override
    public void onMemoryCleanFinished(final int totalProcesses, final long totalPss) {
        Log.d(TAG, "fetch remain cleanable porcess conut begin");
        Bundle params = new Bundle();
        params.putBoolean("is_night_mode", mIsInNightMode);
        final List<ProcessMemoryEntity> cleanList = mMemoryManager.getRunningProcessMemoryEntitys(CLEAN_TYPE,
                params);
        Log.d(TAG, "fetch remain cleanable porcess conut result=" + cleanList.size());
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                long totalPssBytes = totalPss * 1024;
                Log.d(TAG, "clean finished clean size = " + totalPssBytes + " remains = " + cleanList.size());
                long res = 0;
                int currentRemains = Integer.MAX_VALUE;
                processCleanResult(totalPssBytes, cleanList.size());
            }

        });
    }

}
// Gionee <yangxinruo><2016-1-5> modify for CR01618272 end
