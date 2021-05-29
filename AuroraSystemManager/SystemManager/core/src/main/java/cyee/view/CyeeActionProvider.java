package cyee.view;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ActionProvider;
import androidx.fragment.app.Fragment;

import com.cyee.utils.Log;

public abstract class CyeeActionProvider {
	private static final String TAG = "ActionProvider";
	private SubUiVisibilityListener mSubUiVisibilityListener;
	private VisibilityListener mVisibilityListener;

	/**
	 * Creates a new instance. ActionProvider classes should always implement a constructor that takes a
	 * single Context parameter for inflating from menu XML.
	 * 
	 * @param context
	 *            Context for accessing resources.
	 */
	public CyeeActionProvider(Context context) {
	}

	/**
	 * Factory method called by the Android framework to create new action views.
	 * 
	 * <p>
	 * This method has been deprecated in favor of {@link #onCreateActionView(CyeeMenuItem)}. Newer apps that wish
	 * to support platform versions prior to API 16 should also implement this method to return a valid action
	 * view.
	 * </p>
	 * 
	 * @return A new action view.
	 * 
	 * @deprecated use {@link #onCreateActionView(CyeeMenuItem)}
	 */
	public abstract View onCreateActionView();

	/**
	 * Factory method called by the Android framework to create new action views. This method returns a new
	 * action view for the given CyeeMenuItem.
	 * 
	 * <p>
	 * If your ActionProvider implementation overrides the deprecated no-argument overload
	 * {@link #onCreateActionView()}, overriding this method for devices running API 16 or later is
	 * recommended but optional. The default implementation calls {@link #onCreateActionView()} for
	 * compatibility with applications written for older platform versions.
	 * </p>
	 * 
	 * @param forItem
	 *            CyeeMenuItem to create the action view for
	 * @return the new action view
	 */
	public View onCreateActionView(CyeeMenuItem forItem) {
		return onCreateActionView();
	}

	/**
	 * The result of this method determines whether or not {@link #isVisible()} will be used by the
	 * {@link CyeeMenuItem} this ActionProvider is bound to help determine its visibility.
	 * 
	 * @return true if this ActionProvider overrides the visibility of the CyeeMenuItem it is bound to, false
	 *         otherwise. The default implementation returns false.
	 * @see #isVisible()
	 */
	public boolean overridesItemVisibility() {
		return false;
	}

	/**
	 * If {@link #overridesItemVisibility()} returns true, the return value of this method will help determine
	 * the visibility of the {@link CyeeMenuItem} this ActionProvider is bound to.
	 * 
	 * <p>
	 * If the CyeeMenuItem's visibility is explicitly set to false by the application, the CyeeMenuItem will not be
	 * shown, even if this method returns true.
	 * </p>
	 * 
	 * @return true if the CyeeMenuItem this ActionProvider is bound to is visible, false if it is invisible. The
	 *         default implementation returns true.
	 */
	public boolean isVisible() {
		return true;
	}

	/**
	 * If this ActionProvider is associated with an item in a menu, refresh the visibility of the item based
	 * on {@link #overridesItemVisibility()} and {@link #isVisible()}. If {@link #overridesItemVisibility()}
	 * returns false, this call will have no effect.
	 */
	public void refreshVisibility() {
		if (mVisibilityListener != null && overridesItemVisibility()) {
			mVisibilityListener.onActionProviderVisibilityChanged(isVisible());
		}
	}

	/**
	 * Performs an optional default action.
	 * <p>
	 * For the case of an action provider placed in a menu item not shown as an action this method is invoked
	 * if previous callbacks for processing menu selection has handled the event.
	 * </p>
	 * <p>
	 * A menu item selection is processed in the following order:
	 * <ul>
	 * <li>
	 * Receiving a call to {@link CyeeMenuItem.OnMenuItemClickListener#onMenuItemClick
	 * CyeeMenuItem.OnMenuItemClickListener.onMenuItemClick}.</li>
	 * <li>
	 * Receiving a call to {@link AppCompatActivity#onOptionsItemSelected(CyeeMenuItem)
	 * Activity.onOptionsItemSelected(CyeeMenuItem)}</li>
	 * <li>
	 * Receiving a call to {@link Fragment#onOptionsItemSelected(CyeeMenuItem)
	 * Fragment.onOptionsItemSelected(CyeeMenuItem)}</li>
	 * <li>
	 * Launching the {@link android.content.Intent} set via {@link CyeeMenuItem#setIntent(android.content.Intent)
	 * CyeeMenuItem.setIntent(android.content.Intent)}</li>
	 * <li>
	 * Invoking this method.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The default implementation does not perform any action and returns false.
	 * </p>
	 */
	public boolean onPerformDefaultAction() {
		return false;
	}

	/**
	 * Determines if this ActionProvider has a CyeeSubMenu associated with it.
	 * 
	 * <p>
	 * Associated submenus will be shown when an action view is not. This provider instance will receive a
	 * call to {@link #onPrepareSubMenu(CyeeSubMenu)} after the call to {@link #onPerformDefaultAction()} and
	 * before a CyeeSubMenu is displayed to the user.
	 * 
	 * @return true if the item backed by this provider should have an associated CyeeSubMenu
	 */
	public boolean hasSubMenu() {
		return false;
	}

	/**
	 * Called to prepare an associated CyeeSubMenu for the menu item backed by this ActionProvider.
	 * 
	 * <p>
	 * if {@link #hasSubMenu()} returns true, this method will be called when the menu item is selected to
	 * prepare the CyeeSubMenu for presentation to the user. Apps may use this to create or alter
	 * CyeeSubMenu content right before display.
	 * 
	 * @param CyeeSubMenu
	 *            CyeeSubMenu that will be displayed
	 */
	public void onPrepareSubMenu(CyeeSubMenu CyeeSubMenu) {
	}

	/**
	 * Notify the system that the visibility of an action view's sub-UI such as an anchored popup has changed.
	 * This will affect how other system visibility notifications occur.
	 * 
	 * @hide Pending future API approval
	 */
	public void subUiVisibilityChanged(boolean isVisible) {
		if (mSubUiVisibilityListener != null) {
			mSubUiVisibilityListener.onSubUiVisibilityChanged(isVisible);
		}
	}

	/**
	 * @hide Internal use only
	 */
	public void setSubUiVisibilityListener(SubUiVisibilityListener listener) {
		mSubUiVisibilityListener = listener;
	}

	/**
	 * Set a listener to be notified when this ActionProvider's overridden visibility changes. This should
	 * only be used by CyeeMenuItem implementations.
	 * 
	 * @param listener
	 *            listener to set
	 */
	public void setVisibilityListener(VisibilityListener listener) {
		if (mVisibilityListener != null) {
			Log.w(TAG, "setVisibilityListener: Setting a new ActionProvider.VisibilityListener "
					+ "when one is already set. Are you reusing this " + getClass().getSimpleName()
					+ " instance while it is still in use somewhere else?");
		}
		mVisibilityListener = listener;
	}

	/**
	 * @hide Internal use only
	 */
	public interface SubUiVisibilityListener {
		void onSubUiVisibilityChanged(boolean isVisible);
	}

	/**
	 * Listens to changes in visibility as reported by {@link ActionProvider#refreshVisibility()}.
	 * 
	 * @see ActionProvider#overridesItemVisibility()
	 * @see ActionProvider#isVisible()
	 */
	public interface VisibilityListener {
		void onActionProviderVisibilityChanged(boolean isVisible);
	}

}
