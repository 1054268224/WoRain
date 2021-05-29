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

import com.android.internal.widget.LockPatternView;

import java.util.List;

public interface PwdVerifier {
    void verifyPattern(
            List<LockPatternView.Cell> pattern, PwdVerifyResultCallback cb);
    void verifyPin(String pin, PwdVerifyResultCallback cb);
}
