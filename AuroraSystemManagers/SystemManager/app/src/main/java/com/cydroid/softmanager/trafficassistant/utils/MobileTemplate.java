package com.cydroid.softmanager.trafficassistant.utils;

import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.lang.reflect.Method;

import com.cydroid.framework.Common;
import com.cydroid.framework.FrameworkUtility;
import com.cydroid.framework.SIMInfoFactory;
import com.cydroid.framework.provider.SIMInfo;
import com.cydroid.softmanager.trafficassistant.SIMInfoWrapper;
import com.cydroid.softmanager.trafficassistant.SIMParame;
import android.content.Context;
import android.net.NetworkTemplate;
import android.telephony.TelephonyManager;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;

import com.cydroid.softmanager.utils.Log;

public class MobileTemplate {
    private static final String TEST_SUBSCRIBER_PROP = "test.subscriberid";
    private static final String TAG = "MobileTemplate";
    
    public static NetworkTemplate getTemplate(Context context, int slotId) {
        Log.d(TAG,"getTemplate slotId=" + slotId + " android version=" + Build.VERSION.SDK_INT);
        //Gionee: mengdw <2015-11-21> modify for CR01596034 Android M  begin
        NetworkTemplate template;
        if (Build.VERSION.SDK_INT >= 23) {
            TelephonyManager tele = TelephonyManager.from(context);
            //Gionee: mengdw <2016-02-26> modify for CR01640579 begin
            template = buildTemplateMobileAll(getActiveSubscriberId(context, getSubIdBySoltId(slotId), tele));
            //Gionee: mengdw <2016-02-26> modify for CR01640579 end
            // Gionee: mengdw <2016-08-10> modify for CR01738223 begin
            // template = NetworkTemplate.normalize(template, tele.getMergedSubscriberIds());
            template = normalize(template, context, slotId);
            // Gionee: mengdw <2016-08-10> modify for CR01738223 end
        } else {
            template = buildTemplateMobileAll(getSubscriberId(context, slotId));
            template = normalize(template, context, slotId);
        }
       //Gionee: mengdw <2015-11-21> modify for CR01596034 Android M  end
        return template;
    }
    //Gionee: mengdw <2015-11-21> add for CR01596034 Android M  begin
    private static String getActiveSubscriberId(Context context, int subId,TelephonyManager tele) {
        String retVal = tele.getSubscriberId(subId);
        Log.d(TAG, "getActiveSubscriberId=" + retVal + " subId=" + subId);
        return retVal;
    }
   //Gionee: mengdw <2015-11-21> add for CR01596034 Android M  end
    public static String getSubscriberId(Context context, int slotId) {
        long simId = getSimNo(context, slotId);
        //Gionee: mengdw <2015-11-21> modify for CR01596034 Android M  begin
        String imsiId = "";
        if(Build.VERSION.SDK_INT >= 23) {
            TelephonyManager tele = TelephonyManager.from(context);
            imsiId=getActiveSubscriberId(context, getSubIdBySoltId(slotId), tele);
        } else {
            imsiId = getImsi(simId);
        }
        Log.d(TAG,"getSubscriberId slotId=" + slotId +" simId=" + simId + " imsiId=" +imsiId);
        //Gionee: mengdw <2015-11-21> modify for CR01596034 Android M  end
        // isMobileDataAvailable(simId);
        return imsiId;
    }
    
    // Gionee: mengdw <2016-02-26> add for CR01640598 begin
    private static int getSubIdBySoltId(int soltID) {
        int subid = 1;
        try {
            int[] subIds = SubscriptionManager.getSubId(soltID);
            if(subIds!=null) {
                subid = subIds[0];
            }
        } catch(Exception  e) {
            Log.d(TAG,"getSubIdBySoltId Exception e=" + e.toString());
        }
        Log.d(TAG,"getSubIdBySoltId soltID=" + soltID + " subid=" + subid);
        return subid;
    }
    // Gionee: mengdw <2016-02-26> add for CR01640598 end

    private static String getImsi(long simId) {
        Object telephonyManager = FrameworkUtility.invokeStaticMethod(TelephonyManager.class, "getDefault");
        if (Build.VERSION.SDK_INT >= 22) { // android5.1
            return (String) FrameworkUtility.invokeMethod(TelephonyManager.class, telephonyManager,
                    "getSubscriberId", new Class[] {int.class}, new Object[] {(int) simId});
        } else {
            return (String) FrameworkUtility.invokeMethod(TelephonyManager.class, telephonyManager,
                    "getSubscriberId", new Class[] {long.class}, new Object[] {simId});
        }
    }

    private static long getSimNo(Context context, int slot) {
        long simId = -1;
        SIMInfoWrapper wrapper = SIMInfoWrapper.getDefault(context);

        if (wrapper.getInsertedSimCount() == 1) {
            simId = wrapper.getInsertedSimInfo().get(0).mSimId;
        } else if (wrapper.getInsertedSimCount() == 2) {
            if (slot > 0) {
                simId = wrapper.getInsertedSimInfo().get(slot).mSimId;
            } else {
                simId = -1;
            }
        }

        return simId;
    }

    /*
     * public static void setMobileDataEnabled(Context context, boolean enabled)
     * { final TelephonyManager mTelephonyManager =
     * TelephonyManager.from(context);
     * mTelephonyManager.setDataEnabled(enabled); }
     */

