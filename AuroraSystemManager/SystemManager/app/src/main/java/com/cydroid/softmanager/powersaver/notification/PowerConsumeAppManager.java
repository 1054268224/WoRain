/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用及忽略信息管理
 *
 * Date: 2017-02-10
 */
package com.cydroid.softmanager.powersaver.notification;

import android.content.Context;
import com.cydroid.softmanager.common.Consts;
import  com.cydroid.softmanager.utils.Log;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PowerConsumeAppManager {
    private static final String TAG = "PowerConsumeAppManager";

    private static final String PREF_NAME_USER_IGNORE_LIST = "powersaver_notification_user_ignored_list";
    private static final String PREFIX_PREF_NAME_POWER_CONSUME_APPS = "powersaver_power_consume_";
    private static PowerConsumeAppManager sInstance;
    private final Context mContext;
    private List<String> mInternalIgnoredAppsList;
    private HashMap<String, HashMap<String, Double>> mPowerConsumeAppMap;
    private MainProcessSettingsProviderHelper mMainProcessSettingsProviderHelper;

    public static synchronized PowerConsumeAppManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new PowerConsumeAppManager(context);
        }
        return sInstance;
    }

    private PowerConsumeAppManager(Context context) {
        mContext = context.getApplicationContext();
        init();
    }

    private void init() {
        mMainProcessSettingsProviderHelper = new MainProcessSettingsProviderHelper(mContext);
        initInternalIgnoredAppsList();
    }

    private void initInternalIgnoredAppsList() {
        //guoxt modify for CSW1703CX-1220 begin
        if(Consts.cyCXFlag) {
            mInternalIgnoredAppsList = Arrays.asList(
                    mContext.getResources().getStringArray(R.array.powersaver_notification_internal_whitelist_xiaolajiao));
        }else{
            mInternalIgnoredAppsList = Arrays.asList(
                    mContext.getResources().getStringArray(R.array.powersaver_notification_internal_whitelist));
        }
        //guoxt modify for CSW1703CX-1220 end
    }

    private HashMap<String, Double> getPowerConsumeAppMap(String monitorName) {
        mMainProcessSettingsProviderHelper
                .setPreferenceName(PREFIX_PREF_NAME_POWER_CONSUME_APPS + monitorName);
        HashMap<String, Double> res = new HashMap<String, Double>();
        for (Entry<String, String> entry : mMainProcessSettingsProviderHelper.getAll().entrySet()) {
            res.put(entry.getKey(), Double.parseDouble(entry.getValue()));
        }
        return res;
    }

    public ArrayList<String> getIgnoredApps() {
        ArrayList<String> res = new ArrayList<String>();
        res.addAll(mInternalIgnoredAppsList);
        mMainProcessSettingsProviderHelper.setPreferenceName(PREF_NAME_USER_IGNORE_LIST);
        Map<String, String> userIgnoredAppsMap = mMainProcessSettingsProviderHelper.getAll();
        for (Entry<String, String> entry : userIgnoredAppsMap.entrySet()) {
            if (Integer.parseInt(entry.getValue()) > 0) {
                res.add(entry.getKey());
            }
        }
        Log.d(TAG, "getIgnoredApps--->" + res);
        return res;
    }

    public boolean isIgnoredApp(String pkgName) {
        if (mInternalIgnoredAppsList.contains(pkgName)) {
            return true;
        }
        mMainProcessSettingsProviderHelper.setPreferenceName(PREF_NAME_USER_IGNORE_LIST);
        return Integer.parseInt(mMainProcessSettingsProviderHelper.getString(pkgName, "0")) > 0;
    }

    public void putPowerConsumeAppData(String monitorName, HashMap<String, Double> overThresholdResult) {
        Log.d(TAG, "update power consume app info type=" + monitorName + " size=" + overThresholdResult.size()
                + " to perf " + (PREFIX_PREF_NAME_POWER_CONSUME_APPS + monitorName));
        mMainProcessSettingsProviderHelper
                .setPreferenceName(PREFIX_PREF_NAME_POWER_CONSUME_APPS + monitorName);
        mMainProcessSettingsProviderHelper.removeAll();
        for (Entry<String, Double> entry : overThresholdResult.entrySet()) {
            mMainProcessSettingsProviderHelper.putString(entry.getKey(), entry.getValue().toString());
        }
    }

    public Map<String, String> getPowerConsumeAppData(String monitorName) {
        mMainProcessSettingsProviderHelper
                .setPreferenceName(PREFIX_PREF_NAME_POWER_CONSUME_APPS + monitorName);
        return mMainProcessSettingsProviderHelper.getAll();
    }

    public void addUserIgnoredApp(String packageName) {
        mMainProcessSettingsProviderHelper.setPreferenceName(PREF_NAME_USER_IGNORE_LIST);
        mMainProcessSettingsProviderHelper.putString(packageName, "1");
    }

    public void removeUserIgnoredApp(String packageName) {
        mMainProcessSettingsProviderHelper.setPreferenceName(PREF_NAME_USER_IGNORE_LIST);
        mMainProcessSettingsProviderHelper.putString(packageName, "0");
    }

}
