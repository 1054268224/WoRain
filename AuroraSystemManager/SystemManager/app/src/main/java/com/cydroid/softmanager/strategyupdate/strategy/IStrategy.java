package com.cydroid.softmanager.strategyupdate.strategy;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by mengjk on 17-5-16.
 */
public interface IStrategy {
    void parseJson(Context context, String json, ParseStrategyCallBack callBack);

    void setUpdateSuccess(Context context);

    boolean isNotify();

    String getType();

    int getVersion(Context context);

    JSONObject getStrategyRequestBody(Context context);

}
