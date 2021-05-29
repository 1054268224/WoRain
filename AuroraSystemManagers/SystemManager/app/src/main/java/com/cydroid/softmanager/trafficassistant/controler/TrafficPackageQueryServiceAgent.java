//Gionee: mengdw <2015-11-11> add for CR01589343 begin
package com.cydroid.softmanager.trafficassistant.controler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.SparseArray;

import com.cydroid.softmanager.trafficassistant.model.PackageQueryServiceData;
import com.cydroid.softmanager.utils.Log;
import com.opera.max.sdk.traffic_package.IPackageQueryResult;
import com.opera.max.sdk.traffic_package.IPackageQueryService;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

public class TrafficPackageQueryServiceAgent {
    public interface ServiceConnectionListener {
        void onConnected(final IPackageQueryService service);
        void onDisconnected();
    }

    public interface QueryResultListener {
        void onQueryResult(PackageQueryServiceData result);
        void onReceivePackageQueryMessage(String senderAddress, String messageBody,
                                          long timestampMillis);
    }
	
    public static final int QUERY_TYPE_TRAFFIC = 0;
    public static final int QUERY_TYPE_OPERATOR = 1;
    public static final int QUERY_TYPE_OPERATOR_TRAFFIC = 2;
    public static final int QUERY_TYPE_INVALID = -1;
    public static final int QUERY_RESULT_CODE_INVALID = -1;
    
    public static final int QUERY_SUCCESS_CODE = 0;
    public static final int QUERY_RESULT_QUERY_CODE_ERROR = 8;
    public static final int QUERY_RESULT_SMS_SEND_FIAL = 12;
    
    public static final String INVALID_TRAFFIC_FLAG = "invalidTraficData";
    public static final String QUERY_SUCCESS = "0";

    public static final String RESULT_KEY_SLODID = "slotId";
    public static final String RESULT_KEY_QUERY_TYPE = "queryType";
    public static final String RESULT_KEY_ERROR_CODE = "errorCode";
    public static final String RESULT_KEY_ERROR_MESSAGE = "errorMsg";
    public static final String RESULT_KEY_QUERY_ID = "queryId";
    public static final String RESULT_KEY_SUMMARY = "summary";
    public static final String RESULT_KEY_IDLE = "idle";
    public static final String RESULT_KEY_TOTAL = "total";
    public static final String RESULT_KEY_USED = "used";
    public static final String RESULT_KEY_LEFT = "left";
    public static final String RESULT_KEY_OPERATOR_INFO = "operatorInfo";
    public static final String RESULT_KEY_PROVINCE = "province";
    public static final String RESULT_KEY_OPERATOR = "operator";
    public static final String RESULT_KEY_BRAND = "brand";
    public static final String RESULT_KEY_QUERY_CODE = "queryCode";
    public static final String RESULT_KEY_QUERY_PHONE_NUMBER = "phoneNumber";

    public static final String RESULT_KEY_COMMON_TOTAL = "commonTotal";
    public static final String RESULT_KEY_COMMON_USED = "commonUsed";
    public static final String RESULT_KEY_COMMON_LEFT = "commonLeft";

    public static final String RESULT_KEY_IDLE_TOTAL = "IdleTotal";
    public static final String RESULT_KEY_IDLE_USED = "IdleUSED";
    public static final String RESULT_KEY_IDLE_LEFT = "IdleLeft";
    public static final String RESULT_KEY_NO_IDLE_DATA = "NoIdleData";

	private static final String TAG = "PackageQueryServiceAgent";
    private static final String QUERY_SERVICE_CLASS_NAME = "com.opera.max.sdk.traffic_package.PackageQueryService";
    
    private static final int MESSAGE_CABRATE_QUERY_TIME_OUT = 0;
    private static final int QUERY_TIMEOUT_TIME = 3 * 60 * 1000;

    private static TrafficPackageQueryServiceAgent sInstance;
    private final Context mAppContext;
    private String mServicePackage;
    private IPackageQueryService mService;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<ServiceConnectionListener> mListeners = new ArrayList<ServiceConnectionListener>();
    private final SparseArray<QueryResultListener> mQueryListeners = new SparseArray<QueryResultListener>();

    public static TrafficPackageQueryServiceAgent getInstance(Context context) {
        synchronized(TrafficPackageQueryServiceAgent.class){
            if (sInstance == null) {
                sInstance = new TrafficPackageQueryServiceAgent(context);
            }
        }
        return sInstance;
    }

    public IPackageQueryService getService() {
        return mService;
    }
    
    public void query(int slotId, int queryType) {
        try {
            if(mService != null) {
                mService.query(slotId, queryType);
            }
        } catch (Exception e) {
            Log.d(TAG, "query Exception msg=" + e.toString());
        }
    }
    
