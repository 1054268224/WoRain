package com.cydroid.softmanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.AttributeSet;

import com.cydroid.softmanager.R;

public class TrafficCircleView extends BaseView {

    private int mWarningRatio = -1;
    private String mWarningText = "";
    private float smallCircleRadius = 0;
    private int smallCircleTextSize = 0;
    private int aroundCircleTextSize = 0;
    private float mCircleTextRadius = 0;
    private float mScale = 0;
    
    // Gionee: mengdw <2016-11-15> add for CR01773139 begin
    private int mArcColor = Color.WHITE;
    // Gionee: mengdw <2016-11-15> add for CR01773139 end
    
    public TrafficCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateOuterCircleBgAlpha(0);
        initSize(context);
    }
    
    // Gionee: mengdw <2016-11-15> add for CR01773139 begin
    public void setArcColor(int color) {
        mArcColor = color;
    }
   // Gionee: mengdw <2016-11-15> add for CR01773139 end
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawArc(canvas, mPaint);

        drawTextAroundAcr(canvas, mPaint);

        drawIndicator(canvas, mPaint);

    }

    private void drawArc(Canvas canvas, Paint paint) {
        paint.setAlpha(255);
        paint.setColor(mArcColor);
        paint.setStrokeWidth(mInnerStrokeWidth);
        // canvas.drawArc(mInnerRectF, mInnerStartAngle, ratio, false, paint);
        canvas.drawArc(mInnerRectF, mInnerStartAngle, ratio == 0 ? 0.01f : ratio, false, paint);
    }

    private void drawTextAroundAcr(Canvas canvas, Paint paint) {
        canvas.translate(mCenterX, mCenterY);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(mInnerCircleAlpha);
        paint.setTextAlign(Align.CENTER);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextSize(aroundCircleTextSize);
        int count = 12; // text numbers, 0,10,20, ...
        float radiusX;
        float radiusY;
        for (int i = 1; i < count; i++) {
            radiusX = mCircleTextRadius;
            radiusY = mCircleTextRadius;
            if (i != count / 2) {
               // radiusX = radiusX + 10;
                radiusX = getRadiusX(radiusX);
            }

            if (i >= count / 4 && i <= 2 * count / 3) {
                //radiusY = radiusY + 15;
            	radiusY = getRadiusY(radiusY);
            }

            if (i == 1 || i == count - 1){
                radiusX = i == 1? radiusX - 8 : radiusX + 8;
                radiusY = radiusY + 8;
            }

            float x = (float) -(Math.sin(Math.PI * i / 6) * (radiusX));
            float y = (float) (Math.cos(Math.PI * i / 6) * (radiusY));

            canvas.drawText(String.valueOf((i - 1) * 10), x, y + 12, paint); // modify
                                                                             // text
                                                                             // position
        }
    }

    private float getRadiusX(float x){
		if (floatEqual(mScale, 4.0f)) {
			x += 25;
		} else if (floatEqual(mScale, 2.0f)) {
			x += 20;
		} else {
			x += 10;
		}
		return x;
    }
    
    private float getRadiusY(float y){
		if (floatEqual(mScale, 4.0f)) {
			y += 25;
		} else if (floatEqual(mScale, 2.0f)) {
			y += 20;
		} else {
			y += 15;
		}
		return y;
    }
    
    private void drawIndicator(Canvas canvas, Paint paint) {
        if (mWarningRatio == -1) {
            return;
        }

        int n = mWarningRatio;
        paint.setAlpha(255);
        float xStart = (float) ((mInnerRadius - mInnerStrokeWidth / 2) * Math.cos(Math.PI * (n - 80) / 60)); // -PI/3
                                                                                                             // +
                                                                                                             // PI
                                                                                                             // *
                                                                                                             // (n-60)
                                                                                                             // /
                                                                                                             // 60
        float yStart = (float) ((mInnerRadius - mInnerStrokeWidth / 2) * Math.sin(Math.PI * (n - 80) / 60));
        float xEnd = (float) ((mOuterRadius + mOuterStrokeWidth / 2) * Math.cos(Math.PI * (n - 80) / 60));
        float yEnd = (float) ((mOuterRadius + mOuterStrokeWidth / 2) * Math.sin(Math.PI * (n - 80) / 60));
        canvas.drawLine(xStart, yStart, xEnd, yEnd, paint);

        float radiusX = (float) ((mOuterRadius + mOuterStrokeWidth / 2 + smallCircleRadius + 2) * Math
                .cos(Math.PI * (n - 80) / 60));
        float radiusY = (float) ((mOuterRadius + mOuterStrokeWidth / 2 + smallCircleRadius + 2) * Math
                .sin(Math.PI * (n - 80) / 60));
        canvas.drawCircle(radiusX, radiusY, smallCircleRadius, paint);

        if (mWarningText.length() < 5) {
            paint.setTextSize(smallCircleTextSize);
        } else {
            paint.setTextSize(smallCircleTextSize - 3);
        }

        paint.setColor(Color.RED);
        canvas.drawText(mWarningText, radiusX, radiusY + 4, paint);

    }

    public void updateWarningRatio(int value) {
        mWarningRatio = value;
    }

    public void setWarningText(String str) {
        mWarningText = str;
    }

    private void initSize(Context context) {

        mScale = context.getResources().getDisplayMetrics().density;

        if (floatEqual(mScale, 1.5f)) {
            aroundCircleTextSize = 8;
            smallCircleRadius = 18;
            smallCircleTextSize = 13;
            mCircleTextRadius = mOuterRadius;
        } else if (floatEqual(mScale, 2.0f)) {
            aroundCircleTextSize = 20;
            smallCircleRadius = 25;
            smallCircleTextSize = 17;
            mCircleTextRadius = mOuterRadius;
        } else if (floatEqual(mScale, 3.0f)) {
            aroundCircleTextSize = 25;
            smallCircleRadius = 25;
            smallCircleTextSize = 17;
            mCircleTextRadius = mOuterRadius + 10;
        } else {
            aroundCircleTextSize = 25;
            smallCircleRadius = 30;
            smallCircleTextSize = 20;
            // Gionee: mengdw <2017-03-07> modify for 49865 begin
            mCircleTextRadius = mOuterRadius + 15;
            // Gionee: mengdw <2017-03-07> modify for 49865 end
        }

    }

    private boolean floatEqual(float num1, float num2) {
        return (num1 - num2 > -Float.MIN_VALUE) && (num1 - num2) < Float.MIN_VALUE;
    }
}
