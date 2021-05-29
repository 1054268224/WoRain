/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除当前使用相机规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.HelperUtils;

import android.app.ActivityManager.RunningAppProcessInfo;

public class RuleExcludeCurrentUseCamera implements IMemoryCleanRule {

    private ArrayList<String> mCameraUserList;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return mCameraUserList.contains(processEntity.mPackageName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        mCameraUserList = HelperUtils.getActiveCameraApps(manager.getContext().getPackageManager(),
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
