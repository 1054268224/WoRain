package com.cydroid.softmanager.strategyupdate.strategy;
/**
 * Created by mengjk on 17-5-16.
 */
public interface ParseStrategyCallBack {
    void parseStart(String type);

    void parseSuccess(String type);

    void parseError(String type, String messasge);

    void parsing(String type, Object obj);
}
