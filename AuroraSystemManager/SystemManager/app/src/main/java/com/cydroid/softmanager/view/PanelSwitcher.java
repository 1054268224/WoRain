package com.cydroid.softmanager.view;

import java.util.ArrayList;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.animation.AnimationSwitcher;
import com.cydroid.softmanager.utils.Log;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class PanelSwitcher extends FrameLayout {

    private static final boolean DEBUG = false;

    private static final String TAG = "PanelSwitcher";

    // private final int DURATION = 1;
    private static final int MOVEDISTANCE_VALID = 10;
    private View mScrollLayoutView;

    private int startY = 0;
    private int endY = 0;
    private int oldDiff = 0;
    private int mNewTopMargin = 0;
    private int mTopMargin = 0;
    private int mMoveMaxMargin = 0;
    private int mBottomMargin = 0;
    private int mScreenHeight = 0;

    private boolean isListViewTop = false;
    // private boolean isListViewBottom = false;

    private boolean mMoveAllow = false;
    private OnExpandListener mExpandListener;

    private static final boolean ANIM_DIRECTION_COLLAPSE = true;
    private static final boolean ANIM_DIRECTION_EXPAND = false;
    private static final long FULL_ANIM_TIME = 3 * 1000;
    private static final long SINGLE_MOVE_ANIM_TIME = 500;
    private static final float AUTO_EXPAND_THRESHOLD = 1 / 2;

    public PanelSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNewTopMargin = (int) context.getResources().getDimension(R.dimen.background_height);
    }

    public void setTopFlag(boolean top) {
        isListViewTop = top;
    }

    /*
    public void setBottomFlag(boolean bottom) {
        isListViewBottom = bottom;
    }
    */

    public void setLayoutTopRange(int height, int min, int max) {
        mScreenHeight = height;
        mTopMargin = min;
        mMoveMaxMargin = max;
    }

    public void setLayoutBottomValue(int value) {
        mBottomMargin = value;
    }

    public void setNewLayoutTopValue(int value) {
        mNewTopMargin = value;
    }

    public void setScrollLayout(View view) {
        mScrollLayoutView = view;
        Log.d(TAG, "anchor position y should be:"
                + (mScrollLayoutView.getY() + mScrollLayoutView.getMeasuredHeight()));
    }

    public void enableMoveMode(boolean mode) {
        if (mScrollLayoutView == null)
            return;
        mMoveAllow = mode;
        if (mode) {
            LayoutParams layoutParamFull = new LayoutParams(LayoutParams.MATCH_PARENT,
                    mScreenHeight - mMoveMaxMargin - mBottomMargin);
            layoutParamFull.setMargins(0, mTopMargin, 0, 0);
            mScrollLayoutView.setLayoutParams(layoutParamFull);

        } else {
            LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,
                    mScreenHeight - mTopMargin - mBottomMargin);
            layoutParam.setMargins(0, mTopMargin, 0, 0);
            mScrollLayoutView.setLayoutParams(layoutParam);
            Log.d(TAG, "onExpand mNewTopMargin=" + mNewTopMargin + " mTopMargin=" + mTopMargin
                    + " mMoveMaxMargin=" + mMoveMaxMargin + " mBottomMargin=" + mBottomMargin);
            if (mNewTopMargin != mTopMargin) {
                actionAnimStart(MotionEvent.ACTION_UP, ANIM_DIRECTION_COLLAPSE, mMoveMaxMargin,
                        mMoveMaxMargin - mTopMargin);
            }
        }
        mScrollLayoutView.invalidate();
    }

    public int getNewLayoutTopValue() {
        return mNewTopMargin;
    }

    public void expand() {

    }

    public void collapse() {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (!mMoveAllow) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startY = (int) event.getY();
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            endY = (int) event.getY();
            int dValue = startY - endY;
            if (Math.abs(dValue) > MOVEDISTANCE_VALID) {
                if (dValue > 0) {
                    return onTouchEvent(event);
                } else if (dValue < 0) {
                    if (mNewTopMargin == mMoveMaxMargin) {
                        if (!isListViewTop) {
                            return false;
                        } else {
                            return onTouchEvent(event);
                        }
                    } else return mNewTopMargin == mTopMargin;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mMoveAllow) {
            return false;
        }
        endY = (int) event.getY();

        if (!isValid(event)) {
            return false;
        }

        if (mNewTopMargin == mMoveMaxMargin) {
            if (!isListViewTop) {
                return true;
            }
        }

        int dValue = endY - startY;
        int animationDiff = 0;
        boolean animationDir = ANIM_DIRECTION_EXPAND;

        if (mNewTopMargin == mTopMargin) {
            animationDir = ANIM_DIRECTION_EXPAND;
        } else if (mNewTopMargin == mMoveMaxMargin) {
            animationDir = ANIM_DIRECTION_COLLAPSE;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(DEBUG, TAG, "\n------ACTION_MOVE-------");
                // animationDiff = Math.abs(dValue) < (mTopMargin - mMoveMaxMargin) ? Math.abs(dValue)
                // : (mTopMargin - mMoveMaxMargin);
                animationDiff = Math.min(Math.abs(dValue), mTopMargin - mMoveMaxMargin);
                actionStartMove(animationDir, oldDiff, animationDiff);
                // actionStart(MotionEvent.ACTION_MOVE, animationDir, oldDiff, animationDiff);
                oldDiff = animationDiff;
                break;

            case MotionEvent.ACTION_UP:
                Log.d(DEBUG, TAG, "\n\n++++++ACTION_UP++++++");
                int margin = touchUp(dValue);
                animationDiff = mNewTopMargin - margin;
                // actionStart(MotionEvent.ACTION_UP, animationDir, oldDiff, animationDiff);
                actionAnimStart(MotionEvent.ACTION_UP, animationDir, oldDiff, animationDiff);
                break;

            default:
                break;
        }

        return true;
    }

    private boolean isValid(MotionEvent event) {

        boolean flag = true;
        if (mNewTopMargin == mTopMargin
                && (startY < mTopMargin || startY > (mScreenHeight - mBottomMargin))) {
            flag = false;
        }

        if (mNewTopMargin == mMoveMaxMargin && endY < mMoveMaxMargin) {
            flag = false;
        }

        if (mNewTopMargin == mMoveMaxMargin && (endY - startY) < 0
                && (event.getAction() != MotionEvent.ACTION_UP)) {
            flag = false;
        }

        if (mNewTopMargin == mTopMargin && (endY - startY) > 0
                && (event.getAction() != MotionEvent.ACTION_UP)) {
            flag = false;
        }

        return flag;
    }

    private float resetStartScale(int top) {
        float scale = 1.0f;
        if (top == mTopMargin) {
            scale = 1.0f;
        } else if (top == 0) {
            scale = 0.0f;
        }
        return scale;
    }

    private int touchMove(int dValue) {
        int newLocation = 0;
        int margin = mNewTopMargin + dValue;

        if (dValue > 0) {
            if (mNewTopMargin == mMoveMaxMargin) {
                newLocation = margin > mTopMargin ? mTopMargin : margin;
            } else if (mNewTopMargin == mTopMargin) {
                newLocation = mTopMargin;
            }
        } else if (dValue < 0) {
            if (mNewTopMargin == mMoveMaxMargin) {
                newLocation = mNewTopMargin;
            } else if (mNewTopMargin == mTopMargin) {
                newLocation = margin < mMoveMaxMargin ? mMoveMaxMargin : margin;
            }
        } else if (dValue == 0) {
            if (mNewTopMargin == mMoveMaxMargin) {
                newLocation = mNewTopMargin > mTopMargin ? mTopMargin : mNewTopMargin;
            } else if (mNewTopMargin == mTopMargin) {
                newLocation = mNewTopMargin <= 0 ? 0 : mNewTopMargin;
            }
        }

        return newLocation;
    }

    private int touchUp(int dValue) {
        int margin = 0;
        // int threshold = (int) (1 / AUTO_EXPAND_THRESHOLD);
        if (mNewTopMargin == mMoveMaxMargin) {
            // if (dValue * threshold > (mTopMargin - mMoveMaxMargin)) {
            if (dValue * 2 > (mTopMargin - mMoveMaxMargin)) {
                margin = mTopMargin;
            } else {
                margin = mMoveMaxMargin;
            }
        } else if (mNewTopMargin == mTopMargin) {
            // if (dValue * threshold * (-1) > (mTopMargin - mMoveMaxMargin)) {
            if (dValue * 2 * (-1) > (mTopMargin - mMoveMaxMargin)) {
                margin = mMoveMaxMargin;
            } else {
                margin = mTopMargin;
            }
        }

        return margin;
    }

    public void move(int top) {
        mScrollLayoutView.setTranslationY(top - mTopMargin);
        /*
        LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParam.setMargins(0, top, 0, mBottomMargin);
        mScrollLayoutView.setLayoutParams(layoutParam);
        */
        updateListener(top);

    }

    private void updateListener(int top) {
        if (mExpandListener != null) {
            float ratio = (float) Math.abs(top - mTopMargin) / (float) Math.abs(mMoveMaxMargin - mTopMargin);
            Log.d(DEBUG, TAG, "onExpand mNewTopMargin=" + top + " mTopMargin=" + mTopMargin
                    + " mMoveMaxMargin=" + mMoveMaxMargin + " ration=" + ratio);
            if (ratio < 0)
                ratio = 0;
            else if (ratio > 1)
                ratio = 1;
            mExpandListener.onExpand(ratio);
        }
    }

    private void actionStartMove(boolean dir, int oldDis, int newDis) {
        Log.d(DEBUG, TAG, "oldDis :" + oldDis + " ,newDis :" + newDis);
        int finalDis = Math.abs(newDis);
        int symbol = dir ? 1 : -1;
        int newLocation = touchMove(symbol * finalDis);
        move(newLocation);
        // sendMessage(MotionEvent.ACTION_MOVE, symbol * finalDis, dir, false);
    }

    private void actionAnimStart(final int action, final boolean dir, final int oldDis, final int newDis) {
        Log.d(DEBUG, TAG, "oldDis :" + oldDis + " ,newDis :" + newDis + " dir:" + dir);
        float desTop = 0;
        if (dir == ANIM_DIRECTION_EXPAND) {
            desTop = -newDis;
            mNewTopMargin = mTopMargin - newDis;
        } else {
            desTop = -(newDis + (mTopMargin - mMoveMaxMargin));
            mNewTopMargin = mMoveMaxMargin - newDis;
        }
        oldDiff = 0;
        int diff = Math.abs((Math.abs(newDis) - Math.abs(oldDis)));
        long duration = diff * FULL_ANIM_TIME / (mTopMargin - mMoveMaxMargin) / 10;
        ObjectAnimator anim = ObjectAnimator
                .ofFloat(mScrollLayoutView, "translationY", mScrollLayoutView.getTranslationY(), desTop)
                .setDuration(duration);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                updateListener((int) (mTopMargin + cVal));
            }
        });
        anim.start();
    }

    /*
    private void actionStart(final int action, final boolean dir, final int oldDis, final int newDis) {
        Log.d(DEBUG, TAG, "oldDis :" + oldDis + " ,newDis :" + newDis);
    
        final int finalDis = Math.abs(newDis);
        new Thread(new Runnable() {
    
            @Override
            public void run() {
                // TODO Auto-generated method stub
    
                sleep(action, 50);
    
                int i = oldDis;
                boolean isEnd = false;
                int symbol = 1;
                if (dir == ANIM_DIRECTION_EXPAND)
                    symbol = -1;
                int threshold = initThreshold(Math.abs(oldDis - finalDis));
                Log.d(DEBUG, TAG,
                        getAction(action) + "  i :" + i + " ,oldDis :" + oldDis + " ,finalDis :" + finalDis);
                if (oldDis < finalDis) {
                    while (i < finalDis) {
                        sendMessage(action, symbol * i, dir, isEnd);
                        i += threshold;
                        if (i > finalDis) {
                            sendMessage(action, symbol * (finalDis - 1), dir, isEnd);
                            break;
                        }
                    }
                } else if (oldDis > finalDis) {
                    while (i >= finalDis) {
                        sendMessage(action, symbol * i, dir, isEnd);
                        i -= threshold;
                        if (i < finalDis) {
                            sendMessage(action, symbol * finalDis, dir, isEnd);
                            break;
                        }
                    }
                }
    
                if (action == MotionEvent.ACTION_UP) {
                    Log.d(DEBUG, TAG,
                            "===before===mNewTopMargin :" + mNewTopMargin + " ,oldDiff :" + oldDiff);
                    isEnd = true;
                    if (dir) {
                        sendMessage(action, mMoveMaxMargin - newDis, dir, isEnd);
                    } else {
                        sendMessage(action, mTopMargin - newDis, dir, isEnd);
                    }
                }
            }
    
            private int initThreshold(int dis) {
                // int threshold = 2;
                // return threshold;
                return (mTopMargin - mMoveMaxMargin) / 100;
            }
    
            private void sleep(int action, long time) {
                if (action == MotionEvent.ACTION_UP) {
                    Log.d(DEBUG, TAG, "=======ACTION_UP");
                    try {
                        Thread.sleep(time);
                    } catch (Exception ex) {
    
                    }
                }
            }
        }).start();
    }
    
    private void sendMessage(int action, int diff, boolean dir, boolean flag) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt("diff", diff);
        b.putInt("action", action);
        b.putBoolean("dir", dir);
        b.putBoolean("flag", flag);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
    
    Handler mHandler = new Handler() {
    
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int value = b.getInt("diff");
            int action = b.getInt("action");
            boolean dir = b.getBoolean("dir");
            boolean flag = b.getBoolean("flag");
            if (flag) {
                mNewTopMargin = value;
                oldDiff = 0;
                Log.d(DEBUG, TAG, "\n\n===after===mNewTopMargin :" + mNewTopMargin + " ,oldDiff :" + oldDiff);
                Log.d(DEBUG, TAG, "\n\n---------------------\n\n");
            } else {
                int newLocation = touchMove(value);
                move(newLocation);
                Log.d(DEBUG, TAG, getAction(action) + " , yDiff :" + Math.abs(value));
            }
        }
    };
    */

    private String getAction(int action) {
        return action == 2 ? "MotionEvent.ACTION_MOVE" : "MotionEvent.ACTION_UP";
    }

    public void setOnExpandListener(OnExpandListener listener) {
        mExpandListener = listener;
    }

    public interface OnExpandListener {
        void onExpand(float ratio);
    }
}
