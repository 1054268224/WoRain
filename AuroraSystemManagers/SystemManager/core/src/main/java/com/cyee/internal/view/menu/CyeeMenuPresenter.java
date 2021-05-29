package com.cyee.internal.view.menu;

import android.content.Context;
import android.os.Parcelable;
import android.view.Menu;
import android.view.ViewGroup;

public interface CyeeMenuPresenter {
	/**
	 * Called by menu implementation to notify another component of open/close events.
	 */
    interface Callback {
		/**
		 * Called when a menu is closing.
		 * 
		 * @param menu
		 * @param allMenusAreClosing
		 */
        void onCloseMenu(CyeeMenuBuilder menu, boolean allMenusAreClosing);

		/**
		 * Called when a submenu opens. Useful for notifying the application of menu state so that it does not
		 * attempt to hide the action bar while a submenu is open or similar.
		 * 
		 * @param subMenu
		 *            Submenu currently being opened
		 * @return true if the Callback will handle presenting the submenu, false if the presenter should
		 *         attempt to do so.
		 */
        boolean onOpenSubMenu(CyeeMenuBuilder subMenu);
	}

	/**
	 * Initialize this presenter for the given context and menu. This method is called by CyeeMenuBuilder when a
	 * presenter is added. See {@link CyeeMenuBuilder#addMenuPresenter(MenuPresenter)}
	 * 
	 * @param context
	 *            Context for this presenter; used for view creation and resource management
	 * @param menu
	 *            Menu to host
	 */
    void initForMenu(Context context, CyeeMenuBuilder menu);

	/**
	 * Retrieve a MenuView to display the menu specified in {@link #initForMenu(Context, Menu)}.
	 * 
	 * @param root
	 *            Intended parent of the MenuView.
	 * @return A freshly created MenuView.
	 */
    CyeeMenuView getMenuView(ViewGroup root);

	/**
	 * Update the menu UI in response to a change. Called by CyeeMenuBuilder during the normal course of
	 * operation.
	 * 
	 * @param cleared
	 *            true if the menu was entirely cleared
	 */
    void updateMenuView(boolean cleared);

	/**
	 * Set a callback object that will be notified of menu events related to this specific presentation.
	 * 
	 * @param cb
	 *            Callback that will be notified of future events
	 */
    void setCallback(Callback cb);

	/**
	 * Called by Menu implementations to indicate that a submenu item has been selected. An active Callback
	 * should be notified, and if applicable the presenter should present the submenu.
	 * 
	 * @param subMenu
	 *            SubMenu being opened
	 * @return true if the the event was handled, false otherwise.
	 */
    boolean onSubMenuSelected(CyeeSubMenuBuilder subMenu);

	/**
	 * Called by Menu implementations to indicate that a menu or submenu is closing. Presenter implementations
	 * should close the representation of the menu indicated as necessary and notify a registered callback.
	 * 
	 * @param menu
	 *            Menu or submenu that is closing.
	 * @param allMenusAreClosing
	 *            True if all associated menus are closing.
	 */
    void onCloseMenu(CyeeMenuBuilder menu, boolean allMenusAreClosing);

	/**
	 * Called by Menu implementations to flag items that will be shown as actions.
	 * 
	 * @return true if this presenter changed the action status of any items.
	 */
    boolean flagActionItems();

	/**
	 * Called when a menu item with a collapsable action view should expand its action view.
	 * 
	 * @param menu
	 *            Menu containing the item to be expanded
	 * @param item
	 *            Item to be expanded
	 * @return true if this presenter expanded the action view, false otherwise.
	 */
    boolean expandItemActionView(CyeeMenuBuilder menu, CyeeMenuItemImpl item);

	/**
	 * Called when a menu item with a collapsable action view should collapse its action view.
	 * 
	 * @param menu
	 *            Menu containing the item to be collapsed
	 * @param item
	 *            Item to be collapsed
	 * @return true if this presenter collapsed the action view, false otherwise.
	 */
    boolean collapseItemActionView(CyeeMenuBuilder menu, CyeeMenuItemImpl item);

	/**
	 * Returns an ID for determining how to save/restore instance state.
	 * 
	 * @return a valid ID value.
	 */
    int getId();

	/**
	 * Returns a Parcelable describing the current state of the presenter. It will be passed to the
	 * {@link #onRestoreInstanceState(Parcelable)} method of the presenter sharing the same ID later.
	 * 
	 * @return The saved instance state
	 */
    Parcelable onSaveInstanceState();

	/**
	 * Supplies the previously saved instance state to be restored.
	 * 
	 * @param state
	 *            The previously saved instance state
	 */
    void onRestoreInstanceState(Parcelable state);

}
