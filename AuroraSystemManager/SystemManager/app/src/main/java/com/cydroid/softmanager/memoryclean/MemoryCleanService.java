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

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.Log;

import java.util.List;
import com.chenyee.featureoption.ServiceUtil;
public class MemoryCleanService extends Service {
    private static final String TAG = "MemoryCleanService";

    private final IMemoryCleanService.Stub mBinderV1 = new IMemoryCleanService.Stub() {
        public void memoryClean(int cleanType, IMemoryCleanCallback callback) {
            Log.d(TAG, "memoryClean begin");
            MemoryManager memoryManager = MemoryManager.getInstance();
            memoryManager.init(MemoryCleanService.this);
            MemoryCleanNativeCallback memoryCleanNativeCallback =
                    new MemoryCleanNativeCallback(callback);
            memoryManager.memoryClean(cleanType, memoryCleanNativeCallback);
        }
        public List<ProcessMemoryEntity> getRunningProcessMemoryEntitys(int cleanType) {
            Log.d(TAG, "getRunningProcessMemoryEntitys begin");
            MemoryManager memoryManager = MemoryManager.getInstance();
            memoryManager.init(MemoryCleanService.this);
            return memoryManager.getRunningProcessMemoryEntitys(cleanType);
        }
        public void cleanProcessMemoryEntitys(List<ProcessMemoryEntity> entities,
                                       IMemoryCleanCallback callback) {
            Log.d(TAG, "cleanProcessMemoryEntitys begin");            
            MemoryManager memoryManager = MemoryManager.getInstance();
            memoryManager.init(MemoryCleanService.this);
            MemoryCleanNativeCallback memoryCleanNativeCallback =
                    new MemoryCleanNativeCallback(callback);
            memoryManager.cleanProcessMemoryEntitys(entities, memoryCleanNativeCallback);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public MemoryCleanService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinderV1;
    }
}
