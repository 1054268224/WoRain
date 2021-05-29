package com.wheatek.proxy.ui;

import com.example.systemmanageruidemo.actionpresent.PowerSavaManagerPresent;
import com.example.systemmanageruidemo.actionview.PowerSavaManagerView;
import com.example.systemmanageruidemo.powersavemanager.PowerSaveManagerMainActivity;

public class HostPowerSaveManagerMainActivity extends HostProxyActivity<PowerSavaManagerView> implements PowerSavaManagerPresent {
    {
        attach(new PowerSaveManagerMainActivity());
    }

    PowerSavaManagerView viewAvtion;

    @Override
    public void setViewAction(PowerSavaManagerView viewAvtion) {
        this.viewAvtion = viewAvtion;
    }

    @Override
    public PowerSavaManagerView getViewAction() {
        return viewAvtion;
    }

    @Override
    public void onRequestPower() {

    }

    @Override
    public void response(int percent, String hint, int strategy) {
        viewAvtion.onResponse(percent, hint, strategy);
    }

    @Override
    public void onChangeStrategy(int strategy) {

    }
}