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

//import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;

import com.cydroid.softmanager.utils.Log;

import java.util.List;

public class RealPwdVerifier implements PwdVerifier {
    private static final String TAG = "RealPwdVerifier";
    
    private final LockPatternUtils mLockPatternUtils;
    
    public RealPwdVerifier(LockPatternUtils lockPatternUtils) {
        mLockPatternUtils = lockPatternUtils;
    }

    @Override
    public void verifyPattern(
            List<LockPatternView.Cell> pattern, PwdVerifyResultCallback cb) {
       /* LockPatternChecker.checkPattern(mLockPatternUtils, pattern, 0,
            new LockPatternChecker.OnCheckCallback() {
                @Override
                public void onChecked(boolean matched, int timeoutMs) {
                    Log.d(TAG, "onChecked(" + matched + ", " + timeoutMs + ")");
                    if (matched) {
                        cb.onPwdVerifySucceeded();
                    } else {
                        cb.onPwdVerifyFailed();
                    }
                }
            });*/
    }

    @Override
    public void verifyPin(String pin, PwdVerifyResultCallback cb) {
      /*  LockPatternChecker.checkPassword(mLockPatternUtils, pin, 0,
            new LockPatternChecker.OnCheckCallback() {
                @Override
                public void onChecked(boolean matched, int timeoutMs) {
                    Log.d(TAG, "onChecked(" + matched +", " + timeoutMs + ")");
                    if (matched) {
                        cb.onPwdVerifySucceeded();
                    } else {
                        cb.onPwdVerifyFailed();
                    }
                }
            });*/
    }
}
