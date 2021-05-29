/*
 *  
 */
//Gionee <jianghuan> <2013-09-29> add for CR00874734 begin
package com.cydroid.softmanager.trafficassistant.net;

import com.cydroid.softmanager.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.UserInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.SparseArray;

import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;

public class UidDetailProvider {
    private static final String TAG = "UidDetailProvider";
    private final Context mContext;
    private final SparseArray<UidDetail> mUidDetailCache;
    private String mVersionName; // myself added
    private String mPackageName; // myself added
    private int mUid;

    public static int buildKeyForUser(int userHandle) {
        return -(2000 + userHandle);
    }

    public UidDetailProvider(Context context) {
        mContext = context.getApplicationContext();
        mUidDetailCache = new SparseArray<UidDetail>();
    }

    public void clearCache() {
        synchronized (mUidDetailCache) {
            mUidDetailCache.clear();
        }
    }

    public UidDetail getUidDetail(int uid, boolean blocking) {
        UidDetail detail;

        synchronized (mUidDetailCache) {
            detail = mUidDetailCache.get(uid);
        }

        if (detail != null) {
            return detail;
        } else if (!blocking) {
            return null;
        }

        detail = buildUidDetail(uid);

        synchronized (mUidDetailCache) {
            mUidDetailCache.put(uid, detail);
        }

        return detail;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public String getPackageName() {
        if (!isExistApp(getUid())) {
            return null;
        }
        return mPackageName;
    }

    public int getUid() {
        return mUid;
    }

    private boolean isExistApp(int uid) {
        final PackageManager pm = mContext.getPackageManager();
        String label = pm.getNameForUid(uid);
        return label != null;
    }

    private UidDetail buildUidDetail(int uid) {
        final Resources res = mContext.getResources();
        final PackageManager pm = mContext.getPackageManager();

        final UidDetail detail = new UidDetail();
        detail.label = pm.getNameForUid(uid);
        detail.icon = pm.getDefaultActivityIcon();
        Log.d(TAG,"buildUidDetail uid=" + uid + " label=" + detail.label);
        // handle special case labels
        switch (uid) {
            case android.os.Process.SYSTEM_UID:
                detail.label = res.getString(R.string.process_kernel_label);
                detail.icon = pm.getDefaultActivityIcon();
                return detail;

            case TrafficStats.UID_REMOVED:
                detail.label = res
                        .getString(R.string.unknow_app);
                detail.icon = pm.getDefaultActivityIcon();
                return detail;

            case TrafficStats.UID_TETHERING:
                final ConnectivityManager cm = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                detail.label = res.getString(getTetheringLabel(cm));
                detail.icon = pm.getDefaultActivityIcon();
                return detail;
        }

        // Handle keys that are actually user handles
        if (uid <= -2000) {
            final int userHandle = (-uid) - 2000;
            final UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            final UserInfo info = um.getUserInfo(userHandle);
            if (info != null) {
                detail.label = res.getString(R.string.running_process_item_user_label, info.name);
            }
        }

        // otherwise fall back to using packagemanager labels
        final String[] packageNames = pm.getPackagesForUid(uid);
        final int length = packageNames != null ? packageNames.length : 0;
         Log.d(TAG, "buildUidDetail length=" + length);
        try {
            if (length == 1) {
                final PackageInfo packageInfo = pm.getPackageInfo(packageNames[0], 0); // myself added
                final ApplicationInfo info = pm.getApplicationInfo(packageNames[0], 0);
                detail.label = info.loadLabel(pm).toString();
                detail.icon = info.loadIcon(pm);
                mVersionName = packageInfo.versionName; // myself added
                mPackageName = packageNames[0];
                mUid = packageInfo.applicationInfo.uid;
                Log.d(TAG,"buildUidDetail length=1" + " mPackageName=" + mPackageName + " mUid=" + mUid + " label=" +
                    detail.label);
            } else if (length > 1) {
                detail.detailLabels = new CharSequence[length];
                for (int i = 0; i < length; i++) {
                    final String packageName = packageNames[i];
                    final PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                    final ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);

                    detail.detailLabels[i] = appInfo.loadLabel(pm).toString();
                    if (packageInfo.sharedUserLabel != 0) {
                        detail.label = pm.getText(packageName, packageInfo.sharedUserLabel,
                                packageInfo.applicationInfo).toString();
                        detail.icon = appInfo.loadIcon(pm);
                        mVersionName = packageInfo.versionName; // myself added
                        mPackageName = packageName;
                        mUid = packageInfo.applicationInfo.uid;
                        Log.d(TAG,"buildUidDetail length>1 index=" + i + " mPackageName=" + mPackageName 
                                + " mUid=" + mUid + " label=" +detail.label);
                    }
                }
            } else {
                // Gionee: mengdw <2017-06-10> add for 153758 begin
                detail.icon = pm.getDefaultActivityIcon();
                detail.label = res.getString(R.string.unknow_app);
                // Gionee: mengdw <2017-06-10> add for 153758 end
            }
           //Encrypt app begin
           if(TrafficassistantUtil.isEncryptionApp(mContext, mPackageName)) {
               detail.icon = pm.getDefaultActivityIcon();
               detail.label = res.getString(R.string.unknow_app);
           }
         //Encrypt app end
        } catch (NameNotFoundException e) {

        }

        if (TextUtils.isEmpty(detail.label)) {
            detail.label = res.getString(R.string.unknow_app);
        }
        Log.d(TAG,"buildUidDetail label=" + detail.label);
        return detail;

    }

    private int getTetheringLabel(ConnectivityManager cm) {
        String[] usbRegexs = cm.getTetherableUsbRegexs();
        String[] wifiRegexs = cm.getTetherableWifiRegexs();
        String[] bluetoothRegexs = cm.getTetherableBluetoothRegexs();

        boolean usbAvailable = usbRegexs.length != 0;
        boolean wifiAvailable = wifiRegexs.length != 0;
        boolean bluetoothAvailable = bluetoothRegexs.length != 0;

        if (wifiAvailable && usbAvailable && bluetoothAvailable) {
            return R.string.tether_settings_title_all;
        } else if (wifiAvailable && usbAvailable) {
            return R.string.tether_settings_title_all;
        } else if (wifiAvailable && bluetoothAvailable) {
            return R.string.tether_settings_title_all;
        } else if (wifiAvailable) {
            return R.string.tether_settings_title_wifi;
        } else if (usbAvailable && bluetoothAvailable) {
            return R.string.tether_settings_title_usb_bluetooth;
        } else if (usbAvailable) {
            return R.string.tether_settings_title_usb;
        } else {
            return R.string.tether_settings_title_bluetooth;
        }
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00874734 end