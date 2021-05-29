/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-12-27
 */
package com.cydroid.softmanager.memoryclean;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import java.util.List;

public interface IMemoryCleanNativeCallback {
    void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys);
    void onMemoryCleanFinished(int totalProcesses, long totalPss);
}
