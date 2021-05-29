package cyee.widget.blur;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Process;
import com.cyee.utils.Log;
import android.view.Surface;
import android.view.View;

/**
 * 主要是提供模糊图片的一些基本方法，目前模糊算法仅仅支持高斯算法
 * 
 * @author chenng
 * @version 1.0
 * @since Special Effect lib 1.0
 */
public final class CyeeBlur {

    private static final String DBG_TAG = "GioneeBlur";
    private static final boolean DEBUG = true;
    private static final int CORE_POOL_SIZE = 3;
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int KEEP_ALIVE = 1;
    private static final int TYPE_BITMAP = 0;
    private static final int TYPE_DRAWABLE = 1;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            
            Thread resultThread = new Thread(r, "GioneeBlur #" + mCount.getAndIncrement());
            
            return resultThread;
        }
    };

    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    public static native final void blurIntArray(int[] pImg, int w, int h, int r);

    public static native final void blurBitMap(Bitmap bitmap, int r);
    static {
        System.loadLibrary("cy_ImageBlur");
        //System.loadLibrary("gnspecialeffect");
        //System.loadLibrary("gionee_blur");
    }

    private CyeeBlur() {
    }

    private static class GioneeBlurHolder {
        private static final CyeeBlur sInstance = new CyeeBlur();
    }

    private Bitmap scaleBitmap(Bitmap srcBitmap, int dstWidth, int dstHeight) {
        Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Matrix matrix = new Matrix();
        final float sx = dstWidth  / (float)srcBitmap.getWidth();
        final float sy = dstHeight / (float)srcBitmap.getHeight();
        matrix.setScale(sx, sy);
        canvas.drawBitmap(srcBitmap, matrix, new Paint());
        return result;
    }
    
    private Bitmap process(Bitmap srcBitmap, int blurRatio) {
        final int bmpWidth = srcBitmap.getWidth();
        final int bmpHeight = srcBitmap.getHeight();
        if (DEBUG) {
            Log.d(DBG_TAG, "bmpWidth = " + bmpWidth + ",bmpHeight = " + bmpHeight);
        }
        
        /*final int processW = bmpWidth / 4;
        final int processH = bmpHeight / 4;*/
  /*      final int processW = bmpWidth;
        final int processH = bmpHeight;*/

        //Bitmap temp = Bitmap.createScaledBitmap(srcBitmap, processW, processH, false);
        //Bitmap temp = scaleBitmap(srcBitmap, processW, processH);
        
        //int[] srcData = new int[bmpWidth * bmpHeight];
        //int[] dstData = new int[bmpWidth * bmpHeight];
        //srcBitmap.getPixels(srcData, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);

        if (DEBUG) {
            Log.d(DBG_TAG, "nativeProcessBitmap start");
            Log.d(DBG_TAG, "blurRatio = " + blurRatio);
            //Log.d(DBG_TAG, "before nativeProcessBitmap srcData[1] = " + srcData[1]);
        }

        //Bitmap resultBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Config.ARGB_8888);
        //Gionee <chenng> <2013-07-02> modify for CR00832714 start
        //Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap);
        // Gionee <huming><2013-12-02> modify for CR00956828 begin
        Bitmap resultBitmap = null;
        try {
            resultBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError error) {
            Log.d(DBG_TAG, "blur bitmap OutOfMemory. ");
            return null;
        }
        // Gionee <huming><2013-12-02> modify for CR00956828 end
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, new Paint());
        //Gionee <chenng> <2013-07-02> modify for CR00832714 end
        long startTime = System.currentTimeMillis();
        //srcData = nativeProcessBitmap(srcData, 24, bmpWidth, bmpHeight, blurRatio);
        //nativeProcessBitmap(resultBitmap, 24, bmpWidth, bmpHeight, blurRatio);
        blurBitMap(resultBitmap, blurRatio);
        //new Blur().blur(resultBitmap, srcData, 24, bmpWidth, bmpHeight, blurRatio);

        if (DEBUG) {
            Log.d(DBG_TAG, "elapse time = " + (System.currentTimeMillis() - startTime));
        }
       /* if (DEBUG) {
            Log.d(DBG_TAG, "after nativeProcessBitmap srcData[1] = " + srcData[1]);
        }*/
        //resultBitmap.setPixels(srcData, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);
