package com.cydroid.systemmanager.antivirus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.widget.Toast;

import com.cydroid.softmanager.R;
import com.intel.security.Properties;
import com.intel.security.SecurityContext;
import com.intel.security.vsm.UpdateObserver;
import com.intel.security.vsm.UpdateTask;
import com.intel.security.vsm.VirusScan;

import cyee.preference.CyeeSwitchPreference;
import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanSettingsFragment;
import com.cydroid.systemmanager.rubbishcleaner.service.KSCleanerUpdateService;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.ServiceUtil;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkConnectChangedReceiver:";
    public static final String LAST_TIME = "last_time";
    public static final String RUBBISH_UPDATE_LAST_TIME = "rubbish_update_last_time";
    public static final int SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000;
    private long mCurentTime,mRubbishCurentTime;
    private SharedPreferences mLastUpdateTimePreferences, mAutoUpdatePreferences,
            mRubbishAutoUpdatePreferences, mRubbishLastUpdateTimePreferences;
	//Gionee guoxt 2015-03-04 modified for CR01449811 begin
    public static final boolean gnVFflag = SystemProperties.get("ro.cy.custom").equals("VISUALFAN");
	public static final boolean gnNoAnti =   SystemProperties.get("ro.cy.anti.virus.support", "no").equals("no");
    //Gionee guoxt 2015-03-04 modified for CR01449811 end
    private Context mContext;
    private CyeeSwitchPreference mAutoUpdate;
    private ConfigLoader mConfigLoader = null;
	 private VirusScan mVirusScan = null; 
	 private Object SYNC_UPDATE = new Object();
	 private UpdateTask mUpdateTask = null;
	 

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "action:"+intent.getAction());
        mContext = context.getApplicationContext();
		mVirusScan = null;
        mConfigLoader = new ConfigLoader(mContext);
        mConfigLoader.load();
        mAutoUpdatePreferences = mContext.getSharedPreferences(AntiVirusPrefsFragment.AUTO_UPDATE_KEY,
                Context.MODE_PRIVATE);
        mRubbishAutoUpdatePreferences = mContext.getSharedPreferences(
                RubbishCleanSettingsFragment.AUTO_UPDATE_KEY, Context.MODE_PRIVATE);

        /* guoxt modify for update 106509 begin */
		 ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
		sleep(1000);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        Log.d(TAG, "wifi is connect:" + wifiInfo.isConnected());
		/*guoxt modify for VF remove anti begin */
		if(gnVFflag ||gnNoAnti){ 
			return;
		}
		/*guoxt modify for VF remove anti end */
		
        // Gionee <yangxinruo> <2016-4-22> modify for CR01660803 begin
        //if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
        if(wifiInfo.isConnected()){
            //ConnectivityManager manager = (ConnectivityManager) mContext
                    //.getSystemService(Context.CONNECTIVITY_SERVICE);
            //NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isConnected = wifiInfo.isConnected();
            boolean autoUpdateEnable = mAutoUpdatePreferences
                    .getBoolean(AntiVirusPrefsFragment.AUTO_UPDATE_KEY, true);
            boolean rubbishAutoUpdateEnable = mRubbishAutoUpdatePreferences
                    .getBoolean(RubbishCleanSettingsFragment.AUTO_UPDATE_KEY, true);
            boolean flag = getBoolean("is_first_utilization", true);
            Log.d(TAG,
                    "isConnected:" + isConnected + ", autoUpdateEnable:" + autoUpdateEnable
                            + ", rubbishAutoUpdateEnable:" + rubbishAutoUpdateEnable
                            + ", is_first_utilization:" + flag);
            if (isConnected && autoUpdateEnable && !flag) {
                getCurentTimeAndCompare();
            }

            if (isConnected && rubbishAutoUpdateEnable && !flag) {
			     //guoxt remove rubbish update begin
                //getRubbishCurentTimeAndCompare();
				//guoxt remove rubbish update end
            }
        }
		 /* guoxt modify for update  106509 end */
        /*
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                State state = networkInfo.getState();
                boolean isConnected = state == State.CONNECTED;
                boolean autoUpdateEnable = mAutoUpdatePreferences.getBoolean(
                        AntiVirusPrefsFragment.AUTO_UPDATE_KEY, true);
                boolean rubbishAutoUpdateEnable = mRubbishAutoUpdatePreferences.getBoolean(
                        RubbishCleanSettingsFragment.AUTO_UPDATE_KEY, true);
                boolean flag = getBoolean("is_first_utilization", true);
                Log.d(TAG, "isConnected:" + isConnected + ", autoUpdateEnable:"
                    + autoUpdateEnable + ", rubbishAutoUpdateEnable:" + rubbishAutoUpdateEnable
                    + ", is_first_utilization:" + flag);
                if (isConnected && autoUpdateEnable && !flag) {
                    getCurentTimeAndCompare();
                }

                if (isConnected && rubbishAutoUpdateEnable && !flag) {
                    getRubbishCurentTimeAndCompare();
                }

            }
        }
        */
        // Gionee <yangxinruo> <2016-4-22> modify for CR01660803 end
    }

