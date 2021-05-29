package com.example.systemmanageruidemo.actionpresent;


import com.example.systemmanageruidemo.actionview.MainViewActionInterface;

public interface MainActionPresentInterface extends PresentI<MainViewActionInterface> {
    public void onstartScan();

    public void oncancelScan();

    public void StopScan();

    public int ChangeScore(int score);
}