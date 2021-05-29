package com.cydroid.softmanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.cydroid.softmanager.R;

public class CommonCircleView extends View {
    float mCenterX;
    float mCenterY;
    // float mInnerCircleRadius;
    float mOuterCircleRadius;
    // float mInnerStrokeWidth;
    float mOuterStrokeWidth;
    int mOuterCircleAlpha;
    // int mTophalfBackgroundColor = 0x4a8cd6;
    // float mStrokeWidth;
    Paint mPaint;
    // int mInnerBgColor = Color.WHITE;
    int mOuterBgColor = Color.WHITE;
    int mDectorColor;
    int mRatio;
    RectF mRectF;

    public CommonCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonCircleView);
        mCenterX = a.getDimension(R.styleable.CommonCircleView_circleCenterX, 106);
        mCenterY = a.getDimension(R.styleable.CommonCircleView_circleCenterY, 92);
        // mInnerCircleRadius =
        // a.getDimension(R.styleable.CommonCircleView_innerCircleRadius, 65);
        mOuterCircleRadius = a.getDimension(R.styleable.CommonCircleView_outerCircleRadius, 74);
        // mInnerStrokeWidth =
        // a.getDimension(R.styleable.CommonCircleView_innerStrokeWidth, 2);
        mOuterStrokeWidth = a.getDimension(R.styleable.CommonCircleView_outerStrokeWidth, 18);
        mOuterCircleAlpha = a.getInt(R.styleable.CommonCircleView_outerCircleAlpha, 51);
        // mStrokeWidth =
        // a.getDimension(R.styleable.CommonCircleView_strokeWidth, 18);

        a.recycle();
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(mOuterBgColor);
        mPaint.setAlpha(mOuterCircleAlpha);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(mOuterStrokeWidth);
        canvas.drawCircle(mCenterX, mCenterY, mOuterCircleRadius, mPaint);

        /*
         * mPaint.setAntiAlias(true); mPaint.setColor(mOuterBgColor);
         * mPaint.setStyle(Paint.Style.FILL); canvas.drawCircle(mCenterX,
         * mCenterY, mOuterCircleRadius, mPaint);
         * 
         * mPaint.setAlpha(51); mPaint.setColor(mInnerBgColor);
         * mPaint.setStyle(Paint.Style.STROKE);
         * mPaint.setStrokeWidth(mStrokeWidth); canvas.drawCircle(mCenterX,
         * mCenterY, mInnerCircleRadius, mPaint);
         */
    }

    public void updateView(int ratio) {
        mRatio = ratio;
        invalidate();
    }

}