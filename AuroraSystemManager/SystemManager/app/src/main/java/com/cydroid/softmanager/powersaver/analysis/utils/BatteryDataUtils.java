/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 应用功耗数据采集辅助类
 *
 * Revised Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cydroid.softmanager.powersaver.analysis.AppBatteryInfo;
import com.cydroid.softmanager.utils.Log;

public class BatteryDataUtils {
    public static final String TAG = "BatteryDataUtils";

    public static Map<String, AppBatteryInfo> subtractionAppBatteryInfoMap(Map<String, AppBatteryInfo> srcMap,
            Map<String, AppBatteryInfo> diffMap) {
        Log.i(TAG, "subtractionAllBattery   srcMap.size --> " + srcMap.size() + ", diffMap.size --> "
                + diffMap.size());
        Map<String, AppBatteryInfo> resultMap = new ConcurrentHashMap<String, AppBatteryInfo>();
        Set<String> allPkgSet = new HashSet<String>();
        allPkgSet.addAll(srcMap.keySet());
        allPkgSet.addAll(diffMap.keySet());
        for (String pkg : allPkgSet) {
            AppBatteryInfo sumInfo = subtractionAppBatteryInfo(srcMap.get(pkg), diffMap.get(pkg));
            Log.i(TAG, "subtractionBattery -- > " + pkg + ", result --> " + sumInfo.toString());
            resultMap.put(pkg, sumInfo);
        }
        Log.i(TAG, "subtractionAllBattery   resultMap.size --> " + resultMap.size());
        return resultMap;
    }

    private static AppBatteryInfo subtractionAppBatteryInfo(AppBatteryInfo srcInfo, AppBatteryInfo diffInfo) {
        AppBatteryInfo sumInfo = new AppBatteryInfo();
        if (srcInfo == null) {
            srcInfo = new AppBatteryInfo();
        }
        if (diffInfo == null) {
            diffInfo = new AppBatteryInfo();
        }
        sumInfo.userId = srcInfo.userId;
        sumInfo.powerValue = srcInfo.powerValue - diffInfo.powerValue;
        sumInfo.usageTime = srcInfo.usageTime - diffInfo.usageTime;
        sumInfo.cpuTime = srcInfo.cpuTime - diffInfo.cpuTime;
        sumInfo.gpsTime = srcInfo.gpsTime - diffInfo.gpsTime;
        sumInfo.wifiRunningTime = srcInfo.wifiRunningTime - diffInfo.wifiRunningTime;
        sumInfo.cpuFgTime = srcInfo.cpuFgTime - diffInfo.cpuFgTime;
        sumInfo.wakeLockTime = srcInfo.wakeLockTime - diffInfo.wakeLockTime;
        sumInfo.mobileActive = srcInfo.mobileActive - diffInfo.mobileActive;
        sumInfo.mobileActiveCount = srcInfo.mobileActiveCount - diffInfo.mobileActiveCount;
        sumInfo.mobilemspp = srcInfo.mobilemspp - diffInfo.mobilemspp; // milliseconds per packet
        sumInfo.wifiTxPackets = srcInfo.wifiRxBytes - diffInfo.wifiRxBytes;
        sumInfo.mobileRxBytes = srcInfo.mobileRxBytes - diffInfo.mobileRxBytes;
        sumInfo.mobileTxBytes = srcInfo.mobileTxBytes - diffInfo.mobileTxBytes;
        sumInfo.wifiRxBytes = srcInfo.wifiRxBytes - diffInfo.wifiRxBytes;
        sumInfo.wifiTxBytes = srcInfo.wifiTxBytes - diffInfo.wifiTxBytes;
        sumInfo.percent = srcInfo.percent - diffInfo.percent;
        sumInfo.packageWithHighestDrain = srcInfo.packageWithHighestDrain;
        sumInfo.audioTurnedOnTime = srcInfo.audioTurnedOnTime - diffInfo.audioTurnedOnTime;
        sumInfo.vedioTurnedOnTime = srcInfo.vedioTurnedOnTime - diffInfo.vedioTurnedOnTime;
        sumInfo.vibratorTurnedOnTime = srcInfo.vibratorTurnedOnTime - diffInfo.vibratorTurnedOnTime;
        return sumInfo;
    }

