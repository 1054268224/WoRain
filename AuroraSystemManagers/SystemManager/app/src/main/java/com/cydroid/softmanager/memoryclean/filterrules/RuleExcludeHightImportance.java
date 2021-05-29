/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除高进程优先级规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import android.app.ActivityManager.RunningAppProcessInfo;

public class RuleExcludeHightImportance implements IMemoryCleanRule {

    private static final int EXCLUDE_PROCESS_IMPORTANCE_THRESHOLD = 300;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return processEntity.mProcess.importance < EXCLUDE_PROCESS_IMPORTANCE_THRESHOLD;
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
            if (manager.getExcludeAppsRecorder() != null) {
                manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "importance");
            }
        }
        return resList;
    }

}
