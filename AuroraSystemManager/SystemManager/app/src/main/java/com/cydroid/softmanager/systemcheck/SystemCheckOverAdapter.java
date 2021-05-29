package com.cydroid.softmanager.systemcheck;

/**
 * Created by zhaocaili on 18-7-23.
 */

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeButton;

public class SystemCheckOverAdapter extends BaseAdapter {
    private static final String TAG = "SystemCheckOverAdapter";

    private final Context mContext;
    private final boolean isNeedChangeColor;
    private final int deviderColor;
    private final int mOptimizeItemHeight;
    private final int mNormalItemHeight;
    private final int mHeaderItemHeight;
    private final float mOptimizeItemTextSize;
    private final float mNormalItemTextSize;
    private final OnItemClickCallback mItemClickCallback;

    public SystemCheckOverAdapter(Context context, OnItemClickCallback callback) {
        mContext = context;
        mItemClickCallback = callback;
        isNeedChangeColor = ChameleonColorManager.isNeedChangeColor();
        deviderColor = ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2();
        mOptimizeItemHeight = mContext.getResources().getDimensionPixelSize(R.dimen.system_check_over_optimize_item_height);
        mNormalItemHeight = mContext.getResources().getDimensionPixelSize(R.dimen.system_check_over_item_height);
        mHeaderItemHeight = mContext.getResources().getDimensionPixelSize(R.dimen.system_check_over_header_item_height);
        mOptimizeItemTextSize = mContext.getResources().getDimension(R.dimen.system_check_over_optimize_item_text_size);
        mNormalItemTextSize = mContext.getResources().getDimension(R.dimen.system_check_over_item_text_size);
    }

    @Override
    public int getCount() {
        int header = 0;
        if (SystemCheckItem.mToBeOptimizeList.size() != 0){
            header = header + 1;
        }
        if (SystemCheckItem.mSpeedupList.size() != 0){
            header = header + 1;
        }
        if (SystemCheckItem.mPowerManagerList.size() != 0){
            header = header + 1;
        }
        if (SystemCheckItem.mTrafficAssistentList.size() != 0){
            header = header + 1;
        }
        return SystemCheckItem.mSpeedupList.size() + SystemCheckItem.mPowerManagerList.size()
                + SystemCheckItem.mTrafficAssistentList.size() + SystemCheckItem.mToBeOptimizeList.size() + header;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.system_check_over_item_layout, parent, false);
            holder.position = position;
            holder.headerImg = (ImageView) convertView.findViewById(R.id.system_check_item_header_img);
            holder.tailImg = (ImageView) convertView.findViewById(R.id.system_check_item_tail_img);
            holder.contentCategory = (TextView) convertView.findViewById(R.id.system_check_item_category);
            holder.contentTitle = (TextView) convertView.findViewById(R.id.system_check_item_title);
            holder.tailBtn = (CyeeButton) convertView.findViewById(R.id.system_check_item_tail_btn);
            holder.headerDivider = (View) convertView.findViewById(R.id.header_divider);
            holder.itemDivider = (View) convertView.findViewById(R.id.item_divider);

