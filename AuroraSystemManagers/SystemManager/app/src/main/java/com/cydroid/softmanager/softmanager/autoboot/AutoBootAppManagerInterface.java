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

public interface AutoBootAppManagerInterface {
    void init(Context context);

    List<AutoBootAppInfo> getEnableAutoBootApps();

    List<AutoBootAppInfo> getDisableAutoBootApps();

    void enableAutoBootApp(String packageName);

    void disableAutoBootApp(String packageName);

    void setAutoBootAppsChangeCallBack(String key, StateChangeCallback callback);

    void unsetAutoBootAppsChangeCallBack(String key);

    void setLocalChangeCallBack(String key, LocalChangedCallback callback);

    void unsetLocalChangeCallBack(String key);

    void enableAutoBootAppByInstall(String packageName);

    void disableAutoBootAppByInstall(String packageName);

    void setThemeChangedCallback(String key, ThemeChangedCallback callback);

    void unsetThemeChangedCallback(String key);
}