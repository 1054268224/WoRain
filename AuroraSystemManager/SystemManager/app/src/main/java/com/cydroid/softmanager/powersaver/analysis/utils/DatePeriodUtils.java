/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 采集时间段辅助类
 *
 * Revised Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import com.cydroid.softmanager.monitor.utils.CommonUtil;
//import com.cydroid.softmanager.utils.Log;

public class DatePeriodUtils {

    private static final String TAG = "DatePeriodUtils";

    public static final int PERIOD_DURATION = 24;// hours
    private static final String PERIOD_LABEL_FORMAT = "yyyy-MM-dd HH";

    private static Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        return calendar;
    }

    private static boolean isTodayTime(long currentMillis) {
        Calendar todayMin = Calendar.getInstance();
        todayMin.set(Calendar.HOUR_OF_DAY, 0);
        todayMin.set(Calendar.MINUTE, 0);
        todayMin.set(Calendar.SECOND, 0);
        todayMin.set(Calendar.MILLISECOND, 0);
        if (currentMillis < todayMin.getTimeInMillis())
            return false;
        Calendar todayMax = Calendar.getInstance();
        todayMax.set(Calendar.HOUR_OF_DAY, 23);
        todayMax.set(Calendar.MINUTE, 59);
        todayMax.set(Calendar.SECOND, 59);
        todayMax.set(Calendar.MILLISECOND, 999);
        return currentMillis <= todayMax.getTimeInMillis();
    }

    public static long getNextPeriodTimeInMillis(long currentMillis) {
        if (!isTodayTime(currentMillis)) {
            throw new IllegalArgumentException("time out of range");
        }
        for (int i = 24 / PERIOD_DURATION; i >= 1; i--) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 24 - PERIOD_DURATION * i);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 1);
            c.set(Calendar.MILLISECOND, 0);
            if (currentMillis < c.getTimeInMillis()) {
                // Log.i(TAG, "getNextPeriodTimeInMillis " + c.getTimeInMillis() + " time ---> "
                // + CommonUtil.getTimeString(c));
                return c.getTimeInMillis();
            }
        }
        Calendar cNext = Calendar.getInstance();
        cNext.add(Calendar.DATE, 1);
        cNext.set(Calendar.HOUR_OF_DAY, 24 - PERIOD_DURATION * (24 / PERIOD_DURATION));
        cNext.set(Calendar.SECOND, 0);
        cNext.set(Calendar.MINUTE, 1);
        cNext.set(Calendar.MILLISECOND, 0);
        // Log.i(TAG, "getNextPeriodTimeInMillis " + cNext.getTimeInMillis() + " time ---> "
        // + CommonUtil.getTimeString(cNext));
        return cNext.getTimeInMillis();

    }

    public static long getCurrentPeriodTimeInMillis(long currentMillis) {
        if (!isTodayTime(currentMillis)) {
            throw new IllegalArgumentException("time out of range");
        }
        for (int i = 1; i <= 24 / PERIOD_DURATION; i++) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 24 - PERIOD_DURATION * i);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MINUTE, 1);
            c.set(Calendar.MILLISECOND, 0);
            if (currentMillis >= c.getTimeInMillis()) {
                c.set(Calendar.MINUTE, 6);
                // Log.i(TAG, "getCurrentPeriodUploadTimeInMillis " + c.getTimeInMillis() + " time ---> "
                // + CommonUtil.getTimeString(c));
                return c.getTimeInMillis();
            }
        }
        Calendar cLast = Calendar.getInstance();
        cLast.add(Calendar.DATE, -1);
        cLast.set(Calendar.HOUR_OF_DAY, 24 - PERIOD_DURATION);
        cLast.set(Calendar.SECOND, 0);
        cLast.set(Calendar.MINUTE, 6);
        cLast.set(Calendar.MILLISECOND, 0);
        // Log.i(TAG, "getCurrentPeriodUploadTimeInMillis " + cLast.getTimeInMillis() + " time ---> "
        // + CommonUtil.getTimeString(cLast));
        return cLast.getTimeInMillis();
    }

    public static String getCurrentPeriodDateInFormatString(long currentMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(PERIOD_LABEL_FORMAT);
        Calendar c = getCalendar();
        c.setTimeInMillis(getCurrentPeriodTimeInMillis(currentMillis));
        String result = sdf.format(c.getTime());
        // Log.i(TAG, "getCurrentPeriodUpdateDateInString -------> " + result);
        return result;
        // return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" +
        // c.get(Calendar.DAY_OF_MONTH)
        // + " " + c.get(Calendar.HOUR_OF_DAY);
    }

    public static String getLastPeriodDateInFormatString(long currentMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(PERIOD_LABEL_FORMAT);
        Calendar c = getCalendar();
        c.setTimeInMillis(getCurrentPeriodTimeInMillis(currentMillis));
        c.add(Calendar.HOUR_OF_DAY, -PERIOD_DURATION);
        String result = sdf.format(c.getTime());
        // Log.i(TAG, "getLastPeriodUploadDateInFormatString -------> " + result);
        return result;
    }

    public static long convertDateFormatStringToMillis(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(PERIOD_LABEL_FORMAT);
        Date date;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.getTimeInMillis();
    }
}
