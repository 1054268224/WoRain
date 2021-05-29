/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除桌面无入口应用规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import android.content.pm.PackageManager;

public class RuleExcludeNoLauncherIntent implements IMemoryCleanRule {

    private PackageManager mPackageManager;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return mPackageManager.getLaunchIntentForPackage(processEntity.mPackageName) == null;
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        mPackageManager = manager.getContext().getPackageManager();
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
        }
        return resList;
    }

}
