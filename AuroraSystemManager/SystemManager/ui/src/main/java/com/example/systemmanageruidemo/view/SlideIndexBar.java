package com.example.systemmanageruidemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.systemmanageruidemo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlideIndexBar extends View {
    private int mwidth;
    private int mheight;
    private String actioncharindex;
    private boolean upisclick;
    private float eventY2;
    private String actioncharindex2;

    public SlideIndexBar(Context context) {
        super(context);
        init(null);
    }

    public SlideIndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SlideIndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    int frontSize = ((int) (mContext.getResources().getDisplayMetrics().density * 14));
    int floatFrontSize = ((int) (mContext.getResources().getDisplayMetrics().density * 18));
    int selectedtextcolor = mContext.getResources().getColor(R.color.colorPrimary_wyh_traf, null);
    int textcolor = mContext.getResources().getColor(R.color.textcolor_hint3_wyh_traf, null);
    int floattextcolor = mContext.getResources().getColor(R.color.white_wyh_traf, null);
    int floatbgcolor = mContext.getResources().getColor(R.color.colorPrimary_wyh_traf, null);

    Paint textPaint, selectedtextPaint, floattextPaint, floatbgPaint;

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SlideIndexBar);
            frontSize = ta.getDimensionPixelSize(R.styleable.SlideIndexBar_sib_textsize, frontSize);
            floatFrontSize = ta.getDimensionPixelSize(R.styleable.SlideIndexBar_sib_float_textsize, floatFrontSize);
            selectedtextcolor = ta.getColor(R.styleable.SlideIndexBar_sib_select_color, selectedtextcolor);
            textcolor = ta.getColor(R.styleable.SlideIndexBar_sib_textcolor, textcolor);
            floattextcolor = ta.getColor(R.styleable.SlideIndexBar_sib_float_color, floattextcolor);
            floatbgcolor = ta.getColor(R.styleable.SlideIndexBar_sib_float_bg_color, floatbgcolor);
            ta.recycle();
        }
        setFocusable(true);
        setClickable(true);
        setFocusableInTouchMode(true);
        setEnabled(true);
        textPaint = new Paint();
        textPaint.setColor(textcolor);
        textPaint.setTextSize(frontSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        selectedtextPaint = new Paint();
        selectedtextPaint.setColor(selectedtextcolor);
        selectedtextPaint.setTextSize(frontSize);
        selectedtextPaint.setAntiAlias(true);
        selectedtextPaint.setTextAlign(Paint.Align.CENTER);
        floattextPaint = new Paint();
        floattextPaint.setColor(floattextcolor);
        floattextPaint.setAntiAlias(true);
        floattextPaint.setTextSize(floatFrontSize);
        floattextPaint.setTextAlign(Paint.Align.CENTER);
        floatbgPaint = new Paint();
        floatbgPaint.setColor(floatbgcolor);
        floatbgPaint.setAntiAlias(true);
        setDefaultindexstrs();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mheight = getHeight();
        mwidth = getWidth();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        setOnClickListener(v -> {
//            if (upevent != null) {
//                if (upisclick) {
//                    postDelayed(() -> {
//                        clickAction(upevent);
//                    }, 150);
//                }
//            }
//        });
    }

    List<String> indexstrings = new ArrayList<String>();

    public List<String> getIndexstrings() {
        return indexstrings;
    }

    public void setIndexstrings(List<String> indexstrings) {
        this.indexstrings = indexstrings;
    }

    public int getMmargin() {
        return mmargin;
    }

    public void setMmargin(int mmargin) {
        this.mmargin = mmargin;
    }

    public int getMzargin() {
        return mzargin;
    }

    public void setMzargin(int mzargin) {
        this.mzargin = mzargin;
    }

    void setDefaultindexstrs() {
        indexstrings.clear();
        indexstrings.addAll(Arrays.asList(new String[]{"A", "B", "C", "D", "E", "F", "G", "H",
                "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
                "V", "W", "X", "Y", "Z", "#"
        }));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calviewdata();
        drawtextbg(canvas);
        drawtext(canvas);
        drawfloattext(canvas);
    }

    int starttextX;
    int endtextX;
    float eventY;  // 当不为 0 时，绘制float区域
    MotionEvent upevent;

    /**
     * 位于右侧文字区域时，消费事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                upevent = null;
                upisclick = false;
                if (belongActionArea(event)) {
                    prepareAction(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isprepareAction(event)) {
                    if (belongActionArea(event)) {
                        moveAction(event);
                    } else {
                        resetAction(event);
                    }
//                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                resetAction(event);
                break;
            case MotionEvent.ACTION_UP:
                //记录 事件位置，在点击事件中可以处理点击事件
                upevent = event;
                upisclick = belongActionArea(upevent);
                eventY2 = event.getY();
                actioncharindex2 = getActionCharIndexOnMotionE(event);
                resetAction(event);
                break;
        }
        return super.onTouchEvent(event);
    }

    boolean isresetting;

    private void resetAction(MotionEvent event) {
        isprepareAction = false;
        if (isclickAction) return;
        if (!isresetting) {
            isresetting = true;
            postDelayed(() -> {
                eventY = 0;
                postInvalidate();
                if (monSlideActionListener != null) {
                    monSlideActionListener.onResetAction();
                }
                isresetting = false;
            }, 300);
        }

    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (!gainFocus) {
            actioncharindex = null;
            invalidate();
        }
    }

    private void moveAction(MotionEvent event) {
        if (isclickAction) return;
        eventY = event.getY();
        invalidate();
        if (monSlideActionListener != null) {
            String newactioncharindex = getActionCharIndexOnMotionE(event);
            if (newactioncharindex != null && !newactioncharindex.equals(actioncharindex)) {
                actioncharindex = newactioncharindex;
                monSlideActionListener.onMoveAction(actioncharindex);
            }
        } else {
            actioncharindex = getActionCharIndexOnMotionE(event);
        }
    }

    private String getActionCharIndexOnMotionE(MotionEvent event) {
        int ini = ((int) ((event.getY() - mzargin / 2) / (textheight + mzargin)));
        if (ini < 0) ini = 0;
        if (ini >= indexstrings.size()) ini = indexstrings.size() - 1;
        return indexstrings.get(ini);
    }

    boolean isclickAction;

//    private void clickAction(MotionEvent event) {
//        isclickAction = true;
//        invalidate();
//        postDelayed(() -> {
//            isclickAction = false;
//            eventY = 0;
//            if (getContext() != null) {
//                invalidate();
//            }
//        }, 1000);
//        eventY = eventY2;
//        actioncharindex = actioncharindex2;
//        if (monSlideActionListener != null) {
//            monSlideActionListener.onClickAction(actioncharindex);
//        }
//    }

    private boolean isprepareAction(MotionEvent event) {
        return isprepareAction;
    }

    boolean isprepareAction;

    private void prepareAction(MotionEvent event) {
        isprepareAction = true;
    }

    private boolean belongActionArea(MotionEvent event) {
        return event.getX() > starttextX && event.getX() < endtextX;
    }


    int rate = 3;
    int rate2 = 4;

    private void drawfloattext(Canvas canvas) {
        if (eventY != 0 && actioncharindex != null) {
            float top = eventY - floattextheight * rate / 2;
            top = top > 0 ? top : 0;
            if (top + +floattextheight * rate > mheight) {
                top = mheight - floattextheight * rate;
            }
            RectF rectF = new RectF(0, top, floattextheight * rate, top + floattextheight * rate);
            canvas.drawRoundRect(rectF, floatFrontSize / 2, floatFrontSize / 2, floatbgPaint);
            Paint.FontMetrics fontMetrics = floattextPaint.getFontMetrics();
            float top1 = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            int baseLineY = (int) (rectF.centerY() - top1 / 2 - bottom / 2);
            canvas.drawText(actioncharindex, rectF.centerX(), baseLineY, floattextPaint);
        }
    }

    private void drawtext(Canvas canvas) {
        int i = 0;
        for (String indexstring : indexstrings) {
            i++;
            if (actioncharindex != null && actioncharindex.equals(indexstring)) {
                canvas.drawText(indexstring, starttextX + textwidth * rate2 / 2, i * (textheight + mzargin), selectedtextPaint);
            } else {
                canvas.drawText(indexstring, starttextX + textwidth * rate2 / 2, i * (textheight + mzargin), textPaint);
            }
        }
    }

    private void drawtextbg(Canvas canvas) {
    }

    int mmargin = ((int) (mContext.getResources().getDisplayMetrics().density * 15));
    int mzargin = ((int) (mContext.getResources().getDisplayMetrics().density * 1));

    private void calviewdata() {
        starttextX = floattextwidth * rate + mmargin;
        endtextX = starttextX + textwidth * rate2;
    }

    int minwidth;
    int minheight;
    int textwidth;
    int textheight;
    int floattextwidth;
    int floattextheight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Rect XRect = new Rect();
        Rect YRect = new Rect();
        textPaint.getTextBounds("S", 0, "s".length(), XRect);
        floattextPaint.getTextBounds("S", 0, "s".length(), YRect);
        textwidth = XRect.width();
        textheight = XRect.height();
        floattextwidth = YRect.width();
        floattextheight = YRect.height();
        int count = 27;
        if (indexstrings != null || indexstrings.size() != 0) count = indexstrings.size();
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            minwidth = YRect.width() * rate + XRect.width() * rate2 + mmargin;
        }
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            minwidth = getMeasuredWidth();
            mmargin = (minwidth - YRect.width() * rate - XRect.width() * rate2);
        }
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            minwidth = getMeasuredWidth();
            mmargin = (minwidth - YRect.width() * rate - XRect.width() * rate2);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            minheight = (XRect.height() + mzargin) * count + mzargin;
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            minheight = getMeasuredHeight();
            mzargin = (minheight - (XRect.height()) * count) / (count + 1);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            minheight = getMeasuredHeight();
            mzargin = (minheight - (XRect.height()) * count) / (count + 1);
        }
        setMeasuredDimension(minwidth, minheight);
    }

    onSlideActionListener monSlideActionListener;

    public onSlideActionListener getonSlideActionListener() {
        return monSlideActionListener;
    }

    public void setonSlideActionListener(onSlideActionListener monSlideActionListener) {
        this.monSlideActionListener = monSlideActionListener;
    }

    public interface onSlideActionListener {
        void onResetAction();

        void onMoveAction(String actioncCharindex);

        void onClickAction(String actioncCharindex);
    }
}
