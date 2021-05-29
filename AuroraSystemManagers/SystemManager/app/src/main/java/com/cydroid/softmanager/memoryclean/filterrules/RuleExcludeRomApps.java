/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除ROM系统应用规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

public class RuleExcludeRomApps implements IMemoryCleanRule {

    private ApplicationInfoManager mApplicationInfoManager;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return mApplicationInfoManager.isSystemApp(processEntity.mPackageName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        mApplicationInfoManager = ApplicationInfoManager.getInstance(manager.getContext());
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
        }
        return resList;
    }

}
