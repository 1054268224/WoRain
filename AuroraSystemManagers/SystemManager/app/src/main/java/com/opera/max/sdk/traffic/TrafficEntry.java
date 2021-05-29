package com.opera.max.sdk.traffic;

import android.os.Parcel;
import android.os.Parcelable;

public class TrafficEntry implements Parcelable {
    public int uid;
    public long rxBytes;
    public long txBytes;
    public long saveBytes;

    public static final Parcelable.Creator<TrafficEntry> CREATOR = new
Parcelable.Creator<TrafficEntry>() {
        public TrafficEntry createFromParcel(Parcel in) {
            return new TrafficEntry(in);
        }

        public TrafficEntry[] newArray(int size) {
            return new TrafficEntry[size];
        }
    };

    public TrafficEntry() {
    }

    public TrafficEntry(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        uid = in.readInt();
        rxBytes = in.readLong();
        txBytes = in.readLong();
        saveBytes = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeLong(rxBytes);
        dest.writeLong(txBytes);
        dest.writeLong(saveBytes);
    }
}