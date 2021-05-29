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

public interface VerifierUtil {

    boolean isInFreeze();

    int getPwdFailRemainCount();

    long getFreezeRemainingMillis();

    void verifySuccess();

    void verifyFailed();

    boolean isFpInFreeze();

    int getFpFailRemainCount();

    long getFpFreezeRemainingMillis();
}
