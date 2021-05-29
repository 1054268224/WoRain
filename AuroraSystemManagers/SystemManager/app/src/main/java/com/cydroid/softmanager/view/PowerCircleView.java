package com.cydroid.softmanager.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

public class PowerCircleView extends BaseView {

    private static final float START_ANGLE_CONSTANT = 270;
    private static final int SWEEP_ANGLE_CONSTANT = 360;
    private int mArcColor = Color.WHITE;

    public PowerCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(mArcColor);
        mPaint.setStrokeWidth(mInnerStrokeWidth);
        canvas.drawArc(mInnerRectF, START_ANGLE_CONSTANT + ratio, SWEEP_ANGLE_CONSTANT - ratio, false,
                mPaint);
    }
    
    //fengpeipei add for 49344 start
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        widthMeasureSpec = (int) (mCenterX * 2);
        heightMeasureSpec = (int) (mCenterY * 2);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
    //fengpeipei add for 49344 end
    public void setArcColor(int color) {
        mArcColor = color;
    }

}