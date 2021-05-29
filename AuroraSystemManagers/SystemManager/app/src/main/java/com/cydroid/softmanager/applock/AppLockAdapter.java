/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2016-03-22
 */
package com.cydroid.softmanager.applock;

import cyee.widget.CyeeSwitch;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class AppLockAdapter extends CompositeAdapter<AppLockAppInfo> {
    private static final String TAG = "AppLockAdapter";
    
    private final Context mContext;
    private final CompoundButton.OnCheckedChangeListener mSwitchChangeListener;

    public AppLockAdapter(Context context, List<? extends ItemInfo> data,
            CompoundButton.OnCheckedChangeListener switchChangeListener) {
        super(context, data);
        mContext = context;
        mSwitchChangeListener = switchChangeListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;        
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.applock_adapter_layout, parent, false);
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mSwitch = (CyeeSwitch) convertView.findViewById(R.id.switch_btn);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        if (mData.isEmpty()) {
            return convertView;
        }
        
        bindView(position, view, convertView);
        return convertView;
    }

    protected void bindView(int position, ViewHolder view, View convertView) {
        final AppLockAppInfo info = (AppLockAppInfo) mData.get(position);
        String packageName = info.getPackageName();
        ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == applicationInfo) {
            return;
        }
        setIconImage(view.mIcon, packageName, applicationInfo);
        
        view.mTitle.setText(info.getTitle());
        view.mSwitch.setTag(position);
        view.mSwitch.setOnCheckedChangeListener(null);
        Log.d(TAG, "bindView pkgName:" + info.getPackageName() + ", isLocked:" + info.isLocked());
        view.mSwitch.setChecked(info.isLocked());
        view.mSwitch.setOnCheckedChangeListener(mSwitchChangeListener);
    }
}