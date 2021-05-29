/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot;

import android.content.Context;

import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.interfaces.ThemeChangedCallback;

import java.util.List;

public class AutoBootAppManager {
    private static AutoBootAppManager sInstance;

    public static final String ENABLE_AUTO_BOOT_APP_PREFERENCES_NAME =
            "com.cydroid.softmanager.enable_autoboot_preferences";
    public static final String ENABLE_AUTO_BOOT_APP_NAMES_KEY = "enable_autoboot_apps";

    public static final int VERSION_COMPONENT_LEVEL = 0;
    public static final int VERSION_PROCESS_LEVEL = 1;

    private final AutoBootAppManagerInterface mImpl;

    public static synchronized AutoBootAppManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new AutoBootAppManager(context);
        }
        return sInstance;
    }

    private AutoBootAppManager(Context context) {
        int version = getAutoBootAppManagerVersion(context);
        mImpl = AutoBootAppManagerFactory.getAutoBootAppManagerImpl(version);
    }

    public static int getAutoBootAppManagerVersion(Context context) {
        return VERSION_PROCESS_LEVEL;
    }

    public synchronized void init(Context context) {
        mImpl.init(context);
    }

    public synchronized List<AutoBootAppInfo> getEnableAutoBootApps() {
        return mImpl.getEnableAutoBootApps();
    }

    public synchronized List<AutoBootAppInfo> getDisableAutoBootApps() {
        return mImpl.getDisableAutoBootApps();
    }

    public synchronized void enableAutoBootApp(String packageName) {
        mImpl.enableAutoBootApp(packageName);
    }

    public synchronized void disableAutoBootApp(String packageName) {
        mImpl.disableAutoBootApp(packageName);
    }

    public void setAutoBootAppsChangeCallBack(String key, StateChangeCallback callback) {
        mImpl.setAutoBootAppsChangeCallBack(key, callback);
    }

    public void unsetAutoBootAppsChangeCallBack(String key) {
        mImpl.unsetAutoBootAppsChangeCallBack(key);
    }

    public void setLocalChangeCallBack(String key, LocalChangedCallback callback) {
        mImpl.setLocalChangeCallBack(key, callback);
    }

    public void unsetLocalChangeCallBack(String key) {
        mImpl.unsetLocalChangeCallBack(key);
    }

    public void setThemeChangedCallback(String key, ThemeChangedCallback callback) {
        mImpl.setThemeChangedCallback(key, callback);
    }

    public void unsetThemeChangedCallback(String key) {
        mImpl.unsetThemeChangedCallback(key);
    }
}