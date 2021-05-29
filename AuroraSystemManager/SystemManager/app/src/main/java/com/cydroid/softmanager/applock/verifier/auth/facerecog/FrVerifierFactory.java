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

import android.content.Context;

import com.cydroid.softmanager.applock.verifier.Config;

public class FrVerifierFactory {
    private static final String TAG = "FrVerifierFactory";

    private final Context mContext;
    private final Config mConfig;
    private final FaceRecogSettingsUtil mFaceRecogSettingsUtil;

    private final DebugFrVerifier mDebugFrVerifier;
    //private RealFrVerifier mRealFrVerifier;

    public FrVerifierFactory(Context context, 
            Config config, FaceRecogSettingsUtil faceRecogSettingsUtil) {
        mContext = context;
        mConfig = config;
        mFaceRecogSettingsUtil = faceRecogSettingsUtil;

        mDebugFrVerifier = new DebugFrVerifier();
        //mRealFrVerifier = new RealFrVerifier(context);
    }

    public FrVerifier getFrVerifier() {
        FrVerifier frVerifier = mDebugFrVerifier;
        if (mConfig.mUseFr && mFaceRecogSettingsUtil.frCanUnlock()) {
            frVerifier = new RealFrVerifier(mContext);
        }
        return frVerifier;
    }
}
