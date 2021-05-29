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

public class HotSpotMonitoringStrategySimpleFactory {
    public static IHotSpotMonitoringStrategy createStrategy() {
        return createStrategy("default");
    }

    public static IHotSpotMonitoringStrategy createStrategy(String type) {
        switch (type) {
            case "default":
                return new DefaultHotSpotMonitoringStrategy();
            default:
                return new DefaultHotSpotMonitoringStrategy();
        }
    }

}
