package com.example.systemmanageruidemo.actionpresent;

import com.example.systemmanageruidemo.actionview.RubbishCleanView;
import com.example.systemmanageruidemo.bean.DataBean;

import java.util.List;

public interface RubbishCleanPresent extends PresentI<RubbishCleanView> {
    void onInitData(List<DataBean> list);

    void refresh(List<DataBean> list);

    void onCleanRubbish(List<DataBean> list);

}
