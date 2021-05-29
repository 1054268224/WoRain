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

import com.android.internal.widget.LockPatternView;

import com.cydroid.softmanager.applock.verifier.auth.VerifierRequestManager;
import com.cydroid.softmanager.applock.verifier.auth.pwd.PwdVerifier;
import com.cydroid.softmanager.applock.verifier.auth.pwd.PwdVerifyResultCallback;

import java.util.List;

public class PwdVerifierRequest implements 
        VerifierRequestManager.PwdVerifierRequest {
    private static final String TAG = "PwdVerifierRequest";

    private String mPin = null;
    private List<LockPatternView.Cell> mPattern = null;
    private PwdVerifyResultCallback mPwdVerifyResultCallback = null;

    public PwdVerifierRequest(String pin,
            PwdVerifyResultCallback pwdVerifyResultCallback) {
        mPin = pin;
        mPwdVerifyResultCallback = pwdVerifyResultCallback;
    }

    public PwdVerifierRequest(List<LockPatternView.Cell> pattern,
            PwdVerifyResultCallback pwdVerifyResultCallback) {
        mPattern = pattern;
        mPwdVerifyResultCallback = pwdVerifyResultCallback;
    }

    @Override
    public void onObtainVerifier(PwdVerifier verifier) {
        if (null != mPin) {
            verifier.verifyPin(mPin, mPwdVerifyResultCallback);
        } else if (null != mPattern) {
            verifier.verifyPattern(mPattern, mPwdVerifyResultCallback);
        }
    }
}