/* guoxt modify for update 106509 begin */
		private void sleep(long milliseconds) {
			try {
				Thread.sleep(milliseconds);
			} catch (Exception ex) {
			}
		}
/* guoxt modify for update 106509 end */

    private void getCurentTimeAndCompare() {
        mCurentTime = System.currentTimeMillis();
        mLastUpdateTimePreferences = mContext.getSharedPreferences(LAST_TIME, Context.MODE_PRIVATE);
        long lasttime = mLastUpdateTimePreferences.getLong(LAST_TIME, 0);
		Log.d(TAG,"time gap:"+ (int)(mCurentTime - lasttime));
        if (lasttime == 0) {
            update(mContext);
        } else {
            if (mCurentTime - lasttime >= SEVEN_DAYS) {
                update(mContext);
             }
        }

    }

    private void getRubbishCurentTimeAndCompare() {
        mRubbishCurentTime = System.currentTimeMillis();
        mRubbishLastUpdateTimePreferences = mContext.getSharedPreferences(RUBBISH_UPDATE_LAST_TIME,
                Context.MODE_PRIVATE);
        long lasttime = mRubbishLastUpdateTimePreferences.getLong(RUBBISH_UPDATE_LAST_TIME, 0);
        if (lasttime == 0) {
            rubbishUpdate(mContext);
        } else {
            if (mRubbishCurentTime - lasttime >= SEVEN_DAYS) {
                rubbishUpdate(mContext);
            }
        }
    }



    // Gionee <houjie> <2015-10-22> add for CR01564093 begin
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    // Gionee <houjie> <2015-10-22> add for CR01564093 end
    Handler handler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case UpdateObserver.RESULT_SUCCEEDED:
                        Toast.makeText(mContext, R.string.update_finish,
					        Toast.LENGTH_SHORT).show();
                    break;
				 case -1:
                        Toast.makeText(mContext, R.string.update_failed,
					        Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    };
	
	 public void update(Context context)
    {

		Log.e(TAG, "auto update start");
        // Gionee <houjie> <2015-10-22> add for CR01564093 begin
        if (!isNetworkAvailable()) {
            Toast.makeText(context, R.string.update_failed,
					Toast.LENGTH_SHORT).show();
            Log.d(TAG, "startUpdate failed : network does not available");
            return;
        }
        // Gionee <houjie> <2015-10-22> add for CR01564093 end
        
        mVirusScan = (VirusScan) SecurityContext.getService(mContext, SecurityContext.VIRUS_SCAN);

		if (mVirusScan == null)
        {
            return;
        }
		
		    Toast.makeText(context, R.string.update_start_check,
					Toast.LENGTH_SHORT).show();

        synchronized (SYNC_UPDATE)
        {
            mUpdateTask = mVirusScan.update(new UpdateObserver(){
                @Override
                public void onStarted()
                {
                    Log.d("guoxt", "update started");
                }

                @Override
                public void onCompleted(int i)
                {
                   // UpdateObserver.RESULT_SUCCEEDED  0  RESULT_CANCELED   1  
                   // handler.sendEmptyMessage(i);
                    Log.d(TAG, "update completed, result is " + i);
                    synchronized (SYNC_UPDATE)
                    {
                        // Remove reference to update task once update done.
                        mUpdateTask = null;
                    }

                    // Gets signature database version
                    String dat = mVirusScan.getProperties().getString(Properties.KEY_DAT_VERSION);
                    // Gets scan engine version
                    String mcs = mVirusScan.getProperties().getString(Properties.KEY_MCS_VERSION);

                    Log.d(TAG, "update completed, dat is " + dat);
                    Log.d(TAG, "update completed, mcs is " + mcs);
					
                }
            });
			   float progress = mUpdateTask.getState().getProgress();
			    mLastUpdateTimePreferences.edit().putLong(LAST_TIME, mCurentTime).commit();

        }
    }
    
    private void rubbishUpdate(Context context){
        Intent servIntent = new Intent(context, KSCleanerUpdateService.class);
        ServiceUtil.startForegroundService(context,servIntent);
        mRubbishLastUpdateTimePreferences.edit().putLong(RUBBISH_UPDATE_LAST_TIME, mRubbishCurentTime).commit();
    }

    public boolean getBoolean(String key, boolean defValue) {

        boolean returnValue = defValue;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(getUri("boolean"), null, key,
                    new String[] {"" + defValue}, null);
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                returnValue = "true".equals(value) ? true : false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception ", e);
        } finally {
            closeCursor(cursor);
        }
        return returnValue;
    }

    private Uri getUri(String str) {
        return Uri.parse("content://" + "com.cydroid.systemmanager.sp" + "/" + str);
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}