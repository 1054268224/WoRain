package com.cydroid.softmanager.softmanager.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.adapter.CompositeAdapter;
import com.cydroid.softmanager.model.ItemInfo;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftResolveInfo;
import com.cydroid.softmanager.utils.HelperUtils;

import java.util.List;

import cyee.widget.CyeeRadioButton;

public class DefaultSoftDetailAdapter extends CompositeAdapter<DefaultSoftResolveInfo> {
    private int mDefaultPos = -1;

    public DefaultSoftDetailAdapter(Context context, List<? extends ItemInfo> data) {
        super(context, data);
    }

    public void setDefaultPos(int pos) {
        mDefaultPos = pos;
    }

    public int getDefaultPos() {
        return mDefaultPos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.defaultsoft_detail_adapter_layout, parent, false);
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mRadioButton = (CyeeRadioButton) convertView.findViewById(R.id.radio);
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
        DefaultSoftResolveInfo resolveInfo = (DefaultSoftResolveInfo) mData.get(position);
        if (null == resolveInfo) {
            return;
        }

        final String packageName = resolveInfo.getPackageName();
        if (packageName.isEmpty()) {
            view.mIcon.setImageResource(R.drawable.icon_def_not_set);
        } else {
            // Chenyee xionghg 20171214 modify for SW17W16A-2504 begin
            // 只有微信是同步加载组件图标，其他继续使用异步加载应用图标
            ResolveInfo ri = resolveInfo.getResolveInfo();
            if (packageName.equals("com.tencent.mm") && ri != null) {
                view.mIcon.setImageDrawable(ri.loadIcon(mContext.getPackageManager()));
            } else {
                ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, packageName);
                if (null != info) {
                    setIconImage(view.mIcon, packageName, info);
                }
            }
            // Chenyee xionghg 20171214 modify for SW17W16A-2504 end
        }
        view.mTitle.setText(resolveInfo.getTitle());

        view.mRadioButton.setOnCheckedChangeListener(null);
        view.mRadioButton.setChecked(mDefaultPos == position);

        final int pos = position;
        view.mRadioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mDefaultPos = pos;
                notifyDataSetChanged();
            }
        });
    }
}
