/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 卡顿跳帧数据解析
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.skipframes;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class SkipFramesInfoCollector implements ISystemInfoCollector {

    private static final String TAG = "SkipFramesInfoCollector";
    private int mPid;
    private long mSkippedframes;
    private long mVsyncTimestamp;
    private long mDoframeTimestamp;
    private long mDelayTimeMillis;
    private final Context mContext;

    public SkipFramesInfoCollector(Context context) {
        mContext = context;
    }

    @Override
    public void record(SkipFramesInfo skipinfoData) {
        skipinfoData.skippedFramesNumber = (int) mSkippedframes;
        skipinfoData.skipFrameDoFrameTimestamp = mDoframeTimestamp / 1000000;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<String> pkgNames = HelperUtils.getPackagesNameByPid(mContext.getPackageManager(),
                am.getRunningAppProcesses(), mPid);
        if (!pkgNames.isEmpty()) {
            skipinfoData.packageName = pkgNames.get(0);
        } else {
            Log.d(TAG, "no pkgnames find for pid: " + mPid);
        }
    }

    public long getSkipFramesDuration() {
        return mDelayTimeMillis;
    }

    public int getPid() {
        return mPid;
    }

    public void parseData(byte[] buffer) {

        ByteArrayInputStream bintput = null;
        DataInputStream dintput = null;
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            bintput = new ByteArrayInputStream(buffer);
            dintput = new DataInputStream(bintput);
            mPid = dintput.readInt();
            mSkippedframes = dintput.readLong();
            mVsyncTimestamp = dintput.readLong();
            mDoframeTimestamp = dintput.readLong();
            mDelayTimeMillis = (System.nanoTime() - mVsyncTimestamp) / 1000000;// ms
            Log.d(TAG, "get message ------------->" + mPid + " " + mSkippedframes + " " + mVsyncTimestamp
                    + " " + mDoframeTimestamp);
        } catch (Exception e) {
            Log.d(TAG, "------->read error Exception:" + e.getClass().getName() + " " + e.getMessage());
            for (StackTraceElement tst : e.getStackTrace()) {
                Log.d(TAG, "------->read error Exception:" + tst.toString());
            }

        } finally {
            try {
                if (dintput != null)
                    dintput.close();
                if (bintput != null)
                    bintput.close();
            } catch (Exception e) {
                Log.d(TAG, "------->close error Exception:" + e.getClass().getName() + " " + e.getMessage());
            }
        }

    }

}
