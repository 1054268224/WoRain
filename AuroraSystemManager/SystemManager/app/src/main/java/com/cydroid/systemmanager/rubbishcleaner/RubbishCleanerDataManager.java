// Gionee: <houjie> <2015-10-27> add for CR01575153 begin
package com.cydroid.systemmanager.rubbishcleaner;

import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.IAdDirCallback;
import com.cleanmaster.sdk.ICacheCallback;
import com.cleanmaster.sdk.IKSCleaner;
import com.cleanmaster.sdk.IResidualCallback;
import com.cydroid.softmanager.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cydroid.systemmanager.rubbishcleaner.common.CleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.DeeplyCleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;
import com.cydroid.systemmanager.rubbishcleaner.util.FileIconHelper;
import com.cydroid.systemmanager.rubbishcleaner.util.FileNameUtils;
import com.cydroid.systemmanager.rubbishcleaner.util.MimeTypeUtils;
import com.cydroid.systemmanager.utils.ApkInfoHelper;
import com.cydroid.systemmanager.utils.FileUtil;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.UnitUtil;

public class RubbishCleanerDataManager {
    private static final String TAG = "RubbishCleanerDataManager";

    public static final int APK_QUERY_TOKEN = 0x40000000;
    public static final int BIGFILE_QUERY_TOKEN = 0x20000000;
    public static final int TOKEN_MASK = 0x1fffffff;

    private static RubbishCleanerDataManager sInstance;

    private int mToken = 0;
    private Context mContext;
    private Resources mResources;
    private Handler mHandler;
    private PackageManager mPkgMgr;
    private DbQueryHandler mQueryHandler;
    private AsyncQueryHandler mAsyncQuery;

    private String mCountry;
    private String mLanguage;
    private IKSCleaner mKsCleaner;
    private ServiceConnection mServiceConn;
    private int mCleanType = 0;
    private ArrayList<RubbishCleanerScanListener> mRubbishCleanerScanListeners = new ArrayList<RubbishCleanerScanListener>();
    private ArrayList<RubbishCleanerDelListener> mRubbishCleanerDelListeners = new ArrayList<RubbishCleanerDelListener>();
    private RubbishCleanerStrategy mRubbishCleanerStrategy;

    private long mTotalSize = 0L;
    private long mSelectedSize = 0L;
    private long mCleannedSize = 0L;

    private ArrayList<RubbishInfo> mCacheList = new ArrayList<RubbishInfo>();
    private ArrayList<RubbishInfo> mAdList = new ArrayList<RubbishInfo>();
    private ArrayList<RubbishInfo> mResidualList = new ArrayList<RubbishInfo>();
    private ArrayList<RubbishInfo> mApkList = new ArrayList<RubbishInfo>();
    private ArrayList<RubbishInfo> mBigfileList = new ArrayList<RubbishInfo>();
    private ArrayList<ArrayList<RubbishInfo>> mGroupList = new ArrayList<ArrayList<RubbishInfo>>();

    private ArrayList<GroupKeyInfo> mCacheKeyList = new ArrayList<GroupKeyInfo>();
    private HashMap<String, ArrayList<RubbishInfo>> mCacheListGroupMap = new HashMap<String, ArrayList<RubbishInfo>>();

    private ArrayList<GroupKeyInfo> mBigFileKeyList = new ArrayList<GroupKeyInfo>();
    private HashMap<String, ArrayList<RubbishInfo>> mBigFileListGroupMap = new HashMap<String, ArrayList<RubbishInfo>>();
    private Object mBindKey;

    private RubbishCleanerDataManager() {
    }

    public static synchronized RubbishCleanerDataManager getInstance() {
        if (sInstance == null) {
            sInstance = new RubbishCleanerDataManager();
        }
        return sInstance;
    }

    public void init(Context context, int cleanType, RubbishCleanerScanListener listener) {
        mContext = context.getApplicationContext();
        mResources = mContext.getResources();
        mHandler = new MyHandler();
        mPkgMgr = mContext.getPackageManager();
        mQueryHandler = new DbQueryHandler(mContext.getContentResolver());

        Locale locale = mResources.getConfiguration().locale;
        mCountry = locale.getCountry();
        mLanguage = locale.getLanguage();

        mCleanType = cleanType;
        mRubbishCleanerScanListeners.add(listener);

        mRubbishCleanerStrategy = createRubbishCleanStrategy(cleanType);
        mRubbishCleanerStrategy.init();

        // Gionee <houjie><2015-10-08> add for CR01562723 begin
        mAsyncQuery = new MediaHandler(mContext.getContentResolver());
        // Gionee <houjie><2015-10-08> add for CR01562723 end

        mTotalSize = 0L;
        mSelectedSize = 0L;
        mCleannedSize = 0L;
    }

