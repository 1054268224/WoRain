package com.cydroid.softmanager.strategyupdate.listener;

import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.content.Intent;
/**
 * Created by mengjk on 17-5-16.
 */
public class AutoBootStrategyCompletedCallBack implements StrategyUpdateCallBack {

    private static final String TAG = AutoBootStrategyCompletedCallBack.class.getSimpleName();

    @Override
    public void notifyUpdateStart(Context context) {
    }

    @Override
    public void notifyUpdateCompleted(final Context context) {
        Log.d(TAG, "notifyUpdateCompleted");
        Intent intent = new Intent("com.chenyee.intent.action.UPDATE_BOOT_APP_CONDITION");
        context.sendBroadcast(intent);
    }

    @Override
    public void notifyStrategyParseCompleted(Context context, String type, IStrategy strategy) {
    }

    @Override
    public void notifyUpdateFail(Context context) {
    }

}
