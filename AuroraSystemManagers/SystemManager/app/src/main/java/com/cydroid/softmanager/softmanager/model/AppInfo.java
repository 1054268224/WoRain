package com.cydroid.softmanager.softmanager.model;

import android.content.pm.ApplicationInfo;

import com.cydroid.softmanager.model.ItemInfo;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-22 Change List:
 */
public class AppInfo extends ItemInfo {

    public ApplicationInfo mApplicationInfo;
    private boolean mMoving = false;

    public AppInfo(ApplicationInfo info) {
        this.mApplicationInfo = info;
    }

    public void setMoving(boolean moving) {
        this.mMoving = moving;
    }

    public boolean getMoving() {
        return this.mMoving;
    }
}
