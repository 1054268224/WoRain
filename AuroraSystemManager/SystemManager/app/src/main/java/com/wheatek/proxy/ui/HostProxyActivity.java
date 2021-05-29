/*
 * Copyright (C) 2014 singwhatiwanna(任玉刚) <singwhatiwanna@gmail.com>
 *
 * collaborator:田啸,宋思宇,Mr.Simple
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wheatek.proxy.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;

import androidx.appcompat.app.AppCompatActivity;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.powersaver.activities.PowerManagerMainActivity;
import com.cydroid.softmanager.softmanager.SoftManagerActivity;
import com.cydroid.softmanager.systemcheck.SystemCheckActivity;
import com.example.systemmanageruidemo.BaseSupportProxyActivity;
import com.example.systemmanageruidemo.actionpresent.PresentI;
import com.example.systemmanageruidemo.actionview.ViewAction;


public abstract class HostProxyActivity<D extends ViewAction> extends AppCompatActivity implements PresentI<D> {

    protected BaseSupportProxyActivity mRemoteActivity;

    public BaseSupportProxyActivity getmRemoteActivity() {
        return mRemoteActivity;
    }

    public void setmRemoteActivity(BaseSupportProxyActivity mRemoteActivity) {
        this.mRemoteActivity = mRemoteActivity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteActivity.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.host_bar_bg_blue)));
        getSupportActionBar().setElevation(0.0f);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.app_manager);

    }

    public void attach(BaseSupportProxyActivity remoteActivity) {
        mRemoteActivity = remoteActivity;
        remoteActivity.attach(this);
        mRemoteActivity.setPresenter(this);
        setViewAction((D) mRemoteActivity);
        mRemoteActivity.interpolator = new BaseSupportProxyActivity.ActivityNameInterpolator() {
            @Override
            public boolean isfif(String name) {
                if (name.contains("PowerSaveManagerMainActivity")) return true;
                if (name.contains("SoftManagerMainActivity")) return true;
//                if (name.contains("RubbishCleanerMainActivity")) return true;
//                if (name.contains("TrafficMonitorMainActivity")) return true;
                if (name.contains("OptimiseActivity")) return true;
                return false;
            }

            @Override
            public String fifname(String name) {
                if (name.contains("PowerSaveManagerMainActivity"))
                    return PowerManagerMainActivity.class.getName();
                if (name.contains("SoftManagerMainActivity"))
                    return SoftManagerActivity.class.getName();
//                if (name.contains("RubbishCleanerMainActivity"))
//                    return RubbishCleanerMainActivity.class.getName();
//                if (name.contains("TrafficMonitorMainActivity"))
//                    return TrafficAssistantMainActivity.class.getName();
                if (name.contains("OptimiseActivity"))
                    return SystemCheckActivity.class.getName();
                return "";
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mRemoteActivity.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        mRemoteActivity.onStart();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        mRemoteActivity.onRestart();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        mRemoteActivity.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mRemoteActivity.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mRemoteActivity.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mRemoteActivity.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mRemoteActivity.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mRemoteActivity.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mRemoteActivity.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onWindowAttributesChanged(LayoutParams params) {
        mRemoteActivity.onWindowAttributesChanged(params);
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mRemoteActivity.onWindowFocusChanged(hasFocus);
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mRemoteActivity.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        mRemoteActivity.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

}
