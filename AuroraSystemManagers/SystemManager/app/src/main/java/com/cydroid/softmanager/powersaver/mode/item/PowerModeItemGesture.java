package com.cydroid.softmanager.powersaver.mode.item;

import com.cydroid.softmanager.powersaver.utils.PowerConfig;
import com.cydroid.softmanager.powersaver.utils.PowerServiceProviderHelper;
import com.cydroid.softmanager.utils.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.cydroid.softmanager.R;
import com.goodix.gesture.Gesture;
import android.content.Context;
import android.os.SystemProperties;

import cyee.provider.CyeeSettings;

public class PowerModeItemGesture extends PowerModeItem {

    private static final int NODE_TYPE_TPWAKESWITCH_DOUBLE_WAKE = 8;
    private static final int NODE_TYPE_TPWAKESWITCH_GESTURE_WAKE = 10;
    private static final int NODE_TYPE_TPWAKESWITCH_GESTURE_CONFIG = 11;

    private static final int CONFIGTYPE_SMART = 1;
    private static final int CONFIGTYPE_TOUCHLESS = 2;
    private static final int CONFIGTYPE_SCREENOFF = 11;
    private static final int CONFIGTYPE_DOUBLECLICK = 12;

    public PowerModeItemGesture(Context context, String mode, PowerServiceProviderHelper providerHelper) {
        super(context, mode, providerHelper);
    }

    private int getGestureNodeValue(Context context, int nodeType) {
        Log.d(TAG, "enter getGestureNodeValue type=" + nodeType);
        int result = 0;
        //Chenyee guoxt modify for CSW1707A-1160 begin
        Object pm = (Object) (context.getSystemService("chenyeeserver"));
        try {
            Class cls = Class.forName("android.os.chenyeeserver.ChenyeeServerManager");
            Method method = cls.getMethod("GetNodeState", int.class);
            result = (Integer) method.invoke(pm, nodeType);
        } catch (RuntimeException re) {
            Log.d(TAG, "read gesture node value throw exception");
            return 0;
        } catch (Exception ex) {
            Log.d(TAG, "read gesture node value throw exception");
            return 0;
        }
        //Chenyee guoxt modify for CSW1707A-1160 end
        Log.d(TAG, "enter getGestureNodeValue type=" + nodeType + " res=" + result);
        return result;
    }

    private void setGestureConfigVaue(Context context, boolean status) {
        if (status) {
            int value = CyeeSettings.getInt(context.getContentResolver(), "black_gesture_config_value", 0);
            writeGestureNodeValue(context, NODE_TYPE_TPWAKESWITCH_GESTURE_CONFIG, value);
        } else {
            writeGestureNodeValue(context, NODE_TYPE_TPWAKESWITCH_GESTURE_CONFIG, 0);
        }
    }

    private void writeGestureNodeValue(Context context, int nodeType, int value) {
        Log.d(TAG, "enter writeGestureNodeValue type=" + nodeType + " val=" + value);
        //Chenyee guoxt modify for CSW1707A-1160 begin
        Object pm = (Object) (context.getSystemService("chenyeeserver"));
        try {
            Class cls = Class.forName("android.os.chenyeeserver.ChenyeeServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            method.invoke(pm, nodeType, value);
        } catch (RuntimeException re) {
            Log.e(TAG, "write gesture node value throw exception", re);
            return;
        } catch (Exception ex) {
            Log.d(TAG, "write gesture node value throw exception");
            return;
        }
        //Chenyee guoxt modify for CSW1707A-1160 end
    }

    private boolean isSmartGestruesOn() {
        Log.d(TAG, "enter isSmartGestruesOn");
        return CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.GN_SSG_SWITCH, 0) == 1;
    }

