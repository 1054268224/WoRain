/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.softmanager;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.interfaces.LocalChangedCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.softmanager.adapter.FreezeAppAdapter;
import com.cydroid.softmanager.softmanager.adapter.FreezeAppAdapterWheatek;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppInfo;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppManager;
import com.cydroid.softmanager.softmanager.loader.SoftManagerIconLoader;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeExpandableListView;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FreezeAppActivity extends AppCompatActivity
        implements LoaderCallbacks<Object>, StateChangeCallback, LocalChangedCallback {
    private static final String TAG = "FreezeAppActivity";

    private LinearLayout mLoader;
    private RelativeLayout mEmpty;
    private CyeeExpandableListView mFreezeListView;
    private FreezeAppAdapter mFreezeAppAdapter;

    private final FreezeAppManager mFreezeAppManager = FreezeAppManager.getInstance();
    private final List<FreezeAppInfo> mFreezeNormalApps = new ArrayList<>();
    private final List<FreezeAppInfo> mFreezeCautiousApps = new ArrayList<>();
    private FreezeAppInfo data;
    private final Handler mHandler = new Handler();
    private SearchBox mSearchBox;
    private int mSelectedItemOffset;
    private SoftManagerIconLoader mSoftManagerIconLoader;
    private FreezeAppAdapterWheatek freezeAppAdapterWheatek;
    private RecyclerView recyclerView;
    private Context context;
    private Button mBtnFreezeApp;

    public void onCreate(Bundle savedInstanceState) {
        context = this;
        /*if (UiUtils.isSpecialStyleModel()) {
            setTheme(R.style.SystemManagerTheme);
        } else {
            setTheme(R.style.SystemManagerThemeCustom);
        }*/
        super.onCreate(savedInstanceState);
        // Gionee xionghg 2017-06-14 modify for 156425 begin
        if (Consts.gnSwFlag) {
            setContentView(R.layout.sw_softmanager_activity_app_freezable);
        } else {
            setContentView(R.layout.softmanager_activity_app_freezable);
        }
        // Gionee xionghg 2017-06-14 modify for 156425 end
        initViews();
        /*initCustomActionBar();*/
        getLoaderManager().initLoader(SoftManagerLoader.ID_LOADER_FREEZE, null, this);
        mFreezeAppManager.setFreezeAppsChangeCallBack(String.valueOf(this.hashCode()), this);
        mFreezeAppManager.setLocalChangeCallBack(String.valueOf(this.hashCode()), this);
        ChameleonColorManager.getInstance().onCreate(this);
        mSelectedItemOffset = getResources().getDimensionPixelSize(R.dimen.list_selected_item_offset);
        chameleonColorProcess();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.host_bar_bg_white)));
        getSupportActionBar().setElevation(0.0f);
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void chameleonColorProcess() {
        boolean isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        if (isNeedChangeColor) {
            mSearchBox.changeSearchBoxBgColor();
        }
    }

    private void initViews() {
        mLoader = (LinearLayout) findViewById(R.id.loader);
        mEmpty = (RelativeLayout) findViewById(R.id.freeze_app_empty_view);
        mSearchBox = findViewById(R.id.searchbox);
        // Add by HZH on 2019/6/17 for EJSL-1506 start
        RelativeLayout relativeLayout = mSearchBox.findViewById(R.id.card_view);
        layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
        // Add by HZH on 2019/6/17 for EJSL-1506 end

        /*mFreezeListView = (CyeeExpandableListView) findViewById(R.id.freeze_app_expand_listview);
        mFreezeListView.setIsAutoIndent(false);

        mFreezeListView.setVisibility(View.GONE);
        mFreezeListView.setOnItemClickListener(null);
        mFreezeListView.setAdapter(getAdapter());*/

        recyclerView = (RecyclerView) findViewById(R.id.freeze_app_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        freezeAppAdapterWheatek = new FreezeAppAdapterWheatek(context, this, mFreezeNormalApps, mFreezeCautiousApps);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setAdapter(freezeAppAdapterWheatek);
        mBtnFreezeApp = (Button) findViewById(R.id.btn_freeze_app);
        mBtnFreezeApp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (FreezeAppInfo data : mFreezeNormalApps) {
                    if (data.getCheckStaus()) {
                        mFreezeAppManager.freezeApp(data.getPackageName());
                    }
                }
                onStateChange();
            }

        });

        mLoader.setVisibility(View.VISIBLE);

        setTitle(R.string.freeze_app_title);
        mSearchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
                // Add by HZH on 2019/6/17 for EJSL-1506 start
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the mSearchBox term changing
                //Called after it has updated results
                // Add by HZH on 2019/6/17 for EJSL-1506 start
                int size = mSearchBox.getResults().size();
                layoutSearchBox(relativeLayout,
                        !TextUtils.isEmpty(term) && size > 0 ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

            @Override
            public void onSearch(String searchTerm) {
                //Toast.makeText(MainActivity.this, searchTerm +" Searched", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
                final int position = result.position;
             /*   if (position != -1 && position < mFreezeNormalApps.size() + mFreezeCautiousApps.size()){
                    if (position >= 0 && position < mFreezeNormalApps.size()){
                        mFreezeListView.expandGroup(0);
                    }else {
                        mFreezeListView.expandGroup(1);
                    }
                    mFreezeListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mFreezeListView.smoothScrollToPositionFromTop(position, mSelectedItemOffset, 500);
                        }
                    });
                }*/
            }

            @Override
            public void onSearchCleared() {
                //Called when the clear button is clicked
                // Add by HZH on 2019/6/17 for EJSL-1506 start
                layoutSearchBox(relativeLayout, RelativeLayout.CENTER_VERTICAL);
                // Add by HZH on 2019/6/17 for EJSL-1506 end
            }

        });
    }

    // Add by HZH on 2019/6/17 for  start
    private void layoutSearchBox(RelativeLayout relativeLayout, int verb) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(verb);
        relativeLayout.setLayoutParams(layoutParams);
    }
    // Add by HZH on 2019/6/17 for  end

    private FreezeAppAdapter getAdapter() {
        if (mFreezeAppAdapter == null) {
            mFreezeAppAdapter = new FreezeAppAdapter(this, this, mFreezeNormalApps, mFreezeCautiousApps);
        }
        return mFreezeAppAdapter;
    }

    private void initCustomActionBar() {
        LinearLayout actionBar = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.systemmanager_settings_actionbar, null);

        initCustomActionBarFirstClick(actionBar);
        initCustomActionBarSecondClick(actionBar);

        CyeeActionBar.LayoutParams lp = new CyeeActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        /*getCyeeActionBar().setCustomView(actionBar, lp);
        getCyeeActionBar().setDisplayShowCustomEnabled(true);
        getCyeeActionBar().setDisplayHomeAsUpEnabled(true);*/
    }

    private void initCustomActionBarFirstClick(ViewGroup actionBar) {
        LinearLayout first = (LinearLayout) actionBar.findViewById(R.id.first_click_field);
        first.setVisibility(View.GONE);
    }

    private void initCustomActionBarSecondClick(ViewGroup actionBar) {
        initCustomActionBarSecondClickAction(actionBar);
        initCustomActionBarSecondClickImg(actionBar);
    }

    private void initCustomActionBarSecondClickAction(ViewGroup actionBar) {
        LinearLayout second = (LinearLayout) actionBar.findViewById(R.id.second_click_field);
        second.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new CyeeAlertDialog.Builder(FreezeAppActivity.this)
                        .setTitle(R.string.title_about)
                        .setMessage(R.string.app_freeze_prompt_info)
                        .setPositiveButton(
                                R.string.app_freeze_prompt_btn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            }
        });
    }

    private void initCustomActionBarSecondClickImg(ViewGroup actionBar) {
        ImageView img = (ImageView) actionBar.findViewById(R.id.img_actionbar_custom);
        img.setImageResource(R.drawable.app_freeze_info);

        if (ChameleonColorManager.isNeedChangeColor()) {
            int color_T1 = ChameleonColorManager.getContentColorPrimaryOnAppbar_T1();
            if (img.getDrawable() != null) {
                img.getDrawable().setTint(color_T1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*mFreezeAppAdapter.setStateChangeCallback(null);*/
        mFreezeAppManager.unsetFreezeAppsChangeCallBack(String.valueOf(this.hashCode()));
        mFreezeAppManager.unsetLocalChangeCallBack(String.valueOf(this.hashCode()));
        mFreezeNormalApps.clear();
        mFreezeCautiousApps.clear();
        ChameleonColorManager.getInstance().onDestroy(this);
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return new SoftManagerLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        refreshList();
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
    }

    @Override
    public void onStateChange() {
        mHandler.post(new Runnable() {
            public void run() {
                refreshList();
            }
        });
    }

    private void refreshList() {
        List<FreezeAppInfo> freezeNormalApps = mFreezeAppManager.getFreezeNormalApps();
        List<FreezeAppInfo> freezeCautiousApps = mFreezeAppManager.getFreezeCautiousApps();
        List<FreezeAppInfo> allApps = new ArrayList<>();
        allApps.addAll(freezeNormalApps);
        allApps.addAll(freezeCautiousApps);
        if (freezeNormalApps.isEmpty() && freezeCautiousApps.isEmpty()) {
            showEmptyView();
            return;
        }
        // mSearchBox.setVisibility(View.VISIBLE);
        mLoader.setVisibility(View.GONE);
        mEmpty.setVisibility(View.GONE);
        /*mFreezeListView.setVisibility(View.VISIBLE);*/
        recyclerView.setVisibility(View.VISIBLE);
        mFreezeNormalApps.clear();
        mFreezeNormalApps.addAll(freezeNormalApps);
        mFreezeCautiousApps.clear();
        mFreezeCautiousApps.addAll(freezeCautiousApps);
/*        mFreezeListView.expandGroup(0);
        mFreezeListView.expandGroup(1);
        mFreezeAppAdapter.notifyDataSetChanged();*/
        /*setSearchResultList();*/
        freezeAppAdapterWheatek.notifyDataSetChanged();
        freezeNormalApps.addAll(freezeCautiousApps);
        mSoftManagerIconLoader = new SoftManagerIconLoader(this, freezeNormalApps);
        mSoftManagerIconLoader.loadAppIcon(new SoftManagerIconLoader.ImageLoadCompleteCallback() {
            @Override
            public void loadComplete() {
                List<FreezeAppInfo> allFreezeApp = (List<FreezeAppInfo>) mSoftManagerIconLoader.getListDatas();
                for (int i = 0; i < allFreezeApp.size(); i++) {
                    FreezeAppInfo info = allFreezeApp.get(i);
                    if (i < mFreezeNormalApps.size()) {
                        mFreezeNormalApps.set(i, info);
                    } else {
                        mFreezeCautiousApps.set(i - mFreezeNormalApps.size(), info);
                    }
                }
                if (recyclerView != null) {
                    recyclerView.post(() -> {
                        if (recyclerView != null)
                            freezeAppAdapterWheatek.notifyDataSetChanged();
                    });
                }
                 /*mSearchBox.clearSearchable();
                setSearchResultList();*/
            }
        });

    }

    public void setSearchResultList() {
        for (int i = 0; i < mFreezeNormalApps.size() + mFreezeCautiousApps.size(); i++) {
            ItemInfo info;
            if (i < mFreezeNormalApps.size()) {
                info = mFreezeNormalApps.get(i);
            } else {
                info = mFreezeCautiousApps.get(i - mFreezeNormalApps.size());
            }
            SearchResult option = new SearchResult(info.getTitle(), info.getIcon(), i);
            mSearchBox.addSearchable(option);
        }
    }

    private void showEmptyView() {
        mSearchBox.setVisibility(View.GONE);
        mLoader.setVisibility(View.GONE);
        mEmpty.setVisibility(View.VISIBLE);
        mFreezeListView.setVisibility(View.GONE);
    }

    @Override
    public void onLocalChange() {
        refreshList();
    }
}