//        resultBitmap = Bitmap.createScaledBitmap(resultBitmap, bmpWidth, bmpHeight, false);
        //temp = scaleBitmap(temp, bmpWidth, bmpHeight);
        return resultBitmap;
    }

    private void execute(final Bitmap srcBitmap, final Resources res, final int blurRatio,
            final BlurCallback callback) {
        THREAD_POOL_EXECUTOR.execute(new Runnable() {

            @Override
            public void run() {
                Process.setThreadPriority(-19);
                if (DEBUG) {
                    Log.d(DBG_TAG, "execute method!");
                    Log.d(DBG_TAG, "Thread name = " + Thread.currentThread().getName());
                }
                Bitmap resultBitmap = process(srcBitmap, blurRatio);
                // Gionee <huming><2013-12-02> modify for CR00956828 begin
                if (callback == null || null == resultBitmap) {
                    return;
                }
                // Gionee <huming><2013-12-02> modify for CR00956828 begin
                Log.d(DBG_TAG, "mListener = " + (callback instanceof DrawableCallback));
                if (callback instanceof DrawableCallback) {
                    callback.onComplete(new BitmapDrawable(res, resultBitmap));
                } else {
                    callback.onComplete(resultBitmap);
                }
            }
        });
    }

    private void checkArguments(final Object obj, final Resources res, final int blurRatio,
            BlurCallback callback) {
        checkArguments(res, blurRatio, callback);
        if (obj == null) {
            throw new IllegalArgumentException("The argument is null!");
        }
    }

    private void checkArguments(final Resources res, final int blurRatio, BlurCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The BlurCallback is null!");
        }
        if (res == null) {
            throw new IllegalArgumentException("The Resources is null!");
        }
        if (blurRatio <= 0 || blurRatio >= 6) {
            throw new IllegalStateException("The blurRatio must greater than 0 and less than 6");
        }
    }

    /**
     * 获取金立模糊类的对象，该对象是个单实例
     * 
     * @return 金立模糊算法的工具类对象
     */
    public static CyeeBlur getInstance() {
        return GioneeBlurHolder.sInstance;
    }

    static class BlurType {

        public static final int CV_BLUR_NO_SCALE = 0;
        public static final int CV_BLUR = 1;
        public static final int CV_GAUSSIAN = 2;
        public static final int CV_MEDIAN = 3;
        public static final int CV_BILATERAL = 4;
        /*
         * CV_BLUR_NO_SCALE =0, CV_BLUR =1, CV_GAUSSIAN =2, CV_MEDIAN =3,
         * CV_BILATERAL =4
         */
    }

    interface BlurCallback {
        void onComplete(Bitmap completeBmp);

        void onComplete(Drawable completeDrawable);
    }

    /**
     * 以Bitmap形式返回模糊后的图片
     */
    public static abstract class BitmapCallback implements BlurCallback {

        /* (non-Javadoc)
         * @see com.gionee.widget.blur.GioneeBlur.BlurCallback#onComplete(android.graphics.drawable.Drawable)
         */
        @Override
        public void onComplete(Drawable completeDrawable) {
        }
    }

    /**
     * 以Drawable形式返回模糊后的图片
     */
    public static abstract class DrawableCallback implements BlurCallback {

        /* (non-Javadoc)
         * @see com.gionee.widget.blur.GioneeBlur.BlurCallback#onComplete(android.graphics.Bitmap)
         */
        @Override
        public void onComplete(Bitmap completeBmp) {
        }
    }

    private static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int temp = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            temp = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(temp);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 得到整个屏幕的截图
     * 
     * @param screenWidth
     *            屏幕的宽度
     * @param screenHeight
     *            屏幕的高度
     * @param deleteStatusBar
     *            是否删除状态栏部分
     * @return 以Bitmap的形式返回截图
     */
    @SuppressWarnings("unchecked")
    public static Bitmap getScreenshot(int screenWidth, int screenHeight, Context context,
            boolean deleteStatusBar) {
        if (DEBUG) {
            Log.d(DBG_TAG, "screenWidth = " + screenWidth + ",screenHeight = " + screenHeight);
        }
        if (screenHeight <= 0 || screenWidth <= 0) {
            throw new IllegalStateException("The screenHeight and screenWidth must greater than 0!");
        }

        try {
            Class<Surface> surface = (Class<Surface>) Class.forName("android.view.Surface");
            Method method = surface.getMethod("screenshot", int.class, int.class);
            int statusBarHeight = getStatusBarHeight(context);
            if (DEBUG) {
                Log.d(DBG_TAG, "statusBarHeight = " + statusBarHeight);
            }
            Bitmap sBitmap = (Bitmap) method.invoke(surface, new Object[] {screenWidth, screenHeight});
            if (deleteStatusBar) {
                return Bitmap.createBitmap(sBitmap, 0, statusBarHeight, screenWidth, screenHeight
                        - statusBarHeight);
            }
            return sBitmap;
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到整个屏幕的截图
     * 
     * @param screenWidth
     *            屏幕的宽度
     * @param screenHeight
     *            屏幕的高度
     * @return 以Drawable的形式返回截图
     */
    @SuppressWarnings("unchecked")
    public static Drawable getScreenshot(int screenWidth, int screenHeight, Resources res, Context ctx,
            boolean deleteStatusBar) {
        if (res == null) {
            throw new IllegalStateException("The res object is null");
        }
        Bitmap bitmap = getScreenshot(screenWidth, screenHeight, ctx, deleteStatusBar);
        return new BitmapDrawable(res, bitmap);
    }

    /**
     * 模糊一个bitmap的图片，并且以bitmap的格式返回
     * 
     * @param srcBitmap
     *            处理前的bitmap，不能为空
     * @param res
     *            Resources对象，不能为空
     * @param blurRatio
     *            模糊效果的参数，必须大于0小于6，该值越大结果越模糊
     * @param callback
     *            处理完后的回调函数，会把处理的结果已bitmap的形式返回，该回调是在线程里面执行的，不能在回调里面直接更新界面
     */
    public void generateBlurBitmap(Bitmap srcBitmap, Resources res, int blurRatio, BitmapCallback callback) {
        checkArguments(srcBitmap, res, blurRatio, callback);
        execute(srcBitmap, res, blurRatio, callback);
    }

    /**
     * @see com.gionee.CyeeBlur.blur.GioneeBlur#generateBlurBitmap(Bitmap srcBitmap, Resources res, int
     *      blurRatio, BitmapCallback callback)
     */
    public void generateBlurBitmap(Drawable srcDrawable, Resources res, int blurRatio, BitmapCallback callback) {
        checkArguments(srcDrawable, res, blurRatio, callback);
        BitmapDrawable bmpBitmapDrawable = (BitmapDrawable) srcDrawable;
        generateBlurBitmap(bmpBitmapDrawable.getBitmap(), res, blurRatio, callback);
    }

    /**
     * @param resId
     *            模糊源文件的资源id
     * @see com.gionee.CyeeBlur.blur.GioneeBlur#generateBlurBitmap(Bitmap srcBitmap, Resources res, int
     *      blurRatio, BitmapCallback callback)
     */
    public void generateBlurBitmap(int resId, Resources res, int blurRatio, BitmapCallback callback) {
        checkArguments(res, blurRatio, callback);
        Drawable drawable = res.getDrawable(resId);
        generateBlurBitmap(drawable, res, blurRatio, callback);
    }

    /**
     * @param view
     *            模糊源View，会根据view的cache作为原图来作模糊处理
     * @see com.gionee.CyeeBlur.blur.GioneeBlur#generateBlurBitmap(Bitmap srcBitmap, Resources res, int
     *      blurRatio, BitmapCallback callback)
     */
    public void generateBlurBitmap(View view, Resources res, int blurRatio, BitmapCallback callback) {
        checkArguments(view, res, blurRatio, callback);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBitmap = view.getDrawingCache();
        generateBlurBitmap(cacheBitmap, res, blurRatio, callback);
    }

    /**
     * 模糊一个bitmap的图片，并且以Drawable的格式返回
     * 
     * @param srcBitmap
     *            处理前的bitmap，不能为空
     * @param res
     *            Resources对象，不能为空
     * @param blurRatio
     *            模糊效果的参数，必须大于0小于200，该值越大结果越模糊，同时也越耗时，建议取50左右
     * @param callback
     *            处理完后的回调函数，会把处理的结果已Drawable的形式返回，该回调是在线程里面执行的，不能在回调里面直接更新界面
     */
    public void generateBlurDrawable(Bitmap srcBitmap, Resources res, int blurRatio, DrawableCallback callback) {

        checkArguments(srcBitmap, res, blurRatio, callback);
        final int bmpWidth = srcBitmap.getWidth();
        final int bmpHeight = srcBitmap.getHeight();
        if (DEBUG) {
            Log.d(DBG_TAG, "generateBlurDrawable bmpWidth = " + bmpWidth + ",bmpHeight = " + bmpHeight);
        }
        execute(srcBitmap, res, blurRatio, callback);
    }

    /**
     * @param resId
     *            模糊源文件的资源id
     * @see com.gionee.CyeeBlur.blur.GioneeBlur#generateBlurDrawable(Bitmap srcBitmap, Resources res, int
     *      blurRatio, DrawableCallback callback)
     */
    public void generateBlurDrawable(int resId, Resources res, int blurRatio, DrawableCallback callback) {
        checkArguments(res, blurRatio, callback);
        Drawable drawable = res.getDrawable(resId);
        generateBlurDrawable(drawable, res, blurRatio, callback);
    }

    /**
     * @see com.gionee.CyeeBlur.blur.GioneeBlur#generateBlurDrawable(Bitmap srcBitmap, Resources res, int
     *      blurRatio, DrawableCallback callback)
     */
    public void generateBlurDrawable(Drawable srcDrawable, Resources res, int blurRatio,
            DrawableCallback callback) {
        checkArguments(srcDrawable, res, blurRatio, callback);
        BitmapDrawable bmpBitmapDrawable = (BitmapDrawable) srcDrawable;
        generateBlurDrawable(bmpBitmapDrawable.getBitmap(), res, blurRatio, callback);
    }

    /**
     * @param view
     *            模糊源View，会根据view 的cache来模糊
     * @see com.gionee.CyeeBlur.blur.GioneeBlur#generateBlurDrawable(Bitmap srcBitmap, Resources res, int
     *      blurRatio, DrawableCallback callback)
     */
    public void generateBlurDrawable(View view, Resources res, int blurRatio, DrawableCallback callback) {
        checkArguments(view, res, blurRatio, callback);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBitmap = view.getDrawingCache();
        generateBlurDrawable(cacheBitmap, res, blurRatio, callback);
    }

    //private native final int[] nativeProcessBitmap(int[] srcData, int bitOfPic, int width, int height, int blurRatio);
    public native final void nativeProcessBitmap(Bitmap bitmap, int[] srcData, int bitOfPic, int width, int height, int blurRatio);
    public native final void nativeProcessBitmap(Bitmap bitmap, int bitOfPic, int width, int height, int blurRatio);
    //private native final void nativeProcessBitmap(Bitmap bitmap, int[] srcData, int bitOfPic, int width, int height, int blurRatio);
}

