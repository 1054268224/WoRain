package com.cydroid.systemmanager.rubbishcleaner.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.text.format.Formatter;

import com.cleanmaster.sdk.CMCleanConst;
import com.cleanmaster.sdk.IAdDirCallback;
import com.cleanmaster.sdk.ICacheCallback;
import com.cleanmaster.sdk.IKSCleaner;
import com.cleanmaster.sdk.IResidualCallback;
import com.cydroid.softmanager.R;
import com.keniu.security.CleanMasterSDK;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerMainActivity;
import com.cydroid.systemmanager.rubbishcleaner.common.CleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.utils.FileUtil;
import com.cydroid.systemmanager.utils.Log;
import com.cydroid.systemmanager.utils.PreferenceHelper;
import com.cydroid.systemmanager.utils.ServiceUtil;
import com.cydroid.systemmanager.utils.UnitUtil;

public class RubbishScanService extends Service {
    private static final String TAG = "CyeeRubbishCleaner/RubbishCleanerService";

    private IKSCleaner mKSCleaner;
    private ServiceConnection mServiceConn;
    private String mLanguage;
    private String mCountry;
    private long mTotalSize;
    private boolean[] mFlagArray = {false, false, false, false};
    private BackgroundQueryHandler mQueryHandler;
    private boolean DEBUG = true;
    private boolean onlycacheClean = false;
    private boolean startBySystemCheck = false;
    private boolean isSendBroadcast = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(DEBUG, TAG, "RubbishScanService onCreate()");
        super.onCreate();
       try {
            CleanMasterSDK.getInstance().Initialize(this);
        }catch (Exception e){
            Log.e(TAG, "onCreate err " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtil.handleStartForegroundServices(this);
        startBySystemCheck = intent.getBooleanExtra("startBySystemCheck", false);
        //Chenyee guoxt modify for CSW1703VF-53 begin
        String action = intent.getAction();
        if(action != null && action.equals("com.cydroid.systemanager.fourclock.clear")){
            onlycacheClean = true;
        }else {
            onlycacheClean = false;
        }
        mQueryHandler = new BackgroundQueryHandler(getContentResolver());
        Locale locale = getResources().getConfiguration().locale;
        mLanguage = locale.getLanguage();
        mCountry = locale.getCountry();
        bindKSService(intent);
        ServiceUtil.handleStartForegroundServices(RubbishScanService.this);//xuanyuadd
       //Chenyee guoxt modify for CSW1703VF-53 end
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG, TAG, "RubbishScanService is destroy...");
        super.onDestroy();
        try {
            unbindService(mServiceConn);
        }catch (Exception e){
            Log.e(TAG, "unbindService err " + e.getMessage());
        }
        mTotalSize = 0;
        mQueryHandler = null;
    }

