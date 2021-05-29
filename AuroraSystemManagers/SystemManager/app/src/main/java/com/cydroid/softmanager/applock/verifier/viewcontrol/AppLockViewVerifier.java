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

import com.android.internal.widget.LockPatternView;

import java.util.List;

public interface AppLockViewVerifier {
    void verifyPattern(List<LockPatternView.Cell> pattern);
    void verifyPin(String pin);
}
