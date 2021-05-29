/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: mengdw
 *
 * Date: 2016-12-06 for CR01775579
 */
package com.cydroid.softmanager.trafficassistant.lockscreenused;

import cyee.widget.CyeeButton;
import cyee.widget.CyeeListView;
import cyee.widget.CyeeTextView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;

import com.cydroid.softmanager.BaseActivity;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.lockscreenused.adapter.LockScreenTrafficUsedAdapter;
import com.cydroid.softmanager.trafficassistant.lockscreenused.controler.LockScreenTrafficUsedController;
import com.cydroid.softmanager.trafficassistant.net.AppItem;
import com.cydroid.softmanager.trafficassistant.net.UidDetailProvider;
import com.cydroid.softmanager.trafficassistant.TrafficNetworkControlActivity;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

public class LockScreenTrafficDetailActivity extends BaseActivity  {
    private static final String TAG = "LockScreenTrafficDetailActivity";
    
    private CyeeTextView mLockScreenTrafficTextView;
    private CyeeListView mLockScreenTrafficAppListView;
    private CyeeButton mCyeeButton;
    
    private Context mContext;
    private LockScreenTrafficUsedAdapter mLockScreenTrafficUsedAdapter;
    private UidDetailProvider mUidDetailProvider;
    private final ArrayList<AppItem> mAppList = new ArrayList<AppItem>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TrafficTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_screen_traffic_detail_layout);
        mContext = this;
        Log.d(TAG,"LockScreenTrafficDetailActivity onCreate");
        initView();
    }
    
    private void initView() {
        mLockScreenTrafficTextView = (CyeeTextView)this.findViewById(R.id.lock_screen_traffic_total);
        mLockScreenTrafficAppListView = (CyeeListView)this.findViewById(R.id.lock_screen_traffic_list);
        mCyeeButton =  (CyeeButton)this.findViewById(R.id.btn_start_network_ctl);
        mCyeeButton.setOnClickListener(new CyeeButton.OnClickListener() {
            public void onClick(View v) {
                startNetworkControlActivity();
            }
        });
    }
    
    private void startNetworkControlActivity() {
        Log.d(TAG, "startNetworkControlActivity");
        Intent intent = new Intent(mContext, TrafficNetworkControlActivity.class);
        mContext.startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        // mengdw <2017-04-24> add for 123502 begin
        super.onResume();
        ArrayList<AppItem> apps = getAppsInfo();
        showData(apps);
        // mengdw <2017-04-24> add for 123502 end
    }
    
    private ArrayList<AppItem> getAppsInfo() {
        Intent intent = this.getIntent();
        if (null == intent) {
            Log.d(TAG, "getAppsInfo intent is null");
            return null;
        }
        Bundle bundle = intent.getExtras();
        if (null == bundle) {
            Log.d(TAG, "getAppsInfo bundle is null");
            return null;
        }
        return (ArrayList<AppItem>) bundle.get(Constant.KEY_LOCKSCREEN_TRAFFIC);
    }
    
    private void showData(ArrayList<AppItem> apps) {
        if (null == apps) {
            Log.d(TAG, "showData apps is null");
            return;
        }
        long totalTraffic = 0;
        for (int i = 0; i < apps.size(); i++) {
            AppItem app = apps.get(i);
            totalTraffic = totalTraffic + app.total;
            Log.d(TAG, "showData app uid=" + app.key + " total=" + app.total);
        }
        mUidDetailProvider = new UidDetailProvider(mContext);
        mLockScreenTrafficUsedAdapter = new LockScreenTrafficUsedAdapter(mUidDetailProvider, apps);
        mLockScreenTrafficAppListView.setAdapter(mLockScreenTrafficUsedAdapter);
        mLockScreenTrafficUsedAdapter.notifyDataSetChanged(apps);
        String trafficTitle = String.format(mContext.getString(R.string.lock_screen_traffic_title),
                Formatter.formatFileSize(mContext, totalTraffic));
        mLockScreenTrafficTextView.setText(trafficTitle);
        Log.d(TAG, "showData totalTraffic=" + totalTraffic + " apps size=" + apps.size());
    }
    
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        this.finish();
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
//mengdw <2016-03-22> add for CR01657626 end
