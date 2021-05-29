/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.applock;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.Log;

public class AppLockAppInfo extends ItemInfo {
    private boolean mLocked;
    private boolean mInstalled;

    public AppLockAppInfo(String packageName, boolean locked, boolean installed) {
        mPackageName = packageName;
        mLocked = locked;
        mInstalled = installed;
    }

    public boolean isLocked() {
        return mLocked;
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    public boolean isInstalled() {
        return mInstalled;
    }

    public void setInstalled(boolean installed) {
        mInstalled = installed;
    }

    public void updateTitle(Context context, ApplicationInfo info) {
        setTitle(SoftHelperUtils.loadLabel(context, info));
    }
}