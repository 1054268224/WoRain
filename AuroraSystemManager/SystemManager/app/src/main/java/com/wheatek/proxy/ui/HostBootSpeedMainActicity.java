package com.wheatek.proxy.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.oneclean.utils.RamAndMemoryHelper;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.systemcheck.SystemCheckItem;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.NameSorting;
import com.example.systemmanageruidemo.actionpresent.BootSpeedPresent;
import com.example.systemmanageruidemo.actionview.BootSpeedView;
import com.example.systemmanageruidemo.bootspeed.BootSpeedMainActicity;
import com.example.systemmanageruidemo.bootspeed.adapter.BootItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.cydroid.softmanager.view.BoostSpeedActivity.GET_RUNNING_PROCESS;
import static com.cydroid.softmanager.view.BoostSpeedActivity.PERCENT_CONSTANT;
import static com.cydroid.softmanager.view.BoostSpeedActivity.STATE_CHECK_MEMORY;
import static com.cydroid.softmanager.view.BoostSpeedActivity.UPDATE_RAM_UI;
import static com.cydroid.softmanager.view.BoostSpeedActivity.translateCapacity;


public class HostBootSpeedMainActicity extends HostProxyActivity<BootSpeedView> implements BootSpeedPresent, RamAndMemoryHelper.RamInfoUpdateCallback, IMemoryCleanNativeCallback {
    private static final String TAG = HostBootSpeedMainActicity.class.getSimpleName();
    private HandlerThread mUpdateMemoryInfoThread;
    private Handler mUpdateMemoryHandler;
    private WhiteListManager mWhiteListManager;
    private List<String> mUserWhitelistedApps;
    private int mUsedMemoryRatio;
    private List<ItemInfo> mRunningAppsList = new ArrayList<>();
    private RamAndMemoryHelper mOneCleanUtil;

    {
        attach(new BootSpeedMainActicity());
    }

    BootSpeedView viewAvtion;

    @Override
    public void setViewAction(BootSpeedView viewAvtion) {
        this.viewAvtion = viewAvtion;
    }

    @Override
    public BootSpeedView getViewAction() {
        return viewAvtion;
    }

    @Override
    public void onRequestScore() {
        SharedPreferences setting_info = this.getSharedPreferences("BootSpeedMain_tem_memorysize", MODE_PRIVATE);
        reSponseScore(setting_info.getInt("MemoryScore", 0));
        startUpdateMemory();
    }

    @Override
    public void reSponseScore(int percent) {
        viewAvtion.onResponseScore(percent);
    }

