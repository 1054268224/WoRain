// Gionee <liuyb> <2014-2-25> add for CR01083582 begin
package com.cydroid.softmanager.monitor.receiver;

import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;

import com.cydroid.softmanager.CyeeSystemManagerApp;
import com.cydroid.softmanager.applock.AppLockManager;
import com.cydroid.softmanager.applock.AppLockUtils;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.monitor.service.MonitorService;
import com.cydroid.softmanager.monitor.service.ScreenOffCleanService;
import com.cydroid.softmanager.monitor.utils.CommonUtil;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListUtils;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppManager;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootForbiddenMsgManager;
import com.cydroid.softmanager.softmanager.defaultsoft.DefMrgSoftIntent;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftSettingsManager;
import com.cydroid.softmanager.update.UpdateService;
import com.cydroid.softmanager.utils.Log;
import com.chenyee.featureoption.ServiceUtil;
public class MonitorReceiver extends BroadcastReceiver {

    private static final String TAG = "MonitorReceiver";
    // private static final int AUTO_CALIBRATE_INTERVAL_TIME = 12 * 60 * 60 *
    // 1000;
    private static final int BOOT_AUTO_CLEAN_DELAY_TIME = 5 * 60 * 1000;

    // Gionee: houjie <2015-09-21> delete for CR01557560 begin
    // private PendingIntent mCalibrateAlarmPendingIntent;
    // Gionee: houjie <2015-09-21> delete for CR01557560 end
    private PendingIntent mBootAutoCleanAlarmPendingIntent;
    private final Handler mHandler = new Handler();
    private boolean flagsearch = true;

