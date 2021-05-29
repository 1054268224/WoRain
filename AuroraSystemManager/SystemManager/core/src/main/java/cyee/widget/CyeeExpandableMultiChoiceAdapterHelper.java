package cyee.widget;

import cyee.app.CyeeActivity;
import android.view.ActionMode;
import android.view.View;
import android.widget.BaseExpandableListAdapter;

public class CyeeExpandableMultiChoiceAdapterHelper extends
        CyeeExpandableMultiChoiceAdapterHelperBase {

    private ActionMode actionMode;

    public CyeeExpandableMultiChoiceAdapterHelper(
            BaseExpandableListAdapter mOwner) {
        super(mOwner);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void startActionMode(View customView) {
        CyeeActivity activity = (CyeeActivity) getContext();
        actionMode = activity.startActionMode((ActionMode.Callback) mOwner);
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
