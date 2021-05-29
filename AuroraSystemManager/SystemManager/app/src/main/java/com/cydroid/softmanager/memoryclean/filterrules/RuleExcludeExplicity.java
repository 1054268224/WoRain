/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除指定应用规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

public class RuleExcludeExplicity implements IMemoryCleanRule {

    private static final String[] sExplicitPkgNames = {"com.oupeng.max", "com.oupeng.max.sdk"};

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        for (String pkgName : sExplicitPkgNames) {
            if (pkgName.equals(processEntity.mPackageName)) {
                return true;
            }
        }
        return false;
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
                manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "special");
            }
        }
        return resList;
    }

}
