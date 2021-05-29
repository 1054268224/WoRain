package com.cydroid.softmanager.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.systemmanageruidemo.R;

/**
 * Created by wsq
 * on 17-6-1.
 */
public class ScoreCountView extends View {
    private static final String[] scores = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final String TAG = "ScoreCountView";
    private static final int MESSAGE_CIRCLE_ROTATE = 10;
    private static final int MESSAGE_NUM_ROLL = 11;
    private static final int MESSAGE_Hint_LOADING = 12;
    public static final int GRAVITY_RIGHT_TOP = 1;
    public static final int GRAVITY_RIGHT_Bottom = 2;
    private Drawable exCircleDrawable;
    private Drawable innerCircleDrawable;
    private final Rect innerDrawRect;
    private final Rect exDrawRect;
    private Paint mScorePaint;
    private Paint mUnitPaint;
    private Paint mHintPaint;
    private Paint.FontMetricsInt mHintFontMetrics;
    private Paint.FontMetricsInt mScoreFontMetrics;
    private final Rect mScoreRect;

    private Paint.FontMetricsInt mUnitFontMetrics;
    private int mScoreSingleBitsX;
    private int mScoreTenBitsX;
    private int mScrollY;
    private int mDifferenceValue;
    private int centerX;
    private int centerY;
    private int scoreX;
    private int scoreY;
    private final int scoreWidth;
    private Paint mBtnPaint;
    private int scoreHeight;
    private int scoreBaseLine;
    private int hintX;
    private int hintY;
    private int hintWidth;
    private int hintHeight;

    private int hintBaseLine;
    private int mTextGravity = GRAVITY_RIGHT_Bottom;
    private int mSingleBits = 0;
    private int mTenBits = 0;
    private int mHundredBits = 1;
    private int mCurrentScore = 100;
    private int orientation = 1;
    private int mScoreSize;
    private int mScoreColor;
    private int mScoreStyle;
    private int mUnitSize;
    private int mUnitColor;
    private int mUnitStyle;
    private int mHintSize;
    private int mHintColor;
    private int mHintStyle;
    private int nextX;
    private int mNextTenBitsX;
    private int mSize;
    private int mHintPosition;
    private int mScoreWidthHintMargin;
    private int[] mBtnColors;
    private float mAngle;
    private final float mOneBitScoreWidth;
    private float mUnitWidth;
    private float mHintWidth;
    private float[] mBtnPosition;
    private long scoreSpeed = 5;
    private boolean isShowExternalCircle;
    private boolean isShowInnerCircle;

