package com.cydroid.softmanager.softmanager.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
// Gionee <liuyb> <2014-07-23> add for CR01316210 begin
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
// Gionee <liuyb> <2014-07-23> add for CR01316210 end

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.common.Util;
import com.cydroid.softmanager.softmanager.model.SDCardInfo;

import android.app.usage.StorageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.os.SystemProperties;

import com.android.internal.app.IMediaContainerService;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.format.Formatter;
import android.util.AndroidRuntimeException;

import com.android.internal.util.FastXmlSerializer;

import com.cydroid.softmanager.utils.AppFilterUtil;
import com.cydroid.softmanager.utils.Log;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.UUID;

/**
 * 
 * File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-1-17 Change List:
 */
public class SoftHelperUtils {
    private static final String DEFAULT_CONTAINER_PACKAGE = "com.android.defcontainer";

    public static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName(
            DEFAULT_CONTAINER_PACKAGE, "com.android.defcontainer.DefaultContainerService");
    private HandlerThread mMeasurementHandlerThread;
    private MeasurementHandler mHandler;
    private String mExternalFilepath, mInternalFilePath;
    private static final SDCardInfo sInternalSdCardInfo = new SDCardInfo(),
            sExternalCardInfo = new SDCardInfo();
    private final Context mStrongContext;
    private static boolean mInternalCheckFlag = false;
    private static boolean mOutCheckFlag = false;
    private static final String PARTITION_FILE_PATH = "/proc/partitions";
    private static final String TOTAL_SIZE = "mmcblk0";
    public   ArrayList<String> mList = new ArrayList<String>();

    public static boolean getInternalCheckFlag() {
        return mInternalCheckFlag;
    }

    public static boolean getOutCheckFlag() {
        return mOutCheckFlag;
    }

    public static void setInternalCheckFlag(boolean checkFlag) {
        mInternalCheckFlag = checkFlag;
    }

    public static void setOutCheckFlag(boolean checkFlag) {
        mOutCheckFlag = checkFlag;
    }

    // Gionee <liuyb> <2014-07-23> add for CR01316210 begin
    public static final Map<String, String> mPackageLableMap = new HashMap<String, String>();
    private static String mLocaleLanguage = null;
    // Gionee <liuyb> <2014-07-23> add for CR01316210 end

    private StorageInfoUpdateCallback mStorageInfoCallBack;

    private static final String TAG = "SoftHelperUtils";

    public SoftHelperUtils(Context context) {
        mStrongContext = context.getApplicationContext();
    }


    public void init(Context context) {
        if (null == mMeasurementHandlerThread) {
            mMeasurementHandlerThread = new HandlerThread("MemoryMeasurement");
            mMeasurementHandlerThread.start();
        }
        mHandler = new MeasurementHandler(context.getApplicationContext(), 
            mMeasurementHandlerThread.getLooper());
        if (!mHandler.hasMessages(MeasurementHandler.MSG_MEASURE)) {
            mHandler.sendEmptyMessage(MeasurementHandler.MSG_MEASURE);
        }
    }

