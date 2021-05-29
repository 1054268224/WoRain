/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 卡顿跳帧组件相关数据收集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.skipframes;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.text.format.DateFormat;

public class ComponentInfoCollector implements ISystemInfoCollector {

    private final long mDuration;
    private final Context mContext;
    private static final int ACTIVITY_SCENCE_TIME_SPAN = 5000;// ms
    private static final String TAG = "ComponentInfoCollector";

    public ComponentInfoCollector(Context context, int pid, long duration) {
        mDuration = duration;
        mContext = context;
    }

    @Override
    public void record(SkipFramesInfo skipinfoData) {
        String topActivity = getTopActivity(mContext);
        String lastActivity = getActivityScenceInfoMessage(mDuration + ACTIVITY_SCENCE_TIME_SPAN,
                topActivity);

        skipinfoData.componentInfo.currentTopActivity = topActivity;
        skipinfoData.componentInfo.lastTopActivity = lastActivity;

    }

    private String getActivityScenceInfoMessage(long timespan, String topActivity) {
        Stack<String> activityStack = new Stack<String>();
        long time = System.currentTimeMillis() - timespan;
        String beginTimeStr = formatTimestamp(time);
        String resultStr = HelperUtils
                .executeShellCmd("logcat -v threadtime -t '" + beginTimeStr + "' -b events|grep 'am_on'");
        if (!"".equals(resultStr)) {
            String rex = "am_on_(.+?)_called\\:\\s\\[\\d+,(.+?)\\]";
            Pattern patterndiff = Pattern.compile(rex);
            Matcher matcher = patterndiff.matcher(resultStr);
            while (matcher.find()) {
                String actionName = matcher.group(1);
                String activityName = matcher.group(2);
                Log.d(TAG, "getActivityScenceInfoMessage action:" + actionName + " activityName:"
                        + activityName);
                if (actionName != null && !actionName.isEmpty()) {
                    activityStack.push(actionName + "/" + activityName);
                }
            }
            String[] tmpArray = topActivity.split("/");
            String currentActivity = "";
            if (tmpArray.length > 1) {
                currentActivity = tmpArray[1];
            }
            if (currentActivity.isEmpty()) {
                return "";
            }
            boolean isFindCurrent = false;
            while (activityStack.size() > 0) {
                String[] activityRecord = activityStack.pop().split("/");
                if (activityRecord[0].equals("resume") && activityRecord[1].equals(currentActivity)) {
                    isFindCurrent = true;
                    continue;
                }
                if (isFindCurrent && activityRecord[0].equals("paused")) {
                    return activityRecord[1];
                }
            }
        }
        return "";
    }

    private String getTopActivity(Context context) {
        String pkgName = "";
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).baseActivity != null) {
                pkgName = runningTaskInfos.get(0).baseActivity.flattenToString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgName;
    }

    private String formatTimestamp(long time) {
        String beginTimeStr = DateFormat.format("MM-dd kk:mm:ss", time).toString();
        long millisec = time % 1000;
        if (millisec > 0) {
            beginTimeStr += "." + millisec;
        }
        return beginTimeStr;
    }
}
