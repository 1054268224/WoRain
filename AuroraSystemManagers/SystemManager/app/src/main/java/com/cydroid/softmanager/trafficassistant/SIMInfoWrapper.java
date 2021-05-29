//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.cydroid.framework.FrameworkUtility;
import com.cydroid.framework.SIMInfoFactory;
import com.cydroid.framework.provider.SIMInfo;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SIMInfoWrapper {
    private static final String TAG = "SIMInfoWrapper";

    private final TelephonyManager mTelephonyManager;
    // Gionee: mengdw <2016-12-30> add for 56023 begin
    private static final int INVALID_SIM_SLOT_INDEX = -1;
    // Gionee: mengdw <2016-12-30> add for 56023 end

    // Gionee: mengdw <2016-05-10> modify for CR01693943 begin
    private static final List<SIMParame> sInsertedSimInfoList = Collections.synchronizedList(new ArrayList<SIMParame>());
    // Gionee: mengdw <2016-05-10> modify for CR01693943 end
    private static SIMInfoWrapper sSIMInfoWrapper;
    private static Context mContext;
    private int mInsertedSimCount = 0;

    private SIMInfoWrapper(Context context) {
        mTelephonyManager = TelephonyManager.from(context);
        List<SIMInfo> simInfoList = SIMInfoFactory.getDefault().getInsertedSIMList(context);
        if (simInfoList == null) {
            return;
        }
        mInsertedSimCount = simInfoList.size();
        sInsertedSimInfoList.clear();
        SIMParame simParame = null;
        for (SIMInfo siminfo : simInfoList) {
            simParame = new SIMParame();
            simParame.mSimId = (siminfo.mSimId > 0) ? siminfo.mSimId : 0;
            simParame.mDisplayName = siminfo.mDisplayName;
            simParame.mSlot = (siminfo.mSlot > 0) ? siminfo.mSlot : 0;
            // Gionee: mengdw <2015-11-11> modify for CR01589343 begin
            simParame.mNumber = siminfo.mNumber;
            sInsertedSimInfoList.add(simParame);
            Log.d(TAG, "SIMInfoWrapper : " + simParame.mSlot + "," + simParame.mDisplayName + ","
                    + simParame.mSimId + " siminfo.mNumber=" + siminfo.mNumber + " siminfo.mICCId="
                    + siminfo.mICCId + " siminfo.mDispalyNumberFormat=" + siminfo.mDispalyNumberFormat);
            // Gionee: mengdw <2015-11-11> modify for CR01589343 end
        }
    }

    public static SIMInfoWrapper getDefault(Context context) {
        mContext = context.getApplicationContext();

        if (sSIMInfoWrapper == null) {
            sSIMInfoWrapper = new SIMInfoWrapper(context.getApplicationContext());
        }
        return sSIMInfoWrapper;
    }

    public static void setEmptyObject(Context context) {
        if (sSIMInfoWrapper != null) {
            sSIMInfoWrapper = null;
        }
    }

    public SIMParame getSimInfoBySlot(int slot) {
        SIMParame simInfo = null;
        for (int i = 0; i < mInsertedSimCount; i++) {
            simInfo = sInsertedSimInfoList.get(i);
            if (simInfo.mSlot == slot)
                return simInfo;
        }
        return null;// simInfo;
    }

    public int getInsertedSimCount() {
        return mInsertedSimCount;
    }

    public List<SIMParame> getInsertedSimInfo() {
        return sInsertedSimInfoList;
    }

    private int getSlotIdCompat(int defaultDataSubId) {
        if (Build.VERSION.SDK_INT >= 26) { //android O
            return getSlotIndex(defaultDataSubId);
        } else {
            // slotId = SubscriptionManager.getSlotId(subId);
            return (int) FrameworkUtility.invokeStaticMethod(SubscriptionManager.class,
                    "getSlotId", new Class[]{int.class}, new Object[]{defaultDataSubId});
        }
    }

    /*guoxt modify for 8.0 interface begin*/
    public int getSlotIndex(int defaultDataSubId) {
        int id = 0;

        try {
            Class<?> clazz = Class.forName("android.telephony.SubscriptionManager");
            Method method = clazz.getMethod("getSlotIndex", int.class);
            id = (int) method.invoke(null, defaultDataSubId);
            Log.d(TAG, "invoke : --------->" + id);

        } catch (Exception ex) {
            Log.e(TAG, "forceStopPackage exception:" + ex.toString());
        }
        return id;
    }
    /*guoxt modify for 8.0 interface end*/


    public int getSimIndex_CurrentNetworkActivated() {
        // Gionee <jianghuan> <2013-12-13> modify for CR00972909 begin
        int mNetworkIndex = -1;
        int simid = SIMInfoFactory.getDefault().getDefaultDataSubId();
        // guoxt modify for 49550 begin
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        /* guoxt modify for 8.0 interface begin*/
        // int slotId = SubscriptionManager.getSlotId(defaultDataSubId);
        int slotId = getSlotIdCompat(defaultDataSubId);
        /* guoxt modify for 8.0 interface end*/

        for (SIMParame siminfo : sInsertedSimInfoList) {
            // Gionee: mengdw <2015-08-25> modify for CR01543192 begin
            // if (null != siminfo && siminfo.mSimId == simid) {
            if (null != siminfo && siminfo.mSlot == slotId) {
                mNetworkIndex = siminfo.mSlot;
            }
            // guoxt modify for 49550 end
            // Gionee: mengdw <2015-08-25> modify for CR01543192 end
        }
        // Gionee: mengdw <2016-12-30> add for 56023 android N begin
        Log.d(TAG, "getSimIndex_CurrentNetworkActivated android version=" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 24) {// android N
            mNetworkIndex = getOpenDatatSlotId();
        }
        // Gionee: mengdw <2016-12-30> add for 56023 android N end
        return mNetworkIndex;
    }

    // Gionee: mengdw <2016-12-30> add for 56023 android N begin
    private int getOpenDatatSlotId() {
        int slotId = INVALID_SIM_SLOT_INDEX;
        try {
            int subId = SubscriptionManager.getDefaultDataSubscriptionId();
            /*guoxt modify for 8.0 interface begin*/
            // slotId = SubscriptionManager.getSlotId(subId);
            slotId = getSlotIdCompat(subId);
            /*guoxt modify for 8.0 interface end*/
            Log.d(TAG, "getOpenDatatSlotId subId=" + subId + " slotId=" + slotId);
        } catch (Exception e) {
            Log.d(TAG, "getOpenDatatSlotId e=" + e.toString());
        }
        return slotId;
    }
    // Gionee: mengdw <2016-12-30> add for 56023 android N end

    public boolean gprsIsOpenMethod(String methodName) {
        /*
         * ConnectivityManager mCM = (ConnectivityManager)
         * mContext.getSystemService(Context.CONNECTIVITY_SERVICE); Class[]
         * argClasses = null; Object[] argObject = null;
         * 
         * Boolean isOpen = false; try { Method method =
         * mCM.getClass().getMethod(methodName, argClasses); isOpen = (Boolean)
         * method.invoke(mCM, argObject); } catch (Exception e) {
         * e.printStackTrace(); } return isOpen;
         */
        // Gionee: mengdw <2016-12-16> add for CR01776613 begin
        try {
            boolean isEnable = mTelephonyManager.getDataEnabled();
            Log.d(TAG, "gprsIsOpenMethod isEnable=" + isEnable);
            return isEnable;
        } catch (Exception e) {
            Log.d(TAG, "gprsIsOpenMethod Exception e=" + e.toString());
            return false;
        }
        // Gionee: mengdw <2016-12-16> add for CR01776613 end
    }

    public void setGprsEnable(String methodName, boolean isEnable) {
        /*
         * ConnectivityManager mCM = (ConnectivityManager)
         * mContext.getSystemService(Context.CONNECTIVITY_SERVICE); Class[]
         * argClasses = new Class[1]; argClasses[0] = boolean.class;
         * 
         * try { Method method = mCM.getClass().getMethod(methodName,
         * argClasses); method.invoke(mCM, isEnable); } catch (Exception e) {
         * e.printStackTrace(); }
         */
        mTelephonyManager.setDataEnabled(isEnable);
    }

    public boolean isWiFiActived() {
        ConnectivityManager connectMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetInfo.isConnected();
    }

    //Gionee: mengdw <2015-11-11> modify for CR01589343 begin
    public String getSimNumberCurrentNetworkActivated() {
        String simNum = "";
        int simid = SIMInfoFactory.getDefault().getDefaultDataSubId();
        for (SIMParame siminfo : sInsertedSimInfoList) {
            if (null != siminfo && siminfo.mSimId == simid) {
                simNum = siminfo.mNumber;
            }
        }
        return simNum;
    }
    //Gionee: mengdw <2015-11-11> modify for CR01589343 end
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end
