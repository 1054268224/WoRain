
package cyee.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public abstract class AbsListIndexer extends View {

    public AbsListIndexer(Context context) {
        super(context);
    }

    public AbsListIndexer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsListIndexer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public abstract void setList(ListView listView, AbsListView.OnScrollListener scrollListener);

    public abstract void invalidateShowingLetterIndex();

    public abstract boolean isBusying();

    protected int toRawTextSize(float sp) {
        int rawTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, getResources().getDisplayMetrics());

        return rawTextSize;
    }
}
