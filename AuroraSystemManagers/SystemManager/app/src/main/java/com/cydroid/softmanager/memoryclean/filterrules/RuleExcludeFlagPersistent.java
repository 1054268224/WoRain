/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除FLAG_PERSISITENT规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import android.app.ActivityManager.RunningAppProcessInfo;

public class RuleExcludeFlagPersistent implements IMemoryCleanRule {

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return (processEntity.mProcess.flags & RunningAppProcessInfo.FLAG_PERSISTENT) != 0;
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
        }
        return resList;
    }

}
