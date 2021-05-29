package com.cydroid.softmanager.softmanager.utils;

import com.cydroid.softmanager.softmanager.interfaces.SoftMrgCallback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;

import com.cydroid.softmanager.utils.Log;

import java.util.HashMap;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-2-27 Change List:
 */
public class SoftMrgUtil {
    private static final String TAG = "SoftMrgUtil";
    private static final Object LOCK = new Object();
    private static SoftMrgUtil sInstance;

    private final MainHandler mMainHandler = new MainHandler();
    private final BackgroundHandler mHandler;
    private final HandlerThread mThread;

    private final PackageManager mPm;

    private PackageMoveObserver mPackageMoveObserver;
    private SoftMrgCallback mCallBack;

    private final HashMap<Integer, String> mMovedPackageName = new HashMap<Integer, String>();

    public static SoftMrgUtil getInstance(Context context) {
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new SoftMrgUtil(context);
            }
            return sInstance;
        }
    }

    private SoftMrgUtil(Context context) {
        mPm = context.getApplicationContext().getPackageManager();
        mThread = new HandlerThread("SoftMrg:", Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
        mHandler = new BackgroundHandler(mThread.getLooper());
    }

    public void setSoftMrgCallback(SoftMrgCallback callback) {
        this.mCallBack = callback;
    }

    public void movePackage(String pkgName, int flag) {
        Log.v(TAG, "start move package: " + pkgName);
        Message msg = mHandler.obtainMessage(BackgroundHandler.MSG_HANDLE_MOVE_PACKAGE, flag, 0, pkgName);
        mHandler.sendMessage(msg);
    }

    public class MainHandler extends Handler {
        public static final int MSG_HANDLE_FINISH = 0;
        public static final int MSG_HANDLE_UPDATE_RAM = 1;
        public static final int MSG_HANDLE_ONECLEAN_FINISH = 2;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_FINISH:
                    if (mCallBack != null) {
                        mCallBack.onFinish((String) msg.obj, msg.arg1);
                    }
                    break;
                case MSG_HANDLE_UPDATE_RAM:
                    break;
                case MSG_HANDLE_ONECLEAN_FINISH:
                default:
                    break;
            }
        }
    }

    private class BackgroundHandler extends Handler {
        static final int MSG_HANDLE_MOVE_PACKAGE = 0;

        public BackgroundHandler(Looper loop) {
            // TODO Auto-generated constructor stub
            super(loop);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MSG_HANDLE_MOVE_PACKAGE:
                    if (mPackageMoveObserver == null) {
                        mPackageMoveObserver = new PackageMoveObserver();
                    }
                    //mPm.movePackage((String) msg.obj, mPackageMoveObserver, msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    class PackageMoveObserver extends IPackageMoveObserver.Stub {
        public void packageMoved(String packageName, int returnCode) throws RemoteException {
            Message msg = mMainHandler.obtainMessage(MainHandler.MSG_HANDLE_FINISH, returnCode, 0,
                    packageName);
            mMainHandler.sendMessage(msg);
        }

        @Override
        public void onCreated(int moveId, Bundle extras) {
            String packageName = extras.getString(Intent.EXTRA_PACKAGE_NAME, "");
            mMovedPackageName.put(Integer.valueOf(moveId), packageName);
        }

        @Override
        public void onStatusChanged(int moveId, int status, long estMillis) {
            String packageName = mMovedPackageName.get(Integer.valueOf(moveId));

            if (packageName != null && !packageName.isEmpty()) {
                Message msg = mMainHandler.obtainMessage(MainHandler.MSG_HANDLE_FINISH, status, 0,
                        packageName);
                mMainHandler.sendMessage(msg);
            }
        }
    }
}
