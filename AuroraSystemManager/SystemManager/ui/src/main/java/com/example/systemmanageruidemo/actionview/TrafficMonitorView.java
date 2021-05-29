package com.example.systemmanageruidemo.actionview;

import com.example.systemmanageruidemo.actionpresent.TrafficMonitorPresent;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraPagerBean;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraRecyBean;

import java.util.List;

public interface TrafficMonitorView<T extends  TrafficMonitorPresent>  extends ViewAction<T> {
    void requestSIM(TraPagerBean object);

    void onResponseSIM(TraPagerBean object);

    void initData(List<TraRecyBean> list);

    void onRefresh(List<TraRecyBean> list);

    void appChangeState(TraRecyBean object,boolean ischecked);
}
