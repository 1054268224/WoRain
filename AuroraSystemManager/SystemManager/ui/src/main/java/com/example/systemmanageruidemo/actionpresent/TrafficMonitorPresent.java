package com.example.systemmanageruidemo.actionpresent;

import com.example.systemmanageruidemo.actionview.TrafficMonitorView;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraPagerBean;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraRecyBean;

import java.util.List;

public interface TrafficMonitorPresent extends PresentI<TrafficMonitorView> {
    void onRequestSIM(TraPagerBean object);

    void responseSIM(TraPagerBean object);

    void onInitData(List<TraRecyBean> list);

    void refresh(List<TraRecyBean> list);

    void onAppChangeState(TraRecyBean object, boolean ischecked);

}
