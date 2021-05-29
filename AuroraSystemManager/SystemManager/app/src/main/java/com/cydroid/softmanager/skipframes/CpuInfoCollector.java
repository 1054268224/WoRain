/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 卡顿跳帧CPU相关数据收集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.skipframes;

import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.os.SystemProperties;

public class CpuInfoCollector implements ISystemInfoCollector {

    private static final String TAG = "CpuInfoCollector";

    public CpuInfoCollector(int pid) {
    }

    @Override
    public void record(SkipFramesInfo skipinfoData) {
        String cpuAvgLoad = HelperUtils.executeShellCmd("cat /proc/loadavg").split("\\n")[0];
        String[] thermalTemp = HelperUtils.executeShellCmd("cat /sys/class/thermal/thermal_zone*/temp")
                .split("\\n");
        String[] thermalTypes = HelperUtils.executeShellCmd("cat /sys/class/thermal/thermal_zone*/type")
                .split("\\n");
        String cpuTemperature = "";
        if (isMtkPlatform()) {
            for (int i = 0; i < thermalTypes.length; i++) {
                if (thermalTypes[i].startsWith("mtktscpu") && thermalTemp.length > i) {
                    cpuTemperature = thermalTemp[i].replace("\\n", "");
                    break;
                }
            }
        }
        if (isQcomPlatform()) {
            for (int i = 0; i < thermalTypes.length; i++) {
                if (thermalTypes[i].startsWith("tsens_tz_sensor8") && thermalTemp.length > i) {
                    cpuTemperature = thermalTemp[i].replace("\\n", "");
                    break;
                }
            }
        }
        String cpuOnlineCores = HelperUtils.executeShellCmd("cat /sys/devices/system/cpu/online")
                .split("\\n")[0];
        String cpuFreqBig = HelperUtils.executeShellCmd("cat /proc/cpufreq/MT_CPU_DVFS_BIG/cpufreq_freq")
                .split("\\n")[0];
        String cpuFreqLittle = HelperUtils
                .executeShellCmd("cat /proc/cpufreq/MT_CPU_DVFS_LITTLE/cpufreq_freq").split("\\n")[0];
        String cpuFreq = HelperUtils.executeShellCmd("cat /proc/cpufreq/cpufreq_freq").split("\\n")[0];

        skipinfoData.cpuInfo.cpuAverageLoad = cpuAvgLoad;
        try {
            skipinfoData.cpuInfo.cpuTemperature = Integer.parseInt(cpuTemperature);
        } catch (Exception e) {
            Log.d(TAG, "parse cpuTemperature failed " + cpuTemperature);
            skipinfoData.cpuInfo.cpuTemperature = -1;
        }
        skipinfoData.cpuInfo.cpuOnlineNumber = cpuOnlineCores;
        if (cpuFreqBig.trim().isEmpty()) {
            skipinfoData.cpuInfo.cpuFreqBig = 0;
        } else {
            try {
                skipinfoData.cpuInfo.cpuFreqBig = Integer.parseInt(cpuFreqBig.trim().split(" ")[0]);
            } catch (Exception e) {
                Log.d(TAG, "parse cpuFreqBig failed " + cpuFreqBig);
                skipinfoData.cpuInfo.cpuFreqBig = -1;
            }
        }
        if (cpuFreqLittle.trim().isEmpty()) {
            skipinfoData.cpuInfo.cpuFreqLittle = 0;
        } else {
            try {
                skipinfoData.cpuInfo.cpuFreqLittle = Integer.parseInt(cpuFreqLittle.trim().split(" ")[0]);
            } catch (Exception e) {
                Log.d(TAG, "parse cpuFreqLittle failed " + cpuFreqLittle);
                skipinfoData.cpuInfo.cpuFreqLittle = -1;
            }
        }
        if (cpuFreq.trim().isEmpty()) {
            skipinfoData.cpuInfo.cpuFreq = 0;
        } else {
            try {
                skipinfoData.cpuInfo.cpuFreq = Integer.parseInt(cpuFreq.trim().split(" ")[0]);
            } catch (Exception e) {
                Log.d(TAG, "parse cpuFreq failed " + cpuFreq);
                skipinfoData.cpuInfo.cpuFreq = -1;
            }
        }
    }

    private boolean isQcomPlatform() {
        String platformStr = SystemProperties.get("ro.hardware", "");
        return platformStr.equals("qcom");
    }

    private boolean isMtkPlatform() {
        String platformStr = SystemProperties.get("ro.hardware", "");
        return platformStr.startsWith("mt");
    }
}
