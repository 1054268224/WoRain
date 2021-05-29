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

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.ServiceManager;
import android.os.UserHandle;

import com.android.internal.widget.ILockSettings;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Method;

public class FrameworkVerifierUtil implements VerifierUtil {
    private static final String TAG = "FrameworkVerifierUtil";

    private final FingerprintManager mFpm;

    public FrameworkVerifierUtil(Context context) {
        mFpm = context.getSystemService(FingerprintManager.class);
    }

    /*
    public int getPwdFailCount() {
        Object info = (Object)getCyeePwdVerifyInfo(getGnLockSettings(), UserHandle.myUserId());
        int result = 0;
        try {

            Class<?> tclass = (Class<?>)Class.forName("com.android.internal.widget.CyeePwdVerifyInfo");
            Method method = tclass.getDeclaredMethod("getPwdFailCount");
            result = (Integer)method.invoke(info);
            Log.i(TAG,"getPwdFailCount result="+result);
        } catch (Exception ex) {
            Log.i(TAG,"getPwdFailCount ex:"+ex);
            ex.printStackTrace();
        }
        return result;
    }
    */

    @Override
    public int getPwdFailRemainCount() {
        int result = 0;
        Object info = (Object)getCyeePwdVerifyInfo(
                getGnLockSettings(), UserHandle.myUserId());
        try {
            Class<?> tclass = (Class<?>)Class.forName(
                    "com.android.internal.widget.CyeePwdVerifyInfo");
            Method method = tclass.getDeclaredMethod("getPwdFailRemainCount");
            result = (Integer)method.invoke(info);
            Log.i(TAG,"getPwdFailRemainCount result="+result);
        } catch (Exception ex) {
            Log.i(TAG,"getPwdFailRemainCount ex:"+ex);
            ex.printStackTrace();
        }
        return result;
    }

    private Object getCyeePwdVerifyInfo(ILockSettings service, int userId) {
        Object result = null;
        try {
            Class<?> tclass = (Class<?>)Class.forName(
                    "com.android.internal.widget.ILockSettings");
            Method method = tclass.getDeclaredMethod(
                    "getCyeePwdVerifyInfo", int.class);
            result = method.invoke(service, userId);
        } catch (Exception ex) {
            Log.i(TAG,"getCyeePwdVerifyInfo ex:"+ex);
            ex.printStackTrace();
        }
        return result;
    }

    private ILockSettings getGnLockSettings() {
        ILockSettings LockSettingsService = ILockSettings.Stub.asInterface(
                ServiceManager.getService("lock_settings"));
        return LockSettingsService;
    }

