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

import com.cydroid.softmanager.applock.verifier.Config;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FaceRecogSettingsUtil;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpSettingsUtil;

public class VerifierInfoFetcher {
    private final VerifierUtil mVerifierUtil;
    private final FpSettingsUtil mFpSettingsUtil;
    private final FaceRecogSettingsUtil mFaceRecogSettingsUtil;
    private final Config mConfig;

    public VerifierInfoFetcher(Config config,
            VerifierUtil verifierUtil, FpSettingsUtil fpSettingsUtil,
            FaceRecogSettingsUtil faceRecogSettingsUtil) {
        mConfig = config;
        mVerifierUtil = verifierUtil;
        mFpSettingsUtil = fpSettingsUtil;
        mFaceRecogSettingsUtil = faceRecogSettingsUtil;
    }

    public VerifierInfo getFpInfo() {
        if (mFpSettingsUtil.fpCanUnlock() && mConfig.mUseFp) {
            VerifierInfo info = new VerifierInfo();
            info.mIsFrozen = mVerifierUtil.isFpInFreeze();
            info.mSurplusTryCount = mVerifierUtil.getFpFailRemainCount();
            info.mSurplusUnfrozenTick = mVerifierUtil.getFpFreezeRemainingMillis();
            return info;
        }
        return VerifierInfo.invalidInfo();
    }

    public VerifierInfo getPwdInfo() {
        VerifierInfo info = new VerifierInfo();
        info.mIsFrozen = mVerifierUtil.isInFreeze();
        info.mSurplusTryCount = mVerifierUtil.getPwdFailRemainCount();
        info.mSurplusUnfrozenTick = mVerifierUtil.getFreezeRemainingMillis();
        return info;
    }

    public VerifierInfo getFrInfo() {
        if (mFaceRecogSettingsUtil.frCanUnlock() && mConfig.mUseFr) {
            VerifierInfo info = new VerifierInfo();
            return info;
        }
        return VerifierInfo.invalidInfo();
    }
}
