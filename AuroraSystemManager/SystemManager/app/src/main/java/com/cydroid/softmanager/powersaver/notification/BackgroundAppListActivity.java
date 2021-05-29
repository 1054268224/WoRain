/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用列表界面
 *
 * Revised Date: 2017-02-05
 */
package com.cydroid.softmanager.powersaver.notification;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;

public class BackgroundAppListActivity extends BaseActivity {
    private static final String TAG = "BackgroundAppListActivity";
    private Bundle mBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.power_consume_background_app_layout);
        mBundle = getIntent().getExtras();
        Fragment backgroundAppListFragment = new BackgroundAppListFragment();
        backgroundAppListFragment.setArguments(mBundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.background_app_list_fragment, backgroundAppListFragment).commit();
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

}
