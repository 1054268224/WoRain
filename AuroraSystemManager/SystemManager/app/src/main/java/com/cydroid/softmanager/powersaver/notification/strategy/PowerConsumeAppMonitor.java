/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用检测策略接口类
 *
 * Revised Date: 2017-02-05
 */
package com.cydroid.softmanager.powersaver.notification.strategy;

import java.util.ArrayList;
import java.util.HashMap;

//Gionee <yangxinruo> <2016-02-1> modify for CR01634814 begin
public abstract class PowerConsumeAppMonitor {
    public static final int STATE_DEACTIVATED = 1;
    public static final int STATE_ACTIVATED = 2;
    protected long mMonitorInterval = -1L;

    private IMonitorStateChangedCallback mStateCallback;
    private int mState = STATE_DEACTIVATED;

    public abstract void init();

    public abstract void dataUpdate(ArrayList<Integer> excludeList);

    public long getUpdateInterval() {
        return mMonitorInterval;
    }

    protected void setTimeInterval(long interval) {
        mMonitorInterval = interval;
    }

    public void addStateChangedCallback(IMonitorStateChangedCallback callback) {
        mStateCallback = callback;
    }

    private void setState(int newState) {
        if (newState == mState) {
            return;
        }
        mState = newState;
        if (mStateCallback == null) {
            return;
        }
        switch (mState) {
            case STATE_DEACTIVATED:
                mStateCallback.onDeactivate(this);
                break;
            case STATE_ACTIVATED:
                mStateCallback.onActivate(this);
                break;
            default:
                return;
        }
    }

    public int getState() {
        return mState;
    }

    public void deactivate() {
        setState(STATE_DEACTIVATED);
    }

    public boolean activate() {
        setState(STATE_ACTIVATED);
        return getState() == STATE_ACTIVATED;
    }

    public interface IMonitorStateChangedCallback {

        void onDeactivate(PowerConsumeAppMonitor monitor);

        void onActivate(PowerConsumeAppMonitor monitor);

    }

    public void dataUpdate() {
        dataUpdate(new ArrayList<Integer>());
    }

    public abstract HashMap<String, Double> getOverThresholdData();

}
//Gionee <yangxinruo> <2016-02-1> modify for CR01634814 end
