package com.cyee.internal.view;

import com.cyee.internal.R;
import cyee.widget.CyeeWidgetResource;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.view.ViewConfiguration;

/**
 * Allows components to query for various configuration policy decisions about how the action bar should lay
 * out and behave on the current device.
 */
public class CyeeActionBarPolicy {
	private final Context mContext;

	public static CyeeActionBarPolicy get(Context context) {
		return new CyeeActionBarPolicy(context);
	}

	private CyeeActionBarPolicy(Context context) {
		mContext = context;
	}

	public int getMaxActionButtons() {
//		return mContext.getResources().getInteger(R.integer.max_action_buttons);
		return 0;
	}

	public boolean showsOverflowMenuButton() {
//		return !ViewConfiguration.get(mContext).hasPermanentMenuKey();
		return false;
	}

	public int getEmbeddedMenuWidthLimit() {
//		return mContext.getResources().getDisplayMetrics().widthPixels / 2;
		return 0;
	}

	public boolean hasEmbeddedTabs() {
//		final int targetSdk = mContext.getApplicationInfo().targetSdkVersion;
//		if (targetSdk >= Build.VERSION_CODES.JELLY_BEAN) {
			return mContext.getResources().getBoolean(com.cyee.internal.R.bool.cyee_action_bar_embed_tabs);
//		}
//
//		// The embedded tabs policy changed in Jellybean; give older apps the old policy
//		// so they get what they expect.
//		return mContext.getResources().getBoolean(R.bool.action_bar_embed_tabs_pre_jb);
//		return false;
	}

    public int getTabContainerHeight() {
        TypedArray a = mContext.obtainStyledAttributes(null, R.styleable.CyeeActionBar,
                com.cyee.internal.R.attr.cyeeactionBarStyle, 0);
        // shaozj begin
        //int height = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeeheight, 0);
        int height = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeetabheight, 0);
        if(0 == height){
            height = a.getLayoutDimension(R.styleable.CyeeActionBar_cyeeheight, 0);
        }
        // shaozj end
	    a.recycle();
        return height;
    }

	public boolean enableHomeButtonByDefault() {
		// Older apps get the home button interaction enabled by default.
		// Newer apps need to enable it explicitly.
//		return mContext.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
		return false;
	}

	public int getStackedTabMaxWidth() {
		return mContext.getResources().getDimensionPixelSize(com.cyee.internal.R.dimen.cyee_action_bar_stacked_tab_max_width);
	}
}
