/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除当前栈顶应用规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

public class RuleExcludeCurrentTop implements IMemoryCleanRule {

    private String mBasePkgName;
    private String mTopPkgName;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return processEntity.mPackageName.equals(mBasePkgName)
                || processEntity.mPackageName.equals(mTopPkgName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        mBasePkgName = getBaseActivityPackageName(manager.getContext());
        mTopPkgName = getTopActivityPackageName(manager.getContext());
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
            if (manager.getExcludeAppsRecorder() != null) {
                manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "top");
            }
        }
        return resList;
    }

    // Gionee <yangxinruo> <2016-1-20> add for CR01624978 begin
    private String getTopActivityPackageName(Context context) {
        String pkgName = null;
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).topActivity != null) {
                pkgName = runningTaskInfos.get(0).topActivity.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgName;
    }
    // Gionee <yangxinruo> <2016-1-20> add for CR01624978 end

    // Gionee <liuyb> <2014-08-12> add for CR01349248 begin
    // Gionee <yangxinruo> <2016-1-20> modify for CR01624978 begin
    private String getBaseActivityPackageName(Context context) {
        String pkgName = null;
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null && runningTaskInfos.get(0) != null
                    && runningTaskInfos.get(0).baseActivity != null) {
                pkgName = runningTaskInfos.get(0).baseActivity.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pkgName;
    }
    // Gionee <yangxinruo> <2016-1-20> modify for CR01624978 end
    // Gionee <liuyb> <2014-08-12> add for CR01349248 end
}
