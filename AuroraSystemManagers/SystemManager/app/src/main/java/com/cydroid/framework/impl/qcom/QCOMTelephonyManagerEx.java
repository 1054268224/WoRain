package com.cydroid.framework.impl.qcom;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import  com.cydroid.softmanager.utils.Log;
import com.android.internal.telephony.ITelephony;
import com.cydroid.framework.IGnTelePhonyManager;

import android.telephony.TelephonyManager;
import android.provider.Settings;

public class QCOMTelephonyManagerEx implements IGnTelePhonyManager {

    private static final QCOMTelephonyManagerEx mInstance = new QCOMTelephonyManagerEx();

    public static QCOMTelephonyManagerEx getDefault() {
        // TODO Auto-generated method stub
        return mInstance;
    }

    public int getSimIndicatorStateGemini(int slotId) {
        // TODO Auto-generated method stub
        return getSimState(slotId);
    }

    // private ITelephony getITelephony() {
    // return
    // ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
    // }

    // gionee gaoj 2011-12-1 added for 8255 end

    public int getSimState(int slotId) {
        Log.i("lyblyb", "MTKTelephonyManagerEx");
        return 1;// MSimTelephonyManager.getDefault().getSimState(slotId);
    }

    public int getNetworkType() {
        return TelephonyManager.NETWORK_TYPE_UNKNOWN;
    }

    /**
     * Returns the unique device ID of a subscription, for example, the IMEI for GSM and the MEID for CDMA
     * phones. Return null if device ID is not available.
     * 
     * <p>
     * Requires Permission: {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * 
     * @param subscription
     *            of which deviceID is returned
     */
    public String getDeviceId(int subscription) {

        return "111";// MSimTelephonyManager.getDefault().getDeviceId(subscription);

    }

    // gionee jiaoyuan 20130131 add by CR00770292 end
    /**
     * Returns the unique subscriber ID, for example, the IMSI for a GSM phone for a subscription. Return null
     * if it is unavailable.
     * <p>
     * Requires Permission: {@link android.Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * 
     * @param subscription
     *            whose subscriber id is returned
     */
    public String getSubscriberId(int subscription) {
        return "1";// MSimTelephonyManager.getDefault().getSubscriberId(subscription);
    }

    /**
     * Returns the Service Provider Name (SPN) of a subscription.
     * <p>
     * Availability: SIM state must be {@link #SIM_STATE_READY}
     * 
     * @see #getSimState
     * 
     * @hide
     */
    public String getSimOperatorName(int subscription) {
        // return
        // MSimTelephonyManager.getDefault().getSimOperatorName(subscription);
        return null;
    }

    // Gionee guoyx 20130221 add for CR00773050 begin
    public boolean setPreferredDataSubscription(int subscription) {
        return false;// MSimTelephonyManager.getDefault().setPreferredDataSubscription(subscription);
    }

    public int getPreferredDataSubscription() {
        return 1;// MSimTelephonyManager.getDefault().getPreferredDataSubscription();
    }

    // Gionee guoyx 20130221 add for CR00773050 end

    // gionee jiaoyuan 20130305 add for CR00779101 begin
    public String getMultiSimName(Context context, int subscription) {
        // return Settings.System.getString(context.getContentResolver(),
        // Settings.Global.MULTI_SIM_NAME[subscription]);
        return null;
    }

    // gionee jiaoyuan 20130305 add for CR00779101 end

    // Gionee: <fengxb><2013-1-15> add for CR01004495 begin
    public boolean isNetworkRoaming(int subscription) {
        return false;// MSimTelephonyManager.getDefault().isNetworkRoaming(subscription);
    }

    public int getNetworkType(int subscription) {
        return 1;// MSimTelephonyManager.getDefault().getNetworkType(subscription);
    }

    public int getDataState(int subscription) {
        return 0;// MSimTelephonyManager.getDefault().getDataState();
    }
    // Gionee: <fengxb><2013-1-15> add for CR01004495 end
}
