package com.cydroid.softmanager.powersaver.mode;

import java.util.List;

import com.cydroid.softmanager.powersaver.mode.item.PowerModeItem;
import com.cydroid.softmanager.powersaver.utils.PowerConsts;
import com.cydroid.softmanager.powersaver.utils.PowerModeUtils;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.R;

import android.content.Context;

public class NormalModeController {
    private static final String TAG = "NormalModeController";

    private final Context mContext;

    public NormalModeController(Context context) {
        mContext = context;
    }

    public void start(Runnable afterCallback) {
        PowerModeUtils.setCurrentMode(mContext, PowerConsts.NORMAL_MODE);

        ModeItemsController normalModeItems = getNormalModeController(mContext);
        normalModeItems.saveCheckPoint();
        normalModeItems.applyConfig();
        if (afterCallback != null) {
            afterCallback.run();
        }
    }

    public static ModeItemsController getNormalModeController(Context context) {
        PowerServiceProviderHelper mPowerServiceProviderHelper = new PowerServiceProviderHelper(context);
        boolean needInit = !mPowerServiceProviderHelper.hasKey("CONFIG_INITED_NORMAL");
        String[] normalItemArray = context.getResources().getStringArray(R.array.powermode_NORMAL);
        ModeItemsController normalModeItems = new ModeItemsController(context, "NORMAL", normalItemArray);
        if (needInit) {
            normalModeItems.resetConfigToDefault();
            mPowerServiceProviderHelper.putBoolean("CONFIG_INITED_NORMAL", true);
        }
        return normalModeItems;
    }

    public void restore(Runnable afterCallback) {
        ModeItemsController normalModeItems = getNormalModeController(mContext);
        normalModeItems.restoreCheckPoint();

        PowerModeUtils.setCurrentMode(mContext, PowerConsts.NONE_MODE);

        if (afterCallback != null) {
            afterCallback.run();
        }
    }

    public void setPowerModeConfigList(List<ModeItemInfo> configList) {
        ModeItemsController normalModeItems = getNormalModeController(mContext);
        normalModeItems.setConfigFromItemInfos(configList);
    }

    public void applyPowerModeConfig(Runnable afterCallback) {
        ModeItemsController normalModeItems = getNormalModeController(mContext);
        normalModeItems.applyConfig();
        if (afterCallback != null) {
            afterCallback.run();
        }
    }

    public boolean isPowerModeConfigDiffFromDefault() {
        ModeItemsController normalModeItems = getNormalModeController(mContext);
        return normalModeItems.isConfigChangedFromDefault();
    }

    public float getPowerModeConfigWeight() {
        float res = 1f;
        ModeItemsController noneModeItems = NoneModeController.getNoneModeController(mContext);
        ModeItemsController normalModeItems = getNormalModeController(mContext);
        float weight = normalModeItems.getCalculatedWeights();
        Log.d(TAG, "normal mode config item weight sum=" + weight);
        List<ModeItemInfo> infos = normalModeItems.getItemInfos();
        for (ModeItemInfo info : infos) {
            if (!info.configVal.equals(PowerModeItem.SWITCH_PASS))
                noneModeItems.removeItem(info.name);
        }
        float noneWeight = noneModeItems.getCurrentWeights();
        Log.d(TAG, "none mode other item weight sum=" + noneWeight);
        return res + weight + noneWeight;
    }

    public float getPowerModeDefaultWeight() {
        float res = 1f;
        ModeItemsController noneModeItems = NoneModeController.getNoneModeController(mContext);
        ModeItemsController normalModeItems = getNormalModeController(mContext);
        float weight = normalModeItems.getDefaultWeights();
        Log.d(TAG, "normal mode default item weight sum=" + weight);
        List<ModeItemInfo> infos = normalModeItems.getItemInfos();
        for (ModeItemInfo info : infos) {
            if (!info.defaultVal.equals(PowerModeItem.SWITCH_PASS))
                noneModeItems.removeItem(info.name);
        }
        float noneWeight = noneModeItems.getCurrentWeights();
        Log.d(TAG, "none mode other item weight sum=" + noneWeight);
        return res + weight + noneWeight;
    }

    public List<ModeItemInfo> getPowerModeConfigList() {
        ModeItemsController normalModeItems = getNormalModeController(mContext);
        return normalModeItems.getItemInfos();
    }
}
