/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: yangxinruo
 *
 * Date: 2016-10-19
 */
package com.cydroid.softmanager.trafficassistant.hotspotremind;

import com.cydroid.softmanager.utils.Log;

public class DefaultHotSpotMonitoringStrategy implements IHotSpotMonitoringStrategy {

    private static final String TAG = "DefaultHotSpotMonitoringStrategy";

    private static final int MAX_REMINDER_AGAIN_VALUE = 50;// MBytes
    private float mBaseTrafficVal = 0;// 基准监测流量
    private int mCurrentThreshold = 0;// 当前阈值
    private int mCurrentSettingValue = 0;// 当前设置值

    @Override
    public boolean isReachHotspotRemindLimit(int settingValue, float traffic) {
        // 相对流量与阈值作比较
        return (traffic - mBaseTrafficVal) > (mCurrentThreshold * 1024 * 1024 * 1f);
    }

    @Override
    public void resetSettingValue(int settingValue) {
        // 更新设置值,重置阈值
        mCurrentSettingValue = settingValue;
        mCurrentThreshold = settingValue;
        Log.d(TAG, "reset Threshold to " + mCurrentThreshold);
    }

    @Override
    public int setUseAgain(int settingValue) {
        // 更新阈值,阈值累加
        mCurrentThreshold += getUseAgainValue(mCurrentSettingValue);
        return mCurrentThreshold;
    }

    @Override
    public void init(int settingValue, float traffic) {
        // 设置监测基准流量
        mBaseTrafficVal = traffic;
        resetSettingValue(settingValue);
    }

    @Override
    public int getUseAgainValue(int settingValue) {
        // 获取每次阈值累加值,不超过50MB
        if (mCurrentSettingValue > MAX_REMINDER_AGAIN_VALUE) {
            return MAX_REMINDER_AGAIN_VALUE;
        } else {
            return mCurrentSettingValue;
        }
    }

    @Override
    public int getHotspotRemindLimit(int settingValue) {
        // 返回当前阈值
        return mCurrentThreshold;
    }

}
