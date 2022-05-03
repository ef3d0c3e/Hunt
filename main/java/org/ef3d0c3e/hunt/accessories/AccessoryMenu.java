package org.ef3d0c3e.hunt.accessories;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;

public class AccessoryMenu
{
	/* array<accessory> m_list*/
	public Inventory getInventory(final HuntPlayer hp, final int page)
	{
		Inventory inv = Bukkit.createInventory(null, 45, MessageFormat.format("§9Accessoires §8- §c{0}/{1}", page, 10));
		return inv;
	}

	public static class AccessoryMenuEvents implements Listener
	{
	}
}
