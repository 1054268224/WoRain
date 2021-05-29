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

public interface FrVerifyResultCallback {
    void onFrVerifyPrompt();
    void onFrVerifyTimeout(boolean cancel);
    void onFrVerifySucceeded();
    void onFrVerifyFailed(int code);
}
