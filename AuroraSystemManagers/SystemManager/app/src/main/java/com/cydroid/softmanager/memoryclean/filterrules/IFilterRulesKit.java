/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-12-27
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import com.cydroid.softmanager.powersaver.analysis.collector.MemoryCleanExcludeAppsRecorder;

import android.content.Context;

public interface IFilterRulesKit {
    Context getContext();
    MemoryCleanExcludeAppsRecorder getExcludeAppsRecorder();
}
