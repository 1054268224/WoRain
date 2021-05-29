package com.cydroid.softmanager.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.cydroid.softmanager.R;

public class MainOuterCircleView extends BaseView {

    private final BitmapDrawable mRocketBmd;
    private float mOuterArcRadius;
    private final int mRocketWidth;
    private final int mRocketHeigh;
    private boolean isRocketRunning = false;
    private int mRocketAngle = 0;

    // Gionee <houjie> <2015-07-27> add for CR01522222 begin
    private int mOuterArcColor = Color.WHITE;
    // Gionee <houjie> <2015-07-27> add for CR01522222 end

    public MainOuterCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        updateOuterCircleVisibility(false);
        initSize(context);
        Bitmap rocketBmp = BitmapFactory.decodeResource(getResources(), R.drawable.rotate_rocket_img);

        mRocketBmd = new BitmapDrawable(rocketBmp);
        mRocketWidth = rocketBmp.getWidth();
        mRocketHeigh = rocketBmp.getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        drawRocket(canvas, mPaint);
        // drawArcAroundCircle(canvas, mPaint);
    }

    private void drawRocket(Canvas canvas, Paint paint) {
        int alpha = isRocketRunning ? 255 : 0;
        canvas.save();
        // mRocketAngle += 6;
        mRocketAngle = 6;
        float pointRotate = mRocketAngle;
        canvas.rotate(pointRotate, mCenterX, mCenterY);
        mRocketBmd.setBounds((int) mCenterX - mRocketWidth / 2, (int) mCenterY - mRocketHeigh / 2,
                (int) mCenterX + mRocketWidth / 2, (int) mCenterY + mRocketHeigh / 2);
        mRocketBmd.setAlpha(alpha);
        mRocketBmd.draw(canvas);
        canvas.restore();

        paint.setColor(mOuterArcColor);
        paint.setAlpha(alpha);
        paint.setStrokeWidth(2);
        float coordinate = mOuterArcRadius;
        RectF rectF = new RectF(mCenterX - coordinate, mCenterX - coordinate, mCenterX + coordinate, mCenterX
                + coordinate);
        // canvas.drawArc(rectF, mRocketAngle + 175, 90, false, paint);

        for (int i = 0; i <= 255; i += 1) {
            alpha = isRocketRunning ? (255 - i) : 0;
            paint.setAlpha(alpha);
            // Delete by HZH on 2019/5/11 for ui bug start ^_^
//            canvas.drawArc(rectF, mRocketAngle + 264 - i, 1, false, paint);
            // Delete by HZH on 2019/5/11 for ui bug end ^_^
        }
    }

    // private void drawArcAroundCircle(Canvas canvas, Paint paint){
    // int alpha = isRocketRunning ? 0 : 30;
    // drawOuterArcAroundCircle(canvas, paint, alpha);
    // }

    private void drawOuterArcAroundCircle(Canvas canvas, Paint paint, int alpha) {
        float[] angles = getOuterArcAngle();
        if (angles.length % 2 != 0) {
            return;
        }
        float coordinate = mOuterArcRadius;
        RectF rectF = new RectF(mCenterX - coordinate, mCenterY - coordinate, mCenterX + coordinate, mCenterY
                + coordinate);
        for (int i = 0; i < angles.length; i += 2) {
            drawArcAroundCircle(canvas, paint, alpha, rectF, angles[i], angles[i + 1]);
        }
    }

    private float[] getOuterArcAngle() {
        float[] outerAngles = new float[] {45, 70, 140, 60, 250, 40, 330, 40};
        return outerAngles;
    }

    private void drawArcAroundCircle(Canvas canvas, Paint paint, int alpha, RectF rectF, float startAngle,
            float sweepAngle) {
        paint.setAntiAlias(true);
        paint.setColor(mInnerCircleColor);
        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
    }

    public void setRotateRocketFlag(boolean flag) {
        isRocketRunning = flag;
    }

    public void resetRotateStartAngle() {
        mRocketAngle = 0;
    }

    private void initSize(Context context) {

        float mScale = context.getResources().getDisplayMetrics().density;

        if (floatEqual(mScale, 1.5f)) {
            /*guoxt modify for CR01522110 begin*/
            mOuterArcRadius = mInnerRadius + 21;
            /*guoxt modify for CR01522110 end*/
			
        } else if (floatEqual(mScale, 2.0f)) {
            mOuterArcRadius = mInnerRadius + 30;
        } else if (floatEqual(mScale, 3.0f)) {
            mOuterArcRadius = mInnerRadius + 46;
        } else if (floatEqual(mScale, 4.0f)) {
            mOuterArcRadius = mInnerRadius + 71;
        } else {
            mOuterArcRadius = mInnerRadius + 55;
        }

    }

    private boolean floatEqual(float num1, float num2) {
        return (num1 - num2 > -Float.MIN_VALUE) && (num1 - num2) < Float.MIN_VALUE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        widthMeasureSpec = (int) (mCenterX * 2);
        heightMeasureSpec = (int) (mCenterY * 2);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    // Gionee <houjie> <2015-07-27> add for CR01522222 begin
    public void setOuterArcColor(int color) {
        mOuterArcColor = color;
    }

    public void setRocketTint(int color) {
        mRocketBmd.setTint(color);
    }
    // Gionee <houjie> <2015-07-27> add for CR01522222 end
}
