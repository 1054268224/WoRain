package com.example.systemmanageruidemo.actionview;


import com.example.systemmanageruidemo.actionpresent.MainActionPresentInterface;

public interface MainViewActionInterface extends ViewAction<MainActionPresentInterface> {
    public void startScan();

    public void cancelScan();

    public void onStopScan();

    /**
     *
     * @param score   分数加减的值
     * @param isset  是否直接设置分数
     * @return   返回当前分数
     */
    public int onChangeScore(int score, boolean isset);

}