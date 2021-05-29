/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.receiver.LocalChangeReceiver;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.receiver.ThemeChangeReceiver;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.NameSorting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoBootAppManagerImplV2 implements AutoBootAppManagerInterface,
        PackageChangedCallback, LocalChangedCallback, ThemeChangedCallback {
    private static final String TAG = "AutoBootAppManagerImplV2";
    private static final boolean DEBUG = false;

    private static final String ROSTER_AUTO_BOOT_ALLOW_TYPE = "autobootallow";

    private static final String ACTION_UPDATE_ALLOW_BOOT_APP_LIST =
            "com.chenyee.intent.action.UPDATE_ALLOW_BOOT_APP_LIST";

    private boolean mFirstTime = true;
    private Context mContext;
    private PackageStateChangeReceiver mPackageStateChangeReceiver;
    private LocalChangeReceiver mLocalChangeReceiver;
    private ThemeChangeReceiver mThemeChangeReceiver;

    private final List<String> mAllAppPackageNames = new ArrayList<>();
    private final Map<String, AutoBootAppInfo> mAutoBootAppInfos = new HashMap<>();

    private final Map<String, WeakReference<StateChangeCallback>> mAutoBootAppsChangeCallBacks =
            new HashMap<>();
    private final Map<String, WeakReference<LocalChangedCallback>> mLocalChangedCallbacks =
            new HashMap<>();
    private final Map<String, WeakReference<ThemeChangedCallback>> mThemeChangedCallbacks =
            new HashMap<>();

    private final ContentObserver mUserEnableAutoBootAppsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "onChange");
            loadUserEnableAndDisableAutoBootApps();
        }
    };

    @Override
    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
        updateAutoBootAppsState();
    }

    private void initFirstTime(Context context) {
        Log.d(TAG, "initFirstTime");
        mFirstTime = false;
        mContext = context.getApplicationContext();
        AutoBootAppConditionInitV2.initAutoBootAppConditionIfNeed(mContext);
        loadAllAutoBootApps();
        filterAllAppPackageNames();
        loadUserEnableAndDisableAutoBootApps();
        // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
        // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        PackageStateChangeReceiver.addCallBack(mContext, this);
        mLocalChangeReceiver = new LocalChangeReceiver(mContext, this);
        mLocalChangeReceiver.registerLocalChangeReceiver();
        mThemeChangeReceiver = new ThemeChangeReceiver(mContext, this);
        mThemeChangeReceiver.registerThemeChangeReceiver();
        //registerUserEnableAutoBootAppsObserver();
        //register update tow level control trigger.
    }

    private void loadAllAutoBootApps() {
        mAllAppPackageNames.clear();
        List<ApplicationInfo> applications = new ArrayList<>();
        try {
            applications = mContext.getPackageManager().getInstalledApplications(0);
        } catch (Exception e) {
            Log.e(TAG, "loadAllAutoBootApps e:" + e);
        }
        for (ApplicationInfo info : applications) {
            loadAllAutoBootApps(info);
        }
    }

    private void loadAllAutoBootApps(ApplicationInfo info) {
        if (((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                && ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
                && !mAllAppPackageNames.contains(info.packageName)) {
            Log.d(TAG, "loadAllAutoBootApps packageName:" + info.packageName);
            mAllAppPackageNames.add(info.packageName);
        }
    }

    private void filterAllAppPackageNames() {
        String[] autoBootAppWhiteList = mContext.getResources().
                getStringArray(R.array.autoboot_app_default_whitelist);
        for (String pack : autoBootAppWhiteList) {
            Log.d(TAG, "filterAllAppPackageNames remove pack:" + pack);
            mAllAppPackageNames.remove(pack);
        }
    }

    private void loadUserEnableAndDisableAutoBootApps() {
        mAutoBootAppInfos.clear();
        List<String> userEnableAutoBootApps = getUserEnableAutoBootApps(mContext);
        for (String packageName : mAllAppPackageNames) {
            boolean enabled = userEnableAutoBootApps.contains(packageName);
            addAutoBootAppInfo(packageName, enabled);
        }
    }

    private List<String> getUserEnableAutoBootApps(Context context) {
        List<String> userEnableAutoBootApps = new ArrayList<>();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                    new String[]{"packagename"},
                    "usertype='" + ROSTER_AUTO_BOOT_ALLOW_TYPE + "'", null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    Log.d(TAG, "getUserEnableAutoBootApps packageName:" + c.getString(0));
                    userEnableAutoBootApps.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "userEnableAutoBootApps e:", e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return userEnableAutoBootApps;
    }

    private void addAutoBootAppInfo(String packageName, boolean enabled) {
        AutoBootAppInfo info = new AutoBootAppInfo();
        ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null != applicationInfo && ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                && ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)) {
            info.updateTitle(mContext, applicationInfo);
            info.setPackageName(packageName);
            info.setAutoBootState(enabled);
            mAutoBootAppInfos.put(packageName, info);
        }
    }

    /*
    private void registerUserEnableAutoBootAppsObserver() {
        mContext.getContentResolver().registerContentObserver(Consts.ROSTER_CONTENT_URI, 
            true, mUserEnableAutoBootAppsObserver);
    }
    */

    private void updateAutoBootAppsState() {
        //loadFilterAutoBootApps();
        //loadUserEnableAndDisableAutoBootApps();
    }

    @Override
    public synchronized List<AutoBootAppInfo> getEnableAutoBootApps() {
        return getAutoBootAppsByState(true);
    }

    @Override
    public synchronized List<AutoBootAppInfo> getDisableAutoBootApps() {
        return getAutoBootAppsByState(false);
    }

    private List<AutoBootAppInfo> getAutoBootAppsByState(boolean enableState) {
        List<AutoBootAppInfo> result = new ArrayList<>();
        for (AutoBootAppInfo info : mAutoBootAppInfos.values()) {
            if (enableState == info.isAutoBoot()) {
                Log.d(TAG, "getAutoBootAppsByState packageName:" + info.getPackageName());
                result.add(info);
            }
        }
        NameSorting.sort(result);
        return result;
    }

    @Override
    public synchronized void enableAutoBootApp(String packageName) {
        Log.d(TAG, "enableAutoBootApp packageName:" + packageName);
        AutoBootAppInfo info = mAutoBootAppInfos.get(packageName);
        if (null != info) {
            info.setAutoBootState(true);
            insertEnableAutoBootAppDB(mContext, packageName);
            sendUpdateAllowAutoBootAppListBroadcast(mContext);
        }
    }

    private void insertEnableAutoBootAppDB(Context context, String packageName) {
        ContentValues values = new ContentValues();
        values.put("usertype", ROSTER_AUTO_BOOT_ALLOW_TYPE);
        values.put("packagename", packageName);
        values.put("status", "1");
        context.getContentResolver().insert(Consts.ROSTER_CONTENT_URI, values);
    }

    private void sendUpdateAllowAutoBootAppListBroadcast(Context context) {
        Intent intent = new Intent(ACTION_UPDATE_ALLOW_BOOT_APP_LIST);
        context.sendBroadcast(intent);
    }

    @Override
    public synchronized void disableAutoBootApp(String packageName) {
        Log.d(TAG, "disableAutoBootApp packageName:" + packageName);
        AutoBootAppInfo info = mAutoBootAppInfos.get(packageName);
        if (null != info) {
            info.setAutoBootState(false);
            deleteEnableAutoBootAppDB(mContext, packageName);
            sendUpdateAllowAutoBootAppListBroadcast(mContext);
        }
    }

    private int deleteEnableAutoBootAppDB(Context context, String packageName) {
        int count = 0;
        try {
            count = context.getContentResolver().delete(Consts.ROSTER_CONTENT_URI,
                    "usertype='" + ROSTER_AUTO_BOOT_ALLOW_TYPE + "' and packagename='" + packageName + "'",
                    null);
        } catch (Exception e) {
            Log.e(TAG, "deleteEnableAutoBootAppDB e:", e);
        }
        return count;
    }

    @Override
    public synchronized void addPackage(String packageName) {
        Log.d(DEBUG, TAG, "addPackage packageName:" + packageName);
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null != info && ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                && ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
                && !isInWhiteList(packageName)) {
            Log.d(DEBUG, TAG, "addPackage add");
            loadAllAutoBootApps(info);
            loadAutoBootApp(packageName);
            notifyAutoBootAppsChange();
        }
    }

    private boolean isInWhiteList(String packageName) {
        String[] uninstallAppWhiteList = mContext.getResources().
                getStringArray(R.array.autoboot_app_default_whitelist);
        for (String pack : uninstallAppWhiteList) {
            if (packageName.equals(pack)) {
                return true;
            }
        }
        return false;
    }

    private void loadAutoBootApp(String packageName) {
        boolean enable = isUserEnableAutoBootApp(mContext, packageName);
        addAutoBootAppInfo(packageName, enable);
    }

    private boolean isUserEnableAutoBootApp(Context context, String packageName) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(Consts.ROSTER_CONTENT_URI,
                    new String[]{"packagename"},
                    "usertype='" + ROSTER_AUTO_BOOT_ALLOW_TYPE
                            + "' and packagename='" + packageName + "'", null, null);
            if (c != null && c.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "isUserEnableAutoBootApp e:", e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return false;
    }


    @Override
    public synchronized void removePackage(String packageName) {
        Log.d(TAG, "removePackage packageName:" + packageName);
        if (isInWhiteList(packageName)) {
            return;
        }
        removeAllAutoBootApps(packageName);
        removeAutoBootAppInfo(packageName);
        //deleteEnableAutoBootAppDB(mContext, packageName);
        //sendUpdateAllowAutoBootAppListBroadcast(mContext);
        notifyAutoBootAppsChange();
    }

    private void removeAllAutoBootApps(String packageName) {
        mAllAppPackageNames.remove(packageName);
    }

    private void removeAutoBootAppInfo(String packageName) {
        mAutoBootAppInfos.remove(packageName);
    }

    @Override
    public synchronized void changePackage(String packageName) {
        Log.d(DEBUG, TAG, "changePackage packageName:" + packageName);
        ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == applicationInfo) {
            removeAllAutoBootApps(packageName);
            removeAutoBootAppInfo(packageName);
            notifyAutoBootAppsChange();
            return;
        }
        PackageManager pm = mContext.getPackageManager();
        if (pm.getApplicationEnabledSetting(packageName)
                != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return;
        }
        addPackage(packageName);
    }

    @Override
    public void setAutoBootAppsChangeCallBack(String key,
                                              StateChangeCallback callback) {
        WeakReference<StateChangeCallback> cb = new WeakReference<>(callback);
        mAutoBootAppsChangeCallBacks.put(key, cb);
    }

    @Override
    public void unsetAutoBootAppsChangeCallBack(String key) {
        mAutoBootAppsChangeCallBacks.remove(key);
    }

    private void notifyAutoBootAppsChange() {
        if (null == mAutoBootAppsChangeCallBacks) {
            return;
        }
        for (WeakReference<StateChangeCallback> cb : mAutoBootAppsChangeCallBacks.values()) {
            StateChangeCallback callback = cb.get();
            if (null != callback) {
                callback.onStateChange();
            }
        }
    }

    @Override
    public synchronized void onLocalChange() {
        updateAutoBootAppsTitle();
        notifyLocalChange();
    }

    private void updateAutoBootAppsTitle() {
        for (AutoBootAppInfo autoBootAppInfo : mAutoBootAppInfos.values()) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, autoBootAppInfo.getPackageName());
            if (null != info) {
                autoBootAppInfo.updateTitle(mContext, info);
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
    public synchronized void enableAutoBootAppByInstall(String packageName) {
        Log.d(TAG, "enableAutoBootAppByInstall packageName:" + packageName);
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == info || ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                || ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {
            return;
        }

        AutoBootAppInfo autoBootAppInfo = mAutoBootAppInfos.get(packageName);
        if (null != autoBootAppInfo) {
            autoBootAppInfo.setAutoBootState(true);
        }
        insertEnableAutoBootAppDB(mContext, packageName);
        sendUpdateAllowAutoBootAppListBroadcast(mContext);
    }

    @Override
    public synchronized void disableAutoBootAppByInstall(String packageName) {
        Log.d(TAG, "disableAutoBootAppByInstall packageName:" + packageName);
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == info || ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                || ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {
            return;
        }

        AutoBootAppInfo autoBootAppInfo = mAutoBootAppInfos.get(packageName);
        if (null != autoBootAppInfo) {
            autoBootAppInfo.setAutoBootState(false);
        }
        deleteEnableAutoBootAppDB(mContext, packageName);
        sendUpdateAllowAutoBootAppListBroadcast(mContext);
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
