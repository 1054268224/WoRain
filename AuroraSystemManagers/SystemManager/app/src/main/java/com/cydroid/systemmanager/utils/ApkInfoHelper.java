package com.cydroid.systemmanager.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ApkInfoHelper {
	public static Drawable getUninstalledApkIncon(PackageManager pkgManager,
			String path) {
		// PackageManager pkgManager = context.getPackageManager();
		PackageInfo pkgInfo = pkgManager.getPackageArchiveInfo(path,
				PackageManager.GET_ACTIVITIES);
		if (pkgInfo == null) {
			return null;
		}
		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		appInfo.publicSourceDir = path;
		Drawable icon = null;
		icon = appInfo.loadIcon(pkgManager);

		return icon;
	}

	public static String getUninstalledApkLabel(PackageManager pkgManager,
			String path) {
		// PackageManager pkgManager = context.getPackageManager();
		PackageInfo pkgInfo = pkgManager.getPackageArchiveInfo(path,
				PackageManager.GET_ACTIVITIES);
		if (pkgInfo == null) {
			return null;
		}
		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		appInfo.publicSourceDir = path;
		String label = null;
		label = appInfo.loadLabel(pkgManager).toString();

		return label;
	}

	public static String getUninstalledApkPkgName(PackageManager pkgManager,
			String path) {
		// PackageManager pkgManager = context.getPackageManager();
		PackageInfo pkgInfo = pkgManager.getPackageArchiveInfo(path,
				PackageManager.GET_ACTIVITIES);
		if (pkgInfo == null) {
			return null;
		}
		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		appInfo.publicSourceDir = path;
		return appInfo.packageName;
	}

	public static boolean getApkInstallState(PackageManager pkgManager,
			String pkgName) {
		// PackageManager pkgManager = context.getPackageManager();
		try {
			pkgManager.getPackageInfo(pkgName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	public static Drawable getInstalledApkIcon(PackageManager pkgManager,
			String pkgName) {
		Drawable drawable = null;
		// PackageManager pkgManager = context.getPackageManager();
		try {
			drawable = pkgManager.getApplicationIcon(pkgName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return drawable;
	}

	public static String getInstalledApkLabel(PackageManager pkgManager,
			String pkgName) {
		String label = "";
		// PackageManager pkgManager = context.getPackageManager();
		try {
			ApplicationInfo appInfo = pkgManager.getApplicationInfo(pkgName,
					PackageManager.GET_META_DATA);
			CharSequence apkName = pkgManager.getApplicationLabel(appInfo);
			if (apkName != null) {
				label = apkName.toString();
			}
		} catch (NameNotFoundException e) {
			return null;
		} catch(RuntimeException re) {
		    re.printStackTrace();
		}

		return label;
	}

}
