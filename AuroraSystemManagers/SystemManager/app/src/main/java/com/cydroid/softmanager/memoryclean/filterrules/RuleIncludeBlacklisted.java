/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 确认黑名单规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.HelperUtils;

public class RuleIncludeBlacklisted implements IMemoryCleanRule {

    private Set<String> mBlackList;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return mBlackList.contains(processEntity.mPackageName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        mBlackList = HelperUtils.getBlackKillList(manager.getContext());
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
        }
        return resList;
    }

}
