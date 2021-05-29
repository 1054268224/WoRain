/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.applock;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.cydroid.softmanager.utils.Log;

public class AppLockSecurityPasswordUtils {
    //private static final int REQUEST_CODE_CONFIRM_PASSWORD = 10;
    private static final String APP_EXTRA_DATA = "app_extra_data";

    private static final int CONFIRM_PASSWORD_FAIL = -1;
    private static final int CONFIRM_PASSWORD_SUCCESS = 0;
    private static final int CONFIRM_PASSWORD_CANCEL = 1;
    private static final String EXTRA_KEY_CONFIRM_RESULT = "confirm_result";

    private static final String ACTION_NAME = "security_lock_action";
    private static final String MODE_NAME = "security_lock_mode";
    //chenyee zhaocaili modify for CSW1705P-249 begin
    public static final int REQUEST_CODE_CONFIRM_PASSWORD = 5;
    public static final int REQUEST_CODE_MODIFY_PASSWORD = 6;
    public static final int REQUEST_CODE_CLEAR_PASSWORD = 7;
    //chenyee zhaocaili modify for CSW1705P-249 end
    private static final int SECURITY_MODE_TYPE = 1;


    /*
    public static boolean isSecurityPasswordEnable(Context context){
        LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
        if (lockPatternUtils.savedPasswordExists()) {
            return true;
        } else if (lockPatternUtils.savedPatternExists()
                && !lockPatternUtils.checkPattern(null)) {
            return true;
        }
        return false;
    }
    */

    public static void startSecurityPassword(Activity activity) {
        //Intent intent = new Intent("gionee.intent.action.CONFIRM_PASSWORD");
       // activity.startActivityForResult(intent, REQUEST_CODE_CONFIRM_PASSWORD);

        Intent intent = new Intent("cyee.intent.action.CONFIRM_PASSWORD");
        intent.putExtra(MODE_NAME, SECURITY_MODE_TYPE);
        activity.startActivityForResult(intent, REQUEST_CODE_CONFIRM_PASSWORD);
    }

    public static void startClearSecurityPassword(Activity activity) {
        Intent intent = new Intent("cyee.intent.action.CONFIRM_PASSWORD");
        intent.putExtra(ACTION_NAME, "action_clear");
        intent.putExtra(MODE_NAME, SECURITY_MODE_TYPE);
        activity.startActivityForResult(intent, REQUEST_CODE_CLEAR_PASSWORD);
    }

    public static void startChangeSecurityPassword(Activity activity) {
        Intent intent = new Intent("cyee.intent.action.CONFIRM_PASSWORD");
        intent.putExtra(ACTION_NAME, "action_modify");
        intent.putExtra(MODE_NAME, SECURITY_MODE_TYPE);
        activity.startActivityForResult(intent, REQUEST_CODE_MODIFY_PASSWORD);
    }





//    public static boolean onActivityResult(Activity activity, int requestCode,
//            int resultCode, Intent data) {
//        Log.d("AppLockSecurityPasswordUtils", "onActivityResult resultCode:" + resultCode);
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            int type = data.getIntExtra(EXTRA_KEY_CONFIRM_RESULT, CONFIRM_PASSWORD_FAIL);
//            Log.d("AppLockSecurityPasswordUtils", "onActivityResult type:" + type);
//            if (type == CONFIRM_PASSWORD_SUCCESS) {
//                return true;
//            }
//        }
//        activity.finish();
//        return false;
//    }

    public static boolean onActivityResult(AppCompatActivity activity, int requestCode, int resultCode, Intent data) {
        //chenyee zhaocaili modify for CSW1705P-249 begin
        Log.d("AppLockSecurityPasswordUtils", "onActivityResult requestCode:" + requestCode + ",   resultCode:" + resultCode);
        if (requestCode == REQUEST_CODE_CONFIRM_PASSWORD){
            if (resultCode == AppCompatActivity.RESULT_OK) {
                return true;
            }
            activity.finish();
            return false;
        }else {
            return resultCode == AppCompatActivity.RESULT_OK;
        }
        //chenyee zhaocaili modify for CSW1705P-249 end
    }
}
