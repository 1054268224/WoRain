package com.cydroid.softmanager.applock;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Toast;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.Log;

/**
 * Created by guoxt on 18-8-13.
 */

public class AppLockSettingsActivity extends BaseActivity {

    private final String TAG = "AppLockSettingsActivity";
    private boolean mAuthenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applock_settings_layout);
        mAuthenticated = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAuthenticated) {
            Log.d(TAG, "onResume startSecurityPassword");
            AppLockSecurityPasswordUtils.startSecurityPassword(this);
        } else {
            //chenyee zhaocaili 20180903 modify for CSW1705A-2849 begin
            mAuthenticated = false;
            //chenyee zhaocaili 20180903 modify for CSW1705A-2849 end
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //chenyee zhaocaili modify for CSW1705P-249 begin
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + ", resultCode:" + resultCode);
        Intent intent = new Intent();
        intent.putExtra("requestCode", requestCode);
        if (requestCode == AppLockSecurityPasswordUtils.REQUEST_CODE_CLEAR_PASSWORD){
            boolean clear = AppLockSecurityPasswordUtils.onActivityResult(this, requestCode, resultCode, data);
            if (clear){
                setResult(RESULT_OK, intent);
            }
            finish();
        }else if(requestCode == AppLockSecurityPasswordUtils.REQUEST_CODE_CONFIRM_PASSWORD){
            mAuthenticated = AppLockSecurityPasswordUtils.onActivityResult(this, requestCode, resultCode, data);
            Log.d(TAG, "onActivityResult mAuthenticated:" + mAuthenticated);
         /*guoxt modify for CSW1809A-455 begin*/
        }else if(requestCode == AppLockSecurityPasswordUtils.REQUEST_CODE_MODIFY_PASSWORD){
            if(resultCode == Activity.RESULT_CANCELED){
                mAuthenticated = true;
            }
        }
        /*guoxt modify for CSW1809A-455 end*/
        //chenyee zhaocaili modify for CSW1705P-249 end
    }
}
