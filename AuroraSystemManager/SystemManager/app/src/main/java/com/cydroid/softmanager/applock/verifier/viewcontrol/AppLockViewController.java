/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.viewcontrol;

import android.view.View;

public interface AppLockViewController {
    View createAppLockView(String packageName);
    void destroyAppLockView();
    void pauseAppLockView();
    void resumeAppLockView();

    void setAppLockViewVerifier(AppLockViewVerifier verifier);
    void unSetAppLockViewVerifier();

    void allFrozenShow(long pwdSurplusUnfrozenTick);
    void fpFrozenPwdVerifyFailShow(
            boolean fpValid, boolean frValid, boolean frFrozen, int pwdSurplusTryCount);
    void fpFrozenShow(boolean fpValid, boolean frValid,
                      boolean frFrozen, boolean isFirst, long fpSurplusUnfrozenTick);
    void pwdVerifySuccessShow(boolean fpValid, boolean frValid);
    void fpVerifySuccessShow();
    void pwdVerifyFailShow(
            boolean fpValid, boolean frValid, boolean frFrozen, int pwdSurplusTryCount);
    void fpVerifyFailShow(boolean fpValid, boolean frValid,
                          boolean frFrozen, int fpSurplusTryCount);
    void allUsableShow(boolean fpValid, boolean frValid, boolean frFrozen);

    void frVerifyPromptShow(boolean fpValid);
    void frVerifyTimeoutShow(boolean fpValid);
    void frVerifySuccessShow();
}
