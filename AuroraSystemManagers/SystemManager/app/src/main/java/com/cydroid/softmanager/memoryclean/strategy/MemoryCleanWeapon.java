/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-12-27
 */
package com.cydroid.softmanager.memoryclean.strategy;

import android.content.Context;
import android.os.Bundle;

import com.cydroid.softmanager.memoryclean.filterrules.IMemoryCleanRule;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import java.util.List;

public interface MemoryCleanWeapon {
    void init(Context context);

    List<ProcessMemoryEntity> loadRunningPackages();

    List<ProcessMemoryEntity> filterRunningPackages(List<ProcessMemoryEntity> entities);

    List<ProcessMemoryEntity> filterRunningPackagesByRules(List<IMemoryCleanRule> rules, List<ProcessMemoryEntity> entities);

    void setParams(Bundle params);
}
