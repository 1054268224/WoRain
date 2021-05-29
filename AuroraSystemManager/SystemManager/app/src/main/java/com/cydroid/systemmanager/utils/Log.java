/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2014-01-10
 */

package com.cydroid.systemmanager.utils;

/**
 * @author Houjie E-mail:houjie@gionee.com
 * @version dateï¼š2014.1.10 PM 3:36:13
 */
public final class Log {
    private static final String TAG = "AmiSysManager";
    private static final boolean DEBUG = true;

    private Log() {
    }

    public static int v(String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.v(TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int v(boolean enable, String tag, String msg) {
        if (enable) {
            return v(tag, msg);
        }
        return 0;
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.v(TAG, "[" + tag + "]:" + msg, tr);
        }
        return 0;
    }

    public static int v(boolean enable, String tag, String msg, Throwable tr) {
        if (enable) {
            return v(tag, msg, tr);
        }
        return 0;
    }

    public static int d(String tag, String msg) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.d(TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int d(boolean enable, String tag, String msg) {
        if (enable) {
            return d(tag, msg);
        }
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.d(TAG, "[" + tag + "]:" + msg, tr);
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
            return android.util.Log.i(TAG, "[" + tag + "]:" + msg);
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
            return android.util.Log.i(TAG, "[" + tag + "]:" + msg, tr);
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
            return android.util.Log.w(TAG, "[" + tag + "]:" + msg);
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
            return android.util.Log.w(TAG, "[" + tag + "]:" + msg, tr);
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
            return android.util.Log.w(TAG, "[" + tag + "]:", tr);
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
            return android.util.Log.e(TAG, "[" + tag + "]:" + msg);
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
        if (DEBUG && tag != null && msg != null) {
            return android.util.Log.e(TAG, "[" + tag + "]:" + msg, tr);
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
            return android.util.Log.println(priority, TAG, "[" + tag + "]:" + msg);
        }
        return 0;
    }

    public static int println(boolean enable, int priority, String tag, String msg) {
        if (enable) {
            return println(priority, tag, msg);
        }
        return 0;
    }
}

