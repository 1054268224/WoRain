/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除锁屏清理白名单规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;

public class RuleExcludeScreenOffWhitelisted implements IMemoryCleanRule {

    private List<String> mScreenOffWhitelistedApps;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return mScreenOffWhitelistedApps.contains(processEntity.mPackageName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        WhiteListManager whiteListManager = WhiteListManager.getInstance();
        whiteListManager.init(manager.getContext());
        mScreenOffWhitelistedApps = whiteListManager.getScreenOffWhiteApps();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
            if (manager.getExcludeAppsRecorder() != null) {
                manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "whitelist");
            }
        }
        return resList;
    }

}
