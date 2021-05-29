package com.cydroid.softmanager.applock.verifier.auth.pwd;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.internal.widget.ILockSettings;
import com.android.internal.widget.LockPatternUtils;

import com.cydroid.softmanager.utils.Log;

public class GnLockPatternUtils extends LockPatternUtils {
    private static final String TAG = "GnLockPatternUtils";
    private static final int sCurrentUserId = UserHandle.USER_NULL;

    private ILockSettings mGnLockSettingsService;

    public GnLockPatternUtils(Context context) {
         super(context);
    }

    private long gnGetLong(
            String secureSettingKey, long defaultValue, int userHandle) {
        try {
            return getGnLockSettings()
                .getLong(secureSettingKey, defaultValue, userHandle);
        } catch (RemoteException re) {
            return defaultValue;
        }
    }

    public long gnGetLong(String secureSettingKey, long defaultValue) {
        try {
            return getGnLockSettings().getLong(secureSettingKey, 
                defaultValue, getCurrentOrCallingUserIdForSPwd());
        } catch (RemoteException re) {
            return defaultValue;
        }
    }

    private ILockSettings getGnLockSettings() {
        if (mGnLockSettingsService == null) {
            mGnLockSettingsService = ILockSettings.Stub.asInterface(
                ServiceManager.getService("lock_settings")); 
        }
        return mGnLockSettingsService;
    }
    
    public void gnSetLong(String secureSettingKey, long value) {
        gnSetLong(secureSettingKey, value, getCurrentOrCallingUserIdForSPwd());
    }

    private void gnSetLong(String secureSettingKey, long value, int userHandle) {
        try {
            getGnLockSettings().setLong(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            // What can we do?
            Log.e(TAG, "Couldn't write long " + secureSettingKey + re);
        }
    }

    public String gnGetString(String secureSettingKey) {
        return gnGetString(secureSettingKey, getCurrentOrCallingUserIdForSPwd());
    }

    private String gnGetString(String secureSettingKey, int userHandle) {
        try {
            return getGnLockSettings().getString(secureSettingKey, null, userHandle);
        } catch (RemoteException re) {
            return null;
        }
    }

    public void gnSetString(String secureSettingKey, String value) {
        gnSetString(secureSettingKey, value, getCurrentOrCallingUserIdForSPwd());
    }

    private void gnSetString(String secureSettingKey, String value, int userHandle) {
        try {
            getGnLockSettings().setString(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            // What can we do?
            Log.e(TAG, "Couldn't write string " + secureSettingKey + re);
        }
    }

    /* xionghg delete for 8.0 build_env begin
    public void gnSetLockPassword(String password,String savedPassword) {
        try {
            getGnLockSettings().setLockPassword(
                password, savedPassword, getCurrentOrCallingUserIdForSPwd());
        } catch (RemoteException re) {
            Log.e(TAG, "gnSetLockPassword re=" + re);
        }
    }

    public void gnSetLockPattern(String pattern,String savedPattern) {
        try {
            getGnLockSettings().setLockPattern(
                pattern, savedPattern, getCurrentOrCallingUserIdForSPwd());
        } catch (RemoteException re) {
            Log.e(TAG, "gnSetLockPattern re=" + re);
        }
    }
    * xionghg delete for 8.0 build_env end
    */

    private int getCurrentOrCallingUserIdForSPwd() {
        return UserHandle.getCallingUserId();
    }

    public void gnSetBoolean(String secureSettingKey, boolean value) {
        gnSetBoolean(secureSettingKey, value, getCurrentOrCallingUserIdForSPwd());
    }

    public void gnSetBoolean(String secureSettingKey, boolean value, int userHandle) {
        try {
            getGnLockSettings().setBoolean(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write string " + secureSettingKey + re);
        }
    }
    
    public  boolean gnSavedPatternExists( int userId){
        try {
            return getGnLockSettings().hasSecureLockScreen();//getGnLockSettings().havePattern(userId);
        } catch (RemoteException re) {
            return false;
        }
    }
    
    public  boolean gnSavedPasswordExists( int userId){
        try {
            return getGnLockSettings().hasSecureLockScreen();//getGnLockSettings().havePassword(userId);
        } catch (RemoteException re) {
            return false;
        }
    }

    public void setLockPatternEnabled(boolean enabled) {
        gnSetBoolean(Settings.Secure.LOCK_PATTERN_ENABLED, enabled);
    }
}
