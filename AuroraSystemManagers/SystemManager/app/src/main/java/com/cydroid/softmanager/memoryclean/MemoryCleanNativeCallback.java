package com.cydroid.softmanager.memoryclean;

import android.os.RemoteException;

import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

/**
 * Created by houjie on 1/6/17.
 */
public class MemoryCleanNativeCallback implements IMemoryCleanNativeCallback {
    private static final String TAG = "MemoryCleanNativeCallback";

    private IMemoryCleanCallback mCallback;
    private IMemoryCleanCallbackV2 mCallbackV2;
    public MemoryCleanNativeCallback(IMemoryCleanCallback callback) {
        mCallback = callback;
    }

    public MemoryCleanNativeCallback(IMemoryCleanCallbackV2 callback) {
        mCallbackV2 = callback;
    }

    @Override
    public void onMemoryCleanReady(List<ProcessMemoryEntity> entitys) {
        Log.d(TAG, "onMemoryCleanReady begin");
        try {
            if (null != mCallback) {
                Log.d(TAG, "onMemoryCleanReady mCallback");
                mCallback.onMemoryCleanReady(entitys);
            } else if (null != mCallbackV2) {
                int totalProcesses = entitys.size();
                long totalPss = 0;
                for (ProcessMemoryEntity entity : entitys) {
                    totalPss += entity.mPss;
                }
                Log.d(TAG, "onMemoryCleanReady mCallbackV2 totalProcesses:" + 
                    totalProcesses + ", totalPss:" + totalPss);
                mCallbackV2.onMemoryCleanReady(totalProcesses, totalPss);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "onMemoryCleanReady exception:" + e.toString());
        }
    }

    @Override
    public void onMemoryCleanFinished(int totalProcesses, long totalPss) {
        try {
            if (null != mCallback) {
                mCallback.onMemoryCleanFinished(totalProcesses, totalPss);
            } else if (null != mCallbackV2) {
                mCallbackV2.onMemoryCleanFinished(totalProcesses, totalPss);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "onMemoryCleanFinished exception:" + e.toString());
        }
    }
}
