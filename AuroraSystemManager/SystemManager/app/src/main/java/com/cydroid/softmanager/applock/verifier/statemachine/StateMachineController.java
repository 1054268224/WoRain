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

import com.cydroid.softmanager.applock.verifier.auth.VerifierInfoFetcher;
import com.cydroid.softmanager.utils.Log;

public class StateMachineController {
    private static final String TAG = "StateMachineController";
    private static StateMachineController sInstance;

    private StateMachine mStateMachine;
    private final Observable<StateInfo> mStateChangedObservable =
        new Observable<StateInfo>(false);

    public synchronized static StateMachineController createStateMachineController(
            VerifierInfoFetcher verifierInfoFetcher) {
        if (null == sInstance) {
            sInstance = new StateMachineController();
            sInstance.initStateMachine(verifierInfoFetcher);
        }
        return sInstance;
    }

    private StateMachineController() {
    }
    
    private void initStateMachine(VerifierInfoFetcher verifierInfoFetcher) {
        mStateMachine = new StateMachine(verifierInfoFetcher, mStateChangedObservable);
        mStateMachine.start();
    }

    public void updateStateMachine(int key, int triggerFactor) {
        //waitTime(200);
        mStateMachine.syncInternal(key, triggerFactor);
    }

    public boolean canUsePwd() {
        StateMachine.State currentState = mStateMachine.getCurrentState();
        if (null != currentState) {
            return currentState != mStateMachine.getBothUnusableState();
        }
        return false;
    }

    public boolean canUseFp() {
        StateMachine.State currentState = mStateMachine.getCurrentState();
        if (null != currentState && mStateMachine.isFpValid()) {
            return currentState == mStateMachine.getBothUsableState();
        }
        return false;
    }

    public void subscribeStateChange(
            Observable.ISubscriber<StateInfo> Observer) {
        mStateChangedObservable.subscribe(Observer);
    }

    public void unSubscribeStateChange(
            Observable.ISubscriber<StateInfo> Observer) {
        mStateChangedObservable.unsubscribe(Observer);
    }

    public void clearStateChange() {
        mStateChangedObservable.clearLatest();
    }

    private void waitTime(long ms) {
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait(ms);
            } catch (Exception e) {
                Log.i(TAG, "waitTime obj.wait ", e);
            }
        }
    }
}
