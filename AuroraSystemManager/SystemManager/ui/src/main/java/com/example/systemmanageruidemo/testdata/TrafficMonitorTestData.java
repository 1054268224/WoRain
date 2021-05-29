package com.example.systemmanageruidemo.testdata;

import android.content.Context;
import android.widget.Toast;

import com.example.systemmanageruidemo.R;
import com.example.systemmanageruidemo.actionpresent.TrafficMonitorPresent;
import com.example.systemmanageruidemo.actionview.TrafficMonitorView;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraPagerBean;
import com.example.systemmanageruidemo.trafficmonitor.bean.TraRecyBean;

import java.util.ArrayList;
import java.util.List;

public class TrafficMonitorTestData implements TrafficMonitorPresent<TrafficMonitorView> {
    TrafficMonitorView view;
    private TraPagerBean data;
    private List<TraRecyBean> recyBeans;
    private TraRecyBean recyData;

    public TrafficMonitorTestData(TrafficMonitorView view) {
        this.view = view;
    }

    @Override
    public void onRequestSIM(TraPagerBean object) {
        data = object;
        setTraPagerData();
    }

    private void setTraPagerData(){
        data.setList(new ArrayList<>());

        int count = 2 ;

        data.setSimCardScount(count);
        for (int i = 0; i < count ; i++) {
            TraPagerBean.SIMBean tra = new TraPagerBean.SIMBean(i, "中国联通");

            tra.setSurplusFlow(111111);
            tra.setUsedFlow(111);
            tra.setTraPack(1111);

            data.getList().add(tra);
        }

        responseSIM(data);
    }


    @Override
    public void responseSIM(TraPagerBean object) {
        view.onResponseSIM(object);

    }

    @Override
    public void onInitData(List<TraRecyBean> list) {
           this.recyBeans = list;
        for (int i = 0; i < 10; i++) {
            recyData = new TraRecyBean("ss", "软件" + i);
            recyData.setUsedTraSize(i * 100);
            recyBeans.add(recyData);
        }
    }

    @Override
    public void refresh(List<TraRecyBean> list) {
        view.onRefresh(recyBeans);
    }

    @Override
    public void onAppChangeState(TraRecyBean object, boolean ischecked) {

    }

    @Override
    public void setViewAction(TrafficMonitorView viewAvtion) {
        view = viewAvtion;

    }

    @Override
    public TrafficMonitorView getViewAction() {
        return view;
    }
}
