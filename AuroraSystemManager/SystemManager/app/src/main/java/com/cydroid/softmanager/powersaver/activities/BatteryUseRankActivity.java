package com.cydroid.softmanager.powersaver.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
//Gionee <jiangsj> <20170419> add for 113672 begin
import android.text.TextUtils;
//Gionee <jiangsj> <20170419> add for 113672 end

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.powersaver.fuelgauge.ExtendedBatterySipper;
import com.cydroid.softmanager.powersaver.fuelgauge.UsageSummaryFragment;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;
//Chenyee <bianrong> <2018-1-30> add for SW17W16KR-92 begin
import com.cydroid.softmanager.common.Consts;
//Chenyee <bianrong> <2018-1-30> add for SW17W16KR-92 end
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;

public class BatteryUseRankActivity extends CyeeActivity implements CyeeActionBar.TabListener {
    private final static String TAG = "BatteryUseRankActivity";

    private ViewPager mViewPager;
    private LinearLayout mLoadingLayout;
    private CyeeActionBar mActionBar;
    private MyFragmentAdapter mAdapter;

    private BatteryChangeReceiver mBatteryChangeReceiver;

    private BatteryStatsHelper mBatteryHelper;
    private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;
    private ArrayList<ExtendedBatterySipper> mHardwareList = new ArrayList<ExtendedBatterySipper>();
    private ArrayList<ExtendedBatterySipper> mSoftwareList = new ArrayList<ExtendedBatterySipper>();
    private Handler mHandler = new Handler();

    private boolean mIsNeedRefresh = true;
    private boolean mIsLoading = false;

