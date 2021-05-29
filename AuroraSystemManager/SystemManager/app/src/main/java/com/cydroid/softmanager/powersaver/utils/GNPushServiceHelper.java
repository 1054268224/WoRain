package com.cydroid.softmanager.powersaver.utils;

import java.util.HashSet;
import java.util.List;

import com.gionee.push.sdk.SettingPush;
import com.gionee.push.sdk.SettingPushFactory;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class GNPushServiceHelper {
    private static final String TAG = "GNPushServiceHelper";

    private static GNPushServiceHelper sInstance;

    private SettingPush mSettingPush = null;
    private HashSet<String> mSettingPushApps = new HashSet<String>();
    private boolean mIsNeedRefeshSettingPushApps = true;

    private GNPushServiceHelper() {
    }

    public static synchronized GNPushServiceHelper getInstance() {
        if (null == sInstance) {
            sInstance = new GNPushServiceHelper();
        }
        return sInstance;
    }

    // must called in main thread
    public void initPushSettings(Context context) {
        Log.d(TAG, "enter initPushSettings");
        try {
            mSettingPush = SettingPushFactory.getSettingPush(context.getApplicationContext());
            if (mSettingPush != null && !mSettingPush.isSupportSetting()) {
                Log.d(TAG, "is not SupportSetting ");
                mSettingPush = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "can not init SettingPush ");
            mSettingPush = null;
        }
    }

    public synchronized void initPushAppListLocked(Context context) {
        Log.d(TAG, "enter initPushAppListLocked");
        if (mSettingPush == null) {
            Log.d(TAG, "getSettingPush error");
            return;
        }
        List<ApplicationInfo> appList = HelperUtils.getApplicationInfo2(context);
        try {
            for (ApplicationInfo appInfo : appList) {
                String pkgName = appInfo.packageName;
                if (mSettingPush.isAppRegistered(pkgName)) {
                    Log.d(TAG, "pkg:" + pkgName + " support app push.");
                    mSettingPushApps.add(pkgName);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "init PushAppList error");
            mSettingPushApps = new HashSet<String>();
        }
    }

    public SettingPush getSettingPush() {
        return mSettingPush;
    }

    public synchronized HashSet<String> getSettingPushApps(Context context) {
        if (mSettingPushApps.size() == 0 || mIsNeedRefeshSettingPushApps) {
            initPushAppListLocked(context);
            mIsNeedRefeshSettingPushApps = false;
        }
        return mSettingPushApps;
    }

    public synchronized void setNeedRefeshSettingPushApps(boolean mIsNeedRefeshSettingPushApps) {
        this.mIsNeedRefeshSettingPushApps = mIsNeedRefeshSettingPushApps;
    }

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end
}
