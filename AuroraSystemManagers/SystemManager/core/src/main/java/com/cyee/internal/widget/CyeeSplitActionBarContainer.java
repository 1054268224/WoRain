package com.cyee.internal.widget;

import com.cyee.internal.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CyeeSplitActionBarContainer extends FrameLayout {
	private final Drawable mBackground;
	private final Context mContext;
	private final int mContainerHeight;

	public CyeeSplitActionBarContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CyeeActionBar);
		mBackground = a.getDrawable(R.styleable.CyeeActionBar_cyeebackground);
		mContainerHeight = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeeheight, 0);
		a.recycle();
		setBackgroundDrawable(mBackground);
	}

}
