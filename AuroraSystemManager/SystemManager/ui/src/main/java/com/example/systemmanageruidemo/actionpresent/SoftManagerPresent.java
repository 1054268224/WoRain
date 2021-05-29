package com.example.systemmanageruidemo.actionpresent;

import com.example.systemmanageruidemo.actionview.SoftManagerView;
import com.example.systemmanageruidemo.softmanager.adapter.SoftItem;

import java.util.List;

public interface SoftManagerPresent  extends PresentI<SoftManagerView>{
    void onInitData(List<SoftItem> list);

    void onSelectListdo(List<SoftItem> list);

    void refreshData(List<SoftItem> list);

    void finishPosition(boolean success,String packagename);
}