    private AlarmManager mAlarmManager = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d(TAG, "onReceive Action: " + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // Gionee <houjie> <2015-09-15> delete for CR01553826 begin
            // setCalibrateAlarmManager(context);
            // Gionee <houjie> <2015-09-15> delete for CR01553826 begin
            setBootAutoCleanAlarmManager(context);
            // Gionee <houjie> <2015-08-19> add for CR01559020 begin
            CyeeSystemManagerApp.startCpuMonitorService(context);
            // Gionee <houjie> <2015-08-19> add for CR01559020 end

            // Gionee <yangxinruo> <2016-1-5> add for CR01618272 begin
            CyeeSystemManagerApp.startScreenOffCleanService(context);
            // Gionee <yangxinruo> <2016-1-5> add for CR01618272 end

            Intent startIntent = new Intent(context, MonitorService.class);
            startIntent.putExtra("type", CommonUtil.TYPE_BOOT_COMPLETED);
            ServiceUtil.startForegroundService(context,startIntent);

            if (Consts.cyBAFlag){
                //Chenyee guot modify for CSW1703BA-214 begin
                setDefaultApps(context);
                //Chenyee guot modify for CSW1703BA-214 end
            }else {
                setDefaultSoftToRomApp(context);
            }

            bootAutoBootAppManager(context);
            // Gionee <houjie> <2015-09-15> modify for CR01553826 begin
            /*
            } else if (action.equals(CommonUtil.ACTION_CALIBRATE_ALARM)) {
            setMonitorAlarmManagerForAll(context);
            */
            syncUserWhitelistAndDozeUserList(context);
        } else if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
            // Gionee <yangxinruo> <2016-4-20> modify for CR01669840 begin
            // setMonitorAlarmManagerForAll(context);
            Intent startIntent = new Intent(context, MonitorService.class);
            startIntent.putExtra("type", CommonUtil.TYPE_TIME_CHANGED);
            ServiceUtil.startForegroundService(context,startIntent);
            // Gionee <yangxinruo> <2016-4-20> modify for CR01669840 end
            // Gionee <houjie> <2015-09-15> modify for CR01553826 end
			 //guoxt remove for oversea N begin 
            //scheduleUpdateService(context);
			 //guoxt remove for oversea N end 

        } else if (action.contains(CommonUtil.ACTION_MONITOR_ALARM)) {
            String jobkey = intent.getStringExtra(CommonUtil.JOB_KEY);
            Log.i(TAG, "start MonitorService jobKey ---> " + jobkey);
            Intent startIntent = new Intent(context, MonitorService.class);
            startIntent.putExtra(CommonUtil.JOB_KEY, jobkey);
            ServiceUtil.startForegroundService(context,startIntent);
        } else if (action.equals(CommonUtil.ACTION_BOOT_AUTO_CLEAN_ALARM)) {
            final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isInteractive()) {
                Log.i(TAG, "Android is interactive, so doing nothing.");
                return;
            }
            Log.i(TAG, "booted " + BOOT_AUTO_CLEAN_DELAY_TIME + " s, auto clean memory");
            startBootAutoClean(context);
        // Chenyee <guoxt> <2018-02-27> add for CSW1705A-1402 begin
        }else if(action.equals("com.cy.password.clear")){
            startDeleteAppLock(context);
        }else if(action.equals("chenyee.action.closewhitelist.reminder")){
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .cancel(ScreenOffCleanService.ID_WHITELIST_NOTI);
            MainProcessSettingsProviderHelper settingsHelper = new MainProcessSettingsProviderHelper(context);
            settingsHelper.putBoolean(ScreenOffCleanService.SHOULD_SHOW_WHITELIST_NOTI, false);

        }else if (action.equals("com.cyee.action.CLEARALL")) {
            Log.d(TAG, "com.cyee.action.CLEARALL");
            final MemoryManager memoryManager = MemoryManager.getInstance();
            memoryManager.init(context);
            new Thread() {
                @Override
                public void run() {
                    memoryManager.memoryClean(MemoryManager.CLEAN_TYPE_CANNON, new IMemoryCleanNativeCallback() {
                        @Override
                        public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {
                        }

                        @Override
                        public void onMemoryCleanFinished(int totalProcesses, long totalPss) {
                            Log.d(TAG, "CLEARALL finished result: totalProcesses = "
                                    + totalProcesses + " totalPss = " + totalPss);
                        }
                    });
                }
            }.start();
            // Chenyee <guoxt> <2018-02-27> add for CSW1705A-1402 end
        }
        // Chenyee <guoxt> <2018-02-27> add for CSW1705A-1402 end
    }

    private void syncUserWhitelistAndDozeUserList(Context context) {
        try {
            IDeviceIdleController dic = IDeviceIdleController.Stub
                    .asInterface(ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
            if (dic == null) {
                Log.d(TAG, "can not get IDeviceIdleController");
                return;
            }
            String[] userPowerWhitelist = dic.getUserPowerWhitelist();
            List<String> userDozeWhitelistArray = (List<String>) Arrays.asList(userPowerWhitelist);
            WhiteListManager whitelistManager = WhiteListManager.getInstance();
            whitelistManager.init(context);
            List<String> userWhitelist = whitelistManager.getUserWhiteApps();
            Log.d(TAG, "show doze list " + userDozeWhitelistArray);
            for (String whitelistApp : userWhitelist) {
                if (!userDozeWhitelistArray.contains(whitelistApp)) {
                    Log.d(TAG, "sync whitelist app :" + whitelistApp + " to doze list");
                    WhiteListUtils.addUserDozeWhiteApp(whitelistApp);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "deviceldle error! " + e);
            return;
        }
    }
    // Chenyee <guoxt> <2018-02-27> add for CSW1705A-1402 begin
    private void startDeleteAppLock(Context context) {
        Log.d(TAG,"startDeleteAppLock");
        List<String> lockedAppPackageNames = AppLockUtils.getLockedApps(context);
        if(lockedAppPackageNames.size()>0){
            for(int i=0; i< lockedAppPackageNames.size(); i++){
                AppLockManager manager = AppLockManager.getInstance();
                manager.init(context);
                manager.unLockApp(lockedAppPackageNames.get(i));
            }
        }
    }
    // Chenyee <guoxt> <2018-02-27> add for CSW1705A-1402 end


    private void startBootAutoClean(Context context) {
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(context);
        new Thread() {
            @Override
            public void run() {
                memoryManager.memoryClean(MemoryManager.CLEAN_TYPE_ROCKET, new IMemoryCleanNativeCallback() {
                    @Override
                    public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {
                    }

                    @Override
                    public void onMemoryCleanFinished(int totalProcesses, long totalPss) {
                        Log.d(TAG, "ACTION_BOOT_AUTO_CLEAN finished result: totalProcesses = "
                                + totalProcesses + " totalPss = " + totalPss);
                    }
                });
            }
        }.start();
    }

    @SuppressLint("NewApi")
    private void scheduleUpdateService(Context context) {
        Log.d(TAG, "schedule new UpdateService job");
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(UpdateService.JOB_ID,
                new ComponentName(context.getPackageName(), UpdateService.class.getName()));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Log.d(TAG, "schedule UpdateService failed!");
        }
    }

    // Gionee <houjie> <2015-09-15> delete for CR01553826 begin
    /*
    public void setCalibrateAlarmManager(Context context) {
        Intent timeoutIntent = new Intent();
        timeoutIntent.setAction(CommonUtil.ACTION_CALIBRATE_ALARM);
        mCalibrateAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, timeoutIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mCalibrateAlarmPendingIntent != null) {
            Log.d(TAG, "set setCalibrateAlarmManager start");
            if (mAlarmManager == null) {
                mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            }
            mAlarmManager.cancel(mCalibrateAlarmPendingIntent);
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    AUTO_CALIBRATE_INTERVAL_TIME, mCalibrateAlarmPendingIntent);
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.d(TAG, "pending intent is null");
        }
    }
    */
    // Gionee <houjie> <2015-09-15> delete for CR01553826 end

    // 设置开机5分钟后自动清理一次内存
    public void setBootAutoCleanAlarmManager(Context context) {
        Intent timeoutIntent = new Intent();
        timeoutIntent.setAction(CommonUtil.ACTION_BOOT_AUTO_CLEAN_ALARM);
        mBootAutoCleanAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, timeoutIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mBootAutoCleanAlarmPendingIntent != null) {
            Log.d(TAG, "set setBootAutoCleanAlarmManager");
            if (mAlarmManager == null) {
                mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            }
            mAlarmManager.cancel(mBootAutoCleanAlarmPendingIntent);
            Log.d(TAG, "set Alarm here!!  setBootAutoCleanAlarmManager");
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + BOOT_AUTO_CLEAN_DELAY_TIME,
                    mBootAutoCleanAlarmPendingIntent);
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.d(TAG, "pending intent is null");
        }
    }

    // Gionee <yangxinruo> <2016-4-20> delete for CR01669840 begin
    /*
    public void setMonitorAlarmManagerForWeek(Context context) {
        Intent timeoutIntent = new Intent();
        timeoutIntent.setAction(CommonUtil.ACTION_MONITOR_ALARM_WEEK);
        mMonitorWeekAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, timeoutIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mMonitorWeekAlarmPendingIntent != null) {
            Log.d(TAG, "set setMonitorAlarmManagerForWeek start");
            if (mAlarmManager == null) {
                mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            }
            mAlarmManager.cancel(mMonitorWeekAlarmPendingIntent);
            mAlarmManager.set(AlarmManager.RTC, CommonUtil.getNowToNextSundayMonitorInfoTime(),
                    mMonitorWeekAlarmPendingIntent);
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.d(TAG, "pending intent is null");
        }
    }
    
    public void setMonitorAlarmManagerForDay(Context context) {
        Intent timeoutIntent = new Intent();
        timeoutIntent.setAction(CommonUtil.ACTION_MONITOR_ALARM_DAY);
        mMonitorDayAlarmPendingIntent = PendingIntent.getBroadcast(context, 0, timeoutIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (mMonitorDayAlarmPendingIntent != null) {
            Log.d(TAG, "set setMonitorAlarmManagerForDay start");
            if (mAlarmManager == null) {
                mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            }
            mAlarmManager.cancel(mMonitorDayAlarmPendingIntent);
            mAlarmManager.set(AlarmManager.RTC, CommonUtil.getNowToNextDayMonitorInfoTime(),
                    mMonitorDayAlarmPendingIntent);
        } else {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.d(TAG, "pending intent is null");
        }
    }
    
    @SuppressLint("NewApi")
    public void setMonitorAlarmManagerForAll(Context context) {
        Iterator<String> iterator = CommonUtil.mJobMap.keySet().iterator();
        while (iterator != null && iterator.hasNext()) {
            String jobKey = iterator.next();
    
            if (mPendingIntentMap.get(jobKey) == null) {
                Intent timeoutIntent = new Intent();
                timeoutIntent.putExtra(CommonUtil.JOB_KEY, jobKey);
                timeoutIntent.setAction(CommonUtil.ACTION_MONITOR_ALARM + "_" + jobKey.toUpperCase());
                PendingIntent monitorAlarmPendingIntent = PendingIntent.getBroadcast(context, 0,
                        timeoutIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    
                mPendingIntentMap.put(jobKey, monitorAlarmPendingIntent);
            }
    
            Log.d(TAG, "set setMonitorAlarmManagerForAll start " + jobKey);
            if (mAlarmManager == null) {
                mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            }
            mAlarmManager.cancel(mPendingIntentMap.get(jobKey));
            // Gionee <yangxinruo> <2015-09-15> modify for CR01551835 begin
            if (Build.VERSION.SDK_INT >= 19) {
                Log.d(TAG, "set setMonitorAlarmManagerForAll start exact" + jobKey);
                mAlarmManager.setExact(AlarmManager.RTC,
                        CommonUtil.getNowToNextMonitorInfoTime(CommonUtil.mJobMap.get(jobKey)),
                        mPendingIntentMap.get(jobKey));
            } else {
                mAlarmManager.set(AlarmManager.RTC,
                        CommonUtil.getNowToNextMonitorInfoTime(CommonUtil.mJobMap.get(jobKey)),
                        mPendingIntentMap.get(jobKey));
            }
            // Gionee <yangxinruo> <2015-09-15> modify for CR01551835 end
        }
    }
    
    public static final HashMap<String, PendingIntent> mPendingIntentMap = new HashMap<String, PendingIntent>();
    */
    // Gionee <yangxinruo> <2016-4-20> delete for CR01669840 end
    public void bootAutoBootAppManager(Context context) {
        AutoBootAppManager autoBootAppManager = AutoBootAppManager.getInstance(context);
        autoBootAppManager.init(context);
        AutoBootForbiddenMsgManager autoBootForbiddenMsgManager = AutoBootForbiddenMsgManager.getInstance();
        autoBootForbiddenMsgManager.init(context);
        autoBootForbiddenMsgManager.resetAutoBootForbiddenMsgTurnOff();
    }

    public void setDefaultSoftToRomApp(Context context) {
        DefaultSoftSettingsManager defaultSoftSettingsManager = DefaultSoftSettingsManager.getInstance();
        defaultSoftSettingsManager.init(context);
        defaultSoftSettingsManager.setNotHasDefaultSoftToRomApp();
    }

    //Chenyee guot modify for CSW1703BA-214 begin
    private void setDefaultApps(final Context context){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "setRepetiveResponed in thread");
                setDefaultSoftToRomApp(context);
                flagsearch = getBooleanPreference(context,"first_boot",true);
                while (flagsearch) {

                    if(getDefaultSerachApp(context,DefMrgSoftIntent.DEF_READER, DefMrgSoftIntent.DEF_GALLAY
                            ,DefMrgSoftIntent.DEF_VIDEO)){
                        flagsearch =false;
                        setBooleanPreference(context,"first_boot",false);
                        break;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 5000);
    }
    //Chenyee guot modify for CSW1703BA-214 end

    //Chenyee guot modify for CSW1703BA-214 begin
    public boolean getDefaultSerachApp(Context context,int search,int gallery,int video) {
        DefaultSoftSettingsManager defaultSoftSettingsManager = DefaultSoftSettingsManager.getInstance();
        defaultSoftSettingsManager.init(context);
        return defaultSoftSettingsManager.getDefaultSerachapp(search);
               // &&defaultSoftSettingsManager.getDefaultGallery(gallery)
               // &&defaultSoftSettingsManager.getDefaultGallery(video);
    }
    //Chenyee guot modify for CSW1703BA-214 end

    public static boolean getBooleanPreference(Context context, String key, boolean def) {
        SharedPreferences share = context.getSharedPreferences("com.cydroid.softmanager_preferences",
                Context.MODE_MULTI_PROCESS);
        return share.getBoolean(key, def);
    }

    public static void setBooleanPreference(Context context, String key, boolean def) {
        SharedPreferences share = context.getSharedPreferences("com.cydroid.softmanager_preferences",
                Context.MODE_MULTI_PROCESS);
        share.edit().putBoolean(key, def).commit();
    }
}
// Gionee <liuyb> <2014-2-25> add for CR01083582 end
