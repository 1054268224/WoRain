package cyee.widget;

import java.util.Set;

import cyee.widget.CyeeExpandableListView.OnChildClickListener;
import cyee.widget.CyeeExpandableListView.OnGroupClickListener;
import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleExpandableListAdapter;

/**
 * Base class for a {@link ExpandableListAdapter} used to provide data and Views
 * from some data to an expandable list view.
 * <p>
 * Adapters inheriting this class should verify that the base implementations of
 * {@link #getCombinedChildId(long, long)} and {@link #getCombinedGroupId(long)}
 * are correct in generating unique IDs from the group/children IDs.
 * <p>
 * 
 * @see SimpleExpandableListAdapter
 * @see SimpleCursorTreeAdapter
 */
public abstract class CyeeExpandableMultiChoiceBaseAdapter extends
        BaseExpandableListAdapter implements ActionMode.Callback,
        CyeeExpandableMultiChoiceAdapter {
    private final CyeeExpandableMultiChoiceAdapterHelper helper = new CyeeExpandableMultiChoiceAdapterHelper(
            this);

    /**
     * @param savedInstanceState
     *            Pass your activity's saved instance state here. This is
     *            necessary for the adapter to retain its selection in the event
     *            of a configuration change
     */
    public CyeeExpandableMultiChoiceBaseAdapter(Bundle savedInstanceState) {
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    /**
     * Sets the adapter view on which this adapter will operate. You should call
     * this method from the onCreate method of your activity. This method calls
     * setAdapter on the adapter view, so you don't have to do it yourself
     * 
     * @param The
     *            adapter view (typically a ListView) this adapter will operate
     *            on
     */
    public void setAdapterView(CyeeExpandableListView adapterView) {
        helper.setAdapterView(adapterView);
    }

    public void setOnGroupClickListener(
            OnGroupClickListener onGroupClickListener) {
        helper.setOnGroupClickListener(onGroupClickListener);
    }

    public void setOnChildClickListener(
            OnChildClickListener onChildClickListener) {
        helper.setOnChildClickListener(onChildClickListener);
    }

    /**
     * Always call this method from your activity's onSaveInstanceState method.
     * This is necessary for the adapter to retain its selection in the event of
     * a configuration change
     * 
     * @param outState
     *            The same bundle you are passed in onSaveInstanceState
     */
    public void save(Bundle outState) {
        helper.save(outState);
    }

    public void setChildChecked(long position, boolean checked) {
        helper.setChildChecked(position, checked);
    }

    public void setGroupChecked(int position, boolean checked) {
        helper.setGroupChecked(position, checked);
    }

    public void getCheckedChildIndex(int groupPosition, Set<Integer> result) {
        helper.getCheckedChildIndex(groupPosition, result);
    }

    public int getCheckedChildCount(int groupPosition) {
        return helper.getCheckedChildCount(groupPosition);
    }

    public boolean isGroupChecked(int groupIndex) {
        return helper.isGroupChecked(groupIndex);
    }

    public boolean isChildChecked(long combinedIndex) {
        return helper.isChildChecked(combinedIndex);
    }

    /**
     * Gets a View that displays the given group. This View is only for the
     * group--the Views for the group's children will be fetched using
     * {@link #getChildView(int, int, boolean, View, ViewGroup)}.
     * 
     * @param groupPosition
     *            the position of the group for which the View is returned
     * @param isExpanded
     *            whether the group is expanded or collapsed
     * @param convertView
     *            the old view to reuse, if possible. You should check that this
     *            view is non-null and of an appropriate type before using. If
     *            it is not possible to convert this view to display the correct
     *            data, this method can create a new view. It is not guaranteed
     *            that the convertView will have been previously created by
     *            {@link #getGroupView(int, boolean, View, ViewGroup)}.
     * @param parent
     *            the parent that this view will eventually be attached to
     * @return the View corresponding to the group at the specified position
     */
    protected abstract View getGroupViewImpl(int groupPosition,
            boolean isExpanded, View convertView, ViewGroup parent);

    /**
     * Gets a View that displays the data for the given child within the given
     * group.
     * 
     * @param groupPosition
     *            the position of the group that contains the child
     * @param childPosition
     *            the position of the child (for which the View is returned)
     *            within the group
     * @param isLastChild
     *            Whether the child is the last child within the group
     * @param convertView
     *            the old view to reuse, if possible. You should check that this
     *            view is non-null and of an appropriate type before using. If
     *            it is not possible to convert this view to display the correct
     *            data, this method can create a new view. It is not guaranteed
     *            that the convertView will have been previously created by
     *            {@link #getChildView(int, int, boolean, View, ViewGroup)}.
     * @param parent
     *            the parent that this view will eventually be attached to
     * @return the View corresponding to the child at the specified position
     */
    protected abstract View getChildViewImpl(int groupPosition,
            int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent);

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        View customView = getGroupViewImpl(groupPosition, isExpanded,
                convertView, parent);
        Object holder = customView.getTag();
        CyeeCheckBox checkboxView = (CyeeCheckBox) customView
                .findViewById(android.R.id.checkbox);
        if (null == checkboxView) {
            customView = helper.addMultichoiceView(customView, false);
        }
        if (null != holder) {
            customView.setTag(holder);
        }
        return helper.getGroupView(groupPosition, isExpanded, customView);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        View customView = getChildViewImpl(groupPosition, childPosition,
                isLastChild, convertView, parent);
        Object holder = customView.getTag();
        CyeeCheckBox checkboxView = (CyeeCheckBox) customView
                .findViewById(android.R.id.checkbox);
        if (null == checkboxView) {
            customView = helper.addMultichoiceView(customView, true);
        }
        if (null != holder) {
            customView.setTag(holder);
        }
        return helper.getChildView(groupPosition, childPosition, isLastChild,
                customView);
    }

    /**
     * Subclasses can invoke this method in order to finish the action mode.
     * This has the side effect of unselecting all items
     */
    public void finishActionMode() {
        helper.finishActionMode();
    }

    /**
     * Convenience method for subclasses that need an activity context
     */
    protected Context getContext() {
        return helper.getContext();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        helper.onDestroyActionMode();
    }

    @Override
    public boolean isGroupCheckable(int position) {
        return true;
    }

    @Override
    public boolean isChildCheckable(long position) {
        return true;
    }

    public boolean hasItemSelected() {
        return helper.hasItemSelected();
    }

    @Override
    public void enterMultiChoiceMode() {
        helper.enterMultiChoiceMode();
    }
}
