package com.cydroid.softmanager.monitor.utils;

public class MonitorJobSetting {

    private String mJobType;
    private int mDayOfMonth;
    private int mDayOfWeek;
    private int mHour;
    private int mSecond;
    private int mMinute;

    MonitorJobSetting(String jobType, int dayOfMonth, int dayOfWeek, int hour, int minute, int second) {
        mJobType = jobType;
        mDayOfMonth = dayOfMonth;
        mDayOfWeek = dayOfWeek;
        mHour = hour;
        mSecond = second;
        mMinute = minute;
    }

    public String getJobType() {
        return mJobType;
    }

    public void setJobType(String mJobType) {
        this.mJobType = mJobType;
    }

    public int getDayOfMonth() {
        return mDayOfMonth;
    }

    public void setDayOfMonth(int mDayOfMonth) {
        this.mDayOfMonth = mDayOfMonth;
    }

    public int getDayOfWeek() {
        return mDayOfWeek;
    }

    public void setDayOfWeek(int mDayOfWeek) {
        this.mDayOfWeek = mDayOfWeek;
    }

    public int getHour() {
        return mHour;
    }

    public void setHour(int mHour) {
        this.mHour = mHour;
    }

    public int getSecond() {
        return mSecond;
    }

    public void setSecond(int mSecond) {
        this.mSecond = mSecond;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setMinute(int mMinute) {
        this.mMinute = mMinute;
    }

}
