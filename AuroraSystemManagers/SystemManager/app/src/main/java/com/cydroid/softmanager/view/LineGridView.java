package com.cydroid.softmanager.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class LineGridView extends GridView {

    public LineGridView(Context context) {
        super(context);
    }

    public LineGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        View localView1 = getChildAt(0);
        // here, column = 2, childCount = 6
        int column = getWidth() / localView1.getWidth();
        int childCount = getChildCount();
        Paint localPaint = new Paint();
        localPaint.setStyle(Paint.Style.STROKE);
        // localPaint.setColor(Color.RED);
        int grey = Color.parseColor("#d8d8d8");
        int white = Color.parseColor("#ffffff");
        for (int i = 0; i < childCount; i++) {
            View cellView = getChildAt(i);
            if ((i + 1) % column == 0) {
                localPaint.setColor(grey);
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(),
                        cellView.getBottom(), localPaint);
                localPaint.setColor(white);
                canvas.drawLine(cellView.getLeft(), cellView.getBottom() + 1, cellView.getRight(),
                        cellView.getBottom() + 1, localPaint);
            } else if ((i + 1) > (childCount - (childCount % column))) {
                canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(),
                        cellView.getBottom(), localPaint);
            } else {
                localPaint.setColor(grey);
                canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(),
                        cellView.getBottom(), localPaint);
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(),
                        cellView.getBottom(), localPaint);
                localPaint.setColor(white);
                canvas.drawLine(cellView.getRight() + 1, cellView.getTop(), cellView.getRight() + 1,
                        cellView.getBottom(), localPaint);
                canvas.drawLine(cellView.getLeft(), cellView.getBottom() + 1, cellView.getRight(),
                        cellView.getBottom() + 1, localPaint);
            }
        }
        if (childCount % column != 0) {
            for (int j = 0; j < (column - childCount % column); j++) {
                View lastView = getChildAt(childCount - 1);
                canvas.drawLine(lastView.getRight() + lastView.getWidth() * j, lastView.getTop(),
                        lastView.getRight() + lastView.getWidth() * j, lastView.getBottom(), localPaint);
            }
        }
    }

}