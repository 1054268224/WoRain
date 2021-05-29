/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.auth.pwd;

import com.android.internal.widget.LockPatternUtils;

import com.cydroid.softmanager.applock.verifier.Config;

public class PwdVerifierFactory {
    private static final String TAG = "PwdVerifierFactory";

    private final Config mConfig;
    private final DebugPwdVerifier mDebugPwdVerifier;
    private final RealPwdVerifier mRealPwdVerifier;

    public PwdVerifierFactory(Config config,
            LockPatternUtils lockPatternUtils) {
        mConfig = config;
        mDebugPwdVerifier = new DebugPwdVerifier();
        mRealPwdVerifier = new RealPwdVerifier(lockPatternUtils);
    }

    public PwdVerifier getPwdVerifier() {
        PwdVerifier pwdVerifier = mDebugPwdVerifier;
        if (!mConfig.mDebugPwd) {
            pwdVerifier = mRealPwdVerifier;
        }
        return pwdVerifier;
    }
}
