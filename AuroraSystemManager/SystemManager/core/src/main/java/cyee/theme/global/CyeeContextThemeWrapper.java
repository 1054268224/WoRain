package cyee.theme.global;

import cyee.app.CyeeActivity;
import android.annotation.StyleRes;
import android.content.Context;
import android.content.res.Resources;
import com.cyee.utils.Log;
import android.view.ContextThemeWrapper;

public class CyeeContextThemeWrapper extends ContextThemeWrapper {

    private final static String TAG = "CyeeContextThemeWrapper";

    private CyeeResources mCyeeResources;

    public CyeeContextThemeWrapper(Context base, @StyleRes int themeResId) {
        super(base, themeResId);
    }

    public Resources getSuperResources() {

        Context cxt = getBaseContext();
        if (cxt instanceof CyeeActivity) {
            return ((CyeeActivity) cxt).getSuperResources();
        }
        return super.getResources();
    }

    @Override
    public Resources getResources() {
        Context cxt = getBaseContext();

        if (cxt instanceof CyeeActivity) {
            Resources res = ((CyeeActivity) cxt).getResources();
            Resources resSuper = ((CyeeActivity) cxt).getSuperResources();
            return resSuper;
        }
        return super.getResources();
    }
}
