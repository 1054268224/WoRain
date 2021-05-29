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

import com.cydroid.softmanager.softmanager.autoboot.v1.AutoBootAppManagerImplV1;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class AutoBootAppManagerFactory {
    private static final String TAG = "AutoBootAppManagerFactory";

    private static final List<Class<?>> MANAGER_LIST = new ArrayList<>();

    static {
        MANAGER_LIST.add(AutoBootAppManagerImplV1.class);
        MANAGER_LIST.add(AutoBootAppManagerImplV2.class);
    }

    public static AutoBootAppManagerInterface getAutoBootAppManagerImpl(int version) {
        try {
            Class<?> managerClass = MANAGER_LIST.get(version);
            if (managerClass != null) {
                return getAutoBootAppManagerImpl(managerClass);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAutoBootAppManagerImpl exception:" + e.toString());
        }
        return null;
    }

    private static AutoBootAppManagerInterface getAutoBootAppManagerImpl(Class<?> managerClass) {
        AutoBootAppManagerInterface manager = null;
        try {
            Constructor con = managerClass.getConstructor();
            manager = (AutoBootAppManagerInterface) con.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "getAutoBootAppManagerImpl exception:" + e.toString());
        }
        return manager;
    }
}