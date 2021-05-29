//Gionee: mengdw <2015-11-17> add for CR01589343 begin
package com.cydroid.softmanager.trafficassistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

public class SellUtils {
    private static final String TAG = "SellUtils";

    public static final String SELL_PAYMENGINFO_OBJECT_KEY = "SellPaymengInfoKey";
    public static final String SELL_MOBILE_NUMBER = "SellMobileNumber";
    public static final String OPERA_PACKAGE_NAME = "com.oupeng.max.sdk";
    public static final String CONTANTS_PACKAGE_NAME = "com.android.contacts/.activities.PeopleActivity";

    public static final String WEIXIN_PACKNAME = "com.tencent.mm";
    public static final String KEY_START_SOURCE = "StartSource";
    public static final String KEY_SOURCE_BUY_NUBMER = "SourceBuyNumber";
    // Gionee: mengdw <2016-09-06> add for CR01758098 begin
    public static final String KEY_ACTIVITY_FORM = "ActivityFrom";
    public static final int ACTIVITY_FORM_MAIN = 0;
    public static final int ACTIVITY_FORM_NOTIFICATION = 1;
    // Gionee: mengdw <2016-09-06> add for CR01758098 end
    // Gionee: mengdw <2017-04-06> add for 103000 begin
    public static final int SUPPLIER_TEL_INDEX = 0;
    public static final int SUPPLIER_UPLOAD_URL_INDEX = 1;
    // Gionee: mengdw <2017-04-06> add for 103000 end

    public enum BUY_SOURCE {
        SOURCE_MENU,
        SOURCE_EXHAUST,
        SOURCE_ALERT,
        SOURCE_MONITOR
    }

    public static boolean isValidPhoneNumber(String phone) {
        return !TextUtils.isEmpty(phone) && phone.length() == 11;
    }

    public static boolean isInstallApp(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    // Gionee: mengdw <2016-04-07> add for CR01665427 begin
    public static boolean isAppFreezed(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        return packageManager.getApplicationEnabledSetting(packageName) ==
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }
    // Gionee: mengdw <2016-04-07> add for CR01665427 end

    public static boolean hasContants(Context context) {
        ComponentName component = ComponentName.unflattenFromString(CONTANTS_PACKAGE_NAME);
        Intent intent = new Intent();
        intent.setComponent(component);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
        return list != null && list.size() > 0;
    }

    public static void showToast(Context context, String txt) {
        Toast toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static boolean isListEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNetConnected(Context context) {
        boolean netStatus = false;
        try {
            ConnectivityManager connectManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected()) {
                    netStatus = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netStatus;
    }


}
//Gionee: mengdw <2015-11-17> add for CR01589343 end