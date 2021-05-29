package com.cyee.internal.widget;

import android.view.MotionEvent;

public interface MultiChoiceScrollListener {
    boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
}