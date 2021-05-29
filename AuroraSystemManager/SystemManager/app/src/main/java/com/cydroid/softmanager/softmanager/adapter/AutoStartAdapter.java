package com.cydroid.softmanager.softmanager.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.autoboot.AutoBootAppInfo;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

import cyee.widget.CyeeSwitch;

public class AutoStartAdapter extends CompositeAdapter<AutoBootAppInfo> {
    private CompoundButton.OnCheckedChangeListener mSwitchChangeListener;

    public AutoStartAdapter(Context context, List<? extends ItemInfo> data,
                            CompoundButton.OnCheckedChangeListener switchChangeListener) {
        super(context, data);
        mSwitchChangeListener = switchChangeListener;
    }

    public void setSwitchChangeListener(CompoundButton.OnCheckedChangeListener switchChangeListener) {
        mSwitchChangeListener = switchChangeListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.autoboot_adapter_layout, parent, false);
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mSwitch = (CyeeSwitch) convertView.findViewById(R.id.switch_btn);
            view.aSwitch = (Switch) convertView.findViewById(R.id.switch_act);
            view.mSummary = (TextView) convertView.findViewById(R.id.summary);
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
        final AutoBootAppInfo info = (AutoBootAppInfo) mData.get(position);
        final String packageName = info.getPackageName();
        final ApplicationInfo applicationInfo = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null == applicationInfo) {
            return;
        }
        setIconImage(view.mIcon, packageName, applicationInfo);
        view.mTitle.setText(info.getTitle());
        view.mSummary.setText(info.getSummary(mContext));
        view.mSwitch.setTag(position);
        view.mSwitch.setOnCheckedChangeListener(null);
        Log.d("AutoStartAdapter", "bindView pkgName:" + info.getPackageName() + ", isAutoBoot:" + info.isAutoBoot());
        view.mSwitch.setChecked(info.isAutoBoot());
        view.mSwitch.setOnCheckedChangeListener(mSwitchChangeListener);
        view.aSwitch.setTag(position);
        view.mSwitch.setOnCheckedChangeListener(null);
        view.aSwitch.setChecked(info.isAutoBoot());
        view.aSwitch.setOnCheckedChangeListener(mSwitchChangeListener);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.isAutoBoot()) {
                    info.setAutoBootState(false);
                } else {
                    info.setAutoBootState(true);
                }
                view.aSwitch.setChecked(info.isAutoBoot());
            }
        });
    }
}
