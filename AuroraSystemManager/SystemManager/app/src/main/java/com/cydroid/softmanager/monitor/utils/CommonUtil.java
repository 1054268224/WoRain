// Gionee <liuyb> <2014-2-25> add for CR01083582 begin
package com.cydroid.softmanager.monitor.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import com.cydroid.softmanager.utils.Log;

public class CommonUtil {
    public static final String TAG = "CommonUtil";
    public static final int DEFAULT_EXEC_TIME_HOUR = 10;
    public static final int DEFAULT_EXEC_TIME_MINUTES = 0;
    public static final String TYPE_WEEK = "week";
    public static final String TYPE_DAY = "day";
    public static final String TYPE_BOOT_COMPLETED = "boot_completed";
    // Gionee <yangxinruo> <2016-4-20> add for CR01669840 begin
    public static final String TYPE_TIME_CHANGED = "time_change";
    // Gionee <yangxinruo> <2016-4-20> add for CR01669840 end

    public static final String JOB_KEY = "jobKey";

    public static final String ACTION_BOOT_AUTO_CLEAN_ALARM = "com.cydroid.softmanager.monitor.action.BOOT_AUTO_CLEAN_ALARM";

    // Gionee <houjie> <2015-09-15> delete for CR01553826 begin
    /*
    public static final String ACTION_CALIBRATE_ALARM = "com.cydroid.softmanager.monitor.action.CALIBRATE_ALARM";
    */
    // Gionee <houjie> <2015-09-15> delete for CR01553826 end

    public static final String ACTION_MONITOR_ALARM_WEEK = "com.cydroid.softmanager.monitor.action.MONITOR_ALARM_WEEK";

    public static final String ACTION_MONITOR_ALARM_DAY = "com.cydroid.softmanager.monitor.action.MONITOR_ALARM_DAY";

    public static final String ACTION_MONITOR_ALARM = "com.cydroid.softmanager.monitor.action.MONITOR_ALARM";

    public static long getNowToNextSundayMonitorInfoTime() {
        long diffMillis = getNextSundayMillis();
        return diffMillis;
    }

    public static long getNowToNextDayMonitorInfoTime() {
        long diffMillis = getNextDayMillis();
        return diffMillis;
    }

    public static long getNowToNextMonitorInfoTime(MonitorJobSetting jobSetting) {
        long diffMillis = getNextTimeMillis(jobSetting);
        // Gionee <yangxinruo> <2015-09-15> add for CR01551835 begin
        Log.i(TAG, "getNowToNextMonitorInfoTime ---> " + diffMillis);
        // Gionee <yangxinruo> <2015-09-15> add for CR01551835 end
        return diffMillis;
    }

