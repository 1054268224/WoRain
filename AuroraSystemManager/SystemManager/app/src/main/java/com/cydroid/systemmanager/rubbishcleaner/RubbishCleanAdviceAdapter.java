package com.cydroid.systemmanager.rubbishcleaner;

/**
 * Created by zhaocaili on 18-7-23.
 */

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;

import com.cydroid.systemmanager.rubbishcleaner.DeeplyCleanerMainActivity;

import cyee.changecolors.ChameleonColorManager;

public class RubbishCleanAdviceAdapter extends BaseAdapter {
    private static final String TAG = "SystemMainAdapter";

    private Context mContext;
    private int mModeButtonLocked = -1;

    public static final int UNINSTALL_FREQUENCY_LOW_USED_APP = 0;
    public static final int DEEPLY_CLEAN = 1;

    public RubbishCleanAdviceAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return DEEPLY_CLEAN + 1;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.rubbish_clean_advice_layout, parent, false);
            holder.position = position;
            holder.contentImage = (ImageView) convertView.findViewById(R.id.rubbish_clean_advice_item_image);
            holder.contentTitle = (TextView) convertView.findViewById(R.id.rubbish_clean_advice_item_title);
            holder.contentSummary = (TextView) convertView.findViewById(R.id.rubbish_clean_advice_item_summary);
            holder.rightArrow = (ImageView) convertView.findViewById(R.id.rubbish_clean_advice_item_right_arrow);

            convertView.setTag(holder);
        } else {
            holder = (ModeHolder) convertView.getTag();
        }

        convertView.setOnClickListener(mItemClickListener);
        initListItem(position, holder);

        if (ChameleonColorManager.isNeedChangeColor()) {
            holder.rightArrow.setImageTintList(
                    ColorStateList.valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()));
        }
        return convertView;
    }

    private View.OnClickListener mItemClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            ModeHolder holder = (ModeHolder) v.getTag();
            hanleItemClick(holder.position);
        }
    };

    public synchronized boolean isModeButtonLocked(int mode) {
        return mModeButtonLocked != -1 && mModeButtonLocked != mode;
    }

    private void hanleItemClick(final int position) {
        Intent intent = new Intent();
        switch (position) {
            case UNINSTALL_FREQUENCY_LOW_USED_APP:
                intent.putExtra("monitor", 2);
                intent.putExtra("sortByUser", false);
                intent.setComponent(new ComponentName("com.cydroid.softmanager",
                        "com.cydroid.softmanager.softmanager.UninstallAppActivity"));
                break;
            case DEEPLY_CLEAN:
                intent.setClass(mContext, DeeplyCleanerMainActivity.class);
                break;
            default:
                break;
        }

        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {

        }
    }

    private void initListItem(int position, ModeHolder holder) {
        switch (position) {
            case UNINSTALL_FREQUENCY_LOW_USED_APP:
                initCleanRubbishItem(holder);
                break;
            case DEEPLY_CLEAN:
                initSpeedupItem(holder);
                break;
            default:
                break;
        }
    }

    private void initCleanRubbishItem(ModeHolder holder) {
        holder.contentImage.setBackgroundResource(R.drawable.app_uninstall_icon);
        holder.contentTitle.setText(R.string.uninstall_less_used_app);
        holder.contentSummary.setVisibility(View.GONE);
    }

    private void initSpeedupItem(ModeHolder holder) {
        holder.contentImage.setBackgroundResource(R.drawable.deep_clean_icon);
        holder.contentTitle.setText(R.string.deeply_clean);
        holder.contentSummary.setVisibility(View.GONE);
    }


    private static class ModeHolder {
        int position;
        ImageView contentImage;
        TextView contentTitle;
        TextView contentSummary;
        ImageView rightArrow;
    }

}

