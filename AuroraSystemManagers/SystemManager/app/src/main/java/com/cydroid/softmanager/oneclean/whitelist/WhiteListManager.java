/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.oneclean.whitelist;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.receiver.LocalChangeReceiver;
import com.cydroid.softmanager.softmanager.receiver.PackgeChangedReceiver;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.NameSorting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;

public class WhiteListManager implements PackageChangedCallback, LocalChangedCallback {
    private static final String TAG = "WhiteListManager";
    private static final boolean DEBUG = false;

    private static final String GREEN_SWITCH = "com.cydroid.softmanager_preferences";
    private static final String GREEN_BACKGROUND_PREFERENCE = "com.cydroid.softmanager_preferences";
    // xionghg 2017-08-17 modify for 183103 begin
    static final String PREF_KEY_IS_USER_WHITELIST_INITED = "is_user_whitelist_inited";
    static final String PREF_KEY_IS_USER_WHITELIST_INITING = "is_user_whitelist_initializing";
    static final String PREF_KEY_IS_SYSTEM_WHITELIST_INITING = "is_system_whitelist_initializing";
    // xionghg 2017-08-17 modify for 183103 end
    private static final String PREF_KEY_IS_DOZE_USER_WHITELIST_INITED = "is_doze_user_whitelist_inited";
    private static WhiteListManager sInstance;

    private Context mContext;
    private boolean mFirstTime = true;
    // private SharedPreferences mGreenBackgroundPreferences;
    private MainProcessSettingsProviderHelper mMainProcessSettingsProviderHelper;
    // private boolean mIsGreenBackgroundEnable = false;
    // private PackageStateChangeReceiver mPackageStateChangeReceiver;
    private LocalChangeReceiver mLocalChangeReceiver;

    private final List<String> mSystemWhiteApps = new ArrayList<String>();
    private final List<String> mScreenOffWhiteApps = new ArrayList<String>();
    private final List<String> mUserWhiteApps = new ArrayList<String>();
    private final List<String> mNotInUserWhiteApps = new ArrayList<String>();
    private final Map<String, List<String>> mRelatedWhiteApps = new HashMap<String, List<String>>();

    private final Map<String, WeakReference<StateChangeCallback>> mWhiteListChangeCallbacks = new HashMap<String, WeakReference<StateChangeCallback>>();
    private final Map<String, WeakReference<LocalChangedCallback>> mLocalChangedCallbacks = new HashMap<String, WeakReference<LocalChangedCallback>>();

    public static synchronized WhiteListManager getInstance() {
        if (null == sInstance) {
            sInstance = new WhiteListManager();
        }
        return sInstance;
    }

    private WhiteListManager() {
    }

    public synchronized void init(Context context) {
        Log.d(TAG, "init");
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
        updateStates();
    }

    private void initFirstTime(Context context) {
        Log.d(TAG, "initFirstTime");
        // Chenyee xionghg 20171213 modify for SW17W16A-2520 begin
        // mFirstTime = false;
        mContext = context.getApplicationContext();

//      mGreenBackgroundPreferences = mContext.getSharedPreferences(GREEN_BACKGROUND_PREFERENCE,
//      Context.MODE_MULTI_PROCESS);
        mMainProcessSettingsProviderHelper = new MainProcessSettingsProviderHelper(mContext);
        loadRelateWhiteApps();
//        loadIsGreenBackgroundEnable();
        loadSystemWhiteApps();
        loadScreenOffWhiteApps();
        // move to the interior of loadUserWhiteApps()
        // initDefaultUserWhiteApps();
//        initDozeUserWhiteApps();
//        loadUserWhiteApps();
        loadNotInUserWhiteApps();
        filterNotInUserWhiteApps();
        //PackgeChangedReceiver.setCallback("whitelist", this);
        //Guoxt modiyf for SWW1618OTA-725 begin
        PackageStateChangeReceiver.addCallBack(mContext, this);
        //Guoxt modiyf for SWW1618OTA-725 end
//        mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
//        mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
        mLocalChangeReceiver = new LocalChangeReceiver(mContext, this);
        mLocalChangeReceiver.registerLocalChangeReceiver();
        mFirstTime = false;
        // Chenyee xionghg 20171213 modify for SW17W16A-2520 end
    }

