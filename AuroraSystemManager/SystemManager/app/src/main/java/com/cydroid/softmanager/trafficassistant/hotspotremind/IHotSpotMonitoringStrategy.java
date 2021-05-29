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

public interface IHotSpotMonitoringStrategy {
    boolean isReachHotspotRemindLimit(int settingValue, float traffic);

    int setUseAgain(int settingValue);

    void init(int settingValue, float traffic);

    int getUseAgainValue(int settingValue);

    int getHotspotRemindLimit(int settingValue);

    void resetSettingValue(int settingValue);
}
