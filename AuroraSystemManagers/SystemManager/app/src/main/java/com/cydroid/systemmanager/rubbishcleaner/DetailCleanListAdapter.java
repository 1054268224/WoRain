// Gionee: <houjie> <2015-10-27> add for CR01575153 begin
package com.cydroid.systemmanager.rubbishcleaner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cydroid.softmanager.R;

import java.util.ArrayList;

import cyee.widget.CyeeProgressBar;
import com.cydroid.systemmanager.rubbishcleaner.common.CleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.DeeplyCleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.GroupHolder;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;
import com.cydroid.systemmanager.rubbishcleaner.util.FileIconHelper;

public class DetailCleanListAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private OnSelectedListener mSelectedListener;

    private int mCleanType;
    private int mRubbishType;
    
	private ArrayList<RubbishInfo> mGroupList = new ArrayList<>();

	private static String TAG = "DetailCleanExpandableListAdapter";

	public DetailCleanListAdapter(Context context, int cleanType, int rubbishType) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
        mCleanType = cleanType;
        mRubbishType = rubbishType;
	}

    // Chenyee xionghg 20180327 modify for CSW1705A-1667 begin
    public void initGroupInfo(ArrayList<RubbishInfo> groupList) {
        // mGroupList = groupList;
        mGroupList.clear();
        if (groupList != null) {
            mGroupList.addAll(groupList);
        }
        notifyDataSetChanged();
    }
    // Chenyee xionghg 20180327 modify for CSW1705A-1667 end

	private boolean isGroupOutOfBound(int group) {
		return group < 0 || group >= mGroupList.size();
	}

	public void setSelectedListener(OnSelectedListener listener) {
		mSelectedListener = listener;
	}

	public void updateSelectedSize() {
		if (mSelectedListener != null) {
			mSelectedListener.updateSelectedSize();
		}
	}

    public void updateGroupData(int group) {
        if (isGroupOutOfBound(group)) {
        	return;
        }

        notifyDataSetChanged();
    }

	@Override
	public int getGroupCount() {
		if (mGroupList == null) {
			return 0;
		}
		return mGroupList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		RubbishInfo gInfo = null;
		if (mGroupList == null || isGroupOutOfBound(groupPosition)) {
			return null;
		}

		gInfo = mGroupList.get(groupPosition);
		return gInfo;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder holder = null;
		if (convertView == null) {
			holder = new GroupHolder();
			convertView = mInflater.inflate(R.layout.rubbish_group_layout,
					parent, false);
            holder.icon = (ImageView) convertView
					.findViewById(R.id.group_icon);
			holder.name = (TextView) convertView
					.findViewById(R.id.rubbish_group_name);
            holder.advise = (TextView) convertView
					.findViewById(R.id.rubbish_group_advise);
			holder.size = (TextView) convertView
					.findViewById(R.id.group_rubbish_size);
			holder.progressBar = (CyeeProgressBar) convertView
					.findViewById(R.id.group_progressbar);
			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.group_checkbox);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}

		initGroupView(groupPosition, isExpanded, holder);

		return convertView;
	}

	private void initGroupView(final int groupPosition, boolean isExpanded,
			GroupHolder holder) {
		final CheckBox checkBox = holder.checkBox;
		RubbishInfo info = mGroupList.get(groupPosition);
		holder.name.setText(info.name);

        if (mCleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL && mRubbishType == CleanTypeConst.APK) {
            holder.advise.setVisibility(View.VISIBLE);
            if (info.isInstalled) {
				holder.advise.setText(R.string.apk_was_installed);
			} else {
				holder.advise.setText(R.string.apk_not_installed);
			}
        }
        
        holder.icon.setBackground(getRubbishIcon(info));
        
		holder.progressBar.setVisibility(View.GONE);
        holder.icon.setVisibility(View.VISIBLE);
		holder.size.setVisibility(View.VISIBLE);
		checkBox.setVisibility(View.VISIBLE);

		holder.size.setText(convertSize2Str(info.size));
		checkBox.setChecked(info.isChecked);
		checkBox.setOnClickListener(new CheckBox.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isChecked = checkBox.isChecked();
				mGroupList.get(groupPosition).isChecked = isChecked;
				updateSelectedSize();
				notifyDataSetChanged();
			}
		});		
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		return null;
	}

	private Drawable getRubbishIcon(RubbishInfo info) {
		if (info == null) {
			return null;
		}

		Drawable icon = null;

        if (mCleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL) {
    		switch (mRubbishType) {
    		case CleanTypeConst.CACHE:
    		case CleanTypeConst.APK:
    			icon = info.icon;
    			break;
    		case CleanTypeConst.AD:
    			icon = mContext.getDrawable(R.drawable.rubbish_file_icon);
    			break;
    		case CleanTypeConst.RESIDUAL:
    			icon = mContext.getDrawable(R.drawable.mime_apk);
    			break;
    		}
        } else if (mCleanType == MsgConst.DATA_MANAGER_TYPE_DEEPLY) {
            switch (mRubbishType) {
    		case DeeplyCleanTypeConst.CACHE:
    			icon = info.icon;
    			break;
    		case DeeplyCleanTypeConst.RESIDUAL:
    			icon = mContext.getDrawable(R.drawable.mime_apk);
    			break;
    		case DeeplyCleanTypeConst.BIGFILE:
    			icon = mContext.getDrawable(FileIconHelper
    					.getFileIconByFiletype(info.type));
    			break;
    		}
        }

		return icon;
	}

	private String convertSize2Str(long size) {
		return Formatter.formatShortFileSize(mContext, size);
	}

}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end