    // unused now
    private void initDefaultUserWhiteApps() {
        if (mMainProcessSettingsProviderHelper.getBoolean(PREF_KEY_IS_USER_WHITELIST_INITED, false)) {
            return;
        }
        // 这个判断也没有必要
        // String[] defaultUserWhitelistedApps = mContext.getResources()
        //         .getStringArray(R.array.default_user_whitelist);
        // if (defaultUserWhitelistedApps.length <= 0) {
        //     return;
        // }

        // xionghg 2017-08-17 modify for 183103 begin
        // 让主进程和remote进程都启动WhiteListIntentService，在那里进行互斥更安全
        // 开启IntentService来后台操作数据库
        WhiteListIntentService.startActionSaveDefaultUserWhiteApps(mContext);
        // for (String defaultUserWhitelistedApp : defaultUserWhitelistedApps) {
        //     Log.d(TAG, "add default user whitelisted app:" + defaultUserWhitelistedApp);
        //     WhiteListUtils.removeUserWhiteApp(mContext, defaultUserWhitelistedApp);
        //     WhiteListUtils.addUserWhiteApp(mContext, defaultUserWhitelistedApp);
        // }
        // settingsHelper.putBoolean(PREF_KEY_IS_USER_WHITELIST_INITED, true);
        // xionghg 2017-08-17 modify for 183103 end
    }

    private void loadRelateWhiteApps() {
        WhiteListUtils.loadRelateWhiteApps(mContext, mRelatedWhiteApps);
    }

//    private void loadIsGreenBackgroundEnable() {
//        // mIsGreenBackgroundEnable = mGreenBackgroundPreferences.getBoolean(GREEN_SWITCH, false);
//        mIsGreenBackgroundEnable = mMainProcessSettingsProviderHelper.getBoolean(GREEN_SWITCH, false);
//    }

    private void loadSystemWhiteApps() {
        Log.d(TAG, "loadSystemWhiteApps");
        boolean success = loadSystemWhiteAppsFromDB();
        if (!success) {
            loadSystemWhiteAppsByDefault();
        }
    }

    private boolean loadSystemWhiteAppsFromDB() {
        mSystemWhiteApps.clear();
        return WhiteListUtils.loadSystemWhiteAppsFromDB(mContext, mSystemWhiteApps);
    }

    private void loadSystemWhiteAppsByDefault() {
        Log.d(TAG, "loadSystemWhiteAppsByDefault");
        mSystemWhiteApps.clear();
        WhiteListUtils.loadSystemWhiteAppsByDefault(mContext, mSystemWhiteApps);
    }

    private void loadScreenOffWhiteApps() {
        Log.d(TAG, "loadScreenOffWhiteApps");
        mScreenOffWhiteApps.clear();
        WhiteListUtils.loadScreenOffWhiteApps(mContext, mScreenOffWhiteApps);
    }


    private int getVersion() {
        int soVersion = 0;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(
                    mContext.getPackageName(), 0);
            soVersion = packInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return soVersion;
    }


    private void loadUserWhiteApps() {
        mUserWhiteApps.clear();
        int soVersion =0;
        // Chenyee xionghg 20171213 modify for SW17W16A-2520 begin
        //guoxt modify for CSW1805A-1353 begin
        final int inited = mMainProcessSettingsProviderHelper.getInt(WhiteListManager.PREF_KEY_IS_USER_WHITELIST_INITED, 0);
        Log.d(TAG, "loadUserWhiteApps: is inited: " + inited);

        if (inited >= getVersion() ) {
            WhiteListUtils.loadUserWhiteAppsFromDB(mContext, mUserWhiteApps);
        } else {
            String[] defaultUserWhiteListApps = mContext.getResources().getStringArray(R.array.default_user_whitelist);
            mUserWhiteApps.addAll(Arrays.asList(defaultUserWhiteListApps));

            // 让主进程和remote进程都启动WhiteListIntentService，在那里进行互斥更安全
            Log.w(TAG, "loadUserWhiteApps: start IntentService to init, isFirstTime: " + mFirstTime);
            WhiteListIntentService.startActionSaveDefaultUserWhiteApps(mContext);
        }
        //guoxt modify for CSW1805A-1353 end
        // Chenyee xionghg 20171213 modify for SW17W16A-2520 end
        HelperUtils.dumpList(TAG, "loadUserWhiteApps mUserWhiteApps:", mUserWhiteApps);
    }

