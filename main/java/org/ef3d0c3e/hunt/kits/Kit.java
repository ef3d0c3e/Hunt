package org.ef3d0c3e.hunt.kits;

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
	abstract ItemStack getDisplayItem();
	/**
	 * Gets Kit's description
	 * @return Kit's description as an array of lines
	 */
	abstract public String[][] getDescription();
	/**
	 * Gets kit's ID
	 * @return Kit's ID
	 */
	abstract public KitID getID();


	/**
	 * Called once when game starts
	 */
	public void start() {}
	/**
	 * Called every time player gets the kit or revives
	 * @param hp Player
	 */
	public void onStart(HuntPlayer hp) {}
	/**
	 * Called when player wins the game with this kit
	 * @param hp Player
	 */
	public void onWin(HuntPlayer hp) {}
	/**
	 * Called when player attempted to kill wrong player
	 * @param hp Player attempting to kill
	 * @param killed Player that would have been killed
	 */
	public void onKillWrong(HuntPlayer hp, HuntPlayer killed) {}
	/**
	 * Called when player dies or lose his kit
	 * @param hp Player that died or lost his kit
	 */
	public void onDeath(HuntPlayer hp) {}

	/**
	 * Called when kit's ownership has been transferred to another player
	 * @param prev Previous owner
	 * @param next New owner
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

	/**
	 * Makes a copy of this kit
	 * @return A copy of this kit
	 */
	abstract public Kit makeCopy();
}
