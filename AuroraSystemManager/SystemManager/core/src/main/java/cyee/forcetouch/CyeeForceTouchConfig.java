package cyee.forcetouch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import com.cyee.utils.Log;

public class CyeeForceTouchConfig {

    private final static String LOGTAG = "CyeeForceTouchConfig";

    private final static double FORCETHRESHOLD = 0.15;
    private final static double FORCETHRESHOLD_MID = 0.50;
    private final static double FORCETHRESHOLD_MAX = 0.80;
    private final static String NODE_TYPE_FORCE_TOUCH_SUPPORT = "NODE_TYPE_FORCE_TOUCH_SUPPORT";
    private final static String NODE_TYPE_FORCE_TOUCH_ENABLE = "NODE_TYPE_FORCE_TOUCH_ENABLE";
    private final static String LIGHT_THRESHOLD_TYPE = "force_touch_trigger_intensity";
    private final static String MID_THRESHOLD_TYPE = "force_touch_min_intensity";
    private final static String FORCE_THRESHOLD_TYPE = "force_touch_max_intensity";

    private static double sLightThreshold = -1;
    private static double sMidThreshold = -1;
    private static double sForceThreshold = -1;
    private static int sIsSupport = -1;
    private static CyeeForceTouchConfig sInstance;
    private Context mCxt;
    private ForceTouchObserver mObserver;

    public static synchronized CyeeForceTouchConfig getInstance(Context cxt) {
        if (null == sInstance) {
            sInstance = new CyeeForceTouchConfig(cxt);
        }

        return sInstance;
    }

    private CyeeForceTouchConfig(Context cxt) {
        if(null == cxt) {
            return ;
        }
        mCxt = cxt.getApplicationContext();
        mObserver = new ForceTouchObserver(new Handler());
        Uri uriMid = getForceTouchUri(MID_THRESHOLD_TYPE);
        if (null != uriMid) {
            cxt.getContentResolver().registerContentObserver(uriMid, true,
                    mObserver);
        }
        Uri uriForce = getForceTouchUri(FORCE_THRESHOLD_TYPE);
        if (null != uriForce) {
            cxt.getContentResolver().registerContentObserver(uriForce, true,
                    mObserver);
        }
    }

    public double getLightThreshold() {
        if (sLightThreshold == -1) {
            sLightThreshold = (double) getForceTouchThreshold(mCxt,
                    LIGHT_THRESHOLD_TYPE, (int) (FORCETHRESHOLD * 1000)) / 1000;
            Log.d(LOGTAG, "getLightThreshold sLightThreshold="
                    + sLightThreshold);
        }
        return sLightThreshold;
    }

    public double getMidThreshold() {
        if (sMidThreshold == -1) {
            sMidThreshold = (double) getForceTouchThreshold(mCxt,
                    MID_THRESHOLD_TYPE, (int) (FORCETHRESHOLD_MID * 1000)) / 1000;
            Log.d(LOGTAG, "getMidThreshold sMidThreshold=" + sMidThreshold);
        }
        return sMidThreshold;
    }

    public double getForceThreshold() {
        if (sForceThreshold == -1) {
            sForceThreshold = (double) getForceTouchThreshold(mCxt,
                    FORCE_THRESHOLD_TYPE, (int) (FORCETHRESHOLD_MAX * 1000)) / 1000;
            Log.d(LOGTAG, "getForceThreshold sForceThreshold="
                    + sForceThreshold);
        }
        return sForceThreshold;
    }

    public boolean isSupportForceTouch() {
        if (sIsSupport == -1) {
            sIsSupport = readGestureNodeValue(mCxt,
                    NODE_TYPE_FORCE_TOUCH_SUPPORT);
        }
        if (sIsSupport != 1) {
            return false;
        }
        int enable = readGestureNodeValue(mCxt, NODE_TYPE_FORCE_TOUCH_ENABLE);
        enable &= sIsSupport;
        Log.d(LOGTAG, "isSupportForceTouch sIsSupport=" + sIsSupport+";enable="+enable);
        
        return enable == 1;
    }

    public void onDestroyForceTouchConfig() {
        if (null != mCxt && null != mObserver) {
            mCxt.getContentResolver().unregisterContentObserver(mObserver);
        }
        mObserver = null;
        sInstance = null;
    }

    private int getCyeeServerNodeTypeIndex(String nodeType) {
        int result = -1;
        try {
            Class cls = Class
                    .forName("android.os.cyeeserver.CyeeServerManager");
            Field field = cls.getField(nodeType);
            result = (Integer) field.getInt(cls);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
 
    private int getForceTouchThreshold(Context cxt, String type,
            int defaultVal) {
        int retVal = defaultVal;

        try {
            Class cls = Class.forName("cyee.provider.CyeeSettings");
            Method method = cls.getMethod("getInt", ContentResolver.class,
                    String.class, int.class);
            retVal = (Integer) method.invoke(CyeeForceTouchConfig.class,
                    cxt.getContentResolver(), type, defaultVal);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return retVal;
    }

    private int readGestureNodeValue(Context context, String nodeType) {
        int result = 0;
        int index = getCyeeServerNodeTypeIndex(nodeType);

        if (index == -1) {
            return result;
        }

        Object pm = (Object) (context.getSystemService("cyeeserver"));
        try {
            Class cls = Class
                    .forName("android.os.cyeeserver.CyeeServerManager");
            Method method = cls.getMethod("GetNodeState", int.class);
            result = (Integer) method.invoke(pm, index);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private Uri getForceTouchUri(String type) {
        Uri uri = null;

        try {
            Class cls = Class.forName("cyee.provider.CyeeSettings");
            Method method = cls.getMethod("getUriFor", String.class);
            uri = (Uri) method.invoke(CyeeForceTouchConfig.class, type);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return uri;
    }

    public static class ForceTouchObserver extends ContentObserver {

        public ForceTouchObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            String type = uri.getLastPathSegment();
            Log.d(LOGTAG, "ForceTouchObserver uri type=" + type);

            if (MID_THRESHOLD_TYPE.equals(type)) {
                sMidThreshold = -1;
            } else if (FORCE_THRESHOLD_TYPE.equals(type)) {
                sForceThreshold = -1;
            }
        }
    }
}
