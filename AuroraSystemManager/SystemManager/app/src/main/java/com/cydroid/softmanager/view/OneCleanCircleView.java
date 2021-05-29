package com.cydroid.softmanager.view;

import com.cydroid.softmanager.R;
import cyee.changecolors.ChameleonColorManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class OneCleanCircleView extends CommonCircleView {
    private static final int START_ANGLE = 270;
    private final int WHITE_COLOR = Color.WHITE;
    private final int left;
    private final int right;

    public OneCleanCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        left = (int) (mCenterX - UnitUtil.dip2px(context, 12));
        right = (int) (mCenterX + UnitUtil.dip2px(context, 12));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mRectF = new RectF(mCenterX - mOuterCircleRadius, mCenterY - mOuterCircleRadius, mCenterX
                + mOuterCircleRadius, mCenterY + mOuterCircleRadius);

        mDectorColor = updatePaintColor(mRatio);
        mPaint.setColor(mDectorColor);
        mPaint.setAntiAlias(true);
        int sweepAngle = convertToAngle(mRatio);
        canvas.drawArc(mRectF, START_ANGLE - sweepAngle, sweepAngle/* - 7 */, false, mPaint);

        mPaint.setStyle(Paint.Style.FILL);

        if (ChameleonColorManager.isNeedChangeColor()) {
            mPaint.setColor(ChameleonColorManager.getAppbarColor_A1());
        } else {
            // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 begin
            mPaint.setColor(getResources().getColor(R.color.common_green_bg));
            // Gionee <yangxinruo> <2016-5-10> modify for CR01658695 end
        }

        canvas.drawRect(left, 0, right, mCenterY, mPaint);
    }

    private int convertToAngle(int ratio) {
        if (ratio < 0 || ratio > 100)
            return 0;
        else
            return (int) (3.6 * ratio);

    }

    private int updatePaintColor(int ratio) {
        int color = WHITE_COLOR;

        /*
         * if(ratio > 85){ color = RED_COLOR; }else{ color = WHITE_COLOR; }
         */

        return color;

    }

}