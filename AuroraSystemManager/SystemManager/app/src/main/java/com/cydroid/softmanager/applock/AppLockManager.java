/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.applock;


import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
//import android.preference.PreferenceManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.receiver.LocalChangeReceiver;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.NameSorting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cyee.provider.CyeeSettings;

public class AppLockManager implements PackageChangedCallback, LocalChangedCallback {
    private static final String TAG = "AppLockManager";
    private static final boolean DEBUG = false;
    private static final String ACTION_UPDATE_LOCKED_APP_LIST = 
        "com.gionee.intent.action.UPDATE_LOCKED_APP_LIST";
    //private static final String ACTION_UPDATE_LOCKED_APP_SETTING = 
    //    "com.gionee.intent.action.UPDATE_LOCKED_APP_SETTING";
    
    private static final String KEY_APP_LOCK_SETTING = 
        "app_lock_setting";
    
    private static AppLockManager sInstance;
    
    private boolean mFirstTime = true;
    private Context mContext;
    private PackageStateChangeReceiver mPackageStateChangeReceiver;
    private LocalChangeReceiver mLocalChangeReceiver;

    private final List<String> mAllAppPackageNames = new ArrayList<String>();
    private final Map<String, AppLockAppInfo> mLockedApps = new HashMap<String, AppLockAppInfo>();
    private final Map<String, AppLockAppInfo> mUnLockApps = new HashMap<String, AppLockAppInfo>();
    // Gionee <houjie> <2016-06-13> add for #17613 begin
    // I make a concession for BUG, so the code is ugly here.
    private final Map<String, List<String>> mRelatedLockApps = new HashMap<String, List<String>>();
    // Gionee <houjie> <2016-06-13> add for #17613 end
    
    private final Map<String, WeakReference<StateChangeCallback>> mAppsChangeCallbacks =
        new HashMap<String, WeakReference<StateChangeCallback>>();
    private final Map<String, WeakReference<LocalChangedCallback>> mLocalChangedCallbacks =
        new HashMap<String, WeakReference<LocalChangedCallback>>();

    public static synchronized AppLockManager getInstance() {
        if (null == sInstance) {
            sInstance = new AppLockManager();
        }
        return sInstance;
    }

