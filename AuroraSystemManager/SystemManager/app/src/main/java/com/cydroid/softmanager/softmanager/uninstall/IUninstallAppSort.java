/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.uninstall;

import java.util.List;

public interface IUninstallAppSort {
    List<UninstallAppInfo> sortUninstallApps(List<UninstallAppInfo> uninstallAppInfos);
}