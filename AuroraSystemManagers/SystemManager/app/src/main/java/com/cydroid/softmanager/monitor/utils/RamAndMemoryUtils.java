package com.cydroid.softmanager.monitor.utils;

import com.android.internal.util.MemInfoReader;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class RamAndMemoryUtils {
    private static final String TAG = "RamAndMemoryUtils";

    /**
     * To get available memory size
     * 
     * @return size of avail memory
     */
    public static long getAvailMem() {
        String resStr = HelperUtils.executeShellCmd("cat /proc/meminfo|grep MemAvailable:");
        String[] memStrs = resStr.split("\\s+");
        long res = 0;
        try {
            res = Long.parseLong(memStrs[1]) / 1024;
            return res;
        } catch (Exception e) {
            Log.d(TAG, "getAvailMem failed!");
            return 0;
        }
    }
    /*chenyee guoxt 2018-0102 modify for SW17W16A-2535  begin*/
    public static Long getAvailMemRead() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/meminfo")), 1000);
            String load = null;
            long size = 0l;
            while ((load = reader.readLine()) != null) {

                String[] toks = load.split(":");
                if (toks[0].equals("MemAvailable")) {
                    String sizeBuf = toks[1].trim();
                    String[] sizeBufToks = sizeBuf.split(" ");
                    size = Long.parseLong(sizeBufToks[0])/1024; // kb
                    break;
                }
            }
            reader.close();
            return size;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    /*chenyee guoxt 2018-0102 modify for SW17W16A-2535  end*/

    /**
     * To get total memory size
     * 
     * @return size of total memory
     */
    public static long getTotalMem() {
        String resStr = HelperUtils.executeShellCmd("cat /proc/meminfo|grep MemTotal:");
        String[] memStrs = resStr.split("\\s+");
        long res = 0;
        try {
            res = Long.parseLong(memStrs[1]) / 1024;
            return res;
        } catch (Exception e) {
            Log.d(TAG, "getTotalMem failed!");
            return 0;
        }
    }

    public static double getRatioUsedMem() {
        long total = getTotalMem();
        if (total <= 0) {
            return 0;
        }
        return (double) (total - getAvailMem()) / total;
    }
}
