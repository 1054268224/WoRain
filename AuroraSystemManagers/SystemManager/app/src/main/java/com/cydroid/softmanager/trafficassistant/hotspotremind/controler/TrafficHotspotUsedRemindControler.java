// mengdw <2016-10-09> add for CR01766193 begin
package com.cydroid.softmanager.trafficassistant.hotspotremind.controler;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.cydroid.softmanager.trafficassistant.hotspotremind.HotSpotMonitorDataHelper;
import com.cydroid.softmanager.trafficassistant.hotspotremind.HotSpotMonitoringStrategySimpleFactory;
import com.cydroid.softmanager.trafficassistant.hotspotremind.IHotSpotMonitoringStrategy;
import com.cydroid.softmanager.trafficassistant.hotspotremind.model.HotspotTrafficCalculator;
import com.cydroid.softmanager.trafficassistant.hotspotremind.TrafficHotSpotWarningDialog;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;

public class TrafficHotspotUsedRemindControler {
    private static final String TAG = "TrafficHotspotUsedRemindControler";
    
    private static final int QUERY_HOTSPORT_TRAFFIC_MESSAGE = 0;
    private static final int QUERY_HOTSPORT_TRAFFIC_END_MESSAGE = 1;
    private static final int DELAY_TIME = 60 * 1000;// 60s
    
    private static final String KEY_HOTSPOT_TRAFFIC = "hotspotTraffic";
    private static TrafficHotspotUsedRemindControler mInstance;
    
    private final Context mContext;
    private final HotSportTrafficHandler mHotSportTrafficHandler;
    private final IHotSpotMonitoringStrategy mHotSpotMonitoringStrategy;
    private final HotSpotMonitorDataHelper mHotSpotMonitorDataHelper;
    private final HotspotTrafficCalculator mHotspotTrafficCalculator;
    private boolean mIsRunning = false;
    
    public static TrafficHotspotUsedRemindControler getInstance(Context context) {
        synchronized(TrafficHotspotUsedRemindControler.class) {
            if (mInstance == null) {
                mInstance = new TrafficHotspotUsedRemindControler(context);
            }
        }
        return mInstance;
    }
    
    public boolean isMonitorRunning() {
        return mIsRunning;
    }
    
    public void startHotspotMonitor(int startType) {
        int settingValue = mHotSpotMonitorDataHelper.getHotspotRemindSettingValue(mContext);
        float startTraffic = mHotspotTrafficCalculator.getHotSpotTraffic();
        int simIndex = TrafficassistantUtil.getSimCardNo(mContext);
        if(Constant.TYPE_HOTSPOT_START_NORMAL == startType) {
            mHotSpotMonitorDataHelper.setHotSpotStartTraffic(mContext, startTraffic, simIndex);
        } else {
            startTraffic = mHotSpotMonitorDataHelper.getHotSpotStartTraffic(mContext, simIndex);
        }
        mHotSpotMonitoringStrategy.init(settingValue, startTraffic);
        Log.d(TAG, "startHotspotMonitor settingValue=" + settingValue + " traffic=" + startTraffic + " simIndex=" + simIndex);
        if(null == mHotSportTrafficHandler) {
            Log.d(TAG, "startHotspotMonitor mHotSportTrafficHandler is null");
            return;
        }
        mHotSportTrafficHandler.sendEmptyMessageDelayed(QUERY_HOTSPORT_TRAFFIC_MESSAGE, DELAY_TIME);
        mIsRunning = true;
    }
    
    public void selectUseAgain() {
        int settingValue = mHotSpotMonitorDataHelper.getHotspotRemindSettingValue(mContext);
        mHotSpotMonitoringStrategy.setUseAgain(settingValue);
        Log.d(TAG, "selectUseAgain settingValue= " + settingValue);
        if(null == mHotSportTrafficHandler) {
            Log.d(TAG, "selectUseAgain mHotSportTrafficHandler is null");
            return;
        }
        mHotSportTrafficHandler.sendEmptyMessageDelayed(QUERY_HOTSPORT_TRAFFIC_MESSAGE, DELAY_TIME);
    }
    
    public void restartTrafficQuery() {
        if(null == mHotSportTrafficHandler) {
            Log.d(TAG, "reStartTrafficQuery mHotSportTrafficHandler is null");
            return;
        }
        mHotSportTrafficHandler.sendEmptyMessageDelayed(QUERY_HOTSPORT_TRAFFIC_MESSAGE, DELAY_TIME);
    }
    
    public void restartSettingValue(int settingValue) {
        mHotSpotMonitoringStrategy.resetSettingValue(settingValue);
    }
    
    public int getUseAgainValue() {
        int settingValue = mHotSpotMonitorDataHelper.getHotspotRemindSettingValue(mContext);
        int againValue = mHotSpotMonitoringStrategy.getUseAgainValue(settingValue);
        Log.d(TAG, "getUseAgainValue settingValue= " + settingValue + " againValue=" + againValue);
        return againValue;
    }
    
    public int getRemindLimit() {
        int settingValue = mHotSpotMonitorDataHelper.getHotspotRemindSettingValue(mContext);
        int remimdLimit = mHotSpotMonitoringStrategy.getHotspotRemindLimit(settingValue);
        Log.d(TAG, "getRemindLimit settingValue= " + settingValue + " remimdLimit=" + remimdLimit);
        return remimdLimit;
    }
    
