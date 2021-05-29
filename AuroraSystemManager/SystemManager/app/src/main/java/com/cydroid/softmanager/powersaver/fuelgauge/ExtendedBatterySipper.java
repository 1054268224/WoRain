package com.cydroid.softmanager.powersaver.fuelgauge;

import java.util.HashMap;
import android.os.Handler;
import java.util.ArrayList;
import android.content.Context;
import com.cydroid.softmanager.R;
import android.os.BatteryStats.Uid;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager.NameNotFoundException;

import com.android.internal.os.BatterySipper;

public class ExtendedBatterySipper {
    final Context mContext;

    public BatterySipper batterSipper;
    private String mName = "";
    private int mIconId = 0;
    private String mDefaultPackageName = "";

    private int mPercent = -1;

    public ExtendedBatterySipper(Context context, String label, int iconId, BatterySipper sipper) {
        mContext = context;
        batterSipper = sipper;
        mName = label;
        // Gionee <yangxinruo> <2016-3-3> add for CR01645219 begin
        mIconId = iconId;
        // Gionee <yangxinruo> <2016-3-3> add for CR01645219 end
        if (mIconId == 0 && batterSipper != null) {
            getQuickNameIconForUid(batterSipper.getUid());
        }
        // Gionee <yangxinruo> <2016-4-19> modify for CR01680614 end
    }

    public double getSortValue() {
        if (batterSipper == null)
            return 0;
        return batterSipper.totalPowerMah;
    }

    // Gionee <yangxinruo> <2016-3-3> add for CR01645219 begin
    long getCpuTime() {
        if (batterSipper == null)
            return 0;
        return batterSipper.cpuTimeMs;
    }

    long getCpuFgTime() {
        if (batterSipper == null)
            return 0;
        return batterSipper.cpuFgTimeMs;
    }

    long getWakeLockTime() {
        if (batterSipper == null)
            return 0;
        return batterSipper.wakeLockTimeMs;
    }

    long getGpsTime() {
        if (batterSipper == null)
            return 0;
        return batterSipper.gpsTimeMs;
    }

    long getWifiRunningTime() {
        if (batterSipper == null)
            return 0;
        return batterSipper.wifiRunningTimeMs;
    }

    long getUsageTime() {
        if (batterSipper == null)
            return 0;
        return batterSipper.usageTimeMs;
    }

    // Gionee <yangxinruo> <2016-3-3> add for CR01645219 end

    void getQuickNameIconForUid(int uid) {
        String uidString = Integer.toString(uid);
        PackageManager pm = mContext.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            mIconId = R.drawable.ic_power_system;
            if (uid == 0) {
                mName = mContext.getResources().getString(R.string.process_kernel_label); // Android OS
            } else if ("mediaserver".equals(mName)) {
                mName = mContext.getResources().getString(R.string.process_mediaserver_label);
            }
        } else {
            mIconId = -1;
            initPerferedPkgName(pm, packages);
        }
    }

    private void initPerferedPkgName(PackageManager pm, String[] packages) {
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
                    mDefaultPackageName = packages[i];
                }
            } catch (NameNotFoundException e) {
            }
        }

        if (packageLabels.length == 1) {
            mName = packageLabels[0];
        } else {
            // Look for an official name for this UID.
            for (String pkgName : packages) {
                try {
                    final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                    if (pi.sharedUserLabel != 0) {
                        final CharSequence nm = pm.getText(pkgName, pi.sharedUserLabel, pi.applicationInfo);
                        if (nm != null) {
                            mName = nm.toString();
                            mDefaultPackageName = pkgName;
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
                            mName = pi.applicationInfo.loadLabel(pm).toString();
                            mDefaultPackageName = pkgName;
                            break;
                        }
                    }
                    // Gionee <yangxinruo> <2016-1-4> add for CR01616458 end
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
    }

    // Gionee <yangxinruo> <2016-4-19> add for CR01680614 begin
    public void setPercent(int percent) {
        mPercent = percent;
    }

    public int getPercent() {
        return mPercent;
    }
    // Gionee <yangxinruo> <2016-4-19> add for CR01680614 end

    public int getIconId() {
        return mIconId;
    }

    public String getName() {
        return mName;
    }

    public String getDefaultPackageName() {
        return mDefaultPackageName;
    }
}