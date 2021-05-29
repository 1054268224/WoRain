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

public interface PwdVerifyResultCallback {
    void onPwdVerifySucceeded();
    void onPwdVerifyFailed();
}
