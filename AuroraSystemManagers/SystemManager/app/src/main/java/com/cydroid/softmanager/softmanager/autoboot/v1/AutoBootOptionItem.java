/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot.v1;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class AutoBootOptionItem {
    private static final String TAG = "AutoBootOptionItem";
    public static final int SYS_BOOT_AUTO_BOOT_TYPE = 1;
    public static final int BACKGROUND_AUTO_BOOT_TYPE = 2;

    //private String mPackageName;
    private int mAutoBootType = 0;
    private boolean mAutoBootEnabled = false;
    private final List<ComponentName> mAutoBootComponents = new ArrayList<ComponentName>();

    public AutoBootOptionItem(String name, int type) {
        //mPackageName = name;
        mAutoBootType = type;
    }

    public boolean isAutoBoot() {
        return mAutoBootEnabled;
    }

    public void enableAutoBoot(PackageManager pm) {
        for (ComponentName name : mAutoBootComponents) {
            try {
                pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.e(TAG, "enableAutoBoot name:" + name + " e:" + e);
            }
        }
        mAutoBootEnabled = true;
    }

    public void disableAutoBoot(PackageManager pm) {
        for (ComponentName name : mAutoBootComponents) {
            try {
                pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.e(TAG, "disableAutoBoot name:" + name + " e:" + e);
            }
        }
        mAutoBootEnabled = false;
    }

    public int getAutoBootType() {
        return mAutoBootType;
    }

    public void checkAutoBootState(PackageManager pm) {
        for (ComponentName name : mAutoBootComponents) {
            try {
                int stat = pm.getComponentEnabledSetting(name);
                if (stat != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        && stat != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                    mAutoBootEnabled = true;
                    break;
                }
            } catch (Exception e) {
                mAutoBootEnabled = true;
                Log.d(TAG, "checkAutoBootAppState e:" + e.toString());
            }
        }
    }

    public void addAutoBootComponent(ComponentName autoBootComponent) {
        mAutoBootComponents.add(autoBootComponent);
    }

    public boolean containsAutoBootComponent(ComponentName autoBootComponent) {
        return mAutoBootComponents.contains(autoBootComponent);
    }

    public void clearAutoBootComponent() {
        mAutoBootComponents.clear();
    }
}
