package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.HotKnotUtils;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;

import android.content.Context;

public class PowerModeItemHotKnot extends PowerModeItem {

    public PowerModeItemHotKnot(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
        mProviderHelper.putBoolean(saveKey, isHotKnotOn());
    }

    private boolean isHotKnotOn() {
        if (!isFunctionAvailable()) {
            return false;
        }
        return HotKnotUtils.isEnabled(mContext);
    }

    @Override
    public boolean isFunctionAvailable() {
        return HotKnotUtils.isHotKnotSupport();
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        setHotKnotState(mProviderHelper.getBoolean(saveKey, false));
        return true;
    }

    private void setHotKnotState(boolean enabled) {
        if (!isFunctionAvailable()) {
            return;
        }
        if (enabled == isHotKnotOn()) {
            return;
        }
        if (enabled) {
            HotKnotUtils.enable(mContext);
        } else {
            HotKnotUtils.disable(mContext);
        }
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        boolean configVal = mProviderHelper.getBoolean(compareConfigKey, false);
        return isHotKnotOn() != configVal;
    }

}
