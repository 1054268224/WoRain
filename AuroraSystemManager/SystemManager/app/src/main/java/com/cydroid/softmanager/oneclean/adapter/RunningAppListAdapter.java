/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 * Author: yangxinruo
 * Description: 正在运行应用界面
 *
 * Revised Date: 2017-02-16
 */
package com.cydroid.softmanager.oneclean.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.memoryclean.model.ProcessMemoryEntity;
import com.cydroid.softmanager.softmanager.utils.SoftHelperUtils;
import com.cydroid.softmanager.utils.HelperUtils;

import java.util.ArrayList;
import java.util.List;

public class RunningAppListAdapter extends BaseAdapter {
    private static final String TAG = "RunningAppListAdapter";

    protected LayoutInflater mInflater;
    public List<AppInfoData> mListItemData = new ArrayList<AppInfoData>();
    private final Context mContext;
    private final AsyncAppIconLoader mAsyncAppIconLoader;

    public RunningAppListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
    }

    public void setListItemData(ArrayList<AppInfoData> adapterDataList) {
        mListItemData = adapterDataList;
    }

    protected void bindView(int position, ViewCache view, View convertView) {
        final AppInfoData info = mListItemData.get(position);
        if (info == null) {
            return;
        }
        if (view.mCheckbox == null) {
            view.mCheckbox = (CheckBox) convertView.findViewById(R.id.process_checkbox);
            view.mCheckbox.setVisibility(View.VISIBLE);
        }
        view.mCheckbox.setChecked(info.mIsChecked);

        if (view.mTxtProcessInfo == null) {
            view.mTxtProcessInfo = (TextView) convertView.findViewById(R.id.process_info);
        }

        view.mTxtProcessInfo.setVisibility(View.GONE);

        if (info.mProcessMemoryEntity.mIsInUserWhiteAppList) {
            view.mTxtProcessInfo.setVisibility(View.VISIBLE);
        }
    }

    protected boolean shownSummary() {
        return true;
    }

    @Override
    public int getCount() {
        return mListItemData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListItemData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewCache view = null;
        if (convertView == null) {
            view = new ViewCache();
            convertView = mInflater.inflate(R.layout.running_apps_adapter_item, parent, false);
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mSummary = (TextView) convertView.findViewById(R.id.summary);
            convertView.setTag(view);
        } else {
            view = (ViewCache) convertView.getTag();
        }

        AppInfoData data = mListItemData.get(position);
        ApplicationInfo appInfo = HelperUtils.getApplicationInfo(mContext,
                data.mProcessMemoryEntity.mPackageName);

        if (data.mProcessMemoryEntity.mIsPrivateApp) {
            view.mIcon.setImageDrawable(
                    mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon));
        } else {
            setIconImage(view.mIcon, data.mProcessMemoryEntity.mPackageName, appInfo);
        }

        String pkgLabel = "";
        if (data.mProcessMemoryEntity.mIsPrivateApp) {
            pkgLabel = mContext.getResources().getString(R.string.encryptions_app_fake_name);
        } else {
            try {
                pkgLabel = HelperUtils.loadLabel(mContext,
                        HelperUtils.getApplicationInfo(mContext, data.mProcessMemoryEntity.mPackageName));
            } catch (Exception e) {
                pkgLabel = "";
            }
        }

        view.mTitle.setText(pkgLabel);
        if (shownSummary()) {
            view.mSummary.setText(Html
                    .fromHtml(SoftHelperUtils.getSizeStr(mContext, data.mProcessMemoryEntity.mPss * 1024)));
            view.mSummary.setVisibility(View.VISIBLE);
        } else {
            view.mSummary.setVisibility(View.GONE);
        }
        bindView(position, view, convertView);

        return convertView;
    }

    public void clearData() {
        mListItemData.clear();
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
                            icon.setImageDrawable(HelperUtils.loadIcon(mContext, applicationInfo));
                        }
                    }
                });
        if (null != cachedImage) {
            icon.setImageDrawable(cachedImage);
        }
    }

    public ArrayList<AppInfoData> getSelectedItems() {
        ArrayList<AppInfoData> resList = new ArrayList<AppInfoData>();
        for (AppInfoData data : mListItemData) {
            if (data.mIsChecked) {
                resList.add(data);
            }
        }
        return resList;
    }

    public static class AppInfoData {

        public ProcessMemoryEntity mProcessMemoryEntity;
        public boolean mIsChecked;

        public AppInfoData(ProcessMemoryEntity data, boolean isCheck) {
            mProcessMemoryEntity = data;
            mIsChecked = isCheck;
        }
    }

    public static class ViewCache {
        public TextView mSummary;
        public TextView mTitle;
        public ImageView mIcon;
        public TextView mTxtProcessInfo;
        public CheckBox mCheckbox;
    }
}
