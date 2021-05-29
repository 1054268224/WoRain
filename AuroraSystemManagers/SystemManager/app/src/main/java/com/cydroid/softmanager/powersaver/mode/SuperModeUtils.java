package com.cydroid.softmanager.powersaver.mode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.cydroid.softmanager.R;
import com.cydroid.framework.FrameworkUtility;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.utils.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

public class SuperModeUtils {
    private static final String TAG = "SuperModeHelper";
    private static final int TASK_MAX = 50;

    public static void cleanAppDate(Context context, String pkgName) {
        try {
            PackageManager pm = context.getPackageManager();
            context.getPackageManager().clearApplicationUserData(pkgName, null);
        } catch (Exception e) {
            Log.i(TAG, "cleanAppDate ------->", e);
        }
    }

    public static void rebootFromSuperModeException(Context context) {
        PowerModeUtils.setSuperModeExceptionRebootFlag(context);
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        powerManager.reboot(null);
    }

    public static void unFreezeApp(Context context, String pkgName, boolean shouldNotKill) {
        int flag = 0;
        if (shouldNotKill)
            flag |= PackageManager.DONT_KILL_APP;
        try {
            Log.d(TAG, " unFreezeApps(flag:" + flag + ") PkgName ---> " + pkgName);
            // Gionee <yangxinruo> <2016-2-22> modify for CR01638665 begin
            context.getPackageManager().setApplicationEnabledSetting(pkgName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, flag);
            // Gionee <yangxinruo> <2016-2-22> modify for CR01638665 end
        } catch (Exception e) {
            Log.e(TAG, "unfreezeApp", e);
        }
    }

    public static void freezeApp(Context context, String pkgName, boolean shouldNotKill) {
        int flag = 0;
        if (shouldNotKill)
            flag |= PackageManager.DONT_KILL_APP;
        try {
            // Log.d("dzmdzm", " Freeze Apps PkgName ---> " + pkgName);
            Log.d(TAG, " Freeze Apps(flag:" + flag + ") PkgName ---> " + pkgName);
            context.getPackageManager().setApplicationEnabledSetting(pkgName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, flag);
        } catch (Exception e) {
            Log.e(TAG, "freezeApp may package " + pkgName + " not exitst", e);
        }
    }

    public static List<String> getSuperModeTaskWhitelist(Context context) {
        // Gionee <yangxinruo> <2015-08-12> add for CR01525446 begin
        String[] whitearray = context.getResources().getStringArray(R.array.super_power_save_task);
        return Arrays.asList(whitearray);
        // Gionee <yangxinruo> <2015-08-12> add for CR01525446 end
    }

    public static void killRunningAndRecentTask(Context context, int topTaskId, List<String> whitelist) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        int numTasks = recentTasks.size();
        List<RunningTaskInfo> runTasks = am.getRunningTasks(TASK_MAX);
        if (runTasks == null) {
            // recentTasks = new ArrayList<AppTask>();
            Log.d(TAG, "runTasks == null, skip");
            return;
        }
        ArrayList<Integer> excludeIdArray = new ArrayList<Integer>();
        for (int i = 0; i < runTasks.size(); i++) {
            RunningTaskInfo taskInfo = runTasks.get(i);
            // Gionee <yangxinruo> <2015-08-12> add for CR01525446 begin
            String basePkgName = "";
            if (taskInfo.baseActivity != null) {
                basePkgName = taskInfo.baseActivity.getPackageName();
            }
            String topActivityName = "";
            if (taskInfo.topActivity != null) {
                topActivityName = taskInfo.topActivity.flattenToString();
            }
            Log.d(TAG, "process running task: intent=" + taskInfo.baseActivity + " pkg=" + basePkgName
                    + " topActivity=" + topActivityName + " id:" + taskInfo.id);
            excludeIdArray.add(taskInfo.id);
            if (whitelist.contains(basePkgName)) {
                Log.d(TAG, "in task whitelist do not kill " + basePkgName + " id:" + taskInfo.id);
                excludeIdArray.add(taskInfo.id);
                continue;
            }
            if (whitelist.contains(topActivityName)) {
                Log.d(TAG, "in task whitelist do not kill " + topActivityName + " id:" + taskInfo.id);
                excludeIdArray.add(taskInfo.id);
                continue;
            }
            // Gionee <yangxinruo> <2015-08-12> add for CR01525446 end
            else {
                Log.d(TAG, "task-id: " + taskInfo.id);
                // Gionee <yangxinruo> <2016-7-5> modify for CR01727240 begin
                if (topTaskId == taskInfo.id) {
                    Log.d(TAG, "task: it's me, don't kill  myTaskId=" + topTaskId);
                } else {
                    removeTask(am, taskInfo.id);
                }
                // Gionee <yangxinruo> <2016-7-5> modify for CR01727240 end
            }
        }

