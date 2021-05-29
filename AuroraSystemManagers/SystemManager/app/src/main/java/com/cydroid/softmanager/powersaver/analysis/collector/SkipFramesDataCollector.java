/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 卡顿事件采集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cydroid.softmanager.skipframes.ComponentInfoCollector;
import com.cydroid.softmanager.skipframes.CpuInfoCollector;
import com.cydroid.softmanager.skipframes.ISystemInfoCollector;
import com.cydroid.softmanager.skipframes.SkipFramesInfo;
import com.cydroid.softmanager.skipframes.SkipFramesInfoCollector;
import com.cydroid.softmanager.skipframes.SkipFramesService;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SkipFramesDataCollector implements IAnalysisDataCollector {
    private static final String TAG = "SkipFramesDataCollector";
    private final Context mContext;
    private final ArrayList<Map<String, Object>> mSkipframesEvents;
    private INewDataListener mDataListener;
    private SkipframesReceiver mSkipframesReceiver;

    public SkipFramesDataCollector(Context context) {
        mContext = context;
        mSkipframesEvents = new ArrayList<Map<String, Object>>();
    }

    @Override
    public void init() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mSkipframesReceiver = new SkipframesReceiver(localBroadcastManager);
    }

    @Override
    public void deinit() {
        if (mSkipframesReceiver != null) {
            mSkipframesReceiver.remove();
        }
        if (mDataListener != null) {
            mDataListener = null;
        }
    }

    @Override
    public void setNewDataListener(INewDataListener newDataListener) {
        mDataListener = newDataListener;
    }

    @Override
    public synchronized List<Map<String, Object>> getAndFlushNewDataSet() {
        ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (!mSkipframesEvents.isEmpty()) {
            result.addAll(mSkipframesEvents);
        }
        mSkipframesEvents.clear();
        return result;
    }

    public class SkipframesReceiver extends BroadcastReceiver {
        private final LocalBroadcastManager mLocalBroadcastManager;

        public SkipframesReceiver(LocalBroadcastManager localBroadcastManager) {
            mLocalBroadcastManager = localBroadcastManager;
            IntentFilter eventFilter = new IntentFilter();
            eventFilter.addAction(SkipFramesService.ACTION_LOCAL_SKIPFRAMES_EVENT);
            mLocalBroadcastManager.registerReceiver(this, eventFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (actionStr == null || actionStr.isEmpty()) {
                return;
            }
            if (actionStr.equals(SkipFramesService.ACTION_LOCAL_SKIPFRAMES_EVENT)) {
                byte[] buffer = intent.getByteArrayExtra("stats");
                if (buffer == null) {
                    Log.d(TAG, "empty skipframes data");
                    return;
                }
                onSkipFrames(buffer);
            }
        }

        private void onSkipFrames(byte[] buffer) {
            final SkipFramesInfo skipinfoData = new SkipFramesInfo();
            final ArrayList<ISystemInfoCollector> systemInfoCollectorsList = new ArrayList<ISystemInfoCollector>();
            SkipFramesInfoCollector skipFramesInfoCollector = new SkipFramesInfoCollector(mContext);
            skipFramesInfoCollector.parseData(buffer);
            systemInfoCollectorsList.add(skipFramesInfoCollector);
            long duration = skipFramesInfoCollector.getSkipFramesDuration();
            int pid = skipFramesInfoCollector.getPid();
            systemInfoCollectorsList.add(new CpuInfoCollector(pid));
            systemInfoCollectorsList.add(new ComponentInfoCollector(mContext, pid, duration));
            new Thread() {
                @Override
                public void run() {
                    for (ISystemInfoCollector collector : systemInfoCollectorsList) {
                        collector.record(skipinfoData);
                    }
                    if (!isVaildSkipframesData(skipinfoData)) {
                        Log.d(TAG, "invalid skipframe data ,skip");
                        return;
                    }
                    HashMap<String, Object> eventMap = getEventMap(skipinfoData);
                    int resultSize = addToDataSet(eventMap);
                    Log.i(TAG, "resultDataSet.put :" + eventMap + ", currentRecordData.size " + resultSize);
                    if (mDataListener != null) {
                        mDataListener.onNewData(SkipFramesDataCollector.this);
                    }
                }
            }.start();
        }

        private boolean isVaildSkipframesData(SkipFramesInfo skipinfoData) {
            return skipinfoData.packageName != null && !skipinfoData.packageName.isEmpty();
        }

        protected HashMap<String, Object> getEventMap(SkipFramesInfo skipinfoData) {
            HashMap<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("timestamp", skipinfoData.skipFrameDoFrameTimestamp);// ms
            resultMap.put("pkgname", skipinfoData.packageName);
            resultMap.put("pkgversion",
                    HelperUtils.getPackageVersion(mContext.getPackageManager(), skipinfoData.packageName));
            resultMap.put("skippedFrames", skipinfoData.skippedFramesNumber);
            if (skipinfoData.componentInfo != null) {
                resultMap.put("appTopActivity", skipinfoData.componentInfo.currentTopActivity);
                resultMap.put("appPreviousActivity", skipinfoData.componentInfo.lastTopActivity);
            }
            resultMap.put("deviceModel", HelperUtils.getModel());
            resultMap.put("deviceRomVersion", HelperUtils.getRomVersion());
            resultMap.put("devicePlatfrom", HelperUtils.getPlatform());
            if (skipinfoData.cpuInfo != null) {
                resultMap.put("cpuAvgLoad", skipinfoData.cpuInfo.cpuAverageLoad);
                resultMap.put("cpuTemperature", skipinfoData.cpuInfo.cpuTemperature);
                resultMap.put("cpuFreq", skipinfoData.cpuInfo.cpuFreq);
                resultMap.put("cpuOnlines", skipinfoData.cpuInfo.cpuOnlineNumber);
            }
            return resultMap;
        }

        public void remove() {
            mLocalBroadcastManager.unregisterReceiver(this);
        }
    }

    public synchronized int addToDataSet(HashMap<String, Object> event) {
        mSkipframesEvents.add(event);
        return mSkipframesEvents.size();
    }

}
