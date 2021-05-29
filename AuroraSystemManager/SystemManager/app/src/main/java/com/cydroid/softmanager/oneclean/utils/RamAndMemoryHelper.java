package com.cydroid.softmanager.oneclean.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.android.internal.util.MemInfoReader;

/**
 * File Description:
 *
 * @author: Gionee-lihq
 * @see: 2013-2-27 Change List:
 */
public class RamAndMemoryHelper {
    private static final String TAG = "RamAndMemoryHelper";

    private static final Object LOCK = new Object();
    private static RamAndMemoryHelper sInstance;

    private final ActivityManager mAm;
    private static final ActivityManager.MemoryInfo mMemInfo = new MemoryInfo();
    private static final MemInfoReader mMemInfoReader = new MemInfoReader();

    private final MainHandler mMainHandler;
    private final BackgroundHandler mHandler;
    private final HandlerThread mHandlerThread;
    private RamInfoUpdateCallback mRamInfoCallBack;

    private static final long mDelayMillis = 5000;
    private boolean sUpdating = false;

    public static RamAndMemoryHelper getInstance(Context context) {
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new RamAndMemoryHelper(context);
            }
            return sInstance;
        }
    }

    private RamAndMemoryHelper(Context context) {
        mMainHandler = new MainHandler(context.getMainLooper());
        mAm = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        mHandlerThread = new HandlerThread("OneClean:", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mHandler = new BackgroundHandler(mHandlerThread.getLooper());
    }

    /**
     * 更新当前使用ram百分比
     */
    public void startUpdateRam() {
        if (!sUpdating) {
            mHandler.sendEmptyMessageDelayed(BackgroundHandler.MSG_HANDLE_UPDATE_RAM_USAGED, 20);
            sUpdating = true;
        }
    }

    /**
     * 停止更新ram
     */
    public void stopUpdateRam() {
        sUpdating = false;
    }

    private void updateRamUsaged() {
        if (sUpdating) {
            mHandler.sendEmptyMessageDelayed(BackgroundHandler.MSG_HANDLE_UPDATE_RAM_USAGED, mDelayMillis);
        }
    }

    /**
     * To get available memory size
     *
     * @return size of avail memory
     */
    public long getAvailMem() {
        mAm.getMemoryInfo(mMemInfo);
        return mMemInfo.availMem;
    }

    /**
     * To get total memory size
     *
     * @return size of total memory
     */
    public static long getTotalMem() {
        mMemInfoReader.readMemInfo();
        return mMemInfoReader.getTotalSize();
    }

    /**
     * To get the used memory ratio
     *
     * @return the String value of ratio
     */
    public String getRatioUsedMem(long size) {
        double result = (double) (getTotalMem() - getAvailMem() - size) / getTotalMem();

        // Gionee <xuhz> <2014-03-17> modify for CR01116331 begin
        int usedMem = (int) (result * 100);

        if (usedMem < 0) {
            usedMem = 0;
        } else if (usedMem > 100) {
            usedMem = 100;
        }
        String str = "" + usedMem;
        return str;
        // Gionee <xuhz> <2014-03-17> modify for CR01116331 end
    }

    public double getRatioUsedMem() {
        return (double) (getTotalMem() - getAvailMem()) / getTotalMem();
    }

    // Gionee <jingjc> <2014-02-18> add start
    public static long getAvailMem(ActivityManager am) {
        am.getMemoryInfo(mMemInfo);
        return mMemInfo.availMem;
    }

    public static int getRatioUsedMem(Context context) {
        ActivityManager am = (ActivityManager) context.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        double result = (double) (getTotalMem() - getAvailMem(am)) / getTotalMem();

        // Gionee <xuhz> <2014-03-17> modify for CR01116331 begin
        int usedMem = (int) (result * 100);

        if (usedMem < 0) {
            usedMem = 0;
        } else if (usedMem > 100) {
            usedMem = 100;
        }
        return usedMem;
        // Gionee <xuhz> <2014-03-17> modify for CR01116331 end
    }

    // Gionee <jingjc> <2014-02-18> add end

    public interface RamInfoUpdateCallback {
        void onUpdateRam();
    }

    public void setCallback(RamInfoUpdateCallback callback) {
        mRamInfoCallBack = callback;
    }

    public class MainHandler extends Handler {
        public static final int MSG_HANDLE_UPDATE_RAM = 1;

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_UPDATE_RAM:
                    if (mRamInfoCallBack != null) {
                        mRamInfoCallBack.onUpdateRam();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class BackgroundHandler extends Handler {
        static final int MSG_HANDLE_UPDATE_RAM_USAGED = 2;

        // Gionee <yangxinruo> <2016-1-5> add for CR01618272 end
        public BackgroundHandler(Looper loop) {
            super(loop);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_UPDATE_RAM_USAGED:
                    mMainHandler.sendEmptyMessage(MainHandler.MSG_HANDLE_UPDATE_RAM);
                    updateRamUsaged();
                    break;
                default:
                    break;
            }
        }
    }

    public void releaseRes() {
        mRamInfoCallBack = null;
    }
}
