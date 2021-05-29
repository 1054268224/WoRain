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
import com.cydroid.systemmanager.rubbishcleaner.RubbishCleanerDataManager.GroupKeyInfo;

import java.util.ArrayList;
import java.util.HashMap;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeProgressBar;
import com.cydroid.systemmanager.rubbishcleaner.common.ChildHolder;
import com.cydroid.systemmanager.rubbishcleaner.common.CleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.DeeplyCleanTypeConst;
import com.cydroid.systemmanager.rubbishcleaner.common.GroupHolder;
import com.cydroid.systemmanager.rubbishcleaner.common.GroupInfo;
import com.cydroid.systemmanager.rubbishcleaner.common.MsgConst;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;
import com.cydroid.systemmanager.rubbishcleaner.util.FileIconHelper;

public class DetailCleanExpandableListAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private OnSelectedListener mSelectedListener;

    private int mCleanType;
    private int mRubbishType;
    
	private ArrayList<GroupInfo> mGroupList = new ArrayList<GroupInfo>();
    private HashMap<Integer, ArrayList<RubbishInfo>> mGroupListMap =
            new HashMap<Integer, ArrayList<RubbishInfo>>();

	private static String TAG = "DetailCleanExpandableListAdapter";

	public DetailCleanExpandableListAdapter(Context context, int cleanType, int rubbishType) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
        mCleanType = cleanType;
        mRubbishType = rubbishType;
	}

	public void initGroupInfo(ArrayList<GroupKeyInfo> groupKeys) {
		if (groupKeys == null) {
			return;
		}

        mGroupList.clear();
		for (int i = 0; i < groupKeys.size(); i++) {
			GroupInfo info = new GroupInfo();
            info.icon = groupKeys.get(i).icon;
			info.name = groupKeys.get(i).name;
			info.isFinished = true;
			info.isChecked = false;
			mGroupList.add(info);
		}
	}
    
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

    public void updateGroupData(int group, ArrayList<RubbishInfo> list) {
        if (isGroupOutOfBound(group)) {
        	return;
        }

        // Chenyee xionghg 20180327 modify for CSW1705A-1667 begin
        ArrayList<RubbishInfo> newList = new ArrayList<>();
        if (list != null) {
            newList.addAll(list);
        }
        mGroupListMap.put(Integer.valueOf(group), newList);
        setGroupCheckedState(group, newList);
        // Chenyee xionghg 20180327 modify for CSW1705A-1667 end
        notifyDataSetChanged();
    }

    public void updateEmptyGroupData() {
        mGroupListMap.clear();
        mGroupList.clear();
        notifyDataSetChanged();
    }

    private void setGroupCheckedState(int group, ArrayList<RubbishInfo> list) {
		if (mGroupList == null) {
			return;
		}

		/* avoid out of bound */
		if (isGroupOutOfBound(group)) {
			return;
		}

		mGroupList.get(group).isChecked = isGroupAllSelected(list);
	}

    private ArrayList<RubbishInfo> getListByGroup(int group) {
		if (isGroupOutOfBound(group)) {
    		return null;
		}
		
		return mGroupListMap.get(Integer.valueOf(group));
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
		return getChildSizeByGroup(groupPosition);
	}

	@Override
	public Object getGroup(int groupPosition) {
		GroupInfo gInfo = null;
		if (mGroupList == null || isGroupOutOfBound(groupPosition)) {
			return null;
		}

		gInfo = mGroupList.get(groupPosition);
		return gInfo;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		RubbishInfo rInfo = null;
		ArrayList<RubbishInfo> list = getListByGroup(groupPosition);
		if (list == null) {
			return null;
		}

		try {
			rInfo = list.get(childPosition);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}

		return rInfo;
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
			convertView = mInflater.inflate(R.layout.rubbish_expandable_group_layout,
					parent, false);
            holder.icon = (ImageView) convertView
					.findViewById(R.id.group_icon);
			holder.name = (TextView) convertView
					.findViewById(R.id.rubbish_group_name);
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
		GroupInfo info = mGroupList.get(groupPosition);
		holder.name.setText(info.name);
	    boolean isFinished = info.isFinished;
	    int childCnt = getChildSizeByGroup(groupPosition);
        holder.icon.setBackground(info.icon);
		if (isExpanded) {
	         if (ChameleonColorManager.isNeedChangeColor() && childCnt > 0) {
	                holder.name.setTextColor(ChameleonColorManager
	                        .getAccentColor_G1());
	                holder.size.setTextColor(ChameleonColorManager
	                        .getAccentColor_G1());
	            }
		}

		if (isFinished && childCnt > 0) {
			holder.progressBar.setVisibility(View.GONE);
            holder.icon.setVisibility(View.VISIBLE);
			holder.size.setVisibility(View.VISIBLE);
			checkBox.setVisibility(View.VISIBLE);

			holder.size.setText(getGroupTotalSize(groupPosition));
			checkBox.setChecked(info.isChecked);
			checkBox.setOnClickListener(new CheckBox.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isChecked = checkBox.isChecked();
					mGroupList.get(groupPosition).isChecked = isChecked;
					setAllItemChecked(groupPosition, isChecked);
					updateSelectedSize();
					notifyDataSetChanged();
				}
			});
		} else if (isFinished && childCnt == 0) {
			holder.size.setVisibility(View.GONE);
			holder.checkBox.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.GONE);
		} else {
			holder.progressBar.setVisibility(View.VISIBLE);
			holder.size.setVisibility(View.GONE);
			holder.checkBox.setVisibility(View.GONE);
		}
	}

	private void setAllItemChecked(int groupPosition, boolean isChecked) {
		ArrayList<RubbishInfo> list = getListByGroup(groupPosition);
		if (list == null) {
			return;
		}

		for (RubbishInfo info : list) {
			info.isChecked = isChecked;
		}
	}

	private int getChildSizeByGroup(int groupPosition) {
		int count = 0;
		ArrayList<RubbishInfo> list = getListByGroup(groupPosition);
		if (list != null) {
			count = list.size();
		}

		return count;
	}

	public String getGroupTotalSize(int groupPostion) {
		long size = 0;
		ArrayList<RubbishInfo> list = getListByGroup(groupPostion);
		if (list != null) {
			// Gionee <xuwen> <2015-08-25> modify for CR01547240 begin
			RubbishInfo[] infos = new RubbishInfo[list.size()];
			infos = list.toArray(infos);
			for (RubbishInfo info : infos) {
                if (null != info) {
                    size += info.size;
                }
			}
			// Gionee <xuwen> <2015-08-25> modify for CR01547240 end
		}

		return convertSize2Str(size);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder holder = null;
		if (convertView == null) {
			holder = new ChildHolder();
			convertView = mInflater.inflate(R.layout.rubbish_child_layout,
					parent, false);
			holder.icon = (ImageView) convertView.findViewById(R.id.child_icon);
			holder.name = (TextView) convertView
					.findViewById(R.id.rubbish_child_name);
			holder.advise = (TextView) convertView
					.findViewById(R.id.rubbish_child_advice);
			holder.size = (TextView) convertView
					.findViewById(R.id.child_file_size);
			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.child_checkbox);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
			holder.icon.setBackground(null);
			holder.advise.setText(null);
		}

		initChildView(groupPosition, childPosition, holder);

		return convertView;
	}

	private void initChildView(final int groupPosition,
			final int childPosition, ChildHolder holder) {
		final ArrayList<RubbishInfo> list = getListByGroup(groupPosition);
		if (list == null) {
			return;
		}

		RubbishInfo info = null;
		try {
			info = list.get(childPosition);
		} catch (IndexOutOfBoundsException e) {
			return;
		}
		final CheckBox checkbox = holder.checkBox;
		//holder.icon.setBackground(getRubbishIcon(groupPosition, info));
		holder.name.setText(info.desc);
		holder.size.setText(convertSize2Str(info.size));
		if (mCleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL && mRubbishType == CleanTypeConst.APK) {
			if (info.isInstalled) {
				holder.advise.setText(R.string.apk_was_installed);
			} else {
				holder.advise.setText(R.string.apk_not_installed);
			}
		} else if (mCleanType == MsgConst.DATA_MANAGER_TYPE_NORMAL) {
			//holder.advise.setText(R.string.clean_advice);
		} else {
		    //holder.advise.setText(R.string.deeplyclean_item_advice);
		}
		checkbox.setChecked(info.isChecked);
		checkbox.setOnClickListener(new CheckBox.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean checked = checkbox.isChecked();
				try {
					list.get(childPosition).isChecked = checked;
					mGroupList.get(groupPosition).isChecked = isGroupAllSelected(list);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					return;
				}
				notifyDataSetChanged();
				updateSelectedSize();
			}
		});
	}

	private Drawable getRubbishIcon(int groupPosition, RubbishInfo info) {
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

	private boolean isGroupAllSelected(ArrayList<RubbishInfo> list) {
		boolean isAllSelected = true;
		if (list == null) {
			return false;
		}

		for (RubbishInfo info : list) {
			if (!info.isChecked) {
				return false;
			}
		}
		return isAllSelected;
	}

	private String convertSize2Str(long size) {
		return Formatter.formatShortFileSize(mContext, size);
	}

}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end