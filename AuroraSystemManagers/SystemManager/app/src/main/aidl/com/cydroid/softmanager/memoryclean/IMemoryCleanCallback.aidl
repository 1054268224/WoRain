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

/**
 * 内存进程清理的回调接口。
 *
 * @author Houjie
 */
interface IMemoryCleanCallback {
    /**
     * 待清理进程已经筛选完毕，准备清理。
     *
     * @param processMemoryEntity - 将被清理的进程信息列表。
     */
    oneway void onMemoryCleanReady(in List<ProcessMemoryEntity> processMemoryEntity);

    /**
     * 清理执行完毕。
     *
     * @param totalProcesses - 最终被清理的进程总数。
     * @param totalPss - 最终被清理的进程的总PSS。
     */
    oneway void onMemoryCleanFinished(int totalProcesses, long totalPss);
}
