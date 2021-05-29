/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.oneclean.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.oneclean.whitelist.WhiteAppInfo;
import com.cydroid.softmanager.utils.HelperUtils;

import java.util.List;

import cyee.widget.CyeeButton;

public class WhiteListAdapter extends CompositeAdapter<WhiteAppInfo> {
    private static final String TAG = "WhiteListAdapter";

    private final View.OnClickListener mClickListener;

    public WhiteListAdapter(Context context, List<? extends ItemInfo> data,
                            View.OnClickListener clickListener) {
        super(context, data);
        mClickListener = clickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.whitelist_adapter_layout, parent, false);
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mSummary = (TextView) convertView.findViewById(R.id.summary);
            view.mButton = (CyeeButton) convertView.findViewById(R.id.btn);

            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        bindView(position, view, convertView);
        return convertView;
    }

    protected void bindView(int position, final ViewHolder view, View convertView) {
        final String packageName = mData.get(position).getPackageName();
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, packageName);
        if (null != info) {
            setIconImage(view.mIcon, packageName, info);
        }
        view.mTitle.setText(mData.get(position).getTitle());
        view.mSummary.setText(Html.fromHtml(mData.get(position).getSummary()));

        view.mButton.setTag(position);
        if (((WhiteAppInfo) mData.get(position)).isInUserWhiteList()) {
            view.mButton.setText(mContext.getString(R.string.remove));
        } else {
            view.mButton.setText(mContext.getString(R.string.add));
        }
        view.mButton.setOnClickListener(mClickListener);
    }
}