    public static Map<String, AppBatteryInfo> addAppBatteryInfoMap(Map<String, AppBatteryInfo> srcMap,
            Map<String, AppBatteryInfo> addMap) {
        Log.i(TAG, "addAllBatteryInfo   srcMap.size --> " + srcMap.size() + ", addMap.size --> "
                + addMap.size());
        Map<String, AppBatteryInfo> resultMap = new ConcurrentHashMap<String, AppBatteryInfo>();
        Set<String> allPkgSet = new HashSet<String>();
        allPkgSet.addAll(srcMap.keySet());
        allPkgSet.addAll(addMap.keySet());
        for (String pkg : allPkgSet) {
            AppBatteryInfo sumInfo = addAppBatteryInfo(srcMap.get(pkg), addMap.get(pkg));
            Log.i(TAG, "addBattery -- > " + pkg + ", result --> " + sumInfo.toString());
            resultMap.put(pkg, sumInfo);
        }
        Log.i(TAG, "addAllBatteryInfo   resultMap.size --> " + resultMap.size());
        return resultMap;
    }

    private static AppBatteryInfo addAppBatteryInfo(AppBatteryInfo srcInfo, AppBatteryInfo addInfo) {
        AppBatteryInfo sumInfo = new AppBatteryInfo();
        if (srcInfo == null) {
            srcInfo = new AppBatteryInfo();
        }
        if (addInfo == null) {
            addInfo = new AppBatteryInfo();
        }
        sumInfo.userId = srcInfo.userId;
        sumInfo.powerValue = srcInfo.powerValue + addInfo.powerValue;
        sumInfo.usageTime = srcInfo.usageTime + addInfo.usageTime;
        sumInfo.cpuTime = srcInfo.cpuTime + addInfo.cpuTime;
        sumInfo.gpsTime = srcInfo.gpsTime + addInfo.gpsTime;
        sumInfo.wifiRunningTime = srcInfo.wifiRunningTime + addInfo.wifiRunningTime;
        sumInfo.cpuFgTime = srcInfo.cpuFgTime + addInfo.cpuFgTime;
        sumInfo.wakeLockTime = srcInfo.wakeLockTime + addInfo.wakeLockTime;
        sumInfo.mobileActive = srcInfo.mobileActive + addInfo.mobileActive;
        sumInfo.mobileActiveCount = srcInfo.mobileActiveCount + addInfo.mobileActiveCount;
        sumInfo.mobilemspp = srcInfo.mobilemspp + addInfo.mobilemspp; // milliseconds per packet
        sumInfo.wifiTxPackets = srcInfo.wifiRxBytes + addInfo.wifiRxBytes;
        sumInfo.mobileRxBytes = srcInfo.mobileRxBytes + addInfo.mobileRxBytes;
        sumInfo.mobileTxBytes = srcInfo.mobileTxBytes + addInfo.mobileTxBytes;
        sumInfo.wifiRxBytes = srcInfo.wifiRxBytes + addInfo.wifiRxBytes;
        sumInfo.wifiTxBytes = srcInfo.wifiTxBytes + addInfo.wifiTxBytes;
        sumInfo.percent = srcInfo.percent + addInfo.percent;
        sumInfo.packageWithHighestDrain = srcInfo.packageWithHighestDrain;
        sumInfo.audioTurnedOnTime = srcInfo.audioTurnedOnTime + addInfo.audioTurnedOnTime;
        sumInfo.vedioTurnedOnTime = srcInfo.vedioTurnedOnTime + addInfo.vedioTurnedOnTime;
        sumInfo.vibratorTurnedOnTime = srcInfo.vibratorTurnedOnTime + addInfo.vibratorTurnedOnTime;

        return sumInfo;
    }

    @SuppressWarnings("unchecked")
    public static Object deepClone(Object src) {
        Object returnObj = src;
        if (src == null)
            return src;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(src);
            bais = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);
            returnObj = (Map<String, Object>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ex) {

                }
            }

            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ex) {

                }
            }

            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException ex) {

                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ex) {

                }
            }
        }
        return returnObj;
    }

    public static String mapArrayInfoToJsonString(List<Map<String, String>> mapArrayData) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < mapArrayData.size(); i++) {
            Map<String, String> itemMap = mapArrayData.get(i);
            Iterator<Entry<String, String>> iterator = itemMap.entrySet().iterator();

            JSONObject object = new JSONObject();

            while (iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();
                try {
                    object.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {
                    Log.d(TAG, "to json string failed key=" + entry.getKey() + " value=" + entry.getValue());
                    return "";
                }
            }
            jsonArray.put(object);
        }
        return jsonArray.toString();
    }

    public static List<String> jsonStringToArrayInfo(String jsonString) throws JSONException {
        ArrayList<String> res = new ArrayList<String>();
        JSONArray jsonArray = new JSONArray(jsonString);
        if (jsonArray == null || jsonArray.length() == 0) {
            Log.d(TAG, "no array found in str:" + jsonString);
            return res;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            res.add(jsonArray.getString(i));
        }
        return res;
    }
}
