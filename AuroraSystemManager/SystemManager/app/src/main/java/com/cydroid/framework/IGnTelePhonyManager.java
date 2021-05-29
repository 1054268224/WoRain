package com.cydroid.framework;

import android.content.Context;

public interface IGnTelePhonyManager {
    int getSimIndicatorStateGemini(int slotId);

    int getSimState(int slotId);

    int getNetworkType();

    String getDeviceId(int subscription);

    String getSubscriberId(int subscription);

    String getSimOperatorName(int subscription);

    boolean setPreferredDataSubscription(int subscription);

    int getPreferredDataSubscription();

    String getMultiSimName(Context context, int subscription);

    boolean isNetworkRoaming(int subscription);

    int getNetworkType(int subscription);

    int getDataState(int subscription);

}