    private boolean isTouchlessGestruesOn() {
        Log.d(TAG, "enter isTouchlessGestruesOn");
        return CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.GN_DG_SWITCH, 0) == 1;
    }

    private boolean isScreenOffGestureOn() {
        // Gionee <yangxinruo> <2015-10-09> modify for CR01562504 begin
        // Gionee <yangxinruo> <2016-1-27> modify for CR01632006 begin
        Log.d(TAG, "enter isScreenOffGestureOn mIsGoodixVendor=" + isUseGoodixSupport());
        boolean enable = false;
        // if (mIsGoodixVendor) {
        enable = CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.SSG_QUICK_OPERATING,
                0) == 1;
        // } else {
        // int value = getGestureNodeValue(mContext, NODE_TYPE_TPWAKESWITCH_GESTURE_WAKE);
        // enable = (value == 1) ? true : false;
        // }
        Log.d(TAG, "enter isScreenOffGestureOn " + enable);
        return enable;
        // Gionee <yangxinruo> <2016-1-27> modify for CR01632006 end
        // Gionee <yangxinruo> <2015-10-09> modify for CR01562504 end
    }

    private boolean isUseGoodixSupport() {
        return SystemProperties.get("ro.gn.gesture.vendor").equals("goodix");
    }

    private boolean isDoubleClickGestureOn() {
        // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 begin
        /*
        int value = getGestureNodeValue(mContext, NODE_TYPE_TPWAKESWITCH_DOUBLE_WAKE);
        boolean enable = (value == 1) ? true : false;
        return enable;
        */
        boolean enable = false;
        enable = CyeeSettings.getInt(mContext.getContentResolver(), CyeeSettings.SSG_DOUBLECLICK_WAKE,
                0) == 1;
        Log.d(TAG, "enter isDoubleClickGestureOn " + enable);
        return enable;
        // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 end
    }

    private void setSmartGestrues(boolean enabled) {
        int value = enabled ? 1 : 0;
        Log.d(TAG, "enter setSmartGestrues enable=" + enabled);
        CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.GN_SSG_SWITCH, value);
    }

    private void setTouchlessGestrues(boolean enabled) {
        Log.d(TAG, "enter setTouchlessGestrues enable=" + enabled);
        // Gionee <yangxinruo> <2015-09-14> add for CR01551236 begin
        boolean isNoSupportTouchlessGestrues = "no"
                .equals(SystemProperties.get("ro.gn.distancegesture.support", "yes"));
        if (isNoSupportTouchlessGestrues) {
            return;
        }
        // Gionee <yangxinruo> <2015-09-14> add for CR01551236 end
        int value = enabled ? 1 : 0;
        CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.GN_DG_SWITCH, value);
    }

    // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 begin
    private void setScreenOffGesture(boolean enabled, boolean needSetNode) {
        // Gionee <yangxinruo> <2015-10-09> modify for CR01562504 begin
        // Gionee <yangxinruo> <2016-1-27> modify for CR01632006 begin
        boolean isGoodixVendor = isUseGoodixSupport();
        Log.d(TAG, "enter setScreenOffGesture enable=" + enabled + " needSetNode=" + needSetNode + " goodix="
                + isGoodixVendor);
        if (needSetNode) {
            if (isGoodixVendor) {
                Gesture.setGestureEnabledAll(enabled);
            } else {
                setGestureConfigVaue(mContext, enabled);
                writeGestureNodeValue(mContext, NODE_TYPE_TPWAKESWITCH_GESTURE_WAKE, enabled ? 1 : 0);
            }
        }
        CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SSG_QUICK_OPERATING,
                enabled ? 1 : 0);
        // Gionee <yangxinruo> <2016-1-27> modify for CR01632006 end
        // Gionee <yangxinruo> <2015-10-09> modify for CR01562504 end
    }

    private void setDoubleClickWake(boolean enabled, boolean needSetNode) {
        Log.d(TAG, "enter setDoubleClickWake enable=" + enabled + " needSetNode=" + needSetNode);
        if (needSetNode) {
            writeGestureNodeValue(mContext, NODE_TYPE_TPWAKESWITCH_DOUBLE_WAKE, enabled ? 1 : 0);
        }
        CyeeSettings.putInt(mContext.getContentResolver(), CyeeSettings.SSG_DOUBLECLICK_WAKE,
                enabled ? 1 : 0);
    }
    // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 end

    @Override
    public void save() {
        mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_SMART, isSmartGestruesOn());
        mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_TOUCHLESS, isTouchlessGestruesOn());
        mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_SCREENOFF, isScreenOffGestureOn());
        mProviderHelper.putBoolean(mCheckpointKey + CONFIGTYPE_DOUBLECLICK, isDoubleClickGestureOn());
    }

    @Override
    public boolean restore(boolean isForceRestore) {
        Log.d(TAG, "enter restore gesture");
        // Gionee <yangxinruo> <2016-4-15> add for CR01677194 begin
        boolean needSetNode = true;
        // Gionee <yangxinruo> <2016-4-15> add for CR01677194 end
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_SMART)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_SMART))) {
            // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 begin
            needSetNode = mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_SMART, false);
            setSmartGestrues(needSetNode);
            // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 end
        }
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_TOUCHLESS)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_TOUCHLESS))) {
            setTouchlessGestrues(mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_TOUCHLESS, false));
        }
        // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 begin
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_SCREENOFF)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_SCREENOFF))) {
            //guoxt modify for CSW1805A-1045 begin
            setScreenOffGesture(mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_SCREENOFF, false),
                    true);
            //guoxt modify for CSW1805A-1045 end
        }
        if (mProviderHelper.hasKey(mCheckpointKey + CONFIGTYPE_DOUBLECLICK)
                && (isForceRestore || !isCurrentSettingsChangedByExternal(CONFIGTYPE_DOUBLECLICK))) {
            setDoubleClickWake(mProviderHelper.getBoolean(mCheckpointKey + CONFIGTYPE_DOUBLECLICK, false),
                    needSetNode);
        }
        // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 end
        return true;
    }

    private boolean isCurrentSettingsChangedByExternal(int configtype) {
        if (!hasConfig()) {
            return true;
        }
        boolean conVal = mProviderHelper.getBoolean(mConfigKey, false);
        switch (configtype) {
            case CONFIGTYPE_SMART:
                return isSmartGestruesOn() != conVal;
            case CONFIGTYPE_TOUCHLESS:
                return isTouchlessGestruesOn() != conVal;
            case CONFIGTYPE_SCREENOFF:
                return isScreenOffGestureOn() != conVal;
            case CONFIGTYPE_DOUBLECLICK:
                return isDoubleClickGestureOn() != conVal;
            default:
                return true;
        }

    }

    @Override
    public boolean apply() {
        if (!hasConfig()) {
            Log.d(TAG, "no settings " + mConfigKey + " ,do nothing");
            return true;
        }
        Log.d(TAG, "apply setting from key:" + mConfigKey);
        boolean conVal = mProviderHelper.getBoolean(mConfigKey, false);
        setSmartGestrues(conVal);
        setTouchlessGestrues(conVal);
        // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 begin
        setScreenOffGesture(conVal, true);
        setDoubleClickWake(conVal, true);
        // Gionee <yangxinruo> <2016-4-15> modify for CR01677194 end
        return true;
    }

    @Override
    public float getWeightForValue(PowerConfig powerConfig, String value) {
        if (value == null) {
            return 0f;
        }
        if (value.equals("true")) {
            return -powerConfig.gestures_weight;
        } else {
            return 0f;
        }
    }

    @Override
    public ArrayList<String> getAvailCandidateValues() {
        ArrayList<String> res = new ArrayList<String>();
        res.add("false");
        return res;
    }

    @Override
    public String getTitle() {
        return mContext.getResources().getString(R.string.close_gesture);
    }

    @Override
    protected void saveCurrentToPreference(String saveKey) {
    }

    @Override
    protected boolean restoreFromSavedPreference(String saveKey) {
        return true;
    }

    @Override
    protected boolean isCurrentSettingsChangedByExternal(String compareConfigKey) {
        return true;
    }

    @Override
    public float getWeightForCurrent(PowerConfig powerConfig) {
        return getWeightForValue(powerConfig, String.valueOf(isSmartGestruesOn() || isScreenOffGestureOn()
                || isDoubleClickGestureOn() || isTouchlessGestruesOn()));
    }
}
