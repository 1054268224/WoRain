package com.odm.tool;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
    private static String TAG = "GridAdapter";
    private LayoutInflater mInflater;
    private String[] activityStrings;
    private TypedArray activityIcon;
    private TypedArray activityTitle;
    private Context mContext;

    public GridAdapter(Context context, String[] activityStrings, TypedArray activityIcon, TypedArray activityTitle) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        this.activityStrings = activityStrings;
        this.activityIcon = activityIcon;
        this.activityTitle = activityTitle;
    }

    @Override
    public int getCount() {
        return activityTitle.length();
    }

    @Override
    public String getItem(int position) {
        return activityTitle.getString(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.gridview_item, parent, false);
        } else {
            view = convertView;
        }

        ImageView icon = view.findViewById(R.id.item_icon);
        TextView title = view.findViewById(R.id.item_title);
        icon.setImageDrawable(activityIcon.getDrawable(position));
        title.setText(activityTitle.getText(position));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClassName(mContext,activityStrings[position]);
                Log.e("wangjian","setClassName = " + activityStrings[position]);
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
