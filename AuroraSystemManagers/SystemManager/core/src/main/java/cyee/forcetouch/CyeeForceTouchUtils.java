package cyee.forcetouch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.cyee.utils.Log;

import java.lang.reflect.Method;

public class CyeeForceTouchUtils {

    private static final String LOGTAG = "CyeeForceTouchUtils";

    public static int getScreenWidth(Context cxt) {
        int width = 0;

        WindowManager wm = (WindowManager) cxt
                .getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();

        return width;
    }

    public static int getScreenHeight(Context cxt) {
        int height = 0;

        WindowManager wm = (WindowManager) cxt
                .getSystemService(Context.WINDOW_SERVICE);
        height = wm.getDefaultDisplay().getHeight();

        return height;
    }

    public static Point measureView(View view) {
        Point point = new Point();
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        if(view == null) {
            Log.e(LOGTAG,"measureView, view = null");
            return point;
        }
         
        view.measure(w, h);
        point.y = view.getMeasuredHeight();
        point.x = view.getMeasuredWidth();
        Log.e(LOGTAG, "measureView point.y=" + point.y + ";point.x=" + point.x);
        return point;
    }

    public static Bitmap getCurrentScreenShot(Context cxt) {
        Bitmap screenShot = null;
        if (cxt instanceof AppCompatActivity) {
            AppCompatActivity a = ((AppCompatActivity) cxt);
            View view = a.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmap = view.getDrawingCache();
            Rect rect = new Rect();
            a.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

            int width = a.getWindowManager().getDefaultDisplay().getWidth();
            int height = a.getWindowManager().getDefaultDisplay().getHeight();
            screenShot = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            view.setDrawingCacheEnabled(false);
            view.destroyDrawingCache();
            if (null != bitmap) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        return screenShot;
    }

    public static Bitmap getViewBitmap(View view) {
        Bitmap bitmap, tmpBitmap;
        
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        tmpBitmap = view.getDrawingCache();
        bitmap = Bitmap.createBitmap(tmpBitmap);
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        if (null != tmpBitmap) {
            tmpBitmap.recycle();
            tmpBitmap = null;
        }

        return bitmap;
    }

    public static void invokeVibrate(Context context, String effectName,
            long[] pattern, int repeat) {
        Vibrator mVibrator = (Vibrator) context
                .getSystemService(Context.VIBRATOR_SERVICE);
        Method method = null;
        try {
            method = mVibrator.getClass().getMethod("cyeeVibrate",
                    String.class, long[].class, int.class);
            method.invoke(mVibrator, effectName, pattern, repeat);
        } catch (Exception e) {
            Log.e(LOGTAG, "failed caurse : " + e.toString());
        }
    }

    public static Rect getViewRect(View view) {
        Rect rect = new Rect();
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        rect.left = location[0];
        rect.top = location[1];
        rect.bottom = rect.top + view.getHeight();
        rect.right = rect.left + view.getWidth();

        return rect;
    }

    public static Drawable getRoundDrawable(Drawable oldDrawable, float roundPx) {
        if (null == oldDrawable) {
            return null;
        }
        Bitmap bitmap = drawableToBitmap(oldDrawable);
        Bitmap newBitmap = getRoundBitmap(bitmap, roundPx);

        return new BitmapDrawable(newBitmap);
    }

    /* 将Drawable转化为Bitmap */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (null == drawable) {
            return null;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;

    }

    // 获得圆角图片的方法
    public static Bitmap getRoundBitmap(Bitmap bitmap, float roundPx) {

        if (null == bitmap) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();

        return output;
    }

    public static boolean isFullScreen(Context cxt) {
        boolean ret = false;

        if (cxt instanceof AppCompatActivity) {
            AppCompatActivity a = ((AppCompatActivity) cxt);
            WindowManager.LayoutParams attrs = a.getWindow().getAttributes();
            if ((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                ret = true;
            }
        }

        return ret;
    }

    public static void hideStatusBar(Context cxt, boolean hide) {
        if (cxt instanceof AppCompatActivity) {
            Log.d(LOGTAG, "hideStatusBar hide=" + hide);
            AppCompatActivity a = ((AppCompatActivity) cxt);
            if (hide) {
                a.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                a.getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }
    
    
    
    /**
     * dp转化为像素工具类
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 像素转化为dp工具类
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    
    public static boolean isClickViewOnGlobalScreen(View view, MotionEvent ev,
            boolean isGlobal) {
        boolean ret = false;
        float x = ev.getX();
        float y = ev.getY();

        if (isGlobal) {
            x = ev.getRawX();
            y = ev.getRawY();
        }

        if ((x < view.getX()) || (x >= view.getWidth() + view.getX())
                || (y < view.getY()) || (y >= view.getHeight() + view.getY())) {
            return ret;
        }

        return true;
    }
}
