/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.cyee.internal.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import cyee.changecolors.ChameleonColorManager;

public class CyeeTabIndicator extends LinearLayout {
    // Gionee <weidong> <2016-04-22> modify for CR01683201 begin
    private static final int ACTIONBAR_TAB_INDICATOR_BOTTOM_PADDING = 0;
    // Gionee <weidong> <2016-04-22> modify for CR01683201 end
    private static final int ACTIONBAR_TAB_INDICATOR_HEIGHT = 2;
    private final int mSelectedUnderlineThickness;
    private final Paint mSelectedUnderlinePaint;

    private int mIndexForSelection;
    private float mSelectionOffset;
    private final int mSelectedUnderlineBottomPadding;
    private int mIndicatorColor;
    private Drawable mIndicatorDrawable;
    
    private final Context mContext;

    public CyeeTabIndicator(Context context) {
        this(context, null);
    }

    public CyeeTabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.actionBarTabBarStyle);

    }

    public CyeeTabIndicator(Context context, AttributeSet attrs,
            int defaultStyle) {
        super(context, attrs, defaultStyle);
        final Resources res = context.getResources();
        mContext = context;
        mSelectedUnderlineBottomPadding = (int) (ACTIONBAR_TAB_INDICATOR_BOTTOM_PADDING * res
                .getDisplayMetrics().density);
        mIndicatorColor = res.getColor(com.cyee.internal.R.color.cyee_actionbar_title_color_dark);
        int backgroundColor = res.getColor(android.R.color.transparent);
        mSelectedUnderlinePaint = new Paint();

        if (!isActionBarOverlay()) {
            if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
                Resources iCyeeRes = mContext.getResources();
                mIndicatorDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_tab_indicator);
            } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                mIndicatorColor = ChameleonColorManager
                        .getContentColorPrimaryOnAppbar_T1();
            }
        }
        int height = -1;
        if (null != mIndicatorDrawable) {
            height = mIndicatorDrawable.getIntrinsicHeight();
        }
        if (height == -1) {
            height = (int) (ACTIONBAR_TAB_INDICATOR_HEIGHT * res
                    .getDisplayMetrics().density);
        }
        mSelectedUnderlineThickness = height;
        
        mSelectedUnderlinePaint.setColor(mIndicatorColor);
        setBackgroundColor(backgroundColor);
        setWillNotDraw(false);
    }
 
    /**
     * Notifies this view that view pager has been scrolled. We save the tab
     * index and selection offset for interpolating the position and width of
     * selection underline.
     */
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
        mIndexForSelection = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int childCount = getChildCount();

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mIndexForSelection);
            int selectedLeft = selectedTitle.getLeft();
            int selectedRight = selectedTitle.getRight();
            final boolean isRtl = isRtl();
            final boolean hasNextTab = isRtl ? mIndexForSelection > 0
                    : (mIndexForSelection < (getChildCount() - 1));
            if ((mSelectionOffset > 0.0f) && hasNextTab) {
                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mIndexForSelection
                        + (isRtl ? -1 : 1));
                int nextLeft = nextTitle.getLeft();
                int nextRight = nextTitle.getRight();

                selectedLeft = (int) (mSelectionOffset * nextLeft + (1.0f - mSelectionOffset)
                        * selectedLeft);
                selectedRight = (int) (mSelectionOffset * nextRight + (1.0f - mSelectionOffset)
                        * selectedRight);
            }

            int height = getHeight();
            //Chenyee <Cyee_Widget> hushengsong 2018-08-15 modify for p style begin
            int indicatorOffset = (selectedRight - selectedLeft)/6;
            //Chenyee <Cyee_Widget> hushengsong 2018-08-15 modify for p style end
            if (null == mIndicatorDrawable) {
                //Chenyee <Cyee_Widget> hushengsong 2018-08-15 modify for p style begin
                canvas.drawRect(selectedLeft + indicatorOffset, height
                        - mSelectedUnderlineThickness
                        - mSelectedUnderlineBottomPadding, selectedRight - indicatorOffset,
                        height - mSelectedUnderlineBottomPadding,
                        mSelectedUnderlinePaint);
                //Chenyee <Cyee_Widget> hushengsong 2018-08-15 modify for p style end
            } else {
                //Chenyee <Cyee_Widget> hushengsong 2018-08-15 modify for p style begin
                mIndicatorDrawable.setBounds(selectedLeft + indicatorOffset, height
                        - mSelectedUnderlineThickness
                        - mSelectedUnderlineBottomPadding, selectedRight - indicatorOffset,
                        height - mSelectedUnderlineBottomPadding);
                //Chenyee <Cyee_Widget> hushengsong 2018-08-15 modify for p style end
                mIndicatorDrawable.draw(canvas);
            }
            
        }
    }

    private boolean isRtl() {
        return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public void setIndicatorBackgroundColor(int color) {
        mIndicatorColor = color;

        if (!isActionBarOverlay()) {
            if (ChameleonColorManager.getInstance().getCyeeThemeType(mContext) == ChameleonColorManager.CyeeThemeType.GLOBAL_THEME) {
                Resources iCyeeRes = mContext.getResources();
                mIndicatorDrawable = iCyeeRes.getDrawable(com.cyee.internal.R.drawable.cyee_global_theme_actionbar_tab_indicator);

            } else if (ChameleonColorManager.isNeedChangeColor(mContext)) {
                mIndicatorColor = ChameleonColorManager
                        .getContentColorPrimaryOnAppbar_T1();
            }
        }
        mSelectedUnderlinePaint.setColor(mIndicatorColor);
    }
    
    private boolean isActionBarOverlay() {
        if (mContext instanceof AppCompatActivity) {
            return ((AppCompatActivity) mContext).getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        }
        return false;
    }
}
