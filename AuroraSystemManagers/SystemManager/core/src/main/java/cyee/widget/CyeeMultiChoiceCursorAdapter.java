/*
 * Copyright (C) 2013 Manuel Peinado
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cyee.widget;

import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;

import com.cyee.internal.widget.MultiChoiceScrollListener;

/**
 */
public abstract class CyeeMultiChoiceCursorAdapter extends CursorAdapter implements ActionMode.Callback,
        CyeeMultiChoiceAdapter, MultiChoiceScrollListener {

    private int mPos = 0;

    private final CyeeMultiChoiceAdapterHelper helper = new CyeeMultiChoiceAdapterHelper(this);

    public CyeeMultiChoiceCursorAdapter(Bundle savedInstanceState, Context context, Cursor cursor) {
        super(context, cursor);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeMultiChoiceCursorAdapter(Bundle savedInstanceState, Context context, Cursor cursor,
            boolean autoRequery) {
        super(context, cursor, autoRequery);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeMultiChoiceCursorAdapter(Bundle savedInstanceState, Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public void setAdapterView(AdapterView<? super BaseAdapter> adapterView) {
        helper.setAdapterView(adapterView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        helper.setOnItemClickListener(listener);
    }

    public void save(Bundle outState) {
        helper.save(outState);
    }

    public void setItemChecked(long itemId, boolean checked) {
        helper.setItemChecked(itemId, checked);
    }

    public Set<Long> getCheckedItems() {
        return helper.getCheckedItems();
    }

    public int getCheckedItemCount() {
        return helper.getCheckedItemCount();
    }

    public boolean isChecked(long itemId) {
        return helper.isChecked(itemId);
    }

    public void setItemChecked(int position, boolean checked) {
        helper.setItemChecked(position, checked);
    }

    protected Context getContext() {
        return helper.getContext();
    }

    public void finishActionMode() {
        helper.finishActionMode();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        helper.onDestroyActionMode();
    }
    
    @Override
    public int getMode() {
        // TODO Auto-generated method stub
        return CyeeMultiChoiceAdapter.MODE_LIST_VIEW;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        mPos = position;
        return super.getView(position, convertView, parent);
    }

    protected abstract View newViewImpl(Context cxt, Cursor cursor, ViewGroup parent);
    protected abstract void bindViewImpl(View view, Context cxt, Cursor cursor);

    @Override
    public final View newView(Context cxt, Cursor cursor, ViewGroup parent) {
        View viewWithoutSelection = newViewImpl(cxt, cursor, parent);

        Object holder = viewWithoutSelection.getTag();

        CyeeCheckBox checkboxView = (CyeeCheckBox) viewWithoutSelection
                .findViewById(android.R.id.checkbox);
        if (null == checkboxView) {
            viewWithoutSelection = helper
                    .addMultichoiceView(viewWithoutSelection, getMode());
        }

        if (null != holder) {
            viewWithoutSelection.setTag(holder);
        }

        return viewWithoutSelection;
    }

    @Override
    public final void bindView(View view, Context cxt, Cursor cursor) {
        bindViewImpl(view, cxt, cursor);
        helper.getView(mPos, view, getMode());
    }

    @Override
    public boolean isItemCheckable(int position) {
        return true;
    }

    @Override
    public void enterMultiChoiceMode() {
        helper.enterMultiChoiceMode();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        return helper.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean isEnterMultiChoice() {
        return helper.isActionModeStarted();
    }
    
    @Override
    public void setItemCheckedWithUpdate(long position, boolean checked) {
        helper.setItemCheckedWithUpdate(position, checked);
    }
    
    @Override
    public void updateActionModeMenu() {
        helper.updateActionMode();
    }
}
