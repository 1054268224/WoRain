// mengdw <2016-10-09> add for CR01766193 begin
package com.cydroid.softmanager.trafficassistant.hotspotremind.model;

import android.content.Context;

import com.cydroid.softmanager.monitor.utils.TrafficMonitorUtil;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;

public class HotspotTrafficCalculator {
    private static final String TAG = "HotspotTrafficCalculator";
    private static final int START_TIME = 0;
    
    private final Context mContext;
    private TrafficMonitorUtil mTrafficMonitorUtil;
    
    public HotspotTrafficCalculator(Context context) {
        mContext = context.getApplicationContext();
    }
    
    public float getHotSpotTraffic() {
        long endTime = getCurTime();
        // mengdw <2017-04-25> add for 107414 begin
        int simIndex = TrafficassistantUtil.getSimCardNo(mContext);
        mTrafficMonitorUtil = new TrafficMonitorUtil(mContext, START_TIME, endTime, simIndex);
        mTrafficMonitorUtil.initNetworkParam(mContext, simIndex);
        mTrafficMonitorUtil.updateQueryTime(START_TIME, endTime);
        float traffic = mTrafficMonitorUtil.getHotSportTraffic();
        mTrafficMonitorUtil.closeSession();
        Log.d(TAG, "getHotSpotTraffic traffic=" + traffic + " endTime=" + endTime + " simIndex=" + simIndex);
        // mengdw <2017-04-25> add for 107414 end
        return traffic;
    }
    
    public float getDiffHotSpotTraffic(float startTraffic) {
        float diffTraffic = 0;
        float curTraffic = getHotSpotTraffic();
        if(curTraffic > startTraffic) {
            diffTraffic = curTraffic - startTraffic;
        }
        return diffTraffic;
    }
    
    private  long getCurTime() {
        int[] timeArray = null;
        long strartTime = System.currentTimeMillis();
        timeArray = TimeFormat.getNowTimeArray();
        timeArray[1] += 1;

        if (timeArray != null) {
            strartTime = TimeFormat.getStartTime(timeArray[0], timeArray[1], timeArray[2], timeArray[3],
                    timeArray[4], timeArray[5]);
        }
        return strartTime;
    }
}
// mengdw <2016-10-09> add for CR01766193 end