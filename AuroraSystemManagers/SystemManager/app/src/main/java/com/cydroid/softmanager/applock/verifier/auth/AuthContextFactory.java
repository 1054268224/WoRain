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

import com.cydroid.softmanager.applock.verifier.Config;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FaceRecogSettingsUtil;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FrVerifierFactory;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpVerifierFactory;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpSettingsUtil;
import com.cydroid.softmanager.applock.verifier.auth.pwd.GnLockPatternUtils;
import com.cydroid.softmanager.applock.verifier.auth.pwd.PwdVerifierFactory;

public class AuthContextFactory {
    private static final String TAG = "AuthContextFactory";
    private static AuthContextFactory sInstance;

    private final Context mContext;
    private final Config mConfig;
    private final GnLockPatternUtils mLockPatternUtils;
    private final VerifierUtil mVerifierUtil;
    private final FpSettingsUtil mFpSettingsUtil;
    private final FaceRecogSettingsUtil mFaceRecogSettingsUtil;
    private final VerifierInfoFetcher mVerifierInfoFetcher;
    private final VerifierRequestManager mVerifierRequestManager;

    public synchronized static AuthContextFactory getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new AuthContextFactory(context);
        }
        return sInstance;
    }

    private AuthContextFactory(Context context) {
        mContext = context.getApplicationContext();
        mConfig = new Config();
        mLockPatternUtils = new GnLockPatternUtils(mContext);
        // xionghg 2017-08-08 modify for applock 178296 begin
        // 当所有应用都遵循framework的规则时，切换为FrameworkVerifierUtil即可
        // mVerifierUtil = new FrameworkVerifierUtil(mContext);
        mVerifierUtil = new SettingVerifierUtil(mContext);
        // xionghg 2017-08-08 modify for applock 178296 end
        mFpSettingsUtil = new FpSettingsUtil(mContext);
        mFaceRecogSettingsUtil = new FaceRecogSettingsUtil(mContext);
        mVerifierInfoFetcher = new VerifierInfoFetcher(
            mConfig, mVerifierUtil, mFpSettingsUtil, mFaceRecogSettingsUtil);

        FpVerifierFactory fpVerifierFactory = new FpVerifierFactory(
            mContext, mConfig, mFpSettingsUtil);
        PwdVerifierFactory pwdVerifierFactory = new PwdVerifierFactory(
            mConfig, mLockPatternUtils);
        FrVerifierFactory frVerifierFactory = new FrVerifierFactory(
            mContext, mConfig, mFaceRecogSettingsUtil);

        mVerifierRequestManager = new VerifierRequestManager(
            fpVerifierFactory, pwdVerifierFactory, frVerifierFactory);
    }

    public Config getConfig() {
        return mConfig;
    }

    public GnLockPatternUtils getLockPatternUtils() {
        return mLockPatternUtils;
    }

    public VerifierUtil getVerifierUtil() {
        return mVerifierUtil;
    }

    public FpSettingsUtil getFpSettingsUtil() {
        return mFpSettingsUtil;
    }

    public FaceRecogSettingsUtil getFaceRecogSettingsUtil() {
        return mFaceRecogSettingsUtil;
    }

    public VerifierInfoFetcher getVerifierInfoFetcher() {
        return mVerifierInfoFetcher;
    }

    public VerifierRequestManager getVerifierRequestManager() {
        return mVerifierRequestManager;
    }
}
