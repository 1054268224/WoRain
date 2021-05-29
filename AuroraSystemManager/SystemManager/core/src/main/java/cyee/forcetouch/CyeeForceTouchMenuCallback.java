package cyee.forcetouch;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public interface CyeeForceTouchMenuCallback {
    void onCreateForceTouchMenu(View view, Menu menu);

    void onPrepareForceTouchMenu(View view, Menu menu);

    void onForceTouchMenuItemClick(View view, MenuItem menuItem);
}
