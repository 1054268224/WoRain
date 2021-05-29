package com.example.systemmanageruidemo.actionpresent;

import com.example.systemmanageruidemo.actionview.BootSpeedView;
import com.example.systemmanageruidemo.bootspeed.adapter.BootItem;

import java.util.List;

public interface BootSpeedPresent extends PresentI<BootSpeedView> {


    void onRequestScore();

    void reSponseScore(int percent);

    void onInitData(List<BootItem> list);

    void onSelectListdo(List<BootItem> list);

    void refreshData(List<BootItem> list);
}
