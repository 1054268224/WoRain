package com.cydroid.softmanager.trafficassistant.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cyee.widget.CyeeButton;
import cyee.widget.CyeeSwitch;

import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.trafficassistant.controler.TrafficNetworkController;
import com.cydroid.softmanager.trafficassistant.interfaces.NetworkControlAppChangeCallBack;
import com.cydroid.softmanager.trafficassistant.model.TrafficNetworkControlAppInfo;
import com.cydroid.softmanager.trafficassistant.net.AppInfo;
import com.cydroid.softmanager.trafficassistant.net.AppItem;
import com.cydroid.softmanager.trafficassistant.utils.Constant;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkControlAdapter extends BaseAdapter {
    private static final String TAG = "NetworkControlAdapter";
    
    private final AsyncAppIconLoader mAsyncAppIconLoader;
    private final TrafficNetworkController mNetworkControler;
    private List<TrafficNetworkControlAppInfo> mAppList = 
            new ArrayList<TrafficNetworkControlAppInfo>();
    private NetworkControlAppChangeCallBack mNetworkControlAppChangeCallBack;
    private final LayoutInflater mInflater;
    private final Context mContext;
    private int mNetType = 0;

    public NetworkControlAdapter(Context context, ListView listView) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
        mNetworkControler = TrafficNetworkController.getInstance(mContext);
    }
    
    public void setAppNumChangeCallBack(NetworkControlAppChangeCallBack listener) {
        mNetworkControlAppChangeCallBack = listener;
    }

    public void notifyStatusChanged() {
        Log.d(TAG, "notifyStatusChanged");
        super.notifyDataSetChanged();
    }

    public void notifyDataSetChanged(List<TrafficNetworkControlAppInfo> appList, int netType) {
        mAppList = appList;
        mNetType = netType;
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.trafficassistant_app_list_item, parent, false);
            view = new ViewHolder();
            view.icon = (ImageView) convertView.findViewById(R.id.icon);
            view.title = (TextView) convertView.findViewById(R.id.title);
            view.aswitch = (CyeeSwitch) convertView.findViewById(R.id.switch_action);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }
        
        if (null != mAppList && !mAppList.isEmpty()) {
            final TrafficNetworkControlAppInfo appInfo = (TrafficNetworkControlAppInfo) mAppList.get(position);
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, appInfo.getAppPkgName());
            if (null != info) {
                setIconImage(view.icon, appInfo.getAppPkgName(), info);
            }
            view.title.setText(appInfo.getAppName());
            view.aswitch.setOnCheckedChangeListener(null);
            view.aswitch.setChecked(Constant.NETWORK_CONTROL_DISABLE_STATUS != appInfo.getAppStatus());
            bindView(position, view, convertView);
        }
        return convertView;
    }

    private void setIconImage(final ImageView icon, final String packageName, 
            final ApplicationInfo applicationInfo) {
        icon.setTag(packageName);
        Drawable cachedImage = mAsyncAppIconLoader.loadAppIconDrawable(mContext, packageName,
            new ImageCallback() {
                public void imageLoaded(Drawable imageDrawable, String pkgName) {
                    if (!pkgName.equals(icon.getTag())) {
                        return;
                    }
                    if (null != imageDrawable) {
                        icon.setImageDrawable(imageDrawable);
                    } else {
                        icon.setImageDrawable(HelperUtils.loadIcon(mContext,
                            applicationInfo));
                    }
                }
            });
        if (null != cachedImage) {
            icon.setImageDrawable(cachedImage);
        }
    }

    public void bindView(int position, ViewHolder view, View convertView) {
        final TrafficNetworkControlAppInfo appInfo = (TrafficNetworkControlAppInfo) mAppList.get(position);
        view.aswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                int uid = appInfo.getAppUid();
                Log.d(TAG, " onCheckedChanged uid=" + uid + " packagename=" + appInfo.getAppPkgName() + 
                        " status=" + appInfo.getAppStatus() + " mNetType=" + mNetType);
                if (Constant.NETWORK_CONTROL_DISABLE_STATUS == appInfo.getAppStatus()) {
                    if (Constant.MOBILE == mNetType) {
                        mNetworkControler.enableMobileNetwork(appInfo.getAppPkgName());
                    } else {
                        mNetworkControler.enableWifiNetwork(appInfo.getAppPkgName());
                    }
                } else {
                    if (Constant.MOBILE == mNetType) {
                        mNetworkControler.disableMobileNetwork(appInfo.getAppPkgName());
                    } else {
                        mNetworkControler.disableWifiNetwork(appInfo.getAppPkgName());
                    }
                }
                if(null != mNetworkControlAppChangeCallBack) {
                    mNetworkControlAppChangeCallBack.controlAppStateChange(mNetType);
                }
            }
        });
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
        CyeeSwitch aswitch;
    }
}
