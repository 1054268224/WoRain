//Gionee <jianghuan> <2013-11-25> add for CR00975553 begin
package com.cydroid.softmanager.trafficassistant.viewToolbar;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.utils.Log;

import android.content.Context;
import android.graphics.Color;
import android.renderscript.Sampler.Value;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class TitleViewToolbar extends ViewToolbar implements OnClickListener {

    private static final int STR_SET = R.string.text;

    public static final int ACTION_SET = 0;
    public static final int ACTION_POI = 1;
    public static final int ACTION_CANCER = 2;

    public static final int FLAG_SET = 0x00000001;
    public static final int FLAG_POI = 0x00000010;
    public static final int FLAG_CANCER = 0x00000100;

    private int mFlag = FLAG_SET;

    private int mScreenX;
    private int mScreenY;
    private final int mLineHeight = 0;

    private TextView mItemSet;
    // private TextView mItemPoi;
    // private TextView mItemCancel;

    // protected TextView mTextView;
    private int mLeft;
    private int mTop;

    private static TitleViewToolbar sInstance = null;

    public TitleViewToolbar(Context context) {
        super(context, 0, 0);
        initToolbarItem();
    }

    public TitleViewToolbar(Context context, int left, int top) {
        super(context, left, top);
        initToolbarItem();
        // mLeft = left;
        // mTop = top;
    }

    public void setPosition(int left, int top) {
        // TODO Auto-generated method stub
        mLeft = left;
        mTop = top;
    }

    /**
     * called to return singleton
     * 
     * @param hostView
     * @return
     */
    public static TitleViewToolbar getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TitleViewToolbar(context);
        }
        return sInstance;
    }

    /**
     * called to specify current text view for showing tool bar
     * 
     * @param obj
     *            current text view to specify
     */
    /*
     * public void setCurrentTarget(TextView obj) { mTextView = obj; }
     */

    protected void initToolbarItem() {
        mItemSet = initToolbarItem(ACTION_SET, STR_SET);
        // mItemPoi = initToolbarItem(ACTION_POI, STR_POI);
        // mItemCancel = initToolbarItem(ACTION_CANCER, STR_CANCER);
    }

    public void show() {
        if (!mShowing) {
            calculatePopupPosition();
            // int start = mTextView.getSelectionStart();
            // int end = mTextView.getSelectionEnd();
            showInternal(mScreenX, mScreenY, mLineHeight, false);
        }
    }

    public void move() {
        if (mShowing) {
            calculatePopupPosition();
            // int start = mTextView.getSelectionStart();
            // int end = mTextView.getSelectionEnd();
            moveInternal(mScreenX, mScreenY, mLineHeight, false);
        }
    }

    /*
     * @Deprecated private void calculateScreenPosition() { int[] location = new
     * int[2]; mTextView.getLocationOnScreen(location); int start =
     * mTextView.getSelectionStart(); int end = mTextView.getSelectionEnd();
     * Layout layout = mTextView.getLayout(); if (layout == null ) { layout =
     * mTextView.getLayout(); } int line = layout.getLineForOffset(start); int
     * top = layout.getLineTop(line); int bottom = layout.getLineBottom(line);
     * mLineHeight = bottom - top; mScreenY = top + mLineHeight / 2 +
     * location[1] + mTextView.getTotalPaddingTop() - mTextView.getScrollY(); if
     * (start == end) { mScreenX =
     * Math.round(layout.getPrimaryHorizontal(start)) + location[0] +
     * mTextView.getTotalPaddingLeft() - mTextView.getScrollX(); } else { int
     * left = Math.round(layout.getPrimaryHorizontal(start)); int right; int
     * lineEnd = layout.getLineForOffset(end); if (line == lineEnd) { right =
     * Math.round(layout.getPrimaryHorizontal(end)); } else { right =
     * Math.round(layout.getLineRight(line)); } mScreenX = (left + right) / 2 +
     * location[0] + mTextView.getTotalPaddingLeft() - mTextView.getScrollX(); }
     * mScreenY = Math.max(location[1], mScreenY); }
     */

    private void calculatePopupPosition() {
        // int[] location = new int[2];
        // mHostView.getLocationOnScreen(location);

        final int x = mLeft;// location[0];
        final int y = mTop;// location[1];

        // final int height = mHostView.getMeasuredHeight();
        // final int width = mHostView.getMeasuredWidth();

        Log.d("Gaoj", "   x    " + x + "   y   " + y);
        mScreenX = x;// (width>>1) + x;// + mTextView.getTotalPaddingLeft() -
                     // mTextView.getScrollX();
        mScreenY = y;// + 50;// + mTextView.getTotalPaddingTop() -
                     // mTextView.getScrollY();
    }

    protected TextView initToolbarItem(int id, int textResId) {
        TextView textView = new TextView(mContext);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setId(id);
        textView.setPadding(mToolbarItemPaddingLeftAndRight, 0, mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(textResId);
        textView.setOnClickListener(this);
        return textView;
    }

    public void setOptionItemFlag(int flag) {
        mFlag = flag;
    }

    public int getOptionItemFlag() {
        return mFlag;
    }

    @Override
    protected void updateToolbarItems() {
        // TODO Auto-generated method stub

        mToolbarGroup.removeAllViews();
        // construct toolbar.
        if (mFlag == FLAG_SET) {
            mToolbarGroup.addView(mItemSet);
        } /*
          * else if (mFlag == FLAG_POI){
          * mItemPoi.setBackgroundResource(R.drawable.gn_text_toolbar_left);
          * mToolbarGroup.addView(mItemPoi); }
          * mToolbarGroup.addView(mItemCancel);
          */
    }

    public void setOnToolBarItemClickListener(ToolBarItemClickListener l) {
        // mItemClickListener = l;
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        hide();
        // if(mItemClickListener != null && mTextView != null) {
        // mItemClickListener.onAcitonClick(mTextView,v.getId());
        // }
    }

    // private ToolBarItemClickListener mItemClickListener = null;
    public interface ToolBarItemClickListener {
        /**
         * callback to caller of this interface
         * 
         * @param targetView
         *            operation target view ,as current textview settled by caller
         * @param action
         *            action id of user cliked
         */
        void onAcitonClick(View targetView, int action);
    }

    public void setText(String value) {
        // TODO Auto-generated method stub
        mItemSet.setTextSize(12);
        mItemSet.setText(value);
    }
}
// Gionee <jianghuan> <2013-11-25> add for CR00975553 end