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

import cyee.provider.CyeeSettings;

import android.content.Context;

import com.cydroid.softmanager.utils.Log;

public class FaceRecogSettingsUtil {
    private static final String TAG = "FaceRecogSettingsUtil";

    private final Context mContext;

    public FaceRecogSettingsUtil(Context context) {
        mContext = context;
    }

    public boolean frCanUnlock() {
        Log.d(TAG, "frCanUnlock isUseFrToUnlock:" + isUseFrToUnlock() 
            + ", isFrHardwareDetected():" + isFrHardwareDetected()
            + ", frCount():" + frCount());
        return isUseFrToUnlock() 
            && isFrHardwareDetected() && frCount() > 0;
    }

    private boolean isUseFrToUnlock() {
        return CyeeSettings.getInt(mContext.getContentResolver(),
            "face_recognition_apply_applock", 0) == 1;
    }

    private boolean isFrHardwareDetected() {
        return CyeeSettings.getInt(
            mContext.getContentResolver(), "is_support_face_recognition", 0) == 1;
    }

    private int frCount() {
        return CyeeSettings.getInt(
            mContext.getContentResolver(), "face_recognition_count", 0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FaceRecogSettingsUtil");
        sb.append(" frCount: " + frCount());
        sb.append(", isFrHardwareDetected: " + isFrHardwareDetected());
        sb.append(", isUseFrToUnlock: " + isUseFrToUnlock());
        return sb.toString();
    }
}
