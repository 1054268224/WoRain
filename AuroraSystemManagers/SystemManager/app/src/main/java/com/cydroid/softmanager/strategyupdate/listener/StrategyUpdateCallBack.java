package com.cydroid.softmanager.strategyupdate.listener;

import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;

import android.content.Context;
/**
 * Created by mengjk on 17-5-16.
 */
public interface StrategyUpdateCallBack {

    void notifyUpdateStart(Context context);

    void notifyUpdateCompleted(Context context);

    void notifyStrategyParseCompleted(Context context, String type, IStrategy strategy);

    void notifyUpdateFail(Context context);
}
