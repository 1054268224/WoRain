package com.cydroid.systemmanager.rubbishcleaner.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Toast;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.ICmSdkUpdateCallback;
import com.cleanmaster.sdk.IKSCleaner;
import com.cydroid.softmanager.R;
import com.keniu.security.CleanMasterSDK;

import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.ServiceUtil;

public class KSCleanerUpdateService extends Service {
    private static final int SUCCESS = 0;
    private static final int ALREADY_UPDATE = 1;
    private static final int NETWORK_ERROR = 2;
    private static final int UNKNOWN_ERROR = 3;
    private static final int ALREADY_RUNNING = 4;
    private Context mContext;
	private ServiceConnection mServConn;
	private IKSCleaner mCleaner;
	private static final boolean DEBUG = true;
	private static final String TAG = "CyeeRubbishCleaner/KSCleanerUpdateService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		CleanMasterSDK.getInstance().Initialize(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(this);
		bindKSService();
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(mServConn);
		mServConn = null;
		mCleaner = null;
	}

	private void bindKSService() {
		mServConn = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(DEBUG, TAG,
						"KSCleanerUpdateService, connected cleanmaster service succuss");				
				if (!isNetworkAvailable()) {
				    mHandler.sendEmptyMessage(NETWORK_ERROR);
				    return;
                }
				if (mCleaner != null) {
                    return;
                } else {
                    mCleaner = IKSCleaner.Stub.asInterface(service);
                    try {
                        startUpdateKS();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return;
                    }
                }
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(DEBUG, TAG,
						"KSCleanerUpdateService, connected cleanmaster service failed");
			}

		};
		// Intent intent = new Intent("com.cleanmaster.CleanService");
		Intent intent = new Intent(this, com.cleanmaster.CleanService.class);
		try {
			bindService(intent, mServConn, Context.BIND_AUTO_CREATE);
		} catch (SecurityException e) {
			Log.d(DEBUG, TAG,
					"KSCleanerUpdateService call bindService throw exception");
		}
	}

	private  void startUpdateKS() throws RemoteException {
		if (mCleaner == null) {
			Log.d(DEBUG, TAG,
					"mCleaner = null, cann't start update cleanmaster for checking");
			return;
		}

		mCleaner.StartUpdateCheck(new ICmSdkUpdateCallback.Stub() {
			@Override
			public void FinishUpdateCheck(final int nErrorCode,
					final long size, final String strNewVersion)
					throws RemoteException {
				if (size == 0|| nErrorCode != CMCleanConst.UPDATE_ERROR_CODE_SUCCESS) {
					Log.d(DEBUG, TAG,"KSCleanerUpdateService, does't update");
					mHandler.sendEmptyMessage(ALREADY_UPDATE); 
					return;
				} else {
				    try {
                        //Gionee <xuwen><2015-07-30> add for CR01526874 begin
                        if (mCleaner == null) {
                            Log.d(DEBUG, TAG,
                                    "mCleaner = null, cann't start update cleanmaster.");
                            return;
                        }
                        //Gionee <xuwen><2015-07-30> add for CR01526874 end

                        mCleaner.StartUpdateData();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return;
                    }
                }

			}

			@Override
			public void FinishUpdateData(final int nErrorCode)
					throws RemoteException {
				try {
					Log.d(DEBUG, TAG,"KSCleanerUpdateService finish, new version is "+ mCleaner.GetDataVersion());
					if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_SUCCESS ) {
					    mHandler.sendEmptyMessage(SUCCESS);
                    } else if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_NO_NEWER_DB ) {
                        mHandler.sendEmptyMessage(ALREADY_UPDATE);
                    } else if(nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_NETWORK_ERROR){
					    mHandler.sendEmptyMessage(NETWORK_ERROR);
                    } else if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_UNKNOWN_ERROR) {
                        mHandler.sendEmptyMessage(UNKNOWN_ERROR);
                    } else if (nErrorCode == CMCleanConst.UPDATE_ERROR_CODE_ALREADY_RUNNING) {
                        mHandler.sendEmptyMessage(ALREADY_RUNNING);
                    }
				} catch (RemoteException e) {
					Log.d(DEBUG, TAG,
							"KSCleanerUpdateService call GetDataVersion() throws RemoteException");
				}
			}
		});
	}

	private  Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case SUCCESS:
                    Toast.makeText(mContext, R.string.rubbish_update_success,Toast.LENGTH_SHORT).show();
                    mCleaner = null;
                    break;
                case ALREADY_UPDATE:
                    Toast.makeText(mContext, R.string.rubbish_update_already,Toast.LENGTH_SHORT).show();
                    mCleaner = null;
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(mContext, R.string.rubbish_update_network_error,Toast.LENGTH_SHORT).show();
                    mCleaner = null;
                    break;
                case UNKNOWN_ERROR:
                    Toast.makeText(mContext, R.string.rubbish_update_unknown_error,Toast.LENGTH_SHORT).show();
                    mCleaner = null;
                    break;
                case ALREADY_RUNNING:
                    Toast.makeText(mContext, R.string.rubbish_update_already_running,Toast.LENGTH_SHORT).show();
                    mCleaner = null;
                    break;

                default:
                    break;
            }
        }
    };
    
    public boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    Log.d(TAG, "===状态===" + networkInfo[i].getState());
                    Log.d(TAG, "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
