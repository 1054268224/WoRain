//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant.net;

import android.graphics.drawable.Drawable;

public class AppInfo {

    private int mAppUid = 0;
    private String mAppName = "";
    private String mAppPackageName = "";
    private Drawable mAppIcon = null;
    private int mAppMobileStatus = 0;
    private int mAppWifiStatus = 0;
    // Gionee: mengdw <2016-07-18> add for CR01639347 begin
    private boolean mWifiIsChanged = false;
    private boolean mMobileIsChanged = false;
    // Gionee: mengdw <2016-07-18> add for CR01639347 end

    public void setAppUid(int uid) {
        this.mAppUid = uid;
    }

    public void setAppName(String name) {
        this.mAppName = name;
    }

    public void setAppPackageName(String name) {
        this.mAppPackageName = name;
    }

    public void setAppIcon(Drawable icon) {
        this.mAppIcon = icon;
    }

    public void setAppMobileStatus(int status) {
        this.mAppMobileStatus = status;
    }

    public void setAppWifiStatus(int status){
    	this.mAppWifiStatus = status;
    }
    
    public int getAppUid() {
        return this.mAppUid;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public String getAppPackageName() {
        return this.mAppPackageName;
    }

    public Drawable getAppIcon() {
        return this.mAppIcon;
    }

    public int getAppMobileStatus() {
        return this.mAppMobileStatus;
    }
    
    public int getAppWifiStatus(){
    	return this.mAppWifiStatus;
    }
    
    // Gionee: mengdw <2016-07-18> add for CR01639347 begin
    public void setWifiIsChanged(boolean isChanged) {
        mWifiIsChanged = isChanged;
    }

    public boolean isWifiChanged() {
        return mWifiIsChanged;
    }

    public void setMobilesChanged(boolean isChanged) {
        mMobileIsChanged = isChanged;
    }

    public boolean isMobileChanged() {
        return mMobileIsChanged;
    }
    // Gionee: mengdw <2016-07-18> add for CR01639347 end
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end