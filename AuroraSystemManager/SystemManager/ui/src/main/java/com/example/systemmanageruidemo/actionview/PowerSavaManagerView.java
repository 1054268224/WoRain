package com.example.systemmanageruidemo.actionview;

import com.example.systemmanageruidemo.actionpresent.PowerSavaManagerPresent;

public interface PowerSavaManagerView extends ViewAction<PowerSavaManagerPresent> {
    void requestPower();

    void onResponse(int percent, String hint, int strategy);

    void changeStrategy(int strategy);

}
