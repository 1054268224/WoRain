// Gionee <daizm> <2013-10-11> add for CR00919205 begin 
package com.cydroid.softmanager;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

import com.android.internal.util.MemInfoReader;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.greenbackground.GreenBackgroundService;
import com.cydroid.softmanager.monitor.service.CpuRamMonitorService;
import com.cydroid.softmanager.monitor.service.ScreenOffCleanService;
import com.cydroid.softmanager.powersaver.analysis.AnalysisPowerDataService;
import com.cydroid.softmanager.powersaver.notification.PowerConsumeService;
import com.cydroid.softmanager.powersaver.service.PowerManagerService;
import com.cydroid.softmanager.trafficassistant.hotspotremind.service.TrafficHotSportService;
import com.cydroid.softmanager.trafficassistant.service.TrafficResidentService;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.update.UpdateService;
import com.cydroid.softmanager.utils.Log;

import cyee.changecolors.ChameleonColorManager;

import com.chenyee.featureoption.ServiceUtil;
import com.cydroid.systemmanager.CyeeSystemManagerSDKApp;
import com.cyee.utils.LogUtil;

public class CyeeSystemManagerApp extends Application {
    private static final String TAG = "CyeeSystemManagerApp";

    /*guoxt modify for CR01538372 begin*/
    private static final String LIB_PATH = "/data/app-lib/com.cydroid.systemmanager";
    // private static final String[] SO_PATH = new String[]{"libkcmutil.so"};
    private static final MemInfoReader mMemInfoReader = new MemInfoReader();

    @Override
    public void onCreate() {
        LogUtil.LOGD("Application onCreate begin");
        super.onCreate();
//        ChameleonColorManager.getInstance().register(this, false);

        CyeeSystemManagerSDKApp.copySoFile(this);

        new Thread(() -> {
            try {
                Thread.sleep(800);
            } catch (Exception ex) {
            }
            String curProcessName = getCurProcessName();
            String remoteProcessName = getPackageName() + ":remote";
            // if (SystemProperties.getInt("sys.boot_completed", 0) == 0) {
            if (curProcessName.equals(remoteProcessName)) {
                startServiceInRemoteProcess(CyeeSystemManagerApp.this);
            } else if (curProcessName.equals(getPackageName())) {
                startHotSpotService(CyeeSystemManagerApp.this);
                TrafficResidentService.processTrafficServiceIntent(CyeeSystemManagerApp.this, true);
            }

            Log.d(TAG, "CyeeSystemManagerApp is created!(" + curProcessName + ")");
        }).start();
//        startService(new Intent(this,PowerManagerService.class));
        LogUtil.LOGD("Application onCreate end");
    }

    Context mContext;

    private void startServiceInRemoteProcess(Context context) {
        Log.d(TAG, "enter startRemoteProcessServiceByAppCreate!");
        //guoxt modfiy begin
        // scheduleUpdateService(context);
//        startPowerModeService(context);
//        startPowerConsumeService(context);

//        startGreenBackgroundService(context);

        //guoxt modify for CSW1702A-514 begin
        // startAnalysisPowerDataService(context);
        //guoxt modify for CSW1702A-514 end
        // Gionee <yangxinruo> <2016-01-4> add for CR01617315 begin
        // Gionee <houjie> <2015-08-19> add for CR01559020 begin
//        startCpuMonitorService(context);
        // Gionee <houjie> <2015-08-19> add for CR01559020 end
        // Gionee <yangxinruo> <2016-01-4> add for CR01617315 end
        // Gionee <yangxinruo><2016-1-5> add for CR01618272 begin
//        startScreenOffCleanService(context);
        // Gionee <yangxinruo><2016-1-5> add for CR01618272 end
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

    // mengdw <2015-10-20> add for CR01571760 begin
    private void startHotSpotService(Context context) {
        boolean isHotspotOpen = TrafficassistantUtil.isSoftApOpen(context);
        Log.d(TAG, "startHotSpotService isHotspotOpen=" + isHotspotOpen);
        if (isHotspotOpen) {
            Intent startIntent = new Intent(context, TrafficHotSportService.class);
            startIntent.setAction(Constant.ACTION_HOTSPOT_SERVICE_START);
            startIntent.putExtra(Constant.HOTSPOT_START_TYPE, Constant.TYPE_HOTSPOT_START_SYSTEM_MANAGER);
            ServiceUtil.startForegroundService(context, startIntent);
        }
    }
    // mengdw <2015-10-20> add for CR01571760 end

    private void startGreenBackgroundService(Context context) {
        if (SystemProperties.get("ro.gn.app.securepay.support", "no").equals("yes")) {
            return;
        }
        Intent startAnaIntent = new Intent(context, GreenBackgroundService.class);
        ServiceUtil.startForegroundService(context, startAnaIntent);
    }

    private void startAnalysisPowerDataService(Context context) {
        Intent startAnaIntent = new Intent(context, AnalysisPowerDataService.class);
        startAnaIntent.setAction(AnalysisPowerDataService.ACTION_SERVICE_START);
        ServiceUtil.startForegroundService(context, startAnaIntent);
    }

    private void startPowerConsumeService(Context context) {
        // Gionee <yangxinruo> <2015-09-17> add for CR01555251 begin
        // Gionee <yangxinruo> <2015-10-29> modify for CR01576434 begin
        boolean defaultValue = true;
        if (Consts.cyBAFlag) {
            defaultValue = false;
        }
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("power_consume_key", defaultValue)) {
            Log.d(TAG, "set startConsumeAlarm start");
            Intent startPowerConsume = new Intent(context, PowerConsumeService.class);
            startPowerConsume.setAction(PowerConsumeService.ACTION_START_ALARM);
            ServiceUtil.startForegroundService(context, startPowerConsume);
        }
        // Gionee <yangxinruo> <2015-10-29> modify for CR01576434 end
        // Gionee <yangxinruo> <2015-09-17> add for CR01555251 end
    }