    public final static int TYPE_SOFTWARE_LIST = 0;
    public final static int TYPE_HARDWARE_LIST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTabThemeCustom);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_usage_layout);
        initUi();
        mBatteryHelper = new BatteryStatsHelper(this, true);
        mBatteryHelper.create(savedInstanceState);
        if (mBatteryHelper.getStats() instanceof BatteryStatsImpl) {
            ((BatteryStatsImpl) mBatteryHelper.getStats()).setPowerProfileLocked(mBatteryHelper.getPowerProfile());
        }
        mBatteryChangeReceiver = new BatteryChangeReceiver();
        mBatteryChangeReceiver.setReceiver(this);
    }

    private void initUi() {
        getSupportActionBar().hide();
        mActionBar = getCyeeActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setIndicatorBackgroundColor(Color.WHITE);

        mActionBar.setNavigationMode(CyeeActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.power_usage_viewpager);
        mLoadingLayout = (LinearLayout) findViewById(R.id.loading_layout);

        mAdapter = new MyFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        if (mViewPager.getAdapter() != null) {
            Log.d(TAG, "remove old fragment size=" + mViewPager.getAdapter().getCount());
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            int count = mViewPager.getAdapter().getCount();
            Bundle bundle = new Bundle();
            String key = "index";
            for (int i = 0; i < count; i++) {
                bundle.putString(key, String.valueOf(i));
                try {
                    ft.remove(fm.getFragment(bundle, key));
                } catch (Exception e) {
                    Log.d(TAG, "remove fragment index=" + i + " failed!");
                }
            }
            ft.commit();
        }

        //Gionee <jiangsj> <20170419> add for 113672 begin
        if (mViewPager != null && TextUtils.getLayoutDirectionFromLocale(getResources().getConfiguration().locale) == 1) {
            mViewPager.setRotationY(180);
        }
        //Gionee <jiangsj> <20170419> add for 113672 end
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                mActionBar.onPageScrolled(arg0, arg1, arg2);
            }

            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }
        });

        // xionghg delete: seems no sense
        /*mViewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mActionBar.onScrollToEnd(v, event);
                return false;
            }

        });*/

        int[] tabTitle = new int[]{R.string.software, R.string.hardware};

        for (int i = 0; i < mAdapter.getCount(); i++) {
            mActionBar.addTab(mActionBar.newTab().setText(tabTitle[i]).setTabListener(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "BatteryUseRankActivity onResume");
        if (mIsNeedRefresh) {
            mIsNeedRefresh = false;
            mLoadingLayout.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.GONE);
            new Thread() {
                @Override
                public void run() {
                    loadExtendedBatterySipperList();
                }
            }.start();
        }
    }

    private void loadExtendedBatterySipperList() {
        if (mIsLoading) {
            Log.d(TAG, "loading now");
            return;
        }
        mIsLoading = true;
//        mBatteryHelper.clearStats();
        mBatteryHelper.refreshStats(mStatsType, -1);
        mHardwareList.clear();
        mSoftwareList.clear();
        Log.d(TAG, "load batterystats data ");
        double totalPower = 0;
        double softTotalPower = 0;
        double hardTotalPower = 0;
        ArrayList<String> encryptionsApps = HelperUtils.getEncryptionsApps(this.getContentResolver());
        for (BatterySipper sipper : mBatteryHelper.getUsageList()) {
            Log.d(TAG, "get data " + sipper.drainType + " " + sipper.packageWithHighestDrain);
            if (sipper.drainType == BatterySipper.DrainType.USER) {
                // do nothing with type USER;
                continue;
            } else if (sipper.drainType == BatterySipper.DrainType.APP) {
                if (sipper.packageWithHighestDrain == null || sipper.packageWithHighestDrain.isEmpty()
                        || sipper.packageWithHighestDrain.startsWith("*")) {
                    sipper.packageWithHighestDrain = getString(R.string.power_ranking_noname);
                }
                ExtendedBatterySipper exSipper = new ExtendedBatterySipper(this,
                        sipper.packageWithHighestDrain, 0, sipper);
                if (exSipper.getSortValue() < 0) {
                    Log.d(TAG, sipper.packageWithHighestDrain + " <0 skip");
                    continue;
                }
                if (!encryptionsApps.contains(exSipper.getDefaultPackageName())) {
                    mSoftwareList.add(exSipper);
                }
                softTotalPower += exSipper.getSortValue();
                totalPower += exSipper.getSortValue();
            } else {
                String name = "";
                int iconId = 0;
                if (sipper.drainType == BatterySipper.DrainType.BLUETOOTH) {
                    name = getString(R.string.power_bluetooth);
                    iconId = R.drawable.icon_use_bluetooth;
                } else if (sipper.drainType == BatterySipper.DrainType.PHONE) {
                    name = getString(R.string.power_phone);
                    iconId = R.drawable.ic_voice_calls;
                } else if (sipper.drainType == BatterySipper.DrainType.SCREEN) {
                    name = getString(R.string.power_screen);
                    iconId = R.drawable.ic_display_for_power;
                } else if (sipper.drainType == BatterySipper.DrainType.CELL) {
                    name = getString(R.string.power_cell);
                    iconId = R.drawable.ic_cell_standby;
                } else if (sipper.drainType == BatterySipper.DrainType.WIFI) {
                    name = getString(R.string.power_wifi_new);
                    iconId = R.drawable.icon_use_wlan;
                } else if (sipper.drainType == BatterySipper.DrainType.IDLE) {
                    name = getString(R.string.power_idle);
                    iconId = R.drawable.ic_phone_idle;
                } else {
                    continue;
                }
                ExtendedBatterySipper exSipper = new ExtendedBatterySipper(this, name, iconId, sipper);
                if (exSipper.getSortValue() < 0) {
                    Log.d(TAG, sipper.packageWithHighestDrain + " <0 skip");
                    continue;
                }
                //Chenyee <bianrong> <2018-1-30> add for SW17W16KR-92 begin
                if (Consts.gnKRFlag) {
                    if (exSipper.getName().equals(getString(R.string.power_wifi_new))) {
                        continue;
                    }
                }
                //Chenyee <bianrong> <2018-1-30> add for SW17W16KR-92 end
                mHardwareList.add(exSipper);
                hardTotalPower += exSipper.getSortValue();
                totalPower += exSipper.getSortValue();
            }
        }
        int sumPercent = 0;
        sumPercent += addPercentToPowerList(mHardwareList, totalPower);
        sumPercent += addPercentToPowerList(mSoftwareList, totalPower);
        Log.d(TAG, "get BatteryStats data mHardwareList size=" + mHardwareList.size() + " mSoftwareList size="
                + mSoftwareList.size());
        Log.d(TAG, "mSoftTotalPower=" + softTotalPower + " mHardTotalPower=" + hardTotalPower + " totalPower="
                + totalPower + " ActulTotalPower=" + mBatteryHelper.getTotalPower() + " detain="
                + mBatteryHelper.getStats().getDischargeAmount(mStatsType) + " total percent=" + sumPercent);
        if (sumPercent > 0) {
            int otherPercent = 1000 - sumPercent;
            if (otherPercent < 0) {
                overPercentProcess(otherPercent);
            } else if (otherPercent >= 1) {
                String name = getString(R.string.power_app_others);
                int iconId = android.R.drawable.sym_def_app_icon;
                ExtendedBatterySipper otherExtendedSipper = new ExtendedBatterySipper(this, name, iconId,
                        null);
                otherExtendedSipper.setPercent(otherPercent);
                Log.d(TAG, "add extra item percent=" + otherExtendedSipper.getPercent());
                mSoftwareList.add(otherExtendedSipper);
            }
        }
        mIsLoading = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                refreshBatteryStatsData();
            }
        });
    }

    private int addPercentToPowerList(List<ExtendedBatterySipper> list, double totalPower) {
        int sumPercent = 0;
        Iterator<ExtendedBatterySipper> it = list.iterator();
        while (it.hasNext()) {
            ExtendedBatterySipper exSipper = it.next();
            int percent = (int) getPercentOfTotal(exSipper.getSortValue(), totalPower);
            if (percent >= 1) {
                exSipper.setPercent(percent);
                if (exSipper.batterSipper != null) {
                    exSipper.batterSipper.percent = percent;
                }
                sumPercent += percent;
            } else {
                it.remove();
            }
        }
        return sumPercent;
    }

    private double getPercentOfTotal(double value, double total) {
        return value / total * 1000;
    }

    private void overPercentProcess(int otherPercent) {
        for (ExtendedBatterySipper item : mSoftwareList) {
            int modifiedPercent = item.getPercent() + otherPercent;
            if (modifiedPercent >= 1) {
                Log.d(TAG, "reset " + item.getName() + " percent=" + modifiedPercent + " from "
                        + item.getPercent());
                item.setPercent(modifiedPercent);
                if (item.batterSipper != null) {
                    item.batterSipper.percent = modifiedPercent;
                }
                break;
            }
        }
    }

    private void refreshBatteryStatsData() {
        mAdapter.notifyDataSetChanged();
        mLoadingLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "--->onDestroy");
        mBatteryChangeReceiver.unsetReceiver(this);
        mSoftwareList.clear();
        mHardwareList.clear();
        if (isChangingConfigurations()) {
            mBatteryHelper.storeState();
        }
        super.onDestroy();
    }

    private class MyFragmentAdapter extends FragmentPagerAdapter {

        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new UsageSummaryFragment();
            return fragment;
//            return UsageSummaryFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            UsageSummaryFragment fragment = (UsageSummaryFragment) super.instantiateItem(container, position);
            fragment.setDataType(position);
            return fragment;
        }

    }

    @Override
    public void onTabReselected(CyeeActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(CyeeActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(CyeeActionBar.Tab tab, FragmentTransaction ft) {
    }

    public ArrayList<ExtendedBatterySipper> getExtendedBatterySipperList(int type) {
        switch (type) {
            case TYPE_SOFTWARE_LIST:
                return mSoftwareList;

            case TYPE_HARDWARE_LIST:
                return mHardwareList;

            default:
                return new ArrayList<ExtendedBatterySipper>();
        }
    }

    public BatteryStatsHelper getBatteryHelper() {
        return mBatteryHelper;
    }

    public int getStatsType() {
        return mStatsType;
    }

    class BatteryChangeReceiver extends BroadcastReceiver {
        private boolean mIsReceiverSet = false;
        private boolean mIsFirstReceived = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getAction() + " received,need refresh data");
            if (mIsFirstReceived) {
                mIsFirstReceived = false;
                return;
            }
            mIsNeedRefresh = true;
        }

        public void setReceiver(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(this, filter);
            mIsReceiverSet = true;
        }

        public void unsetReceiver(Context context) {
            if (!mIsReceiverSet) {
                Log.d(TAG, "receiver not set yet");
            }
            try {
                context.unregisterReceiver(this);
            } catch (Exception e) {
                Log.d(TAG, "unSet BatteryChangeReceiver Exception " + e);
            } finally {
                mIsReceiverSet = false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}