package com.cydroid.softmanager.trafficassistant.net;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.cydroid.softmanager.utils.Log;
import com.opera.max.sdk.saving.ISavingService;

public class AppSavingService {
    private static final String TAG = "AppSavingService";
	private static final String EXTRA_SHOW_NOTIFICATION = "EXTRA_SHOW_NOTIFICATION";
    private static final String EXTRA_HIDE_NOTIFICATION_AFTER_SAVING_OFF = "EXTRA_HIDE_NOTIFICATION_AFTER_SAVING_OFF";
    private static final String SAVING_SERVICE_INTENT_ACTION = "com.opera.max.sdk.saving.SavingService";
    private static final String SAVING_SERVICE_CLASS_NAME = "com.opera.max.sdk.saving.SavingService";
    private final SavingServiceConnection savingServiceConnection = new SavingServiceConnection();
    private final String[] pkgList = { "com.oupeng.max.sdk", "com.trafficctr.miui" };
    private final String currentPackageName = pkgList[0];
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static AppSavingService sInstance;
    private ISavingService mSavingService;
	private final Context mContext;
	
    public static void setInstance(AppSavingService instance) {
        sInstance = instance;
    }

	private AppSavingService(Context context){
		mContext = context.getApplicationContext();
	}
		
    public static AppSavingService getInstance(Context context) {
        Log.d(TAG, "AppSavingService getInstance context=" + context);
        if (sInstance == null) {
            sInstance = new AppSavingService(context.getApplicationContext());
        }
        return sInstance;
    }
	
	public ISavingService getSavingService(){
		return mSavingService;
	}
	
	public void bindSavingService() {
	    Log.d(TAG, "bindSavingService");
		if (mSavingService == null) {
			try {
				if (!mContext.bindService(getSavingServiceIntent(),
						savingServiceConnection, Context.BIND_AUTO_CREATE)) {
					Log.d(TAG, "bindSavingService failed");
				}
			} catch (Exception e) {
				Log.e(TAG, "bindSavingService failed with exception");
			}
		}
	}
	
	public void unbindSavingService() {
	    Log.d(TAG, "unbindSavingService");
	    setInstance(null);
		if (mSavingService != null) {
			mContext.unbindService(savingServiceConnection);
			mSavingService = null;
		}
	}
	
	private Intent getSavingServiceIntent() {
		Intent intent = new Intent(SAVING_SERVICE_INTENT_ACTION);
		intent.setClassName(currentPackageName, SAVING_SERVICE_CLASS_NAME);
		intent.putExtra(EXTRA_SHOW_NOTIFICATION, false);
		intent.putExtra(EXTRA_HIDE_NOTIFICATION_AFTER_SAVING_OFF, false);
		return intent;
	}
	
	private class SavingServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.d(TAG, "onServiceConnected name=" + name.getClassName());
            if (name.getClassName().equals(SAVING_SERVICE_CLASS_NAME)) {
            	mSavingService = ISavingService.Stub.asInterface(service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        	Log.d(TAG, "onServiceDisconnected name=" + name.getClassName());
            if (name.getClassName().equals(SAVING_SERVICE_CLASS_NAME)) {
            	mSavingService = null;
            }
        }       
   }
}
