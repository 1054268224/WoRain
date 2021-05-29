// Gionee: <houjie> <2015-10-27> add for CR01575153 begin
package com.cydroid.systemmanager.rubbishcleaner;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cydroid.softmanager.R;

import java.util.ArrayList;
import java.util.HashMap;

import cyee.widget.CyeeProgressBar;
import com.cydroid.systemmanager.rubbishcleaner.common.GroupHolder;
import com.cydroid.systemmanager.rubbishcleaner.common.GroupInfo;
import com.cydroid.systemmanager.rubbishcleaner.common.RubbishInfo;

//Gionee jiangsj 2017.05.09 add for 127722 begin
//Gionee jiangsj 2017.05.09 add for for 127722 end

public class MainCleanAdapter extends BaseExpandableListAdapter {
	//Gionee jiangsj 2017.05.09 add for 127722 begin
	private static final int DIRECTIONALITY_RIGHT_TO_LEFT=1;
	private int textLayoutDirection;
	//Gionee jiangsj 2017.05.09 add for 127722 end
	private Context mContext;
	private LayoutInflater mInflater;
	private OnSelectedListener mSelectedListener;
	private ArrayList<GroupInfo> mGroupList;
    private HashMap<Integer, ArrayList<RubbishInfo>> mGroupListMap =
            new HashMap<Integer, ArrayList<RubbishInfo>>();
    private int mCleanType;
    
	private static String TAG = "MainCleanAdapter";

    private static final int[] GROUP_NAME_INDEXS = {R.array.group_name_arrays, 
        R.array.deeplyclean_group_arrays};
    private static final int[] GROUP_NUMS = {4, 3};
    private static final boolean[] GROUP_NORMAL_CHECKED = {true, true, true, false};
    private static final boolean[] GROUP_DEEPLY_CHECKED = {false, false, false};
    private static final boolean[][] GROUP_CHECKED = {GROUP_NORMAL_CHECKED, GROUP_DEEPLY_CHECKED};

	public MainCleanAdapter(Context context, int cleanType) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
        mCleanType = cleanType;
	}

	public void initGroupInfo() {
		mGroupList = new ArrayList<GroupInfo>();

		String[] groupNames = null;
		try {
			groupNames = mContext.getResources()
                .getStringArray(GROUP_NAME_INDEXS[mCleanType]);
		} catch (NotFoundException e) {
			e.printStackTrace();
			return;
		}

		if (groupNames == null) {
			return;
		}
		//Gionee jiangsj 2017.05.09 add for 127722 begin
		  textLayoutDirection=TextUtils.getLayoutDirectionFromLocale(mContext.getResources().getConfiguration().locale);
		//Gionee jiangsj 2017.05.09 add for 127722 end

		for (int i = 0; i < GROUP_NUMS[mCleanType]; i++) {
			GroupInfo info = new GroupInfo();
			//Gionee jiangsj 2017.05.09 modify for 127722 begin
			if(DIRECTIONALITY_RIGHT_TO_LEFT == textLayoutDirection){
				info.name ="\u200F"+groupNames[i];
			}else {
				info.name = groupNames[i];
			}
			//Gionee jiangsj 2017.05.09 add for 127722 end
			info.isFinished = false;
            info.isChecked = GROUP_CHECKED[mCleanType][i];
			mGroupList.add(info);
		}
	}

	private boolean isGroupOutOfBound(int group) {
		return group < 0 || group >= GROUP_NUMS[mCleanType];
	}

	public void setSelectedListener(OnSelectedListener listener) {
		mSelectedListener = listener;
	}

	public void updateSelectedSize() {
		if (mSelectedListener != null) {
			mSelectedListener.updateSelectedSize();
		}
	}

	public boolean isAllGroupScanFinish() {
		if (mGroupList == null || mGroupList.isEmpty()) {
			return false;
		}

		for (GroupInfo gInfo : mGroupList) {
			if (!gInfo.isFinished) {
				return false;
			}
		}
		return true;
	}

	public void updateGroupData(int group, ArrayList<RubbishInfo> list) {
		if (isGroupOutOfBound(group)) {
			return;
		}

        mGroupListMap.put(Integer.valueOf(group), list);
        setGroupCheckedState(group, list);
		setGroupScanState(group, true);
		notifyDataSetChanged();
	}

	private void setGroupScanState(int group, boolean isFinish) {
		if (mGroupList == null && mGroupList.size() != GROUP_NUMS[mCleanType]) {
			return;
		}

		/* avoid out of bound */
		if (isGroupOutOfBound(group)) {
			return;
		}

		mGroupList.get(group).isFinished = isFinish;
	}

    private void setGroupCheckedState(int group, ArrayList<RubbishInfo> list) {
		if (mGroupList == null && mGroupList.size() != GROUP_NUMS[mCleanType]) {
			return;
		}

		/* avoid out of bound */
		if (isGroupOutOfBound(group)) {
			return;
		}

		mGroupList.get(group).isChecked = isGroupAllSelected(list);
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
		return 0;
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
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder holder = null;
		if (convertView == null) {
			holder = new GroupHolder();
			convertView = mInflater.inflate(R.layout.rubbish_main_clean_item_layout,
					parent, false);
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

    @Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

    @Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
	    return null;
    }

    @Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

    @Override
	public Object getChild(int groupPosition, int childPosition) {
	    return null;
    }

	private void initGroupView(final int groupPosition, boolean isExpanded,
			GroupHolder holder) {
		final CheckBox checkBox = holder.checkBox;
		GroupInfo info = mGroupList.get(groupPosition);
		holder.name.setText(info.name);
	    boolean isFinished = info.isFinished;
	    int childCnt = getChildSizeByGroup(groupPosition);

		if (isFinished && childCnt > 0) {
			holder.progressBar.setVisibility(View.GONE);
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

	private String convertSize2Str(long size) {
		return Formatter.formatShortFileSize(mContext, size);
	}

}
// Gionee: <houjie> <2015-10-27> add for CR01575153 end