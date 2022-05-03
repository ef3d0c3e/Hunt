package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.items.HuntItems;

public class KitMenu implements Listener
{
	static Kit m_kitList[];
	static String m_name = " §l §l §l §l §l §l §l §l §l §l §l §l §l §9§lKits";
	static Inventory m_inv;

	/**
	 * Gets menu's name
	 * @return Menu's name
	 */
	public static String getMenuName()
	{
		return m_name;
	}

	/**
	 * Initializes kit list & kit menu
	 */
	public static void init()
	{
		m_kitList = new Kit[]
		{
			new KitEsteban(),
			new KitMehdi(),
			new KitJb(),
			new KitBaptiste(),
			new KitLino(),
			new KitJulien(),
			new KitKelian(),
			new KitThomas(),
			new KitEnzo(),
			new KitTom(),
			new KitFlavien(),
			new KitBk(),
			new KitLanczos(),
			//new KitHasagi(),
		};

		m_inv = Bukkit.createInventory(null, 18, m_name);
		for (int i = 0; i < m_kitList.length; ++i)
			m_inv.setItem(i, m_kitList[i].getDisplayItem());
	}

	/**
	 * Gets list of kits
	 * @return List of kits
	 */
	public static Kit[] getList()
	{
		return m_kitList;
	}

	/**
	 * Gets kit menu's inventory
	 * @return Kit menu's inventory
	 */
	public static Inventory getInventory()
	{
		return m_inv;
	}

	/**
	 * Processes player's click in kit menu
	 * @param ev Event
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev)
	{
		if (!Game.isKitMode())
			return;
		if (!ev.getView().getTitle().equals(m_name))
			return;
		ev.setCancelled(true);
		if (ev.getCurrentItem() == null || ev.getCurrentItem().getType() == Material.AIR)
			return;

		if (ev.getCurrentItem().getType() == Material.BARRIER)
			return;

		if (ev.getRawSlot() >= m_kitList.length)
			return;
		final Kit kit = m_kitList[ev.getRawSlot()];
		HuntPlayer hp = Game.getPlayer(ev.getWhoClicked().getName());
		
		hp.setKit(kit.makeCopy());
		hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 65536.f, 1.4f);
	}

	/**
	 * Prevents dragging
	 * @param ev Event
	 */
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent ev)
	{
		if (!Game.isKitMode())
			return;
		if (!ev.getView().getTitle().equals(m_name))
			return;

		ev.setCancelled(true);
	}


	/**
	 * Opens menu on right-click with item
	 * @param ev Event
	 */
	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (Game.hasStarted() || !Game.isKitMode())
			return;
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null || !ev.getItem().isSimilar(HuntItems.getKitSelector()))
			return;

		ev.setCancelled(true);

		ev.getPlayer().openInventory(getInventory());
	}

	/**
	 * Sets kit to taken or not
	 * @param kit The kit
	 * @param taken Whether the kit is taken or not
	 * @note If ```Kit.singleKitOnly``` is false, this has no effect
	 */
	public static void setTaken(final Kit kit, boolean taken)
	{
		if (!Kit.singleKitOnly)
			return;

		for (ItemStack item : m_inv)
		{
			if (item == null || !item.getItemMeta().getDisplayName().equals(kit.getDisplayItem().getItemMeta().getDisplayName()))
				continue;

			if (taken)
				item.setType(Material.BARRIER);
			else
				item.setType(kit.getDisplayItem().getType());

			break;
		}
	}
}
