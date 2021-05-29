/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2017-03-30
 */
package com.cydroid.softmanager.applock.verifier.viewcontrol;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;

import com.android.internal.widget.ILockSettings;
import com.cydroid.softmanager.utils.Log;

import cyee.provider.CyeeSettings;

public final class SecurityPwdUtils {
    private static final String TAG = "SecurityPwdUtils";
    private static final String SECURITY_PASSWORD_TYPE = "securitypassword_type";

    public static long getSecurityPasswordType(){
        ILockSettings lockSettingsService = ILockSettings.Stub.asInterface(
            ServiceManager.getService("lock_settings")); 
        
        long result = 0;
        try {
            result = lockSettingsService.getLong(SECURITY_PASSWORD_TYPE, 
                0, UserHandle.USER_OWNER);
        } catch (RemoteException e) {
            Log.e(TAG, "getSecurityPasswordType Couldn't get long " 
                + SECURITY_PASSWORD_TYPE + " " + e);
        }
        return result;
    }

    public static boolean useSecureKeyboardSupport(Context context) {
        return CyeeSettings.getInt(context.getContentResolver(),
            "security_keyboard_values", 0) == 1;
    }
}
