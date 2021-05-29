package com.example.systemmanageruidemo.actionpresent;

import com.example.systemmanageruidemo.actionview.ViewAction;

public interface PresentI <T extends ViewAction> {
    void setViewAction(T viewAvtion);
    T  getViewAction();
}
