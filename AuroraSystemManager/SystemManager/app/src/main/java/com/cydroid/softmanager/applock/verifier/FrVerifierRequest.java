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
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FrVerifier;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FrVerifyResultCallback;
import com.cydroid.softmanager.utils.Log;

public class FrVerifierRequest implements 
        VerifierRequestManager.FrVerifierRequest {
    private static final String TAG = "FrVerifierRequest";
    private static final long PROMPT_DELAY = 2000L;

    private final Handler mHandler = new Handler();
    private FrVerifier mFrVerifier;
    private final FrVerifyResultCallback mFrVerifyResultCallback;
    private final Runnable mPromptRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.d(TAG, "mPromptRunnable");
            //mFrVerifyResultCallback.onFrVerifyPrompt();
        }
    };

    public FrVerifierRequest(FrVerifyResultCallback frVerifyResultCallback) {
        mFrVerifyResultCallback = frVerifyResultCallback;
    }

    @Override
    public void onObtainVerifier(FrVerifier verifier) {
        mFrVerifier = verifier;
        verifier.verify(mFrVerifyResultCallback);
        mHandler.postDelayed(mPromptRunnable, PROMPT_DELAY);
    }

    @Override
    public void onLostVerifier(boolean updateShow) {
        if (null != mFrVerifier) {
            removeFrVerifyPrompt();
            if (updateShow) {
                mFrVerifyResultCallback.onFrVerifyTimeout(false);
            }
            mFrVerifier.cancel();
            mFrVerifier = null;
        }
    }

    public boolean isFrVerifying() {
        if (null != mFrVerifier) {
            return mFrVerifier.isVerifying();
        }
        return false;
    }

    public void removeFrVerifyPrompt() {
        mHandler.removeCallbacks(mPromptRunnable);
    }
}
