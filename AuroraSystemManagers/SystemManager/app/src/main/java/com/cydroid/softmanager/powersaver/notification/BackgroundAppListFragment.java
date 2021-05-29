/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 异常耗电行为应用列表界面组件
 *
 * Revised Date: 2017-02-05
 */
package com.cydroid.softmanager.powersaver.notification;


import android.content.Context;
import android.content.DialogInterface;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.memoryclean.IMemoryCleanNativeCallback;
import com.cydroid.softmanager.memoryclean.MemoryManager;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.oneclean.whitelist.WhiteListManager;
import com.cydroid.softmanager.powersaver.notification.BackgroundAppListAdapter.AppBatteryData;
import com.cydroid.softmanager.powersaver.notification.BackgroundAppListAdapter.ViewCache;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeTextView;

//Gionee <yangxinruo> <2016-02-1> modify for CR01634814 begin
public class BackgroundAppListFragment extends Fragment
        implements OnItemLongClickListener, OnItemClickListener, LoaderManager.LoaderCallbacks<List<PowerConsumeAppData>> {
//Gionee <yangxinruo> <2016-02-1> modify for CR01634814 end
    public static final String TAG = "BackgroundAppListFragment";

    public static final int MAX_ITEMS_TO_LIST = 20;
    //    public static final double DISPLAY_POWER_THERSHOLD = 0.1d;

    private BackgroundAppListAdapter mAdapter;

    private Context mContext;

    private LinearLayout mLoadingLayout;
    private LinearLayout mEmptyText;
    private CyeeListView mListView;
    private CyeeTextView mLoadingText;
    private CyeeButton mBtnOneClean;

    // Gionee <yangxinruo> <2016-02-1> add for CR01634814 begin
    private ArrayList<String> mAlertPackages = new ArrayList<String>();
    private String mAlertType = "";
    // Gionee <yangxinruo> <2016-02-1> add for CR01634814 end

    private boolean mIsLoaderInited = false;

    // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 begin
    private void refreshList(List<PowerConsumeAppData> data) {
        Log.d(TAG, "refresh data start");
        ArrayList<AppBatteryData> itemListData = transToAppPowerData(data);
        if (!mAlertPackages.isEmpty()) {
            ArrayList<AppBatteryData> fakeData = getFakeAppPowerData(mAlertPackages, mAlertType);
            itemListData.addAll(fakeData);
        }
        Collections.sort(itemListData, mComparator);
        if (itemListData.size() > MAX_ITEMS_TO_LIST) {
            Log.d(TAG, "trunc data list to size " + MAX_ITEMS_TO_LIST);
            itemListData.subList(MAX_ITEMS_TO_LIST, itemListData.size()).clear();
        }
        mAdapter.setListItemData(itemListData);
        mAdapter.notifyDataSetChanged();
        if (itemListData.isEmpty()) {
            showEmptyLayout();
        } else {
            showListLayout();
        }
    }

    private ArrayList<AppBatteryData> getFakeAppPowerData(ArrayList<String> fakePackages, String alertType) {
        ArrayList<AppBatteryData> resList = new ArrayList<AppBatteryData>();
        ArrayList<String> alertTypeArray = new ArrayList<String>();
        alertTypeArray.add(alertType);
        WhiteListManager whiteListManager = WhiteListManager.getInstance();
        whiteListManager.init(mContext);
        PowerConsumeAppManager powerConsumeAppManager = PowerConsumeAppManager.getInstance(mContext);
        for (String packageName : fakePackages) {
            boolean isInIgnoredAppList = powerConsumeAppManager.isIgnoredApp(packageName);
            boolean isInUserWhiteAppList = whiteListManager.isInUserWhiteApps(packageName);
            ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(mContext.getContentResolver());
            boolean isPrivateApp = encryptionsApps.contains(packageName);
            boolean isChecked = true;
            if (isInIgnoredAppList || isInUserWhiteAppList) {
                isChecked = false;
            }
            Log.d(TAG, "make fake data for not running pkg:" + packageName);
            AppBatteryData fakeData = new AppBatteryData(null, packageName, 0, isInIgnoredAppList,
                    isInUserWhiteAppList, alertTypeArray, 0, isChecked, isPrivateApp);
            resList.add(fakeData);
        }
        return resList;
    }

    private ArrayList<AppBatteryData> transToAppPowerData(List<PowerConsumeAppData> data) {
        Log.d(TAG, "process raw DATA size=" + data.size());
        ArrayList<AppBatteryData> tmpList = new ArrayList<AppBatteryData>();
        // Gionee <yangxinruo> <2016-7-12> modify for CR01730410 begin
        if (data.isEmpty()) {
            return tmpList;
        }
        // Gionee <yangxinruo> <2016-7-12> modify for CR01730410 end
        double totalPower = 0;

        for (PowerConsumeAppData powerConsumeData : data) {
            totalPower += powerConsumeData.mPowerValue;
        }

        // Gionee <yangxinruo> <2016-7-12> modify for CR01730410 begin
        if (totalPower <= 0) {
            Log.d(TAG, "totalPower abnormal,A list is full of fake data,set total power =1");
            totalPower = 1;
        }
        // Gionee <yangxinruo> <2016-7-12> modify for CR01730410 end
        for (PowerConsumeAppData powerConsumeData : data) {
            // Gionee <yangxinruo> <2015-08-19> add for begin
            double percentOfTotal = getPercentOfItem(totalPower, powerConsumeData.mPowerValue);
            // Gionee <yangxinruo> <2015-08-19> add for end
            // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 begin
            boolean isChecked = true;
            // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 begin
            Log.d(TAG, "ischecked=" + isChecked + " init");
            // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 end

            // Gionee <yangxinruo> <2015-12-1> modify for CR01602138 begin
            // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 begin
            if (powerConsumeData.mIsInUserWhiteAppList) {
                isChecked = false;
            } else if (powerConsumeData.mIsInIgnoredAppList) {
                isChecked = false;
            }
            // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 end
            // Gionee <yangxinruo> <2015-12-1> modify for CR01602138 end
            Log.d(TAG, "add pkg=" + powerConsumeData.mPackageName + " percentOfTotal=" + percentOfTotal
                    + " isChecked=" + isChecked);
            AppBatteryData appData = new AppBatteryData(powerConsumeData, powerConsumeData.mPackageName,
                    powerConsumeData.mPowerValue, powerConsumeData.mIsInIgnoredAppList,
                    powerConsumeData.mIsInUserWhiteAppList, powerConsumeData.mAlertTypes, percentOfTotal,
                    isChecked, powerConsumeData.mIsPrivateApp);
            tmpList.add(appData);
            if (mAlertPackages.contains(appData.mPackageName)) {
                Log.d(TAG, "real data of " + appData.mPackageName + " exist ,remove fake data");
                mAlertPackages.remove(appData.mPackageName);
                if (!mAlertType.isEmpty() && !appData.mAlertTypes.contains(mAlertType)) {
                    appData.mAlertTypes.add(mAlertType);
                }
            }
            // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 end
        }
        return tmpList;
    }

    private double getPercentOfItem(double totalPower, double powerValue) {
        if (totalPower <= 0) {
            return 0;
        }
        return (powerValue / totalPower) * 100;
    }
    // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mAdapter = new BackgroundAppListAdapter(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            mAlertPackages = argsBundle.getStringArrayList("alert_packages");
            if (mAlertPackages == null) {
                mAlertPackages = new ArrayList<String>();
            }
            mAlertType = argsBundle.getString("alert_type");
            if (mAlertType == null) {
                mAlertType = "";
            }
        }
        Log.d(TAG, "onCreateView alert_packages=" + mAlertPackages + " alert_type=" + mAlertType);
        View view = inflater.inflate(R.layout.power_consume_background_app_fragment, container, false);
        mListView = (CyeeListView) view.findViewById(R.id.background_app_list);
        mListView.setItemsCanFocus(true);
        mListView.setAdapter(mAdapter);
        // Gionee <yangxinruo> <2016-02-1> add for CR01634814 begin
        mListView.setLongClickable(true);
        mListView.setOnItemLongClickListener(this);
        // Gionee <yangxinruo> <2016-02-1> add for CR01634814 end
        mListView.setOnItemClickListener(this);
        mLoadingLayout = (LinearLayout) view.findViewById(R.id.loader);
        mEmptyText = (LinearLayout) view.findViewById(R.id.text_empty);
        // Gionee <yangxinruo> <2016-1-9> add for CR01620682 begin
        mLoadingText = (CyeeTextView) mLoadingLayout.findViewById(R.id.txt_noti);
        mBtnOneClean = (CyeeButton) view.findViewById(R.id.btn_clean);
        mBtnOneClean.setVisibility(View.GONE);
        // Gionee <yangxinruo> <2016-1-9> add for CR01620682 end
        mBtnOneClean.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                performClean();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLoader();
    }

    private void performClean() {
        List<ProcessMemoryEntity> toBeCleanedPackageList = new ArrayList<ProcessMemoryEntity>();
        List<AppBatteryData> selectedItems = mAdapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }
        for (AppBatteryData selectedItem : selectedItems) {
            if (mAlertPackages.contains(selectedItem.mPackageName)) {
                Log.d(TAG, "remove package:" + selectedItem.mPackageName + " from alert packages");
                mAlertPackages.remove(selectedItem.mPackageName);
            }
            PowerConsumeAppData SelectedPowerConsumeAppData = selectedItem.getPowerConsumeAppData();
            if (SelectedPowerConsumeAppData != null) {
                toBeCleanedPackageList.add(SelectedPowerConsumeAppData);
            }
        }

        MemoryManager memoryManager = MemoryManager.getInstance();
        memoryManager.init(mContext);
        memoryManager.cleanProcessMemoryEntitys(toBeCleanedPackageList, new IMemoryCleanNativeCallback() {
            @Override
            public void onMemoryCleanReady(List<ProcessMemoryEntity> processMemoryEntitys) {
            }

            @Override
            public void onMemoryCleanFinished(int totalProcesses, long totalPss) {
                onCleanFinished();
            }
        });
        showCleaningLayout();
    }

    private void onCleanFinished() {
        Loader<Object> loader = getLoaderManager().getLoader(0);
        if (loader != null) {
            loader.forceLoad();
        }
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

    private void showCleaningLayout() {
        mBtnOneClean.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.GONE);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mLoadingText.setText(R.string.oneclean_action_running);
    }

    private void showLoadingLayout() {
        mBtnOneClean.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.GONE);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mLoadingText.setText(R.string.text_load);
    }

    private void showListLayout() {
        mLoadingLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.GONE);
        mBtnOneClean.setVisibility(View.VISIBLE);
        mBtnOneClean.setEnabled(!mAdapter.getSelectedItems().isEmpty());
    }

    private void showEmptyLayout() {
        mLoadingLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.VISIBLE);
        mBtnOneClean.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private final Comparator<AppBatteryData> mComparator = new Comparator<AppBatteryData>() {
        @Override
        public int compare(AppBatteryData paramT1, AppBatteryData paramT2) {
            BigDecimal d1 = new BigDecimal(Double.toString(paramT1.mPowerValue));
            BigDecimal d2 = new BigDecimal(Double.toString(paramT2.mPowerValue));
            double d3 = d1.subtract(d2).doubleValue();
            if (d3 > 0.0000d) {
                return -1;
            } else if (d3 < 0.0000d) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    // Gionee <yangxinruo> <2016-02-1> add for CR01634814 begin
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final AppBatteryData item = mAdapter.getItem(position);
        Log.d(TAG, "item long click isIgnored=" + item.mIsInIgnoredAppList);
        Resources resContext = mContext.getResources();
        CharSequence[] dialogMsg;
        final boolean setIgnore = !item.mIsInIgnoredAppList;
        if (!setIgnore) {
            dialogMsg = new CharSequence[]{
                    resContext.getString(R.string.background_running_remove_ignore_message)};
        } else {
            dialogMsg = new CharSequence[]{
                    resContext.getString(R.string.background_running_set_ignore_message)};
        }
        CyeeAlertDialog alertDialog = new CyeeAlertDialog.Builder(mContext)
                .setTitle(resContext.getString(R.string.background_running_remove_ignore_title))
                .setItems(dialogMsg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ignorePackage(item, setIgnore);
                        mAdapter.notifyDataSetChanged();
                    }
                }).create();
        alertDialog.show();
        return true;
    }
    // Gionee <yangxinruo> <2016-02-1> add for CR01634814 end

    // Gionee <yangxinruo> <2016-02-1> add for CR01634814 begin
    private void ignorePackage(AppBatteryData item, boolean ignore) {
        Log.d(TAG, (ignore ? "ignore" : "notify") + " this app " + item.mPackageName);
        PowerConsumeAppManager powerConsumeAppManager = PowerConsumeAppManager.getInstance(mContext);
        if (ignore) {
            item.mIsInIgnoredAppList = true;
            powerConsumeAppManager.addUserIgnoredApp(item.mPackageName);
        } else {
            item.mIsInIgnoredAppList = false;
            powerConsumeAppManager.removeUserIgnoredApp(item.mPackageName);
        }
    }
    // Gionee <yangxinruo> <2016-02-1> add for CR01634814 end

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppBatteryData item = mAdapter.getItem(position);
        ViewCache viewCache = (ViewCache) view.getTag();
        viewCache.mCheckbox.toggle();
        boolean isChecked = viewCache.mCheckbox.isChecked();
        item.mIsChecked = isChecked;
        Log.d(TAG, "Checkbox click " + item.mPackageName + " " + item.mIsChecked);
        mBtnOneClean.setEnabled(!mAdapter.getSelectedItems().isEmpty());
    }

    @Override
    public Loader<List<PowerConsumeAppData>> onCreateLoader(int id, Bundle args) {
        BackgroundPowerConsumeLoader loader = new BackgroundPowerConsumeLoader(mContext);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<PowerConsumeAppData>> loader, List<PowerConsumeAppData> data) {
        Log.d(TAG, "loader finish ");
        mIsLoaderInited = true;
        refreshList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<PowerConsumeAppData>> arg0) {
    }
}
