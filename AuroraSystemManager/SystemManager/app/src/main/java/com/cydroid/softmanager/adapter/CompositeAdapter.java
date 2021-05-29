package com.cydroid.softmanager.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.utils.HelperUtils;

import java.util.List;

public abstract class CompositeAdapter<T extends ItemInfo> extends BaseAdapter {
    protected final Context mContext;
    protected final LayoutInflater mInflater;
    protected final AsyncAppIconLoader mAsyncAppIconLoader;
    
    protected List<? extends ItemInfo> mData;

    public CompositeAdapter(Context context, List<? extends ItemInfo> data) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();

        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected void setIconImage(final ImageView icon, final String packageName, 
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
