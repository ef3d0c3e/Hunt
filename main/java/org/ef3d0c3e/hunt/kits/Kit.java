package org.ef3d0c3e.hunt.kits;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.player.HuntPlayer;

// Abstract class for a kit
public abstract class Kit
{
	protected static final String itemColor = "§a";
	protected static final String itemLoreColor = "§6";

	/**
	 * Sets whether kits can be picked only once or not
	 */
	protected static final boolean singleKitOnly = true;

	/**
	 * Get kit's short name
	 * @return kit's short name (lowercase alphanumerical)
	 */
	abstract public String getName();
	/**
	 * Gets kit's display name
	 * @return Kit display's name (may contain unicode and capitals, but no space)
	 */
	abstract public String getDisplayName();
	/**
	 * Gets kit's display item for GUI
	 * @return Kit's display item
	 */
	public abstract ItemStack getDisplayItem();
	/**
	 * Gets Kit's description
	 * @return Kit's description as an array of lines
	 */
	abstract public String[][] getDescription();

	/**
	 * Called when kit's ownership has been transferred to another player
	 * @param prev Previous owner
	 * @param next New owner, if null, considered to be dead
	 */
	public void changeOwner(HuntPlayer prev, HuntPlayer next) {}

	private static final ItemStack[] emptyItems = new ItemStack[]{};
	/**
	 * Gets the list of all items from this kit
	 * @return An array containing every item in this kit
	 */
	public ItemStack[] getItems() { return emptyItems; };

	/**
	 * Filters item for death chest
	 * @param item Item to check
	 * @return True if item can be put in the death chest, false otherwise
	 */
	public boolean itemFilter(final ItemStack item) { return true; };
}
