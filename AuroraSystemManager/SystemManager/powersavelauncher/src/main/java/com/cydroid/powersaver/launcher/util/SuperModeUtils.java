package com.cydroid.powersaver.launcher.util;

import java.util.Arrays;
import java.util.List;

import com.cydroid.powersaver.launcher.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Build;

public class SuperModeUtils {
    private static final String TAG = "SuperModeUtils";
    private static final int TASK_MAX = 50;

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

        for (int i = 0; i < runTasks.size(); i++) {
            RunningTaskInfo taskInfo = runTasks.get(i);
            // Gionee <yangxinruo> <2015-08-12> add for CR01525446 begin
            String basePkgName = "";
            if (taskInfo.baseActivity != null)
                basePkgName = taskInfo.baseActivity.getPackageName();
            Log.d(TAG, "process running task: intent=" + taskInfo.baseActivity + " pkg=" + basePkgName);
            if (whitelist.contains(basePkgName)) {
                Log.d(TAG, "in task whitelist do not kill " + basePkgName);
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
                ActivityManager.RECENT_IGNORE_UNAVAILABLE /*| ActivityManager.RECENT_INCLUDE_PROFILES*/);
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
                    removeTask(am, taskInfo.persistentId);
                }
                // Gionee <yangxinruo> <2016-7-5> modify for CR01727240 end
            }
        }

        recentTasks = am.getRecentTasks(TASK_MAX,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE/* | ActivityManager.RECENT_INCLUDE_PROFILES*/);
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
}
