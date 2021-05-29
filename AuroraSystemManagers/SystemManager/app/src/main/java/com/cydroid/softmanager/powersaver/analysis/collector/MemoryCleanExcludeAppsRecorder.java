package com.cydroid.softmanager.powersaver.analysis.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;

public class MemoryCleanExcludeAppsRecorder {
    private static final String TAG = "MemoryCleanExcludeAppsRecorder";
    public static final String POST_EVENT_KEY_STAT_NOCLEAN = "STAT_NOCLEAN";

    HashMap<String, String> recordMap = new HashMap<String, String>();

    private final ArrayList<String> mNotPostReason = new ArrayList<String>();

    private String getDelimiter() {
        return "|";
    }

    public void record(String key, String appendix) {
        if (!recordMap.containsKey(key)) {
            recordMap.put(key, appendix);
        } else {
            recordMap.put(key, recordMap.get(key) + getDelimiter() + appendix);
        }
    }

    public void erase(String key) {
        recordMap.remove(key);
    }

    public void post(Context context) {
        long timestamp = System.currentTimeMillis();
        for (Entry<String, String> entry : recordMap.entrySet()) {
            String pkgName = entry.getKey();
            String[] appendixStrs = entry.getValue().split("\\" + getDelimiter());
            if (appendixStrs == null) {
                continue;
            }
            if (appendixStrs.length < 1) {
                continue;
            }
            String pkgVersion = HelperUtils.getPackageVersion(context.getPackageManager(), pkgName);
            for (String reason : appendixStrs) {
                if (mNotPostReason.contains(reason)) {
                    continue;
                }
                HashMap<String, Object> noCleanMap = new HashMap<String, Object>();
                noCleanMap.put("timestamp", timestamp);
                noCleanMap.put("pkgName", pkgName);
                noCleanMap.put("pkgVersion", pkgVersion);
                noCleanMap.put("reason", reason);
//                postData(context, POST_EVENT_KEY_STAT_NOCLEAN, noCleanMap);
            }
        }
    }

//    private void postData(Context context, String key, Map data/*, long timestamp*/) {
//        String imei = HelperUtils.getImei(context);
//        // data.put("timestamp", timestamp);// milliseconds
//        Log.d(TAG, "SHOW DATA----->:" + data.toString());
//        YouJuManager.onEvent(context, key, null, data);
//    }
}