    @Override
    public long getFreezeRemainingMillis() {
        long result = 0;
        Object info = (Object)getCyeePwdVerifyInfo(
                getGnLockSettings(), UserHandle.myUserId());
        try {
            Class<?> tclass = (Class<?>)Class.forName(
                    "com.android.internal.widget.CyeePwdVerifyInfo");
            Method method = tclass.getDeclaredMethod("getFreezeRemainingMillis");
            result = (long)method.invoke(info);
            Log.i(TAG,"getFreezeRemainingMillis result="+result);
        } catch (Exception ex) {
            Log.i(TAG,"getFreezeRemainingMillis ex:"+ex);
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean isInFreeze() {
        Boolean result = false;
        Object info = (Object)getCyeePwdVerifyInfo(
                getGnLockSettings(), UserHandle.myUserId());
        try {
            Class<?> tclass = (Class<?>)Class.forName(
                    "com.android.internal.widget.CyeePwdVerifyInfo");
            Method method = tclass.getDeclaredMethod("isInFreeze");
            result = (Boolean)method.invoke(info);
            Log.i(TAG,"isInFreeze result="+result);
        } catch (Exception ex) {
            Log.i(TAG,"isInFreeze ex:"+ex);
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public void verifySuccess() {
        fpUnlockSuccess();
        pwdUnlockSuccess();
    }

    @Override
    public void verifyFailed() {
        //null implements
    }

    private void pwdUnlockSuccess() {
        Object result = null;
        try {
            Class<?> tclass = (Class<?>)Class.forName(
                    "com.android.internal.widget.ILockSettings");
            Method method = tclass.getDeclaredMethod(
                    "resetCyeePwdVerifyOk", int.class);
            ILockSettings service = getGnLockSettings();
            result = method.invoke(service, 0);
        } catch (Exception ex) {
            Log.i(TAG,"pwdUnlockSuccess ex:"+ex);
            ex.printStackTrace();
        }
    }

    private void fpUnlockSuccess() {
        try {
            Class classType = Class.forName(
                    "android.hardware.fingerprint.FingerprintManager");
            Method method = classType.getMethod("resetTimeout", byte[].class);
            method.invoke(mFpm, new byte[]{});
        } catch (Exception ex) {
            Log.i(TAG,"fpUnlockSuccess ex:"+ex);
            ex.printStackTrace();
        }
    }

    private Object getFpState() {
        Object fingerprintCyeeInfo = null;
        try {
            Class classType = Class.forName(
                    "android.hardware.fingerprint.FingerprintManager");
            Method method = classType.getMethod("getFingerprintCyeeInfo");
            fingerprintCyeeInfo = method.invoke(mFpm);
        } catch (Exception ex) {
            Log.i(TAG,"getFpState ex:"+ex);
            ex.printStackTrace();
        }
        return fingerprintCyeeInfo;
    }

    @Override
    public boolean isFpInFreeze() {
        boolean isFreeze = false;
        Object fingerprintCyeeInfo = getFpState();
        if (null == fingerprintCyeeInfo) {
            return isFreeze;
        }
        try {
            Class classType = Class.forName(
                    "android.hardware.fingerprint.FingerprintCyeeInfo");
            Method isFpInFreeze = classType.getMethod("isFpInFreeze");
            isFreeze = (Boolean) isFpInFreeze.invoke(fingerprintCyeeInfo);
        } catch (Exception ex) {
            Log.i(TAG,"isFpInFreeze ex:"+ex);
            ex.printStackTrace();
        }
        return isFreeze;
    }

    /*
    public int getFpFailCount() {
        Object fingerprintCyeeInfo = getFpState();
        int fpFailCount = 0;

        if (fingerprintCyeeInfo != null) {
            try {
                Class classType = Class.forName("android.hardware.fingerprint.FingerprintCyeeInfo");
                Method getFpFailCount = classType.getMethod("getFpFailCount");
                fpFailCount = (Integer) getFpFailCount.invoke(fingerprintCyeeInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fpFailCount;
    }
    */

    @Override
    public int getFpFailRemainCount() {
        int fpFailRemainingCount = -1;
        Object fingerprintCyeeInfo = getFpState();
        if (null == fingerprintCyeeInfo) {
            return fpFailRemainingCount;
        }
        try {
            Class classType = Class.forName(
                    "android.hardware.fingerprint.FingerprintCyeeInfo");
            Method getFpFailRemainingCount = classType.getMethod(
                    "getFpFailRemainingCount");
            fpFailRemainingCount = (Integer) getFpFailRemainingCount
                    .invoke(fingerprintCyeeInfo);
        } catch (Exception ex) {
            Log.i(TAG,"getFpFailRemainCount ex:"+ex);
            ex.printStackTrace();
        }
        Log.e(TAG, "getFpFailRemainCount: "+fpFailRemainingCount);
        return fpFailRemainingCount;
    }

    @Override
    public long getFpFreezeRemainingMillis() {
        long fpFreezeRemainingMillis = 0;
        Object fingerprintCyeeInfo = getFpState();
        if (null == fingerprintCyeeInfo) {
            return fpFreezeRemainingMillis;
        }
        try {
            Class classType = Class.forName(
                    "android.hardware.fingerprint.FingerprintCyeeInfo");
            Method getFpFreezeRemainingMillis = classType
                    .getMethod("getFpFreezeRemainingMillis");
            fpFreezeRemainingMillis = (Long) getFpFreezeRemainingMillis
                    .invoke(fingerprintCyeeInfo);
        } catch (Exception ex) {
            Log.i(TAG,"getFpFreezeRemainingMillis ex:"+ex);
            ex.printStackTrace();
        }
        return fpFreezeRemainingMillis;
    }
}
