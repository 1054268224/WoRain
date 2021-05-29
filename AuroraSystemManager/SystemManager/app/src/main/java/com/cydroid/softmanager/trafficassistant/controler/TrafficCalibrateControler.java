//Gionee: mengdw <2015-12-28> add for CR01589343 begin
package com.cydroid.softmanager.trafficassistant.controler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.model.PackageQueryServiceData;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.TimeFormat;
import com.cydroid.softmanager.trafficassistant.utils.TrafficPreference;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.opera.max.sdk.traffic_package.IPackageQueryService;

public class TrafficCalibrateControler {
    public static final String ACTION_CALIBRATE_RESULT = "com.cydroid.softmanager.trafficassistant.controler.action.calibrate.result";

    public static final int STATUS_CALIBRATE_SUCCESS = 0;
    public static final int STATUS_CALIBRATE_NO_QUERY_CODE = 1;
    public static final int STATUS_CALIBRATE_FAIL = 2;
    public static final int STATUS_CALIBRATE_SEND_SMS_FIAL = 3;
    public static final String SIM_INDEX = "SimIndex";
    public static final String RESULT_CODE = "ResultCode";

    private static final int MESSAGE_CALIBRATE_RECEIVE_RESULT_SUCCESS = 0;
    private static final int MESSAGE_CALIBRATE_RECEIVE_RESULT_FAIL = 1;
    private static final int MESSAGE_CALIBRATE_TIME_OUT = 2;
    private static final int CALIBRATE_TIME_OUT_TIME = 3 * 60 * 1000;

    private static final String TAG = "TrafficCalibrateControler";
    private static TrafficCalibrateControler sInstance;
    private final TrafficPackageQueryServiceAgent mTrafficPackageQueryServiceAgent;
    private final TrafficCalibrateControlerHander mTrafficCalibrateControlerHander;
    private final PackageQueryServiceClient mPackageQueryServiceClient;
    private final Context mContext;
    private final boolean[] mCalibrateState = {false, false};
    private final int[] mStatusList = {STATUS_CALIBRATE_SUCCESS, STATUS_CALIBRATE_SUCCESS};

    public static TrafficCalibrateControler getInstance(Context context) {
        synchronized (TrafficCalibrateControler.class) {
            if (sInstance == null) {
                sInstance = new TrafficCalibrateControler(context);
            }
        }
        return sInstance;
    }

    public void finalizeTrafficCalibrateControler() {
        mTrafficPackageQueryServiceAgent.unbindService();
        mTrafficPackageQueryServiceAgent.removeQueryResultListener(Constant.SIM1);
        mTrafficPackageQueryServiceAgent.removeQueryResultListener(Constant.SIM2);
        mTrafficPackageQueryServiceAgent.removeConnectionListener(mPackageQueryServiceClient);
    }

    public boolean isFirstEntry(Context context) {
    	//fengpeipei modify for 42555 start 
    	SharedPreferences sharedPreferences = context.getSharedPreferences("config_preferences",Context.MODE_PRIVATE);
    	return sharedPreferences.getBoolean(TrafficPreference.KEY_FIRST_ENTRY_FLAG, true);
        //return TrafficPreference.getBooleanPreference(context, TrafficPreference.KEY_FIRST_ENTRY_FLAG, true);
    	//fengpeipei modify for 42555 end 
    }

    public boolean resetFirstEntryFlag(Context context) {
    	//fengpeipei modify for 42555 start 
    	SharedPreferences sharedPreferences = context.getSharedPreferences("config_preferences",Context.MODE_PRIVATE);
    	sharedPreferences.edit().putBoolean(TrafficPreference.KEY_FIRST_ENTRY_FLAG, false).commit();
        //TrafficPreference.setBooleanPreference(context, TrafficPreference.KEY_FIRST_ENTRY_FLAG, false);
    	//fengpeipei modify for 42555 end
        return true;
    }

    public boolean isCalibrateControlerInited() {
        return mTrafficPackageQueryServiceAgent.isServiceBound();
    }

