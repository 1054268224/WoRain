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

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class UninstallAppSortByName implements IUninstallAppSort {

    public List<UninstallAppInfo> sortUninstallApps(List<UninstallAppInfo> uninstallAppInfos) {
        Collections.sort(uninstallAppInfos, new Comparator<UninstallAppInfo>() {
            @Override
            public int compare(UninstallAppInfo lhs, UninstallAppInfo rhs) {
                return Collator.getInstance(Locale.CHINESE).compare(lhs.getTitle(), rhs.getTitle());
            }
        });
        return uninstallAppInfos;
    }
}