package cyee.view;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;

public interface CyeeSubMenu extends CyeeMenu {
	/**
	 * Sets the CyeeSubMenu header's title to the title given in <var>titleRes</var> resource identifier.
	 * 
	 * @param titleRes
	 *            The string resource identifier used for the title.
	 * @return This CyeeSubMenu so additional setters can be called.
	 */
    CyeeSubMenu setHeaderTitle(int titleRes);

	/**
	 * Sets the CyeeSubMenu header's title to the title given in <var>title</var>.
	 * 
	 * @param title
	 *            The character sequence used for the title.
	 * @return This CyeeSubMenu so additional setters can be called.
	 */
    CyeeSubMenu setHeaderTitle(CharSequence title);

	/**
	 * Sets the CyeeSubMenu header's icon to the icon given in <var>iconRes</var> resource id.
	 * 
	 * @param iconRes
	 *            The resource identifier used for the icon.
	 * @return This CyeeSubMenu so additional setters can be called.
	 */
    CyeeSubMenu setHeaderIcon(int iconRes);

	/**
	 * Sets the CyeeSubMenu header's icon to the icon given in <var>icon</var> {@link Drawable}.
	 * 
	 * @param icon
	 *            The {@link Drawable} used for the icon.
	 * @return This CyeeSubMenu so additional setters can be called.
	 */
    CyeeSubMenu setHeaderIcon(Drawable icon);

	/**
	 * Sets the header of the CyeeSubMenu to the {@link View} given in <var>view</var>. This replaces the header
	 * title and icon (and those replace this).
	 * 
	 * @param view
	 *            The {@link View} used for the header.
	 * @return This CyeeSubMenu so additional setters can be called.
	 */
    CyeeSubMenu setHeaderView(View view);

	/**
	 * Clears the header of the CyeeSubMenu.
	 */
    void clearHeader();

	/**
	 * Change the icon associated with this CyeeSubMenu's item in its parent menu.
	 * 
	 * @see MenuItem#setIcon(int)
	 * @param iconRes
	 *            The new icon (as a resource ID) to be displayed.
	 * @return This CyeeSubMenu so additional setters can be called.
	 */
    CyeeSubMenu setIcon(int iconRes);

	/**
	 * Change the icon associated with this CyeeSubMenu's item in its parent menu.
	 * 
	 * @see MenuItem#setIcon(Drawable)
	 * @param icon
	 *            The new icon (as a Drawable) to be displayed.
	 * @return This CyeeSubMenu so additional setters can be called.
	 */
    CyeeSubMenu setIcon(Drawable icon);

	/**
	 * Gets the {@link MenuItem} that represents this CyeeSubMenu in the parent menu. Use this for setting
	 * additional item attributes.
	 * 
	 * @return The {@link MenuItem} that launches the CyeeSubMenu when invoked.
	 */
    CyeeMenuItem getItem();

}
