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

import com.cydroid.softmanager.memoryclean.IMemoryCleanCallback;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

/**
 * 客户端调用接口。客户端通过调用下面的 AIDL 接口与系统管家的服务进行交互,使用内存清理的相关功能。
 * Service Intent action:"com.cydroid.softmanager.memoryclean.action.bindcleanservice"
 *
 * @author Houjie
 */
interface IMemoryCleanService {
    /**
     * 根据内存清理类型，筛选并清理当前系统内运行的进程
     *
     * @param cleanType - 内存清理的类型。
     * @see #CLEAN_TYPE_ROCKET  0 可用于系统管家小火箭清理。
     * @see #CLEAN_TYPE_ASSAULT_RIFLE   1 可用于系统管家的锁屏清理。
     * @see #CLEAN_TYPE_CANNON  2 可用于类似SystemUI的清理。
     * @see #ClEAN_TYPE_RUBBISH 3 可用于系统管家垃圾清理中的内存清理。
     *
     * @param callback - 回调接口，可以为空。
     */
    oneway void memoryClean(int cleanType, IMemoryCleanCallback callback);

    /*
     * 根据包名，清理对应进程。
     *
     * @param packageName - 将被清理的进程包名。
     * @param callback - 回调接口，可以为空。
     */
    //void appMemoryClean(String packageName, IMemoryCleanCallback callback);


    /**
     * 获取正在运行的进程列表，此接口目前仅供系统管家垃圾清理内存清理使用，
     * 列表筛选规则为系统管家垃圾清理内存清理进程筛选规则。
     *
     * @param cleanType - 内存清理的类型。
     * @see #CLEAN_TYPE_ROCKET  1 可用于系统管家小火箭清理。
     * @see #CLEAN_TYPE_ASSAULT_RIFLE   2 可用于系统管家的锁屏清理。
     * @see #CLEAN_TYPE_CANNON  3 可用于类似SystemUI的清理。
     * @see #ClEAN_TYPE_RUBBISH 4 可用于系统管家垃圾清理中的内存清理。
     *
     * @return 筛选出的正在运行的进程内存信息列表。
     */
    List<ProcessMemoryEntity> getRunningProcessMemoryEntitys(int cleanType);

    /**
     * 清理列表内的所有进程，此接口目前仅供系统管家垃圾清理内存清理使用。
     *
     * @param entities - 将被清理的进程内存信息列表。
     * @param callback - 回调接口，可以为空。
     */
    oneway void cleanProcessMemoryEntitys(in List<ProcessMemoryEntity> entities,
            IMemoryCleanCallback callback);
}
