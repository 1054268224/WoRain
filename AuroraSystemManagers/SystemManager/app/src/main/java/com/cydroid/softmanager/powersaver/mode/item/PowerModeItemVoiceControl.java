package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import cyee.provider.CyeeSettings;

public class PowerModeItemVoiceControl extends PowerModeItem {
    public static final String SOUND_CTRL_PACKAGE_NAME = "gn.com.voice";

    private static final int CONFIGTYPE_CONTROL_SWITCH = 1;
    private static final int CONFIGTYPE_CALLING_CONTROL = 2;
    private static final int CONFIGTYPE_MESSAGE_CONTROL = 11;

    public PowerModeItemVoiceControl(Context context, String mode,
            PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    @Override
    public boolean isFunctionAvailable() {
        try {
            PackageManager pm = mContext.getPackageManager();
            pm.getInstallerPackageName(SOUND_CTRL_PACKAGE_NAME);
            Log.d(TAG, "packagename " + SOUND_CTRL_PACKAGE_NAME + " exist");
            return true;
        } catch (Exception e) {
            Log.d(TAG, "packagename " + SOUND_CTRL_PACKAGE_NAME + " not exist");
            return false;
        }
    }

    @Override
    public void save() {
        mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_CONTROL_SWITCH, getVoiceControlSwitch());
        mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_CALLING_CONTROL, getVoiceControlCalling());
        mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_MESSAGE_CONTROL, getVoiceControlMessage());
    }

    private boolean getVoiceControlSwitch() {
        if (!isFunctionAvailable()) {
            return false;
        }
        return CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_SWITCH,
                0) != 0;
    }

    private boolean getVoiceControlCalling() {
        if (!isFunctionAvailable()) {
            return false;
        }
        return CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_CALLING,
                0) != 0;
    }

    private boolean getVoiceControlMessage() {
        if (!isFunctionAvailable()) {
            return false;
        }
        return CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_MESSAGE,
                0) != 0;
    }

    @Override
    public boolean apply() {
        if (hasConfig()) {
            Log.d(TAG, "apply setting from key:" + mConfigKey);
            boolean conVal = mProviderHelper.getBoolean(mConfigKey, false);
            if (getVoiceControlSwitch() == conVal) {
                Log.d(TAG, "main control switch already " + conVal + "  ,return");
                return true;
            }
            setVoiceControlCalling(conVal);
            setVoiceControlMessage(conVal);
            setVoiceControlSwitch(conVal);
        }
        return true;
    }

    private void setVoiceControlCalling(boolean conVal) {
        if (!isFunctionAvailable()) {
            return;
        }
        if (conVal) {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_CALLING, 1);
        } else {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_CALLING, 0);
        }
    }

    private void setVoiceControlMessage(boolean conVal) {
        if (!isFunctionAvailable()) {
            return;
        }
        if (conVal) {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_MESSAGE, 1);
        } else {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_MESSAGE, 0);
        }
    }

    private void setVoiceControlSwitch(boolean conVal) {
        if (!isFunctionAvailable()) {
            return;
        }
        if (conVal) {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_SWITCH, 1);
        } else {
            CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SOUND_CONTROL_SWITCH, 0);
        }
    }

    @Override
    public boolean restore(boolean isForceRestore) {
        Log.d(TAG, "enter restore");
        boolean soundSwitch = getVoiceControlSwitch();
        if (soundSwitch == mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_CONTROL_SWITCH, false)) {
            Log.d(TAG, "main control switch already " + soundSwitch + "  ,return");
            return true;
        }
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_CONTROL_SWITCH)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_CONTROL_SWITCH))) {
            setVoiceControlSwitch(
                    mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_CONTROL_SWITCH, false));
        }
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_CALLING_CONTROL)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_CALLING_CONTROL))) {
            setVoiceControlCalling(
                    mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_CALLING_CONTROL, false));
        }
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_MESSAGE_CONTROL)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_MESSAGE_CONTROL))) {
            setVoiceControlMessage(
                    mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_MESSAGE_CONTROL, false));
        }
        try {
            Intent intent = new Intent("gn.com.voice.BootBroadCastReceiver");
            mContext.sendBroadcast(intent);
            Log.d(TAG, "send Broadcast gn.com.voice.BootBroadCastReceiver");
        } catch (Exception e) {
            Log.e(TAG, "send Broadcast gn.com.voice.BootBroadCastReceiver", e);
        }
        return true;
    }

    private boolean isCurrentSettingsChangedByExternal(int configtype) {
        if (!hasConfig()) {
            return true;
        }
        boolean conVal = mProviderHelper.getBoolean(mConfigKey, false);
        switch (configtype) {
            case CONFIGTYPE_CONTROL_SWITCH:
                return getVoiceControlSwitch() != conVal;
            case CONFIGTYPE_CALLING_CONTROL:
                return getVoiceControlCalling() != conVal;
            case CONFIGTYPE_MESSAGE_CONTROL:
                return getVoiceControlMessage() != conVal;
            default:
                return true;
        }
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return false;
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        return true;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
    }

}
