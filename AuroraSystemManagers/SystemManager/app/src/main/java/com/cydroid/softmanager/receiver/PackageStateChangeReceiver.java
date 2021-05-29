/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.interfaces.PackageChangedCallback;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PackageStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageStateChangeReceiver";

    private final String mTag;
    private static final int PACKAGE_ADD = 0;
    private static final int PACKAGE_REMOVE = 1;
    private static final int PACKAGE_CHANGED = 2;
    private static final Map<String, Integer> ACTION_MSG_MAP = new HashMap<>();

    static {
        ACTION_MSG_MAP.put(Intent.ACTION_PACKAGE_ADDED, PACKAGE_ADD);
        ACTION_MSG_MAP.put(Intent.ACTION_PACKAGE_REMOVED, PACKAGE_REMOVE);
        ACTION_MSG_MAP.put(Intent.ACTION_PACKAGE_CHANGED, PACKAGE_CHANGED);
    }

    private final Context mContext;
    private final PackageChangedCallback mPackageChangedCallback;
    private final BackgroundHandler mBackgroundHandler;
    private final HandlerThread mPackageStateChangeThread = new HandlerThread("PackageStateChangeReceiver/Background");

    private PackageStateChangeReceiver(Context context, PackageChangedCallback callback) {
        mContext = context;
        mPackageChangedCallback = callback;
        mPackageStateChangeThread.start();
        mBackgroundHandler = new BackgroundHandler(mPackageStateChangeThread.getLooper());
        mTag = Util.toString(this);
    }

    private void registerPackageStateChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mContext.registerReceiver(this, filter);
    }

    public void unregisterPackageStateChangeReceiver() {
        mContext.unregisterReceiver(this);
    }

    // 强引用，确保callback是各单例manager，或者在onDestroy中remove callback
    private final ArrayList<PackageChangedCallback> mCallbacks = new ArrayList<>();

    private static PackageStateChangeReceiver INSTANCE;

    private static boolean sRegistered = false;

    public synchronized static PackageStateChangeReceiver getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PackageStateChangeReceiver(context, null);
        }
        if (!sRegistered) {
            INSTANCE.registerPackageStateChangeReceiver();
            sRegistered = true;
        }
        return INSTANCE;
    }

    // 静态单例manager添加callback使用
    public static void addCallBack(Context context, PackageChangedCallback callback) {
        PackageStateChangeReceiver receiver = getInstance(context);
        receiver.addCallBack(callback);
    }

    public static void removeCallBack(Context context, PackageChangedCallback callback) {
        PackageStateChangeReceiver receiver = getInstance(context);
        receiver.removeCallBack(callback);
    }

    // 在Activity中先取回本类实例，再添加callback，并在onDestroy中remove callback
    public synchronized void addCallBack(PackageChangedCallback callback) {
        if (callback != null) {
            Log.d(mTag, "addCallBack: " + Util.toString(callback));
            mCallbacks.add(callback);
        }
    }

    public synchronized void removeCallBack(PackageChangedCallback callback) {
        if (callback != null) {
            Log.d(mTag, "removeCallBack: " + Util.toString(callback));
            mCallbacks.remove(callback);
        }
    }

    private void dispatchCallbacks(int message, String pkgName) {
        Log.d(mTag, "dispatchCallbacks: callbacks count=" + mCallbacks.size());
        for (PackageChangedCallback callback : mCallbacks) {
            if (callback != null) {
                // long start = SystemClock.elapsedRealtime();
                if (message == PACKAGE_ADD) {
                    callback.addPackage(pkgName);
                } else if (message == PACKAGE_REMOVE) {
                    callback.removePackage(pkgName);
                } else if (message == PACKAGE_CHANGED) {
                    callback.changePackage(pkgName);
                }
                // long end = SystemClock.elapsedRealtime();
                // Log.v(mTag, "dispatchCallbacks: callback[" + callback.getClass().getSimpleName()
                //         + "@" + callback.hashCode() + "] cost " + (end-start) + "ms");
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(mTag, "onReceive action:" + action);
        if ((!Intent.ACTION_PACKAGE_ADDED.equals(action)
                && !Intent.ACTION_PACKAGE_REMOVED.equals(action)
                && !Intent.ACTION_PACKAGE_CHANGED.equals(action))
                || null == mCallbacks) {
            return;
        }

        String packageName = getPackageName(intent);
        Log.d(TAG, "packageName:" + packageName);
        Message message = mBackgroundHandler.obtainMessage(ACTION_MSG_MAP.get(action), packageName);
        mBackgroundHandler.sendMessage(message);
    }

    private String getPackageName(Intent intent) {
        Uri data = intent.getData();
        return null == data ? "" : data.getEncodedSchemeSpecificPart();
    }

    class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            String packageName = message.obj.toString();
            Log.d(mTag, "BackgroundHandler msg.what:" + message.what + ", packageName:" + packageName);
            switch (message.what) {
                case PACKAGE_ADD:
                    // mPackageChangedCallback.addPackage(packageName);
                    dispatchCallbacks(PACKAGE_ADD, packageName);
                    break;
                case PACKAGE_REMOVE:
                    // mPackageChangedCallback.removePackage(packageName);
                    dispatchCallbacks(PACKAGE_REMOVE, packageName);
                    break;
                case PACKAGE_CHANGED:
                    // mPackageChangedCallback.changePackage(packageName);
                    dispatchCallbacks(PACKAGE_CHANGED, packageName);
                    break;
                default:
                    break;
            }
        }
    }
}