package com.odm.tool.heavyvertical;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.odm.tool.R;

public class PlumbPointView extends View {

    private Bitmap mPlumbPointBitmap;
    private double mDegree;

    public PlumbPointView(Context context) {
        this(context,null);
    }

    public PlumbPointView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PlumbPointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        BitmapDrawable drawble = (BitmapDrawable) context.getResources().getDrawable(R.drawable.plumb_point,null);
        mPlumbPointBitmap = drawble.getBitmap();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        double w = mPlumbPointBitmap.getWidth();
        double h = mPlumbPointBitmap.getHeight();

        Rect rect = new Rect();
        getDrawingRect(rect);

        canvas.rotate((int)mDegree, rect.width() / 2, 0);
        //add bug:TELYL-210 wushanfei 20200407 start
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        //add bug:TELYL-210 wushanfei 20200407 end
        canvas.drawBitmap(mPlumbPointBitmap, (float) ((rect.width() - w) / 2),0,null);
    }

    public void setDegree(double degree) {
        mDegree = degree;
        invalidate();
    }
}
