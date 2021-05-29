package com.cydroid.systemmanager.rubbishcleaner.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.cleanmaster.sdk.IKSCleaner;
import com.keniu.security.CleanMasterSDK;

import com.cydroid.systemmanager.rubbishcleaner.ResidualRubbishAlertActivity;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.ServiceUtil;

public class ResidualRemoveService extends Service {
	private ServiceConnection mServConn;
	private IKSCleaner mKSCleaner;
	private boolean DEBUG = true;
	private static final String TAG = "CyeeRubbishCleaner/ResidualRemoveService";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(DEBUG, TAG, "onCreate() is Called");
		CleanMasterSDK.getInstance().Initialize(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String pkgName = intent.getStringExtra("pkgName");
		Log.d(DEBUG, TAG,
				"ResidualRemoveService onStartCommand(), removed apk's pkgName = "
						+ pkgName);
		ServiceUtil.handleStartForegroundServices(this);
		bindKSService(pkgName);
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(mServConn);
		mServConn = null;
		mKSCleaner = null;
	}

	private void bindKSService(final String pkgName) {
		mServConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(DEBUG, TAG,
						"service connected, start to deteck whether left residual rubbish");
				mKSCleaner = IKSCleaner.Stub.asInterface(service);
				popRemoveResidualAlertActivity(pkgName);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}

		};
		// Intent remoteIntent = new Intent("com.cleanmaster.CleanService");
		Intent remoteIntent = new Intent(this,
				com.cleanmaster.CleanService.class);
		bindService(remoteIntent, mServConn, BIND_AUTO_CREATE);
	}

	private void popRemoveResidualAlertActivity(String pkgName) {
		if (mKSCleaner == null || pkgName == null) {
			return;
		}

		String[] canDelPaths = null;
		long size = 0;
		try {
			canDelPaths = mKSCleaner.getCanDeleteResidualFilePaths(pkgName);
			if (canDelPaths == null) {
				Log.d(DEBUG, TAG,
						"ResidualRemoveService, removed apk has not residual rubbish");
				return;
			}
			for (int i = 0; i < canDelPaths.length; i++) {
				size += mKSCleaner.pathCalcSize(canDelPaths[i]);
			}
			if (size == 0) {
				return;
			}
			Intent intent = new Intent(this, ResidualRubbishAlertActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("package_name", pkgName);
			intent.putExtra("residual_size", size);
			intent.putExtra("files_path", canDelPaths);
			startActivity(intent);
		} catch (RemoteException e) {
			Log.d(DEBUG, TAG,
					"getCanDeleteResidualFilePaths() throw exception");
			e.printStackTrace();
			return;
		}
	}

}
