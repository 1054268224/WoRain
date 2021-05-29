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

import android.os.CountDownTimer;

import com.cydroid.softmanager.applock.verifier.auth.VerifierInfo;
import com.cydroid.softmanager.applock.verifier.auth.VerifierInfoFetcher;
import com.cydroid.softmanager.utils.Log;

import static com.cydroid.softmanager.applock.verifier.AppLockController.TRIGGER_FACTOR_FP_VERIFY_FAIL;
import static com.cydroid.softmanager.applock.verifier.AppLockController.TRIGGER_FACTOR_FP_VERIFY_SUCCESS;
import static com.cydroid.softmanager.applock.verifier.AppLockController.TRIGGER_FACTOR_NORMAL;
import static com.cydroid.softmanager.applock.verifier.AppLockController.TRIGGER_FACTOR_PWD_VERIFY_SUCCESS;

public class StateMachine {
    private static final String TAG = "StateMachine";

    private final State mUnknownState = new UnknownState();
    private final State mBothUsableState = new BothUsableState();
    private final State mFpUnusableState = new FpUnusableState();
    private final State mBothUnusableState = new BothUnusableState();
    private State mCurrentState;

    private final VerifierInfoFetcher mVerifierInfoFetcher;
    private final Observable<StateInfo> mStateInfoChangedObservable;

    StateMachine(VerifierInfoFetcher verifierInfoFetcher, 
            Observable<StateInfo> stateInfoChangedObservable) {
        mVerifierInfoFetcher = verifierInfoFetcher;
        mStateInfoChangedObservable = stateInfoChangedObservable;
        initState();
    }

    private void initState() {
        stateTransfer(mUnknownState);
    }

    private void stateTransfer(State toState) {
        stateTransfer(toState, 0, 0);
    }

    private void stateTransfer(State toState, int key, int triggerFactor) {
        Log.d(TAG, "stateTransfer mCurrentState:" + mCurrentState + ", toState:" 
            + toState + ", key:" + key + ", triggerFactor:" + triggerFactor);
        if (null == mCurrentState) {
            mCurrentState = toState;
            mCurrentState.onEnter(key, triggerFactor);
        } else {
            boolean sameState = (mCurrentState == toState);
            if (sameState) {
                mCurrentState.reset(key, triggerFactor);
            } else {
                mCurrentState.onExit();
                mCurrentState = toState;
                mCurrentState.onEnter(key, triggerFactor);
            }
        }
    }

    void start() {
        Log.d(TAG, "start");
        syncInternal();
    }

    void syncInternal() {
        syncInternal(0, 0);
    }

    // Gionee xionghg 2017-08-04 add for 178296 begin
    private final static long MIN_UNFROZEN_TICK = 30 * 1000L;
    private int mFpFailCount = 0;
    private int mTotalFailTimes = 0;
    // Gionee xionghg 2017-08-04 add for 178296 end