    private void loadNotInUserWhiteApps() {
        mNotInUserWhiteApps.clear();
        List<ApplicationInfo> applications = HelperUtils.getApplicationInfos(mContext);
        for (ApplicationInfo info : applications) {
            if (!mUserWhiteApps.contains(info.packageName)) {
                mNotInUserWhiteApps.add(info.packageName);
            }
        }
        HelperUtils.dumpList(TAG, "loadNotInUserWhiteApps mNotInUserWhiteApps:", mNotInUserWhiteApps);
    }

    private void filterNotInUserWhiteApps() {
        String[] userWhiteAppsWhiteList = mContext.getResources()
                .getStringArray(R.array.user_whiteapps_whitelist);
        for (String pack : userWhiteAppsWhiteList) {
            Log.d(TAG, "filterNotInUserWhiteApps remove pack:" + pack);
            mNotInUserWhiteApps.remove(pack);
        }
    }

    private void updateStates() {
//        loadIsGreenBackgroundEnable();
        loadUserWhiteApps();
        //guoxt modify for CSW1802A-1559 begin
        loadNotInUserWhiteApps();
        //guoxt modify for CSW1802A-1559 end
    }

    public synchronized boolean isGreenBackgroundEnable() {
        // loadIsGreenBackgroundEnable();
        if (SystemProperties.get("ro.gn.app.securepay.support", "no").equals("yes")) {
            return false;
        }
        boolean isGreenBackgroundEnable = mMainProcessSettingsProviderHelper.getBoolean(GREEN_SWITCH, false);
        Log.d(TAG, "is GreenBackground on " + isGreenBackgroundEnable);
        return isGreenBackgroundEnable;
    }

    public synchronized void setGreenBackgroundEnable(boolean enable) {
//      mGreenBackgroundPreferences.edit().putBoolean(GREEN_SWITCH, enable).commit();
        Log.d(TAG, "setGreenBackgroundEnable " + enable);
        mMainProcessSettingsProviderHelper.putBoolean(GREEN_SWITCH, enable);
//        mIsGreenBackgroundEnable = enable;
    }

    public synchronized boolean isFirstIn() {
//      return mGreenBackgroundPreferences.getBoolean("first_in", true);
        return mMainProcessSettingsProviderHelper.getBoolean("first_in", true);
    }

    public synchronized void setNoFirstIn() {
//      mGreenBackgroundPreferences.edit().putBoolean("first_in", false).commit();
        mMainProcessSettingsProviderHelper.putBoolean("first_in", false);
    }

    public synchronized void updateSystemWhiteApps() {
        loadSystemWhiteApps();
    }

    public synchronized List<String> getSystemWhiteApps() {
        List<String> systemWhiteApps = new ArrayList<String>();
        systemWhiteApps.addAll(mSystemWhiteApps);
        return systemWhiteApps;
    }

