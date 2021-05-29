/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.auth.facerecog;

public class DebugFrVerifier implements FrVerifier {
    @Override
    public void verify(FrVerifyResultCallback cb) {
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isVerifying() {
        return false;
    }
}
