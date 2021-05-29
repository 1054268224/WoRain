package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;

public class PowerModeItemBluetooth extends PowerModeItem {

    public PowerModeItemBluetooth(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    public PowerModeItemBluetooth(Context context) {
        super(context);
    }
    public boolean isBluetoothOn() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        int status = btAdapter.getState();
        return status == BluetoothAdapter.STATE_ON || status == BluetoothAdapter.STATE_TURNING_ON;
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isBluetoothOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (configVal) {
            btAdapter.enable();
        } else {
            btAdapter.disable();
        }
        return true;
    }

    public boolean setBluetooth(boolean flag) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (flag) {
            btAdapter.enable();
        } else {
            btAdapter.disable();
        }
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isBluetoothOn() != configVal;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return -powerConfig.bt_weight;
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
        return mContext.getResources().getString(R.string.close_bluetooth);
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isBluetoothOn()));
    }

}
