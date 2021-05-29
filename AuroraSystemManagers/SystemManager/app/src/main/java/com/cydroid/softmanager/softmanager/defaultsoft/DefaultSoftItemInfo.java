/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.defaultsoft;

import com.cydroid.softmanager.model.ItemInfo;

public class DefaultSoftItemInfo extends ItemInfo {
    private int mTitleResId;
    private int mIconResId;
    private boolean mHasDefaultSoftApp;

    public int getIconResId() {
        return mIconResId;
    }

    public void setIconResId(int iconResId) {
        mIconResId = iconResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public void setTitleResId(int titleResId) {
        mTitleResId = titleResId;
    }

    public boolean hasDefaultSoftApp() {
        return mHasDefaultSoftApp;
    }

    public void setHasDefaultSoftApp(boolean hasDefaultSoftApp) {
        mHasDefaultSoftApp = hasDefaultSoftApp;
    }
}