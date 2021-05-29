/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.interfaces;

public interface PackageChangedCallback {
    void addPackage(String pkgName);

    void removePackage(String pkgName);

    void changePackage(String pkgName);
}
