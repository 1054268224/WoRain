package com.cydroid.softmanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.cydroid.softmanager.R;

public class TrafficaBallView extends BaseView {

    private Context mContext;

    private int mScreenWidth;
    private int mScreenHeight;

    private Paint mRingPaint;
    private Paint mCirclePaint;
    private Paint mWavePaint;
    private Paint mPaint;

    private int mRingSTROKEWidth = 15;
    private int mCircleSTROKEWidth = 2;
    private int mLineSTROKEWidth = 1;

    private int mWarningRatio = -1;

    private int mCircleColor = Color.WHITE;
    private int mRingColor = Color.WHITE;
    private int mWaveColor = Color.WHITE;

    private Handler mHandler;
    private long c = 0L;
    private boolean mStarted = false;
    private final float f = 0.033F;
    private int mAlpha = 50;// 透明度
    private float mAmplitude = 11.0F; // 振幅
    private float mWaterLevel = 0.5f;// 水高(0~1)
    private Path mPath;

    private String mWarningText = "";

    // 绘制文字显示在圆形中间，只是我没有设置，我觉得写在布局上也挺好的
    /*private float mUsedFlow = 1024;
    private String flowLeft = "还剩余";*/
    /**
     * @param context
     * @param attrs
     */
    public TrafficaBallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        init(mContext);
    }




    public void updateWaterLevel(float WaterLevel) { mWaterLevel = WaterLevel; }

    public void updateWarningRatio(int value){
        mWarningRatio = value;
    }

    public void setWarningText(String str){ mWarningText = str; }

    /*public void setUsedFlow(float mUsedFlow){ this.mUsedFlow = mUsedFlow; }*/

    public void setCircleClor(int color){ mCircleColor = color;}

    public void setRingColor(int color){ mRingColor = color;}

    public void setWaveColor(int color){ mWaveColor = color;}

    private void init(Context context) {
        mRingPaint = new Paint();
        mRingPaint.setColor(mRingColor);
        mRingPaint.setAlpha(255);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStrokeWidth(mRingSTROKEWidth);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(mCircleSTROKEWidth);

        mWavePaint = new Paint();
        mWavePaint.setStrokeWidth(1.0F);
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setAlpha(mAlpha);
        mPath = new Path();

        mPaint = new Paint();

        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) {
                    invalidate();
                    if (mStarted) {
                        // 不断发消息给自己，使自己不断被重绘
                        mHandler.sendEmptyMessageDelayed(0, 60L);
                    }
                }
            }
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measure(widthMeasureSpec, true);
        int height = measure(heightMeasureSpec, false);
        if (width < height) {
            setMeasuredDimension(width, width);
        } else {
            setMeasuredDimension(height, height);
        }

    }

    /**
     * @category 测量
     * @param measureSpec
     * @param isWidth
     * @return
     */
    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight()
                : getPaddingTop() + getPaddingBottom();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth()
                    : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.min(result, size);
                }
            }
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mScreenHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        drawWave(canvas);
        drawIndicator(canvas);

    }

    private void drawIndicator(Canvas canvas) {
        if (mWarningRatio == -1) {
            return;
        }
        if(mWarningRatio <= mWaterLevel){
            canvas.drawText(mWarningText,getWidth()/2,getHeight()/2,mPaint);
        }
    }

    private  void drawWave(Canvas canvas){
        canvas.save();
        // 得到控件的宽高
        int width = getWidth();
        int height = getHeight();
        /*setBackgroundColor(mContext.getResources().getColor(R.color.common_blue));*/
        // 计算当前油量线和水平中线的距离
        float centerOffset = Math.abs(mScreenWidth / 2 * mWaterLevel
                - mScreenWidth / 4);
        // 计算油量线和与水平中线的角度
        float horiAngle = (float) (Math.asin(centerOffset / (mScreenWidth / 4)) * 180 / Math.PI);
        // 扇形的起始角度和扫过角度
        float startAngle, sweepAngle;
        if (mWaterLevel > 0.5F) {
            startAngle = 360F - horiAngle;
            sweepAngle = 180F + 2 * horiAngle;
        } else {
            startAngle = horiAngle;
            sweepAngle = 180F - 2 * horiAngle;
        }

        /*canvas.drawLine(mScreenWidth * 3 / 8, mScreenHeight * 5 / 8,
                mScreenWidth * 5 / 8, mScreenHeight * 5 / 8, linePaint);*/

        /*float num = flowPaint.measureText(flowNum);
        canvas.drawText(flowNum, mScreenWidth * 4 / 8 - num / 2,
                mScreenHeight * 4 / 8, flowPaint);
        float left = leftPaint.measureText(flowLeft);
        canvas.drawText(flowLeft, mScreenWidth * 4 / 8 - left / 2,
                mScreenHeight * 3 / 8, leftPaint);*/

        /*float num = flowPaint.measureText(String.valueOf(mUsedFlow));
        canvas.drawText(String.valueOf(mUsedFlow), mScreenWidth * 4 / 8 - num / 2,
                mScreenHeight * 4 / 8, flowPaint);
*/

        // 如果未开始（未调用startWave方法）,绘制一个扇形
        if ((!mStarted) || (mScreenWidth == 0) || (mScreenHeight == 0)) {
            // 绘制,即水面静止时的高度
            RectF oval = new RectF(mScreenWidth / 4, mScreenHeight / 4,
                    mScreenWidth * 3 / 4, mScreenHeight * 3 / 4);
            canvas.drawArc(oval, startAngle, sweepAngle, false, mWavePaint);
            return;
        }
        // 绘制,即水面静止时的高度
        // 绘制,即水面静止时的高度
        RectF oval = new RectF(mScreenWidth / 4, mScreenHeight / 4,
                mScreenWidth * 3 / 4, mScreenHeight * 3 / 4);
        canvas.drawArc(oval, startAngle, sweepAngle, false, mWavePaint);

        if (this.c >= 8388607L) {
            this.c = 0L;
        }
        // 每次onDraw时c都会自增
        c = (1L + c);
        float f1 = mScreenHeight * (1.0F - (0.25F + mWaterLevel / 2))
                - mAmplitude;
        // 当前油量线的长度
        float waveWidth = (float) Math.sqrt(mScreenWidth * mScreenWidth / 16
                - centerOffset * centerOffset);
        // 与圆半径的偏移量
        float offsetWidth = mScreenWidth / 4 - waveWidth;

        int top = (int) (f1 + mAmplitude);
        mPath.reset();
        // 起始振动X坐标，结束振动X坐标
        int startX, endX;
        if (mWaterLevel > 0.50F) {
            startX = (int) (mScreenWidth / 4 + offsetWidth);
            endX = (int) (mScreenWidth / 2 + mScreenWidth / 4 - offsetWidth);
        } else {
            startX = (int) (mScreenWidth / 4 + offsetWidth - mAmplitude);
            endX = (int) (mScreenWidth / 2 + mScreenWidth / 4 - offsetWidth + mAmplitude);
        }
        // 波浪效果
        while (startX < endX) {
            int startY = (int) (f1 - mAmplitude
                    * Math.sin(Math.PI
                    * (2.0F * (startX + this.c * width * this.f))
                    / width));
            canvas.drawLine(startX, startY, startX, top, mWavePaint);
            startX++;
        }
        canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2, mScreenWidth / 4
                + mRingSTROKEWidth / 2, mRingPaint);

        canvas.drawCircle(mScreenWidth / 2, mScreenHeight / 2,
                mScreenWidth / 4, mCirclePaint);
        canvas.restore();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.progress = (int) c;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        c = ss.progress;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 关闭硬件加速，防止异常unsupported operation exception
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * @category 开始波动
     */
    public void startWave() {
        if (!mStarted) {
            this.c = 0L;
            mStarted = true;
            this.mHandler.sendEmptyMessage(0);
        }
    }

    /**
     * @category 停止波动
     */
    public void stopWave() {
        if (mStarted) {
            this.c = 0L;
            mStarted = false;
            this.mHandler.removeMessages(0);
        }
    }

    /**
     * @category 保存状态
     */
    static class SavedState extends BaseSavedState {
        int progress;

        /**
         * Constructor called from {@link ProgressBar#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
