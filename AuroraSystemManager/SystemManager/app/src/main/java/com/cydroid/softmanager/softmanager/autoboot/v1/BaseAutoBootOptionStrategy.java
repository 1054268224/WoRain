/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager.softmanager.autoboot.v1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseAutoBootOptionStrategy implements AutoBootOptionStrategy {
    private static final String TAG = "BaseAutoBootOptionStrategy";
    private static final List<String> sWhiteList = new ArrayList<>();

    static {
        sWhiteList.add("com.cydroid.softmanager");
        sWhiteList.add("com.android.mms");
        sWhiteList.add("com.android.contacts");
        sWhiteList.add("com.android.settings");
        sWhiteList.add("com.android.deskclock");
        sWhiteList.add("com.cydroid.note");
    }

    private final int mType;
    private final Intent mIntent;
    private final Context mContext;

    public BaseAutoBootOptionStrategy(Context context, Intent intent, int type) {
        mContext = context;
        mIntent = intent;
        mType = type;
    }

    @Override
    public Map<String, AutoBootOptionItem> queryAutoBootOptions() {
        Map<String, AutoBootOptionItem> options = new HashMap<>();
        List<ResolveInfo> autoBootAppResolves = queryBroadcastReceivers();
        filter(autoBootAppResolves);
        return queryAutoBootOptions(options, autoBootAppResolves);
    }

    private List<ResolveInfo> queryBroadcastReceivers() {
        PackageManager pm = mContext.getPackageManager();
        return pm.queryBroadcastReceivers(mIntent,
                PackageManager.GET_INTENT_FILTERS | PackageManager.GET_DISABLED_COMPONENTS);
    }

    protected List<ResolveInfo> filter(List<ResolveInfo> autoBootAppResolves) {
        for (int i = 0; i < autoBootAppResolves.size(); ++i) {
            ResolveInfo ri = autoBootAppResolves.get(i);
            ActivityInfo ai = ri.activityInfo;
            if (null == ai
                    || (null != ai.applicationInfo && sWhiteList.contains(ai.applicationInfo.packageName))
                    || ri.system) {
                autoBootAppResolves.remove(i);
                --i;
            }
        }
        return autoBootAppResolves;
    }

    private Map<String, AutoBootOptionItem> queryAutoBootOptions(Map<String, AutoBootOptionItem> options,
                                                                 List<ResolveInfo> autoBootAppResolves) {
        for (ResolveInfo ri : autoBootAppResolves) {
            ActivityInfo ai = ri.activityInfo;
            AutoBootOptionItem item = options.get(ai.applicationInfo.packageName);
            ComponentName component = new ComponentName(ai.applicationInfo.packageName, ai.name);

            if (null == item) {
                item = new AutoBootOptionItem(ai.applicationInfo.packageName, mType);
                item.addAutoBootComponent(component);
                options.put(ai.applicationInfo.packageName, item);
            } else if (!item.containsAutoBootComponent(component)) {
                item.addAutoBootComponent(component);
            }
        }
        return options;
    }

    @Override
    public AutoBootOptionItem queryAutoBootOption(String packageName) {
        List<ResolveInfo> autoBootAppResolves = queryBroadcastReceivers();
        filter(autoBootAppResolves);
        filterPackageResolveInfo(autoBootAppResolves, packageName);
        return queryAutoBootOption(packageName, autoBootAppResolves);
    }

    protected List<ResolveInfo> filterPackageResolveInfo(List<ResolveInfo> autoBootAppResolves,
                                                         String packageName) {
        for (int i = 0; i < autoBootAppResolves.size(); ++i) {
            ResolveInfo ri = autoBootAppResolves.get(i);
            ActivityInfo ai = ri.activityInfo;
            if (null == ai
                    || null == ai.applicationInfo
                    || (null != ai.applicationInfo && !packageName.equals(ai.applicationInfo.packageName))) {
                autoBootAppResolves.remove(i);
                --i;
            }
        }
        return autoBootAppResolves;
    }

    private AutoBootOptionItem queryAutoBootOption(String packageName,
                                                   List<ResolveInfo> autoBootAppResolves) {
        if (autoBootAppResolves.isEmpty()) {
            return null;
        }

        AutoBootOptionItem item = new AutoBootOptionItem(packageName, mType);
        for (ResolveInfo ri : autoBootAppResolves) {
            ActivityInfo ai = ri.activityInfo;
            ComponentName component = new ComponentName(ai.applicationInfo.packageName, ai.name);

            if (!item.containsAutoBootComponent(component)) {
                item.addAutoBootComponent(component);
            }
        }
        return item;
    }
}