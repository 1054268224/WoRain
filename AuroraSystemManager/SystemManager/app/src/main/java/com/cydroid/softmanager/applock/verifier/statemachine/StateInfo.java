/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.statemachine;

public final class StateInfo {
    public int mKey = 0;
    public int mTriggerFactor = 0;

    public boolean mFpValid = true;
    public boolean mFpFrozen;
    public boolean mPwdFrozen;

    public int mFpSurplusTryCount = 0;
    public int mPwdSurplusTryCount = 0;

    public long mFpSurplusUnfrozenTick;
    public long mPwdSurplusUnfrozenTick;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StateInfo{"); 
        sb.append("mKey=" + mKey);
        sb.append(",mTriggerFactor=" + mTriggerFactor);
        sb.append(",mFpValid=" + mFpValid);
        sb.append(",mFpFrozen=" + mFpFrozen);
        sb.append(",mPwdFrozen=" + mPwdFrozen);
        sb.append(",mFpSurplusTryCount=" + mFpSurplusTryCount);
        sb.append(",mPwdSurplusTryCount=" + mPwdSurplusTryCount);
        sb.append(",mFpSurplusUnfrozenTick=" + mFpSurplusUnfrozenTick);
        sb.append(",mPwdSurplusUnfrozenTick=" + mPwdSurplusUnfrozenTick);
        sb.append("}");
        return sb.toString();
    }
}