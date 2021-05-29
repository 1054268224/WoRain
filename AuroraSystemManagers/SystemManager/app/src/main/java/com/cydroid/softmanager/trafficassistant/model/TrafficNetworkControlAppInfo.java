/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: mengdw
 *
 * Date: 2016-12-13
 * 
 * for CR01776232
 */
package com.cydroid.softmanager.trafficassistant.model;

import android.graphics.drawable.Drawable;

import com.cydroid.softmanager.trafficassistant.utils.Constant;

public class TrafficNetworkControlAppInfo {
    private int mAppUid = 0;
    private String mAppPkgName = "";
    private String mAppName = "";
    private int mAppStatus = Constant.NETWORK_CONTROL_ENABLE_STATUS;
    private Drawable mIcon;
    
    public void copyAppInfo(TrafficNetworkControlAppInfo appInfo) {
        if (null == appInfo) {
            return;
        }
        mAppUid = appInfo.getAppUid();
        mAppPkgName = appInfo.getAppPkgName();
        mAppName = appInfo.getAppName();
        mAppStatus = appInfo.getAppStatus();
        mIcon = appInfo.getIcon();
    }
    
    public void setAppUid(int uid) {
        mAppUid = uid;
    }
    
    public int getAppUid() {
        return mAppUid;
    }
    
    public void setAppStatus(int status) {
        mAppStatus = status;
    }
    
    public int getAppStatus() {
        return mAppStatus;
    }
    
    public void setAppPkgName(String appPkgName) {
        mAppPkgName = appPkgName;
    }
    
    public String getAppPkgName() {
        return mAppPkgName;
    }
    
    public void setAppName(String appName) {
        mAppName = appName;
    }
    
    public String getAppName() {
        return mAppName;
    }

    public void setIcon(Drawable drawable) {
        mIcon = drawable;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" mAppUid=" + mAppUid);
        sb.append(" mAppName=" + mAppName);
        sb.append(" mAppStatus=" + mAppStatus);
        sb.append(" mAppPkgName=" + mAppPkgName);
        return sb.toString();
    }
}