// Gionee <liuyb> <2014-2-25> add for CR01083582 begin

package com.cydroid.softmanager.monitor.interfaces;

import com.cydroid.softmanager.monitor.utils.CommonUtil;

import android.content.Context;

public interface IMonitorJob {
    int mExecHour = CommonUtil.DEFAULT_EXEC_TIME_HOUR;
    int mExecMinutes = CommonUtil.DEFAULT_EXEC_TIME_MINUTES;

    void setExecTime(int hour, int minutes);

    void execute(Context context);
}
// Gionee <liuyb> <2014-2-25> add for CR01083582 end