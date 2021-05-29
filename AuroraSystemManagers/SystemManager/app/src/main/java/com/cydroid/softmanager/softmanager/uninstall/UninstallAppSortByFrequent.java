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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UninstallAppSortByFrequent implements IUninstallAppSort {

    public List<UninstallAppInfo> sortUninstallApps(List<UninstallAppInfo> uninstallAppInfos) {
        Collections.sort(uninstallAppInfos, new Comparator<UninstallAppInfo>() {
            @Override
            public int compare(UninstallAppInfo lhs, UninstallAppInfo rhs) {
                Long first = lhs.getUseFrequency();
                Long second = rhs.getUseFrequency();
                return second.compareTo(first);
            }
        });
        return uninstallAppInfos;
    }
}
