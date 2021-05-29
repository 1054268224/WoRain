/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.oneclean.whitelist;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.IDeviceIdleController;
import android.os.ServiceManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WhiteListUtils {
    private static final String TAG = "WhiteListUtils";

    public static boolean loadSystemWhiteAppsFromDB(Context context, final List<String> systemWhiteApps) {
        boolean ret = false;
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI, new String[]{"packagename"},
                    "usertype='oneclean' and status='1' ", null, null);
            ret = (c.getCount() != 0);
            if (c.moveToFirst()) {
                do {
                    Log.d(TAG, "loadSystemWhiteAppsFromDB packageName:" + c.getString(0));
                    systemWhiteApps.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            ret = false;
            Log.e(TAG, "loadSystemWhiteAppsFromDB e:", e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return ret;
    }

    public static void loadSystemWhiteAppsByDefault(Context context, final List<String> systemWhiteApps) {
        String[] whiteListDefault = context.getResources().getStringArray(R.array.oneclean_whitelist);
        for (String packageName : whiteListDefault) {
            ContentValues cv = new ContentValues();
            cv.put("packagename", packageName);
            cv.put("usertype", "oneclean");
            cv.put("status", "1");
            context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, cv);
        }
        systemWhiteApps.addAll(Arrays.asList(whiteListDefault));
    }

    public static void loadScreenOffWhiteApps(Context context, final List<String> screenOffWhiteApps) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI, new String[]{"packagename"},
                    "usertype='oneclean' and status='10' ", null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    Log.d(TAG, "loadScreenOffWhiteApps packageName:" + c.getString(0));
                    screenOffWhiteApps.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "loadScreenOffWhiteApps e:", e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    public static void loadUserWhiteAppsFromDB(Context context, final List<String> userWhiteApps) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI, new String[]{"packagename"},
                    "usertype='oneclean' and status='2' ", null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    // Log.d(TAG, "loadUserWhiteAppsFromDB packageName:" + c.getString(0));
                    userWhiteApps.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "loadUserWhiteAppsFromDB e:", e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    public static void addUserWhiteApp(Context context, String packageName) {
        ContentValues cv = new ContentValues();
        cv.put("packagename", packageName);
        cv.put("usertype", "oneclean");
        cv.put("status", "2");
        context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, cv);
        addUserDozeWhiteApp(packageName);
    }

    public static void addUserDozeWhiteApp(String packageName) {
        try {
            IDeviceIdleController dic = IDeviceIdleController.Stub
                    .asInterface(ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
            if (dic == null) {
                return;
            }
            dic.addPowerSaveWhitelistApp(packageName);
        } catch (Exception e) {
            Log.d(TAG, "deviceldle error! " + e);
        }
    }

    public static int removeUserWhiteApp(Context context, String packageName) {
        int count = 0;
        try {
            count = context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI,
                    "usertype='oneclean' and status='2' and packagename='" + packageName + "'", null);
        } catch (Exception e) {
            Log.e(TAG, "removeUserWhiteApp e:", e);
        }
        removeUserDozeWhiteApp(packageName);
        return count;
    }

    private static void removeUserDozeWhiteApp(String packageName) {
        try {
            IDeviceIdleController dic = IDeviceIdleController.Stub
                    .asInterface(ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
            if (dic == null) {
                return;
            }
            dic.removePowerSaveWhitelistApp(packageName);
        } catch (Exception e) {
            Log.d(TAG, "deviceldle error! " + e);
        }
    }

    public static void sendUserWhiteListChangeBroadcast(Context context) {
        Intent intent = new Intent("com.cydroid.softmanager.intent.WHITELIST_CHANGE");
        context.sendBroadcast(intent);
    }

    public static void loadRelateWhiteApps(Context context, Map<String, List<String>> relatedWhiteApps) {
        String[] relateWhiteAppPairs = context.getResources()
                .getStringArray(R.array.oneclean_whitelist_linkage);
        for (String pair : relateWhiteAppPairs) {
            String[] pairInfo = pair.split("/");
            String[] relatedApps = pairInfo[1].split(",");
            HelperUtils.dumpList(TAG, "loadRelateWhiteApps " + pairInfo[0], Arrays.asList(relatedApps));
            relatedWhiteApps.put(pairInfo[0], Arrays.asList(relatedApps));
        }
    }

    public static boolean isAppHasLauncherShowActivity(Context context, String packageName) {
        List<ResolveInfo> infos = HelperUtils.getLauncherShowActivity(context);
        for (ResolveInfo info : infos) {
            if (packageName.equals(info.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}
