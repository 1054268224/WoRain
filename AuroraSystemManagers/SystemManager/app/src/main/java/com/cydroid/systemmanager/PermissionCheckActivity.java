/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-25
 */
package com.cydroid.systemmanager;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cydroid.softmanager.R;

import cyee.changecolors.ChameleonColorManager;

public class PermissionCheckActivity extends BaseActivity {
    private static final String TAG = "PermissionCheckActivity";
    private static final String PACKAGE_URI_PREFIX = "package:";
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 2000;
    private static final long AUTOMATED_RESULT_THRESHOLD_MILLLIS = 250;

    private long mRequestTimeMillis;
    private TextView mExit;
    private TextView mRequiredPpermissions;
    private TextView mNextView;
    private TextView mSettingsView;
    private TextView mEnablePermissionProcedure;
    private String mPreActivity = null; //fengpeipei add for 48303

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fengpeipei add for 48303 start
        Intent intent = getIntent();
        if(intent != null){
            mPreActivity = getIntent().getStringExtra("preactivity");
        }
        //fengpeipei add for 48303 end
        if (redirectIfNeeded()) {
            return;
        }
        setContentView(R.layout.permission_check_activity);
        initView();
        /*guoxt modify for CR01634797 begin*/
        chameleonColorProcess();
        /*guoxt modify for CR01634797 begin*/
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
    	//fengpeipei modify for 48303 start
    	if(mPreActivity != null){
    		if(mPreActivity.equals("RubbishCleanerMainActivity")){
    			launcherActivity("com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerMainActivity");
    		}
    		if(mPreActivity.equals("AntivirusActivity")){
    			launcherActivity("com.cydroid.systemmanager.antivirus.AntivirusActivity");
    		}
    	}else{
            launcherMainActivity();
    	}
    	//fengpeipei modify for 48303 end
        finish();
    }

    private void initView() {
        mExit = (TextView) findViewById(R.id.exit);
        /*guoxt modify for CR01634797 begin*/        
        mRequiredPpermissions = (TextView) findViewById(R.id.required_permissions);
        /*guoxt modify for CR01634797 end*/
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

        /*guoxt modify for CR01634797 begin*/
        private void chameleonColorProcess() {
            if (ChameleonColorManager.isNeedChangeColor()) {
            int color_C2 = ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2();
            mRequiredPpermissions.setTextColor(color_C2);
            mExit.setTextColor(color_C2);
            mNextView.setTextColor(color_C2);
        }
    }
    /*guoxt modify for CR01634797 end*/

    private void tryRequestPermission() {
        if (redirectIfNeeded()) {
            return;
        }
        mRequestTimeMillis = SystemClock.elapsedRealtime();
        RuntimePermissionsManager.requestRequiredPermissions(this, REQUIRED_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (!isRequestPermissionsResult(requestCode)) {
            return;
        }

        if (RuntimePermissionsManager.hasDeniedPermissions(permissions, grantResults)) {
            final long currentTimeMillis = SystemClock.elapsedRealtime();
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
		   ComponentName cmp = new ComponentName("com.cydroid.softmanager", 
            "com.cydroid.softmanager.MainActivity");
        Intent intent = new Intent();
        intent.setComponent(cmp);
        startActivity(intent);
    }
    
    //fengpeipei add for 48303 start
    private void launcherActivity(String act) {
        ComponentName cmp = new ComponentName(getPackageName(), act);
        Intent intent = new Intent();
        intent.setComponent(cmp);
        startActivity(intent);
    }
    //fengpeipei add for 48303 end
    
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
