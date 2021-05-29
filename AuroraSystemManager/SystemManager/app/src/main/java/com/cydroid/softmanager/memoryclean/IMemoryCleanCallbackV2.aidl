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

/**
 * 内存进程清理的回调接口。
 *
 * @author Houjie
 */
interface IMemoryCleanCallbackV2 {
    /**
     * 待清理进程已经筛选完毕，准备清理。
     *
     * @param totalProcesses - 确定将被清理的进程总数。
     * @param totalPss - 确定将被清理的进程的总PSS。
     */
    oneway void onMemoryCleanReady(int totalProcesses, long totalPss);

    /**
     * 清理执行完毕。
     *
     * @param totalProcesses - 最终实际被清理的进程总数。
     * @param totalPss - 最终实际被清理的进程的总PSS。
     */
    oneway void onMemoryCleanFinished(int totalProcesses, long totalPss);
}
