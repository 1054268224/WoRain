/**
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: mengdw
 *
 * Date: 2016-12-06 for CR01775579
 */
package com.cydroid.softmanager.trafficassistant.lockscreenused.adapter;

import cyee.widget.CyeeTextView;
import android.content.Context;
import static android.net.TrafficStats.UID_REMOVED;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import static com.android.internal.util.Preconditions.checkNotNull;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.trafficassistant.net.AppItem;
import com.cydroid.softmanager.trafficassistant.net.UidDetail;
import com.cydroid.softmanager.trafficassistant.net.UidDetailProvider;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;

public class LockScreenTrafficUsedAdapter extends BaseAdapter {
    private static final String TAG = "LockScreenTrafficUsedAdapter";
    private final UidDetailProvider mProvider;
    private ArrayList<AppItem> mItems = new ArrayList<AppItem>();
    private Context mContext;
    
    public LockScreenTrafficUsedAdapter(UidDetailProvider provider, ArrayList<AppItem> items) {
        mProvider = checkNotNull(provider);
        mItems = items;
    }
    
    public void notifyDataSetChanged(ArrayList<AppItem> items) {
        mItems = items;
        super.notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).key;
    }

    @Override
    public boolean isEnabled(int position) {
        return mItems.get(position).key >= 10000;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.lock_screen_traffic_list_item, parent, false);
        }
        final Context context = parent.getContext();
        final CyeeTextView appTrafficView = (CyeeTextView) convertView.findViewById(R.id.lock_screen_traffic_app_data);
        final AppItem item = mItems.get(position);
        UidDetailTask.bindView(mProvider, item, convertView);
        appTrafficView.setText(Formatter.formatFileSize(context, item.total));
        return convertView;
    }
    
    private static class UidDetailTask extends AsyncTask<Void, Void, UidDetail> {
        private final UidDetailProvider mProvider;
        private final AppItem mItem;
        private final View mTarget;

        private UidDetailTask(UidDetailProvider provider, AppItem item, View target) {
            mProvider = checkNotNull(provider);
            mItem = checkNotNull(item);
            mTarget = checkNotNull(target);
        }

        public static void bindView(UidDetailProvider provider, AppItem item, View target) {

            final UidDetailTask existing = (UidDetailTask) target.getTag();
            if (existing != null) {
                existing.cancel(false);
            }

            final UidDetail cachedDetail = provider.getUidDetail(item.key, false);
           
            if (cachedDetail != null) {
                cachedDetail.uid = item.key;
                bindView(cachedDetail, target);
            } else {
                target.setTag(new UidDetailTask(provider, item, target)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
            }
        }

        private static void bindView(UidDetail detail, View target) {

            final ImageView icon = (ImageView) target.findViewById(R.id.lock_screen_traffic_app_icon);
            final CyeeTextView title = (CyeeTextView) target.findViewById(R.id.lock_screen_traffic_app_label);

            if (detail != null) {
                icon.setImageDrawable(detail.icon);
                title.setText(detail.label);
            } else {
                icon.setImageDrawable(null);
                title.setText(null);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            bindView(null, mTarget);
        }

        @Override
        protected UidDetail doInBackground(Void... params) {
            // TODO Auto-generated method stub
            return mProvider.getUidDetail(mItem.key, true);
        }

        @Override
        protected void onPostExecute(UidDetail result) {
            // TODO Auto-generated method stub
            bindView(result, mTarget);
        }
    }
    
}
