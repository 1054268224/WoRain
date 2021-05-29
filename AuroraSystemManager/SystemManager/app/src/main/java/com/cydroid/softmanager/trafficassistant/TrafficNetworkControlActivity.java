//Gionee <jianghuan> <2013-11-29> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.Consts;
import com.cydroid.softmanager.trafficassistant.actionBarTab.ActionBarTabs;
import com.cydroid.softmanager.trafficassistant.actionBarTab.TabInfos;
import com.cydroid.softmanager.trafficassistant.adapter.NetworkControlAdapter;
import com.cydroid.softmanager.trafficassistant.controler.TrafficNetworkController;
import com.cydroid.softmanager.trafficassistant.interfaces.NetworkControlAppChangeCallBack;
import com.cydroid.softmanager.trafficassistant.loader.TrafficAppIconLoader;
import com.cydroid.softmanager.trafficassistant.loader.TrafficLoader;
import com.cydroid.softmanager.trafficassistant.model.TrafficNetworkControlAppInfo;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.trafficassistant.utils.TrafficassistantUtil;
import com.cydroid.softmanager.utils.Log;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar.Tab;
import cyee.app.CyeeProgressDialog;
import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeListView;

//Chenyee <bianrong> <2018-1-25> modify for SW17W16KR-83 begin
//Chenyee <bianrong> <2018-1-25> modify for SW17W16KR-83 end

