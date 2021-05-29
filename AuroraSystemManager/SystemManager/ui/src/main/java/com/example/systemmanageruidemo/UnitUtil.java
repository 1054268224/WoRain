package com.example.systemmanageruidemo;

import android.content.Context;

import com.android.internal.inputmethod.CancellationGroup;

public class UnitUtil {
    public static float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    public static float px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static String getStr(Context context, int resId) {
        final String mString = context.getResources().getString(resId);
        return mString;
    }

    /**
     * @param size
     * @return
     */
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1fGB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0fMB" : "%.1fMB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0fKB" : "%.1fKB", f);
        } else {
            return String.format("%dB", size);
        }
    }

    /**
     * mb最小
     *
     * @param size0
     * @return
     */
    public static String convertStorage3(float size0) {
        int size = Math.round(size0);
        long kb = 1024;
        long mb = kb * 1024;
        if (size >= mb) {
            return String.format("%.1fTB", (float) size / mb);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0fGB" : "%.1fGB", f);
        } else {
            return String.format("%dMB", size);
        }
    }

    /**
     * @param size0
     * @return
     */
    public static String convertStorage(float size0) {
        return convertStorage(Math.round(size0));
    }

    /**
     * 单位分开
     *
     * @param size
     * @return
     */
    public static String[] convertStorage2(long size) {
        String re[] = new String[2];
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            re[0] = String.format("%.1f", (float) size / gb);
            re[1] = "GB";
        } else if (size >= mb) {
            float f = (float) size / mb;
            re[0] = String.format(f > 100 ? "%.0f" : "%.1f", f);
            re[1] = "MB";
        } else if (size >= kb) {
            float f = (float) size / kb;
            re[0] = String.format(f > 100 ? "%.0f" : "%.1f", f);
            re[1] = "KB";
        } else {
            re[0] = String.format("%d", size);
            re[1] = "B";
        }
        return re;
    }

    public static String[] convertStorage2(float size) {
        return convertStorage2(Math.round(size));
    }


    public static String[] convertStorage4(float size0) {
        return convertStorage4(Math.round(size0));
    }

    /**
     * mb最小 单位分开
     *
     * @param size
     * @return
     */
    public static String[] convertStorage4(long size) {
        String re[] = new String[2];
        long Gb = 1024;
        long tb = Gb * 1024;
        if (size >= tb) {
            re[0] = String.format("%.1f", (float) size / tb);
            re[1] = "TB";
        } else if (size >= Gb) {
            float f = (float) size / Gb;
            re[0] = String.format(f > 100 ? "%.0f" : "%.1f", f);
            re[1] = "GB";
        } else {
            re[0] = String.format("%d", size);
            re[1] = "MB";
        }
        return re;
    }
}
