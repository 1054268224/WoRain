/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot.v1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.interfaces.ThemeChangedCallback;
import com.cydroid.softmanager.receiver.LocalChangeReceiver;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.receiver.ThemeChangeReceiver;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppInfo;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppManager;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppManagerInterface;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AutoBootAppManagerImplV1 implements AutoBootAppManagerInterface,
        PackageChangedCallback, LocalChangedCallback, ThemeChangedCallback {
    private static final String TAG = "AutoBootAppManagerImplV1";

    private boolean mFirstTime = true;
    private Context mContext;
    private SharedPreferences mSp;
    private PackageStateChangeReceiver mPackageStateChangeReceiver;   
    private LocalChangeReceiver mLocalChangeReceiver;
    private ThemeChangeReceiver mThemeChangeReceiver;

    private final List<String> mUserEnableAutoBootApps = new ArrayList<String>();
    private final List<Map<String, AutoBootOptionItem>> mAutoBootMaps =
        new ArrayList<Map<String, AutoBootOptionItem>>();
    private final Map<String, AutoBootAppInfo> mAutoBootAppInfos = new HashMap<String, AutoBootAppInfo>();

    private final Map<String, WeakReference<StateChangeCallback>> mAutoBootAppsChangeCallBacks =
        new HashMap<String, WeakReference<StateChangeCallback>>();
    private final Map<String, WeakReference<LocalChangedCallback>> mLocalChangedCallbacks =
        new HashMap<String, WeakReference<LocalChangedCallback>>();
    private final Map<String, WeakReference<ThemeChangedCallback>> mThemeChangedCallbacks =
        new HashMap<String, WeakReference<ThemeChangedCallback>>();

    @Override
    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
        updateAutoBootAppsState();
    }

    private void initFirstTime(Context context) {
        mFirstTime = false;
        mContext = context.getApplicationContext();
        mSp = mContext.getSharedPreferences(
            AutoBootAppManager.ENABLE_AUTO_BOOT_APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        loadUserEnableAutoBootApps();
        loadAllAutoBootApps();
        checkAllAutoBootAppsState();
        setAutoBootAppsProperState();
        // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
        // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        PackageStateChangeReceiver.addCallBack(mContext, this);
        mLocalChangeReceiver = new LocalChangeReceiver(mContext, this);
        mLocalChangeReceiver.registerLocalChangeReceiver();
        mThemeChangeReceiver = new ThemeChangeReceiver(mContext, this);
        mThemeChangeReceiver.registerThemeChangeReceiver();
    }

    private void updateAutoBootAppsState() {
        loadUserEnableAutoBootApps();
        checkAllAutoBootAppsState();
        setAutoBootAppsProperState();
    }

    private void loadUserEnableAutoBootApps() {
        try {
            Set<String> userEnableApps = new HashSet<String>();
            userEnableApps = mSp.getStringSet(AutoBootAppManager.ENABLE_AUTO_BOOT_APP_NAMES_KEY, 
                userEnableApps);
            mUserEnableAutoBootApps.clear();
            mUserEnableAutoBootApps.addAll(userEnableApps);
        } catch (Exception e) {
            Log.d(TAG, "loadUserEnableAutoBootApps e:" + e.toString());
        }
    }

    private void loadAllAutoBootApps() {
        AutoBootOptionFactory factory = AutoBootOptionFactory.getInstance();
        List<AutoBootOptionStrategy> autoBootOptionStrategies =
            factory.createAutoBootOptionStrategies(mContext);
        for (AutoBootOptionStrategy strategy : autoBootOptionStrategies) {
            Map<String, AutoBootOptionItem> items = strategy.queryAutoBootOptions();
            mAutoBootMaps.add(items);
        }
    }

    private synchronized void checkAllAutoBootAppsState() {
        PackageManager pm = mContext.getPackageManager();
        for (Map<String, AutoBootOptionItem> map : mAutoBootMaps) {
            for (Map.Entry<String, AutoBootOptionItem> entry : map.entrySet()) {
                AutoBootOptionItem item = entry.getValue();
                item.checkAutoBootState(pm);
            }
        }
    }

    private synchronized void setAutoBootAppsProperState() {
        PackageManager pm = mContext.getPackageManager();
        for (Map<String, AutoBootOptionItem> map : mAutoBootMaps) {
            for (Map.Entry<String, AutoBootOptionItem> entry : map.entrySet()) {
                String packageName = entry.getKey();
                AutoBootOptionItem item = entry.getValue();
                setAutoBootOptionItemProperState(pm, packageName, item);
            }
        }
    }

    private void setAutoBootOptionItemProperState(PackageManager pm, String packageName, 
            AutoBootOptionItem item) {
        if (null == item) {
            return;
        }
        if (mUserEnableAutoBootApps.contains(packageName)) {
            item.enableAutoBoot(pm);
        } else {
            item.disableAutoBoot(pm);
        }
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
        Map<String, AutoBootAppInfo> resultMaps = new HashMap<String, AutoBootAppInfo>();
        for (Map<String, AutoBootOptionItem> map : mAutoBootMaps) {
            for (Map.Entry<String, AutoBootOptionItem> entry : map.entrySet()) {
                String packageName = entry.getKey();
                AutoBootOptionItem item = entry.getValue();
                ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
                if (item.isAutoBoot() != enableState || null == applicationInfo) {
                    continue;
                }
                AutoBootAppInfo info = resultMaps.get(packageName);
                if (null == info) {
                    info = mAutoBootAppInfos.get(packageName);
                    if (null == info) {
                        info = new AutoBootAppInfo();
                        mAutoBootAppInfos.put(packageName, info);
                    }
                    info.updateTitle(mContext, applicationInfo);
                    info.setPackageName(packageName);
                    info.setAutoBootState(enableState);
                    resultMaps.put(packageName, info);
                }                
                info.setAutoBootType(item.getAutoBootType());
            }
        }
        List<AutoBootAppInfo> result = new ArrayList<AutoBootAppInfo>();
        result.addAll(resultMaps.values());
        return result;
    }

    @Override
    public synchronized void enableAutoBootApp(String packageName) {
        enableAutoBootOptionItem(packageName);
        setAutoBootAppInfoState(packageName, true);
        addUserEnableAutoBootAppPersistence(packageName);
        youjuSetEnableAutoBootApp(packageName);
    }

    private void enableAutoBootOptionItem(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        for (Map<String, AutoBootOptionItem> map : mAutoBootMaps) {
            AutoBootOptionItem item = map.get(packageName);
            if (null == item) {
                continue;
            }
            item.enableAutoBoot(pm);
        }
    }

    private void setAutoBootAppInfoState(String packageName, boolean autoBootEnabled) {
        AutoBootAppInfo info = mAutoBootAppInfos.get(packageName);
        if (null == info) {
            return;
        }
        info.setAutoBootState(autoBootEnabled);
    }

    private void youjuSetEnableAutoBootApp(String packageName) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("pkgname", packageName);
    }

    @Override
    public synchronized void disableAutoBootApp(String packageName) {
        disableAutoBootOptionItem(packageName);
        setAutoBootAppInfoState(packageName, false);
        removeUserEnableAutoBootAppPersistence(packageName);
    }

    private void disableAutoBootOptionItem(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        for (Map<String, AutoBootOptionItem> map : mAutoBootMaps) {
            AutoBootOptionItem item = map.get(packageName);
            if (null == item) {
                continue;
            }
            item.disableAutoBoot(pm);
        }
    }

    @Override
    public synchronized void addPackage(String packageName) {
        Log.d(TAG, "addPackage packageName:" + packageName);
        loadAutoBootApp(packageName);
        checkAutoBootAppState(packageName);
        disableAutoBootOptionItem(packageName);
        notifyAutoBootAppsChange();
    }

    private void loadAutoBootApp(String packageName) {
        AutoBootOptionFactory factory = AutoBootOptionFactory.getInstance();
        List<AutoBootOptionStrategy> autoBootOptionStrategies = 
            factory.createAutoBootOptionStrategies(mContext);
        for (int i = 0; i < autoBootOptionStrategies.size(); ++i) {
            AutoBootOptionStrategy strategy = autoBootOptionStrategies.get(i);
            Map<String, AutoBootOptionItem> autoBootMap = mAutoBootMaps.get(i);
            AutoBootOptionItem item = strategy.queryAutoBootOption(packageName);
            if (null != item) {
                autoBootMap.put(packageName, item);
            }
        }
    }

    private void checkAutoBootAppState(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        for (Map<String, AutoBootOptionItem> map : mAutoBootMaps) {
            AutoBootOptionItem item = map.get(packageName);
            if (null != item) {
                item.checkAutoBootState(pm);
            }
        }
    }

    @Override
    public void removePackage(String packageName) {
        Log.d(TAG, "removePackage packageName:" + packageName);
        removeAutoBootAppInfo(packageName);
        removeAutoBootOptionItems(packageName);
        removeUserEnableAutoBootAppPersistence(packageName);
        notifyAutoBootAppsChange();
    }

    private void removeAutoBootAppInfo(String packageName) {
        mAutoBootAppInfos.remove(packageName);
    }

    private void removeAutoBootOptionItems(String packageName) {
        for (Map<String, AutoBootOptionItem> map : mAutoBootMaps) {
            AutoBootOptionItem item = map.remove(packageName);
            if (null != item) {
                item.clearAutoBootComponent();
            }
        }
    }

    private synchronized void removeUserEnableAutoBootAppPersistence(String packageName) {
        if (!mUserEnableAutoBootApps.contains(packageName)) {
            return;
        }
        mUserEnableAutoBootApps.remove(packageName);
        setUserEnableAutoBootAppPersistence();
    }

    private synchronized void addUserEnableAutoBootAppPersistence(String packageName) {
        if (mUserEnableAutoBootApps.contains(packageName)) {
            return;
        }
        mUserEnableAutoBootApps.add(packageName);
        setUserEnableAutoBootAppPersistence();
    }

    private void setUserEnableAutoBootAppPersistence() {
        try {
            Set<String> userEnableApps = new HashSet<String>(mUserEnableAutoBootApps);
            SharedPreferences.Editor editor = mSp.edit();
            editor.putStringSet(AutoBootAppManager.ENABLE_AUTO_BOOT_APP_NAMES_KEY, userEnableApps);
            editor.commit();
        } catch (Exception e) {
            Log.d(TAG, "setUserEnableAutoBootAppPersistence e:" + e.toString());
        }
    }

    @Override
    public synchronized void changePackage(String packageName) {
    }

    @Override
    public void setAutoBootAppsChangeCallBack(String key, 
            StateChangeCallback callback) {
        WeakReference<StateChangeCallback> cb = 
            new WeakReference<StateChangeCallback>(callback);
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
    public void onLocalChange() {
        notifyLocalChange();
    }

    public void setLocalChangeCallBack(String key, 
            LocalChangedCallback callback) {
        WeakReference<LocalChangedCallback> cb = 
            new WeakReference<LocalChangedCallback>(callback);
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
    public void enableAutoBootAppByInstall(String packageName) {
    }

    @Override
    public void disableAutoBootAppByInstall(String packageName) {
    }

    @Override
    public synchronized void changeTheme(String category) {
        notifyThemeChange(category);
    }

    public void setThemeChangedCallback(String key, 
            ThemeChangedCallback callback) {
        WeakReference<ThemeChangedCallback> cb = 
            new WeakReference<ThemeChangedCallback>(callback);
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
