package cyee.forcetouch;

import android.content.Context;

import com.android.internal.view.menu.MenuBuilder;

public class CyeeForceTouchMenu extends MenuBuilder {

    private static final String LOGTAG = "CyeeForceTouchMenu";
    private int mMenuSort;

    public CyeeForceTouchMenu(Context context) {
        super(context);
    }

    public void setMenuSort(int sort) {
        this.mMenuSort = sort;
    }

    public int getMenuSort() {
        return this.mMenuSort;
    }
}
