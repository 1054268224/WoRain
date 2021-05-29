// Gionee: mengdw <2016-05-03> modify for CR01684930 begin
package com.cydroid.softmanager.trafficassistant.model;

import android.content.Context;

import java.io.Serializable;

public class TrafficSettingUpdateUiData implements Serializable {
    private static final long serialVersionUID = 2L;
    private String mTodayFlow;
    private String mTotalFlow;
    private String mNotifiFlow;
    private int mNotificationType;
    private int mSimCount;
    private int mPowerMode;
    private int mActivateSindex;
    private boolean mIsChecked;
    private boolean mIsStartBuyActivity;
    
    public void setTodayFlow(String todayFlow) {
        mTodayFlow = todayFlow;
    }
    
    public String gettTodayFlow() {
        return mTodayFlow;
    }
    
    public void setTotalFlow(String totalFlow) {
        mTotalFlow = totalFlow;
    }
    
    public String getTotalFlow() {
        return mTotalFlow;
    }
    
    public void setNotifiFlow(String notifiFlow) {
        mNotifiFlow = notifiFlow;
    }
    
    public String getNotifiFlow() {
        return mNotifiFlow;
    }
    
    public void setNotificationType(int notificationType) {
        mNotificationType = notificationType;
    }
    
    public int getNotificationType() {
        return mNotificationType;
    }
    
    public void setSimCount(int simCount) {
        mSimCount = simCount;
    }
    
    public int getSimCount() {
        return mSimCount;
    }
    
    public void setPowerMode(int powerMode) {
        mPowerMode = powerMode;
    }
    
    public int getPowerMode() {
        return mPowerMode;
    }
    
    public void setActivateSindex(int activateSimdex) {
        mActivateSindex = activateSimdex;
    }
    
    public int getActivateSindex() {
        return mActivateSindex;
    }
    
    public void setIsChecked(boolean isCheck) {
        mIsChecked = isCheck;
    }
    
    public boolean ischecked() {
        return mIsChecked;
    }
    
    public void setIsStartBuyActivity(boolean isStartBuy) {
        mIsStartBuyActivity = isStartBuy;
    }
    
    public boolean isStartBuyActivity() {
        return mIsStartBuyActivity;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{")
                .append(" mTodayFlow=").append(mTodayFlow)
                .append(" mTotalFlow=").append(mTotalFlow)
                .append(" mNotifiFlow=").append(mNotifiFlow)
                .append(" mNotificationType=").append(mNotificationType)
                .append(" mSimCount=").append(mSimCount)
                .append(" mPowerMode=").append(mPowerMode)
                .append(" mIsChecked=").append(mIsChecked)
                .append(" mIsStartBuyActivity=").append(mIsStartBuyActivity)
                .append(" mActivateSindex=").append(mActivateSindex)
                .append(" }");
        return sb.toString();
    }
}
// Gionee: mengdw <2016-05-03> modify for CR01684930 end