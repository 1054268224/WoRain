/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.softmanager.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppInfo;
import com.cydroid.softmanager.utils.HelperUtils;
import com.example.systemmanageruidemo.UnitUtil;

import java.util.List;

import cyee.widget.CyeeButton;

public class FreezedAppAdapter extends CompositeAdapter<FreezeAppInfo> {
    private static final String TAG = "FreezedAppAdapter";

    private View.OnClickListener mClickListener;

    public FreezedAppAdapter(Context context, List<? extends ItemInfo> data,
                             View.OnClickListener clickListener) {
        super(context, data);
        mClickListener = clickListener;
    }

    public void setOnClickListener(View.OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.freezedapp_adapter_layout, parent, false);
            view = new ViewHolder();
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mButton = (CyeeButton) convertView.findViewById(R.id.btn);
            view.mBtn = (Button) convertView.findViewById(R.id.mbtn);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        bindView(position, view, convertView);
        return convertView;
    }

    protected void bindView(int position, ViewHolder view, View convertView) {
        final String packageName = mData.get(position).getPackageName();
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext,
                packageName);
        if (null != info) {
            setIconImage(view.mIcon, packageName, info);
        }
        view.mTitle.setText(mData.get(position).getTitle());
        view.mButton.setTag(position);
        view.mButton.setText(mContext.getResources().getString(R.string.text_freezed_btn));
        view.mButton.setOnClickListener(mClickListener);

        view.mBtn.setTag(position);
        view.mBtn.setOnClickListener(mClickListener);
    }
}