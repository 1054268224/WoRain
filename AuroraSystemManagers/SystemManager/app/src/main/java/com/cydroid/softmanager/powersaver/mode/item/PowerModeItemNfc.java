package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import android.nfc.NfcAdapter;
import android.os.SystemProperties;
import android.content.Context;

public class PowerModeItemNfc extends PowerModeItem {

    public PowerModeItemNfc(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private NfcAdapter getNfcAdapter() {
        return NfcAdapter.getDefaultAdapter(mContext);
    }

    @Override
    public boolean isFunctionAvailable() {
        return getNfcAdapter() != null;
    }

    private void setNfcState(boolean enable) {
        if (!isFunctionAvailable()) {
            return;
        }
        if (enable && !isNfcOn()) {
            Log.d(TAG, "----->setNfcEnable");
            getNfcAdapter().enable();
        } else if (!enable && isNfcOn()) {
            Log.d(TAG, "----->setNfcDisable");
            getNfcAdapter().disable();
        }
    }

    private boolean isNfcOn() {
        if (!isFunctionAvailable()) {
            return false;
        }
        return getNfcAdapter().isEnabled();
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isNfcOn());
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        boolean configVal = mProviderHelper.getBoolean(saveKey, false);
        setNfcState(configVal);
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isNfcOn() != configVal;
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("false");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.close_nfc);
    }

}
