/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.oneclean.whitelist;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;

public class WhiteAppInfo extends ItemInfo {
    private boolean mInUserWhiteList;

    public void setInUserWhiteList(boolean inUserWhiteList) {
        mInUserWhiteList = inUserWhiteList;
    }

    public boolean isInUserWhiteList() {
        return mInUserWhiteList;
    }

    public void updateTitle(Context context, ApplicationInfo info) {
        setTitle(SoftHelperUtils.loadLabel(context, info));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WhiteAppInfo");
        sb.append(" mPackageName=" + mPackageName);
        sb.append(" mInUserWhiteList=" + mInUserWhiteList);
        return sb.toString();
    }
}