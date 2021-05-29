package com.cydroid.softmanager.monitor.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Method;
import java.util.Map;
import com.cydroid.softmanager.common.MainProcessSettingsProviderHelper;


//import com.antiy.security.SecurityManager;
import com.cydroid.softmanager.utils.Log;

//public class ActivityChangeMonitor implements SecurityManager.IActivityListener {
public class ActivityChangeMonitor {


    private static final String TAG = "ActivityChangeMonitor";

    private static final String PREF_NAME_ACTIVITY_LOST_FORCUS_STATE = "pref_activity_state";
    private static final String PREF_KEY_TOP_ACTIVITY = "top_activity";

    private boolean isListen = false;
//    private MainProcessSettingsProviderHelper mMainProcessSettingsProvider;
    private final SharedPreferences mPrefActivityLostForcus;

    public ActivityChangeMonitor(Context context) {
        // mPrefActivityLostForcus is READ ONLY for other class!!!!
//        mMainProcessSettingsProvider = new MainProcessSettingsProviderHelper(context);
//        mMainProcessSettingsProvider
//                .setPreferenceName(ActivityChangeMonitor.PREF_NAME_ACTIVITY_LOST_FORCUS_STATE);
        mPrefActivityLostForcus = context.getSharedPreferences(
                ActivityChangeMonitor.PREF_NAME_ACTIVITY_LOST_FORCUS_STATE, Context.MODE_PRIVATE);
    }

    public void listen() {
        Log.d(TAG, "ActivityChangeMonitor listen -----> ");
/*
        try {
            Class<?> clsSecMgr = (Class<?>) Class.forName("com.antiy.security.SecurityManager");
            Method methodSetListener = clsSecMgr.getMethod("setActivityListenerForCyee",
                    SecurityManager.IActivityListener.class);
            methodSetListener.invoke(null, this);
            isListen = true;
        } catch (Exception e) {
            Log.e(TAG, "exception = " + e);
        }
*/
    }


    public void remove() {
        Log.d(TAG, "ActivityChangeMonitor remove -----> ");
        if (!isListen) {
            return;
        }
        try {
            Class<?> clsSecMgr = (Class<?>) Class.forName("com.antiy.security.SecurityManager");
            Method methodUnsetListener = clsSecMgr.getMethod("unregisterActivityListenerForCyee");
            methodUnsetListener.invoke(null);
            isListen = false;
        } catch (Exception e) {
            Log.e(TAG, "exception = " + e);
        }
    }
/*
    @Override
    public void onStartActivity(String packageName, String activityName, boolean createProcess) {
        Log.d(TAG, "show me activity change pkg:" + packageName + " activityName:" + activityName
                + " createProcess:" + createProcess);
        String lastTopActivity = mPrefActivityLostForcus.getString(PREF_KEY_TOP_ACTIVITY, "");
        compareTopActivity(packageName, activityName, lastTopActivity);
        mPrefActivityLostForcus.edit().putString(PREF_KEY_TOP_ACTIVITY, activityName).commit();
    }
*/
    private void compareTopActivity(String newPackageName, String newActivityName, String lastTopActivity) {
        if (lastTopActivity == null || lastTopActivity.isEmpty()) {
            return;
        }
        long currentTimestamp = System.currentTimeMillis();
        String[] componentInfo = lastTopActivity.split("/");
        if (componentInfo.length < 2) {
            return;
        }
        if (!componentInfo[0].equals(newPackageName)) {
            Log.d(TAG, "record app to background =" + componentInfo[0] + "/" + componentInfo[1]
                    + " timestamp = " + currentTimestamp);
            mPrefActivityLostForcus.edit().putString(componentInfo[0], String.valueOf(currentTimestamp))
                    .commit();
        }
    }

    public static Map<String, String> getAppToBackgroundTimes(Context context) {

        MainProcessSettingsProviderHelper mainProcessSettingsProvider = new MainProcessSettingsProviderHelper(
                context);
        mainProcessSettingsProvider
                .setPreferenceName(ActivityChangeMonitor.PREF_NAME_ACTIVITY_LOST_FORCUS_STATE);
        SharedPreferences prefActivityLostForcus = context.getSharedPreferences(
                ActivityChangeMonitor.PREF_NAME_ACTIVITY_LOST_FORCUS_STATE, Context.MODE_PRIVATE);
        Map<String, String> res = (Map<String, String>) prefActivityLostForcus.getAll();
        res.remove(PREF_KEY_TOP_ACTIVITY);
        return res;
    }

}


