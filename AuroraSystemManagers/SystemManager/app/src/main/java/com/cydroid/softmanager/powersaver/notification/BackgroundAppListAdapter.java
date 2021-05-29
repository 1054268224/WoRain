package com.cydroid.softmanager.powersaver.notification;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.powersaver.notification.strategy.SimplePowerConsumeAppMonitorFactory;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cyee.widget.CyeeTextView;

public class BackgroundAppListAdapter extends BaseAdapter {
    public static final String TAG = "BackgroundAppListAdapter";

    private final Context mContext;
    private List<AppBatteryData> mListItemData = new ArrayList<AppBatteryData>();
    private int mProgress;
    private final AsyncAppIconLoader mAsyncAppIconLoader;

    public void setListItemData(List<AppBatteryData> newListItem) {
        mListItemData = newListItem;
    }

    public BackgroundAppListAdapter(Context context) {
        mContext = context;
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
    }

    @Override
    public int getCount() {
        if (mListItemData != null) {
            return mListItemData.size();
        }
        return 0;
    }

    @Override
    public AppBatteryData getItem(int position) {
        if (mListItemData == null) {
            return null;
        }
        return mListItemData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private void setPercent(double percentOfMax, double percentOfTotal) {
        mProgress = (int) Math.ceil(percentOfMax);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        final ViewCache viewCache;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.power_consume_background_app_item,
                    parent, false);

            viewCache = new ViewCache();
            view.setTag(viewCache);
        } else {
            viewCache = (ViewCache) view.getTag();
        }

        final AppBatteryData sipper = mListItemData.get(position);
        setPercent(sipper.mPercent, sipper.mPercent);

        String pkgLable = "";
        if (sipper.mIsPrivateApp) {
            pkgLable = mContext.getResources().getString(R.string.encryptions_app_fake_name);
        } else {
            try {
                pkgLable = HelperUtils.loadLabel(mContext,
                        HelperUtils.getApplicationInfo(mContext, sipper.mPackageName));
            } catch (Exception e) {
                pkgLable = "";
            }
        }

        viewCache.mIcon = (ImageView) view.findViewById(android.R.id.icon);
        if (sipper.mIsPrivateApp) {
            viewCache.mIcon.setImageDrawable(
                    mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon));
        } else {
            try {
                ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, sipper.mPackageName);
                if (null != info) {
                    setIconImage(viewCache.mIcon, sipper.mPackageName, info);
                }
            } catch (Exception e) {
                Log.d(TAG, "load icon for pkg:" + sipper.mPackageName + " failed!");
            }
        }

        viewCache.mText = (CyeeTextView) view.findViewById(R.id.title);
        // Gionee <yangxinruo> <2015-08-19> add for begin
        java.math.BigDecimal bd = null;
        bd = new java.math.BigDecimal(sipper.mPowerValue);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

        viewCache.mText.setText(pkgLable);

        // Gionee <yangxinruo> <2015-08-19> add for end

        viewCache.mProgress = (ProgressBar) view.findViewById(R.id.progress);
        viewCache.mProgress.setProgress(mProgress);

        viewCache.mTextSummary = (CyeeTextView) view.findViewById(R.id.summary);
        // Gionee <yangxinruo> <2015-12-2> modify for CR01602473 begin
        // Gionee <yangxinruo> <2016-7-12> modify for CR01730410 begin
        if (sipper.mPowerValue == 0) {
            viewCache.mTextSummary.setText("");
        } else if ("0.00".equals(bd.toString())) {
            viewCache.mTextSummary.setText("< 0.01 mAh");
        } else {
            viewCache.mTextSummary.setText(bd + " mAh");
        }
        // Gionee <yangxinruo> <2016-7-12> modify for CR01730410 end
        // Gionee <yangxinruo> <2015-12-2> modify for CR01602473 end

        // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 begin
        CyeeTextView alertText = ((CyeeTextView) view.findViewById(R.id.alert));
        // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 begin
        alertText.setVisibility(View.GONE);
        if (sipper.mIsInUserWhiteAppList) {
            alertText.setVisibility(View.VISIBLE);
            alertText.setText(R.string.power_consume_whitelist_tip);
        } else if (sipper.mIsInIgnoredAppList) {
            alertText.setVisibility(View.VISIBLE);
            alertText.setText(R.string.power_consume_ignore_tip);
        }

        CyeeTextView monitorText = ((CyeeTextView) view.findViewById(R.id.monitor_alert));
        monitorText.setVisibility(View.INVISIBLE);
        if (!sipper.mAlertTypes.isEmpty()) {
            StringBuilder msgStr = new StringBuilder();
            for (String alertType : sipper.mAlertTypes) {
                msgStr.append(mContext
                        .getString(SimplePowerConsumeAppMonitorFactory.getMessageStringId(alertType)));
                msgStr.append(" ");
            }
            monitorText.setText(msgStr.toString().trim());
            monitorText.setVisibility(View.VISIBLE);
        }
        // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 end
        // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 end

        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
        checkbox.setChecked(sipper.mIsChecked);
        viewCache.mCheckbox = checkbox;

        return view;
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

    public static class ViewCache {
        CheckBox mCheckbox;
        ImageView mIcon;
        CyeeTextView mText;
        ProgressBar mProgress;
        CyeeTextView mTextSummary;
        // CyeeTextView textMonitor;
    }

    public static class AppBatteryData {
        // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 begin
        // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 begin
        private final PowerConsumeAppData mPowerConsumeAppData;
        public String mPackageName;
        public double mPowerValue;
        public boolean mIsInIgnoredAppList;
        public boolean mIsInUserWhiteAppList;
        public ArrayList<String> mAlertTypes;
        public boolean mIsPrivateApp;

        double mPercent;
        boolean mIsChecked;

        public AppBatteryData(PowerConsumeAppData powerConsumeAppData, String packageName, double powerValue,
                              boolean isInIgnoredAppList, boolean isInUserWhiteAppList, ArrayList<String> alertTypes,
                              double percent, boolean isChecked, boolean isPrivateApp) {
            mPowerConsumeAppData = powerConsumeAppData;
            mPackageName = packageName;
            mPowerValue = powerValue;
            mIsInIgnoredAppList = isInIgnoredAppList;
            mIsInUserWhiteAppList = isInUserWhiteAppList;
            mAlertTypes = alertTypes;
            mIsPrivateApp = isPrivateApp;

            mPercent = percent;
            mIsChecked = isChecked;
        }

        public PowerConsumeAppData getPowerConsumeAppData() {
            return mPowerConsumeAppData;
        }
        // Gionee <yangxinruo> <2015-12-29> modify for CR01615676 end
        // Gionee <yangxinruo> <2016-02-1> modify for CR01634814 end
    }

    public List<AppBatteryData> getSelectedItems() {
        ArrayList<AppBatteryData> resList = new ArrayList<AppBatteryData>();
        for (AppBatteryData data : mListItemData) {
            if (data.mIsChecked) {
                resList.add(data);
            }
        }
        return resList;
    }

}
