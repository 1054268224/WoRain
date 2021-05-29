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

import com.cydroid.softmanager.applock.verifier.auth.AuthContextFactory;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FaceRecogSettingsUtil;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FrVerifyResultCallback;
import com.cydroid.softmanager.applock.verifier.statemachine.StateInfo;
import com.cydroid.softmanager.utils.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class FrController {
    private static final String TAG = "FrController";    

    private final String mTAG;
    private final Config mConfig;
    private final FrVerifierRequest mFrVerifierRequest;
    private final FaceRecogSettingsUtil mFaceRecogSettingsUtil;
    private final AuthContextFactory mAuthContextFactory;
    private final AtomicBoolean mFrVerifyFirst = new AtomicBoolean(true);
    
    public FrController(int key, Config config, 
            AuthContextFactory authContextFactory, FrVerifierRequest frVerifierRequest) {
        mTAG = TAG + key;
        mConfig = config;
        mFaceRecogSettingsUtil = authContextFactory.getFaceRecogSettingsUtil();
        mAuthContextFactory = authContextFactory;
        mFrVerifierRequest = frVerifierRequest;
    }

    public boolean isFrValid() {
        return mFaceRecogSettingsUtil.frCanUnlock() && mConfig.mUseFr;
    }

    public boolean isFrFrozen() {
        Log.d(TAG, "isFrFrozen:" + mFrVerifyFirst.get());
        return !mFrVerifyFirst.get();
    }

    public void handleFrWhenFpFrozen(StateInfo stateInfo) {
        if (isFrValid() && 0 == stateInfo.mTriggerFactor && !isFrFrozen()) {
            Log.d(mTAG, "handleFrWhenFpFrozen verifyFr");
            verifyFr();
            mFrVerifyFirst.set(false);
        }
    }

    private void verifyFr() {
        mAuthContextFactory.getVerifierRequestManager()
            .requestFrVerifier(mFrVerifierRequest);
    }

    public boolean handleFrWhenAllUsable(StateInfo stateInfo) {
        if (isFrValid() && 0 == stateInfo.mTriggerFactor) {
            Log.d(mTAG, "handleFrWhenAllUsable verifyFr");
            verifyFr();
            mFrVerifyFirst.set(false);
            return true;
        }
        return false;
    }

    public void cancelVerifyFr(boolean updateShow) {
        Log.d(mTAG, "cancelVerifyFr");
        mAuthContextFactory.getVerifierRequestManager()
            .cancelFrVerifierRequest(mFrVerifierRequest, updateShow);
    }

    public void removeFrVerifyPrompt() {
        mFrVerifierRequest.removeFrVerifyPrompt();
    }

    public boolean isFrVerifying() {
        return mFrVerifierRequest.isFrVerifying();
    }
}