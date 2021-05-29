package cyee.widget;

import java.util.HashSet;
import java.util.Set;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeExpandableListConnector.PositionMetadata;
import cyee.widget.CyeeExpandableListView.OnChildClickListener;
import cyee.widget.CyeeExpandableListView.OnGroupClickListener;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import com.cyee.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public abstract class CyeeExpandableMultiChoiceAdapterHelperBase implements
        OnItemLongClickListener, OnGroupClickListener, OnChildClickListener {

    protected static final String TAG = "CyeeExpandableMultiChoiceAdapterHelperBase";
    private static final String BUNDLE_KEY = "emca_selection";
    private static final String BUNDLE_ENTERPOS = "emca_enter_last_pos";
    private static final int MOVEX = 48; // dp
    private static final int MOVE_TIME = 250; // ms

    private final Set<Long> mCheckedItems = new HashSet<Long>();
    protected CyeeExpandableListView mAdapterView;
    protected BaseExpandableListAdapter mOwner;
    private OnGroupClickListener mGroupClickListener;
    private OnChildClickListener mChildClickListener;
    private CyeeTextView mTitleView;
    private CyeeButton mSelectAllBtn;
    private int mEnterLastPos = -1;
    private int mExitLastPos = -1;
    private boolean mNeedEnterAnimal = false;
    private boolean mNeedExitAnimal = false;

    protected abstract void setActionModeTitle(String title);

    protected abstract boolean isActionModeStarted();

    protected abstract void startActionMode(View customView);

    protected abstract void finishActionMode();

    protected abstract void clearActionMode();

    protected abstract void updateActionMode();

    public CyeeExpandableMultiChoiceAdapterHelperBase(
            BaseExpandableListAdapter mOwner) {
        this.mOwner = mOwner;
    }

    public void restoreSelectionFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        long[] array = savedInstanceState.getLongArray(BUNDLE_KEY);
        mCheckedItems.clear();
        if (array != null) {
            for (long id : array) {
                mCheckedItems.add(id);
            }
        }
        mNeedEnterAnimal = false;
        mNeedExitAnimal = false;
        mEnterLastPos = savedInstanceState.getInt(BUNDLE_ENTERPOS);
    }

    public Context getContext() {
        return this.mAdapterView.getContext();
    }

    public void setOnGroupClickListener(
            OnGroupClickListener onGroupClickListener) {
        this.mGroupClickListener = onGroupClickListener;
    }

    public void setOnChildClickListener(
            OnChildClickListener onChildClickListener) {
        this.mChildClickListener = onChildClickListener;
    }

    public void save(Bundle outState) {
        long[] array = new long[mCheckedItems.size()];
        int i = 0;
        for (Long id : mCheckedItems) {
            array[i++] = id;
        }
        outState.putLongArray(BUNDLE_KEY, array);
        outState.putInt(BUNDLE_ENTERPOS, mEnterLastPos);
    }

    public void setAdapterView(CyeeExpandableListView adapterView) {
        this.mAdapterView = adapterView;
        checkActivity();
        adapterView.setOnItemLongClickListener(this);
        adapterView.setOnChildClickListener(this);
        adapterView.setOnGroupClickListener(this);
        adapterView.setAdapter(mOwner);
        if (!mCheckedItems.isEmpty()) {
            View view = getActionModeCustomView();
            startActionMode(view);
            updateActionModeCustomView();
        }
    }

    public boolean isGroupChecked(int groupIndex) {
        int checkableCnt = getCheckableChildCount(groupIndex);
        int checkedCnt = getCheckedChildCount(groupIndex);
        return checkableCnt == checkedCnt;
    }

    public boolean isChildChecked(long combinedIndex) {
        return mCheckedItems.contains(combinedIndex);
    }

    public void setGroupChecked(int groupPosition, boolean checked) {
        Log.v(TAG, "groupPosition: " + groupPosition + "  checked: " + checked);
        if (checked) {
            checkGroup(groupPosition);
        } else {
            unCheckGroup(groupPosition);
        }
    }

    public void setChildChecked(long combinedIndex, boolean checked) {
        if (checked) {
            checkChild(combinedIndex);
        } else {
            unCheckChild(combinedIndex);
        }
    }

    public void checkChild(long combinedIndex) {
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        if (isChildChecked(combinedIndex)
                || !adapter.isChildCheckable(combinedIndex)
                || !adapter.isGroupCheckable(getGroupIndex(combinedIndex))) {
            return;
        }
        if (!isActionModeStarted()) {
            View view = getActionModeCustomView();
            startActionMode(view);
        }
        mCheckedItems.add(combinedIndex);
        mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    public void unCheckChild(long combinedIndex) {
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        if (!isChildChecked(combinedIndex)
                || !adapter.isChildCheckable(combinedIndex)
                || !adapter.isGroupCheckable(getGroupIndex(combinedIndex))) {
            return;
        }
        mCheckedItems.remove(combinedIndex);
        mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    public void checkGroup(int groupPosition) {
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        if (isGroupChecked(groupPosition)
                || !adapter.isGroupCheckable(groupPosition)) {
            return;
        }
        if (!isActionModeStarted()) {
            View view = getActionModeCustomView();
            startActionMode(view);
        }
        int childNum = mOwner.getChildrenCount(groupPosition);
        long combinedIndex = 0L;
        for (int i = 0; i < childNum; i++) {
            combinedIndex = getCombinedIndex(groupPosition, i);
            if (adapter.isChildCheckable(combinedIndex)) {
                mCheckedItems.add(combinedIndex);
            }
        }
        mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    public void unCheckGroup(int groupPosition) {
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        if (!isGroupChecked(groupPosition)
                || !adapter.isGroupCheckable(groupPosition)) {
            return;
        }
        int childNum = mOwner.getChildrenCount(groupPosition);
        long combinedIndex = 0L;
        for (int i = 0; i < childNum; i++) {
            if (adapter.isChildCheckable(combinedIndex)) {
                combinedIndex = getCombinedIndex(groupPosition, i);
                mCheckedItems.remove(combinedIndex);
            }
        }
        mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    public long getCombinedIndex(int groupIndex, int childIndex) {
        return (((long) groupIndex) << 32) | childIndex;
    }

    public int getGroupIndex(long combinedIndex) {
        return (int) (combinedIndex >> 32);
    }

    public int getChildIndex(long combinedIndex) {
        return (int) combinedIndex;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        // TODO Auto-generated method stub
        if (isActionModeStarted()) {
            return false;
        }
        mExitLastPos = -1;
        mEnterLastPos = mAdapterView.getLastVisiblePosition();
        CyeeExpandableListView listView = (CyeeExpandableListView) adapterView;
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        long packedPosition = listView.getExpandableListPosition(position);
        
        if (CyeeExpandableListView.getPackedPositionType(packedPosition) == CyeeExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            int groupPosition = CyeeExpandableListView.getPackedPositionGroup(packedPosition);
            if (!mAdapterView.isGroupExpanded(groupPosition)) {
                return true;
            }
            if (!adapter.isGroupCheckable(groupPosition)) {
                return false;
            }
            setGroupChecked(groupPosition,
                    !isGroupChecked(groupPosition));
        } else {
            long combinedIndex = getCombinedIndex(
                    CyeeExpandableListView.getPackedPositionGroup(packedPosition),
                    CyeeExpandableListView.getPackedPositionChild(packedPosition));
            if (!adapter.isChildCheckable(combinedIndex)) {
                return false;
            }
            setChildChecked(combinedIndex, !isChildChecked(combinedIndex));
        }
        return true;
    }

    @Override
    public boolean onGroupClick(CyeeExpandableListView parent, View view,
            int groupPosition, long id) {
        // TODO Auto-generated method stub
        if (isActionModeStarted()) {
            // if (mAdapterView.isGroupExpanded(groupPosition)) {
            // return doGroupClick(parent, view, groupPosition, id);
            // } else {
            // return false;
            // }

            mEnterLastPos = mAdapterView.getLastVisiblePosition();
            return false;
        }
        if (mGroupClickListener != null) {
            mGroupClickListener.onGroupClick(parent, view, groupPosition, id);
        }
        return false;
    }

    @Override
    public boolean onChildClick(CyeeExpandableListView parent, View view,
            int groupPosition, int childPosition, long id) {
        // TODO Auto-generated method stub
        if (isActionModeStarted()) {
            return doChildClick(parent, view, groupPosition, childPosition, id);
        }
        if (mChildClickListener != null) {
            mChildClickListener.onChildClick(parent, view, groupPosition,
                    childPosition, id);
        }
        return true;
    }

    public void enterMultiChoiceMode() {
        if (!isActionModeStarted()) {
            View view = getActionModeCustomView();
            startActionMode(view);
        }
        mOwner.notifyDataSetChanged();
        updateActionModeCustomView();
    }

    public void onDestroyActionMode() {
        mCheckedItems.clear();
        clearActionMode();
        mNeedExitAnimal = false;
        mNeedEnterAnimal = false;
        mEnterLastPos = -1;
        mExitLastPos = mAdapterView.getLastVisiblePosition();
        mOwner.notifyDataSetChanged();
    }

    public View getGroupView(final int groupPosition, boolean isExpanded,
            View convertView) {
        ViewGroup root = (ViewGroup) convertView;
        CyeeCheckBox checkboxView = (CyeeCheckBox) root
                .findViewById(android.R.id.checkbox);
        checkboxView.setChecked(isGroupChecked(groupPosition));
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        Log.v(TAG, "Group visibility: " + checkboxView.getVisibility() + " "
                + checkboxView.toString());
        if (isActionModeStarted()) {
            if (adapter.isGroupCheckable(groupPosition)) {
                if (checkboxView.getVisibility() == View.GONE) {
                    displayMultichoiceView(checkboxView, View.VISIBLE,
                            mNeedEnterAnimal);
                    CyeeExpandableListView listView = (CyeeExpandableListView) mAdapterView;
                    long packedPosition = listView.getExpandableListPosition(mEnterLastPos);
                    if (CyeeExpandableListView.getPackedPositionType(packedPosition) == CyeeExpandableListView.PACKED_POSITION_TYPE_GROUP
                            && CyeeExpandableListView.getPackedPositionGroup(packedPosition) == groupPosition) {
                        mNeedEnterAnimal = false;
                    }
                }
            } else {
                displayMultichoiceView(checkboxView, View.GONE, false);
            }
        } else {
            if (checkboxView.getVisibility() == View.VISIBLE) {
                displayMultichoiceView(checkboxView, View.GONE, mNeedExitAnimal);
                CyeeExpandableListView listView = (CyeeExpandableListView) mAdapterView;
                long packedPosition = listView.getExpandableListPosition(mExitLastPos);
                if (CyeeExpandableListView.getPackedPositionType(packedPosition) == CyeeExpandableListView.PACKED_POSITION_TYPE_GROUP
                        && CyeeExpandableListView.getPackedPositionGroup(packedPosition) == groupPosition) {
                    mNeedExitAnimal = false;
                }
            } else {
                displayMultichoiceView(checkboxView, View.GONE, false);
            }
        }
        checkboxView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                doGroupClick(groupPosition);
            }
        });
        return convertView;
    }

    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView) {
        ViewGroup root = (ViewGroup) convertView;
        CyeeCheckBox checkboxView = (CyeeCheckBox) root
                .findViewById(android.R.id.checkbox);
        long combinedIndex = getCombinedIndex(groupPosition, childPosition);
        checkboxView.setChecked(isChildChecked(combinedIndex));
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        Log.v(TAG, "Child visibility: " + checkboxView.getVisibility() + " "
                + checkboxView.toString());
        if (isActionModeStarted()) {
            if (adapter.isChildCheckable(combinedIndex)) {
                if (checkboxView.getVisibility() == View.GONE) {
                    displayMultichoiceView(checkboxView, View.VISIBLE,
                            mNeedEnterAnimal);
                    CyeeExpandableListView listView = (CyeeExpandableListView) mAdapterView;
                    long packedPosition = listView.getExpandableListPosition(mEnterLastPos);
                    if (CyeeExpandableListView.getPackedPositionType(packedPosition) == CyeeExpandableListView.PACKED_POSITION_TYPE_CHILD
                            && CyeeExpandableListView.getPackedPositionGroup(packedPosition) == groupPosition
                            && CyeeExpandableListView.getPackedPositionChild(packedPosition) == childPosition) {
                        mNeedEnterAnimal = false;
                    }
                }
            } else {
                displayMultichoiceView(checkboxView, View.GONE, false);
            }
        } else {
            if (checkboxView.getVisibility() == View.VISIBLE) {
                displayMultichoiceView(checkboxView, View.GONE, mNeedExitAnimal);
                CyeeExpandableListView listView = (CyeeExpandableListView) mAdapterView;
                long packedPosition = listView.getExpandableListPosition(mExitLastPos);
                if (CyeeExpandableListView.getPackedPositionType(packedPosition) == CyeeExpandableListView.PACKED_POSITION_TYPE_CHILD
                        && CyeeExpandableListView.getPackedPositionGroup(packedPosition) == groupPosition
                        && CyeeExpandableListView.getPackedPositionChild(packedPosition) == childPosition) {
                    mNeedExitAnimal = false;
                }
            } else {
                displayMultichoiceView(checkboxView, View.GONE, false);
            }
        }
        return convertView;
    }

    public View addMultichoiceView(View view, boolean isChildView) {
        if (null == view) {
            return null;
        }
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int containerId = com.cyee.internal.R.layout.cyee_expandable_multichoice_container;
        RelativeLayout multichoiceContainer = (RelativeLayout) inflater.inflate(
                containerId, null);
        FrameLayout frame = (FrameLayout) multichoiceContainer
                .findViewById(com.cyee.internal.R.id.cyee_customView);
        frame.addView(view);
        if (isChildView) {
            int VerticalPaddingId = com.cyee.internal.R.dimen.cyee_list_item_margin_top_bottom;
            frame.setPadding(frame.getPaddingStart(),
                    getContext().getResources().getDimensionPixelSize(VerticalPaddingId),
                    frame.getPaddingEnd(),
                    getContext().getResources().getDimensionPixelSize(VerticalPaddingId));
        }
        View dividerView = multichoiceContainer
                .findViewById(com.cyee.internal.R.id.cyee_expandable_divider);
        if (isChildView) {
            dividerView.setVisibility(View.VISIBLE);
        }else {
            dividerView.setVisibility(View.INVISIBLE);
        }
        
        return multichoiceContainer;
    }

    private void displayMultichoiceView(final View view, final int visible,
            boolean needAnimal) {
        if (null == view) {
            return;
        }

        if (!needAnimal) {
            view.setVisibility(visible);
            return;
        }

        if (visible == View.VISIBLE) {
            view.setVisibility(visible);
        }

        TranslateAnimation transAnim = null;
        int moveX = dip2px(getContext(), MOVEX);
        if (visible == View.VISIBLE) {
            transAnim = new TranslateAnimation(moveX, 0, 0, 0);
        } else {
            transAnim = new TranslateAnimation(0, moveX, 0, 0);
        }
        transAnim.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                if (visible == View.GONE) {
                    view.setVisibility(visible);
                }
            }
        });
        if (null != transAnim) {
            transAnim.setDuration(MOVE_TIME);
            view.startAnimation(transAnim);
        }
    }

    public void checkActivity() {
        Context context = getContext();
        if (context instanceof ListActivity) {
            throw new RuntimeException(
                    "ExpandableListView cannot belong to an activity which subclasses ListActivity");
        }
    }

    private View getActionModeCustomView() {
        View view;
        int defaultActionModeLayout = com.cyee.internal.R.layout.cyee_multichoice_select_action_mode_layout;
        view = LayoutInflater.from(getContext()).inflate(
                defaultActionModeLayout, null);
        mTitleView = (CyeeTextView) view.findViewById(com.cyee.internal.R.id.cyee_multichoice_selectedCount);
        mSelectAllBtn = (CyeeButton) view
                .findViewById(com.cyee.internal.R.id.cyee_multichoice_selectall);
        if (ChameleonColorManager.isNeedChangeColor(getContext())) {
            mSelectAllBtn.setTextColor(ChameleonColorManager
                    .getContentColorPrimaryOnAppbar_T1());
        }
        mSelectAllBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                doClickSelectAllBtn();
            }
        });

        return view;
    }

    private void updateActionModeCustomView() {
        if (null == mTitleView || null == mSelectAllBtn) {
            return;
        }
        mTitleView
                .setText(getContext().getResources().getString(
                        com.cyee.internal.R.string.cyee_multichoice_select_text,
                        mCheckedItems.size()));
        if (isAllGroupsChecked()) {
            mSelectAllBtn.setText(getContext().getResources().getString(
                    com.cyee.internal.R.string.cyee_multichoice_cancel_select_all));
        } else {
            mSelectAllBtn.setText(getContext().getResources().getString(
                    com.cyee.internal.R.string.cyee_multichoice_select_all));
        }
        updateActionMode();
    }

    private void doClickSelectAllBtn() {
        boolean isAllChecked = isAllGroupsChecked();
        int groupNum = mOwner.getGroupCount();
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        for (int i = 0; i < groupNum; i++) {
            if (adapter.isGroupCheckable(i)) {
                if (isAllChecked) {
                    unCheckGroup(i);
                } else {
                    checkGroup(i);
                }
            }
        }
    }

    private boolean isAllGroupsChecked() {
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        int groupNum = mOwner.getGroupCount();
        for (int i = 0; i < groupNum; i++) {
            if (adapter.isGroupCheckable(i) && !isGroupChecked(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean doGroupClick(int groupPosition) {
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        if (!adapter.isGroupCheckable(groupPosition)) {
            return false;
        }
        setGroupChecked(groupPosition, !isGroupChecked(groupPosition));
        return true;
    }

    private boolean doChildClick(CyeeExpandableListView parent, View view,
            int groupPosition, int childPosition, long id) {
        long combinedIndex = getCombinedIndex(groupPosition, childPosition);
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        if (!adapter.isChildCheckable(combinedIndex)) {
            return false;
        }
        setChildChecked(combinedIndex, !isChildChecked(combinedIndex));
        return true;
    }

    private int getCheckableChildCount(int groupPoistion) {
        int result = 0;
        int childNum = mOwner.getChildrenCount(groupPoistion);
        boolean isCheckable = false;
        CyeeExpandableMultiChoiceAdapter adapter = (CyeeExpandableMultiChoiceAdapter) mOwner;
        for (int i = 0; i < childNum; i++) {
            isCheckable = adapter.isChildCheckable(getCombinedIndex(
                    groupPoistion, i));
            if (isCheckable) {
                result++;
            }
        }
        return result;
    }

    public int getCheckedChildCount(int groupPosition) {
        int result = 0;

        for (Long id : mCheckedItems) {
            if (getGroupIndex(id) == groupPosition) {
                result++;
            }
        }
        return result;
    }

    public void getCheckedChildIndex(int groupPosition, Set<Integer> result) {
        if (result != null) {
            for (Long id : mCheckedItems) {
                if (getGroupIndex(id) == groupPosition) {
                    result.add(getChildIndex(id));
                }
            }
        }
    }

    public boolean hasItemSelected() {
        return !mCheckedItems.isEmpty();
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
