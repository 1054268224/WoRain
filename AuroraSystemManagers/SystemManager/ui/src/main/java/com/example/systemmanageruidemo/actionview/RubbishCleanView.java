package com.example.systemmanageruidemo.actionview;

import com.example.systemmanageruidemo.actionpresent.RubbishCleanPresent;
import com.example.systemmanageruidemo.bean.DataBean;

import java.util.List;

public interface RubbishCleanView  extends ViewAction<RubbishCleanPresent>{
    void initData(List<DataBean> list);
    void onFiniesScan();
    void onRefresh(List<DataBean> list);
    void cleanRubbish(List<DataBean> list);

    /**
     *
     * @param dataBean
     * @param index  父元素的位置
     * @param dataBeanChild
     */
    void onFinditem(DataBean dataBean, int index, DataBean.DataBeanChild dataBeanChild);

    void onDeleteitem(DataBean dataBean, int index, DataBean.DataBeanChild dataBeanChild);
}
