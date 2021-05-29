package com.cydroid.softmanager.applock.verifier.auth.facerecog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.cydroid.softmanager.utils.Log;

import gn.com.android.facerecog.aidl.IFaceRecogService;
import gn.com.android.facerecog.aidl.IServiceCallback;

/**
 * Created by houjie on 17-5-19.
 */
public class RemoteFaceRecogServiceStub {
    private static final String TAG = "RemoteFaceRecogServiceStub";
    private static final String SERVICE_BIND_ACTION =
            "gn.com.android.facerecog.RemoteService";
    private static final String SERVICE_PACKAGE =
            "gn.com.android.facerecog";

    private IFaceRecogService sService = null;
    private final Object sLock = new Object();
    private final ServiceConnection mConnectionCallback;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            synchronized (sLock) {
                sService = IFaceRecogService.Stub.asInterface((IBinder) service);
                mConnectionCallback.onServiceConnected(className, service);
                sLock.notify();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            sService = null;
            mConnectionCallback.onServiceDisconnected(className);
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    public RemoteFaceRecogServiceStub(ServiceConnection connectionCallback) {
        mConnectionCallback = connectionCallback;
    }

    public synchronized IFaceRecogService createIFaceRecogService(Context context) {
        if (null == sService) {
            bindFaceRecogService(context);
            try {
                synchronized (sLock) {
                    sLock.wait();
                }
            } catch (InterruptedException e) {
            }
        }
        return sService;
    }

    private void bindFaceRecogService(Context context) {
        try {
            Log.d(TAG, "bindFaceRecogService");
            Intent intent = new Intent(SERVICE_BIND_ACTION);
            intent.setPackage(SERVICE_PACKAGE);
            if (!context.bindService(intent, mConnection,
                    Context.BIND_AUTO_CREATE)) {
                Log.e(TAG, "bindFaceRecogService failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "bindFaceRecogService failed. e:" + e.toString());
        }
    }

    public void releaseIFaceRecogService(Context context) {
        unBindFaceRecogService(context);
    }

    private void unBindFaceRecogService(Context context) {
        if (sService == null) {
            Log.w(TAG, "unBindFaceRecogService sService == null");
            return;
        }
        try {
            Log.d(TAG, "unbindService");
            context.unbindService(mConnection);
        } catch (Exception e) {
            Log.e(TAG, "unBindFaceRecogService failed. e:" + e.toString());
        }
    }
}
