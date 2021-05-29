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

public interface FpVerifier {
    void verify(FpVerifyResultCallback cb);
    void cancel();
    boolean isVerifying();
}
