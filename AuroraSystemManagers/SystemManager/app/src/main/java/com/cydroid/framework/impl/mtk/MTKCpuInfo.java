package com.cydroid.framework.impl.mtk;

import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import  com.cydroid.softmanager.utils.Log;

import com.cydroid.framework.ICpuInfo;
import com.mediatek.perfservice.IPerfService;

public class MTKCpuInfo implements ICpuInfo{
	private static final String MTK_PERF_SERVICE = "mtk-perfservice";
	private static final String TAG = "CpuInfoUtils";
    private IPerfService mPerfService = null;
    private int mPerfServiceinited = 0;
    private int mPerfServiceHandle = -1;

    public MTKCpuInfo(){    	
    }
 
    public void mutliCoreEnable(boolean enable) {
        perfServiceInit();
        if (mPerfService != null && mPerfServiceHandle != -1) {
            Log.d(TAG, "mutliCore enable: " + enable);
            try {
                if(enable){
                	mPerfService.userEnable(mPerfServiceHandle);
                }
                if (!enable){
                	mPerfService.userDisable(mPerfServiceHandle);
                }
            } catch (RemoteException e) {
                Log.d(TAG, "ERR: RemoteException in mutliCoreEnable:" + e);
            }
        }
    }
    
    private void perfServiceInit() {
        if(mPerfServiceinited == 0) {
            IBinder b = ServiceManager.checkService(MTK_PERF_SERVICE);
            if(b != null) {
                mPerfService = IPerfService.Stub.asInterface(b);
                if (mPerfService != null) {
                    try {
                        mPerfServiceHandle = mPerfService.userReg(2, 0,Process.myPid(), Process.myTid());                     
                    } catch (RemoteException e) {
                        Log.d(TAG, "ERR: RemoteException in perfServiceInit:" + e);
                    }
                    mPerfServiceinited = 1;
                }
            }
        }
    }
}