    private void bindKSService(final Intent intent) {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(DEBUG, TAG,
                        "RubbishScanService, service connected, can scan rubbish");
                mKSCleaner = IKSCleaner.Stub.asInterface(service);
                scanPhoneRubbish();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(DEBUG, TAG,
                        "RubbishScanService, service disconnected, maybe have some problems");
            }

        };
        // Intent remoteIntent = new Intent("com.cleanmaster.CleanService");
        Intent remoteIntent = new Intent(this, com.cleanmaster.CleanService.class);
        bindService(remoteIntent, mServiceConn, BIND_AUTO_CREATE);
    }




    private void scanPhoneRubbish() {
        //Chenyee guoxt modify for CSW1703VF-53 begin
        if (onlycacheClean){
            scanCaches();
        }else{
            scanCaches();
            scanAds();
            scanResiduals();
            scanApks();
        }
        //Chenyee guoxt modify for CSW1703VF-53 end

    }

    private long calculateItemSize(String path) {
        long size = 0;
        if (mKSCleaner == null) {
            return size;
        }
        try {
            size = mKSCleaner.pathCalcSize(path);
        } catch (RemoteException e) {
            e.printStackTrace();
            return size;
        }

        return size;
    }

    private void scanCaches() {
        final ICacheCallback.Stub cacheCallbackStub = new ICacheCallback.Stub() {
            @Override
            public void onStartScan(int nTotalScanItem) throws RemoteException {
            }

            @Override
            public boolean onScanItem(String desc, int nProgressIndex)
                    throws RemoteException {
                return false;
            }

            @Override
            public void onFindCacheItem(String cacheType, String dirPath,
                                        String pkgName, boolean bAdviseDel, String alertInfo,
                                        String descx) throws RemoteException {
                long size = calculateItemSize(dirPath);
                Log.d(DEBUG, TAG, "cache dirPath" + dirPath + ",size:" + size);
                sendScanMsg(MsgConst.FIND_ITEM, -1, size);
                //Chenyee guoxt modify for CSW1703VF-53 begin
                if(onlycacheClean && MsgConst.cy1703VF) {
                    deleteFileReally(dirPath);
                }
                //Chenyee guoxt modify for CSW1703VF-53 end
            }

            @Override
            public void onCacheScanFinish() throws RemoteException {
                sendScanMsg(MsgConst.END, CleanTypeConst.CACHE, 0);

            }
        };
        new Thread() {
            public void run() {
                if (mKSCleaner == null) {
                    return;
                }
                Log.d(DEBUG, TAG,
                        "RubbishScanService, begin scanCache() in new thread");
                try {
                    mKSCleaner.init(mLanguage, mCountry);
                    mKSCleaner.scanCache(CMCleanConst.MASK_SCAN_COMMON,
                            cacheCallbackStub);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }.start();
    }

    private void scanAds() {
        final IAdDirCallback.Stub adCallbackStub = new IAdDirCallback.Stub() {

            @Override
            public void onStartScan(int nTotalScanItem) throws RemoteException {
            }

            @Override
            public boolean onScanItem(String desc, int nProgressIndex)
                    throws RemoteException {
                return false;
            }

            @Override
            public void onFindAdDir(String name, String dirPath)
                    throws RemoteException {
                long size = calculateItemSize(dirPath);
                Log.d(DEBUG, TAG, "AD dirPath" + dirPath + ",size:" + size);
                sendScanMsg(MsgConst.FIND_ITEM, -1, size);
            }

            @Override
            public void onAdDirScanFinish() throws RemoteException {
                sendScanMsg(MsgConst.END, CleanTypeConst.AD, 0);
            }
        };
        new Thread() {
            public void run() {
                if (mKSCleaner == null) {
                    return;
                }
                Log.d(DEBUG, TAG,
                        "RubbishScanService, begin scanAdDir() in new thread");
                try {
                    mKSCleaner.init(mLanguage, mCountry);
                    mKSCleaner.scanAdDir(adCallbackStub);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }.start();
    }

    private void scanResiduals() {
        final IResidualCallback.Stub residualCallStub = new IResidualCallback.Stub() {

            @Override
            public void onStartScan(int nTotalScanItem) throws RemoteException {
            }

            @Override
            public boolean onScanItem(String desc, int nProgressIndex)
                    throws RemoteException {
                return false;
            }

            @Override
            public void onFindResidualItem(String dirPath, String descName,
                                           boolean bAdviseDel, String alertInfo)
                    throws RemoteException {
                long size = calculateItemSize(dirPath);
                Log.d(DEBUG, TAG, "residual dirPath" + dirPath + ",size:" + size);
                sendScanMsg(MsgConst.FIND_ITEM, -1, size);
            }

            @Override
            public void onResidualScanFinish() throws RemoteException {
                sendScanMsg(MsgConst.END, CleanTypeConst.RESIDUAL, 0);
            }
        };
        new Thread() {
            public void run() {
                if (mKSCleaner == null) {
                    return;
                }
                Log.d(DEBUG, TAG,
                        "RubbishScanService, begin scanResidual() in new thread");
                try {
                    mKSCleaner.init(mLanguage, mCountry);
                    mKSCleaner.scanResidual(CMCleanConst.MASK_SCAN_COMMON,
                            residualCallStub);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }.start();
    }

    private void scanApks() {
        String volumeName = "external";
        final Uri uri = Files.getContentUri(volumeName);
        final String[] columns = new String[]{"_data", "_size"};
        final String selection = "_data like '%.apk'";
        mQueryHandler.post(new Runnable() {
            @Override
            public void run() {
                mQueryHandler.startQuery(0, null, uri, columns, selection,
                        null, null);
            }

        });
    }

    private class BackgroundQueryHandler extends AsyncQueryHandler {

        public BackgroundQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            try {
                if (null == cursor || !cursor.moveToFirst()) {
                    sendScanMsg(MsgConst.END, CleanTypeConst.APK, 0);
                    return;
                }
                Log.d(TAG, "RubbishScanService, onQueryComplete, get apks size");
                do {
                    Log.d(DEBUG, TAG, "apk :"  + ",size:" + cursor.getLong(1));
                    sendScanMsg(MsgConst.FIND_ITEM, -1, cursor.getLong(1)); // _size
                } while (cursor.moveToNext());
            } catch (Exception e) {
                // TODO: handle exception
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            sendScanMsg(MsgConst.END, CleanTypeConst.APK, 0);
        }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case MsgConst.FIND_ITEM:
                    mTotalSize += Long.valueOf((String) msg.obj);
                    if (startBySystemCheck && mTotalSize >= 100000000){
                        stopServiceAndSendBroadcast();
                    }
                    break;
                case MsgConst.END:
                    setScanFinishFlag(msg.arg1, true);
                    if (isAllScanFinished()) {
                        if (startBySystemCheck){
                            stopServiceAndSendBroadcast();
                        }else {
                            //Chenyee guoxt modify for CSW1703VF-53 begin
                            if (onlycacheClean){
                                onlycacheClean = false;
                            }else {
                                judgeWhetherPopNotification();
                                resetScanFlag();
                            }
                            //Chenyee guoxt modify for CSW1703VF-53 end
                        }
                    }
                    break;
            }
        }

    };

    private boolean isAllScanFinished() {
        for (int i = 0; i < mFlagArray.length; i++) {
            if (!mFlagArray[i]) {
                return false;
            }
        }
        return true;
    }

    private void setScanFinishFlag(int category, boolean isFinished) {
        if (category < 0 && category > CleanTypeConst.APK) {
            return;
        }
        mFlagArray[category] = isFinished;
    }

    private void resetScanFlag() {
        for (int i = 0; i < mFlagArray.length; i++) {
            setScanFinishFlag(i, false);
        }
    }

    private void judgeWhetherPopNotification() {
        String defaultSize = PreferenceHelper.getString(this,
                "rubbish_size_alert_key", "100");
        // Chenyee xionghg 20171216 modify for storage conversion begin
        // long threshold = Long.valueOf(defaultSize) * 1048576; // 1024 * 1024
        long threshold = Long.valueOf(defaultSize) * 1000000; // 1000 * 1000
        // Chenyee xionghg 20171216 modify for storage conversion end
        Log.d(DEBUG, TAG, "RubbishScanService, detected " + mTotalSize
                + " rubbish, threshold is " + threshold);
        if (mTotalSize >= threshold) {
            showNotification(Formatter.formatShortFileSize(this, threshold));
        }
    }

    private void showNotification(String thresholdStr) {
        Intent intent = new Intent(this,
                RubbishCleanerMainActivity.class);
        // Gionee <changph> <2016-09-07> modify for CR01758203 begin
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        // Gionee <changph> <2015-09-07> modify for CR01758203 end
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager notifiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.notifi_title) + thresholdStr)
                .setContentText(getResources().getString(R.string.notifi_content))
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(0x4fc6bb)
                .setShowWhen(false)
                .setAutoCancel(true)
                .setChannelId(ServiceUtil.CHANNEL_ID);
        notifiManager.notify(0, builder.build());
    }

    private void sendScanMsg(int what, int group, long size) {
        String sizeObj = Long.toString(size);
        Message msg = mHandler.obtainMessage(what, group, 0, sizeObj);
        mHandler.sendMessage(msg);
    }
    //Chenyee guoxt modify for CSW1703VF-53 begin
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
        UnitUtil.updateMediaVolume(this,null, path);
        // Gionee <houjie><2015-10-08> add for CR01562723 end
    }
    //Chenyee guoxt modify for CSW1703VF-53 end

    private void stopServiceAndSendBroadcast(){
        if (isSendBroadcast){
            return;
        }
        isSendBroadcast = true;
        Intent intent = new Intent("com.cydroid.softmanager.action.rubbishcheck");
        intent.putExtra("RubbishSize", mTotalSize);
        sendBroadcast(intent);
        try {
            unbindService(mServiceConn);
        }catch (Exception e){
            Log.e(TAG, "unbindService err " + e.getMessage());
        }
        stopSelf();
    }
}
