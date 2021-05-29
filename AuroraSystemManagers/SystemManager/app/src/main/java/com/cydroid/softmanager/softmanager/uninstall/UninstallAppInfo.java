/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.uninstall;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;

public class UninstallAppInfo extends ItemInfo {
    private long mLastUpdateTime;
    private long mPackageSize;
    private long mUseFrequency;

    //fengpeipei modify for 64205 start 
    private Context mContext;

    public void init(Context context, ApplicationInfo info) {
        mContext = context;
        //fengpeipei modify for 64205 end
        setPackageName(info.packageName);
        setTitle(SoftHelperUtils.loadLabel(context, info));
        //guoxt modify for CSW1803A-1204 begin
        //setLastUpdateTime(UninstallAppUtils.getLastUpdateTime(context, mPackageName));
        //fengpeipei modify for 48899 start
        setPackageSize(UninstallAppUtils.invokeSSMGetPackageSize(context, mPackageName));
        //fengpeipei modify for 48899 end
        //setUseFrequency(UninstallAppUtils.getAppFrequent(context, mPackageName));
        //guoxt modify for CSW1803A-1204 end
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        mLastUpdateTime = lastUpdateTime;
    }

    public long getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public void setPackageSize(long packageSize) {
        mPackageSize = packageSize;
    }

    public long getPackageSize() {
        return mPackageSize;
    }

    public void setUseFrequency(long useFrequency) {
        mUseFrequency = useFrequency;
    }

    public long getUseFrequency() {
        //fengpeipei modify for 64205 start
        //return mUseFrequency;
        // Gionee xionghonggang 2017-03-01 modify for 73800 begin
        // return UninstallAppUtils.getAppFrequent(mContext, mPackageName);
        return UninstallAppUtils.getAppFrequentNew(mContext, mPackageName);
        // Gionee xionghonggang 2017-03-01 modify for 73800 end
        //fengpeipei modify for 64205 end
    }

    public String getSummary(Context context, int showType) {
        String summary = null;
        switch (showType) {
            case 1:
            case 2:
            case 3:
                summary = SoftHelperUtils.getStringDate(mLastUpdateTime);
                break;
            case 0:
            default:
                break;
        }
        return summary;
    }

    public void updateTitle(Context context, ApplicationInfo info) {
        setTitle(SoftHelperUtils.loadLabel(context, info));
    }

    public void updateUseFrequency(Context context) {
        setUseFrequency(UninstallAppUtils.getAppFrequent(context, mPackageName));
    }
}