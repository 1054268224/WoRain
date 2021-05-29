package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.GNPushServiceHelper;
import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.gionee.push.sdk.SettingPush;
import com.cydroid.softmanager.R;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.UserHandle;

public class PowerModeItemGNPush extends PowerModeItem {
    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 begin
    private static final String CONFIGLIST_APPS = "_list";
    private static final String CONFIGTYPE_PUSH = "_push";
    private static final String CONFIGTYPE_APPPUSH = "_app_";

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end

    public PowerModeItemGNPush(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private GNPushServiceHelper getGNPushServiceHelper() {
        return GNPushServiceHelper.getInstance();
    }

    private boolean isSupportNewGNPush() {
        GNPushServiceHelper gNPushService = getGNPushServiceHelper();
        return gNPushService.getSettingPush() != null;
    }

    /* push controller */
    private void setPushStateOld(boolean enabled) {
        Log.d(TAG, "->setPushState, enabled = " + enabled);
        boolean isOn = isPushOn();
        if (enabled) {
            if (!isOn) {
                Intent intent = new Intent("gn.push.action.TURN_MAIN_SWITCH_ON");
                mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            }
        } else {
            if (isOn) {
                Intent intent = new Intent("gn.push.action.TURN_MAIN_SWITCH_OFF");
                mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            }
        }
    }

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 begin
    private void setPushStateNew(SettingPush settingPush, boolean enabled) {
        Log.d(TAG, "->setPushState(new), enabled = " + enabled);
        // boolean isOn = isPushOnNew(settingPush);
//        settingPush.setNotificationEnable(enabled);
        if (enabled) {
            settingPush.openAllPushSwitch();
        } else {
            settingPush.closeAllPushSwitch();
        }
    }

    private void setPushState(boolean enabled) {
        if (isSupportNewGNPush()) {
            setPushStateNew(getGNPushServiceHelper().getSettingPush(), enabled);
        } else {
            setPushStateOld(enabled);
        }
    }
    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end

    private boolean isPushOnOld() {
        boolean pushEnable = false;
        Uri pushUri = Uri.parse("content://com.cyee.settings.NotifyPushProvider/pushapp");
        Cursor cursor = mContext.getContentResolver().query(pushUri, new String[] {"switch"}, "package=?",
                new String[] {"notify_push_switch"}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                pushEnable = cursor.getInt(0) != 0;
            } else {
                pushEnable = false;
            }
            cursor.close();
        }
        cursor = null;
        Log.d(TAG, "->isPushOn = " + pushEnable);
        return pushEnable;
    }

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 begin
    private boolean isPushOnNew(SettingPush settingPush) {
        boolean pushEnable = false;
        pushEnable = settingPush.isNotificationEnable();
        Log.d(TAG, "->isPushOn(new) = " + pushEnable);
        return pushEnable;
    }

    private boolean isPushOn() {
        if (isSupportNewGNPush()) {
            return isPushOnNew(getGNPushServiceHelper().getSettingPush());
        } else {
            return isPushOnOld();
        }
    }

//    private boolean isAppPushOn(SettingPush settingPush, String pkgName) {
//        boolean pushEnable = false;
//        pushEnable = settingPush.isAppPushSwitchEnable(pkgName);
//        Log.d(TAG, "->isAppPushOnNew(new) = " + pushEnable + " pkg = " + pkgName);
//        return pushEnable;
//    }

//    private void setAppPushState(SettingPush settingPush, boolean enabled, String pkgName) {
//        Log.d(TAG, "->setAppPushStateNew(new), enabled = " + enabled + " pkg = " + pkgName);
//        // boolean isOn = isAppPushOn(settingPush, pkgName);
//        settingPush.setAppPushSwitchEnable(pkgName, enabled);
//    }
    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isPushOn());
    }

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 begin
    @Override
    public void save() {
        if (isSupportNewGNPush()) {
//            HashSet<String> pushSupportedApps = getGNPushServiceHelper().getSettingPushApps(mContext);
//            Log.d(TAG, "->save(new) apps.size=" + pushSupportedApps.size());
            mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_PUSH, isPushOn());

//            for (String pkgName : pushSupportedApps) {
//                mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_APPPUSH + pkgName,
//                        isAppPushOn(getGNPushServiceHelper().getSettingPush(), pkgName));
//            }
//            mProviderHelper.putStringSet(mCheckpointKey + CONFIGLIST_APPS, pushSupportedApps);
        } else {
            super.save();
        }
    }
    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        setPushState(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isPushOn() != configVal;
    }

    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 begin
    private boolean isCurrentSettingsChangedByExternalNew(SettingPush settingPush, String configtype,
            String pkgName) {
        if (!hasConfig()) {
            return true;
        }
        boolean conVal = mProviderHelper.getBoolean(mConfigKey, false);
        switch (configtype) {
            case CONFIGTYPE_PUSH:
                return isPushOnNew(settingPush) != conVal;
//            case CONFIGTYPE_APPPUSH:
//                return isAppPushOn(settingPush, pkgName) != conVal;
            default:
                return true;
        }
    }

    @Override
    public boolean restore(boolean isForceRestore) {
        if (isSupportNewGNPush()) {
            Log.d(TAG, "->restore(new)");
//            Set<String> pushSupportedApps = mProviderHelper.getStringSet(mCheckpointKey + CONFIGLIST_APPS,
//                    getGNPushServiceHelper().getSettingPushApps(mContext));
//            for (String pkgName : pushSupportedApps) {
//                if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_APPPUSH + pkgName) && (isForceRestore
//                        || !isCurrentSettingsChangedByExternalNew(getGNPushServiceHelper().getSettingPush(),
//                                CONFIGTYPE_APPPUSH, pkgName))) {
//                    setAppPushState(getGNPushServiceHelper().getSettingPush(),
//                            mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_APPPUSH + pkgName, false),
//                            pkgName);
//                }
//            }
//            mProviderHelper.removeKey(mCheckpointKey + CONFIGLIST_APPS);
            if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_PUSH) && (isForceRestore
                    || !isCurrentSettingsChangedByExternalNew(getGNPushServiceHelper().getSettingPush(),
                            CONFIGTYPE_PUSH, ""))) {
                setPushStateNew(getGNPushServiceHelper().getSettingPush(),
                        mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_PUSH, false));
            }
            return true;
        } else {
            return super.restore(isForceRestore);
        }
    }

    public boolean apply() {
        if (isSupportNewGNPush()) {
            Log.d(TAG, "->apply(new)");
            if (!hasConfig()) {
                Log.d(TAG + "/" + getName(), "no settings " + mConfigKey + " ,do nothing");
                return true;
            }
            Log.d(TAG + "/" + getName(), "apply setting from key:" + mConfigKey);
            boolean conVal = mProviderHelper.getBoolean(mConfigKey, false);
            setPushState(conVal);
//            Set<String> pushSupportedApps = mProviderHelper.getStringSet(mCheckpointKey + CONFIGLIST_APPS,
//                    getGNPushServiceHelper().getSettingPushApps(mContext));
//            for (String pkgName : pushSupportedApps) {
//                setAppPushState(getGNPushServiceHelper().getSettingPush(), conVal, pkgName);
//            }
            return true;
        } else {
            return super.apply();
        }
    }
    // Gionee <yangxinruo> <2016-6-14> add for CR01707701 end

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return -powerConfig.push_weight;
        } else {
            return 0f;
        }
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("false");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.close_push_notification);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isPushOn()));
    }
}
