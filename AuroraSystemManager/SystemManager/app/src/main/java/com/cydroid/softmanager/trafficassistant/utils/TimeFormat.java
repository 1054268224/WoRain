package com.cydroid.softmanager.trafficassistant.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;

import com.cydroid.softmanager.utils.Log;
//Gionee <yangxinruo> <2015-07-31> add for CR01529084 begin
import java.util.Date;
import java.util.Locale;
import android.view.View;
//Gionee <yangxinruo> <2015-07-31> add for CR01529084 end

public class TimeFormat {
    private static final String TAG = "TimeFormat";
    
    public static long changeMonthToUTC() {
        return 0;
    }

    public static long getStartTime(int year, int month, int day, int hour, int mins, int sec) {
        return covertToUTC(year, month, day, hour, mins, sec);
    }

    public static long getStartTime(String date, int hour, int mins, int sec) {
        return covertToUTC(date, hour, mins, sec);
    }

    private static long covertToUTC(String date, int hour, int mins, int sec) {
        long utcTime = 0;
        String tString = date + " " + hour + ":" + mins + ":"
                + sec;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d = format.parse(tString);
            utcTime = d.getTime();
        } catch (ParseException e) {

        }

        return utcTime;
    }

    private static long covertToUTC(int year, int month, int day, int hour, int mins, int sec) {
        long utcTime = 0;
        String tString = year + "-" + month + "-" + day
                + " " + hour + ":" + mins + ":" + sec;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(tString);
            utcTime = date.getTime();
        } catch (ParseException e) {

        }

        return utcTime;
    }

    public static String[] getDaysOfMonth(int[] mDateInterval) {

        int[] mDayOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((mDateInterval[0] % 4 == 0 && mDateInterval[0] % 100 != 0) || mDateInterval[0] % 400 == 0) {
            mDayOfMonth[1] = 29;
        }

        ArrayList<String> list = new ArrayList<String>();

        int index;
        String result = "";
        if (mDateInterval[1] != mDateInterval[4]) {
            int size = mDayOfMonth[mDateInterval[1] - 1];
            for (index = mDateInterval[2]; index <= size; index++) {
                result = mDateInterval[0] + "-" + mDateInterval[1] + "-" + index;
                list.add(result);
            }

            for (index = 1; index <= mDateInterval[5]; index++) {
                result = mDateInterval[3] + "-" + mDateInterval[4] + "-" + index;
                list.add(result);
            }
        } else {
            for (index = mDateInterval[2]; index <= mDateInterval[5]; index++) {
                result = mDateInterval[0] + "-" + mDateInterval[1] + "-" + index;
                list.add(result);
            }
        }

        String[] days = (String[]) list.toArray(new String[0]);

        return days;
    }

    public static String[] getHoursOfDay() {
        String[] hours = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23"/* , "24" */};

        return hours;
    }

    public static int getStartDay(Context context, int simId) {
        SharedPreferences sp = context.getSharedPreferences("traffic_preference" + simId,
                Context.MODE_MULTI_PROCESS);
        return Integer.parseInt(sp.getString("START_DATE", "1"));
    }

    public static int[] getNowTimeArray() {
        Calendar cal = Calendar.getInstance();
        int[] times = new int[6];
        times[0] = cal.get(Calendar.YEAR);
        times[1] = cal.get(Calendar.MONTH);
        times[2] = cal.get(Calendar.DAY_OF_MONTH);
        times[3] = cal.get(Calendar.HOUR_OF_DAY);
        times[4] = cal.get(Calendar.MINUTE);
        times[5] = cal.get(Calendar.SECOND);
        return times;
    }

    public static String[] getWeekArray() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(System.currentTimeMillis());
        Date d = null;
        Calendar cal = Calendar.getInstance();
        try {
            d = format.parse(date);
            cal.setTime(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] typesFR = { Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,Calendar.SUNDAY};
        int[] types = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};
        String[] timeArray = new String[types.length];
        // Gionee <xionghonggang> <2017-03-02> modify for 76863 begin
        int startDay = cal.getFirstDayOfWeek();
        Log.d(TAG, "First day of week is: " + startDay);        
        for (int i = 0; i < types.length; i++) {
            /*guoxt modify for CR01601758 begin*/
            if (Locale.getDefault().getLanguage().toLowerCase().equals("fr")
                    || startDay == Calendar.MONDAY){
                // Gionee <xionghonggang> <2017-03-02> modify for 76863 end
                cal.set(Calendar.DAY_OF_WEEK, typesFR[i]);
            }else{
                cal.set(Calendar.DAY_OF_WEEK, types[i]);
            }
            /*guoxt modify for CR01601758 end*/
			
            timeArray[i] = format.format(cal.getTime());
            Log.d(TAG, timeArray[i]);
        }

        return timeArray;
    }

    public static String[] getNotificationWeekArray() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String[] dateZone = new String[8];
        Long time = System.currentTimeMillis();
        for (int i = 7; i >= 0; i--) {
            dateZone[i] = dateFormat.format(time);
            time -= 24 * 60 * 60 * 1000;
        }

        return dateZone;
    }
    
    // mengdw <2016-01-04> add for CR01571760 begin
    public static String getNowDate() {
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
    // mengdw <2016-01-04> add for CR01571760 end
}