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

import cyee.app.CyeeActivity;
import com.cyee.utils.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.BaseAdapter;

public class CyeeMultiChoiceAdapterHelper extends CyeeMultiChoiceAdapterHelperBase {
    private ActionMode actionMode;

    protected CyeeMultiChoiceAdapterHelper(BaseAdapter owner) {
        super(owner);
    }

    @Override
    protected void startActionMode(View customView) {
        CyeeActivity activity = (CyeeActivity) getContext();
        actionMode = activity.startActionMode((ActionMode.Callback) mOwner);
        mIsInActionModePre = mIsInActionModeNow;
        mIsInActionModeNow = true;
        mIsMoved = false;
        if (null != customView) {
            actionMode.setCustomView(customView);
        }
    }

    @Override
    protected void finishActionMode() {
        if (null != actionMode) {
            actionMode.finish();
        }
    }

    @Override
    protected void updateActionMode() {
        if (null != actionMode) {
            actionMode.invalidate();
        }
    }

    @Override
    protected void setActionModeTitle(String title) {
        if (null != actionMode) {
            actionMode.setTitle(title);
        }
    }

    @Override
    protected boolean isActionModeStarted() {
        return actionMode != null;
    }

    @Override
    protected void clearActionMode() {
        actionMode = null;
    }
}