    // This is show in white list manager ui
    public synchronized List<WhiteAppInfo> getUserWhiteAppsForUI() {
        List<WhiteAppInfo> userWhiteAppInfos = new ArrayList<WhiteAppInfo>();
        for (String userWhiteApp : mUserWhiteApps) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, userWhiteApp);
            if (null != info) {
                WhiteAppInfo whiteAppInfo = createWhiteAppInfo(info, true);
                userWhiteAppInfos.add(whiteAppInfo);
                //guoxt modify for CSW1802A-1559 begin
                mNotInUserWhiteApps.remove(userWhiteApp);
                //guoxt modify for CSW1802A-1559 end
            }

        }
        NameSorting.sort(userWhiteAppInfos);
        HelperUtils.dumpList(TAG, "getUserWhiteAppsForUI userWhiteAppInfos:", userWhiteAppInfos);
        return userWhiteAppInfos;
    }

    private WhiteAppInfo createWhiteAppInfo(ApplicationInfo info, boolean inUserWhiteList) {
        WhiteAppInfo whiteAppInfo = new WhiteAppInfo();
        int flag = info.flags & ApplicationInfo.FLAG_SYSTEM;
        whiteAppInfo.setSummary((flag != 0) ? mContext.getString(R.string.system_process)
                : mContext.getString(R.string.user_process));
        String title = SoftHelperUtils.loadLabel(mContext, info);
        whiteAppInfo.setTitle(title);
        whiteAppInfo.setPackageName(info.packageName);
        whiteAppInfo.setInUserWhiteList(inUserWhiteList);
        return whiteAppInfo;
    }

    public synchronized List<WhiteAppInfo> getNotInUserWhiteApps() {
        List<WhiteAppInfo> notInUserWhiteAppInfo = new ArrayList<WhiteAppInfo>();
        for (String notInUserWhiteApp : mNotInUserWhiteApps) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, notInUserWhiteApp);
            if (null != info) {
                WhiteAppInfo whiteAppInfo = createWhiteAppInfo(info, false);
                notInUserWhiteAppInfo.add(whiteAppInfo);
            }
        }
        NameSorting.sort(notInUserWhiteAppInfo);
        HelperUtils.dumpList(TAG, "getNotInUserWhiteApps notInUserWhiteAppInfo:", notInUserWhiteAppInfo);
        return notInUserWhiteAppInfo;
    }

    public synchronized void addUserWhiteApp(String packageName) {
        Log.d(TAG, "addUserWhiteApp packageName:" + packageName);
        WhiteListUtils.addUserWhiteApp(mContext, packageName);
        if (!mUserWhiteApps.contains(packageName)) {
            mUserWhiteApps.add(packageName);
        }
        mNotInUserWhiteApps.remove(packageName);
        WhiteListUtils.sendUserWhiteListChangeBroadcast(mContext);
    }

    public synchronized void removeUserWhiteApp(String packageName) {
        Log.d(TAG, "removeUserWhiteApp packageName:" + packageName);
        WhiteListUtils.removeUserWhiteApp(mContext, packageName);
        mUserWhiteApps.remove(packageName);
        if (!mNotInUserWhiteApps.contains(packageName)) {
            mNotInUserWhiteApps.add(packageName);
        }
        WhiteListUtils.sendUserWhiteListChangeBroadcast(mContext);
    }

    @Override
    public synchronized void addPackage(String packageName) {
        Log.d(DEBUG, TAG, "addPackage packageName:" + packageName);
        if (!mUserWhiteApps.contains(packageName) && !mNotInUserWhiteApps.contains(packageName)
                && !isInNotInUserWhiteAppsFilter(packageName)
                && WhiteListUtils.isAppHasLauncherShowActivity(mContext, packageName)) {
            mNotInUserWhiteApps.add(packageName);
        } else if (mUserWhiteApps.contains(packageName)) {
            Log.d(TAG, "add " + packageName + "to doze");
            WhiteListUtils.addUserDozeWhiteApp(packageName);
        }
        notifyWhiteListChange();
    }

    private boolean isInNotInUserWhiteAppsFilter(String packageName) {
        String[] userWhiteAppsWhiteList = mContext.getResources()
                .getStringArray(R.array.user_whiteapps_whitelist);
        return Arrays.asList(userWhiteAppsWhiteList).contains(packageName);
    }

    @Override
    public synchronized void removePackage(String packageName) {
        Log.d(TAG, "removePackage packageName:" + packageName);
        mNotInUserWhiteApps.remove(packageName);
        notifyWhiteListChange();
    }

    @Override
    public synchronized void changePackage(String packageName) {
        Log.d(DEBUG, TAG, "changePackage packageName:" + packageName);
        ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == applicationInfo) {
            mNotInUserWhiteApps.remove(packageName);
            notifyWhiteListChange();
            return;
        }
        PackageManager pm = mContext.getPackageManager();
        if (pm.getApplicationEnabledSetting(packageName) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return;
        }
        addPackage(packageName);
    }

    public void setWhiteListChangeCallBack(String key, StateChangeCallback callback) {
        WeakReference<StateChangeCallback> cb = new WeakReference<StateChangeCallback>(callback);
        mWhiteListChangeCallbacks.put(key, cb);
    }

    public void unsetWhiteListChangeCallBack(String key) {
        mWhiteListChangeCallbacks.remove(key);
    }

    private void notifyWhiteListChange() {
        if (null == mWhiteListChangeCallbacks) {
            return;
        }
        for (WeakReference<StateChangeCallback> cb : mWhiteListChangeCallbacks.values()) {
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

    public void setLocalChangeCallBack(String key, LocalChangedCallback callback) {
        WeakReference<LocalChangedCallback> cb = new WeakReference<LocalChangedCallback>(callback);
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

    public synchronized List<String> getScreenOffWhiteApps() {
        List<String> screenOffWhiteApps = new ArrayList<String>();
        screenOffWhiteApps.addAll(mScreenOffWhiteApps);
        HelperUtils.dumpList(TAG, "getScreenOffWhiteApps screenOffWhiteApps:", screenOffWhiteApps);
        return screenOffWhiteApps;
    }

    public synchronized boolean isInUserWhiteApps(String packageName) {
        if (mUserWhiteApps.contains(packageName)) {
            Log.d(TAG, "isInUserWhiteApps packageName:" + packageName + " return true");
            return true;
        }

        List<String> relatedApps = mRelatedWhiteApps.get(packageName);
        if (null != relatedApps) {
            for (String relatedApp : relatedApps) {
                if (mUserWhiteApps.contains(relatedApp)) {
                    Log.d(TAG, "isInUserWhiteApps packageName:" + packageName + "relatedApp:" + relatedApp
                            + " return true");
                    return true;
                }
            }
        }
        Log.d(TAG, "isInUserWhiteApps packageName:" + packageName + " return false");
        return false;
    }

    // This is real user white packagenames.
    public synchronized List<String> getUserWhiteApps() {
        List<String> userWhiteApps = new ArrayList<String>();
        userWhiteApps.addAll(mUserWhiteApps);
        for (Entry<String, List<String>> entry : mRelatedWhiteApps.entrySet()) {
            List<String> relatedApps = entry.getValue();
            for (String relatedApp : relatedApps) {
                String key = entry.getKey();
                if (userWhiteApps.contains(relatedApp) && !userWhiteApps.contains(key)) {
                    userWhiteApps.add(key);
                }
            }
        }
        HelperUtils.dumpList(TAG, "getUserWhiteApps userWhiteApps:", userWhiteApps);
        return userWhiteApps;
    }

    public synchronized void initReset() {
        Log.d(TAG, "initReset");
        mFirstTime = true;
    }

    public synchronized void addUserWhiteApps(List<String> packageNames) {
        for (String packageName : packageNames) {
            if (mUserWhiteApps.contains(packageName)) {
                // 列表中已经有一个,继续
                if (mUserWhiteApps.indexOf(packageName) == mUserWhiteApps.lastIndexOf(packageName)) {
                    continue;
                }
                // 列表中有多个
                mUserWhiteApps.remove(packageName);
                WhiteListUtils.removeUserWhiteApp(mContext, packageName);
            }
            WhiteListUtils.addUserWhiteApp(mContext, packageName);
            mUserWhiteApps.add(packageName);
            mNotInUserWhiteApps.remove(packageName);
        }
        WhiteListUtils.sendUserWhiteListChangeBroadcast(mContext);
    }
}
