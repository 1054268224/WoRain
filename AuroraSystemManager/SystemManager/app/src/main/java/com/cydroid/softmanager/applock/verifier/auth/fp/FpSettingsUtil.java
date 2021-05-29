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

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Settings;

import java.util.List;

public class FpSettingsUtil {
    private final static String FP_USED_FOR_UNLOCK = "fingerprint_used_for_unlock";

    private final Context mContext;
    private final FingerprintManager mFpm;

    public FpSettingsUtil(Context context) {
        mContext = context;
        mFpm = context.getSystemService(FingerprintManager.class);
    }

    public boolean fpCanUnlock() {
        return isUseFpToUnlock() 
            && isFpHardwareDetected() && fpCount() > 0;
    }

    private boolean isUseFpToUnlock() {
        int unlockValue = Settings.Secure.getInt(
            mContext.getContentResolver(), FP_USED_FOR_UNLOCK, 0);
        return unlockValue == 1;
    }

    private boolean isFpHardwareDetected() {
        return mFpm.isHardwareDetected();
    }

    private int fpCount() {
        final List<Fingerprint> items = mFpm.getEnrolledFingerprints();
        final int fpCount = items != null ? items.size() : 0;
        return fpCount;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FpSettingsUtil");
        sb.append(" fpCount: " + fpCount());
        sb.append(", isFpHardwareDetected: " + isFpHardwareDetected());
        sb.append(", isUseFpToUnlock: " + isUseFpToUnlock());
        return sb.toString();
    }
}