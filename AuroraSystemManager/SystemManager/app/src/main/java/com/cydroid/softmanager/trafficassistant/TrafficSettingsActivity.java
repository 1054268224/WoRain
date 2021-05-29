package com.cydroid.softmanager.trafficassistant;

import android.content.Intent;
import android.os.Bundle;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;

public class TrafficSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trafficassistant_settings_layout);
    }

}
