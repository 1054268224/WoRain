package com.cydroid.softmanager.greenbackground.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.model.ViewHolder;

import cyee.changecolors.ChameleonColorManager;

public class GreenBackgroundAdapter extends BaseAdapter {
    private static final int GREEN_BACKGROUND_ITEM_COUNT = 1;

    private final Context mContext;
    private final String[] mTitles;
    private final Resources mRes;
    public boolean mSwitchState = false;

    public GreenBackgroundAdapter(Context context) {
        mContext = context;
        mRes = mContext.getResources();
        mTitles = mRes.getStringArray(R.array.green_list_item);
    }

    @Override
    public int getCount() {
        return GREEN_BACKGROUND_ITEM_COUNT;
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
    public boolean isEnabled(int position) {
        return mSwitchState;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.greenbackground_adapter_layout, parent,
                    false);
            viewHolder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.mContainer_icon = (LinearLayout) convertView.findViewById(R.id.container_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTitle.setText(mTitles[position]);
        viewHolder.mTitle.setEnabled(mSwitchState);
        if (!mSwitchState) {
            viewHolder.mTitle.setTextColor(mRes.getColor(R.color.green_bg_off_textcolor));
            if (ChameleonColorManager.isNeedChangeColor()) {
                viewHolder.mTitle.setTextColor(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3());
                viewHolder.mIcon.setImageTintList(ColorStateList.valueOf(ChameleonColorManager
                        .getContentColorThirdlyOnBackgroud_C3()));
            }
            viewHolder.mIcon.setImageDrawable(mRes.getDrawable(R.drawable.icon_green_app_disable));
        } else {
            viewHolder.mTitle.setTextColor(mRes.getColor(R.color.list_item_title_color));
            if (ChameleonColorManager.isNeedChangeColor()) {
                viewHolder.mTitle.setTextColor(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
                viewHolder.mIcon.setImageTintList(ColorStateList.valueOf(ChameleonColorManager
                        .getContentColorPrimaryOnBackgroud_C1()));
            }
            viewHolder.mIcon.setImageDrawable(mRes.getDrawable(R.drawable.icon_green_app_enable));
        }
        return convertView;
    }
}
