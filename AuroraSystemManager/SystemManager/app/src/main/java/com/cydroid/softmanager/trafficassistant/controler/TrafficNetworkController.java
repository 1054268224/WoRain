//Gionee: mengdw <2016-07-18> add for CR01639347 begin
package com.cydroid.softmanager.trafficassistant.controler;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.receiver.LocalChangeReceiver;
import com.cydroid.softmanager.receiver.PackageStateChangeReceiver;
import com.cydroid.softmanager.trafficassistant.interfaces.NetworkControlAppChangeCallBack;
import com.cydroid.softmanager.trafficassistant.model.TrafficNetworkControlAppInfo;
import com.cydroid.softmanager.trafficassistant.net.NetControlExecutor;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.NetworkControlXmlFileUtil;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficNetworkController implements PackageChangedCallback, LocalChangedCallback {
    private static final String TAG = "TrafficNetworkController";
    private static TrafficNetworkController sInstance;
    
    private final Context mContext;
    private NetControlExecutor mNetControlExecutor;
    
    private final Map<String, List<String>> mRelatedControlApps = new HashMap<String, List<String>>();
    private final List<String> mExcludeControlApps = new ArrayList<String>();
    private boolean isFirst = true;
    private final Map<String, TrafficNetworkControlAppInfo> mWifiControlApps = new HashMap<String, TrafficNetworkControlAppInfo>();
    private final Map<String, TrafficNetworkControlAppInfo> mMobileControlApps = new HashMap<String, TrafficNetworkControlAppInfo>();
    private PackageStateChangeReceiver mPackageStateChangeReceiver;
    // Gionee: mengdw <2017-05-19> add for 138543 begin
    private LocalChangeReceiver mLocalChangeReceiver;
    // Gionee: mengdw <2017-05-19> add for 138543 end
    private final Map<String, WeakReference<NetworkControlAppChangeCallBack>> mControlAppChangeCallBacks =
            new HashMap<String, WeakReference<NetworkControlAppChangeCallBack>>();
    
    public static TrafficNetworkController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TrafficNetworkController.class) {
                if (sInstance == null) {
                    sInstance = new TrafficNetworkController(context);
                }
            }
        }
        return sInstance;
    }
    
    private TrafficNetworkController(Context context) {
        mContext = context.getApplicationContext();
    }
    
    public synchronized void init() {
        Log.d(TAG, "init isFirst=" + isFirst);
        if (isFirst) {
            mNetControlExecutor = NetControlExecutor.getInstance();
            TrafficassistantUtil.loadRelateControlApps(mContext, mRelatedControlApps);
            TrafficassistantUtil.loadExcludeApps(mContext, mExcludeControlApps);
            loadNetwokControlApps();
            // mPackageStateChangeReceiver = new PackageStateChangeReceiver(mContext, this);
            // mPackageStateChangeReceiver.registerPackageStateChangeReceiver();
            PackageStateChangeReceiver.addCallBack(mContext, this);
            // Gionee: mengdw <2017-05-19> add for 138543 begin
            mLocalChangeReceiver = new LocalChangeReceiver(mContext, this);
            mLocalChangeReceiver.registerLocalChangeReceiver();
            // Gionee: mengdw <2017-05-19> add for 138543 end
            isFirst = false;
        }
    }
    
    private synchronized void loadNetwokControlApps() {
        clearNetwokControlAppList();
        List<ApplicationInfo> launcherApps = TrafficassistantUtil.getLauncherActivityApps(mContext);
        for (int i = 0; i < launcherApps.size(); i++) {
            ApplicationInfo appInfo = launcherApps.get(i);
            if (null != appInfo && isValidControlApp(appInfo.packageName)) {
                loadWifiControlApps(appInfo.packageName, appInfo.uid);
                loadMobileControlApps(appInfo.packageName, appInfo.uid);
            }
        }
    }

    private synchronized void clearNetwokControlAppList() {
        mWifiControlApps.clear();
        mMobileControlApps.clear();
    }
    
    private synchronized void loadWifiControlApps(String pkgName, int uid) {
        if (mWifiControlApps.containsKey(pkgName)) {
            return;
        }
        TrafficNetworkControlAppInfo controlAppInfo = createControlAppInfo(pkgName, uid, Constant.WIFI);
        Log.d(TAG, "loadWifiControlApps controlAppInfo=" + controlAppInfo);
        mWifiControlApps.put(pkgName, controlAppInfo);
    }
    
    private TrafficNetworkControlAppInfo createControlAppInfo(String pkgName, int uid, int netType) {
        NetworkControlXmlFileUtil xmlFileUtil = NetworkControlXmlFileUtil.getInstance();
        List<String> disableApps = xmlFileUtil.getDisabledApps(netType);
        TrafficNetworkControlAppInfo controlAppInfo = new TrafficNetworkControlAppInfo();
        String appName = TrafficassistantUtil.getAppLabel(mContext, pkgName);
        controlAppInfo.setAppPkgName(pkgName);
        controlAppInfo.setAppName(appName);
        controlAppInfo.setAppUid(uid);
        if (disableApps.contains(pkgName)) {
            controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_DISABLE_STATUS);
        } else {
            controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_ENABLE_STATUS);
        }
        return controlAppInfo;
    }
    
    private void loadMobileControlApps(String pkgName, int uid) {
        if (mMobileControlApps.containsKey(pkgName)) {
            return;
        }
        TrafficNetworkControlAppInfo controlAppInfo = createControlAppInfo(pkgName, uid, Constant.MOBILE);
        Log.d(TAG, "loadMobileControlApps controlAppInfo = " + controlAppInfo);
        mMobileControlApps.put(pkgName, controlAppInfo);
    }

    @Override
    protected void finalize() {
        Log.d(TAG, "finalize TrafficNetworkControlControler");
        // if (null != mPackageStateChangeReceiver) {
        //     mPackageStateChangeReceiver.unregisterPackageStateChangeReceiver();
        // }
        PackageStateChangeReceiver.removeCallBack(mContext, this);
        // Gionee: mengdw <2017-05-19> add for 138543 begin
        if (null != mLocalChangeReceiver) {
            mLocalChangeReceiver.unregisterLocalChangeReceiver();
        }
        // Gionee: mengdw <2017-05-19> add for 138543 end
    }
    
    public void addAppChangeCallBack(String key, NetworkControlAppChangeCallBack listener) {
        WeakReference<NetworkControlAppChangeCallBack> callbackListener = 
                new WeakReference<NetworkControlAppChangeCallBack>(listener);
        if (!mControlAppChangeCallBacks.containsKey(key)) {
            mControlAppChangeCallBacks.put(key, callbackListener);
        }
    }
    
    public void removeAppChangeCallBack(String key) {
        mControlAppChangeCallBacks.remove(key);
    }
    
    public synchronized void enableWifiNetwork(String pkgName) {
        // Gionee: mengdw <2017-04-27> add for 126192 begin
        if (null == pkgName || pkgName.isEmpty()) {
            Log.d(TAG, "enableWifiNetwork pkgName=" + pkgName);
            return;
        }
        // Gionee: mengdw <2017-04-27> add for 126192 end
        List<String> deleteFromXmlApps = new ArrayList<String>();
        TrafficNetworkControlAppInfo controlAppInfo = mWifiControlApps.get(pkgName);
        controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_ENABLE_STATUS);
        mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.WIFI, false);
        Log.d(TAG, "enableWifiNetwork controlAppInfo=" + controlAppInfo);
        deleteFromXmlApps.add(pkgName);
        // related apps
        setRelatedAppsRule(pkgName, false, Constant.WIFI);
        // xml
        removeAppsFromXml(deleteFromXmlApps, Constant.WIFI);
    }
    
    private void removeAppsFromXml(List<String> apps, int netType) {
        if (null == apps) {
            return;
        }
        if (apps.isEmpty()) {
            return;
        }
        NetworkControlXmlFileUtil xmlFileUtil = NetworkControlXmlFileUtil.getInstance();
        List<String> disableApps = xmlFileUtil.getDisabledApps(netType);
        for (int i = 0; i < apps.size(); i++) {
            String pkgName = apps.get(i);
            disableApps.remove(pkgName);
        }
        xmlFileUtil.saveDisabledApps(disableApps, netType);
    }
    
    private void setRelatedAppsRule(String pkgName, boolean isDisable, int netType) {
        List<String> relatedApps = mRelatedControlApps.get(pkgName);
        if (null == relatedApps) {
            return;
        }
        for (String relatedAppPackageName : relatedApps) {
            ApplicationInfo relatedAppInfo = HelperUtils.getApplicationInfo(mContext, relatedAppPackageName);
            if (null == relatedAppInfo) {
                continue;
            }
            Log.d(TAG, "controlRelatedApp pkgName=" + pkgName + " isDisable=" + isDisable + " netType=" + 
                    netType + " relatedAppInfo uid=" + relatedAppInfo.uid + " pkgName=" + relatedAppInfo.packageName);
            mNetControlExecutor.setNetworkControlRule(relatedAppInfo.uid, netType, isDisable);
        }
    }
    
    public synchronized  void disableWifiNetwork(String pkgName) {
        // Gionee: mengdw <2017-04-27> add for 126192 begin
        if (null == pkgName || pkgName.isEmpty()) {
            Log.d(TAG, "disableWifiNetwork pkgName=" + pkgName);
            return;
        }
        // Gionee: mengdw <2017-04-27> add for 126192 end
        List<String> addToXmlApps = new ArrayList<String>();
        TrafficNetworkControlAppInfo controlAppInfo = mWifiControlApps.get(pkgName);
        controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_DISABLE_STATUS);
        mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.WIFI, true);
        Log.d(TAG, "disableWifiNetwork controlAppInfo=" + controlAppInfo);
        addToXmlApps.add(pkgName);
        //related apps
        setRelatedAppsRule(pkgName, true, Constant.WIFI);
        //xml
        addAppsToXml(addToXmlApps, Constant.WIFI);
    }
    
    private void addAppsToXml(List<String> apps, int netType) {
        if (null == apps) {
            return;
        }
        if (apps.isEmpty()) {
            return;
        }
        NetworkControlXmlFileUtil xmlFileUtil = NetworkControlXmlFileUtil.getInstance();
        List<String> disableApps = xmlFileUtil.getDisabledApps(netType);
        for (int i = 0; i < apps.size(); i++) {
            String pkgName = apps.get(i);
            if (!disableApps.contains(pkgName)) {
                disableApps.add(pkgName);
            }
        }
        xmlFileUtil.saveDisabledApps(disableApps, netType);
    }

    public synchronized void enableMobileNetwork(String pkgName) {
        // Gionee: mengdw <2017-04-27> add for 126192 begin
        if (null == pkgName || pkgName.isEmpty()) {
            Log.d(TAG, "enableMobileNetwork pkgName=" + pkgName);
            return;
        }
        // Gionee: mengdw <2017-04-27> add for 126192 end
        List<String> deleteFromXmlApps = new ArrayList<String>();
        TrafficNetworkControlAppInfo controlAppInfo = mMobileControlApps.get(pkgName);
        // xionghg <2017-08-09> modify for 183974 begin
        if (controlAppInfo != null) {
            controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_ENABLE_STATUS);
            mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.MOBILE, false);
        }
        // xionghg <2017-08-09> modify for 183974 end
        Log.d(TAG, "enableMobileNetwork controlAppInfo=" + controlAppInfo);
        deleteFromXmlApps.add(pkgName);
        //related apps
        setRelatedAppsRule(pkgName, false, Constant.MOBILE);
        //xml
        removeAppsFromXml(deleteFromXmlApps, Constant.MOBILE);
    }

    public synchronized void disableMobileNetwork(String pkgName) {
        // Gionee: mengdw <2017-04-27> add for 126192 begin
        if (null == pkgName || pkgName.isEmpty()) {
            Log.d(TAG, "disableMobileNetwork pkgName=" + pkgName);
            return;
        }
        // Gionee: mengdw <2017-04-27> add for 126192 end
        List<String> addToXmlApps = new ArrayList<String>();
        TrafficNetworkControlAppInfo controlAppInfo = mMobileControlApps.get(pkgName);
        controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_DISABLE_STATUS);
        mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.MOBILE, true);
        Log.d(TAG, "disableMobileNetwork controlAppInfo=" + controlAppInfo);
        addToXmlApps.add(pkgName);
        //related apps
        setRelatedAppsRule(pkgName, true, Constant.MOBILE);
        //xml
        addAppsToXml(addToXmlApps, Constant.MOBILE);
    }

    public synchronized void enableAllWifiNetwork() {
        Log.d(TAG, "enableAllWifiNetwork");
        List<String> deleteFromXmlApps = new ArrayList<String>();
        for (TrafficNetworkControlAppInfo controlAppInfo : mWifiControlApps.values()) {
            Log.d(TAG, "enableAllWifiNetwork controlAppInfo=" + controlAppInfo);
            if (null == controlAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_DISABLE_STATUS == controlAppInfo.getAppStatus()) {
                mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.WIFI, false);
                deleteFromXmlApps.add(controlAppInfo.getAppPkgName());
                //related apps
                setRelatedAppsRule(controlAppInfo.getAppPkgName(), false, Constant.WIFI);
                controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_ENABLE_STATUS);
            }
        }
        removeAppsFromXml(deleteFromXmlApps, Constant.WIFI);
    }

    public synchronized void disableAllWifiNetwork() {
        Log.d(TAG, "disableAllWifiNetwork");
        List<String> addToXmlApps = new ArrayList<String>();
        for (TrafficNetworkControlAppInfo controlAppInfo : mWifiControlApps.values()) {
            Log.d(TAG, "disableAllWifiNetwork controlAppInfo=" + controlAppInfo);
            if (null == controlAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_ENABLE_STATUS == controlAppInfo.getAppStatus()) {
                mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.WIFI, true);
                addToXmlApps.add(controlAppInfo.getAppPkgName());
                //related apps
                setRelatedAppsRule(controlAppInfo.getAppPkgName(), true, Constant.WIFI);
                controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_DISABLE_STATUS);
            }
        }
        addAppsToXml(addToXmlApps, Constant.WIFI);
    }

    public synchronized void enableAllMobileNetwork() {
        Log.d(TAG, "enableAllMobileNetwork");
        List<String> deleteFromXmlApps = new ArrayList<String>();
        for (TrafficNetworkControlAppInfo controlAppInfo : mMobileControlApps.values()) {
            Log.d(TAG, "enableAllMobileNetwork controlAppInfo=" + controlAppInfo);
            if (null == controlAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_DISABLE_STATUS == controlAppInfo.getAppStatus()) {
                mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.MOBILE, false);
                deleteFromXmlApps.add(controlAppInfo.getAppPkgName());
                // related apps
                setRelatedAppsRule(controlAppInfo.getAppPkgName(), false, Constant.MOBILE);
                controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_ENABLE_STATUS);
            }
        }
        removeAppsFromXml(deleteFromXmlApps, Constant.MOBILE);
    }

    public synchronized void disableAllMobileNetwork() {
        Log.d(TAG, "disableAllMobileNetwork");
        List<String> addToXmlApps = new ArrayList<String>();
        for (TrafficNetworkControlAppInfo controlAppInfo : mMobileControlApps.values()) {
            Log.d(TAG, "disableAllMobileNetwork controlAppInfo=" + controlAppInfo);
            if (null == controlAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_ENABLE_STATUS == controlAppInfo.getAppStatus()) {
                mNetControlExecutor.setNetworkControlRule(controlAppInfo.getAppUid(), Constant.MOBILE, true);
                addToXmlApps.add(controlAppInfo.getAppPkgName());
                // related apps
                setRelatedAppsRule(controlAppInfo.getAppPkgName(), true, Constant.MOBILE);
                controlAppInfo.setAppStatus(Constant.NETWORK_CONTROL_DISABLE_STATUS);
            }
        }
        addAppsToXml(addToXmlApps, Constant.MOBILE);
    }
    
    public synchronized void rebootDisableNetwork() {
        Log.d(TAG, "rebootDisableNetwork");
        for (TrafficNetworkControlAppInfo wifiAppInfo : mWifiControlApps.values()) {
            if (null == wifiAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_DISABLE_STATUS == wifiAppInfo.getAppStatus()) {
                mNetControlExecutor.setNetworkControlRule(wifiAppInfo.getAppUid(), Constant.WIFI, true);
                setRelatedAppsRule(wifiAppInfo.getAppPkgName(), true, Constant.WIFI);
            }
        }

        for (TrafficNetworkControlAppInfo mobileAppInfo : mMobileControlApps.values()) {
            if (null == mobileAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_DISABLE_STATUS == mobileAppInfo.getAppStatus()) {
                mNetControlExecutor.setNetworkControlRule(mobileAppInfo.getAppUid(), Constant.MOBILE, true);
                setRelatedAppsRule(mobileAppInfo.getAppPkgName(), true, Constant.MOBILE);
            }
        }
    }
    
    public synchronized List<TrafficNetworkControlAppInfo> getWifiDisableAppList() {
        List<TrafficNetworkControlAppInfo> wifiDisableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
        for (TrafficNetworkControlAppInfo wifiAppInfo : mWifiControlApps.values()) {
            if (null == wifiAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_DISABLE_STATUS == wifiAppInfo.getAppStatus()) {
                TrafficNetworkControlAppInfo appInfo = new TrafficNetworkControlAppInfo();
                appInfo.copyAppInfo(wifiAppInfo);
                wifiDisableAppList.add(appInfo);
            }
        }
        return wifiDisableAppList;
    }

    public synchronized List<TrafficNetworkControlAppInfo> getWifiEnableAppList() {
        List<TrafficNetworkControlAppInfo> wifiEnableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
        for (TrafficNetworkControlAppInfo wifiAppInfo : mWifiControlApps.values()) {
            if (null == wifiAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_ENABLE_STATUS == wifiAppInfo.getAppStatus()) {
                TrafficNetworkControlAppInfo appInfo = new TrafficNetworkControlAppInfo();
                appInfo.copyAppInfo(wifiAppInfo);
                wifiEnableAppList.add(appInfo);
            }
        }
        return wifiEnableAppList;
    }

    public synchronized List<TrafficNetworkControlAppInfo> getMobileDisableAppList() {
        List<TrafficNetworkControlAppInfo> mobileDisableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
        for (TrafficNetworkControlAppInfo mobileAppInfo : mMobileControlApps.values()) {
            if (null == mobileAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_DISABLE_STATUS == mobileAppInfo.getAppStatus()) {
                TrafficNetworkControlAppInfo appInfo = new TrafficNetworkControlAppInfo();
                appInfo.copyAppInfo(mobileAppInfo);
                mobileDisableAppList.add(appInfo);
            }
        }
        return mobileDisableAppList;
    }

    public synchronized List<TrafficNetworkControlAppInfo> getMobileEnableAppList() {
        List<TrafficNetworkControlAppInfo> mobileEnableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
        for (TrafficNetworkControlAppInfo mobileAppInfo : mMobileControlApps.values()) {
            if (null == mobileAppInfo) {
                continue;
            }
            if (Constant.NETWORK_CONTROL_ENABLE_STATUS == mobileAppInfo.getAppStatus()) {
                TrafficNetworkControlAppInfo appInfo = new TrafficNetworkControlAppInfo();
                appInfo.copyAppInfo(mobileAppInfo);
                mobileEnableAppList.add(appInfo);
            }
        }
        return mobileEnableAppList;
    }
    
    // Gionee: mengdw <2017-05-19> add for 138543 begin
    @Override
    public synchronized void onLocalChange() {
        Log.d(TAG, "onLocalChange");
        changeAppsLocal(mWifiControlApps);
        changeAppsLocal(mMobileControlApps);
        notifyAppChange(Constant.WIFI);
        notifyAppChange(Constant.MOBILE); 
    }
    
    private void changeAppsLocal(Map<String, TrafficNetworkControlAppInfo> apps) {
        for (TrafficNetworkControlAppInfo app : apps.values()) {
            changeAppName(app);
        }
    }
    
    private  void changeAppName(TrafficNetworkControlAppInfo appInfo) {
        if (null == appInfo) {
            Log.d(TAG, "changeAppLocal appInfo is null");
            return;
        }
        String pkgName = appInfo.getAppPkgName();
        String appName = TrafficassistantUtil.getAppLabel(mContext, pkgName);
        appInfo.setAppName(appName);
    }
    // Gionee: mengdw <2017-05-19> add for 138543 end

    @Override
    public synchronized void addPackage(String pkgName) {
        Log.d(TAG, "addPackage pkgName=" + pkgName);
        if (!isValidControlApp(pkgName)) {
            return;
        }
        int uid = TrafficassistantUtil.getUidByPackageName(mContext, pkgName);
        if (Constant.INVALID_UID == uid) {
            return;
        }
        addAppToMobileControlList(pkgName, uid);
        addAppToWifiControlList(pkgName, uid);
        notifyAppChange(Constant.MOBILE);
        notifyAppChange(Constant.WIFI);
    }
    
    private void addAppToMobileControlList(String pkgName, int uid) {
        if (mMobileControlApps.containsKey(pkgName)) {
            return;
        }
        TrafficNetworkControlAppInfo controlAppInfo  = createControlAppInfo(pkgName, uid, Constant.MOBILE);
        mMobileControlApps.put(pkgName, controlAppInfo);
    }
    
    private void addAppToWifiControlList(String pkgName, int uid) {
        if (mWifiControlApps.containsKey(pkgName)) {
            return;
        }
        TrafficNetworkControlAppInfo controlAppInfo  = createControlAppInfo(pkgName, uid, Constant.WIFI);
        mWifiControlApps.put(pkgName, controlAppInfo);
    }

    @Override
    public synchronized void removePackage(String pkgName) {
        Log.d(TAG, "removePackage pkgName=" + pkgName);
        mobileControlListRemoveApp(pkgName);
        wifiControlListRemoveApp(pkgName);
        notifyAppChange(Constant.MOBILE);
        notifyAppChange(Constant.WIFI);
    }
    
    private void wifiControlListRemoveApp(String pkgName) {
        if (null == pkgName) {
            return;
        }
        mWifiControlApps.remove(pkgName);
    }

    private void mobileControlListRemoveApp(String pkgName) {
        if (null == pkgName) {
            return;
        }
        mMobileControlApps.remove(pkgName);
    }

    @Override
    public synchronized void changePackage(String pkgName) {
        Log.d(TAG, "changePackage pkgName=" + pkgName);
        // Encryption App
        if (isValidControlApp(pkgName)) {
            int uid = TrafficassistantUtil.getUidByPackageName(mContext, pkgName);
            if (Constant.INVALID_UID == uid) {
                return;
            }
            addAppToMobileControlList(pkgName, uid);
            addAppToWifiControlList(pkgName, uid);
        } else {
            mobileControlListRemoveApp(pkgName);
            wifiControlListRemoveApp(pkgName);
        }
        notifyAppChange(Constant.MOBILE);
        notifyAppChange(Constant.WIFI);
    }
    
    private boolean isValidControlApp(String pkgName) {
        if(null == pkgName) {
            return false;
        }
        //
        if(isInExcludeApps(pkgName)) {
            return false;
        }
        //Encryption App
        ApplicationInfo appInfo = HelperUtils.getApplicationInfo(mContext, pkgName);
        if(null == appInfo) {
            return false;
        }

        if(appInfo.uid < Constant.SYSTEMID_CONSTANT) {
            return false;
        }
        
        List<ApplicationInfo> launcherApps = TrafficassistantUtil.getLauncherActivityApps(mContext);
        return TrafficassistantUtil.isInApplications(launcherApps, appInfo) &&
                TrafficassistantUtil.isUseInternetPermissionApp(mContext, pkgName);
    }
    
    private boolean isInExcludeApps(String pkgName) {
        for(int i = 0; i < mExcludeControlApps.size(); i++) {
            String excludeApp = mExcludeControlApps.get(i);
            if(null != excludeApp && excludeApp.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }
    
    private void notifyAppChange(int netType) {
        if(null == mControlAppChangeCallBacks) {
            return;
        }
        for( WeakReference<NetworkControlAppChangeCallBack> callback : mControlAppChangeCallBacks.values()) {
            NetworkControlAppChangeCallBack listener = callback.get();
            if (null != listener) {
                listener.controlAppNumChange(netType);
            }
        }
    }
}
//Gionee: mengdw <2016-07-18> add for CR01639347 end

