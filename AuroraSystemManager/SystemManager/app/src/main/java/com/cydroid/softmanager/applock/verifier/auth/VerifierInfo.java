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

public class VerifierInfo {
    public boolean mIsValidInfo = true;
    public boolean mIsFrozen = false;
    public int mSurplusTryCount = 0;
    public long mSurplusUnfrozenTick = 0L;

    public VerifierInfo(boolean b) {
        mIsValidInfo = b;
    }

    public VerifierInfo() {
        mIsValidInfo = true;
    }

    public static VerifierInfo invalidInfo() {
        return new VerifierInfo(false);
    }

    @Override
    public String toString() {
        return "VerifierInfo{" +
                "mIsValidInfo=" + mIsValidInfo +
                ", mIsFrozen=" + mIsFrozen +
                ", mSurplusTryCount=" + mSurplusTryCount +
                ", mSurplusUnfrozenTick=" + mSurplusUnfrozenTick +
                '}';
    }
}
