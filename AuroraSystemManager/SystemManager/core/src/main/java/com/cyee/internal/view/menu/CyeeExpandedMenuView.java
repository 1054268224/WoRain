package com.cyee.internal.view.menu;

import com.cyee.internal.view.menu.CyeeMenuBuilder.ItemInvoker;
import com.cyee.internal.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class CyeeExpandedMenuView extends ListView implements ItemInvoker, CyeeMenuView, OnItemClickListener {
	private CyeeMenuBuilder mMenu;

	/** Default animations for this menu */
	private int mAnimations;

	/**
	 * Instantiates the CyeeExpandedMenuView that is linked with the provided CyeeMenuBuilder.
	 * 
	 * @param menu
	 *            The model for the menu which this MenuView will display
	 */
	public CyeeExpandedMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);

//		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CyeeMenuView, 0, 0);
//		mAnimations = a.getResourceId(R.styleable.CyeeMenuView_cyeeWindowAnimationStyle, 0);
//		a.recycle();

		setOnItemClickListener(this);
	}

	public void initialize(CyeeMenuBuilder menu) {
		mMenu = menu;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		// Clear the cached bitmaps of children
		setChildrenDrawingCacheEnabled(false);
	}

	public boolean invokeItem(CyeeMenuItemImpl item) {
		return mMenu.performItemAction(item, 0);
	}

	public void onItemClick(AdapterView parent, View v, int position, long id) {
		invokeItem((CyeeMenuItemImpl) getAdapter().getItem(position));
	}

	public int getWindowAnimations() {
		return mAnimations;
	}

}