    private void startPowerModeService(Context context) {
        Log.d(TAG, " startPowerModeService and check supermode restore before bootcompleted broadcast!");
        // Gionee <xuhz> <2013-12-13> add for CR00974497 begin
        Intent startIntent = new Intent(context, PowerManagerService.class);
        startIntent.setAction(PowerManagerService.ACTION_CHECK_SUPER_MODE_BOOT_STATE);
        startIntent.putExtra("bootcheck", true);
        startIntent.putExtra("reason", "boot");
        ServiceUtil.startForegroundService(context, startIntent);
        // Gionee <xuhz> <2013-12-13> add for CR00974497 end
    }

    // Gionee <yangxinruo> <2016-01-4> add for CR01617315 begin
    public static void startCpuMonitorService(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        /*guoxt modfiy for CR01692103 begin */
        boolean flag = true;
        mMemInfoReader.readMemInfo();
        long ramSize = getRamTotalMemory();
        flag = ramSize > 1073741824L && !Consts.cyBAFlag;
        /*guoxt modfiy for CR01692103 end */
        Log.d(TAG, "totalRam: " + getRamTotalMemory());
        if (pref.getBoolean("cpu_overload_monitor_key", flag)) {
            /*guoxt modify for CR01692103 begin*/
            if (flag) {
                Intent cpuMonitorIntent = new Intent(context, CpuRamMonitorService.class);
                cpuMonitorIntent.putExtra("event", "start");
                ServiceUtil.startForegroundService(context, cpuMonitorIntent);
            }
            /*guoxt modify for CR01692103 end*/
        }
    }

    // Gionee <yangxinruo> <2016-01-4> add for CR01617315 end
    /*guoxt modfiy for CR01692103 begin */
    public static long getRamTotalMemory() {
        long totalSize = mMemInfoReader.getTotalSize();
        return Util.translateCapacity(totalSize);
    }
    /*guoxt modfiy for CR01692103 end */

    // Gionee <yangxinruo><2016-1-5> add for CR01618272 begin
    public static void startScreenOffCleanService(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean defaultValue = true;
        //Chenyee guoxt modify for CSW1703VF-53 end
        if (Consts.gnIPFlag || Consts.cy1703VF || Consts.cyBAFlag) {
            defaultValue = false;
        }
        if (pref.getBoolean("screenoff_clean_key", defaultValue)) {
            String str = "start";
            if (pref.getBoolean("screenoff_clean_toast_key", defaultValue))
                str += "-showtoast";
            else
                str += "-hidetoast";
            Intent cleanIntent = new Intent(context, ScreenOffCleanService.class);
            cleanIntent.setAction("com.cydroid.screenoffclean");
            cleanIntent.putExtra("event", str);
            ServiceUtil.startForegroundService(context, cleanIntent);
        }
        //Chenyee guoxt modify for CSW1703VF-53 end
    }
    // Gionee <yangxinruo><2016-1-5> add for CR01618272 end

    // Gionee <yangxinruo> <2016-4-7> add for CR01670129 begin
    private String getCurProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }
    // Gionee <yangxinruo> <2016-4-7> add for CR01670129 end
}
