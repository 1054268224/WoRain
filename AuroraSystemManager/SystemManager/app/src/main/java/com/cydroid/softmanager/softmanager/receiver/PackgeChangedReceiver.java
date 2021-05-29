package com.cydroid.softmanager.softmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftSettingsManager;
import com.cydroid.softmanager.softmanager.model.ApplicationsInfo;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * File Description:
 *
 * @author: Gionee-lihq
 * @see: 2013-1-16 Change List:
 */
public class PackgeChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "PackgeChangedReceiver";
    // Gionee <houjie> <2015-09-08> modify for CR01532224 begin
    private static final HashMap<String, WeakReference<PackageChangedCallback>> mCallbacks = new HashMap<String, WeakReference<PackageChangedCallback>>();
    // Gionee <houjie> <2015-09-08> modify for CR01532224 end

    private BackgroundHandler mBackgroundHandler;
    private Context mContext;

    private static final String PREF_WHITELIST_CHANGE_STATS = "whitelist_change";
    private static final int WHITELIST_CHANGE = 1;
    private static final int AUTOBOOT_CHANGE = 2;

    // Gionee <liuyb> <2014-07-23> add for CR01316210 begin
    public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
    // Gionee <liuyb> <2014-07-23> add for CR01316210 end

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mBackgroundHandler = new BackgroundHandler();
        Log.d(TAG, "init whitelist");
        WhiteListManager whitelistManager = WhiteListManager.getInstance();
        whitelistManager.init(context);
        String actionStr = intent.getAction();
        Log.d(TAG, "onReceive action:" + actionStr);
        if (Intent.ACTION_PACKAGE_ADDED.equals(actionStr)) {
            Uri data = intent.getData();
            String pkgName = data.getEncodedSchemeSpecificPart();
            Log.d(TAG, "ACTION_PACKAGE_ADDED----->" + pkgName);
            if (!mCallbacks.isEmpty()) {
                Message message = mBackgroundHandler.obtainMessage(BackgroundHandler.PACKAGE_ADD, pkgName);
                mBackgroundHandler.sendMessage(message);
            }
            setDefaultSoftToRomApp(mContext);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(actionStr)) {
            Uri data = intent.getData();
            String pkgName = data.getEncodedSchemeSpecificPart();
            Log.d(TAG, "ACTION_PACKAGE_REMOVED----->" + pkgName);
            if (!mCallbacks.isEmpty()) {
                Message message = mBackgroundHandler.obtainMessage(BackgroundHandler.PACKAGE_REMOVE, pkgName);
                mBackgroundHandler.sendMessage(message);
            }
            setDefaultSoftToRomApp(mContext);
        }
        // Gionee <liuyb> <2014-07-23> add for CR01316210 begin
        else if (ACTION_LOCALE_CHANGED.equals(actionStr)) {
            Message message = mBackgroundHandler.obtainMessage(BackgroundHandler.LOCALE_CHANGED, null);
            mBackgroundHandler.sendMessage(message);
        }
        // Gionee <liuyb> <2014-07-23> add for CR01316210 end
    }

    private class BackgroundHandler extends Handler {
        static final int PACKAGE_ADD = 0;
        static final int PACKAGE_REMOVE = 1;
        // Gionee <liuyb> <2014-07-23> modify for CR01316210 begin
        static final int LOCALE_CHANGED = 2;
        // Gionee <liuyb> <2014-07-23> modify for CR01316210 end

        String pkgName;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PACKAGE_ADD:
                    pkgName = msg.obj.toString();
                    // Gionee <houjie> <2015-09-08> modify for CR01532224 begin
                    ApplicationsInfo.getInstance().addPackage(mContext, pkgName);
                    callCallbackAddPackage(pkgName);
                    // Gionee <houjie> <2015-09-08> modify for CR01532224 end
                    break;
                case PACKAGE_REMOVE:
                    if (ApplicationsInfo.mPackageSizeInfoMap != null) {
                        ApplicationsInfo.mPackageSizeInfoMap.remove(pkgName);
                    }
                    pkgName = msg.obj.toString();
                    // Gionee <houjie> <2015-09-08> modify for CR01532224 begin
                    ApplicationsInfo.getInstance().removePackage(mContext, pkgName);
                    callCallbackRemovePackage(pkgName);
                    // Gionee <houjie> <2015-09-08> modify for CR01532224 end
                    break;
                case LOCALE_CHANGED:
                    SoftHelperUtils.reflashLocale(mContext);
                    break;
                default:
                    break;
            }
        }
        // Gionee <liuyb> <2014-07-23> modify for CR01316210 end
    }

    // Gionee <houjie> <2015-09-08> modify for CR01532224 begin
    public static void setCallback(String key, PackageChangedCallback callback) {
        WeakReference<PackageChangedCallback> cb = new WeakReference<PackageChangedCallback>(callback);
        mCallbacks.put(key, cb);
    }

    public static void releaseCallback(String key) {
        mCallbacks.remove(key);
    }
    // Gionee <houjie> <2015-09-08> modify for CR01532224 end

    // Gionee <houjie> <2015-09-08> add for CR01532224 begin
    private static void callCallbackAddPackage(String pkgName) {
        if (mCallbacks != null) {
            for (WeakReference<PackageChangedCallback> cb : mCallbacks.values()) {
                PackageChangedCallback callback = cb.get();
                if (callback != null) {
                    callback.addPackage(pkgName);
                }
            }
        }
    }

    private static void callCallbackRemovePackage(String pkgName) {
        if (mCallbacks != null) {
            for (WeakReference<PackageChangedCallback> cb : mCallbacks.values()) {
                PackageChangedCallback callback = cb.get();
                if (callback != null) {
                    callback.removePackage(pkgName);
                }
            }
        }
    }
    // Gionee <houjie> <2015-09-08> add for CR01532224 end

    public void setDefaultSoftToRomApp(Context context) {
        DefaultSoftSettingsManager defaultSoftSettingsManager = DefaultSoftSettingsManager.getInstance();
        defaultSoftSettingsManager.init(context);
        defaultSoftSettingsManager.setNotHasDefaultSoftToRomApp();
    }
}