    public void recycle() {
        mContext = null;
        if (mRubbishCleanerStrategy != null) {
            mRubbishCleanerStrategy.recycle();
            mRubbishCleanerStrategy = null;
        }
        mRubbishCleanerScanListeners.clear();
        mRubbishCleanerDelListeners.clear();
        mTotalSize = 0L;
        mSelectedSize = 0L;
        mCleannedSize = 0L;
    }

    public synchronized void bindKSCleanerService(Object bindKey) {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mKsCleaner = IKSCleaner.Stub.asInterface(service);
                if (mKsCleaner != null) {
                    startScanRubbish();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "cleanmaster service disconnected");
            }
        };
        Intent intent = new Intent(mContext, com.cleanmaster.CleanService.class);
        mContext.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
        mBindKey = bindKey;
    }

    public synchronized void unBindKSCleanerService(Object bindKey) {
        if (mContext != null && mServiceConn != null && bindKey == mBindKey) {
            mContext.unbindService(mServiceConn);
            mServiceConn = null;
        }
    }

    // maybe sometime have some 0.0B files
    public boolean isAllNotSelected() {
        boolean isNoItem = true;
        for (ArrayList<RubbishInfo> list : mGroupList) {
            for (RubbishInfo info : list) {
                if (info.isChecked) {
                    return false;
                }
            }
        }
        return isNoItem;
    }

    public boolean isAllNotSelectedInGroup(int groupId) {
        boolean isNoItem = true;
        if (groupId >= mGroupList.size()) {
            return isNoItem;
        }
        ArrayList<RubbishInfo> list = mGroupList.get(groupId);
        if (null != list) {
            for (RubbishInfo info : list) {
                if (info.isChecked) {
                    return false;
                }
            }
        }
        return isNoItem;
    }

    public void startDeleteRubbish(int cleanType) {
        for (int group = 0; group < mGroupList.size(); group++) {
            deleteGroupRubbishs(cleanType, group);
        }
        sendMsg2Handler(MsgConst.DEL_END, -1, 0, null);
    }

    public void startDeleteRubbishByType(int cleanType, int rubbishType) {
        deleteGroupRubbishs(cleanType, rubbishType);
        sendMsg2Handler(MsgConst.DEL_END, -1, 0, null);
    }

    public long calGroupSelectedSize(int group) {
        long size = 0;
        if (isGroupOutOfBound(group)) {
            return size;
        }

        ArrayList<RubbishInfo> list = mGroupList.get(group);
        if (list == null || list.isEmpty()) {
            return size;
        }

        for (RubbishInfo info : list) {
            if (info.isChecked) {
                size += info.size;
            }
        }

        return size;
    }

    public boolean isGroupOutOfBound(int group) {
        if (mGroupList == null) {
            Log.d(TAG, "isGroupOutOfBound mGroupList is null");
            return true;
        }
        int size = mGroupList.size();
        Log.d(TAG, "isGroupOutOfBound group=" + group + ", size=" + size);
        return group < 0 || group >= size;
    }

    public int getGroupSize() {
        return mGroupList.size();
    }

    public int getRubbishInfoListSize(int group) {
        return mGroupList.get(group).size();
    }

    public ArrayList<RubbishInfo> getRubbishInfoList(int group) {
        return mGroupList.get(group);
    }

    public void removeRubbishInfo(int cleanType, int rubbishType, int groupPosition, RubbishInfo rInfo) {
        if (cleanType != mCleanType) {
            return;
        }

        ArrayList<RubbishInfo> list = mGroupList.get(rubbishType);
        if (list != null) {
            list.remove(rInfo);
        }

        ArrayList<GroupKeyInfo> expandableGroupKeyList = getExpandableGroupKeyList(cleanType, rubbishType);
        if (expandableGroupKeyList != null) {
            GroupKeyInfo kInfo = expandableGroupKeyList.get(groupPosition);
            ArrayList<RubbishInfo> gRubbishInfolist = getExpandableGroupData(cleanType, rubbishType,
                    kInfo.key);
            if (kInfo != null && gRubbishInfolist != null) {
                gRubbishInfolist.remove(rInfo);
                if (gRubbishInfolist.isEmpty()) {
                    getExpandableGroupMap(cleanType, rubbishType).remove(kInfo.key);
                    getExpandableGroupKeyList(cleanType, rubbishType).remove(kInfo);
                }
            }
        }
    }

    public long calAllGroupsSize() {
        long size = 0L;
        if (mGroupList == null) {
            return size;
        }
        for (int i = 0; i < mGroupList.size(); i++) {
            try {
                ArrayList<RubbishInfo> list = mGroupList.get(i);
                for (RubbishInfo info : list) {
                    size += info.size;
                }
            } catch (Exception e) {
                Log.e(TAG, "calAllGroupSize throw exception");
                return 0;
            }
        }
        Log.d(TAG, "calAllGroupSize, size = " + Formatter.formatShortFileSize(mContext, size));
        return size;
    }

    // TODO: if file is very large, maybe have some problems(eg:ANR)
    public void deleteFileReally(String path) {
        if (path == null) {
            return;
        }
        if (FileUtil.isWhiteListFile(path)) {
            return;
        }
        FileUtil.recurDelete(new File(path));
        // Gionee <houjie><2015-10-08> add for CR01562723 begin
        UnitUtil.updateMediaVolume(mContext, mAsyncQuery, path);
        // Gionee <houjie><2015-10-08> add for CR01562723 end
    }

    // TODO:according to id to delete files has some problems
    public void syncLocalDatabase(int token, long id) {
        if (mQueryHandler == null) {
            return;
        }
        Uri uri = Files.getContentUri("external");
        Uri newUri = ContentUris.withAppendedId(uri, id);
        Log.d(TAG, "syncLocalDatabase newUri:" + newUri.toString());
        if (null != mContext.getContentResolver().acquireProvider(newUri)) {
            mQueryHandler.startDelete(token, null, newUri, null, null);
        }
    }

    public void delItemByDialog(long size) {
        updateTotalSizeWhenDel(size);
        sendMsg2Handler(MsgConst.DEL_ITEM_BY_DIALOG, -1, 0, Long.valueOf(size));
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public long getSelectedSize() {
        return mSelectedSize;
    }

    public long getCleannedSize() {
        return mCleannedSize;
    }

    public void resetCleannedSize() {
        if (mCleannedSize != 0) {
            mCleannedSize = 0;
        }
    }

    public void calculateSelectedSize() {
        mSelectedSize = 0;
        for (int i = 0; i < getGroupSize(); i++) {
            mSelectedSize += calGroupSelectedSize(i);
        }
    }

    public void registerRubbishCleanerScanListener(RubbishCleanerScanListener listener) {
        mRubbishCleanerScanListeners.add(listener);
    }

    public void unregisterRubbishCleanerScanListener(RubbishCleanerScanListener listener) {
        mRubbishCleanerScanListeners.remove(listener);
    }

    public void registerRubbishCleanerDelListener(RubbishCleanerDelListener listener) {
        mRubbishCleanerDelListeners.add(listener);
    }

    public void unregisterRubbishCleanerDelListener(RubbishCleanerDelListener listener) {
        mRubbishCleanerDelListeners.remove(listener);
    }

    public ArrayList<GroupKeyInfo> getExpandableGroupKeyList(int cleanType, int rubbishType) {
        if (mCleanType == cleanType) {
            if ((cleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL && rubbishType == CleanTypeConst.CACHE)
                    || (cleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && rubbishType == DeeplyCleanTypeConst.CACHE)) {
                return mCacheKeyList;
            } else if (cleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && rubbishType == DeeplyCleanTypeConst.BIGFILE) {
                return mBigFileKeyList;
            }
        }
        return null;
    }

    public HashMap<String, ArrayList<RubbishInfo>> getExpandableGroupMap(int cleanType, int rubbishType) {
        if (mCleanType == cleanType) {
            if ((cleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL && rubbishType == CleanTypeConst.CACHE)
                    || (cleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && rubbishType == DeeplyCleanTypeConst.CACHE)) {
                return mCacheListGroupMap;
            } else if (cleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && rubbishType == DeeplyCleanTypeConst.BIGFILE) {
                return mBigFileListGroupMap;
            }
        }
        return null;
    }

    public ArrayList<RubbishInfo> getExpandableGroupData(int cleanType, int rubbishType, String key) {
        if (mCleanType == cleanType && key != null) {
            if ((cleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL && rubbishType == CleanTypeConst.CACHE)
                    || (cleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && rubbishType == DeeplyCleanTypeConst.CACHE)) {
                return mCacheListGroupMap.get(key);
            } else if (cleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY
                    && rubbishType == DeeplyCleanTypeConst.BIGFILE) {
                return mBigFileListGroupMap.get(key);
            }
        }
        return null;
    }

    public ArrayList<RubbishInfo> getGroupData(int cleanType, int rubbishType) {
        return getRubbishInfoList(rubbishType);
    }

    // private
    private RubbishCleanerStrategy createRubbishCleanStrategy(int dataManagerType) {
        if (dataManagerType == MsgConst.DATA_MANAGER_TYPE_DEEPLY) {
            return new RubbishCleanDeeplyStrategy();
        } else {
            return new RubbishCleanNormalStrategy();
        }
    }

    private void startScanRubbish() {
        callRubbishCleanerScanStart();
        mRubbishCleanerStrategy.startScanRubbish();
    }

    private void sendMsg2Handler(int what, int group, int arg2, Object obj) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(what, group, arg2, obj);
            mHandler.sendMessage(msg);
        }
    }

    private RubbishInfo createAdRubbish(String name, String path) {
        RubbishInfo info = new RubbishInfo();
        info.name = name;
        info.desc = name;
        info.path = path;
        info.isChecked = true;
        long size = 0;
        try {
            size = mKsCleaner.pathCalcSize(path);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        info.size = size;

        return info;
    }

    private void startScanAd(final int token) {
        final IAdDirCallback.Stub adCallback = new IAdDirCallback.Stub() {
            @Override
            public void onStartScan(int nTotalScanItem) throws RemoteException {
            }

            @Override
            public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
                if (isServing()) {
                    sendMsg2Handler(MsgConst.SCAN_ITEM, mRubbishCleanerStrategy.getAdScanType(), 0, desc);
                }
                return false;
            }

            @Override
            public void onFindAdDir(String name, String dirPath) throws RemoteException {
                if (mAdList != null && isServing()) {
                    RubbishInfo info = createAdRubbish(name, dirPath);
                    mAdList.add(info);
                    sendMsg2Handler(MsgConst.FIND_ITEM, mRubbishCleanerStrategy.getAdScanType(), token, Long.valueOf(info.size));
                }
            }

            @Override
            public void onAdDirScanFinish() throws RemoteException {
                if (isServing()) {
                    sendMsg2Handler(MsgConst.END, mRubbishCleanerStrategy.getAdScanType(), token, null);
                }
            }
        };

        new Thread() {
            @Override
            public void run() {
                try {
                    mKsCleaner.init(mLanguage, mCountry);
                    mKsCleaner.scanAdDir(adCallback);
                } catch (Exception e) {
                    Log.d(TAG, "startScanAd e:" + e);
                    if (isServing()) {
                        sendMsg2Handler(MsgConst.END, mRubbishCleanerStrategy.getAdScanType(), token, null);
                    }
                    return;
                }
            }
        }.start();

    }

    private RubbishInfo createResidualRubbish(String name, String path) {
        RubbishInfo info = new RubbishInfo();
        info.name = name;
        info.desc = name;
        info.path = path;
        info.isChecked = mRubbishCleanerStrategy.getResidualChecked();
        long size = 0;
        try {
            size = mKsCleaner.pathCalcSize(path);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        info.size = size;

        return info;
    }

    private void startScanResidual(final int token) {
        final IResidualCallback.Stub residualCallback = new IResidualCallback.Stub() {
            @Override
            public void onStartScan(int nTotalScanItem) throws RemoteException {

            }

            @Override
            public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
                if (isServing()) {
                    sendMsg2Handler(MsgConst.SCAN_ITEM, mRubbishCleanerStrategy.getResidualScanType(), 0, desc);
                }
                return false;
            }

            @Override
            public void onFindResidualItem(String dirPath, String descName, boolean bAdviseDel,
                                           String alertInfo) throws RemoteException {
                // chenyee <liu_shuang> <2017-11-17> modify for SW17W16A-1185 begin
                if (FileUtil.isWhiteListFile(dirPath) || !isServing()) {
                    return;
                }
                if (mResidualList != null) {
                    if (TextUtils.isEmpty(descName)) {
                        //如果传入的descName为空或者字符串长度为0，则显示“未知应用”字段。
                        descName = mResources.getString(R.string.unknown_apk);
                    }
                    RubbishInfo info = createResidualRubbish(descName, dirPath);
                    Log.d(TAG, "onFindResidualItem: " + info);
                    // chenyee <liu_shuang> <2017-11-17> modify for SW17W16A-1185 end
                    /*guoxt modify for CSW1703A-756 begin*/
                    /*guoxt modify for CR01639812 begin*/
                    if (!dirPath.equals("/storage/emulated/0/.tmfs") &&
                            !dirPath.equals("/storage/emulated/0/weather")) {
                        mResidualList.add(info);
                    }
                    /*guoxt modify for CSW1703A-756 end*/
                    /*guoxt modify for CR01639812 end*/
                    sendMsg2Handler(MsgConst.FIND_ITEM, mRubbishCleanerStrategy.getResidualScanType(), token, Long.valueOf(info.size));
                }
            }

            @Override
            public void onResidualScanFinish() throws RemoteException {
                // Gionee <yangxinruo> <2016-1-26> modify for CR01631907 begin
                // 可能是线程重复启动了,连续回调本函数
                if (isServing() && mRubbishCleanerStrategy != null) {
                    sendMsg2Handler(MsgConst.END, mRubbishCleanerStrategy.getResidualScanType(), token, null);
                }
                // Gionee <yangxinruo> <2016-1-26> modify for CR01631907 end
            }
        };

        new Thread() {
            @Override
            public void run() {
                try {
                    mKsCleaner.init(mLanguage, mCountry);
                    mKsCleaner.scanResidual(mRubbishCleanerStrategy.getCMCleanScanType(), residualCallback);
                } catch (Exception e) {
                    Log.d(TAG, "startScanResidual e:" + e);
                    if (isServing() && mRubbishCleanerStrategy != null) {
                        sendMsg2Handler(MsgConst.END, mRubbishCleanerStrategy.getResidualScanType(), token, null);
                    }
                    return;
                }
            }
        }.start();

    }

    private RubbishInfo createCacheRubbish(String desc, String path, String pkgName, String descx) {
        RubbishInfo info = new RubbishInfo();
        info.path = path;
        info.pkgName = pkgName;
        info.isChecked = mRubbishCleanerStrategy.getCacheChecked();
        info.icon = ApkInfoHelper.getInstalledApkIcon(mPkgMgr, info.pkgName);
        info.name = ApkInfoHelper.getInstalledApkLabel(mPkgMgr, pkgName);
        info.desc = desc;
        Pattern p_html;
        Matcher m_html;

        String regEx_html = "<[^>]+>";
        p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        m_html = p_html.matcher(descx);
        descx = m_html.replaceAll("");
        info.descx = descx;
        long size = 0;
        try {
            size = mKsCleaner.pathCalcSize(path);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        info.size = size;

        return info;
    }

    private void startScanCache(final int token) {
        final ICacheCallback.Stub cacheCallback = new ICacheCallback.Stub() {
            @Override
            public void onStartScan(int nTotalScanItem) throws RemoteException {

            }

            @Override
            public boolean onScanItem(String desc, int nProgressIndex) throws RemoteException {
                if (isServing()) {
                    sendMsg2Handler(MsgConst.SCAN_ITEM, mRubbishCleanerStrategy.getCacheScanType(), 0, desc);
                }
                return false;
            }

            @Override
            public void onFindCacheItem(String cacheType, String dirPath, String pkgName, boolean bAdviseDel,
                                        String alertInfo, String descx) throws RemoteException {
                if (mCacheList != null && isServing()) {
                    RubbishInfo info = createCacheRubbish(cacheType, dirPath, pkgName, descx);
                    Log.d(TAG, "onFindCacheItem info:" + info);
                    mCacheList.add(info);
                    addToCacheListGroupMap(info);
                    sendMsg2Handler(MsgConst.FIND_ITEM, mRubbishCleanerStrategy.getCacheScanType(), token, Long.valueOf(info.size));
                }
            }

            @Override
            public void onCacheScanFinish() throws RemoteException {
                if (isServing()) {
                    Log.d(TAG, "mCacheList.size():" + mCacheList.size());
                    sendMsg2Handler(MsgConst.END, mRubbishCleanerStrategy.getCacheScanType(), token, null);
                }
            }
        };

        new Thread() {
            @Override
            public void run() {
                try {
                    mKsCleaner.init(mLanguage, mCountry);
                    mKsCleaner.scanCache(mRubbishCleanerStrategy.getCMCleanScanType(), cacheCallback);
                } catch (Exception e) {
                    Log.d(TAG, "startScanCache e:" + e);
                    if (isServing()) {
                        sendMsg2Handler(MsgConst.END, mRubbishCleanerStrategy.getCacheScanType(), token, null);
                    }
                    return;
                }
            }
        }.start();

    }

    private void initApkRubbishList(Cursor cursor, int token) {
        while (cursor.moveToNext()) {
            if (FileUtil.isWhiteListFile(FileNameUtils.getPathFromAbsolutelypath(cursor.getString(0)))) {
                continue;
            }
            RubbishInfo info = new RubbishInfo();
            info.path = cursor.getString(0); // _data
            info.size = cursor.getLong(1); // _size
            info.db_id = cursor.getLong(2); // _id
            info.isChecked = false;
            info.pkgName = ApkInfoHelper.getUninstalledApkPkgName(mPkgMgr, info.path);
            info.isInstalled = ApkInfoHelper.getApkInstallState(mPkgMgr, info.pkgName);
            if (info.isInstalled) {
                info.icon = ApkInfoHelper.getInstalledApkIcon(mPkgMgr, info.pkgName);
                info.name = ApkInfoHelper.getInstalledApkLabel(mPkgMgr, info.pkgName);
            } else {
                info.icon = ApkInfoHelper.getUninstalledApkIncon(mPkgMgr, info.path);
                info.name = ApkInfoHelper.getUninstalledApkLabel(mPkgMgr, info.path);
            }
            info.desc = info.name;

            if (info.icon != null && info.name != null && 0L != info.size && token == mToken) {
                mApkList.add(info);
                sendMsg2Handler(MsgConst.FIND_ITEM, CleanTypeConst.APK, token, Long.valueOf(info.size));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void startScanApk(final int token) {
        final Uri uri = Files.getContentUri("external");
        final String[] columns = new String[]{"_data", "_size", "_id"};
        final String selection = "_data like '%.apk'";

        mQueryHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "start to query database to find apk");
                Log.d(TAG, "startScanApk token:" + token);
                mApkList.clear();
                mQueryHandler.startQuery(token, null, uri, columns, selection, null, null);
            }

        });
    }

    private void deleteGroupRubbishs(int cleanType, int group) {
        if (isGroupOutOfBound(group)) {
            return;
        }
        ArrayList<RubbishInfo> list = mGroupList.get(group);
        if (list == null) {
            return;
        }
        int count = list.size();
        ArrayList<Integer> flagList = new ArrayList<Integer>();
        try {
            for (int i = 0; i < count; i++) {
                if (list.get(i).isChecked) {
                    flagList.add(i);
                }
            }

            for (int i = flagList.size() - 1; i >= 0; i--) {
                int index = flagList.get(i);
                RubbishInfo rInfo = list.get(index);
                if (null == rInfo) {
                    list.remove(index);
                    continue;
                }
                deleteFileReally(rInfo.path);
                list.remove(rInfo);
                if (group == CleanTypeConst.APK && cleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL) {
                    syncLocalDatabase(APK_QUERY_TOKEN, rInfo.db_id);
                } else if (group == DeeplyCleanTypeConst.BIGFILE
                        && cleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY) {
                    syncLocalDatabase(BIGFILE_QUERY_TOKEN, rInfo.db_id);
                }
                sendMsg2Handler(MsgConst.DEL_ITEM, group, 0, Long.valueOf(rInfo.size));
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "deleteGroupRubbishs throws IndexOutOfBoundsException, group = " + group);
            return;
        }

    }

    private void startScanBigfile(final int token) {
        final Uri uri = Files.getContentUri("external");
        final String[] columns = new String[]{"_data", "_size", "_id", "mime_type"};
        final String selection = "_size > 10485760"; // > 10M

        mQueryHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "start to query database to find big files");
                Log.d(TAG, "startScanBigfile token:" + token);
                mQueryHandler.startQuery(token, null, uri, columns, selection, null, null);
            }

        });
    }

    private void initBigfileList(Cursor cursor, int token) {
        while (cursor.moveToNext()) {
            if (FileUtil.isWhiteListFile(FileNameUtils.getPathFromAbsolutelypath(cursor.getString(0)))) {
                continue;
            }
            RubbishInfo info = new RubbishInfo();
            info.path = cursor.getString(0); // _data
            info.size = cursor.getLong(1); // _size
            info.db_id = cursor.getLong(2); // _id
            info.isChecked = false;
            info.type = MimeTypeUtils.guessExtensionFromMime(cursor.getString(3));// mime_type
            info.name = getBigfileNameByType("bigfile_name_" + info.type);
            info.desc = FileNameUtils.getFileNameFromAbsolutelypath(info.path);
            if (token == mToken) {
                mBigfileList.add(info);
                addToBigFileListGroupMap(info);
                sendMsg2Handler(MsgConst.FIND_ITEM, DeeplyCleanTypeConst.BIGFILE, token, Long.valueOf(info.size));
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        bigFileListSortBySize();
    }

    private long longValue(Object obj) {
        Long lSize = (Long) obj;
        return lSize.longValue();
    }

    private void addToCacheListGroupMap(RubbishInfo info) {
        ArrayList<RubbishInfo> cachelist = mCacheListGroupMap.get(info.name);
        if (cachelist == null) {
            cachelist = new ArrayList<RubbishInfo>();
            mCacheListGroupMap.put(info.name, cachelist);

            GroupKeyInfo groupKeyinfo = new GroupKeyInfo(info.name, info.name, info.icon);
            mCacheKeyList.add(groupKeyinfo);
        }
        Log.d(TAG, "addToCacheListGroupMap info" + info);
        cachelist.add(info);
    }

    private void addToBigFileListGroupMap(RubbishInfo info) {
        ArrayList<RubbishInfo> bigFilelist = mBigFileListGroupMap.get(info.type);
        if (bigFilelist == null) {
            bigFilelist = new ArrayList<RubbishInfo>();
            mBigFileListGroupMap.put(info.type, bigFilelist);

            GroupKeyInfo groupKeyinfo = new GroupKeyInfo(info.type, info.name,
                    mContext.getDrawable(FileIconHelper.getFileIconByFiletype(info.type)));
            mBigFileKeyList.add(groupKeyinfo);
        }
        bigFilelist.add(info);
    }

    private void bigFileListSortBySize() {
        Iterator iter = mBigFileListGroupMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ArrayList<RubbishInfo>> entry = (Map.Entry<String, ArrayList<RubbishInfo>>) iter
                    .next();
            ArrayList<RubbishInfo> bigFilelist = entry.getValue();
            Collections.sort(bigFilelist, new Comparator<RubbishInfo>() {
                @Override
                public int compare(RubbishInfo lhs, RubbishInfo rhs) {
                    return Long.valueOf(rhs.size).compareTo(lhs.size);
                }
            });
        }
    }

    private void callRubbishCleanerDelItem(Object obj) {
        for (RubbishCleanerDelListener listener : mRubbishCleanerDelListeners) {
            listener.onDelItem(obj);
        }
    }

    private void callRubbishCleanerDelItemByDialog(Object obj) {
        for (RubbishCleanerDelListener listener : mRubbishCleanerDelListeners) {
            listener.onDelItemByDialog(obj);
        }
    }

    private void callRubbishCleanerDelEnd() {
        for (RubbishCleanerDelListener listener : mRubbishCleanerDelListeners) {
            listener.onDelEnd();
        }
    }

    private void callRubbishCleanerScanStart() {
        for (RubbishCleanerScanListener listener : mRubbishCleanerScanListeners) {
            listener.onScanStart();
        }
    }

    private void callRubbishCleanerScanItem(int group, Object obj) {
        for (RubbishCleanerScanListener listener : mRubbishCleanerScanListeners) {
            listener.onScanItem(group, obj);
        }
    }

    private void callRubbishCleanerScanFindItem(int group, Object obj) {
        for (RubbishCleanerScanListener listener : mRubbishCleanerScanListeners) {
            listener.onFindItem(group, obj);
        }
    }

    private void callRubbishCleanerScanEnd(int group) {
        for (RubbishCleanerScanListener listener : mRubbishCleanerScanListeners) {
            listener.onScanEnd(group);
        }
    }

    private void updateTotalSizeWhenDel(long size) {
        mTotalSize -= size;
        if (mTotalSize < 0) {
            mTotalSize = 0L;
        }
    }

    private String getBigfileNameByType(String type) {
        final String packageName = mContext.getPackageName();
        int id = mResources.getIdentifier(type, "string", packageName);
        return mResources.getString(id);
    }

    private boolean isServing() {
        return mServiceConn != null;
    }

    // class
    private class MyHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case MsgConst.START:
                    break;
                case MsgConst.SCAN_ITEM:
                    callRubbishCleanerScanItem(msg.arg1, msg.obj);
                    break;
                case MsgConst.FIND_ITEM:
                    if (msg.arg2 == mToken) {
                        mTotalSize += longValue(msg.obj);
                        callRubbishCleanerScanFindItem(msg.arg1, msg.obj);
                    }
                    break;
                case MsgConst.END:
                    if (msg.arg2 == mToken) {
                        callRubbishCleanerScanEnd(msg.arg1);
                    }
                    break;
                case MsgConst.DEL_ITEM:
                    mTotalSize -= longValue(msg.obj);
                    if (mTotalSize < 0) {
                        mTotalSize = 0L;
                    }
                    mCleannedSize += longValue(msg.obj);
                    callRubbishCleanerDelItem(msg.obj);
                    break;
                case MsgConst.DEL_ITEM_BY_DIALOG:
                    callRubbishCleanerDelItemByDialog(msg.obj);
                    break;
                case MsgConst.DEL_END:
                    callRubbishCleanerDelEnd();
                    break;
                default:
                    break;
            }
        }
    }

    private class DbQueryHandler extends AsyncQueryHandler {

        public DbQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(final int token, Object cookie, final Cursor cursor) {
            Log.d(TAG, "onQueryComplete token:" + token);
            boolean isValidApkToken = verifyTokenByMask(APK_QUERY_TOKEN, token);
            boolean isValidBigFileToken = verifyTokenByMask(BIGFILE_QUERY_TOKEN, token);
            Log.d(TAG, "onQueryComplete isValidApkToken:" + isValidApkToken + ", isValidBigFileToken:" + isValidBigFileToken);
            if (!isServing() || (!isValidApkToken && !isValidBigFileToken)) {
                if (null != cursor) {
                    cursor.close();
                }
                return;
            }

            if (cursor == null) {
                int type;
                if (isValidApkToken) {
                    type = CleanTypeConst.APK;
                } else if (isValidBigFileToken) {
                    type = DeeplyCleanTypeConst.BIGFILE;
                } else {
                    return;
                }
                sendMsg2Handler(MsgConst.END, type, token, null);
                return;
            }

            if (isValidApkToken && mApkList != null) {
                new Thread() {
                    public void run() {
                        initApkRubbishList(cursor, token);
                        sendMsg2Handler(MsgConst.END, CleanTypeConst.APK, token, null);
                    }
                }.start();
            }

            if (isValidBigFileToken && mBigfileList != null) {
                new Thread() {
                    public void run() {
                        initBigfileList(cursor, token);
                        sendMsg2Handler(MsgConst.END, DeeplyCleanTypeConst.BIGFILE, token, null);
                    }
                }.start();
            }
        }
    }

    // Gionee <houjie><2015-10-08> add for CR01562723 begin
    private class MediaHandler extends AsyncQueryHandler {
        public MediaHandler(ContentResolver cr) {
            super(cr);
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
            Log.d(TAG, "onDeleteComplete result:" + result);
        }
    }
    // Gionee <houjie><2015-10-08> add for CR01562723 end

    private class RubbishCleanNormalStrategy implements RubbishCleanerStrategy {
        public void init() {
            mToken = createTokenByMask(APK_QUERY_TOKEN);
            mGroupList.clear();
            mGroupList.add(mCacheList);
            mGroupList.add(mAdList);
            mGroupList.add(mResidualList);
            mGroupList.add(mApkList);
        }

        public void recycle() {
            mCacheList.clear();
            mAdList.clear();
            mResidualList.clear();
            mApkList.clear();
            mGroupList.clear();

            mCacheKeyList.clear();

            Iterator iter = mCacheListGroupMap.values().iterator();
            while (iter.hasNext()) {
                ArrayList<RubbishInfo> list = (ArrayList<RubbishInfo>) iter.next();
                list.clear();
            }
            mCacheListGroupMap.clear();
        }

        public void startScanRubbish() {
            startScanAd(mToken);
            startScanResidual(mToken);
            startScanApk(mToken);
            startScanCache(mToken);
        }

        public int getCMCleanScanType() {
            return CMCleanConst.MASK_SCAN_COMMON;
        }

        public int getAdScanType() {
            return CleanTypeConst.AD;
        }

        public int getCacheScanType() {
            return CleanTypeConst.CACHE;
        }

        public int getApkScanType() {
            return CleanTypeConst.APK;
        }

        public int getResidualScanType() {
            return CleanTypeConst.RESIDUAL;
        }

        public int getBigFileScanType() {
            throw new RuntimeException("getBigFileScanType not support");
        }

        public boolean getCacheChecked() {
            return true;
        }

        public boolean getResidualChecked() {
            return true;
        }
    }

    private class RubbishCleanDeeplyStrategy implements RubbishCleanerStrategy {
        public void init() {
            mToken = createTokenByMask(BIGFILE_QUERY_TOKEN);
            mGroupList.clear();
            mGroupList.add(mCacheList);
            mGroupList.add(mResidualList);
            mGroupList.add(mBigfileList);
        }

        public void recycle() {
            mCacheList.clear();
            mResidualList.clear();
            mBigfileList.clear();
            mGroupList.clear();

            mCacheKeyList.clear();
            mBigFileKeyList.clear();

            Iterator iter = mCacheListGroupMap.values().iterator();
            while (iter.hasNext()) {
                ArrayList<RubbishInfo> list = (ArrayList<RubbishInfo>) iter.next();
                list.clear();
            }
            mCacheListGroupMap.clear();

            iter = mBigFileListGroupMap.values().iterator();
            while (iter.hasNext()) {
                ArrayList<RubbishInfo> list = (ArrayList<RubbishInfo>) iter.next();
                list.clear();
            }
            mBigFileListGroupMap.clear();
        }

        public void startScanRubbish() {
            startScanCache(mToken);
            startScanResidual(mToken);
            startScanBigfile(mToken);
        }

        public int getCMCleanScanType() {
            return CMCleanConst.MASK_SCAN_ADVANCED;
        }

        public int getAdScanType() {
            throw new RuntimeException("getAdScanType not support");
        }

        public int getCacheScanType() {
            return DeeplyCleanTypeConst.CACHE;
        }

        public int getApkScanType() {
            throw new RuntimeException("getApkScanType not support");
        }

        public int getResidualScanType() {
            return DeeplyCleanTypeConst.RESIDUAL;
        }

        public int getBigFileScanType() {
            return DeeplyCleanTypeConst.BIGFILE;
        }

        public boolean getCacheChecked() {
            return false;
        }

        public boolean getResidualChecked() {
            return false;
        }
    }

    public class GroupKeyInfo {
        public String key;
        public String name;
        public Drawable icon;

        private GroupKeyInfo(String key, String name, Drawable icon) {
            this.key = key;
            this.name = name;
            this.icon = icon;
        }
    }

    private int createTokenByMask(int mask) {
        mToken &= TOKEN_MASK;
        Log.d(TAG, "createTokenByMask mToken:" + mToken);
        return (++mToken) | mask;
    }

    private boolean verifyTokenByMask(int mask, int token) {
        return (mask & token) == mask && ((mToken | mask) == token);
    }
}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end
