package cyee.forcetouch;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class CyeeForceTouchBackgroud {

    
    /**
     * 获得高斯模糊的图
     * @param context
     * @param bitmap 清晰的图
     * @param recycle
     * @return 高斯模糊的图
     */
    static Bitmap getBlurBitmap(Context context, Bitmap bitmap,
            boolean recycle) {
        if (bitmap == null) {
            return null;
        }
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        RenderScript rs = RenderScript.create(context.getApplicationContext());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs,
                Element.U8_4(rs));
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        blurScript.setRadius(25.f);
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        allOut.copyTo(outBitmap);
        if (recycle) {
            bitmap.recycle();
        }
        rs.destroy();
        return outBitmap;
    }
    
    /**
     * 复用bitmap
     * @param srcBmp 需要绘制的bitmap
     * @param reusedBmp 要复用的bitmap
     * @param isRecycle 是否需要释放srcBmp
     * @return
     */
    static Bitmap getReusedBitmap(Bitmap srcBmp, Bitmap reusedBmp,
            boolean isRecycle) {
        if (srcBmp != null) {
            if (reusedBmp == null) {
                reusedBmp = Bitmap.createBitmap(srcBmp.getWidth(),
                        srcBmp.getHeight(), srcBmp.getConfig());
            }
            Canvas canvas = new Canvas(reusedBmp);
            canvas.drawBitmap(srcBmp, new Matrix(), null);
            if (isRecycle) {
                srcBmp.recycle();
                srcBmp = null;
            }
            return reusedBmp;
        }
        return null;
    }
    
    static Bitmap getScreenShotBitmap(Context cxt) {
        Bitmap shotBmp = null;
        String FB0FILE1 = "/dev/graphics/fb0";
        File fbFile;
        DataInputStream dStream = null;
        FileInputStream buf = null;
        
        int screenWidth = CyeeForceTouchUtils.getScreenWidth(cxt);
        int screenHeight = CyeeForceTouchUtils.getScreenHeight(cxt);
        int[] colors = new int[screenHeight * screenWidth];
        WindowManager wm = (WindowManager) cxt
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int pixelformat = display.getPixelFormat();
        PixelFormat localPixelFormat1 = new PixelFormat();
        PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
        int deepth = localPixelFormat1.bytesPerPixel;// 位深
        byte[] piex = new byte[screenWidth * screenHeight * deepth];// 像素
        
        try {
            fbFile = new File(FB0FILE1);
            buf = new FileInputStream(fbFile);
            dStream = new DataInputStream(buf);
            dStream.readFully(piex);
            dStream.close();

            for (int i = 0; i < piex.length; i += 2) {
                colors[i / 2] = (int) 0xff000000
                        + (int) (((piex[i + 1]) << (16)) & 0x00f80000)
                        + (int) (((piex[i + 1]) << 13) & 0x0000e000)
                        + (int) (((piex[i]) << 5) & 0x00001A00)
                        + (int) (((piex[i]) << 3) & 0x000000f8);
            }

            return Bitmap.createBitmap(colors,screenWidth,screenHeight,Bitmap.Config.RGB_565);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (buf != null) {
                try {
                    buf.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return shotBmp;
    }
}
