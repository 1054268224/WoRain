package cyee.forcetouch;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;

public interface CyeeForceTouchControllerCallback {
    Bitmap getBlurBitmap();

    Bitmap getForceTouchView(View touchView, Point p, Point marginP);
}
