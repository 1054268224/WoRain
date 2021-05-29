package com.cydroid.systemmanager.antivirus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import java.util.HashSet;

public class AntivirusUtil {
    public static final String UPDATE_PERMISSION = "com.qihoo.antivirus.update.permission.qvs_sdk.com.cydroid.systemmanager";
    
    public static final HashSet<String> sScanFilter = new HashSet<String>();
    static {
        sScanFilter.add("未知");
        sScanFilter.add("unknown");
        sScanFilter.add("com.android.cts");
        sScanFilter.add("com.tencent.mm");
    }
    
    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
}