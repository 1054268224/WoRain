package com.cydroid.softmanager.view;

import cyee.changecolors.ChameleonColorManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.cydroid.softmanager.R;

public class SoftManagerCircleView extends BaseView {
    private final int left;
    private final int right;

    public SoftManagerCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        left = (int) (mCenterX - UnitUtil.dip2px(context, 4));
        right = (int) (mCenterX + UnitUtil.dip2px(context, 4));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonCircleView);
        mInnerCircleColor = Color.parseColor(a.getString(R.styleable.CommonCircleView_innerCircleColor));
        mInnerCircleAlpha = 255;
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Paint paint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        if (ChameleonColorManager.isNeedChangeColor()) {
            mPaint.setColor(ChameleonColorManager.getAppbarColor_A1());
        } else {
            // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 begin
            mPaint.setColor(getResources().getColor(R.color.common_green_bg));// "#4b7aaf"
            // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 end
        }
        canvas.drawRect(left, 0, right, mCenterY, mPaint);

    }

    public void updateViewBySDUsage(int sdUsageRadio) {
        float sweepAngle = getAngleByRadio(sdUsageRadio);
        mInnerStartAngle = -90 - sweepAngle;
        mInnerSweepAngle = sweepAngle;
    }

    public void updateViewByPhoneUsage(int phoneUsageRadio) {
        float sweepAngle = getAngleByRadio(phoneUsageRadio);
        mOuterStartAngle = -90 - sweepAngle;
        mOuterSweepAngle = sweepAngle;
    }

    private float getAngleByRadio(int radio) {
        return radio;//radio * 3.6f;
    }
}
