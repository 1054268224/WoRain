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

import com.cydroid.softmanager.memoryclean.filterrules.IMemoryCleanRule;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCts;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLauncher;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLiveWallpaper;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentTop;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseAudioChannel;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeExplicity;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagCantSaveState;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagPersistent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeHightImportance;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeSystemWhitelisted;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeUserWhitelisted;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentInputMethod;

import java.util.ArrayList;
import java.util.List;

//SystemUI使用
public class Cannon extends Weapon {
    private static final String TAG = "Cannon";

    private static final List<IMemoryCleanRule> excludeAppRules = new ArrayList<IMemoryCleanRule>();

    static {
        excludeAppRules.add(new RuleExcludeFlagCantSaveState());
        excludeAppRules.add(new RuleExcludeFlagPersistent());
        excludeAppRules.add(new RuleExcludeSystemWhitelisted());
        excludeAppRules.add(new RuleExcludeUserWhitelisted());
        excludeAppRules.add(new RuleExcludeHightImportance());
        excludeAppRules.add(new RuleExcludeCurrentUseAudioChannel());
        excludeAppRules.add(new RuleExcludeCurrentLauncher());
        excludeAppRules.add(new RuleExcludeCurrentTop());
        excludeAppRules.add(new RuleExcludeExplicity());
        excludeAppRules.add(new RuleExcludeCurrentLiveWallpaper());
        excludeAppRules.add(new RuleExcludeCts());
        excludeAppRules.add(new RuleExcludeCurrentInputMethod());
    }

    @Override
    public List<ProcessMemoryEntity> filterRunningPackages(List<ProcessMemoryEntity> entities) {
        List<ProcessMemoryEntity> toBeCleanedApps = filterRunningPackagesByRules(excludeAppRules, entities);
        return toBeCleanedApps;
    }
}
