/**
 * Copyright Statement:
 * <p>
 * Company: Gionee Communication Equipment Limited
 * <p>
 * Author: Houjie
 * <p>
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.applock;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;

import com.android.internal.widget.LockPatternUtils;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AppLockUtils {
    private static final String TAG = "AppLockUtils";
    private static final String ROSTER_APP_LOCK_TYPE = "applock";

    public static List<String> getLockedApps(Context context) {
        List<String> lockedApps = new ArrayList<String>();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                    new String[]{"packagename"},
                    "usertype='" + ROSTER_APP_LOCK_TYPE + "'", null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    Log.d(TAG, "getLockedApps pkg:" + c.getString(0));
                    lockedApps.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getLockedApps e:", e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return lockedApps;
    }

    public static Map<String, Boolean> getLockedAppsAndStates(Context context) {
        Map<String, Boolean> lockedApps = new HashMap<String, Boolean>();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                    new String[]{"packagename"},
                    "usertype='" + ROSTER_APP_LOCK_TYPE + "'", null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    lockedApps.put(c.getString(0), Boolean.valueOf(true));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getLockedAppsAndStates e:", e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return lockedApps;
    }

    public static void insertLockedApps(Context context, String packageName) {
        ContentValues values = new ContentValues();
        values.put("usertype", ROSTER_APP_LOCK_TYPE);
        values.put("packagename", packageName);
        values.put("status", "1");
        context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, values);
    }

    public static int deleteLockedApps(Context context, String packageName) {
        int count = 0;
        try {
            count = context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI,
                    "usertype='" + ROSTER_APP_LOCK_TYPE + "' and packagename='" + packageName + "'",
                    null);
        } catch (Exception e) {
            Log.e(TAG, "delLockedApps e:", e);
        }
        return count;
    }

    public static String loadLabel(Context context, ApplicationInfo info) {
        CharSequence cs = info.loadLabel(context.getPackageManager());
        if (cs == null) {
            return info.packageName;
        }
        return cs.toString();
    }

    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo warning NameNotFoundException:" + e);
        }
        return result;
    }

    public static void loadRelateLockApps(Context context, Map<String,
            List<String>> relatedLockApps) {
        String[] relateLockAppPairs = context.getResources()
                .getStringArray(R.array.romapp_whiteboxapp_related_items);
        for (String pair : relateLockAppPairs) {
            String[] pairInfo = pair.split("/");
            String[] relatedApps = pairInfo[1].split(",");
            HelperUtils.dumpList(TAG, "loadRelateLockApps " + pairInfo[0], Arrays.asList(relatedApps));
            relatedLockApps.put(pairInfo[0], Arrays.asList(relatedApps));
        }
        // Gionee <houjie> <2016-12-02> add for #31863 begin
        // I make a concession for BUG, so the code is ugly here.  
        /*
        List<String> mRelateVideos = relatedLockApps.get("com.cydroid.video");
        if (null != mRelateVideos) {
            for (String video : mRelateVideos) {
                if (null != HelperUtils.getApplicationInfo(context, video)) {
                    return;
                }
            }
            relatedLockApps.remove("com.cydroid.video");
        }
        */
        // Gionee <houjie> <2016-12-02> add for #31863 end
    }

    public static boolean isLockScreenOn(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
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

    public static boolean isLockSecure(Context context) {
        LockPatternUtils mLockPatternUtils = new LockPatternUtils(context);

        final int security = mLockPatternUtils.getActivePasswordQuality(0);
        Log.d(TAG, "getSecurityMode() - security = " + security);
        switch (security) {
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                return true;
            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
                return false;

            default:
                throw new IllegalStateException("Unknown security quality:" + security);
        }
    }

    public static boolean isLockScreenDisabled(Context context) {
        LockPatternUtils mLockPatternUtils = new LockPatternUtils(context);
        return mLockPatternUtils.isLockScreenDisabled(UserHandle.myUserId());
    }

    public static String getPackageTitle(Context context, String packageName) {
        ApplicationInfo info = getApplicationInfo(context,
                packageName);
        if (null != info) {
            return AppLockUtils.loadLabel(context, info);
        }
        return "Null";
    }

    public static int getEffectiveUserId(Context context) {
        UserManager um = UserManager.get(context);
        if (um != null) {
            return um.getCredentialOwnerProfile(UserHandle.myUserId());
        } else {
            Log.e("settingcode.Utils", "Unable to acquire UserManager");
            return UserHandle.myUserId();
        }
    }

    public static boolean isInKidsHome(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "kidsHomeMode", 0) == 1;
    }
}