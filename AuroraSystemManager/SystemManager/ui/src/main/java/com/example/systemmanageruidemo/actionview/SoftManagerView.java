package com.example.systemmanageruidemo.actionview;

import com.example.systemmanageruidemo.actionpresent.SoftManagerPresent;
import com.example.systemmanageruidemo.softmanager.adapter.SoftItem;

import java.util.List;

public interface SoftManagerView extends ViewAction<SoftManagerPresent> {

    void initData(List<SoftItem> list);

    void selectListdo(List<SoftItem> list);

    void onRefresh(List<SoftItem> list);

    /**
     *  某个软件卸载完毕
     * @param packagename
     */
    void onFinishPosition(boolean success,String packagename);
}
