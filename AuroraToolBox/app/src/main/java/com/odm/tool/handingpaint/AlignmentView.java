package com.odm.tool.handingpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class AlignmentView extends View {

    private double mDegree;
    private Paint mPaint;
    private Paint mPaint1;
    private int mViewWidth,mViewHeight; //控件的宽和高

    public AlignmentView(Context context) {
        this(context,null);
    }

    public AlignmentView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AlignmentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setColor(0xff2ea6df);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));

        mPaint1 = new Paint();
        mPaint1.setColor(Color.WHITE);
        mPaint1.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint1.setStrokeWidth(5);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect rect = new Rect();
        getDrawingRect(rect);

        canvas.drawLine(mViewWidth/2,0, mViewWidth/2, mViewHeight, mPaint1);
        canvas.drawLine(0,mViewHeight/2, mViewWidth, mViewHeight/2, mPaint1);
        canvas.save();

        canvas.rotate((int)mDegree, rect.width() / 2, rect.height() / 2);

        Path pathX = new Path();
        pathX.moveTo(mViewWidth/2,-mViewHeight);
        pathX.lineTo(mViewWidth/2, mViewHeight*3);
        canvas.drawPath(pathX, mPaint);
        canvas.save();

        Path pathY = new Path();
        pathY.moveTo(-48,mViewHeight/2);
        pathY.lineTo(mViewWidth*3, mViewHeight/2);
        canvas.drawPath(pathY, mPaint);
        canvas.save();
    }

    public void setDegree(float degree) {
        mDegree = degree;
        invalidate();
    }
}
