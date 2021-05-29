package com.cydroid.softmanager.powersaver.notification;

import android.os.Parcel;
import android.os.Parcelable;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;

import java.util.ArrayList;

public class PowerConsumeAppData extends ProcessMemoryEntity {
    /**
     * 该进程消耗电量
     */
    public double mPowerValue = 0d;

    /**
     * 该进程是否在忽略（不提醒）名单中。
     */
    public boolean mIsInIgnoredAppList = false;

    /**
     * 该进程触发的异常应用行为。
     */
    public ArrayList<String> mAlertTypes = new ArrayList<String>();

    public PowerConsumeAppData() {
    }

    public PowerConsumeAppData(ProcessMemoryEntity processMemoryEntity, double powerValue,
                               boolean isInIgnoredAppList) {
        super(processMemoryEntity.mPackageName, processMemoryEntity.mPss,
                processMemoryEntity.mIsInUserWhiteAppList, processMemoryEntity.mIsPrivateApp,
                processMemoryEntity.mProcess);
        mPowerValue = powerValue;
        mIsInIgnoredAppList = isInIgnoredAppList;
    }

    public PowerConsumeAppData(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(mPowerValue);
        dest.writeInt(mIsInIgnoredAppList ? 1 : 0);
        dest.writeStringList(mAlertTypes);
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mPowerValue = in.readDouble();
        mIsInIgnoredAppList = (in.readInt() == 1);
        mAlertTypes = new ArrayList<String>();
        in.readStringList(mAlertTypes);
    }

    public static final Parcelable.Creator<PowerConsumeAppData> CREATOR = new Parcelable.Creator<PowerConsumeAppData>() {
        @Override
        public PowerConsumeAppData createFromParcel(Parcel in) {
            return new PowerConsumeAppData(in);
        }

        @Override
        public PowerConsumeAppData[] newArray(int size) {
            return new PowerConsumeAppData[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" mPowerValue=" + mPowerValue);
        sb.append(" mIsInIgnoredAppList=" + mIsInIgnoredAppList);
        sb.append(" mAlertTypes=" + mAlertTypes);
        return sb.toString();
    }
}
