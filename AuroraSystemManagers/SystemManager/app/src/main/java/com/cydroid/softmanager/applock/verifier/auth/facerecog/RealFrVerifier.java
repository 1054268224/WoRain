/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.auth.facerecog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.cydroid.softmanager.utils.Log;

import gn.com.android.facerecog.aidl.IFaceRecogService;
import gn.com.android.facerecog.aidl.IServiceCallback;

public class RealFrVerifier implements FrVerifier {
    private static final String TAG = "RealFrVerifier";
    private static final String FACE_RECOG_SERVICE_BIND_ACTION =
            "gn.com.android.facerecog.RemoteService";
    private static final String FACE_RECOG_SERVICE_PACKAGE =
            "gn.com.android.facerecog";
    private static final int FACE_RECOG_TIMEOUT = 5000;

    private final String mTAG;
    private final Context mContext;
    private boolean mAuthenticating;
    private IFaceRecogService mService = null;
    private final FrServiceConnection mFrServiceConnection = new FrServiceConnection();
    private final RemoteFaceRecogServiceStub mRemoteFaceRecogServiceStub =
            new RemoteFaceRecogServiceStub(mFrServiceConnection);
    private final Handler mHandler = new Handler();

    private static final int MSG_VERIFY = 1;
    private static final int MSG_CANCEL = 2;

    private final BackgroundHandler mBackgroundHandler;
    private final HandlerThread mBackgroundThread = new HandlerThread("RealFrVerifier/Background");
    {
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
    }

    private class FrServiceConnection implements ServiceConnection {
        private FrVerifyResultCallback mFrVerifyResultCallback;
        
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mService = IFaceRecogService.Stub.asInterface((IBinder)service);
            final IServiceCallback.Stub callback = new IServiceCallback.Stub() {
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    Log.d(TAG, "onAuthenticationError errorCode:" + errorCode);
                    mHandler.post(new Runnable() {
                        public void run() {
                            mFrVerifyResultCallback.onFrVerifyFailed(errorCode);
                        }
                    });
                }
                public void onAuthenticationSucceeded(int resultCode) {
                    Log.d(TAG, "onAuthenticationSucceeded:" + resultCode);
                    if (0 == resultCode) {
                        mHandler.post(new Runnable() {
                        public void run() {
                                mFrVerifyResultCallback.onFrVerifySucceeded();
                            }
                        });
                    } else {
                        mHandler.post(new Runnable() {
                        public void run() {
                                mFrVerifyResultCallback.onFrVerifyTimeout(true);
                            }
                        });
                    }
                }
            };
            try {
                mService.startFaceRecognition(FACE_RECOG_TIMEOUT, callback);
            } catch (Exception e) {
                Log.e(TAG, "startFaceRecognition e:" + e.toString());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.d(TAG, "onServiceDisconnected");
        }
    }

    public RealFrVerifier(Context context) {
        mContext = context;
        mTAG = TAG + hashCode();
    }

    public void verify(final FrVerifyResultCallback cb) {
        Log.d(TAG, "verify");
        try {
            mFrServiceConnection.mFrVerifyResultCallback = cb;
            if (null == mService) {
                Message message = mBackgroundHandler.obtainMessage(MSG_VERIFY, mFrServiceConnection);
                mBackgroundHandler.sendMessage(message);
                //bindFaceRecogService(mFrServiceConnection);
            }
        } catch (Exception e) {
            Log.e(TAG, "verify e:" + e.toString());
        }
        mAuthenticating = true;
    }

    private void bindFaceRecogService(FrServiceConnection frServiceConnection) {
        try {
            Log.d(TAG, "bindFaceRecogService");
            Intent intent = new Intent(FACE_RECOG_SERVICE_BIND_ACTION);
            intent.setPackage(FACE_RECOG_SERVICE_PACKAGE);
            if (!mContext.bindService(intent, frServiceConnection,
                    Context.BIND_AUTO_CREATE)) {
                Log.e(TAG, "bindFaceRecogService failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "bindFaceRecogService failed. e:" + e.toString());
        }
    }

    public void cancel() {
        if (null == mService) {
            Log.w(TAG, "cancel null == mService");
            return;
        }

        try {
            Log.d(TAG, "unbindService");
            Message message = mBackgroundHandler.obtainMessage(MSG_CANCEL, mFrServiceConnection);
            mBackgroundHandler.sendMessage(message);
            //mContext.unbindService(mFrServiceConnection);
            //mService = null;
        } catch (Exception e) {
            Log.e(TAG, "cancel failed. e:" + e.toString());
        }
        mAuthenticating = false;
    }

    public boolean isVerifying() {
        return mAuthenticating;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RealFrVerifier{");
        sb.append("mAuthenticating=" + mAuthenticating);
        sb.append("}");
        return sb.toString();
    }

    class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            FrServiceConnection frServiceConnection;
            switch (message.what) {
                case MSG_VERIFY:
                    //frServiceConnection = (FrServiceConnection)message.obj;
                    //bindFaceRecogService(frServiceConnection);
                    mRemoteFaceRecogServiceStub.createIFaceRecogService(mContext);
                    break;
                case MSG_CANCEL:
                    frServiceConnection = (FrServiceConnection)message.obj;
                    //mContext.unbindService(frServiceConnection);
                    mRemoteFaceRecogServiceStub.releaseIFaceRecogService(mContext);
                    mService = null;
                    mBackgroundThread.quitSafely();
                    break;
                default:
                    break;
            }
        }
    }
}
