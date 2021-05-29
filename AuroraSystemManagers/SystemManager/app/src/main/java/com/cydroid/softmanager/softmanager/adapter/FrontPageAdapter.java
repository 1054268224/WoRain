package com.cydroid.softmanager.softmanager.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;

public class FrontPageAdapter extends FrontPageBaseAdapter<ItemInfo> {

    public FrontPageAdapter(Context context, List<? extends ItemInfo> data) {
        super(context, data);
    }

    @Override
    protected void bindView(int position, ViewHolder view, View convertView) {

    }

}
