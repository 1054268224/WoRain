/**
 * Copyright Statement:
 *
 * Company: Chenyee Communication Equipment Limited
 * Author: guoxt
 * Description: 排除当前使用桌面widget规则
 *
 * Date: 2018-4-27
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.app.ActivityManager.RunningAppProcessInfo;

public class RuleExcludeCurrentUseWidget implements IMemoryCleanRule {

    private ArrayList<String> mUseWidgetList;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return mUseWidgetList.contains(processEntity.mPackageName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        mUseWidgetList = HelperUtils.getEnableAppWidgetPackages(manager.getContext(),
                getRunningAppListFromEntityList(srcList));
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
            if (manager.getExcludeAppsRecorder() != null) {
                manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "camera");
            }
        }
        return resList;
    }

    private List<RunningAppProcessInfo> getRunningAppListFromEntityList(
            List<ProcessMemoryEntity> entityList) {
        ArrayList<RunningAppProcessInfo> resList = new ArrayList<RunningAppProcessInfo>();
        for (ProcessMemoryEntity entity : entityList) {
            if (entity.mProcess != null) {
                resList.add(entity.mProcess);
            }
        }
        return resList;
    }

}