    public void setProvince(int slotId, String province) {
        try {
            if(mService != null) {
                mService.setProvince(slotId, province);
            }
        } catch (Exception e) {
            Log.d(TAG, "setProvince Exception msg=" + e.toString());
        }
    }
    
    public void setOperatorBrand(int slotId, String brand) {
        try {
            if(mService != null) {
                mService.setOperatorBrand(slotId, brand);
            }
        } catch (Exception e) {
            Log.d(TAG, "setOperatorBrand Exception msg=" + e.toString());
        }
    }

    public void addConnectionListener(ServiceConnectionListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeConnectionListener(ServiceConnectionListener listener) {
        mListeners.remove(listener);
    }

    public void addQueryResultListener(int slotId, QueryResultListener listner) {
        mQueryListeners.put(slotId, listner);
    }

    public void removeQueryResultListener(int slotId) {
        mQueryListeners.remove(slotId);
    }

    public void clearListeners() {
        mListeners.clear();
        mQueryListeners.clear();
    }

    public boolean isServiceBound() {
        return mService != null;
    }

    public String getServicePackage() {
        return mServicePackage;
    }

    public boolean bindService(final String targetPackage) {
        boolean result = true;
		if(isServiceBound()) {
			return result;
	    }
        try {
            if (!mAppContext.bindService(getPackageQueryServiceIntent(targetPackage), mConnection,
                    Context.BIND_AUTO_CREATE)) {
                Log.e(TAG, "bindService failed");
                result = false;
            } else {
                mServicePackage = targetPackage;
            }
        } catch (Exception e) {
            Log.e(TAG, "bindService failed with exception e=" + e.toString());
            result = false;
        }
        return result;
    }

    public void unbindService() {
        if (isServiceBound()) {
            try {
                mService.setQueryListener(null);
            } catch (Exception e) {
            }
            mAppContext.unbindService(mConnection);
            mServicePackage = null;
            mService = null;
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected name=" + name.getClassName());
            if (name.getClassName().equals(QUERY_SERVICE_CLASS_NAME)) {
                try {
                    mService = IPackageQueryService.Stub.asInterface(service);
                    mService.setQueryListener(new IPackageQueryResult.Stub() {
                        // this method is not called in main thread
                        @Override
                        public void onQueryResult(final String jsonResult) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    TrafficPackageQueryServiceAgent.this.onQueryResult(jsonResult);
                                }

                            });
                        }

                        @Override
                        public void onReceivePackageQueryMessage(final int slotId, final String senderAddress,
                            final String messageBody, final long timestampMillis) throws RemoteException {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    TrafficPackageQueryServiceAgent.this.onReceivePackageQueryMessage(slotId,
                                            senderAddress, messageBody, timestampMillis);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                   Log.d(TAG,"onServiceConnected RemoteException e=" + e.toString());
                }
                for (ServiceConnectionListener listener : mListeners) {
                    listener.onConnected(mService);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected name=" + name.getClassName());
            if (name.getClassName().equals(QUERY_SERVICE_CLASS_NAME)) {
                mService = null;
                mServicePackage = null;
                for (ServiceConnectionListener listener : mListeners) {
                    listener.onDisconnected();
                }
            }
        }
    };

    private TrafficPackageQueryServiceAgent(Context context) {
        mAppContext = context.getApplicationContext();
    }

    private void onQueryResult(final String jsonResult) {
        Log.d(TAG, "onQueryResult  jsonResult=" + jsonResult);
        try {
            JSONObject result = new JSONObject(jsonResult);
            int slotId = result.getInt("slotId");
            PackageQueryServiceData dataResult = analyzeJsonData(result);
            QueryResultListener listener = mQueryListeners.get(slotId);
            if (listener != null) {
                listener.onQueryResult(dataResult);
            }
        } catch (Exception e) {
            Log.d(TAG, "invalid query result e=" + e.toString());
        }
    }

    private PackageQueryServiceData analyzeJsonData(JSONObject result) {
        PackageQueryServiceData queryResultData = new PackageQueryServiceData();
        try {
            int slotId = 0;
            if (result.has(RESULT_KEY_SLODID)) {
                slotId = result.getInt(RESULT_KEY_SLODID);
            }
            queryResultData.setSlotIndex(slotId);
            int queryType = QUERY_TYPE_INVALID;
            if (result.has(RESULT_KEY_QUERY_TYPE)) {
                queryType = result.getInt(RESULT_KEY_QUERY_TYPE);
            }
            queryResultData.setQueryType(queryType);
            int errorCode = QUERY_RESULT_CODE_INVALID;
            if (result.has(RESULT_KEY_ERROR_CODE)) {
                errorCode = result.getInt(RESULT_KEY_ERROR_CODE);
            }
            queryResultData.setErrorCode(errorCode);
            String errorMsg = "";
            if (result.has(RESULT_KEY_ERROR_MESSAGE)) {
                errorMsg = result.getString(RESULT_KEY_ERROR_MESSAGE);
            }
            queryResultData.setErrorMsg(errorMsg);
            String queryID = "";
            if (result.has(RESULT_KEY_QUERY_ID)) {
                queryID = result.getString(RESULT_KEY_QUERY_ID);
            }
            queryResultData.setQueryID(queryID);
            JSONObject summary;
            if (result.has(RESULT_KEY_SUMMARY)) {
                summary = result.getJSONObject(RESULT_KEY_SUMMARY);
                int total = 0;
                int used = 0;
                int left = 0;
                if (summary.has(RESULT_KEY_TOTAL)) {
                    total = summary.getInt(RESULT_KEY_TOTAL);
                    queryResultData.setCommonTotalInvalid(isInvalidData(total));
                }
                queryResultData.setCommonTotal(total);
                if (summary.has(RESULT_KEY_USED)) {
                    used = summary.getInt(RESULT_KEY_USED);
                    queryResultData.setCommonUsedInvalid(isInvalidData(used));
                }
                queryResultData.setCommonUsed(used);
                if (summary.has(RESULT_KEY_LEFT)) {
                    left = summary.getInt(RESULT_KEY_LEFT);
                    queryResultData.setCommonLeftInvalid(isInvalidData(left));
                }
                queryResultData.setCommonLeft(left);
            }
            JSONObject idle;
            if (result.has(RESULT_KEY_IDLE)) {
                queryResultData.setHasIdleData(true);
                idle = result.getJSONObject(RESULT_KEY_IDLE);
                int idleTotal = 0;
                int idleUsed = 0;
                int idleLeft = 0;
                if (idle.has(RESULT_KEY_TOTAL)) {
                    idleTotal = idle.getInt(RESULT_KEY_TOTAL);
                    queryResultData.setIdleTotalInvalid(isInvalidData(idleTotal));
                }
                queryResultData.setIdleTotal(idleTotal);
                if (idle.has(RESULT_KEY_USED)) {
                    idleUsed = idle.getInt(RESULT_KEY_USED);
                    queryResultData.setIdleUsedInvalid(isInvalidData(idleUsed));
                }
                queryResultData.setIdleUsed(idleUsed);
                if (idle.has(RESULT_KEY_LEFT)) {
                    idleLeft = idle.getInt(RESULT_KEY_LEFT);
                    queryResultData.setIdleLeftInvalid(isInvalidData(idleLeft));
                }
                queryResultData.setIdleLeft(idleLeft);
            } else {
                queryResultData.setHasIdleData(false);
            }
            JSONObject operatorInfo;
            if (result.has(RESULT_KEY_OPERATOR_INFO)) {
                operatorInfo = result.getJSONObject(RESULT_KEY_OPERATOR_INFO);
                String province = "";
                String operator = "";
                String brand = "";
                String queryCode = "";
                String phoneNumber = "";
                if (operatorInfo.has(RESULT_KEY_PROVINCE)) {
                    province = operatorInfo.getString(RESULT_KEY_PROVINCE);
                }
                queryResultData.setProvince(province);
                if (operatorInfo.has(RESULT_KEY_OPERATOR)) {
                    operator = operatorInfo.getString(RESULT_KEY_OPERATOR);
                }
                queryResultData.setOperator(operator);
                if (operatorInfo.has(RESULT_KEY_BRAND)) {
                    brand = operatorInfo.getString(RESULT_KEY_BRAND);
                }
                queryResultData.setBrand(brand);
                if (operatorInfo.has(RESULT_KEY_QUERY_CODE)) {
                    queryCode = operatorInfo.getString(RESULT_KEY_QUERY_CODE);
                }
                queryResultData.setQueryCode(queryCode);
                if (operatorInfo.has(RESULT_KEY_QUERY_PHONE_NUMBER)) {
                    phoneNumber = operatorInfo.getString(RESULT_KEY_QUERY_PHONE_NUMBER);
                }
                queryResultData.setPhoneNumber(phoneNumber);
            }
        } catch (JSONException e) {
            Log.d(TAG, "analyzeJsonData JSONException e=" + e.toString());
        }
        return queryResultData;
    }

    private void onReceivePackageQueryMessage(int slotId, String senderAddress, String messageBody,
            long timestampMillis) {
        try {
            QueryResultListener listener = mQueryListeners.get(slotId);
            if (listener != null) {
                listener.onReceivePackageQueryMessage(senderAddress, messageBody, timestampMillis);
            }
        } catch (Exception e) {
            Log.e(TAG, "invalid query result: " + messageBody);
        }
    }

    private static Intent getPackageQueryServiceIntent(final String targetPackage) {
        Intent intent = new Intent();
        intent.setClassName(targetPackage, QUERY_SERVICE_CLASS_NAME);
        return intent;
    }

	private boolean isInvalidData(int value) {
		return Integer.MIN_VALUE == value;
	}
}
//Gionee: mengdw <2015-11-11> add for CR01589343 end