            convertView.setTag(holder);
        } else {
            holder = (ModeHolder) convertView.getTag();
        }

        initCheckOverListItem(convertView, position, holder);
        return convertView;
    }

    private void initCheckOverListItem(View view, int position, ModeHolder holder) {
        if (position >= getCount()){
            return;
        }

        int optimizeSize = SystemCheckItem.mToBeOptimizeList.size();
        int speedSize = SystemCheckItem.mSpeedupList.size();
        int powerSize = SystemCheckItem.mPowerManagerList.size();
        int trafficSize = SystemCheckItem.mTrafficAssistentList.size();

        boolean header = setHeader(view, position, holder);
        if (header){
            return;
        }

        boolean show = true;
        Bundle bundle = null;
        if (optimizeSize == 0){
            holder.contentTitle.setMinHeight(mNormalItemHeight);
            holder.contentTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalItemTextSize);
            holder.tailBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalItemTextSize);
            if (position <= speedSize + 1){
                bundle = SystemCheckItem.mSpeedupList.get(position - 1);
            }else if (position <= speedSize + powerSize + 2){
                bundle = SystemCheckItem.mPowerManagerList.get(position - speedSize - 2);
            }else if (position <= speedSize + powerSize + trafficSize + 3){
                if (trafficSize != 0){
                    bundle = SystemCheckItem.mTrafficAssistentList.get(position - (speedSize + powerSize) - 3);
                }
            }
        }else {
            holder.contentTitle.setMinHeight(mNormalItemHeight);
            holder.contentTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalItemTextSize);
            holder.tailBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalItemTextSize);
            if (position <= optimizeSize){
                holder.contentTitle.setMinHeight(mOptimizeItemHeight);
                holder.contentTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mOptimizeItemTextSize);
                holder.tailBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNormalItemTextSize);
                show = false;
                bundle = SystemCheckItem.mToBeOptimizeList.get(position - 1);
            }else if (position <= optimizeSize + speedSize + 1){
                bundle = SystemCheckItem.mSpeedupList.get(position - optimizeSize - 2);
            }else if (position <= optimizeSize + speedSize + powerSize + 2){
                if (powerSize != 0){
                    bundle = SystemCheckItem.mPowerManagerList.get(position - (optimizeSize + speedSize) - 3);
                }else if (trafficSize != 0){
                    bundle = SystemCheckItem.mTrafficAssistentList.get(position - (optimizeSize + speedSize) - 3);
                }

            }else if(position <= optimizeSize + speedSize + powerSize + trafficSize + 3){
                if (trafficSize != 0){
                    bundle = SystemCheckItem.mTrafficAssistentList.get(position - (optimizeSize + speedSize + powerSize) - 4);
                }
            }
        }
        setViewContent(view, position, holder, bundle, show);
    }

    private boolean setHeader(View view, int position, ModeHolder holder){
        int optimizeSize = SystemCheckItem.mToBeOptimizeList.size();
        int speedSize = SystemCheckItem.mSpeedupList.size();
        int powerSize = SystemCheckItem.mPowerManagerList.size();
        int trafficSize = SystemCheckItem.mTrafficAssistentList.size();
        if (optimizeSize == 0){
            if (position == 0){
                setSpeedHeaderView(view, position, holder);
                return true;
            }else if (position == speedSize + 1){
                setPowerSaveHeaderView(view, position, holder);
                return true;
            }else if (position == speedSize + powerSize + 2){
                setTrafficAssistantHeaderView(view, position, holder);
                return true;
            }
        }else {
            if (position == 0){
                setOptimizeHeaderView(view, position, holder);
                return true;
            }else if (position == optimizeSize + 1){
                setSpeedHeaderView(view, position, holder);
                return true;
            }else if (position == optimizeSize + speedSize + 2){
                if (powerSize != 0){
                    setPowerSaveHeaderView(view, position, holder);
                    return true;
                }else if (trafficSize != 0){
                    setTrafficAssistantHeaderView(view, position, holder);
                    return true;
                }
            }else if (position == optimizeSize + speedSize + powerSize + 3){
                if (trafficSize != 0){
                    setTrafficAssistantHeaderView(view, position, holder);
                    return true;
                }
            }
        }
        return false;
    }

    private void setOptimizeHeaderView(View view, int position, ModeHolder holder){
        setHeaderViewContent(view, position, holder);
        holder.contentCategory.setText(R.string.system_check_to_be_optimize_category);
        holder.headerImg.setBackgroundResource(R.drawable.app_manager);
    }

    private void setSpeedHeaderView(View view, int position, ModeHolder holder){
        setHeaderViewContent(view, position, holder);
        holder.contentCategory.setText(R.string.system_check_speedup_category);
        holder.headerImg.setBackgroundResource(R.drawable.boot_speed);
    }

    private void setPowerSaveHeaderView(View view, int position, ModeHolder holder){
        setHeaderViewContent(view, position, holder);
        holder.contentCategory.setText(R.string.text_menu_savepower);
        holder.headerImg.setBackgroundResource(R.drawable.power_manager);
    }

    private void setTrafficAssistantHeaderView(View view, int position, ModeHolder holder){
        setHeaderViewContent(view, position, holder);
        holder.contentCategory.setText(R.string.text_menu_traffic);
        holder.headerImg.setBackgroundResource(R.drawable.traffic_monitor);
    }

    private void setHeaderViewContent(View view, int position, ModeHolder holder){
        if (!isNeedChangeColor){
            if (position == 0){
                view.setBackgroundResource(R.drawable.list_item_bg);
            }else {
                view.setBackgroundResource(R.drawable.list_item_header_bg);
            }
        }else {
            holder.headerDivider.setBackgroundColor(deviderColor);
        }
        holder.contentCategory.setMinHeight(mHeaderItemHeight);
        holder.contentTitle.setVisibility(View.GONE);
        holder.headerImg.setVisibility(View.VISIBLE);
        holder.tailImg.setVisibility(View.GONE);
        holder.contentCategory.setVisibility(View.VISIBLE);
        holder.tailBtn.setVisibility(View.GONE);
        holder.headerDivider.setVisibility(View.VISIBLE);
        holder.itemDivider.setVisibility(View.GONE);
    }

    private void setViewContent(View view, int position, ModeHolder holder, Bundle bundle, boolean showHeaderImg){
        if (bundle == null) return;
        if (!isNeedChangeColor){
            view.setBackgroundResource(R.drawable.list_item_bg);
        }
        holder.headerImg.setVisibility(View.GONE);
        holder.tailImg.setVisibility(View.VISIBLE);
        holder.contentTitle.setVisibility(View.VISIBLE);
        holder.headerDivider.setVisibility(View.GONE);
        holder.contentCategory.setVisibility(View.GONE);
        holder.itemDivider.setVisibility(View.GONE);
        holder.isSuggestedItem = bundle.getBoolean(SystemCheckItem.BUNDLE_KEY.isSuggestedItem);
        holder.contentTitle.setText(bundle.getString(SystemCheckItem.BUNDLE_KEY.contentTitle));
        holder.tailBtn.setTag(bundle.getInt(SystemCheckItem.BUNDLE_KEY.checkItem));
        holder.tailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickCallback.onItemClick(view, position);
            }
        });

        String btnText = bundle.getString(SystemCheckItem.BUNDLE_KEY.btnText);
        boolean checkStatus = bundle.getBoolean(SystemCheckItem.BUNDLE_KEY.checkStatus);
        if (checkStatus){
            holder.tailImg.setImageResource(R.drawable.system_check_normal);
        }else {
            if (showHeaderImg){
                holder.tailImg.setImageResource(R.drawable.system_check_abnormal);
            }else {
                holder.tailImg.setVisibility(View.GONE);
            }
        }
        if (holder.isSuggestedItem){
            if (checkStatus){
                holder.tailBtn.setVisibility(View.GONE);
            }else {
                if (showHeaderImg){
                    holder.tailBtn.setVisibility(View.GONE);
                }else {
                    //chenyee zhaocaili modify for CSW1809A-323 begin
                    if (btnText == null || TextUtils.isEmpty(btnText)){
                        holder.tailBtn.setVisibility(View.GONE);
                    }else {
                        holder.tailBtn.setVisibility(View.VISIBLE);
                        holder.tailBtn.setText(btnText);
                    }
                    //chenyee zhaocaili modify for CSW1809A-323 end
                }
            }
        }else {
            holder.tailBtn.setVisibility(View.GONE);
        }
    }

    public static class ModeHolder {
        int position;
        boolean isSuggestedItem;
        ImageView headerImg;
        ImageView tailImg;
        TextView contentCategory;
        TextView contentTitle;
        CyeeButton tailBtn;
        View headerDivider;
        View itemDivider;
    }

}

