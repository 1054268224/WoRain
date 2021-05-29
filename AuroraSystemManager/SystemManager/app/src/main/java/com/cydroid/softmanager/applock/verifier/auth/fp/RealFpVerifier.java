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
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Method;

public class RealFpVerifier implements FpVerifier {
    private static final String TAG = "RealFpVerifier";

    private final FingerprintManager mFpm;
    private CancellationSignal mCancellationSignal;
    private boolean mAuthenticating;

    public RealFpVerifier(Context context) {
        mFpm = context.getSystemService(FingerprintManager.class);
    }

    public void verify(FpVerifyResultCallback cb) {
        if (mFpm.getEnrolledFingerprints().size() <= 0) {
            Log.w(TAG, "verify mFpm.getEnrolledFingerprints().size() <= 0");
            return;
        }
        Log.d(TAG, "verify");
        mCancellationSignal = new CancellationSignal();
        mFpm.authenticate(null, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.w(TAG, "onAuthenticationError errorCode:" 
                        + errorCode + ", errString:" + errString);
                if (errorCode == 7) {
                    mAuthenticating = false;
                    // Gionee xionghg 2017-08-04 delete for 178296 begin
                    // 外部5次验证失败后自动发生，后面应该根据此error判断指纹是否可用
                    //cb.onFpVerifyFailed();
                    // Gionee xionghg 2017-08-04 delete for 178296 end
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                Log.w(TAG, "onAuthenticationHelp helpCode:" 
                        + helpCode + ", helpString:" + helpString);
                if (helpCode == 1200 || helpCode == 1201) {
                    cb.onFpVerifyFailed();
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "onAuthenticationSucceeded");
                mAuthenticating = false;
                cb.onFpVerifySucceeded();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "onAuthenticationFailed");
                cb.onFpVerifyFailed();
            }

            @Override
            public void onAuthenticationAcquired(int acquireInfo) {
                super.onAuthenticationAcquired(acquireInfo);
                Log.d(TAG, "onAuthenticationAcquired");
            }
        }, null);
        // Gionee xionghg 2017-07-31 add for 178296 begin
        // 将指纹验证和密码验证合并之后，好像没有必要解开5次上限，为了不影响其他应用先解开
        try {
            Method method = FingerprintManager.class.getMethod("setOff");
            method.invoke(mFpm);
            Log.d(TAG, "setOff: success");
        } catch (Exception e) {
            Log.e(TAG, "setOff: ", e);
        }
        // Gionee xionghg 2017-07-31 add for 178296 end
        mAuthenticating = true;
    }

    public void cancel() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            // Gionee xionghg 2017-07-31 add for 178296 begin
            try {
                Method method = FingerprintManager.class.getMethod("setOn");
                method.invoke(mFpm);
                Log.d(TAG, "setOn: success");
            } catch (Exception e) {
                Log.e(TAG, "setOn: ", e);
            }
            // Gionee xionghg 2017-07-31 add for 178296 end
        }
        mAuthenticating = false;
    }

    public boolean isVerifying() {
        return mAuthenticating;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RealFpVerifier{");
        sb.append("mAuthenticating=" + mAuthenticating);
        sb.append("}");
        return sb.toString();
    }
}
