package com.cydroid.softmanager.softmanager.adapter;

import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.model.AppInfo;
import com.cydroid.softmanager.softmanager.utils.SoftMrgUtil;
import com.cydroid.softmanager.utils.HelperUtils;
import com.cydroid.softmanager.utils.Log;

import android.R.integer;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 File Description:
 * 
 * @author: Gionee-daizhm
 * @date：2013-08-22
 */
public class MoveAdapter extends BaseAdapter {
    private final Context mContext;
    protected List<List<AppInfo>> mAppItems = new ArrayList<List<AppInfo>>();
    private AppInfo mAppInfo = null;

    private int mItemPositon;
    private int mCount = 0;
    private final StateChangeCallback mCallback;
    private final AsyncAppIconLoader mAsyncAppIconLoader;

    public MoveAdapter(Context context, StateChangeCallback callback) {
        mContext = context;
        mCount = 0;
        mCallback = callback;
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
    }

    @Override
    public int getCount() {
        if (mCount == 0) {
            mCount = mAppItems.get(mItemPositon).size();
        }
        return mCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;// = ( ViewHolder ) convertView.getTag();
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.softmanager_adapter_layout, parent,
                    false);
            viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.mSummary = (TextView) convertView.findViewById(R.id.summary);
            viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        mAppInfo = mAppItems.get(mItemPositon).get(position);
        if (mAppInfo != null) {
            ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, mAppInfo.getPackageName());
            if (info == null) {
                return convertView;
            }
            setIconImage(viewHolder.mIcon, mAppInfo.getPackageName(), info);
            
            viewHolder.mTitle.setText(mAppInfo.getTitle());
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setChecked(mAppInfo.getCheckStaus());
            viewHolder.mCheckBox.setTag(position);

            if (mAppInfo.getSummary() == null) {
                viewHolder.mSummary.setVisibility(View.GONE);
            } else {
                viewHolder.mSummary.setVisibility(View.VISIBLE);
                viewHolder.mSummary.setText(mAppInfo.getSummary());
            }
            if (viewHolder.mMovingView == null) {
                viewHolder.mMovingView = (LinearLayout) convertView.findViewById(R.id.movingview);
            }

            viewHolder.mCheckBox.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mItemPositon >= mAppItems.size()
                            || (Integer) v.getTag() >= mAppItems.get(mItemPositon).size()) {
                        return;
                    }
                    boolean status = mAppItems.get(mItemPositon).get((Integer) v.getTag()).getCheckStaus();
                    mAppItems.get(mItemPositon).get((Integer) v.getTag()).setCheckStatus(!status);
                    notifyDataSetChanged();
                    mCallback.onStateChange();
                }
            });
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

    public void updateChild(int position, List<List<AppInfo>> mData) {
        mItemPositon = position;
        mAppItems = mData;
    }

}