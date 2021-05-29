/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除当前输入法规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.HelperUtils;

import android.view.inputmethod.InputMethodInfo;

public class RuleExcludeCurrentInputMethod implements IMemoryCleanRule {

    private String mInputMethodPkgName;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return processEntity.mPackageName.equals(mInputMethodPkgName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        InputMethodInfo inputMethodInfo = HelperUtils.getDefInputMethod(manager.getContext());
        if (inputMethodInfo == null) {
            return resList;
        }
        mInputMethodPkgName = inputMethodInfo.getPackageName();
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
                if (manager.getExcludeAppsRecorder() != null) {
                    manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "ime");
                }
            }
        }
        return resList;
    }

}
