package com.example.systemmanageruidemo.actionpresent;

import com.example.systemmanageruidemo.actionview.PowerSavaManagerView;

public interface PowerSavaManagerPresent extends PresentI<PowerSavaManagerView> {
    void onRequestPower();

    void response(int percent, String hint, int strategy);

    void onChangeStrategy(int strategy);
}
