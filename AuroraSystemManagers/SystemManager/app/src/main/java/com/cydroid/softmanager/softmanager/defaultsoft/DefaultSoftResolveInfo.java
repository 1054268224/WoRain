package com.cydroid.softmanager.softmanager.defaultsoft;

import android.content.Context;
import android.content.pm.ResolveInfo;

import com.cydroid.softmanager.model.ItemInfo;

public class DefaultSoftResolveInfo extends ItemInfo {
    private ResolveInfo mResolveInfo;
    private boolean mIsBestMatched = false;
    private String mUnique;

    public DefaultSoftResolveInfo() {
    }

    public DefaultSoftResolveInfo(ResolveInfo info, Context context) {
        mResolveInfo = info;
        mTitle = loadLabel(context, info);
    }

    public boolean isBestMatched() {
        return mIsBestMatched;
    }

    public void setBestMatched(boolean bestMatch) {
        mIsBestMatched = bestMatch;
    }

    private String loadLabel(Context context, ResolveInfo info) {
        CharSequence result;
        if (info.activityInfo != null) {
            // Chenyee xionghg 20171214 modify for SW17W16A-2504 begin
            // 当组件的intent-filter中含有label时，使用activityInfo.loadLabel获取不到
            // result = info.activityInfo.loadLabel(context.getPackageManager());
            result = info.loadLabel(context.getPackageManager());
            // Chenyee xionghg 20171214 modify for SW17W16A-2504 end
            if (result == null) {
                return info.activityInfo.packageName;
            }
            return result.toString();
        } else {
            return "";
        }
    }

    public ResolveInfo getResolveInfo() {
        return mResolveInfo;
    }

    public void setUnique(String str) {
        mUnique = str;
    }

    public String getUnique() {
        return mUnique;
    }
}
