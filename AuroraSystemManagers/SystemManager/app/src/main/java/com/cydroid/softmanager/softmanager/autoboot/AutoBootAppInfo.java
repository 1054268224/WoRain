/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;

public class AutoBootAppInfo extends ItemInfo {

    private static final int AUTO_BOOT_TYPE_SYS_BOOT   = 1;
    private static final int AUTO_BOOT_TYPE_BACKGROUND = 2;

    private boolean mAutoBootEnabled;
    private int mAutoBootType;

    public boolean isAutoBoot() {
        return mAutoBootEnabled;
    }

    public void setAutoBootState(boolean autoBootEnabled) {
        mAutoBootEnabled = autoBootEnabled;
    }

    public void setAutoBootType(int type) {
        mAutoBootType |= type;
    }

    public String getSummary(Context context) {
        switch (mAutoBootType){
            case AUTO_BOOT_TYPE_SYS_BOOT:
                return context.getResources().getString(R.string.auto_start_power);
            case AUTO_BOOT_TYPE_BACKGROUND:
                return context.getResources().getString(R.string.auto_start_back);
            default:
                return context.getResources().getString(R.string.auto_start_both);
        }
    }

    public void updateTitle(Context context, ApplicationInfo info) {
        setTitle(SoftHelperUtils.loadLabel(context, info));
    }

    public String getDataGhostSummary() {
        return this.mPackageName + "," + (mAutoBootEnabled ? "1" : "0");
    }
}