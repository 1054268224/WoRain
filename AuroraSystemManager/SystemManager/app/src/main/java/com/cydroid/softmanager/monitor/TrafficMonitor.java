package com.cydroid.softmanager.monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.monitor.interfaces.IMonitorJob;
import com.cydroid.softmanager.monitor.utils.CommonUtil;
import com.cydroid.softmanager.monitor.utils.MonitorJobSetting;
import com.cydroid.softmanager.monitor.utils.TrafficMonitorUtil;
import com.cydroid.softmanager.trafficassistant.NotificationController;
import com.cydroid.softmanager.trafficassistant.SIMInfoWrapper;
import com.cydroid.softmanager.trafficassistant.SIMParame;
import com.cydroid.softmanager.trafficassistant.TrafficPopWindows;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.utils.Log;

//Gionee <jianghuan> <2014-03-08> add for CR01111459 begin
public class TrafficMonitor implements IMonitorJob {
    private static final String TAG = "TrafficMonitor";

    private Context mContext;
    private static final int MSG_OK = 0;
    private final long mCycleTime = 7 * 24 * 60 * 60 * 1000;
    private long mCurrTime;
    private long mLastCurrTime;
    private String mCurrDate;
    private String mLastCurrDate;
    private Handler mHandler;
    private int mSimIndex = 0;
    private String mMessage = "";
    private String mDateZone = "";

    @Override
    public void setExecTime(int hour, int minutes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void execute(Context context) {
        // TODO Auto-generated method stub

        mContext = context;
        if (!getSimState(mContext)) {// no sim card
            return;
        }

        new Thread(runnable).start();
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_OK:
                        NotificationController notification = NotificationController.getDefault(mContext);
                        notification.setSimIndex(mSimIndex);
                        notification.setCycleInfo(mDateZone);
                        notification.setSoundId(false);
                        notification.setTitle(R.string.traffic_monitor_title);
                        String content = String
                                .format(mContext.getResources().getString(R.string.traffic_monitor_message),
                                        mMessage);
                        notification.setContent(content);
                        notification.setTickerText(R.string.traffic_monitor_title);
                        notification.setSmallIcon(R.drawable.notify);
                        notification
                                .setClass(com.cydroid.softmanager.trafficassistant.TrafficRankActivity.class);
                        notification.show(notification.getWarningId());
                        break;
                    default:
                        break;
                }
            }

        };

    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            getTimeInfo();
            getNotificationInfo();
            // mengdw <2016-06-15> add for CR01718413 begin
            TrafficMonitorUtil trafficMonitor = new TrafficMonitorUtil(mContext, mLastCurrTime, mCurrTime,
                    mSimIndex);
            // mengdw <2016-06-15> add for CR01718413 end
            trafficMonitor.updateDetailData();
            mMessage = trafficMonitor.getTrafficConsumptionApp();
            Log.d(TAG, "run mMessage=" + mMessage);
            if (!mMessage.isEmpty()) {
                sendMessage(MSG_OK);
            }
            trafficMonitor.closeSession();
        }

        private void sendMessage(int message) {
            Message m = new Message();
            m.what = message;
            mHandler.sendMessage(m);
        }
    };
    
    // mengdw <2016-03-23> add for CR01652041 begin
    private long[] getWeekTimeZone() {
        long[] timeZone = new long[3];
        int[] timeArray = null;
        String[] weekArray = TimeFormat.getWeekArray();
        String[] split = weekArray[0].split("-");
        try {
        timeArray = new int[] {Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                Integer.parseInt(split[2]), 0, 0, 0};
        if (timeArray != null) {
            timeZone[0] = TimeFormat.getStartTime(timeArray[0], timeArray[1], timeArray[2], 0, 0, 0);
            timeZone[1] = System.currentTimeMillis();
        }

        timeZone[2] = System.currentTimeMillis();
        } catch(Exception e) {
            Log.d(TAG, "getWeekTimeZone Exception e=" + e.toString());
        }
        return timeZone;
    }
    // mengdw <2016-03-23> add for CR01652041 end

    private void getTimeInfo() {
        MonitorJobSetting mJob = CommonUtil.mJobMap.get("net");
        int hour = mJob.getHour();
        int minute = mJob.getMinute();
        int second = mJob.getSecond();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mCurrTime = System.currentTimeMillis()
                - (hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000 + 1000);
        mLastCurrTime = mCurrTime - mCycleTime + 1000;
        mCurrDate = dateFormat.format(mCurrTime);
        mLastCurrDate = dateFormat.format(mLastCurrTime);
    }

    private void getNotificationInfo() {
        String[] lastDate = mLastCurrDate.split("-");
        mDateZone = Integer.valueOf(lastDate[1]) + "/" + Integer.valueOf(lastDate[2]);
        String[] currDate = mCurrDate.split("-");
        mDateZone += "~" + Integer.valueOf(currDate[1]) + "/" + Integer.valueOf(currDate[2]);
        mDateZone += "-" + mLastCurrTime + "-" + mCurrTime;

    }

    private boolean getSimState(Context context) {
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(context);
        mSimIndex = wrapper.getSimIndex_CurrentNetworkActivated();
        if (mSimIndex > 0) {
            return true;
        }

        int count = wrapper.getInsertedSimCount();
        if (count == 0) {
            return false;
        }

        mSimIndex = wrapper.getInsertedSimInfo().get(0).mSlot;
        return true;
    }

}
// Gionee <jianghuan> <2014-03-08> add for CR01111459 end