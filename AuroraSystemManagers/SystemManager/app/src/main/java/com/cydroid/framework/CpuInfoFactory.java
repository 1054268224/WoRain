package com.cydroid.framework;

import com.cydroid.framework.Common;
import com.cydroid.framework.impl.mtk.MTKCpuInfo;
import com.cydroid.framework.impl.qcom.QCOMCpuInfo;

public class CpuInfoFactory {
    public static String TAG = "CpuInfoFactory";

    private CpuInfoFactory() {
    }

    static class CpuInfoHodler {
        private static ICpuInfo instance;
        static {
            if (Common.MTK_PLATFORM.equals(Common.getPlatform())) {
                instance = new MTKCpuInfo();
            }
            if (Common.QCOM_PLATFORM.equals(Common.getPlatform())) {
                instance = new QCOMCpuInfo();
            }
        }
    }

    public static ICpuInfo getDefault() {
        return CpuInfoHodler.instance;
    }
}
