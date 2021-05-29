package com.cydroid.powersaver.launcher;

import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.Html;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
//Gionee <yangxinruo> <2015-07-31> add for CR01529084 begin
import android.text.format.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.View;

import com.cydroid.powersaver.launcher.util.Log;

//Gionee <yangxinruo> <2015-07-31> add for CR01529084 end
@SuppressLint("NewApi")
public class DateTimeView extends LinearLayout {
    private static final String TAG = "DateTimeView";
    private static final String FENHAO = ":";

    private Time mCalendar;
    private boolean mAttached;
    private final Handler mHandler = new Handler();

    private TextView mTimeViewHour,mTimeViewMinute;
    private TextView mDateView;
    private TextView mWeekView;
    //Gionee <yangxinruo> <2015-07-31> add for CR01529084 begin
    private TextView mHourAMPMZHTextView;
    private TextView mHourAMPMENTextView;
    //Gionee <yangxinruo> <2015-07-31> add for CR01529084 end
    private String[] mMonth;

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCalendar = new Time();
        mMonth = context.getResources().getStringArray(R.array.month);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTimeViewHour = (TextView) findViewById(R.id.tv_time_hour);
        mTimeViewMinute = (TextView) findViewById(R.id.tv_time_minute);
        mDateView = (TextView) findViewById(R.id.tv_date);
        mWeekView = (TextView) findViewById(R.id.tv_week);
        //Gionee <yangxinruo> <2015-07-31> add for CR01529084 begin
        mHourAMPMZHTextView = (TextView) findViewById(R.id.tv_am_pm_zh);
        mHourAMPMENTextView = (TextView) findViewById(R.id.tv_am_pm_en);
        //Gionee <yangxinruo> <2015-07-31> add for CR01529084 end
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            Log.d(TAG, "onAttachedToWindow, registerReceiver");
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
        }

        // NOTE: It's safe to do these after registering the receiver since the
        // receiver always runs
        // in the main thread, therefore the receiver can't run before this
        // method returns.

        // The time zone may have changed while the receiver wasn't registered,
        // so update the Time
        mCalendar = new Time();

        onTimeChanged();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            Log.d(TAG, "onDetachedFromWindow, unregisterReceiver");
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }
    //Gionee <yangxinruo> <2015-07-31> modify for CR01529084 begin
    private void onTimeChanged() {
        mCalendar.setToNow();

        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());
        String hourString =CalendarUtil.getHour();
        // Gionee xionghg modify for power saving optimization 145357 begin
        String amPmPrefix = "";
        if (!is24HourFormat) {
            Date d = new Date();
            SimpleDateFormat ss = new SimpleDateFormat("hh");
            hourString = ss.format(d);

            if (ConfigUtil.SUPPORT_NEW_LAUNCHER) {
                boolean isAM = CalendarUtil.getAMPM().equals("AM");
                amPmPrefix = getResources().getString(isAM ? R.string.am : R.string.pm) + " ";
            } else {
                if (isZH()) {
                    mHourAMPMENTextView.setVisibility(View.GONE);

                    mHourAMPMZHTextView.setVisibility(View.VISIBLE);
                    if (CalendarUtil.getAMPM().equals("AM")) {
                        mHourAMPMZHTextView.setText(R.string.am);
                    } else {
                        mHourAMPMZHTextView.setText(R.string.pm);
                    }
                } else {
                    mHourAMPMZHTextView.setVisibility(View.GONE);
                    mHourAMPMENTextView.setVisibility(View.VISIBLE);
                    mHourAMPMENTextView.setText(CalendarUtil.getAMPM());
                }
            }
        } else {
            mHourAMPMENTextView.setVisibility(View.GONE);
            mHourAMPMZHTextView.setVisibility(View.GONE);
        }

        // time hour
        /*guoxt modify for CR01618791 begin*/
        if(isAR()){
        	mTimeViewHour.setText("" + Integer.valueOf(hourString));
        }else{
        	mTimeViewHour.setText(hourString);
        }
        /*guoxt modify for CR01618791 end*/
        // time minute
        mTimeViewMinute.setText(FENHAO+CalendarUtil.getMinute());

        // date
        StringBuffer date = new StringBuffer();
        String month = mMonth[mCalendar.month];

        date.append(String.format(month, CalendarUtil.getDay()));
       // date.append("    ");
        mDateView.setText(date.toString());

        // week
        StringBuffer week = new StringBuffer();
        if (ConfigUtil.SUPPORT_NEW_LAUNCHER) {
            week.append(amPmPrefix);
        }
        // Gionee xionghg modify for power saving optimization 145357 end
        week.append(CalendarUtil.getWeek(getContext(), R.array.weekday));
        mWeekView.setText(week.toString());
    }

    private boolean isZH() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isAR() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("ar")) {
            return true;
        } else {
            return false;
        }
    }
    /*
    private void onTimeChanged() {
        mCalendar.setToNow();

        // time
        mTimeViewHour.setText(CalendarUtil.getHour());
        mTimeViewMinute.setText(FENHAO + CalendarUtil.getMinute());
        
        // date
        StringBuffer date = new StringBuffer();
        String month = mMonth[mCalendar.month];

        date.append(String.format(month, CalendarUtil.getDay()));
        date.append("    ");
        mDateView.setText(date.toString());

        // week
        StringBuffer week = new StringBuffer();
        week.append(CalendarUtil.getWeek(getContext(),R.array.weekday));
        mWeekView.setText(week.toString());
    }
    */
    //Gionee <yangxinruo> <2015-07-31> modify for CR01529084 end
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive, action=" + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }
            onTimeChanged();
            invalidate();
        }
    };
}
