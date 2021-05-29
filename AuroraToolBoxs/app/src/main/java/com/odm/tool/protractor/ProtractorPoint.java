package com.odm.tool.protractor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import android.graphics.RectF;
import android.icu.text.DecimalFormat;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.odm.tool.R;
//add bug:TEWBW-1110 wushanfei 20200313 start
import android.graphics.Rect;
//add bug:TEWBW-1110 wushanfei 20200313 end

public class ProtractorPoint extends View {

    private int pointerColor = Color.parseColor("#1080C3");   //指针颜色

    private int mViewWidth = 0;
    private int mViewHeight = 0;
    //add bug:TEWBB-278 wushanfei 20200722 start
    private double mPointRadio = 0.86;
    //add bug:TEWBB-278 wushanfei 20200722 end
    private Path mDotpath = new Path();
    private Path mPointerPathOne = new Path();   //指针绘制路径
    private Path mPointerPathTwo = new Path();   //指针绘制路径

    private Context mContext;
    private Bitmap mBitmapProtractor;

    private RectF mRectFOne = new RectF();
    private RectF mRrectFTwo = new RectF();

    private boolean isChoosePointerOne = false;
    private boolean isChoosePointerTwo = false;

    private double mDegreeOne = 45;
    private double mDegreeTwo = 135;

    private Paint mPointPaint;
    private Paint mDotPaint;
    //add bug:TEWBW-1110 wushanfei 20200313 start
    private Paint mTextPaint;
    private static  final String MAX_DEGREE = "180.00°";
    Rect mRect = new Rect();
    private int mTextColor = Color.parseColor("#fff600"); 
    private int mTextSize = 30;
    private int protractorTextViewmarginTop;
    private int protractorTextViewmarginLeft;
    //add bug:TEWBW-1110 wushanfei 20200313 end

    private int mScreenWidth, mScreenHeight; //控件的宽和高
    
	// A: Bug_id:TEWBW-454 chenchunyong 20191227 {
    private int mComputeBounds;
	// A: }

    private DecimalFormat mDecimalFormat = new DecimalFormat("0.00");

    public ProtractorPoint(Context context) {
        this(context, null);
    }

