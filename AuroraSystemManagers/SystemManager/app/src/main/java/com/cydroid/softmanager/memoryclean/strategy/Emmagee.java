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
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeEncryptionSpaceApps;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeFlagPersistent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentInputMethod;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeNoLauncherIntent;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeRomApps;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeSystemWhitelisted;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeUserWhitelisted;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLiveWallpaper;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentUseAudioChannel;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentLauncher;
import com.cydroid.softmanager.memoryclean.filterrules.RuleExcludeCurrentTop;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import java.util.ArrayList;
import java.util.List;

//垃圾清理使用
public class Emmagee extends Weapon {
    private static final String TAG = "Emmagee";
    private static final List<IMemoryCleanRule> romAppRules = new ArrayList<IMemoryCleanRule>();
    static {
        romAppRules.add(new RuleExcludeNoLauncherIntent());
        romAppRules.add(new RuleExcludeFlagPersistent());
        romAppRules.add(new RuleExcludeSystemWhitelisted());
        romAppRules.add(new RuleExcludeCurrentInputMethod());
        romAppRules.add(new RuleExcludeCurrentUseAudioChannel());
        romAppRules.add(new RuleExcludeCurrentLauncher());
        romAppRules.add(new RuleExcludeCurrentTop());
    }

    private static final List<IMemoryCleanRule> userAppRules = new ArrayList<IMemoryCleanRule>();
    static {
        userAppRules.add(new RuleExcludeCurrentInputMethod());
        userAppRules.add(new RuleExcludeCurrentLiveWallpaper());
        userAppRules.add(new RuleExcludeCurrentUseAudioChannel());
        userAppRules.add(new RuleExcludeCurrentLauncher());
        userAppRules.add(new RuleExcludeSystemWhitelisted());
    }

    @Override
    public List<ProcessMemoryEntity> filterRunningPackages(List<ProcessMemoryEntity> entities) {
        RuleExcludeRomApps ruleExcludeRomApps = new RuleExcludeRomApps();
        List<ProcessMemoryEntity> romApps = ruleExcludeRomApps.getFulfilledProcesses(this, entities);
        List<ProcessMemoryEntity> userApps = new ArrayList<ProcessMemoryEntity>();
        userApps.addAll(entities);
        userApps.removeAll(romApps);

        //romApps
        romApps = filterRunningPackagesByRules(romAppRules, romApps);
        configInUserWhiteAppListEntities(romApps);

        //userApps
        userApps = filterRunningPackagesByRules(userAppRules, userApps);
        configInUserWhiteAppListEntities(userApps);
        
        configPrivateEntities(romApps);
        configPrivateEntities(userApps);
        
        List<ProcessMemoryEntity> ret = new ArrayList<ProcessMemoryEntity>();
        ret.addAll(romApps);
        ret.addAll(userApps);
        return ret;
    }

    private void configInUserWhiteAppListEntities(List<ProcessMemoryEntity> apps) {
        RuleExcludeUserWhitelisted ruleExcludeUserWhitelisted = new RuleExcludeUserWhitelisted();
        List<ProcessMemoryEntity> userWhiteEntities =
                ruleExcludeUserWhitelisted.getFulfilledProcesses(this, apps);
        for (ProcessMemoryEntity entity: userWhiteEntities) {
            entity.mIsInUserWhiteAppList = true;
        }
    }

    private void configPrivateEntities(List<ProcessMemoryEntity> apps) {
        RuleExcludeEncryptionSpaceApps ruleExcludeEncryptionSpaceApps = new RuleExcludeEncryptionSpaceApps();
        List<ProcessMemoryEntity> privateEntities =
                ruleExcludeEncryptionSpaceApps.getFulfilledProcesses(this, apps);
        for (ProcessMemoryEntity entity: privateEntities) {
            entity.mIsPrivateApp = true;
        }
    }
}
