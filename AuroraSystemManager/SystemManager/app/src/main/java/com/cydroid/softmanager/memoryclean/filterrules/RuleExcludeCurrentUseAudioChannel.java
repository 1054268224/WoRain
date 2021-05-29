/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 排除当前占用音频规则
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.HelperUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class RuleExcludeCurrentUseAudioChannel implements IMemoryCleanRule {
    private ArrayList<String> mAudioUserList;

    private boolean isFulfilled(IFilterRulesKit manager, ProcessMemoryEntity processEntity) {
        return mAudioUserList.contains(processEntity.mPackageName);
    }

    @Override
    public List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
            List<ProcessMemoryEntity> srcList) {
        ArrayList<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        mAudioUserList = HelperUtils.getActiveAudioTrackApps(manager.getContext().getPackageManager(),
                getRunningAppListFromEntityList(manager.getContext(), srcList));
        for (ProcessMemoryEntity srcProcess : srcList) {
            if (isFulfilled(manager, srcProcess)) {
                resList.add(srcProcess);
            }
            if (manager.getExcludeAppsRecorder() != null) {
                manager.getExcludeAppsRecorder().record(srcProcess.mPackageName, "audio");
            }
        }
        return resList;
    }

    private List<RunningAppProcessInfo> getRunningAppListFromEntityList(Context context,
            List<ProcessMemoryEntity> entityList) {
//        Log.d(TAG, "entityList size = " + entityList.size());
//        ArrayList<RunningAppProcessInfo> resList = new ArrayList<RunningAppProcessInfo>();
//        for (ProcessMemoryEntity entity : entityList) {
//            if (entity.mProcess != null) {
//                resList.add(entity.mProcess);
//            }
//        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> resList = am.getRunningAppProcesses();
        return resList;
    }
}
