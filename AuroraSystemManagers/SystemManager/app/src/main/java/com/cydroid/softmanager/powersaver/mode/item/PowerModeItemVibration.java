package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;
import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;

import com.cydroid.softmanager.R;

public class PowerModeItemVibration extends PowerModeItem {

    private static final int CONFIGTYPE_PHONE = 1;
    private static final int CONFIGTYPE_ONTOUCH = 2;

    public PowerModeItemVibration(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private void setVibrationPhoneState(boolean enable) {
        Log.d(TAG, "StateController->setVibrationPhoneState enable " + enable);
        //add by zhaopeng for TEWBW-1899 20200716 start
        //CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.CYEE_VIBRATION_SWITCH,
        //        enable ? 1 : 0);
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING,
                enable ? 1 : 0);
        //add by zhaopeng for TEWBW-1899 20200716 end
    }

    private int getVibrationPhone() {
        //add by zhaopeng for TEWBW-1899 20200716 start
        //return CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.CYEE_VIBRATION_SWITCH, 1);
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 1);
        //add by zhaopeng for TEWBW-1899 20200716 end
    }

    private int getVibrationOnTouch() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1);
    }

    private void setVibrationPhone(int value) {
        //add by zhaopeng for TEWBW-1899 20200716 start
        //CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.CYEE_VIBRATION_SWITCH, value);
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, value);
        //add by zhaopeng for TEWBW-1899 20200716 end
    }

    private void setVibrationOnTouch(int value) {
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, value);
    }

    @Override
    public String getName() {
        return "Vibration";
    }

    @Override
    public void save() {
        int phoneVibration = getVibrationPhone();
        Log.d(TAG, "save current VibrationPhone " + phoneVibration + " setting to key:" + mCheckpointKey
                + CONFIGTYPE_PHONE);
        mProviderHelper.putInt(mCheckpointKey + CONFIGTYPE_PHONE, phoneVibration);
        int onTouchVibration = getVibrationOnTouch();
        Log.d(TAG, "save current VibrationOnTouch " + onTouchVibration + " setting to key:" + mCheckpointKey
                + CONFIGTYPE_ONTOUCH);
        mProviderHelper.putInt(mCheckpointKey + CONFIGTYPE_ONTOUCH, onTouchVibration);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        // override by save()
    }

    @Override
    public boolean restore(boolean isForceRestore) {

        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_ONTOUCH)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_ONTOUCH))) {
            Log.d(TAG, "restore setting from key:" + mCheckpointKey + CONFIGTYPE_ONTOUCH);
            setVibrationOnTouch(mProviderHelper.getInt(mCheckpointKey + CONFIGTYPE_ONTOUCH, 1));
        }
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_PHONE)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_PHONE))) {
            Log.d(TAG, "restore setting from key:" + mCheckpointKey + CONFIGTYPE_PHONE);
            setVibrationPhone(mProviderHelper.getInt(mCheckpointKey + CONFIGTYPE_PHONE, 1));
        }
        return true;
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        // override by restore()
        return true;
    }

    private boolean isCurrentSettingsChangedByExternal(int configtype) {
        if (!hasConfig()) {
            return true;
        }
        int conVal = mProviderHelper.getBoolean(mConfigKey, false) ? 1 : 0;
        switch (configtype) {
            case CONFIGTYPE_PHONE:
                return getVibrationPhone() != conVal;
            case CONFIGTYPE_ONTOUCH:
                return getVibrationOnTouch() != conVal;
            default:
                return true;
        }
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        // override by isCurrentSettingsChangedByExternal(int)
        return true;
    }

    @Override
    public boolean apply() {
        if (!hasConfig()) {
            Log.d(TAG, "no settings " + mConfigKey + " ,do nothing");
            return true;
        }
        Log.d(TAG, "apply setting from key:" + mConfigKey);
        int conVal = mProviderHelper.getBoolean(mConfigKey, false) ? 1 : 0;
        setVibrationPhone(conVal);
        setVibrationOnTouch(conVal);
        return true;
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("false");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.close_vibration);
    }

}