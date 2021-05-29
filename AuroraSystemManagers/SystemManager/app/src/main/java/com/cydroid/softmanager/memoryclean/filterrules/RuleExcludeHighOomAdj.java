/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除高OOM ADJ规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import android.os.Process;

public class RuleExcludeHighOomAdj implements IMemoryCleanRule {

    private static final int EXCLUDE_OOM_ADJ_THRESHOLD = 2;
    // Gionee <yangxinruo> <2016-1-20> add for CR01627879 begin
    private static final int[] SINGLE_LONG_FORMAT = new int[] {
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG};
    // Gionee <yangxinruo> <2016-1-20> add for CR01627879 end

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        int oomAdj = getAdjValue(processEntity.mProcess.pid);
        return oomAdj < EXCLUDE_OOM_ADJ_THRESHOLD;
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
                manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "adj");
            }
        }
        return resList;
    }

    // Gionee <yangxinruo> <2016-1-20> add for CR01627879 begin
    private int getAdjValue(int pid) {
        long[] longTmp = {(long) Integer.MAX_VALUE};// new long[1];
        Process.readProcFile("/proc/" + pid + "/oom_adj", SINGLE_LONG_FORMAT, null, longTmp, null);
        return (int) longTmp[0];
    }
    // Gionee <yangxinruo> <2016-1-20> add for CR01627879 end

}
