package com.cydroid.softmanager;

/**
 * Created by zhaocaili on 18-7-23.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.model.ItemInfo;

import java.util.ArrayList;
import java.util.List;

import cyee.changecolors.ChameleonColorManager;

public class RunningProcessAdapter extends BaseAdapter {
    private static final String TAG = "RunningProcessAdapter";

    private final Context mContext;
    private final View.OnClickListener mClickListener;

    private List<ItemInfo> runningProcess = new ArrayList<>();

    public RunningProcessAdapter(Context context, View.OnClickListener listener) {
        mContext = context;
        mClickListener = listener;
    }

    @Override
    public int getCount() {
        return runningProcess.size() + 1;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.running_process_item_layout, parent, false);
            holder.headImage = (ImageView) convertView.findViewById(R.id.running_process_item_image);
            holder.contentTitle = (TextView) convertView.findViewById(R.id.running_process_item_title);
            holder.tailImage =  convertView.findViewById(R.id.running_process_item_tail_image);

            convertView.setTag(holder);
        } else {
            holder = (ModeHolder) convertView.getTag();
        }
        initListItem(position, holder);
        return convertView;
    }

    private void initListItem(int position, ModeHolder holder) {
       if (position == 0){
           initFirstItem(holder);
       }else {
           initRunningItem(position, holder);
       }
    }

    private void initFirstItem(ModeHolder holder) {
        holder.headImage.setVisibility(View.GONE);
        holder.contentTitle.setText(String.format(mContext.getResources().getString(R.string.running_process_title), runningProcess.size()));
        holder.tailImage.setVisibility(View.GONE);
    }

    private void initRunningItem(int position, ModeHolder holder) {
        ItemInfo info = runningProcess.get(position - 1);
        holder.headImage.setVisibility(View.VISIBLE);
        holder.tailImage.setVisibility(View.VISIBLE);
        holder.contentTitle.setText(info.getTitle());
        holder.headImage.setBackground(info.getIcon());
        if (info.getGreenWhiteListItemState()){
            holder.tailImage.setImageResource(R.drawable.icon_soft_applock);
        }else {
            holder.tailImage.setImageResource(R.drawable.icon_soft_appunlock);
        }
        holder.tailImage.setOnClickListener(mClickListener);
        holder.tailImage.setTag(info.getPackageName());
    }

    public void setRunningProcess(List<ItemInfo> entities){
        runningProcess = entities;
        notifyDataSetChanged();
    }

    private static class ModeHolder {
        ImageView headImage;
        TextView contentTitle;
        ImageView tailImage;
    }

}

