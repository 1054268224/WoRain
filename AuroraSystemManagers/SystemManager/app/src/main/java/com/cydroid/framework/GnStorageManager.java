package com.cydroid.framework;

import com.cydroid.framework.impl.mtk.MTKStorageManager;
import com.cydroid.framework.impl.qcom.QCOMStorageManager;

public class GnStorageManager {
    public static String TAG = "GnStorageManager";

    private GnStorageManager() {
    }

    static class StorageManagerHodler {
        private static IGnStorageManager instance;
        static {
            if (Common.MTK_PLATFORM.equals(Common.getPlatform())) {
                instance = new MTKStorageManager();
            }
            if (Common.QCOM_PLATFORM.equals(Common.getPlatform())) {
                instance = new QCOMStorageManager();
            }
        }
    }

    public static IGnStorageManager getInstance() {
        return StorageManagerHodler.instance;
    }
}
