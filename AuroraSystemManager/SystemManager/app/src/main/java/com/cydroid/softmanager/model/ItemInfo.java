/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.model;

import android.graphics.drawable.Drawable;

public class ItemInfo {
    protected String mTitle;
    protected String mSummary;
    protected String mPackageName;
    protected Drawable mIcon = null;

    protected String mSize;
    protected long mFrequency;
    protected boolean mCheckStatus;
    protected boolean mIsInGreenWhiteList;

    protected String mAutoStartSummary;
    protected boolean mIsSystemApp;

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setPackageName(String packagename) {
        mPackageName = packagename;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setSummary(String summary) {
        if (summary == null) {
            summary = "";
        }
        mSummary = summary;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setIcon(Drawable drawable) {
        mIcon = drawable;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setCheckStatus(boolean status) {
        mCheckStatus = status;
    }

    public boolean getCheckStaus() {
        return mCheckStatus;
    }

    public void setSize(String size) {
        mSize = size;
    }

    public String getSize() {
        return mSize;
    }

    public void setAppFrequency(long frequency) {
        mFrequency = frequency;
    }

    public long getAppFrequency() {
        return mFrequency;
    }

    public void setGreenWhiteListItemState(boolean state) {
        mIsInGreenWhiteList = state;
    }

    public boolean getGreenWhiteListItemState() {
        return mIsInGreenWhiteList;
    }

    public void setSystemApp(boolean isSystem) {
        mIsSystemApp = isSystem;
    }

    public boolean isSystemApp() {
        return mIsSystemApp;
    }

    public void setAutoStartSummary(String autoSummary) {
        mAutoStartSummary = autoSummary;
    }

    public String getAutoStartSummary() {
        return mAutoStartSummary;
    }
}
