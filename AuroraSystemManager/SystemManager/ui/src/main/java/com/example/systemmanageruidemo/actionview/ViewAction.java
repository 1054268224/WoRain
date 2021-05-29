package com.example.systemmanageruidemo.actionview;

import com.example.systemmanageruidemo.actionpresent.PresentI;

public interface ViewAction<T extends PresentI> {
    public abstract  void setPresenter(T presenter);
    public abstract  T getPresenter(T presenter);
}
