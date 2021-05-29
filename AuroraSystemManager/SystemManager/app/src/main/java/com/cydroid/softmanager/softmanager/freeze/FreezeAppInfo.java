/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.freeze;

import com.cydroid.softmanager.model.ItemInfo;

public class FreezeAppInfo extends ItemInfo {
    private boolean mIsFreezed;
    private boolean mIsCautious;

    public void setIsFreezed(boolean isFreezed) {
        mIsFreezed = isFreezed;
    }

    public boolean isFreezed() {
        return mIsFreezed;
    }

    public void setIsCautious(boolean isCautious) {
        mIsCautious = isCautious;
    }

    public boolean isCautious() {
        return mIsCautious;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FreezeAppInfo{");
        sb.append("mPackageName:" + mPackageName);
        sb.append(",mIsSystemApp:" + mIsSystemApp);
        sb.append(",mIsFreezed:" + mIsFreezed);
        sb.append(",mIsCautious:" + mIsCautious);
        sb.append("}");
        return sb.toString();
    }
}