public class TrafficNetworkControlActivity extends ActionBarTabs implements OnClickListener,
        NetworkControlAppChangeCallBack {
    private static final String TAG = "TrafficNetworkControlActivity";

    private static final int MESSAGE_UPDATE_UI = 0;

    private final ArrayList<CyeeListView> mAppListView = new ArrayList<CyeeListView>();
    private final ArrayList<TextView> mTxtEmpty = new ArrayList<TextView>();
    private final ArrayList<TextView> mTxtContent = new ArrayList<TextView>();
    private final ArrayList<TextView> mTxtAction = new ArrayList<TextView>();
    private final ArrayList<NetworkControlAdapter> mListAdapter = new ArrayList<NetworkControlAdapter>();
    private final ArrayList<LinearLayout> mLayoutListView = new ArrayList<LinearLayout>();
    private final ArrayList<RelativeLayout> mLayoutBtn = new ArrayList<RelativeLayout>();
    private final ArrayList<View> mSeparatorLine = new ArrayList<View>();
    private final ArrayList<SearchBox> mSearchBox = new ArrayList<SearchBox>();

    private Context mContext;
    private int mSimIndex;
    private int mNetType = 0;
    private TrafficNetworkController mTrafficNetworkController;
    private CyeeProgressDialog mCyeeProgressDialog = null;
    private final List<TrafficNetworkControlAppInfo> mWifiEnableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
    private final List<TrafficNetworkControlAppInfo> mWifiDisableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
    private final List<TrafficNetworkControlAppInfo> mMobileEnableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
    private final List<TrafficNetworkControlAppInfo> mMobileDisableAppList = new ArrayList<TrafficNetworkControlAppInfo>();
    private int mSelectedItemOffset;
    private TrafficAppIconLoader mTrafficAppIconLoader;
    private final List<TrafficNetworkControlAppInfo> mAllWifiAppList = new ArrayList<TrafficNetworkControlAppInfo>();
    private final List<TrafficNetworkControlAppInfo> mAllMobileAppList = new ArrayList<TrafficNetworkControlAppInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        // Gionee: mengdw <2016-07-25> add for CR01639347 begin
        mTrafficNetworkController = TrafficNetworkController.getInstance(mContext);
        initActionTabInfo(this);
        super.onCreate(savedInstanceState);
        // Gionee: mengdw <2016-07-25> add for CR01639347 end
        Log.d(TAG, "onCreate ChameleonColorManager");
        getLoaderManager().initLoader(TrafficLoader.ID_LOADER_TRAFFIC_NETWORK_CONTROL_LIST,
                null, new AppInitCallbacks());
        mTrafficNetworkController.addAppChangeCallBack(String.valueOf(this.hashCode()), this);
        showProgressDialog(mContext.getString(R.string.text_load));
        // Gionee: mengdw <2017-05-08> add for 129236 begin
        ChameleonColorManager.getInstance().onCreate(this);
        // Gionee: mengdw <2017-05-08> add for 129236 end
        chameleonColorProcess();
        mSelectedItemOffset = getResources().getDimensionPixelSize(R.dimen.list_selected_item_offset);
    }

    private void chameleonColorProcess() {
        boolean isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        if (isNeedChangeColor) {
            for (int i = 0; i < mSearchBox.size(); i++){
                mSearchBox.get(i).changeSearchBoxBgColor();
            }
        }
    }

    private void showProgressDialog(final String text) {
        Log.d(TAG, "showProgressDialog");
        if (null == mCyeeProgressDialog) {
            mCyeeProgressDialog = new CyeeProgressDialog(mContext);
            mCyeeProgressDialog.setOwnerActivity(TrafficNetworkControlActivity.this);
        }
        mCyeeProgressDialog.setCancelable(false);
        mCyeeProgressDialog.setMessage(text);
        mCyeeProgressDialog.show();
        mCyeeProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "call onDismiss");
                mCyeeProgressDialog = null;
            }
        });
    }

    private void initActionTabInfo(Context context) {
        //Chenyee <bianrong> <2018-1-25> modify for SW17W16KR-83 begin
        String[] titlesN = null;
        int[] layoutIdsN = null;
        if(Consts.gnKRFlag){
             titlesN = new String[]{context.getString(R.string.traffic_netcontrol_tab_bar_prohibit_text)};
             layoutIdsN = new int[]{R.layout.trafficassistant_network_control};
        }else {
             titlesN = new String[]{context.getString(R.string.traffic_netcontrol_tab_bar_prohibit_text),
                    context.getString(R.string.traffic_netcontrol_tab_bar_allow_text_new)};
             layoutIdsN = new int[]{R.layout.trafficassistant_network_control,
                    R.layout.trafficassistant_network_control};
        }
        //Chenyee <bianrong> <2018-1-25> modify for SW17W16KR-83 end
        mTabInfos = new TabInfos();
        mTabInfos.setTabNums(titlesN.length);
        mTabInfos.setTabTexts(titlesN);
        mTabInfos.setLayoutIds(layoutIdsN);
    }

    @Override
    public void initUI(int children) {
        View view = mSections.get(children);
        mLayoutListView.add(children, (LinearLayout) view.findViewById(R.id.listview));
        mLayoutBtn.add(children, (RelativeLayout) view.findViewById(R.id.title_layout));
        mSearchBox.add(children, view.findViewById(R.id.searchbox));

        mAppListView.add(children, (CyeeListView) view.findViewById(R.id.app_list));
        mListAdapter.add(children, new NetworkControlAdapter(this, mAppListView.get(children)));
        mListAdapter.get(children).setAppNumChangeCallBack(this);
        mAppListView.get(children).setAdapter(mListAdapter.get(children));

        mTxtEmpty.add(children, (TextView) view.findViewById(R.id.text_empty));
        mTxtContent.add(children, (TextView) view.findViewById(R.id.content_txt));
        mTxtAction.add(children, (TextView) view.findViewById(R.id.action_txt));

        mLayoutListView.get(children).setVisibility(View.GONE);
        mLayoutBtn.get(children).setVisibility(View.GONE);
        mSearchBox.get(children).setVisibility(View.GONE);
        mSeparatorLine.add(children, (View) view.findViewById(R.id.separator_line));
        //Gionee <jiangsj> <20170419> add for 113672 begin
        if (isLayoutRtl()) {
            view.setRotationY(180);
        }
        //Gionee <jiangsj> <20170419> add for 113672 begin
        mSearchBox.get(children).setSearchListener(new SearchBox.SearchListener(){

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
            }

            @Override
            public void onSearchTermChanged(String term) {
                //React to the mSearchBox term changing
                //Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                //Toast.makeText(MainActivity.this, searchTerm +" Searched", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to a result being clicked
                final int position = result.position;
                if (position != -1 && position < (mNetType == 0? mAllMobileAppList.size() : mAllWifiAppList.size())){
                    mAppListView.get(mNetType).post(new Runnable() {
                        @Override
                        public void run() {
                            mAppListView.get(mNetType).smoothScrollToPositionFromTop(position, mSelectedItemOffset, 500);
                        }
                    });
                }
            }

            @Override
            public void onSearchCleared() {
                //Called when the clear button is clicked
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onClick(View arg0) {
        // Gionee: mengdw <2016-12-22> modify for 52843 begin
        switch (arg0.getId()) {
            case R.id.action_txt:
                allNetworkControlOnclick(mNetType);
                break;
            default:
                break;
        }
        // Gionee: mengdw <2016-12-22> modify for 52843 end
    }

    private void allNetworkControlOnclick(final int netType) {
        // Gionee: mengdw <2015-08-05> modify for CR01532976 begin
        Log.d(TAG, "allNetworkControlOnclick netType=" + netType);
        showProgressDialog(mContext.getString(R.string.text_load));
        if (Constant.MOBILE == netType) {
            startMobileAllNetworkControl();
        } else if (Constant.WIFI == netType) {
            startWifiAllNetworkControl();
        }
        // Gionee: mengdw <2015-08-05> modify for CR01532976 end
    }

    private void startWifiAllNetworkControl() {
        Thread wifiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mWifiDisableAppList.isEmpty()) {
                    mTrafficNetworkController.disableAllWifiNetwork();
                } else {
                    mTrafficNetworkController.enableAllWifiNetwork();
                }
                sendUpdateUiMessage(Constant.WIFI);
            }
        });
        wifiThread.start();
    }

    private void sendUpdateUiMessage(int netType) {
        Message msg = new Message();
        msg.what = MESSAGE_UPDATE_UI;
        msg.arg1 = netType;
        if (null != mHandler) {
            mHandler.sendMessage(msg);
        }
    }

    private void startMobileAllNetworkControl() {
        Thread mobileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mMobileDisableAppList.isEmpty()) {
                    mTrafficNetworkController.disableAllMobileNetwork();
                } else {
                    mTrafficNetworkController.enableAllMobileNetwork();
                }
                sendUpdateUiMessage(Constant.MOBILE);
            }
        });
        mobileThread.start();
    }

    @Override
    public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        super.onTabSelected(arg0, arg1);
        mNetType = arg0.getPosition();
        // Gionee: mengdw <2015-08-05>add log begin
        Log.d(TAG, "onTabSelected mNetType=" + mNetType);
        // Gionee: mengdw <2015-08-05>add log end
        refreshAllControlTxt(mNetType);
        updateSearchBoxStatus(mNetType);
    }

    private void refreshAllControlTxt(int netType) {
        try {
            if (null != mTxtAction && mTxtAction.size() > netType) {
                if (Constant.MOBILE == netType) {
                    if (mMobileDisableAppList.isEmpty()) {
                        mTxtAction.get(netType).setText(mContext.getString(R.string.action_all_prohibit));
                    } else {
                        mTxtAction.get(netType).setText(mContext.getString(R.string.action_all_allow));
                    }
                } else {
                    if (mWifiDisableAppList.isEmpty()) {
                        mTxtAction.get(netType).setText(mContext.getString(R.string.action_all_prohibit));
                    } else {
                        mTxtAction.get(netType).setText(mContext.getString(R.string.action_all_allow));
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "refreshAllControlTxt e=" + e.toString());
        }
    }

    private void updateSearchBoxStatus(int netType){
        mSearchBox.get(netType == 0? 1 : 0).closeSearch();
    }

    @Override
    public void controlAppNumChange(int netType) {
        sendUpdateUiMessage(netType);
    }

    @Override
    public void controlAppStateChange(int netType) {
        sendUpdateUiMessage(netType);
    }

    private void updateUI(final int netType) {
        Log.d(TAG, "updateUI netType=" + netType);
        updateControlAppList(netType);
        refreshView(netType);
        dismissProcessDialog();
    }

    private void updateControlAppList(int netType) {
        if (Constant.MOBILE == netType) {
            mMobileEnableAppList.clear();
            mMobileDisableAppList.clear();
            List<TrafficNetworkControlAppInfo> mobileEnableList = mTrafficNetworkController.getMobileEnableAppList();
            List<TrafficNetworkControlAppInfo> mobileDisableList = mTrafficNetworkController.getMobileDisableAppList();
            Log.d(TAG, "updateControlAppList mobileEnable size=" + mobileEnableList.size() +
                    " mobileDisable size=" + mobileDisableList.size());
            mMobileEnableAppList.addAll(mobileEnableList);
            mMobileDisableAppList.addAll(mobileDisableList);
        } else {
            mWifiEnableAppList.clear();
            mWifiDisableAppList.clear();
            List<TrafficNetworkControlAppInfo> wifiEnableList = mTrafficNetworkController.getWifiEnableAppList();
            List<TrafficNetworkControlAppInfo> wifiDisableList = mTrafficNetworkController.getWifiDisableAppList();
            Log.d(TAG, "updateControlAppList wifiEnableList size=" + wifiEnableList.size() +
                    " wifiDisableList size=" + wifiDisableList.size());
            mWifiEnableAppList.addAll(wifiEnableList);
            mWifiDisableAppList.addAll(wifiDisableList);
        }
    }

    private void refreshView(int netType) {
        updateNetworkControlViewVisibility(netType);
        refreshAppNumTxt(netType);
        refreshAllControlTxt(netType);
        // Gionee: mengdw <2017-04-06>add for 66469 begin
        try {
            if (null != mListAdapter && mListAdapter.size() > netType) {
                mListAdapter.get(netType).notifyDataSetChanged(getControlAppList(netType), netType);
            }
        } catch (Exception e) {
            Log.d(TAG, "mListAdapter Exception e=" + e.toString());
        }
        // Gionee: mengdw <2017-04-06>add for 66469 end
        updateSearchBox(netType);
    }

    private void updateSearchBox(int netType){
        List<TrafficNetworkControlAppInfo> appList = (netType == 0? mAllMobileAppList : mAllWifiAppList);
        setSearchResultList(appList, netType);
        mTrafficAppIconLoader = new TrafficAppIconLoader(this);
        TrafficNetworkControlAppInfo info = new TrafficNetworkControlAppInfo();
        info.setAppStatus(netType);
        appList.add(info);
        mTrafficAppIconLoader.loadAppIcon(appList, new TrafficAppIconLoader.ImageLoadCompleteCallback() {
            @Override
            public void loadComplete(List<TrafficNetworkControlAppInfo> data) {
                int type = data.get(data.size() - 1).getAppStatus();
                data.remove(data.size() - 1);
                mSearchBox.get(type).clearSearchable();
                setSearchResultList(data, type);
            }
        });
    }

    public void setSearchResultList(List<TrafficNetworkControlAppInfo> appList, int netType){
        for (int i = 0; i < appList.size(); i++){
            TrafficNetworkControlAppInfo info = appList.get(i);
            SearchResult option = new SearchResult(info.getAppName(), info.getIcon(), i);
            mSearchBox.get(netType).addSearchable(option);
        }
    }


    private void updateNetworkControlViewVisibility(int netType) {
        if (!isShowEmptyView(netType)) {
            setSearchBoxVisibility(netType, View.VISIBLE);
            setLayoutListViewVisibility(netType, View.VISIBLE);
            setLayoutBtnVisibility(netType, View.VISIBLE);
            setTxtEmptyVisibility(netType, View.GONE);
            setSeparatorLineVisibility(netType, View.VISIBLE);
        } else {
            setSearchBoxVisibility(netType, View.GONE);
            setLayoutListViewVisibility(netType, View.GONE);
            setLayoutBtnVisibility(netType, View.GONE);
            setTxtEmptyVisibility(netType, View.VISIBLE);
            setSeparatorLineVisibility(netType, View.GONE);
        }
    }

    private boolean isShowEmptyView(int netType) {
        int appNum = 0;
        if (Constant.MOBILE == netType) {
            appNum = mMobileDisableAppList.size() + mMobileEnableAppList.size();
        } else {
            appNum = mWifiDisableAppList.size() + mWifiEnableAppList.size();
        }
        return appNum <= 0;
    }

    private void setSearchBoxVisibility(int netType, int visibility) {
        try {
            if (null != mSearchBox && mSearchBox.size() > netType) {
                mSearchBox.get(netType).setVisibility(visibility);
            }
        } catch (Exception e) {
            Log.d(TAG, "setLayoutListViewVisibility e=" + e.toString());
        }
    }

    private void setLayoutListViewVisibility(int netType, int visibility) {
        try {
            if (null != mLayoutListView && mLayoutListView.size() > netType) {
                mLayoutListView.get(netType).setVisibility(visibility);
            }
        } catch (Exception e) {
            Log.d(TAG, "setLayoutListViewVisibility e=" + e.toString());
        }
    }

    private void setLayoutBtnVisibility(int netType, int visibility) {
        try {
            if (null != mLayoutBtn && mLayoutBtn.size() > netType) {
                mLayoutBtn.get(netType).setVisibility(visibility);
            }
        } catch (Exception e) {
            Log.d(TAG, "setLayoutBtnVisibility e=" + e.toString());
        }
    }

    private void setTxtEmptyVisibility(int netType, int visibility) {
        try {
            if (null != mTxtEmpty && mTxtEmpty.size() > netType) {
                mTxtEmpty.get(netType).setVisibility(visibility);
            }
        } catch (Exception e) {
            Log.d(TAG, "setTxtEmptyVisibility e=" + e.toString());
        }
    }

    private void setSeparatorLineVisibility(int netType, int visibility) {
        try {
            if (null != mSeparatorLine && mSeparatorLine.size() > netType) {
                mSeparatorLine.get(netType).setVisibility(visibility);
            }
        } catch (Exception e) {
            Log.d(TAG, "setSeparatorLineVisibility e=" + e.toString());
        }
    }

    private void refreshAppNumTxt(int netType) {
        try {
            if (null != mTxtContent && mTxtContent.size() > netType) {
                mTxtContent.get(netType).setText(
                        String.format(mContext.getString(R.string.traffic_title_text), getAppNum(netType)));
            }
        } catch (Exception e) {
            Log.d(TAG, "refreshAppNumTxt e=" + e.toString());
        }
    }

    private int getAppNum(int netType) {
        return Constant.MOBILE == netType ? mMobileEnableAppList.size() + mMobileDisableAppList.size()
                : mWifiEnableAppList.size() + mWifiDisableAppList.size();
    }

    private List<TrafficNetworkControlAppInfo> getControlAppList(int netType) {
        List<TrafficNetworkControlAppInfo> appList = new ArrayList<TrafficNetworkControlAppInfo>();
        if (Constant.MOBILE == netType) {
            mAllMobileAppList.clear();
            appList.addAll(mMobileEnableAppList);
            appList.addAll(mMobileDisableAppList);
            TrafficassistantUtil.appNameSort(appList);
            mAllMobileAppList.addAll(appList);
        } else {
            mAllWifiAppList.clear();
            appList.addAll(mWifiEnableAppList);
            appList.addAll(mWifiDisableAppList);
            TrafficassistantUtil.appNameSort(appList);
            mAllWifiAppList.addAll(appList);
        }

        return appList;
    }

    private void setAppStatusInList(List<TrafficNetworkControlAppInfo> list, int status) {
        if (null == list) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setAppStatus(status);
        }
    }

    private void dismissProcessDialog() {
        Log.d(TAG, "dismissProcessDialog");
        if (null == mCyeeProgressDialog) {
            return;
        }
        if (mCyeeProgressDialog.isShowing()) {
            mCyeeProgressDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppListView.clear();
        mTxtEmpty.clear();
        mTxtContent.clear();
        mTxtAction.clear();
        mListAdapter.get(Constant.MOBILE).setAppNumChangeCallBack(null);
        //Chenyee <bianrong> <2018-1-25> modify for SW17W16KR-83 begin
        if(!Consts.gnKRFlag) {
            mListAdapter.get(Constant.WIFI).setAppNumChangeCallBack(null);
        }
        //Chenyee <bianrong> <2018-1-25> modify for SW17W16KR-83 end
        mListAdapter.clear();
        mLayoutListView.clear();
        mLayoutBtn.clear();
        mSeparatorLine.clear();
        mTrafficNetworkController.removeAppChangeCallBack(String.valueOf(this.hashCode()));
        dismissProcessDialog();
        // Gionee: mengdw <2017-05-08> add for 129236 begin
        ChameleonColorManager.getInstance().onDestroy(this);
        // Gionee: mengdw <2017-05-08> add for 129236 end
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage msg=" + msg.what + " netType=" + msg.arg1);
            if (MESSAGE_UPDATE_UI == msg.what) {
                int netType = msg.arg1;
                updateUI(netType);
            }
        }
    };

    // Gionee: mengdw <2016-12-13> add for CR01776232 begin

    private class AppInitCallbacks implements LoaderCallbacks<Object> {
        @Override
        public Loader<Object> onCreateLoader(int id, Bundle args) {
            return new TrafficLoader(mContext);
        }

        @Override
        public void onLoadFinished(Loader<Object> arg0, Object arg1) {
            Log.d(TAG, "onLoadFinished");
            sendUpdateUiMessage(Constant.MOBILE);
            sendUpdateUiMessage(Constant.WIFI);
        }

        @Override
        public void onLoaderReset(Loader<Object> arg0) {
        }
    }
    // Gionee: mengdw <2016-12-13> add for CR01776232 end
}
