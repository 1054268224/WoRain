/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.freeze;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.receiver.LocalChangeReceiver;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.NameSorting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreezeAppManager implements PackageChangedCallback, LocalChangedCallback {
    private static final String TAG = "FreezeAppManager";
    private static final boolean DEBUG = false;
    private static FreezeAppManager sInstance;

    private Context mContext;
    private boolean mFirstTime = true;
    private PackageStateChangeReceiver mPackageStateChangeReceiver;
    private LocalChangeReceiver mLocalChangeReceiver;

    private final List<String> mFreezeNormalPackages = new ArrayList<>();
    private final List<String> mFreezableNormalPackages = new ArrayList<>();
    private final List<String> mFreezeCautiousPackages = new ArrayList<>();
    private final List<String> mFreezedPackages = new ArrayList<>();

    private final List<String> mDefaultFreezeNormalPackages = new ArrayList<>();
    private final List<String> mDefaultFreezableNormalPackages = new ArrayList<>();
    private final List<String> mDefaultFreezeCautiousPackages = new ArrayList<>();

    private final Map<String, WeakReference<StateChangeCallback>> mFreezeAppsChangeCallbacks =
            new HashMap<>();
    private final Map<String, WeakReference<LocalChangedCallback>> mLocalChangedCallbacks =
            new HashMap<>();
    // Gionee <houjie> <2016-06-13> add for #17613 begin
    // I make a concession for BUG, so the code is ugly here.
    private final Map<String, List<String>> mRelatedLockApps = new HashMap<>();
    // Gionee <houjie> <2016-06-13> add for #17613 end

    public static synchronized FreezeAppManager getInstance() {
        if (null == sInstance) {
            sInstance = new FreezeAppManager();
        }
        return sInstance;
    }

    private FreezeAppManager() {
    }

    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
        updateFreezeAppsState();
    }

    private void initFirstTime(Context context) {
        Log.d(TAG, "initFirstTime");
        mFirstTime = false;
        mContext = context.getApplicationContext();
        loadRelateLockApps();
        loadDefaultFreezeAppsInfoPersistence();
        loadFreezeAppsInfoPersistence();
        loadOtherThirdFreezeApps();
        filterUninstallFreezeApps();
        // Gionee <houjie> <2016-04-07> add for CR01657689 begin
        // I make a concession for BUG, so the code is ugly here.
        filterSpecialRelatedApp();
        // Gionee <houjie> <2016-04-07> add for CR01657689 end
        // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
        // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        PackageStateChangeReceiver.addCallBack(mContext, this);
        mLocalChangeReceiver = new LocalChangeReceiver(mContext, this);
        mLocalChangeReceiver.registerLocalChangeReceiver();
    }

    private void loadRelateLockApps() {
        FreezeAppUtils.loadRelateLockApps(mContext, mRelatedLockApps,
                R.array.romapp_whiteboxapp_related_items);
    }

    private void loadDefaultFreezeAppsInfoPersistence() {
        clearDefaultFreezeAppsInfo();
        FreezeAppUtils.loadFreezeAppsInfoByDefault(mContext, mDefaultFreezeNormalPackages,
                mDefaultFreezeCautiousPackages, mDefaultFreezableNormalPackages,
                R.array.freeze_app_arrays, R.array.freeze_cautious_arrays);
    }

    private void clearDefaultFreezeAppsInfo() {
        mDefaultFreezeNormalPackages.clear();
        mDefaultFreezeCautiousPackages.clear();
        mDefaultFreezableNormalPackages.clear();
    }

    private void loadFreezeAppsInfoPersistence() {
        Log.d(TAG, "loadFreezeAppsInfoPersistence");
        boolean success = loadFreezeAppsInfoFromXml();
        if (!success) {
            loadFreezeAppsInfoByDefault();
        } else {
            filterNoFreezeApps();
            filterUninstallFreezeApps();
            filterUnFreezeAppsFromPersistence();
        }
    }

    private boolean loadFreezeAppsInfoFromXml() {
        clearFreezeAppsInfo();
        return FreezeAppUtils.loadFreezeAppsInfoFromXml(mFreezeNormalPackages,
                mFreezeCautiousPackages, mFreezedPackages, mFreezableNormalPackages,
                FreezeAppUtils.getsFreezeAppsInfoFile(mContext));
    }

    private void clearFreezeAppsInfo() {
        mFreezeNormalPackages.clear();
        mFreezableNormalPackages.clear();
        mFreezeCautiousPackages.clear();
        mFreezedPackages.clear();
    }

    private void loadFreezeAppsInfoByDefault() {
        clearFreezeAppsInfo();
        FreezeAppUtils.loadFreezeAppsInfoByDefault(mContext, mFreezeNormalPackages,
                mFreezeCautiousPackages, mFreezableNormalPackages,
                R.array.freeze_app_arrays, R.array.freeze_cautious_arrays);
    }

    private void filterUnFreezeAppsFromPersistence() {
        for (int i = 0; i < mFreezedPackages.size(); ++i) {
            String packageName = mFreezedPackages.get(i);
            try {
                if (mContext.getPackageManager().getApplicationEnabledSetting(packageName)
                        == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Log.d(TAG, "filterUnFreezeAppsFromPersistence freezed packageName:" + packageName);
                    continue;
                }
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "filterUnFreezeAppsFromPersistence e:" + e);
                mFreezedPackages.remove(i);
                --i;
                continue;
            }
            Log.d(TAG, "filterUnFreezeAppsFromPersistence no freezed packageName:" + packageName);
            if (mFreezableNormalPackages.contains(packageName)) {
                mFreezeNormalPackages.add(packageName);
            } else if (isThirdApp(packageName)) {
                mFreezeNormalPackages.add(packageName);
                mFreezableNormalPackages.add(packageName);
            } else {
                mFreezeCautiousPackages.add(packageName);
            }
            mFreezedPackages.remove(i);
            --i;
            // Gionee <houjie> <2016-04-07> add for CR01657689 begin
            // I make a concession for BUG, so the code is ugly here.
            unfreezeSpecialRelatedApp(packageName);
            // Gionee <houjie> <2016-04-07> add for CR01657689 end
        }
    }

    private void loadOtherThirdFreezeApps() {
        Log.d(TAG, "loadOtherThirdFreezeApps");
        List<ApplicationInfo> applications = SoftHelperUtils.getThirdApplicationInfo(mContext, false);
        FreezeAppUtils.filterNoFreezeThirdApps(mContext, applications, R.array.no_freeze_arrays);
        loadOtherThirdFreezeApps(applications);
    }

    private void loadOtherThirdFreezeApps(List<ApplicationInfo> applications) {
        for (int i = 0; i < applications.size(); i++) {
            String packageName = applications.get(i).packageName;
            loadOtherThirdFreezeApps(packageName);
        }
    }

    private void loadOtherThirdFreezeApps(String packageName) {
        if (mFreezeNormalPackages.contains(packageName)
                || mFreezedPackages.contains(packageName)
                || mFreezeCautiousPackages.contains(packageName)) {
            return;
        }
        Log.d(TAG, "loadOtherThirdFreezeApps packageName:" + packageName);
        // Gionee <houjie> <2016-03-19> modify for CR01655587 begin
        try {
            if (mContext.getPackageManager().getApplicationEnabledSetting(packageName)
                    != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                mFreezeNormalPackages.add(packageName);
            } else {
                mFreezedPackages.add(packageName);
            }
            mFreezableNormalPackages.add(packageName);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "loadOtherThirdFreezeApps e:" + e);
        }
        /*
        mFreezeNormalPackages.add(packageName);
        mFreezableNormalPackages.add(packageName);
        */
        // Gionee <houjie> <2016-03-19> modify for CR01655587 end
    }

    private void filterNoFreezeApps() {
        filterNoFreezeApps(mFreezeNormalPackages);
        filterNoFreezeApps(mFreezableNormalPackages);
        filterNoFreezeApps(mFreezeCautiousPackages);
        List<String> freezedAppsFiltered = filterNoFreezeApps(mFreezedPackages);
        unFreezeInNoFreezeApps(freezedAppsFiltered);
        FreezeAppUtils.writeFreezeAppsInfoToXml(mFreezeNormalPackages,
                mFreezeCautiousPackages, mFreezedPackages, mFreezableNormalPackages,
                FreezeAppUtils.getsFreezeAppsInfoFile(mContext));
    }

    private List<String> filterNoFreezeApps(List<String> packages) {
        return FreezeAppUtils.filterInNoFreezeApps(mContext, packages, R.array.no_freeze_arrays);
    }

    private void unFreezeInNoFreezeApps(List<String> packages) {
        for (String packageName : packages) {
            try {
                mContext.getPackageManager().setApplicationEnabledSetting(packageName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            } catch (Exception e) {
                Log.e(TAG, "unFreezeInNoFreezeApps e:" + e);
            }
        }
    }

    private void filterUninstallFreezeApps() {
        FreezeAppUtils.filterUninstallFreezeApps(mContext, mFreezeNormalPackages);
        FreezeAppUtils.filterUninstallFreezeApps(mContext, mFreezeCautiousPackages);
        FreezeAppUtils.filterUninstallFreezeApps(mContext, mFreezedPackages);
    }

    // Gionee <houjie> <2016-04-07> add for CR01657689 begin
    // I make a concession for BUG, so the code is ugly here.
    private void filterSpecialRelatedApp() {
        for (List<String> relatedApps : mRelatedLockApps.values()) {
            for (String relatedApp : relatedApps) {
                mFreezeNormalPackages.remove(relatedApp);
                mFreezeCautiousPackages.remove(relatedApp);
                mFreezedPackages.remove(relatedApp);
            }
        }
    }
    // Gionee <houjie> <2016-04-07> add for CR01657689 end

    private void updateFreezeAppsState() {
    }

    @Override
    public synchronized void addPackage(String packageName) {
        if (!isPackageValid(packageName)) {
            return;
        }

        if (mFreezedPackages.contains(packageName)) {
            unFreezeApp(packageName);
        } else {
            loadOtherThirdFreezeApps(packageName);
            FreezeAppUtils.writeFreezeAppsInfoToXml(mFreezeNormalPackages,
                    mFreezeCautiousPackages, mFreezedPackages, mFreezableNormalPackages,
                    FreezeAppUtils.getsFreezeAppsInfoFile(mContext));
        }
        notifyFreezeAppsChange();
    }

    private boolean isPackageValid(String packageName) {
        if (FreezeAppUtils.isAppInNoFreezeApps(mContext, packageName,
                R.array.no_freeze_arrays)) {
            return false;
        }

        return isThirdApp(packageName)
                || (mDefaultFreezeNormalPackages.contains(packageName)
                || mDefaultFreezeCautiousPackages.contains(packageName));
    }

    public synchronized void freezeApp(String packageName) {
        Log.d(TAG, "freezeApp packageName:" + packageName);
        mFreezeNormalPackages.remove(packageName);
        /*mFreezeCautiousPackages.remove(packageName);*/
        try {
            mContext.getPackageManager().setApplicationEnabledSetting(packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            FreezeAppUtils.addToNoContains(mFreezedPackages, packageName);
            FreezeAppUtils.writeFreezeAppsInfoToXml(mFreezeNormalPackages,
                    mFreezeCautiousPackages, mFreezedPackages, mFreezableNormalPackages,
                    FreezeAppUtils.getsFreezeAppsInfoFile(mContext));
        } catch (Exception e) {
            Log.e(TAG, "freezeApp e:" + e);
        }
        // Gionee <houjie> <2016-04-07> add for CR01657689 begin
        // I make a concession for BUG, so the code is ugly here.
        freezeSpecialRelatedApp(packageName);
        // Gionee <houjie> <2016-04-07> add for CR01657689 end
    }

    // Gionee <houjie> <2016-04-07> add for CR01657689 begin
    // I make a concession for BUG, so the code is ugly here.    
    private void freezeSpecialRelatedApp(String packageName) {
        List<String> relatedApps = mRelatedLockApps.get(packageName);
        if (null != relatedApps) {
            for (String relatedApp : relatedApps) {
                try {
                    mContext.getPackageManager().setApplicationEnabledSetting(relatedApp,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                } catch (Exception e) {
                    Log.e(TAG, "freezeSpecialRelatedApp e:" + e);
                }
            }
        }
    }
    // Gionee <houjie> <2016-04-07> add for CR01657689 end

    public synchronized void unFreezeApp(String packageName) {
        Log.d(TAG, "unFreezeApp packageName:" + packageName);
        mFreezedPackages.remove(packageName);
        try {
            mContext.getPackageManager().setApplicationEnabledSetting(packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            if (mFreezableNormalPackages.contains(packageName)) {
                FreezeAppUtils.addToNoContains(mFreezeNormalPackages, packageName);
            } else if (isThirdApp(packageName)) {
                FreezeAppUtils.addToNoContains(mFreezeNormalPackages, packageName);
                FreezeAppUtils.addToNoContains(mFreezableNormalPackages, packageName);
            } else {
                FreezeAppUtils.addToNoContains(mFreezeCautiousPackages, packageName);
            }
            FreezeAppUtils.writeFreezeAppsInfoToXml(mFreezeNormalPackages,
                    mFreezeCautiousPackages, mFreezedPackages, mFreezableNormalPackages,
                    FreezeAppUtils.getsFreezeAppsInfoFile(mContext));
        } catch (Exception e) {
            Log.e(TAG, "unFreezeApp e:" + e);
        }
        // Gionee <houjie> <2016-04-07> add for CR01657689 begin
        // I make a concession for BUG, so the code is ugly here.
        unfreezeSpecialRelatedApp(packageName);
        // Gionee <houjie> <2016-04-07> add for CR01657689 end
    }

    // Gionee <houjie> <2016-04-07> add for CR01657689 begin
    // I make a concession for BUG, so the code is ugly here.    
    private void unfreezeSpecialRelatedApp(String packageName) {
        List<String> relatedApps = mRelatedLockApps.get(packageName);
        if (null != relatedApps) {
            for (String relatedApp : relatedApps) {
                try {
                    mContext.getPackageManager().setApplicationEnabledSetting(relatedApp,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
                } catch (Exception e) {
                    Log.e(TAG, "unfreezeSpecialRelatedApp e:" + e);
                }
            }
        }
    }
    // Gionee <houjie> <2016-04-07> add for CR01657689 end

    private boolean isThirdApp(String packageName) {
        ApplicationInfo appInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == appInfo) {
            return false;
        }
        return ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) &&
                ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0);
    }

    @Override
    public synchronized void removePackage(String packageName) {
        Log.d(TAG, "removePackage packageName:" + packageName);
        removeFreezeApp(packageName);
        FreezeAppUtils.writeFreezeAppsInfoToXml(mFreezeNormalPackages,
                mFreezeCautiousPackages, mFreezedPackages, mFreezableNormalPackages,
                FreezeAppUtils.getsFreezeAppsInfoFile(mContext));
        notifyFreezeAppsChange();
    }

    private void removeFreezeApp(String packageName) {
        removeFreezeApp(mFreezeNormalPackages, packageName);
        removeFreezeApp(mFreezeCautiousPackages, packageName);
        removeFreezeApp(mFreezedPackages, packageName);
        removeFreezeApp(mFreezableNormalPackages, packageName);
    }

    private void removeFreezeApp(List<String> freezeApps, String packageName) {
        freezeApps.remove(packageName);
    }

    @Override
    public synchronized void changePackage(String packageName) {
        Log.d(DEBUG, TAG, "changePackage packageName:" + packageName);
    }

    public synchronized List<FreezeAppInfo> getFreezedApps() {
        List<FreezeAppInfo> freezedApps = new ArrayList<>();
        for (String packageName : mFreezedPackages) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext,
                    packageName);
            if (null != info) {
                FreezeAppInfo freezeAppInfo = createFreezeAppInfo(info, packageName, true);
                freezedApps.add(freezeAppInfo);
            }
        }
        NameSorting.sort(freezedApps);
        HelperUtils.dumpList(TAG, "getFreezedApps freezedApps:", freezedApps);
        return freezedApps;
    }

    private FreezeAppInfo createFreezeAppInfo(ApplicationInfo info, String packageName,
                                              boolean isFreezed) {
        String title = SoftHelperUtils.loadLabel(mContext, info);
        FreezeAppInfo freezeAppInfo = new FreezeAppInfo();
        freezeAppInfo.setTitle(title);
        freezeAppInfo.setPackageName(packageName);
        freezeAppInfo.setIsFreezed(isFreezed);
        return freezeAppInfo;
    }

    public synchronized List<FreezeAppInfo> getFreezeNormalApps() {
        List<FreezeAppInfo> freezeApps = new ArrayList<>();
        for (String packageName : mFreezeNormalPackages) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext,
                    packageName);
            if (null != info) {
                FreezeAppInfo freezeAppInfo = createFreezeAppInfo(info, packageName, false);
                freezeApps.add(freezeAppInfo);
            }
        }
        NameSorting.sort(freezeApps);
        HelperUtils.dumpList(TAG, "getFreezeNormalApps freezeApps:", freezeApps);
        return freezeApps;
    }

    public synchronized List<FreezeAppInfo> getFreezeCautiousApps() {
        List<FreezeAppInfo> freezeApps = new ArrayList<>();
        for (String packageName : mFreezeCautiousPackages) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext,
                    packageName);
            if (null != info) {
                FreezeAppInfo freezeAppInfo = createFreezeAppInfo(info, packageName, false);
                freezeApps.add(freezeAppInfo);
            }
        }
        NameSorting.sort(freezeApps);
        HelperUtils.dumpList(TAG, "getFreezeCautiousApps freezeApps:", freezeApps);
        return freezeApps;
    }

    public void setFreezeAppsChangeCallBack(String key,
                                            StateChangeCallback callback) {
        WeakReference<StateChangeCallback> cb = new WeakReference<>(callback);
        mFreezeAppsChangeCallbacks.put(key, cb);
    }

    public void unsetFreezeAppsChangeCallBack(String key) {
        mFreezeAppsChangeCallbacks.remove(key);
    }

    private void notifyFreezeAppsChange() {
        if (null == mFreezeAppsChangeCallbacks) {
            return;
        }
        for (WeakReference<StateChangeCallback> cb : mFreezeAppsChangeCallbacks.values()) {
            StateChangeCallback callback = cb.get();
            if (null != callback) {
                callback.onStateChange();
            }
        }
    }

    @Override
    public void onLocalChange() {
        notifyLocalChange();
    }

    public void setLocalChangeCallBack(String key,
                                       LocalChangedCallback callback) {
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
}
