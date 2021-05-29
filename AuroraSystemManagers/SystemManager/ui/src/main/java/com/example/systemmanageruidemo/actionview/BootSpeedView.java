package com.example.systemmanageruidemo.actionview;

import com.example.systemmanageruidemo.actionpresent.BootSpeedPresent;
import com.example.systemmanageruidemo.bootspeed.adapter.BootItem;

import java.util.List;

public interface BootSpeedView extends ViewAction<BootSpeedPresent> {

    void requestScore();

    void onResponseScore(int percent);

    void initData(List<BootItem> list);

    void selectListdo(List<BootItem> list);

    void onRefresh(List<BootItem> list);

}
