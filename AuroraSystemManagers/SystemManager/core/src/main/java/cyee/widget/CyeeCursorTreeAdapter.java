package cyee.widget;

import java.util.Set;

import cyee.widget.CyeeExpandableListView.OnChildClickListener;
import cyee.widget.CyeeExpandableListView.OnGroupClickListener;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;

public abstract class CyeeCursorTreeAdapter extends CursorTreeAdapter
        implements ActionMode.Callback, CyeeExpandableMultiChoiceAdapter {

    private final CyeeExpandableMultiChoiceAdapterHelper helper = new CyeeExpandableMultiChoiceAdapterHelper(
            this);
    private int mGroupPosition;
    private long mChildPosition;

    public CyeeCursorTreeAdapter(Bundle savedInstanceState, Cursor cursor,
            Context context, boolean autoRequery) {
        super(cursor, context, autoRequery);
        // TODO Auto-generated constructor stub
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeCursorTreeAdapter(Bundle savedInstanceState, Cursor cursor,
            Context context) {
        super(cursor, context);
        // TODO Auto-generated constructor stub
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

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        mChildPosition = helper.getCombinedIndex(groupPosition, childPosition);
        return super.getChildView(groupPosition, childPosition, isLastChild,
                convertView, parent);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        mGroupPosition = groupPosition;
        return super.getGroupView(groupPosition, isExpanded, convertView,
                parent);
    }

    /**
     * Makes a new group view to hold the group data pointed to by cursor.
     * 
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The group cursor from which to get the data. The cursor is
     *            already moved to the correct position.
     * @param isExpanded
     *            Whether the group is expanded.
     * @param parent
     *            The parent to which the new view is attached to
     * @return The newly created view.
     */
    @Override
    public View newGroupView(Context context, Cursor cursor,
            boolean isExpanded, ViewGroup parent) {
        View customView = newGroupViewImpl(context, cursor, isExpanded, parent);
        Object holder = customView.getTag();
        CyeeCheckBox checkboxView = (CyeeCheckBox) customView
                .findViewById(android.R.id.checkbox);
        if (null == checkboxView) {
            customView = helper.addMultichoiceView(customView, false);
        }
        if (null != holder) {
            customView.setTag(holder);
        }

        return customView;
    }

    /**
     * Bind an existing view to the group data pointed to by cursor.
     * 
     * @param view
     *            Existing view, returned earlier by newGroupView.
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The cursor from which to get the data. The cursor is already
     *            moved to the correct position.
     * @param isExpanded
     *            Whether the group is expanded.
     */
    @Override
    public void bindGroupView(View view, Context context, Cursor cursor,
            boolean isExpanded) {
        bindGroupViewImpl(view, context, cursor, isExpanded);
        helper.getGroupView(mGroupPosition, isExpanded, view);
    }

    /**
     * Makes a new child view to hold the data pointed to by cursor.
     * 
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The cursor from which to get the data. The cursor is already
     *            moved to the correct position.
     * @param isLastChild
     *            Whether the child is the last child within its group.
     * @param parent
     *            The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newChildView(Context context, Cursor cursor,
            boolean isLastChild, ViewGroup parent) {
        View customView = newChildViewImpl(context, cursor, isLastChild, parent);
        Object holder = customView.getTag();
        CyeeCheckBox checkboxView = (CyeeCheckBox) customView
                .findViewById(android.R.id.checkbox);
        if (null == checkboxView) {
            customView = helper.addMultichoiceView(customView, true);
        }
        if (null != holder) {
            customView.setTag(holder);
        }

        return customView;
    }

    /**
     * Bind an existing view to the child data pointed to by cursor
     * 
     * @param view
     *            Existing view, returned earlier by newChildView
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The cursor from which to get the data. The cursor is already
     *            moved to the correct position.
     * @param isLastChild
     *            Whether the child is the last child within its group.
     */
    @Override
    public void bindChildView(View view, Context context, Cursor cursor,
            boolean isLastChild) {
        bindChildViewImpl(view, context, cursor, isLastChild);
        helper.getChildView(helper.getGroupIndex(mChildPosition),
                helper.getChildIndex(mChildPosition), isLastChild, view);
    }

    /**
     * Makes a new child view to hold the data pointed to by cursor.
     * 
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The cursor from which to get the data. The cursor is already
     *            moved to the correct position.
     * @param isLastChild
     *            Whether the child is the last child within its group.
     * @param parent
     *            The parent to which the new view is attached to
     * @return the newly created view.
     */
    protected abstract View newChildViewImpl(Context context, Cursor cursor,
            boolean isLastChild, ViewGroup parent);

    /**
     * Bind an existing view to the child data pointed to by cursor
     * 
     * @param view
     *            Existing view, returned earlier by newChildView
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The cursor from which to get the data. The cursor is already
     *            moved to the correct position.
     * @param isLastChild
     *            Whether the child is the last child within its group.
     */
    protected abstract void bindChildViewImpl(View view, Context context,
            Cursor cursor, boolean isLastChild);

    /**
     * Makes a new group view to hold the group data pointed to by cursor.
     * 
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The group cursor from which to get the data. The cursor is
     *            already moved to the correct position.
     * @param isExpanded
     *            Whether the group is expanded.
     * @param parent
     *            The parent to which the new view is attached to
     * @return The newly created view.
     */
    protected abstract View newGroupViewImpl(Context context, Cursor cursor,
            boolean isExpanded, ViewGroup parent);

    /**
     * Bind an existing view to the group data pointed to by cursor.
     * 
     * @param view
     *            Existing view, returned earlier by newGroupView.
     * @param context
     *            Interface to application's global information
     * @param cursor
     *            The cursor from which to get the data. The cursor is already
     *            moved to the correct position.
     * @param isExpanded
     *            Whether the group is expanded.
     */
    protected abstract void bindGroupViewImpl(View view, Context context,
            Cursor cursor, boolean isExpanded);
}