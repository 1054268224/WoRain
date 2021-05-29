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

import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.cyee.internal.widget.MultiChoiceScrollListener;

public abstract class CyeeMultiChoiceArrayAdapter<T> extends ArrayAdapter<T> implements ActionMode.Callback,
        CyeeMultiChoiceAdapter, MultiChoiceScrollListener {
    private final CyeeMultiChoiceAdapterHelper helper = new CyeeMultiChoiceAdapterHelper(this);

    public CyeeMultiChoiceArrayAdapter(Bundle savedInstanceState, Context context, int resource,
            int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeMultiChoiceArrayAdapter(Bundle savedInstanceState, Context context, int resource,
            int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeMultiChoiceArrayAdapter(Bundle savedInstanceState, Context context, int resource,
            int textViewResourceId) {
        super(context, resource, textViewResourceId);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeMultiChoiceArrayAdapter(Bundle savedInstanceState, Context context, int textViewResourceId,
            List<T> objects) {
        super(context, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeMultiChoiceArrayAdapter(Bundle savedInstanceState, Context context, int textViewResourceId,
            T[] objects) {
        super(context, textViewResourceId, objects);
        helper.restoreSelectionFromSavedInstanceState(savedInstanceState);
    }

    public CyeeMultiChoiceArrayAdapter(Bundle savedInstanceState, Context context, int textViewResourceId) {
        super(context, textViewResourceId);
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

    public void setItemChecked(long position, boolean checked) {
        helper.setItemChecked(position, checked);
    }

    public Set<Long> getCheckedItems() {
        return helper.getCheckedItems();
    }

    public int getCheckedItemCount() {
        return helper.getCheckedItemCount();
    }

    public boolean isChecked(long position) {
        return helper.isChecked(position);
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
        View viewWithoutSelection = getViewImpl(position, convertView, parent);
        
        Object holder = viewWithoutSelection.getTag();
        
        CyeeCheckBox checkboxView = (CyeeCheckBox) viewWithoutSelection
                .findViewById(android.R.id.checkbox);
        if (null == checkboxView) {
            viewWithoutSelection = helper.addMultichoiceView(viewWithoutSelection, getMode());
        }
        
        if (null != holder) {
            viewWithoutSelection.setTag(holder);
        }
        
        return helper.getView(position, viewWithoutSelection, getMode());
    }

    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
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