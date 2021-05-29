package com.cydroid.softmanager.trafficassistant.net;

import com.cydroid.softmanager.utils.Log;
import com.opera.max.sdk.traffic.ITrafficService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class AppTrafficService {
	
	private static final String TRAFFIC_SERVICE_INTENT_ACTION = "com.opera.max.sdk.traffic.TrafficService";
	private static final String TRAFFIC_SERVICE_CLASS_NAME = "com.opera.max.sdk.traffic.TrafficService";
	private final TrafficServiceConnection trafficServiceConnection = new TrafficServiceConnection();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final String[] pkgList = {"com.oupeng.max.sdk", "com.trafficctr.miui"};
    private final String currentPackageName = pkgList[0];
	private static ITrafficService trafficService;
	private static AppTrafficService sInstance;
	private final Context mContext;
	private String mImsi;

	private AppTrafficService(Context context) {
		mContext = context.getApplicationContext();
	}

	public static AppTrafficService getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AppTrafficService(context);
		}
		return sInstance;
	}

	public void setIMSI(String imsi) {
		mImsi = imsi;
	}

	public void bindTrafficService() {
		if (trafficService == null) {
			try {
				if (!mContext.bindService(getTrafficServiceIntent(),
						trafficServiceConnection, Context.BIND_AUTO_CREATE)) {
				}
			} catch (Exception e) {
				Log.d("simcardjh","bindTrafficService failed with exception " + e);
			}
		}
	}

	public void unbindTrafficService() {
		if (trafficService != null) {
			try {
				mContext.unbindService(trafficServiceConnection);
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("simcardjh","unbindService failed with exception " + e);
			}
			setTrafficService(null);
		}
	}

	public static void setTrafficService(ITrafficService _trafficService) {
	    trafficService = _trafficService;
        }

    private Intent getTrafficServiceIntent() {
		Intent intent = new Intent(TRAFFIC_SERVICE_INTENT_ACTION);
		intent.setClassName(currentPackageName, TRAFFIC_SERVICE_CLASS_NAME);
		return intent;
	}

	private class TrafficServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (name.getClassName().equals(TRAFFIC_SERVICE_CLASS_NAME)) {
				trafficService = ITrafficService.Stub.asInterface(service);
				if (trafficService != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							try {
								trafficService.setImsi(mImsi);
							} catch (Exception e) {
								Log.d("simcardjh","set imsi fail!!" + e);
							}
						}
					});
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (name.getClassName().equals(TRAFFIC_SERVICE_CLASS_NAME)) {
				trafficService = null;
			}
		}
	}
}
