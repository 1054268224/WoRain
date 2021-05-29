package com.wheatek.utils;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;

import java.util.List;

public class UninstallPackageManager {

    public static void DeletePackage(String pakName, IPackageDeleteObserver.Stub observer, Context mContext) {
        PackageManager pkgManager = mContext.getPackageManager();
        pkgManager.deletePackage(pakName, observer, 0);
    }

    public static void DeletePackage(List<String> pakNames, IPackageDeleteObserver.Stub observer, Context mContext) {
        PackageManager pkgManager = mContext.getPackageManager();
        for (String pakName : pakNames) {
            pkgManager.deletePackage(pakName, observer, 0);
        }
    }

}
