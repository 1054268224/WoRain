// Gionee <houjie> <2015-08-19> add for CR01559020 begin
// Gionee <houjie> <2016-1-6> modify for CR01618420 begin
package com.cydroid.softmanager.monitor.utils;

import static android.os.Process.*;

import android.os.Process;

import com.cydroid.softmanager.utils.Log;

public class ProcessCpuTracker {
    private static final String TAG = "ProcessCpuTracker";
    private static final boolean DEBUG = false;

    private static final int[] LOAD_AVERAGE_FORMAT = new int[] {PROC_SPACE_TERM, // 0: 1 min
            PROC_SPACE_TERM, // 1: 5 mins
            PROC_SPACE_TERM, // 2: 15 mins
            PROC_SPACE_TERM | PROC_OUT_STRING};

    private final float[] mLoadAverageData = new float[3];
    private final String[] mLoadAverageStringData = new String[2];

    private float mLoad1 = 0;
    private float mLoad5 = 0;
    private float mLoad15 = 0;

    public void onLoadChanged(float load1, float load5, float load15, String loadAverageStringData) {
    }

    public int onMeasureProcessName(String name) {
        return 0;
    }

    public void init() {
        if (DEBUG)
            Log.v(TAG, "Init: " + this);
        //mFirst = true;
        update();
    }

    public void update() {
        if (DEBUG)
            Log.v(TAG, "Update: " + this);

        final float[] loadAverages = mLoadAverageData;
        final String[] loadAverageStringData = mLoadAverageStringData;
        if (Process.readProcFile("/proc/loadavg", LOAD_AVERAGE_FORMAT, loadAverageStringData, null,
                loadAverages)) {
            float load1 = loadAverages[0];
            float load5 = loadAverages[1];
            float load15 = loadAverages[2];
            String loadAverageString = loadAverageStringData[0];
            if (load1 != mLoad1 || load5 != mLoad5 || load15 != mLoad15) {
                mLoad1 = load1;
                mLoad5 = load5;
                mLoad15 = load15;
            }
            onLoadChanged(load1, load5, load15, loadAverageString);
        }
    }
}
//Gionee <houjie> <2016-1-6> modify for CR01618420 end
// Gionee <houjie> <2015-08-19> add for CR01559020 end