    public void setStartDate(Context context, int simIndex, int date) {
        TrafficPreference.setSimIntPreference(context, simIndex, TrafficPreference.KEY_START_DATE, date);
    }

    public int getStartDate(Context context, int simIndex) {
        return TrafficPreference.getSimIntPreference(context, simIndex, TrafficPreference.KEY_START_DATE, 1);
    }

    public void setWarnPercent(Context context, int simIndex, int percent) {
        TrafficPreference.setSimIntPreference(context, simIndex, TrafficPreference.KEY_WARNING_PERCENT,
                percent);
    }

    public int getWarnPercent(Context context, int simIndex) {
        return TrafficPreference.getSimIntPreference(context, simIndex,
                TrafficPreference.KEY_WARNING_PERCENT, 80);
    }

    public void setCommonTotalTaffic(Context context, int simIndex, int total) {
        TrafficPreference.setSimIntPreference(context, simIndex, TrafficPreference.KEY_COMMON_TOTAL, total);
    }

    public void setCommonTafficMonitor(Context context, int simIndex, boolean flag) {
        TrafficPreference.setSimBooleanPreference(context, simIndex, TrafficPreference.KEY_TRAFFIC_MONITOR, flag);
    }

    public boolean getCommonTafficMonitor(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex, TrafficPreference.KEY_TRAFFIC_MONITOR,false);
    }


    public int getCommonTotalTaffic(Context context, int simIndex) {
        return TrafficPreference.getSimIntPreference(context, simIndex, TrafficPreference.KEY_COMMON_TOTAL, 0);
    }

    public void setCommonUsedTaffic(Context context, int simIndex, float used) {
        TrafficPreference.setSimFloatPreference(context, simIndex, TrafficPreference.KEY_COMMON_USED, used);
    }

    public float getCommonUsedTaffic(Context context, int simIndex) {
        // Gionee: mengdw <2016-05-26> add for CR01707486 begin
        resetFlowData(context, simIndex);
        // Gionee: mengdw <2016-05-26> add for CR01707486 end
        float saveUsed = TrafficPreference.getSimFloatPreference(context, simIndex,
                TrafficPreference.KEY_COMMON_USED, 0);
        return saveUsed + getDiffActualFlow(context, simIndex);
    }

    public void setCommonLeftTraffic(Context context, int simIndex, float left) {
        TrafficPreference.setSimFloatPreference(context, simIndex, TrafficPreference.KEY_COMMON_LEFT, left);
    }

    public float getCommonLeftTraffic(Context context, int simIndex) {
        float saveLeft = TrafficPreference.getSimFloatPreference(context, simIndex,
                TrafficPreference.KEY_COMMON_LEFT, 0);
        return saveLeft - getDiffActualFlow(context, simIndex);
    }

    public boolean isCommonTrafficSurplus(Context context, int simIndex) {
        float left = getCommonLeftTraffic(context, simIndex);
        return left >= 0;
    }

    public boolean isHasIdleData(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_HAS_IDLE_DATA_FLAG, false);
    }

    public void setIdleTotal(Context context, int simIndex, int total) {
        TrafficPreference.setSimIntPreference(context, simIndex, TrafficPreference.KEY_IDLE_TOTAL, total);
    }

    public int getIdleTotal(Context context, int simIndex) {
        return TrafficPreference.getSimIntPreference(context, simIndex, TrafficPreference.KEY_IDLE_TOTAL, 0);
    }

    public void setIdleUsed(Context context, int simIndex, float used) {
        TrafficPreference.setSimFloatPreference(context, simIndex, TrafficPreference.KEY_IDLE_USED, used);
    }

    public float getIdleUsed(Context context, int simIndex) {
        return TrafficPreference.getSimFloatPreference(context, simIndex, TrafficPreference.KEY_IDLE_USED, 0);
    }

    public void setIdleLeft(Context context, int simIndex, float left) {
        TrafficPreference.setSimFloatPreference(context, simIndex, TrafficPreference.KEY_IDLE_LEFT, left);
    }

    public float getIdleLeft(Context context, int simIndex) {
        return TrafficPreference.getSimFloatPreference(context, simIndex, TrafficPreference.KEY_IDLE_LEFT, 0);
    }

    public void setCalibratedActualFlow(Context context, int simIndex, float actualFlow) {
        TrafficPreference.setSimFloatPreference(context, simIndex,
                TrafficPreference.KEY_CALIBRATED_ACTUAL_FLOW, actualFlow);
    }

    public float getCalibratedActualFlow(Context context, int simIndex) {
        return TrafficPreference.getSimFloatPreference(context, simIndex,
                TrafficPreference.KEY_CALIBRATED_ACTUAL_FLOW, 0);
    }

    public void setSaveActualFlow(Context context, int simIndex, float actualFlow) {
        TrafficPreference.setSimFloatPreference(context, simIndex, TrafficPreference.KEY_SAVE_ACTUAL_FLOW,
                actualFlow);
    }

    public float getSaveActualFlow(Context context, int simIndex) {
        return TrafficPreference.getSimFloatPreference(context, simIndex,
                TrafficPreference.KEY_SAVE_ACTUAL_FLOW, 0);
    }

    public void setTafficPackageSetted(Context context, int simIndex, boolean isSetted) {
        TrafficPreference.setSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_TRAFFIC_PACKAGE_SETTED_FLAG, isSetted);
    }

    public boolean isTafficPackageSetted(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_TRAFFIC_PACKAGE_SETTED_FLAG, false);
    }

    public void setFlowlinkFlag(Context context, int simIndex, int flag) {
        TrafficPreference
                .setSimIntPreference(context, simIndex, TrafficPreference.KEY_SIM_FLOWLINKFLAG, flag);
    }

    public int getFlowlinkFlag(Context context, int simIndex) {
        return TrafficPreference.getSimIntPreference(context, simIndex,
                TrafficPreference.KEY_SIM_FLOWLINKFLAG, 0);
    }

    public void setSimReset(Context context, int simIndex, boolean value) {
        TrafficPreference.setSimBooleanPreference(context, simIndex, TrafficPreference.KEY_SIM_RESET, value);
    }

    public boolean isSimReset(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex, TrafficPreference.KEY_SIM_RESET,
                false);
    }

    public void setStopWarningFlag(Context context, int simIndex, boolean value) {
        TrafficPreference.setSimBooleanPreference(context, simIndex, TrafficPreference.KEY_SIM_STOPWARNING,
                value);
    }

    public boolean isStopWarningFlag(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_SIM_STOPWARNING, true);
    }

    public void setStopExhaustFlag(Context context, int simIndex, boolean value) {
        TrafficPreference.setSimBooleanPreference(context, simIndex, TrafficPreference.KEY_SIM_STOP_EXHAUST,
                value);
    }

    public boolean isStopExhaustFlag(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_SIM_STOP_EXHAUST, true);
    }

    public void requestCalibrate(Context context, int simIndex) {
        int brandIndex = getSettedBrand(context, simIndex);
        int provinceIndex = getSettedProvice(context, simIndex);
        if (brandIndex != TrafficPreference.CALIBRATE_NO_SETTING && 
                provinceIndex != TrafficPreference.CALIBRATE_NO_SETTING) {
            setCalibrateState(context, simIndex, true);
            String[] arrayProvince = mContext.getResources().getStringArray(
                    R.array.calibrate_province_setting_array);
            String[] arrayBrand = mContext.getResources().getStringArray(
                    R.array.calibrate_setting_brand_array);
            // Gionee: mengdw <2016-12-17> add for CR01776636 begin
            //requsetOperaCalibrate(simIndex, arrayProvince[provinceIndex], arrayBrand[brandIndex]);
            /* setProvinceAndBrand(simIndex, arrayProvince[provinceIndex], arrayBrand[brandIndex]);
                    callServiceQuery(TrafficPackageQueryServiceAgent.QUERY_TYPE_TRAFFIC, simIndex);*/
            // Gionee: mengdw <2016-12-17> add for CR01776636 end
            Message msg = new Message();
            msg.what = MESSAGE_CALIBRATE_TIME_OUT;
            msg.arg1 = simIndex;
            mTrafficCalibrateControlerHander.sendMessageDelayed(msg, CALIBRATE_TIME_OUT_TIME);
        } else {
            setStatus(simIndex, STATUS_CALIBRATE_NO_QUERY_CODE);
            sendCalibrateResultBroadcast(simIndex, STATUS_CALIBRATE_NO_QUERY_CODE);
        }
    }

    public int getStauts(int simIndex) {
        return mStatusList[simIndex];
    }

    public void setStatus(int simIndex, int status) {
        mStatusList[simIndex] = status;
    }

    public boolean isCalibrateState(Context context, int simIndex) {
        return mCalibrateState[simIndex];
    }

    public void setCommonOnlyLeftFlag(Context context, int simIndex, boolean flag) {
        TrafficPreference.setSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_ONLY_LEFT_COMMON_FLAG, flag);
    }

    public boolean isCommonOnlyLeft(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_ONLY_LEFT_COMMON_FLAG, false);
    }

    public boolean isIdleOnlyLeftFlag(Context context, int simIndex) {
        return TrafficPreference.getSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_ONLY_LEFT_IDLE_FLAG, false);
    }

    public void saveSettedProvice(Context context, int simIndex, int provinceIndex) {
        TrafficPreference.setSimIntPreference(context, simIndex,
                TrafficPreference.KEY_CALIBRATE_PROVINCE_SETTING, provinceIndex);
    }

    public int getSettedProvice(Context context, int simIndex) {
        return TrafficPreference.getSimIntPreference(context, simIndex,
                TrafficPreference.KEY_CALIBRATE_PROVINCE_SETTING, TrafficPreference.CALIBRATE_NO_SETTING);
    }

    public void saveSeettedBrand(Context context, int simIndex, int brandIndex) {
        TrafficPreference.setSimIntPreference(context, simIndex,
                TrafficPreference.KEY_CALIBRATE_BRAND_SETTING, brandIndex);
    }

    public int getSettedBrand(Context context, int simIndex) {
        return TrafficPreference.getSimIntPreference(context, simIndex,
                TrafficPreference.KEY_CALIBRATE_BRAND_SETTING, TrafficPreference.CALIBRATE_NO_SETTING);
    }

    public void saveSettedCurrentDate(Context context, int simIndex, String date) {
        TrafficPreference.setSimStringPreference(context, simIndex, TrafficPreference.KEY_CURRENT_DATE, date);
    }

    public String getSettedCurrentDate(Context context, int simIndex) {
        int[] times = TimeFormat.getNowTimeArray();
        String curDate = times[0] + "-" + times[1] + "-"
                + times[2] + "-" + times[3] + "-" + times[4]
                + "-" + times[5];
        return TrafficPreference.getSimStringPreference(context, simIndex,
                TrafficPreference.KEY_CURRENT_DATE, curDate);
    }
    
    // Gionee: mengdw <2016-12-17> add for CR01776636 begin
    private void requsetOperaCalibrate(final int simIndex, final String province, final String brand) {
        Log.d(TAG, "requsetOperaCalibrate simIndex=" + simIndex + " province=" + province + " brand=" + brand);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setProvinceAndBrand(simIndex, province, brand);
                callServiceQuery(TrafficPackageQueryServiceAgent.QUERY_TYPE_TRAFFIC, simIndex);
            }
        }).start();
        
    }
    // Gionee: mengdw <2016-12-17> add for CR01776636 end
    
    // Gionee: mengdw <2016-05-26> add for CR01707486 begin
    private void resetFlowData(Context context, int simIndex) {
        if (isTafficPackageSetted(context, simIndex)) {
            try {
                String oldDate = getSettedCurrentDate(context, simIndex);
                String[] oldTimes = oldDate.split("-");
                long oldUtcTime = 0;
                oldUtcTime = TimeFormat.getStartTime(Integer.valueOf(oldTimes[0]), Integer.valueOf(oldTimes[1]),
                        Integer.valueOf(oldTimes[2]), Integer.valueOf(oldTimes[3]), Integer.valueOf(oldTimes[4]),
                        Integer.valueOf(oldTimes[5]));
                int[] currTimes = TimeFormat.getNowTimeArray();
                long currUtcTime = TimeFormat.getStartTime(currTimes[0], currTimes[1], currTimes[2], currTimes[3],
                        currTimes[4], currTimes[5]);
                if ((currTimes[1] != Integer.valueOf(oldTimes[1])) || (currUtcTime < oldUtcTime)) {
                    Log.d(TAG, "resetFlowData simIndex=" + simIndex);
                    setCommonUsedTaffic(context, simIndex, 0);
                    float total = getCommonTotalTaffic(context, simIndex);
                    setCommonLeftTraffic(context, simIndex, total);
                    setSaveActualFlow(context, simIndex, 0);
                }
            } catch (Exception e) {
                Log.d(TAG, "resetFlowData Exception e=" + e.toString());
            }
        }
    }
    // Gionee: mengdw <2016-05-26> add log for CR01580861 end
    
    private void callServiceQuery(int queryType, int simIndex) {
        if (mTrafficPackageQueryServiceAgent.isServiceBound()) {
            Log.d(TAG, "callServiceQuery SlotId=" + simIndex + " type=" + queryType);
            mTrafficPackageQueryServiceAgent.query(simIndex, queryType);
        } else {
            Log.d(TAG, "callServiceQuery not bind");
        }
    }

    private void setProvinceAndBrand(int simIndex, String province, String brand) {
        if (mTrafficPackageQueryServiceAgent.isServiceBound()) {
            mTrafficPackageQueryServiceAgent.setProvince(simIndex, province);
            mTrafficPackageQueryServiceAgent.setOperatorBrand(simIndex, brand);
        } else {
            Log.d(TAG, "setProvinceAndBrand not bind");
        }
    }

    private void setCalibrateState(Context context, int simIndex, boolean isCalibrated) {
        mCalibrateState[simIndex] = isCalibrated;
    }

    private float getDiffActualFlow(Context context, int simIndex) {
        checkCalibrateFlow(context, simIndex);
        float calibrateActualFlow = TrafficPreference.getSimFloatPreference(context, simIndex,
                TrafficPreference.KEY_CALIBRATED_ACTUAL_FLOW, 0);
        float saveActualFlow = TrafficPreference.getSimFloatPreference(context, simIndex,
                TrafficPreference.KEY_SAVE_ACTUAL_FLOW, 0);
        int day = TrafficPreference.getSimIntPreference(context, simIndex, TrafficPreference.KEY_START_DATE, 1);
        float actualFlow = TrafficassistantUtil.getActualFlow(context, simIndex, day);
        if (actualFlow >= calibrateActualFlow) {
            return actualFlow - calibrateActualFlow;
        } else if (saveActualFlow >= calibrateActualFlow) {
            return saveActualFlow - calibrateActualFlow;
        } else {
            return 0;
        }
    }

    private void checkCalibrateFlow(Context context, int simIndex) {
        if (!isTafficPackageSetted(context, simIndex)) {
            return;
        }
        String[] oldTimes = getSettedCurrentDate(context, simIndex).split("-");
        // Gionee: mengdw <2015-08-26> modify for CR01543245 begin
        long oldUtcTime = 0;
        try {
            oldUtcTime = TimeFormat.getStartTime(Integer.valueOf(oldTimes[0]), Integer.valueOf(oldTimes[1]),
                    Integer.valueOf(oldTimes[2]), Integer.valueOf(oldTimes[3]), Integer.valueOf(oldTimes[4]),
                    Integer.valueOf(oldTimes[5]));
        } catch (NumberFormatException e) {
            Log.e(TAG, "getoldUtcTime Exception :" + e.toString());
        }
        // Gionee: mengdw <2015-08-26> modify for CR01543245 end
        int[] currTimes = TimeFormat.getNowTimeArray();
        long currUtcTime = TimeFormat.getStartTime(currTimes[0], currTimes[1], currTimes[2], currTimes[3],
                currTimes[4], currTimes[5]);
        // Gionee: mengdw <2015-10-19> modify for CR01569888 begin
        try {
            if ((currTimes[1] != Integer.valueOf(oldTimes[1])) || (currUtcTime < oldUtcTime)) {
                Log.d(TAG, "checkCalibrateFlow reset CalibratedActualFlow");
                setCalibratedActualFlow(context, simIndex, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "reset Exception :" + e.toString());
        }
        // Gionee: mengdw <2015-10-19> modify for CR01569888 end
    }

    private TrafficCalibrateControler(Context context) {
        mContext = context.getApplicationContext();
        mTrafficPackageQueryServiceAgent = TrafficPackageQueryServiceAgent.getInstance(mContext);
        mPackageQueryServiceClient = new PackageQueryServiceClient();
        mTrafficPackageQueryServiceAgent.addConnectionListener(mPackageQueryServiceClient);
        mTrafficPackageQueryServiceAgent.addQueryResultListener(Constant.SIM1, mPackageQueryServiceClient);
        mTrafficPackageQueryServiceAgent.addQueryResultListener(Constant.SIM2, mPackageQueryServiceClient);
        mTrafficCalibrateControlerHander = new TrafficCalibrateControlerHander();
    }

    private void setIdleOnlyLeftFlag(Context context, int simIndex, boolean flag) {
        TrafficPreference.setSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_ONLY_LEFT_IDLE_FLAG, flag);
    }

    private void setHasIdleDataFlag(Context context, int simIndex, boolean flag) {
        TrafficPreference.setSimBooleanPreference(context, simIndex,
                TrafficPreference.KEY_HAS_IDLE_DATA_FLAG, flag);
    }

    private void processOperatorQueryResult(PackageQueryServiceData result) {
        int simIndex = result.getSlotIndex();
        int errorCode = result.getErrorCode();
        if (TrafficPackageQueryServiceAgent.QUERY_SUCCESS_CODE == errorCode) {
            String province = result.getProvince();
            String brand = result.getBrand();
            String[] arrayProvince = mContext.getResources().getStringArray(
                    R.array.calibrate_province_setting_array);
            String[] arrayBrand = mContext.getResources().getStringArray(
                    R.array.calibrate_setting_brand_array);
            if (!province.isEmpty()) {
                int provinceIndex = TrafficassistantUtil.getIndexByName(province, arrayProvince);
                saveSettedProvice(mContext, simIndex, provinceIndex);
            }
            if (!brand.isEmpty()) {
                int brandIndex = TrafficassistantUtil.getIndexByName(brand, arrayBrand);
                saveSeettedBrand(mContext, simIndex, brandIndex);
            }
        }
    }

    private void processCalibrationCommonData(PackageQueryServiceData result) {
        int CommonTotalFlow = !result.isCommonTotalInvalid() ? result.getCommonTotal() : 0;
        float commonUsedFlow = !result.isCommonUsedInvalid() ? result.getCommonUsed() : 0;
        float commonLeftFlow = !result.isCommonLeftInvalid() ? result.getCommonLeft() : 0;
        int slotIndex = result.getSlotIndex();

        if (result.isCommonTotalInvalid() && (!result.isCommonUsedInvalid())
                && (!result.isCommonLeftInvalid())) {
            CommonTotalFlow = (int) (commonUsedFlow + commonLeftFlow);
            setStopExhaustFlag(mContext, slotIndex, false);
        }
        if (result.isCommonUsedInvalid() && (!result.isCommonTotalInvalid())
                && (!result.isCommonLeftInvalid())) {
            commonUsedFlow = CommonTotalFlow - commonLeftFlow;
            setStopExhaustFlag(mContext, slotIndex, false);
            setStopWarningFlag(mContext, slotIndex, false);
        }
        if (result.isCommonLeftInvalid() && (!result.isCommonUsedInvalid())
                && (!result.isCommonTotalInvalid())) {
            commonLeftFlow = CommonTotalFlow - commonUsedFlow;
            setStopExhaustFlag(mContext, slotIndex, false);
            setStopWarningFlag(mContext, slotIndex, false);
        }

        setCommonOnlyLeftFlag(mContext, slotIndex, result.isCommonTotalInvalid() && result.isCommonUsedInvalid() && (!result.isCommonLeftInvalid()));
        // only used
        setTafficPackageSetted(mContext, slotIndex, !result.isCommonTotalInvalid() || !result.isCommonLeftInvalid() || (result.isCommonUsedInvalid()));

        setCommonTotalTaffic(mContext, slotIndex, CommonTotalFlow / Constant.UNIT);
        setCommonUsedTaffic(mContext, slotIndex, commonUsedFlow / Constant.UNIT);
        setCommonLeftTraffic(mContext, slotIndex, commonLeftFlow / Constant.UNIT);

        int startDay = getStartDate(mContext, slotIndex);
        float actualFlow = TrafficassistantUtil.getActualFlow(mContext, slotIndex, startDay);
        setCalibratedActualFlow(mContext, slotIndex, actualFlow);

        int[] times = TimeFormat.getNowTimeArray();
        String date = times[0] + "-" + times[1] + "-"
                + times[2] + "-" + times[3] + "-" + times[4]
                + "-" + times[5];
        saveSettedCurrentDate(mContext, slotIndex, date);
    }

    private void processCalibrationIdleData(PackageQueryServiceData result) {
        int idleTotalFlow = !result.isIdleTotalInvalid() ? result.getIdleTotal() : 0;
        float idleUsedFlow = !result.isIdleUsedInvalid() ? result.getIdleUsed() : 0;
        float idleLeftFlow = !result.isIdleLeftInvalid() ? result.getIdleLeft() : 0;
        int slotIndex = result.getSlotIndex();

        if (result.isIdleUsedInvalid() && result.isIdleTotalInvalid() && (!result.isCommonLeftInvalid())) {
            setIdleOnlyLeftFlag(mContext, slotIndex, true);
            setHasIdleDataFlag(mContext, slotIndex, true);
        }
        if ((!result.isIdleUsedInvalid()) && (!result.isIdleTotalInvalid()) && result.isIdleLeftInvalid()) {
            idleLeftFlow = idleTotalFlow - idleUsedFlow;
            setHasIdleDataFlag(mContext, slotIndex, true);
        }
        if ((!result.isIdleTotalInvalid()) && (!result.isIdleLeftInvalid()) && result.isIdleUsedInvalid()) {
            idleUsedFlow = idleTotalFlow - idleLeftFlow;
            setHasIdleDataFlag(mContext, slotIndex, true);
        }
        if (((!result.isIdleTotalInvalid()) && (!result.isIdleLeftInvalid()) && (!result.isIdleUsedInvalid()))
                || ((!result.isIdleLeftInvalid()) && (!result.isIdleUsedInvalid()))) {
            setHasIdleDataFlag(mContext, slotIndex, true);
        }
        setIdleTotal(mContext, slotIndex, idleTotalFlow / Constant.UNIT);
        setIdleUsed(mContext, slotIndex, idleUsedFlow / Constant.UNIT);
        setIdleLeft(mContext, slotIndex, idleLeftFlow / Constant.UNIT);

        if (!result.isHasIdleData()
                || (result.isIdleTotalInvalid() && result.isIdleLeftInvalid() && result.isIdleUsedInvalid())
                || (result.isIdleUsedInvalid() && result.isIdleLeftInvalid())) {
            setHasIdleDataFlag(mContext, slotIndex, false);
        }
    }

    private void sendCalibrateResultBroadcast(int simIndex, int status) {
        Intent intent = new Intent();
        intent.setAction(ACTION_CALIBRATE_RESULT);
        intent.putExtra(SIM_INDEX, simIndex);
        intent.putExtra(RESULT_CODE, status);
        mContext.sendBroadcast(intent);
    }

    public class TrafficCalibrateControlerHander extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int simIndex = msg.arg1;
            int errorCode = msg.arg2;
            Log.d(TAG, "TrafficCalibrateControlerHander receive msg=" + msg.what + " simIndex=" + simIndex);
            setCalibrateState(mContext, simIndex, false);
            switch (msg.what) {
                case MESSAGE_CALIBRATE_TIME_OUT:
                    setStatus(simIndex, STATUS_CALIBRATE_FAIL);
                    sendCalibrateResultBroadcast(simIndex, STATUS_CALIBRATE_FAIL);
                    break;
                case MESSAGE_CALIBRATE_RECEIVE_RESULT_SUCCESS:
                    setStatus(simIndex, STATUS_CALIBRATE_SUCCESS);
                    sendCalibrateResultBroadcast(simIndex, STATUS_CALIBRATE_SUCCESS);
                    mTrafficCalibrateControlerHander.removeMessages(MESSAGE_CALIBRATE_TIME_OUT);
                    break;
                case MESSAGE_CALIBRATE_RECEIVE_RESULT_FAIL:
                    if (TrafficPackageQueryServiceAgent.QUERY_RESULT_SMS_SEND_FIAL == errorCode
                            || TrafficPackageQueryServiceAgent.QUERY_RESULT_QUERY_CODE_ERROR == errorCode) {
                        setStatus(simIndex, STATUS_CALIBRATE_SEND_SMS_FIAL);
                        sendCalibrateResultBroadcast(simIndex, STATUS_CALIBRATE_SEND_SMS_FIAL);
                    } else {
                        setStatus(simIndex, STATUS_CALIBRATE_FAIL);
                        sendCalibrateResultBroadcast(simIndex, STATUS_CALIBRATE_FAIL);
                    }
                    mTrafficCalibrateControlerHander.removeMessages(MESSAGE_CALIBRATE_TIME_OUT);
                    break;
                default:
                    Log.d(TAG, "TrafficCalibrateControlerHander message invalid");
                    break;
            }
        }
    }

    private class PackageQueryServiceClient implements TrafficPackageQueryServiceAgent.QueryResultListener,
            TrafficPackageQueryServiceAgent.ServiceConnectionListener {

        @Override
        public void onConnected(IPackageQueryService service) {
            int simCount = TrafficassistantUtil.getSimCount(mContext);
            Log.d(TAG, "PackageQueryServiceClient onConnected simCount=" + simCount);
            if (simCount == 1) {
                int slotID = TrafficassistantUtil.getSingleCardSlotID(mContext);
                callServiceQuery(TrafficPackageQueryServiceAgent.QUERY_TYPE_OPERATOR, slotID);
            } else if (simCount == 2) {
                callServiceQuery(TrafficPackageQueryServiceAgent.QUERY_TYPE_OPERATOR, Constant.SIM1);
                callServiceQuery(TrafficPackageQueryServiceAgent.QUERY_TYPE_OPERATOR, Constant.SIM2);
            }
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "PackageQueryServiceClient onDisconnected");
        }

        @Override
        public void onQueryResult(PackageQueryServiceData result) {
            Log.d(TAG, "onQueryResult result:" + result);
            if (result != null) {
                int slotIndex = result.getSlotIndex();
                int queryType = result.getQueryType();
                int errorCode = result.getErrorCode();
                if (TrafficPackageQueryServiceAgent.QUERY_TYPE_OPERATOR == queryType) {
                    processOperatorQueryResult(result);
                } else {
                    setCalibrateState(mContext, slotIndex, false);
                    Message msg = new Message();
                    msg.arg1 = slotIndex;
                    msg.arg2 = errorCode;
                    if (TrafficPackageQueryServiceAgent.QUERY_SUCCESS_CODE == errorCode) {
                        processCalibrationCommonData(result);
                        processCalibrationIdleData(result);
                        msg.what = MESSAGE_CALIBRATE_RECEIVE_RESULT_SUCCESS;
                    } else {
                        msg.what = MESSAGE_CALIBRATE_RECEIVE_RESULT_FAIL;
                    }
                    mTrafficCalibrateControlerHander.sendMessage(msg);
                }
            }
        }

        @Override
        public void onReceivePackageQueryMessage(String senderAddress, String messageBody,
                long timestampMillis) {
            Log.d(TAG, "onReceivePackageQueryMessage");
        }
    }
}
//Gionee: mengdw <2015-12-28> add for CR01589343 end