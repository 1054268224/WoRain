/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.widget.LockPatternView;
import com.cydroid.softmanager.applock.AppLockUtils;
import com.cydroid.softmanager.applock.verifier.auth.AuthContextFactory;
import com.cydroid.softmanager.applock.verifier.auth.facerecog.FrVerifyResultCallback;
import com.cydroid.softmanager.applock.verifier.auth.fp.FpVerifyResultCallback;
import com.cydroid.softmanager.applock.verifier.auth.pwd.PwdVerifyResultCallback;
import com.cydroid.softmanager.applock.verifier.statemachine.Observable;
import com.cydroid.softmanager.applock.verifier.statemachine.StateInfo;
import com.cydroid.softmanager.applock.verifier.statemachine.StateMachineController;
import com.cydroid.softmanager.applock.verifier.viewcontrol.AppLockViewVerifier;
import com.cydroid.softmanager.applock.verifier.viewcontrol.BasePwdViewController;
import com.cydroid.softmanager.applock.verifier.viewcontrol.PasswdViewControllerFactory;
import com.cydroid.softmanager.applock.verifier.viewcontrol.SecurityPwdUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

public final class AppLockController implements AppLockViewVerifier,
        PwdVerifyResultCallback, FpVerifyResultCallback, FrVerifyResultCallback {
    private static final String TAG = "AppLockController";

    public static final int TRIGGER_FACTOR_NORMAL = 0;
    public static final int TRIGGER_FACTOR_PWD_VERIFY_SUCCESS = 1;
    public static final int TRIGGER_FACTOR_PWD_VERIFY_FAIL = 2;
    public static final int TRIGGER_FACTOR_FP_VERIFY_SUCCESS = 3;
    public static final int TRIGGER_FACTOR_FP_VERIFY_FAIL = 4;

    private long mCurrentAppLockType = -1;
    private boolean mFinal = false;

    private final int mKey;
    private final String mTAG;
    private Context mContext;
    private LifecycleAwareManager mLifecycleAwareManager;
    private FrController mFrController;
    private AuthContextFactory mAuthContextFactory;
    private StateMachineController mStateMachineController;
    private FpVerifierRequest mFpVerifierRequest;
    private FrVerifierRequest mFrVerifierRequest;
    private BasePwdViewController mAppLockViewController;
    private final LifecycleAware mControllerLifecycleAwareListener = new LifecycleAware() {
        @Override
        public void onCreate() {
        }

        @Override
        public void onStart() {
            mStateMachineController.subscribeStateChange(mStateInfoListener);
        }

        @Override
        public void onResume() {
            Log.d(mTAG, "onResume");
            if (!mAuthContextFactory.getConfig().mUseFpWhenWindowFocus) {
                mStateMachineController.updateStateMachine(mKey, TRIGGER_FACTOR_NORMAL);
            }
            if (AppLockUtils.isLockScreenDisabled(mContext)) {
                mAppLockViewController.resumeAppLockView();
            }
        }

        @Override
        public void onPause() {
            Log.d(mTAG, "onPause");
            if (!mAuthContextFactory.getConfig().mUseFpWhenWindowFocus) {
                cancelVerifyFp();
                mFrController.cancelVerifyFr(false);
            }
            mAppLockViewController.pauseAppLockView();
        }

        @Override
        public void onStop() {
            mStateMachineController.unSubscribeStateChange(mStateInfoListener);
        }

        @Override
        public void onDestroy() {
            cancelVerifyFp();
            mFrController.cancelVerifyFr(false);
        }

        @Override
        public void onWindowFocusChanged(boolean focus) {
            if (!mAuthContextFactory.getConfig().mUseFpWhenWindowFocus) {
                return;
            }
            if (focus) {
                mStateMachineController.updateStateMachine(mKey, TRIGGER_FACTOR_NORMAL);
            } else {
                cancelVerifyFp();
                mFrController.cancelVerifyFr(false);
            }
        }
    };

    private void cancelVerifyFp() {
        mAuthContextFactory.getVerifierRequestManager()
                .cancelFpVerifierRequest(mFpVerifierRequest);
    }

    private final Observable.ISubscriber<StateInfo> mStateInfoListener =
            new Observable.ISubscriber<StateInfo>() {
                @Override
                public void next(StateInfo stateInfo) {
                    handleStateMachineChangeInfo(stateInfo);
                }
            };

    private void handleStateMachineChangeInfo(StateInfo stateInfo) {
        if (null == mAppLockViewController) {
            return;
        }
        Log.d(mTAG, "handleStateMachineChangeInfo " + stateInfo);

        final boolean fpValid = stateInfo.mFpValid;
        final boolean fpFrozen = stateInfo.mFpFrozen;
        final boolean pwdFrozen = stateInfo.mPwdFrozen;

        if (fpFrozen && pwdFrozen) {
            cancelVerifyFp();
            mFrController.cancelVerifyFr(false);
            handleAllFrozen(stateInfo);
        } else if (fpFrozen) {
            cancelVerifyFp();
            mFrController.handleFrWhenFpFrozen(stateInfo);
            handleFpFrozen(stateInfo);
        } else {
            if (stateInfo.mKey == mKey || stateInfo.mKey == 0) {
                if (fpValid) {
                    verifyFp();
                }
                mFrController.handleFrWhenAllUsable(stateInfo);
            }
            handleAllUsable(stateInfo);
        }
    }

    private void handleAllFrozen(StateInfo stateInfo) {
        mAppLockViewController.allFrozenShow(stateInfo.mPwdSurplusUnfrozenTick);
    }

    private void handleFpFrozen(StateInfo stateInfo) {
        final boolean fpValid = stateInfo.mFpValid;
        final boolean frValid = mFrController.isFrValid();
        final boolean frFrozen = !mFrController.isFrVerifying();

        if (TRIGGER_FACTOR_PWD_VERIFY_FAIL == stateInfo.mTriggerFactor
                && stateInfo.mKey == mKey) {
            mAppLockViewController.fpFrozenPwdVerifyFailShow(
                    fpValid, frValid, frFrozen, stateInfo.mPwdSurplusTryCount);
        } else {
            boolean isFirst = (TRIGGER_FACTOR_FP_VERIFY_FAIL == stateInfo.mTriggerFactor);
            mAppLockViewController.fpFrozenShow(
                    fpValid, frValid, frFrozen, isFirst, stateInfo.mFpSurplusUnfrozenTick);
        }
    }

    private void verifyFp() {
        boolean isLockScreenOn = AppLockUtils.isLockScreenOn(mContext);
        boolean isLockSecure = AppLockUtils.isLockSecure(mContext);
        boolean isInKidsHome = AppLockUtils.isInKidsHome(mContext);
        Log.d(mTAG, "verifyFp isLockScreenOn:" + isLockScreenOn
                + ", isLockSecure:" + isLockSecure
                + ", isInKidsHome:" + isInKidsHome);
        if (!isLockScreenOn || !isLockSecure || isInKidsHome) {
            mAuthContextFactory.getVerifierRequestManager()
                    .requestFpVerifier(mFpVerifierRequest);
        }
    }

    private void handleAllUsable(StateInfo stateInfo) {
        final boolean fpValid = stateInfo.mFpValid;
        final boolean frValid = mFrController.isFrValid();
        final boolean frFrozen = !mFrController.isFrVerifying();

        Log.d(mTAG, "handleAllUsable frValid:" + frValid + ", frFrozen:" + frFrozen);

        if (TRIGGER_FACTOR_PWD_VERIFY_SUCCESS == stateInfo.mTriggerFactor
                && stateInfo.mKey == mKey) {
            mStateMachineController.unSubscribeStateChange(mStateInfoListener);
            mAppLockViewController.pwdVerifySuccessShow(fpValid, frValid);
        } else if (TRIGGER_FACTOR_FP_VERIFY_SUCCESS == stateInfo.mTriggerFactor
                && stateInfo.mKey == mKey) {
            mStateMachineController.unSubscribeStateChange(mStateInfoListener);
            mAppLockViewController.fpVerifySuccessShow();
        } else if (TRIGGER_FACTOR_PWD_VERIFY_FAIL == stateInfo.mTriggerFactor
                && stateInfo.mKey == mKey) {
            mAppLockViewController.pwdVerifyFailShow(
                    fpValid, frValid, frFrozen, stateInfo.mPwdSurplusTryCount);
        } else if (TRIGGER_FACTOR_FP_VERIFY_FAIL == stateInfo.mTriggerFactor
                && stateInfo.mKey == mKey) {
            mAppLockViewController.fpVerifyFailShow(
                    fpValid, frValid, frFrozen, stateInfo.mFpSurplusTryCount);
        } else {
            mAppLockViewController.allUsableShow(fpValid, frValid, frFrozen);
        }
    }

    public AppLockController(Context context,
                             LifecycleAwareManager lifecycleAwareManager) {
        mKey = hashCode();
        mTAG = TAG + mKey;
        mContext = context;
        mLifecycleAwareManager = lifecycleAwareManager;
    }

    public void init() {
        mAuthContextFactory = AuthContextFactory.getInstance(mContext);
        mStateMachineController = StateMachineController
                .createStateMachineController(mAuthContextFactory.getVerifierInfoFetcher());
        mLifecycleAwareManager.registerHandler(mControllerLifecycleAwareListener);
        mFpVerifierRequest = new FpVerifierRequest(this);
        mFrVerifierRequest = new FrVerifierRequest(this);
        mFrController = new FrController(mKey, mAuthContextFactory.getConfig(),
                mAuthContextFactory, mFrVerifierRequest);
    }

    @Override
    public void verifyPattern(List<LockPatternView.Cell> pattern) {
        Log.d(mTAG, "verifyPattern");
        PwdVerifierRequest pwdVerifierRequest = new PwdVerifierRequest(pattern, this);
        mAuthContextFactory.getVerifierRequestManager().requestPwdVerifier(pwdVerifierRequest);
    }

    @Override
    public void verifyPin(String pin) {
        Log.d(mTAG, "verifyPin");
        PwdVerifierRequest pwdVerifierRequest = new PwdVerifierRequest(pin, this);
        mAuthContextFactory.getVerifierRequestManager().requestPwdVerifier(pwdVerifierRequest);
    }

    @Override
    public void onPwdVerifySucceeded() {
        Log.d(mTAG, "onPwdVerifySucceeded mFinal:" + mFinal);
        mFinal = true;
        mAuthContextFactory.getVerifierUtil().verifySuccess();
        mStateMachineController.updateStateMachine(mKey, TRIGGER_FACTOR_PWD_VERIFY_SUCCESS);
    }

    @Override
    public void onPwdVerifyFailed() {
        Log.d(mTAG, "onPwdVerifyFailed mFinal:" + mFinal);
        if (!mFinal) {
            // xionghg 2017-08-08 modify for applock 178296 begin
            mAuthContextFactory.getVerifierUtil().verifyFailed();
            // xionghg 2017-08-08 modify for applock 178296 end
            mStateMachineController.updateStateMachine(mKey, TRIGGER_FACTOR_PWD_VERIFY_FAIL);
        }
    }

    @Override
    public void onFpVerifySucceeded() {
        Log.d(mTAG, "onFpVerifySucceeded mFinal:" + mFinal);
        mFinal = true;
        mAuthContextFactory.getVerifierUtil().verifySuccess();
        mStateMachineController.updateStateMachine(mKey, TRIGGER_FACTOR_FP_VERIFY_SUCCESS);
    }

    @Override
    public void onFpVerifyFailed() {
        Log.d(mTAG, "onFpVerifyFailed mFinal:" + mFinal);
        if (!mFinal) {
            // xionghg 2017-08-08 modify for applock 178296 begin
            mAuthContextFactory.getVerifierUtil().verifyFailed();
            // xionghg 2017-08-08 modify for applock 178296 end
            mStateMachineController.updateStateMachine(mKey, TRIGGER_FACTOR_FP_VERIFY_FAIL);
        }
    }

    @Override
    public void onFrVerifyPrompt() {
        Log.d(mTAG, "onFrVerifyPrompt mFinal:" + mFinal);
        if (!mFinal) {
            mAppLockViewController.frVerifyPromptShow(mStateMachineController.canUseFp());
        }
    }

    @Override
    public void onFrVerifyTimeout(boolean cancel) {
        Log.d(mTAG, "onFrVerifyTimeout cancel:" + cancel + ", mFinal:" + mFinal);
        if (!mFinal) {
            if (cancel) {
                mFrController.cancelVerifyFr(false);
            }
            mAppLockViewController.frVerifyTimeoutShow(mStateMachineController.canUseFp());
        }
    }

    @Override
    public void onFrVerifySucceeded() {
        Log.d(mTAG, "onFrVerifySucceeded mFinal:" + mFinal);
        mFinal = true;
        mAuthContextFactory.getVerifierUtil().verifySuccess();
        mFrController.cancelVerifyFr(false);
        mAppLockViewController.frVerifySuccessShow();
    }

    @Override
    public void onFrVerifyFailed(int code) {
        Log.d(mTAG, "onFrVerifyFailed code:" + code + ", mFinal:" + mFinal);
        if (!mFinal) {
            mFrController.cancelVerifyFr(false);
            mAppLockViewController.frVerifyTimeoutShow(mStateMachineController.canUseFp());
        }
    }

    public void createAppLockViewIfNeed(ViewGroup container, String packageName) {
        long type = SecurityPwdUtils.getSecurityPasswordType();
        if (type == mCurrentAppLockType) {
            return;
        }
        Log.d(mTAG, "createAppLockViewIfNeed type:" + type
                + ", mCurrentAppLockType:" + mCurrentAppLockType);
        destroyAppLockViewController();
        createAppLockViewController(type);
        createAppLockView(container, packageName);
        mCurrentAppLockType = type;
    }

    private void destroyAppLockViewController() {
        if (null != mAppLockViewController) {
            mLifecycleAwareManager.unregisterHandler(mAppLockViewController);
            mAppLockViewController.unSetAppLockViewVerifier();
            mAppLockViewController.destroyAppLockView();
            mAppLockViewController = null;
        }
    }

    private void createAppLockViewController(long type) {
        PasswdViewControllerFactory factory = PasswdViewControllerFactory.getInstance();
        mAppLockViewController = factory.createPasswdViewController(type, mContext);
        if (null == mAppLockViewController) {
            throw new RuntimeException(
                    "createAppLockViewController; type " + type + " controller is null");
        }
        mAppLockViewController.setAppLockViewVerifier(this);
        mLifecycleAwareManager.registerHandler(mAppLockViewController);
    }

    private void createAppLockView(ViewGroup container, String packageName) {
        container.removeAllViews();
        View appLockView = createAppLockView(packageName);
        if (null != appLockView) {
            container.addView(appLockView);
        } else {
            Log.w(mTAG, "createAppLockView warning: contentView is null.");
        }
    }

    private View createAppLockView(String packageName) {
        return mAppLockViewController.createAppLockView(packageName);
    }

    public void adjustSoftInputMode(Window window) {
        boolean isLockScreenOn = AppLockUtils.isLockScreenOn(mContext);
        boolean isLockSecure = AppLockUtils.isLockSecure(mContext);
        boolean isInKidsHome = AppLockUtils.isInKidsHome(mContext);
        Log.d(mTAG, "adjustSoftInputMode isLockScreenOn:" + isLockScreenOn
                + ", isLockSecure:" + isLockSecure
                + ", isInKidsHome:" + isInKidsHome);
        if (!isLockScreenOn || !isLockSecure || isInKidsHome) {
            long type = SecurityPwdUtils.getSecurityPasswordType();
            adjustSoftInputModeByPasswdType(window, type);
        }
    }

    private void adjustSoftInputModeByPasswdType(
            Window window, long type) {
        // PATTERN_TYPE
        if (type == 1L) {
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } else {
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
                            //| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    public void deinit() {
        mStateMachineController.unSubscribeStateChange(mStateInfoListener);
        mLifecycleAwareManager.unregisterHandler(mControllerLifecycleAwareListener);
        mLifecycleAwareManager.unregisterHandler(mAppLockViewController);
        mLifecycleAwareManager = null;
        mContext = null;
    }

    public int getKey() {
        return mKey;
    }
}
