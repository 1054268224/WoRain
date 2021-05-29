package com.cydroid.softmanager.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-2-1 Change List:
 */
public class GNToast {

    private static Toast sToast;

    public static void showToast(Context context, String text) {
        // Gionee xionghg 2017-06-06 modify for memory leak begin
        if (sToast == null) {
            // sToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            sToast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        } else {
            sToast.cancel();
            sToast = null;
            // sToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            sToast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        // Gionee xionghg 2017-06-06 modify for memory leak end
        sToast.show();
    }

}
