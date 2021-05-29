// Gionee <yangxinruo> <2016-3-18> add for CR01654969 begin
package com.cydroid.softmanager.powersaver.mode;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ModeItemInfo implements Parcelable {
    public String name;
    public String defaultVal;
    public String configVal;
    public String title;
    public String summary;
    public ArrayList<String> candidateVals;
    public ArrayList<String> candidateValDecs;
    public String format;
//    public int displayPriority;

    public ModeItemInfo(String configName) {
        name = configName;
        defaultVal = "";
        configVal = "";
        title = "";
        summary = "";
        format = "";
//        displayPriority = -1;
        candidateVals = new ArrayList<String>();
        candidateValDecs = new ArrayList<String>();
    }

    public ModeItemInfo(Parcel source) {
        name = source.readString();
        defaultVal = source.readString();
        configVal = source.readString();
        title = source.readString();
        summary = source.readString();
        format = source.readString();
//        displayPriority = source.readInt();
        candidateVals = new ArrayList<String>();
        source.readStringList(candidateVals);
        candidateValDecs = new ArrayList<String>();
        source.readStringList(candidateValDecs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(defaultVal);
        dest.writeString(configVal);
        dest.writeString(title);
        dest.writeString(summary);
        dest.writeString(format);
//        dest.writeInt(displayPriority);
        dest.writeStringList(candidateVals);
        dest.writeStringList(candidateValDecs);
    }

    public static final Parcelable.Creator<ModeItemInfo> CREATOR = new Parcelable.Creator<ModeItemInfo>() {

        @Override
        public ModeItemInfo createFromParcel(Parcel source) {
            return new ModeItemInfo(source);
        }

        @Override
        public ModeItemInfo[] newArray(int size) {
            return new ModeItemInfo[size];
        }

    };
}
// Gionee <yangxinruo> <2016-3-18> add for CR01654969 end