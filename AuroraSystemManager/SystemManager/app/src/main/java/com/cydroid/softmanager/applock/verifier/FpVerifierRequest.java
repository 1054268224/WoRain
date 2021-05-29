/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier;

import android.os.Handler;

import com.cydroid.softmanager.applock.verifier.auth.VerifierRequestManager;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpVerifier;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpVerifyResultCallback;
import com.cydroid.softmanager.utils.Log;

public class FpVerifierRequest implements 
        VerifierRequestManager.FpVerifierRequest {
    private static final String TAG = "FpVerifierRequest";

    private final FpVerifyResultCallback mFpVerifyResultCallback;
    private FpVerifier mFpVerifier;

    public FpVerifierRequest(FpVerifyResultCallback fpVerifyResultCallback) {
        mFpVerifyResultCallback = fpVerifyResultCallback;
    }

    @Override
    public void onObtainVerifier(FpVerifier verifier) {
        mFpVerifier = verifier;
        verifier.verify(mFpVerifyResultCallback);
    }

    @Override
    public void onLostVerifier() {
        if (null != mFpVerifier) {
            mFpVerifier.cancel();
            mFpVerifier = null;
        }
    }

    public boolean isFpVerifying() {
        if (null != mFpVerifier) {
            return mFpVerifier.isVerifying();
        }
        return false;
    }
}
