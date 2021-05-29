/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-12-27
 */
package com.cydroid.softmanager.memoryclean.model;

import android.app.ActivityManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 运行进程内存信息的封装类
 *
 * @author Houjie
 */
public class ProcessMemoryEntity implements Parcelable {
    /**
     * 该进程所在应用包名。
     */
    public String mPackageName;

    /**
     * 该进程占用的PSS
     */
    public long mPss;

    /**
     * 该进程是否在用户白名单中。
     */
    public boolean mIsInUserWhiteAppList;

    /**
     * 该进程是否在私密空间中。
     */
    public boolean mIsPrivateApp;

    /**
     * 改进城的ActivityManager.RunningAppProcessInfo对象
     */
    public ActivityManager.RunningAppProcessInfo mProcess;

    public ProcessMemoryEntity() {
    }

    public ProcessMemoryEntity(String packageName, long pss,
                               boolean isInUserWhiteAppList, boolean isPrivateApp,
                               ActivityManager.RunningAppProcessInfo process) {
        mPackageName = packageName;
        mPss = pss;
        mIsInUserWhiteAppList = isInUserWhiteAppList;
        mIsPrivateApp = isPrivateApp;
        mProcess = process;
    }

    public ProcessMemoryEntity(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPackageName);
        dest.writeLong(mPss);
        dest.writeInt(mIsInUserWhiteAppList ? 1 : 0);
        dest.writeInt(mIsPrivateApp ? 1 : 0);
        dest.writeParcelable(mProcess, flags);
    }

    public void readFromParcel(Parcel in) {
        mPackageName = in.readString();
        mPss = in.readLong();
        mIsInUserWhiteAppList = (in.readInt() == 1);
        mIsPrivateApp = (in.readInt() == 1);
        mProcess = in.readParcelable(ActivityManager.RunningAppProcessInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<ProcessMemoryEntity> CREATOR =
            new Parcelable.Creator<ProcessMemoryEntity>() {
        @Override
        public ProcessMemoryEntity createFromParcel(Parcel in) {
            return new ProcessMemoryEntity(in);
        }

        @Override
        public ProcessMemoryEntity[] newArray(int size) {
            return new ProcessMemoryEntity[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" mPackageName=" + mPackageName);
        sb.append(" mPss=" + mPss);
        sb.append(" isInUserWhiteAppList=" + mIsInUserWhiteAppList);
        sb.append(" mIsPrivateApp=" + mIsPrivateApp);
        sb.append(" mProcess=" + (mProcess == null ? "null" : mProcess.toString()));
        return sb.toString();
    }
}
