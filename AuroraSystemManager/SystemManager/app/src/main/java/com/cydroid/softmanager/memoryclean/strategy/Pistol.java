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
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentInputMethod;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLauncher;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLiveWallpaper;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeEncryptionSpaceApps;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagCantSaveState;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagPersistent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeNoLauncherIntent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeRomApps;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeSystemWhitelisted;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

//高耗电以及正在运行应用列表使用
public class Pistol extends Weapon {
    private static final String TAG = "Pistol";

    @Override
    public List<ProcessMemoryEntity> filterRunningPackages(List<ProcessMemoryEntity> entities) {
        boolean isThirdAppOnly = true;
        boolean isLauncherShowed = false;
        if (mParams != null) {
            isThirdAppOnly = mParams.getBoolean("is_third_only", true);
            isLauncherShowed = mParams.getBoolean("is_launcher_show", false);
        }
        List<ProcessMemoryEntity> toBeCleanedApps = filterRunningPackagesByRules(
                getExcludeRules(isThirdAppOnly, isLauncherShowed), entities);
        return toBeCleanedApps;
    }

    protected List<IMemoryCleanRule> getExcludeRules(boolean isThirdAppOnly, boolean isLauncherShowed) {
        List<IMemoryCleanRule> excludeAppRules = new ArrayList<IMemoryCleanRule>();
        if (isThirdAppOnly) {
            excludeAppRules.add(new RuleExcludeRomApps());
        } else {
            Log.d(TAG, "ThirdAppOnly ,do not use RuleExcludeRomApps");
        }
        if (isLauncherShowed) {
            excludeAppRules.add(new RuleExcludeNoLauncherIntent());
            excludeAppRules.add(new RuleExcludeFlagCantSaveState());
            excludeAppRules.add(new RuleExcludeFlagPersistent());
        } else {
            Log.d(TAG, "isLauncherShowed ,do not use RuleExcludeNoLauncherIntent");
        }
        excludeAppRules.add(new RuleExcludeSystemWhitelisted());
//        excludeAppRules.add(new RuleExcludeEncryptionSpaceApps());
        excludeAppRules.add(new RuleExcludeCurrentLauncher());
        excludeAppRules.add(new RuleExcludeCurrentLiveWallpaper());
        excludeAppRules.add(new RuleExcludeCurrentInputMethod());
        return excludeAppRules;
    }
}
