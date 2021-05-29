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

public class InvalidFpVerifier implements FpVerifier {
    @Override
    public void verify(FpVerifyResultCallback cb) {
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isVerifying() {
        return false;
    }
}
