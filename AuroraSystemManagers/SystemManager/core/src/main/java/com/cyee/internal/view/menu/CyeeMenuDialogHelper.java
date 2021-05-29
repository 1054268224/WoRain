package com.cyee.internal.view.menu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;

public class CyeeMenuDialogHelper implements DialogInterface.OnKeyListener, DialogInterface.OnClickListener,
		DialogInterface.OnDismissListener, CyeeMenuPresenter.Callback {
	private final CyeeMenuBuilder mMenu;
	private CyeeAlertDialog mDialog;
	CyeeListMenuPresenter mPresenter;
	private Fragment mFragment;
	private CyeeMenuPresenter.Callback mPresenterCallback;

	public CyeeMenuDialogHelper(CyeeMenuBuilder menu) {
		mMenu = menu;
	}

	/**
	 * Shows menu as a dialog.
	 * 
	 * @param windowToken
	 *            Optional token to assign to the window.
	 */
	public void show(IBinder windowToken) {
		// Many references to mMenu, create local reference
		final CyeeMenuBuilder menu = mMenu;

		// Get the builder for the dialog
		final CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(menu.getContext());

		mPresenter = new CyeeListMenuPresenter(builder.getContext(),
				com.cyee.internal.R.layout.cyee_list_menu_item_layout);

		mPresenter.setCallback(this);
		mMenu.addMenuPresenter(mPresenter);
		builder.setAdapter(mPresenter.getAdapter(), this);

		// Set the title
		final View headerView = menu.getHeaderView();
		if (headerView != null) {
			// Menu's client has given a custom header view, use it
			builder.setCustomTitle(headerView);
		} else {
			// Otherwise use the (text) title and icon
			builder.setIcon(menu.getHeaderIcon()).setTitle(menu.getHeaderTitle());
		}

		// Set the key listener
		builder.setOnKeyListener(this);

		// Show the menu
		mDialog = builder.create();
		mDialog.setOnDismissListener(this);

		WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		if (windowToken != null) {
			lp.token = windowToken;
		}
		lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

		mDialog.show();
	}

	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
				Window win = mDialog.getWindow();
				if (win != null) {
					View decor = win.getDecorView();
					if (decor != null) {
						KeyEvent.DispatcherState ds = decor.getKeyDispatcherState();
						if (ds != null) {
							ds.startTracking(event, this);
							return true;
						}
					}
				}
			} else if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
				Window win = mDialog.getWindow();
				if (win != null) {
					View decor = win.getDecorView();
					if (decor != null) {
						KeyEvent.DispatcherState ds = decor.getKeyDispatcherState();
						if (ds != null && ds.isTracking(event)) {
							mMenu.close(true);
							dialog.dismiss();
							return true;
						}
					}
				}
			}
		}

// Menu shortcut matching
		return mMenu.performShortcut(keyCode, event, 0);

	}

	public void setPresenterCallback(CyeeMenuPresenter.Callback cb) {
		mPresenterCallback = cb;
	}

	/**
	 * Dismisses the menu's dialog.
	 * 
	 * @see Dialog#dismiss()
	 */
	public void dismiss() {
       // Gionee <zhangxx> <2014-08-13> modify for CR01347435 begin
       boolean mIsActivityFinish = false;
       Context context = mMenu.getContext();
       if (context instanceof CyeeActivity) {
           CyeeActivity activity = (CyeeActivity) context;
           if (activity != null) {
               mIsActivityFinish = activity.isFinishing() || activity.isDestroyed();
           }
       }
		if (mDialog != null && !mIsActivityFinish) {
          // Gionee <zhangxx> <2014-08-13> modify for CR01347435 end
			mDialog.dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mPresenter.onCloseMenu(mMenu, true);
	}

	@Override
	public void onCloseMenu(CyeeMenuBuilder menu, boolean allMenusAreClosing) {
		if (allMenusAreClosing || menu == mMenu) {
			dismiss();
		}
		if (mPresenterCallback != null) {
			mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
		}
		
		CyeeActivity activity = (CyeeActivity) mMenu.getContext();
		activity.onContextMenuClosed(mMenu);
	}

	@Override
	public boolean onOpenSubMenu(CyeeMenuBuilder subMenu) {
		if (mPresenterCallback != null) {
			return mPresenterCallback.onOpenSubMenu(subMenu);
		}
		return false;
	}

	public void onClick(DialogInterface dialog, int which) {
//		mMenu.performItemAction((CyeeMenuItemImpl) mPresenter.getAdapter().getItem(which), 0);
        Context context = mMenu.getContext();
	    
        CyeeMenuItemImpl menuItem = (CyeeMenuItemImpl) mPresenter.getAdapter().getItem(which);

        Intent intent = menuItem.getIntent();
        if (intent != null) {
            context.startActivity(intent);
            return;
        }

        MenuItem.OnMenuItemClickListener clickListener = menuItem.getMenuItemClickListener();
        if (clickListener == null || !clickListener.onMenuItemClick(menuItem)) {
            if (mFragment!=null) {
                mFragment.onContextItemSelected(menuItem);    
            }else{
                ((CyeeActivity)context).onContextItemSelected(menuItem);
            }
        }
    }
	
	public void setFragment(Fragment fragment){
        mFragment = fragment;
    }
}
