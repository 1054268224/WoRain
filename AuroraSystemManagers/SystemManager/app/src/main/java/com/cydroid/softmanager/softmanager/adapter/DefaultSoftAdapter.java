package com.cydroid.softmanager.softmanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftItemInfo;

import java.util.List;

import cyee.changecolors.ChameleonColorManager;

public class DefaultSoftAdapter extends CompositeAdapter<DefaultSoftItemInfo> {

    public DefaultSoftAdapter(Context context, List<? extends ItemInfo> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.defaultsoft_adapter_layout, parent, false);
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
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
        DefaultSoftItemInfo info = (DefaultSoftItemInfo) mData.get(position);
        if (null == info) {
            return;
        }

        int resId = info.getIconResId();
        Drawable icon = mContext.getResources().getDrawable(resId);
        if (null != icon) {
            if (ChameleonColorManager.isNeedChangeColor()) {
                icon.setTint(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
            }
            view.mIcon.setImageDrawable(icon);
        }

        int titleResId = info.getTitleResId();
        view.mTitle.setText(titleResId);

        String summary = info.getSummary();
        view.mSummary.setText(Html.fromHtml(summary));
    }
}
