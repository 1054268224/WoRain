/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 功耗卡顿数据采集主控
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.powersaver.analysis.collector.AnalysisDataCollectorFactory;
import com.cydroid.softmanager.powersaver.analysis.collector.IAnalysisDataCollector;
import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class AnalysisManager implements IAnalysisDataCollector.INewDataListener {
    private static final String TAG = "AnalysisManager";
    private static final int MSG_UPLOAD_DATA = 1;
    private static AnalysisManager sInstance;

    private static final String[] sDataCollectorNames = {"AppsBatteryData", "PowerLevelData",
            "SleepEventBatteryData", "BatteryChargeData", "SystemShutdownData", "SkipFramesData"};
    private static final long[] sDataCollectorUploadDelays = {1 * 3600 * 1000, 0, 1 * 3600 * 1000, 0, 0, 0};
    private static final String[] sDataCollectorUploadKeys = {"STAT_APP", "STAT_BATTERY_POWER", "STAT_SCREENOFF",
            "STAT_CHARGE_END", "STAT_POWEROFF", "STAT_SKIPFRAMES"};

    private boolean mFirstTime = true;
    private final Map<String, CollectorInfo> mDataCollectorInfos = new HashMap<String, CollectorInfo>();
    private Context mContext;
    private Handler mHandler;

    private class CollectorInfo {
        IAnalysisDataCollector collector;
        String name;
//        long minUploadDelay;
        long maxUploadDelay;
        String uploadKey;

        public CollectorInfo(IAnalysisDataCollector collector, String name, long minUploadDelay,
                long maxUploadDelay, String uploadKey) {
            this.collector = collector;
            this.name = name;
//            this.minUploadDelay = minUploadDelay;
            this.maxUploadDelay = maxUploadDelay;
            this.uploadKey = uploadKey;
        }
    }

    public static synchronized AnalysisManager getInstance() {
        if (null == sInstance) {
            sInstance = new AnalysisManager();
        }
        return sInstance;
    }

    public synchronized void init(Context context) {
        if (mFirstTime) {
            initFirstTime(context);
            return;
        }
    }

    private void initFirstTime(Context context) {
        mFirstTime = false;
        mContext = context;
        mHandler = new Handler(mContext.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPLOAD_DATA:
                        String collectorName = msg.obj.toString();
                        if (collectorName != null && !collectorName.isEmpty()) {
                            postDataSetToServer(collectorName);
                        }
                        break;
                    default:
                        Log.d(TAG, "unknow msg=" + msg.what);
                        break;
                }
            }

        };
        for (int i = 0; i < sDataCollectorNames.length; i++) {
            String collectorName = sDataCollectorNames[i];
            IAnalysisDataCollector collector = AnalysisDataCollectorFactory.createInstanceByName(mContext,
                    collectorName);
            if (collector == null) {
                Log.d(TAG, "can not create collectorName ,skip");
                continue;
            }
            CollectorInfo collectorInfo = new CollectorInfo(collector, collectorName, 0,
                    sDataCollectorUploadDelays[i], sDataCollectorUploadKeys[i]);
            mDataCollectorInfos.put(collectorName, collectorInfo);

            collector.init();
            collector.setNewDataListener(this);
        }
    }

    private void postDataSetToServer(final String collectorName) {
        Log.d(TAG, "postDataSetToServer for " + collectorName);
        if (!isUpdateConditionFulfilled()) {
            Log.d(TAG, "upload conditions not fulfilled ,cancel");
            return;
        }
        CollectorInfo collectorInfo = mDataCollectorInfos.get(collectorName);
        IAnalysisDataCollector collector = collectorInfo.collector;
        final List<Map<String, Object>> dataSet = collector.getAndFlushNewDataSet();
        if (dataSet == null || dataSet.isEmpty()) {
            Log.d(TAG, "no data to upload ,cancel");
            return;
        }
        final String uploadKey = collectorInfo.uploadKey;
        if (uploadKey == null || uploadKey.isEmpty()) {
            Log.d(TAG, "no upload key found ,cancel");
            return;
        }
        new Thread() {

            @Override
            public void run() {
                for (Map<String, Object> dataItem : dataSet) {
                    Log.i(TAG, " uploadAppData " + uploadKey + "(" + collectorName + ") --> "
                            + dataItem.toString());

                    SmartCleanInfoWriter.log(TAG, " uploadAppData " + uploadKey + "(" + collectorName + ")"
                            + (uploadKey.equals("STAT_APP") ? (" --> " + dataItem.toString()) : ""));

                    //YouJuManager.onEvent(mContext, uploadKey, null, dataItem);
                }
            }

        }.start();
    }

    private boolean isUpdateConditionFulfilled() {
        // Let's YouJu check upload condition, always return true here.
        return true;
    }

    public void deinit() {
        for (CollectorInfo collectorInfo : mDataCollectorInfos.values()) {
            collectorInfo.collector.deinit();
        }
        mDataCollectorInfos.clear();
    }

    @Override
    public void onNewData(IAnalysisDataCollector collector) {
        for (CollectorInfo collectorInfo : mDataCollectorInfos.values()) {
            if (collector == collectorInfo.collector) {
                long delayTime = Util.getRandomDelayTime(collectorInfo.maxUploadDelay);
                Log.d(TAG, "upload data from " + collectorInfo.name + " after " + delayTime);
                mHandler.removeMessages(MSG_UPLOAD_DATA, collectorInfo.name);
                Message msg = mHandler.obtainMessage(MSG_UPLOAD_DATA, collectorInfo.name);
                mHandler.sendMessageDelayed(msg, delayTime);
                break;
            }
        }
    }
}
