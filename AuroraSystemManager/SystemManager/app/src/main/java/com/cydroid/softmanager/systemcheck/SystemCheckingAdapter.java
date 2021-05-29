package com.cydroid.softmanager.systemcheck;

/**
 * Created by zhaocaili on 18-7-23.
 */

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;

public class SystemCheckingAdapter extends BaseAdapter {
    private static final String TAG = "SystemCheckingAdapter";

    private final Context mContext;

    public SystemCheckingAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ModeHolder holder;
        if (convertView == null) {
            holder = new ModeHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.system_checking_item_layout, parent, false);
            holder.headerImg = (ImageView) convertView.findViewById(R.id.system_check_item_header);
            holder.contentCategory = (TextView) convertView.findViewById(R.id.system_check_item_category);
            holder.contentTitle = (TextView) convertView.findViewById(R.id.system_check_item_title);

            convertView.setTag(holder);
        } else {
            holder = (ModeHolder) convertView.getTag();
        }

        initListItem(position, holder);

        return convertView;
    }

    private void initListItem(int position, ModeHolder holder) {
        if (position >= SystemCheckItem.mSystemCheckList.size()){
            return;
        }
        Bundle bundle = SystemCheckItem.mSystemCheckList.get(SystemCheckItem.mSystemCheckList.size() - 1 - position);
        setViewContent(holder, bundle);
    }

    private void setViewContent(ModeHolder holder, Bundle bundle){
        if (bundle == null) return;
        String category = bundle.getString(SystemCheckItem.BUNDLE_KEY.category);
        if (category.equals(mContext.getResources().getString(R.string.system_check_speedup_category))){
            holder.headerImg.setBackgroundResource(R.drawable.boot_speed);
        }else if (category.equals(mContext.getResources().getString(R.string.text_menu_savepower))){
            holder.headerImg.setBackgroundResource(R.drawable.power_manager);
        }else if (category.equals(mContext.getResources().getString(R.string.text_menu_traffic))){
            holder.headerImg.setBackgroundResource(R.drawable.traffic_monitor);
        }
        holder.contentCategory.setText(category);
        holder.contentTitle.setText(bundle.getString(SystemCheckItem.BUNDLE_KEY.contentTitle));
    }

    public static class ModeHolder {
        ImageView headerImg;
        TextView contentCategory;
        TextView contentTitle;
    }

}

