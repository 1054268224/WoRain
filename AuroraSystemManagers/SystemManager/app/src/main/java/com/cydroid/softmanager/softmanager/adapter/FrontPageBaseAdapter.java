package com.cydroid.softmanager.softmanager.adapter;

import java.util.List;

import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.utils.HelperUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public abstract class FrontPageBaseAdapter<T extends ItemInfo> extends BaseAdapter {
    public List<? extends ItemInfo> mData;
    protected LayoutInflater mInflater;
    private final Context mContext;
    protected final AsyncAppIconLoader mAsyncAppIconLoader;

    public FrontPageBaseAdapter(Context context, List<? extends ItemInfo> data) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
    }

    @Override
    public int getCount() {
        return this.mData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.softmanager_frontpage_adapter_layout, parent, false);
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mSummary = (TextView) convertView.findViewById(R.id.summary);
            view.mAppSize = (TextView) convertView.findViewById(R.id.app_size);
            view.mIndicator = (ImageView) convertView.findViewById(R.id.indicator);
            view.mIndicator.setVisibility(View.VISIBLE);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }
        if (mData.size() > 0) {
            if (mData.get(position).getIcon() != null) {
                view.mIcon.setImageDrawable(mData.get(position).getIcon());
            } else {
                String pkgName = mData.get(position).getPackageName();                
                ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, pkgName);
                if (null != info) {
                    setIconImage(view.mIcon, pkgName, info);
                }
            }

            view.mTitle.setText(mData.get(position).getTitle());
            String summary = mData.get(position).getSummary();
            if (summary != null) {
                view.mSummary.setText(Html.fromHtml(mData.get(position).getSummary()));
                view.mSummary.setVisibility(View.VISIBLE);
            } else {
                view.mSummary.setVisibility(View.GONE);
            }

            bindView(position, view, convertView);
        }
        return convertView;
    }

    protected abstract void bindView(int position, ViewHolder view, View convertView);

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
}
