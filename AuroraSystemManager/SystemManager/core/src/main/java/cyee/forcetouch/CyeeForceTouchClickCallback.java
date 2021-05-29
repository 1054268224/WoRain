package cyee.forcetouch;

import android.view.View;

public interface CyeeForceTouchClickCallback {
    boolean onLightTouchClick(View view, float pressure);

    boolean onForceTouchClick(View view);

    void onForceTouchClickView(View view);
}
