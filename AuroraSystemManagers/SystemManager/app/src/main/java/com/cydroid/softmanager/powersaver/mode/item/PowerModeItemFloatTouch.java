package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;

import cyee.provider.CyeeSettings;

public class PowerModeItemFloatTouch extends PowerModeItem {

    public PowerModeItemFloatTouch(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private boolean isFloatTouchOn() {
        int value = CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0);
        return value == 1;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isFloatTouchOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        setFloatTouch(mProviderHelper.getBoolean(saveKey, true));
        return true;
    }

    private void setFloatTouch(boolean configVal) {
        if (isFloatTouchOn() == configVal) {
            return;
        }
        if (configVal) {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 1);
            Intent intent = new Intent("com.cydroid.floatingtouch.action.START_SERVICE");
            intent.putExtra("from_softmanager", true);
            /*guoxt modify for SW17W16A-1476 begin */
            intent.setComponent(new ComponentName("com.cydroid.floatingtouch",
                    "com.cydroid.floatingtouch.service.TouchReceiver"));
            /*guoxt modify for SW17W16A-1476 end */
            mContext.sendBroadcast(intent);
            Log.d(TAG, "send intent START FloatTouch SERVICE");
        } else {
            Intent intent = new Intent("com.cydroid.floatingtouch.action.STOP_SERVICE");
            intent.putExtra("from_softmanager", true);
            intent.setComponent(new ComponentName("com.cydroid.floatingtouch",
                    "com.cydroid.floatingtouch.service.TouchReceiver"));
            mContext.sendBroadcast(intent);
            Log.d(TAG, "send intent STOP_SERVICE");
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SUSPEND_BUTTON, 0);
        }
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, true);
        return isFloatTouchOn() != configVal;
    }

}
