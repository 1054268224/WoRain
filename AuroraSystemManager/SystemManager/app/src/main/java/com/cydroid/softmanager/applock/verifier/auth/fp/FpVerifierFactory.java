/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.auth.fp;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;

import com.cydroid.softmanager.applock.verifier.Config;

public class FpVerifierFactory {
    private static final String TAG = "FpVerifierFactory";

    private final Config mConfig;
    private final FpSettingsUtil mFpSettingsUtil;

    private final InvalidFpVerifier mInvalidFpVerifier;
    private final RealFpVerifier mRealFpVerifier;

    public FpVerifierFactory(Context context, 
            Config config, FpSettingsUtil fpSettingsUtil) {
        mConfig = config;
        mFpSettingsUtil = fpSettingsUtil;

        mInvalidFpVerifier = new InvalidFpVerifier();
        mRealFpVerifier = new RealFpVerifier(context);
    }

    public FpVerifier getFpVerifier() {
        FpVerifier fpVerifier = mInvalidFpVerifier;
        if (mConfig.mUseFp && mFpSettingsUtil.fpCanUnlock()) {
            fpVerifier = mRealFpVerifier;
        }
        return fpVerifier;
    }
}
