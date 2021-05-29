/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Pu Yongming
 *
 * Date: 2017-03-24
 */
package com.cydroid.softmanager.applock.verifier.statemachine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Observable<T> {
    private volatile T mLatest = null;
    private boolean mBehaviour = true;
    private final Queue<ISubscriber> mSubscriberList =
        new ConcurrentLinkedQueue<>();
    
    public Observable() {
        mBehaviour = true;
    }

    public Observable(boolean behaviour) {
        mBehaviour = behaviour;
    }

    public void subscribe(ISubscriber subscriber) {
        if (mSubscriberList.contains(subscriber)) {
            return;
        }
        mSubscriberList.add(subscriber);
        if (mBehaviour && null != mLatest) {
            subscriber.next(mLatest);
        }
    }

    public void unsubscribe(ISubscriber subscriber) {
        mSubscriberList.remove(subscriber);
    }

    public void publish(T t) {
        mLatest = t;
        for (ISubscriber<T> i : mSubscriberList) {
            i.next(t);
        }
    }

    public interface ISubscriber<T> {
        void next(T t);
    }

    public void clearLatest() {
        mLatest = null;
    }
}
