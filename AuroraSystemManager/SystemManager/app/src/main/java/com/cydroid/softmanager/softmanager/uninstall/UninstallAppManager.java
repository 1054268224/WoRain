/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.uninstall;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.interfaces.TimeChangedCallback;
import com.cydroid.softmanager.receiver.LocalChangeReceiver;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.receiver.ThemeChangeReceiver;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.systemcheck.SystemCheckItem;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UninstallAppManager implements PackageChangedCallback,
        LocalChangedCallback, TimeChangedCallback, ThemeChangedCallback {
    private static final String TAG = "UninstallAppManager";
    private static final boolean DEBUG = false;
    private static UninstallAppManager sInstance;

    private Context mContext;
    private boolean mFirstTime = true;
    private PackageStateChangeReceiver mPackageStateChangeReceiver;
    private LocalChangeReceiver mLocalChangeReceiver;
    private ThemeChangeReceiver mThemeChangeReceiver;
    //private TimeChangeReceiver mTimeChangeReceiver;

    private final Map<String, UninstallAppInfo> mUninstallAppInfos =
            new HashMap<>();

    private final Map<String, WeakReference<StateChangeCallback>> mStateChangeCallbacks =
            new HashMap<>();
    private final Map<String, WeakReference<LocalChangedCallback>> mLocalChangedCallbacks =
            new HashMap<>();
    private final Map<String, WeakReference<TimeChangedCallback>> mTimeChangedCallbacks =
            new HashMap<>();
    private final Map<String, WeakReference<ThemeChangedCallback>> mThemeChangedCallbacks =
            new HashMap<>();

    public static synchronized UninstallAppManager getInstance() {
        if (null == sInstance) {
            sInstance = new UninstallAppManager();
        }
        return sInstance;
    }

    private UninstallAppManager() {
    }

    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            //guoxt modify for CSW1803A-1204 begin
            new Thread() {
                @Override
                public void run() {
                    updateLastUpdateTime();
                    updateUninstallAppsState();
                }
            }.start();
            //guoxt modify for CSW1803A-1204 end
            return;
        }
       //guoxt modify for CSW1803A-1204 begin
        new Thread() {
            @Override
            public void run() {
                updateUninstallAppsState();
            }
        }.start();
        //guoxt modify for CSW1803A-1204 end

    }

    private void initFirstTime(Context context) {
        Log.d(TAG, "initFirstTime");
        mFirstTime = false;
        mContext = context.getApplicationContext();
        loadAllUninstallApps();
        // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
        // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        PackageStateChangeReceiver.addCallBack(mContext, this);
        mLocalChangeReceiver = new LocalChangeReceiver(mContext, this);
        mLocalChangeReceiver.registerLocalChangeReceiver();
        mThemeChangeReceiver = new ThemeChangeReceiver(mContext, this);
        mThemeChangeReceiver.registerThemeChangeReceiver();
        //mTimeChangeReceiver = new TimeChangeReceiver(mContext, this);
        //mTimeChangeReceiver.registerTimeChangeReceiver();
    }

    private void loadAllUninstallApps() {
        mUninstallAppInfos.clear();
        List<ApplicationInfo> applications = new ArrayList<>();
        try {
            applications = SoftHelperUtils.getThirdApplicationInfo(mContext, false);
        } catch (Exception e) {
            Log.e(TAG, "loadAllUninstallApps e:" + e);
        }
        for (ApplicationInfo info : applications) {
            UninstallAppInfo uninstallAppInfo = new UninstallAppInfo();
            uninstallAppInfo.init(mContext, info);
            mUninstallAppInfos.put(uninstallAppInfo.getPackageName(), uninstallAppInfo);
        }
    }

    private void updateUninstallAppsState() {
        updateAllUninstallAppsUseFrequency();
    }

    private void updateAllUninstallAppsUseFrequency() {
        for (UninstallAppInfo uninstallAppInfo : mUninstallAppInfos.values()) {
            uninstallAppInfo.updateUseFrequency(mContext);
        }
    }

    //guoxt modify for CSW1803A-1204 begin
    private void updateLastUpdateTime() {
        for (UninstallAppInfo uninstallAppInfo : mUninstallAppInfos.values()) {
            uninstallAppInfo.setLastUpdateTime(
                    UninstallAppUtils.getLastUpdateTime(mContext, uninstallAppInfo.getPackageName()));
        }
    }
    //guoxt modify for CSW1803A-1204 end
    public List<UninstallAppInfo> getAllUninstallAppsByShowType(int showType) {
        UninstallAppSortFactory factory = UninstallAppSortFactory.getInstance();
        IUninstallAppSort uninstallAppSort = factory.createUninstallAppSort(showType);
        Log.d(TAG, "getAllUninstallAppsByShowType showType:" + showType);
        List<UninstallAppInfo> result = new ArrayList<>();
        result.addAll(mUninstallAppInfos.values());
        if (null != uninstallAppSort) {
            // Gionee xionghonggang 2017-03-01 add for 73800 begin
            if (showType == 2) {
                UninstallAppUtils.reloadUsageStats(mContext);
            }
            // Gionee xionghonggang 2017-03-01 add for 73800 end            
            uninstallAppSort.sortUninstallApps(result);
        }
        return result;
    }

    @Override
    public synchronized void addPackage(String packageName) {
        Log.d(DEBUG, TAG, "addPackage packageName:" + packageName);
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null != info && ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0)) {
            UninstallAppInfo uninstallAppInfo = new UninstallAppInfo();
            uninstallAppInfo.init(mContext, info);
            mUninstallAppInfos.put(packageName, uninstallAppInfo);
            notifyAppsChange();
        }
    }

    @Override
    public synchronized void removePackage(String packageName) {
        Log.d(TAG, "removePackage packageName:" + packageName);
        mUninstallAppInfos.remove(packageName);
        SystemCheckItem.removeRunningItemByPackageName(packageName);
        notifyAppsChange();
    }

    @Override
    public synchronized void changePackage(String packageName) {
        Log.d(DEBUG, TAG, "changePackage packageName:" + packageName);
        ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == applicationInfo) {
            mUninstallAppInfos.remove(packageName);
            notifyAppsChange();
            return;
        }
        PackageManager pm = mContext.getPackageManager();
        if (pm.getApplicationEnabledSetting(packageName)
                != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return;
        }
        addPackage(packageName);
    }

    public void setAppsChangeCallBack(String key, StateChangeCallback callback) {
        WeakReference<StateChangeCallback> cb = new WeakReference<>(callback);
        mStateChangeCallbacks.put(key, cb);
    }

    public void unsetAppsChangeCallBack(String key) {
        mStateChangeCallbacks.remove(key);
    }

    private void notifyAppsChange() {
        if (null == mStateChangeCallbacks) {
            return;
        }
        for (WeakReference<StateChangeCallback> cb : mStateChangeCallbacks.values()) {
            StateChangeCallback callback = cb.get();
            if (callback != null) {
                callback.onStateChange();
            }
        }
    }

    @Override
    public synchronized void onLocalChange() {
        updateUninstallAppsTitle();
        notifyLocalChange();
    }

    private void updateUninstallAppsTitle() {
        for (UninstallAppInfo uninstallAppInfo : mUninstallAppInfos.values()) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, uninstallAppInfo.getPackageName());
            if (null != info) {
                uninstallAppInfo.updateTitle(mContext, info);
            }
        }
    }

    public void setLocalChangeCallBack(String key, LocalChangedCallback callback) {
        WeakReference<LocalChangedCallback> cb = new WeakReference<>(callback);
        mLocalChangedCallbacks.put(key, cb);
    }

    public void unsetLocalChangeCallBack(String key) {
        mLocalChangedCallbacks.remove(key);
    }

    private void notifyLocalChange() {
        if (null == mLocalChangedCallbacks) {
            return;
        }
        for (WeakReference<LocalChangedCallback> cb : mLocalChangedCallbacks.values()) {
            LocalChangedCallback callback = cb.get();
            if (null != callback) {
                callback.onLocalChange();
            }
        }
    }

    @Override
    public synchronized void onTimeChange() {
        notifyTimeChange();
    }

    public void setTimeChangeCallBack(String key, TimeChangedCallback callback) {
        WeakReference<TimeChangedCallback> cb = new WeakReference<>(callback);
        mTimeChangedCallbacks.put(key, cb);
    }

    public void unsetTimeChangeCallBack(String key) {
        mTimeChangedCallbacks.remove(key);
    }

    private void notifyTimeChange() {
        if (null == mTimeChangedCallbacks) {
            return;
        }
        for (WeakReference<TimeChangedCallback> cb : mTimeChangedCallbacks.values()) {
            TimeChangedCallback callback = cb.get();
            if (null != callback) {
                callback.onTimeChange();
            }
        }
    }

    @Override
    public synchronized void changeTheme(String category) {
        notifyThemeChange(category);
    }

    public void setThemeChangedCallback(String key, ThemeChangedCallback callback) {
        WeakReference<ThemeChangedCallback> cb = new WeakReference<>(callback);
        mThemeChangedCallbacks.put(key, cb);
    }

    public void unsetThemeChangedCallback(String key) {
        mThemeChangedCallbacks.remove(key);
    }

    private void notifyThemeChange(String category) {
        if (null == mThemeChangedCallbacks) {
            return;
        }
        for (WeakReference<ThemeChangedCallback> cb : mThemeChangedCallbacks.values()) {
            ThemeChangedCallback callback = cb.get();
            if (null != callback) {
                callback.changeTheme(category);
            }
        }
    }
}