    public ProtractorPoint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProtractorPoint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        // A: Bug_id:TEWBW-454 chenchunyong 20191227 {
        mComputeBounds = context.getResources().getDimensionPixelSize(R.dimen.configs_compute_bounds);
        // A: }
        //add bug:TEWBW-1110 wushanfei 20200313 start
        mTextSize = context.getResources().getDimensionPixelSize(R.dimen.configs_protractor_textsize);
        protractorTextViewmarginTop = context.getResources().getDimensionPixelSize(R.dimen.protractor_text_marginTop);
        protractorTextViewmarginLeft = context.getResources().getDimensionPixelSize(R.dimen.protractor_text_marginleft);
        //add bug:TEWBW-1110 wushanfei 20200313 end
        initPaint();
    }

    private void initPaint() {
        mPointPaint = new Paint();
        mPointPaint.setColor(pointerColor);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setStrokeWidth(5);

        mDotPaint = new Paint();
        mDotPaint.setColor(pointerColor);
        mDotPaint.setAntiAlias(true);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setStrokeWidth(5);
        //add bug:TEWBW-1110 wushanfei 20200313 start
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.getTextBounds(MAX_DEGREE, 0, MAX_DEGREE.length(), mRect);
        //add bug:TEWBW-1110 wushanfei 20200313 end    
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mScreenHeight = h;

        mViewWidth = getWidth();
        mViewHeight = getHeight() - 16;

        Bitmap protractor = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.protractor);
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        mBitmapProtractor = Bitmap.createBitmap(protractor, 0, 0, protractor.getWidth(), protractor.getHeight(), null, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.translate(mViewWidth,mViewHeight - 100);//将画布中心移动到控件底部中间
        //drawProtractor(canvas);
        //canvas.translate(0, 0);
        drawPoint(canvas, mDegreeOne, mDegreeTwo);  //绘制指针
        //modify bug:TEWBW-1110 wushanfei 20200313 start
        //mDegreeTextView.setText(mDecimalFormat.format(Math.abs(mDegreeOne - mDegreeTwo)) + "°");
        canvas.drawText(mDecimalFormat.format(Math.abs(mDegreeOne - mDegreeTwo)) + "°",
                protractorTextViewmarginLeft, protractorTextViewmarginTop + mRect.width(), mTextPaint);
        //modify bug:TEWBW-1110 wushanfei 20200313 end
    }

    private void drawProtractor(Canvas canvas) {
        canvas.drawBitmap(mBitmapProtractor, -mBitmapProtractor.getWidth(), -mBitmapProtractor.getHeight(), null);
        canvas.save();
    }

    //绘制指针
    private void drawPoint(Canvas canvas, double degreeone, double degreetwo) {
        mPointerPathOne.reset();
        mPointerPathOne.moveTo(mViewWidth / 2, mViewHeight); //下切点
        //add bug:TEWBB-278 wushanfei 20200722 start
        mPointerPathOne.lineTo(mViewWidth / 2 - (float) (Math.cos(degreeone / 180f * Math.PI) * mViewHeight * mPointRadio), mViewHeight - (float) (Math.sin(degreeone / 180f * Math.PI) * mViewHeight * mPointRadio));
        //add bug:TEWBB-278 wushanfei 20200722 end
        mPointerPathOne.close();
        mPointerPathOne.computeBounds(mRectFOne, true);
        mRectFOne.set((int) mRectFOne.left - mComputeBounds, (int) mRectFOne.top - mComputeBounds, (int) mRectFOne.right + mComputeBounds, (int) mRectFOne.bottom + mComputeBounds);
        canvas.drawPath(mPointerPathOne, mPointPaint);

        mPointerPathTwo.reset();
        mPointerPathTwo.moveTo(mViewWidth / 2, mViewHeight);
        //add bug:TEWBB-278 wushanfei 20200722 start
        mPointerPathTwo.lineTo(mViewWidth / 2 - (float) (Math.cos(degreetwo / 180f * Math.PI) * mViewHeight * mPointRadio), mViewHeight - (float) (Math.sin(degreetwo / 180f * Math.PI) * mViewHeight * mPointRadio));
        //add bug:TEWBB-278 wushanfei 20200722 end
        mPointerPathTwo.close();
        mPointerPathTwo.computeBounds(mRrectFTwo, true);
        mRrectFTwo.set((int) mRrectFTwo.left - mComputeBounds, (int) mRrectFTwo.top - mComputeBounds, (int) mRrectFTwo.right + mComputeBounds, (int) mRrectFTwo.bottom + mComputeBounds);
        canvas.drawPath(mPointerPathTwo, mPointPaint);

        mDotpath.reset();
        mDotpath.addCircle(mViewWidth / 2, mViewHeight, 15, Path.Direction.CW);
        canvas.drawPath(mDotpath, mDotPaint);
    }

    //设置度数
    public void setDegree(double degreeone, double degreetwo) {
        if (0 <= degreeone && degreeone <= 180 || 0 <= degreetwo && degreetwo <= 180) {
            mDegreeOne = degreeone;
            mDegreeTwo = degreetwo;
            invalidate();
        }
    }


    //触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float startx, starty;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startx = event.getX();
            starty = event.getY();

            if (mRectFOne.contains((int) startx, (int) starty)) {  //在其中
                isChoosePointerOne = true;
                invalidate();
                return true;   //消费当前事件，否则不会继续分发后续事件
            } else if (mRrectFTwo.contains((int) startx, (int) starty)) {  //在其中
                isChoosePointerTwo = true;
                invalidate();
                return true;   //消费当前事件，否则不会继续分发后续事件
            }
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (isChoosePointerOne) {
                float x = event.getX();
                float y = event.getY();
                if (y <= mViewHeight && x != mViewWidth / 2) {
                    double degree = Math.atan2((mViewHeight - y), (mViewWidth / 2 - x));
                    setDegree(degree / Math.PI * 180, mDegreeTwo);
                } else {
                    if (y > mViewHeight && x < mViewWidth / 2) {  //说明滑到下面了
                        setDegree(0, mDegreeTwo);
                    } else if (y > mViewHeight && x > mViewWidth / 2) {
                        setDegree(180, mDegreeTwo);
                    }
                }
                return true;
            } else if (isChoosePointerTwo) {
                float x = event.getX();
                float y = event.getY();
                if (y <= mViewHeight && x != mViewWidth / 2) {
                    double degree = Math.atan2((mViewHeight - y), (mViewWidth / 2 - x));
                    setDegree(mDegreeOne, degree / Math.PI * 180);
                } else {
                    if (y > mViewHeight && x < mViewWidth / 2) {  //说明滑到下面了
                        setDegree(mDegreeOne, 0);
                    } else if (y > mViewHeight && x > mViewWidth / 2) {
                        setDegree(mDegreeOne, 180);
                    }
                }
                return true;
            } else {
                return false;
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            isChoosePointerOne = false;
            isChoosePointerTwo = false;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

}