    List<BootItem> list;
    AppCompatActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.mContext = this;
        initParams();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateMemoryInfoThread != null) {
            mUpdateMemoryInfoThread.quit();
        }
        mRunningAppsList.clear();
        recycleHandler();
    }

    private synchronized void recycleHandler() {
        if (mUpdateMemoryHandler != null) {
            mUpdateMemoryHandler.removeCallbacksAndMessages(null);
            mUpdateMemoryHandler = null;
        }
    }

    @Override
    public void onInitData(List<BootItem> list) {
        this.list = list;
        sendMessageToHandler(GET_RUNNING_PROCESS);
    }

    private void initParams() {
        mOneCleanUtil = RamAndMemoryHelper.getInstance(mContext);
        mOneCleanUtil.setCallback(this);
        mUpdateMemoryInfoThread = new HandlerThread("SystemManager/queryMemoryInfo");
        mUpdateMemoryInfoThread.start();
        mUpdateMemoryHandler = new Handler(mUpdateMemoryInfoThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case STATE_CHECK_MEMORY:
                        String[] memoryInfo = queryMemoryInfo();
                        Message updateUi = mHandler.obtainMessage(UPDATE_RAM_UI);
                        updateUi.obj = memoryInfo;
                        mHandler.sendMessage(updateUi);
                        break;
                    case GET_RUNNING_PROCESS:
                        List<ItemInfo> runnings = getRunningProcess();
                        Message runningsMsg = mHandler.obtainMessage(GET_RUNNING_PROCESS);
                        runningsMsg.obj = runnings;
                        mHandler.sendMessage(runningsMsg);
                        break;
                    default:
                        break;
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                mWhiteListManager = WhiteListManager.getInstance();
                mWhiteListManager.init(mContext);
                mUserWhitelistedApps = mWhiteListManager.getUserWhiteApps();
            }
        }).start();

    }

    private void onCleanMemory(Context context, List<BootItem> list) {
        for (BootItem bootItem : list) {
            String packageName = bootItem.getPackagename();
            if (!bootItem.getTrue()) {
                mWhiteListManager.addUserWhiteApp(packageName);
                mUserWhitelistedApps.add(packageName);
            } else {
                if (mUserWhitelistedApps.contains(packageName)) {
                    mWhiteListManager.removeUserWhiteApp(packageName);
                    mUserWhitelistedApps.remove(packageName);
                }
            }
            for (ItemInfo info : mRunningAppsList) {
                if (packageName.equals(info.getPackageName())) {
                    info.setGreenWhiteListItemState(!bootItem.getTrue());
                    break;
                }
            }
        }
        // use new weapon here
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(context);
        new Thread() {
            @Override
            public void run() {
                memoryManager.memoryClean(MemoryManager.CLEAN_TYPE_ROCKET, HostBootSpeedMainActicity.this);
            }
        }.start();
    }

    @Override
    public void onSelectListdo(List<BootItem> list) {
        onCleanMemory(this, list);
    }

    private void startUpdateMemory() {
        mOneCleanUtil.startUpdateRam();
    }

    private void stopUpdateMemory() {
        mOneCleanUtil.stopUpdateRam();
    }

    @Override
    public void refreshData(List<BootItem> list) {
        viewAvtion.onRefresh(list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reData();
    }


    private synchronized void sendMessageToHandler(int what) {
        if (mUpdateMemoryHandler != null) {
            mUpdateMemoryHandler.sendEmptyMessage(what);
        }
    }


    private void updateMemoryScoreTxt(int txt) {
        SharedPreferences setting_info = this.getSharedPreferences("BootSpeedMain_tem_memorysize", MODE_PRIVATE);
        SharedPreferences.Editor edit = setting_info.edit();
        edit.putInt("MemoryScore", txt);
        edit.commit();
        reSponseScore(txt);
    }

    @Deprecated
    private void updateMemoryUsedTxt(String txt) {

    }

    @Deprecated
    private void updateMemoryReleaseTxt(Context context, String txt) {

    }

    private String[] queryMemoryInfo() {
        String[] info = new String[3];
        double usedRamRatio = mOneCleanUtil.getRatioUsedMem();
        long totalRam = RamAndMemoryHelper.getTotalMem();
        totalRam = translateCapacity(totalRam);

        mUsedMemoryRatio = (int) (usedRamRatio * PERCENT_CONSTANT);
        info[0] = String.valueOf(mUsedMemoryRatio);
        info[1] = Formatter.formatFileSize(mContext, (long) (usedRamRatio * totalRam)).replace(" ", "");
        info[2] = Formatter.formatFileSize(mContext, totalRam).replace(" ", "");
        return info;
    }

    private List<ItemInfo> getRunningProcess() {
        final MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(mContext);
        List<ProcessMemoryEntity> runnings = memoryManager.getRunningProcessMemoryEntitysIncludeWhitelist(MemoryManager.CLEAN_TYPE_ROCKET);
        List<ItemInfo> notInUserWhiteListRunningApps = new ArrayList<>();
        List<ItemInfo> all = new ArrayList<>();
        for (ProcessMemoryEntity entity : runnings) {
            ApplicationInfo appInfo = HelperUtils.getApplicationInfo(mContext, entity.mPackageName);
            //chenyee zhaocaili 20181025 add for BoostSpeed list just display the apps have icon in launcher begin
            Intent intent = getPackageManager().getLaunchIntentForPackage(entity.mPackageName);
            if (appInfo != null && intent != null) {
                //chenyee zhaocaili 20181025 add for BoostSpeed list just display the apps have icon in launcher end
                ItemInfo info = new ItemInfo();
                boolean isWhiteApp = mUserWhitelistedApps.contains(entity.mPackageName);
                info.setGreenWhiteListItemState(isWhiteApp);
                info.setPackageName(entity.mPackageName);
                info.setTitle(HelperUtils.loadLabel(mContext, appInfo));
                info.setIcon(HelperUtils.loadIcon(mContext, appInfo));
                if (isWhiteApp) {
                    all.add(info);
                } else {
                    notInUserWhiteListRunningApps.add(info);
                }
            }
        }
        NameSorting.sort(all);
        NameSorting.sort(notInUserWhiteListRunningApps);
        all.addAll(notInUserWhiteListRunningApps);
        SystemCheckItem.setRunningProcessList(all);
        return all;
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // Chenyee xionghg 20171220 add for SW17W16A-2243 begin
            if (mContext.isDestroyed()) {
                Log.w(TAG, "handleMessage after activity is destroyed, msg=" + msg.what
                        + ", obj=" + msg.obj);
                return;
            }
            int flag = -1;
            String cleanResult = null;
            switch (msg.what) {
                case UPDATE_RAM_UI:
                    String[] memoryInfo = (String[]) msg.obj;
                    updateMemoryScoreTxt
                            (Integer.valueOf(memoryInfo[0]));
                    return;
                case GET_RUNNING_PROCESS:
                    updateRunningListView(false, (List<ItemInfo>) msg.obj);
                    return;
            }
        }

    };


    private void updateRunningListView(boolean fromOnCreate, List<ItemInfo> runnings) {
        mRunningAppsList = runnings;
        if (list == null) {
            return;
        }
        Iterator<BootItem> i = list.iterator();
        while (i.hasNext()) {
            BootItem item = i.next();
            boolean r = false;
            for (ItemInfo itemInfo : mRunningAppsList) {
                if (item.getPackagename().equals(itemInfo.getPackageName())) {
                    r = true;
                    break;
                }
            }
            if (!r) {
                i.remove();
            }
        }
        for (ItemInfo itemInfo : mRunningAppsList) {
            boolean r = false;
            for (BootItem itemInfo2 : list) {
                if (itemInfo2.getPackagename().equals(itemInfo.getPackageName())) {
                    r = true;
                    break;
                }
            }
            if (!r) {
                BootItem bootItem = new BootItem(itemInfo.getPackageName(), itemInfo.getIcon(), itemInfo.getTitle(), true);
                list.add(bootItem);
            }
        }
        refreshData(list);
    }

    @Override
    public void onUpdateRam() {
        sendMessageToHandler(STATE_CHECK_MEMORY);
    }

    @Override
    public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {

    }

    public void reData() {
        startUpdateMemory();
        sendMessageToHandler(GET_RUNNING_PROCESS);
    }

    @Override
    public void onMemoryCleanFinished(int totalProcesses, final long totalPss) {

        if (isDestroyed()) {
            Log.e(TAG, "onMemoryCleanFinished, activity is destroyed, do nothing");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                reData();
            }
        });
    }

}