    void syncInternal(int key, int triggerFactor) {
        VerifierInfo fpInfo = mVerifierInfoFetcher.getFpInfo();
        VerifierInfo pwdInfo = mVerifierInfoFetcher.getPwdInfo();

        // Gionee xionghg 2017-08-04 add for 178296 begin
        // 当发生了一次验证且我们没有从framework得到指纹验证的数据
        if (triggerFactor != TRIGGER_FACTOR_NORMAL && fpInfo.mSurplusTryCount < 0) {
            switch (triggerFactor) {
                case TRIGGER_FACTOR_FP_VERIFY_SUCCESS:
                case TRIGGER_FACTOR_PWD_VERIFY_SUCCESS:
                    mFpFailCount = 0;
                    mTotalFailTimes = 0;
                    break;
                case TRIGGER_FACTOR_FP_VERIFY_FAIL:
                    mFpFailCount++;
                    break;
                default:
                    break;
            }
            Log.d(TAG, "Before merge, pwdInfo:" + pwdInfo + ",mFpFailCount=" + mFpFailCount
                    + ",mTotalFailTimes=" + mTotalFailTimes);
            pwdInfo.mSurplusTryCount -= mFpFailCount;
            fpInfo.mSurplusTryCount = pwdInfo.mSurplusTryCount;
            if (pwdInfo.mSurplusTryCount <= 0) {
                // 第一轮验证有5次，失败后锁30s，后面每轮验证只有1次，锁的时间翻倍，验证成功则轮次清零
                // 这也是之前手机密码验证的逻辑，现在是为了合并指纹验证的次数和手机密码验证的次数
                pwdInfo.mIsFrozen = true;
                pwdInfo.mSurplusUnfrozenTick = MIN_UNFROZEN_TICK << mTotalFailTimes;
                if (mTotalFailTimes < 11) mTotalFailTimes++;
            }
        }
        // Gionee xionghg 2017-08-04 add for 178296 end
        Log.d(TAG, "syncInternal key:" + key + ", triggerFactor:" + triggerFactor 
            + ", fpInfo:" + fpInfo + ", pwdInfo:" + pwdInfo);

        if (fpInfo.mIsValidInfo && pwdInfo.mIsValidInfo) {
            if (!fpInfo.mIsFrozen && !pwdInfo.mIsFrozen) {  // both can use
                ((BothUsableState) mBothUsableState)
                    .fpSurplusTryCount = fpInfo.mSurplusTryCount;
                ((BothUsableState) mBothUsableState)
                    .pwdSurplusTryCount = pwdInfo.mSurplusTryCount;
                ((BothUsableState) mBothUsableState)
                    .fpValid = fpInfo.mIsValidInfo;
                stateTransfer(mBothUsableState, key, triggerFactor);
            } else if (pwdInfo.mIsFrozen) {                 // both unuseable
                ((BothUnusableState) mBothUnusableState)
                    .totalRemainning = pwdInfo.mSurplusUnfrozenTick;
                ((BothUnusableState) mBothUnusableState)
                    .currentRemaining = pwdInfo.mSurplusUnfrozenTick;
                stateTransfer(mBothUnusableState, 0, triggerFactor);
            } else {                                        // fp unuseable
                ((FpUnusableState) mFpUnusableState)
                    .pwdSurplusTryCount = pwdInfo.mSurplusTryCount;
                ((FpUnusableState) mFpUnusableState)
                    .totalRemainning = fpInfo.mSurplusUnfrozenTick;
                ((FpUnusableState) mFpUnusableState)
                    .currentRemaining = fpInfo.mSurplusUnfrozenTick;
                stateTransfer(mFpUnusableState, key, triggerFactor);
            }
        } else {
            if (!pwdInfo.mIsFrozen) {                       // both can use
                ((BothUsableState) mBothUsableState)
                    .pwdSurplusTryCount = pwdInfo.mSurplusTryCount;
                ((BothUsableState) mBothUsableState)
                    .fpValid = fpInfo.mIsValidInfo;
                stateTransfer(mBothUsableState, key, triggerFactor);
            } else {                   // both unuseable
                ((BothUnusableState) mBothUnusableState)
                    .totalRemainning  = pwdInfo.mSurplusUnfrozenTick;
                ((BothUnusableState) mBothUnusableState)
                    .currentRemaining  = pwdInfo.mSurplusUnfrozenTick;
                stateTransfer(mBothUnusableState, 0, triggerFactor);
            }
        }
    }

    void stop() {
        Log.d(TAG, "stop");
    }

    State getBothUsableState() {
        return mBothUsableState;
    }

    State getFpUnusableState() {
        return mFpUnusableState;
    }

    State getBothUnusableState() {
        return mBothUnusableState;
    }

    State getCurrentState() {
        return mCurrentState;
    }

    boolean isFpValid() {
        VerifierInfo fpInfo = mVerifierInfoFetcher.getFpInfo();
        return fpInfo.mIsValidInfo;
    }

    abstract class State {
        void onEnter(int key, int triggerFactor) {
            Log.d(TAG, "onEnter: " + toString());
            notifyStateChanged(key, triggerFactor);
        }

        protected void notifyStateChanged() {
            notifyStateChanged(0, 0);
        }

        protected void notifyStateChanged(int key, int triggerFactor) {
            publishEvent(generateStateInfo(key, triggerFactor));
        }

        abstract StateInfo generateStateInfo(int key, int triggerFactor);

        void onExit() {
            Log.d(TAG, "onExit: " + toString());
        }

        abstract void reset(int key, int triggerFactor);
    }

    private void publishEvent(StateInfo stateInfo) {
        Log.d(TAG, "publishEvent: " + stateInfo);
        mStateInfoChangedObservable.publish(stateInfo);
    }

    class UnknownState extends State {
        @Override
        public String toString() {
            return "UnknownState";
        }