    /*
     * public static boolean isMobileDataAvailable(long subId) { long
     * defaultDataSubId = SubscriptionManager.getDefaultDataSubId();
     * Log.d("imsiid", "defaultDataSubId : " + defaultDataSubId + " : "
     * +(defaultDataSubId == subId)); return defaultDataSubId == subId; }
     */
    // Gionee: mengdw <2016-08-10> modify for CR01738223 begin
    private static boolean isMTKCdmaLteDcSupport() {
        return SystemProperties.get("ro.mtk_svlte_support").equals("1")
                || SystemProperties.get("ro.mtk_srlte_support").equals("1");
    }
    // Gionee: mengdw <2016-08-10> modify for CR01738223 end

    private static void mtkFillTemplateForCdmaLte(Context context, NetworkTemplate template, int slotId) {
        if (isMTKCdmaLteDcSupport()) {
            Log.d(TAG, "subId:" + slotId);
            final Object teleEx = getMTKDefaultTelephonyManagerEx();
            int simId = 1;
            SIMInfo simInfo = SIMInfoFactory.getDefault().getSIMInfoBySlot(context, slotId);
            if(simInfo != null) {
                simId = (int) simInfo.mSimId;
            }
            String svlteSubscriberId = getMtkSubscriberIdForLteDcPhone(teleEx, (int)simId);
            Log.e(TAG, "getDefaultTelephonyManagerEx  svlteSubscriberId1:" + svlteSubscriberId);            
                       
            if (!(TextUtils.isEmpty(svlteSubscriberId)) && svlteSubscriberId.length() > 0) {
                Log.d(TAG, "bf:" + template);
                addMTKMatchSubscriberIds(template, svlteSubscriberId);
                Log.d(TAG, "af:" + template);
            }
        }
    }

    private static Object getMTKDefaultTelephonyManagerEx() {
        Object result = null;
        try {
            Class<?> tclass = (Class<?>) Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            Method method = tclass.getDeclaredMethod("getDefault");
            result = method.invoke(null);
        } catch (Exception ex) {
            Log.e(TAG, "getDefaultTelephonyManagerEx  Exception:" + ex);
            ex.printStackTrace();
        }
        return result;
    }

    private static String getMtkSubscriberIdForLteDcPhone(Object teleEx, int subId) {
        String result = null;
        try {
            Class<?> tclass = (Class<?>) Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            Method method = tclass.getDeclaredMethod("getSubscriberIdForLteDcPhone", int.class);
            result = (String) method.invoke(teleEx, subId);
        } catch (Exception ex) {
            Log.e(TAG, "getSubscriberIdForLteDcPhone  Exception:" + ex);
            ex.printStackTrace();
        }
        return result;
    }
    
    // Gionee: mengdw <2016-08-10> modify for CR01738223 begin
    private static void addMTKMatchSubscriberIds(NetworkTemplate template, String SubscriberId) {
        try {
            Class<NetworkTemplate> nclass = NetworkTemplate.class;
            Method method = nclass.getDeclaredMethod("addMatchSubscriberIds", String.class);
            method.invoke(template, SubscriberId);
        } catch (NoSuchMethodException ex) {
            Log.d(TAG, " addMTKMatchSubscriberIds NoSuchMethodException");
            addMatchSubscriberId(template, SubscriberId);
        } catch (Exception ex) {
            Log.e(TAG, "addMatchSubscriberIds  Exception:" + ex);
            ex.printStackTrace();
        }
    }

    public static void addMatchSubscriberId(NetworkTemplate template, String SubscriberId) {
        try {
            Class<NetworkTemplate> nclass = NetworkTemplate.class;
            Method method = nclass.getDeclaredMethod("addMatchSubscriberId", String.class);
            method.invoke(template, SubscriberId);
        } catch (Exception ex) {
            Log.e(TAG, "addMatchSubscriberId  Exception:" + ex);
            ex.printStackTrace();
        }
    }
    // Gionee: mengdw <2016-08-10> modify for CR01738223 end
    
    private static String[] getMTKMergedSubscriberIds(Context context) {
        return (String[]) FrameworkUtility.invokeMethod(TelephonyManager.class,
                TelephonyManager.from(context), "getMergedSubscriberIds");
    }

    private static NetworkTemplate normalize(NetworkTemplate template, String[] merged) {
        if (Build.VERSION.SDK_INT >= 22) {// android 5.1
            return (NetworkTemplate) FrameworkUtility.invokeStaticMethod(NetworkTemplate.class, "normalize",
                    new Class[] {NetworkTemplate.class, String[].class}, new Object[] {template, merged});
        }
        return template;
    }

    private static NetworkTemplate normalize(NetworkTemplate template, Context context, int simIndex) {
        // Gionee: mengdw <2016-08-10> add for CR01738223 begin
        Log.d(TAG, "normalize simIndex=" + simIndex + " Platform()=" + Common.getPlatform());
        // Gionee: mengdw <2016-08-10> add for CR01738223 end
        if (Build.VERSION.SDK_INT >= 22 && Common.getPlatform().equals(Common.MTK_PLATFORM)) {
            try {
                mtkFillTemplateForCdmaLte(context, template, simIndex);
                template = normalize(template, getMTKMergedSubscriberIds(context));
            } catch (Exception e) {
                Log.e(TAG, "mtk normalize ", e);
            }
        }
        return template;
    }

}
