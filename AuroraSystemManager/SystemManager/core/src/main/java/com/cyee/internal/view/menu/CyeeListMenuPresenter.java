package com.cyee.internal.view.menu;

import java.util.ArrayList;

import com.cyee.internal.R;
import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;


public class CyeeListMenuPresenter  implements CyeeMenuPresenter, AdapterView.OnItemClickListener {
    private static final String TAG = "CyeeListMenuPresenter";

    Context mContext;
    LayoutInflater mInflater;
    CyeeMenuBuilder mMenu;

    CyeeExpandedMenuView mMenuView;

    private int mItemIndexOffset;
    int mThemeRes;
    int mItemLayoutRes;

    private Callback mCallback;
    MenuAdapter mAdapter;

    private int mId;

    public static final String VIEWS_TAG = "android:menu:list";

    /**
     * Construct a new CyeeListMenuPresenter.
     * @param context Context to use for theming. This will supersede the context provided
     *                to initForMenu when this presenter is added.
     * @param itemLayoutRes Layout resource for individual item views.
     */
    public CyeeListMenuPresenter(Context context, int itemLayoutRes) {
        this(itemLayoutRes, 0);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    /**
     * Construct a new CyeeListMenuPresenter.
     * @param itemLayoutRes Layout resource for individual item views.
     * @param themeRes Resource ID of a theme to use for views.
     */
    public CyeeListMenuPresenter(int itemLayoutRes, int themeRes) {
        mItemLayoutRes = itemLayoutRes;
        mThemeRes = themeRes;
    }

    @Override
    public void initForMenu(Context context, CyeeMenuBuilder menu) {
        if (mThemeRes != 0) {
            mContext = new ContextThemeWrapper(context, mThemeRes);
            mInflater = LayoutInflater.from(mContext);
        } else if (mContext != null) {
            mContext = context;
            if (mInflater == null) {
                mInflater = LayoutInflater.from(mContext);
            }
        }
        mMenu = menu;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public CyeeMenuView getMenuView(ViewGroup root) {
        if (mMenuView == null) {
            mMenuView = (CyeeExpandedMenuView) mInflater.inflate(
                    com.cyee.internal.R.layout.cyee_expanded_menu_layout, root, false);
            if (mAdapter == null) {
                mAdapter = new MenuAdapter();
            }
            mMenuView.setAdapter(mAdapter);
            mMenuView.setOnItemClickListener(this);
        }
        return mMenuView;
    }

    /**
     * Call this instead of getMenuView if you want to manage your own ListView.
     * For proper operation, the ListView hosting this adapter should add
     * this presenter as an OnItemClickListener.
     *
     * @return A ListAdapter containing the items in the menu.
     */
    public ListAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new MenuAdapter();
        }
        return mAdapter;
    }

    @Override
    public void updateMenuView(boolean cleared) {
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    @Override
    public boolean onSubMenuSelected(CyeeSubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) return false;

        // The window manager will give us a token.
        new CyeeMenuDialogHelper(subMenu).show(null);
        if (mCallback != null) {
            mCallback.onOpenSubMenu(subMenu);
        }
        return true;
    }

    @Override
    public void onCloseMenu(CyeeMenuBuilder menu, boolean allMenusAreClosing) {
        if (mCallback != null) {
            mCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    int getItemIndexOffset() {
        return mItemIndexOffset;
    }

    public void setItemIndexOffset(int offset) {
        mItemIndexOffset = offset;
        if (mMenuView != null) {
            updateMenuView(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMenu.performItemAction(mAdapter.getItem(position), 0);
    }

    @Override
    public boolean flagActionItems() {
        return false;
    }

    public boolean expandItemActionView(CyeeMenuBuilder menu, CyeeMenuItemImpl item) {
        return false;
    }

    public boolean collapseItemActionView(CyeeMenuBuilder menu, CyeeMenuItemImpl item) {
        return false;
    }

    public void saveHierarchyState(Bundle outState) {
        SparseArray<Parcelable> viewStates = new SparseArray<Parcelable>();
        if (mMenuView != null) {
            ((View) mMenuView).saveHierarchyState(viewStates);
        }
        outState.putSparseParcelableArray(VIEWS_TAG, viewStates);
    }

    public void restoreHierarchyState(Bundle inState) {
        SparseArray<Parcelable> viewStates = inState.getSparseParcelableArray(VIEWS_TAG);
        if (viewStates != null) {
            ((View) mMenuView).restoreHierarchyState(viewStates);
        }
    }

    public void setId(int id) {
        mId = id;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (mMenuView == null) {
            return null;
        }

        Bundle state = new Bundle();
        saveHierarchyState(state);
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        restoreHierarchyState((Bundle) state);
    }

    private class MenuAdapter extends BaseAdapter {
        private int mExpandedIndex = -1;

        public MenuAdapter() {
            findExpandedIndex();
        }

        public int getCount() {
            ArrayList<CyeeMenuItemImpl> items = mMenu.getNonActionItems();
            int count = items.size() - mItemIndexOffset;
            if (mExpandedIndex < 0) {
                return count;
            }
            return count - 1;
        }

        public CyeeMenuItemImpl getItem(int position) {
            ArrayList<CyeeMenuItemImpl> items = mMenu.getNonActionItems();
            position += mItemIndexOffset;
            if (mExpandedIndex >= 0 && position >= mExpandedIndex) {
                position++;
            }
            return items.get(position);
        }

        public long getItemId(int position) {
            // Since a menu item's ID is optional, we'll use the position as an
            // ID for the item in the AdapterView
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(mItemLayoutRes, parent, false);
            }

            CyeeMenuView.ItemView itemView = (CyeeMenuView.ItemView) convertView;
            itemView.initialize(getItem(position), 0);
            return convertView;
        }

        void findExpandedIndex() {
            final CyeeMenuItemImpl expandedItem = mMenu.getExpandedItem();
            if (expandedItem != null) {
                final ArrayList<CyeeMenuItemImpl> items = mMenu.getNonActionItems();
                final int count = items.size();
                for (int i = 0; i < count; i++) {
                    final CyeeMenuItemImpl item = items.get(i);
                    if (item == expandedItem) {
                        mExpandedIndex = i;
                        return;
                    }
                }
            }
            mExpandedIndex = -1;
        }

        @Override
        public void notifyDataSetChanged() {
            findExpandedIndex();
            super.notifyDataSetChanged();
        }
    }
}
