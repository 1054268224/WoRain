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

import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.memoryclean.filterrules.IMemoryCleanRule;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCts;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentInputMethod;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLauncher;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLiveWallpaper;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentTop;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseAudioChannel;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseCamera;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseSensor;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeExplicity;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagCantSaveState;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagPersistent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeHighOomAdj;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeHightImportance;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeRecentAndFrequent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeRomApps;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeScreenOffWhitelisted;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeSystemWhitelisted;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeUserWhitelisted;
import com.cydroid.softmanager.memoryclean.filterrules.RuleIncludeBlacklisted;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseWidget;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

//锁屏清理使用
public class AssultRifle extends Weapon {
    private static final String TAG = "AssultRifle";
    private static final List<IMemoryCleanRule> includeAppRules = new ArrayList<IMemoryCleanRule>();

    static {
        includeAppRules.add(new RuleIncludeBlacklisted());
    }

    @Override
    public List<ProcessMemoryEntity> filterRunningPackages(List<ProcessMemoryEntity> entities) {
        List<ProcessMemoryEntity> mustBeCleanProcesses = filterRunningPackagesByIncludeRules(includeAppRules,
                entities);
        entities.removeAll(mustBeCleanProcesses);
        boolean isNightMode = false;
        if (mParams != null) {
            isNightMode = mParams.getBoolean("is_night_mode", false);
        }
        List<ProcessMemoryEntity> toBeCleanedApps = filterRunningPackagesByRules(getExcludeRules(isNightMode),
                entities);
        toBeCleanedApps.addAll(mustBeCleanProcesses);
        return toBeCleanedApps;
    }

    protected List<IMemoryCleanRule> getExcludeRules(boolean isNightMode) {
        List<IMemoryCleanRule> excludeAppRules = new ArrayList<IMemoryCleanRule>();
        excludeAppRules.add(new RuleExcludeCts());
        excludeAppRules.add(new RuleExcludeFlagCantSaveState());
        excludeAppRules.add(new RuleExcludeFlagPersistent());
        excludeAppRules.add(new RuleExcludeSystemWhitelisted());
        excludeAppRules.add(new RuleExcludeScreenOffWhitelisted());
        excludeAppRules.add(new RuleExcludeUserWhitelisted());
        // Chenyee <guoxt> <2018-05-05> add for CSW1703VF-53 begin
        if (!isNightMode) {
            if(!Consts.cy1703VF) {
               excludeAppRules.add(new RuleExcludeRomApps());
            }
        } else {
            Log.d(TAG, "night mode now ,do not use RuleExcludeRomApps");
        }
        // Chenyee <guoxt> <2018-05-05> add for CSW1703VF-53 end
        excludeAppRules.add(new RuleExcludeCurrentUseAudioChannel());
        excludeAppRules.add(new RuleExcludeCurrentUseSensor());
        excludeAppRules.add(new RuleExcludeCurrentUseCamera());
        excludeAppRules.add(new RuleExcludeCurrentLauncher());
        excludeAppRules.add(new RuleExcludeCurrentTop());
        excludeAppRules.add(new RuleExcludeExplicity());
        excludeAppRules.add(new RuleExcludeCurrentInputMethod());
        excludeAppRules.add(new RuleExcludeCurrentLiveWallpaper());
        excludeAppRules.add(new RuleExcludeHighOomAdj());
        //Chenyee guoxt modify for CSW1703A-1870 begin
        excludeAppRules.add(new RuleExcludeCurrentUseWidget());
        //Chenyee guoxt modify for CSW1703A-1870 end
        // Chenyee <guoxt> <2018-05-05> add for CSW1703VF-53 begin
        if(!Consts.cy1703VF) {
            excludeAppRules.add(new RuleExcludeRecentAndFrequent());// must be the finally rule
        }
        // Chenyee <guoxt> <2018-05-05> add for CSW1703VF-53 end
        return excludeAppRules;
    }

    private List<ProcessMemoryEntity> filterRunningPackagesByIncludeRules(List<IMemoryCleanRule> rules,
            List<ProcessMemoryEntity> entities) {
        List<ProcessMemoryEntity> resList = new ArrayList<ProcessMemoryEntity>();
        for (IMemoryCleanRule rule : rules) {
            resList.addAll(rule.getFulfilledProcesses(this, entities));
        }
        return resList;
    }
}
