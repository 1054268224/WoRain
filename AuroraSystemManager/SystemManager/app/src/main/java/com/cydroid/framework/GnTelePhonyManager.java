package com.cydroid.framework;

import com.cydroid.framework.impl.mtk.MTKTelephonyManagerEx;
import com.cydroid.framework.impl.qcom.QCOMTelephonyManagerEx;

public class GnTelePhonyManager {
    public static String TAG = "TelePhonyManager";

    private GnTelePhonyManager() {
    }

    static class TelePhonyManagerHodler {
        private static IGnTelePhonyManager instance;
        static {
            if (Common.MTK_PLATFORM.equals(Common.getPlatform())) {
                instance = new MTKTelephonyManagerEx();
            }
            if (Common.QCOM_PLATFORM.equals(Common.getPlatform())) {
                instance = new QCOMTelephonyManagerEx();
            }
        }
    }

    public static IGnTelePhonyManager getDefault() {
        return TelePhonyManagerHodler.instance;
    }
}
