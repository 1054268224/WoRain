package com.cydroid.softmanager.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.cydroid.softmanager.R;

public class MainCircleView extends BaseView {

    // private int mWarningRatio = 0;
    // private String mWarningText = "";
    private float mInnerArcRadius;
    private float mRootImgMarginTop = 0;
    private float mRootTxtMarginTop = 0;
    private int aroundCircleTextSize = 0;
    private final BitmapDrawable pointBmd;
    private final int mRootBgAlpha = 23;
    private final int mPointWidth;
    private int mPointHeigh;
    private float mRootRadius;
    private float mRootX;
    private float mRootY;
    private boolean mInOutField = false;
    private boolean mInRootField = false;
    private boolean mRootDisplay = false;
    // Gionee <houjie> <2015-07-27> add for CR01522222 begin
    private int mArcColor = Color.WHITE;
    // Gionee <houjie> <2015-07-27> add for CR01522222 end

    public MainCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateOuterCircleVisibility(false);
        initSize(context);

        Bitmap pointBmp = BitmapFactory.decodeResource(getResources(), R.drawable.point_img);
        pointBmd = new BitmapDrawable(pointBmp);
        mPointWidth = pointBmp.getWidth();
        mPointHeigh = pointBmp.getHeight();
        // Add by HZH on 2019/6/26 for  start 
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        float xxx = wm.getDefaultDisplay().getWidth() / 3.5f + context.getResources().getInteger(R.integer.indicator_offset);
        mPointHeigh = (int) (xxx * 2);
        // Add by HZH on 2019/6/26 for  end
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //drawOval(canvas, mPaint);
        //drawRootWarning(canvas, mPaint);
        drawIndicator(canvas, mPaint);
    }

    private void drawArc(Canvas canvas, Paint paint) {
        paint.setAlpha(255);
        paint.setColor(mArcColor);
        paint.setStrokeWidth(mInnerStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        // canvas.drawArc(mInnerRectF, mInnerStartAngle, ratio, false, paint);
        float coordinate = mInnerRadius;
        RectF rectF = new RectF(-coordinate, -coordinate, coordinate, coordinate);
        // canvas.drawArc(rectF, mInnerStartAngle, ratio, false, paint);
        canvas.drawArc(rectF, mInnerStartAngle, ratio == 0 ? 0.01f : ratio, false, paint);
    }

    private void drawPoint(Canvas canvas, Paint paint) {
        canvas.save();
        float pointRotate = 214 + ratio;// + degree[0];
        // canvas.rotate(pointRotate, mCenterX, mCenterY);
        canvas.rotate(pointRotate, 0, 0);
        // Modify by HZH on 2019/6/6 for EJSL-1509 start
        pointBmd.setBounds((int) (-mPointWidth / 2), (int) (-mPointHeigh / 2), (int) (mPointWidth / 2),
                (int) (mPointHeigh / 2));
        // Modify by HZH on 2019/6/6 for EJSL-1509 end 
        pointBmd.draw(canvas);
        canvas.restore();
    }

    private void drawOval(Canvas canvas, Paint paint) {
        canvas.save();
        paint.setAlpha(mRootBgAlpha);
        paint.setStyle(Paint.Style.FILL);
        float leftD = (float) Math.sin(mInnerStartAngle - 90) * mInnerRadius;
        float rightD = (float) Math.abs(Math.cos(mInnerStartAngle + mInnerSweepAngle - 360 - 14)
                * mInnerRadius);
        float left = mCenterX - leftD;
        float right = mCenterX + rightD;
        float top = (float) (mCenterY + mInnerRadius - 2 * (mInnerRadius - Math.abs(Math
                .cos(mInnerStartAngle - 90)) * mInnerRadius));
        float bottom = mCenterY + mInnerRadius;

        RectF oval = new RectF(0, 0, 2 * mCenterX, 2 * mCenterY);
        oval.set(left - mInnerStrokeWidth + 2, top - 3, right - mInnerStrokeWidth - 3.6f, bottom - 7);
        canvas.drawOval(oval, paint);
        canvas.restore();
    }

    private void drawRootWarning(Canvas canvas, Paint paint) {
//        if (!mRootDisplay) {
//            return;
//        }
//
//        float strokeWidth = 1;
//        int color = Color.parseColor("#ff0000");
//        float cy = (float) Math.abs(Math.sin(mInnerStartAngle + mInnerSweepAngle - 360 - 14) * mInnerRadius)
//                + mCenterX;
//        float cx = mCenterY
//                + (float) Math.abs(Math.cos(mInnerStartAngle + mInnerSweepAngle - 360 - 14) * mInnerRadius)
//                - mInnerStrokeWidth - 5;
//        float radius = mRootRadius;
//        mRootX = cx;
//        mRootY = cy;
//        // fill
//        paint.setAlpha(mRootBgAlpha + 10);
//        paint.setStyle(Paint.Style.FILL);
//        canvas.drawCircle(cx, cy, radius, paint);
//        // draw circle with color of #ff0000
//        Paint paintB = new Paint();
//        paintB.setAntiAlias(true);
//        paintB.setStyle(Paint.Style.STROKE);
//        paintB.setColor(color);
//        paintB.setStrokeWidth(strokeWidth);
//        canvas.drawCircle(cx, cy, radius, paintB);
//        // draw text
//        Paint paintT = new Paint();
//        paintT.setColor(color);
//        paintT.setTextSize(aroundCircleTextSize);
//        String str1 = getResources().getString(R.string.text_root_remind);
//        String str2 = getResources().getString(R.string.text_root_remind_restore);
//        float str1Wdith, str2Wdith;
//        str1Wdith = paintT.measureText(str1);
//        str2Wdith = paintT.measureText(str2);
//
//        canvas.drawText(getResources().getString(R.string.text_root_remind), cx - radius
//                + (radius * 2 - str1Wdith) / 2, cy + mRootTxtMarginTop, paintT);
//        canvas.drawText(getResources().getString(R.string.text_root_remind_restore), cx - radius
//                + (radius * 2 - str2Wdith) / 2, cy + mRootTxtMarginTop + aroundCircleTextSize + 8, paintT);
//
//        // draw bitmap
//        BitmapFactory.Options factory = new BitmapFactory.Options();
//        factory.inSampleSize = 1;
//        Bitmap rootBmp = BitmapFactory.decodeResource(getResources(), R.drawable.root_img, factory);
//        int rootWidth = rootBmp.getWidth() / factory.inSampleSize;
//        int rootHeight = rootBmp.getHeight() / factory.inSampleSize;
//        float startX = cx - radius + (2 * radius - rootWidth) / 2;
//        float startY = cy - radius + (radius - rootHeight) / 2;
//        canvas.drawBitmap(rootBmp, startX, startY + mRootImgMarginTop, paintT);
    }

    private void drawIndicator(Canvas canvas, Paint paint) {
        drawTextAroundCircle(canvas, paint);
        // drawArcAroundCircle(canvas, paint);
        drawArc(canvas, paint);
        drawPoint(canvas, paint);
    }

    private void drawTextAroundCircle(Canvas canvas, Paint paint) {
        canvas.translate(mCenterX, mCenterY);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(51);
        paint.setTextAlign(Align.CENTER);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextSize(aroundCircleTextSize);
        float radius = mInnerStrokeWidth + mInnerRadius;
        int count = 12; // text numbers, 0,10,20, ...
        for (int i = 1; i < count; i++) {
            float x = (float) -(Math.sin(Math.PI * i / 6) * (radius));
            float y = (float) (Math.cos(Math.PI * i / 6) * (radius));
            if (i < count / 2) {
                x = x - 20;
                y = y + 5;
            } else if (i > count / 2) {
                x = x + 20;
                y = y + 5;
            } else {
                y = y - 5;
            }

            // canvas.drawText(String.valueOf((i-1)*10), x , y, paint); //
            // modify text position
        }
    }

    // private void drawArcAroundCircle(Canvas canvas, Paint paint){
    // int alpha = 51;
    // drawInnerArcAroundCircle(canvas, paint, alpha);
    // }

    private void drawInnerArcAroundCircle(Canvas canvas, Paint paint, int alpha) {

        float[] angles = getInnerArcAngle();
        if (angles.length % 2 != 0) {
            return;
        }

        float coordinate = mInnerArcRadius;
        RectF rectF = new RectF(-coordinate, -coordinate, coordinate, coordinate);
        for (int i = 0; i < angles.length; i += 2) {
            drawArcAroundCircle(canvas, paint, alpha, rectF, angles[i], angles[i + 1]);
        }
    }

    private float[] getInnerArcAngle() {
        float[] innerAngles = new float[] {15, 30, 90, 45, 195, 15, 240, 25, 300, 40};
        return innerAngles;
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

    public void updateWarningRatio(int value) {
        // mWarningRatio = value;
    }

    public void setWarningText(String str) {
        // mWarningText = str;
    }

    private void initSize(Context context) {

        float mScale = context.getResources().getDisplayMetrics().density;

        if (floatEqual(mScale, 1.5f)) {
            aroundCircleTextSize = 12;
            mRootImgMarginTop = 0;
            mRootTxtMarginTop = 15;
            mInnerArcRadius = mInnerRadius - 15;
            mRootRadius = 45;
        } else if (floatEqual(mScale, 2.0f)) {
            aroundCircleTextSize = 18;
            mRootImgMarginTop = 0;
            mRootTxtMarginTop = 20;
            mInnerArcRadius = mInnerRadius - 25;
            mRootRadius = 65;
        } else if (floatEqual(mScale, 3.0f)) {
            aroundCircleTextSize = 25;
            mRootImgMarginTop = 5;
            mRootTxtMarginTop = 30;
            mInnerArcRadius = mInnerRadius - 25;
            mRootRadius = 93;
        } else {
            aroundCircleTextSize = 25;
            mRootImgMarginTop = 5;
            mRootTxtMarginTop = 30;
            mInnerArcRadius = mInnerRadius - 25;
            mRootRadius = 90;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        float x = event.getX();
        float y = event.getY();
        float cx = x - mCenterX;
        float cy = y - mCenterY;

        float cXR = x - mRootX;
        float cYR = y - mRootY;
        mInRootField = false;
        mInOutField = false;

        if (mRootDisplay && cXR * cXR + cYR * cYR <= mRootRadius * mRootRadius) {
            mInRootField = true;
        } else if (cx * cx + cy * cy <= mInnerRadius * mInnerRadius) {
            mInOutField = true;
        }

        return super.onTouchEvent(event);
    }

    public boolean getRootField() {
        return mInRootField;
    }

    public boolean getOutField() {
        return mInOutField;
    }

    public void setRootDisplay(boolean flag) {
        mRootDisplay = flag;
    }

    // Gionee <houjie> <2015-07-27> add for CR01522222 begin
    public void setArcColor(int color) {
        mArcColor = color;
    }
    // Gionee <houjie> <2015-07-27> add for CR01522222 end

    // Gionee <houjie> <2015-11-13> add for CR01565278 begin
    public void setPointTint(int color) {
        pointBmd.setTint(color);
    }
    // Gionee <houjie> <2015-11-13> add for CR01565278 end
}
