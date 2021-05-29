/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Pu Yongming
 *
 * Date: 2017-03-24
 */
package com.cydroid.softmanager.applock.verifier;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class LifecycleAwareManager {
    private final Queue<LifecycleAware> handlerQueue = new ConcurrentLinkedQueue<>();

    public void registerHandler(LifecycleAware handler) {
        if (!handlerQueue.contains(handler)) {
            handlerQueue.add(handler);
        }
    }

    public void unregisterHandler(LifecycleAware handler) {
        handlerQueue.remove(handler);
    }

    public void clearHandler() {
        handlerQueue.clear();
    }

    public void notifyOnCreate() {
        for (LifecycleAware h : handlerQueue) {
            h.onCreate();
        }
    }

    public void notifyOnStart() {
        for (LifecycleAware h : handlerQueue) {
            h.onStart();
        }
    }

    public void notifyOnResume() {
        for (LifecycleAware h : handlerQueue) {
            h.onResume();
        }
    }

    public void notifyOnPause() {
        for (LifecycleAware h : handlerQueue) {
            h.onPause();
        }
    }

    public void notifyOnStop() {
        for (LifecycleAware h : handlerQueue) {
            h.onStop();
        }
    }

    public void notifyOnDestroy() {
        for (LifecycleAware h : handlerQueue) {
            h.onDestroy();
        }
    }

    public void notifyOnWindowFocusChanged(boolean focus) {
        for (LifecycleAware h : handlerQueue) {
            h.onWindowFocusChanged(focus);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("queue:").append(handlerQueue);
        return sb.toString();
    }
}
