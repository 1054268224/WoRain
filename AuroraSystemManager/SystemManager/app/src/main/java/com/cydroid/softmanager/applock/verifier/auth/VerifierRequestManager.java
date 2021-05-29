/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.auth;

import com.cydroid.softmanager.applock.verifier.auth.facerecog.FrVerifier;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FrVerifierFactory;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpVerifier;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpVerifierFactory;
import com.cydroid.softmanager.applock.verifier.auth.pwd.PwdVerifier;
import com.cydroid.softmanager.applock.verifier.auth.pwd.PwdVerifierFactory;
import com.cydroid.softmanager.utils.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VerifierRequestManager {
    private static final String TAG = "VerifierRequestManager";

    public interface FpVerifierRequest {
        void onObtainVerifier(FpVerifier verifier);
        void onLostVerifier();
    }

    public interface PwdVerifierRequest {
        void onObtainVerifier(PwdVerifier verifier);
    }

    public interface FrVerifierRequest {
        void onObtainVerifier(FrVerifier verifier);
        void onLostVerifier(boolean updateShow);
    }

    private final FpVerifierFactory mFpVerifierFactory;
    private final PwdVerifierFactory mPwdVerifierFactory;
    private final FrVerifierFactory mFrVerifierFactory;

    private final Queue<FpVerifierRequest> mFpVerifierRequestQueue =
        new ConcurrentLinkedQueue<>();
    private FpVerifierRequest mCurrentFpVerifierRequest = null;

    private FrVerifierRequest mCurrentFrVerifierRequest = null;

    public VerifierRequestManager(FpVerifierFactory fpVerifierFactory, 
            PwdVerifierFactory pwdVerifierFactory, FrVerifierFactory frVerifierFactory) {
        mFpVerifierFactory = fpVerifierFactory;
        mPwdVerifierFactory = pwdVerifierFactory;
        mFrVerifierFactory = frVerifierFactory;
    }

    public synchronized void requestFpVerifier(FpVerifierRequest fpVerifierRequest) {
        if (mCurrentFpVerifierRequest == fpVerifierRequest) {
            Log.w(TAG, "requestFpVerifier again!");
            return;
        }
        
        if (null != mCurrentFpVerifierRequest) {
            Log.d(TAG, "requestFpVerifier add to queue, " + toString());
            saveFpVerifierRequest(fpVerifierRequest);
        } else {
            Log.d(TAG, "requestFpVerifier setCurrentFpVerifierRequest");
            setCurrentFpVerifierRequest(fpVerifierRequest);
        }
    }

    private void saveFpVerifierRequest(FpVerifierRequest fpVerifierRequest) {
        if (!mFpVerifierRequestQueue.contains(fpVerifierRequest)) {
            mFpVerifierRequestQueue.offer(fpVerifierRequest);
        }
    }

    private void setCurrentFpVerifierRequest(FpVerifierRequest fpVerifierRequest) {
        mCurrentFpVerifierRequest = fpVerifierRequest;
        if (null != mCurrentFpVerifierRequest) {
            FpVerifier fpVerifier = mFpVerifierFactory.getFpVerifier();
            if (null != fpVerifier) {
                mCurrentFpVerifierRequest.onObtainVerifier(fpVerifier);
            }
        }
    }

    public synchronized void cancelFpVerifierRequest(FpVerifierRequest fpVerifierRequest) {
        if (mFpVerifierRequestQueue.contains(fpVerifierRequest)) {
            Log.d(TAG, "cancelFpVerifierRequest queue contains.");
            mFpVerifierRequestQueue.remove(fpVerifierRequest);
        } else if (mCurrentFpVerifierRequest == fpVerifierRequest) {
            Log.d(TAG, "cancelFpVerifierRequest current, mFpVerifierRequestQueue.size():"
                + mFpVerifierRequestQueue.size());
            mCurrentFpVerifierRequest.onLostVerifier();
            doNextFpVerifierRequest();
        } else {
            Log.w(TAG, "cancelFpVerifierRequest mCurrentFpVerifierRequest != fpVerifierRequest" 
                + " && !mFpVerifierRequestQueue.contains(fpVerifierRequest)");
        }
    }

    private synchronized void doNextFpVerifierRequest() {
        FpVerifierRequest fpVerifierRequest = mFpVerifierRequestQueue.poll();
        setCurrentFpVerifierRequest(fpVerifierRequest);
    }

    public void requestPwdVerifier(PwdVerifierRequest pwdVerifierRequest) {
        PwdVerifier pwdVerifier = mPwdVerifierFactory.getPwdVerifier();
        if (null != pwdVerifier) {
            pwdVerifierRequest.onObtainVerifier(pwdVerifier);
        }
    }

    public void cancelPwdVerifierRequest(PwdVerifierRequest pwdVerifierRequest) {
    }

    public synchronized void requestFrVerifier(FrVerifierRequest frVerifierRequest) {
        if (mCurrentFrVerifierRequest == frVerifierRequest) {
            Log.w(TAG, "requestFrVerifier again!");
            return;
        }

        if (null != mCurrentFrVerifierRequest) {
            Log.d(TAG, "requestFrVerifier mCurrentFrVerifierRequest != null, " + toString());
            mCurrentFrVerifierRequest.onLostVerifier(true);
        }
        setCurrentFrVerifierRequest(frVerifierRequest);
    }

    private void setCurrentFrVerifierRequest(FrVerifierRequest frVerifierRequest) {
        mCurrentFrVerifierRequest = frVerifierRequest;
        if (null != mCurrentFrVerifierRequest) {
            FrVerifier frVerifier = mFrVerifierFactory.getFrVerifier();
            if (null != frVerifier) {
                mCurrentFrVerifierRequest.onObtainVerifier(frVerifier);
            }
        }
    }

    public synchronized void cancelFrVerifierRequest(FrVerifierRequest frVerifierRequest, 
            boolean updateShow) {
        if (null == mCurrentFrVerifierRequest) {
            Log.w(TAG, "cancelFrVerifierRequest mCurrentFrVerifierRequest == null");
            return;
        } else if (mCurrentFrVerifierRequest == frVerifierRequest) {
            mCurrentFrVerifierRequest.onLostVerifier(updateShow);
            mCurrentFrVerifierRequest = null;
        } else {
            Log.w(TAG, "cancelFrVerifierRequest mCurrentFrVerifierRequest != frVerifierRequest");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VerifierRequestManager{");
        sb.append("mCurrentFpVerifierRequest:" + mCurrentFpVerifierRequest);
        sb.append(",mFpVerifierRequestQueue:" + mFpVerifierRequestQueue);
        sb.append(",mCurrentFrVerifierRequest:" + mCurrentFrVerifierRequest);
        sb.append("}");
        return sb.toString();
    }
}
