package com.cydroid.softmanager.powersaver.mode;

import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.R;
import android.content.Context;

public class NoneModeController {
    private static final String TAG = "NoneModeController";

    private final Context mContext;

    public NoneModeController(Context context) {
        mContext = context;
    }

    public float getPowerModeConfigWeight() {
        return getPowerModeDefaultWeight();
    }

    public float getPowerModeDefaultWeight() {
        float res = 1f;
        ModeItemsController noneModeItems = getNoneModeController(mContext);
        float noneWeight = noneModeItems.getCurrentWeights();
        Log.d(TAG, "none mode other item weight sum=" + noneWeight);
        return res + noneWeight;
    }

    public static ModeItemsController getNoneModeController(Context context) {
        String[] noneItemArray = context.getResources().getStringArray(R.array.powermode_NONE);
        ModeItemsController noneModeItems = new ModeItemsController(context, "NONE", noneItemArray);
        return noneModeItems;
    }

}
