package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.provider.Settings;

public class PowerModeItemNotificationReceivers extends PowerModeItem {
    // 微信播报,日历等设置的全局通知receiver,冻结时会被系统清空
    public PowerModeItemNotificationReceivers(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private String getNotificationReceivers() {
        String val = Settings.Secure.getString(mContext.getContentResolver(),
                "enabled_notification_listeners");
        if (val == null) {
            val = "";
        }
        return val;
    }

    private void setNotificationReceivers(String val) {
        Log.d(TAG, "restoreSystemNotificationReceivers --> set to " + val);
        Settings.Secure.putString(mContext.getContentResolver(), "enabled_notification_listeners", val);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putString(saveKey, getNotificationReceivers());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        setNotificationReceivers(mProviderHelper.getString(saveKey, ""));
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return false;
    }

}