        @Override
        StateInfo generateStateInfo(int key, int triggerFactor) {
            StateInfo stateInfo = new StateInfo();
            stateInfo.mKey = key;
            stateInfo.mTriggerFactor = triggerFactor;
            stateInfo.mFpFrozen = false;
            stateInfo.mFpSurplusTryCount = 5;
            stateInfo.mPwdFrozen = false;
            stateInfo.mPwdSurplusTryCount = 5;
            return stateInfo;
        }

        @Override
        void reset(int key, int triggerFactor) {
        }
    }

    class BothUsableState extends State {
        int fpSurplusTryCount;
        int pwdSurplusTryCount;
        boolean fpValid;

        @Override
        public String toString() {
            return "BothUsableState";
        }

        @Override
        StateInfo generateStateInfo(int key, int triggerFactor) {
            StateInfo stateInfo = new StateInfo();
            stateInfo.mKey = key;
            stateInfo.mTriggerFactor = triggerFactor;
            stateInfo.mFpValid = fpValid;
            stateInfo.mFpFrozen = false;
            stateInfo.mFpSurplusTryCount = fpSurplusTryCount;
            stateInfo.mPwdFrozen = false;
            stateInfo.mPwdSurplusTryCount = pwdSurplusTryCount;
            return stateInfo;
        }

        @Override
        void reset(int key, int triggerFactor) {
            notifyStateChanged(key, triggerFactor);
        }
    }

    class FpUnusableState extends State {
        long currentRemaining;
        long totalRemainning;

        int pwdSurplusTryCount;
        CountDownTimer downTimer;

        @Override
        public String toString() {
            return "FpUnusableState";
        }

        @Override
        void onEnter(int key, int triggerFactor) {
            super.onEnter(key, triggerFactor);
            startTimer();
        }

        @Override
        void onExit() {
            super.onExit();
            stopTimer();
        }

        @Override
        StateInfo generateStateInfo(int key, int triggerFactor) {
            StateInfo stateInfo = new StateInfo();
            stateInfo.mKey = key;
            stateInfo.mTriggerFactor = triggerFactor;
            stateInfo.mFpFrozen = true;
            stateInfo.mPwdFrozen = false;
            stateInfo.mPwdSurplusTryCount = pwdSurplusTryCount;
            stateInfo.mFpSurplusUnfrozenTick = currentRemaining;
            return stateInfo;
        }

        @Override
        void reset(int key, int triggerFactor) {
            notifyStateChanged(key, triggerFactor);
            stopTimer();
            startTimer();
        }

        private void startTimer() {
            downTimer = new CountDownTimer(totalRemainning, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    currentRemaining = millisUntilFinished;
                    //notifyStateChanged();
                }

                @Override
                public void onFinish() {
                    stateTransfer(mBothUsableState);
                }
            };
            downTimer.start();
        }

        private void stopTimer() {
            if (null != downTimer) {
                downTimer.cancel();
            }
        }
    }

    class BothUnusableState extends State {
        long currentRemaining;
        long totalRemainning;
        CountDownTimer downTimer;

        @Override
        public String toString() {
            return "BothUnusableState";
        }

        @Override
        void onEnter(int key, int triggerFactor) {
            super.onEnter(key, triggerFactor);
            startTimer();
        }

        @Override
        void onExit() {
            super.onExit();
            stopTimer();
        }

        @Override
        StateInfo generateStateInfo(int key, int triggerFactor) {
            StateInfo stateInfo = new StateInfo();
            stateInfo.mKey = key;
            stateInfo.mTriggerFactor = triggerFactor;
            stateInfo.mFpFrozen = true;
            stateInfo.mPwdFrozen = true;
            stateInfo.mPwdSurplusUnfrozenTick = currentRemaining;
            return stateInfo;
        }

        @Override
        void reset(int key, int triggerFactor) {
            notifyStateChanged(key, triggerFactor);
            stopTimer();
            startTimer();
        }

        private void startTimer() {
            downTimer = new CountDownTimer(totalRemainning, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    currentRemaining = millisUntilFinished;
                    notifyStateChanged();
                }

                @Override
                public void onFinish() {
                    syncInternal();
                }
            };
            downTimer.start();
        }

        private void stopTimer() {
            if (null != downTimer) {
                downTimer.cancel();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Statemachine{");
        sb.append("current:");
        sb.append(mCurrentState);
        sb.append("}");
        return sb.toString();
    }
}
