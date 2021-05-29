/*
 * Copyright Statement:
 *
 * Company: Gionee Communication Equipment Limited
 *
 * Author: Houjie
 *
 * Date: 2015-12-29
 */
package com.cydroid.softmanager.softmanager.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.common.AsyncAppIconLoader;
import com.cydroid.softmanager.common.AsyncAppIconLoader.ImageCallback;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.model.ViewHolder;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppInfo;
import com.cydroid.softmanager.softmanager.freeze.FreezeAppManager;
import com.cydroid.softmanager.utils.HelperUtils;

import java.lang.reflect.Array;
import java.util.List;

import cyee.app.CyeeAlertDialog;
import cyee.widget.CyeeButton;

public class FreezeAppAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "FreezeAppAdapter";

    private final Context mContext;
    private final LayoutInflater mInflater;
    private StateChangeCallback mCallback;
    private final FreezeAppManager mFreezeAppManager = FreezeAppManager.getInstance();
    private final AsyncAppIconLoader mAsyncAppIconLoader;

    private final String[] mFreezeAppTitles;
    private final List<FreezeAppInfo>[] mFreezeAppsGroup;

    public FreezeAppAdapter(Context context, StateChangeCallback callback,
                            List<FreezeAppInfo> freezeNormalApps, List<FreezeAppInfo> freezeCautiousApps) {
        mContext = context;
        mCallback = callback;
        mInflater = LayoutInflater.from(context);
        mFreezeAppsGroup = newArrayByClass(freezeNormalApps.getClass(), 2);
        mFreezeAppsGroup[0] = freezeNormalApps;
        mFreezeAppsGroup[1] = freezeCautiousApps;
        mFreezeAppTitles = mContext.getResources().
                getStringArray(R.array.listitem_freeze_app_summary);
        mAsyncAppIconLoader = AsyncAppIconLoader.getInstance();
    }

    public void setStateChangeCallback(StateChangeCallback callback) {
        mCallback = callback;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mFreezeAppsGroup[groupPosition].get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.freezeapp_adapter_layout, parent, false);
            view = new ViewHolder();
            view.mIcon = (ImageView) convertView.findViewById(R.id.icon);
            view.mTitle = (TextView) convertView.findViewById(R.id.title);
            view.mButton = (CyeeButton) convertView.findViewById(R.id.btn);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }
        view.mButton.setTag(childPosition);
        view.mButton.setId(groupPosition);
        view.mButton.setText(R.string.text_freeze_btn);

        final FreezeAppInfo freezeAppInfo = mFreezeAppsGroup[groupPosition].get(childPosition);
        ApplicationInfo info = HelperUtils.getApplicationInfo(mContext, freezeAppInfo.getPackageName());
        if (null != info) {
            setIconImage(view.mIcon, freezeAppInfo.getPackageName(), info);
        }
        view.mTitle.setText(freezeAppInfo.getTitle());

        view.mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int groupIndex = v.getId();
                if (0 == groupIndex) {
                    String packageName = getPackageName(v, groupIndex);
                    if (!TextUtils.isEmpty(packageName)) {
                        mFreezeAppManager.freezeApp(packageName);
                    }
                    mCallback.onStateChange();
                } else {
                    createDialog((Integer) v.getTag());
                }
            }
        });
        return convertView;
    }

    private void setIconImage(final ImageView icon, final String packageName,
                              final ApplicationInfo applicationInfo) {
        icon.setTag(packageName);
        Drawable cachedImage = mAsyncAppIconLoader.loadAppIconDrawable(mContext, packageName,
                new ImageCallback() {
                    public void imageLoaded(Drawable imageDrawable, String pkgName) {
                        if (!pkgName.equals(icon.getTag())) {
                            return;
                        }
                        if (null != imageDrawable) {
                            icon.setImageDrawable(imageDrawable);
                        } else {
                            icon.setImageDrawable(HelperUtils.loadIcon(mContext, applicationInfo));
                        }
                    }
                });
        if (null != cachedImage) {
            icon.setImageDrawable(cachedImage);
        }
    }

    private String getPackageName(View v, int groupPosition) {
        int pos = (Integer) v.getTag();
        if (mFreezeAppsGroup[groupPosition].size() <= pos) {
            return "";
        }
        FreezeAppInfo info = mFreezeAppsGroup[groupPosition].get(pos);
        return info.getPackageName();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mFreezeAppsGroup[groupPosition].size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mFreezeAppTitles[groupPosition];
    }

    @Override
    public int getGroupCount() {
        return mFreezeAppTitles.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.softmanager_permission_adapter_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String title = getChildrenCount(groupPosition) + " " + getGroup(groupPosition).toString();
        viewHolder.mTitle.setText(title);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private void createDialog(final int position) {
        Resources resources = mContext.getResources();
        CyeeAlertDialog permDialog = new CyeeAlertDialog.Builder(mContext,
                CyeeAlertDialog.THEME_CYEE_FULLSCREEN).create();
        //permDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        //        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        permDialog.setTitle(resources.getString(R.string.freeze_title));
        String message = resources.getString(R.string.freeze_message);
        message += "\n" + "\n" + resources.getString(R.string.freeze_note);
        permDialog.setMessage(message);

        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CyeeAlertDialog.BUTTON_POSITIVE:
                        if (position >= mFreezeAppsGroup[1].size()) {
                            return;
                        }
                        String packageName = mFreezeAppsGroup[1].get(position).getPackageName();
                        if (null != packageName && !packageName.isEmpty()) {
                            mFreezeAppManager.freezeApp(packageName);
                        }
                        mCallback.onStateChange();
                        break;
                    default:
                        break;
                }
            }

        };

        permDialog.setButton(CyeeAlertDialog.BUTTON_POSITIVE, resources.getString(R.string.freeze_continue),
                dialogClickLsn);
        permDialog.setButton(CyeeAlertDialog.BUTTON_NEGATIVE, resources.getString(R.string.freeze_cancel),
                dialogClickLsn);

        permDialog.show();

        // Add by zhiheng.huang on 2020/4/3 for  start
        permDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.dialog_ripple);
        permDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.dialog_ripple);
        // Add by zhiheng.huang on 2020/4/3 for  end
    }

    private static <T> T[] newArrayByClass(Class<T> clazz, int length) {
        return (T[]) Array.newInstance(clazz, length);
    }
}
