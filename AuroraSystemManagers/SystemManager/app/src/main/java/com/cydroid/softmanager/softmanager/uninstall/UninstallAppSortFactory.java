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

import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class UninstallAppSortFactory {
    private static final String TAG = "UninstallAppSortFactory";

    private static UninstallAppSortFactory sInstance;

    private static final List<Class<?>> UNINSTALL_APP_SORT_LIST = new ArrayList<>();

    static {
        UNINSTALL_APP_SORT_LIST.add(UninstallAppSortByName.class);
        UNINSTALL_APP_SORT_LIST.add(UninstallAppSortByTime.class);
        UNINSTALL_APP_SORT_LIST.add(UninstallAppSortByFrequent.class);
        UNINSTALL_APP_SORT_LIST.add(UninstallAppSortBySize.class);
    }

    public static synchronized UninstallAppSortFactory getInstance() {
        if (null == sInstance) {
            sInstance = new UninstallAppSortFactory();
        }
        return sInstance;
    }

    private UninstallAppSortFactory() {
    }

    public IUninstallAppSort createUninstallAppSort(int sortType) {
        try {
            Class<?> uninstallAppSortClass = UNINSTALL_APP_SORT_LIST.get(sortType);
            if (uninstallAppSortClass != null) {
                return createUninstallAppSort(uninstallAppSortClass);
            }
        } catch (Exception e) {
            Log.e(TAG, "createUninstallAppSort exception:" + e.toString());
        }
        return null;
    }

    private IUninstallAppSort createUninstallAppSort(Class<?> uninstallAppSortClass) {
        IUninstallAppSort uninstallAppSort = null;
        try {
            Constructor con = uninstallAppSortClass.getConstructor();
            uninstallAppSort = (IUninstallAppSort) con.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "createUninstallAppSort exception:" + e.toString());
        }
        return uninstallAppSort;
    }
}