    private static Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        return calendar;
    }

    @SuppressWarnings("unused")
    private static long getNowMillis() {
        Calendar calendar = getCalendar();
        Log.i(TAG, "getNowMillis ---> " + calendar.toString());
        return calendar.getTimeInMillis();
    }

    public static long getNextTimeMillis(MonitorJobSetting jobSetting) {
        Calendar jobSettingCalendar = Calendar.getInstance();
        jobSettingCalendar.set(Calendar.HOUR_OF_DAY, jobSetting.getHour());
        jobSettingCalendar.set(Calendar.SECOND, jobSetting.getSecond());
        jobSettingCalendar.set(Calendar.MINUTE, jobSetting.getMinute());
        jobSettingCalendar.set(Calendar.MILLISECOND, 0);

        // Log.i(TAG, "getNext jobSettingCalendar time 1 ---> " +
        // getTimeString(jobSettingCalendar));

        Calendar c = Calendar.getInstance();
        int day_of_seek = 0;
        // Gionee <jianghuan> <2014-05-14> delete for CR01255397 begin
        if (jobSetting.getDayOfMonth() == 1) {

            return getNextMonthMillis(jobSettingCalendar, jobSetting);
            // Gionee <jianghuan> <2014-05-14> delete for CR01255397 end
        } else if (jobSetting.getDayOfWeek() == -1) {
            if (jobSettingCalendar.getTimeInMillis() < c.getTimeInMillis()) {
                jobSettingCalendar.add(Calendar.DATE, 1);
                // Log.i(TAG, "getNext jobSettingCalendar time 2 ---> " +
                // getTimeString(jobSettingCalendar));
            }
        } else {
            jobSettingCalendar.set(Calendar.DAY_OF_WEEK, jobSetting.getDayOfWeek());
            // Log.i(TAG, "getNext jobSettingCalendar time 3 ---> " +
            // getTimeString(jobSettingCalendar));
            // c.time < jobtime
            if (jobSettingCalendar.getTimeInMillis() < c.getTimeInMillis()) {
                day_of_seek = 7;
            }
            jobSettingCalendar.add(Calendar.DATE, day_of_seek);
            // Log.i(TAG, "getNext jobSettingCalendar time 4 ---> " +
            // getTimeString(jobSettingCalendar));
        }
        Log.i(TAG, "getNextTimeMillis time ---> " + getTimeString(jobSettingCalendar));
        return jobSettingCalendar.getTimeInMillis();
    }

    public static long getNextDayMillis() {
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.HOUR_OF_DAY) >= DEFAULT_EXEC_TIME_HOUR) {
            c.add(Calendar.DATE, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, DEFAULT_EXEC_TIME_HOUR);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, DEFAULT_EXEC_TIME_MINUTES);
        c.set(Calendar.MILLISECOND, 0);
        Log.i(TAG, "getNextDayMillis time ---> " + getTimeString(c));
        return c.getTimeInMillis();
    }

    public static long getNextSundayMillis() {
        Calendar c = Calendar.getInstance();
        int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0 && c.get(Calendar.HOUR_OF_DAY) < DEFAULT_EXEC_TIME_HOUR)
            day_of_week = 7;
        c.add(Calendar.DATE, -day_of_week + 7);
        c.set(Calendar.HOUR_OF_DAY, DEFAULT_EXEC_TIME_HOUR);
        c.set(Calendar.SECOND, 2);
        c.set(Calendar.MINUTE, DEFAULT_EXEC_TIME_MINUTES);
        c.set(Calendar.MILLISECOND, 0);
        Log.i(TAG, "getSundayOfThisWeek time ---> " + getTimeString(c));
        return c.getTimeInMillis();
    }

    public static String getTimeString(Calendar c) {
        int month = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + month + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
                + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
    }

    // Gionee <jianghuan> <2014-05-14> add for CR01255397 begin
    private static long getNextMonthMillis(Calendar jobSettingCalendar, MonitorJobSetting jobSetting) {
        Calendar c = Calendar.getInstance();
        jobSettingCalendar.set(Calendar.DAY_OF_MONTH, jobSetting.getDayOfMonth());
        long scheduleTime = jobSettingCalendar.getTimeInMillis();
        long nowTime = System.currentTimeMillis();
        if (nowTime < scheduleTime) {
            Log.i(TAG, "scheduleTime :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(scheduleTime));
            return scheduleTime;
        } else {
            jobSettingCalendar.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
            jobSettingCalendar.set(Calendar.DAY_OF_MONTH, jobSetting.getDayOfMonth());
            long nextTime = jobSettingCalendar.getTimeInMillis();
            Log.i(TAG, "nextTime :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextTime));
            return nextTime;
        }
    }

    // Gionee <jianghuan> <2014-05-14> add for CR01255397 end

    public static final HashMap<String, MonitorJobSetting> mJobMap = new HashMap<String, MonitorJobSetting>();

    static {
        mJobMap.put("root", new MonitorJobSetting("day", -1, -1, 10, 0, 0));
        mJobMap.put("apps", new MonitorJobSetting("week", -1, 1, 19, 20, 0));
        mJobMap.put("net", new MonitorJobSetting("week", -1, 1, 13, 0, 0));
        mJobMap.put("power", new MonitorJobSetting("week", -1, 1, 9, 0, 0));
        // Gionee <jianghuan> <2014-05-14> add for CR01255397 begin
        mJobMap.put("netpackage", new MonitorJobSetting("month", 1, -1, 8, 0, 0));
        // Gionee <jianghuan> <2014-05-14> add for CR01255397 end

    }

}
// Gionee <liuyb> <2014-2-25> add for CR01083582 end