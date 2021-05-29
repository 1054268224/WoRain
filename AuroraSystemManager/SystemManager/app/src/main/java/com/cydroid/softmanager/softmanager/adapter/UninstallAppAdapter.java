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
import android.content.res.ColorStateList;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppInfo;
import com.cydroid.softmanager.softmanager.uninstall.UninstallAppUtils;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.util.List;

import cyee.changecolors.ChameleonColorManager;

public class UninstallAppAdapter extends CompositeAdapter<UninstallAppInfo> {
    private static final String TAG = "UninstallAppAdapter";

    private static final long DAY_TIME = 24 * 60 * 60 * 1000;
    private static final long HALF_MONTH_TIME = DAY_TIME * 15;
    private static final long MONTH_TIME_3 = DAY_TIME * 30*3;
    private static final long MONTH_TIME_1 = DAY_TIME * 30;

    private final View.OnClickListener mClickListener;
    private final boolean mSortByUser;

    public UninstallAppAdapter(Context context, List<? extends ItemInfo> data,
                               View.OnClickListener clickListener, boolean sortByUser) {
        super(context, data);
        mClickListener = clickListener;
        mSortByUser = sortByUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.uninstallapp_adapter_layout, parent, false);
            view = new ViewHolder();
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mSummary = (TextView) convertView.findViewById(R.id.summary);
            view.mAppSize = (TextView) convertView.findViewById(R.id.app_size);
            view.mAppFrequency = (TextView) convertView.findViewById(R.id.frequency);

            view.mUninstall = (ImageView) convertView.findViewById(R.id.indicator);
            view.mUninstall.setImageResource(R.drawable.uninstall_img);
            view.mUninstall.setBackground(mContext.getResources().getDrawable(
                    R.drawable.img_bg_selector_material));
            if (ChameleonColorManager.isNeedChangeColor()) {
                view.mUninstall.setImageTintList(ColorStateList.valueOf(ChameleonColorManager
                        .getContentColorPrimaryOnBackgroud_C1()));
                view.mAppSize.setTextColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
            }

            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        bindView(position, view, convertView);
        return convertView;
    }

    protected void bindView(int position, ViewHolder view, View convertView) {
        int showType = mSortByUser? UninstallAppUtils.getUninstallShowType(mContext) : 2;
        UninstallAppInfo uninstallAppInfo = (UninstallAppInfo) mData.get(position);
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext,
                uninstallAppInfo.getPackageName());
        if (null != info) {
            setIconImage(view.mIcon, uninstallAppInfo.getPackageName(), info);
        }
        view.mTitle.setText(uninstallAppInfo.getTitle());

        String summary = uninstallAppInfo.getSummary(mContext, showType);
        if (summary != null) {
            view.mSummary.setText(Html.fromHtml(summary));
            view.mSummary.setVisibility(View.VISIBLE);
        } else {
            view.mSummary.setVisibility(View.GONE);
        }

        if (2 == showType) {
            long appFrequency = uninstallAppInfo.getUseFrequency();
            view.mAppFrequency.setVisibility(View.VISIBLE);
            view.mSummary.setVisibility(View.GONE);
            if (appFrequency > MONTH_TIME_3) {
               // summary = mContext.getResources().getString(R.string.app_frequency_nerver);
                summary = mContext.getResources().getString(R.string.app_frequency_neverp,
                        3);
                view.mAppFrequency.setTextColor(mContext.getResources().getColor(R.color.app_use_red));
            } else if (appFrequency > MONTH_TIME_1 && appFrequency <= MONTH_TIME_3) {
                summary = mContext.getResources().getString(R.string.app_frequency_neverp,
                       1);
                view.mAppFrequency.setTextColor(mContext.getResources().getColor(R.color.app_use_often));
            } else if (appFrequency > DAY_TIME && appFrequency <= MONTH_TIME_1) {
                summary = mContext.getResources().getString(R.string.app_frequency_always,
                        appFrequency / DAY_TIME);
                view.mAppFrequency.setTextColor(mContext.getResources().getColor(R.color.app_use_always));
            } else {
                summary = mContext.getResources().getString(R.string.app_frequency_today);
                view.mAppFrequency.setTextColor(
                        mContext.getResources().getColor(R.color.softmanager_trust_child_summary_txtcolor));
            }
            view.mAppFrequency.setText(summary);
        } else {
            view.mAppFrequency.setVisibility(View.GONE);
        }

        String appSize = "";
        try {
            appSize = SoftHelperUtils.getSizeStr(mContext, uninstallAppInfo.getPackageSize());
        } catch (Exception e) {
            Log.e(TAG, "bindView e:" + e);
            appSize = "";
        }

        view.mAppSize.setText(appSize);
        view.mUninstall.setTag(position);
        view.mUninstall.setOnClickListener(mClickListener);
    }
}