    private boolean mCircleExamLoading;
    private boolean mHintLoading = false;
    private boolean isShowBtnLight;
    private String mUnitText = "分";
    private String mHintText = "正在检测11";
    private final String[] mLoadPoint;
    private String drawHintText = "正在检测22";
    private ScoreChangeListener scoreChangeListener;
    private final Rect mScoreBounds;
    private LinearGradient gradient;
    private boolean mScrolling = false;
    private int mAngleDelta = 1;
    private int mScoreUnitSize;


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_CIRCLE_ROTATE:
                    mAngle = mAngle + mAngleDelta;
                    if (mCircleExamLoading) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_CIRCLE_ROTATE, 20);
                    }
                    break;
                case MESSAGE_NUM_ROLL:
                    mScrollY = mScrollY + orientation * scoreHeight / 10;
                    if (orientation * mScrollY >= scoreHeight) {
                        mScrolling = true;
                        whenScoreChange();
                        updateOrientation();
                        mScrollY = 0;
                        if (orientation == 1) {
                            scoreIncrease();
                        } else {
                            scoreSubtract();
                        }
                        mDifferenceValue -= orientation;
                    }
                    if (mDifferenceValue != 0) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_NUM_ROLL, scoreSpeed);
                    } else {
                        mScrolling = false;
                    }
                    break;
                case MESSAGE_Hint_LOADING:
                    if (!mHintLoading) {
                        return;
                    }
                    if (mHintPosition < mLoadPoint.length) {
                        drawHintText = mHintText + mLoadPoint[mHintPosition];
                        mHintPosition++;
                    } else {
                        mHintPosition = 0;
                    }
                    mHandler.sendEmptyMessageDelayed(MESSAGE_Hint_LOADING, 300);
                    break;
            }
            invalidate();
        }
    };

    private void whenScoreChange() {
        if (scoreChangeListener == null) {
            return;
        }
        scoreChangeListener.onScoreChange(Integer.parseInt(scores[mHundredBits] + scores[mTenBits] + scores[mSingleBits]) + orientation);
    }

    private void scoreSubtract() {
        if (mSingleBits > 0) {
            mSingleBits--;
        } else if (mTenBits > 0) {
            mSingleBits = scores.length - 1;
            mTenBits--;
        } else if (mHundredBits >= 0) {
            mTenBits = scores.length - 1;
            mSingleBits = scores.length - 1;
            mHundredBits--;
        }
    }

    private void scoreIncrease() {
        if (mSingleBits < scores.length - 1) {
            mSingleBits++;
        } else if (mSingleBits >= scores.length - 1 && mTenBits < scores.length - 1) {
            mTenBits++;
            mSingleBits = 0;
        } else if (mTenBits >= scores.length - 1) {
            mHundredBits++;
            mSingleBits = 0;
            mTenBits = 0;
        }
    }

    public void setScoreChangeListener(ScoreChangeListener scoreChangeListener) {
        this.scoreChangeListener = scoreChangeListener;
    }

    public void setScoreColor(int mScoreColor) {
        this.mScoreColor = mScoreColor;
    }

    public void setScoreSize(int mScoreSize) {
        this.mScoreSize = mScoreSize;
    }

    public void setScoreStyle(int mScoreStyle) {
        this.mScoreStyle = mScoreStyle;
    }

    public void setUnitSize(int mUnitSize) {
        this.mUnitSize = mUnitSize;
    }

    public void setUnitColor(int mUnitColor) {
        this.mUnitColor = mUnitColor;
    }

    public void setUnitStyle(int mUnitStyle) {
        this.mUnitStyle = mUnitStyle;
    }

    public void setHintSize(int mHintSize) {
        this.mHintSize = mHintSize;
    }

    public void setHintColor(int mHintColor) {
        this.mHintColor = mHintColor;
    }

    public void setHintStyle(int mHintStyle) {
        this.mHintStyle = mHintStyle;
    }

    public ScoreCountView(Context context) {
        this(context, null);
    }

    public ScoreCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getAttributes(context, attrs);
        mLoadPoint = getResources().getStringArray(R.array.score_exam_hint);
        mHandler.sendEmptyMessageDelayed(MESSAGE_CIRCLE_ROTATE, 20);
        mHandler.sendEmptyMessageDelayed(MESSAGE_Hint_LOADING, 300);

        initPaint();
        exDrawRect = new Rect();
        innerDrawRect = new Rect();
        mScoreBounds = new Rect();
        mScoreRect = new Rect();
        mScorePaint.getTextBounds("000", 0, "000".length(), mScoreBounds);
        mOneBitScoreWidth = mScorePaint.measureText("0");
        scoreWidth = (int) (3 * mOneBitScoreWidth);

    }

    public void calculateCoordinator() {
        exDrawRect.left = (getMeasuredWidth() - mSize) / 2;
        exDrawRect.top = (getMeasuredHeight() - mSize) / 2;
        exDrawRect.right = (getMeasuredWidth() + mSize) / 2;
        exDrawRect.bottom = (getMeasuredHeight() + mSize) / 2;

        innerDrawRect.left = (getMeasuredWidth() - mSize) / 2;
        innerDrawRect.top = (getMeasuredHeight() - mSize) / 2;
        innerDrawRect.right = (getMeasuredWidth() + mSize) / 2;
        innerDrawRect.bottom = (getMeasuredHeight() + mSize) / 2;

        centerX = exDrawRect.centerX();
        centerY = exDrawRect.centerY();

        mUnitWidth = mUnitPaint.measureText(mUnitText);
        mHintWidth = mHintPaint.measureText(mHintLoading ? mHintText + "..." : mHintText);
        hintHeight = 0;//mHintFontMetrics.descent - mHintFontMetrics.ascent;
        hintX = (int) ((getMeasuredWidth() - mHintWidth) / 2);

        scoreX = centerX - (scoreWidth + mScoreUnitSize) / 2;
        scoreY = (getMeasuredHeight() - (scoreHeight + hintHeight + mScoreWidthHintMargin)) / 2;
        new scoreLocate(scoreX, scoreY);

        scoreHeight = mScoreFontMetrics.descent - mScoreFontMetrics.ascent;
        scoreBaseLine = scoreY - mScoreFontMetrics.ascent;
        hintBaseLine = scoreY + scoreHeight + mScoreWidthHintMargin - mHintFontMetrics.ascent;
        mScoreRect.set(scoreX, scoreY, (int) (scoreX + scoreWidth + mUnitWidth), scoreY + scoreHeight);
        mScoreSingleBitsX = scoreX + (int) (2 * mOneBitScoreWidth);
        mScoreTenBitsX = scoreX + (int) mOneBitScoreWidth;
        setNextNumCoordinatorX();
    }

    public class scoreLocate {
        private int x;
        private int y;

        public scoreLocate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "scoreLocate{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScoreCountView);
        scoreSpeed = typedArray.getInt(R.styleable.ScoreCountView_number_scroll_speed, 10);
        mScoreSize = typedArray.getDimensionPixelSize(R.styleable.ScoreCountView_number_size, sp2px(context, 50f));
        mScoreColor = typedArray.getColor(R.styleable.ScoreCountView_number_color, Color.GREEN);
        mScoreStyle = typedArray.getInt(R.styleable.ScoreCountView_number_style, 0);
        mScoreWidthHintMargin = typedArray.getDimensionPixelSize(R.styleable.ScoreCountView_number_hint_margin, 0);

        mUnitText = typedArray.getString(R.styleable.ScoreCountView_unit);
        mUnitSize = typedArray.getDimensionPixelSize(R.styleable.ScoreCountView_unit_size, sp2px(context, 18f));
        mUnitColor = typedArray.getColor(R.styleable.ScoreCountView_unit_color, Color.GREEN);
        mUnitStyle = typedArray.getInt(R.styleable.ScoreCountView_unit_style, 0);
        mScoreUnitSize = typedArray.getDimensionPixelSize(R.styleable.ScoreCountView_score_view_unit_size, sp2px(context, 16f));


        mHintSize = typedArray.getDimensionPixelSize(R.styleable.ScoreCountView_hint_size, sp2px(context, 18f));
        mHintColor = typedArray.getColor(R.styleable.ScoreCountView_hint_color, Color.YELLOW);
        mHintStyle = typedArray.getInt(R.styleable.ScoreCountView_hint_style, 0);

        mSize = typedArray.getDimensionPixelSize(R.styleable.ScoreCountView_size, (int) dip2px(context, 160f));
        isShowExternalCircle = typedArray.getBoolean(R.styleable.ScoreCountView_show_external_circle, false);
        isShowInnerCircle = typedArray.getBoolean(R.styleable.ScoreCountView_show_inner_circle, false);
        mCircleExamLoading = typedArray.getBoolean(R.styleable.ScoreCountView_rotate_animator, true);

        exCircleDrawable = getResources().getDrawable(R.mipmap.circle01);

        innerCircleDrawable = getResources().getDrawable(R.mipmap.circle02);

        typedArray.recycle();
    }
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    
    private void initPaint() {
        mBtnPaint = new Paint();
        mBtnColors = new int[2];
        mBtnColors[0] = 0x60ffffff;
        mBtnColors[1] = 0x00ffffff;
        mBtnPosition = new float[2];
        mBtnPosition[0] = 0f;
        mBtnPosition[1] = 0.4f;

        mScorePaint = new Paint();
        mScorePaint.setAntiAlias(true);
        mScorePaint.setTextSize(mScoreSize);
        mScorePaint.setColor(mScoreColor);
        mScorePaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        mScoreFontMetrics = mScorePaint.getFontMetricsInt();

        mUnitPaint = new Paint();
        mUnitPaint.setAntiAlias(true);
        mUnitPaint.setTextSize(mUnitSize);
        mUnitPaint.setColor(mUnitColor);
        mUnitFontMetrics = mUnitPaint.getFontMetricsInt();

        mHintPaint = new Paint();
        mHintPaint.setColor(mHintColor);
        mHintPaint.setAntiAlias(true);
        mHintPaint.setTextSize(mHintSize);
        mHintFontMetrics = mHintPaint.getFontMetricsInt();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureSize(widthMeasureSpec, true);
        int height = measureSize(heightMeasureSpec, false);
        setMeasuredDimension(width, height);

    }

    private int measureSize(int measureSpec, boolean isWidth) {
        int size = 0;
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);

        if (measureMode == MeasureSpec.EXACTLY) {
            size = measureSize;
        } else if (measureMode == MeasureSpec.AT_MOST && isWidth) {
            size = mSize + 20;
        } else if (measureMode == MeasureSpec.AT_MOST) {
            size = mSize + 20;
        }
        return size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateCoordinator();
        if (isShowBtnLight) {
            showButtonLight(canvas);
        }
        if (isShowExternalCircle) {
            //drawExternalCircle(canvas);
        }
        if (isShowInnerCircle) {
            //drawInnerCircle(canvas);
        }

        drawScore(canvas);
        //guoxt modify begin
        //drawHint(canvas);
        //guoxt modify end
    }

    private void showButtonLight(Canvas canvas) {
        //162/204:内圆环与图片宽度比值
        canvas.drawCircle(centerX, centerY, (mSize * 162) / (2 * 204), mBtnPaint);
    }

    private void drawInnerCircle(Canvas canvas) {
        canvas.save();
        canvas.rotate(-mAngle, innerDrawRect.centerX(), innerDrawRect.centerY());
        exCircleDrawable.setBounds(innerDrawRect);
        exCircleDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawExternalCircle(Canvas canvas) {
        canvas.save();
        canvas.rotate(mAngle, exDrawRect.centerX(), exDrawRect.centerY());
        innerCircleDrawable.setBounds(exDrawRect);
        innerCircleDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawScore(Canvas canvas) {
        canvas.save();
        canvas.clipRect(mScoreRect);
        //draw 个位
        canvas.drawText(scores[mSingleBits], mScoreSingleBitsX, scoreBaseLine + mScrollY, mScorePaint);
        if ((orientation == 1 && mSingleBits < scores.length - 1) || (orientation == -1 && mSingleBits > 0)) {
            mScorePaint.setStyle(Paint.Style.STROKE);
            mScorePaint.setStrokeWidth(8);
            canvas.drawText(scores[mSingleBits + orientation], nextX, scoreBaseLine + mScrollY -
                    orientation * scoreHeight, mScorePaint);
        } else {
            mScorePaint.setStyle(Paint.Style.STROKE);
            mScorePaint.setStrokeWidth(8);
            canvas.drawText(orientation == 1 ? scores[0] : scores[scores.length - 1],
                    nextX, scoreBaseLine + mScrollY - orientation * scoreHeight, mScorePaint);
        }

        //draw 十位
        if ((orientation == 1 && mSingleBits == 9) || (orientation == -1 && mSingleBits == 0)) {
            if (mHundredBits != 0 || mTenBits != 0) {
                mScorePaint.setStyle(Paint.Style.STROKE);
                mScorePaint.setStrokeWidth(8);
                canvas.drawText(scores[mTenBits], mScoreTenBitsX, scoreBaseLine + mScrollY, mScorePaint);
            }
            if ((orientation == 1 && mTenBits < scores.length - 1) || (orientation == -1 && mTenBits > 0)) {
                if (mTenBits + orientation != 0) {
                    mScorePaint.setStyle(Paint.Style.STROKE);
                    mScorePaint.setStrokeWidth(8);
                    canvas.drawText(scores[mTenBits + orientation], mNextTenBitsX, scoreBaseLine +
                            mScrollY - orientation * scoreHeight, mScorePaint);
                }
            } else {
                mScorePaint.setStyle(Paint.Style.STROKE);
                mScorePaint.setStrokeWidth(8);
                canvas.drawText(orientation == 1 ? scores[0] : scores[scores.length - 1],
                        mNextTenBitsX, scoreBaseLine + mScrollY - orientation * scoreHeight, mScorePaint);
            }
        } else {
            if (mHundredBits != 0 || mTenBits != 0) {
                mScorePaint.setStyle(Paint.Style.STROKE);
                mScorePaint.setStrokeWidth(8);
                canvas.drawText(scores[mTenBits], mScoreTenBitsX, scoreBaseLine, mScorePaint);
            }
        }

        //draw 百位
        if ((orientation == 1 && mTenBits == 9 && mSingleBits == 9) || (orientation == -1 &&
                mTenBits == 0 && mSingleBits == 0)) {
            if (mHundredBits != 0) {
                mScorePaint.setStyle(Paint.Style.STROKE);
                mScorePaint.setStrokeWidth(8);
                canvas.drawText(scores[mHundredBits], scoreX, scoreBaseLine + mScrollY, mScorePaint);
            }
            if (mHundredBits + orientation > 0) {
                mScorePaint.setStyle(Paint.Style.STROKE);
                mScorePaint.setStrokeWidth(8);
                canvas.drawText(scores[mHundredBits + orientation], scoreX, scoreBaseLine + mScrollY
                        - scoreHeight, mScorePaint);
            }
        } else if (mHundredBits != 0) {
            mScorePaint.setStyle(Paint.Style.STROKE);
            mScorePaint.setStrokeWidth(8);
            canvas.drawText(scores[mHundredBits], scoreX, scoreBaseLine, mScorePaint);
        }

        //draw 分
        float ratio = mHundredBits == 0 ? 2.8f : 3;
        switch (mTextGravity) {
            case GRAVITY_RIGHT_TOP:
                mUnitPaint.setStyle(Paint.Style.STROKE);
                mUnitPaint.setStrokeWidth(2);
                canvas.drawText(mUnitText, scoreX + ratio * mOneBitScoreWidth, scoreBaseLine + mScoreBounds.top - mUnitFontMetrics.ascent, mUnitPaint);
                break;
            case GRAVITY_RIGHT_Bottom:
                mUnitPaint.setStyle(Paint.Style.STROKE);
                mUnitPaint.setStrokeWidth(2);
                canvas.drawText(mUnitText, scoreX + ratio * mOneBitScoreWidth, scoreBaseLine, mUnitPaint);
                break;
        }

        canvas.restore();
    }

    private void drawHint(Canvas canvas) {
        canvas.save();
        canvas.drawText(drawHintText, hintX, hintBaseLine, mHintPaint);
        canvas.restore();
    }

    private void setNextNumCoordinatorX() {
        if (mHundredBits > 0) {
            mScoreTenBitsX = (int) (scoreX + mOneBitScoreWidth);
            mNextTenBitsX = (int) ((getMeasuredWidth() - 2 * mOneBitScoreWidth) / 2);
        } else if (mTenBits > 0) {
            mScoreTenBitsX = (int) ((getMeasuredWidth() - 2 * mOneBitScoreWidth) / 2);

            if (orientation == 1 && mTenBits == 9 && mSingleBits == 9) {
                nextX = (int) (scoreX + 2 * mOneBitScoreWidth);
                mNextTenBitsX = (int) (scoreX + mOneBitScoreWidth);
            } else {
                mNextTenBitsX = (int) ((getMeasuredWidth() - 2 * mOneBitScoreWidth) / 2);
            }
            if (orientation == -1 && mTenBits == 1 && mSingleBits == 0) {
                nextX = (int) ((getMeasuredWidth() - mOneBitScoreWidth) / 2);
                return;
            }
        } else {
            nextX = (int) ((getMeasuredWidth() - mOneBitScoreWidth) / 2);
            mScoreSingleBitsX = (int) ((getMeasuredWidth() - mOneBitScoreWidth) / 2);
            if (orientation == 1 && mSingleBits == 9) {
                nextX = (int) ((getMeasuredWidth() - 2 * mOneBitScoreWidth) / 2 + mOneBitScoreWidth);
            }
            return;
        }
        nextX = (int) (mNextTenBitsX + mOneBitScoreWidth);
        mScoreSingleBitsX = (int) (mScoreTenBitsX + mOneBitScoreWidth);
    }

    public void setHintText(String mHintText, boolean isLoading) {
        this.mHintText = isShowInnerCircle || isShowExternalCircle ? adapterTextLength(mHintText) : mHintText;
        drawHintText = this.mHintText;
        this.mHintLoading = isLoading;
        if (mHintLoading) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_Hint_LOADING, 300);
            return;
        }
        invalidate();
    }

    public void setUnitGravity(int mTextGravity) {
        this.mTextGravity = mTextGravity;
    }

    public void setShowInnerCircle(boolean showInnerCircle) {
        isShowInnerCircle = showInnerCircle;
    }

    public void setShowExternalCircle(boolean showExternalCircle) {
        isShowExternalCircle = showExternalCircle;
    }

    public void isExamLoadingAnim(boolean isExamLoading) {
        if (isExamLoading && !this.mCircleExamLoading) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_CIRCLE_ROTATE, 20);
        }
        this.mCircleExamLoading = isExamLoading;
    }

    public synchronized int scoreChange(int change) throws Exception {
        mDifferenceValue += change;
        mCurrentScore += change;
        Log.d("guoxt:", "mDifferenceValue:" + mDifferenceValue + ", mCurrentScore: " + mCurrentScore + ", change:" + change);
        if (mCurrentScore < 0 || mCurrentScore > 100) {
            //Log.d(TAG, "Exception : score > 100 or score < 0 : " + 100 + mDifferenceValue);
            throw new Exception("score more than range");
        }
        if (mDifferenceValue == change) {
            updateOrientation();
            mHandler.sendEmptyMessageDelayed(MESSAGE_NUM_ROLL, scoreSpeed);
        }
        return mCurrentScore;
    }

    private void updateOrientation() {
        if (mDifferenceValue > 0) {
            orientation = 1;
        } else {
            orientation = -1;
        }
    }

    public void release() {
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Log.d(TAG, "onDetachedFromWindow()");
        release();
        super.onDetachedFromWindow();
    }

    public interface ScoreChangeListener {
        void onScoreChange(int score);
    }

    public void setShowButtonLight(boolean isShow) {
        this.isShowBtnLight = isShow;
        if (gradient == null) {
            gradient = new LinearGradient(centerX - (getMeasuredWidth() * 162) / (2 * 204),
                    centerY - (getMeasuredHeight() * 162) / (2 * 204),
                    centerX + (getMeasuredWidth() * 162) / (2 * 204),
                    centerY + (getMeasuredHeight() * 162) / (2 * 204),
                    mBtnColors, mBtnPosition, Shader.TileMode.REPEAT);
            mBtnPaint.setShader(gradient);
        }
        invalidate();
    }

    public void setDefaultScore(int score) {
        mHundredBits = score / 100;
        mTenBits = score % 100 / 10;
        mSingleBits = score % 100 % 10;
        this.mCurrentScore = score;
    }

    public int getCurrentScore() {
        return mCurrentScore;
    }

    public void showCircleView() {
        innerCircleDrawable.setAlpha(255);
        exCircleDrawable.setAlpha(255);
    }

    public void hideCircleView(boolean anim) {
        if (anim) {
            ValueAnimator showAnim = ValueAnimator.ofInt(255, 0);
            showAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int alpha = (int) animation.getAnimatedValue();
                    innerCircleDrawable.setAlpha(alpha);
                    exCircleDrawable.setAlpha(alpha);
                }
            });
            showAnim.setDuration(800);
            showAnim.start();
        } else {
            innerCircleDrawable.setAlpha(0);
            exCircleDrawable.setAlpha(0);
        }
    }

    private String adapterTextLength(String str) {
        if (str != null && str.length() > 3) {
            float textWidth = mHintPaint.measureText(str);
            if (textWidth > 2 * mSize / 3) {
                int subIndex = mHintPaint.breakText(str, 0, str.length(), true, 2 * mSize / 3, null);
                str = str.substring(0, subIndex - 3) + "...";
            }
        }
        return str;
    }

    public boolean isScoreScrolling() {
        return mScrolling;
    }

    public void setCircleRotateAngle(int checkItem, int allcount) {
        if (checkItem <= allcount / 2) {
            mAngleDelta = checkItem * 2;
        } else {
            mAngleDelta = (allcount - checkItem + 1) * 2;
        }
    }
}
