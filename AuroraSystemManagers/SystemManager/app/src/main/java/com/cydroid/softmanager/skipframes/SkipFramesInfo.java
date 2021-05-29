/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 卡顿跳帧数据
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.skipframes;

public class SkipFramesInfo {
    public String packageName = "";
    int pid = -1;
    public int skippedFramesNumber = -1;
    long skipFrameVsyncTimestamp = -1;
    public long skipFrameDoFrameTimestamp = -1;

    int runningAppProcessesNumber = -1;
    int runningAppThreadsNumber = -1;

    public CpuInfo cpuInfo = new CpuInfo();
    public ComponentInfo componentInfo = new ComponentInfo();
    StrictModeInfo strictModeInfo = new StrictModeInfo();

    public static class CpuInfo {
        public String cpuAverageLoad = "";
        public int cpuTemperature = -1;
        public String cpuOnlineNumber = "";
        int cpuFreqBig = -1;
        int cpuFreqLittle = -1;
        public int cpuFreq = -1;

    }

    public static class ComponentInfo {
        public String currentTopActivity = "";
        public String lastTopActivity = "";
    }

    public static class StrictModeInfo {
        String strictModeLog = "";
    }
}
