package com.example.systemmanageruidemo.view;

import android.annotation.Nullable;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.systemmanageruidemo.R;

//————————————————
//        版权声明：本文为CSDN博主「Aminy123」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/Aminy123/article/details/82011221
public class CustomProgressView extends View {

    private int cp_percent_textsize = 1;//百分比字体大小
    private int cp_percent_textcolor = 0xff009ACD;
    private int cp_background_color = 0xfff0f0f0;
    private int cp_progress_color = 0xff03C89F;
    private boolean cp_background_is_stroke = true;
    private int cp_rect_round = 3;
    private Paint mPaint;
    private int mCenterX;
    private int mCenterY;

    private int progressCurrent = 0;
    private int progressMax = 100;


    public CustomProgressView(Context context) {
        this(context, null);
    }

    public CustomProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.CustomProgressView);
        cp_percent_textsize = (int) typedArray.getDimension(R.styleable.CustomProgressView_cp_percent_textsize, cp_percent_textsize);
        cp_percent_textcolor = typedArray.getColor(R.styleable.CustomProgressView_cp_percent_textcolor, cp_percent_textcolor);
        cp_background_color = typedArray.getColor(R.styleable.CustomProgressView_cp_background_color, cp_background_color);
        cp_progress_color = typedArray.getColor(R.styleable.CustomProgressView_cp_progress_color, cp_progress_color);
        cp_background_is_stroke = typedArray.getBoolean(R.styleable.CustomProgressView_cp_background_is_stroke, cp_background_is_stroke);
        cp_rect_round = (int) typedArray.getDimension(R.styleable.CustomProgressView_cp_rect_round, getResources().getDisplayMetrics().density * cp_rect_round);
        progressCurrent = typedArray.getInt(R.styleable.CustomProgressView_cp_progress, 0);
        typedArray.recycle();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
        drawHorProgress(mPaint, canvas);
    }

    private void drawHorProgress(Paint paint, Canvas canvas) {
        //画背景
        paint.setColor(cp_background_color);
        if (cp_background_is_stroke) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
        } else {
            paint.setStyle(Paint.Style.FILL);
        }
        canvas.drawRoundRect(new RectF(mCenterX - getWidth() / 2, mCenterY - getHeight() / 2,
                mCenterX + getWidth() / 2, mCenterY + getHeight() / 2), cp_rect_round, cp_rect_round, paint);
        //画进度条
        paint.setColor(cp_progress_color);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(new RectF(mCenterX - getWidth() / 2, mCenterY - getHeight() / 2,
                (int) (progressCurrent * getWidth() / progressMax), mCenterY + getHeight() / 2), cp_rect_round, cp_rect_round, paint);
        //画文字
//        paint.setColor(cp_percent_textcolor);
//        paint.setTextSize(cp_percent_textsize);
//        paint.setStyle(Paint.Style.FILL);
//        String value_str = (int) (progressCurrent * 100 / progressMax) + "%";
//        Rect rect = new Rect();
//        paint.getTextBounds(value_str, 0, value_str.length(), rect);
//
//        float textWidth = rect.width();
//        float textHeight = rect.height();
//        if (textWidth >= getWidth()) {
//            textWidth = getWidth();
//        }
//        Paint.FontMetrics metrics = paint.getFontMetrics();
//        float baseline = (getMeasuredHeight() - metrics.bottom + metrics.top) / 2 - metrics.top;
//        canvas.drawText(value_str, mCenterX - textWidth / 2, baseline, paint);

    }

    public int getProgressCurrent() {
        return progressCurrent;
    }

    public void setProgressCurrent(int progressCurrent) {
        if (progressCurrent > progressMax) {
            this.progressCurrent = progressMax;
        } else {
            this.progressCurrent = progressCurrent;
        }
        postInvalidate();
    }

}
