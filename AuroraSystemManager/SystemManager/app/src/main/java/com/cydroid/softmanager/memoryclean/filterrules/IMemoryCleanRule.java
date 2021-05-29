/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 规则接口
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.List;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

public interface IMemoryCleanRule {

    // 返回srcList中满足规则条件的List
    List<ProcessMemoryEntity> getFulfilledProcesses(IFilterRulesKit manager,
                                                    final List<ProcessMemoryEntity> srcList);
}
