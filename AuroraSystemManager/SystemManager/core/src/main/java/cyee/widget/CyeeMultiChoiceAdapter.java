/*
 * Copyright (C) 2013 Gionee
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

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

public interface CyeeMultiChoiceAdapter {
    int MODE_LIST_VIEW = 0;
    int MODE_GRID_VIEW = 1;
    void setAdapterView(AdapterView<? super BaseAdapter> adapterView);
    void setOnItemClickListener(OnItemClickListener listener);
    void save(Bundle outState);
    void setItemChecked(long position, boolean checked);
    Set<Long> getCheckedItems();
    int getCheckedItemCount();
    boolean isChecked(long position);
    boolean isItemCheckable(int position);
    void enterMultiChoiceMode();
    /**
     * get the what is this adapter used for
     * @return the use of the this adapter
     */
    int getMode();
    
    boolean isEnterMultiChoice();
    
    void setItemCheckedWithUpdate(long position, boolean checked);
    void updateActionModeMenu();
}
