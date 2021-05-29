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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.cydroid.systemmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class RuntimePermissionsManager {
    private static final String TAG = "RuntimePermissionsManager";
    private static final List<String> REQUIRED_PERMISSIONS = new ArrayList<>();
    static {
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_PHONE_STATE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		//guoxt modify for CR01775652 begin	
		REQUIRED_PERMISSIONS.add(Manifest.permission.READ_SMS);
		//guoxt modify for CR01775652 end	
		
    }

    public static void redirectToPermissionCheck(final AppCompatActivity activity) {
        launcherPermissionCheckActivity(activity);
    }
    
    public static boolean isBuildSysNeedRequiredPermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasNeedRequiredPermissions(AppCompatActivity activity) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (activity.checkCallingOrSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private static void launcherPermissionCheckActivity(Context context) {
    	//fengpeipei modify for 48303 start
    	String contextString = context.toString();
    	String activityname = contextString.substring(contextString.lastIndexOf(".") + 1, contextString.indexOf("@"));
        final Intent intent = new Intent(context, PermissionCheckActivity.class);
        intent.putExtra("preactivity", activityname);
        //fengpeipei modify for 48303 end
        context.startActivity(intent);
    }

    public static void requestRequiredPermissions(AppCompatActivity activity, int resultCode) {
        List<String> requiredPermissions = getNoGrantedPermissions(activity);
        if (requiredPermissions.isEmpty()) {
            return;
        }
        requestPermissions(activity, requiredPermissions, resultCode);
    }

    private static List<String> getNoGrantedPermissions(AppCompatActivity activity) {
        List<String> noGrantedPermissions = new ArrayList<String>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (activity.checkCallingOrSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                noGrantedPermissions.add(permission);
            }
        }
        return noGrantedPermissions;
    }

    private static void requestPermissions(AppCompatActivity activity,
                                           List<String> requiredPermissions, int resultCode) {
        String[] permissions = requiredPermissions.toArray(new String[requiredPermissions.size()]);
        activity.requestPermissions(permissions, resultCode);
    }

    public static boolean hasDeniedPermissions(String[] permissions,
            int[] grantResults) {
        for (int i = 0; i < grantResults.length; ++i) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "hasDeniedPermissions permission:" + permissions[i]
                    + ", grantResult:" + grantResults[i]);
                return true;
            }
        }
        return false;
    }
}
