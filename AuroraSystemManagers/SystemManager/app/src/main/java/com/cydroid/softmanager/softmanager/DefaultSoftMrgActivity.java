package com.cydroid.softmanager.softmanager;

import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.softmanager.adapter.DefaultSoftAdapter;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftItemInfo;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftSettingsManager;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;
import com.cydroid.softmanager.utils.GNToast;

import java.util.ArrayList;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;

public class DefaultSoftMrgActivity extends BaseListActivity<DefaultSoftAdapter>
        implements OnItemClickListener, StateChangeCallback {
    private static final String TAG = "DefaultSoftMrgActivity";

    private LinearLayout mLoaderView;
    private DefaultSoftAdapter mAdapter;
    private final List<DefaultSoftItemInfo> mData = new ArrayList<>();
    private DefaultSoftSettingsManager mDefaultSoftSettingsManager;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.defaultsoft_manager_activity);

        mLoaderView = (LinearLayout) findViewById(R.id.loader);
        mLoaderView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mListView.setOnItemClickListener(this);

        mDefaultSoftSettingsManager = DefaultSoftSettingsManager.getInstance();
        mDefaultSoftSettingsManager.setAppsChangeCallBack(String.valueOf(this.hashCode()), this);
        ChameleonColorManager.getInstance().onCreate(this);
        getLoaderManager().initLoader(SoftManagerLoader.ID_LOADER_DEFAULTSOFT, null, this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.host_bar_bg_white)));
        getSupportActionBar().setElevation(0.0f);
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDefaultSoftSettingsManager.unsetAppsChangeCallBack(String.valueOf(this.hashCode()));
        mData.clear();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        DefaultSoftItemInfo info = mData.get(position);
        if (!info.hasDefaultSoftApp()) {
            GNToast.showToast(this, mRes.getString(R.string.text_no_installed));
            return;
        }

        Intent intent = new Intent(this, DefaultSoftDetailActivity.class);
        intent.putExtra(DefaultSoftDetailActivity.DEFAULT_SOFT_ITEM_INDEX, position);
        startActivity(intent);
    }

    @Override
    public void onLoadFinished(Loader<Object> arg0, Object arg1) {
        refreshList();
    }

    @Override
    public void onStateChange() {
        mHandler.post(new Runnable() {
            public void run() {
                refreshList();
            }
        });
    }

    @Override
    protected DefaultSoftAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new DefaultSoftAdapter(this, mData);
        }
        return mAdapter;
    }

    private void refreshList() {
        List<DefaultSoftItemInfo> defaultSoftItemInfos =
                mDefaultSoftSettingsManager.getDefaultSoftItems();
        mData.clear();
        mData.addAll(defaultSoftItemInfos);
        mLoaderView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
    }
}