    private AppLockManager() {
    }

    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
        updateAppsState();
    }

    private void initFirstTime(Context context) {
        mFirstTime = false;
        mContext = context.getApplicationContext();
        loadRelateLockApps();
        loadAllAppPackageNames();
        filterAllAppPackageNames();
        removeWhiteListAppByNewVersion();
        loadLockedAndUnLockApps();
        // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
        // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        PackageStateChangeReceiver.addCallBack(mContext, this);
        mLocalChangeReceiver = new LocalChangeReceiver(mContext, this);
        mLocalChangeReceiver.registerLocalChangeReceiver();
    }

    private void loadRelateLockApps() {
        AppLockUtils.loadRelateLockApps(mContext, mRelatedLockApps);
    }

    private void loadAllAppPackageNames() {
        mAllAppPackageNames.clear();
        List<ResolveInfo> infos = HelperUtils.getLauncherShowActivity(mContext);
        for (ResolveInfo info : infos) {
            if (!mAllAppPackageNames.contains(info.activityInfo.packageName)) {
                Log.d(TAG, "loadAllAppPackageNames packageName:" + info.activityInfo.packageName);
                mAllAppPackageNames.add(info.activityInfo.packageName);
            }
        }
    }

    private void filterAllAppPackageNames() {
        String[] applockWhiteList = mContext.getResources().
            getStringArray(R.array.app_lock_whitelist);
        for (String pack : applockWhiteList) {
            Log.d(TAG, "filterAllAppPackageNames remove pack:" + pack);
            mAllAppPackageNames.remove(pack);
        }
    }

    private void removeWhiteListAppByNewVersion() {
        String[] applockWhiteList = mContext.getResources().
            getStringArray(R.array.app_lock_whitelist);
        for (String pack : applockWhiteList) {
            AppLockUtils.deleteLockedApps(mContext, pack);
        }
    }

    private void loadLockedAndUnLockApps() {
        mLockedApps.clear();
        mUnLockApps.clear();
        List<String> lockedAppPackageNames = AppLockUtils.getLockedApps(mContext);
        for (String packageName : mAllAppPackageNames) {
            boolean locked = lockedAppPackageNames.contains(packageName);
            if (!locked) {
                locked = isRelatedAppInLockedApps(lockedAppPackageNames, packageName);
            }
            addAppLockAppInfoByLockState(packageName, locked);
            if (locked) {
                lockedAppPackageNames.remove(packageName);
            }
        }
        for (String packageName : lockedAppPackageNames) {
            AppLockAppInfo info = new AppLockAppInfo(packageName, true, false);
            mLockedApps.put(packageName, info);
        }
    }

    private boolean isRelatedAppInLockedApps(List<String> lockedAppPackageNames, String packageName) {
        List<String> relatedApps = mRelatedLockApps.get(packageName);
        if (null != relatedApps) {
            for (String relatedApp : relatedApps) {
                if (lockedAppPackageNames.contains(relatedApp)) {
                    Log.d(TAG, "isRelatedAppInLockedApps packageName:" + packageName + "relatedApp:"
                        + relatedApp + " return true");
                    return true;
                }
            }
        }
        Log.d(TAG, "isRelatedAppInLockedApps packageName:" + packageName + " return false");
        return false;
    }

    private void addAppLockAppInfoByLockState(String packageName, boolean locked) {
        AppLockAppInfo info = new AppLockAppInfo(packageName, locked, true);
        ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null != applicationInfo) {
            info.updateTitle(mContext, applicationInfo);
            if (locked) {
                mLockedApps.remove(packageName);
                mLockedApps.put(packageName, info);
            } else {
                mUnLockApps.remove(packageName);
                mUnLockApps.put(packageName, info);
            }
        }
    }

    private void updateAppsState() {
    }

    @Override
    public synchronized void addPackage(String packageName) {
        Log.d(DEBUG, TAG, "addPackage packageName:" + packageName);
        if (isAppInLockWhiteList(packageName) 
                || !AppLockUtils.isAppHasLauncherShowActivity(mContext, packageName)) {
            Log.d(DEBUG, TAG, "addPackage " + packageName + " is in WhiteList, so return");
            return;
        }
        List<String> lockedAppPackageNames = AppLockUtils.getLockedApps(mContext);
        boolean locked = lockedAppPackageNames.contains(packageName);
        addAppLockAppInfoByLockState(packageName, locked);
        if (!mAllAppPackageNames.contains(packageName)) {
            mAllAppPackageNames.add(packageName);
        }
        notifyAppsChange();
    }

    private boolean isAppInLockWhiteList(String packageName) {
        String[] appLockWhiteList = mContext.getResources().
            getStringArray(R.array.app_lock_whitelist);
        return Arrays.asList(appLockWhiteList).contains(packageName);
    }

    @Override
    public synchronized void removePackage(String packageName) {
        Log.d(TAG, "removePackage packageName:" + packageName);
        List<String> lockedAppPackageNames = AppLockUtils.getLockedApps(mContext);
        boolean locked = lockedAppPackageNames.contains(packageName);
        if (locked) {
            AppLockAppInfo info = mLockedApps.get(packageName);
            if (null != info) {
                info.setInstalled(false);
            }
        } else {
            mUnLockApps.remove(packageName);
        }
        mAllAppPackageNames.remove(packageName);
        notifyAppsChange();
    }

    @Override
    public synchronized void changePackage(String packageName) {
        Log.d(DEBUG, TAG, "changePackage packageName:" + packageName);
        PackageManager pm = mContext.getPackageManager();
        ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        /*guoxt moidify for CSW1702A-2479 begin*/
        if (null == applicationInfo || pm.getApplicationEnabledSetting(packageName)
                 == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ) {
            mLockedApps.remove(packageName);
            mUnLockApps.remove(packageName);
            notifyAppsChange();
            return;
        }
        /*guoxt moidify for CSW1702A-2479 end*/
        if (pm.getApplicationEnabledSetting(packageName) 
                != PackageManager.COMPONENT_ENABLED_STATE_ENABLED){
            return;
        }
        addPackage(packageName);
    }

    public synchronized List<AppLockAppInfo> getLockedApps() {
        List<AppLockAppInfo> result = new ArrayList<AppLockAppInfo>();
        result.addAll(mLockedApps.values());
        int count = result.size();
        for (int i = 0; i < count; ++i) {
            AppLockAppInfo info = result.get(i);
            if (!info.isInstalled()) {
                result.remove(i);
                --i;
                --count;
            }
        }
        NameSorting.sort(result);
        return result;
    }

    public synchronized List<AppLockAppInfo> getUnLockApps() {
        List<AppLockAppInfo> result = new ArrayList<AppLockAppInfo>();
        result.addAll(mUnLockApps.values());
        NameSorting.sort(result);
        return result;
    }

    public synchronized void lockApp(String packageName) {
        Log.d(TAG, "lockApp packageName:" + packageName);
        AppLockAppInfo info = mUnLockApps.remove(packageName);
        if (null != info) {
            info.setLocked(true);
            mLockedApps.put(packageName, info);
        }
        // Gionee <houjie> <2016-06-13> add for #17613 begin
        // I make a concession for BUG, so the code is ugly here.
        boolean isSpecial = lockSpecialRelatedApp(packageName);
        // Gionee <houjie> <2016-06-13> add for #17613 end
        if (!isSpecial) {
            AppLockUtils.insertLockedApps(mContext, packageName);
        }
        sendUpdateLockedAppListBroadcast(mContext);
    }

    // Gionee <houjie> <2016-06-13> add for #17613 begin
    // I make a concession for BUG, so the code is ugly here.    
    private boolean lockSpecialRelatedApp(String packageName) {
        Log.d(TAG, "lockSpecialRelatedApp packageName:" + packageName);
        List<String> relatedApps = mRelatedLockApps.get(packageName);
        if (null != relatedApps) {
            for (String relatedAppPackageName : relatedApps) {
                Log.d(TAG, "lockSpecialRelatedApp relatedAppPackageName:" + relatedAppPackageName);
                AppLockUtils.insertLockedApps(mContext, relatedAppPackageName);
            }
            return true;
        }
        return false;
    }
    // Gionee <houjie> <2016-06-13> add for #17613 end

    private void sendUpdateLockedAppListBroadcast(Context context) {
        Intent intent = new Intent(ACTION_UPDATE_LOCKED_APP_LIST);
        context.sendBroadcast(intent);
    }

    public synchronized void unLockApp(String packageName) {
        Log.d(TAG, "unLockApp packageName:" + packageName);
        AppLockAppInfo info = mLockedApps.remove(packageName);
        info.setLocked(false);
        mUnLockApps.put(packageName, info);
        // Gionee <houjie> <2016-06-13> add for #17613 begin
        // I make a concession for BUG, so the code is ugly here.
        boolean isSpecial = unlockSpecialRelatedApp(packageName);
        // Gionee <houjie> <2016-06-13> add for #17613 end
        if (!isSpecial) {
            AppLockUtils.deleteLockedApps(mContext, packageName);
        }
        sendUpdateLockedAppListBroadcast(mContext);
    }

    // Gionee <houjie> <2016-06-13> add for #17613 begin
    // I make a concession for BUG, so the code is ugly here.    
    private boolean unlockSpecialRelatedApp(String packageName) {
        List<String> relatedApps = mRelatedLockApps.get(packageName);
        if (null != relatedApps) {
            for (String relatedAppPackageName : relatedApps) {
                Log.d(TAG, "unlockSpecialRelatedApp relatedAppPackageName:" + relatedAppPackageName);
                AppLockUtils.deleteLockedApps(mContext, relatedAppPackageName);
            }
            return true;
        }
        return false;
    }
    // Gionee <houjie> <2016-06-13> add for #17613 end

    public synchronized void setAppsChangeCallBack(String key, 
            StateChangeCallback callback) {
        WeakReference<StateChangeCallback> cb = 
            new WeakReference<StateChangeCallback>(callback);
        mAppsChangeCallbacks.put(key, cb);
    }    

    public synchronized void unsetAppsChangeCallBack(String key) {
        mAppsChangeCallbacks.remove(key);
    }

    private void notifyAppsChange() {
        if (null == mAppsChangeCallbacks) {
            return;
        }
        for (WeakReference<StateChangeCallback> cb : mAppsChangeCallbacks.values()) {
            StateChangeCallback callback = cb.get();
            if (null != callback) {
                callback.onStateChange();
            }
        }
    }

    public synchronized int getAppLockSetting(Context context) {
        /*
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(KEY_APP_LOCK_SETTING, 0);
        */
        return CyeeSettings.getInt(context.getContentResolver(), KEY_APP_LOCK_SETTING, 0);
    }

    /*
    public synchronized void setAppLockSettingAndBroadcast(Context context, int type) {
        setAppLockSetting(context, type);
        sendUpdateLockedAppSettingBroadcast(context);
    }
    */

    public synchronized void setAppLockSetting(Context context, int type) {
        /*
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_APP_LOCK_SETTING, type);
        editor.commit();
        */
        CyeeSettings.putInt(context.getContentResolver(), KEY_APP_LOCK_SETTING, type);
    }

    /*
    public void sendUpdateLockedAppSettingBroadcast(Context context) {
        Intent intent = new Intent(ACTION_UPDATE_LOCKED_APP_SETTING);
        context.sendBroadcast(intent);
    }
    */

    @Override
    public synchronized void onLocalChange() {
        updateAllAppsTitle();
        notifyLocalChange();
    }

    private void updateAllAppsTitle() {
        updateAppsTitle(mLockedApps);
        updateAppsTitle(mUnLockApps);
    }

    private void updateAppsTitle(Map<String, AppLockAppInfo> apps) {
        for (AppLockAppInfo appLockAppInfo : apps.values()) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, 
                appLockAppInfo.getPackageName());
            if (null != info) {
                appLockAppInfo.updateTitle(mContext, info);
            }
        }
    }

    public synchronized void setLocalChangeCallBack(String key, 
            LocalChangedCallback callback) {
        WeakReference<LocalChangedCallback> cb = 
            new WeakReference<LocalChangedCallback>(callback);
        mLocalChangedCallbacks.put(key, cb);
    }    

    public synchronized void unsetLocalChangeCallBack(String key) {
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
}