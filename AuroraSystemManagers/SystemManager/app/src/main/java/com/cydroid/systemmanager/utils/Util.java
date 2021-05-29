package com.cydroid.systemmanager.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

/**
 * chenyee zhaocaili 20180508 add for CSW1707A-845 begin
 */
public class Util {
    private final static String TAG = "Util";
    public static void unbindDrawables(View view) {
        try {
            if (view == null) {
                Log.w("Util", "unbindDrawables view == null");
                return;
            }

            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Drawable drawable = imageView.getDrawable();
                if (drawable != null && drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null) {
                        // bitmap.recycle();
                        bitmap = null;
                    }
                    drawable = null;
                }
                drawable = imageView.getBackground();
                if (drawable != null && drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null) {
                        // bitmap.recycle();
                        bitmap = null;
                    }
                    drawable = null;
                }
                imageView.setImageDrawable(null);
                imageView.setBackgroundDrawable(null);
            }

            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
                view.setBackgroundResource(0);
            }

            if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        } catch (Exception e) {
            Log.e("Util", "unbindDrawables exception --->", e);
        }
    }
}
