/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//Gionee <xuhz> <2014-03-03> add for CR01090601 begin
package com.cydroid.softmanager.monitor.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryStats;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.cydroid.softmanager.powersaver.analysis.AppBatteryInfo;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays a list of apps and subsystems that consume power, ordered by how much power was consumed since the
 * last time it was unplugged.
 */
public class PowerUsageUtil {

    private static final String TAG = "PowerUsageUtil";

    private final HashMap<Integer, Double> mHashMap = new HashMap<Integer, Double>();
    private List<Map.Entry<Integer, Double>> mHashMapEntryList;

    private final int mStatsType = BatteryStats.STATS_SINCE_CHARGED;

    private final Context mContext;
    private final BatteryStatsHelper mBatteryHelper;

    public PowerUsageUtil(Context context) {
        // loadBatteryInfo(context);
        mContext = context;
        mBatteryHelper = new BatteryStatsHelper(mContext, true);
        mBatteryHelper.create((Bundle) null);
    }

    public void updateAppUsage() {
        mBatteryHelper.clearStats();
        mBatteryHelper.refreshStats(mStatsType, -1);
        List<BatterySipper> sipperList = mBatteryHelper.getUsageList();
        if (sipperList != null) {
            for (BatterySipper sipper : sipperList) {
                /*
                if (sipper.drainType != BatterySipper.DrainType.APP || sipper.uidObj == null
                        || sipper.getUid() <= 0)
                    continue;
                */
                AppBatteryInfo appinfo = new AppBatteryInfo(sipper);
                // Gionee <yangxinruo> <2016-1-7> modify for CR01619392 begin
                if (appinfo.drainType != BatterySipper.DrainType.APP || appinfo.userId <= 10000)
                    continue;
                // Gionee <yangxinruo> <2016-1-7> modify for CR01619392 end
                int uid = appinfo.userId;
                double power = appinfo.powerValue;
                if (power != 0) {
                    Log.d(TAG, "record new data :" + uid + " " + power);
                    mHashMap.put(uid, power);
                }
            }
        }
        getAppPowerUsageList();
    }

    private void getAppPowerUsageList() {
        mHashMapEntryList = new ArrayList<Map.Entry<Integer, Double>>(mHashMap.entrySet());

        Collections.sort(mHashMapEntryList, new Comparator<Map.Entry<Integer, Double>>() {

            @Override
            public int compare(Map.Entry<Integer, Double> firstMapEntry,
                    Map.Entry<Integer, Double> secondMapEntry) {
                return secondMapEntry.getValue().compareTo(firstMapEntry.getValue());
            }
        });
    }

    public static String getPackageLabelForUid(Context context, int uid) {
        String labelName = null;
        String defaultPackageName = null;
        PackageManager pm = context.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);

        if (packages == null) {
            return labelName;
        }

        String[] packageLabels = new String[packages.length];
        System.arraycopy(packages, 0, packageLabels, 0, packages.length);

        // Convert package names to user-facing labels where possible
        for (int i = 0; i < packageLabels.length; i++) {
            // Check if package matches preferred package
            try {
                ApplicationInfo ai = pm.getApplicationInfo(packageLabels[i], 0);
                CharSequence label = ai.loadLabel(pm);
                if (label != null) {
                    packageLabels[i] = label.toString();
                }
                if (ai.icon != 0) {
                    defaultPackageName = packages[i];
                    break;
                }
            } catch (NameNotFoundException e) {
            }
        }

        if (packageLabels.length == 1) {
            labelName = packageLabels[0];
        } else {
            // Look for an official name for this UID.
            for (String pkgName : packages) {
                try {
                    final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                    if (pi.sharedUserLabel != 0) {
                        final CharSequence nm = pm.getText(pkgName, pi.sharedUserLabel, pi.applicationInfo);
                        if (nm != null) {
                            labelName = nm.toString();
                            if (pi.applicationInfo.icon != 0) {
                                defaultPackageName = pkgName;
                            }
                            break;
                        }
                    }

                    // Gionee <yangxinruo> <2016-1-4> add for CR01616458 begin
                    if (pi.sharedUserId != null) {
                        String[] tmp = pi.sharedUserId.split("\\.");
                        String shareIdSuffix = "";
                        if (tmp != null && tmp.length > 0)
                            shareIdSuffix = tmp[tmp.length - 1];
                        else
                            shareIdSuffix = pi.sharedUserId;
                        String[] tmp2 = pkgName.split("\\.");
                        String pkgSuffix = "";
                        if (tmp2 != null && tmp2.length > 0)
                            pkgSuffix = tmp2[tmp2.length - 1];
                        else
                            pkgSuffix = pkgName;
                        if (pkgSuffix.equals(shareIdSuffix)) {
                            labelName = pi.applicationInfo.loadLabel(pm).toString();
                            if (pi.applicationInfo.icon != 0) {
                                defaultPackageName = pkgName;
                            }
                            break;
                        }
                    }
                    // Gionee <yangxinruo> <2016-1-4> add for CR01616458 end
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
        Log.i(TAG, "labelName " + labelName);
        Log.i(TAG, "defaultPackageName " + defaultPackageName);

        if (defaultPackageName == null || defaultPackageName.equals("android")) {
            return null;
        } else if (TextUtils.isEmpty(labelName)) {
            // Gionee <yangxinruo> <2016-1-7> add for CR01619392 begin
            return null;
            // Gionee <yangxinruo> <2016-1-7> add for CR01619392 end
        }
        return labelName;
    }

    public String[] getPowerUsageAppName(int length) {
        String[] appNames = new String[length];

        if (mHashMapEntryList == null || mHashMapEntryList.isEmpty()) {
            return null;
        }

        int j = 0;
        for (int i = 0; i < mHashMapEntryList.size(); i++) {
            Log.i(TAG, "Uid = " + mHashMapEntryList.get(i).getKey() + "   power = "
                    + mHashMapEntryList.get(i).getValue());

            String appName = getPackageLabelForUid(mContext, mHashMapEntryList.get(i).getKey());

            if (!TextUtils.isEmpty(appName)) {
                appNames[j] = appName;
                j++;
            }

            if (j == length) {
                return appNames;
            }
        }
        return appNames;
    }
}
// Gionee <xuhz> <2014-03-03> add for CR01090601 end