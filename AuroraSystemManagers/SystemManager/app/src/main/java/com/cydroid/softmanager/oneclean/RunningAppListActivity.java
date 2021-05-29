/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 正在运行的应用
 *
 * Revised Date: 2017-02-16
 */
package com.cydroid.softmanager.oneclean;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.monitor.service.CpuRamMonitorService;
import com.cydroid.softmanager.oneclean.adapter.RunningAppListAdapter;
import com.cydroid.softmanager.oneclean.adapter.RunningAppListAdapter.AppInfoData;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cyee.widget.CyeeButton;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeTextView;
import com.chenyee.featureoption.ServiceUtil;
public class RunningAppListActivity extends BaseActivity
        implements LoaderCallbacks<List<ProcessMemoryEntity>>, OnItemClickListener {

    private static final String TAG = "RunningAppListActivity";
    private ArrayList<String> mExcludePkgList = new ArrayList<String>();
    private Context mContext;

    private View mLoaderLayout;
    private View mListLayout;
    private View mCleaningProcessText;
    private int mStartFromNotificationFlag = -1;
    private CyeeTextView mHeaderTxt;
    private ActivityManager mActivityManager;
    private long mCleanSize;
    private MemoryManager mMemoryCleanManager;
    private RunningAppListAdapter mAdapter;
    private CyeeListView mListView;
    private CyeeButton mCleanBtn;
    private TextView mLoadText;
    private TextView mEmptyLayout;
    private boolean mIsLoaderInited = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (mStartFromNotificationFlag > 0) {
            removeNotification();
        }
        startLoader();
    }

    private void startLoader() {
        showLoadingLayout();
        if (!mIsLoaderInited) {
            Log.d(TAG, "----->startLoader init ");
            getLoaderManager().initLoader(0, null, this);
        } else {
            Log.d(TAG, "----->startLoader restart ");
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mStartFromNotificationFlag);
    }

    @Override
    protected void onPause() {
        notifyCpuAndMemoryMonitor();
        super.onPause();
    }

    private void notifyCpuAndMemoryMonitor() {
        Intent cpuMonitorIntent = new Intent(mContext, CpuRamMonitorService.class);
        cpuMonitorIntent.putExtra("event", "headupremoved");
        ServiceUtil.startForegroundService(mContext,cpuMonitorIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clean_running_app_layout);
        mContext = this;
        initUI();
        mAdapter = getAdapter();
        mListView.setAdapter(mAdapter);
        mMemoryCleanManager = MemoryManager.getInstance();
        mMemoryCleanManager.init(mContext);
        mExcludePkgList = getIntent().getStringArrayListExtra("packages");
        if (mExcludePkgList == null) {
            mExcludePkgList = new ArrayList<String>();
        }
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mStartFromNotificationFlag = getIntent().getIntExtra("noti", -1);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.clearData();
        }
        super.onDestroy();
    }

    private void performClean() {
        ArrayList<AppInfoData> selectedList = mAdapter.getSelectedItems();
        if (selectedList.isEmpty()) {
            return;
        }
        ArrayList<ProcessMemoryEntity> toBeCleanList = new ArrayList<ProcessMemoryEntity>();
        for (AppInfoData selectedData : selectedList) {
            toBeCleanList.add(selectedData.mProcessMemoryEntity);
        }
        showCleaningLayout();
        mMemoryCleanManager.cleanProcessMemoryEntitys(toBeCleanList, new IMemoryCleanNativeCallback() {

            @Override
            public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {
            }

            @Override
            public void onMemoryCleanFinished(int totalProcesses, long totalPss) {
                endCleanLoad(totalPss * 1024);
            }

        });
    }

    private void endCleanLoad(long cleanSize) {
        // 清理完成
        // Gionee <yangxinruo> <2016-8-9> modify for CR01741379 begin
        Loader<List<ProcessMemoryEntity>> loader = getLoaderManager().getLoader(0);
        if (loader != null) {
            loader.forceLoad();
        }
        // Gionee <yangxinruo> <2016-8-9> modify for CR01741379 end
        String memStr = SoftHelperUtils.getSizeStr(mContext, cleanSize);
        Toast.makeText(mContext, mContext.getResources().getString(R.string.running_apps_finish, memStr),
                Toast.LENGTH_LONG).show();
        finish();
    }

    private void initUI() {
        mLoaderLayout = (LinearLayout) findViewById(R.id.loader);
        mListLayout = (LinearLayout) findViewById(R.id.list_layout);
        mCleaningProcessText = (TextView) findViewById(R.id.txt_no_cleaning_process);
        mCleanBtn = (CyeeButton) findViewById(R.id.btn_clean);
        mHeaderTxt = (CyeeTextView) findViewById(R.id.header_text);
        mLoadText = (TextView) findViewById(R.id.load_text);
        mEmptyLayout = (TextView) findViewById(R.id.text_empty);
        mCleanBtn.setEnabled(false);
        mCleanBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performClean();
            }
        });
        mListView = (CyeeListView) findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public Loader<List<ProcessMemoryEntity>> onCreateLoader(int id, Bundle params) {
        return new RunningAppListLoader(mContext);
    }

    @Override
    public void onLoaderReset(Loader<List<ProcessMemoryEntity>> loader) {
        Log.d(TAG, "loader reset !");
    }

    @Override
    public void onLoadFinished(Loader<List<ProcessMemoryEntity>> loader, List<ProcessMemoryEntity> data) {
        Log.d(TAG, "load finish");
        mIsLoaderInited = true;
        refreshAppList(data);
    }

    private void refreshAppList(List<ProcessMemoryEntity> data) {
        refreshListView(data);
        mAdapter.notifyDataSetChanged();
    }

    private void refreshListView(List<ProcessMemoryEntity> newData) {
        Log.d(TAG, "refresh data " + newData.size());
        if (newData.isEmpty()) {
            showEmptyLayout();
            return;
        }
        mExcludePkgList.add(getTopSecondPackage());
        ArrayList<AppInfoData> adapterDataList = transToAdapterData(newData);
        mAdapter.setListItemData(adapterDataList);
        mAdapter.notifyDataSetChanged();
        mHeaderTxt.setText(
                mContext.getResources().getString(R.string.running_apps_header, adapterDataList.size()));
        showListLayout();
        mCleanBtn.setEnabled(!mAdapter.getSelectedItems().isEmpty());
    }

    private ArrayList<AppInfoData> transToAdapterData(List<ProcessMemoryEntity> newData) {
        ArrayList<AppInfoData> adapterDataList = new ArrayList<AppInfoData>();
        for (ProcessMemoryEntity data : newData) {
            if (mExcludePkgList.contains(data.mPackageName)) {
                continue;
            }
            boolean isCheck = true;
            if (data.mIsInUserWhiteAppList) {
                isCheck = false;
            }
            adapterDataList.add(new AppInfoData(data, isCheck));
        }
        //xionghg note: 都是lhs,相当于不排序,笔误?
        Collections.sort(adapterDataList, new Comparator<AppInfoData>() {
            @Override
            public int compare(AppInfoData lhs, AppInfoData rhs) {
                if (lhs.mProcessMemoryEntity.mProcess.importance > lhs.mProcessMemoryEntity.mProcess.importance) {
                    return -1;
                } else if (lhs.mProcessMemoryEntity.mProcess.importance < lhs.mProcessMemoryEntity.mProcess.importance) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return adapterDataList;
    }

    private String getTopSecondPackage() {
        String topPkg = "";
        List<RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(2);
        try {
            if (runningTaskInfos != null && runningTaskInfos.size() >= 2 && runningTaskInfos.get(1) != null
                    && runningTaskInfos.get(1).baseActivity != null) {
                topPkg = runningTaskInfos.get(1).baseActivity.getPackageName();
            }
        } catch (Exception e) {
            Log.d(TAG, "get top app error " + e);
            topPkg = "";
        }
        return topPkg;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfoData item = (AppInfoData) mAdapter.getItem(position);
        RunningAppListAdapter.ViewCache viewCache = (RunningAppListAdapter.ViewCache) view.getTag();
        viewCache.mCheckbox.toggle();
        item.mIsChecked = viewCache.mCheckbox.isChecked();
        Log.d(TAG, "Checkbox click " + item.mProcessMemoryEntity.mPackageName + " " + item.mIsChecked);
        mCleanBtn.setEnabled(!mAdapter.getSelectedItems().isEmpty());
    }

    protected RunningAppListAdapter getAdapter() {
        return new RunningAppListAdapter(mContext);
    }

    protected void showLoadingLayout() {
        mListLayout.setVisibility(View.GONE);
        mLoaderLayout.setVisibility(View.VISIBLE);
        mLoadText.setText(R.string.text_load);
        mEmptyLayout.setVisibility(View.GONE);
        mCleanBtn.setVisibility(View.GONE);
    }

    protected void showListLayout() {
        mLoaderLayout.setVisibility(View.GONE);
        mListLayout.setVisibility(View.VISIBLE);
        mEmptyLayout.setVisibility(View.GONE);
        mCleanBtn.setVisibility(View.VISIBLE);
    }

    protected void showEmptyLayout() {
        mListLayout.setVisibility(View.GONE);
        mLoaderLayout.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
        mCleanBtn.setVisibility(View.GONE);
    }

    protected void showCleaningLayout() {
        mListLayout.setVisibility(View.GONE);
        mLoaderLayout.setVisibility(View.VISIBLE);
        mLoadText.setText(R.string.oneclean_action_running);
        mEmptyLayout.setVisibility(View.GONE);
        mCleanBtn.setVisibility(View.GONE);
    }
}
