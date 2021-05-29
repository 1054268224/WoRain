/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 维护安装的应用信息
 *
 * Date: 2017-1-4
 */
package com.cydroid.softmanager.memoryclean.filterrules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class ApplicationInfoManager implements PackageChangedCallback {

    private static final String TAG = "ApplicationInfoManager";
    private static ApplicationInfoManager sInstance;
    private boolean mFirstTime = true;
    private Context mContext;
    private PackageStateChangeReceiver mPackageStateChangeReceiver;
    private Map<String, ApplicationInfoEntity> mApplicationInfoEntitiesMap = new HashMap<String, ApplicationInfoEntity>();
//    private Set<String> mEncryptionsSpaceAppsSet = new HashSet<String>();
//    private Set<String> mScreenOffCleanBlackListSet = new HashSet<String>();

    private ApplicationInfoManager() {
    }

    private ApplicationInfoManager(Context context) {
        init(context);
    }

    public static synchronized ApplicationInfoManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new ApplicationInfoManager(context);
        }
        return sInstance;
    }

    public synchronized void init(Context context) {
        Log.d(TAG, "init");
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
    }

    private void initFirstTime(Context context) {
        mFirstTime = false;
        mContext = context.getApplicationContext();

        mApplicationInfoEntitiesMap = getApplicationInfo();
//        mEncryptionsSpaceAppsSet = getEncryptionsSpaceAppsInfo();
//        mScreenOffCleanBlackListSet = getScreenOffCleanBlackListAppsInfo();

        updateAllApplicationInfo();

        // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
        // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        PackageStateChangeReceiver.addCallBack(mContext, this);
    }

    private Map<String, ApplicationInfoEntity> getApplicationInfo() {
        HashMap<String, ApplicationInfoEntity> resultMap = new HashMap<String, ApplicationInfoEntity>();
        List<ApplicationInfo> rawApplcationInfoList = mContext.getPackageManager()
                .getInstalledApplications(HelperUtils.getEncryptAppFlag()
                        | PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
        for (ApplicationInfo appInfo : rawApplcationInfoList) {
            ApplicationInfoEntity appEntity = new ApplicationInfoEntity(appInfo);
            resultMap.put(appInfo.packageName, appEntity);
        }
        return resultMap;
    }

//    private Set<String> getEncryptionsSpaceAppsInfo() {
//        HashSet<String> resultMap = new HashSet<String>();
//        for (String pkgName : HelperUtils.getEncryptionsApps(mContext.getContentResolver())) {
//            resultMap.add(pkgName);
//        }
//        return resultMap;
//    }
//
//    private Set<String> getScreenOffCleanBlackListAppsInfo() {
//        HashSet<String> resultMap = new HashSet<String>();
//        for (String pkgName : HelperUtils.getBlackKillList(mContext)) {
//            resultMap.add(pkgName);
//        }
//        return resultMap;
//    }

    private void updateAllApplicationInfo() {
        for (Entry<String, ApplicationInfoEntity> mapEntry : mApplicationInfoEntitiesMap.entrySet()) {
            updateApplicationInfo(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    private synchronized void updateApplicationInfo(String pkgName,
            ApplicationInfoEntity applicationInfoEntity) {
//        applicationInfoEntity.isEncryptionsSpaceApp = mEncryptionsSpaceAppsSet.contains(pkgName);
//        applicationInfoEntity.isScreenOffCleanBlackListApp = mScreenOffCleanBlackListSet.contains(pkgName);
    }

    // implements PackageChangedCallback start
    @Override
    public void addPackage(String pkgName) {
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(pkgName, 0);
            if (applicationInfo == null) {
                return;
            }
            ApplicationInfoEntity appEntity = new ApplicationInfoEntity(applicationInfo);
            addApplicationInfo(pkgName, appEntity);
            updateApplicationInfo(pkgName, appEntity);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removePackage(String pkgName) {
        removeApplicationInfo(pkgName);
    }

    private synchronized void addApplicationInfo(String pkgName, ApplicationInfoEntity appEntity) {
        mApplicationInfoEntitiesMap.put(pkgName, appEntity);
    }

    private synchronized void removeApplicationInfo(String pkgName) {
        mApplicationInfoEntitiesMap.remove(pkgName);
    }

    @Override
    public void changePackage(String pkgName) {
    }
    // implements PackageChangedCallback end

    public static class ApplicationInfoEntity {

//        public boolean isScreenOffCleanBlackListApp = false;
//        public boolean isEncryptionsSpaceApp = false;
        public ApplicationInfo applicationInfo = null;

        public ApplicationInfoEntity(ApplicationInfo appInfo) {
            this.applicationInfo = appInfo;
        }

    }

    public synchronized boolean isSystemApp(String pkgName) {
        if (!mApplicationInfoEntitiesMap.containsKey(pkgName)) {
            return false;
        }
        ApplicationInfoEntity appEntity = mApplicationInfoEntitiesMap.get(pkgName);
        return (appEntity.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                || (appEntity.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
    }
}