        List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(TASK_MAX,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        for (int i = 0; i < recentTasks.size(); i++) {
            RecentTaskInfo taskInfo = recentTasks.get(i);
            // Gionee <yangxinruo> <2015-08-12> add for CR01525446 begin
            String basePkgName = "";
            if (taskInfo.baseIntent.getComponent() != null)
                basePkgName = taskInfo.baseIntent.getComponent().getPackageName();
            Log.d(TAG, "process recent task: intent=" + taskInfo.baseIntent + " pkg=" + basePkgName);
            if (whitelist.contains(basePkgName)) {
                Log.d(TAG, "in task whitelist do not kill " + basePkgName);
                continue;
            }
            // Gionee <yangxinruo> <2015-08-12> add for CR01525446 end
            else {
                Log.d(TAG, "task-persistentId: " + taskInfo.persistentId);
                // Gionee <yangxinruo> <2016-7-5> modify for CR01727240 begin
                if (topTaskId == taskInfo.persistentId) {
                    Log.d(TAG, "task: it's me, don't kill  myTaskId=" + topTaskId);
                } else {
                    if (excludeIdArray.contains(taskInfo.persistentId)) {
                        Log.d(TAG, "in running task exclude list do not kill " + basePkgName + " id:"
                                + topTaskId);
                        continue;
                    }
                    removeTask(am, taskInfo.persistentId);
                }
                // Gionee <yangxinruo> <2016-7-5> modify for CR01727240 end
            }
        }
        recentTasks = am.getRecentTasks(TASK_MAX,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        runTasks = am.getRunningTasks(TASK_MAX);
        Log.d(TAG, "task num = " + runTasks.size() + " recent task num = " + recentTasks.size()
                + " after remove task");
    }

    private static void removeTask(ActivityManager am, int taskId) {
        if (Build.VERSION.SDK_INT >= 22) {
            Log.d(TAG, "remove task task-id: " + taskId);
            FrameworkUtility.invokeMethod(ActivityManager.class, am, "removeTask", new Class[] {int.class},
                    new Object[] {(int) taskId});
        } else {
            Log.d(TAG, "remove task task-id(old version) : " + taskId);
            FrameworkUtility.invokeMethod(ActivityManager.class, am, "removeTask",
                    new Class[] {int.class, int.class}, new Object[] {(int) taskId, 0});
        }
    }

    // Gionee <xuwen><2015-08-31> add for CR01536073 begin
    public static List<String> getPowerSaveWhiteList(Context context) {
        String[] whitearray = null;

            whitearray = context.getResources().getStringArray(R.array.super_power_save_whitelist);

        // <!-- guoxt 2018-03-31 add for CSW1702A-3063 begin -->
        return Arrays.asList(whitearray);
    }
    // Gionee <xuwen><2015-08-31> add for CR01536073 end

    public static HashSet<String> getStartSuperModeForceKillList(Context context) {
        HashSet<String> res = new HashSet<String>();
        String[] blackKillArray = context.getResources().getStringArray(R.array.super_power_kill_blacklist);
        List<String> blackKillList = (List<String>) Arrays.asList(blackKillArray);
        res.addAll(blackKillList);
        return res;
    }

    public static HashSet<String> getExitSuperModeForceKillList(Context context) {
        HashSet<String> res = new HashSet<String>();
        String[] blackKillArray = context.getResources()
                .getStringArray(R.array.super_power_kill_exit_blacklist);
        List<String> blackKillList = (List<String>) Arrays.asList(blackKillArray);
        res.addAll(blackKillList);
        return res;
    }

    public static List<String> getSuperModeTaskComponentWhitelist(Context context) {
        String[] whitearray = context.getResources().getStringArray(R.array.super_power_save_component);
        return Arrays.asList(whitearray);
    }
}
