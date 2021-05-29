package com.cydroid.softmanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.Log;
import android.view.WindowManager;

public class BaseView extends View {
    float mInnerRadius;
    float mOuterRadius;
    float mInnerStrokeWidth;
    float mOuterStrokeWidth;
    float mCenterX;
    float mCenterY;
    int mInnerCircleAlpha;
    int mInnerCircleBgAlpha;
    int mOuterCircleAlpha;
    int mOuterCircleBgAlpha = 51;
    float mInnerStartAngle;
    float mInnerSweepAngle;
    float mOuterStartAngle;
    float mOuterSweepAngle;
    // int mTophalfBackgroundColor = 0x4a8cd6;
    int mInnerCircleColor = Color.WHITE;
    // int mOuterCircleColor = Color.WHITE;
    int mOuterCircleColor = 0xeef2f7;
    int ratio;
    Paint mPaint;
    RectF mInnerRectF;
    RectF mOuterRectF;
    boolean mOuterCircelVibility = true;

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonCircleView);
        mInnerRadius = a.getDimension(R.styleable.CommonCircleView_innerRadius, 91.5f);
        mOuterRadius = a.getDimension(R.styleable.CommonCircleView_outerRadius, 108f);
        mInnerStrokeWidth = a.getDimension(R.styleable.CommonCircleView_innerStrokeWidth, 25f);
        mOuterStrokeWidth = a.getDimension(R.styleable.CommonCircleView_outerStrokeWidth, 2f);
        mInnerCircleAlpha = a.getInt(R.styleable.CommonCircleView_innerCircleAlpha, 51);
        mOuterCircleAlpha = a.getInt(R.styleable.CommonCircleView_outerCircleAlpha, 51);
        mInnerStartAngle = a.getFloat(R.styleable.CommonCircleView_innerStartAngle, 120);
        mInnerSweepAngle = a.getFloat(R.styleable.CommonCircleView_innerSweepAngle, 300);
        mOuterStartAngle = a.getFloat(R.styleable.CommonCircleView_outerStartAngle, 0);
        mOuterSweepAngle = a.getFloat(R.styleable.CommonCircleView_outerSweepAngle, 360);
        // Modify by HZH on 2019/5/9 for ui adapt start ^_^
//        mCenterX = a.getDimension(R.styleable.CommonCircleView_centerX, 180);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mCenterX = wm.getDefaultDisplay().getWidth() / 2;
        float xxx = wm.getDefaultDisplay().getWidth() / 3.5f;
        // Modify by HZH on 2019/5/9 for ui adapt end ^_^
        mCenterY = a.getDimension(R.styleable.CommonCircleView_centerY, 116);
        a.recycle();
        mPaint = new Paint();
        // Add by HZH on 2019/5/11 for ui adapt start ^_^
        mInnerRadius = xxx - 10;
        mOuterRadius = xxx;
        // Add by HZH on 2019/5/11 for ui adapt end ^_^
        mInnerRectF = new RectF(mCenterX - mInnerRadius, mCenterY - mInnerRadius, mCenterX + mInnerRadius,
                mCenterY + mInnerRadius);
        mOuterRectF = new RectF(mCenterX - mOuterRadius, mCenterY - mOuterRadius, mCenterX + mOuterRadius,
                mCenterY + mOuterRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setAntiAlias(true);
        // draw background inner circle
        // draw background inner circle
        drawInnerCircle(canvas, mPaint);

        // draw outside circle
        drawOuterCircle(canvas, mPaint);

        super.onDraw(canvas);
    }

    private void drawInnerCircle(Canvas canvas, Paint paint) {
        paint.setColor(mInnerCircleColor);
        paint.setAlpha(mInnerCircleBgAlpha);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mInnerStrokeWidth);
        canvas.drawArc(mInnerRectF, -450, 360, false, paint);

        paint.setColor(mInnerCircleColor);
        paint.setAlpha(mInnerCircleAlpha);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mInnerStrokeWidth);
        canvas.drawArc(mInnerRectF, mInnerStartAngle, mInnerSweepAngle, false, paint);
    }

    private void drawOuterCircle(Canvas canvas, Paint paint) {
        if (mOuterCircelVibility) {
            paint.setColor(mOuterCircleColor);
            paint.setAlpha(mOuterCircleBgAlpha);
            paint.setStrokeWidth(mOuterStrokeWidth);
            canvas.drawArc(mOuterRectF, -450, 360, false, paint);

            paint.setColor(mOuterCircleColor);
            paint.setAlpha(mOuterCircleAlpha);
            paint.setStrokeWidth(mOuterStrokeWidth);
            canvas.drawArc(mOuterRectF, mOuterStartAngle, mOuterSweepAngle, false, paint);
        }
    }

    public void updateInnerCircleAlpha(int alpha) {
        mInnerCircleAlpha = alpha;
    }

    public void updateInnerCircleBgAlpha(int alpha) {
        mInnerCircleBgAlpha = alpha;
    }

    public void updateOuterCircleBgAlpha(int alpha) {
        mOuterCircleBgAlpha = alpha;
    }

    public void updateOuterCircleVisibility(boolean visible) {
        mOuterCircelVibility = visible;
    }

    public void updateRatio(int ratio) {
        this.ratio = ratio;
        invalidate();
    }

    public void updateViews() {
        invalidate();
    }

    // Gionee <houjie> <2015-07-27> add for CR01522222 begin
    public void setInnerCircleColor(int color) {
        mInnerCircleColor = color;
    }

    public void setOuterCircleColor(int color) {
        mOuterCircleColor = color;
    }
    // Gionee <houjie> <2015-07-27> add for CR01522222 end
}