    public void setStorageInfoPath(boolean external, String path) {
        if (external) {
            mExternalFilepath = path;
        } else {
            mInternalFilePath = path;
        }
        // Modify by zhiheng.huang on 2019/11/28 for EJQQQ-144 start
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MeasurementHandler.MSG_GET_STORAGEINFO, 0);
        }
        // Modify by zhiheng.huang on 2019/11/28 for EJQQQ-141 end

    }

    public SDCardInfo getExternalStorageInfo() {
        return sExternalCardInfo;
    }

    public SDCardInfo getInternalStorageInfo() {

        return sInternalSdCardInfo;
    }

    public void setCallback(StorageInfoUpdateCallback callback) {
        mStorageInfoCallBack = callback;
    }

    public interface StorageInfoUpdateCallback {
        void onUpdateStorageInfo();
    }

    public void cleanup() {
        unbindservice();
        cleanSdcardInfo();
        mMeasurementHandlerThread.quit();
        mMeasurementHandlerThread = null;
    }

    private void unbindservice() {
        mHandler.sendEmptyMessage(MeasurementHandler.MSG_DISCONNECT);
    }

    public void cleanSdcardInfo() {
        sExternalCardInfo.mTotal = 0;
        sExternalCardInfo.mFree = 0;
        sInternalSdCardInfo.mTotal = 0;
        sInternalSdCardInfo.mFree = 0;
    }

    // Gionee <yangxinruo> <2016-4-8> add for CR01670643 begin
    /**
     * To get application of current installed application
     * 
     * @return
     */
    public static List<ApplicationInfo> getThirdApplicationInfo(Context context) {
        return getThirdApplicationInfo(context, true);
    }
    // Gionee <yangxinruo> <2016-4-8> add for CR01670643 end

    /**
     * To get application of current installed application
     *
     * @param context
     * @return
     */
    public static List<ApplicationInfo> getThirdApplicationInfo(Context context, boolean includeUninstalled) {
        return getThirdApplicationInfo(context, includeUninstalled, 0);
    }

    public static List<ApplicationInfo> getThirdApplicationInfo(Context context, boolean includeUninstalled,
            int flags) {
        int getPkgsFlag = flags;
        List<ApplicationInfo> mApplications = new ArrayList<ApplicationInfo>();
        try {
            // Gionee <yangxinruo> <2016-4-8> modify for CR01670643 begin
            getPkgsFlag |= PackageManager.GET_DISABLED_COMPONENTS;
            if (includeUninstalled)
                getPkgsFlag |= PackageManager.GET_UNINSTALLED_PACKAGES;
            mApplications = context.getPackageManager().getInstalledApplications(getPkgsFlag);
            // Gionee <yangxinruo> <2016-4-8> modify for CR01670643 end
        } catch (AndroidRuntimeException ar) {
            Log.i(TAG, "AndroidRuntimeException", ar);
        } catch (Exception e) {
            Log.i(TAG, "Exception", e);
        }
        for (int i = 0; i < mApplications.size(); i++) {
            final ApplicationInfo info = mApplications.get(i);
            // Gionee <yangxinruo> <2016-5-6> modify for CR01692658 begin
            String label = loadLabel(context, info);
            if (label == null)
                label = "";
            if (!AppFilterUtil.THIRD_PARTY_FILTER.filterApp(info)
                    || label.equalsIgnoreCase("com.android.nfc.tests")) {
                mApplications.remove(i);
                i--;
                continue;
            }
            // Gionee <yangxinruo> <2016-5-6> modify for CR01692658 end
        }
        return mApplications;
    }

    public static String getSizeStr(Context context, long size) {
        if (size >= 0) {
            return Formatter.formatFileSize(context, size);
        }
        return null;
    }

    // Gionee <liuyb> <2014-07-23> modify for CR01316210 begin
    // Gionee <xionghg> <2017-06-21> modify for 158979 begin
    public synchronized static String loadLabel(Context context, ApplicationInfo info) {
        // Gionee <houjie> <2015-09-07> add for CR01537276 begin
        String localeLanguage = getLocaleLanguage(context);
        // Gionee <houjie> <2015-09-07> add for CR01537276 end
        if (mLocaleLanguage == null || !localeLanguage.equals(mLocaleLanguage)) {
            reflashLocale(context);
        }
        if (mPackageLableMap.containsKey(mLocaleLanguage + info.packageName)) {
            return mPackageLableMap.get(mLocaleLanguage + info.packageName);
        }
        CharSequence result = info.loadLabel(context.getPackageManager());
        if (result == null) {
            result = info.packageName;
        }
        mPackageLableMap.put(mLocaleLanguage + info.packageName, result.toString());
        return result.toString();
    }
    // Gionee <xionghg> <2017-06-21> modify for 158979 end

    public static void reflashLocale(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        mLocaleLanguage = locale.getLanguage();
    }

    // Gionee <houjie> <2015-09-07> add for CR01537276 begin
    public static String getLocaleLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        return locale.getLanguage();
    }

    // Gionee <houjie> <2015-09-07> add for CR01537276 end
    // Gionee <liuyb> <2014-07-23> modify for CR01316210 begin
    /**
     * To convert String {@value src} to "src(num)"
     * 
     * @param src
     * @param num
     * @return
     */
    public static String joinStr(String src, int num) {
        StringBuffer strBuf = new StringBuffer();
        int index = src.indexOf("(");
        if (index > 0) {
            src = src.substring(0, index);
        }
        if (num > 0) {
            strBuf.append(src);
            strBuf.append("(");
            strBuf.append(num);
            strBuf.append(")");
            return strBuf.toString();
        } else {
            return src;
        }
    }

    public static String getHtmlString(String str, String sec) {
        StringBuffer tempStr = new StringBuffer();
        tempStr.append("<font color=red>");
        tempStr.append(sec);
        tempStr.append("</font>");
        tempStr.append(str);
        return tempStr.toString();
    }

    public static String convertStorage(long size) {
        /*guoxt modify for SW17W16A-2741 begin*/
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        /*guoxt modify for SW17W16A-2741 end*/

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

    // Add by HZH on 2019/6/15 for EJSL-1532 start
    public static String unit1000(long size) {
        long kb = 1000;
        long mb = kb * 1000;
        long gb = mb * 1000;

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
    // Add by HZH on 2019/6/15 for EJSL-1532 end

    public static SDCardInfo getSDCardInfo() {
        String sDcString = android.os.Environment.getExternalStorageState();
        if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File pathFile = android.os.Environment.getExternalStorageDirectory();
            // Log.e("Dazme", "pathFile:"+pathFile);
            SDCardInfo sdCardInfo = new SDCardInfo();
            setSDCardInfo(pathFile, sdCardInfo);
            return sdCardInfo;
        }
        return null;
    }

    public static SDCardInfo getSDCardInfo(boolean isMounted, String path) {
        SDCardInfo info = null;
        if (isMounted && path != null) {
            File pathFile = new File(path);
            info = new SDCardInfo();
            setSDCardInfo(pathFile, info);
        }
        return info;
    }

    private static void setSDCardInfo(File pathFile, SDCardInfo info) {
        /*
         * try { android.os.StatFs statfs = new
         * android.os.StatFs(pathFile.getPath());
         * 
         * // 获取SDCard上BLOCK总数 long nTotalBlocks = statfs.getBlockCount();
         * 
         * // 获取SDCard上每个block的SIZE long nBlocSize = statfs.getBlockSize();
         * 
         * // 获取可供程序使用的Block的数量 long nAvailaBlock = statfs.getAvailableBlocks();
         * 
         * // 计算SDCard 总容量大小MB info.mTotal = nTotalBlocks * nBlocSize;
         * 
         * // 计算 SDCard 剩余大小MB info.mFree = nAvailaBlock * nBlocSize; } catch
         * (IllegalArgumentException e) { DebugUtil.v(e.toString()); }
         */

    }

    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean isExSdcardInserted(Context context) {
        List<String> mVolumePathList = new ArrayList<String>();
        List<StorageVolume> mStorageVolumes = new ArrayList<StorageVolume>();
        int mExternalStorageId = -1;

        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        String[] mPathList = mStorageManager.getVolumePaths();
        StorageVolume[] volumes = mStorageManager.getVolumeList();

        int len = volumes.length;

        for (int i = 0; i < len; i++) {
            if (!mStorageManager.getVolumeState(mPathList[i]).equals("not_present")) {
                mVolumePathList.add(mPathList[i]);
                mStorageVolumes.add(volumes[i]);
            }
        }

        for (int i = 0; i < mStorageVolumes.size(); i++) {
            if (isExternalSDCardStorage(context, mStorageVolumes.get(i))) {
                mExternalStorageId = i;
            }
        }

        if (mExternalStorageId == -1) {
            return false;
        }

        return mStorageManager.getVolumeState(mVolumePathList.get(mExternalStorageId)).equals("mounted");

    }

    public static boolean isExternalStorage(Context context, StorageVolume mVolume) {
//        Log.d(TAG, "removeAllSpace(mVolume.getDescription(context))"
//                + removeAllSpace(mVolume.getDescription(context)) + " path:" + mVolume.getPath());
        return !mVolume.getPath().equals(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static boolean isExternalSDCardStorage(Context context, StorageVolume mVolume) {
//        Log.d(TAG, "removeAllSpace(mVolume.getDescription(context))"
//                + removeAllSpace(mVolume.getDescription(context)) + " path:" + mVolume.getPath());
        return removeAllSpace(mVolume.getDescription(context))
                .contains(context.getResources().getString(R.string.sd_card_title));
    }

    private static String removeAllSpace(String str) {
        String tmpstr = str.replace(" ", "");
        return tmpstr;
    }

    private static int sStorageCount;
    private static File[] sSystemSDCardMountPointPathList = null;
    private static File[] sSDCardMountPointPathList = null;
    private static int sStorageMountedCount;
    private static String sSDCardPath;
    private static String sSDCard2Path;

    public static boolean isSdcardMounted(StorageManager storageManager) {
        if (sStorageCount >= 2) {
            return checkSDCardMount(storageManager, sSystemSDCardMountPointPathList[0].getAbsolutePath())
                    || checkSDCardMount(storageManager, sSystemSDCardMountPointPathList[1].getAbsolutePath());
        } else if (sStorageCount > 0) {
            return checkSDCardMount(storageManager, sSystemSDCardMountPointPathList[0].getAbsolutePath());
        }
        return false;
    }

    public static int getStorageCount() {
        return sStorageCount;
    }

    public static int getStorageMountedCount() {
        return sStorageMountedCount;
    }

    public static String getSDCardPath() {
        return sSDCardPath;
    }

    public static String getSDCard2Path() {
        return sSDCard2Path;
    }

    public static void setsSDCard2Path(String sSDCard2Path) {
        SoftHelperUtils.sSDCard2Path = sSDCard2Path;
    }

    public static void initStorageState(Context context, StorageManager storageManager) {
        StorageVolume[] storageVolume = storageManager.getVolumeList();
        sStorageCount = storageVolume.length;
        sSystemSDCardMountPointPathList = new File[sStorageCount];
        for (int i = 0; i < sStorageCount; i++) {
            sSystemSDCardMountPointPathList[i] = new File(storageVolume[i].getPath());
        }
        updateMountedPointList(storageManager);
        sStorageMountedCount = sSDCardMountPointPathList.length;
        if (sStorageMountedCount >= 2) {
            sSDCardPath = sSDCardMountPointPathList[0].getAbsolutePath();
            sSDCard2Path = sSDCardMountPointPathList[1].getAbsolutePath();
        } else if (sStorageMountedCount == 1 && sSDCardMountPointPathList[0] != null) {
            sSDCardPath = sSDCardMountPointPathList[0].getAbsolutePath();
        }
    }

    private static void updateMountedPointList(StorageManager storageManager) {
        int mountCount = 0;
        for (int i = 0; i < sSystemSDCardMountPointPathList.length; i++) {
            if (checkSDCardMount(storageManager, sSystemSDCardMountPointPathList[i].getAbsolutePath())) {
                mountCount++;
            }
        }
        sSDCardMountPointPathList = new File[mountCount];
        if (twoSDCardSwap() && mountCount >= 2) {
            for (int i = mountCount - 1, j = 0; i >= 0; i--) {
                if (checkSDCardMount(storageManager, sSystemSDCardMountPointPathList[i].getAbsolutePath())) {
                    sSDCardMountPointPathList[j++] = sSystemSDCardMountPointPathList[i];
                }
            }
        } else {
            for (int i = 0, j = 0; i < sSystemSDCardMountPointPathList.length; i++) {
                if (checkSDCardMount(storageManager, sSystemSDCardMountPointPathList[i].getAbsolutePath())) {
                    sSDCardMountPointPathList[j++] = sSystemSDCardMountPointPathList[i];
                }
            }
        }
    }

    /**
     * This method checks whether SDcard is mounted or not
     * 
     * @param mountPoint
     *            the mount point that should be checked
     * @return true if SDcard is mounted, false otherwise
     */
    protected static boolean checkSDCardMount(StorageManager storageManager, String mountPoint) {
        if (mountPoint == null) {
            return false;
        }
        String state = null;
        state = storageManager.getVolumeState(mountPoint);
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @SuppressWarnings("rawtypes")
    public static boolean twoSDCardSwap() {
        String sSDSwapEnabled = SystemProperties.get("ro.gn.gn2sdcardswap", "no");
        boolean bRes = sSDSwapEnabled.equals("yes");
        return bRes;
    }

    // public static boolean isExSdcardInserted() {
    // return GnStorageManager.getInstance().isSDExist();
    // }

    public static boolean isNotSupportSDCard() {
        return SystemProperties.get("ro.gn.sdcard.type").equals("internal");
    }

    private class MeasurementHandler extends Handler {
        public static final int MSG_MEASURE = 1;
        public static final int MSG_CONNECTED = 2;
        public static final int MSG_DISCONNECT = 3;
        public static final int MSG_GET_STORAGEINFO = 4;

        // private Object mLock = new Object();

        private IMediaContainerService mDefaultContainer;

        private volatile boolean mBound = false;

        private final WeakReference<Context> mContext;

        private final ServiceConnection mDefContainerConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                final IMediaContainerService imcs = IMediaContainerService.Stub.asInterface(service);
                mDefaultContainer = imcs;
                mBound = true;
                sendEmptyMessage(MSG_GET_STORAGEINFO);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
            }
        };

        public MeasurementHandler(Context context, Looper looper) {
            super(looper);
            mContext = new WeakReference<Context>(context);
        }

        /*chenyee guoxt modify for  CSW1702A-163 begin 20171129*/

        public  StorageInfo getStorageInfo(String mountPoint) {
            StorageInfo info = null;
            if (null == mountPoint) {
                Log.e(TAG, "getStorageInfo: invalid param!");
                return null;
            }
            // Modify by HZH on 2019/6/15 for EJSL-1532 start
              /*  try {
                    android.os.StatFs statfs = new android.os.StatFs(mountPoint);
                    long nTotalBlocks = statfs.getBlockCount();
                    long nBlocSize = statfs.getBlockSize();
                    long nAvailaBlock = statfs.getAvailableBlocks();
                    info = new StorageInfo();
                    info.total = nTotalBlocks * nBlocSize;
                    info.free = nAvailaBlock * nBlocSize;
                    info.used = info.total - info.free;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.toString());
                }*/

            File file = new File(mountPoint);
            StorageManager mStorageManager = (StorageManager) mStrongContext.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume mStorageVolume = mStorageManager.getStorageVolume(file);
            boolean removable = mStorageVolume.isRemovable();
            info = new StorageInfo();
            if (removable) {
                // sdcard
                info.free = file.getFreeSpace();
                info.total = file.getTotalSpace();
                info.used = info.total - info.free;
            } else {
                try {
                    final StorageStatsManager stats = mStrongContext.getSystemService(StorageStatsManager.class);
                    info.free = stats.getFreeBytes(mStorageVolume.getUuid());
                    info.total = stats.getTotalBytes(mStorageVolume.getUuid());
                    info.used = info.total - info.free;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Modify by HZH on 2019/6/15 for EJSL-1532 end

                Log.d(TAG, "getStorageInfo: mountpoint = " + mountPoint + ", info = " + info);
            return info;
        }

        //Chenyee guoxt modify for CSW1707ST-71 begin
        public  StorageInfo getStorageInfoIntel(String mountPoint) {
            StorageInfo info = null;
            if (null == mountPoint) {
                Log.e(TAG, "getStorageInfo: invalid param!");
                return null;
            }
            try {
                android.os.StatFs statfs = new android.os.StatFs(mountPoint);
                long nTotalBlocks = statfs.getBlockCount();
                long nBlocSize = statfs.getBlockSize();
                long nAvailaBlock = statfs.getAvailableBlocks();
                info = new StorageInfo();
                info.total = readRomSize();//nTotalBlocks * nBlocSize; 真实的可用空间
                info.free = nAvailaBlock * nBlocSize;
                info.used = info.total - info.free + (info.total - nTotalBlocks * nBlocSize );
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.toString());
            }

            Log.d(TAG, "getStorageInfo: mountpoint = " + mountPoint + ", info = " + info);
            return info;
        }


        public  ArrayList<String> readFile(String path) {
            mList.clear();
            File file = new File(path);
            FileReader fr = null;
            BufferedReader br = null;
            try {
                if (file.exists()) {
                    fr = new FileReader(file);
                }else{
                    Log.d(TAG,"file in "+path+" does not exist!");
                    return null;
                }
                br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    Log.d(TAG," read line "+line);
                    mList.add(line);
                }
                return mList;
            } catch (IOException io) {
                Log.d(TAG,"IOException");
                io.printStackTrace();
            } finally{
                try{
                    if (br != null){
                        br.close();
                    }
                    if (fr != null){
                        fr.close();
                    }
                }catch (IOException io){
                    io.printStackTrace();
                }
            }
            return null;
        }

        public  long readRomSize() {
            long mTotalSize=0;
            long totalSize;
            long systemStorageSize;
            String lineStr;

            ArrayList<String> mPartitionInfor = readFile(PARTITION_FILE_PATH);
            Log.d(TAG, "readRomSize()  size= " + mPartitionInfor.size());
            for (int i = 0; i < mPartitionInfor.size(); i++) {
                lineStr = mPartitionInfor.get(i).trim();
                int nIndex = lineStr.indexOf(TOTAL_SIZE);
                int n = 0;
                Log.d(TAG, "lineStr= " + lineStr + ",nIndex= " + nIndex);
                if (nIndex > 2 && (lineStr.length() == nIndex + TOTAL_SIZE.length())) {
                    String[] strSplit = lineStr.split(" ");
                    for (String str : strSplit) {
                        if (!TextUtils.isEmpty(str) && (n++ == 2)) {
                            Log.d(TAG, "str= " + str);
                            mTotalSize = Long.parseLong(str) * 1024;
                        }
                    }
                    mTotalSize = Util.translateCapacity(mTotalSize);
                    Log.d(TAG, "readRomSize()  mTotalSize= " + mTotalSize);
                }
            }
            return mTotalSize;
        }
        //Chenyee guoxt modify for CSW1707ST-71 end

        /*chenyee guoxt modify for  CSW1702A-163 end 20171129*/
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MEASURE: {

                    // final Context context = (mContext != null) ? mContext.get()
                    // : null;
                    // if (context == null) {
                    // Log.e("dzmdzm", "context == null");
                    // return;
                    // }
                    //
                    // synchronized (mLock) {
                    Intent service = new Intent().setComponent(DEFAULT_CONTAINER_COMPONENT);
                    mStrongContext.getApplicationContext().bindService(service, mDefContainerConn,
                            Context.BIND_AUTO_CREATE);

                    // }
                    break;
                }

                case MSG_DISCONNECT: {
                    // synchronized (mLock) {
                    if (mBound) {
                        /*
                         * final Context context = (mContext != null) ? mContext
                         * .get() : null; if (context == null) { return; }
                         */
                        mBound = false;
                        try {
                            mStrongContext.getApplicationContext().unbindService(mDefContainerConn);
                        } catch (Exception e) {
                            Log.w("Dazme", "Problem in unbindService ", e);
                        }
                    }
                    // }
                    break;
                }

                case MSG_GET_STORAGEINFO: {
                    try {
                        /*chenyee guoxt modify for  CSW1702A-163 begin 20171129*/
                        if (mInternalFilePath != null) {
                            //final long[] stats = mDefaultContainer.getFileSystemStats(mInternalFilePath);
                            //Chenyee guoxt modify for CSW1707ST-71 begin
                            StorageInfo internal = null;
                            if(Consts.cySTFlag){
                                //xuanyutag
                                internal= getStorageInfoIntel(mInternalFilePath);
                            }else{
                                internal = getStorageInfo(mInternalFilePath);
                            }
                            //Chenyee guoxt modify for CSW1707ST-71 end
                            sInternalSdCardInfo.mTotal = internal.total;//stats[0];
                            sInternalSdCardInfo.mFree = internal.free;//stats[1];
                            sInternalSdCardInfo.mUsed = internal.used;
                        }

                        if (mExternalFilepath != null) {
                            //final long[] stats1 = mDefaultContainer.getFileSystemStats(mExternalFilepath);
                            StorageInfo external = getStorageInfo(mExternalFilepath);
                            //sExternalCardInfo.mTotal = stats1[0];
                           // sExternalCardInfo.mFree = stats1[1];
                            sExternalCardInfo.mTotal = external.total;
                            sExternalCardInfo.mFree = external.free;
                            sExternalCardInfo.mUsed = external.used;
                        }
                        /*chenyee guoxt modify for  CSW1702A-163 end 20171129*/

                        if (mStorageInfoCallBack != null) {
                            mStorageInfoCallBack.onUpdateStorageInfo();
                        }

//                        Intent intent = new Intent();
//                        intent.setAction("refresh.action.sdcard.info");
//                        mStrongContext.sendBroadcast(intent);

                    } catch (Exception e) {
                        Log.w("Dazme", "Problem in container service", e);
                    }

                    break;
                }
                default:
                    break;

            }
        }

    }

    public static final HashSet<String> mCheckPermissions = new HashSet<String>();

    static {
        mCheckPermissions.add("android.permission.CALL_PHONE");
        mCheckPermissions.add("android.permission.SEND_SMS");
        mCheckPermissions.add("android.permission.SEND_SMS_MMS");
        mCheckPermissions.add("android.permission.READ_SMS");
        mCheckPermissions.add("android.permission.READ_SMS_MMS");
        mCheckPermissions.add("android.permission.READ_CONTACTS");
        mCheckPermissions.add("android.permission.READ_CONTACTS_CALLS");
        mCheckPermissions.add("android.permission.READ_CALL_LOG");
        mCheckPermissions.add("android.permission.WRITE_SMS");
        mCheckPermissions.add("android.permission.WRITE_SMS_MMS");
        mCheckPermissions.add("android.permission.WRITE_CONTACTS");
        mCheckPermissions.add("android.permission.WRITE_CONTACTS_CALLS");
        mCheckPermissions.add("android.permission.WRITE_CALL_LOG");
        mCheckPermissions.add("android.permission.ACCESS_FINE_LOCATION");
        mCheckPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        mCheckPermissions.add("android.permission.RECORD_AUDIO");
        mCheckPermissions.add("android.permission.READ_PHONE_STATE");
        mCheckPermissions.add("android.permission.CAMERA");
        mCheckPermissions.add("android.permission.BLUETOOTH_ADMIN");
        mCheckPermissions.add("android.permission.BLUETOOTH");
        mCheckPermissions.add("android.permission.CHANGE_WIFI_STATE");
        mCheckPermissions.add("android.permission.CHANGE_NETWORK_STATE");
        mCheckPermissions.add("android.permission.NFC");
    }

    public static final HashSet<String> mImportantPermissions = new HashSet<String>();

    static {
        mImportantPermissions.add("android.permission.READ_SMS");
        mImportantPermissions.add("android.permission.READ_SMS_MMS");
        mImportantPermissions.add("android.permission.READ_CONTACTS");
        mImportantPermissions.add("android.permission.READ_CONTACTS_CALLS");
        mImportantPermissions.add("android.permission.READ_CALL_LOG");
        mImportantPermissions.add("android.permission.ACCESS_FINE_LOCATION");
        mImportantPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        mImportantPermissions.add("android.permission.RECORD_AUDIO");
    }

    public static String getStringDate(long currentTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(new Date(currentTime));
        return dateString;
    }

    /*chenyee guoxt modify for  CSW1702A-163 begin 20171129*/
    public  class StorageInfo {
        public long total;
        public long free;
        public long used;

        @Override
        public String toString() {
            return "total size: " + total + " free size: " + free +" used: "+used;
        }
    }
    /*chenyee guoxt modify for  CSW1702A-163 end 20171129*/
    private long mAppSize;
    public static final long MAX_WAIT_TIME = 4 * 1000;
    public static final long WAIT_TIME_INCR = 10;

    public String invokePMGetPackageSizeInfo(Context context, String packageName) {
        try {
            PackageStatsObserver observer = new PackageStatsObserver();
            PackageManager pm = context.getPackageManager();
            // wait on observer
            synchronized (observer) {
                pm.getPackageSizeInfo(packageName, observer);
                long waitTime = 0;
                while ((!observer.isDone()) && (waitTime < MAX_WAIT_TIME)) {
                    observer.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                Log.i(TAG, "invokePMGetPackageSizeInfo waitTime= " + waitTime);

                if (!observer.isDone()) {
                    Log.e(TAG, "Timed out waiting for PackageStatsObserver.onGetStatsCompleted");
                    mAppSize = 0;
                    observer.notifyAll();
                    return null;
                }
            }
            mAppSize = observer.stats.cacheSize + observer.stats.codeSize + observer.stats.dataSize
                    + observer.stats.externalCodeSize + observer.stats.externalDataSize;

            Log.i(TAG, "invokePMGetPackageSizeInfo packageName= " + packageName + ", size = " + mAppSize);

            return SoftHelperUtils.getSizeStr(context, mAppSize);
        } catch (Exception e) {
            Log.w(TAG, "Exception :" + e);
            return null;
        }
    }

    class PackageStatsObserver extends IPackageStatsObserver.Stub {
        public boolean retValue = false;
        public PackageStats stats;
        private boolean doneFlag = false;

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            synchronized (this) {
                retValue = succeeded;
                stats = pStats;
                doneFlag = true;
                notifyAll();
            }
        }

        public boolean isDone() {
            return doneFlag;
        }
    }
}