    public float getCurHotspotTraffic() {
        float curTraffic = 0;
        if (null == mHotspotTrafficCalculator) {
            Log.d(TAG, "getCurHotspotTraffic mHotspotTrafficCalculator is null");
            return curTraffic;
        }
        curTraffic = mHotspotTrafficCalculator.getHotSpotTraffic();
        return curTraffic;
    }
    
    public float getDiffHotspotTraffic() {
        float diffTraffic = 0;
        if (null == mHotspotTrafficCalculator) {
            Log.d(TAG, "getCurHotspotTraffic mHotspotTrafficCalculator is null");
            return diffTraffic;
        }
        int simIndex = TrafficassistantUtil.getSimCardNo(mContext);
        float startTraffic = mHotSpotMonitorDataHelper.getHotSpotStartTraffic(mContext, simIndex);
        diffTraffic = mHotspotTrafficCalculator.getDiffHotSpotTraffic(startTraffic);
        return diffTraffic;
    }

    public void stopHotspotMonitor() {
        Log.d(TAG, "stopHotspotMonitor");
        if (null == mHotSportTrafficHandler) {
            Log.d(TAG, "stopHotspotMonitor mHotSportTrafficHandler is null");
            return;
        }
        if (mHotSportTrafficHandler.hasMessages(QUERY_HOTSPORT_TRAFFIC_MESSAGE)) {
            mHotSportTrafficHandler.removeMessages(QUERY_HOTSPORT_TRAFFIC_MESSAGE);
        }
        if (mHotSportTrafficHandler.hasMessages(QUERY_HOTSPORT_TRAFFIC_END_MESSAGE)) {
            mHotSportTrafficHandler.removeMessages(QUERY_HOTSPORT_TRAFFIC_END_MESSAGE);
        }
        mIsRunning = false;
    }
   
    private TrafficHotspotUsedRemindControler(Context context) {
        mContext = context.getApplicationContext();
        mHotSportTrafficHandler = new HotSportTrafficHandler();
        mHotSpotMonitoringStrategy = HotSpotMonitoringStrategySimpleFactory.createStrategy();
        mHotspotTrafficCalculator = new HotspotTrafficCalculator(context);
        mHotSpotMonitorDataHelper = new HotSpotMonitorDataHelper();
    }

    private boolean isShouldShowRemindDialog(float curTraffic) {
        boolean isShow = false;
        //float curTraffic = mHotspotTrafficCalculator.getHotSpotTraffic();
        int settingValue = mHotSpotMonitorDataHelper.getHotspotRemindSettingValue(mContext);
        boolean isReachLimit = mHotSpotMonitoringStrategy.isReachHotspotRemindLimit(settingValue, curTraffic);
        boolean isClosed = mHotSpotMonitorDataHelper.isClosedRemind(mContext);
        boolean isHotspotOpen = TrafficassistantUtil.isSoftApOpen(mContext);
        Log.d(TAG, "isShowRemindDialog curTraffic=" + curTraffic + " settingValue=" + settingValue + " isReachLimit=" + 
                isReachLimit + " isClosed=" + isClosed + " isHotspotOpen=" + isHotspotOpen);
        if(isReachLimit && !isClosed && isHotspotOpen) {
            isShow = true;
        }
        return isShow;
    }
    
    private void showRemidDialog() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(mContext.getApplicationContext(), TrafficHotSpotWarningDialog.class);
        mContext.startActivity(intent);
    }
    
    // mengdw <2017-04-22> add for 118238 begin
    private void queryHotspotTraffic() {
        Thread queryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                float curTraffic = mHotspotTrafficCalculator.getHotSpotTraffic();
                Message msg = new Message();
                msg.what = QUERY_HOTSPORT_TRAFFIC_END_MESSAGE;
                Bundle bundle = new Bundle();
                bundle.putFloat(KEY_HOTSPOT_TRAFFIC, curTraffic);
                msg.setData(bundle);
                mHotSportTrafficHandler.sendMessage(msg);
            }
        });
        queryThread.start();
    }
    // mengdw <2017-04-22> add for 118238 end
    
    private class HotSportTrafficHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // mengdw <2017-04-22> modify for 118238 begin
            Log.d(TAG, "handleMessage msg=" + msg.what);
            if (QUERY_HOTSPORT_TRAFFIC_MESSAGE == msg.what) {
                queryHotspotTraffic();
            } else if (QUERY_HOTSPORT_TRAFFIC_END_MESSAGE == msg.what) {
                Bundle bundle = msg.getData();
                if (null == bundle) {
                    return;
                }
                float hotspotTraffic = bundle.getFloat(KEY_HOTSPOT_TRAFFIC);
                Log.d(TAG, "handleMessage hotspotTraffic = " + hotspotTraffic);
                if (isShouldShowRemindDialog(hotspotTraffic)) {
                    showRemidDialog();
                } else {
                    mHotSportTrafficHandler.sendEmptyMessageDelayed(QUERY_HOTSPORT_TRAFFIC_MESSAGE,
                            DELAY_TIME);
                }
            }
            // mengdw <2017-04-22> modify for 118238 end
        }
    }
}
// mengdw <2016-10-09> add for CR01766193 end