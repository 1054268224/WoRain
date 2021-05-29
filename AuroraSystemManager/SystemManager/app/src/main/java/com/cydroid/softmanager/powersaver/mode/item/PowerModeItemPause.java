package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;

import android.content.Context;

public class PowerModeItemPause extends PowerModeItem {

    private String mPauseTag;

    public PowerModeItemPause(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        return false;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return false;
    }

    public void runBeforeCallback() {
        if (mBeforeCallback != null) {
            mBeforeCallback.run();
        }
    }

    public void runAfterCallback() {
        if (mAfterCallback != null) {
            mAfterCallback.run();
        }
    }

    @Override
    public String getName() {
        if (mPauseTag == null || mPauseTag.isEmpty()) {
            return super.getName();
        }
        return mPauseTag;
    }

    public void setName(String name) {
        mPauseTag = name;
    }

}
