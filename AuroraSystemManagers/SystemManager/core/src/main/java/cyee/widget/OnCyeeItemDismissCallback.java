package cyee.widget;

import android.widget.AbsListView;
import android.widget.ListView;

public interface OnCyeeItemDismissCallback {
    /**
     * Called when the user has indicated they she would like to dismiss one or
     * more list item positions.
     * 
     * @param listView
     *            The originating {@link ListView}.
     * @param reverseSortedPositions
     *            An array of positions to dismiss, sorted in descending order
     *            for convenience.     */
    void onDismiss(AbsListView listView, int[] reverseSortedPositions);
}
