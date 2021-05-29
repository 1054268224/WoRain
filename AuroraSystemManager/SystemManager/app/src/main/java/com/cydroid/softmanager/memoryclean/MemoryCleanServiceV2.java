/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-12-27
 */
package com.cydroid.softmanager.memoryclean;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cydroid.softmanager.utils.Log;

import java.util.List;
import com.chenyee.featureoption.ServiceUtil;
public class MemoryCleanServiceV2 extends Service {
    private static final String TAG = "MemoryCleanServiceV2";

    private final IMemoryCleanServiceV2.Stub mBinderV2 = new IMemoryCleanServiceV2.Stub() {
        public void memoryClean(int cleanType, IMemoryCleanCallbackV2 callback) {
            Log.d(TAG, "memoryClean v2 begin");
            MemoryManager memoryManager = MemoryManager.getInstance();
            memoryManager.init(MemoryCleanServiceV2.this);
            MemoryCleanNativeCallback memoryCleanNativeCallback =
                    new MemoryCleanNativeCallback(callback);
            memoryManager.memoryClean(cleanType, memoryCleanNativeCallback);
        }
    };

    public MemoryCleanServiceV2() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(this);
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinderV2;
    }
}
