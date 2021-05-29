/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 数据采集器接口
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.powersaver.analysis.collector;

import java.util.List;
import java.util.Map;

public interface IAnalysisDataCollector {

    void init();

    void deinit();

    void setNewDataListener(INewDataListener newDataListener);

    List<Map<String, Object>> getAndFlushNewDataSet();

    interface INewDataListener {
        void onNewData(IAnalysisDataCollector collector);
    }
}
