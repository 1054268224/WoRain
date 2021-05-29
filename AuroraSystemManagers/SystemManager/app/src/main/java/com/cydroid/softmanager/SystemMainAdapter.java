package com.cydroid.softmanager;

/**
 * Created by zhaocaili on 18-7-23.
 */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cydroid.softmanager.powersaver.activities.PowerManagerMainActivity;
import com.cydroid.softmanager.softmanager.SoftManagerActivity;
import com.cydroid.softmanager.trafficassistant.TrafficAssistantMainActivity;
import com.cydroid.softmanager.utils.Log;
import com.cydroid.softmanager.view.BoostSpeedActivity;


public class SystemMainAdapter extends BaseAdapter {
    private static final String TAG = "SystemMainAdapter";

    private final Context mContext;
    private int mAppManagerSummary = 0;
    private String mPowerManagerSummary = "";
    private boolean isCharging = false;

    public static final int SYSTEM_MAIN_CLEAN_RUBBISH = 0;
    public static final int SYSTEM_MAIN_SPEED_UP = 1;
    public static final int SYSTEM_MAIN_POWER_MANAGEMENT = 2;
    public static final int SYSTEM_MAIN_TRAFFIC_ASSISTANT = 3;
    public static final int SYSTEM_MAIN_APPS_MANAGEMENT = 4;
    public boolean mIsAdmin;

    public SystemMainAdapter(Context context) {
        mContext = context;
//        mIsAdmin = UserManager.get(mContext).isAdminUser();
        mIsAdmin = false;
    }

    @Override
    public int getCount() {
    if(mIsAdmin){
        return SYSTEM_MAIN_APPS_MANAGEMENT + 1;
    }else{
        return SYSTEM_MAIN_APPS_MANAGEMENT;
    }
        
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.system_main_item_layout, parent, false);
            holder.position = position;
            holder.contentImage = (ImageView) convertView.findViewById(R.id.system_main_item_image);
            holder.contentTitle = (TextView) convertView.findViewById(R.id.system_main_item_title);
            holder.contentSummary = (TextView) convertView.findViewById(R.id.system_main_item_summary);
            holder.contentSummary.setVisibility(View.GONE);
            holder.rightArrow = (ImageView) convertView.findViewById(R.id.system_main_item_tail_image);
            holder.rightArrow.setBackgroundResource(R.drawable.right_arrow);

            convertView.setTag(holder);
        } else {
            holder = (ModeHolder) convertView.getTag();
        }

        convertView.setOnClickListener(mItemClickListener);
        initListItem(position, holder);

//        if (ChameleonColorManager.isNeedChangeColor()) {
//            holder.rightArrow.setImageTintList(
//                    ColorStateList.valueOf(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1()));
//        }
        return convertView;
    }

    private final View.OnClickListener mItemClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            ModeHolder holder = (ModeHolder) v.getTag();
            if(!mIsAdmin){
                handleItemClick(holder.position + 1);
            }else{
                handleItemClick(holder.position);
            }
            
        }
    };

    private void handleItemClick(final int position) {
        Intent intent = new Intent();
        Class<?> cls = null;
        switch (position) {
            case SYSTEM_MAIN_CLEAN_RUBBISH:
                intent.setAction("com.cydroid.rubbishcleaner");
                intent.addCategory("com.cydroid.rubbishcleaner.category");
                break;
            case SYSTEM_MAIN_SPEED_UP:
                cls = BoostSpeedActivity.class;
                break;
            case SYSTEM_MAIN_POWER_MANAGEMENT:
                cls = PowerManagerMainActivity.class;
                break;
            case SYSTEM_MAIN_TRAFFIC_ASSISTANT:
                cls = TrafficAssistantMainActivity.class;
                break;
            case SYSTEM_MAIN_APPS_MANAGEMENT:
                cls = SoftManagerActivity.class;
                break;
            default:
                break;
        }
        if (position != SYSTEM_MAIN_CLEAN_RUBBISH){
            intent.setClass(mContext, cls);
        }
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "mode details activity not found, " + e.toString());
        }
    }

    private void initListItem(int position, ModeHolder holder) {
        if(!mIsAdmin){
            position = position + 1;
        }
        switch (position) {
            case SYSTEM_MAIN_CLEAN_RUBBISH:
                initCleanRubbishItem(holder);
                break;
            case SYSTEM_MAIN_SPEED_UP:
                initSpeedupItem(holder);
                break;
            case SYSTEM_MAIN_POWER_MANAGEMENT:
                initPowerManagementItem(holder);
                break;
            case SYSTEM_MAIN_TRAFFIC_ASSISTANT:
                initTrafficAssistantItem(holder);
                break;
            case SYSTEM_MAIN_APPS_MANAGEMENT:
                initAppsManagementItem(holder);
                break;
            default:
                break;
        }
    }

    private void initCleanRubbishItem(ModeHolder holder) {
        holder.contentImage.setBackgroundResource(R.drawable.cache_cleaner);
        holder.contentTitle.setText(R.string.rubbish_clean);
        holder.contentSummary.setText(R.string.system_main_rubbish_clean_summary);
    }

    private void initSpeedupItem(ModeHolder holder) {
        holder.contentImage.setBackgroundResource(R.drawable.boot_speed);
        holder.contentTitle.setText(R.string.text_speedup);
        holder.contentSummary.setText(R.string.system_main_speed_up_summary);
    }

    private void initPowerManagementItem(ModeHolder holder) {
        holder.contentImage.setBackgroundResource(R.drawable.power_manager);
        holder.contentTitle.setText(R.string.text_menu_savepower);
        holder.contentSummary.setText(isCharging? mPowerManagerSummary :
                mContext.getResources().getString(R.string.system_main_power_management_summary) + mPowerManagerSummary);
    }

    private void initTrafficAssistantItem(ModeHolder holder) {
        holder.contentImage.setBackgroundResource(R.drawable.traffic_monitor);
        holder.contentTitle.setText(R.string.text_menu_traffic);
        holder.contentSummary.setText(R.string.system_main_traffic_assistant_summary);
    }

    private void initAppsManagementItem(ModeHolder holder) {
        holder.contentImage.setBackgroundResource(R.drawable.app_manager);
        holder.contentTitle.setText(R.string.soft_manager);
        holder.contentSummary.setText(String.format(mContext.getResources().getString(R.string.system_main_apps_management_summary), mAppManagerSummary));
    }

    public void updateAppManagerSummary(int size){
        mAppManagerSummary = size;
        notifyDataSetChanged();
    }

    public void updatePowerManagerSummary(int time, String timeStr){
        isCharging = time <= 0;
        mPowerManagerSummary = timeStr;
        notifyDataSetChanged();
    }

    private static class ModeHolder {
        int position;
        ImageView contentImage;
        TextView contentTitle;
        TextView contentSummary;
        ImageView rightArrow;
    }

}

