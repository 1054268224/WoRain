package com.cydroid.softmanager.powersaver.analysis;

import android.os.BatteryStats;
import android.os.SystemClock;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;

public class AppBatteryInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public int userId;
    public double powerValue;
    public DrainType drainType;
    public long usageTime;
    public long cpuTime;
    public long gpsTime;
    public long wifiRunningTime;
    public long cpuFgTime;
    public long wakeLockTime;
    public long mobileRxPackets;
    public long mobileTxPackets;
    public long mobileActive;
    public int mobileActiveCount;
    public double mobilemspp; // milliseconds per packet
    public long wifiRxPackets;
    public long wifiTxPackets;
    public long mobileRxBytes;
    public long mobileTxBytes;
    public long wifiRxBytes;
    public long wifiTxBytes;
    public long audioTurnedOnTime;
    public long vedioTurnedOnTime;
    public long vibratorTurnedOnTime;
    public double percent;
    public String packageWithHighestDrain;

    public AppBatteryInfo(BatterySipper sipper) {
        if (sipper == null) {
            return;
        }
        if (sipper.uidObj != null) {
            userId = sipper.uidObj.getUid();
        }
        powerValue = sipper.totalPowerMah;
        drainType = sipper.drainType;
        usageTime = sipper.usageTimeMs;
        cpuTime = sipper.cpuTimeMs;
        gpsTime = sipper.gpsTimeMs;
        wifiRunningTime = sipper.wifiRunningTimeMs;
        cpuFgTime = sipper.cpuFgTimeMs;
        wakeLockTime = sipper.wakeLockTimeMs;
        mobileActive = sipper.mobileActive;
        mobileActiveCount = sipper.mobileActiveCount;
        mobilemspp = sipper.mobilemspp; // milliseconds per packet
        wifiRxPackets = sipper.wifiRxPackets;
        wifiTxPackets = sipper.wifiTxPackets;
        mobileRxBytes = sipper.mobileRxBytes;
        mobileTxBytes = sipper.mobileTxBytes;
        wifiRxBytes = sipper.wifiRxBytes;
        wifiTxBytes = sipper.wifiTxBytes;
        percent = sipper.percent;
        packageWithHighestDrain = sipper.packageWithHighestDrain;
        if (sipper.uidObj == null) {
            return;
        }
        if (sipper.uidObj.getAudioTurnedOnTimer() != null)
            audioTurnedOnTime = sipper.uidObj.getAudioTurnedOnTimer().getTotalTimeLocked(
                    SystemClock.elapsedRealtime() * 1000, BatteryStats.STATS_SINCE_CHARGED);
        if (sipper.uidObj.getVideoTurnedOnTimer() != null)
            vedioTurnedOnTime = sipper.uidObj.getVideoTurnedOnTimer().getTotalTimeLocked(
                    SystemClock.elapsedRealtime() * 1000, BatteryStats.STATS_SINCE_CHARGED);
        if (sipper.uidObj.getVibratorOnTimer() == null) {
            return;
        }
        vibratorTurnedOnTime = (sipper.uidObj.getVibratorOnTimer().getTotalTimeLocked(
                SystemClock.elapsedRealtime() * 1000, BatteryStats.STATS_SINCE_CHARGED) + 500) / 1000;
    }

    public AppBatteryInfo() {
    }

    @Override
    public String toString() {
        return "AppBatteryInfo [userId=" + userId + ", powerValue=" + powerValue + ", usageTime=" + usageTime
                + ", cpuTime=" + cpuTime + ", gpsTime=" + gpsTime + ", wifiRunningTime=" + wifiRunningTime
                + ", cpuFgTime=" + cpuFgTime + ", wakeLockTime=" + wakeLockTime + ", mobileRxPackets="
                + mobileRxPackets + ", mobileTxPackets=" + mobileTxPackets + ", mobileActive=" + mobileActive
                + ", mobileActiveCount=" + mobileActiveCount + ", mobilemspp=" + mobilemspp
                + ", wifiRxPackets=" + wifiRxPackets + ", wifiTxPackets=" + wifiTxPackets
                + ", mobileRxBytes=" + mobileRxBytes + ", mobileTxBytes=" + mobileTxBytes + ", wifiRxBytes="
                + wifiRxBytes + ", wifiTxBytes=" + wifiTxBytes + ", audioTurnedOnTime=" + audioTurnedOnTime
                + ", vedioTurnedOnTime=" + vedioTurnedOnTime + ", vibratorTurnedOnTime="
                + vibratorTurnedOnTime + ", percent=" + percent + ", packageWithHighestDrain="
                + packageWithHighestDrain + "]";
    }

    // Gionee <yangxinruo> <2015-12-1> add for CR01602034 begin
    public AppBatteryInfo add(AppBatteryInfo other) {
        this.powerValue += other.powerValue;
        this.usageTime += other.usageTime;
        this.cpuTime += other.cpuTime;
        this.gpsTime += other.gpsTime;
        this.wifiRunningTime += other.wifiRunningTime;
        this.cpuFgTime += other.cpuFgTime;
        this.wakeLockTime += other.wakeLockTime;
        this.mobileActive += other.mobileActive;
        this.mobileActiveCount += other.mobileActiveCount;
        this.wifiRxPackets += other.wifiRxPackets;
        this.wifiTxPackets += other.wifiTxPackets;
        this.mobileRxBytes += other.mobileRxBytes;
        this.mobileTxBytes += other.mobileTxBytes;
        this.wifiRxBytes += other.wifiRxBytes;
        this.wifiTxBytes += other.wifiTxBytes;
        this.percent += other.percent;
        this.audioTurnedOnTime += other.audioTurnedOnTime;
        this.vedioTurnedOnTime += other.vedioTurnedOnTime;
        this.vibratorTurnedOnTime += other.vibratorTurnedOnTime;
        return this;
    }

    // Gionee <yangxinruo> <2015-12-1> add for CR01602034 end

    // Gionee <yangxinruo> <2015-12-28> add for CR01615275 begin
    public boolean isVaild() {
        return !(powerValue < 0) && usageTime >= 0 && cpuTime >= 0 && gpsTime >= 0 && wifiRunningTime >= 0
                && cpuFgTime >= 0 && wakeLockTime >= 0 && mobileActive >= 0 && mobileActiveCount >= 0
                && wifiRxPackets >= 0 && wifiTxPackets >= 0 && mobileRxBytes >= 0 && mobileTxBytes >= 0
                && wifiRxBytes >= 0 && wifiTxBytes >= 0;
    }
    // Gionee <yangxinruo> <2015-12-28> add for CR01615275 end
}
