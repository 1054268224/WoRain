/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-04-22
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.HelperUtils;

public class RuleExcludeCts implements IMemoryCleanRule {
    private static final String CTS_PACKAGE_PREFIX = "com.android.cts";
    
    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return processEntity.mPackageName.startsWith(CTS_PACKAGE_PREFIX);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
                if (manager.getExcludeAppsRecorder() != null) {
                    manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "cts");
                }
            }
        }
        return resList;
    }
}
