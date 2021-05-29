/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2014-01-10
 */

package com.cydroid.softmanager.utils;

import android.os.SystemProperties;

/**
 * @author Houjie E-mail:houjie@gionee.com
 * @version dateï¼š2014.1.10 PM 3:36:13
 */
public final class Log {
    private static final String TAG = "CyeeSystemManager";
    /*guoxt 2018-0317 modify for CSW1703A-1060 begin*/
//    public static boolean DEBUG = SystemProperties.get("vendor.MB.running", "0").equals("1");
    /*guoxt 2018-0317 modify for CSW1703A-1060 end*/
    public static boolean DEBUG =true;
    private Log() {
    }

    public static int v(String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.v(TAG + "_" + tag, msg);
        }
        return 0;
    }

    public static int v(boolean enable, String tag, String msg) {
        if (DEBUG && enable) {
            return v(tag, msg);
        }
        return 0;
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.v(TAG + "_" + tag, msg, tr);
        }
        return 0;
    }

    public static int v(boolean enable, String tag, String msg, Throwable tr) {
        if (DEBUG && enable) {
            return v(tag, msg, tr);
        }
        return 0;
    }

    public static int d(String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.d(TAG + "_" + tag,msg);
        }
        return 0;
    }

    public static int d(boolean enable, String tag, String msg) {
        if (DEBUG && enable) {
            return d(tag, msg);
        }
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.d(TAG + "_" + tag, msg, tr);
        }
        return 0;
    }

    public static int d(boolean enable, String tag, String msg, Throwable tr) {
        if (enable) {
            return d(tag, msg, tr);
        }
        return 0;
    }

    public static int i(String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.i(TAG + "_" + tag, msg);
        }
        return 0;
    }

    public static int i(boolean enable, String tag, String msg) {
        if (enable) {
            return i(tag, msg);
        }
        return 0;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.i(TAG + "_" + tag, msg, tr);
        }
        return 0;
    }

    public static int i(boolean enable, String tag, String msg, Throwable tr) {
        if (enable) {
            return i(tag, msg, tr);
        }
        return 0;
    }

    public static int w(String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.w(TAG + "_" + tag,  msg);
        }
        return 0;
    }

    public static int w(boolean enable, String tag, String msg) {
        if (enable) {
            return w(tag, msg);
        }
        return 0;
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.w(TAG + "_" + tag, msg, tr);
        }
        return 0;
    }

    public static int w(boolean enable, String tag, String msg, Throwable tr) {
        if (enable) {
            return w(tag, msg, tr);
        }
        return 0;
    }

    public static int w(String tag, Throwable tr) {
        if (DEBUG && tag != null) {
            return android.util.Log.w(TAG + "_" + tag, tr);
        }
        return 0;
    }

    public static int w(boolean enable, String tag, Throwable tr) {
        if (enable) {
            return w(tag, tr);
        }
        return 0;
    }

    public static int e(String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.e(TAG + "_" + tag, msg);
        }
        return 0;
    }

    public static int e(boolean enable, String tag, String msg) {
        if (enable) {
            return e(tag, msg);
        }
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr) {
        if ( tag != null && msg != null) {
            return android.util.Log.e(TAG + "_" + tag, msg, tr);
        }
        return 0;
    }

    public static int e(boolean enable, String tag, String msg, Throwable tr) {
        if (enable) {
            return e(tag, msg, tr);
        }
        return 0;
    }

    public static int println(int priority, String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.println(priority, TAG + "_" + tag, msg);
        }
        return 0;
    }

    public static int println(boolean enable, int priority, String tag, String msg) {
        if (enable) {
            return println(priority, tag, msg);
        }
        return 0;
    }


    public static void refreshLogEnable() {
        DEBUG = SystemProperties.get("vendor.MB.running", "0").equals("1");
    }


}

