package com.cydroid.softmanager.strategyupdate.request;

public interface StrategyUpdateHttpRequestCallBack {
    void onRequestSuccess(String data);

    void onRequestFail(int code, String msg);
}
