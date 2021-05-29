/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 卡顿跳帧相关数据收集器
 *
 * Date: 2017-04-06
 */
package com.cydroid.softmanager.skipframes;

public interface ISystemInfoCollector {

    void record(SkipFramesInfo skipinfoData);

}
