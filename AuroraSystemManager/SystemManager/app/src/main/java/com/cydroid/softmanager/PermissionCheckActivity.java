/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.softmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PermissionCheckActivity extends BaseActivity {
    private static final String TAG = "PermissionCheckActivity";
    private static final String PACKAGE_URI_PREFIX = "package:";
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 2000;
    private static final long AUTOMATED_RESULT_THRESHOLD_MILLLIS = 250;

    private long mRequestTimeMillis;
    private TextView mExit;
    private TextView mNextView;
    private TextView mSettingsView;
    private TextView mEnablePermissionProcedure;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (redirectIfNeeded()) {
            return;
        }
        setContentView(R.layout.permission_check_activity);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (redirectIfNeeded()) {
            return;
        }
    }

    private boolean redirectIfNeeded() {
        if (RuntimePermissionsManager.hasNeedRequiredPermissions(this)) {
            return false;
        }

        redirect();
        return true;
    }

    private void redirect() {
        launcherMainActivity();
        finish();
    }

    private void initView() {
        mExit = (TextView) findViewById(R.id.exit);
        mExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                finish();
            }
        });

        mNextView = (TextView) findViewById(R.id.next);
        mNextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                tryRequestPermission();
            }
        });

        mSettingsView = (TextView) findViewById(R.id.settings);
        mSettingsView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                launcherPMSettingsActivity();
            }
        });

        mEnablePermissionProcedure = (TextView) findViewById(R.id.enable_permission_procedure);
    }

    private void tryRequestPermission() {
        if (redirectIfNeeded()) {
            return;
        }
        mRequestTimeMillis = SystemClock.elapsedRealtime();
        RuntimePermissionsManager.requestRequiredPermissions(this, REQUIRED_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        if (!isRequestPermissionsResult(requestCode)) {
            return;
        }

        if (RuntimePermissionsManager.hasDeniedPermissions(permissions, grantResults)) {
            if (isAutomatedDenied()) {
                mNextView.setVisibility(View.GONE);
                mSettingsView.setVisibility(View.VISIBLE);
                mEnablePermissionProcedure.setVisibility(View.VISIBLE);
            }
        } else {
            redirect();
        }
    }

    private boolean isRequestPermissionsResult(int requestCode) {
        return REQUIRED_PERMISSIONS_REQUEST_CODE == requestCode;
    }

    private void launcherMainActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void launcherPMSettingsActivity() {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse(PACKAGE_URI_PREFIX + getPackageName()));
        startActivity(intent);
    }

    private boolean isAutomatedDenied() {
        final long currentTimeMillis = SystemClock.elapsedRealtime();
        return (currentTimeMillis - mRequestTimeMillis) < AUTOMATED_RESULT_THRESHOLD_MILLLIS;
    }
}
