package com.cydroid.softmanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.systemmanageruidemo.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimBallView extends View {

    public static final int LOCATION_LEFT_TOP = 0;
    public static final int LOCATION_RIGHT_TOP = 1;
    public static final int LOCATION_LEFT_BOTTOM = 2;
    public static final int LOCATION_RIGHT_BOTTOM = 3;
    //圆球的最大数量
    public static final int BALL_COUNT = 4;
    private boolean isAnimStart = false;
    //结束坐标
    private int mEndX, mEndY;
    private final Paint mPaint;
    //存放圆球的集合
    private final List<BallBean> mBalls = new ArrayList<BallBean>();
    private final WeakHandler mHandler;


    private int mBallRadius;
    private int mBallColor;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnimBallView(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnimBallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnimBallView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnimBallView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getAttributes(context, attrs);

        mHandler = new WeakHandler((AppCompatActivity) context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
//        mPaint.setColor(mBallColor);
        mPaint.setDither(true);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initsize();
            }
        });


    }

    private void initsize() {
        //获取中心点的位置
        mEndX = getMeasuredWidth() / 2;
        mEndY = getMeasuredHeight() / 2;
        //清除存放圆球的集合
        if (mBalls.size() < BALL_COUNT)
            for (int i = 0; i < BALL_COUNT; i++) {
                BallBean mBall = new BallBean(i, getMeasuredWidth(), getMeasuredHeight() / 3 * 2, mEndX, mEndY, ((ZD.get(i)[0])));
                mBalls.add(mBall);
            }

    }

    private void getAttributes(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AnimBallView);
        mBallRadius = typedArray.getInt(R.styleable.AnimBallView_radius_size, 100);
        mBallColor = typedArray.getColor(R.styleable.AnimBallView_ball_color, Color.argb(255, 149, 200, 252));
    }


    static List<float[]> ZD = new ArrayList<>();

    {
        ZD.add(new float[]{0.704f});
        ZD.add(new float[]{0.634f});
        ZD.add(new float[]{0.3089f});
        ZD.add(new float[]{0.273f});
//        ZD.add(new float[]{0.5f});
//        ZD.add(new float[]{0.5f});
//        ZD.add(new float[]{0.5f});
//        ZD.add(new float[]{0.5f});
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制每个圆的位置
        onDrawBall(canvas);
        if (isAnimStart) {
            //开启延时刷新
            mHandler.sendEmptyMessageDelayed(1, 20);
        } else {
            if (!isZD()) {
                mHandler.sendEmptyMessageDelayed(1, 20);
            }
        }
    }

    private boolean isZD() {
        for (BallBean mBall : mBalls) {
            if (!mBall.isZD()) {
                return false;
            }
        }
        return true;
    }

    private void onDrawBall(Canvas canvas) {
        for (int i = 0; i < mBalls.size(); i++) {
            BallBean mTempBall = mBalls.get(i);
            int m = mTempBall.mAlpha;
            if (m > 70) {
                m = 255;
            }
            mPaint.setAlpha(m);
            LinearGradient mSweepGradient = new LinearGradient(mTempBall.mX + mBallRadius / 2,
                    mTempBall.mY - mBallRadius, mTempBall.mX - mBallRadius / 2, mTempBall.mY + mBallRadius,
                    getResources().getColor(R.color.common_blue_gradientsta),
                    getResources().getColor(R.color.common_blue_gradientend),
                    Shader.TileMode.CLAMP);
            mPaint.setShader(mSweepGradient);
            canvas.drawCircle(mTempBall.mX, mTempBall.mY, mTempBall.mRadius, mPaint);
        }
    }


    public void onStartAnim() {
        isAnimStart = true;
        //开启延时刷新
        for (BallBean mBall : mBalls) {
            mBall.setZDmode(false);
        }
        mHandler.sendEmptyMessageDelayed(1, 20);
    }

    public void onStopAnim() {
//        mHandler.removeCallbacksAndMessages(null);
        for (BallBean mBall : mBalls) {
            mBall.setZDmode(true);
        }
        isAnimStart = false;
    }

    //计算位置和大小
    private void onCalculationLocalWithSize() {
        for (int i = 0; i < mBalls.size(); i++) {
            BallBean mTempBall = mBalls.get(i);
            //计算当前球与终点的差距
            //到达终点，设置新的坐标
            mTempBall.onCalculationLocalWithSize(i);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        onStopAnim();
        super.onDetachedFromWindow();
    }


    class WeakHandler extends Handler {
        WeakReference<AppCompatActivity> mActivity;

        public WeakHandler(AppCompatActivity activity) {
            mActivity = new WeakReference<AppCompatActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() != null) {
                switch (msg.what) {
                    case 1:
                        //计算移动后的位置
                        onCalculationLocalWithSize();
                        //重新draw
                        invalidate();
                        break;
                }
            }
        }
    }


    class BallBean {

        int mX;
        int mY;
        //球半径
        int mRadius;
        int mAlpha;

        private final int mEndX;
        private final int mEndY;
        private final int mWidth;
        private final int mHeight;
        private int mSpeedX;
        private int mSpeedY;
        private int mSpeedRadiu;
        private int mSpeedAlpah;
        private float rate;
        private int qidianx;
        private int qidiany;

        public BallBean(int location, int mWidth, int mHeight, int mEndX, int mEndY, float zdx) {
            this.mEndX = mEndX;
            this.mEndY = mEndY;
            this.mWidth = mWidth;
            this.mHeight = mHeight;
            this.zdx = zdx;
            init(location);
        }

        private float zdx;

        //每次移动的距离（速度）
        private static final int BALL_SPEED = 20;

        private void init(int location) {
            Random mRandom = new Random();
            //只是生成随机数，无视这段话
            int xDiff = 100 - mRandom.nextInt(150);
            int yDiff = 200 - mRandom.nextInt(300);

            switch (location) {
                case LOCATION_LEFT_TOP://左上
                    mX = xDiff;
                    mY = yDiff;
                    break;
                case LOCATION_RIGHT_TOP://右上
                    mX = mWidth + xDiff;
                    mY = yDiff;
                    break;
                case LOCATION_LEFT_BOTTOM://左下
                    mX = xDiff;
                    mY = mHeight;
                    break;
                case LOCATION_RIGHT_BOTTOM://右下
                    mX = mWidth;
                    mY = mHeight + yDiff;
                    break;
            }

            qidianx = mX;
            qidiany = mY;
            int diffX = Math.abs(mEndX - mX);
            int diffY = Math.abs(mEndY - mY);

            //计算初始位置到终点的距离
            int diffZ = (int) Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
            int mSpeedZ = diffZ / BALL_SPEED;
            int mUrplus = diffZ % BALL_SPEED;

            if (mUrplus > 0) {
                mSpeedZ++;
            }

            if (mSpeedZ > 0) {
                mSpeedX = diffX / mSpeedZ + 1;
                mSpeedY = diffY / mSpeedZ + 1;
                mRadius = mBallRadius - mRandom.nextInt(40);
                mSpeedRadiu = mRadius / mSpeedZ;
                mAlpha = 255;

                mSpeedAlpah = mAlpha / mSpeedZ;
            }
            rate = 0;
        }

        public void onCalculationLocalWithSize(int location) {
            Boolean isChange = false;
            if (isZDmode && rate <= zdx) {
                isChange = true;
                move(((int) (zdx * mEndX + (1 - zdx) * qidianx)), ((int) (zdx * mEndY + (1 - zdx) * qidiany)));
                rate = zdx;
                mRadius = ((int) (mBallRadius * (1 - rate)));
                mAlpha = 255;
            } else {
                isChange = move(mEndX, mEndY);
                //缩小
                mRadius -= mSpeedRadiu;
                mAlpha -= mSpeedAlpah;
                rate = Math.abs(mX - qidianx + 0.0f) / Math.abs(mEndX - qidianx + 0.0f);
            }

            if (mAlpha < 0)
                mAlpha = 0;
            //如果位置没有变，重置
            if (!isChange) {
                //重置
                init(location);
            }
            if (rate > 0.7) {
                mRadius = mRadius / 2;
            }
        }

        private boolean move(int mEndX, int mEndY) {
            boolean isChange = false;
            //计算新的位置
            if (Math.abs(mEndX - mX) > mSpeedX) {
                //如果从上往下移动
                if (mEndX > mX) {
                    mX += mSpeedX;
                } else {
                    mX -= mSpeedX;
                }
                isChange = true;
            } else {
                mX = mEndX;
            }

            if (Math.abs(mEndY - mY) > mSpeedY) {
                //如果从左往右移动
                if (mEndY > mY) {
                    mY += mSpeedY;
                } else {
                    mY -= mSpeedY;
                }
                isChange = true;
            } else {
                mY = mEndY;
            }
            return isChange;
        }

        public boolean isZD() {
            return mX == ((int) (zdx * mEndX + (1 - zdx) * qidianx))
                    && mY == ((int) (zdx * mEndY + (1 - zdx) * qidiany));
        }

        boolean isZDmode = false;

        public void setZDmode(boolean b) {
            isZDmode = b;
        }
    }
}
